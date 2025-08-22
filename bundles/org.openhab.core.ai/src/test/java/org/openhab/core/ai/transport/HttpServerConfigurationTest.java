package org.openhab.core.ai.transport;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for HttpServerConfiguration builder integration.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
class HttpServerConfigurationTest {

    @Test
    void testBuilderCreation() {
        HttpServerConfiguration config = HttpServerConfiguration.builder().withBaseUrl("http://localhost:9090")
                .withPort(9090).withContextPath("/api").withMcpServletPath("/mcp").withMcpServletPattern("/mcp/*")
                .withA2aServletPath("/a2a").withA2aServletPattern("/a2a/*").withEnableAuthentication(true)
                .withPrimaryAuthMethod("oauth2.1").withFallbackAuthMethod("api_key").withEnableFallbackAuth(true)
                .withMaxConnections(200).withRateLimitPerMinute(2000).withEnableRequestValidation(true)
                .withOauthIssuerUrl("https://auth.example.com").withOauthClientId("client123")
                .withOauthClientSecret("secret123").withOauthRedirectUri("http://localhost:9090/callback")
                .withOauthPkceEnabled(true).withOpenhabUsersFile("/etc/openhab/users.json")
                .withOpenhabUsersEnabled(true).withApiKeyHeader("X-API-Key").withApiKeyValue("test-key")
                .withApiKeyEnabled(true).withJwtSecret("jwt-secret").withJwtIssuer("openhab-server")
                .withJwtExpirationMinutes(120).withJwtEnabled(true).withEnableMetrics(true).withEnableHealthChecks(true)
                .withHealthCheckInterval(60000).withEnablePerformanceMonitoring(true)
                .withMetricsEndpoint("/admin/metrics").withProductionMode(true).withRequestTimeout(60000)
                .withConnectionTimeout(20000).withEnableGracefulShutdown(true).withShutdownTimeout(60000)
                .withEnableCors(true).withCorsAllowedOrigins("https://example.com")
                .withCorsAllowedMethods("GET, POST, PUT, DELETE").withCorsAllowedHeaders("Content-Type, Authorization")
                .withEnableSsl(true).withSslKeyStore("/path/to/keystore.jks").withSslKeyStorePassword("keystore-pass")
                .withSslTrustStore("/path/to/truststore.jks").withSslTrustStorePassword("truststore-pass")
                .withServerOption("custom.option", "custom.value").build();

        // Test all getters
        assertEquals("http://localhost:9090", config.getBaseUrl());
        assertEquals(9090, config.getPort());
        assertEquals("/api", config.getContextPath());
        assertEquals("/mcp", config.getMcpServletPath());
        assertEquals("/mcp/*", config.getMcpServletPattern());
        assertEquals("/a2a", config.getA2aServletPath());
        assertEquals("/a2a/*", config.getA2aServletPattern());
        assertTrue(config.isEnableAuthentication());
        assertEquals("oauth2.1", config.getPrimaryAuthMethod());
        assertEquals("api_key", config.getFallbackAuthMethod());
        assertTrue(config.isEnableFallbackAuth());
        assertEquals(200, config.getMaxConnections());
        assertEquals(2000, config.getRateLimitPerMinute());
        assertTrue(config.isEnableRequestValidation());
        assertEquals("https://auth.example.com", config.getOauthIssuerUrl());
        assertEquals("client123", config.getOauthClientId());
        assertEquals("secret123", config.getOauthClientSecret());
        assertEquals("http://localhost:9090/callback", config.getOauthRedirectUri());
        assertTrue(config.isOauthPkceEnabled());
        assertEquals("/etc/openhab/users.json", config.getOpenhabUsersFile());
        assertTrue(config.isOpenhabUsersEnabled());
        assertEquals("X-API-Key", config.getApiKeyHeader());
        assertEquals("test-key", config.getApiKeyValue());
        assertTrue(config.isApiKeyEnabled());
        assertEquals("jwt-secret", config.getJwtSecret());
        assertEquals("openhab-server", config.getJwtIssuer());
        assertEquals(120, config.getJwtExpirationMinutes());
        assertTrue(config.isJwtEnabled());
        assertTrue(config.isEnableMetrics());
        assertTrue(config.isEnableHealthChecks());
        assertEquals(60000, config.getHealthCheckInterval());
        assertTrue(config.isEnablePerformanceMonitoring());
        assertEquals("/admin/metrics", config.getMetricsEndpoint());
        assertTrue(config.isProductionMode());
        assertEquals(60000, config.getRequestTimeout());
        assertEquals(20000, config.getConnectionTimeout());
        assertTrue(config.isEnableGracefulShutdown());
        assertEquals(60000, config.getShutdownTimeout());
        assertTrue(config.isEnableCors());
        assertEquals("https://example.com", config.getCorsAllowedOrigins());
        assertEquals("GET, POST, PUT, DELETE", config.getCorsAllowedMethods());
        assertEquals("Content-Type, Authorization", config.getCorsAllowedHeaders());
        assertTrue(config.isEnableSsl());
        assertEquals("/path/to/keystore.jks", config.getSslKeyStore());
        assertEquals("keystore-pass", config.getSslKeyStorePassword());
        assertEquals("/path/to/truststore.jks", config.getSslTrustStore());
        assertEquals("truststore-pass", config.getSslTrustStorePassword());

        // Test server options
        Map<String, Object> serverOptions = config.getServerOptions();
        assertEquals("custom.value", serverOptions.get("custom.option"));
    }

