package za.co.nomcebo.bank.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDTO {
    @NotBlank(message = "Username is required.")
    @Size(max = 35, message = "Username cannot exceed 35 characters.")
    private String username;

    @NotBlank(message = "Password is required.")
    @Size(max = 50, message = "Password cannot exceed 50 characters.")
    private String password;
}
