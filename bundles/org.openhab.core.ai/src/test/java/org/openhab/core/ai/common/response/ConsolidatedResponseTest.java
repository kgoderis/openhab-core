package org.openhab.core.ai.common.response;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.openhab.core.ai.agent.infrastructure.synchronization.AgentResponse;
import org.openhab.core.ai.stub.StubResponse;

/**
 * Comprehensive tests for the consolidated response hierarchy.
 * 
 * Tests that all existing response classes now implement the unified Response interface
 * and maintain backward compatibility while providing consistent behavior.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
class ConsolidatedResponseTest {

    @Test
    void testModelResponseUnifiedInterface() {
        // Test that existing ModelResponse implements Response interface
        ModelResponse response = ModelResponse.success("Test content", "gpt-4", "openai");

        // Verify Response interface methods work
        assertNotNull(response.getId());
        assertTrue(response.isSuccess());
        assertEquals("Test content", response.getData());
        assertNull(response.getErrorMessage());
        assertTrue(response.getTimestamp() > 0);

        // Verify existing methods still work
        assertEquals("Test content", response.getContent());
        assertEquals("gpt-4", response.getModelName());
        assertEquals("openai", response.getProviderType());
    }

    @Test
    void testToolResponseUnifiedInterface() {
        // Test that existing ToolResponse implements Response interface
        ToolResponse response = ToolResponse.success("test-result", "test-tool", "test-operation");

        // Verify Response interface methods work
        assertNotNull(response.getId());
        assertTrue(response.isSuccess());
        assertEquals("test-result", response.getData());
        assertNull(response.getErrorMessage());
        assertTrue(response.getTimestamp() > 0);

        // Verify existing methods still work
        assertEquals("2.0", response.getJsonrpc());
        assertEquals("test-result", response.getData());
    }

    @Test
    void testAgentResponseUnifiedInterface() {
        // Test that existing AgentResponse implements Response interface
        AgentResponse response = new AgentResponse("task-123", true, "Success", "data");

        // Verify Response interface methods work
        assertEquals("task-123", response.getId());
        assertTrue(response.isSuccess());
        assertEquals("data", response.getData());
        assertNull(response.getErrorMessage());
        assertTrue(response.getTimestamp() > 0);

        // Verify existing methods still work
        assertEquals("task-123", response.getTaskId());
        assertEquals("Success", response.getMessage());
    }

    @Test
    void testMessageResponseUnifiedInterface() {
        // Test that existing MessageResponse implements Response interface
        MessageResponse response = MessageResponse.success("Success message");

        // Verify Response interface methods work
        assertNotNull(response.getId());
        assertTrue(response.isSuccess());
        assertEquals("Success message", response.getData());
        assertNull(response.getErrorMessage());
        assertTrue(response.getTimestamp() > 0);

        // Verify existing methods still work
        assertTrue(response.isAcknowledged());
        assertEquals("Success message", response.getMessage());
    }

    @Test
    void testStubResponseUnifiedInterface() {
        // Test that existing StubResponse implements Response interface
        StubResponse response = StubResponse.success("test data");

        // Verify Response interface methods work
        assertNotNull(response.getId());
        assertTrue(response.isSuccess());
        assertEquals("test data", response.getData());
        assertNull(response.getErrorMessage());
        assertTrue(response.getTimestamp() > 0);

        // Verify existing methods still work
        assertTrue(response.isSuccess());
        assertTrue(response.getData().isPresent());
        assertEquals("test data", response.getData().get());
    }

    @Test
    void testErrorResponses() {
        // Test error responses across all types
        ModelResponse modelError = ModelResponse.error("Model error", "gpt-4", "openai");

        assertFalse(modelError.isSuccess());
        assertEquals("Model error", modelError.getErrorMessage());

        MessageResponse messageError = MessageResponse.failure("Message error");
        assertFalse(messageError.isSuccess());
        assertEquals("Message error", messageError.getErrorMessage());

        StubResponse stubError = StubResponse.error("Stub error");
        assertFalse(stubError.isSuccess());
        assertEquals("Stub error", stubError.getErrorMessage());
    }

    @Test
    void testResponsePolymorphism() {
        // Test that all response types can be treated as Response interface
        Response<String> modelResponse = ModelResponse.success("model content", "gpt-4", "openai");

        Response<Object> toolResponse = ToolResponse.success("tool result", "test-tool", "test-operation");

        Response<Object> agentResponse = new AgentResponse("agent-task", true, "Success", "agent data");

        Response<String> messageResponse = MessageResponse.success("message content");

        Response<Object> stubResponse = StubResponse.success("stub data");

        // Verify all implement the same interface contract
        assertNotNull(modelResponse.getId());
        assertNotNull(toolResponse.getId());
        assertNotNull(agentResponse.getId());
        assertNotNull(messageResponse.getId());
        assertNotNull(stubResponse.getId());

        assertTrue(modelResponse.isSuccess());
        assertTrue(toolResponse.isSuccess());
        assertTrue(agentResponse.isSuccess());
        assertTrue(messageResponse.isSuccess());
        assertTrue(stubResponse.isSuccess());
    }

    @Test
    void testBackwardCompatibility() {
        // Test that existing code patterns still work

        // ModelResponse static factory
        ModelResponse modelResponse = ModelResponse.success("content", "model", "provider");

        assertEquals("content", modelResponse.getContent());
        assertEquals("model", modelResponse.getModelName());
        assertEquals("provider", modelResponse.getProviderType());

        // ToolResponse static factory
        ToolResponse toolResponse = ToolResponse.success("result", "test-tool", "test-operation");
        assertNotNull(toolResponse.getId());
        assertEquals("result", toolResponse.getData());

        // MessageResponse static factories
        MessageResponse success = MessageResponse.success("success");
        MessageResponse failure = MessageResponse.failure("failure");

        assertTrue(success.isAcknowledged());
        assertFalse(failure.isAcknowledged());

        // StubResponse static factories
        StubResponse stubSuccess = StubResponse.success("data");
        StubResponse stubError = StubResponse.error("error");

        assertTrue(stubSuccess.isSuccess());
        assertFalse(stubError.isSuccess());
    }
}
