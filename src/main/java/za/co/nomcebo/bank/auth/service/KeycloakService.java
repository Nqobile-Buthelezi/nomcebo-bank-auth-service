package za.co.nomcebo.bank.auth.service;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import za.co.nomcebo.bank.auth.model.User;

import jakarta.ws.rs.core.Response;
import java.util.Collections;

@Service
public class KeycloakService {

    private final Keycloak keycloak;
    private final String realm;

    public KeycloakService(Keycloak keycloak, @Value("${keycloak.realm}") String realm) {
        this.keycloak = keycloak;
        this.realm = realm;
    }

    public void createUser(User user, String password) {
        UserRepresentation userRep = new UserRepresentation();
        userRep.setUsername(user.getUsername());
        userRep.setEmail(user.getEmail());
        userRep.setFirstName(user.getFirstName());
        userRep.setLastName(user.getLastName());
        userRep.setEnabled(true);
        userRep.setEmailVerified(false);
        userRep.setAttributes(Collections.singletonMap("saIdNumber", Collections.singletonList(user.getSouthAfricanIdNumber())));

        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(password);
        credential.setTemporary(false);
        userRep.setCredentials(Collections.singletonList(credential));

        RealmResource realmResource = keycloak.realm(realm);
        UsersResource usersResource = realmResource.users();
        Response response = usersResource.create(userRep);
        if (response.getStatus() != 201) {
            throw new RuntimeException("Failed to create user in Keycloak: " + response.getStatusInfo());
        }
        response.close();
    }

    public void invalidateUserTokens(String username) {

    }
}
