# Backend Dockerfile
FROM gradle:8.5-jdk17 AS builder

WORKDIR /app

COPY build.gradle.kts settings.gradle.kts ./
COPY src ./src

RUN gradle clean build --no-daemon

FROM eclipse-temurin:17-jre-alpine

RUN apk add --no-cache \
    wireguard-tools \
    iptables \
    ip6tables \
    iproute2 \
    bash \
    curl

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

# Create directories
RUN mkdir -p /etc/wireguard /etc/singbox /var/log/wg-singbox /data

# Set permissions
RUN chmod 755 /data /etc/wireguard /etc/singbox /var/log/wg-singbox

EXPOSE 8080 51820/udp

# Give the container NET_ADMIN capability at runtime
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
