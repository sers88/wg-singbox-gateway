# DockerHub Repository

This repository contains automated Docker image builds for wg-singbox-gateway.

## Quick Start

```bash
# Backend
docker pull sers88/wg-singbox-gateway:backend
docker run -d \
  --name wg-singbox-backend \
  -p 8080:8080 \
  -p 51820:51820/udp \
  -v wg-data:/data \
  -v wg-config:/etc/wireguard \
  sers88/wg-singbox-gateway:backend

# Frontend
docker pull sers88/wg-singbox-gateway:frontend
docker run -d \
  --name wg-singbox-frontend \
  -p 80:80 \
  sers88/wg-singbox-gateway:frontend

# Or with docker compose
docker pull sers88/wg-singbox-gateway:latest
docker compose up -d
```

## Configuration

The application uses environment variables for configuration. See the main repository for more details on available options.

### Required Environment Variables

| Variable | Description |
|----------|-------------|
| `DB_PATH` | SQLite database path |
| `JWT_SECRET` | JWT signing secret |
| `WG_CONFIG_PATH` | WireGuard config directory |
| `SINGBOX_CONFIG_PATH` | Singbox config path |

### Optional Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `LOG_LEVEL` | Application log level | `INFO` |
| `WG_LISTEN_PORT` | WireGuard listen port | `51820` |
| `WG_ADDRESS` | WireGuard address | `10.0.0.1/24` |
| `WG_MTU` | WireGuard MTU | `1280` |

## Features

- **WireGuard Management**
  - Interface configuration
  - Peer management
  - QR code generation for mobile clients
  - Real-time status monitoring

- **Singbox Proxy**
  - Support for Trojan, VLESS, VMess, Shadowsocks
  - Multiple proxy configurations
  - Connection testing

- **Routing**
  - Domain-based routing
  - IP CIDR rules
  - GeoSite categories

- **System**
  - Real-time status via WebSocket
  - Configuration backup and restore
  - System monitoring
  - User authentication

## Architecture

```
┌─────────────────┐         WireGuard (wg0)         ┌─────────────────┐
│   Web UI      │◄─────────────────────────────────►│  Nginx          │
│  (Vue.js)    │                              │  (Frontend)     │
│               │                              │                  │
│               │  ┌─────────────┐              │                  │
│               │  │ Spring Boot │              │                  │
│               │  │ (Backend)   │◄─────────────────►│                  │
│               │  └─────────────┘              │                  │
└───────────────┘                              │                  │
                                                     │  ┌─────────────┐ │
                                                     │  │ Singbox     │ │
                                                     │◄─────────────┤ │
                                                     │              │      │
                                          Internet            │      │
                                          │      │
                                                     └───────────────┘
```

## Support

For issues and questions, please open an issue on GitHub.

## License

MIT License

## Links

- [GitHub Repository](https://github.com/sers88/wg-singbox-gateway)
- [Docker Hub](https://hub.docker.com/r/sers88/wg-singbox-gateway)
