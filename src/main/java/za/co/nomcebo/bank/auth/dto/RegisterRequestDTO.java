package za.co.nomcebo.bank.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequestDTO {

    @NotBlank(message = "Username is required.")
    @Size(max = 35, message = "Username cannot exceed 35 characters.")
    private String username;

    @NotBlank(message = "Email is required.")
    @Email(message = "Email should be valid.")
    private String email;

    @NotBlank(message = "Password is required.")
    @Size(max = 50, message = "Password cannot exceed 50 characters.")
    private String password;

    @NotBlank(message = "South African ID number is required.")
    private String SouthAfricanIdNumber;

    @NotBlank(message = "First name is required.")
    private String firstName;

    @NotBlank(message = "Last name is required.")
    private String lastName;

    @NotBlank(message = "Phone number is required.")
    private String phoneNumber;

    @NotBlank(message = "Address is required.")
    private String address;

    @NotBlank(message = "City is required.")
    private String city;

    @NotBlank(message = "Province is required.")
    private String province;

    @NotBlank(message = "Postal code is required.")
    private String postalCode;

}
