# Quick Start Guide

Get the Nomcebo Bank Auth Service up and running in 5 minutes!

## Prerequisites

Make sure you have installed:
- Java 17+ ([Download](https://adoptium.net/))
- Maven 3.8+ ([Download](https://maven.apache.org/download.cgi))
- Docker Desktop ([Download](https://www.docker.com/products/docker-desktop))

## 1. Clone the Repository

```bash
git clone https://github.com/Nqobile-Buthelezi/nomcebo-bank-auth-service.git
cd nomcebo-bank-auth-service
```

## 2. Start Keycloak

```bash
docker-compose up -d
```

This starts Keycloak on http://localhost:8090

## 3. Build the Application

```bash
mvn clean package -DskipTests
```

## 4. Run the Application

```bash
mvn spring-boot:run
```

Or run the JAR directly:

```bash
java -jar target/nomcebo-bank-auth-service-1.0-0.jar
```

## 5. Verify It's Running

Open your browser and visit:

- **Application Health**: http://localhost:8080/actuator/health
- **API Documentation**: http://localhost:8080/swagger-ui.html
- **Keycloak Admin**: http://localhost:8090/admin (admin/admin)

## 6. Test the API

### Register a New User

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "Test123!@#",
    "firstName": "Test",
    "lastName": "User",
    "phoneNumber": "+27821234567",
    "southAfricanIdNumber": "9001015009087",
    "address": "123 Test Street",
    "city": "Johannesburg",
    "province": "Gauteng",
    "postalCode": "2000"
  }'
```

### Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "Test123!@#"
  }'
```

Save the `accessToken` from the response!

### Validate Token

```bash
curl -X GET http://localhost:8080/api/auth/validate \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN_HERE"
```

## Next Steps

- 📖 Read the [API Documentation](docs/API.md)
- 🔒 Review [Security Best Practices](docs/SECURITY.md)
- ⚙️ Explore [Configuration Options](docs/CONFIGURATION.md)
- 🚀 Learn about [Deployment](docs/DEPLOYMENT.md)
- 🤝 Check [Contributing Guidelines](CONTRIBUTING.md)

## Common Issues

### Port 8080 already in use

Change the port in `application.properties`:
```properties
server.port=8081
```

### Keycloak connection failed

Make sure Keycloak is running:
```bash
docker-compose ps
```

Restart if needed:
```bash
docker-compose restart keycloak
```

### Build fails

Clean and rebuild:
```bash
mvn clean install -DskipTests
```

## Development Tips

### Hot Reload

Use Spring Boot DevTools for automatic restarts during development.

### Debug Mode

Run with debug enabled:
```bash
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"
```

Then attach your IDE debugger to port 5005.

### View Logs

```bash
tail -f logs/application.log
```

## Need Help?

- 📝 Check the [full README](README.md)
- 💬 Open an issue on GitHub
- 📧 Contact via LinkedIn

---

**Ready to build secure authentication? Let's go! 🚀**
