package wireguard

import (
	"fmt"
	"os"
	"os/exec"
	"strings"

	"github.com/sersb/wg-singbox-gateway/internal/config"
)

type Manager struct {
	cfg *config.WireGuardConfig
}

func New(cfg config.WireGuardConfig) (*Manager, error) {
	return &Manager{cfg: &cfg}, nil
}

func (m *Manager) Setup() error {
	configPath := "/etc/wireguard/wg0.conf"

	content, err := m.generateConfig()
	if err != nil {
		return err
	}

	if err := os.MkdirAll("/etc/wireguard", 0755); err != nil {
		return fmt.Errorf("creating wireguard directory: %w", err)
	}

	if err := os.WriteFile(configPath, []byte(content), 0600); err != nil {
		return fmt.Errorf("writing wireguard config: %w", err)
	}

	return nil
}

func (m *Manager) generateConfig() (string, error) {
	var sb strings.Builder

	sb.WriteString(fmt.Sprintf("[Interface]\n"))
	sb.WriteString(fmt.Sprintf("PrivateKey = %s\n", m.cfg.PrivateKey))
	sb.WriteString(fmt.Sprintf("Address = %s\n", m.cfg.Address))
	sb.WriteString(fmt.Sprintf("ListenPort = %d\n", m.cfg.ListenPort))

	if m.cfg.PostUp != "" {
		sb.WriteString(fmt.Sprintf("PostUp = %s\n", m.cfg.PostUp))
	}
	if m.cfg.PostDown != "" {
		sb.WriteString(fmt.Sprintf("PostDown = %s\n", m.cfg.PostDown))
	}

	for k, v := range m.cfg.ExtraOptions {
		sb.WriteString(fmt.Sprintf("%s = %s\n", k, v))
	}

	for _, peer := range m.cfg.Peers {
		sb.WriteString(fmt.Sprintf("\n[Peer]\n"))
		sb.WriteString(fmt.Sprintf("PublicKey = %s\n", peer.PublicKey))
		if peer.PresharedKey != "" {
			sb.WriteString(fmt.Sprintf("PresharedKey = %s\n", peer.PresharedKey))
		}
		sb.WriteString(fmt.Sprintf("AllowedIPs = %s\n", peer.AllowedIPs))
		if peer.Endpoint != "" {
			sb.WriteString(fmt.Sprintf("Endpoint = %s\n", peer.Endpoint))
		}
		if peer.PersistentKeepalive > 0 {
			sb.WriteString(fmt.Sprintf("PersistentKeepalive = %d\n", peer.PersistentKeepalive))
		}
		for _, opt := range peer.ExtraOptions {
			sb.WriteString(fmt.Sprintf("%s\n", opt))
		}
	}

	return sb.String(), nil
}

func (m *Manager) Start() error {
	if err := exec.Command("wg-quick", "up", "wg0").Run(); err != nil {
		return fmt.Errorf("starting wireguard: %w", err)
	}
	return nil
}

func (m *Manager) Stop() error {
	if err := exec.Command("wg-quick", "down", "wg0").Run(); err != nil {
		return fmt.Errorf("stopping wireguard: %w", err)
	}
	return nil
}
