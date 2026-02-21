BINARY_NAME=gateway
VERSION=$(shell git describe --tags --always --dirty 2>/dev/null || echo "dev")
LDFLAGS=-ldflags "-X main.version=$(VERSION) -s -w"

.PHONY: build clean test run docker-build docker-run generate-keys

build:
	go build $(LDFLAGS) -o $(BINARY_NAME) ./cmd/gateway

clean:
	go clean
	rm -f $(BINARY_NAME)

test:
	go test -v ./...

run:
	go run ./cmd/gateway

docker-build:
	docker build -t wg-singbox-gateway:$(VERSION) .

docker-run:
	docker run --rm --cap-add=NET_ADMIN --cap-add=NET_RAW --device=/dev/net/tun \
		-v $(PWD)/configs:/etc/wg-singbox:ro \
		-v $(PWD)/configs/singbox.json:/etc/singbox/config.json:ro \
		-p 51820:51820/udp \
		wg-singbox-gateway:$(VERSION)

generate-keys:
	@echo "Generating server keys..."
	wg genkey | tee server_private.key | wg pubkey > server_public.key
	@echo "Generating Keenetic router keys..."
	wg genkey | tee client1_private.key | wg pubkey > client1_public.key
	wg genpsk > client1_preshared.key
	@echo ""
	@echo "=== Server keys ==="
	@echo "WG_PRIVATE_KEY=$(shell cat server_private.key)"
	@echo "WG_PUBLIC_KEY=$(shell cat server_public.key)"
	@echo ""
	@echo "=== Keenetic router keys ==="
	@echo "Router private key (for Keenetic config):"
	@cat client1_private.key
	@echo ""
	@echo "Router public key (for .env file):"
	@echo "KEENETIC_PUBLIC_KEY=$(shell cat client1_public.key)"
	@echo ""
	@echo "Preshared key (for both sides):"
	@echo "KEENETIC_PRESHARED_KEY=$(shell cat client1_preshared.key)"
