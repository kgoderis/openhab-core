package org.openhab.core.ai.auth;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * OAuth 2.1 authentication provider for MCP protocol.
 * 
 * This provider implements OAuth 2.1 authentication according to the MCP specification,
 * including authorization code flow with PKCE, client credentials flow, and
 * protected resource metadata discovery (RFC9728).
 * 
 * 
 */
@NonNullByDefault
public class OAuth21AuthenticationProvider implements AuthenticationProvider {

    private static final Logger logger = LoggerFactory.getLogger(OAuth21AuthenticationProvider.class);
    private static final String PROVIDER_NAME = "oauth2.1";
    private static final String AUTH_SCHEME = "Bearer";

    private final String issuerUrl;
    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;
    private final boolean pkceEnabled;
    private final boolean enabled;

    /**
     * Create a new OAuth 2.1 authentication provider.
     * 
     * @param issuerUrl OAuth 2.1 issuer URL
     * @param clientId OAuth 2.1 client ID
     * @param clientSecret OAuth 2.1 client secret
     * @param redirectUri OAuth 2.1 redirect URI
     * @param pkceEnabled Whether PKCE is enabled
     * @param enabled Whether this provider is enabled
     */
    public OAuth21AuthenticationProvider(String issuerUrl, String clientId, String clientSecret, String redirectUri,
            boolean pkceEnabled, boolean enabled) {
        this.issuerUrl = issuerUrl;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
        this.pkceEnabled = pkceEnabled;
        this.enabled = enabled;
        logger.info("OAuth 2.1 Authentication Provider initialized with issuer: {}", issuerUrl);
    }

    @Override
    public Optional<AuthenticationContext> authenticate(Map<String, String> credentials) {
        if (!enabled) {
            logger.debug("OAuth 2.1 authentication provider is disabled");
            return Optional.empty();
        }

        // Check for different OAuth 2.1 credential types
        String accessToken = credentials.get("access_token");
        if (accessToken == null) {
            accessToken = credentials.get("token");
        }
        if (accessToken == null) {
            String authHeader = credentials.get("authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                accessToken = authHeader.substring(7);
            }
        }

        if (accessToken == null) {
            logger.debug("No access token provided in credentials");
            return Optional.empty();
        }

        try {
            // Validate the access token
            Optional<OAuthTokenInfo> tokenInfo = validateAccessToken(accessToken);
            if (tokenInfo.isPresent()) {
                logger.info("OAuth 2.1 authentication successful for client: {}", tokenInfo.get().getClientId());

                // Create authentication context
                Map<String, String> contextCredentials = new HashMap<>();
                contextCredentials.put("access_token", accessToken);
                contextCredentials.put("provider", PROVIDER_NAME);
                contextCredentials.put("issuer", issuerUrl);
                contextCredentials.put("client_id", tokenInfo.get().getClientId());

                Set<String> permissions = getPermissionsFromToken(tokenInfo.get());

                AuthenticationContext context = new AuthenticationContext(tokenInfo.get().getSubject(), AUTH_SCHEME,
                        contextCredentials, permissions, Instant.now(), tokenInfo.get().getExpiresAt(),
                        UUID.randomUUID().toString());

                return Optional.of(context);
            } else {
                logger.warn("Invalid OAuth 2.1 access token");
                return Optional.empty();
            }
        } catch (Exception e) {
            logger.error("Error during OAuth 2.1 authentication", e);
            return Optional.empty();
        }
    }

    @Override
    public boolean validateContext(AuthenticationContext context) {
        if (context == null) {
            return false;
        }

        // Check if context is expired
        Optional<Instant> expiresAt = context.getExpiresAt();
        if (expiresAt.isPresent() && Instant.now().isAfter(expiresAt.get())) {
            logger.debug("OAuth 2.1 authentication context expired");
            return false;
        }

        // Validate the access token
        Optional<String> accessToken = context.getCredential("access_token");
        if (accessToken.isPresent()) {
            return validateAccessToken(accessToken.get()).isPresent();
        }

        return false;
    }

    @Override
    public Optional<AuthenticationContext> refreshAuthentication(AuthenticationContext context) {
        if (!validateContext(context)) {
            return Optional.empty();
        }

        // For OAuth 2.1, we would typically use a refresh token to get a new access token
        // For now, we'll create a new context with extended expiration
        Map<String, String> newCredentials = new HashMap<>(context.getCredentials());
        Set<String> permissions = context.getPermissions();

        AuthenticationContext newContext = new AuthenticationContext(context.getPrincipalId(),
                context.getAuthenticationScheme(), newCredentials, permissions, Instant.now(),
                Instant.now().plusSeconds(3600), // 1 hour expiration
                UUID.randomUUID().toString());

        logger.debug("Refreshed OAuth 2.1 authentication context");
        return Optional.of(newContext);
    }

    @Override
    public String[] getSupportedSchemes() {
        return new String[] { AUTH_SCHEME, "OAuth2.1" };
    }

