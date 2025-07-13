package za.co.nomcebo.bank.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterResponseDTO {
    private String userId;
    private String email;
    private String message;
    private boolean emailVerificationRequired;
}
