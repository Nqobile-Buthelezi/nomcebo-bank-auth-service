package za.co.nomcebo.bank.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service to automatically set up Keycloak realm and client configuration
 * when the application starts.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class KeycloakSetupService {

    private final Keycloak keycloak;

    @Value("${keycloak.realm}")
    private String realmName;

    @Value("${keycloak.resource}")
    private String clientId;

    /**
     * Sets up Keycloak realm and client when application starts
     */
    @EventListener(ApplicationReadyEvent.class)
    public void setupKeycloak() {
        try {
            log.info("Setting up Keycloak realm: {}", realmName);
            
            // Create realm if it doesn't exist
            createRealmIfNotExists();
            
            // Create client if it doesn't exist
            createClientIfNotExists();
            
            // Create default roles
            createDefaultRoles();
            
            log.info("Keycloak setup completed successfully");
            
        } catch (Exception e) {
            // Don't fail application startup, just log the error
            log.error("Failed to setup Keycloak: {}", e.getMessage(), e);
        }
    }

    private void createRealmIfNotExists() {
        try {
            // Try to get the realm
            keycloak.realm(realmName).toRepresentation();
            log.info("Realm '{}' already exists", realmName);
        } catch (NotFoundException e) {
            // Realm doesn't exist, create it
            log.info("Creating realm: {}", realmName);
            
            RealmRepresentation realm = new RealmRepresentation();
            realm.setRealm(realmName);
            realm.setDisplayName("Nomcebo Bank");
            realm.setEnabled(true);
            // Only allow registration through our API
            realm.setRegistrationAllowed(false);
            realm.setLoginWithEmailAllowed(true);
            realm.setDuplicateEmailsAllowed(false);
            realm.setResetPasswordAllowed(true);
            realm.setEditUsernameAllowed(false);
            
            // Set token lifespans
            realm.setAccessTokenLifespan(900); // 15 minutes
            realm.setRefreshTokenMaxReuse(0);
            realm.setSsoSessionMaxLifespan(43200); // 12 hours
            
            keycloak.realms().create(realm);
            
//            if (response.getStatus() == 201) {
//                log.info("Realm '{}' created successfully", realmName);
//            } else {
//                log.error("Failed to create realm. Status: {}", response.getStatus());
//            }
//            response.close();
        }
    }

    private void createClientIfNotExists() {
        try {
            RealmResource realmResource = keycloak.realm(realmName);
            
            // Check if client exists
            List<ClientRepresentation> existingClients = realmResource.clients()
                    .findByClientId(clientId);
            
            if (!existingClients.isEmpty()) {
                log.info("Client '{}' already exists", clientId);
                return;
            }
            
            log.info("Creating client: {}", clientId);
            
            ClientRepresentation client = new ClientRepresentation();
            client.setClientId(clientId);
            client.setName("Nomcebo Bank Auth Service");
            client.setDescription("Authentication service for Nomcebo Bank");
            client.setEnabled(true);
            client.setPublicClient(false); // Confidential client
            client.setDirectAccessGrantsEnabled(true); // Allow password grant
            client.setServiceAccountsEnabled(true);
            client.setAuthorizationServicesEnabled(false);
            
            // Set valid redirect URIs
            client.setRedirectUris(Arrays.asList(
                "http://localhost:8080/*",
                "http://localhost:3000/*"
            ));
            
            // Set web origins for CORS
            client.setWebOrigins(Arrays.asList(
                "http://localhost:8080",
                "http://localhost:3000"
            ));
            
            Response response = realmResource.clients().create(client);
            if (response.getStatus() == 201) {
                log.info("Client '{}' created successfully", clientId);
            } else {
                log.error("Failed to create client. Status: {}", response.getStatus());
            }
            response.close();
            
        } catch (Exception e) {
            log.error("Error creating client: {}", e.getMessage(), e);
        }
    }

    private void createDefaultRoles() {
        try {
            RealmResource realmResource = keycloak.realm(realmName);
            
            // Create USER role
            createRoleIfNotExists(realmResource, "USER", "Standard user role");
            
            // Create ADMIN role
            createRoleIfNotExists(realmResource, "ADMIN", "Administrator role");
            
        } catch (Exception e) {
            log.error("Error creating roles: {}", e.getMessage(), e);
        }
    }

    private void createRoleIfNotExists(RealmResource realmResource, String roleName, String description) {
        try {
            // Check if role exists
            realmResource.roles().get(roleName).toRepresentation();
            log.info("Role '{}' already exists", roleName);
        } catch (NotFoundException e) {
            // Role doesn't exist, create it
            log.info("Creating role: {}", roleName);
            
            RoleRepresentation role = new RoleRepresentation();
            role.setName(roleName);
            role.setDescription(description);
            
            realmResource.roles().create(role);
            log.info("Role '{}' created successfully", roleName);
        } catch (Exception ex) {
            log.error("Error creating role '{}': {}", roleName, ex.getMessage());
        }
    }
}