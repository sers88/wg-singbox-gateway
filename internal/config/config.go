package config

import (
	"fmt"
	"os"

	"gopkg.in/yaml.v3"
)

type Config struct {
	WireGuard WireGuardConfig `yaml:"wireguard"`
	SingBox   SingBoxConfig   `yaml:"singbox"`
}

type WireGuardConfig struct {
	PrivateKey string `yaml:"private_key"`
	PublicKey  string `yaml:"public_key"`
	ListenPort int    `yaml:"listen_port"`
	Address    string `yaml:"address"`
	MTU        int    `yaml:"mtu"`
	PostUp     string `yaml:"post_up"`
	PostDown   string `yaml:"post_down"`
}

type PeerConfig struct {
	PublicKey    string `yaml:"public_key"`
	PresharedKey string `yaml:"preshared_key"`
	AllowedIPs   string `yaml:"allowed_ips"`
}

type SingBoxConfig struct {
	ConfigPath string `yaml:"config_path"`
	LogLevel   string `yaml:"log_level"`
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
	if cfg.WireGuard.MTU == 0 {
		cfg.WireGuard.MTU = 1280
	}
	if cfg.WireGuard.Address == "" {
		cfg.WireGuard.Address = "10.0.0.1/24"
	}
	if cfg.SingBox.LogLevel == "" {
		cfg.SingBox.LogLevel = "info"
	}
	if cfg.SingBox.ConfigPath == "" {
		cfg.SingBox.ConfigPath = "/etc/singbox/config.json"
	}

	return &cfg, nil
}
