package org.openhab.core.ai.tool.adapter;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.core.ai.tool.api.Tool;
import org.openhab.core.ai.tool.api.ToolContext;
import org.openhab.core.ai.tool.api.ToolException;
import org.openhab.core.ai.tool.api.ToolMetadata;
import org.openhab.core.ai.tool.api.ToolResult;
import org.openhab.core.ai.tool.api.ToolValidationResult;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;

/**
 * Test class for ToolAdapter with MCP Integration.
 * 
 * Tests the enhanced adapter's ability to bridge the internal Tool interface with MCP SDK interfaces,
 * ensuring proper MCP protocol compliance and tool execution.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
class ToolAdapterTest {

    private TestTool testTool;
    private ToolAdapter toolAdapter;

    @BeforeEach
    void setUp() {
        testTool = new TestTool();
        toolAdapter = new ToolAdapter(testTool);
    }

    @Test
    void testToMcpTool() {
        // Test conversion of internal Tool to MCP Tool specification
        McpSchema.Tool mcpTool = toolAdapter.toMcpTool();

        assertNotNull(mcpTool);
        assertEquals("test_tool", mcpTool.name());
        assertEquals("Test Tool for MCP", mcpTool.description());
        assertNotNull(mcpTool.inputSchema());
        assertEquals("object", mcpTool.inputSchema().type());
    }

    @Test
    void testCreateSyncToolSpecification() {
        // Test creation of sync tool specification
        McpServerFeatures.SyncToolSpecification spec = toolAdapter.createSyncToolSpecification();

        assertNotNull(spec);
        assertNotNull(spec.tool());
        assertEquals("test_tool", spec.tool().name());
        assertNotNull(spec.callHandler());
    }

    @Test
    void testCreateAsyncToolSpecification() {
        // Test creation of async tool specification
        McpServerFeatures.AsyncToolSpecification spec = toolAdapter.createAsyncToolSpecification();

        assertNotNull(spec);
        assertNotNull(spec.tool());
        assertEquals("test_tool", spec.tool().name());
        assertNotNull(spec.callHandler());
    }

    @Test
    void testValidateParameters() {
        // Test parameter validation
        Map<String, Object> validParams = Map.of("testParam", "testValue");
        boolean isValid = toolAdapter.validateParameters(validParams);

        assertTrue(isValid);
    }

    @Test
    void testValidateParametersWithInvalidParams() {
        // Test parameter validation with invalid parameters
        Map<String, Object> invalidParams = Map.of("invalidParam", "invalidValue");
        boolean isValid = toolAdapter.validateParameters(invalidParams);

        assertFalse(isValid);
    }

    @Test
    void testConvertToJsonSchema() {
        // Test schema conversion with valid schema
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");
        schema.put("properties", Map.of("testProp", Map.of("type", "string")));
        schema.put("required", java.util.List.of("testProp"));

        McpSchema.JsonSchema jsonSchema = toolAdapter.toMcpTool().inputSchema();

        assertNotNull(jsonSchema);
        assertEquals("object", jsonSchema.type());
        assertNotNull(jsonSchema.properties());
    }

    @Test
    void testConvertToJsonSchemaWithNullSchema() {
        // Test schema conversion with null schema
        Tool nullSchemaTool = new TestTool() {
            @Override
            public Map<String, Object> getInputSchema() {
                return null;
            }
        };

        ToolAdapter nullAdapter = new ToolAdapter(nullSchemaTool);
        McpSchema.Tool mcpTool = nullAdapter.toMcpTool();
        assertNotNull(mcpTool);
        assertNotNull(mcpTool.inputSchema());
        assertEquals("object", mcpTool.inputSchema().type());
    }

    @Test
    void testConvertToJsonSchemaWithEmptySchema() {
        // Test schema conversion with empty schema
        Tool emptySchemaTool = new TestTool() {
            @Override
            public Map<String, Object> getInputSchema() {
                return Map.of();
            }
        };

        ToolAdapter emptyAdapter = new ToolAdapter(emptySchemaTool);
        McpSchema.Tool mcpTool = emptyAdapter.toMcpTool();
        assertNotNull(mcpTool);
        assertNotNull(mcpTool.inputSchema());
        assertEquals("object", mcpTool.inputSchema().type());
    }

    /**
     * Test implementation of Tool interface for testing purposes.
     */
    private static class TestTool implements Tool {

        @Override
        public String getId() {
            return "test_tool";
        }

        @Override
        public String getName() {
            return "test_tool";
        }

        @Override
        public String getDescription() {
            return "Test Tool for MCP";
        }

        @Override
        public Map<String, Object> getInputSchema() {
            Map<String, Object> schema = new HashMap<>();
            schema.put("type", "object");
            schema.put("properties", Map.of("testParam", Map.of("type", "string", "description", "Test parameter")));
            schema.put("required", java.util.List.of("testParam"));
            return schema;
        }

        @Override
        public Map<String, Object> getOutputSchema() {
            Map<String, Object> schema = new HashMap<>();
            schema.put("type", "object");
            schema.put("properties", Map.of("result", Map.of("type", "string", "description", "Test result")));
            return schema;
        }

        @Override
        public ToolValidationResult validateParameters(Map<String, Object> parameters) {
            if (parameters.containsKey("testParam")) {
                return ToolValidationResult.valid();
            } else {
                return ToolValidationResult.invalid("testParam is required");
            }
        }

        @Override
        public ToolResult execute(Map<String, Object> parameters, ToolContext context) throws ToolException {
            if (parameters.containsKey("testParam")) {
                return ToolResult.successJson(getId(), Map.of("result", "success"), 100);
            } else {
                throw new ToolException(getId(), "testParam is required",
                        ToolException.ToolErrorCode.INVALID_PARAMETER);
            }
        }

        @Override
        public ToolMetadata getMetadata() {
            return ToolMetadata.builder().version("1.0.0").author("test").description("Test tool metadata").build();
        }
    }
}
