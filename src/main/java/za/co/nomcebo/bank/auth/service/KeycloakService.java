package za.co.nomcebo.bank.auth.service;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import za.co.nomcebo.bank.auth.model.User;

import jakarta.ws.rs.core.Response;
import java.util.Collections;
import java.util.List;

/**
 * Keycloak integration service for Nomcebo Bank Authentication.
 * <p>
 * Provides integration with Keycloak Identity and Access Management platform
 * for user management, authentication, and session control. Handles user
 * creation, token invalidation, and user synchronization.
 * </p>
 * <p>
 * Key Features:
 * - User creation in Keycloak realm
 * - Password credential management
 * - Session and token invalidation
 * - User attribute synchronization
 * - South African ID number storage
 * </p>
 *
 * @author Nqobile Thabo Buthelezi
 * @version 1.0.0
 * @since 2025-07-13
 */
@Service
@Slf4j
public class KeycloakService {

    private final Keycloak keycloak;
    private final String realm;

    /**
     * Constructs KeycloakService with Keycloak client and realm name.
     *
     * @param keycloak Keycloak admin client instance
     * @param realm Keycloak realm name from configuration
     */
    public KeycloakService(Keycloak keycloak, @Value("${keycloak.realm}") String realm) {
        this.keycloak = keycloak;
        this.realm = realm;
    }

    /**
     * Creates a new user in Keycloak realm.
     * <p>
     * Registers user with complete profile information including:
     * - Username and email
     * - First name and last name
     * - Password credentials
     * - South African ID number as custom attribute
     * - Email verification status
     * </p>
     *
     * @param user User entity with profile information
     * @param password User's plain-text password (will be hashed by Keycloak)
     * @throws RuntimeException if user creation fails
     */
    public void createUser(User user, String password) {
        log.info("Creating user in Keycloak: {}", user.getUsername());
        
        UserRepresentation userRep = new UserRepresentation();
        userRep.setUsername(user.getUsername());
        userRep.setEmail(user.getEmail());
        userRep.setFirstName(user.getFirstName());
        userRep.setLastName(user.getLastName());
        userRep.setEnabled(true);
        userRep.setEmailVerified(false);
        
        // Store SA ID number as custom attribute for compliance
        userRep.setAttributes(Collections.singletonMap(
            "saIdNumber", 
            Collections.singletonList(user.getSouthAfricanIdNumber())
        ));

        // Set password credential
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(password);
        credential.setTemporary(false);
        userRep.setCredentials(Collections.singletonList(credential));

        // Create user in Keycloak
        RealmResource realmResource = keycloak.realm(realm);
        UsersResource usersResource = realmResource.users();
        Response response = usersResource.create(userRep);
        
        if (response.getStatus() != 201) {
            log.error("Failed to create user in Keycloak. Status: {}, Info: {}", 
                response.getStatus(), response.getStatusInfo());
            throw new RuntimeException("Failed to create user in Keycloak: " + response.getStatusInfo());
        }
        
        response.close();
        log.info("User created successfully in Keycloak: {}", user.getUsername());
    }

    /**
     * Invalidates all active sessions and tokens for a user.
     * <p>
     * Logs out user from all sessions and revokes all active tokens.
     * This is used during logout or security events requiring immediate
     * session termination.
     * </p>
     * <p>
     * Implementation:
     * 1. Searches for user by username in Keycloak
     * 2. Logs out user from all active sessions
     * 3. Invalidates all user's tokens
     * </p>
     *
     * @param username Username of the user whose tokens should be invalidated
     */
    public void invalidateUserTokens(String username) {
        log.info("Invalidating tokens for user: {}", username);
        
        try {
            RealmResource realmResource = keycloak.realm(realm);
            UsersResource usersResource = realmResource.users();
            
            // Search for user by username
            List<UserRepresentation> users = usersResource.search(username, true);
            
            if (users.isEmpty()) {
                log.warn("User not found in Keycloak for token invalidation: {}", username);
                return;
            }
            
            // Get the first matching user
            UserRepresentation userRep = users.get(0);
            UserResource userResource = usersResource.get(userRep.getId());
            
            // Logout user from all sessions (this invalidates all tokens)
            userResource.logout();
            
            log.info("Successfully invalidated tokens for user: {}", username);
        } catch (Exception e) {
            log.error("Error invalidating tokens for user {}: {}", username, e.getMessage(), e);
            // Don't throw exception - logout should succeed even if Keycloak invalidation fails
        }
    }
}
