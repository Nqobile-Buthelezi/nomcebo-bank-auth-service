package za.co.nomcebo.bank.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import za.co.nomcebo.bank.auth.dto.*;
import za.co.nomcebo.bank.auth.service.AuthService;

/**
 * Authentication controller for Nomcebo Bank Authentication Service
 * <p>
 * Handles user authentication, registration, token management and logout
 * operations. All endpoints comply with South African banking regulations
 * and most importantly POPIA ( Protection of Personal Information Act ) requirements.
 * </p>
 *
 * @author Nqobile Thabo Buthelezi
 * @version 1.0.0
 * @since 2025-07-13
 */
@RestController
@RequestMapping("/api/auth")
@Tag(
        name = "Authentication",
        description = "Authentication and authorisation endpoints"
)
@Slf4j
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000"})
public class AuthController {

    private final AuthService authService;

    /**
     * Registers a new user account.
     * <p>
     * Creates new user in Keycloak with appropriate roles for South African Banking
     * Validates SA ID number format and performs KYC checks.
     *
     * @param registerRequest new user registration details
     * @param request         HTTP request for IP tracking
     * @return Registration confirmation and user details
     */
    @Operation(
            summary = "User Registration",
            description = "Register new user account with KYC validation and SA ID verification"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "User registered successfully",
                    content = @Content(schema = @Schema(implementation = RegisterResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid registration data or SA ID format",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "User already exists",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
            )
    })
    @PostMapping("/register")
    public ResponseEntity<RegisterResponseDTO> register(
            @Valid @RequestBody RegisterRequestDTO registerRequest,
            HttpServletRequest request
    ) {
        log.info(
                "Registration attempt for email: {} from IP: {}",
                registerRequest.getEmail(), request.getRemoteAddr()
        );
        RegisterResponseDTO response = authService.register(registerRequest, request.getRemoteAddr());

        log.info(
                "Registration successful for email: {}", registerRequest.getEmail()
        );
        return ResponseEntity.status(201).body(response);
    }

    /**
     * Authenticates a user and returns JWT tokens.
     * <p>
     * Validates user credentials against Keycloak and returns access/refresh tokens.
     * Implements rate limiting and audit logging for security compliance.
     *
     * @param loginRequest User login credentials
     * @param request      HTTP request for IP tracking
     * @return JWT tokens and user information
     */
    @Operation(
            summary = "User Login",
            description = "Authenticate user with email/username and password. Returns JWT access and refresh tokens."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Authentication successful.",
                    content = @Content(schema = @Schema(implementation = LoginResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request format.",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid credentials.",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "423",
                    description = "Account locked due to multiple failed attempts.",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "429",
                    description = "Too many requests - rate limit exceeded.",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
            )
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(
            @Valid @RequestBody LoginRequestDTO loginRequest,
            HttpServletRequest request
    ) {
        log.info(
                "Login attempt for user: {} from IP: {}",
                loginRequest.getUsername(), request.getRemoteAddr()
        );
        LoginResponseDTO response = authService.authenticate(
                loginRequest,
                request.getRemoteAddr()
        );
        log.info(
                "Login successful for user: {}",
                loginRequest.getUsername()
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Refreshes JWT access token using refresh token.
     *
     * @param refreshRequest Refresh token request
     * @return New access token and updated refresh token
     */
    @Operation(
            summary = "Refresh Token",
            description = "Generate new access token using valid refresh token"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Token refreshed successfully",
                    content = @Content(schema = @Schema(implementation = TokenResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid or expired refresh token",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
            )
    })
    @PostMapping("refresh")
    public ResponseEntity<TokenResponseDTO> refresh(
            @Valid @RequestBody RefreshTokenRequestDTO refreshRequest
    ) {
        log.info("Token refresh request received");

        TokenResponseDTO response = authService.refreshToken(refreshRequest);

        log.info("Token refresh successful");
        return ResponseEntity.ok(response);
    }

    /**
     * Validates JWT token and returns user information.
     *
     * @param token JWT token from Authorisation header
     * @return Token validation result and user details
     */
    @Operation(
            summary = "Validate Token",
            description = "Validate JWT token and return user information",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Token is valid",
                    content = @Content(schema = @Schema(implementation = TokenValidationResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid or expired token",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
            )
    })
    @GetMapping("/validate")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<TokenValidationResponseDTO> validate(
            @Parameter(description = "JWT token", required = true)
            @RequestHeader("Authorization") String token
    ) {
        log.info("Token validation request received.");

        TokenValidationResponseDTO response = authService.validateToken(token);

        log.info("Token validation request successfully processed.");

        return ResponseEntity.ok(response);
    }

    /**
     * Logs out user and invalidates tokens.
     *
     * @param logoutRequest Logout request with refresh token
     * @param request       HTTP request for session tracking
     * @return Lout confirmation
     */
    @Operation(
            summary = "User Logout",
            description = "Logout user and invalidate all tokens",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Logout successful",
                    content = @Content(schema = @Schema(implementation = LogoutResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid token",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
            )
    })
    @PostMapping("/logout")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<LogoutResponseDTO> logout(
            @Valid @RequestBody LogoutRequestDTO logoutRequest,
            HttpServletRequest request
    ) {
        log.info("Logout request received from IP: {}", request.getRemoteAddr());

        LogoutResponseDTO response = authService.logout(logoutRequest, request.getRemoteAddr());

        log.info("Logout successful");
        return ResponseEntity.ok(response);
    }

    /**
     * Initiates password reset process.
     *
     * @param resetRequest Password reset request
     * @return Password reset confirmation
     */
    @Operation(
            summary = "Password Reset",
            description = "Initiate password reset process via email."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Password reset email sent",
                    content = @Content(schema = @Schema(implementation = PasswordResetResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "429",
                    description = "Too many reset attempts",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
            )
    })
    @PostMapping("/reset-password")
    public ResponseEntity<PasswordResetResponseDTO> resetPassword(
            @Valid @RequestBody PasswordResetRequestDTO resetRequest
    ) {
        log.info(
                "Password reset request for email: {}",
                resetRequest.getEmail()
        );
        PasswordResetResponseDTO response = authService.resetPassword(resetRequest);

        log.info("Password reset successful for email: {}",
                resetRequest.getEmail()
        );

        return ResponseEntity.ok(response);
    }

}
