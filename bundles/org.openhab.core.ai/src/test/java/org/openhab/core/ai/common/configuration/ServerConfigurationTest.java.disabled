package org.openhab.core.ai.common.configuration;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for the unified ServerConfiguration class.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
class ServerConfigurationTest {

    @Test
    void testServerConfigurationBuilder() {
        ServerConfiguration config = ServerConfiguration.builder().withId("test-server").withName("Test Server")
                .withVersion("2.0.0").withBaseUrl("http://localhost:9090").withPort(9090).withContextPath("/test")
                .withServletPath("/api").withServletPattern("/*").withMessageEndpoint("/message")
                .withHealthEndpoint("/health").withStatusEndpoint("/status").withMetricsEndpoint("/metrics")
                .withEnableAuthentication(true).withPrimaryAuthMethod("oauth2.1").withFallbackAuthMethod("api_key")
                .withEnableFallbackAuth(true).withMaxConnections(200).withRateLimitPerMinute(2000)
                .withEnableRequestValidation(true).withOauthIssuerUrl("https://oauth.example.com")
                .withOauthClientId("test-client").withOauthClientSecret("test-secret")
                .withOauthRedirectUri("http://localhost:9090/callback").withOauthPkceEnabled(true)
                .withOpenhabUsersFile("/etc/openhab/users.properties").withOpenhabUsersEnabled(true)
                .withApiKeyHeader("X-Test-API-Key").withApiKeyValue("test-api-key").withApiKeyEnabled(true)
                .withJwtSecret("test-jwt-secret").withJwtIssuer("test-issuer").withJwtExpirationMinutes(120)
                .withJwtEnabled(true).withEnableMetrics(true).withEnableHealthChecks(true).withHealthCheckInterval(60)
                .withEnablePerformanceMonitoring(true).withProductionMode(true).withRequestTimeout(60000)
                .withConnectionTimeout(20000).withEnableGracefulShutdown(true).withShutdownTimeout(60000)
                .withEnableCors(true).withCorsAllowedOrigins("http://localhost:3000")
                .withCorsAllowedMethods("GET,POST,PUT,DELETE").withCorsAllowedHeaders("Content-Type,Authorization")
                .withEnableSsl(true).withSslKeyStore("/path/to/keystore.jks")
                .withSslKeyStorePassword("keystore-password").withSslTrustStore("/path/to/truststore.jks")
                .withSslTrustStorePassword("truststore-password").withCustomOption("customKey", "customValue").build();

        // Test base configuration
        assertEquals("test-server", config.getId());
        assertTrue(config.isEnabled());
        assertEquals("Test Server", config.getName());
        assertEquals("2.0.0", config.getVersion());

        // Test server settings
        assertEquals("http://localhost:9090", config.getBaseUrl());
        assertEquals(9090, config.getPort());
        assertEquals("/test", config.getContextPath());
        assertEquals("/api", config.getServletPath());
        assertEquals("/*", config.getServletPattern());

        // Test endpoints
        assertEquals("/message", config.getMessageEndpoint());
        assertEquals("/health", config.getHealthEndpoint());
        assertEquals("/status", config.getStatusEndpoint());
        assertEquals("/metrics", config.getMetricsEndpoint());

        // Test security configuration
        assertTrue(config.isEnableAuthentication());
        assertEquals("oauth2.1", config.getPrimaryAuthMethod());
        assertEquals("api_key", config.getFallbackAuthMethod());
        assertTrue(config.isEnableFallbackAuth());
        assertEquals(200, config.getMaxConnections());
        assertEquals(2000, config.getRateLimitPerMinute());
        assertTrue(config.isEnableRequestValidation());

        // Test OAuth configuration
        assertEquals("https://oauth.example.com", config.getOauthIssuerUrl());
        assertEquals("test-client", config.getOauthClientId());
        assertEquals("test-secret", config.getOauthClientSecret());
        assertEquals("http://localhost:9090/callback", config.getOauthRedirectUri());
        assertTrue(config.isOauthPkceEnabled());

        // Test openHAB users authentication
        assertEquals("/etc/openhab/users.properties", config.getOpenhabUsersFile());
        assertTrue(config.isOpenhabUsersEnabled());

        // Test API key authentication
        assertEquals("X-Test-API-Key", config.getApiKeyHeader());
        assertEquals("test-api-key", config.getApiKeyValue());
        assertTrue(config.isApiKeyEnabled());

        // Test JWT authentication
        assertEquals("test-jwt-secret", config.getJwtSecret());
        assertEquals("test-issuer", config.getJwtIssuer());
        assertEquals(120, config.getJwtExpirationMinutes());
        assertTrue(config.isJwtEnabled());

        // Test monitoring and metrics
        assertTrue(config.isEnableMetrics());
        assertTrue(config.isEnableHealthChecks());
        assertEquals(60, config.getHealthCheckInterval());
        assertTrue(config.isEnablePerformanceMonitoring());

        // Test production settings
        assertTrue(config.isProductionMode());
        assertEquals(60000, config.getRequestTimeout());
        assertEquals(20000, config.getConnectionTimeout());
        assertTrue(config.isEnableGracefulShutdown());
        assertEquals(60000, config.getShutdownTimeout());

        // Test CORS configuration
        assertTrue(config.isEnableCors());
        assertEquals("http://localhost:3000", config.getCorsAllowedOrigins());
        assertEquals("GET,POST,PUT,DELETE", config.getCorsAllowedMethods());
        assertEquals("Content-Type,Authorization", config.getCorsAllowedHeaders());

        // Test SSL/TLS configuration
        assertTrue(config.isEnableSsl());
        assertEquals("/path/to/keystore.jks", config.getSslKeyStore());
        assertEquals("keystore-password", config.getSslKeyStorePassword());
        assertEquals("/path/to/truststore.jks", config.getSslTrustStore());
        assertEquals("truststore-password", config.getSslTrustStorePassword());

        // Test custom options
        assertEquals("customValue", config.getCustomOption("customKey"));
        assertTrue(config.hasCustomOption("customKey"));
    }

