package org.openhab.core.ai.tool.unit;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.openhab.core.ai.tool.api.Tool;
import org.openhab.core.ai.tool.api.ToolContext;
import org.openhab.core.ai.tool.api.ToolException;
import org.openhab.core.ai.tool.api.ToolMetadata;
import org.openhab.core.ai.tool.api.ToolResult;
import org.openhab.core.ai.tool.validation.api.ToolValidationResult;

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
        ToolContext context = new ToolContext();

        // Test setting and getting properties
        context.setProperty("testKey", "testValue");
        assertEquals("testValue", context.getProperty("testKey"));

        // Test null property
        assertNull(context.getProperty("nonexistentKey"));

        // Test multiple properties
        context.setProperty("number", 42);
        context.setProperty("boolean", true);
        assertEquals(42, context.getProperty("number"));
        assertEquals(true, context.getProperty("boolean"));
    }

    /**
     * Test ToolValidationResult functionality.
     */
    @Test
    void testToolValidationResult() {
        // Test valid result
        ToolValidationResult validResult = ToolValidationResult.valid();
        assertTrue(validResult.isValid());
        assertNull(validResult.getMessage());

        // Test invalid result
        String errorMessage = "Parameter validation failed";
        ToolValidationResult invalidResult = ToolValidationResult.invalid(errorMessage);
        assertFalse(invalidResult.isValid());
        assertEquals(errorMessage, invalidResult.getMessage());
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
        ToolMetadata metadata = ToolMetadata.builder().version("2.0.0").author("Test Author")
                .description("Test tool description").build();

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
        org.openhab.core.ai.tool.api.ToolErrorCode errorCode = org.openhab.core.ai.tool.api.ToolErrorCode.INVALID_PARAMETER;

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
                return ToolMetadata.builder().version("1.0.0").author("Test Author").description("Test tool").build();
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

        ToolContext context = new ToolContext();
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
        org.openhab.core.ai.tool.api.ToolErrorCode[] errorCodes = org.openhab.core.ai.tool.api.ToolErrorCode.values();

        assertEquals(6, errorCodes.length);

        // Test specific error codes
        assertNotNull(org.openhab.core.ai.tool.api.ToolErrorCode.valueOf("INVALID_PARAMETER"));
        assertNotNull(org.openhab.core.ai.tool.api.ToolErrorCode.valueOf("SERVICE_UNAVAILABLE"));
        assertNotNull(org.openhab.core.ai.tool.api.ToolErrorCode.valueOf("RESOURCE_NOT_FOUND"));
        assertNotNull(org.openhab.core.ai.tool.api.ToolErrorCode.valueOf("ACCESS_DENIED"));
        assertNotNull(org.openhab.core.ai.tool.api.ToolErrorCode.valueOf("EXECUTION_ERROR"));
        assertNotNull(org.openhab.core.ai.tool.api.ToolErrorCode.valueOf("TIMEOUT"));
    }
}
