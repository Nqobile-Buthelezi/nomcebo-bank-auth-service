package za.co.nomcebo.bank.auth.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * User entity representing a Nomcebo Bank customer account.
 * <p>
 * This entity stores comprehensive user information including:
 * - Personal details (name, contact info, SA ID)
 * - Authentication data (password, roles)
 * - Security tracking (login attempts, lockout status)
 * - Audit information (registration IP, timestamps)
 * - POPIA-compliant data storage
 * </p>
 * <p>
 * Security Features:
 * - Passwords stored as BCrypt hash
 * - Failed login attempt tracking
 * - Account lockout mechanism
 * - IP address logging for audit
 * - Email verification status
 * </p>
 *
 * @author Nqobile Thabo Buthelezi
 * @version 1.0.0
 * @since 2025-07-12
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    
    /** Unique identifier for the user (UUID v4) */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    
    /** Count of consecutive failed login attempts */
    
    /** Count of consecutive failed login attempts */
    private int failedLoginAttempts;
    
    /** Timestamp of last successful login */
    private LocalDateTime lastLoginTime;
    
    /** Timestamp of last failed login attempt */
    private LocalDateTime lastFailedLoginTime;
    
    /** Account locked until this timestamp (null if not locked) */
    private LocalDateTime lockedUntil;
    
    /** IP address of last successful login */
    private String lastLoginIp;
    
    /** User's email address (unique, used for login and communication) */
    private String email;
    
    /** Username (unique, used for login) */
    private String username;
    
    /** User's first name */
    private String firstName;
    
    /** User's last name */
    private String lastName;
    
    /** User's phone number (South African format) */
    private String phoneNumber;
    
    /** South African ID number (13 digits, unique, validated) */
    private String southAfricanIdNumber;
    
    /** Date of birth (extracted from SA ID number) */
    private LocalDateTime dateOfBirth;
    
    /** Gender (M/F, extracted from SA ID number) */
    private String gender;
    
    /** Physical address */
    private String address;
    
    /** City of residence */
    private String city;
    
    /** Province of residence */
    private String province;
    
    /** Postal code */
    private String postalCode;
    
    /** BCrypt hashed password (never store plain-text) */
    private String passwordHash;
    
    /** One-time token for password reset (expires after 1 hour) */
    private String passwordResetToken;
    
    /** Expiration timestamp for password reset token */
    private LocalDateTime passwordResetExpiry;
    
    /** Whether the account is active (can login) */
    private boolean isActive;
    
    /** Whether the user's email has been verified */
    private boolean isEmailVerified;
    
    /** Timestamp when account was created */
    private LocalDateTime createdAt;
    
    /** IP address from which account was registered */
    private String registrationIp;
    
    /** User's roles for authorization (e.g., USER, ADMIN) */
    private List<String> roles;
}
