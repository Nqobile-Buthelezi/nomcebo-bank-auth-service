# Nomcebo Bank Auth Service

> **Nomcebo Bank** - Identity and Access Management Service

## Overview

The Auth Service is the core identity and access management component of Nomcebo Bank's microservices architecture. 
It provides secure authentication, authorisation, and user management capabilities using Keycloak and Spring Security.

## Features

- JWT-based authentication
- Role-based access control (RBAC)
- Keycloak integration
- User registration and management
- Token validation and refresh
- Audit logging
- OpenAPI documentation with Swagger UI

## Technology Stack

- **Java 17**
- **Spring Boot 3.2.0**
- **Spring Security**
- **Keycloak 23.0.1**
- **Maven**
- **PostgreSQL/H2**
- **Docker**

## Quick Start

### Prerequisites

- Java 17+
- Maven 3.8+
- Docker & Docker Compose
- Git

### Local Development

1. **Clone the repository:**
   ```bash
   git clone https://github.com/Nqobile-Buthelezi/nomcebo-bank-auth-service.git
   cd nomcebo-bank-auth-service
   ```

2. **Start dependencies:**
   ```bash
   docker-compose up -d
   ```

3. **Run the application:**
   ```bash
   mvn spring-boot:run
   ```

4. **Access services:**
    - Application: http://localhost:8080
    - Swagger UI: http://localhost:8080/swagger-ui.html
    - Keycloak Admin: http://localhost:8090/admin
    - H2 Console: http://localhost:8080/h2-console

### Docker Deployment

```bash
# Build image
docker build -t nomcebo-bank/auth-service .

# Run container
docker run -p 8080:8080 nomcebo-bank/auth-service
```

## API Documentation

### Authentication Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/login` | User authentication |
| POST | `/api/auth/register` | User registration |
| POST | `/api/auth/refresh` | Token refresh |
| POST | `/api/auth/logout` | User logout |
| GET | `/api/auth/validate` | Token validation |

### User Management Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/users/profile` | Get user profile |
| PUT | `/api/users/profile` | Update user profile |
| GET | `/api/users/{id}` | Get user by ID |
| PUT | `/api/users/{id}/roles` | Update user roles |

## Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `KEYCLOAK_URL` | Keycloak server URL | `http://localhost:8090` |
| `KEYCLOAK_REALM` | Keycloak realm | `nomcebo-bank` |
| `KEYCLOAK_CLIENT_ID` | Client ID | `nb-auth-service` |
| `DATABASE_URL` | Database connection URL | `jdbc:h2:mem:nomcebo-bank-db` |
| `JWT_SECRET` | JWT signing secret | `your-very-secure-secret-key` |
| `MAIL_USERNAME` | Email service username | `placeholder` |

### Application Properties

```properties
# Server Configuration
server.port=8080
spring.application.name=nb-auth-service

# Database Configuration
spring.datasource.url=${DATABASE_URL:jdbc:h2:mem:nomcebo-bank-db}
spring.datasource.driver-class-name=org.h2.Driver
spring.jpa.hibernate.ddl-auto=update

# Keycloak Configuration
keycloak.auth-server-url=${KEYCLOAK_URL:http://localhost:8090}
keycloak.realm=${KEYCLOAK_REALM:nomcebo-bank}
keycloak.resource=${KEYCLOAK_CLIENT_ID:nb-auth-service}

# Keycloak Admin Configuration
keycloak.admin.username=admin
keycloak.admin.password=admin
keycloak.admin.client-secret=

# Security Configuration
jwt.secret=your-very-secure-secret-key
nomcebo.auth.max-login-attempts=5
nomcebo.auth.lockout-duration=30

# CORS Configuration
nomcebo.cors.allowed-origins=http://localhost:3000

# Documentation
springdoc.swagger-ui.path=/swagger-ui.html

# Logging
logging.level.root=info
```

## Testing

### Unit Tests
```bash
mvn test
```

### Integration Tests
```bash
mvn verify -Dspring.profiles.active=test
```

### Test Coverage
```bash
mvn jacoco:report
```

## Architecture

See [Architecture Documentation](docs/architecture.puml) for detailed system design.

## Contributing

1. Fork the repository
2. Create feature branch (`git checkout -b feature/new-feature`)
3. Commit changes (`git commit -m 'Add new feature'`)
4. Push to branch (`git push origin feature/new-feature`)
5. Open Pull Request

## Security

- All endpoints require authentication except registration
- JWT tokens expire after 15 minutes
- Refresh tokens expire after 7 days
- Failed login attempts are rate-limited
- All requests are logged for audit purposes

## Monitoring

The service exposes health checks and metrics:
- Health: `/actuator/health`
- Metrics: `/actuator/metrics`
- Info: `/actuator/info`

## License

This project is not proprietary to any bank, it is a personal development project.

## Support

For support, or clarification contact me via LinkedIn.