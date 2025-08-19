package org.openhab.core.ai.common.context;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for the ToolContext class.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
class ToolContextTest {

    @Test
    void testToolContextCreation() {
        // Given
        String contextId = "test-context-1";
        String toolId = "test-tool";
        String toolName = "Test Tool";
        String toolVersion = "1.0.0";
        String clientId = "test-client";
        String sessionId = "test-session";
        Map<String, Object> values = Map.of("param1", "value1", "param2", 42);
        Map<String, Object> metadata = Map.of("source", "test", "priority", "high");

        // When
        ToolContext context = new ToolContext(contextId, toolId, toolName, toolVersion, clientId, sessionId, values,
                metadata);

        // Then
        assertEquals(contextId, context.getContextId());
        assertEquals("tool", context.getContextType());
        assertEquals("1.0.0", context.getVersion());
        assertEquals(toolId, context.getToolId());
        assertEquals(toolName, context.getToolName());
        assertEquals(toolVersion, context.getToolVersion());
        assertEquals(clientId, context.getClientId());
        assertEquals(sessionId, context.getSessionId());
        assertEquals("value1", context.getValue("param1"));
        assertEquals(42, context.getValue("param2", Integer.class));
        assertEquals("test", context.getMetadata("source"));
        assertEquals("high", context.getMetadata("priority"));
        assertTrue(context.getExecutionStartTime() > 0);
        assertTrue(context.getExecutionDuration() >= 0);
        assertFalse(context.isEmpty());
        assertEquals(2, context.size());
    }

    @Test
    void testToolContextWithCustomTimestamps() {
        // Given
        String contextId = "test-context-2";
        String toolId = "test-tool-2";
        String toolName = "Test Tool 2";
        String toolVersion = "2.0.0";
        Instant createdAt = Instant.now().minusSeconds(3600);
        Instant lastModifiedAt = Instant.now().minusSeconds(1800);
        long executionStartTime = System.currentTimeMillis() - 5000;

        // When
        ToolContext context = new ToolContext(contextId, toolId, toolName, toolVersion, null, null, null, null,
                createdAt, lastModifiedAt, executionStartTime);

        // Then
        assertEquals(createdAt, context.getCreatedAt());
        assertEquals(lastModifiedAt, context.getLastModifiedAt());
        assertEquals(executionStartTime, context.getExecutionStartTime());
        assertNull(context.getClientId());
        assertNull(context.getSessionId());
        assertTrue(context.isEmpty());
    }

    @Test
    void testToolContextNullValidation() {
        // Given & When & Then
        assertThrows(NullPointerException.class,
                () -> new ToolContext(null, "tool", "name", "1.0.0", null, null, null, null));
        assertThrows(NullPointerException.class,
                () -> new ToolContext("id", null, "name", "1.0.0", null, null, null, null));
        assertThrows(NullPointerException.class,
                () -> new ToolContext("id", "tool", null, "1.0.0", null, null, null, null));
        assertThrows(NullPointerException.class,
                () -> new ToolContext("id", "tool", "name", null, null, null, null, null));
    }

    @Test
    void testToolContextEquality() {
        // Given
        String contextId = "test-context-3";
        String toolId = "test-tool-3";
        String toolName = "Test Tool 3";
        String toolVersion = "3.0.0";
        Map<String, Object> values = Map.of("test", "value");

        ToolContext context1 = new ToolContext(contextId, toolId, toolName, toolVersion, "client1", "session1", values,
                null);
        ToolContext context2 = new ToolContext(contextId, toolId, toolName, toolVersion, "client1", "session1", values,
                null);

        // When & Then
        assertEquals(context1, context2);
        assertEquals(context1.hashCode(), context2.hashCode());
    }

    @Test
    void testToolContextInequality() {
        // Given
        String contextId = "test-context-4";
        String toolId = "test-tool-4";
        String toolName = "Test Tool 4";
        String toolVersion = "4.0.0";

        ToolContext context1 = new ToolContext(contextId, toolId, toolName, toolVersion, "client1", "session1", null,
                null);
        ToolContext context2 = new ToolContext(contextId, toolId, toolName, toolVersion, "client2", "session1", null,
                null);

        // When & Then
        assertNotEquals(context1, context2);
        assertNotEquals(context1.hashCode(), context2.hashCode());
    }

    @Test
    void testToolContextToString() {
        // Given
        String contextId = "test-context-5";
        String toolId = "test-tool-5";
        String toolName = "Test Tool 5";
        String toolVersion = "5.0.0";

        ToolContext context = new ToolContext(contextId, toolId, toolName, toolVersion, "client5", "session5", null,
                null);

        // When
        String result = context.toString();

        // Then
        assertTrue(result.contains("ToolContext"));
        assertTrue(result.contains(contextId));
        assertTrue(result.contains(toolId));
        assertTrue(result.contains(toolName));
        assertTrue(result.contains(toolVersion));
        assertTrue(result.contains("client5"));
        assertTrue(result.contains("session5"));
        assertTrue(result.contains("executionTime="));
    }

    @Test
    void testToolContextValueRetrieval() {
        // Given
        Map<String, Object> values = new HashMap<>();
        values.put("string", "test");
        values.put("integer", 42);
        values.put("boolean", true);
        values.put("double", 3.14);

        ToolContext context = new ToolContext("test", "tool", "name", "1.0.0", null, null, values, null);

        // When & Then
        assertEquals("test", context.getValue("string", String.class));
        assertEquals(42, context.getValue("integer", Integer.class));
        assertTrue(context.getValue("boolean", Boolean.class));
        assertEquals(3.14, context.getValue("double", Double.class));
        assertNull(context.getValue("nonexistent"));
        assertNull(context.getValue("string", Integer.class)); // Wrong type
    }

    @Test
    void testToolContextMetadata() {
        // Given
        Map<String, Object> metadata = Map.of("source", "test", "priority", "high", "tags", "important");

        ToolContext context = new ToolContext("test", "tool", "name", "1.0.0", null, null, null, metadata);

        // When & Then
        assertEquals("test", context.getMetadata("source"));
        assertEquals("high", context.getMetadata("priority"));
        assertEquals("important", context.getMetadata("tags"));
        assertNull(context.getMetadata("nonexistent"));
        assertTrue(context.hasMetadata("source"));
        assertFalse(context.hasMetadata("nonexistent"));
    }
}
