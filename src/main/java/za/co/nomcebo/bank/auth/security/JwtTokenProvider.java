package za.co.nomcebo.bank.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String jwtSecret;

    public String generateAccessToken(Authentication authentication) {
        return null;
    }

    public String generateRefreshToken(Authentication authentication) {
        return null;
    }

    public Long getAccessTokenValidity() {
        return null;
    }

    public String getUsernameFromToken(String refreshToken) {
        return null;
    }

    public boolean validateRefreshToken(String refreshToken) {
        // TODO: Implement actual JWT validation logic (signature, expiration, etc.)
        // This is a placeholder for demonstration purposes.
        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            return false;
        }
        // Add real validation logic here (e.g., parse token, check signature, check expiry)
        return true;
    }

    public boolean validateToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(jwtSecret.getBytes())
                    .parseClaimsJws(token)
                    .getBody();
            // Optionally check expiration
            if (claims.getExpiration() != null && claims.getExpiration().before(new java.util.Date())) {
                return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

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

    public LocalDateTime getExpirationDateFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(jwtSecret.getBytes())
                .parseClaimsJws(token)
                .getBody();
        Date expiration = claims.getExpiration();
        if (expiration == null) {
            return null;
        }
        return expiration.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }
}
