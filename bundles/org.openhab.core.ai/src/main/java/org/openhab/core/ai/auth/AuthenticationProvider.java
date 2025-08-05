package org.openhab.core.ai.auth;

import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Authentication provider interface for AI services.
 * 
 * This interface defines the contract for authentication providers
 * that can authenticate users for AI protocol access.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface AuthenticationProvider {

    /**
     * Authenticate a request using the provided credentials.
     * 
     * @param credentials Map containing authentication credentials (e.g., API keys, tokens)
     * @return Authentication context if successful, empty if authentication fails
     */
    Optional<AuthenticationContext> authenticate(Map<String, String> credentials);

    /**
     * Validate an existing authentication context.
     * 
     * @param context The authentication context to validate
     * @return true if the context is valid, false otherwise
     */
    boolean validateContext(AuthenticationContext context);

    /**
     * Refresh authentication tokens if supported.
     * 
     * @param context The current authentication context
     * @return New authentication context with refreshed tokens, or empty if refresh is not possible
     */
    Optional<AuthenticationContext> refreshAuthentication(AuthenticationContext context);

    /**
     * Get the authentication schemes supported by this provider.
     * 
     * @return Array of supported authentication schemes (e.g., "Bearer", "Basic", "API-Key")
     */
    String[] getSupportedSchemes();

    /**
     * Check if the provider supports a specific authentication scheme.
     * 
     * @param scheme The authentication scheme to check
     * @return true if the scheme is supported, false otherwise
     */
    default boolean supportsScheme(String scheme) {
        String[] supported = getSupportedSchemes();
        for (String supportedScheme : supported) {
            if (supportedScheme.equalsIgnoreCase(scheme)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get the name of this authentication provider.
     * 
     * @return Provider name
     */
    String getProviderName();

    /**
     * Check if this provider is currently enabled.
     * 
     * @return true if enabled, false otherwise
     */
    boolean isEnabled();
}
