# wg-singbox-gateway

Маршрутизатор‑шлюз на базе WireGuard и sing-box: контейнер поднимает WireGuard‑сервер и пропускает трафик клиентов через sing-box, позволяя гибко разделять его между прямым выходом в интернет и Trojan/другими прокси по правилам маршрутизации.

## 📋 Содержание

- [Особенности](#особенности)
- [Требования](#требования)
- [Установка по ОС](#установка-по-ос)
- [Настройка](#настройка)
- [Запуск](#запуск)
- [Настройка клиента](#настройка-клиента)
- [Проверка работы](#проверка-работы)
- [Устранение проблем](#устранение-проблем)

## ✨ Особенности

- WireGuard VPN для защищённого подключения клиентов
- sing-box для гибкой маршрутизации трафика
- Правила маршрутизации по доменам, IP-адресам и портам
- Поддержка множественных upstream-прокси (Trojan, VLESS, VMess и др.)
- Лёгкий контейнер на Alpine Linux
- Конфигурация через YAML

## 📦 Требования

- Docker 20.10+ и Docker Compose v2
- Для генерации ключей: WireGuard (Linux/macOS) или WSL2 (Windows)

---

## 🖥️ Установка по ОС

### 🪟 Windows

> **Примечание**: Рекомендуется использовать WSL2 для лучшей совместимости с Docker

#### 1. Установка WSL2 (если не установлено)

```powershell
# В PowerShell с правами администратора
wsl --install
# Перезагрузите компьютер и завершите установку Ubuntu
```

#### 2. Установка Docker Desktop

1. Скачайте [Docker Desktop for Windows](https://www.docker.com/products/docker-desktop/)
2. Установите и убедитесь, что включена интеграция с WSL2

#### 3. Установка WireGuard инструментов

**Через WSL2:**

```bash
# В терминале WSL Ubuntu
sudo apt update
sudo apt install -y wireguard
```

**Или через Chocolatey (PowerShell):**

```powershell
# Если Chocolatey не установлен:
Set-ExecutionPolicy Bypass -Scope Process -Force
[System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072
iex ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))

# Установка WireGuard
choco install wireguard
```

#### 4. Клонирование репозитория

```bash
# В WSL2 или Git Bash
git clone https://github.com/sersb/wg-singbox-gateway.git
cd wg-singbox-gateway
```

### 🍎 macOS

#### 1. Установка Homebrew (если не установлен)

```bash
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
```

#### 2. Установка Docker Desktop

```bash
brew install --cask docker
# Запустите Docker Desktop из Applications
```

#### 3. Установка WireGuard

```bash
brew install wireguard-tools
```

#### 4. Клонирование репозитория

```bash
git clone https://github.com/sersb/wg-singbox-gateway.git
cd wg-singbox-gateway
```

### 🐧 Linux

#### 1. Установка Docker

**Ubuntu/Debian:**

```bash
# Установка зависимостей
sudo apt update
sudo apt install -y ca-certificates curl gnupg

# Добавление Docker GPG ключа
sudo install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
sudo chmod a+r /etc/apt/keyrings/docker.gpg

# Добавление репозитория
echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu $(. /etc/os-release && echo "$VERSION_CODENAME") stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

# Установка Docker
sudo apt update
sudo apt install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

# Добавление пользователя в группу docker
sudo usermod -aG docker $USER
newgrp docker
```

#### 2. Установка WireGuard

```bash
sudo apt install -y wireguard
```

#### 3. Клонирование репозитория

```bash
git clone https://github.com/sersb/wg-singbox-gateway.git
cd wg-singbox-gateway
```

---

## ⚙️ Настройка

### 1. Генерация WireGuard ключей

**Windows (WSL2) / macOS / Linux:**

```bash
# Серверные ключи
wg genkey | tee server_private.key | wg pubkey > server_public.key

# Ключи для каждого клиента
wg genkey | tee client1_private.key | wg pubkey > client1_public.key
wg genpsk > client1_preshared.key

# Дополнительные клиенты (при необходимости)
wg genkey | tee client2_private.key | wg pubkey > client2_public.key
wg genpsk > client2_preshared.key
```

**Windows (PowerShell без WSL):**

```powershell
# Скачайте WireGuard tools и выполните:
wg genkey > server_private.key
$type = Get-Content server_private.key
$type | wg pubkey > server_public.key
```

### 2. Создание файла переменных окружения

**Linux/macOS:**

```bash
cp .env.example .env
nano .env  # или vim .env
```

**Windows (PowerShell):**

```powershell
Copy-Item .env.example .env
notepad .env
```

**Windows (WSL2):**

```bash
cp .env.example .env
nano .env
```

Заполните `.env` сгенерированными ключами:

```env
# Серверные ключи
WG_PRIVATE_KEY=<содержимое server_private.key>
WG_PUBLIC_KEY=<содержимое server_public.key>

# Пир 1 (клиент 1)
PEER1_PUBLIC_KEY=<содержимое client1_public.key>
PEER1_PRESHARED_KEY=<содержимое client1_preshared.key>

# Пир 2 (клиент 2)
PEER2_PUBLIC_KEY=<содержимое client2_public.key>
PEER2_PRESHARED_KEY=<содержимое client2_preshared.key>
```

### 3. Настройка конфигурации приложения

Отредактируйте `configs/config.yaml`:

**Linux/macOS:**

```bash
nano configs/config.yaml
```

**Windows:**

```powershell
notepad configs\config.yaml
```

Пример конфигурации:

```yaml
wireguard:
  private_key: ${WG_PRIVATE_KEY}
  public_key: ${WG_PUBLIC_KEY}
  listen_port: 51820
  address: 10.0.0.1/24

  post_up: |
    iptables -A FORWARD -i %i -j ACCEPT;
    iptables -A FORWARD -o %i -j ACCEPT;
    iptables -t nat -A POSTROUTING -o eth0 -j MASQUERADE

  post_down: |
    iptables -D FORWARD -i %i -j ACCEPT;
    iptables -D FORWARD -o %i -j ACCEPT;
    iptables -t nat -D POSTROUTING -o eth0 -j MASQUERADE

  peers:
    - public_key: ${PEER1_PUBLIC_KEY}
      allowed_ips: 10.0.0.2/32
      preshared_key: ${PEER1_PRESHARED_KEY}

singbox:
  config_path: /etc/singbox/config.json
  log_level: info

routes:
  - name: Direct to Google
    domain:
      - google.com
      - "*.google.com"
    upstream: direct

  - name: Proxy to Telegram
    domain:
      - telegram.org
      - "*.telegram.org"
    upstream: trojan-out
```

### 4. Настройка sing-box

Отредактируйте `configs/singbox.json`:

```json
{
  "log": {"level": "info", "timestamp": true},
  "dns": {
    "servers": [
      {"tag": "google", "address": "tls://8.8.8.8"}
    ]
  },
  "inbounds": [
    {
      "type": "tun",
      "tag": "tun-in",
      "interface_name": "tun0",
      "inet4_address": "198.18.0.1/16",
      "auto_route": true
    }
  ],
  "outbounds": [
    {"type": "direct", "tag": "direct"},
    {"type": "block", "tag": "block"},
    {
      "type": "trojan",
      "tag": "trojan-out",
      "server": "your-proxy-server.com",
      "server_port": 443,
      "password": "your-password",
      "tls": {"enabled": true, "server_name": "your-proxy-server.com"}
    }
  ],
  "route": {
    "rules": [
      {"protocol": "dns", "outbound": "direct"},
      {"ip_is_private": true, "outbound": "direct"}
    ],
    "final": "direct",
    "auto_detect_interface": true
  }
}
```

---

## 🚀 Запуск

### Docker Compose (рекомендуется)

**Windows (PowerShell):**

```powershell
# Первый запуск
docker compose up -d

# Просмотр логов
docker compose logs -f

# Остановка
docker compose down

# Перезапуск
docker compose restart
```

**Windows (WSL2) / macOS / Linux:**

```bash
# Первый запуск
docker compose up -d

# Просмотр логов
docker compose logs -f

# Остановка
docker compose down

# Перезапуск
docker compose restart
```

### Docker

**Windows (PowerShell):**

```powershell
docker run -d --name wg-singbox-gateway --cap-add=NET_ADMIN --cap-add=SYS_MODULE --device=/dev/net/tun --sysctl net.ipv4.ip_forward=1 --sysctl net.ipv6.conf.all.forwarding=1 -p 51820:51820/udp -v ${PWD}/configs:/etc/wg-singbox:ro -v ${PWD}/configs/singbox.json:/etc/singbox/config.json:ro wg-singbox-gateway:latest
```

**Windows (WSL2) / macOS / Linux:**

```bash
docker run -d \
  --name wg-singbox-gateway \
  --cap-add=NET_ADMIN \
  --cap-add=SYS_MODULE \
  --device=/dev/net/tun \
  --sysctl net.ipv4.ip_forward=1 \
  --sysctl net.ipv6.conf.all.forwarding=1 \
  -p 51820:51820/udp \
  -v $(pwd)/configs:/etc/wg-singbox:ro \
  -v $(pwd)/configs/singbox.json:/etc/singbox/config.json:ro \
  wg-singbox-gateway:latest
```

---

## 📱 Настройка клиента

### 🪟 Windows

#### Способ 1: WireGuard for Windows (рекомендуется)

1. Скачайте [WireGuard for Windows](https://www.wireguard.com/install/)
2. Создайте файл конфигурации `client1.conf`:

```ini
[Interface]
PrivateKey = <содержимое client1_private.key>
Address = 10.0.0.2/24
DNS = 8.8.8.8, 8.8.4.4

[Peer]
PublicKey = <содержимое server_public.key>
PresharedKey = <содержимое client1_preshared.key>
Endpoint = <IP-адрес-сервера>:51820
AllowedIPs = 0.0.0.0/0
PersistentKeepalive = 25
```

3. Импортируйте конфигурацию в WireGuard (Import tunnel(s) from file)
4. Нажмите "Activate"

#### Способ 2: PowerShell (через WSL)

```powershell
# Импорт ключей
$clientPrivateKey = Get-Content client1_private.key
$serverPublicKey = Get-Content server_public.key
$presharedKey = Get-Content client1_preshared.key

# Создание конфига
@"
[Interface]
PrivateKey = $clientPrivateKey
Address = 10.0.0.2/24
DNS = 8.8.8.8, 8.8.4.4

[Peer]
PublicKey = $serverPublicKey
PresharedKey = $presharedKey
Endpoint = <IP-адрес-сервера>:51820
AllowedIPs = 0.0.0.0/0
PersistentKeepalive = 25
"@ | Out-File -Encoding UTF8 client1.conf
```

### 🍎 macOS

#### Установка WireGuard

```bash
brew install wireguard-tools
```

#### Создание конфигурации

```bash
nano client1.conf
```

```ini
[Interface]
PrivateKey = <содержимое client1_private.key>
Address = 10.0.0.2/24
DNS = 8.8.8.8, 8.8.4.4

[Peer]
PublicKey = <содержимое server_public.key>
PresharedKey = <содержимое client1_preshared.key>
Endpoint = <IP-адрес-сервера>:51820
AllowedIPs = 0.0.0.0/0
PersistentKeepalive = 25
```

#### Запуск

```bash
# Включение VPN
sudo wg-quick up ./client1.conf

# Выключение VPN
sudo wg-quick down ./client1.conf

# Проверка статуса
wg show
```

#### Альтернатива: WireGuard GUI

```bash
brew install --cask wireguard-go
```

### 🐧 Linux

#### Создание конфигурации

```bash
nano client1.conf
```

```ini
[Interface]
PrivateKey = <содержимое client1_private.key>
Address = 10.0.0.2/24
DNS = 8.8.8.8, 8.8.4.4

[Peer]
PublicKey = <содержимое server_public.key>
PresharedKey = <содержимое client1_preshared.key>
Endpoint = <IP-адрес-сервера>:51820
AllowedIPs = 0.0.0.0/0
PersistentKeepalive = 25
```

#### Запуск

```bash
# Включение VPN
sudo wg-quick up ./client1.conf

# Выключение VPN
sudo wg-quick down ./client1.conf

# Автозапуск при старте системы
sudo cp client1.conf /etc/wireguard/wg0.conf
sudo systemctl enable wg-quick@wg0
sudo systemctl start wg-quick@wg0

# Проверка статуса
sudo wg show
```

### 📲 iOS

1. Установите WireGuard из [App Store](https://apps.apple.com/app/wireguard/id1441195209)
2. Откройте приложение → "+" → "Create from QR code"
3. Для создания QR-кода:

```bash
# На сервере или Linux/macOS
qrencode -t ansiutf8 < client1.conf
```

4. Или импортируйте файл конфигурации через AirDrop
5. Активируйте туннель

### 📲 Android

1. Установите WireGuard из [Google Play](https://play.google.com/store/apps/details?id=com.wireguard.android)
2. Откройте приложение → "+" → "Create from QR code"
3. Для создания QR-кода:

```bash
# На сервере или Linux/macOS
qrencode -t ansiutf8 < client1.conf
```

4. Или импортируйте файл конфигурации
5. Активируйте туннель

---

## ✅ Проверка работы

### Windows (PowerShell)

```powershell
# Проверка подключения
ping 10.0.0.1

# Проверка публичного IP
Invoke-RestMethod -Uri "https://ifconfig.me"

# Проверка DNS
Resolve-DnsName google.com
```

### Windows (WSL2) / macOS / Linux

```bash
# Проверка WireGuard (внутри контейнера)
docker exec -it wg-singbox-gateway wg show

# Проверка соединения с клиента
ping 10.0.0.1

# Проверка маршрутизации
ip route show

# Проверка DNS
nslookup google.com

# Проверка публичного IP
curl ifconfig.me
```

---

## 🛠️ Устранение проблем

### ❌ Не запускается WireGuard

**Windows (WSL2) / macOS / Linux:**

```bash
# Проверка модуля ядра (Linux)
lsmod | grep wireguard

# Проверка прав доступа
docker exec -it wg-singbox-gateway ls -la /etc/wireguard/

# Проверка конфигурации
docker exec -it wg-singbox-gateway cat /etc/wireguard/wg0.conf

# Проверка логов
docker logs wg-singbox-gateway
```

**Windows (PowerShell):**

```powershell
# Проверка статуса контейнера
docker ps -a

# Просмотр логов
docker logs wg-singbox-gateway

# Проверка настроек WSL2
wsl --status
```

### ❌ Нет интернета на клиенте

**Windows (WSL2) / macOS / Linux:**

```bash
# Проверка NAT правил
docker exec -it wg-singbox-gateway iptables -t nat -L -n -v

# Проверка форвардинга
docker exec -it wg-singbox-gateway sysctl net.ipv4.ip_forward

# Проверка маршрутов на клиенте
ip route show

# Тест traceroute
traceroute 8.8.8.8
```

**Windows (PowerShell):**

```powershell
# Проверка маршрутов
route print

# Тест traceroute
tracert 8.8.8.8
```

### 📋 Просмотр логов

**Docker Compose:**

```bash
docker compose logs -f gateway
```

**Docker:**

```bash
docker logs -f wg-singbox-gateway
```

---

## 📚 Дополнительные команды

### Makefile (Linux/macOS)

```bash
make build          # Сборка бинарника
make clean          # Очистка
make test           # Запуск тестов
make run            # Локальный запуск
make docker-build   # Сборка Docker образа
make docker-run     # Запуск контейнера
```

### Полный сброс контейнера

```bash
# Остановка и удаление контейнера
docker compose down -v

# Пересборка образа
docker compose build --no-cache

# Запуск
docker compose up -d
```

---

## 📄 Лицензия

MIT License
