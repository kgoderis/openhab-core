package org.openhab.core.ai.common.auth;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Authentication context for AI service requests.
 * 
 * This class encapsulates authentication information including user identity,
 * permissions, and session details for AI protocol requests.
 * 
 * @author AI Assistant
 * @since 1.0.0
 */
@NonNullByDefault
public class AIAuthenticationContext {

    private final String principalId;
    private final String authenticationScheme;
    private final Map<String, String> credentials;
    private final Set<String> permissions;
    private final @NonNull Instant issuedAt;
    private final @Nullable Instant expiresAt;
    private final String sessionId;

    /**
     * Create a new authentication context.
     * 
     * @param principalId The principal identifier (user, service, etc.)
     * @param authenticationScheme The authentication scheme used
     * @param credentials Map of credentials (tokens, keys, etc.)
     * @param permissions Set of permissions granted to this principal
     * @param issuedAt When this context was created
     * @param expiresAt When this context expires (optional)
     * @param sessionId Unique session identifier
     */
    public AIAuthenticationContext(String principalId, String authenticationScheme, Map<String, String> credentials,
            Set<String> permissions, Instant issuedAt, @Nullable Instant expiresAt, String sessionId) {
        this.principalId = Objects.requireNonNull(principalId, "Principal ID cannot be null");
        this.authenticationScheme = Objects.requireNonNull(authenticationScheme,
                "Authentication scheme cannot be null");
        this.credentials = Map.copyOf(Objects.requireNonNull(credentials, "Credentials cannot be null"));
        this.permissions = Set.copyOf(Objects.requireNonNull(permissions, "Permissions cannot be null"));
        this.issuedAt = Objects.requireNonNull(issuedAt, "Issued at cannot be null");
        this.expiresAt = expiresAt;
        this.sessionId = Objects.requireNonNull(sessionId, "Session ID cannot be null");
    }

    /**
     * Get the principal identifier.
     * 
     * @return Principal ID
     */
    public String getPrincipalId() {
        return principalId;
    }

    /**
     * Get the authentication scheme.
     * 
     * @return Authentication scheme
     */
    public String getAuthenticationScheme() {
        return authenticationScheme;
    }

    /**
     * Get a specific credential value.
     * 
     * @param key The credential key
     * @return The credential value, or empty if not found
     */
    public Optional<String> getCredential(String key) {
        return Optional.ofNullable(credentials.get(key));
    }

    /**
     * Get all credentials (read-only).
     * 
     * @return Immutable map of credentials
     */
    public Map<String, String> getCredentials() {
        return credentials;
    }

    /**
     * Get the permissions for this context.
     * 
     * @return Immutable set of permissions
     */
    public Set<String> getPermissions() {
        return permissions;
    }

    /**
     * Check if this context has a specific permission.
     * 
     * @param permission The permission to check
     * @return true if the permission is granted, false otherwise
     */
    public boolean hasPermission(String permission) {
        return permissions.contains(permission);
    }

    /**
     * Get when this context was issued.
     * 
     * @return Issued timestamp
     */
    public Instant getIssuedAt() {
        return issuedAt;
    }

    /**
     * Get when this context expires.
     * 
     * @return Expiration timestamp, or empty if it doesn't expire
     */
    public Optional<Instant> getExpiresAt() {
        return Optional.ofNullable(expiresAt);
    }

    /**
     * Check if this context is expired.
     * 
     * @return true if expired, false otherwise
     */
    public boolean isExpired() {
        Instant expiry = expiresAt;
        return expiry != null && Instant.now().isAfter(expiry);
    }

    /**
     * Get the session identifier.
     * 
     * @return Session ID
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * Check if this context is valid (not expired).
     * 
     * @return true if valid, false if expired
     */
    public boolean isValid() {
        return !isExpired();
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        AIAuthenticationContext that = (AIAuthenticationContext) o;
        return Objects.equals(principalId, that.principalId) && Objects.equals(sessionId, that.sessionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(principalId, sessionId);
    }

    @Override
    public String toString() {
        String expiresAtStr = expiresAt != null ? expiresAt.toString() : "null";
        return "AIAuthenticationContext{" + "principalId='" + principalId + '\'' + ", authenticationScheme='"
                + authenticationScheme + '\'' + ", permissions=" + permissions + ", issuedAt=" + issuedAt
                + ", expiresAt=" + expiresAtStr + ", sessionId='" + sessionId + '\'' + ", valid=" + isValid() + '}';
    }
}
