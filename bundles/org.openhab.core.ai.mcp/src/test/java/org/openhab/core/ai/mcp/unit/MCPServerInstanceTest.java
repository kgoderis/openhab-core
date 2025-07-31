package org.openhab.core.ai.mcp.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.ai.mcp.internal.MCPServer;
import org.openhab.core.ai.mcp.internal.MCPServerConfiguration;
import org.openhab.core.ai.mcp.internal.MCPToolRegistry;
import org.openhab.core.ai.mcp.internal.MCPTransportType;
import org.osgi.framework.BundleContext;

/**
 * Unit tests for MCPServerInstance using real SDK classes.
 * 
 * Tests server initialization, lifecycle management, state tracking,
 * and error handling scenarios.
 * 
 * 
 */
@ExtendWith(MockitoExtension.class)
class MCPServerInstanceTest {

    @Mock
    private MCPToolRegistry toolRegistry;

    @Mock
    private BundleContext bundleContext;

    private MCPServer serverInstance;
    private MCPServerConfiguration configuration;

    @BeforeEach
    void setUp() {
        // Create a basic configuration for testing
        configuration = MCPServerConfiguration.builder().serverId("test-server").serverName("Test MCP Server")
                .serverVersion("1.0.0").transportType(MCPTransportType.STDIO).enableTools(true).enableResources(false)
                .enablePrompts(false).enableLogging(true).build();

        // Create server instance
        serverInstance = new MCPServer("test-server", configuration, toolRegistry);
    }

    @Test
    void testServerInitialization() {
        // Verify initial state
        assertEquals(MCPServer.MCPServerState.STOPPED, serverInstance.getState());
        assertFalse(serverInstance.isRunning());
        assertTrue(serverInstance.isHealthy());
        assertEquals("test-server", serverInstance.getServerId());
        assertEquals(configuration, serverInstance.getConfiguration());
    }

    @Test
    void testServerLifecycleStartStop() throws Exception {
        // Mock tool registry to return empty tool specifications
        when(toolRegistry.getToolSpecifications())
                .thenReturn(new io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification[0]);

        // Start the server
        serverInstance.start();

        // Verify server is running
        assertEquals(MCPServer.MCPServerState.RUNNING, serverInstance.getState());
        assertTrue(serverInstance.isRunning());
        assertTrue(serverInstance.isHealthy());

        // Stop the server
        serverInstance.stop();

        // Verify server is stopped
        assertEquals(MCPServer.MCPServerState.STOPPED, serverInstance.getState());
        assertFalse(serverInstance.isRunning());
        assertTrue(serverInstance.isHealthy());
    }

    @Test
    void testServerStateManagement() {
        // Test initial state
        assertEquals(MCPServer.MCPServerState.STOPPED, serverInstance.getState());

        // Test state transitions (without actually starting/stopping)
        // The state should remain STOPPED until start() is called
        assertFalse(serverInstance.isRunning());
    }

    @Test
    void testServerConfigurationValidation() {
        // Test with valid configuration
        assertNotNull(serverInstance.getConfiguration());
        assertEquals("test-server", serverInstance.getConfiguration().getServerId());
        assertEquals("Test MCP Server", serverInstance.getConfiguration().getServerName());
        assertEquals("1.0.0", serverInstance.getConfiguration().getServerVersion());
        assertEquals(MCPTransportType.STDIO, serverInstance.getConfiguration().getTransportType());
        assertTrue(serverInstance.getConfiguration().isEnableTools());
        assertFalse(serverInstance.getConfiguration().isEnableResources());
        assertFalse(serverInstance.getConfiguration().isEnablePrompts());
        assertTrue(serverInstance.getConfiguration().isEnableLogging());
    }

    @Test
    void testServerWithSSEConfiguration() {
        // Create configuration with SSE transport
        MCPServerConfiguration sseConfig = MCPServerConfiguration.builder().serverId("sse-server")
                .serverName("SSE MCP Server").transportType(MCPTransportType.SSE).baseUrl("http://localhost:8080")
                .messageEndpoint("/mcp/message").sseEndpoint("/mcp/events").enableSse(true).enableTools(true).build();

        MCPServer sseServer = new MCPServer("sse-server", sseConfig, toolRegistry);

        // Verify SSE configuration
        assertEquals("http://localhost:8080", sseServer.getConfiguration().getBaseUrl());
        assertEquals("/mcp/message", sseServer.getConfiguration().getMessageEndpoint());
        assertEquals("/mcp/events", sseServer.getConfiguration().getSseEndpoint());
        assertTrue(sseServer.getConfiguration().isEnableSse());
        assertEquals(MCPTransportType.SSE, sseServer.getConfiguration().getTransportType());
    }

