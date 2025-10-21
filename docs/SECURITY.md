# Security Best Practices

## Overview

The Nomcebo Bank Auth Service implements comprehensive security measures to protect user data and prevent unauthorized access. This document outlines the security features and best practices implemented in the application.

---

## Authentication & Authorization

### JWT Token Security

**Access Tokens:**
- Validity: 15 minutes
- Algorithm: HS512 (HMAC with SHA-512)
- Contains: username, roles, expiration
- Use: API authentication

**Refresh Tokens:**
- Validity: 7 days
- Algorithm: HS512
- Contains: username, roles, expiration
- Use: Obtaining new access tokens

**Best Practices:**
```java
// Always validate token signature and expiration
if (!tokenProvider.validateToken(token)) {
    throw new InvalidTokenException("Invalid or expired token");
}

// Extract user information securely
Authentication auth = tokenProvider.getAuthenticationFromToken(token);
```

### Password Security

**Requirements:**
- Minimum 8 characters
- At least one uppercase letter
- At least one lowercase letter
- At least one number
- At least one special character

**Storage:**
- Passwords hashed using BCrypt (cost factor: 10)
- Never store plain-text passwords
- Password reset tokens expire after 1 hour

```java
// Password encoding
String hashedPassword = passwordEncoder.encode(plainPassword);

// Password verification
boolean matches = passwordEncoder.matches(plainPassword, hashedPassword);
```

---

## Account Protection

### Rate Limiting

**Login Attempts:**
- Maximum 5 failed attempts per account
- Automatic lockout for 30 minutes after threshold
- IP address tracking for security monitoring

```java
@Value("${nomcebo.auth.max-login-attempts:5}")
private int maxLoginAttempts;

@Value("${nomcebo.auth.lockout-duration:30}")
private int lockoutDurationMinutes;
```

**Implementation:**
```java
private void handleFailedLogin(String username, String ipAddress) {
    User user = userRepository.findByUsername(username);
    if (user != null) {
        user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
        
        if (user.getFailedLoginAttempts() >= maxLoginAttempts) {
            user.setLockedUntil(LocalDateTime.now().plusMinutes(lockoutDurationMinutes));
        }
        
        userRepository.save(user);
    }
}
```

### Session Management

**Keycloak Integration:**
- Centralized session management
- Single logout across all sessions
- Session timeout configuration
- Token revocation on logout

```java
public void invalidateUserTokens(String username) {
    // Logout user from all Keycloak sessions
    UserResource userResource = usersResource.get(userId);
    userResource.logout();
}
```

---

## Data Protection

### POPIA Compliance

The Protection of Personal Information Act (POPIA) compliance features:

**Data Minimization:**
- Collect only necessary user information
- Clear purpose for each data field
- User consent for data collection

**Data Security:**
- Encryption in transit (HTTPS/TLS)
- Encrypted password storage (BCrypt)
- Secure token generation
- Database encryption at rest

**User Rights:**
- Right to access personal data
- Right to correction
- Right to deletion
- Data portability

### South African ID Validation

**Security Features:**
- Format validation (13 digits)
- Checksum verification (Luhn algorithm)
- Date of birth extraction
- Gender identification
- Duplicate prevention

```java
/**
 * Validates South African ID number format and checksum
 */
public boolean isValidSaIdNumber(String idNumber) {
    if (!ID_NUMBER_PATTERN.matcher(idNumber).matches()) {
        return false;
    }
    return validateChecksum(idNumber);
}
```

---

## Audit Logging

### What We Log

**Successful Events:**
- User registration
- Successful login
- Token refresh
- Password reset requests
- Logout

**Security Events:**
- Failed login attempts
- Account lockouts
- Invalid token usage
- Suspicious activities

**Audit Log Structure:**
```java
@Entity
public class AuditLog {
    private UUID id;
    private String eventType;      // LOGIN_SUCCESS, REGISTRATION_FAILED, etc.
    private String username;       // User involved
    private String ipAddress;      // Client IP
    private String description;    // Event details
    private LocalDateTime timestamp; // When it occurred
}
```

### Implementation

```java
private void logAuditEvent(String eventType, String username, 
                          String ipAddress, String description) {
    AuditLog auditLog = AuditLog.builder()
            .id(UUID.randomUUID())
            .eventType(eventType)
            .username(username)
            .ipAddress(ipAddress)
            .description(description)
            .timestamp(LocalDateTime.now())
            .build();
    
    auditLogRepository.save(auditLog);
}
```

---

## Input Validation

### Request Validation

**Using Jakarta Validation:**
```java
public class RegisterRequestDTO {
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
    
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;
}
```

### SQL Injection Prevention

**Use JPA/Hibernate:**
```java
// Safe - Uses parameterized queries
@Query("SELECT u FROM User u WHERE u.email = :email")
User findByEmail(@Param("email") String email);

// NEVER use string concatenation
// UNSAFE: query = "SELECT * FROM users WHERE email = '" + email + "'";
```

### XSS Prevention

