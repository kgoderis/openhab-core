package org.openhab.core.ai.common.configuration;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for AgentModelConfiguration.
 * 
 * @author Karel Goderis - Initial Contribution
 */
class AgentModelConfigurationTest {

    private AgentModelConfiguration.Builder builder;

    @BeforeEach
    void setUp() {
        builder = AgentModelConfiguration.builder();
    }

    @Test
    void testBasicBuilder() {
        AgentModelConfiguration config = builder.withId("test-config").withAgentId("test-agent")
                .withPreferredModel("gpt-4").withFallbackModel("gpt-3.5-turbo").build();

        assertEquals("test-config", config.getId());
        assertEquals("test-agent", config.getAgentId());
        assertEquals("gpt-4", config.getPreferredModel());
        assertEquals("gpt-3.5-turbo", config.getFallbackModel());
        assertTrue(config.isEnabled());
    }

    @Test
    void testModelParameters() {
        AgentModelConfiguration config = builder.withId("test-config").withAgentId("test-agent").withTemperature(0.8)
                .withMaxTokens(2000).withTimeout(Duration.ofMinutes(5)).build();

        assertEquals(0.8, config.getTemperature());
        assertEquals(2000, config.getMaxTokens());
        assertEquals(Duration.ofMinutes(5), config.getTimeout());
    }

    @Test
    void testRetrySettings() {
        AgentModelConfiguration config = builder.withId("test-config").withAgentId("test-agent").withMaxRetries(5)
                .withRetryDelay(Duration.ofSeconds(10)).build();

        assertEquals(5, config.getMaxRetries());
        assertEquals(Duration.ofSeconds(10), config.getRetryDelay());
    }

    @Test
    void testCachingSettings() {
        AgentModelConfiguration config = builder.withId("test-config").withAgentId("test-agent")
                .withEnableCaching(false).withCacheExpiration(Duration.ofHours(2)).withMaxCacheSize(500).build();

        assertFalse(config.isEnableCaching());
        assertEquals(Duration.ofHours(2), config.getCacheExpiration());
        assertEquals(500, config.getMaxCacheSize());
    }

    @Test
    void testOptimizationSettings() {
        Map<String, Object> optimizationSettings = new HashMap<>();
        optimizationSettings.put("batch_size", 32);
        optimizationSettings.put("learning_rate", 0.001);

        AgentModelConfiguration config = builder.withId("test-config").withAgentId("test-agent")
                .withEnableOptimization(true).withOptimizationSettings(optimizationSettings).build();

        assertTrue(config.isEnableOptimization());
        Map<String, Object> resultSettings = config.getOptimizationSettings();
        assertEquals(32, resultSettings.get("batch_size"));
        assertEquals(0.001, resultSettings.get("learning_rate"));
    }

    @Test
    void testSecuritySettings() {
        Map<String, Object> securitySettings = new HashMap<>();
        securitySettings.put("encryption", "AES-256");
        securitySettings.put("auth_required", true);

        AgentModelConfiguration config = builder.withId("test-config").withAgentId("test-agent")
                .withEnableSecurity(true).withSecuritySettings(securitySettings).build();

        assertTrue(config.isEnableSecurity());
        Map<String, Object> resultSettings = config.getSecuritySettings();
        assertEquals("AES-256", resultSettings.get("encryption"));
        assertEquals(true, resultSettings.get("auth_required"));
    }

    @Test
    void testMonitoringSettings() {
        Map<String, Object> monitoringSettings = new HashMap<>();
        monitoringSettings.put("log_level", "DEBUG");
        monitoringSettings.put("metrics_enabled", true);

        AgentModelConfiguration config = builder.withId("test-config").withAgentId("test-agent")
                .withEnableMonitoring(true).withMonitoringSettings(monitoringSettings).build();

        assertTrue(config.isEnableMonitoring());
        Map<String, Object> resultSettings = config.getMonitoringSettings();
        assertEquals("DEBUG", resultSettings.get("log_level"));
        assertEquals(true, resultSettings.get("metrics_enabled"));
    }

    @Test
    void testPromptTemplates() {
        Map<String, String> promptTemplates = new HashMap<>();
        promptTemplates.put("greeting", "Hello, I am {agent_name}");
        promptTemplates.put("farewell", "Goodbye from {agent_name}");

        AgentModelConfiguration config = builder.withId("test-config").withAgentId("test-agent")
                .withPromptTemplates(promptTemplates).build();

        Map<String, String> resultTemplates = config.getPromptTemplates();
        assertEquals("Hello, I am {agent_name}", resultTemplates.get("greeting"));
        assertEquals("Goodbye from {agent_name}", resultTemplates.get("farewell"));
    }

    @Test
    void testToBuilder() {
        AgentModelConfiguration original = builder.withId("test-config").withAgentId("test-agent")
                .withPreferredModel("gpt-4").withTemperature(0.7).build();

        AgentModelConfiguration copy = original.toBuilder().withTemperature(0.9).build();

        assertEquals("test-config", copy.getId());
        assertEquals("test-agent", copy.getAgentId());
        assertEquals("gpt-4", copy.getPreferredModel());
        assertEquals(0.9, copy.getTemperature());
    }

    @Test
    void testValidation() {
        // Test blank ID
        assertThrows(IllegalArgumentException.class, () -> {
            builder.withId("").withAgentId("test-agent").build();
        });

        // Test blank agent ID
        assertThrows(IllegalArgumentException.class, () -> {
            builder.withId("test-config").withAgentId("").build();
        });

        // Test invalid temperature
        assertThrows(IllegalArgumentException.class, () -> {
            builder.withId("test-config").withAgentId("test-agent").withTemperature(-0.1).build();
        });

        assertThrows(IllegalArgumentException.class, () -> {
            builder.withId("test-config").withAgentId("test-agent").withTemperature(2.1).build();
        });

        // Test invalid max tokens
        assertThrows(IllegalArgumentException.class, () -> {
            builder.withId("test-config").withAgentId("test-agent").withMaxTokens(0).build();
        });

        // Test negative max retries
        assertThrows(IllegalArgumentException.class, () -> {
            builder.withId("test-config").withAgentId("test-agent").withMaxRetries(-1).build();
        });

        // Test negative max cache size
        assertThrows(IllegalArgumentException.class, () -> {
            builder.withId("test-config").withAgentId("test-agent").withMaxCacheSize(-1).build();
        });
    }

    @Test
    void testEqualsAndHashCode() {
        AgentModelConfiguration config1 = builder.withId("test-config").withAgentId("test-agent")
                .withPreferredModel("gpt-4").build();

        AgentModelConfiguration config2 = builder.withId("test-config").withAgentId("test-agent")
                .withPreferredModel("gpt-4").build();

        assertEquals(config1, config2);
        assertEquals(config1.hashCode(), config2.hashCode());
    }

    @Test
    void testToString() {
        AgentModelConfiguration config = builder.withId("test-config").withAgentId("test-agent")
                .withPreferredModel("gpt-4").withFallbackModel("gpt-3.5-turbo").withTemperature(0.7).withMaxTokens(1000)
                .build();

        String result = config.toString();
        assertTrue(result.contains("test-config"));
        assertTrue(result.contains("test-agent"));
        assertTrue(result.contains("gpt-4"));
        assertTrue(result.contains("gpt-3.5-turbo"));
        assertTrue(result.contains("0.70"));
        assertTrue(result.contains("1000"));
        assertTrue(result.contains("true"));
    }
}
