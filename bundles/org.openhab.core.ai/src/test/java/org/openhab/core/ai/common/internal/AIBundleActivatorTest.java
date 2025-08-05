package org.openhab.core.ai.common.internal;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.ai.action.ActionContext;
import org.openhab.core.ai.action.ActionError;
import org.openhab.core.ai.action.ActionMetadata;
import org.openhab.core.ai.action.ActionResult;
import org.openhab.core.ai.action.ActionValidationResult;

@ExtendWith(MockitoExtension.class)
class ActionFrameworkTest {

    @BeforeEach
    void setUp() {
        // Setup for Action framework tests
    }

    @Test
    void testActionContextBuilder() {
        // Test ActionContext builder
        ActionContext context = ActionContext.builder().protocol("mcp").clientId("test-client")
                .sessionId("test-session").correlationId("test-correlation").build();

        assertEquals("mcp", context.getProtocol());
        assertEquals("test-client", context.getClientId());
        assertEquals("test-session", context.getSessionId());
        assertEquals("test-correlation", context.getCorrelationId());
    }

    @Test
    void testActionResultSuccess() {
        // Test successful ActionResult
        Map<String, Object> data = Map.of("key", "value");
        ActionResult result = ActionResult.success(data, 100L);

        assertTrue(result.isSuccess());
        assertEquals("Success", result.getMessage());
        assertEquals(data, result.getData());
        assertEquals(100L, result.getExecutionTimeMs());
        assertNull(result.getError());
    }

    @Test
    void testActionResultError() {
        // Test error ActionResult
        ActionError error = new ActionError("TEST_ERROR", "Test error message");
        ActionResult result = ActionResult.error("Operation failed", error, 50L);

        assertFalse(result.isSuccess());
        assertEquals("Operation failed", result.getMessage());
        assertNull(result.getData());
        assertEquals(50L, result.getExecutionTimeMs());
        assertEquals(error, result.getError());
    }

    @Test
    void testActionError() {
        // Test ActionError
        ActionError error = new ActionError("VALIDATION_ERROR", "Invalid parameters", "VALIDATION_ERROR");

        assertEquals("VALIDATION_ERROR", error.getErrorCode());
        assertEquals("Invalid parameters", error.getErrorMessage());
        assertEquals("VALIDATION_ERROR", error.getErrorType());
        assertNull(error.getCause());
    }

    @Test
    void testActionValidationResultValid() {
        // Test valid ActionValidationResult
        Map<String, Object> sanitizedParams = Map.of("param1", "value1");
        ActionValidationResult result = ActionValidationResult.valid(sanitizedParams);

        assertTrue(result.isValid());
        assertTrue(result.getErrors().isEmpty());
        assertTrue(result.getWarnings().isEmpty());
        assertEquals(sanitizedParams, result.getSanitizedParameters());
    }

    @Test
    void testActionValidationResultInvalid() {
        // Test invalid ActionValidationResult
        ActionValidationResult result = ActionValidationResult
                .invalid(java.util.List.of("Parameter 'name' is required"));

        assertFalse(result.isValid());
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getWarnings().isEmpty());
        assertTrue(result.getSanitizedParameters().isEmpty());
    }

    @Test
    void testActionMetadataBuilder() {
        // Test ActionMetadata builder
        ActionMetadata metadata = ActionMetadata.builder().version("1.0.0").author("Test Author")
                .description("Test action").build();

        assertEquals("1.0.0", metadata.getVersion());
        assertEquals("Test Author", metadata.getAuthor());
        assertEquals("Test action", metadata.getDescription());
        assertNotNull(metadata.getCreated());
        assertNotNull(metadata.getLastModified());
    }
}
