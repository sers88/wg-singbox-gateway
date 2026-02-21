#!/bin/sh
set -e

echo "=== wg-singbox-gateway v${VERSION:-dev} ==="
echo ""

# Function to replace placeholders in config files
replace_in_file() {
    file="$1"
    shift
    while [ "$#" -gt 0 ]; do
        key="$1"
        value="$2"
        shift 2
        if [ -n "$value" ]; then
            value=$(echo "$value" | sed 's/&/\&/g; s/|/\\|/g; s/\//\\\//g')
            sed -i "s|\${$key}|$value|g" "$file"
            echo "  ✓ Set $key in $file"
        fi
    done
}

# Function to generate WireGuard config
generate_wg_config() {
    echo "Generating WireGuard configuration..."

    cat > /etc/wireguard/wg0.conf << EOF
[Interface]
PrivateKey = ${WG_PRIVATE_KEY}
Address = ${WG_ADDRESS:-10.0.0.1/24}
ListenPort = ${WG_LISTEN_PORT:-51820}
MTU = ${WG_MTU:-1280}
${WG_POST_UP:+PostUp = ${WG_POST_UP}}
${WG_POST_DOWN:+PostDown = ${WG_POST_DOWN}}

${PEER1_PUBLIC_KEY:+[Peer]}
${PEER1_PUBLIC_KEY:+PublicKey = ${PEER1_PUBLIC_KEY}}
${PEER1_PRESHARED_KEY:+PresharedKey = ${PEER1_PRESHARED_KEY}}
${PEER1_ALLOWED_IPS:+AllowedIPs = ${PEER1_ALLOWED_IPS:-10.0.0.2/32}}
EOF

    # Remove empty lines from optional fields
    sed -i '/^$/d' /etc/wireguard/wg0.conf

    # Add newline between sections for additional peers
    if [ -n "$PEER2_PUBLIC_KEY" ]; then
        cat >> /etc/wireguard/wg0.conf << EOF

[Peer]
PublicKey = ${PEER2_PUBLIC_KEY}
${PEER2_PRESHARED_KEY:+PresharedKey = ${PEER2_PRESHARED_KEY}}
${PEER2_ALLOWED_IPS:+AllowedIPs = ${PEER2_ALLOWED_IPS:-10.0.0.3/32}}
EOF
    fi

    if [ -n "$PEER3_PUBLIC_KEY" ]; then
        cat >> /etc/wireguard/wg0.conf << EOF

[Peer]
PublicKey = ${PEER3_PUBLIC_KEY}
${PEER3_PRESHARED_KEY:+PresharedKey = ${PEER3_PRESHARED_KEY}}
${PEER3_ALLOWED_IPS:+AllowedIPs = ${PEER3_ALLOWED_IPS:-10.0.0.4/32}}
EOF
    fi

    echo "  ✓ WireGuard config generated at /etc/wireguard/wg0.conf"
}

# Function to generate app config
generate_app_config() {
    echo "Generating application configuration..."

    cat > /etc/wg-singbox/config.yaml << EOF
wireguard:
  private_key: \${WG_PRIVATE_KEY}
  public_key: \${WG_PUBLIC_KEY}
  listen_port: ${WG_LISTEN_PORT:-51820}
  address: ${WG_ADDRESS:-10.0.0.1/24}
  mtu: ${WG_MTU:-1280}
  post_up: ${WG_POST_UP:-"iptables -t nat -A PREROUTING -i wg0 -j REDIRECT --to-ports 2080"}
  post_down: ${WG_POST_DOWN:-"iptables -t nat -D PREROUTING -i wg0 -j REDIRECT --to-ports 2080"}

singbox:
  config_path: /etc/singbox/config.json
  log_level: ${SINGBOX_LOG_LEVEL:-info}
EOF

    echo "  ✓ App config generated at /etc/wg-singbox/config.yaml"
}

