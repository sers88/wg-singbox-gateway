package gateway

import (
	"context"
	"fmt"
	"log"
	"os/exec"
	"syscall"

	"github.com/sersb/wg-singbox-gateway/internal/config"
	"github.com/sersb/wg-singbox-gateway/internal/wireguard"
)

type Gateway struct {
	cfg     *config.Config
	wg      *wireguard.Manager
	singBox *exec.Cmd
}

func New(cfg *config.Config) (*Gateway, error) {
	wgMgr, err := wireguard.New(cfg.WireGuard)
	if err != nil {
		return nil, fmt.Errorf("creating wireguard manager: %w", err)
	}

	return &Gateway{
		cfg: cfg,
		wg:  wgMgr,
	}, nil
}

func (g *Gateway) Run(ctx context.Context) error {
	log.Printf("Starting wg-singbox-gateway")

	if err := g.wg.Setup(); err != nil {
		return fmt.Errorf("wireguard setup: %w", err)
	}

	if err := g.wg.Start(); err != nil {
		return fmt.Errorf("wireguard start: %w", err)
	}

	g.singBox = exec.CommandContext(ctx, "sing-box", "run", "-c", g.cfg.SingBox.ConfigPath)
	g.singBox.Stdout = nil
	g.singBox.Stderr = nil

	if err := g.singBox.Start(); err != nil {
		return fmt.Errorf("sing-box start: %w", err)
	}

	log.Printf("WireGuard and sing-box are running")

	<-ctx.Done()
	return g.Shutdown()
}

func (g *Gateway) Shutdown() error {
	log.Printf("Shutting down...")

	if g.singBox != nil && g.singBox.Process != nil {
		g.singBox.Process.Signal(syscall.SIGTERM)
	}

	return g.wg.Stop()
}
