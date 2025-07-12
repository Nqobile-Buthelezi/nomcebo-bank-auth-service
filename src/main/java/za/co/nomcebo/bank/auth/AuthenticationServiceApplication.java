package za.co.nomcebo.bank.auth;


import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/**
 * Main application class for Nomcebo Bank Authentication Service
 * This service provides identity and access management capabilities
 * including user authentication, authorisation, and JWT token management
 * integrated with Keycloak.
 *
 * @author Nqobile Thabo Buthelezi
 * @version 1.0.0
 * @since 2025-07-12
 */
@SpringBootApplication
@EnableMethodSecurity( prePostEnabled = true )
@OpenAPIDefinition(
        info = @Info(
                title = "Nomcebo Bank Auth Service API",
                version = "1.0.0",
                description = "Identity and Access Management Service for Nomcebo Bank"
        ),
        security = {
                @SecurityRequirement( name = "bearerAuth")
        }
)
public class AuthenticationServiceApplication {

    /**
     * Main method to start the authentication service application
     *
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(AuthenticationServiceApplication.class, args);
    }

}