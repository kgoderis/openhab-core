package org.openhab.core.ai.config.common;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for the unified configuration system.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
class ConfigurationTest {

    @Test
    void testConfigurationInterface() {
        // Test that we can create a configuration with all required methods
        Configuration config = new TestConfiguration("test-id", true, "Test Config", "1.0.0", Map.of("key", "value"));

        assertEquals("test-id", config.getId());
        assertTrue(config.isEnabled());
        assertEquals("Test Config", config.getName());
        assertEquals("1.0.0", config.getVersion());
        assertEquals("value", config.getCustomOption("key"));
        assertTrue(config.hasCustomOption("key"));
        assertFalse(config.hasCustomOption("nonexistent"));
        assertNull(config.getCustomOption("nonexistent"));
    }

    @Test
    void testBaseConfiguration() {
        Map<String, Object> options = new HashMap<>();
        options.put("key1", "value1");
        options.put("key2", 42);

        BaseConfiguration config = new TestConfiguration("test-id", true, "Test Config", "1.0.0", options);

        assertEquals("test-id", config.getId());
        assertTrue(config.isEnabled());
        assertEquals("Test Config", config.getName());
        assertEquals("1.0.0", config.getVersion());
        assertEquals("value1", config.getCustomOption("key1"));
        assertEquals(42, config.getCustomOption("key2"));
        assertTrue(config.hasCustomOption("key1"));
        assertTrue(config.hasCustomOption("key2"));
        assertFalse(config.hasCustomOption("nonexistent"));

        // Test that custom options are immutable
        Map<String, Object> returnedOptions = config.getCustomOptions();
        assertThrows(UnsupportedOperationException.class, () -> returnedOptions.put("new", "value"));
    }

    @Test
    void testBaseConfigurationWithNullOptions() {
        BaseConfiguration config = new TestConfiguration("test-id", true, "Test Config", "1.0.0", null);

        assertNotNull(config.getCustomOptions());
        assertTrue(config.getCustomOptions().isEmpty());
        assertNull(config.getCustomOption("any"));
        assertFalse(config.hasCustomOption("any"));
    }

    @Test
    void testBaseConfigurationEquality() {
        Map<String, Object> options1 = Map.of("key", "value");
        Map<String, Object> options2 = Map.of("key", "value");

        BaseConfiguration config1 = new TestConfiguration("test-id", true, "Test Config", "1.0.0", options1);
        BaseConfiguration config2 = new TestConfiguration("test-id", true, "Test Config", "1.0.0", options2);
        BaseConfiguration config3 = new TestConfiguration("different-id", true, "Test Config", "1.0.0", options1);

        assertEquals(config1, config2);
        assertEquals(config1.hashCode(), config2.hashCode());
        assertNotEquals(config1, config3);
        assertNotEquals(config1.hashCode(), config3.hashCode());
    }

    @Test
    void testBaseConfigurationToString() {
        BaseConfiguration config = new TestConfiguration("test-id", true, "Test Config", "1.0.0", Map.of());
        String toString = config.toString();

        assertTrue(toString.contains("test-id"));
        assertTrue(toString.contains("Test Config"));
        assertTrue(toString.contains("1.0.0"));
        assertTrue(toString.contains("true"));
    }

    @Test
    void testNullValidation() {
        assertThrows(NullPointerException.class, () -> new TestConfiguration(null, true, "Test", "1.0", Map.of()));
        assertThrows(NullPointerException.class, () -> new TestConfiguration("id", true, null, "1.0", Map.of()));
        assertThrows(NullPointerException.class, () -> new TestConfiguration("id", true, "Test", null, Map.of()));
    }

    /**
     * Test implementation of BaseConfiguration for testing purposes.
     */
    private static class TestConfiguration extends BaseConfiguration {
        public TestConfiguration(String id, boolean enabled, String name, String version,
                Map<String, Object> customOptions) {
            super(id, enabled, name, version, customOptions);
        }
    }
}
