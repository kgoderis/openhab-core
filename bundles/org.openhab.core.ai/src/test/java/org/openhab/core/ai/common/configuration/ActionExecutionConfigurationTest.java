package org.openhab.core.ai.common.configuration;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for ActionExecutionConfiguration and ActionExecutionConfigurationBuilder.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
class ActionExecutionConfigurationTest {

    @Test
    void testBuilderWithDefaultValues() {
        ActionExecutionConfiguration config = ActionExecutionConfiguration.builder().build();

        assertEquals("action-execution", config.getId());
        assertEquals("Action Execution Configuration", config.getName());
        assertEquals("1.0.0", config.getVersion());
        assertTrue(config.isEnabled());
        assertNull(config.getMaxRetryAttempts());
        assertNull(config.getRetryDelay());
        assertNull(config.isEnableCaching());
        assertNull(config.isEnableSecurityValidation());
        assertNull(config.getCacheExpiration());
        assertNull(config.getExecutionTimeout());
        assertNull(config.isEnableAsyncExecution());
        assertNull(config.getMaxConcurrentExecutions());
        assertTrue(config.getCustomOptions().isEmpty());
    }

    @Test
    void testBuilderWithAllValues() {
        ActionExecutionConfiguration config = ActionExecutionConfiguration.builder().withConfigId("test-config")
                .withMaxRetryAttempts(3).withRetryDelay(Duration.ofSeconds(5)).withEnableCaching(true)
                .withEnableSecurityValidation(false).withCacheExpiration(Duration.ofMinutes(30))
                .withExecutionTimeout(Duration.ofMinutes(10)).withEnableAsyncExecution(true)
                .withMaxConcurrentExecutions(5).withCustomSetting("key1", "value1").withCustomSetting("key2", 42)
                .build();

        assertEquals("test-config", config.getId());
        assertEquals("Action Execution Configuration", config.getName());
        assertEquals("1.0.0", config.getVersion());
        assertTrue(config.isEnabled());
        assertEquals(3, config.getMaxRetryAttempts());
        assertEquals(Duration.ofSeconds(5), config.getRetryDelay());
        assertTrue(config.isEnableCaching());
        assertFalse(config.isEnableSecurityValidation());
        assertEquals(Duration.ofMinutes(30), config.getCacheExpiration());
        assertEquals(Duration.ofMinutes(10), config.getExecutionTimeout());
        assertTrue(config.isEnableAsyncExecution());
        assertEquals(5, config.getMaxConcurrentExecutions());
        assertEquals("value1", config.getCustomOptions().get("key1"));
        assertEquals(42, config.getCustomOptions().get("key2"));
    }

    @Test
    void testBuilderWithNullValues() {
        ActionExecutionConfiguration config = ActionExecutionConfiguration.builder().withMaxRetryAttempts(null)
                .withRetryDelay(null).withEnableCaching(null).withEnableSecurityValidation(null)
                .withCacheExpiration(null).withExecutionTimeout(null).withEnableAsyncExecution(null)
                .withMaxConcurrentExecutions(null).build();

        assertNull(config.getMaxRetryAttempts());
        assertNull(config.getRetryDelay());
        assertNull(config.isEnableCaching());
        assertNull(config.isEnableSecurityValidation());
        assertNull(config.getCacheExpiration());
        assertNull(config.getExecutionTimeout());
        assertNull(config.isEnableAsyncExecution());
        assertNull(config.getMaxConcurrentExecutions());
    }

