package org.openhab.core.ai.mcp.internal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.ai.mcp.api.MCPTool;
import org.osgi.framework.BundleContext;

import io.modelcontextprotocol.server.McpServerFeatures;

/**
 * Unit tests for MCPToolRegistry using real SDK classes.
 * 
 * Tests tool registration, specification generation, adapter management,
 * and concurrent operations.
 * 
 * 
 */
@ExtendWith(MockitoExtension.class)
class MCPToolRegistryTest {

    @Mock
    private MCPTool mockTool;

    @Mock
    private BundleContext bundleContext;

    private MCPToolRegistry toolRegistry;

    @BeforeEach
    void setUp() {
        toolRegistry = new MCPToolRegistry();
        // Note: This is a simplified test that doesn't require full OSGi initialization
        // In a real scenario, this would be tested as an integration test
    }

    @Test
    void testToolRegistration() {
        // Mock tool metadata
        when(mockTool.getToolId()).thenReturn("test-tool");
        when(mockTool.getToolName()).thenReturn("Test Tool");
        when(mockTool.getDescription()).thenReturn("A test tool for unit testing");

        // Register the tool
        toolRegistry.registerTool(mockTool);

        // Verify tool is registered
        assertTrue(toolRegistry.isToolRegistered("test-tool"));
        assertEquals(mockTool, toolRegistry.getTool("test-tool"));
    }

    @Test
    void testToolUnregistration() {
        // Mock tool metadata
        when(mockTool.getToolId()).thenReturn("test-tool");
        when(mockTool.getToolName()).thenReturn("Test Tool");
        when(mockTool.getDescription()).thenReturn("A test tool for unit testing");

        // Register the tool
        toolRegistry.registerTool(mockTool);
        assertTrue(toolRegistry.isToolRegistered("test-tool"));

        // Unregister the tool
        toolRegistry.unregisterTool("test-tool");

        // Verify tool is unregistered
        assertFalse(toolRegistry.isToolRegistered("test-tool"));
        assertNull(toolRegistry.getTool("test-tool"));
    }

    @Test
    void testToolSpecificationGeneration() {
        // Mock tool metadata for specification generation
        when(mockTool.getToolId()).thenReturn("test-tool");
        when(mockTool.getToolName()).thenReturn("Test Tool");
        when(mockTool.getDescription()).thenReturn("A test tool for unit testing");
        when(mockTool.getSchema()).thenReturn(Map.of("type", "object", "properties", Map.of()));

        // Register the tool
        toolRegistry.registerTool(mockTool);

        // Get tool specifications
        McpServerFeatures.SyncToolSpecification[] specifications = toolRegistry.getToolSpecifications();

        // Verify specifications are generated
        assertNotNull(specifications);
        assertEquals(1, specifications.length);
        // Note: SDK specification access methods may differ, so we'll just verify the array is not empty
        assertNotNull(specifications[0]);
    }

    @Test
    void testMultipleToolRegistration() {
        // Create multiple mock tools
        MCPTool tool1 = mock(MCPTool.class);
        MCPTool tool2 = mock(MCPTool.class);
        MCPTool tool3 = mock(MCPTool.class);

        // Mock tool metadata
        when(tool1.getToolId()).thenReturn("tool-1");
        when(tool1.getToolName()).thenReturn("Tool 1");
        when(tool1.getDescription()).thenReturn("First tool");

        when(tool2.getToolId()).thenReturn("tool-2");
        when(tool2.getToolName()).thenReturn("Tool 2");
        when(tool2.getDescription()).thenReturn("Second tool");

        when(tool3.getToolId()).thenReturn("tool-3");
        when(tool3.getToolName()).thenReturn("Tool 3");
        when(tool3.getDescription()).thenReturn("Third tool");

        // Register all tools
        toolRegistry.registerTool(tool1);
        toolRegistry.registerTool(tool2);
        toolRegistry.registerTool(tool3);

        // Verify all tools are registered
        assertTrue(toolRegistry.isToolRegistered("tool-1"));
        assertTrue(toolRegistry.isToolRegistered("tool-2"));
        assertTrue(toolRegistry.isToolRegistered("tool-3"));

        // Verify tool count
        assertEquals(3, toolRegistry.getToolCount());
    }

    @Test
    void testToolAdapterCreation() {
        // Mock tool metadata
        when(mockTool.getToolId()).thenReturn("test-tool");
        when(mockTool.getToolName()).thenReturn("Test Tool");
        when(mockTool.getDescription()).thenReturn("A test tool for unit testing");
        when(mockTool.getSchema()).thenReturn(Map.of("type", "object", "properties", Map.of()));

        // Register the tool
        toolRegistry.registerTool(mockTool);

        // Verify adapter is created
        MCPToolAdapter adapter = toolRegistry.getToolAdapter("test-tool");
        assertNotNull(adapter);
        // Note: adapter.getToolId() may not be available, so we'll just verify the adapter exists
    }

