#!/bin/bash
# Test script for entrypoint.sh without running Docker

set -e

echo "=== Testing entrypoint.sh configuration generation ==="
echo ""

# Set test environment variables
export WG_PRIVATE_KEY="test_private_key_abcdef"
export WG_PUBLIC_KEY="test_public_key_ghijkl"
export KEENETIC_PUBLIC_KEY="keenetic_public_key_mnopqr"
export KEENETIC_PRESHARED_KEY="preshared_key_stuvwxyz"
export PROXY_TYPE="trojan"
export PROXY_SERVER="test.example.com"
export PROXY_PORT="443"
export PROXY_PASSWORD="test_password"
export PROXY_DOMAINS='["telegram.org","*.twitter.com"]'

# Create temp directory for configs
TMPDIR=$(mktemp -d)
echo "Using temp directory: $TMPDIR"

# Copy entrypoint.sh
cp entrypoint.sh "$TMPDIR/"
chmod +x "$TMPDIR/entrypoint.sh"

# Create fake /app/gateway (cat will just exit)
mkdir -p "$TMPDIR/app"
echo '#!/bin/sh
echo "Gateway binary would run here"
echo "WG config:"
cat /etc/wireguard/wg0.conf
' > "$TMPDIR/app/gateway"
chmod +x "$TMPDIR/app/gateway"

# Create necessary directories
mkdir -p "$TMPDIR/etc/wg-singbox" "$TMPDIR/etc/wireguard" "$TMPDIR/etc/singbox"

# Run entrypoint with fake gateway
cd "$TMPDIR"
ln -s /bin/true /app/gateway

# Test config generation only
bash -c '
source ./entrypoint.sh 2>&1 | head -100
'

echo ""
echo "=== Generated configs ==="
echo ""
echo "WireGuard config:"
cat "$TMPDIR/etc/wireguard/wg0.conf" || echo "Not generated"
echo ""
echo "App config:"
cat "$TMPDIR/etc/wg-singbox/config.yaml" || echo "Not generated"
echo ""
echo "sing-box config:"
cat "$TMPDIR/etc/singbox/config.json" || echo "Not generated"

# Cleanup
cd -
rm -rf "$TMPDIR"

echo ""
echo "=== Test completed ==="
