# API Documentation

This document describes the REST API endpoints for WG Singbox Gateway.

**Base URL**: `http://localhost:8080/api`

**Authentication**: Bearer token in Authorization header

```
Authorization: Bearer <token>
```

---

## Authentication Endpoints

### Login
Authenticate user and receive JWT token.

```http
POST /auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}
```

**Response**:
```json
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "...",
    "user": {
      "id": 1,
      "username": "admin",
      "email": null,
      "role": "ADMIN",
      "lastLogin": "2024-01-01T12:00:00"
    },
  "message": null,
  "errors": null
}
```

### Logout
Invalidate current session.

```http
POST /auth/logout
Authorization: Bearer <token>
```

### Get Current User
Get authenticated user information.

```http
GET /auth/me
Authorization: Bearer <token>
```

### Change Password
Change current user's password.

```http
POST /auth/change-password
Authorization: Bearer <token>
Content-Type: application/json

{
  "currentPassword": "oldpass",
  "newPassword": "newpass123"
}
```

---

## WireGuard Endpoints

### Get Configuration
Retrieve current WireGuard interface configuration.

```http
GET /wireguard/config
Authorization: Bearer <token>
```

**Response**:
```json
{
  "success": true,
  "data": {
    "id": 1,
    "privateKey": "...",
    "publicKey": "...",
    "listenPort": 51820,
    "address": "10.0.0.1/24",
    "mtu": 1280,
    "postUp": "iptables -t nat -A PREROUTING -i wg0 -j REDIRECT --to-ports 2080",
    "postDown": null,
    "enabled": true,
    "interfaceName": "wg0",
    "status": "RUNNING",
    "createdAt": "2024-01-01T12:00:00",
    "updatedAt": "2024-01-01T12:00:00"
  }
}
```

### Update Configuration
Update WireGuard interface configuration.

```http
PUT /wireguard/config
Authorization: Bearer <token>
Content-Type: application/json

{
  "privateKey": "...",
  "publicKey": "...",
  "listenPort": 51820,
  "address": "10.0.0.1/24",
  "mtu": 1280,
  "postUp": "iptables -t nat -A PREROUTING -i wg0 -j REDIRECT --to-ports 2080",
  "postDown": null,
  "enabled": true,
  "interfaceName": "wg0"
}
```

### Get Status
Get WireGuard interface status and peer information.

```http
GET /wireguard/status
Authorization: Bearer <token>
```

### Start Interface
Start WireGuard interface.

```http
POST /wireguard/start
Authorization: Bearer <token>
```

### Stop Interface
Stop WireGuard interface.

```http
POST /wireguard/stop
Authorization: Bearer <token>
```

### List Peers
Get all WireGuard peers.

```http
GET /wireguard/peers
Authorization: Bearer <token>
```

**Response**:
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "publicKey": "...",
      "presharedKey": "...",
      "allowedIps": "10.0.0.2/32",
      "name": "Client 1",
      "deviceType": "android",
      "endpointIp": null,
      "endpointPort": null,
      "persistentKeepalive": 25,
      "lastHandshake": "2024-01-01T12:00:00",
      "transferRx": 1024000,
      "transferTx": 512000,
      "enabled": true,
      "createdAt": "2024-01-01T12:00:00",
      "updatedAt": "2024-01-01T12:00:00"
    }
  ]
}
```

### Create Peer
Create a new WireGuard peer.

```http
POST /wireguard/peers
Authorization: Bearer <token>
Content-Type: application/json

{
  "publicKey": "...",
  "presharedKey": "...",
  "allowedIps": "10.0.0.2/32",
  "name": "Client 1",
  "deviceType": "android",
  "persistentKeepalive": 25,
  "enabled": true
}
```

### Update Peer
Update existing peer.

```http
PUT /wireguard/peers/{id}
Authorization: Bearer <token>
Content-Type: application/json

{
  "publicKey": "...",
  "allowedIps": "10.0.0.2/32",
  "name": "Client 1",
  "enabled": true
}
```

### Delete Peer
Delete a peer.

```http
DELETE /wireguard/peers/{id}
Authorization: Bearer <token>
```

### Toggle Peer
Enable or disable a peer.

```http
POST /wireguard/peers/{id}/toggle
Authorization: Bearer <token>
```

### Get Peer QR Code
Get QR code for mobile WireGuard client.

```http
GET /wireguard/peers/{id}/qrcode?serverEndpoint=example.com
Authorization: Bearer <token>
```

Returns WireGuard configuration text for QR code generation.

---

## Proxy Endpoints

### List Proxy Configurations
Get all proxy configurations.

```http
GET /proxy/configs
Authorization: Bearer <token>
```

**Response**:
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "type": "TROJAN",
      "server": "example.com",
      "serverPort": 443,
      "password": "...",
      "serverName": "example.com",
      "insecure": false,
      "network": "TCP",
      "enabled": true,
      "priority": 1,
      "createdAt": "2024-01-01T12:00:00",
      "updatedAt": "2024-01-01T12:00:00"
    }
  ]
}
```

### Get Active Proxy
Get currently active proxy configuration.

```http
GET /proxy/configs/active
Authorization: Bearer <token>
```

### Create Proxy Configuration
Create new proxy configuration.

