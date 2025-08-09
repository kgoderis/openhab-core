package org.openhab.core.ai.tool.security.filters;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Result of authentication operations.
 * 
 * This class encapsulates the result of authentication operations, including
 * success status, user information, and authentication details.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class SecurityResult {

    private final boolean authenticated;
    private final String userId;
    private final String username;
    private final Map<String, Object> userInfo;
    private final Map<String, Object> details;

    /**
     * Create a new authentication result.
     * 
     * @param authenticated whether the authentication was successful
     * @param userId the user ID
     * @param username the username
     * @param userInfo additional user information
     * @param details authentication details
     */
    public SecurityResult(boolean authenticated, String userId, String username, Map<String, Object> userInfo,
            Map<String, Object> details) {
        this.authenticated = authenticated;
        this.userId = userId;
        this.username = username;
        this.userInfo = userInfo;
        this.details = details;
    }

    /**
     * Create a successful authentication result.
     * 
     * @param userId the user ID
     * @param username the username
     * @return successful authentication result
     */
    public static SecurityResult success(String userId, String username) {
        return new SecurityResult(true, userId, username, Map.of(), Map.of());
    }

    /**
     * Create a failed authentication result.
     * 
     * @return failed authentication result
     */
    public static SecurityResult failure() {
        return new SecurityResult(false, "", "", Map.of(), Map.of());
    }

    /**
     * Check if the authentication was successful.
     * 
     * @return true if authenticated, false otherwise
     */
    public boolean isAuthenticated() {
        return authenticated;
    }

    /**
     * Get the user ID.
     * 
     * @return the user ID
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Get the username.
     * 
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Get additional user information.
     * 
     * @return user information
     */
    public Map<String, Object> getUserInfo() {
        return userInfo;
    }

    /**
     * Get authentication details.
     * 
     * @return authentication details
     */
    public Map<String, Object> getDetails() {
        return details;
    }
}
