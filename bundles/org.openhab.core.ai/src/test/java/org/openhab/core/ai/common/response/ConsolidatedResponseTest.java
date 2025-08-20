package org.openhab.core.ai.common.response;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.openhab.core.ai.agent.infrastructure.synchronization.AgentResponse;
import org.openhab.core.ai.model.ModelResponse;
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
        ModelResponse response = ModelResponse.builder().withContent("Test content").withModelName("gpt-4")
                .withProviderType("openai").build();

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
        ToolResponse response = new ToolResponse("test-id", "test-result");

        // Verify Response interface methods work
        assertEquals("test-id", response.getId());
        assertTrue(response.isSuccess());
        assertEquals("test-result", response.getData());
        assertNull(response.getErrorMessage());
        assertTrue(response.getTimestamp() > 0);

        // Verify existing methods still work
        assertEquals("2.0", response.getJsonrpc());
        assertTrue(response.getResult().isPresent());
        assertEquals("test-result", response.getResult().get());
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
        ModelResponse modelError = ModelResponse.builder().withContent("").withModelName("gpt-4")
                .withProviderType("openai").withErrorMessage("Model error").build();

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
        Response<String> modelResponse = ModelResponse.builder().withContent("model content").withModelName("gpt-4")
                .withProviderType("openai").build();

        Response<Object> toolResponse = new ToolResponse("tool-id", "tool result");

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

        // ModelResponse builder pattern
        ModelResponse modelResponse = ModelResponse.builder().withContent("content").withModelName("model")
                .withProviderType("provider").withPromptTokens(10).withCompletionTokens(20).withTotalTokens(30)
                .withCost(0.001).withResponseTimeMs(1000).withMetadata(Map.of("key", "value")).withFinishReason("stop")
                .build();

        assertEquals("content", modelResponse.getContent());
        assertEquals(10, modelResponse.getPromptTokens());
        assertEquals(0.001, modelResponse.getCost());

        // ToolResponse constructors
        ToolResponse toolResponse = new ToolResponse("id", "result");
        assertEquals("id", toolResponse.getId());
        assertEquals("result", toolResponse.getResult().orElse(null));

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
