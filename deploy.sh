#!/bin/bash
set -e

PI_HOST="pi@192.168.1.200"
PI_KEY="$HOME/.ssh/apidego-multi"
REMOTE_DIR="/home/pi/romadmin"
SSH="ssh -i $PI_KEY $PI_HOST"

echo "==> Deploying RomAdmin to Pi..."

# Pull latest code
echo "==> Pulling latest changes..."
$SSH "cd $REMOTE_DIR && git pull"

# Build and restart
echo "==> Building Docker image..."
$SSH "cd $REMOTE_DIR && docker compose -f docker-compose.prod.yml build"

echo "==> Restarting containers..."
$SSH "cd $REMOTE_DIR && docker compose -f docker-compose.prod.yml up -d"

# Run migrations
echo "==> Running database migrations..."
sleep 5  # wait for containers to be ready
$SSH "docker exec romadmin sh -c 'cd packages/backend && node ../../node_modules/.pnpm/prisma@*/node_modules/prisma/build/index.js migrate deploy'"

echo "==> Done! RomAdmin is live at http://192.168.1.200:3083"
