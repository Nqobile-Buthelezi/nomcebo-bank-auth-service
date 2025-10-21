# Configuration Guide

## Overview

This guide covers configuration options for the Nomcebo Bank Auth Service across different environments.

---

## Environment Profiles

Spring Boot supports multiple profiles for different environments. Use the `spring.profiles.active` property to activate a specific profile.

### Available Profiles

- **default** - Local development with H2 database
- **dev** - Development environment with PostgreSQL
- **test** - Testing environment with in-memory database
- **prod** - Production environment with full security

---

## Application Configuration

### Base Configuration (application.properties)

```properties
# Server Configuration
server.port=8080
spring.application.name=nb-auth-service

# Database Configuration
spring.datasource.url=${DATABASE_URL:jdbc:h2:mem:nomcebo-bank-db}
spring.datasource.driver-class-name=org.h2.Driver
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=true

# Keycloak Configuration
keycloak.auth-server-url=${KEYCLOAK_URL:http://localhost:8090}
keycloak.realm=${KEYCLOAK_REALM:nomcebo-bank}
keycloak.resource=${KEYCLOAK_CLIENT_ID:nb-auth-service}

# Keycloak Admin Configuration
keycloak.admin.username=${KEYCLOAK_ADMIN_USERNAME:admin}
keycloak.admin.password=${KEYCLOAK_ADMIN_PASSWORD:admin}
keycloak.admin.client-secret=${KEYCLOAK_CLIENT_SECRET:}

# JWT Configuration
jwt.secret=${JWT_SECRET:your-very-secure-secret-key-change-in-production}

# Security Configuration
nomcebo.auth.max-login-attempts=${MAX_LOGIN_ATTEMPTS:5}
nomcebo.auth.lockout-duration=${LOCKOUT_DURATION_MINUTES:30}

# CORS Configuration
nomcebo.cors.allowed-origins=${CORS_ALLOWED_ORIGINS:http://localhost:3000}

# API Documentation
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.api-docs.path=/api-docs

# Logging Configuration
logging.level.root=info
logging.level.za.co.nomcebo.bank.auth=debug
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} - %msg%n

# Actuator Configuration
management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.show-details=when-authorized
management.health.defaults.enabled=true
```

---

## Development Environment

### application-dev.properties

```properties
# Development-specific settings
spring.profiles.active=dev

# PostgreSQL Database
spring.datasource.url=${DATABASE_URL:jdbc:postgresql://localhost:5432/nomcebo_bank_dev}
spring.datasource.driver-class-name=org.postgresql.Driver
spring.datasource.username=${DB_USERNAME:postgres}
spring.datasource.password=${DB_PASSWORD:postgres}

# JPA Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# H2 Console (for debugging)
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# Keycloak Dev Settings
keycloak.auth-server-url=http://localhost:8090
keycloak.realm=nomcebo-bank-dev
keycloak.resource=nb-auth-service-dev

# Development JWT Secret (Use strong secret in production)
jwt.secret=dev-secret-key-this-should-be-very-long-and-random

# CORS - Allow local frontend
nomcebo.cors.allowed-origins=http://localhost:3000,http://localhost:4200

# Logging - Verbose for development
logging.level.root=info
logging.level.za.co.nomcebo.bank.auth=debug
logging.level.org.springframework.security=debug
logging.level.org.hibernate.SQL=debug
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=trace

# Actuator - All endpoints exposed for development
management.endpoints.web.exposure.include=*
management.endpoint.health.show-details=always

# Email Configuration (for development - use MailHog or similar)
spring.mail.host=localhost
spring.mail.port=1025
spring.mail.username=
spring.mail.password=
spring.mail.properties.mail.smtp.auth=false
spring.mail.properties.mail.smtp.starttls.enable=false
```

