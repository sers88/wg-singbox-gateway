package config

import (
	"fmt"
	"os"

	"gopkg.in/yaml.v3"
)

type Config struct {
	WireGuard WireGuardConfig `yaml:"wireguard"`
	SingBox   SingBoxConfig   `yaml:"singbox"`
	Routes    []Route         `yaml:"routes"`
}

type WireGuardConfig struct {
	PrivateKey   string            `yaml:"private_key"`
	PublicKey    string            `yaml:"public_key"`
	ListenPort   int               `yaml:"listen_port"`
	Address      string            `yaml:"address"`
	Peers        []Peer            `yaml:"peers"`
	PostUp       string            `yaml:"post_up"`
	PostDown     string            `yaml:"post_down"`
	ExtraOptions map[string]string `yaml:"extra_options"`
}

type Peer struct {
	PublicKey           string   `yaml:"public_key"`
	PresharedKey        string   `yaml:"preshared_key"`
	AllowedIPs          string   `yaml:"allowed_ips"`
	Endpoint            string   `yaml:"endpoint"`
	PersistentKeepalive int      `yaml:"persistent_keepalive"`
	ExtraOptions        []string `yaml:"extra_options"`
}

type SingBoxConfig struct {
	ConfigPath string `yaml:"config_path"`
	LogLevel   string `yaml:"log_level"`
}

type Route struct {
	Name       string   `yaml:"name"`
	MatchIPs   []string `yaml:"match_ips"`
	MatchPorts []string `yaml:"match_ports"`
	Domain     []string `yaml:"domain"`
	Upstream   string   `yaml:"upstream"` // "direct" or outbound name
}

func Load(path string) (*Config, error) {
	if path == "" {
		path = "/etc/wg-singbox/config.yaml"
	}

	data, err := os.ReadFile(path)
	if err != nil {
		return nil, fmt.Errorf("reading config: %w", err)
	}

	var cfg Config
	if err := yaml.Unmarshal(data, &cfg); err != nil {
		return nil, fmt.Errorf("parsing config: %w", err)
	}

	// Set defaults
	if cfg.WireGuard.ListenPort == 0 {
		cfg.WireGuard.ListenPort = 51820
	}
	if cfg.SingBox.LogLevel == "" {
		cfg.SingBox.LogLevel = "info"
	}
	if cfg.SingBox.ConfigPath == "" {
		cfg.SingBox.ConfigPath = "/etc/singbox/config.json"
	}

	return &cfg, nil
}
