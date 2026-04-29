#!/bin/bash
# CalSnap production deploy script
# Usage: ./deploy.sh
# Run from your local machine — SSHes to server and sets everything up.

set -e

SERVER="root@72.61.208.66"
REPO="https://github.com/sragen/calories-tracker.git"
APP_DIR="/opt/calsnap"

echo "==> Connecting to $SERVER..."

ssh -o StrictHostKeyChecking=no "$SERVER" bash -s << 'REMOTE'
set -e

echo "==> [1/6] Installing Docker..."
if ! command -v docker &>/dev/null; then
  apt-get update -qq
  apt-get install -y -qq ca-certificates curl gnupg
  install -m 0755 -d /etc/apt/keyrings
  curl -fsSL https://download.docker.com/linux/ubuntu/gpg | gpg --dearmor -o /etc/apt/keyrings/docker.gpg
  chmod a+r /etc/apt/keyrings/docker.gpg
  echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] \
    https://download.docker.com/linux/ubuntu $(. /etc/os-release && echo "$VERSION_CODENAME") stable" \
    > /etc/apt/sources.list.d/docker.list
  apt-get update -qq
  apt-get install -y -qq docker-ce docker-ce-cli containerd.io docker-compose-plugin
  systemctl enable docker --now
  echo "Docker installed: $(docker --version)"
else
  echo "Docker already installed: $(docker --version)"
fi

echo "==> [2/6] Cloning / updating repo..."
if [ -d /opt/calsnap/.git ]; then
  git -C /opt/calsnap pull origin master
else
  git clone https://github.com/sragen/calories-tracker.git /opt/calsnap
fi
cd /opt/calsnap

echo "==> [3/6] Installing Certbot..."
if ! command -v certbot &>/dev/null; then
  apt-get install -y -qq certbot
fi

echo "==> [4/6] Checking .env..."
if [ ! -f /opt/calsnap/.env ]; then
  echo ""
  echo "ERROR: /opt/calsnap/.env not found!"
  echo "Copy .env.example to .env and fill in your secrets:"
  echo "  cp /opt/calsnap/.env.example /opt/calsnap/.env && nano /opt/calsnap/.env"
  exit 1
fi

echo "==> [5/6] Building backend JAR..."
cd /opt/calsnap/backend
chmod +x gradlew
./gradlew bootJar -q

echo "==> [6/6] Starting containers (DB + MinIO + Backend + Admin)..."
cd /opt/calsnap

# Start without nginx first (so certbot can get certs)
docker compose up -d postgres minio backend admin
echo "Waiting 20s for services to start..."
sleep 20

echo "==> Requesting SSL certificates..."
docker compose stop nginx 2>/dev/null || true
certbot certonly --standalone \
  --non-interactive --agree-tos --email admin@adikur.com \
  -d api.adikur.com -d admin.adikur.com || \
  echo "WARNING: Certbot failed — check that DNS A records point to this server IP"

echo "==> Starting Nginx..."
docker compose up -d nginx

echo ""
echo "========================================"
echo " CalSnap is live!"
echo "  API:   https://api.adikur.com"
echo "  Admin: https://admin.adikur.com"
echo "========================================"
REMOTE
