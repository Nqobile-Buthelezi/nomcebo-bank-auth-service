package za.co.nomcebo.bank.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import za.co.nomcebo.bank.auth.model.User;

import java.util.UUID;

/**
 * Repository interface for User entity operations.
 * <p>
 * Provides methods to find users by username or email, and to check for existence
 * of users by email or South African ID number. Use existsBy... methods for greater control.
 * </p>
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Finds a user by their username.
     *
     * @param username the username to search for
     * @return the User entity if found, otherwise null
     */
    User findByUsername(String username);

    /**
     * Finds a user by their email address.
     *
     * @param email the email to search for
     * @return the User entity if found, otherwise null
     */
    User findByEmail(String email);

    /**
     * Checks if a user exists with the given email.
     *
     * @param email the email to check
     * @return true if a user exists with the email, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Checks if a user exists with the given South African ID number.
     *
     * @param southAfricanIdNumber the SA ID number to check
     * @return true if a user exists with the SA ID number, false otherwise
     */
    boolean existsBySouthAfricanIdNumber(String southAfricanIdNumber);

    /**
     * Checks if a user exists with the given email, excluding a specific user ID.
     * Useful for updates to ensure email uniqueness.
     *
     * @param email the email to check
     * @param id the user ID to exclude
     * @return true if another user exists with the email, false otherwise
     */
    boolean existsByEmailAndIdNot(String email, UUID id);

}
