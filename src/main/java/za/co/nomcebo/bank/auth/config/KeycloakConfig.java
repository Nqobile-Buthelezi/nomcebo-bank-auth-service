package za.co.nomcebo.bank.auth.config;

import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KeycloakConfig {

    @Value("${keycloak.auth-server-url}")
    private String serverUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.resource}")
    private String clientId;

    @Value("${keycloak.admin.username}")
    private String username;

    @Value("${keycloak.admin.password}")
    private String password;

    @Value("${keycloak.admin.client-secret}")
    private String clientSecret;

    @Bean
    public Keycloak keycloak()
    {
        return KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm("master") // Use master realm for admin operations
                .grantType(OAuth2Constants.PASSWORD)
                .clientId("admin-cli") // Use admin-cli client for admin operations
                .clientSecret(clientSecret.isEmpty() ? null : clientSecret)
                .username(username)
                .password(password)
                .build();
    }
}

