package org.openhab.core.ai.auth;

import java.time.Instant;
import java.util.Optional;

/**
 * JWT token manager interface for AI authentication.
 * 
 * This interface defines the contract for JWT token generation,
 * validation, and management in AI protocol authentication.
 * 
 * 
 */
public interface JWTManager {

    /**
     * Generate a JWT token for an authentication context.
     * 
     * @param context Authentication context
     * @return JWT token string
     */
    String generateToken(AuthenticationContext context);

    /**
     * Generate a JWT token with custom expiration.
     * 
     * @param context Authentication context
     * @param expiresAt Custom expiration time
     * @return JWT token string
     */
    String generateToken(AuthenticationContext context, Instant expiresAt);

    /**
     * Validate a JWT token and extract authentication context.
     * 
     * @param token JWT token to validate
     * @return Authentication context if token is valid
     */
    Optional<AuthenticationContext> validateToken(String token);

    /**
     * Check if a JWT token is valid without extracting context.
     * 
     * @param token JWT token to check
     * @return true if token is valid
     */
    boolean isTokenValid(String token);

    /**
     * Get the expiration time of a JWT token.
     * 
     * @param token JWT token
     * @return Expiration time, or empty if token is invalid
     */
    Optional<Instant> getTokenExpiration(String token);

    /**
     * Refresh a JWT token.
     * 
     * @param token Current JWT token
     * @return New JWT token, or empty if refresh failed
     */
    Optional<String> refreshToken(String token);

    /**
     * Invalidate a JWT token.
     * 
     * @param token JWT token to invalidate
     * @return true if token was invalidated
     */
    boolean invalidateToken(String token);

    /**
     * Get the issuer of JWT tokens.
     * 
     * @return Token issuer
     */
    String getIssuer();

    /**
     * Set the signing key for JWT tokens.
     * 
     * @param signingKey Signing key
     */
    void setSigningKey(String signingKey);
}