### Docker Compose for Development

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:15-alpine
    container_name: nomcebo-bank-postgres-dev
    environment:
      POSTGRES_DB: nomcebo_bank_dev
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5432:5432"
    volumes:
      - postgres-data:/var/lib/postgresql/data

  keycloak:
    image: quay.io/keycloak/keycloak:24.0.2
    container_name: nomcebo-bank-keycloak-dev
    command: start-dev
    environment:
      KEYCLOAK_ADMIN: admin
      KEYCLOAK_ADMIN_PASSWORD: admin
      KC_DB: postgres
      KC_DB_URL: jdbc:postgresql://postgres:5432/keycloak
      KC_DB_USERNAME: postgres
      KC_DB_PASSWORD: postgres
    ports:
      - "8090:8080"
    depends_on:
      - postgres

  mailhog:
    image: mailhog/mailhog:latest
    container_name: nomcebo-bank-mailhog
    ports:
      - "1025:1025"  # SMTP
      - "8025:8025"  # Web UI

volumes:
  postgres-data:
```

---

## Testing Environment

### application-test.properties

```properties
# Testing-specific settings
spring.profiles.active=test

# In-memory H2 Database for tests
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# JPA Configuration
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect

# Disable Keycloak for unit tests (use mocks)
keycloak.enabled=false

# Test JWT Secret
jwt.secret=test-secret-key-for-unit-tests-only

# Disable security for specific tests
spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration

# Logging - Minimal for tests
logging.level.root=error
logging.level.za.co.nomcebo.bank.auth=info

# Disable Actuator for tests
management.endpoints.web.exposure.include=health

# Disable email for tests
spring.mail.host=localhost
spring.mail.port=25
```

---

## Production Environment

### application-prod.properties

```properties
# Production settings
spring.profiles.active=prod

# PostgreSQL Database (use environment variables)
spring.datasource.url=${DATABASE_URL}
spring.datasource.driver-class-name=org.postgresql.Driver
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

# JPA Configuration
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# Connection Pool
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.idle-timeout=600000
spring.datasource.hikari.max-lifetime=1800000

# Keycloak Production Settings
keycloak.auth-server-url=${KEYCLOAK_URL}
keycloak.realm=${KEYCLOAK_REALM}
keycloak.resource=${KEYCLOAK_CLIENT_ID}
keycloak.admin.username=${KEYCLOAK_ADMIN_USERNAME}
keycloak.admin.password=${KEYCLOAK_ADMIN_PASSWORD}
keycloak.admin.client-secret=${KEYCLOAK_CLIENT_SECRET}

# JWT Configuration - MUST use strong secret
jwt.secret=${JWT_SECRET}

# Security Settings
nomcebo.auth.max-login-attempts=5
nomcebo.auth.lockout-duration=30

# CORS - Restrict to production domain
nomcebo.cors.allowed-origins=${CORS_ALLOWED_ORIGINS}

# SSL/TLS Configuration
server.ssl.enabled=true
server.ssl.key-store=${SSL_KEYSTORE_PATH}
server.ssl.key-store-password=${SSL_KEYSTORE_PASSWORD}
server.ssl.key-store-type=PKCS12
server.ssl.key-alias=nomcebo-bank

# Logging - Structured JSON logs
logging.level.root=warn
logging.level.za.co.nomcebo.bank.auth=info
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n
logging.file.name=/var/log/nomcebo-bank/auth-service.log
logging.file.max-size=10MB
logging.file.max-history=30

# Actuator - Limited exposure
management.endpoints.web.exposure.include=health,metrics,info
management.endpoint.health.show-details=when-authorized
management.metrics.export.prometheus.enabled=true

# Email Configuration (Production SMTP)
spring.mail.host=${SMTP_HOST}
spring.mail.port=${SMTP_PORT:587}
spring.mail.username=${SMTP_USERNAME}
spring.mail.password=${SMTP_PASSWORD}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true
```

### Environment Variables for Production

Create a `.env` file (DO NOT commit to version control):

```bash
# Database
DATABASE_URL=jdbc:postgresql://db-host:5432/nomcebo_bank_prod
DB_USERNAME=nomcebo_user
DB_PASSWORD=super-secure-db-password

# Keycloak
KEYCLOAK_URL=https://keycloak.yourdomain.com
KEYCLOAK_REALM=nomcebo-bank
KEYCLOAK_CLIENT_ID=nb-auth-service
KEYCLOAK_ADMIN_USERNAME=admin
KEYCLOAK_ADMIN_PASSWORD=secure-admin-password
KEYCLOAK_CLIENT_SECRET=your-keycloak-client-secret

# JWT
JWT_SECRET=your-very-long-random-secret-minimum-256-bits

