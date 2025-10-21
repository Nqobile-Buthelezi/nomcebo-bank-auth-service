# Deployment Guide

## Overview

This guide provides step-by-step instructions for deploying the Nomcebo Bank Auth Service to various environments.

---

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Local Deployment](#local-deployment)
3. [Docker Deployment](#docker-deployment)
4. [Production Deployment](#production-deployment)
5. [Cloud Deployment](#cloud-deployment)
6. [Monitoring and Maintenance](#monitoring-and-maintenance)

---

## Prerequisites

### Required Software

- **Java 17+** (OpenJDK or Oracle JDK)
- **Maven 3.8+**
- **Docker & Docker Compose** (for containerized deployment)
- **PostgreSQL 15+** (for production)
- **Keycloak 24+** (for identity management)

### Optional Software

- **Nginx** or **Apache** (reverse proxy)
- **Let's Encrypt** (SSL certificates)
- **Prometheus** (metrics collection)
- **Grafana** (monitoring dashboards)

---

## Local Deployment

### Step 1: Clone Repository

```bash
git clone https://github.com/Nqobile-Buthelezi/nomcebo-bank-auth-service.git
cd nomcebo-bank-auth-service
```

### Step 2: Start Dependencies

```bash
docker-compose up -d
```

This starts:
- Keycloak on port 8090
- PostgreSQL (if configured)

### Step 3: Build Application

```bash
mvn clean package -DskipTests
```

### Step 4: Run Application

```bash
mvn spring-boot:run
```

Or run the JAR directly:

```bash
java -jar target/nomcebo-bank-auth-service-1.0-0.jar
```

### Step 5: Verify Deployment

```bash
# Health check
curl http://localhost:8080/actuator/health

# Expected response: {"status":"UP"}
```

Access Swagger UI: http://localhost:8080/swagger-ui.html

---

## Docker Deployment

### Build Docker Image

```bash
# Build the image
docker build -t nomcebo-bank/auth-service:1.0.0 .

# Verify image
docker images | grep nomcebo-bank
```

### Run Single Container

```bash
docker run -d \
  --name nomcebo-auth \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DATABASE_URL=jdbc:postgresql://host.docker.internal:5432/nomcebo_bank \
  -e DB_USERNAME=postgres \
  -e DB_PASSWORD=your-password \
  -e JWT_SECRET=your-secret-key \
  nomcebo-bank/auth-service:1.0.0
```

### Using Docker Compose

**docker-compose.prod.yml:**

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:15-alpine
    container_name: nomcebo-postgres
    environment:
      POSTGRES_DB: nomcebo_bank
      POSTGRES_USER: ${DB_USERNAME}
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    volumes:
      - postgres-data:/var/lib/postgresql/data
      - ./init-db.sql:/docker-entrypoint-initdb.d/init.sql
    ports:
      - "5432:5432"
    restart: unless-stopped
    networks:
      - nomcebo-network

  keycloak:
    image: quay.io/keycloak/keycloak:24.0.2
    container_name: nomcebo-keycloak
    command: start --optimized
    environment:
      KC_DB: postgres
      KC_DB_URL: jdbc:postgresql://postgres:5432/keycloak
      KC_DB_USERNAME: ${DB_USERNAME}
      KC_DB_PASSWORD: ${DB_PASSWORD}
      KEYCLOAK_ADMIN: ${KEYCLOAK_ADMIN_USERNAME}
      KEYCLOAK_ADMIN_PASSWORD: ${KEYCLOAK_ADMIN_PASSWORD}
      KC_HOSTNAME: ${KEYCLOAK_HOSTNAME}
      KC_PROXY: edge
      KC_HTTP_ENABLED: true
      KC_HEALTH_ENABLED: true
    ports:
      - "8090:8080"
    depends_on:
      - postgres
    restart: unless-stopped
    networks:
      - nomcebo-network

  auth-service:
    build: .
    container_name: nomcebo-auth-service
    environment:
      SPRING_PROFILES_ACTIVE: prod
      DATABASE_URL: jdbc:postgresql://postgres:5432/nomcebo_bank
      DB_USERNAME: ${DB_USERNAME}
      DB_PASSWORD: ${DB_PASSWORD}
      KEYCLOAK_URL: http://keycloak:8080
      KEYCLOAK_REALM: ${KEYCLOAK_REALM}
      KEYCLOAK_CLIENT_ID: ${KEYCLOAK_CLIENT_ID}
      KEYCLOAK_ADMIN_USERNAME: ${KEYCLOAK_ADMIN_USERNAME}
      KEYCLOAK_ADMIN_PASSWORD: ${KEYCLOAK_ADMIN_PASSWORD}
      JWT_SECRET: ${JWT_SECRET}
      CORS_ALLOWED_ORIGINS: ${CORS_ALLOWED_ORIGINS}
    ports:
      - "8080:8080"
    depends_on:
      - postgres
      - keycloak
    restart: unless-stopped
    networks:
      - nomcebo-network
    healthcheck:
      test: ["CMD", "wget", "--no-verbose", "--tries=1", "--spider", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 40s

volumes:
  postgres-data:

networks:
  nomcebo-network:
    driver: bridge
```

**Deploy:**

```bash
# Create .env file with secrets
cp .env.example .env
# Edit .env with actual values

# Start all services
docker-compose -f docker-compose.prod.yml up -d

# View logs
docker-compose -f docker-compose.prod.yml logs -f

# Check status
docker-compose -f docker-compose.prod.yml ps
```

---

## Production Deployment

### Server Requirements

**Minimum:**
- 2 CPU cores
- 4 GB RAM
- 20 GB storage
- Ubuntu 20.04+ or RHEL 8+

**Recommended:**
- 4 CPU cores
- 8 GB RAM
- 50 GB SSD storage
- Ubuntu 22.04 LTS

### Step 1: Prepare Server

```bash
# Update system
sudo apt update && sudo apt upgrade -y

# Install Java 17
sudo apt install openjdk-17-jdk -y

# Verify Java version
java -version

# Install PostgreSQL
sudo apt install postgresql postgresql-contrib -y

# Install Docker (optional)
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# Install Docker Compose
sudo apt install docker-compose -y
```

### Step 2: Configure Database

```bash
# Switch to postgres user
sudo -u postgres psql

# Create database and user
CREATE DATABASE nomcebo_bank;
CREATE USER nomcebo_user WITH ENCRYPTED PASSWORD 'your-secure-password';
GRANT ALL PRIVILEGES ON DATABASE nomcebo_bank TO nomcebo_user;

# Exit psql
\q
```

### Step 3: Configure Firewall

```bash
# Allow SSH
sudo ufw allow 22/tcp

# Allow HTTP/HTTPS (if using reverse proxy)
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp

# Allow application port (or keep internal behind proxy)
# sudo ufw allow 8080/tcp

# Enable firewall
sudo ufw enable
```

### Step 4: Deploy Application

**Option A: Direct JAR Deployment**

```bash
# Create application user
sudo useradd -r -m -s /bin/bash nomcebo

# Create application directory
sudo mkdir -p /opt/nomcebo-bank/auth-service
sudo chown nomcebo:nomcebo /opt/nomcebo-bank/auth-service

# Copy JAR file
sudo cp target/nomcebo-bank-auth-service-1.0-0.jar /opt/nomcebo-bank/auth-service/

# Create configuration
sudo mkdir -p /etc/nomcebo-bank
sudo nano /etc/nomcebo-bank/application-prod.properties
```

**Create systemd service:**

```bash
sudo nano /etc/systemd/system/nomcebo-auth.service
```

```ini
[Unit]
Description=Nomcebo Bank Auth Service
After=network.target postgresql.service

[Service]
Type=simple
User=nomcebo
Group=nomcebo
WorkingDirectory=/opt/nomcebo-bank/auth-service
ExecStart=/usr/bin/java -jar \
  -Dspring.profiles.active=prod \
  -Dspring.config.location=/etc/nomcebo-bank/application-prod.properties \
  /opt/nomcebo-bank/auth-service/nomcebo-bank-auth-service-1.0-0.jar

SuccessExitStatus=143
TimeoutStopSec=10
Restart=on-failure
RestartSec=5

[Install]
WantedBy=multi-user.target
```

**Start service:**

```bash
# Reload systemd
sudo systemctl daemon-reload

# Start service
sudo systemctl start nomcebo-auth

# Enable auto-start
sudo systemctl enable nomcebo-auth

# Check status
sudo systemctl status nomcebo-auth

# View logs
sudo journalctl -u nomcebo-auth -f
```

**Option B: Docker Deployment**

```bash
# Pull or build image
docker pull nomcebo-bank/auth-service:1.0.0
# OR
docker build -t nomcebo-bank/auth-service:1.0.0 .

# Run container
docker run -d \
  --name nomcebo-auth \
  --restart unless-stopped \
  -p 8080:8080 \
  -v /etc/nomcebo-bank/application-prod.properties:/app/application-prod.properties \
  -e SPRING_PROFILES_ACTIVE=prod \
  nomcebo-bank/auth-service:1.0.0
```

### Step 5: Configure Reverse Proxy (Nginx)

```bash
# Install Nginx
sudo apt install nginx -y

# Create site configuration
sudo nano /etc/nginx/sites-available/nomcebo-auth
```

```nginx
server {
    listen 80;
    server_name auth.yourdomain.com;

    # Redirect HTTP to HTTPS
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name auth.yourdomain.com;

    # SSL Configuration
    ssl_certificate /etc/letsencrypt/live/auth.yourdomain.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/auth.yourdomain.com/privkey.pem;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;
    ssl_prefer_server_ciphers on;

    # Security Headers
    add_header Strict-Transport-Security "max-age=31536000; includeSubDomains" always;
    add_header X-Frame-Options "DENY" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header X-XSS-Protection "1; mode=block" always;

    # Proxy settings
    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # WebSocket support (if needed)
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
    }

    # Health check endpoint
    location /actuator/health {
        proxy_pass http://localhost:8080/actuator/health;
        access_log off;
    }
}
```

**Enable site and SSL:**

```bash
# Enable site
sudo ln -s /etc/nginx/sites-available/nomcebo-auth /etc/nginx/sites-enabled/

# Install Certbot for Let's Encrypt
sudo apt install certbot python3-certbot-nginx -y

# Obtain SSL certificate
sudo certbot --nginx -d auth.yourdomain.com

# Test Nginx configuration
sudo nginx -t

# Reload Nginx
sudo systemctl reload nginx
```

### Step 6: Configure Monitoring

**Install Prometheus:**

```bash
# Download and install Prometheus
wget https://github.com/prometheus/prometheus/releases/download/v2.40.0/prometheus-2.40.0.linux-amd64.tar.gz
tar xvfz prometheus-*.tar.gz
sudo mv prometheus-2.40.0.linux-amd64 /opt/prometheus

# Create configuration
sudo nano /opt/prometheus/prometheus.yml
```

```yaml
global:
  scrape_interval: 15s

scrape_configs:
  - job_name: 'nomcebo-auth'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['localhost:8080']
```

---

## Cloud Deployment

### AWS Deployment

**Using EC2:**

1. Launch EC2 instance (t3.medium recommended)
2. Configure Security Groups (ports 22, 80, 443)
3. Follow [Production Deployment](#production-deployment) steps

**Using ECS:**

```yaml
# task-definition.json
{
  "family": "nomcebo-auth-service",
  "networkMode": "awsvpc",
  "requiresCompatibilities": ["FARGATE"],
  "cpu": "1024",
  "memory": "2048",
  "containerDefinitions": [
    {
      "name": "auth-service",
      "image": "nomcebo-bank/auth-service:1.0.0",
      "portMappings": [
        {
          "containerPort": 8080,
          "protocol": "tcp"
        }
      ],
      "environment": [
        {"name": "SPRING_PROFILES_ACTIVE", "value": "prod"}
      ],
      "secrets": [
        {"name": "DATABASE_URL", "valueFrom": "arn:aws:secretsmanager:..."},
        {"name": "JWT_SECRET", "valueFrom": "arn:aws:secretsmanager:..."}
      ]
    }
  ]
}
```

### Azure Deployment

**Using Azure App Service:**

```bash
# Login to Azure
az login

# Create resource group
az group create --name nomcebo-rg --location eastus

# Create App Service plan
az appservice plan create \
  --name nomcebo-plan \
  --resource-group nomcebo-rg \
  --sku B2 \
  --is-linux

# Create web app
az webapp create \
  --resource-group nomcebo-rg \
  --plan nomcebo-plan \
  --name nomcebo-auth \
  --runtime "JAVA|17-java17"

# Configure app settings
az webapp config appsettings set \
  --resource-group nomcebo-rg \
  --name nomcebo-auth \
  --settings \
    SPRING_PROFILES_ACTIVE=prod \
    DATABASE_URL=${DATABASE_URL} \
    JWT_SECRET=${JWT_SECRET}

# Deploy JAR
az webapp deploy \
  --resource-group nomcebo-rg \
  --name nomcebo-auth \
  --src-path target/nomcebo-bank-auth-service-1.0-0.jar \
  --type jar
```

---

## Monitoring and Maintenance

### Health Checks

```bash
# Application health
curl https://auth.yourdomain.com/actuator/health

# Detailed health (requires authentication)
curl -H "Authorization: Bearer <token>" \
  https://auth.yourdomain.com/actuator/health
```

### Log Management

```bash
# View application logs
sudo journalctl -u nomcebo-auth -f

# Search for errors
sudo journalctl -u nomcebo-auth | grep ERROR

# Log rotation (automatically configured)
sudo nano /etc/logrotate.d/nomcebo-auth
```

```
/var/log/nomcebo-bank/auth-service.log {
    daily
    rotate 30
    compress
    delaycompress
    missingok
    notifempty
    create 0644 nomcebo nomcebo
}
```

### Backup Strategy

**Database Backup:**

```bash
# Create backup script
sudo nano /opt/nomcebo-bank/backup.sh
```

```bash
#!/bin/bash
BACKUP_DIR="/var/backups/nomcebo-bank"
DATE=$(date +%Y%m%d_%H%M%S)

# Create backup directory
mkdir -p $BACKUP_DIR

# Backup PostgreSQL database
pg_dump -U nomcebo_user nomcebo_bank > $BACKUP_DIR/nomcebo_bank_$DATE.sql

# Compress backup
gzip $BACKUP_DIR/nomcebo_bank_$DATE.sql

# Keep only last 30 days of backups
find $BACKUP_DIR -name "*.sql.gz" -mtime +30 -delete
```

**Schedule with cron:**

```bash
# Edit crontab
sudo crontab -e

# Add daily backup at 2 AM
0 2 * * * /opt/nomcebo-bank/backup.sh
```

### Updates and Upgrades

```bash
# 1. Backup current version
sudo systemctl stop nomcebo-auth

# 2. Backup database
sudo -u postgres pg_dump nomcebo_bank > backup.sql

# 3. Deploy new version
sudo cp new-version.jar /opt/nomcebo-bank/auth-service/

# 4. Start service
sudo systemctl start nomcebo-auth

# 5. Verify deployment
curl http://localhost:8080/actuator/health

# 6. Monitor logs
sudo journalctl -u nomcebo-auth -f
```

### Troubleshooting

**Service won't start:**
```bash
# Check logs
sudo journalctl -u nomcebo-auth -n 100

# Verify configuration
java -jar app.jar --spring.config.location=/etc/nomcebo-bank/application-prod.properties

# Check port availability
sudo netstat -tulpn | grep 8080
```

**Database connection issues:**
```bash
# Test database connectivity
psql -h localhost -U nomcebo_user -d nomcebo_bank

# Check PostgreSQL status
sudo systemctl status postgresql
```

**High memory usage:**
```bash
# Adjust JVM settings
JAVA_OPTS="-Xms512m -Xmx2g -XX:+UseG1GC"
```

---

## Security Checklist

Before going to production:

- [ ] Change all default passwords
- [ ] Use strong JWT secret (minimum 256 bits)
- [ ] Enable HTTPS/SSL
- [ ] Configure firewall rules
- [ ] Set up database backups
- [ ] Enable audit logging
- [ ] Configure monitoring and alerts
- [ ] Restrict CORS to production domains
- [ ] Use environment variables for secrets
- [ ] Enable rate limiting
- [ ] Set up log rotation
- [ ] Configure health checks
- [ ] Test disaster recovery procedures

---

## Support

For deployment assistance, consult:
- [Configuration Guide](CONFIGURATION.md)
- [Security Best Practices](SECURITY.md)
- [API Documentation](API.md)

For issues, contact via LinkedIn or open a GitHub issue.