    @Test
    void testToBuilder() {
        HttpServerConfiguration original = HttpServerConfiguration.builder().withBaseUrl("http://localhost:8080")
                .withPort(8080).withContextPath("/").withMcpServletPath("/mcp").withMcpServletPattern("/mcp/*")
                .withA2aServletPath("/a2a").withA2aServletPattern("/a2a/*").withEnableAuthentication(false)
                .withPrimaryAuthMethod("oauth2.1").withFallbackAuthMethod("openhab_users").withEnableFallbackAuth(true)
                .withMaxConnections(100).withRateLimitPerMinute(1000).withEnableRequestValidation(true)
                .withOauthIssuerUrl("").withOauthClientId("").withOauthClientSecret("").withOauthRedirectUri("")
                .withOauthPkceEnabled(true).withOpenhabUsersFile("").withOpenhabUsersEnabled(true)
                .withApiKeyHeader("X-API-Key").withApiKeyValue("").withApiKeyEnabled(false).withJwtSecret("")
                .withJwtIssuer("openhab-http-server").withJwtExpirationMinutes(60).withJwtEnabled(false)
                .withEnableMetrics(true).withEnableHealthChecks(true).withHealthCheckInterval(30000)
                .withEnablePerformanceMonitoring(true).withMetricsEndpoint("/metrics").withProductionMode(false)
                .withRequestTimeout(30000).withConnectionTimeout(10000).withEnableGracefulShutdown(true)
                .withShutdownTimeout(30000).withEnableCors(true).withCorsAllowedOrigins("*")
                .withCorsAllowedMethods("GET, POST, PUT, DELETE, OPTIONS")
                .withCorsAllowedHeaders("Content-Type, Authorization, X-API-Key").withEnableSsl(false)
                .withSslKeyStore("").withSslKeyStorePassword("").withSslTrustStore("").withSslTrustStorePassword("")
                .build();

        // Create modified configuration using toBuilder
        HttpServerConfiguration modified = original.toBuilder().withPort(9090).withEnableAuthentication(true)
                .withApiKeyEnabled(true).build();

        // Verify original is unchanged
        assertEquals(8080, original.getPort());
        assertFalse(original.isEnableAuthentication());
        assertFalse(original.isApiKeyEnabled());

        // Verify modified has new values
        assertEquals(9090, modified.getPort());
        assertTrue(modified.isEnableAuthentication());
        assertTrue(modified.isApiKeyEnabled());

        // Verify other values are copied
        assertEquals(original.getBaseUrl(), modified.getBaseUrl());
        assertEquals(original.getContextPath(), modified.getContextPath());
        assertEquals(original.getMcpServletPath(), modified.getMcpServletPath());
        assertEquals(original.getA2aServletPath(), modified.getA2aServletPath());
    }

