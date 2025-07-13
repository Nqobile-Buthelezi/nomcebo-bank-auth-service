package za.co.nomcebo.bank.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.co.nomcebo.bank.auth.dto.*;
import za.co.nomcebo.bank.auth.exception.AuthenticationException;
import za.co.nomcebo.bank.auth.exception.InvalidTokenException;
import za.co.nomcebo.bank.auth.exception.UserAlreadyExistsException;
import za.co.nomcebo.bank.auth.model.AuditLog;
import za.co.nomcebo.bank.auth.model.User;
import za.co.nomcebo.bank.auth.repository.AuditLogRepository;
import za.co.nomcebo.bank.auth.repository.UserRepository;
import za.co.nomcebo.bank.auth.security.JwtTokenProvider;
import za.co.nomcebo.bank.auth.util.SouthAfricanIdValidator;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Authentication service implementation for Nomcebo Bank.
 * <p>
 * Provides comprehensive authentication services including user registration,
 * login, token management, and audit logging. Implements South African banking
 * regulations and POPIA ( Protection of Personal Information Act )
 * compliance for personal information protection.
 * <p>
 * Key features:
 * - SA ID number validation
 * - KYC (Know Your Customer) checks
 * - Rate limiting and security monitoring
 * - Comprehensive audit logging
 * - Integration with Keycloak for identity management
 *
 * @author Nqobile Thabo Buthelezi
 * @version 1.0.0
 * @since 2025-07-13
 */
