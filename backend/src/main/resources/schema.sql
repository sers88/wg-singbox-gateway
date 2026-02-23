-- WireGuard configuration
CREATE TABLE IF NOT EXISTS wireguard_config (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    private_key TEXT NOT NULL UNIQUE,
    public_key TEXT NOT NULL,
    listen_port INTEGER NOT NULL DEFAULT 51820,
    address TEXT NOT NULL DEFAULT '10.0.0.1/24',
    mtu INTEGER NOT NULL DEFAULT 1280,
    post_up TEXT,
    post_down TEXT,
    enabled BOOLEAN NOT NULL DEFAULT 1,
    interface_name TEXT DEFAULT 'wg0',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- WireGuard peers
CREATE TABLE IF NOT EXISTS peer (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    config_id INTEGER NOT NULL,
    public_key TEXT NOT NULL UNIQUE,
    preshared_key TEXT,
    allowed_ips TEXT NOT NULL,
    name TEXT NOT NULL,
    device_type TEXT,
    endpoint_ip TEXT,
    endpoint_port INTEGER,
    persistent_keepalive INTEGER DEFAULT 25,
    last_handshake TIMESTAMP,
    transfer_rx BIGINT DEFAULT 0,
    transfer_tx BIGINT DEFAULT 0,
    enabled BOOLEAN NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (config_id) REFERENCES wireguard_config(id)
);

-- Proxy configurations (Trojan, VLESS, VMess, Shadowsocks)
CREATE TABLE IF NOT EXISTS proxy_config (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    type TEXT NOT NULL CHECK (type IN ('TROJAN', 'VLESS', 'VMESS', 'SHADOWSOCKS')),
    server TEXT NOT NULL,
    server_port INTEGER NOT NULL,
    password TEXT,
    uuid TEXT,
    server_name TEXT,
    insecure BOOLEAN NOT NULL DEFAULT 0,
    network TEXT DEFAULT 'TCP' CHECK (network IN ('TCP', 'UDP', 'QUIC')),
    flow TEXT,
    alter_id INTEGER DEFAULT 0,
    security TEXT DEFAULT 'AUTO',
    method TEXT DEFAULT 'AES_256_GCM',
    enabled BOOLEAN NOT NULL DEFAULT 1,
    priority INTEGER DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Routing rules
CREATE TABLE IF NOT EXISTS routing_rule (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    type TEXT NOT NULL CHECK (type IN ('DOMAIN', 'IP_CIDR', 'GEOSITE')),
    value TEXT NOT NULL,
    outbound_tag TEXT NOT NULL CHECK (outbound_tag IN ('proxy', 'direct', 'block')),
    enabled BOOLEAN NOT NULL DEFAULT 1,
    priority INTEGER DEFAULT 100,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Users
CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT NOT NULL UNIQUE,
    password TEXT NOT NULL,
    email TEXT,
    role TEXT NOT NULL DEFAULT 'USER' CHECK (role IN ('ADMIN', 'USER')),
    enabled BOOLEAN NOT NULL DEFAULT 1,
    last_login TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Configuration backups
CREATE TABLE IF NOT EXISTS config_backup (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    description TEXT,
    data TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Audit log
CREATE TABLE IF NOT EXISTS audit_log (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER,
    action TEXT NOT NULL,
    entity_type TEXT NOT NULL,
    entity_id INTEGER,
    details TEXT,
    ip_address TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_peer_config_id ON peer(config_id);
CREATE INDEX IF NOT EXISTS idx_peer_public_key ON peer(public_key);
CREATE INDEX IF NOT EXISTS idx_peer_enabled ON peer(enabled);
CREATE INDEX IF NOT EXISTS idx_proxy_config_type ON proxy_config(type);
CREATE INDEX IF NOT EXISTS idx_proxy_config_enabled ON proxy_config(enabled);
CREATE INDEX IF NOT EXISTS idx_routing_rule_type ON routing_rule(type);
CREATE INDEX IF NOT EXISTS idx_routing_rule_enabled ON routing_rule(enabled);
CREATE INDEX IF NOT EXISTS idx_routing_rule_priority ON routing_rule(priority);
CREATE INDEX IF NOT EXISTS idx_audit_log_user_id ON audit_log(user_id);
CREATE INDEX IF NOT EXISTS idx_audit_log_created_at ON audit_log(created_at);
CREATE INDEX IF NOT EXISTS idx_audit_log_entity_type ON audit_log(entity_type);

-- Insert default admin user (password: admin123)
-- BCrypt hash of 'admin123'
INSERT INTO users (username, password, email, role, enabled)
VALUES ('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MI9H4nQv1f0Z5Y2q6Z0K3qM3nGpV0q6', 'admin@local', 'ADMIN', 1)
ON CONFLICT(username) DO NOTHING;

-- Insert default WireGuard config (empty keys - will be generated)
INSERT INTO wireguard_config (private_key, public_key, listen_port, address, mtu, enabled)
VALUES ('', '', 51820, '10.0.0.1/24', 1280, 0)
ON CONFLICT(id) DO NOTHING;
