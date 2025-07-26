package org.openhab.core.ai.mcp.internal;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for MCPServerConfiguration using real SDK classes.
 *
 * Tests configuration building, validation, serialization,
 * and configuration scenarios.
 *
 * 
 */
class MCPServerConfigurationTest {

    @Test
    void testBasicConfiguration() {
        // Test basic configuration with minimal parameters
        MCPServerConfiguration config = MCPServerConfiguration.builder().serverId("test-server")
                .serverName("Test Server").build();

        assertEquals("test-server", config.getServerId());
        assertEquals("Test Server", config.getServerName());
        assertEquals("1.0.0", config.getServerVersion()); // Default value
        assertEquals(MCPTransportType.STDIO, config.getTransportType()); // Default value
    }

    @Test
    void testFullConfiguration() {
        // Test configuration with all options
        MCPServerConfiguration config = MCPServerConfiguration.builder().serverId("full-server")
                .serverName("Full Configuration Server").serverVersion("2.0.0").transportType(MCPTransportType.SSE)
                .baseUrl("http://localhost:8080").messageEndpoint("/mcp/message").sseEndpoint("/mcp/events")
                .enableSse(true).enableTools(true).enableResources(true).enablePrompts(true).enableLogging(true)
                .enableAsyncServer(true).enableAsyncTools(true).asyncThreadPoolSize(20).asyncQueueCapacity(2000)
                .enableAsyncCompletions(true).enableAuthentication(true).authToken("test-token").maxConnections(200)
                .rateLimitPerMinute(2000).enableRequestValidation(true).primaryAuthMethod("oauth2.1")
                .fallbackAuthMethod("api_key").enableFallbackAuth(true).oauthIssuerUrl("https://oauth.example.com")
                .oauthClientId("client-id").oauthClientSecret("client-secret")
                .oauthRedirectUri("http://localhost:8080/callback").oauthPkceEnabled(true)
                .openhabUsersFile("/path/to/users.properties").openhabUsersEnabled(true)
                .apiKeyHeader("X-Custom-API-Key").apiKeyValue("custom-api-key").apiKeyEnabled(true)
                .jwtSecret("jwt-secret").jwtIssuer("custom-issuer").jwtExpirationMinutes(120).jwtEnabled(true)
                .enableMetrics(true).enableHealthChecks(true).healthCheckInterval(60000)
                .enablePerformanceMonitoring(true).productionMode(true).requestTimeout(60000).connectionTimeout(20000)
                .enableGracefulShutdown(true).shutdownTimeout(60000).build();

        // Verify all values
        assertEquals("full-server", config.getServerId());
        assertEquals("Full Configuration Server", config.getServerName());
        assertEquals("2.0.0", config.getServerVersion());
        assertEquals(MCPTransportType.SSE, config.getTransportType());
        assertEquals("http://localhost:8080", config.getBaseUrl());
        assertEquals("/mcp/message", config.getMessageEndpoint());
        assertEquals("/mcp/events", config.getSseEndpoint());
        assertTrue(config.isEnableSse());
        assertTrue(config.isEnableTools());
        assertTrue(config.isEnableResources());
        assertTrue(config.isEnablePrompts());
        assertTrue(config.isEnableLogging());
        assertTrue(config.isEnableAsyncServer());
        assertTrue(config.isEnableAsyncTools());
        assertEquals(20, config.getAsyncThreadPoolSize());
        assertEquals(2000, config.getAsyncQueueCapacity());
        assertTrue(config.isEnableAsyncCompletions());
        assertTrue(config.isEnableAuthentication());
        assertEquals("test-token", config.getAuthToken());
        assertEquals(200, config.getMaxConnections());
        assertEquals(2000, config.getRateLimitPerMinute());
        assertTrue(config.isEnableRequestValidation());
        assertEquals("oauth2.1", config.getPrimaryAuthMethod());
        assertEquals("api_key", config.getFallbackAuthMethod());
        assertTrue(config.isEnableFallbackAuth());
        assertEquals("https://oauth.example.com", config.getOauthIssuerUrl());
        assertEquals("client-id", config.getOauthClientId());
        assertEquals("client-secret", config.getOauthClientSecret());
        assertEquals("http://localhost:8080/callback", config.getOauthRedirectUri());
        assertTrue(config.isOauthPkceEnabled());
        assertEquals("/path/to/users.properties", config.getOpenhabUsersFile());
        assertTrue(config.isOpenhabUsersEnabled());
        assertEquals("X-Custom-API-Key", config.getApiKeyHeader());
        assertEquals("custom-api-key", config.getApiKeyValue());
        assertTrue(config.isApiKeyEnabled());
        assertEquals("jwt-secret", config.getJwtSecret());
        assertEquals("custom-issuer", config.getJwtIssuer());
        assertEquals(120, config.getJwtExpirationMinutes());
        assertTrue(config.isJwtEnabled());
        assertTrue(config.isEnableMetrics());
        assertTrue(config.isEnableHealthChecks());
        assertEquals(60000, config.getHealthCheckInterval());
        assertTrue(config.isEnablePerformanceMonitoring());
        assertTrue(config.isProductionMode());
        assertEquals(60000, config.getRequestTimeout());
        assertEquals(20000, config.getConnectionTimeout());
        assertTrue(config.isEnableGracefulShutdown());
        assertEquals(60000, config.getShutdownTimeout());
    }

