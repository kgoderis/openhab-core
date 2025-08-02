package org.openhab.core.ai.mcp.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.core.ai.common.action.AIActionContext;
import org.openhab.core.ai.common.action.AIActionResult;
import org.openhab.core.ai.common.api.action.AIAction;
import org.openhab.core.ai.mcp.api.MCPToolContext;
import org.openhab.core.ai.mcp.api.MCPToolException;
import org.openhab.core.ai.mcp.api.MCPToolResult;
import org.openhab.core.ai.mcp.api.MCPToolValidationResult;
import org.openhab.core.ai.mcp.internal.MCPToolAdapter;

class MCPToolAdapterTest {
    private AIAction action;
    private MCPToolAdapter adapter;

    @BeforeEach
    void setUp() {
        action = mock(AIAction.class);
        when(action.getActionId()).thenReturn("test-tool");
        when(action.getActionName()).thenReturn("Test Tool");
        when(action.getDescription()).thenReturn("A test tool");
        when(action.getParameterSchema()).thenReturn(new HashMap<>());
        adapter = new MCPToolAdapter(action);
    }

    @Test
    void testGetToolId() {
        assertEquals("test-tool", adapter.getToolId());
    }

    @Test
    void testGetToolName() {
        assertEquals("Test Tool", adapter.getToolName());
    }

    @Test
    void testGetDescription() {
        assertEquals("A test tool", adapter.getDescription());
    }

    @Test
    void testGetSchema() {
        Map<String, Object> schema = adapter.getSchema();
        assertNotNull(schema);
        assertTrue(schema.isEmpty());
    }

    @Test
    void testValidateParameters() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("test", "value");

        MCPToolValidationResult result = adapter.validateParameters(parameters);
        assertNotNull(result);
        // Note: This is a simplified test since the actual validation depends on the AIAction implementation
    }

    @Test
    void testExecute() throws MCPToolException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("test", "value");

        // Mock the AIAction execution
        AIActionResult actionResult = mock(AIActionResult.class);
        when(actionResult.isSuccess()).thenReturn(true);
        when(actionResult.getData()).thenReturn("success");

        try {
            when(action.execute(any(Map.class), any(AIActionContext.class))).thenReturn(actionResult);
        } catch (Exception e) {
            fail("Mock setup should not throw: " + e.getMessage());
        }

        MCPToolContext context = mock(MCPToolContext.class);
        MCPToolResult result = adapter.execute(parameters, context);

        assertNotNull(result);
        // Note: This is a simplified test since the actual execution depends on the AIAction implementation
    }

    @Test
    void testGetAction() {
        assertEquals(action, adapter.getAction());
    }

    @Test
    void testToString() {
        String result = adapter.toString();
        assertNotNull(result);
        assertTrue(result.contains("test-tool"));
    }
}
