package za.co.nomcebo.bank.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenValidationResponseDTO {
    private boolean valid;
    private String userId;
    private String username;
    private String email;
    private String[] roles;
    private List<String> authorities;
    private LocalDateTime expiresAt;
}