**Output Escaping:**
- All user-generated content is escaped
- JSON responses automatically escaped by Jackson
- HTML content sanitized if rendered

---

## CORS Configuration

### Development Configuration

```properties
nomcebo.cors.allowed-origins=http://localhost:3000
```

### Production Configuration

**Recommended Settings:**
```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(Arrays.asList("https://yourdomain.com"));
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
    configuration.setAllowedHeaders(Arrays.asList("*"));
    configuration.setAllowCredentials(true);
    configuration.setMaxAge(3600L);
    
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/api/**", configuration);
    return source;
}
```

---

## Environment Security

### Configuration Management

**DO:**
- Use environment variables for secrets
- Keep different configs for dev/prod
- Rotate secrets regularly
- Use secret management tools (Vault, AWS Secrets Manager)

**DON'T:**
- Commit secrets to version control
- Use default passwords in production
- Share credentials in plain text
- Reuse passwords across environments

### Example Configuration

```properties
# Development (application-dev.properties)
jwt.secret=${JWT_SECRET:dev-secret-key-change-in-production}
keycloak.admin.password=${KEYCLOAK_PASSWORD:admin}

# Production (application-prod.properties)
jwt.secret=${JWT_SECRET}
keycloak.admin.password=${KEYCLOAK_PASSWORD}
```

---

## API Security

### Endpoint Protection

**Public Endpoints:**
- POST /api/auth/register
- POST /api/auth/login
- POST /api/auth/refresh
- POST /api/auth/reset-password

**Protected Endpoints (Require Authentication):**
- GET /api/auth/validate
- POST /api/auth/logout
- GET /api/users/profile
- PUT /api/users/profile

**Implementation:**
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http
            .csrf().disable()
            .authorizeHttpRequests()
                .requestMatchers("/api/auth/register", "/api/auth/login").permitAll()
                .anyRequest().authenticated()
            .and()
            .addFilterBefore(jwtAuthenticationFilter, 
                           UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}
```

### HTTPS/TLS

**Production Requirements:**
- Force HTTPS for all endpoints
- Use TLS 1.2 or higher
- Valid SSL certificate
- HSTS headers enabled

```properties
# Force HTTPS
server.ssl.enabled=true
server.ssl.key-store=classpath:keystore.p12
server.ssl.key-store-password=${KEYSTORE_PASSWORD}
server.ssl.key-store-type=PKCS12
```

---

## Security Headers

### Recommended Headers

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) {
    http.headers()
        .contentSecurityPolicy("default-src 'self'")
        .and()
        .xssProtection()
        .and()
        .frameOptions().deny()
        .and()
        .httpStrictTransportSecurity()
            .maxAgeInSeconds(31536000)
            .includeSubDomains(true);
    
    return http.build();
}
```

**Headers Added:**
- Content-Security-Policy
- X-XSS-Protection
- X-Frame-Options
- Strict-Transport-Security
- X-Content-Type-Options

---

## Monitoring and Alerts

### Security Monitoring

**Metrics to Track:**
- Failed login attempts per IP
- Account lockouts
- Token validation failures
- Unusual access patterns
- API rate limit hits

**Alerting Rules:**
- More than 10 failed logins from same IP
- Multiple account lockouts in short time
- Unusual geographic access patterns
- High volume of password reset requests

### Actuator Endpoints

```properties
# Enable only specific actuator endpoints
management.endpoints.web.exposure.include=health,metrics
management.endpoint.health.show-details=when-authorized
```

---

## Incident Response

### Security Incident Checklist

1. **Identify the Issue**
   - Review audit logs
   - Check for unusual patterns
   - Identify affected users

2. **Contain the Threat**
   - Revoke compromised tokens
   - Lock affected accounts
   - Block malicious IPs

3. **Investigate**
   - Analyze audit trail
   - Identify root cause
   - Assess data exposure

4. **Remediate**
   - Apply security patches
   - Update configurations
   - Rotate compromised credentials

5. **Communicate**
   - Notify affected users
   - Document incident
   - Update security procedures

---

## Security Checklist

### Before Deployment

- [ ] All secrets in environment variables
- [ ] HTTPS/TLS configured
- [ ] CORS properly configured
- [ ] Rate limiting enabled
- [ ] Audit logging active
- [ ] Input validation on all endpoints
- [ ] SQL injection prevention verified
- [ ] XSS prevention implemented
- [ ] Security headers configured
- [ ] Password requirements enforced
- [ ] Token expiration set appropriately
- [ ] Error messages don't expose sensitive data
- [ ] Database backups configured
- [ ] Monitoring and alerting set up

---

## Resources

- [OWASP Top 10](https://owasp.org/www-project-top-ten/)
- [POPIA Guidelines](https://popia.co.za/)
- [Spring Security Documentation](https://docs.spring.io/spring-security/reference/index.html)
- [JWT Best Practices](https://tools.ietf.org/html/rfc8725)

---

## Questions or Concerns?

If you discover a security vulnerability, please report it responsibly. Contact the maintainer directly via LinkedIn rather than opening a public issue.
