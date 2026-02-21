BINARY_NAME=gateway
VERSION=$(shell git describe --tags --always --dirty 2>/dev/null || echo "dev")
LDFLAGS=-ldflags "-X main.version=$(VERSION) -s -w"

.PHONY: build clean test run docker-build docker-run

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
	docker run --rm --cap-add=NET_ADMIN --device=/dev/net/tun \
		-v $(PWD)/configs:/etc/wg-singbox:ro \
		-p 51820:51820/udp \
		wg-singbox-gateway:$(VERSION)
