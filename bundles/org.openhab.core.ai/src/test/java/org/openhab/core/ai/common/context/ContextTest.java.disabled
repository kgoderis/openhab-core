package org.openhab.core.ai.common.context;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for the Context interface and BaseContext class.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
class ContextTest {

    @Test
    void testBaseContextCreation() {
        Context context = new TestContext("test-id", "test-type", "1.0.0", Map.of("key", "value"),
                Map.of("meta", "data"));

        assertEquals("test-id", context.getContextId());
        assertEquals("test-type", context.getContextType());
        assertEquals("1.0.0", context.getVersion());
        assertEquals("value", context.getValue("key"));
        assertEquals("data", context.getMetadata("meta"));
        assertTrue(context.hasValue("key"));
        assertTrue(context.hasMetadata("meta"));
        assertFalse(context.hasValue("nonexistent"));
        assertFalse(context.hasMetadata("nonexistent"));
        assertEquals(1, context.size());
        assertFalse(context.isEmpty());
    }

    @Test
    void testBaseContextWithNullValues() {
        Context context = new TestContext("test-id", "test-type", "1.0.0", null, null);

        assertEquals("test-id", context.getContextId());
        assertEquals("test-type", context.getContextType());
        assertEquals("1.0.0", context.getVersion());
        assertNull(context.getValue("key"));
        assertNull(context.getMetadata("meta"));
        assertFalse(context.hasValue("key"));
        assertFalse(context.hasMetadata("meta"));
        assertEquals(0, context.size());
        assertTrue(context.isEmpty());
    }

    @Test
    void testTypedValueRetrieval() {
        Context context = new TestContext("test-id", "test-type", "1.0.0",
                Map.of("string", "value", "integer", 42, "boolean", true), Map.of());

        assertEquals("value", context.getValue("string", String.class));
        assertEquals(42, context.getValue("integer", Integer.class));
        assertEquals(true, context.getValue("boolean", Boolean.class));
        assertNull(context.getValue("string", Integer.class)); // Wrong type
        assertNull(context.getValue("nonexistent", String.class)); // Non-existent
    }

    @Test
    void testImmutableValues() {
        Map<String, Object> originalValues = Map.of("key", "value");
        Map<String, Object> originalMetadata = Map.of("meta", "data");

        Context context = new TestContext("test-id", "test-type", "1.0.0", originalValues, originalMetadata);

        Map<String, Object> returnedValues = context.getAllValues();
        Map<String, Object> returnedMetadata = context.getMetadata();

        // Verify that returned maps are immutable
        assertThrows(UnsupportedOperationException.class, () -> returnedValues.put("new", "value"));
        assertThrows(UnsupportedOperationException.class, () -> returnedMetadata.put("new", "data"));

        // Verify that original maps are not modified
        assertEquals(1, originalValues.size());
        assertEquals(1, originalMetadata.size());
    }

    @Test
    void testEquality() {
        Context context1 = new TestContext("test-id", "test-type", "1.0.0", Map.of("key", "value"),
                Map.of("meta", "data"));
        Context context2 = new TestContext("test-id", "test-type", "1.0.0", Map.of("key", "value"),
                Map.of("meta", "data"));
        Context context3 = new TestContext("different-id", "test-type", "1.0.0", Map.of("key", "value"),
                Map.of("meta", "data"));

        assertEquals(context1, context2);
        assertEquals(context1.hashCode(), context2.hashCode());
        assertNotEquals(context1, context3);
        assertNotEquals(context1.hashCode(), context3.hashCode());
    }

    @Test
    void testToString() {
        Context context = new TestContext("test-id", "test-type", "1.0.0", Map.of("key", "value"),
                Map.of("meta", "data"));
        String toString = context.toString();

        assertTrue(toString.contains("test-id"));
        assertTrue(toString.contains("test-type"));
        assertTrue(toString.contains("1.0.0"));
        assertTrue(toString.contains("size=1"));
        assertTrue(toString.contains("metadataSize=1"));
    }

    @Test
    void testTimestamps() {
        Instant before = Instant.now();
        Context context = new TestContext("test-id", "test-type", "1.0.0", Map.of(), Map.of());
        Instant after = Instant.now();

        assertTrue(context.getCreatedAt().isAfter(before) || context.getCreatedAt().equals(before));
        assertTrue(context.getCreatedAt().isBefore(after) || context.getCreatedAt().equals(after));
        assertTrue(context.getLastModifiedAt().isAfter(before) || context.getLastModifiedAt().equals(before));
        assertTrue(context.getLastModifiedAt().isBefore(after) || context.getLastModifiedAt().equals(after));
    }

    /**
     * Test implementation of BaseContext for testing purposes.
     */
    private static class TestContext extends BaseContext {
        public TestContext(String contextId, String contextType, String version, Map<String, Object> values,
                Map<String, Object> metadata) {
            super(contextId, contextType, version, values, metadata);
        }
    }
}
