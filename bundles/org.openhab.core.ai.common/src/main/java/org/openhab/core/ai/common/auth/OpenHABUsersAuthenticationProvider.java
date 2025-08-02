package org.openhab.core.ai.common.auth;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.OpenHAB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Authentication provider that uses openHAB's users.properties file.
 * 
 * This provider authenticates users against the standard openHAB users.properties
 * file located in the openHAB configuration directory.
 * 
 * 
 */
@NonNullByDefault
public class OpenHABUsersAuthenticationProvider implements AIAuthenticationProvider {

    private static final Logger logger = LoggerFactory.getLogger(OpenHABUsersAuthenticationProvider.class);
    private static final String PROVIDER_NAME = "openhab-users";
    private static final String AUTH_SCHEME = "Basic";

    private final String usersFilePath;
    private final boolean enabled;

    /**
     * Create a new OpenHAB users authentication provider.
     * 
     * @param usersFilePath Path to the users.properties file (null for default)
     * @param enabled Whether this provider is enabled
     */
    public OpenHABUsersAuthenticationProvider(String usersFilePath, boolean enabled) {
        this.usersFilePath = usersFilePath != null ? usersFilePath
                : Paths.get(OpenHAB.getConfigFolder(), "users.properties").toString();
        this.enabled = enabled;
        logger.info("OpenHAB Users Authentication Provider initialized with file: {}", this.usersFilePath);
    }

    @Override
    public Optional<AIAuthenticationContext> authenticate(Map<String, String> credentials) {
        if (!enabled) {
            logger.debug("OpenHAB users authentication provider is disabled");
            return Optional.empty();
        }

        String username = credentials.get("username");
        String password = credentials.get("password");

        if (username == null || password == null) {
            logger.debug("Missing username or password in credentials");
            return Optional.empty();
        }

        try {
            if (validateUser(username, password)) {
                logger.info("User authenticated successfully: {}", username);

                // Create authentication context
                Map<String, String> contextCredentials = new HashMap<>();
                contextCredentials.put("username", username);
                contextCredentials.put("provider", PROVIDER_NAME);

                Set<String> permissions = getDefaultPermissions(username);

                AIAuthenticationContext context = new AIAuthenticationContext(username, AUTH_SCHEME, contextCredentials,
                        permissions, Instant.now(), Instant.now().plusSeconds(3600), // 1 hour expiration
                        UUID.randomUUID().toString());

                return Optional.of(context);
            } else {
                logger.warn("Authentication failed for user: {}", username);
                return Optional.empty();
            }
        } catch (Exception e) {
            logger.error("Error during authentication for user: {}", username, e);
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
            logger.debug("Authentication context expired for user: {}", context.getPrincipalId());
            return false;
        }

        // Check if user still exists in users.properties
        String username = context.getPrincipalId();
        try {
            return userExists(username);
        } catch (Exception e) {
            logger.error("Error validating context for user: {}", username, e);
            return false;
        }
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
                Instant.now().plusSeconds(3600), // 1 hour expiration
                UUID.randomUUID().toString());

        logger.debug("Refreshed authentication context for user: {}", context.getPrincipalId());
        return Optional.of(newContext);
    }

    @Override
    public String[] getSupportedSchemes() {
        return new String[] { AUTH_SCHEME, "Basic" };
    }

    @Override
    public boolean supportsScheme(String scheme) {
        return AUTH_SCHEME.equals(scheme) || "Basic".equals(scheme);
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
     * Validate a user against the users.properties file.
     * 
     * @param username Username to validate
     * @param password Password to validate
     * @return true if user is valid
     */
    private boolean validateUser(String username, String password) {
        Path usersFile = Paths.get(usersFilePath);

        if (!Files.exists(usersFile)) {
            logger.warn("Users file does not exist: {}", usersFilePath);
            return false;
        }

        try (Stream<String> lines = Files.lines(usersFile)) {
            return lines.filter(line -> !line.trim().isEmpty() && !line.startsWith("#")).anyMatch(line -> {
                String[] parts = line.split("=", 2);
                if (parts.length == 2) {
                    String fileUsername = parts[0].trim();
                    String filePassword = parts[1].trim();

                    if (fileUsername.equals(username)) {
                        // Simple password comparison (in production, use proper hashing)
                        return filePassword.equals(password);
                    }
                }
                return false;
            });
        } catch (IOException e) {
            logger.error("Error reading users file: {}", usersFilePath, e);
            return false;
        }
    }

    /**
     * Check if a user exists in the users.properties file.
     * 
     * @param username Username to check
     * @return true if user exists
     */
    private boolean userExists(String username) {
        Path usersFile = Paths.get(usersFilePath);

        if (!Files.exists(usersFile)) {
            return false;
        }

        try (Stream<String> lines = Files.lines(usersFile)) {
            return lines.filter(line -> !line.trim().isEmpty() && !line.startsWith("#")).anyMatch(line -> {
                String[] parts = line.split("=", 2);
                return parts.length == 2 && parts[0].trim().equals(username);
            });
        } catch (IOException e) {
            logger.error("Error reading users file: {}", usersFilePath, e);
            return false;
        }
    }

    /**
     * Get default permissions for a user.
     * 
     * @param username Username
     * @return Set of default permissions
     */
    private Set<String> getDefaultPermissions(String username) {
        // For now, all users in users.properties get basic permissions
        // This can be enhanced with role-based permissions later
        return Set.of("mcp:connect", "mcp:tools", "mcp:read", "mcp:write");
    }
}