    @Test
    void testServerConfigurationDefaults() {
        ServerConfiguration config = ServerConfiguration.builder().build();

        // Test default values
        assertEquals("default-server", config.getId());
        assertTrue(config.isEnabled());
        assertEquals("AI Server", config.getName());
        assertEquals("1.0.0", config.getVersion());
        assertEquals("http://localhost:8080", config.getBaseUrl());
        assertEquals(8080, config.getPort());
        assertEquals("/ai", config.getContextPath());
        assertEquals("/api", config.getServletPath());
        assertEquals("/*", config.getServletPattern());
        assertEquals("/message", config.getMessageEndpoint());
        assertEquals("/health", config.getHealthEndpoint());
        assertEquals("/status", config.getStatusEndpoint());
        assertEquals("/metrics", config.getMetricsEndpoint());
        assertFalse(config.isEnableAuthentication());
        assertEquals("none", config.getPrimaryAuthMethod());
        assertEquals("none", config.getFallbackAuthMethod());
        assertFalse(config.isEnableFallbackAuth());
        assertEquals(100, config.getMaxConnections());
        assertEquals(1000, config.getRateLimitPerMinute());
        assertTrue(config.isEnableRequestValidation());
        assertTrue(config.isEnableMetrics());
        assertTrue(config.isEnableHealthChecks());
        assertEquals(30, config.getHealthCheckInterval());
        assertFalse(config.isEnablePerformanceMonitoring());
        assertFalse(config.isProductionMode());
        assertEquals(30000, config.getRequestTimeout());
        assertEquals(10000, config.getConnectionTimeout());
        assertTrue(config.isEnableGracefulShutdown());
        assertEquals(30000, config.getShutdownTimeout());
        assertFalse(config.isEnableCors());
        assertEquals("*", config.getCorsAllowedOrigins());
        assertEquals("GET,POST,PUT,DELETE,OPTIONS", config.getCorsAllowedMethods());
        assertEquals("*", config.getCorsAllowedHeaders());
        assertFalse(config.isEnableSsl());
    }