    @Test
    void testBuilderWithNullValues() {
        HttpServerConfiguration config = HttpServerConfiguration.builder().withBaseUrl("http://localhost:8080")
                .withPort(8080).withContextPath("/").withMcpServletPath("/mcp").withMcpServletPattern("/mcp/*")
                .withA2aServletPath("/a2a").withA2aServletPattern("/a2a/*").withEnableAuthentication(false)
                .withPrimaryAuthMethod("oauth2.1").withFallbackAuthMethod("openhab_users").withEnableFallbackAuth(true)
                .withMaxConnections(100).withRateLimitPerMinute(1000).withEnableRequestValidation(true)
                .withOauthIssuerUrl(null).withOauthClientId(null).withOauthClientSecret(null).withOauthRedirectUri(null)
                .withOauthPkceEnabled(true).withOpenhabUsersFile(null).withOpenhabUsersEnabled(true)
                .withApiKeyHeader("X-API-Key").withApiKeyValue(null).withApiKeyEnabled(false).withJwtSecret(null)
                .withJwtIssuer("openhab-http-server").withJwtExpirationMinutes(60).withJwtEnabled(false)
                .withEnableMetrics(true).withEnableHealthChecks(true).withHealthCheckInterval(30000)
                .withEnablePerformanceMonitoring(true).withMetricsEndpoint("/metrics").withProductionMode(false)
                .withRequestTimeout(30000).withConnectionTimeout(10000).withEnableGracefulShutdown(true)
                .withShutdownTimeout(30000).withEnableCors(true).withCorsAllowedOrigins("*")
                .withCorsAllowedMethods("GET, POST, PUT, DELETE, OPTIONS")
                .withCorsAllowedHeaders("Content-Type, Authorization, X-API-Key").withEnableSsl(false)
                .withSslKeyStore(null).withSslKeyStorePassword(null).withSslTrustStore(null)
                .withSslTrustStorePassword(null).build();

        // Verify null values are converted to empty strings
        assertEquals("", config.getOauthIssuerUrl());
        assertEquals("", config.getOauthClientId());
        assertEquals("", config.getOauthClientSecret());
        assertEquals("", config.getOauthRedirectUri());
        assertEquals("", config.getOpenhabUsersFile());
        assertEquals("", config.getApiKeyValue());
        assertEquals("", config.getJwtSecret());
        assertEquals("", config.getSslKeyStore());
        assertEquals("", config.getSslKeyStorePassword());
        assertEquals("", config.getSslTrustStore());
        assertEquals("", config.getSslTrustStorePassword());
    }

