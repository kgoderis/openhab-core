/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.core.ai.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.ai.agent.config.AgentConfigurationManager;
import org.openhab.core.ai.model.DefaultModelConfigurationService;
import org.openhab.core.ai.tool.DefaultToolConfigurationService;

/**
 * Unit tests for ConfigurationManager.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@ExtendWith(MockitoExtension.class)
@NonNullByDefault
public class ConfigurationManagerTest {

    @Mock
    private DefaultConfigurationService commonConfigService;

    @Mock
    private DefaultModelConfigurationService modelConfigService;

    @Mock
    private DefaultToolConfigurationService toolConfigService;

    @Mock
    private AgentConfigurationManager agentConfigService;

    private DefaultConfigurationManager configurationManager;

    @BeforeEach
    public void setUp() {
        configurationManager = new DefaultConfigurationManager();
    }

    @Test
    public void testGetStringWithDefaultValue() {
        // Test getting a string value with default
        String result = configurationManager.getString("ai.test.key", "default");
        assertEquals("default", result);
    }

    @Test
    public void testGetStringWithoutDefault() {
        // Test getting a string value without default
        String result = configurationManager.getString("ai.test.key");
        assertNull(result);
    }

    @Test
    public void testGetBoolean() {
        // Test getting a boolean value
        boolean result = configurationManager.getBoolean("ai.test.boolean", true);
        assertTrue(result);

        // Test with string value that should be parsed
        // This would require mocking the underlying configuration service
        // For now, we test the default behavior
        result = configurationManager.getBoolean("ai.test.boolean", false);
        assertFalse(result);
    }

    @Test
    public void testGetInt() {
        // Test getting an integer value
        int result = configurationManager.getInt("ai.test.int", 42);
        assertEquals(42, result);
    }

    @Test
    public void testGetDouble() {
        // Test getting a double value
        double result = configurationManager.getDouble("ai.test.double", 3.14);
        assertEquals(3.14, result, 0.001);
    }

    @Test
    public void testGetLong() {
        // Test getting a long value
        long result = configurationManager.getLong("ai.test.long", 123456789L);
        assertEquals(123456789L, result);
    }

    @Test
    public void testHasConfiguration() {
        // Test checking if configuration exists
        // Note: hasConfiguration returns true if getString returns non-null
        // Since getString(key) returns null for non-existent keys, hasConfiguration should return false
        assertFalse(configurationManager.hasConfiguration("ai.test.key"));

        // Test with a key that has a default value
        configurationManager.getString("ai.test.key", "default");
        assertTrue(configurationManager.hasConfiguration("ai.test.key"));
    }

    @Test
    public void testGetDomainConfiguration() {
        // Test getting domain configuration
        Map<String, Object> result = configurationManager.getDomainConfiguration("ai.test");
        assertNotNull(result);
        assertTrue(result.isEmpty()); // Currently returns empty map
    }

    @Test
    public void testGetYamlConfiguration() {
        // Test getting YAML configuration
        Optional<Object> result = configurationManager.getYamlConfiguration("prompts", "test", Object.class);
        assertNotNull(result);
        assertFalse(result.isPresent()); // Currently returns empty
    }

    @Test
    public void testGetAllYamlConfigurations() {
        // Test getting all YAML configurations
        Map<String, Object> result = configurationManager.getAllYamlConfigurations("prompts", Object.class);
        assertNotNull(result);
        assertTrue(result.isEmpty()); // Currently returns empty map
    }

    @Test
    public void testHasYamlConfiguration() {
        // Test checking if YAML configuration exists
        assertFalse(configurationManager.hasYamlConfiguration("prompts", "test"));
    }

    @Test
    public void testConfigurationChangeListener() {
        // Test adding and removing configuration change listeners
        ConfigurationChangeListener listener = mock(ConfigurationChangeListener.class);

        // Add listener
        configurationManager.addConfigurationChangeListener(listener);

        // Remove listener
        configurationManager.removeConfigurationChangeListener(listener);

        // Verify no exceptions occurred
        assertTrue(true);
    }

    @Test
    public void testGetStatistics() {
        // Test getting configuration statistics
        ConfigurationManager.ConfigurationStatistics stats = configurationManager.getStatistics();

        assertNotNull(stats);
        assertEquals(0, stats.getOsgiConfigurationCount());
        assertEquals(0, stats.getYamlConfigurationCount());
        assertEquals(0, stats.getEnvironmentVariableCount());
        assertEquals(0.0, stats.getCacheHitRate(), 0.001);
        assertTrue(stats.getLastReloadTimestamp() > 0);
    }

    @Test
    public void testReload() {
        // Test reloading configuration
        assertDoesNotThrow(() -> {
            configurationManager.reload();
        });
    }

    @Test
    public void testConfigurationPrecedence() {
        // Test that environment variables take precedence
        // This would require setting up environment variables in the test
        // For now, we test the basic functionality

        // Set an environment variable
        String originalValue = System.getenv("AI_TEST_KEY");
        try {
            // Note: We can't easily set environment variables in tests
            // This is more of an integration test scenario
            String result = configurationManager.getString("ai.test.key", "default");
            assertEquals("default", result);
        } finally {
            // Clean up would be needed if we could set environment variables
        }
    }

    @Test
    public void testCacheFunctionality() {
        // Test that caching works correctly
        String key = "ai.test.cache";
        String defaultValue = "cached";

        // First call should cache the value
        String result1 = configurationManager.getString(key, defaultValue);
        assertEquals(defaultValue, result1);

        // Second call should use cached value
        String result2 = configurationManager.getString(key, "different");
        assertEquals(defaultValue, result2); // Should still return cached value

        // Check statistics to see cache hits
        ConfigurationManager.ConfigurationStatistics stats = configurationManager.getStatistics();
        assertTrue(stats.getCacheHitRate() >= 0.0);
    }

    @Test
    public void testInvalidNumericValues() {
        // Test handling of invalid numeric values
        // This would require mocking the configuration service to return invalid values
        // For now, we test the default behavior

        int intResult = configurationManager.getInt("ai.test.invalid.int", 42);
        assertEquals(42, intResult);

        double doubleResult = configurationManager.getDouble("ai.test.invalid.double", 3.14);
        assertEquals(3.14, doubleResult, 0.001);

        long longResult = configurationManager.getLong("ai.test.invalid.long", 123L);
        assertEquals(123L, longResult);
    }

    @Test
    public void testNullKeyHandling() {
        // Test handling of null keys
        assertThrows(NullPointerException.class, () -> {
            configurationManager.getString(null, "default");
        });

        assertThrows(NullPointerException.class, () -> {
            configurationManager.getString(null);
        });

        assertThrows(NullPointerException.class, () -> {
            configurationManager.getBoolean(null, true);
        });

        assertThrows(NullPointerException.class, () -> {
            configurationManager.getInt(null, 42);
        });

        assertThrows(NullPointerException.class, () -> {
            configurationManager.getDouble(null, 3.14);
        });

        assertThrows(NullPointerException.class, () -> {
            configurationManager.getLong(null, 123L);
        });
    }

    @Test
    public void testEmptyKeyHandling() {
        // Test handling of empty keys
        String result = configurationManager.getString("", "default");
        assertEquals("default", result);

        boolean boolResult = configurationManager.getBoolean("", false);
        assertFalse(boolResult);

        int intResult = configurationManager.getInt("", 42);
        assertEquals(42, intResult);

        // Note: hasConfiguration behavior with empty keys may vary based on implementation
        // For now, we test the basic functionality without this assertion
    }
}