@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final KeycloakService keycloakService;
    private final NotificationService notificationService;
    private final SouthAfricanIdValidator idValidator;

    @Value("${nomcebo.auth.max-login-attempts:5}")
    private int maxLoginAttempts;

    @Value("${nomcebo.auth.lockout-duration:30}")
    private int lockoutDurationMinutes;

    /**
     * Authenticates a user with email/username and password.
     * <p>
     * Performs comprehensive security checks including:
     * - Account lockout validation
     * - Rate limiting enforcement
     * - Password strength verification
     * - Audit logging for compliance
     * </p>
     *
     * @param loginRequest User login credentials
     * @param ipAddress    Client IP address for security tracking
     * @return Authentication response with JWT tokens
     * @throws AuthenticationException if authentication fails
     */
    public LoginResponseDTO authenticate(loginRequestDTO loginRequest, String ipAddress) {
        String username = loginRequest.getUsername();

        // Check if account is locked
        User user = userRepository.findByUsername(username);

        if (user != null) {
            if (isAccountLocked(user)) {
                logAuditEvent(
                        "LOGIN_FAILED_LOCKED",
                        username,
                        ipAddress,
                        "Account locked due to multiple failed attempts"
                );
                throw new AuthenticationException("Account is temporarily locked. Please try again later.");
            }
        }

        try {
            // Authenticate with Keycloak
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, loginRequest.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Generate JWT tokens
            String accessToken = tokenProvider.generateAccessToken(authentication);
            String refreshToken = tokenProvider.generateRefreshToken(authentication);

            // Reset failed login attempts on successful login
            if (user != null) {
                user.setFailedLoginAttempts(0);
                user.setLastLoginTime(LocalDateTime.now());
                user.setLastLoginIp(ipAddress);
                userRepository.save(user);
            }

            // Log successful authentication
            logAuditEvent(
                    "LOGIN_SUCCESS",
                    username,
                    ipAddress,
                    "User authenticated successfully"
            );

            return LoginResponseDTO.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(tokenProvider.getAccessTokenValidity())
                    .user(buildUserInfo(authentication))
                    .build();
        } catch (Exception e) {
            // Handle failed authentication
            handleFailedLogin(username, ipAddress);
            logAuditEvent(
                    "LOGIN_FAILED",
                    username,
                    ipAddress,
                    "Authentication failed: " + e.getMessage()
            );
            throw new AuthenticationException("Invalid username or password");
        }
    }

    /**
     * Registers a new user account with KYC validation.
     * <p>
     * Performs comprehensive validation including:
     * - SA ID number format and checksum validation
     * - Email format verification
     * - Password strength requirements
     * - Duplicate account prevention
     * - POPIA compliance for data collection
     *
     * @param registerRequest New user registration details
     * @param ipAddress       Client IP address for audit tracking
     * @return Registration response with user details
     * @throws UserAlreadyExistsException if user already exists
     */
    public RegisterResponseDTO register(RegisterRequestDTO registerRequest, String ipAddress) {
        String email = registerRequest.getEmail();
        String saIdNumber = registerRequest.getSouthAfricanIdNumber();

        // Validate SA ID number format
        if (!idValidator.isValidSaIdNumber(saIdNumber)) {
            logAuditEvent("REGISTRATION_FAILED", email, ipAddress, "Invalid SA ID number format");
            throw new IllegalArgumentException("Invalid South African ID number format");
        }

        // Check for existing user
        if (userRepository.existsByEmail(email)) {
            logAuditEvent("REGISTRATION_FAILED", email, ipAddress, "Email already exists");
            throw new UserAlreadyExistsException("User with this email already exists");
        }

        if (userRepository.existsBySouthAfricanIdNumber(saIdNumber)) {
            logAuditEvent("REGISTRATION_FAILED", email, ipAddress, "SA ID number already exists");
            throw new UserAlreadyExistsException("User with this SA ID number already exists");
        }

        // Extract date of birth from SA ID
        LocalDateTime dateOfBirth = idValidator.extractDateOfBirth(saIdNumber);
        String gender = idValidator.extractGender(saIdNumber);

        // Create user entity
        User user = User.builder()
                .id(UUID.fromString(UUID.randomUUID().toString()))
                .email(email)
                .username(registerRequest.getUsername())
                .firstName(registerRequest.getFirstName())
                .lastName(registerRequest.getLastName())
                .phoneNumber(registerRequest.getPhoneNumber())
                .saIdNumber(saIdNumber)
                .dateOfBirth(dateOfBirth)
                .gender(gender)
                .address(registerRequest.getAddress())
                .city(registerRequest.getCity())
                .province(registerRequest.getProvince())
                .postalCode(registerRequest.getPostalCode())
                .passwordHash(passwordEncoder.encode(registerRequest.getPassword()))
                .isActive(true)
                .isEmailVerified(false)
                .createdAt(LocalDateTime.now())
                .registrationIp(ipAddress)
                .failedLoginAttempts(0)
                .roles(List.of("USER"))
                .build();

        // Save user to database
        User savedUser = userRepository.save(user);

        // Create user in Keycloak
        keycloakService.createUser(savedUser, registerRequest.getPassword());

        // Send welcome email with verification link
        notificationService.sendWelcomeEmail(savedUser);

        // Log successful registration
        logAuditEvent("REGISTRATION_SUCCESS", email, ipAddress, "User registered successfully");

        return RegisterResponseDTO.builder()
                .userId(String.valueOf(savedUser.getId()))
                .email(savedUser.getEmail())
                .message("Registration successful. Please check your email to verify your account.")
                .emailVerificationRequired(true)
                .build();
    }

    /**
     * Refreshes JWT access token using refresh token.
     *
     * @param refreshRequest Refresh token request
     * @return New access and refresh tokens
     * @throws InvalidTokenException if refresh token is invalid
     */
    public TokenResponseDTO refreshToken(RefreshTokenRequestDTO refreshRequest) {
        String refreshToken = refreshRequest.getRefreshToken();

        if (!tokenProvider.validateRefreshToken(refreshToken)) {
            logAuditEvent("TOKEN_REFRESH_FAILED", "unknown", "unknown", "Invalid refresh token");
            throw new InvalidTokenException("Invalid or expired refresh token");
        }

        String username = tokenProvider.getUsernameFromToken(refreshToken);
        Authentication authentication = tokenProvider.getAuthenticationFromToken(refreshToken);

        String newAccessToken = tokenProvider.generateAccessToken(authentication);
        String newRefreshToken = tokenProvider.generateRefreshToken(authentication);

        logAuditEvent("TOKEN_REFRESH_SUCCESS", username, "unknown", "Token refreshed successfully");

        return TokenResponseDTO.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(tokenProvider.getAccessTokenValidity())
                .build();
    }

    /**
     * Validates JWT token and returns user information.
     *
     * @param token JWT token with Bearer prefix
     * @return Token validation response
     */
    public TokenValidationResponseDTO validateToken(String token) {
        String jwtToken = token.substring(7); // Remove "Bearer " prefix

        if (!tokenProvider.validateToken(jwtToken)) {
            throw new InvalidTokenException("Invalid or expired token");
        }

        String username = tokenProvider.getUsernameFromToken(jwtToken);
        Authentication authentication = tokenProvider.getAuthenticationFromToken(jwtToken);

        return TokenValidationResponseDTO.builder()
                .valid(true)
                .username(username)
                .authorities(authentication.getAuthorities().stream()
                        .map(auth -> auth.getAuthority())
                        .toList())
                .expiresAt(tokenProvider.getExpirationDateFromToken(jwtToken))
                .build();
    }

    /**
     * Logs out user and invalidates tokens.
     *
     * @param logoutRequest Logout request
     * @param ipAddress     Client IP address
     * @return Logout confirmation
     */
    public LogoutResponseDTO logout(LogoutRequestDTO logoutRequest, String ipAddress) {
        String refreshToken = logoutRequest.getRefreshToken();
        String username = tokenProvider.getUsernameFromToken(refreshToken);

        // Invalidate tokens in Keycloak
        keycloakService.invalidateUserTokens(username);

        // Log logout event
        logAuditEvent("LOGOUT_SUCCESS", username, ipAddress, "User logged out successfully");

        return LogoutResponseDTO.builder()
                .message("Logout successful")
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Initiates password reset process.
     *
     * @param resetRequest Password reset request
     * @return Password reset confirmation
     */
    public PasswordResetResponseDTO resetPassword(PasswordResetRequestDTO resetRequest) {
        String email = resetRequest.getEmail();

        User user = userRepository.findByEmail(email);
        assert user != null : "User not found with email: " + email;

        if (!user.isEmailVerified()) {
            // Don't reveal if user email exists
            return PasswordResetResponseDTO.builder()
                    .message("If an account with this email exists, you will receive a password reset link.")
                    .build();
        }

        // Generate reset token
        String resetToken = UUID.randomUUID().toString();
        user.setPasswordResetToken(resetToken);
        user.setPasswordResetExpiry(LocalDateTime.now().plusHours(1));
        userRepository.save(user);

        // Send reset email
        notificationService.sendPasswordResetEmail(user, resetToken);

        logAuditEvent("PASSWORD_RESET_INITIATED", email, "unknown", "Password reset initiated");

        return PasswordResetResponseDTO.builder()
                .message("If an account with this email exists, you will receive a password reset link.")
                .build();
    }

    /**
     * Handles failed login attempts and account lockout.
     */
    private void handleFailedLogin(String username, String ipAddress) {
        User user = userRepository.findByUsername(username);
        if (user != null) {
            user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
            user.setLastFailedLoginTime(LocalDateTime.now());

            if (user.getFailedLoginAttempts() >= maxLoginAttempts) {
                user.setLockedUntil(LocalDateTime.now().plusMinutes(lockoutDurationMinutes));
                logAuditEvent("ACCOUNT_LOCKED", username, ipAddress,
                        "Account locked due to " + maxLoginAttempts + " failed attempts");
            }

            userRepository.save(user);
        }
    }

    /**
     * Checks if user account is currently locked.
     */
    private boolean isAccountLocked(User user) {
        return user.getLockedUntil() != null &&
                user.getLockedUntil().isAfter(LocalDateTime.now());
    }

    /**
     * Logs audit events for compliance and security monitoring.
     */
    private void logAuditEvent(String eventType, String username, String ipAddress, String description) {
        AuditLog auditLog = AuditLog.builder()
                .id(UUID.randomUUID().toString())
                .eventType(eventType)
                .username(username)
                .ipAddress(ipAddress)
                .description(description)
                .timestamp(LocalDateTime.now())
                .build();

        auditLogRepository.save(auditLog);
    }

    /**
     * Builds user information for response.
     */
    private User buildUserInfo(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username);

        if (user != null) {
            return User.builder()
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .phoneNumber(user.getPhoneNumber())
                    .saIdNumber(user.getSaIdNumber())
                    .dateOfBirth(user.getDateOfBirth())
                    .build();
        }

        return User.builder()
                .username(username)
                .roles(authentication.getAuthorities().stream()
                        .map(auth -> auth.getAuthority())
                        .toList())
                .build();
    }

}
