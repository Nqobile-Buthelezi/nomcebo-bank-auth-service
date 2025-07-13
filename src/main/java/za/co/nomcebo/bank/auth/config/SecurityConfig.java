package za.co.nomcebo.bank.auth.config;

import za.co.nomcebo.bank.auth.security.JwtAuthenticationEntryPoint;
import za.co.nomcebo.bank.auth.security.JwtAuthenticationFilter;
import za.co.nomcebo.bank.auth.security.KeycloakAuthenticationProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Security configuration for Nomcebo Bank Auth Service.
 * <p>
 * Implements comprehensive security measures including:
 * - JWT token-based authentication
 * - Keycloak integration for identity management
 * - CORS configuration for web clients
 * - Method-level security authorization
 * - Rate limiting and security headers
 * - Compliance with South African banking security standards
 * </p>
 * Security features:
 * - Stateless session management
 * - Strong password encoding (BCrypt)
 * - Custom authentication entry point
 * - JWT token validation
 * - Role-based access control
 *
 * @author Nqobile Thabo Buthelezi
 * @version 1.0.0
 * @since 2025-07-13
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final KeycloakAuthenticationProvider keycloakAuthenticationProvider;

    @Value("${keycloak.auth-server-url}")
    private String keycloakServerUrl;

    @Value("${keycloak.realm}")
    private String keycloakRealm;

    @Value("${nomcebo.cors.allowed-origins}")
    private List<String> allowedOrigins;

    /**
     * Configures the main security filter chain.
     *
     * Security configuration includes:
     * - Public endpoints for authentication
     * - Protected endpoints requiring authentication
     * - JWT token processing
     * - Exception handling
     * - Security headers for banking compliance
     *
     * @param http HttpSecurity configuration
     * @return SecurityFilterChain
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                // Disable CSRF for stateless JWT authentication
                .csrf(AbstractHttpConfigurer::disable)

                // Configure CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Configure session management
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Configure authentication entry point
                .exceptionHandling(exceptions ->
                        exceptions.authenticationEntryPoint(jwtAuthenticationEntryPoint))

                // Configure authorization rules
                .authorizeHttpRequests(authz -> authz
                        // Public endpoints
                        .requestMatchers(
                                "/api/auth/login",
                                "/api/auth/register",
                                "/api/auth/refresh",
                                "/api/auth/reset-password",
                                "/api/auth/verify-email/**"
                        ).permitAll()

                        // Actuator endpoints (health checks)
                        .requestMatchers(
                                "/actuator/health",
                                "/actuator/info",
                                "/actuator/metrics"
                        ).permitAll()

                        // OpenAPI/Swagger documentation
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/swagger-resources/**",
                                "/webjars/**"
                        ).permitAll()

                        // H2 Console for development
                        .requestMatchers("/h2-console/**").permitAll()

                        // Admin endpoints - restricted to admin users
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // User management endpoints
                        .requestMatchers("/api/users/**").hasAnyRole("USER", "ADMIN")

                        // All other endpoints require authentication
                        .anyRequest().authenticated()
                )

                // Add JWT authentication filter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                // Configure OAuth2 resource server for JWT validation
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.decoder(jwtDecoder()))
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                )

                .build();
    }

    /**
     * Configures CORS for cross-origin requests.
     *
     * Allows requests from approved origins including:
     * - Local development environments
     * - Production web applications
     * - Mobile applications
     *
     * @return CorsConfigurationSource
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Set allowed origins from configuration
        configuration.setAllowedOrigins(allowedOrigins != null ? allowedOrigins :
                Arrays.asList("http://localhost:3000", "https://nomcebo-bank.co.za"));

        // Set allowed methods
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        ));

        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization", "Content-Type", "X-Requested-With", "Accept",
                "Origin", "Access-Control-Request-Method", "Access-Control-Request-Headers"
        ));

        configuration.setExposedHeaders(Arrays.asList(
                "Authorization", "Content-Type", "X-Total-Count"
        ));

        configuration.setAllowCredentials(true);

        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    /**
     * Configures JWT decoder for token validation.
     *
     * @return JwtDecoder configured for Keycloak
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        String jwkSetUri = keycloakServerUrl + "/realms/" + keycloakRealm + "/protocol/openid-connect/certs";
        return NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
    }

    /**
     * Configures authentication manager with Keycloak provider.
     *
     * @return AuthenticationManager
     */
    @Bean
    public AuthenticationManager authenticationManager() {
        return new ProviderManager(keycloakAuthenticationProvider);
    }

    /**
     * Configures password encoder with BCrypt.
     *
     * BCrypt is chosen for its adaptive nature and resistance to rainbow table attacks.
     * Strength is set to 12 for enhanced security suitable for banking applications.
     *
     * @return PasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}