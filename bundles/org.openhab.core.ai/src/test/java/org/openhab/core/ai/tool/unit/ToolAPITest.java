package org.openhab.core.ai.tool.unit;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.openhab.core.ai.common.context.ToolContext;
import org.openhab.core.ai.common.validation.ToolValidationResult;
import org.openhab.core.ai.tool.api.Tool;
import org.openhab.core.ai.tool.api.ToolErrorCode;
import org.openhab.core.ai.tool.api.ToolException;
import org.openhab.core.ai.tool.api.ToolMetadata;
import org.openhab.core.ai.tool.api.ToolResult;

/**
 * Test class for the new Tool API classes with Tool* prefix naming convention.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
class ToolAPITest {

    /**
     * Test ToolContext functionality.
     */
    @Test
    void testToolContext() {
        Map<String, Object> values = new HashMap<>();
        values.put("testKey", "testValue");
        values.put("number", 42);
        values.put("boolean", true);

        ToolContext context = new ToolContext("test-context", "test-tool", "Test Tool", "1.0.0", "test-client",
                "test-session", values, null);

        // Test getting properties
        assertEquals("testValue", context.getValue("testKey"));
        assertEquals("testValue", context.getValue("testKey", String.class));

        // Test null property
        assertNull(context.getValue("nonexistentKey"));

        // Test multiple properties
        assertEquals(42, context.getValue("number"));
        assertEquals(42, context.getValue("number", Integer.class));
        assertEquals(true, context.getValue("boolean"));
        assertEquals(true, context.getValue("boolean", Boolean.class));

        // Test context properties
        assertEquals("test-context", context.getContextId());
        assertEquals("test-tool", context.getToolId());
        assertEquals("Test Tool", context.getToolName());
        assertEquals("1.0.0", context.getToolVersion());
        assertEquals("test-client", context.getClientId());
        assertEquals("test-session", context.getSessionId());
    }

    /**
     * Test ToolValidationResult functionality.
     */
    @Test
    void testToolValidationResult() {
        // Test valid result
        ToolValidationResult validResult = ToolValidationResult.valid();
        assertTrue(validResult.isValid());
        assertTrue(validResult.getErrors().isEmpty());

        // Test invalid result
        String errorMessage = "Parameter validation failed";
        ToolValidationResult invalidResult = ToolValidationResult.invalid(List.of(errorMessage));
        assertFalse(invalidResult.isValid());
        assertEquals(List.of(errorMessage), invalidResult.getErrors());
    }

    /**
     * Test ToolResult functionality.
     */
    @Test
    void testToolResult() {
        String toolId = "test-tool";
        long executionTime = 100L;

        // Test success result
        Map<String, Object> content = new HashMap<>();
        content.put("result", "success");
        ToolResult successResult = ToolResult.successJson(toolId, content, executionTime);

        assertEquals(toolId, successResult.getToolId());
        assertTrue(successResult.isSuccess());
        assertEquals(content, successResult.getContent());
        assertNull(successResult.getError());
        assertEquals(executionTime, successResult.getExecutionTimeMs());

        // Test error result
        String errorMessage = "Tool execution failed";
        ToolResult errorResult = ToolResult.error(toolId, errorMessage, executionTime);

        assertEquals(toolId, errorResult.getToolId());
        assertFalse(errorResult.isSuccess());
        assertNull(errorResult.getContent());
        assertEquals(errorMessage, errorResult.getError());
        assertEquals(executionTime, errorResult.getExecutionTimeMs());
    }

    /**
     * Test ToolMetadata functionality.
     */
    @Test
    void testToolMetadata() {
        // Test builder pattern
        ToolMetadata metadata = ToolMetadata.builder().withVersion("2.0.0").withAuthor("Test Author")
                .withDescription("Test tool description").build();

        assertEquals("2.0.0", metadata.getVersion());
        assertEquals("Test Author", metadata.getAuthor());
        assertEquals("Test tool description", metadata.getDescription());

        // Test default values
        ToolMetadata defaultMetadata = ToolMetadata.builder().build();
        assertEquals("1.0.0", defaultMetadata.getVersion());
        assertEquals("Unknown", defaultMetadata.getAuthor());
        assertEquals("No description provided", defaultMetadata.getDescription());
    }

    /**
     * Test ToolException functionality.
     */
    @Test
    void testToolException() {
        String toolId = "test-tool";
        String message = "Test error message";
        ToolErrorCode errorCode = ToolErrorCode.INVALID_PARAMETER;

        // Test exception with message
        ToolException exception = new ToolException(toolId, message, errorCode);
        assertEquals(toolId, exception.getToolId());
        assertEquals(errorCode, exception.getErrorCode());
        assertEquals(message, exception.getMessage());

        // Test exception with cause
        Throwable cause = new RuntimeException("Root cause");
        ToolException exceptionWithCause = new ToolException(toolId, message, cause, errorCode);
        assertEquals(toolId, exceptionWithCause.getToolId());
        assertEquals(errorCode, exceptionWithCause.getErrorCode());
        assertEquals(message, exceptionWithCause.getMessage());
        assertEquals(cause, exceptionWithCause.getCause());
    }

    /**
     * Test Tool interface implementation.
     */
    @Test
    void testToolInterface() {
        // Create a simple test implementation
        Tool testTool = new Tool() {
            @Override
            public String getId() {
                return "test-tool";
            }

            @Override
            public String getName() {
                return "Test Tool";
            }

            @Override
            public String getDescription() {
                return "A test tool implementation";
            }

            @Override
            public Map<String, Object> getInputSchema() {
                Map<String, Object> schema = new HashMap<>();
                schema.put("type", "object");
                schema.put("properties", new HashMap<>());
                return schema;
            }

            @Override
            public Map<String, Object> getOutputSchema() {
                Map<String, Object> schema = new HashMap<>();
                schema.put("type", "object");
                schema.put("properties", new HashMap<>());
                return schema;
            }

            @Override
            public ToolValidationResult validateParameters(Map<String, Object> parameters) {
                return ToolValidationResult.valid();
            }

            @Override
            public ToolResult execute(Map<String, Object> parameters, ToolContext context) throws ToolException {
                return ToolResult.successJson(getId(), Map.of("status", "executed"), 50L);
            }

            @Override
            public ToolMetadata getMetadata() {
                return ToolMetadata.builder().withVersion("1.0.0").withAuthor("Test Author")
                        .withDescription("Test tool").build();
            }
        };

        // Test tool methods
        assertEquals("test-tool", testTool.getId());
        assertEquals("Test Tool", testTool.getName());
        assertEquals("A test tool implementation", testTool.getDescription());

        Map<String, Object> schema = testTool.getInputSchema();
        assertEquals("object", schema.get("type"));

        ToolValidationResult validation = testTool.validateParameters(new HashMap<>());
        assertTrue(validation.isValid());

        ToolContext context = new ToolContext("test-context", "test-tool", "Test Tool", "1.0.0", null, null, null,
                null);
        ToolResult result;
        try {
            result = testTool.execute(new HashMap<>(), context);
            assertTrue(result.isSuccess());
            assertEquals("test-tool", result.getToolId());
        } catch (ToolException e) {
            fail("Tool execution should not throw exception: " + e.getMessage());
        }

        ToolMetadata metadata = testTool.getMetadata();
        assertEquals("1.0.0", metadata.getVersion());
        assertEquals("Test Author", metadata.getAuthor());
    }

    /**
     * Test ToolErrorCode enum values.
     */
    @Test
    void testToolErrorCode() {
        ToolErrorCode[] errorCodes = ToolErrorCode.values();

        assertEquals(6, errorCodes.length);

        // Test specific error codes
        assertNotNull(ToolErrorCode.valueOf("INVALID_PARAMETER"));
        assertNotNull(ToolErrorCode.valueOf("SERVICE_UNAVAILABLE"));
        assertNotNull(ToolErrorCode.valueOf("RESOURCE_NOT_FOUND"));
        assertNotNull(ToolErrorCode.valueOf("ACCESS_DENIED"));
        assertNotNull(ToolErrorCode.valueOf("EXECUTION_ERROR"));
        assertNotNull(ToolErrorCode.valueOf("TIMEOUT"));
    }
}