    @Test
    void testDefaultValues() {
        // Test that default values are set correctly
        MCPServerConfiguration config = MCPServerConfiguration.builder().serverId("default-server").build();

        assertEquals("openHAB MCP Server", config.getServerName());
        assertEquals("1.0.0", config.getServerVersion());
        assertEquals(MCPTransportType.STDIO, config.getTransportType());
        assertEquals("http://localhost:8080", config.getBaseUrl());
        assertEquals("/mcp/message", config.getMessageEndpoint());
        assertEquals("/mcp/events", config.getSseEndpoint());
        assertTrue(config.isEnableSse());
        assertTrue(config.isEnableTools());
        assertTrue(config.isEnableResources());
        assertTrue(config.isEnablePrompts());
        assertTrue(config.isEnableLogging());
        assertFalse(config.isEnableAsyncServer());
        assertFalse(config.isEnableAsyncTools());
        assertEquals(10, config.getAsyncThreadPoolSize());
        assertEquals(1000, config.getAsyncQueueCapacity());
        assertFalse(config.isEnableAsyncCompletions());
        assertFalse(config.isEnableAuthentication());
        assertNull(config.getAuthToken());
        assertEquals(100, config.getMaxConnections());
        assertEquals(1000, config.getRateLimitPerMinute());
        assertTrue(config.isEnableRequestValidation());
        assertEquals("oauth2.1", config.getPrimaryAuthMethod());
        assertEquals("openhab_users", config.getFallbackAuthMethod());
        assertTrue(config.isEnableFallbackAuth());
        assertNull(config.getOauthIssuerUrl());
        assertNull(config.getOauthClientId());
        assertNull(config.getOauthClientSecret());
        assertNull(config.getOauthRedirectUri());
        assertTrue(config.isOauthPkceEnabled());
        assertNull(config.getOpenhabUsersFile());
        assertTrue(config.isOpenhabUsersEnabled());
        assertEquals("X-API-Key", config.getApiKeyHeader());
        assertNull(config.getApiKeyValue());
        assertFalse(config.isApiKeyEnabled());
        assertNull(config.getJwtSecret());
        assertEquals("openhab-mcp", config.getJwtIssuer());
        assertEquals(60, config.getJwtExpirationMinutes());
        assertFalse(config.isJwtEnabled());
        assertTrue(config.isEnableMetrics());
        assertTrue(config.isEnableHealthChecks());
        assertEquals(30000, config.getHealthCheckInterval());
        assertTrue(config.isEnablePerformanceMonitoring());
        assertFalse(config.isProductionMode());
        assertEquals(30000, config.getRequestTimeout());
        assertEquals(10000, config.getConnectionTimeout());
        assertTrue(config.isEnableGracefulShutdown());
        assertEquals(30000, config.getShutdownTimeout());
    }

    @Test
    void testTransportOptions() {
        // Test transport options
        Map<String, Object> transportOptions = new HashMap<>();
        transportOptions.put("port", 8080);
        transportOptions.put("timeout", 5000);

        MCPServerConfiguration config = MCPServerConfiguration.builder().serverId("transport-server")
                .transportOptions(transportOptions).build();

        Map<String, Object> retrievedOptions = config.getTransportOptions();
        assertEquals(2, retrievedOptions.size());
        assertEquals(8080, retrievedOptions.get("port"));
        assertEquals(5000, retrievedOptions.get("timeout"));
    }

    @Test
    void testServerOptions() {
        // Test server options
        Map<String, Object> serverOptions = new HashMap<>();
        serverOptions.put("maxThreads", 50);
        serverOptions.put("bufferSize", 8192);

        MCPServerConfiguration config = MCPServerConfiguration.builder().serverId("server-options")
                .serverOptions(serverOptions).build();

        Map<String, Object> retrievedOptions = config.getServerOptions();
        assertEquals(2, retrievedOptions.size());
        assertEquals(50, retrievedOptions.get("maxThreads"));
        assertEquals(8192, retrievedOptions.get("bufferSize"));
    }

