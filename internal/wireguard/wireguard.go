package wireguard

import (
	"fmt"
	"log"
	"os"
	"os/exec"
	"strings"
	"text/template"

	"github.com/sersb/wg-singbox-gateway/internal/config"
)

type Manager struct {
	cfg config.WireGuardConfig
}

const wgConfigTemplate = `[Interface]
PrivateKey = {{.PrivateKey}}
Address = {{.Address}}
ListenPort = {{.ListenPort}}
MTU = {{.MTU}}
{{- if .PostUp}}
PostUp = {{.PostUp}}
{{- end}}
{{- if .PostDown}}
PostDown = {{.PostDown}}
{{- end}}

{{range .Peers}}
[Peer]
PublicKey = {{.PublicKey}}
{{- if .PresharedKey}}
PresharedKey = {{.PresharedKey}}
{{- end}}
AllowedIPs = {{.AllowedIPs}}
{{end}}
`

func New(cfg config.WireGuardConfig) (*Manager, error) {
	return &Manager{cfg: cfg}, nil
}

func (m *Manager) Setup() error {
	configPath := "/etc/wireguard/wg0.conf"

	if err := os.MkdirAll("/etc/wireguard", 0700); err != nil {
		return fmt.Errorf("creating wireguard directory: %w", err)
	}

	// Generate config from peers stored in environment or config
	content, err := m.generateConfig()
	if err != nil {
		return fmt.Errorf("generating config: %w", err)
	}

	if err := os.WriteFile(configPath, []byte(content), 0600); err != nil {
		return fmt.Errorf("writing wireguard config: %w", err)
	}

	log.Printf("WireGuard config written to %s", configPath)
	return nil
}

func (m *Manager) generateConfig() (string, error) {
	// Get peers from config
	peers, err := m.getPeers()
	if err != nil {
		return "", fmt.Errorf("getting peers: %w", err)
	}

	data := struct {
		PrivateKey string
		Address    string
		ListenPort int
		MTU        int
		PostUp     string
		PostDown   string
		Peers      []config.PeerConfig
	}{
		PrivateKey: m.cfg.PrivateKey,
		Address:    m.cfg.Address,
		ListenPort: m.cfg.ListenPort,
		MTU:        m.cfg.MTU,
		PostUp:     m.cfg.PostUp,
		PostDown:   m.cfg.PostDown,
		Peers:      peers,
	}

	tmpl, err := template.New("wgconfig").Parse(wgConfigTemplate)
	if err != nil {
		return "", fmt.Errorf("parsing template: %w", err)
	}

	var sb strings.Builder
	if err := tmpl.Execute(&sb, data); err != nil {
		return "", fmt.Errorf("executing template: %w", err)
	}

	return sb.String(), nil
}

func (m *Manager) getPeers() ([]config.PeerConfig, error) {
	var peers []config.PeerConfig

	// PEER1_PUBLIC_KEY, PEER1_PRESHARED_KEY, PEER1_ALLOWED_IPS
	for i := 1; i <= 10; i++ {
		pubKey := os.Getenv(fmt.Sprintf("PEER%d_PUBLIC_KEY", i))
		if pubKey == "" {
			continue
		}

		psk := os.Getenv(fmt.Sprintf("PEER%d_PRESHARED_KEY", i))
		allowedIPs := os.Getenv(fmt.Sprintf("PEER%d_ALLOWED_IPS", i))
		if allowedIPs == "" {
			allowedIPs = fmt.Sprintf("10.0.0.%d/32", i+1)
		}

		peers = append(peers, config.PeerConfig{
			PublicKey:    pubKey,
			PresharedKey: psk,
			AllowedIPs:   allowedIPs,
		})

		log.Printf("Added peer %d: %s -> %s", i, pubKey[:16]+"...", allowedIPs)
	}

	if len(peers) == 0 {
		log.Printf("Warning: No peers configured")
	}

	return peers, nil
}

func (m *Manager) Start() error {
	// Check if interface already exists
	if _, err := os.Stat("/sys/class/net/wg0"); err == nil {
		log.Printf("Interface wg0 already exists, bringing it down first...")
		exec.Command("wg-quick", "down", "wg0").Run()
	}

	cmd := exec.Command("wg-quick", "up", "wg0")
	output, err := cmd.CombinedOutput()
	if err != nil {
		return fmt.Errorf("starting wireguard: %w, output: %s", err, string(output))
	}
	log.Printf("wg-quick up: %s", string(output))
	return nil
}

func (m *Manager) Stop() error {
	cmd := exec.Command("wg-quick", "down", "wg0")
	if err := cmd.Run(); err != nil {
		log.Printf("Warning: wg-quick down error: %v", err)
	}
	return nil
}
