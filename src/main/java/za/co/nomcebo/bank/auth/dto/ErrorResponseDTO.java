package za.co.nomcebo.bank.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponseDTO {

    @NotBlank( message = "Message cannot be blank" )
    private String message;

    @NotBlank( message = "Error code cannot be blank" )
    private String errorCode;

}