    @Test
    void testBuilderValidation() {
        // Test blank baseUrl
        assertThrows(IllegalArgumentException.class, () -> {
            HttpServerConfiguration.builder().withBaseUrl("").withPort(8080).withContextPath("/")
                    .withMcpServletPath("/mcp").withMcpServletPattern("/mcp/*").withA2aServletPath("/a2a")
                    .withA2aServletPattern("/a2a/*").withEnableAuthentication(false).withPrimaryAuthMethod("oauth2.1")
                    .withFallbackAuthMethod("openhab_users").withEnableFallbackAuth(true).withMaxConnections(100)
                    .withRateLimitPerMinute(1000).withEnableRequestValidation(true).withOauthIssuerUrl("")
                    .withOauthClientId("").withOauthClientSecret("").withOauthRedirectUri("").withOauthPkceEnabled(true)
                    .withOpenhabUsersFile("").withOpenhabUsersEnabled(true).withApiKeyHeader("X-API-Key")
                    .withApiKeyValue("").withApiKeyEnabled(false).withJwtSecret("").withJwtIssuer("openhab-http-server")
                    .withJwtExpirationMinutes(60).withJwtEnabled(false).withEnableMetrics(true)
                    .withEnableHealthChecks(true).withHealthCheckInterval(30000).withEnablePerformanceMonitoring(true)
                    .withMetricsEndpoint("/metrics").withProductionMode(false).withRequestTimeout(30000)
                    .withConnectionTimeout(10000).withEnableGracefulShutdown(true).withShutdownTimeout(30000)
                    .withEnableCors(true).withCorsAllowedOrigins("*")
                    .withCorsAllowedMethods("GET, POST, PUT, DELETE, OPTIONS")
                    .withCorsAllowedHeaders("Content-Type, Authorization, X-API-Key").withEnableSsl(false)
                    .withSslKeyStore("").withSslKeyStorePassword("").withSslTrustStore("").withSslTrustStorePassword("")
                    .build();
        });

        // Test invalid port
        assertThrows(IllegalArgumentException.class, () -> {
            HttpServerConfiguration.builder().withBaseUrl("http://localhost:8080").withPort(0).withContextPath("/")
                    .withMcpServletPath("/mcp").withMcpServletPattern("/mcp/*").withA2aServletPath("/a2a")
                    .withA2aServletPattern("/a2a/*").withEnableAuthentication(false).withPrimaryAuthMethod("oauth2.1")
                    .withFallbackAuthMethod("openhab_users").withEnableFallbackAuth(true).withMaxConnections(100)
                    .withRateLimitPerMinute(1000).withEnableRequestValidation(true).withOauthIssuerUrl("")
                    .withOauthClientId("").withOauthClientSecret("").withOauthRedirectUri("").withOauthPkceEnabled(true)
                    .withOpenhabUsersFile("").withOpenhabUsersEnabled(true).withApiKeyHeader("X-API-Key")
                    .withApiKeyValue("").withApiKeyEnabled(false).withJwtSecret("").withJwtIssuer("openhab-http-server")
                    .withJwtExpirationMinutes(60).withJwtEnabled(false).withEnableMetrics(true)
                    .withEnableHealthChecks(true).withHealthCheckInterval(30000).withEnablePerformanceMonitoring(true)
                    .withMetricsEndpoint("/metrics").withProductionMode(false).withRequestTimeout(30000)
                    .withConnectionTimeout(10000).withEnableGracefulShutdown(true).withShutdownTimeout(30000)
                    .withEnableCors(true).withCorsAllowedOrigins("*")
                    .withCorsAllowedMethods("GET, POST, PUT, DELETE, OPTIONS")
                    .withCorsAllowedHeaders("Content-Type, Authorization, X-API-Key").withEnableSsl(false)
                    .withSslKeyStore("").withSslKeyStorePassword("").withSslTrustStore("").withSslTrustStorePassword("")
                    .build();
        });

        // Test negative maxConnections
        assertThrows(IllegalArgumentException.class, () -> {
            HttpServerConfiguration.builder().withBaseUrl("http://localhost:8080").withPort(8080).withContextPath("/")
                    .withMcpServletPath("/mcp").withMcpServletPattern("/mcp/*").withA2aServletPath("/a2a")
                    .withA2aServletPattern("/a2a/*").withEnableAuthentication(false).withPrimaryAuthMethod("oauth2.1")
                    .withFallbackAuthMethod("openhab_users").withEnableFallbackAuth(true).withMaxConnections(-1)
                    .withRateLimitPerMinute(1000).withEnableRequestValidation(true).withOauthIssuerUrl("")
                    .withOauthClientId("").withOauthClientSecret("").withOauthRedirectUri("").withOauthPkceEnabled(true)
                    .withOpenhabUsersFile("").withOpenhabUsersEnabled(true).withApiKeyHeader("X-API-Key")
                    .withApiKeyValue("").withApiKeyEnabled(false).withJwtSecret("").withJwtIssuer("openhab-http-server")
                    .withJwtExpirationMinutes(60).withJwtEnabled(false).withEnableMetrics(true)
                    .withEnableHealthChecks(true).withHealthCheckInterval(30000).withEnablePerformanceMonitoring(true)
                    .withMetricsEndpoint("/metrics").withProductionMode(false).withRequestTimeout(30000)
                    .withConnectionTimeout(10000).withEnableGracefulShutdown(true).withShutdownTimeout(30000)
                    .withEnableCors(true).withCorsAllowedOrigins("*")
                    .withCorsAllowedMethods("GET, POST, PUT, DELETE, OPTIONS")
                    .withCorsAllowedHeaders("Content-Type, Authorization, X-API-Key").withEnableSsl(false)
                    .withSslKeyStore("").withSslKeyStorePassword("").withSslTrustStore("").withSslTrustStorePassword("")
                    .build();
        });
    }

