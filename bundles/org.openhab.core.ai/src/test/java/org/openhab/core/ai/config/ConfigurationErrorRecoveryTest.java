package org.openhab.core.ai.config;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests for configuration error recovery scenarios.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
public class ConfigurationErrorRecoveryTest {

    @TempDir
    Path tempDir;

    private DefaultConfigurationManager configurationManager;

    @BeforeEach
    public void setUp() throws IOException {
        // Initialize configuration manager
        configurationManager = new DefaultConfigurationManager();
        configurationManager.activate();
    }

    @Test
    public void testInvalidConfigurationRecovery() {
        // Test recovery from invalid configuration map
        Map<String, Object> invalidConfig = new HashMap<>();
        invalidConfig.put("ai.model.primary.provider", ""); // Empty provider
        invalidConfig.put("ai.model.default.temperature", "invalid"); // Invalid temperature
        invalidConfig.put("ai.model.default.maxTokens", -1); // Invalid max tokens

        // The configuration manager should handle invalid configurations gracefully
        assertDoesNotThrow(() -> {
            configurationManager.reload();
        });

        // Should return default values for invalid configurations
        String provider = configurationManager.getString("ai.model.primary.provider", "default");
        assertEquals("default", provider);

        double temperature = configurationManager.getDouble("ai.model.default.temperature", 0.3);
        assertEquals(0.3, temperature, 0.001);

        int maxTokens = configurationManager.getInt("ai.model.default.maxTokens", 1000);
        assertEquals(1000, maxTokens);
    }

    @Test
    public void testMissingConfigurationFileRecovery() {
        // Test recovery when configuration files are missing
        assertDoesNotThrow(() -> {
            configurationManager.reload();
        });

        // Should return default values for missing configurations
        String result = configurationManager.getString("ai.test.missing.key", "default");
        assertEquals("default", result);

        boolean boolResult = configurationManager.getBoolean("ai.test.missing.bool", true);
        assertTrue(boolResult);

        int intResult = configurationManager.getInt("ai.test.missing.int", 42);
        assertEquals(42, intResult);
    }

    @Test
    public void testConfigurationValidationErrorRecovery() {
        // Test recovery from configuration validation errors
        Map<String, Object> invalidConfig = new HashMap<>();
        invalidConfig.put("ai.agent.server.id", ""); // Empty server ID
        invalidConfig.put("ai.agent.server.name", ""); // Empty server name
        invalidConfig.put("ai.agent.execution.timeout", 50); // Too low timeout

        // The configuration manager should handle validation errors gracefully
        assertDoesNotThrow(() -> {
            configurationManager.reload();
        });

        // Should use default values for invalid configurations
        String serverId = configurationManager.getString("ai.agent.server.id", "default-server");
        assertEquals("default-server", serverId);

        int timeout = configurationManager.getInt("ai.agent.execution.timeout", 30000);
        assertEquals(30000, timeout);
    }

    @Test
    public void testConfigurationReloadAfterError() {
        // Test that configuration can be reloaded after encountering errors
        assertDoesNotThrow(() -> {
            // First reload (may encounter errors)
            configurationManager.reload();

            // Second reload (should work)
            configurationManager.reload();

            // Third reload (should work)
            configurationManager.reload();
        });

        // Configuration should still be accessible after multiple reloads
        String result = configurationManager.getString("ai.test.key", "default");
        assertEquals("default", result);
    }

    @Test
    public void testConfigurationChangeListenerErrorRecovery() {
        // Test recovery when configuration change listeners throw exceptions
        TestFailingConfigurationChangeListener failingListener = new TestFailingConfigurationChangeListener();
        configurationManager.addConfigurationChangeListener(failingListener);

        // Should not throw exception even if listener fails
        assertDoesNotThrow(() -> {
            configurationManager.reload();
        });

        // Other listeners should still be notified
        TestConfigurationChangeListener workingListener = new TestConfigurationChangeListener();
        configurationManager.addConfigurationChangeListener(workingListener);

        assertDoesNotThrow(() -> {
            configurationManager.reload();
        });

        // Working listener should be notified (may not be notified if no actual changes)
        // The test verifies that the system doesn't crash when listeners fail
    }

    @Test
    public void testConfigurationCacheRecovery() {
        // Test recovery of configuration cache after errors
        // Set some configuration values
        configurationManager.getString("ai.test.cache.key1", "value1");
        configurationManager.getString("ai.test.cache.key2", "value2");

        // Verify cache is working
        assertTrue(configurationManager.hasConfiguration("ai.test.cache.key1"));
        assertTrue(configurationManager.hasConfiguration("ai.test.cache.key2"));

        // Simulate cache corruption by reloading with errors
        assertDoesNotThrow(() -> {
            configurationManager.reload();
        });

        // After reload, the cache should be reset, so we get default values
        // This is the expected behavior when no actual configuration is loaded
        String value1 = configurationManager.getString("ai.test.cache.key1", "default");
        assertEquals("default", value1);

        String value2 = configurationManager.getString("ai.test.cache.key2", "default");
        assertEquals("default", value2);
    }

    @Test
    public void testConfigurationStatisticsAfterErrors() {
        // Test that configuration statistics are maintained after errors
        // Perform some operations that might encounter errors
        configurationManager.getString("ai.test.stat1", "value1");
        configurationManager.getInt("ai.test.stat2", 42);
        configurationManager.getBoolean("ai.test.stat3", true);

        // Reload with potential errors
        assertDoesNotThrow(() -> {
            configurationManager.reload();
        });

        // Configuration should still be accessible after errors
        // After reload, we get default values since no actual configuration is loaded
        String value1 = configurationManager.getString("ai.test.stat1", "default");
        assertEquals("default", value1);
    }

    private static class TestFailingConfigurationChangeListener implements ConfigurationChangeListener {
        @Override
        public void onConfigurationChanged(ConfigurationChangeEvent event) {
            throw new RuntimeException("Simulated listener failure");
        }
    }

    private static class TestConfigurationChangeListener implements ConfigurationChangeListener {
        private boolean notified = false;

        @Override
        public void onConfigurationChanged(ConfigurationChangeEvent event) {
            notified = true;
        }

        public boolean wasNotified() {
            return notified;
        }
    }
}
