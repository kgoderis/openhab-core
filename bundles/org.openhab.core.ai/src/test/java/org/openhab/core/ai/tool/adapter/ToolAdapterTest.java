/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.core.ai.tool.adapter;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

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
    void testExecuteTool() {
        // Test tool execution
        Map<String, Object> validParams = Map.of("testParam", "testValue");
        Map<String, Object> result = toolAdapter.execute(validParams);

        assertNotNull(result);
        assertTrue((Boolean) result.get("success"));
        assertEquals("test_tool", result.get("toolId"));
        assertNotNull(result.get("content"));
        assertNotNull(result.get("executionTime"));
    }

    @Test
    void testExecuteToolWithInvalidParams() {
        // Test tool execution with invalid parameters
        Map<String, Object> invalidParams = Map.of("invalidParam", "invalidValue");
        Map<String, Object> result = toolAdapter.execute(invalidParams);

        assertNotNull(result);
        assertFalse((Boolean) result.get("success"));
        assertEquals("test_tool", result.get("toolId"));
        assertNotNull(result.get("error"));
    }

    @Test
    void testGetInputSchema() {
        // Test getting input schema
        Map<String, Object> schema = toolAdapter.getInputSchema();

        assertNotNull(schema);
        assertEquals("object", schema.get("type"));
        assertNotNull(schema.get("properties"));
        assertNotNull(schema.get("required"));
    }

    @Test
    void testGetOutputSchema() {
        // Test getting output schema
        Map<String, Object> schema = toolAdapter.getOutputSchema();

        assertNotNull(schema);
        assertEquals("object", schema.get("type"));
        assertNotNull(schema.get("properties"));
    }

    @Test
    void testGetMetadata() {
        // Test getting tool metadata
        ToolMetadata metadata = toolAdapter.getMetadata();

        assertNotNull(metadata);
        assertEquals("1.0.0", metadata.getVersion());
        assertEquals("test", metadata.getAuthor());
        assertEquals("Test tool metadata", metadata.getDescription());
    }

    @Test
    void testConvertToJsonSchemaWithNullSchema() {
        // Test schema conversion with empty schema (null handling is done internally)
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
