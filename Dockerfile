# Build stage
FROM golang:1.21-alpine AS builder

RUN apk add --no-cache git

WORKDIR /build

# Copy go.mod and download dependencies
COPY go.mod go.sum* ./
RUN go mod download

# Copy source code
COPY . .

# Build the application
RUN CGO_ENABLED=0 GOOS=linux GOARCH=amd64 go build \
    -ldflags="-s -w -X main.version=${VERSION:-dev}" \
    -o gateway ./cmd/gateway

# Final stage
FROM alpine:3.19

# Install WireGuard and sing-box dependencies
RUN apk add --no-cache \
    wireguard-tools \
    iptables \
    ip6tables \
    ca-certificates \
    tzdata \
    curl

# Install sing-box
RUN curl -fsSL https://github.com/SagerNet/sing-box/releases/download/v1.10.0/sing-box-1.10.0-linux-amd64.tar.gz | tar -xz && \
    mv sing-box-1.10.0-linux-amd64/sing-box /usr/local/bin/ && \
    rm -rf sing-box-1.10.0-linux-amd64

WORKDIR /app

# Copy binary from builder
COPY --from=builder /build/gateway /app/gateway

# Create directories for configs
RUN mkdir -p /etc/wg-singbox /etc/wireguard

# Copy example configs (optional - can be mounted as volumes)
COPY configs/singbox.json /etc/singbox/config.json.example
COPY configs/config.yaml /etc/wg-singbox/config.yaml.example

# Enable IPv4 forwarding
RUN echo "net.ipv4.ip_forward=1" > /etc/sysctl.d/99-sysctl.conf && \
    echo "net.ipv6.conf.all.forwarding=1" >> /etc/sysctl.d/99-sysctl.conf

# Expose WireGuard port
EXPOSE 51820/udp

# Required for WireGuard and TUN device
ENV WG_CONFIG_PATH=/etc/wg-singbox/config.yaml

ENTRYPOINT ["/app/gateway"]