# Function to generate sing-box config
generate_singbox_config() {
    echo "Generating sing-box configuration..."

    # Get proxy type from environment
    PROXY_TYPE="${PROXY_TYPE:-trojan}"

    # Build proxy outbound
    case "$PROXY_TYPE" in
        trojan)
            PROXY_OUTBOUND=$(cat << 'EOF'
    {
      "type": "trojan",
      "tag": "proxy-out",
      "server": "${PROXY_SERVER}",
      "server_port": ${PROXY_PORT:-443},
      "password": "${PROXY_PASSWORD}",
      "tls": {
        "enabled": true,
        "server_name": "${PROXY_SERVER_NAME:-${PROXY_SERVER}}",
        "insecure": ${PROXY_INSECURE:-false}
      },
      "network": "${PROXY_NETWORK:-tcp}"
    }
EOF
            )
            ;;
        vless)
            PROXY_OUTBOUND=$(cat << 'EOF'
    {
      "type": "vless",
      "tag": "proxy-out",
      "server": "${PROXY_SERVER}",
      "server_port": ${PROXY_PORT:-443},
      "uuid": "${PROXY_UUID}",
      "tls": {
        "enabled": true,
        "server_name": "${PROXY_SERVER_NAME:-${PROXY_SERVER}}",
        "insecure": ${PROXY_INSECURE:-false}
      },
      "network": "${PROXY_NETWORK:-tcp}",
      "flow": "${PROXY_FLOW:-}"
    }
EOF
            )
            ;;
        vmess)
            PROXY_OUTBOUND=$(cat << 'EOF'
    {
      "type": "vmess",
      "tag": "proxy-out",
      "server": "${PROXY_SERVER}",
      "server_port": ${PROXY_PORT:-443},
      "uuid": "${PROXY_UUID}",
      "alter_id": ${PROXY_ALTER_ID:-0},
      "security": "${PROXY_SECURITY:-auto}",
      "tls": {
        "enabled": true,
        "server_name": "${PROXY_SERVER_NAME:-${PROXY_SERVER}}",
        "insecure": ${PROXY_INSECURE:-false}
      },
      "network": "${PROXY_NETWORK:-tcp}"
    }
EOF
            )
            ;;
        shadowsocks)
            PROXY_OUTBOUND=$(cat << 'EOF'
    {
      "type": "shadowsocks",
      "tag": "proxy-out",
      "server": "${PROXY_SERVER}",
      "server_port": ${PROXY_PORT:-8388},
      "method": "${PROXY_METHOD:-aes-256-gcm}",
      "password": "${PROXY_PASSWORD}"
    }
EOF
            )
            ;;
        *)
            PROXY_OUTBOUND=$(cat << 'EOF'
    {
      "type": "direct",
      "tag": "proxy-out"
    }
EOF
            )
            ;;
    esac

    # Replace placeholders in proxy outbound
    PROXY_OUTBOUND=$(echo "$PROXY_OUTBOUND" | sed \
        -e "s/\${PROXY_SERVER}/${PROXY_SERVER:-}/g" \
        -e "s/\${PROXY_PORT}/${PROXY_PORT:-443}/g" \
        -e "s/\${PROXY_PASSWORD}/${PROXY_PASSWORD:-}/g" \
        -e "s/\${PROXY_UUID}/${PROXY_UUID:-}/g" \
        -e "s/\${PROXY_SERVER_NAME}/${PROXY_SERVER_NAME:-${PROXY_SERVER:-}}/g" \
        -e "s/\${PROXY_INSECURE}/${PROXY_INSECURE:-false}/g" \
        -e "s/\${PROXY_NETWORK}/${PROXY_NETWORK:-tcp}/g" \
        -e "s/\${PROXY_FLOW}/${PROXY_FLOW:-}/g" \
        -e "s/\${PROXY_ALTER_ID}/${PROXY_ALTER_ID:-0}/g" \
        -e "s/\${PROXY_SECURITY}/${PROXY_SECURITY:-auto}/g" \
        -e "s/\${PROXY_METHOD}/${PROXY_METHOD:-aes-256-gcm}/g")

    cat > /etc/singbox/config.json << EOF
{
  "log": {
    "level": "${SINGBOX_LOG_LEVEL:-info}",
    "timestamp": true
  },
  "dns": {
    "servers": [
      {
        "tag": "google-dns",
        "address": "tls://8.8.8.8",
        "address_resolver": "local"
      },
      {
        "tag": "cloudflare-dns",
        "address": "tls://1.1.1.1",
        "address_resolver": "local"
      },
      {
        "tag": "local",
        "address": "local"
      }
    ],
    "rules": [
      {
        "outbound": "any"
      }
    ]
  },
  "inbounds": [
    {
      "type": "tun",
      "tag": "tun-in",
      "interface_name": "tun0",
      "inet4_address": "198.18.0.1/16",
      "auto_route": true,
      "strict_route": false,
      "sniff": true,
      "sniff_override_destination": false,
      "stack": "system"
    },
    {
      "type": "mixed",
      "tag": "mixed-in",
      "listen": "127.0.0.1",
      "listen_port": 2080
    }
  ],
  "outbounds": [
    {
      "type": "direct",
      "tag": "direct"
    },
    {
      "type": "block",
      "tag": "block"
    },
    {
      "type": "dns",
      "tag": "dns-out"
    }
${PROXY_SERVER:+,}
${PROXY_SERVER:+${PROXY_OUTBOUND}}
  ],
  "route": {
    "rules": [
      {
        "protocol": "dns",
        "outbound": "dns-out"
      },
      {
        "ip_is_private": true,
        "outbound": "direct"
      }
${PROXY_DOMAINS:+,}
${PROXY_DOMAINS:+      {
        "domain": ${PROXY_DOMAINS},
        "outbound": "proxy-out"
      }}
${PROXY_IPS:+,}
${PROXY_IPS:+      {
        "ip_cidr": ${PROXY_IPS},
        "outbound": "proxy-out"
      }}
${PROXY_GEOSITE:+,}
${PROXY_GEOSITE:+      {
        "geosite": ${PROXY_GEOSITE},
        "outbound": "proxy-out"
      }}
    ],
${PROXY_GEOSITE:+    "rule_set": [
      {
        "tag": "geosite-custom",
        "type": "remote",
        "format": "binary",
        "url": "https://raw.githubusercontent.com/SagerNet/sing-geosite/rule-set/${PROXY_GEOSITE_FILE:-geosite-category-social}.srs",
        "download_detour": "direct"
      }
    ],}
    "final": "direct",
    "auto_detect_interface": true
  }
}
EOF

    echo "  ✓ sing-box config generated at /etc/singbox/config.json"
}

