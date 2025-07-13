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
import java.util.List;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    private int failedLoginAttempts;
    private LocalDateTime lastLoginTime;
    private LocalDateTime lastFailedLoginTime;
    private LocalDateTime lockedUntil;
    private String lastLoginIp;
    private String email;
    private String username;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String saIdNumber;
    private LocalDateTime dateOfBirth;
    private String gender;
    private String address;
    private String city;
    private String province;
    private String postalCode;
    private String passwordHash;
    private String passwordResetToken;
    private LocalDateTime passwordResetExpiry;
    private boolean isActive;
    private boolean isEmailVerified;
    private LocalDateTime createdAt;
    private String registrationIp;
    private List<String> roles;
}
