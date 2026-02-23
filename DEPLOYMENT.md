# Deployment Guide

This guide covers various deployment scenarios for WG Singbox Gateway.

## Table of Contents

- [Prerequisites](#prerequisites)
- [Docker Deployment](#docker-deployment)
- [Kubernetes Deployment](#kubernetes-deployment)
- [Manual Installation](#manual-installation)
- [Security Considerations](#security-considerations)
- [Troubleshooting](#troubleshooting)

## Prerequisites

- Linux server with kernel 3.10+ (for WireGuard support)
- Docker 20.10+ and Docker Compose 2.0+
- 512MB RAM minimum, 1GB recommended
- 1GB disk space minimum

## Docker Deployment

### Quick Start

```bash
git clone https://github.com/sersb/wg-singbox-gateway.git
cd wg-singbox-gateway
docker-compose up -d
```

### Environment Configuration

Create `.env` file:

```bash
# Database
DB_PATH=/data/wg-singbox.db

# JWT Secret (generate with: openssl rand -base64 32)
JWT_SECRET=your-secret-key-min-256-bits

# WireGuard
WG_CONFIG_PATH=/etc/wireguard
WG_LISTEN_PORT=51820
WG_ADDRESS=10.0.0.1/24
WG_MTU=1280

# Singbox
SINGBOX_CONFIG_PATH=/etc/singbox/config.json

# Logging
LOG_LEVEL=INFO
```

### Persistent Data

By default, Docker Compose creates named volumes for:
- `wg-data` - Database
- `wg-config` - WireGuard configuration
- `singbox-config` - Singbox configuration
- `logs` - Application logs

### Updating

```bash
docker-compose pull
docker-compose up -d
```

### Backing Up

```bash
# Backup volumes
docker run --rm -v wg-data:/data -v $(pwd):/backup alpine tar czf /backup/wg-data-backup.tar.gz -C /data .
docker run --rm -v wg-config:/config -v $(pwd):/backup alpine tar czf /backup/wg-config-backup.tar.gz -C /config .
```

## Kubernetes Deployment

### Prerequisites

- Kubernetes cluster 1.20+
- kubectl configured
- StorageClass configured for PVCs
- LoadBalancer or Ingress controller

### Deployment Steps

1. Update `deploy/kubernetes/deployment.yaml`:
   - Set resource limits
   - Configure environment variables
   - Adjust storage class

2. Apply manifests:
```bash
kubectl apply -f deploy/kubernetes/
```

3. Get service IP:
```bash
kubectl get svc wg-singbox-gateway
```

### Scaling

For high availability, deploy multiple backend replicas with a shared database:

```yaml
replicas: 3
```

Note: Only one pod should manage WireGuard at a time. Use a leader election mechanism.

## Manual Installation

### System Requirements

- Ubuntu 20.04+ or similar
- OpenJDK 17+
- Node.js 20+
- WireGuard tools
- Singbox binary

### Install Dependencies

```bash
# Update system
sudo apt update
sudo apt install -y openjdk-17-jre nodejs npm wireguard iptables

# Install Singbox
wget https://github.com/SagerNet/sing-box/releases/download/v1.10.3/sing-box_1.10.3_linux_amd64.deb
sudo dpkg -i sing-box_1.10.3_linux_amd64.deb

# Clone repository
git clone https://github.com/sersb/wg-singbox-gateway.git
cd wg-singbox-gateway
```

### Build Backend

```bash
cd backend
./gradlew clean build
java -jar build/libs/wg-singbox-gateway-1.0.0.jar
```

### Build Frontend

```bash
cd frontend
npm install
npm run build
```

Serve built files with nginx or similar web server.

### Systemd Service

Create `/etc/systemd/system/wg-singbox.service`:

```ini
[Unit]
Description=WG Singbox Gateway
After=network.target

[Service]
Type=simple
User=wgsingbox
WorkingDirectory=/opt/wg-singbox-gateway
ExecStart=/usr/bin/java -jar /opt/wg-singbox-gateway/backend/build/libs/wg-singbox-gateway-1.0.0.jar
Restart=on-failure
RestartSec=5

[Install]
WantedBy=multi-user.target
```

Enable and start:
```bash
sudo systemctl enable wg-singbox
sudo systemctl start wg-singbox
```

## Security Considerations

### Network Security

1. Use firewall to limit access:
```bash
# Allow only WireGuard, HTTP, and HTTPS
sudo ufw allow 51820/udp
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw enable
```

2. Change default JWT secret

3. Use strong passwords for users

4. Enable HTTPS in production (use reverse proxy like nginx)

### Data Protection

1. Regular backups of SQLite database
2. Secure storage of WireGuard keys
3. Encrypted database at rest (for production)

### WireGuard Security

1. Generate strong keys:
```bash
wg genkey | tee privatekey | wg pubkey > publickey
```

2. Use pre-shared keys for additional security

3. Limit peers to necessary IPs

### Singbox Security

1. Validate TLS certificates
2. Use secure cipher suites
3. Keep Singbox updated

## Troubleshooting

### WireGuard Interface Won't Start

```bash
# Check logs
docker logs wg-singbox-backend

# Verify WireGuard tools
docker exec -it wg-singbox-backend wg --version

# Check interface
docker exec -it wg-singbox-backend ip link show wg0
```

### Singbox Connection Issues

```bash
# Check Singbox config
docker exec -it wg-singbox-backend cat /etc/singbox/config.json

# Validate config
docker exec -it wg-singbox-backend sing-box check -c /etc/singbox/config.json

# Test proxy manually
curl --proxy socks5://127.0.0.1:2080 https://google.com
```

### Database Issues

```bash
# Check database file
docker exec -it wg-singbox-backend ls -la /data/

# Backup before repair
docker cp wg-singbox-backend:/data/wg-singbox.db ./backup.db

# Repair SQLite database
sqlite3 ./backup.db "PRAGMA integrity_check;"
```

### Performance Issues

```bash
# Monitor resources
docker stats wg-singbox-backend

# Check system load
docker exec -it wg-singbox-backend top
```

### Getting Logs

```bash
# Application logs
docker logs -f wg-singbox-backend

# Follow logs
docker logs --tail=100 -f wg-singbox-backend

# Check service logs
docker exec -it wg-singbox-backend cat /var/log/wg-singbox/application.log
```

## Monitoring

### Health Checks

```bash
# Application health
curl http://localhost:8080/actuator/health

# Prometheus metrics
curl http://localhost:8080/actuator/prometheus
```

### Metrics

Key metrics to monitor:
- `wireguard_status` - WireGuard interface status
- `singbox_status` - Singbox process status
- `connected_peers` - Number of active peers
- `traffic_bytes` - Network traffic
- `system_cpu`, `system_memory` - System resources

## Maintenance

### Regular Tasks

1. **Weekly**: Review logs for errors
2. **Monthly**: Update dependencies
3. **Quarterly**: Rotate encryption keys
4. **As needed**: Backup before major changes

### Updates

```bash
# Docker
docker-compose pull
docker-compose up -d

# Manual
cd backend
git pull
./gradlew clean build
systemctl restart wg-singbox
```

## Support

For additional help:
- Check [GitHub Issues](https://github.com/sersb/wg-singbox-gateway/issues)
- Review [README.md](README.md)
