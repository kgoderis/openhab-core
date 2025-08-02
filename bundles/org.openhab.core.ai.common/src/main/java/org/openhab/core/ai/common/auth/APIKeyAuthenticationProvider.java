package org.openhab.core.ai.common.auth;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Authentication provider that uses API keys.
 * 
 * This provider authenticates clients using API keys, typically passed
 * in HTTP headers or request parameters.
 * 
 * 
 */
@NonNullByDefault
public class APIKeyAuthenticationProvider implements AIAuthenticationProvider {

    private static final Logger logger = LoggerFactory.getLogger(APIKeyAuthenticationProvider.class);
    private static final String PROVIDER_NAME = "api-key";
    private static final String AUTH_SCHEME = "API-Key";

    private final String expectedApiKey;
    private final String apiKeyHeader;
    private final boolean enabled;

    /**
     * Create a new API key authentication provider.
     * 
     * @param expectedApiKey The expected API key value
     * @param apiKeyHeader The header name for the API key (default: "X-API-Key")
     * @param enabled Whether this provider is enabled
     */
    public APIKeyAuthenticationProvider(String expectedApiKey, String apiKeyHeader, boolean enabled) {
        this.expectedApiKey = expectedApiKey;
        this.apiKeyHeader = apiKeyHeader != null ? apiKeyHeader : "X-API-Key";
        this.enabled = enabled;
        logger.info("API Key Authentication Provider initialized with header: {}", this.apiKeyHeader);
    }

    @Override
    public Optional<AIAuthenticationContext> authenticate(Map<String, String> credentials) {
        if (!enabled) {
            logger.debug("API key authentication provider is disabled");
            return Optional.empty();
        }

        if (expectedApiKey == null || expectedApiKey.trim().isEmpty()) {
            logger.warn("No API key configured for authentication");
            return Optional.empty();
        }

        // Check for API key in various possible locations
        String providedApiKey = credentials.get("api_key");
        if (providedApiKey == null) {
            providedApiKey = credentials.get("apikey");
        }
        if (providedApiKey == null) {
            providedApiKey = credentials.get(apiKeyHeader);
        }
        if (providedApiKey == null) {
            providedApiKey = credentials.get("authorization");
            if (providedApiKey != null && providedApiKey.startsWith("Bearer ")) {
                providedApiKey = providedApiKey.substring(7);
            }
        }

        if (providedApiKey == null) {
            logger.debug("No API key provided in credentials");
            return Optional.empty();
        }

        try {
            if (validateApiKey(providedApiKey)) {
                logger.info("API key authentication successful");

                // Create authentication context
                Map<String, String> contextCredentials = new HashMap<>();
                contextCredentials.put("api_key", providedApiKey);
                contextCredentials.put("provider", PROVIDER_NAME);
                contextCredentials.put("header", apiKeyHeader);

                Set<String> permissions = getDefaultPermissions();

                AIAuthenticationContext context = new AIAuthenticationContext(
                        "api-client-" + UUID.randomUUID().toString().substring(0, 8), AUTH_SCHEME, contextCredentials,
                        permissions, Instant.now(), Instant.now().plusSeconds(7200), // 2 hours expiration for API keys
                        UUID.randomUUID().toString());

                return Optional.of(context);
            } else {
                logger.warn("Invalid API key provided");
                return Optional.empty();
            }
        } catch (Exception e) {
            logger.error("Error during API key authentication", e);
            return Optional.empty();
        }
    }

    @Override
    public boolean validateContext(AIAuthenticationContext context) {
        if (context == null) {
            return false;
        }

        // Check if context is expired
        Optional<Instant> expiresAt = context.getExpiresAt();
        if (expiresAt.isPresent() && Instant.now().isAfter(expiresAt.get())) {
            logger.debug("API key authentication context expired");
            return false;
        }

        // Check if API key is still valid
        Optional<String> apiKey = context.getCredential("api_key");
        if (apiKey.isPresent()) {
            return validateApiKey(apiKey.get());
        }

        return false;
    }

    @Override
    public Optional<AIAuthenticationContext> refreshAuthentication(AIAuthenticationContext context) {
        if (!validateContext(context)) {
            return Optional.empty();
        }

        // Create a new context with extended expiration
        Map<String, String> newCredentials = new HashMap<>(context.getCredentials());
        Set<String> permissions = context.getPermissions();

        AIAuthenticationContext newContext = new AIAuthenticationContext(context.getPrincipalId(),
                context.getAuthenticationScheme(), newCredentials, permissions, Instant.now(),
                Instant.now().plusSeconds(7200), // 2 hours expiration
                UUID.randomUUID().toString());

        logger.debug("Refreshed API key authentication context");
        return Optional.of(newContext);
    }

    @Override
    public String[] getSupportedSchemes() {
        return new String[] { AUTH_SCHEME, "Bearer" };
    }

    @Override
    public boolean supportsScheme(String scheme) {
        return AUTH_SCHEME.equals(scheme) || "Bearer".equals(scheme);
    }

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Validate the provided API key.
     * 
     * @param providedApiKey The API key to validate
     * @return true if the API key is valid
     */
    private boolean validateApiKey(String providedApiKey) {
        if (providedApiKey == null || providedApiKey.trim().isEmpty()) {
            return false;
        }

        // Simple string comparison (in production, use secure comparison)
        return expectedApiKey.equals(providedApiKey.trim());
    }

    /**
     * Get default permissions for API key clients.
     * 
     * @return Set of default permissions
     */
    private Set<String> getDefaultPermissions() {
        // API key clients get full access (can be restricted based on key)
        return Set.of("mcp:connect", "mcp:tools", "mcp:read", "mcp:write", "mcp:admin");
    }
}