# Check if custom configs are mounted
if [ -f /etc/wg-singbox/config.yaml ]; then
    echo "Using custom app config from /etc/wg-singbox/config.yaml"
    APP_CONFIG="/etc/wg-singbox/config.yaml"
else
    generate_app_config
    APP_CONFIG="/etc/wg-singbox/config.yaml"
fi

if [ -f /etc/singbox/config.json ]; then
    echo "Using custom sing-box config from /etc/singbox/config.json"
    SINGBOX_CONFIG="/etc/singbox/config.json"
else
    generate_singbox_config
    SINGBOX_CONFIG="/etc/singbox/config.json"
fi

# Always generate WireGuard config from ENV (it's sensitive)
generate_wg_config

# Display generated configs
echo ""
echo "=== Generated configuration ==="
echo "WireGuard config:"
cat /etc/wireguard/wg0.conf
echo ""
echo "App config:"
cat $APP_CONFIG
echo ""
echo "sing-box config:"
cat $SINGBOX_CONFIG
echo ""
echo "==================================="
echo ""

# Enable IP forwarding
echo "Enabling IP forwarding..."
sysctl -w net.ipv4.ip_forward=1
sysctl -w net.ipv6.conf.all.forwarding=1
sysctl -w net.ipv4.conf.all.rp_filter=0

# Start the gateway
exec /app/gateway
