# Contributing to Nomcebo Bank Auth Service

Thank you for your interest in contributing to the Nomcebo Bank Auth Service! This document provides guidelines and instructions for contributing to this project.

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [Development Setup](#development-setup)
- [Project Structure](#project-structure)
- [Coding Standards](#coding-standards)
- [Testing Guidelines](#testing-guidelines)
- [Commit Message Guidelines](#commit-message-guidelines)
- [Pull Request Process](#pull-request-process)
- [Security Considerations](#security-considerations)

---

## Code of Conduct

This project adheres to professional standards of conduct. All contributors are expected to:

- Be respectful and inclusive
- Provide constructive feedback
- Focus on what is best for the community
- Show empathy towards other community members

---

## Getting Started

### Prerequisites

Before you begin, ensure you have the following installed:

- **Java 17 or higher** - [Download](https://adoptium.net/)
- **Maven 3.8+** - [Download](https://maven.apache.org/download.cgi)
- **Docker & Docker Compose** - [Download](https://www.docker.com/products/docker-desktop)
- **Git** - [Download](https://git-scm.com/downloads)
- **IDE** - IntelliJ IDEA, Eclipse, or VS Code with Java extensions

### Fork and Clone

1. Fork the repository on GitHub
2. Clone your fork locally:
   ```bash
   git clone https://github.com/YOUR-USERNAME/nomcebo-bank-auth-service.git
   cd nomcebo-bank-auth-service
   ```
3. Add the upstream repository:
   ```bash
   git remote add upstream https://github.com/Nqobile-Buthelezi/nomcebo-bank-auth-service.git
   ```

---

## Development Setup

### 1. Start Dependencies

Start Keycloak and other required services:

```bash
docker-compose up -d
```

### 2. Build the Project

```bash
mvn clean install
```

### 3. Run the Application

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`.

### 4. Verify Setup

- Application Health: http://localhost:8080/actuator/health
- Swagger UI: http://localhost:8080/swagger-ui.html
- Keycloak Admin: http://localhost:8090/admin (admin/admin)

---

## Project Structure

```
nomcebo-bank-auth-service/
├── src/
│   ├── main/
│   │   ├── java/za/co/nomcebo/bank/auth/
│   │   │   ├── config/          # Configuration classes
│   │   │   ├── controller/      # REST controllers
│   │   │   ├── dto/             # Data Transfer Objects
│   │   │   ├── exception/       # Custom exceptions
│   │   │   ├── model/           # JPA entities
│   │   │   ├── repository/      # Data repositories
│   │   │   ├── security/        # Security components
│   │   │   ├── service/         # Business logic
│   │   │   └── util/            # Utility classes
│   │   └── resources/
│   │       └── application.properties
│   └── test/                    # Test files (to be added)
├── docs/                        # Documentation
├── docker-compose.yml          # Docker services
├── Dockerfile                  # Application container
├── pom.xml                     # Maven configuration
└── README.md
```

---

## Coding Standards

### Java Code Style

- **Formatting:** Follow Google Java Style Guide
- **Line Length:** Maximum 120 characters
- **Indentation:** 4 spaces (no tabs)
- **Naming Conventions:**
  - Classes: PascalCase (e.g., `AuthController`)
  - Methods: camelCase (e.g., `authenticateUser`)
  - Constants: UPPER_SNAKE_CASE (e.g., `MAX_LOGIN_ATTEMPTS`)
  - Variables: camelCase (e.g., `userName`)

### JavaDoc Requirements

All public classes and methods must have JavaDoc comments:

```java
/**
 * Authenticates a user with email/username and password.
 * <p>
 * Performs comprehensive security checks including account lockout
 * validation, rate limiting enforcement, and audit logging.
 * </p>
 *
 * @param loginRequest User login credentials
 * @param ipAddress Client IP address for security tracking
 * @return Authentication response with JWT tokens
 * @throws AuthenticationException if authentication fails
 */
public LoginResponseDTO authenticate(LoginRequestDTO loginRequest, String ipAddress) {
    // Implementation
}
```

### Lombok Usage

Use Lombok annotations to reduce boilerplate:

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private String username;
    private String email;
}
```

### Exception Handling

- Use custom exceptions for domain-specific errors
- Always log exceptions with appropriate log levels
- Never expose sensitive information in error messages

```java
try {
    // Business logic
} catch (Exception e) {
    log.error("Error processing request for user: {}", username, e);
    throw new AuthenticationException("Authentication failed");
}
```

---

## Testing Guidelines

### Unit Tests

- Write unit tests for all service methods
- Use JUnit 5 and Mockito
- Aim for 80%+ code coverage
- Test file naming: `ClassNameTest.java`

Example:
```java
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private AuthService authService;
    
    @Test
    @DisplayName("Should authenticate user with valid credentials")
    void testAuthenticateSuccess() {
        // Arrange
        LoginRequestDTO request = new LoginRequestDTO();
        request.setUsername("testuser");
        request.setPassword("password");
        
        // Act & Assert
        assertDoesNotThrow(() -> authService.authenticate(request, "127.0.0.1"));
    }
}
```

### Integration Tests

- Use `@SpringBootTest` for integration tests
- Use Testcontainers for database testing
- Test file naming: `ClassNameIntegrationTest.java`

### Running Tests

```bash
# Run all tests
mvn test

# Run with coverage
mvn clean verify

# View coverage report
open target/site/jacoco/index.html
```

---

## Commit Message Guidelines

Follow the Conventional Commits specification:

### Format

```
<type>(<scope>): <subject>

<body>

<footer>
```

### Types

- **feat:** New feature
- **fix:** Bug fix
- **docs:** Documentation changes
- **style:** Code style changes (formatting, no logic change)
- **refactor:** Code refactoring
- **test:** Adding or updating tests
- **chore:** Maintenance tasks

### Examples

```
feat(auth): add password reset functionality

Implement password reset flow with email verification.
Includes reset token generation and expiration handling.

Closes #123
```

```
fix(security): prevent token replay attacks

Add timestamp validation to JWT tokens to prevent
replay attacks within the token validity window.

Fixes #456
```

---

## Pull Request Process

### Before Submitting

1. **Update your fork:**
   ```bash
   git fetch upstream
   git rebase upstream/main
   ```

2. **Run tests:**
   ```bash
   mvn clean verify
   ```

3. **Check code style:**
   ```bash
   mvn checkstyle:check
   ```

4. **Update documentation** if needed

### Submitting a PR

1. **Create a feature branch:**
   ```bash
   git checkout -b feature/your-feature-name
   ```

2. **Make your changes** following coding standards

3. **Commit your changes** with descriptive messages

4. **Push to your fork:**
   ```bash
   git push origin feature/your-feature-name
   ```

5. **Create Pull Request** on GitHub

### PR Template

Your PR should include:

- **Description:** What does this PR do?
- **Motivation:** Why is this change needed?
- **Related Issues:** Links to related issues
- **Testing:** How was this tested?
- **Screenshots:** If applicable
- **Checklist:**
  - [ ] Code follows project style guidelines
  - [ ] Self-review completed
  - [ ] Comments added for complex code
  - [ ] Documentation updated
  - [ ] Tests added/updated
  - [ ] All tests passing
  - [ ] No new warnings

### Review Process

- PRs require at least one approval
- Address all review comments
- Keep PRs focused and reasonably sized
- Update PR based on feedback
- Squash commits before merging (if requested)

---

## Security Considerations

### Sensitive Data

- **Never commit secrets** (passwords, API keys, tokens)
- Use environment variables for configuration
- Add sensitive files to `.gitignore`
- Use Spring's `@Value` for external configuration

### Code Review Checklist

When reviewing security-sensitive code:

- [ ] No hardcoded credentials
- [ ] Input validation implemented
- [ ] SQL injection prevention (use parameterized queries)
- [ ] XSS prevention (escape output)
- [ ] CSRF protection enabled
- [ ] Authentication required for sensitive endpoints
- [ ] Authorization checks in place
- [ ] Audit logging for sensitive operations
- [ ] Secure password hashing (BCrypt)
- [ ] Rate limiting implemented

### Reporting Security Issues

If you discover a security vulnerability:

1. **DO NOT** open a public issue
2. Contact the maintainer directly via LinkedIn
3. Provide detailed description and reproduction steps
4. Allow time for fix before public disclosure

---

## Questions and Support

- **Issues:** Use GitHub Issues for bug reports and feature requests
- **Discussions:** Use GitHub Discussions for questions
- **Contact:** Reach out via LinkedIn for direct support

---

## License

By contributing, you agree that your contributions will be licensed under the same license as the project.

---

Thank you for contributing to Nomcebo Bank Auth Service! 🚀
