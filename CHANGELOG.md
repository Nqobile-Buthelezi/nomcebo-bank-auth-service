# Changelog

All notable changes to the Nomcebo Bank Auth Service will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0] - 2025-10-21

### Added

#### Core Features
- JWT-based authentication with HS512 signing
- Keycloak integration for identity management
- User registration with South African ID validation
- User login with rate limiting and account lockout
- Token refresh mechanism
- Token validation endpoint
- User logout with session invalidation
- Password reset functionality
- Comprehensive audit logging for security compliance

#### Security Features
- BCrypt password hashing
- Failed login attempt tracking (max 5 attempts)
- Account lockout mechanism (30-minute duration)
- IP address tracking for security monitoring
- POPIA (Protection of Personal Information Act) compliance
- Role-based access control (RBAC)
- CORS configuration for security
- Input validation on all endpoints

#### South African Banking Features
- SA ID number format validation
- SA ID checksum validation (Luhn algorithm)
- Date of birth extraction from SA ID
- Gender identification from SA ID
- Citizenship status verification
- Age calculation utilities

#### API Endpoints
- POST `/api/auth/register` - User registration
- POST `/api/auth/login` - User authentication
- POST `/api/auth/refresh` - Token refresh
- GET `/api/auth/validate` - Token validation
- POST `/api/auth/logout` - User logout
- POST `/api/auth/reset-password` - Password reset

#### Documentation
- Comprehensive README with project overview
- Quick Start Guide for rapid setup
- Complete API documentation with examples
- Security best practices guide
- Configuration guide for all environments
- Deployment guide (local, Docker, cloud)
- Contributing guidelines with code standards
- Architecture documentation (PlantUML diagram)
- JavaDoc comments on all public classes and methods

#### Development & Deployment
- Multi-stage Dockerfile for optimized builds
- Docker Compose configuration for development
- Environment variable configuration support
- Health check endpoints via Spring Actuator
- Swagger/OpenAPI documentation
- Maven build configuration
- JaCoCo code coverage integration

#### Infrastructure
- PostgreSQL database support
- H2 in-memory database for development
- Spring Boot 3.5.0 framework
- Keycloak 25.0.3 integration
- Prometheus metrics export support

### Technical Stack
- Java 17
- Spring Boot 3.5.0
- Spring Security
- Spring Data JPA
- Keycloak 25.0.3
- PostgreSQL/H2 Database
- JWT (JSON Web Tokens)
- Maven
- Docker
- Lombok
- SpringDoc OpenAPI

### Security
- All passwords hashed with BCrypt (cost factor: 10)
- JWT tokens expire after 15 minutes (access tokens)
- Refresh tokens expire after 7 days
- Password reset tokens expire after 1 hour
- All sensitive operations logged for audit
- Environment variables for secret management

### Compliance
- POPIA (Protection of Personal Information Act) compliant
- South African banking regulations support
- Comprehensive audit trail for all authentication events
- Secure data storage and transmission

---

## [Unreleased]

### Planned Features
- Email verification for new accounts
- Multi-factor authentication (MFA/2FA)
- OAuth2 social login integration
- Password complexity requirements configuration
- Account activity monitoring dashboard
- Automated security alerts
- API rate limiting per endpoint
- Session management improvements
- User profile management endpoints
- Role and permission management API

---

## Version History

- **1.0.0** (2025-10-21) - Initial production-ready release with complete authentication system

---

## Migration Guides

### From Development to Production

When deploying to production for the first time:

1. Change all default passwords
2. Generate strong JWT secret (minimum 256 bits)
3. Configure production database
4. Set up SSL/TLS certificates
5. Configure proper CORS origins
6. Enable proper logging levels
7. Set up monitoring and alerts

See [Deployment Guide](docs/DEPLOYMENT.md) for detailed instructions.

---

## Support

For questions, issues, or contributions:
- GitHub Issues: https://github.com/Nqobile-Buthelezi/nomcebo-bank-auth-service/issues
- Documentation: See [docs/](docs/) directory
- Contact: LinkedIn profile

---

## License

This project is a personal development project and is not proprietary to any bank.