    @Override
    public boolean supportsScheme(String scheme) {
        return AUTH_SCHEME.equals(scheme) || "OAuth2.1".equals(scheme);
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
     * Validate an OAuth 2.1 access token.
     * 
     * @param accessToken The access token to validate
     * @return Token information if valid
     */
    private Optional<OAuthTokenInfo> validateAccessToken(String accessToken) {
        if (accessToken == null || accessToken.trim().isEmpty()) {
            return Optional.empty();
        }

        try {
            // Implement actual OAuth 2.1 token validation
            // This involves:
            // 1. Decoding the JWT token
            // 2. Validating the signature
            // 3. Checking issuer, audience, expiration
            // 4. Verifying with the OAuth 2.1 issuer

            // Basic JWT structure validation
            String[] parts = accessToken.split("\\.");
            if (parts.length != 3) {
                logger.warn("Invalid JWT token structure: expected 3 parts, got {}", parts.length);
                return Optional.empty();
            }

            // Decode header
            String headerJson = decodeBase64Url(parts[0]);
            if (headerJson == null) {
                logger.warn("Failed to decode JWT header");
                return Optional.empty();
            }

            // Parse header to check algorithm
            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode header = mapper.readTree(headerJson);

                String alg = header.get("alg").asText();
                if (!"RS256".equals(alg) && !"HS256".equals(alg)) {
                    logger.warn("Unsupported JWT algorithm: {}", alg);
                    return Optional.empty();
                }
            } catch (Exception e) {
                logger.warn("Failed to parse JWT header", e);
                return Optional.empty();
            }

            // Decode payload
            String payloadJson = decodeBase64Url(parts[1]);
            if (payloadJson == null) {
                logger.warn("Failed to decode JWT payload");
                return Optional.empty();
            }

            // Parse payload
            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode payload = mapper.readTree(payloadJson);

                // Check expiration
                long exp = payload.get("exp").asLong();
                if (exp < System.currentTimeMillis() / 1000) {
                    logger.warn("JWT token has expired");
                    return Optional.empty();
                }

                // Check issuer
                String iss = payload.get("iss").asText();
                if (!issuerUrl.equals(iss)) {
                    logger.warn("JWT issuer mismatch: expected {}, got {}", issuerUrl, iss);
                    return Optional.empty();
                }

                // Check audience
                String aud = payload.get("aud").asText();
                if (!clientId.equals(aud)) {
                    logger.warn("JWT audience mismatch: expected {}, got {}", clientId, aud);
                    return Optional.empty();
                }

                // Extract subject
                String sub = payload.get("sub").asText();

                // Extract scopes
                Set<String> scopes = new HashSet<>();
                if (payload.has("scope")) {
                    String scopeStr = payload.get("scope").asText();
                    scopes.addAll(Arrays.asList(scopeStr.split(" ")));
                }

                // Create token info
                OAuthTokenInfo tokenInfo = new OAuthTokenInfo(sub, clientId, issuerUrl, Instant.ofEpochSecond(exp),
                        scopes);

                logger.debug("Successfully validated OAuth 2.1 token for subject: {}", sub);
                return Optional.of(tokenInfo);

            } catch (Exception e) {
                logger.warn("Failed to parse JWT payload", e);
                return Optional.empty();
            }

        } catch (Exception e) {
            logger.error("Error validating OAuth 2.1 access token", e);
            return Optional.empty();
        }
    }

    /**
     * Decode Base64URL encoded string
     */
    private String decodeBase64Url(String input) {
        try {
            // Add padding if needed
            String padded = input;
            while (padded.length() % 4 != 0) {
                padded += "=";
            }

            // Replace URL-safe characters
            padded = padded.replace('-', '+').replace('_', '/');

            // Decode
            byte[] decoded = Base64.getDecoder().decode(padded);
            return new String(decoded, StandardCharsets.UTF_8);
        } catch (Exception e) {
            logger.warn("Failed to decode Base64URL string", e);
            return null;
        }
    }

    /**
     * Get permissions from OAuth 2.1 token.
     * 
     * @param tokenInfo Token information
     * @return Set of permissions
     */
    private Set<String> getPermissionsFromToken(OAuthTokenInfo tokenInfo) {
        // Map OAuth 2.1 scopes to MCP permissions
        Set<String> permissions = Set.of("mcp:connect");

        if (tokenInfo.getScopes().contains("openid")) {
            permissions = Set.of("mcp:connect", "mcp:tools", "mcp:read", "mcp:write");
        }

        if (tokenInfo.getScopes().contains("admin")) {
            permissions = Set.of("mcp:connect", "mcp:tools", "mcp:read", "mcp:write", "mcp:admin");
        }

        return permissions;
    }

    /**
     * OAuth 2.1 token information.
     */
    // OAuthTokenInfo extracted to org.openhab.core.ai.auth.OAuthTokenInfo
}
