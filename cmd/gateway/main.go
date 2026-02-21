package main

import (
	"context"
	"log"
	"os"
	"os/signal"
	"syscall"

	"github.com/sersb/wg-singbox-gateway/internal/config"
	"github.com/sersb/wg-singbox-gateway/internal/gateway"
)

var version = "dev"

func main() {
	log.Printf("wg-singbox-gateway v%s starting...", version)

	cfg, err := config.Load("/etc/wg-singbox/config.yaml")
	if err != nil {
		log.Fatalf("Failed to load config: %v", err)
	}

	gw, err := gateway.New(cfg)
	if err != nil {
		log.Fatalf("Failed to create gateway: %v", err)
	}

	ctx, cancel := context.WithCancel(context.Background())
	defer cancel()

	go func() {
		sigChan := make(chan os.Signal, 1)
		signal.Notify(sigChan, syscall.SIGINT, syscall.SIGTERM)
		sig := <-sigChan
		log.Printf("Received signal: %v, shutting down...", sig)
		cancel()
	}()

	if err := gw.Run(ctx); err != nil {
		log.Fatalf("Gateway error: %v", err)
	}
	log.Printf("Gateway stopped gracefully")
}