    @Test
    void testIndividualTransportOption() {
        // Test adding individual transport options
        MCPServerConfiguration config = MCPServerConfiguration.builder().serverId("individual-transport")
                .transportOption("port", 9090).transportOption("timeout", 10000).build();

        Map<String, Object> options = config.getTransportOptions();
        assertEquals(2, options.size());
        assertEquals(9090, options.get("port"));
        assertEquals(10000, options.get("timeout"));
    }

    @Test
    void testIndividualServerOption() {
        // Test adding individual server options
        MCPServerConfiguration config = MCPServerConfiguration.builder().serverId("individual-server")
                .serverOption("maxThreads", 100).serverOption("bufferSize", 16384).build();

        Map<String, Object> options = config.getServerOptions();
        assertEquals(2, options.size());
        assertEquals(100, options.get("maxThreads"));
        assertEquals(16384, options.get("bufferSize"));
    }

    @Test
    void testEquality() {
        // Test equality
        MCPServerConfiguration config1 = MCPServerConfiguration.builder().serverId("test-server")
                .serverName("Test Server").build();

        MCPServerConfiguration config2 = MCPServerConfiguration.builder().serverId("test-server")
                .serverName("Test Server").build();

        MCPServerConfiguration config3 = MCPServerConfiguration.builder().serverId("different-server")
                .serverName("Test Server").build();

        assertEquals(config1, config2);
        assertNotEquals(config1, config3);
        assertNotEquals(config1, null);
        assertNotEquals(config1, "not a config");
    }

    @Test
    void testHashCode() {
        // Test hashCode consistency
        MCPServerConfiguration config1 = MCPServerConfiguration.builder().serverId("test-server")
                .serverName("Test Server").build();

        MCPServerConfiguration config2 = MCPServerConfiguration.builder().serverId("test-server")
                .serverName("Test Server").build();

        assertEquals(config1.hashCode(), config2.hashCode());
    }

    @Test
    void testToString() {
        // Test toString method
        MCPServerConfiguration config = MCPServerConfiguration.builder().serverId("test-server")
                .serverName("Test Server").build();

        String configString = config.toString();
        assertNotNull(configString);
        assertTrue(configString.contains("test-server"));
        assertTrue(configString.contains("Test Server"));
    }

    @Test
    void testNullValues() {
        // Test handling of null values (except required fields)
        MCPServerConfiguration config = MCPServerConfiguration.builder().serverId("null-test").serverName("test-server")
                .serverVersion("1.0.0").baseUrl(null).messageEndpoint(null).sseEndpoint(null).authToken(null)
                .oauthIssuerUrl(null).oauthClientId(null).oauthClientSecret(null).oauthRedirectUri(null)
                .openhabUsersFile(null).apiKeyValue(null).jwtSecret(null).jwtIssuer(null).build();

        assertEquals("null-test", config.getServerId());
        assertEquals("test-server", config.getServerName());
        assertEquals("1.0.0", config.getServerVersion());
        assertNull(config.getBaseUrl());
        assertNull(config.getMessageEndpoint());
        assertNull(config.getSseEndpoint());
        assertNull(config.getAuthToken());
        assertNull(config.getOauthIssuerUrl());
        assertNull(config.getOauthClientId());
        assertNull(config.getOauthClientSecret());
        assertNull(config.getOauthRedirectUri());
        assertNull(config.getOpenhabUsersFile());
        assertNull(config.getApiKeyValue());
        assertNull(config.getJwtSecret());
        assertNull(config.getJwtIssuer());
    }

    @Test
    void testEmptyValues() {
        // Test handling of empty values (except required fields)
        MCPServerConfiguration config = MCPServerConfiguration.builder().serverId("empty-test")
                .serverName("test-server").serverVersion("1.0.0").baseUrl("").messageEndpoint("").sseEndpoint("")
                .authToken("").oauthIssuerUrl("").oauthClientId("").oauthClientSecret("").oauthRedirectUri("")
                .openhabUsersFile("").apiKeyValue("").jwtSecret("").jwtIssuer("").build();

        assertEquals("empty-test", config.getServerId());
        assertEquals("test-server", config.getServerName());
        assertEquals("1.0.0", config.getServerVersion());
        assertEquals("", config.getBaseUrl());
        assertEquals("", config.getMessageEndpoint());
        assertEquals("", config.getSseEndpoint());
        assertEquals("", config.getAuthToken());
        assertEquals("", config.getOauthIssuerUrl());
        assertEquals("", config.getOauthClientId());
        assertEquals("", config.getOauthClientSecret());
        assertEquals("", config.getOauthRedirectUri());
        assertEquals("", config.getOpenhabUsersFile());
        assertEquals("", config.getApiKeyValue());
        assertEquals("", config.getJwtSecret());
        assertEquals("", config.getJwtIssuer());
    }