```http
POST /proxy/configs
Authorization: Bearer <token>
Content-Type: application/json

{
  "type": "TROJAN",
  "server": "example.com",
  "serverPort": 443,
  "password": "password123",
  "serverName": "example.com",
  "network": "TCP",
  "enabled": true,
  "priority": 1
}
```

### Update Proxy Configuration
Update existing proxy.

```http
PUT /proxy/configs/{id}
Authorization: Bearer <token>
Content-Type: application/json

{
  "server": "new-server.com",
  "enabled": true
}
```

### Delete Proxy Configuration
Delete proxy configuration.

```http
DELETE /proxy/configs/{id}
Authorization: Bearer <token>
```

### Toggle Proxy Configuration
Enable or disable proxy.

```http
POST /proxy/configs/{id}/toggle
Authorization: Bearer <token>
```

### Set Active Proxy
Set proxy as active (others will be disabled).

```http
POST /proxy/configs/{id}/active
Authorization: Bearer <token>
```

### Test Proxy Connection
Test proxy connectivity.

```http
POST /proxy/configs/{id}/test
Authorization: Bearer <token>
```

---

## Routing Endpoints

### List Routing Rules
Get all routing rules.

```http
GET /routing/rules
Authorization: Bearer <token>
```

**Response**:
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "type": "DOMAIN",
      "value": "[\"telegram.org\",\"*.twitter.com\"]",
      "outboundTag": "proxy",
      "enabled": true,
      "priority": 100,
      "description": "Social media",
      "createdAt": "2024-01-01T12:00:00",
      "updatedAt": "2024-01-01T12:00:00"
    }
  ]
}
```

### Create Routing Rule
Create new routing rule.

```http
POST /routing/rules
Authorization: Bearer <token>
Content-Type: application/json

{
  "type": "DOMAIN",
  "value": "[\"telegram.org\"]",
  "outboundTag": "proxy",
  "enabled": true,
  "priority": 100,
  "description": "Telegram"
}
```

### Update Routing Rule
Update existing routing rule.

```http
PUT /routing/rules/{id}
Authorization: Bearer <token>
Content-Type: application/json

{
  "enabled": true
}
```

### Delete Routing Rule
Delete routing rule.

```http
DELETE /routing/rules/{id}
Authorization: Bearer <token>
```

### Toggle Routing Rule
Enable or disable routing rule.

```http
POST /routing/rules/{id}/toggle
Authorization: Bearer <token>
```

### Get Routing Rule Types
Get available rule types.

```http
GET /routing/rules/types
Authorization: Bearer <token>
```

### Get GeoSite Categories
Get available GeoSite categories.

```http
GET /routing/rules/geosite-categories
Authorization: Bearer <token>
```

---

## System Endpoints

### Get System Status
Get overall system status.

```http
GET /system/status
Authorization: Bearer <token>
```

**Response**:
```json
{
  "success": true,
  "data": {
    "wireGuardStatus": "RUNNING",
    "singBoxStatus": "RUNNING",
    "connectedPeers": 2,
    "totalTransferRx": 1048576,
    "totalTransferTx": 524288,
    "uptime": 86400000,
    "cpuUsage": 5.2,
    "memoryUsage": 45.3,
    "diskUsage": 32.1,
    "timestamp": "2024-01-01T12:00:00"
  }
}
```

### Get System Info
Get system information.

```http
GET /system/info
Authorization: Bearer <token>
```

### Get System Stats
Get system statistics.

```http
GET /system/stats
Authorization: Bearer <token>
```

### Create Backup
Create configuration backup.

```http
POST /system/backup
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "Backup 2024-01-01",
  "description": "Pre-update backup"
}
```

### List Backups
List all configuration backups.

```http
GET /system/backups
Authorization: Bearer <token>
```

### Restore Backup
Restore configuration from backup.

```http
POST /system/backups/{id}/restore
Authorization: Bearer <token>
```

### Delete Backup
Delete configuration backup.

```http
DELETE /system/backups/{id}
Authorization: Bearer <token>
```

---

## WebSocket Endpoints

### Status Updates
Real-time system status updates.

```
WS /ws/status
```

**Message Format**:
```json
{
  "wireGuardStatus": "RUNNING",
  "singBoxStatus": "RUNNING",
  "connectedPeers": 2,
  "totalTransferRx": 1048576,
  "totalTransferTx": 524288,
  "uptime": 86400000,
  "cpuUsage": 5.2,
  "memoryUsage": 45.3,
  "diskUsage": 32.1,
  "timestamp": "2024-01-01T12:00:00"
}
```

### Log Streaming
Real-time application log streaming.

```
WS /ws/logs
```

**Message Format**:
```json
{
  "type": "update",
  "logs": ["2024-01-01 12:00:00 INFO Started...", "2024-01-01 12:00:01 INFO Ready"]
}
```

---

## Error Responses

All endpoints return errors in the following format:

```json
{
  "success": false,
  "data": null,
  "message": "Error description",
  "errors": ["Additional error details"]
}
```

### HTTP Status Codes

- `200 OK` - Successful request
- `201 Created` - Resource created
- `400 Bad Request` - Invalid request data
- `401 Unauthorized` - Missing or invalid token
- `403 Forbidden` - Insufficient permissions
- `404 Not Found` - Resource not found
- `500 Internal Server Error` - Server error

---

## Pagination

Not implemented yet. All list endpoints return complete arrays.

---

## Rate Limiting

Not implemented yet.

---

## Versioning

Current API version: v1
