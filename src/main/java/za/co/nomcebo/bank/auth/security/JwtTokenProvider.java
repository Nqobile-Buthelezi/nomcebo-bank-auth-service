package za.co.nomcebo.bank.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * JWT Token Provider for managing JSON Web Tokens.
 * <p>
 * Handles creation, validation, and parsing of JWT tokens for authentication
 * and authorization. Implements secure token generation with configurable
 * expiration times and role-based access control.
 * </p>
 * <p>
 * Key Features:
 * - Access token generation with 15-minute expiration
 * - Refresh token generation with 7-day expiration
 * - Token validation with signature verification
 * - Role and authority extraction from tokens
 * - Secure signing using HS512 algorithm
 * </p>
 *
 * @author Nqobile Thabo Buthelezi
 * @version 1.0.0
 * @since 2025-07-13
 */
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String jwtSecret;

    /** Access token validity duration in milliseconds (15 minutes) */
    private static final long ACCESS_TOKEN_VALIDITY = 15 * 60 * 1000L;

    /** Refresh token validity duration in milliseconds (7 days) */
    private static final long REFRESH_TOKEN_VALIDITY = 7 * 24 * 60 * 60 * 1000L;

    /**
     * Generates an access token for authenticated user.
     * <p>
     * Creates a JWT token containing user's username and roles with 15-minute expiration.
     * The token is signed using HS512 algorithm with the configured secret key.
     * </p>
     *
     * @param authentication The authenticated user's authentication object
     * @return JWT access token as string
     */
    public String generateAccessToken(Authentication authentication) {
        String username = authentication.getName();
        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + ACCESS_TOKEN_VALIDITY);

        return Jwts.builder()
                .setSubject(username)
                .claim("roles", roles)
                .claim("type", "access")
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, jwtSecret.getBytes())
                .compact();
    }

    /**
     * Generates a refresh token for authenticated user.
     * <p>
     * Creates a long-lived JWT token for obtaining new access tokens.
     * Refresh tokens have 7-day expiration and contain minimal user information.
     * </p>
     *
     * @param authentication The authenticated user's authentication object
     * @return JWT refresh token as string
     */
    public String generateRefreshToken(Authentication authentication) {
        String username = authentication.getName();
        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + REFRESH_TOKEN_VALIDITY);

        return Jwts.builder()
                .setSubject(username)
                .claim("roles", roles)
                .claim("type", "refresh")
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, jwtSecret.getBytes())
                .compact();
    }

    /**
     * Gets the access token validity period in seconds.
     *
     * @return Access token validity in seconds
     */
    public Long getAccessTokenValidity() {
        return ACCESS_TOKEN_VALIDITY / 1000;
    }

    /**
     * Extracts username from JWT token.
     *
     * @param token JWT token (access or refresh)
     * @return Username from token subject
     */
    public String getUsernameFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(jwtSecret.getBytes())
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getSubject();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Validates refresh token.
     * <p>
     * Performs comprehensive validation including:
     * - Signature verification
     * - Expiration check
     * - Token type verification
     * - Null and empty checks
     * </p>
     *
     * @param refreshToken The refresh token to validate
     * @return true if token is valid, false otherwise
     */
    public boolean validateRefreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            return false;
        }

        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(jwtSecret.getBytes())
                    .parseClaimsJws(refreshToken)
                    .getBody();

            // Verify token type
            String tokenType = claims.get("type", String.class);
            if (!"refresh".equals(tokenType)) {
                return false;
            }

            // Check expiration
            if (claims.getExpiration() != null && claims.getExpiration().before(new Date())) {
                return false;
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Validates any JWT token (access or refresh).
     * <p>
     * Verifies token signature, expiration, and format.
     * Returns false for any invalid, expired, or malformed tokens.
     * </p>
     *
     * @param token JWT token to validate
     * @return true if token is valid, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(jwtSecret.getBytes())
                    .parseClaimsJws(token)
                    .getBody();
            
            // Check expiration
            if (claims.getExpiration() != null && claims.getExpiration().before(new Date())) {
                return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Extracts authentication object from JWT token.
     * <p>
     * Parses the token and creates an Authentication object containing
     * username and granted authorities (roles).
     * </p>
     *
     * @param token JWT token to parse
     * @return Authentication object with user details and authorities
     */
    public Authentication getAuthenticationFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(jwtSecret.getBytes())
                    .parseClaimsJws(token)
                    .getBody();
            
            String username = claims.getSubject();
            List<String> roles = claims.get("roles", List.class);
            Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
            
            if (roles != null) {
                for (String role : roles) {
                    authorities.add(new SimpleGrantedAuthority(role));
                }
            }
            
            return new UsernamePasswordAuthenticationToken(username, null, authorities);
        } catch (SignatureException | IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Extracts expiration date from JWT token.
     *
     * @param token JWT token to parse
     * @return LocalDateTime representing token expiration, or null if not present
     */
    public LocalDateTime getExpirationDateFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(jwtSecret.getBytes())
                    .parseClaimsJws(token)
                    .getBody();
            
            Date expiration = claims.getExpiration();
            if (expiration == null) {
                return null;
            }
            
            return expiration.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        } catch (Exception e) {
            return null;
        }
    }
}