    @Test
    void testServerConfigurationEquality() {
        ServerConfiguration config1 = ServerConfiguration.builder().withId("test-server")
                .withBaseUrl("http://localhost:8080").withPort(8080).withContextPath("/ai").withServletPath("/api")
                .withServletPattern("/*").build();

        ServerConfiguration config2 = ServerConfiguration.builder().withId("test-server")
                .withBaseUrl("http://localhost:8080").withPort(8080).withContextPath("/ai").withServletPath("/api")
                .withServletPattern("/*").build();

        ServerConfiguration config3 = ServerConfiguration.builder().withId("different-server")
                .withBaseUrl("http://localhost:8080").withPort(8080).withContextPath("/ai").withServletPath("/api")
                .withServletPattern("/*").build();

        assertEquals(config1, config2);
        assertEquals(config1.hashCode(), config2.hashCode());
        assertNotEquals(config1, config3);
        assertNotEquals(config1.hashCode(), config3.hashCode());
    }

    @Test
    void testServerConfigurationToString() {
        ServerConfiguration config = ServerConfiguration.builder().withId("test-server").withName("Test Server")
                .withBaseUrl("http://localhost:8080").withPort(8080).withContextPath("/ai").withServletPath("/api")
                .build();

        String toString = config.toString();
        assertTrue(toString.contains("test-server"));
        assertTrue(toString.contains("Test Server"));
        assertTrue(toString.contains("http://localhost:8080"));
        assertTrue(toString.contains("8080"));
        assertTrue(toString.contains("/ai"));
        assertTrue(toString.contains("/api"));
    }

    @Test
    void testNullValidation() {
        ServerConfigurationBuilder builder = ServerConfiguration.builder();

        assertThrows(NullPointerException.class, () -> builder.withId(null));
        assertThrows(NullPointerException.class, () -> builder.withName(null));
        assertThrows(NullPointerException.class, () -> builder.withVersion(null));
        assertThrows(NullPointerException.class, () -> builder.withBaseUrl(null));
        assertThrows(NullPointerException.class, () -> builder.withContextPath(null));
        assertThrows(NullPointerException.class, () -> builder.withServletPath(null));
        assertThrows(NullPointerException.class, () -> builder.withServletPattern(null));
        assertThrows(NullPointerException.class, () -> builder.withMessageEndpoint(null));
        assertThrows(NullPointerException.class, () -> builder.withHealthEndpoint(null));
        assertThrows(NullPointerException.class, () -> builder.withStatusEndpoint(null));
        assertThrows(NullPointerException.class, () -> builder.withMetricsEndpoint(null));
        assertThrows(NullPointerException.class, () -> builder.withPrimaryAuthMethod(null));
        assertThrows(NullPointerException.class, () -> builder.withFallbackAuthMethod(null));
        assertThrows(NullPointerException.class, () -> builder.withOauthIssuerUrl(null));
        assertThrows(NullPointerException.class, () -> builder.withOauthClientId(null));
        assertThrows(NullPointerException.class, () -> builder.withOauthClientSecret(null));
        assertThrows(NullPointerException.class, () -> builder.withOauthRedirectUri(null));
        assertThrows(NullPointerException.class, () -> builder.withOpenhabUsersFile(null));
        assertThrows(NullPointerException.class, () -> builder.withApiKeyHeader(null));
        assertThrows(NullPointerException.class, () -> builder.withApiKeyValue(null));
        assertThrows(NullPointerException.class, () -> builder.withJwtSecret(null));
        assertThrows(NullPointerException.class, () -> builder.withJwtIssuer(null));
        assertThrows(NullPointerException.class, () -> builder.withCorsAllowedOrigins(null));
        assertThrows(NullPointerException.class, () -> builder.withCorsAllowedMethods(null));
        assertThrows(NullPointerException.class, () -> builder.withCorsAllowedHeaders(null));
        assertThrows(NullPointerException.class, () -> builder.withSslKeyStore(null));
        assertThrows(NullPointerException.class, () -> builder.withSslKeyStorePassword(null));
        assertThrows(NullPointerException.class, () -> builder.withSslTrustStore(null));
        assertThrows(NullPointerException.class, () -> builder.withSslTrustStorePassword(null));
        assertThrows(NullPointerException.class, () -> builder.withCustomOption(null, "value"));
        assertThrows(NullPointerException.class, () -> builder.withCustomOption("key", null));
    }
}