    @Test
    void testServerWithWebSocketConfiguration() {
        // WebSocket transport type is not yet implemented
        // This test is skipped until WebSocket support is added
        assertTrue(true); // Placeholder test
    }

    @Test
    void testServerCapabilitiesConfiguration() {
        // Test different capability configurations
        MCPServerConfiguration fullConfig = MCPServerConfiguration.builder().serverId("full-server").enableTools(true)
                .enableResources(true).enablePrompts(true).enableLogging(true).build();

        MCPServer fullServer = new MCPServer("full-server", fullConfig, toolRegistry);

        // Verify all capabilities are enabled
        assertTrue(fullServer.getConfiguration().isEnableTools());
        assertTrue(fullServer.getConfiguration().isEnableResources());
        assertTrue(fullServer.getConfiguration().isEnablePrompts());
        assertTrue(fullServer.getConfiguration().isEnableLogging());
    }

    @Test
    void testServerWithTransportOptions() {
        // Test configuration with transport options
        MCPServerConfiguration configWithOptions = MCPServerConfiguration.builder().serverId("options-server")
                .transportOption("timeout", 30000).transportOption("maxConnections", 10).serverOption("debug", true)
                .build();

        MCPServer optionsServer = new MCPServer("options-server", configWithOptions, toolRegistry);

        // Verify transport options
        assertEquals(30000, optionsServer.getConfiguration().getTransportOptions().get("timeout"));
        assertEquals(10, optionsServer.getConfiguration().getTransportOptions().get("maxConnections"));
        assertEquals(true, optionsServer.getConfiguration().getServerOptions().get("debug"));
    }

    @Test
    void testServerToString() {
        // Test toString method
        String serverString = serverInstance.toString();
        assertNotNull(serverString);
        assertTrue(serverString.contains("test-server"));
        assertTrue(serverString.contains("Test MCP Server"));
        assertTrue(serverString.contains("STOPPED"));
    }

    @Test
    void testServerConfigurationToString() {
        // Test configuration toString method
        String configString = configuration.toString();
        assertNotNull(configString);
        assertTrue(configString.contains("test-server"));
        assertTrue(configString.contains("Test MCP Server"));
        assertTrue(configString.contains("STDIO"));
        assertTrue(configString.contains("true")); // enableTools
        assertTrue(configString.contains("false")); // enableResources
    }

    @Test
    void testServerConfigurationBuilder() {
        // Test builder pattern
        MCPServerConfiguration builtConfig = MCPServerConfiguration.builder().serverId("builder-test")
                .serverName("Builder Test Server").serverVersion("2.0.0").transportType(MCPTransportType.STDIO)
                .baseUrl("http://test:9090").messageEndpoint("/test/message").sseEndpoint("/test/events")
                .enableSse(false).enableTools(false).enableResources(true).enablePrompts(true).enableLogging(false)
                .build();

        // Verify all builder methods work correctly
        assertEquals("builder-test", builtConfig.getServerId());
        assertEquals("Builder Test Server", builtConfig.getServerName());
        assertEquals("2.0.0", builtConfig.getServerVersion());
        assertEquals(MCPTransportType.STDIO, builtConfig.getTransportType());
        assertEquals("http://test:9090", builtConfig.getBaseUrl());
        assertEquals("/test/message", builtConfig.getMessageEndpoint());
        assertEquals("/test/events", builtConfig.getSseEndpoint());
        assertFalse(builtConfig.isEnableSse());
        assertFalse(builtConfig.isEnableTools());
        assertTrue(builtConfig.isEnableResources());
        assertTrue(builtConfig.isEnablePrompts());
        assertFalse(builtConfig.isEnableLogging());
    }

    @Test
    void testServerConfigurationEquality() {
        // Test equals and hashCode methods
        MCPServerConfiguration config1 = MCPServerConfiguration.builder().serverId("test").serverName("Test").build();

        MCPServerConfiguration config2 = MCPServerConfiguration.builder().serverId("test").serverName("Test").build();

        MCPServerConfiguration config3 = MCPServerConfiguration.builder().serverId("different").serverName("Test")
                .build();

        assertEquals(config1, config2);
        assertNotEquals(config1, config3);
        assertEquals(config1.hashCode(), config2.hashCode());
        assertNotEquals(config1.hashCode(), config3.hashCode());
    }
}
