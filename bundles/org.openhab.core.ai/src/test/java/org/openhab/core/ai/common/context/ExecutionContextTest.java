package org.openhab.core.ai.common.context;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.openhab.core.ai.auth.AuthenticationContext;

/**
 * Unit tests for ExecutionContext.Builder.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
class ExecutionContextTest {

    @Test
    void testBuilderCreation() {
        ExecutionContext context = ExecutionContext.builder().withContextId("test-context").withProtocol("mcp")
                .withClientId("test-client").withCorrelationId("test-correlation").build();

        assertEquals("test-context", context.getContextId());
        assertEquals("mcp", context.getProtocol());
        assertEquals("test-client", context.getClientId());
        assertEquals("test-correlation", context.getCorrelationId());
        assertEquals("execution", context.getContextType());
        assertEquals("1.0.0", context.getVersion());
    }

    @Test
    void testDefaultValues() {
        ExecutionContext context = ExecutionContext.builder().withContextId("test").withProtocol("mcp")
                .withClientId("client").withCorrelationId("correlation").build();

        assertNotNull(context.getSessionId());
        assertNotNull(context.getCorrelationId());
        assertTrue(context.getExecutionStartTime() > 0);
        assertFalse(context.getPriority().isPresent());
        assertNull(context.getAuthContext());
    }

    @Test
    void testCustomValues() {
        String sessionId = "custom-session";
        String priority = "high";
        long startTime = System.currentTimeMillis();
        AuthenticationContext authContext = new AuthenticationContext("test-user", "test-token");

        ExecutionContext context = ExecutionContext.builder().withContextId("test").withProtocol("mcp")
                .withClientId("client").withCorrelationId("correlation").withSessionId(sessionId).withPriority(priority)
                .withExecutionStartTime(startTime).withAuthContext(authContext).build();

        assertEquals(sessionId, context.getSessionId());
        assertEquals(priority, context.getPriority().orElse(null));
        assertEquals(startTime, context.getExecutionStartTime());
        assertEquals(authContext, context.getAuthContext());
    }

    @Test
    void testToBuilder() {
        ExecutionContext original = ExecutionContext.builder().withContextId("original").withProtocol("mcp")
                .withClientId("client").withCorrelationId("correlation").withSessionId("original-session")
                .withPriority("low").build();

        ExecutionContext modified = original.toBuilder().withSessionId("modified-session").withPriority("high").build();

        assertEquals("original", modified.getContextId());
        assertEquals("mcp", modified.getProtocol());
        assertEquals("client", modified.getClientId());
        assertEquals("correlation", modified.getCorrelationId());
        assertEquals("modified-session", modified.getSessionId());
        assertEquals("high", modified.getPriority().orElse(null));
    }

    @Test
    void testValidation() {
        // Test blank contextId
        assertThrows(IllegalArgumentException.class, () -> {
            ExecutionContext.builder().withContextId("").withProtocol("mcp").withClientId("client")
                    .withCorrelationId("correlation").build();
        });

        // Test blank protocol
        assertThrows(IllegalArgumentException.class, () -> {
            ExecutionContext.builder().withContextId("test").withProtocol("").withClientId("client")
                    .withCorrelationId("correlation").build();
        });

        // Test blank clientId
        assertThrows(IllegalArgumentException.class, () -> {
            ExecutionContext.builder().withContextId("test").withProtocol("mcp").withClientId("")
                    .withCorrelationId("correlation").build();
        });

        // Test blank correlationId
        assertThrows(IllegalArgumentException.class, () -> {
            ExecutionContext.builder().withContextId("test").withProtocol("mcp").withClientId("client")
                    .withCorrelationId("").build();
        });
    }

    @Test
    void testValuesAndMetadata() {
        Map<String, Object> values = new HashMap<>();
        values.put("key1", "value1");
        values.put("key2", 42);

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("meta1", "data1");
        metadata.put("meta2", true);

        ExecutionContext context = ExecutionContext.builder().withContextId("test").withProtocol("mcp")
                .withClientId("client").withCorrelationId("correlation").withValues(values).withMetadata(metadata)
                .build();

        assertEquals("value1", context.getValue("key1"));
        assertEquals(42, context.getValue("key2"));
        assertEquals("data1", context.getMetadata("meta1"));
        assertEquals(true, context.getMetadata("meta2"));
    }

    @Test
    void testProtocolValues() {
        ExecutionContext context = ExecutionContext.builder().withContextId("test").withProtocol("mcp")
                .withClientId("client").withCorrelationId("correlation").withActionName("test-action")
                .withActionId("action-123").withParameters(Map.of("param1", "value1"))
                .withArguments(Map.of("arg1", "value1")).build();

        assertEquals("test-action", context.getValue("protocol.actionName"));
        assertEquals("action-123", context.getValue("protocol.actionId"));
        assertEquals(Map.of("param1", "value1"), context.getValue("protocol.parameters"));
        assertEquals(Map.of("arg1", "value1"), context.getValue("protocol.arguments"));
    }

    @Test
    void testImmutability() {
        Map<String, Object> originalValues = new HashMap<>();
        originalValues.put("key", "value");

        Map<String, Object> originalMetadata = new HashMap<>();
        originalMetadata.put("meta", "data");

        ExecutionContext context = ExecutionContext.builder().withContextId("test").withProtocol("mcp")
                .withClientId("client").withCorrelationId("correlation").withValues(originalValues)
                .withMetadata(originalMetadata).build();

        // Verify that modifying the original maps doesn't affect the context
        originalValues.put("newKey", "newValue");
        originalMetadata.put("newMeta", "newData");

        assertNull(context.getValue("newKey"));
        assertNull(context.getMetadata("newMeta"));
    }

    @Test
    void testNullHandling() {
        ExecutionContext context = ExecutionContext.builder().withContextId("test").withProtocol("mcp")
                .withClientId("client").withCorrelationId("correlation").withAuthContext(null).withPriority(null)
                .withValues(null).withMetadata(null).build();

        assertNull(context.getAuthContext());
        assertFalse(context.getPriority().isPresent());
        assertTrue(context.getAllValues().isEmpty());
        assertTrue(context.getMetadata().isEmpty());
    }

    @Test
    void testEquality() {
        ExecutionContext context1 = ExecutionContext.builder().withContextId("test").withProtocol("mcp")
                .withClientId("client").withCorrelationId("correlation").build();

        ExecutionContext context2 = ExecutionContext.builder().withContextId("test").withProtocol("mcp")
                .withClientId("client").withCorrelationId("correlation").build();

        // Note: Since ExecutionContext doesn't override equals/hashCode,
        // these will not be equal. This test verifies the current behavior.
        assertNotEquals(context1, context2);
    }

    @Test
    void testToString() {
        ExecutionContext context = ExecutionContext.builder().withContextId("test-context").withProtocol("mcp")
                .withClientId("test-client").withSessionId("test-session").withCorrelationId("test-correlation")
                .build();

        String toString = context.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("ExecutionContext"));
        assertTrue(toString.contains("test-context"));
        assertTrue(toString.contains("mcp"));
        assertTrue(toString.contains("test-client"));
        assertTrue(toString.contains("test-session"));
        assertTrue(toString.contains("test-correlation"));
    }

    @Test
    void testExecutionDuration() {
        ExecutionContext context = ExecutionContext.builder().withContextId("test").withProtocol("mcp")
                .withClientId("client").withCorrelationId("correlation").build();

        long duration = context.getExecutionDuration();
        assertTrue(duration >= 0);
    }

    @Test
    void testCustomTimestamps() {
        Instant createdAt = Instant.now().minusSeconds(3600); // 1 hour ago
        Instant lastModifiedAt = Instant.now().minusSeconds(1800); // 30 minutes ago

        ExecutionContext context = ExecutionContext.builder().withContextId("test").withProtocol("mcp")
                .withClientId("client").withCorrelationId("correlation").withCreatedAt(createdAt)
                .withLastModifiedAt(lastModifiedAt).build();

        assertEquals(createdAt, context.getCreatedAt());
        assertEquals(lastModifiedAt, context.getLastModifiedAt());
    }
}
