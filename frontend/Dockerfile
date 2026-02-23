# Multi-arch Dockerfile with improved build reliability
FROM node:20-alpine AS builder

WORKDIR /app

# Install build dependencies
RUN apk add --no-cache \
    git \
    python3 \
    make \
    g++ \
    alpine-sdk

# Copy package files
COPY package*.json package-lock.json ./

# Install dependencies (use npm ci with --legacy-peer-deps for better compatibility)
RUN npm ci --legacy-peer-deps

# Copy source files
COPY . .

# Build application
RUN npm run build

# Production stage with nginx:alpine
FROM node:20-alpine AS runner

WORKDIR /app

# Install nginx
RUN apk add --no-cache \
    nginx

# Copy built application
COPY --from=builder /app/dist /usr/share/nginx/html

# Copy nginx configuration
COPY nginx.conf /etc/nginx/nginx.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