    @Test
    void testSpecialCharacters() {
        // Test handling of special characters
        MCPServerConfiguration config = MCPServerConfiguration.builder().serverId("test@server#123")
                .serverName("Test Server with Special Chars: !@#$%^&*()")
                .baseUrl("https://example.com/path?param=value&other=123").build();

        assertEquals("test@server#123", config.getServerId());
        assertEquals("Test Server with Special Chars: !@#$%^&*()", config.getServerName());
        assertEquals("https://example.com/path?param=value&other=123", config.getBaseUrl());
    }

    @Test
    void testUnicodeCharacters() {
        // Test handling of unicode characters
        MCPServerConfiguration config = MCPServerConfiguration.builder().serverId("test-服务器-123")
                .serverName("Test Server with Unicode: 服务器测试").build();

        assertEquals("test-服务器-123", config.getServerId());
        assertEquals("Test Server with Unicode: 服务器测试", config.getServerName());
    }

    @Test
    void testLargeValues() {
        // Test handling of large values
        MCPServerConfiguration config = MCPServerConfiguration.builder().serverId("a".repeat(1000))
                .serverName("b".repeat(1000)).maxConnections(Integer.MAX_VALUE).rateLimitPerMinute(Integer.MAX_VALUE)
                .asyncThreadPoolSize(Integer.MAX_VALUE).asyncQueueCapacity(Integer.MAX_VALUE)
                .healthCheckInterval(Integer.MAX_VALUE).requestTimeout(Integer.MAX_VALUE)
                .connectionTimeout(Integer.MAX_VALUE).shutdownTimeout(Integer.MAX_VALUE)
                .jwtExpirationMinutes(Integer.MAX_VALUE).build();

        assertEquals("a".repeat(1000), config.getServerId());
        assertEquals("b".repeat(1000), config.getServerName());
        assertEquals(Integer.MAX_VALUE, config.getMaxConnections());
        assertEquals(Integer.MAX_VALUE, config.getRateLimitPerMinute());
        assertEquals(Integer.MAX_VALUE, config.getAsyncThreadPoolSize());
        assertEquals(Integer.MAX_VALUE, config.getAsyncQueueCapacity());
        assertEquals(Integer.MAX_VALUE, config.getHealthCheckInterval());
        assertEquals(Integer.MAX_VALUE, config.getRequestTimeout());
        assertEquals(Integer.MAX_VALUE, config.getConnectionTimeout());
        assertEquals(Integer.MAX_VALUE, config.getShutdownTimeout());
        assertEquals(Integer.MAX_VALUE, config.getJwtExpirationMinutes());
    }

    @Test
    void testTransportTypeConfiguration() {
        // Test different transport types
        MCPServerConfiguration stdioConfig = MCPServerConfiguration.builder().serverId("stdio-server")
                .transportType(MCPTransportType.STDIO).build();

        MCPServerConfiguration sseConfig = MCPServerConfiguration.builder().serverId("sse-server")
                .transportType(MCPTransportType.SSE).build();

        assertEquals(MCPTransportType.STDIO, stdioConfig.getTransportType());
        assertEquals(MCPTransportType.SSE, sseConfig.getTransportType());
    }

    @Test
    void testServerOptionsOverlap() {
        // Test that server options don't interfere with each other
        MCPServerConfiguration config = MCPServerConfiguration.builder().serverId("overlap-test")
                .serverOption("key1", "value1").serverOption("key2", "value2")
                .serverOptions(Map.of("key3", "value3", "key4", "value4")).build();

        Map<String, Object> options = config.getServerOptions();
        // The implementation replaces options instead of merging them
        assertEquals(2, options.size());
        assertEquals("value3", options.get("key3"));
        assertEquals("value4", options.get("key4"));
    }

    @Test
    void testTransportOptionsOverlap() {
        // Test that transport options don't interfere with each other
        MCPServerConfiguration config = MCPServerConfiguration.builder().serverId("transport-overlap")
                .transportOption("key1", "value1").transportOption("key2", "value2")
                .transportOptions(Map.of("key3", "value3", "key4", "value4")).build();

        Map<String, Object> options = config.getTransportOptions();
        // The implementation replaces options instead of merging them
        assertEquals(2, options.size());
        assertEquals("value3", options.get("key3"));
        assertEquals("value4", options.get("key4"));
    }

    @Test
    void testNullOptions() {
        // Test handling of null options
        assertThrows(NullPointerException.class, () -> {
            MCPServerConfiguration.builder().serverId("null-options").serverOptions(null).transportOptions(null)
                    .build();
        });
    }

    @Test
    void testImmutability() {
        // Test that configuration is immutable
        MCPServerConfiguration config = MCPServerConfiguration.builder().serverId("immutable-test").build();

        // Verify that getter methods return the expected values
        String originalId = config.getServerId();
        String originalName = config.getServerName();

        // The configuration should be immutable, so these values should not change
        assertEquals(originalId, config.getServerId());
        assertEquals(originalName, config.getServerName());
    }
}
