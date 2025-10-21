# API Documentation

## Overview

The Nomcebo Bank Auth Service provides RESTful APIs for authentication, authorization, and user management. All endpoints return JSON responses and use JWT tokens for authentication.

## Base URL

```
http://localhost:8080/api
```

## Authentication

Most endpoints require authentication using JWT tokens. Include the token in the Authorization header:

```
Authorization: Bearer <your-jwt-token>
```

---

## Authentication Endpoints

### 1. Register New User

Creates a new user account with KYC validation and South African ID verification.

**Endpoint:** `POST /auth/register`

**Headers:**
```
Content-Type: application/json
```

**Request Body:**
```json
{
  "username": "john.doe",
  "email": "john.doe@example.com",
  "password": "SecurePass123!",
  "firstName": "John",
  "lastName": "Doe",
  "phoneNumber": "+27821234567",
  "southAfricanIdNumber": "9001015009087",
  "address": "123 Main Street",
  "city": "Johannesburg",
  "province": "Gauteng",
  "postalCode": "2000"
}
```

**Response (201 Created):**
```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "email": "john.doe@example.com",
  "message": "Registration successful. Please check your email to verify your account.",
  "emailVerificationRequired": true
}
```

**Error Response (409 Conflict):**
```json
{
  "timestamp": "2025-10-21T07:45:30",
  "status": 409,
  "error": "Conflict",
  "message": "User with this email already exists",
  "path": "/api/auth/register"
}
```

**Validation Rules:**
- Username: 3-50 characters, alphanumeric and underscores only
- Email: Valid email format
- Password: Minimum 8 characters, must contain uppercase, lowercase, number, and special character
- SA ID Number: Valid 13-digit South African ID format with checksum validation
- Phone Number: Valid South African phone number format

---

### 2. User Login

Authenticates a user and returns JWT access and refresh tokens.

**Endpoint:** `POST /auth/login`

**Headers:**
```
Content-Type: application/json
```

**Request Body:**
```json
{
  "username": "john.doe",
  "password": "SecurePass123!"
}
```

**Response (200 OK):**
```json
{
  "accessToken": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJqb2huLmRvZSIsInJvbGVzIjpbIlJPTEVfVVNFUiJdLCJ0eXBlIjoiYWNjZXNzIiwiaWF0IjoxNzI5NDk3OTMwLCJleHAiOjE3Mjk0OTg4MzB9.xxx",
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJqb2huLmRvZSIsInJvbGVzIjpbIlJPTEVfVVNFUiJdLCJ0eXBlIjoicmVmcmVzaCIsImlhdCI6MTcyOTQ5NzkzMCwiZXhwIjoxNzMwMTAyNzMwfQ.xxx",
  "tokenType": "Bearer",
  "expiresIn": 900,
  "user": {
    "username": "john.doe",
    "email": "john.doe@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "phoneNumber": "+27821234567",
    "southAfricanIdNumber": "9001015009087",
    "dateOfBirth": "1990-01-01T00:00:00"
  }
}
```

**Error Response (401 Unauthorized):**
```json
{
  "timestamp": "2025-10-21T07:45:30",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid username or password",
  "path": "/api/auth/login"
}
```

**Error Response (423 Locked):**
```json
{
  "timestamp": "2025-10-21T07:45:30",
  "status": 423,
  "error": "Locked",
  "message": "Account is temporarily locked. Please try again later.",
  "path": "/api/auth/login"
}
```

**Security Features:**
- Rate limiting: Maximum 5 failed attempts
- Account lockout: 30 minutes after 5 failed attempts
- IP tracking and audit logging
- Secure password hashing with BCrypt

---

### 3. Refresh Token

Generates a new access token using a valid refresh token.

**Endpoint:** `POST /auth/refresh`

**Headers:**
```
Content-Type: application/json
```

**Request Body:**
```json
{
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJqb2huLmRvZSIsInJvbGVzIjpbIlJPTEVfVVNFUiJdLCJ0eXBlIjoicmVmcmVzaCIsImlhdCI6MTcyOTQ5NzkzMCwiZXhwIjoxNzMwMTAyNzMwfQ.xxx"
}
```