# Security
MAX_LOGIN_ATTEMPTS=5
LOCKOUT_DURATION_MINUTES=30

# CORS
CORS_ALLOWED_ORIGINS=https://yourdomain.com,https://www.yourdomain.com

# SSL
SSL_KEYSTORE_PATH=/path/to/keystore.p12
SSL_KEYSTORE_PASSWORD=keystore-password

# Email
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USERNAME=noreply@yourdomain.com
SMTP_PASSWORD=smtp-password
```

---

## Docker Configuration

### Production Dockerfile

```dockerfile
FROM maven:3.9.5-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
LABEL maintainer="Nqobile Thabo Buthelezi"
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
RUN chown -R appuser:appgroup /app
USER appuser
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

### Production Docker Compose

```yaml
version: '3.8'

services:
  auth-service:
    build: .
    container_name: nomcebo-bank-auth-service
    environment:
      SPRING_PROFILES_ACTIVE: prod
      DATABASE_URL: jdbc:postgresql://postgres:5432/nomcebo_bank_prod
      DB_USERNAME: ${DB_USERNAME}
      DB_PASSWORD: ${DB_PASSWORD}
      KEYCLOAK_URL: http://keycloak:8080
      JWT_SECRET: ${JWT_SECRET}
    ports:
      - "8080:8080"
    depends_on:
      - postgres
      - keycloak
    restart: unless-stopped
    networks:
      - nomcebo-network

  postgres:
    image: postgres:15-alpine
    container_name: nomcebo-bank-postgres
    environment:
      POSTGRES_DB: nomcebo_bank_prod
      POSTGRES_USER: ${DB_USERNAME}
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    volumes:
      - postgres-data:/var/lib/postgresql/data
    restart: unless-stopped
    networks:
      - nomcebo-network

  keycloak:
    image: quay.io/keycloak/keycloak:24.0.2
    container_name: nomcebo-bank-keycloak
    command: start
    environment:
      KC_DB: postgres
      KC_DB_URL: jdbc:postgresql://postgres:5432/keycloak
      KC_DB_USERNAME: ${DB_USERNAME}
      KC_DB_PASSWORD: ${DB_PASSWORD}
      KEYCLOAK_ADMIN: ${KEYCLOAK_ADMIN_USERNAME}
      KEYCLOAK_ADMIN_PASSWORD: ${KEYCLOAK_ADMIN_PASSWORD}
      KC_HOSTNAME: keycloak.yourdomain.com
      KC_PROXY: edge
    ports:
      - "8090:8080"
    depends_on:
      - postgres
    restart: unless-stopped
    networks:
      - nomcebo-network

volumes:
  postgres-data:

networks:
  nomcebo-network:
    driver: bridge
```

---

## Configuration Tips

### Security Best Practices

1. **Never commit secrets** to version control
2. **Use strong JWT secrets** (minimum 256 bits)
3. **Rotate credentials regularly**
4. **Use environment variables** for all sensitive data
5. **Enable HTTPS** in production
6. **Restrict CORS** to known domains
7. **Monitor and log** security events

### Performance Tuning

```properties
# Connection Pool
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5

# JVM Options
JAVA_OPTS=-Xms512m -Xmx2g -XX:+UseG1GC
```

### Monitoring

```properties
# Prometheus Metrics
management.metrics.export.prometheus.enabled=true
management.endpoint.prometheus.enabled=true

# Custom Metrics
management.metrics.tags.application=${spring.application.name}
management.metrics.tags.environment=${spring.profiles.active}
```

---

## Troubleshooting

### Common Issues

**Issue: Database connection failed**
```
Solution: Verify DATABASE_URL, DB_USERNAME, and DB_PASSWORD
Check network connectivity to database host
```

**Issue: Keycloak connection failed**
```
Solution: Ensure Keycloak is running
Verify KEYCLOAK_URL is correct
Check realm name matches configuration
```

**Issue: JWT validation fails**
```
Solution: Verify JWT_SECRET matches between instances
Check token expiration times
Ensure clocks are synchronized
```

---

## Additional Resources

- [Spring Boot Configuration Reference](https://docs.spring.io/spring-boot/docs/current/reference/html/application-properties.html)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [Keycloak Documentation](https://www.keycloak.org/documentation)
