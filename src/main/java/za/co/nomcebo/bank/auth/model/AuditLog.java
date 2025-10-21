package za.co.nomcebo.bank.auth.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Audit Log entity for tracking security and compliance events.
 * <p>
 * Records all significant authentication and authorization events
 * for security monitoring, compliance reporting, and forensic analysis.
 * Supports POPIA compliance and banking regulatory requirements.
 * </p>
 * <p>
 * Event Types Include:
 * - LOGIN_SUCCESS - Successful user authentication
 * - LOGIN_FAILED - Failed login attempt
 * - LOGIN_FAILED_LOCKED - Login attempt on locked account
 * - REGISTRATION_SUCCESS - New user registered
 * - REGISTRATION_FAILED - Failed registration attempt
 * - TOKEN_REFRESH_SUCCESS - Token successfully refreshed
 * - TOKEN_REFRESH_FAILED - Failed token refresh
 * - LOGOUT_SUCCESS - User logged out
 * - PASSWORD_RESET_INITIATED - Password reset requested
 * - ACCOUNT_LOCKED - Account locked due to failed attempts
 * </p>
 *
 * @author Nqobile Thabo Buthelezi
 * @version 1.0.0
 * @since 2025-07-12
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {
    
    /** Unique identifier for the audit log entry */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    
    /** Type of event (e.g., LOGIN_SUCCESS, REGISTRATION_FAILED) */
    private String eventType;
    
    /** Username associated with the event */
    private String username;
    
    /** IP address from which the event originated */
    private String ipAddress;
    
    /** Detailed description of the event */
    private String description;
    
    /** Timestamp when the event occurred */
    private LocalDateTime timestamp;
}