    @Test
    void testServerOptionsImmutability() {
        Map<String, Object> originalOptions = new HashMap<>();
        originalOptions.put("test.key", "test.value");

        HttpServerConfiguration config = HttpServerConfiguration.builder().withBaseUrl("http://localhost:8080")
                .withPort(8080).withContextPath("/").withMcpServletPath("/mcp").withMcpServletPattern("/mcp/*")
                .withA2aServletPath("/a2a").withA2aServletPattern("/a2a/*").withEnableAuthentication(false)
                .withPrimaryAuthMethod("oauth2.1").withFallbackAuthMethod("openhab_users").withEnableFallbackAuth(true)
                .withMaxConnections(100).withRateLimitPerMinute(1000).withEnableRequestValidation(true)
                .withOauthIssuerUrl("").withOauthClientId("").withOauthClientSecret("").withOauthRedirectUri("")
                .withOauthPkceEnabled(true).withOpenhabUsersFile("").withOpenhabUsersEnabled(true)
                .withApiKeyHeader("X-API-Key").withApiKeyValue("").withApiKeyEnabled(false).withJwtSecret("")
                .withJwtIssuer("openhab-http-server").withJwtExpirationMinutes(60).withJwtEnabled(false)
                .withEnableMetrics(true).withEnableHealthChecks(true).withHealthCheckInterval(30000)
                .withEnablePerformanceMonitoring(true).withMetricsEndpoint("/metrics").withProductionMode(false)
                .withRequestTimeout(30000).withConnectionTimeout(10000).withEnableGracefulShutdown(true)
                .withShutdownTimeout(30000).withEnableCors(true).withCorsAllowedOrigins("*")
                .withCorsAllowedMethods("GET, POST, PUT, DELETE, OPTIONS")
                .withCorsAllowedHeaders("Content-Type, Authorization, X-API-Key").withEnableSsl(false)
                .withSslKeyStore("").withSslKeyStorePassword("").withSslTrustStore("").withSslTrustStorePassword("")
                .withServerOptions(originalOptions).build();

        Map<String, Object> returnedOptions = config.getServerOptions();

        // Verify the returned map is a copy
        assertNotSame(originalOptions, returnedOptions);

        // Verify the content is the same
        assertEquals("test.value", returnedOptions.get("test.key"));

        // Verify modifying the returned map doesn't affect the original
        returnedOptions.put("new.key", "new.value");
        Map<String, Object> secondCall = config.getServerOptions();
        assertFalse(secondCall.containsKey("new.key"));
    }

