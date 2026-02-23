# WG Singbox Gateway

Web UI for managing WireGuard and Singbox VPN gateway.

## Features

- **WireGuard Management**
  - Configure WireGuard interface (listen port, MTU, address)
  - Manage WireGuard peers
  - Generate QR codes for mobile clients
  - Real-time status monitoring

- **Singbox Proxy Configuration**
  - Support for multiple proxy types: Trojan, VLESS, VMess, Shadowsocks
  - Multiple proxy configurations with priority
  - Test proxy connections

- **Routing Rules**
  - Domain-based routing
  - IP CIDR rules
  - GeoSite categories
  - Custom outbound selection (proxy/direct/block)

- **System Features**
  - Real-time status updates via WebSocket
  - Configuration backup and restore
  - System monitoring (CPU, memory, disk usage)
  - User authentication and authorization

## Tech Stack

**Backend:**
- Spring Boot 3.2 + Kotlin
- SQLite database
- Spring Security + JWT
- WebSocket for real-time updates
- Process execution for WireGuard/Singbox control

**Frontend:**
- Vue 3 + TypeScript
- Vite
- Element Plus UI framework
- Pinia for state management
- Vue Router

## Getting Started

### Prerequisites

- Docker and Docker Compose
- Or JDK 17+ and Node.js 20+ for local development

### Quick Start with Docker

1. Clone the repository:
```bash
git clone <repository-url>
cd wg-singbox-gateway
```

2. Start the services:
```bash
docker-compose up -d
```

3. Access the web UI:
```
http://localhost
```

Default credentials:
- Username: `admin`
- Password: `admin123`

**Important:** Change the default password after first login!

### Local Development

**Backend:**
```bash
cd backend
./gradlew bootRun
```

**Frontend:**
```bash
cd frontend
npm install
npm run dev
```

The backend will run on `http://localhost:8080` and the frontend on `http://localhost:3000`.

## Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `DB_PATH` | SQLite database path | `./data/wg-singbox.db` |
| `JWT_SECRET` | JWT signing secret | (change in production) |
| `WG_CONFIG_PATH` | WireGuard config directory | `/etc/wireguard` |
| `SINGBOX_CONFIG_PATH` | Singbox config path | `/etc/singbox/config.json` |
| `LOG_LEVEL` | Application log level | `INFO` |

### WireGuard Setup

1. Generate keys:
```bash
wg genkey | tee private.key | wg pubkey > public.key
```

2. Add keys to the configuration via web UI

3. Create peers and scan QR codes with WireGuard mobile app

### Singbox Setup

1. Get proxy server details (type, server, port, credentials)

2. Add proxy configuration via web UI

3. Configure routing rules for domain/IP/GeoSite filtering

## API Endpoints

### Authentication
- `POST /api/auth/login` - Login
- `POST /api/auth/logout` - Logout
- `GET /api/auth/me` - Get current user

### WireGuard
- `GET /api/wireguard/config` - Get configuration
- `PUT /api/wireguard/config` - Update configuration
- `POST /api/wireguard/start` - Start interface
- `POST /api/wireguard/stop` - Stop interface
- `GET /api/wireguard/peers` - List peers
- `POST /api/wireguard/peers` - Create peer
- `GET /api/wireguard/peers/{id}/qrcode` - Get QR code

### Proxy
- `GET /api/proxy/configs` - List proxy configurations
- `POST /api/proxy/configs` - Create proxy
- `PUT /api/proxy/configs/{id}` - Update proxy
- `DELETE /api/proxy/configs/{id}` - Delete proxy

### Routing
- `GET /api/routing/rules` - List routing rules
- `POST /api/routing/rules` - Create rule
- `PUT /api/routing/rules/{id}` - Update rule
- `DELETE /api/routing/rules/{id}` - Delete rule

### System
- `GET /api/system/status` - Get system status
- `POST /api/system/backup` - Create backup
- `POST /api/system/backups/{id}/restore` - Restore backup

## Deployment

### Docker

```bash
docker-compose up -d
```

### Kubernetes

Apply the manifests:
```bash
kubectl apply -f deploy/kubernetes/
```

## Contributing

Contributions are welcome! Please read the contributing guidelines first.

## License

MIT License

## Support

For issues and questions, please open an issue on GitHub.
