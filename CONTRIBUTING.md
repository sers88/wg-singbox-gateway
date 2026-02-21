# Contributing to wg-singbox-gateway

Спасибо за интерес к проекту! Вот руководство по участию в разработке.

## 🚀 Quick Start

```bash
git clone https://github.com/sersb/wg-singbox-gateway.git
cd wg-singbox-gateway

# Generate keys for testing
make generate-keys

# Run locally
docker compose up -d
```

## 📝 Разработка

### Структура проекта

```
wg-singbox-gateway/
├── cmd/gateway/        # Главный бинарник
├── internal/           # Внутренние пакеты
│   ├── config/         # Загрузка конфигурации
│   ├── gateway/        # Основная логика
│   └── wireguard/      # Управление WireGuard
├── entrypoint.sh       # Скрипт для генерации конфигов из ENV
├── Dockerfile          # Билд контейнера
├── docker-compose.yml  # Для локальной разработки
└── .github/workflows/  # CI/CD
```

### Локальный запуск

```bash
# Сборка
make build

# Запуск (требует root)
sudo ./gateway

# С Docker
docker compose up -d
docker compose logs -f
```

### Тестирование

```bash
go test -v ./...

# Покрытие кода
go test -cover ./...
```

## 🔧 GitHub Actions

Автоматический билд и пуш на DockerHub при:
- Push в `main`
- Создании тега `v*`

### Secrets

Добавьте в GitHub repository settings → Secrets:

| Имя | Описание |
|-----|----------|
| `DOCKERHUB_USERNAME` | Логин DockerHub |
| `DOCKERHUB_TOKEN` | Access token (создать на DockerHub) |

### Создание релиза

```bash
git tag v1.0.0
git push origin v1.0.0
```

Это запустит workflow и создаст GitHub релиз.

## 📦 Добавление нового прокси-протокола

1. Добавьте в `entrypoint.sh` новый case:

```bash
myproxy)
    PROXY_OUTBOUND=$(cat << 'EOF'
    {
      "type": "myproxy",
      "tag": "proxy-out",
      ...
    }
EOF
    )
    ;;
```

2. Добавьте ENV переменные в `.env.example`

3. Обновите `README.md`

## 🤝 Pull Requests

1. Форкните репозиторий
2. Создайте ветку: `git checkout -b feature/my-feature`
3. Коммитьте: `git commit -m 'Add feature'`
4. Push: `git push origin feature/my-feature`
5. Откройте PR

## 📝 Кодстайл

- Go стандартный `gofmt`
- Комментарии на английском
- Переменные camelCase
- Константы UPPER_CASE

## 🐛 Багрепорты

Используйте issue tracker. Включите:
- OS/Docker версия
- Логи
- Конфигурация (без секретов)

## 📄 Лицензия

MIT License