    @Test
    void testEqualsAndHashCode() {
        HttpServerConfiguration config1 = HttpServerConfiguration.builder().withBaseUrl("http://localhost:8080")
                .withPort(8080).withContextPath("/").withMcpServletPath("/mcp").withMcpServletPattern("/mcp/*")
                .withA2aServletPath("/a2a").withA2aServletPattern("/a2a/*").withEnableAuthentication(false)
                .withPrimaryAuthMethod("oauth2.1").withFallbackAuthMethod("openhab_users").withEnableFallbackAuth(true)
                .withMaxConnections(100).withRateLimitPerMinute(1000).withEnableRequestValidation(true)
                .withOauthIssuerUrl("").withOauthClientId("").withOauthClientSecret("").withOauthRedirectUri("")
                .withOauthPkceEnabled(true).withOpenhabUsersFile("").withOpenhabUsersEnabled(true)
                .withApiKeyHeader("X-API-Key").withApiKeyValue("").withApiKeyEnabled(false).withJwtSecret("")
                .withJwtIssuer("openhab-http-server").withJwtExpirationMinutes(60).withJwtEnabled(false)
                .withEnableMetrics(true).withEnableHealthChecks(true).withHealthCheckInterval(30000)
                .withEnablePerformanceMonitoring(true).withMetricsEndpoint("/metrics").withProductionMode(false)
                .withRequestTimeout(30000).withConnectionTimeout(10000).withEnableGracefulShutdown(true)
                .withShutdownTimeout(30000).withEnableCors(true).withCorsAllowedOrigins("*")
                .withCorsAllowedMethods("GET, POST, PUT, DELETE, OPTIONS")
                .withCorsAllowedHeaders("Content-Type, Authorization, X-API-Key").withEnableSsl(false)
                .withSslKeyStore("").withSslKeyStorePassword("").withSslTrustStore("").withSslTrustStorePassword("")
                .build();

        HttpServerConfiguration config2 = HttpServerConfiguration.builder().withBaseUrl("http://localhost:8080")
                .withPort(8080).withContextPath("/").withMcpServletPath("/mcp").withMcpServletPattern("/mcp/*")
                .withA2aServletPath("/a2a").withA2aServletPattern("/a2a/*").withEnableAuthentication(false)
                .withPrimaryAuthMethod("oauth2.1").withFallbackAuthMethod("openhab_users").withEnableFallbackAuth(true)
                .withMaxConnections(100).withRateLimitPerMinute(1000).withEnableRequestValidation(true)
                .withOauthIssuerUrl("").withOauthClientId("").withOauthClientSecret("").withOauthRedirectUri("")
                .withOauthPkceEnabled(true).withOpenhabUsersFile("").withOpenhabUsersEnabled(true)
                .withApiKeyHeader("X-API-Key").withApiKeyValue("").withApiKeyEnabled(false).withJwtSecret("")
                .withJwtIssuer("openhab-http-server").withJwtExpirationMinutes(60).withJwtEnabled(false)
                .withEnableMetrics(true).withEnableHealthChecks(true).withHealthCheckInterval(30000)
                .withEnablePerformanceMonitoring(true).withMetricsEndpoint("/metrics").withProductionMode(false)
                .withRequestTimeout(30000).withConnectionTimeout(10000).withEnableGracefulShutdown(true)
                .withShutdownTimeout(30000).withEnableCors(true).withCorsAllowedOrigins("*")
                .withCorsAllowedMethods("GET, POST, PUT, DELETE, OPTIONS")
                .withCorsAllowedHeaders("Content-Type, Authorization, X-API-Key").withEnableSsl(false)
                .withSslKeyStore("").withSslKeyStorePassword("").withSslTrustStore("").withSslTrustStorePassword("")
                .build();

        // Test equality
        assertEquals(config1, config2);
        assertEquals(config1.hashCode(), config2.hashCode());

        // Test inequality
        HttpServerConfiguration config3 = config1.toBuilder().withPort(9090).build();
        assertNotEquals(config1, config3);
        assertNotEquals(config1.hashCode(), config3.hashCode());
    }
}
