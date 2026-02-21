# wg-singbox-gateway

[![Docker Hub](https://img.shields.io/badge/docker-latest-blue)](https://hub.docker.com/r/sersb/wg-singbox-gateway)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

Маршрутизатор-шлюз для роутера Keenetic на базе WireGuard и sing-box.

## 📋 Как это работает

```
┌─────────────┐         WireGuard (0.0.0.0/0)         ┌─────────────────┐
│   Keenetic  │ ───────────────────────────────────────►│                 │
│   Router    │  Весь трафик идёт через WireGuard      │  Docker         │
│             │                                      │  Container      │
└─────────────┘                                      │                 │
                                                     │  ┌───────────┐  │
                                                     │  │ sing-box  │  │
                                                     │  │           │  │
                              Direct                 │  │ Белый     │  │
                        ┌─────────────┐              │  │ список:   │  │
                        │             │              │  │ telegram  │  │
                        │  Internet   │◄─────────────┤  │ twitter   │  │
                        │             │  Прокси       │  └─────┬─────┘  │
                        └─────────────┘              │        │        │
                                                     │        ▼        │
                                                     │  ┌───────────┐  │
                                                     │  │  Trojan   │  │
                                                     │  │  Server   │  │
                                                     │  └───────────┘  │
                                                     └─────────────────┘
```

## ✨ Особенности

- ✅ Полная настройка через environment variables
- ✅ Готовый образ на Docker Hub
- ✅ Поддержка Keenetic роутеров
- ✅ Белый список доменов для проксирования
- ✅ Прокси: Trojan, VLESS, VMess, Shadowsocks
- ✅ Автоматическая генерация конфигов

## 🚀 Быстрый старт

### Docker Pull & Run

```bash
docker run -d \
  --name wg-singbox-gateway \
  --cap-add=NET_ADMIN --cap-add=NET_RAW \
  --device=/dev/net/tun \
  -p 51820:51820/udp \
  -e WG_PRIVATE_KEY="<key>" \
  -e WG_PUBLIC_KEY="<key>" \
  -e KEENETIC_PUBLIC_KEY="<key>" \
  -e KEENETIC_PRESHARED_KEY="<key>" \
  -e PROXY_TYPE=trojan \
  -e PROXY_SERVER="example.com" \
  -e PROXY_PASSWORD="password" \
  sersb/wg-singbox-gateway:latest
```

### Docker Compose

```bash
git clone https://github.com/sersb/wg-singbox-gateway.git
cd wg-singbox-gateway

# Generate keys
make generate-keys

# Setup .env
cp .env.example .env
nano .env

# Run
docker compose up -d
```

## 📦 Установка

### Linux

```bash
sudo apt update
sudo apt install docker.io docker-compose-plugin
sudo usermod -aG docker $USER
```

### macOS

```bash
brew install --cask docker
```

### Windows

1. Установите [Docker Desktop](https://www.docker.com/products/docker-desktop/)
2. Включите WSL2 интеграцию

---

## ⚙️ Настройка

### Генерация ключей

```bash
make generate-keys
```

### Environment Variables

#### WireGuard

| Переменная | Описание | По умолчанию |
|------------|----------|--------------|
| `WG_PRIVATE_KEY` | Приватный ключ сервера | **обязательно** |
| `WG_PUBLIC_KEY` | Публичный ключ сервера | **обязательно** |
| `WG_LISTEN_PORT` | Порт WireGuard | `51820` |
| `WG_ADDRESS` | Адрес VPN | `10.0.0.1/24` |
| `WG_MTU` | MTU | `1280` |

#### Пиры (Keenetic)

| Переменная | Описание |
|------------|----------|
| `KEENETIC_PUBLIC_KEY` | Публичный ключ роутера |
| `KEENETIC_PRESHARED_KEY` | PSK для защиты |

#### Прокси

| Переменная | Описание |
|------------|----------|
| `PROXY_TYPE` | Тип: `trojan`, `vless`, `vmess`, `shadowsocks` |
| `PROXY_SERVER` | Адрес прокси-сервера |
| `PROXY_PORT` | Порт (по умолчанию 443) |
| `PROXY_PASSWORD` | Пароль (Trojan/SS) |
| `PROXY_UUID` | UUID (VLESS/VMess) |

#### Белый список

| Переменная | Описание | Пример |
|------------|----------|--------|
| `PROXY_DOMAINS` | Домены через прокси | `["telegram.org","*.twitter.com"]` |
| `PROXY_IPS` | IP через прокси | `["149.154.0.0/16"]` |
| `PROXY_GEOSITE` | Категории | `["category-social"]` |

---

## 📡 Настройка Keenetic

### Web интерфейс

1. **Сеть** → **Другие подключения** → **WireGuard** → **Добавить**
2. Заполните параметры:

```
Название:          wg-singbox-gateway
Приватный ключ:    <из client1_private.key>
Локальный адрес:   10.0.0.2/24
Публичный ключ:    <из server_public.key>
Адрес сервера:     <IP вашего сервера>
Порт сервера:      51820
PSK:               <из client1_preshared.key>
Постоянное:        25 сек
```

3. **Маршрутизация** → "Использовать как шлюз по умолчанию"

---

## 📝 Примеры конфигурации

### Trojan

```yaml
PROXY_TYPE=trojan
PROXY_SERVER=example.com
PROXY_PASSWORD=password123
PROXY_DOMAINS=["telegram.org","*.twitter.com"]
```

### VLESS

```yaml
PROXY_TYPE=vless
PROXY_SERVER=example.com
PROXY_UUID=xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
PROXY_FLOW=xtls-rprx-vision
```

### Shadowsocks

```yaml
PROXY_TYPE=shadowsocks
PROXY_SERVER=example.com
PROXY_PORT=8388
PROXY_PASSWORD=password123
PROXY_METHOD=aes-256-gcm
```

---

## 🔧 Устранение проблем

```bash
# Логи
docker logs -f wg-singbox-gateway

# Статус WireGuard
docker exec -it wg-singbox-gateway wg show

# Проверка sing-box
docker exec -it wg-singbox-gateway ps aux | grep sing-box
```

---

## 📄 Лицензия

MIT License
