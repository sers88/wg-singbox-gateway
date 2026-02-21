ARG VERSION=dev
ARG SINGBOX_VERSION=1.10.3

# Build stage
FROM golang:1.21-alpine AS builder

SHELL ["/bin/sh", "-c", "-o", "pipefail"]

ARG VERSION=dev
ARG SINGBOX_VERSION=1.10.3
ENV VERSION=${VERSION}

RUN apk add --no-cache git

WORKDIR /build

# Copy go.mod and download dependencies
COPY go.mod go.sum* ./
RUN go mod download -mod=mod && go mod verify

# Copy source code
COPY . .

# Build the application
RUN CGO_ENABLED=0 GOOS=linux GOARCH=amd64 go build -ldflags="-s -w" -o gateway ./cmd/gateway

# Final stage
FROM alpine:3.19

ARG SINGBOX_VERSION=1.10.3

# Install WireGuard and required tools
RUN apk add --no-cache \
    wireguard-tools \
    iptables \
    ip6tables \
    ca-certificates \
    tzdata \
    curl \
    wget \
    bash \
    sed

# Install sing-box
RUN SINGBOX_VERSION=${SINGBOX_VERSION:-1.10.3} && \
    curl -fsSL "https://github.com/SagerNet/sing-box/releases/download/v${SINGBOX_VERSION}/sing-box-${SINGBOX_VERSION}-linux-amd64.tar.gz" | tar -xz && \
    mv sing-box-${SINGBOX_VERSION}-linux-amd64/sing-box /usr/local/bin/ && \
    chmod +x /usr/local/bin/sing-box && \
    rm -rf sing-box-${SINGBOX_VERSION}-linux-amd64

WORKDIR /app

# Copy binary from builder
COPY --from=builder /build/gateway /app/gateway

# Copy entrypoint script
COPY entrypoint.sh /app/entrypoint.sh
RUN chmod +x /app/entrypoint.sh

# Create directories for configs
RUN mkdir -p /etc/wg-singbox /etc/wireguard /etc/singbox

# Enable IPv4 forwarding
RUN echo "net.ipv4.ip_forward=1" > /etc/sysctl.d/99-sysctl.conf && \
    echo "net.ipv6.conf.all.forwarding=1" >> /etc/sysctl.d/99-sysctl.conf && \
    echo "net.ipv4.conf.all.rp_filter=0" >> /etc/sysctl.d/99-sysctl.conf

# Expose WireGuard port
EXPOSE 51820/udp

# Default environment variables (can be overridden)
ENV WG_LISTEN_PORT=51820
ENV WG_ADDRESS=10.0.0.1/24
ENV WG_MTU=1280
ENV SINGBOX_LOG_LEVEL=info
ENV SINGBOX_VERSION=1.10.3

# Volume mounts for custom configs
VOLUME ["/etc/wg-singbox", "/etc/singbox"]

# Healthcheck
HEALTHCHECK --interval=30s --timeout=10s --start-period=10s --retries=3 \
    CMD wg show || exit 1

ENTRYPOINT ["/app/entrypoint.sh"]