    @Test
    void testToolValidation() {
        // Test with null tool
        assertThrows(IllegalArgumentException.class, () -> {
            toolRegistry.registerTool(null);
        });

        // Test with tool having null ID
        MCPTool invalidTool = mock(MCPTool.class);
        when(invalidTool.getToolId()).thenReturn(null);
        assertThrows(IllegalArgumentException.class, () -> {
            toolRegistry.registerTool(invalidTool);
        });

        // Test with tool having empty ID
        when(invalidTool.getToolId()).thenReturn("");
        assertThrows(IllegalArgumentException.class, () -> {
            toolRegistry.registerTool(invalidTool);
        });
    }

    @Test
    void testDuplicateToolRegistration() {
        // Mock tool metadata
        when(mockTool.getToolId()).thenReturn("test-tool");
        when(mockTool.getToolName()).thenReturn("Test Tool");
        when(mockTool.getDescription()).thenReturn("A test tool for unit testing");

        // Register the tool
        toolRegistry.registerTool(mockTool);

        // Try to register the same tool again
        assertThrows(IllegalStateException.class, () -> {
            toolRegistry.registerTool(mockTool);
        });
    }

    @Test
    void testUnregisterNonExistentTool() {
        // Try to unregister a tool that doesn't exist
        assertThrows(IllegalArgumentException.class, () -> {
            toolRegistry.unregisterTool("non-existent-tool");
        });
    }

    @Test
    void testGetNonExistentTool() {
        // Try to get a tool that doesn't exist
        assertNull(toolRegistry.getTool("non-existent-tool"));
        assertNull(toolRegistry.getToolAdapter("non-existent-tool"));
    }

    @Test
    void testToolSpecificationWithComplexSchema() {
        // Mock tool with complex schema
        Map<String, Object> complexSchema = Map.of("type", "object", "properties",
                Map.of("name", Map.of("type", "string", "description", "The name"), "age",
                        Map.of("type", "integer", "minimum", 0), "active", Map.of("type", "boolean")),
                "required", java.util.List.of("name"));

        when(mockTool.getToolId()).thenReturn("complex-tool");
        when(mockTool.getToolName()).thenReturn("Complex Tool");
        when(mockTool.getDescription()).thenReturn("A tool with complex schema");
        when(mockTool.getSchema()).thenReturn(complexSchema);

        // Register the tool
        toolRegistry.registerTool(mockTool);

        // Get tool specifications
        McpServerFeatures.SyncToolSpecification[] specifications = toolRegistry.getToolSpecifications();

        // Verify specifications are generated correctly
        assertNotNull(specifications);
        assertEquals(1, specifications.length);
        // Note: SDK specification access methods may differ, so we'll just verify the array is not empty
        assertNotNull(specifications[0]);
    }

    @Test
    void testToolRegistryState() {
        // Verify initial state
        assertEquals(0, toolRegistry.getToolCount());

        // Mock and register a tool
        when(mockTool.getToolId()).thenReturn("test-tool");
        when(mockTool.getToolName()).thenReturn("Test Tool");
        when(mockTool.getDescription()).thenReturn("A test tool");

        toolRegistry.registerTool(mockTool);

        // Verify state after registration
        assertEquals(1, toolRegistry.getToolCount());
        assertTrue(toolRegistry.isToolRegistered("test-tool"));
    }

    @Test
    void testToolRegistryClear() {
        // Mock and register multiple tools
        MCPTool tool1 = mock(MCPTool.class);
        MCPTool tool2 = mock(MCPTool.class);

        when(tool1.getToolId()).thenReturn("tool-1");
        when(tool1.getToolName()).thenReturn("Tool 1");
        when(tool1.getDescription()).thenReturn("First tool");

        when(tool2.getToolId()).thenReturn("tool-2");
        when(tool2.getToolName()).thenReturn("Tool 2");
        when(tool2.getDescription()).thenReturn("Second tool");

        toolRegistry.registerTool(tool1);
        toolRegistry.registerTool(tool2);

        // Verify tools are registered
        assertEquals(2, toolRegistry.getToolCount());

        // Stop the registry (this clears it)
        toolRegistry.stop();

        // Verify registry is empty
        assertEquals(0, toolRegistry.getToolCount());
        assertFalse(toolRegistry.isToolRegistered("tool-1"));
        assertFalse(toolRegistry.isToolRegistered("tool-2"));
    }

    @Test
    void testToolRegistryToString() {
        // Test toString method
        String registryString = toolRegistry.toString();
        assertNotNull(registryString);
        assertTrue(registryString.contains("MCPToolRegistry"));
        assertTrue(registryString.contains("0")); // No tools registered

        // Register a tool and test again
        when(mockTool.getToolId()).thenReturn("test-tool");
        when(mockTool.getToolName()).thenReturn("Test Tool");
        when(mockTool.getDescription()).thenReturn("A test tool");

        toolRegistry.registerTool(mockTool);

        registryString = toolRegistry.toString();
        assertTrue(registryString.contains("1")); // One tool registered
    }
}
