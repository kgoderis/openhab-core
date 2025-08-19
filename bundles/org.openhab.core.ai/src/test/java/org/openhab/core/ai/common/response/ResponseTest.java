package org.openhab.core.ai.common.response;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for the unified Response hierarchy.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
class ResponseTest {

    @Test
    void testModelResponseSuccess() {
        ModelResponse response = ModelResponse.success("Test content", "gpt-4", "openai");

        assertNotNull(response.getId());
        assertTrue(response.isSuccess());
        assertEquals("Test content", response.getData());
        assertNull(response.getErrorMessage());
        assertTrue(response.getTimestamp() > 0);
        assertEquals("gpt-4", response.getModelName());
        assertEquals("openai", response.getProviderType());
    }

    @Test
    void testModelResponseError() {
        ModelResponse response = ModelResponse.error("Test error", "gpt-4", "openai");

        assertNotNull(response.getId());
        assertFalse(response.isSuccess());
        assertEquals("", response.getData());
        assertEquals("Test error", response.getErrorMessage());
        assertTrue(response.getTimestamp() > 0);
        assertEquals("gpt-4", response.getModelName());
        assertEquals("openai", response.getProviderType());
    }

    @Test
    void testToolResponseSuccess() {
        Map<String, Object> data = Map.of("result", "success", "value", 42);
        ToolResponse response = ToolResponse.success(data, "test-tool", "execute");

        assertNotNull(response.getId());
        assertTrue(response.isSuccess());
        assertEquals(data, response.getData());
        assertNull(response.getErrorMessage());
        assertTrue(response.getTimestamp() > 0);
        assertEquals("test-tool", response.getToolId());
        assertEquals("execute", response.getOperation());
    }

    @Test
    void testToolResponseError() {
        ToolResponse response = ToolResponse.error("Tool failed", "test-tool", "execute");

        assertNotNull(response.getId());
        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertEquals("Tool failed", response.getErrorMessage());
        assertTrue(response.getTimestamp() > 0);
        assertEquals("test-tool", response.getToolId());
        assertEquals("execute", response.getOperation());
    }

    @Test
    void testToolResponseWithExecutionTime() {
        Map<String, Object> data = Map.of("result", "success");
        ToolResponse response = ToolResponse.success(data, "test-tool", "execute", 150);

        assertNotNull(response.getId());
        assertTrue(response.isSuccess());
        assertEquals(data, response.getData());
        assertEquals(150, response.getExecutionTimeMs());
    }

    @Test
    void testAgentResponseSuccess() {
        Map<String, Object> data = Map.of("action", "completed", "status", "ok");
        AgentResponse response = AgentResponse.success(data, "test-agent", "perform-action");

        assertNotNull(response.getId());
        assertTrue(response.isSuccess());
        assertEquals(data, response.getData());
        assertNull(response.getErrorMessage());
        assertTrue(response.getTimestamp() > 0);
        assertEquals("test-agent", response.getAgentId());
        assertEquals("perform-action", response.getAction());
        assertEquals("SUCCESS", response.getResponseType());
    }

    @Test
    void testAgentResponseError() {
        AgentResponse response = AgentResponse.error("Agent failed", "test-agent", "perform-action");

        assertNotNull(response.getId());
        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertEquals("Agent failed", response.getErrorMessage());
        assertTrue(response.getTimestamp() > 0);
        assertEquals("test-agent", response.getAgentId());
        assertEquals("perform-action", response.getAction());
        assertEquals("ERROR", response.getResponseType());
    }

    @Test
    void testAgentResponseNotification() {
        AgentResponse response = AgentResponse.notification("Task completed", "test-agent", "perform-action");

        assertNotNull(response.getId());
        assertTrue(response.isSuccess());
        assertEquals("Task completed", response.getData());
        assertNull(response.getErrorMessage());
        assertEquals("NOTIFICATION", response.getResponseType());
    }

    @Test
    void testMessageResponseSuccess() {
        MessageResponse response = MessageResponse.success("Test message", "INFO", "test-source");

        assertNotNull(response.getId());
        assertTrue(response.isSuccess());
        assertEquals("Test message", response.getData());
        assertNull(response.getErrorMessage());
        assertTrue(response.getTimestamp() > 0);
        assertEquals("INFO", response.getMessageType());
        assertEquals("test-source", response.getSource());
    }

    @Test
    void testMessageResponseError() {
        MessageResponse response = MessageResponse.error("Message failed", "ERROR", "test-source");

        assertNotNull(response.getId());
        assertFalse(response.isSuccess());
        assertEquals("", response.getData());
        assertEquals("Message failed", response.getErrorMessage());
        assertTrue(response.getTimestamp() > 0);
        assertEquals("ERROR", response.getMessageType());
        assertEquals("test-source", response.getSource());
    }

    @Test
    void testMessageResponseInfo() {
        MessageResponse response = MessageResponse.info("Information message", "test-source");

        assertNotNull(response.getId());
        assertTrue(response.isSuccess());
        assertEquals("Information message", response.getData());
        assertEquals("INFO", response.getMessageType());
        assertEquals("test-source", response.getSource());
    }

    @Test
    void testMessageResponseWarning() {
        MessageResponse response = MessageResponse.warning("Warning message", "test-source");

        assertNotNull(response.getId());
        assertTrue(response.isSuccess());
        assertEquals("Warning message", response.getData());
        assertEquals("WARNING", response.getMessageType());
        assertEquals("test-source", response.getSource());
    }

    @Test
    void testMessageResponseDebug() {
        MessageResponse response = MessageResponse.debug("Debug message", "test-source");

        assertNotNull(response.getId());
        assertTrue(response.isSuccess());
        assertEquals("Debug message", response.getData());
        assertEquals("DEBUG", response.getMessageType());
        assertEquals("test-source", response.getSource());
    }

    @Test
    void testResponseEquality() {
        ModelResponse response1 = ModelResponse.success("Test", "gpt-4", "openai");
        ModelResponse response2 = ModelResponse.success("Test", "gpt-4", "openai");

        // Responses with different IDs should not be equal
        assertNotEquals(response1, response2);

        // Same response should be equal to itself
        assertEquals(response1, response1);
    }

    @Test
    void testResponseHashCode() {
        ModelResponse response1 = ModelResponse.success("Test", "gpt-4", "openai");
        ModelResponse response2 = ModelResponse.success("Test", "gpt-4", "openai");

        // Different responses should have different hash codes
        assertNotEquals(response1.hashCode(), response2.hashCode());

        // Same response should have same hash code
        assertEquals(response1.hashCode(), response1.hashCode());
    }

    @Test
    void testResponseToString() {
        ModelResponse response = ModelResponse.success("Test", "gpt-4", "openai");
        String toString = response.toString();

        assertNotNull(toString);
        assertTrue(toString.contains("ModelResponse"));
        assertTrue(toString.contains("Test"));
        assertTrue(toString.contains("gpt-4"));
        assertTrue(toString.contains("openai"));
    }
}
