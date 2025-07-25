package za.co.nomcebo.bank.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogoutRequestDTO {
    @NotBlank(message = "Refresh token is required.")
    private String refreshToken;
}