    @Test
    void testValidationErrors() {
        // Test negative maxRetryAttempts
        assertThrows(IllegalArgumentException.class, () -> {
            ActionExecutionConfiguration.builder().withMaxRetryAttempts(-1).build();
        });

        // Test negative retryDelay
        assertThrows(IllegalArgumentException.class, () -> {
            ActionExecutionConfiguration.builder().withRetryDelay(Duration.ofSeconds(-1)).build();
        });

        // Test negative cacheExpiration
        assertThrows(IllegalArgumentException.class, () -> {
            ActionExecutionConfiguration.builder().withCacheExpiration(Duration.ofMinutes(-1)).build();
        });

        // Test negative executionTimeout
        assertThrows(IllegalArgumentException.class, () -> {
            ActionExecutionConfiguration.builder().withExecutionTimeout(Duration.ofMinutes(-1)).build();
        });

        // Test zero maxConcurrentExecutions
        assertThrows(IllegalArgumentException.class, () -> {
            ActionExecutionConfiguration.builder().withMaxConcurrentExecutions(0).build();
        });

        // Test negative maxConcurrentExecutions
        assertThrows(IllegalArgumentException.class, () -> {
            ActionExecutionConfiguration.builder().withMaxConcurrentExecutions(-1).build();
        });

        // Test blank configId
        assertThrows(IllegalArgumentException.class, () -> {
            ActionExecutionConfiguration.builder().withConfigId("").build();
        });
    }

    @Test
    void testEqualsAndHashCode() {
        ActionExecutionConfiguration config1 = ActionExecutionConfiguration.builder().withConfigId("test")
                .withMaxRetryAttempts(3).withRetryDelay(Duration.ofSeconds(5)).build();

        ActionExecutionConfiguration config2 = ActionExecutionConfiguration.builder().withConfigId("test")
                .withMaxRetryAttempts(3).withRetryDelay(Duration.ofSeconds(5)).build();

        ActionExecutionConfiguration config3 = ActionExecutionConfiguration.builder().withConfigId("test")
                .withMaxRetryAttempts(5).withRetryDelay(Duration.ofSeconds(10)).build();

        assertEquals(config1, config2);
        assertEquals(config1.hashCode(), config2.hashCode());
        assertNotEquals(config1, config3);
        assertNotEquals(config1.hashCode(), config3.hashCode());
    }

    @Test
    void testToString() {
        ActionExecutionConfiguration config = ActionExecutionConfiguration.builder().withConfigId("test")
                .withMaxRetryAttempts(3).withRetryDelay(Duration.ofSeconds(5)).withEnableCaching(true).build();

        String toString = config.toString();
        assertTrue(toString.contains("ActionExecutionConfiguration"));
        assertTrue(toString.contains("maxRetryAttempts=3"));
        assertTrue(toString.contains("retryDelay=PT5S"));
        assertTrue(toString.contains("enableCaching=true"));
    }

    @Test
    void testToBuilder() {
        ActionExecutionConfiguration original = ActionExecutionConfiguration.builder().withConfigId("original")
                .withMaxRetryAttempts(3).withRetryDelay(Duration.ofSeconds(5)).withEnableCaching(true).build();

        ActionExecutionConfigurationBuilder builder = new ActionExecutionConfigurationBuilder(original);
        ActionExecutionConfiguration copy = builder.build();

        assertEquals(original, copy);
        assertEquals(original.hashCode(), copy.hashCode());
    }

    @Test
    void testCustomSettings() {
        Map<String, Object> customSettings = Map.of("key1", "value1", "key2", 42);

        ActionExecutionConfiguration config = ActionExecutionConfiguration.builder().withCustomSettings(customSettings)
                .build();

        assertEquals("value1", config.getCustomOptions().get("key1"));
        assertEquals(42, config.getCustomOptions().get("key2"));
        assertEquals(2, config.getCustomOptions().size());
    }

    @Test
    void testReset() {
        ActionExecutionConfigurationBuilder builder = ActionExecutionConfiguration.builder().withConfigId("test")
                .withMaxRetryAttempts(3).withRetryDelay(Duration.ofSeconds(5)).withEnableCaching(true)
                .withCustomSetting("key", "value");

        builder.reset();
        ActionExecutionConfiguration config = builder.build();

        assertEquals("action-execution", config.getId());
        assertNull(config.getMaxRetryAttempts());
        assertNull(config.getRetryDelay());
        assertNull(config.isEnableCaching());
        assertTrue(config.getCustomOptions().isEmpty());
    }
}
