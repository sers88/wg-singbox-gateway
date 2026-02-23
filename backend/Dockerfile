# Multi-stage Dockerfile for Spring Boot Backend
# Supports multi-arch builds (amd64, arm64)

# Builder stage using Gradle image
FROM gradle:8.8-jdk21-alpine AS builder

WORKDIR /app

# Copy build files first for better caching
COPY build.gradle.kts settings.gradle.kts ./

# Copy source code
COPY src ./src

# Build application (gradle is pre-installed in this image)
RUN gradle clean build -x test --no-daemon

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

# Install runtime dependencies
RUN apk add --no-cache \
    wireguard-tools \
    iptables \
    iproute2 \
    ca-certificates \
    curl \
    bash

WORKDIR /app

# Create directories
RUN mkdir -p /etc/wireguard /etc/singbox /var/log/wg-singbox /app/data

# Copy built JAR
COPY --from=builder /app/build/libs/*.jar app.jar

# Expose ports
EXPOSE 8080 51820/udp

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]