**Response (200 OK):**
```json
{
  "accessToken": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJqb2huLmRvZSIsInJvbGVzIjpbIlJPTEVfVVNFUiJdLCJ0eXBlIjoiYWNjZXNzIiwiaWF0IjoxNzI5NDk4NTAwLCJleHAiOjE3Mjk0OTk0MDB9.xxx",
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJqb2huLmRvZSIsInJvbGVzIjpbIlJPTEVfVVNFUiJdLCJ0eXBlIjoicmVmcmVzaCIsImlhdCI6MTcyOTQ5ODUwMCwiZXhwIjoxNzMwMTAzMzAwfQ.xxx",
  "tokenType": "Bearer",
  "expiresIn": 900
}
```

**Error Response (401 Unauthorized):**
```json
{
  "timestamp": "2025-10-21T07:45:30",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid or expired refresh token",
  "path": "/api/auth/refresh"
}
```

---

### 4. Validate Token

Validates a JWT token and returns user information.

**Endpoint:** `GET /auth/validate`

**Headers:**
```
Authorization: Bearer <access-token>
```

**Response (200 OK):**
```json
{
  "valid": true,
  "username": "john.doe",
  "authorities": ["ROLE_USER"],
  "expiresAt": "2025-10-21T08:00:30"
}
```

**Error Response (401 Unauthorized):**
```json
{
  "timestamp": "2025-10-21T07:45:30",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid or expired token",
  "path": "/api/auth/validate"
}
```

---

### 5. User Logout

Logs out a user and invalidates all tokens.

**Endpoint:** `POST /auth/logout`

**Headers:**
```
Authorization: Bearer <access-token>
Content-Type: application/json
```

**Request Body:**
```json
{
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJqb2huLmRvZSIsInJvbGVzIjpbIlJPTEVfVVNFUiJdLCJ0eXBlIjoicmVmcmVzaCIsImlhdCI6MTcyOTQ5NzkzMCwiZXhwIjoxNzMwMTAyNzMwfQ.xxx"
}
```

**Response (200 OK):**
```json
{
  "message": "Logout successful",
  "timestamp": "2025-10-21T07:45:30"
}
```

---

### 6. Password Reset

Initiates the password reset process by sending a reset link to the user's email.

**Endpoint:** `POST /auth/reset-password`

**Headers:**
```
Content-Type: application/json
```

**Request Body:**
```json
{
  "email": "john.doe@example.com"
}
```

**Response (200 OK):**
```json
{
  "message": "If an account with this email exists, you will receive a password reset link."
}
```

**Note:** For security reasons, this endpoint always returns success even if the email doesn't exist.

---

## Error Codes

| Status Code | Description |
|-------------|-------------|
| 200 | Success |
| 201 | Created |
| 400 | Bad Request - Invalid input data |
| 401 | Unauthorized - Invalid or missing credentials |
| 403 | Forbidden - Insufficient permissions |
| 404 | Not Found - Resource not found |
| 409 | Conflict - Resource already exists |
| 423 | Locked - Account temporarily locked |
| 429 | Too Many Requests - Rate limit exceeded |
| 500 | Internal Server Error |

---

## Rate Limiting

The API implements rate limiting to prevent abuse:

- **Login:** Maximum 5 failed attempts within 30 minutes
- **Registration:** Maximum 3 registrations per IP per hour
- **Password Reset:** Maximum 3 requests per email per hour

Exceeding these limits results in a 429 status code.

---

## CORS Configuration

By default, CORS is configured to allow requests from:
- `http://localhost:3000` (Development frontend)

For production, configure the `nomcebo.cors.allowed-origins` property.

---

## Token Expiration

- **Access Token:** 15 minutes
- **Refresh Token:** 7 days
- **Password Reset Token:** 1 hour

---

## Example cURL Commands

### Register
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john.doe",
    "email": "john.doe@example.com",
    "password": "SecurePass123!",
    "firstName": "John",
    "lastName": "Doe",
    "phoneNumber": "+27821234567",
    "southAfricanIdNumber": "9001015009087",
    "address": "123 Main Street",
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
    "username": "john.doe",
    "password": "SecurePass123!"
  }'
```

### Validate Token
```bash
curl -X GET http://localhost:8080/api/auth/validate \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

### Logout
```bash
curl -X POST http://localhost:8080/api/auth/logout \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "YOUR_REFRESH_TOKEN"
  }'
```

---

## Swagger UI

Interactive API documentation is available at:
```
http://localhost:8080/swagger-ui.html
```

This provides a web interface to test all endpoints directly from your browser.
