package org.openhab.core.ai.config;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.openhab.core.ai.config.common.ProtocolConfiguration;

/**
 * Unit tests for {@link ProtocolConfiguration}.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
class ProtocolConfigurationTest {

    @Test
    void testBasicConfiguration() {
        ProtocolConfiguration config = ProtocolConfiguration.builder("mcp").withEnabled(true)
                .withEndpoint("http://localhost:8080").withTimeoutSeconds(60).withRetryAttempts(3).build();

        assertEquals("mcp", config.getId());
        assertEquals("mcp", config.getProtocolName());
        assertEquals("1.0.0", config.getVersion());
        assertTrue(config.isEnabled());
        assertEquals("http://localhost:8080", config.getEndpoint());
        assertEquals(60, config.getTimeoutSeconds());
        assertEquals(3, config.getRetryAttempts());
    }

    @Test
    void testAuthenticationConfiguration() {
        Map<String, String> authConfig = Map.of("apiKey", "test-key", "token", "test-token");

        ProtocolConfiguration config = ProtocolConfiguration.builder("a2a").withAuthenticationConfig(authConfig)
                .build();

        assertEquals(authConfig, config.getAuthenticationConfig());
        assertEquals("test-key", config.getAuthenticationValue("apiKey").orElse(null));
        assertEquals("test-token", config.getAuthenticationValue("token").orElse(null));
        assertTrue(config.getAuthenticationValue("nonexistent").isEmpty());
    }

    @Test
    void testProtocolSpecificConfiguration() {
        Map<String, Object> protocolConfig = Map.of("maxTokens", 1000, "temperature", 0.7);

        ProtocolConfiguration config = ProtocolConfiguration.builder("mcp").withProtocolSpecificConfig(protocolConfig)
                .build();

        assertEquals(protocolConfig, config.getProtocolSpecificConfig());
        assertEquals(1000, config.getProtocolValue("maxTokens", Integer.class).orElse(null));
        assertEquals(0.7, config.getProtocolValue("temperature", Double.class).orElse(null));
        assertTrue(config.getProtocolValue("nonexistent", String.class).isEmpty());
    }

    @Test
    void testCustomOptionsIntegration() {
        Map<String, String> authConfig = Map.of("apiKey", "test-key");
        Map<String, Object> protocolConfig = Map.of("maxTokens", 1000);

        ProtocolConfiguration config = ProtocolConfiguration.builder("mcp").withAuthenticationConfig(authConfig)
                .withProtocolSpecificConfig(protocolConfig).build();

        // Custom options should contain both auth and protocol configs
        assertTrue(config.hasCustomOption("apiKey"));
        assertTrue(config.hasCustomOption("maxTokens"));
        assertEquals("test-key", config.getCustomOption("apiKey"));
        assertEquals(1000, config.getCustomOption("maxTokens"));
    }

    @Test
    void testValidation() {
        ProtocolConfiguration.Builder builder = ProtocolConfiguration.builder("mcp");

        // Test valid configuration - build should succeed
        ProtocolConfiguration validConfig = builder.build();
        assertNotNull(validConfig);

        // Test invalid timeout - should throw exception
        assertThrows(IllegalArgumentException.class, () -> {
            builder.withTimeoutSeconds(0).build();
        });

        // Test invalid retry attempts - should throw exception
        assertThrows(IllegalArgumentException.class, () -> {
            builder.withTimeoutSeconds(30).withRetryAttempts(-1).build();
        });
    }

    @Test
    void testEquality() {
        ProtocolConfiguration config1 = ProtocolConfiguration.builder("mcp").withEnabled(true)
                .withEndpoint("http://localhost:8080").build();

        ProtocolConfiguration config2 = ProtocolConfiguration.builder("mcp").withEnabled(true)
                .withEndpoint("http://localhost:8080").build();

        assertEquals(config1, config2);
        assertEquals(config1.hashCode(), config2.hashCode());
    }

    @Test
    void testToString() {
        ProtocolConfiguration config = ProtocolConfiguration.builder("mcp").withEnabled(true)
                .withEndpoint("http://localhost:8080").build();

        String toString = config.toString();
        assertTrue(toString.contains("mcp"));
        assertTrue(toString.contains("true"));
        assertTrue(toString.contains("http://localhost:8080"));
    }
}
