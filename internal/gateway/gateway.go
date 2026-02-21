package gateway

import (
	"context"
	"fmt"
	"log"
	"os/exec"
	"syscall"
	"time"

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
		cfg:     cfg,
		wg:      wgMgr,
		singBox: nil,
	}, nil
}

func (g *Gateway) Run(ctx context.Context) error {
	log.Printf("Setting up WireGuard interface...")

	// Setup WireGuard
	if err := g.wg.Setup(); err != nil {
		return fmt.Errorf("wireguard setup: %w", err)
	}

	// Start WireGuard
	if err := g.wg.Start(); err != nil {
		return fmt.Errorf("wireguard start: %w", err)
	}

	log.Printf("WireGuard interface is up at %s", g.cfg.WireGuard.Address)

	// Start sing-box
	log.Printf("Starting sing-box...")
	g.singBox = exec.CommandContext(ctx, "sing-box", "run", "-c", g.cfg.SingBox.ConfigPath)
	g.singBox.Stdout = nil
	g.singBox.Stderr = nil

	if err := g.singBox.Start(); err != nil {
		return fmt.Errorf("sing-box start: %w", err)
	}

	log.Printf("sing-box is running")

	// Wait for context cancellation
	<-ctx.Done()
	return g.Shutdown()
}

func (g *Gateway) Shutdown() error {
	log.Printf("Shutting down gateway...")

	var errs []error

	// Stop sing-box
	if g.singBox != nil && g.singBox.Process != nil {
		log.Printf("Stopping sing-box...")
		if err := g.singBox.Process.Signal(syscall.SIGTERM); err != nil {
			errs = append(errs, fmt.Errorf("sing-box stop: %w", err))
		}

		// Wait for graceful shutdown
		time.Sleep(2 * time.Second)

		if g.singBox.ProcessState == nil {
			g.singBox.Process.Kill()
		}
	}

	// Stop WireGuard
	log.Printf("Stopping WireGuard...")
	if err := g.wg.Stop(); err != nil {
		errs = append(errs, fmt.Errorf("wireguard stop: %w", err))
	}

	if len(errs) > 0 {
		return fmt.Errorf("shutdown errors: %v", errs)
	}

	return nil
}
