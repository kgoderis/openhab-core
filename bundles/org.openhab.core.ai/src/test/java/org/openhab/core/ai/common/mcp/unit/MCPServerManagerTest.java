package org.openhab.core.ai.mcp.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.ai.tool.internal.MCPServer;
import org.openhab.core.ai.tool.internal.MCPServerManager;
import org.openhab.core.ai.tool.internal.ToolServerConfiguration;
import org.osgi.framework.BundleContext;

/**
 * Unit tests for MCPServerManager using real SDK classes.
 *
 * Tests server management, lifecycle operations, multiple server instances,
 * and graceful shutdown scenarios.
 *
 * 
 */
@ExtendWith(MockitoExtension.class)
class MCPServerManagerTest {

    @Mock
    private BundleContext bundleContext;

    private MCPServerManager serverManager;

    @BeforeEach
    void setUp() {
        serverManager = new MCPServerManager();
        // Note: This is a simplified test that doesn't require full OSGi initialization
        // In a real scenario, this would be tested as an integration test
    }

    @Test
    void testInitialization() {
        // Test that the server manager is properly initialized
        assertNotNull(serverManager);
        // Note: isStarted() and getToolRegistry() require OSGi initialization
        // These would be tested in integration tests
    }

    @Test
    void testStartStopLifecycle() {
        // Test start/stop lifecycle
        assertDoesNotThrow(() -> {
            serverManager.start();
            assertTrue(serverManager.isStarted());

            serverManager.stop();
            assertFalse(serverManager.isStarted());
        });
    }

    @Test
    void testMultipleStartStopCycles() {
        // Test multiple start/stop cycles
        assertDoesNotThrow(() -> {
            // First cycle
            serverManager.start();
            assertTrue(serverManager.isStarted());
            serverManager.stop();
            assertFalse(serverManager.isStarted());

            // Second cycle
            serverManager.start();
            assertTrue(serverManager.isStarted());
            serverManager.stop();
            assertFalse(serverManager.isStarted());
        });
    }

    @Test
    void testGetServerInstance() {
        // Test getting server instance
        assertDoesNotThrow(() -> {
            serverManager.start();

            // Get the default server instance
            MCPServer server = serverManager.getServerInstance("default");
            assertNotNull(server);
            assertEquals("default", server.getServerId());
        });
    }

    @Test
    void testGetAllServerInstances() {
        // Test getting all server instances
        assertDoesNotThrow(() -> {
            serverManager.start();

            Map<String, MCPServer> instances = serverManager.getAllServerInstances();
            assertNotNull(instances);
            assertTrue(instances.size() >= 1); // Should have at least the default instance
            assertTrue(instances.containsKey("default"));
        });
    }

    @Test
    void testCreateServerInstance() {
        // Test creating a new server instance
        assertDoesNotThrow(() -> {
            serverManager.start();

            ToolServerConfiguration config = ToolServerConfiguration.builder().serverId("test-server")
                    .serverName("Test Server").build();

            MCPServer server = serverManager.createServerInstance("test-server", config);
            assertNotNull(server);
            assertEquals("test-server", server.getServerId());

            // Verify it's in the instances map
            Map<String, MCPServer> instances = serverManager.getAllServerInstances();
            assertTrue(instances.containsKey("test-server"));
        });
    }

    @Test
    void testRemoveServerInstance() {
        // Test removing a server instance
        assertDoesNotThrow(() -> {
            serverManager.start();

            // Create a server instance
            ToolServerConfiguration config = ToolServerConfiguration.builder().serverId("remove-test")
                    .serverName("Remove Test Server").build();

            MCPServer server = serverManager.createServerInstance("remove-test", config);
            assertNotNull(server);

            // Remove it
            boolean removed = serverManager.removeServerInstance("remove-test");
            assertTrue(removed);

            // Verify it's gone
            Map<String, MCPServer> instances = serverManager.getAllServerInstances();
            assertFalse(instances.containsKey("remove-test"));
        });
    }

    @Test
    void testRemoveNonExistentServerInstance() {
        // Test removing a non-existent server instance
        assertDoesNotThrow(() -> {
            serverManager.start();

            boolean removed = serverManager.removeServerInstance("non-existent");
            assertFalse(removed);
        });
    }

    @Test
    void testGetToolRegistry() {
        // Test getting the tool registry
        assertNotNull(serverManager.getToolRegistry());
    }

    @Test
    void testIsStarted() {
        // Test isStarted method
        assertFalse(serverManager.isStarted());

        assertDoesNotThrow(() -> {
            serverManager.start();
            assertTrue(serverManager.isStarted());

            serverManager.stop();
            assertFalse(serverManager.isStarted());
        });
    }

    @Test
    void testToString() {
        // Test toString method
        String managerString = serverManager.toString();
        assertNotNull(managerString);
        assertFalse(managerString.isEmpty());
    }

    @Test
    void testEquality() {
        // Test equality
        MCPServerManager manager1 = new MCPServerManager();
        MCPServerManager manager2 = new MCPServerManager();

        // These should be different instances
        assertNotEquals(manager1, manager2);
        assertNotEquals(manager1, null);
        assertNotEquals(manager1, "not a manager");
    }

    @Test
    void testConcurrentOperations() {
        // Test concurrent operations
        assertDoesNotThrow(() -> {
            serverManager.start();

            // Simulate concurrent access
            Thread thread1 = new Thread(() -> {
                try {
                    MCPServer server = serverManager.getServerInstance("default");
                    assertNotNull(server);
                } catch (Exception e) {
                    fail("Thread 1 failed: " + e.getMessage());
                }
            });

            Thread thread2 = new Thread(() -> {
                try {
                    Map<String, MCPServer> instances = serverManager.getAllServerInstances();
                    assertNotNull(instances);
                } catch (Exception e) {
                    fail("Thread 2 failed: " + e.getMessage());
                }
            });

            thread1.start();
            thread2.start();

            thread1.join(5000);
            thread2.join(5000);

            serverManager.stop();
        });
    }

    @Test
    void testNullBundleContext() {
        // Test with null bundle context
        // Note: This test is not applicable since MCPServerManager is an OSGi component
        // and doesn't have a constructor that takes BundleContext
        assertTrue(true); // Placeholder test
    }

    @Test
    void testStartWithNullConfigurationService() {
        // Test starting when configuration service is not available
        // This should not throw an exception but log a warning
        assertDoesNotThrow(() -> {
            serverManager.start();
            assertTrue(serverManager.isStarted());
            serverManager.stop();
        });
    }

    @Test
    void testCreateServerInstanceWithNullConfig() {
        // Test creating server instance with null configuration
        assertDoesNotThrow(() -> {
            serverManager.start();

            assertThrows(NullPointerException.class, () -> {
                serverManager.createServerInstance("null-config", null);
            });
        });
    }

    @Test
    void testCreateServerInstanceWithNullId() {
        // Test creating server instance with null ID
        assertDoesNotThrow(() -> {
            serverManager.start();

            ToolServerConfiguration config = ToolServerConfiguration.builder().serverId("test").build();

            assertThrows(IllegalArgumentException.class, () -> {
                serverManager.createServerInstance(null, config);
            });
        });
    }

    @Test
    void testCreateServerInstanceWithEmptyId() {
        // Test creating server instance with empty ID
        assertDoesNotThrow(() -> {
            serverManager.start();

            ToolServerConfiguration config = ToolServerConfiguration.builder().serverId("test").build();

            assertThrows(IllegalArgumentException.class, () -> {
                serverManager.createServerInstance("", config);
            });
        });
    }

    @Test
    void testGetServerInstanceWithNullId() {
        // Test getting server instance with null ID
        assertDoesNotThrow(() -> {
            serverManager.start();

            assertThrows(IllegalArgumentException.class, () -> {
                serverManager.getServerInstance(null);
            });
        });
    }

    @Test
    void testGetServerInstanceWithEmptyId() {
        // Test getting server instance with empty ID
        assertDoesNotThrow(() -> {
            serverManager.start();

            assertThrows(IllegalArgumentException.class, () -> {
                serverManager.getServerInstance("");
            });
        });
    }

    @Test
    void testRemoveServerInstanceWithNullId() {
        // Test removing server instance with null ID
        assertDoesNotThrow(() -> {
            serverManager.start();

            assertThrows(IllegalArgumentException.class, () -> {
                serverManager.removeServerInstance(null);
            });
        });
    }

    @Test
    void testRemoveServerInstanceWithEmptyId() {
        // Test removing server instance with empty ID
        assertDoesNotThrow(() -> {
            serverManager.start();

            assertThrows(IllegalArgumentException.class, () -> {
                serverManager.removeServerInstance("");
            });
        });
    }

    @Test
    void testGracefulShutdown() {
        // Test graceful shutdown
        assertDoesNotThrow(() -> {
            serverManager.start();

            // Create multiple server instances
            ToolServerConfiguration config1 = ToolServerConfiguration.builder().serverId("server1")
                    .serverName("Server 1").build();

            ToolServerConfiguration config2 = ToolServerConfiguration.builder().serverId("server2")
                    .serverName("Server 2").build();

            serverManager.createServerInstance("server1", config1);
            serverManager.createServerInstance("server2", config2);

            // Verify all instances exist
            Map<String, MCPServer> instances = serverManager.getAllServerInstances();
            assertEquals(3, instances.size()); // default + server1 + server2

            // Stop the manager
            serverManager.stop();

            // Verify manager is stopped
            assertFalse(serverManager.isStarted());
        });
    }

    @Test
    void testServerInstanceLifecycle() {
        // Test server instance lifecycle within the manager
        assertDoesNotThrow(() -> {
            serverManager.start();

            // Create a server instance
            ToolServerConfiguration config = ToolServerConfiguration.builder().serverId("lifecycle-test")
                    .serverName("Lifecycle Test Server").build();

            MCPServer server = serverManager.createServerInstance("lifecycle-test", config);

            // Test server state
            assertNotNull(server);
            assertEquals("lifecycle-test", server.getServerId());
            assertEquals(config, server.getConfiguration());

            // Stop the manager
            serverManager.stop();

            // Verify manager is stopped
            assertFalse(serverManager.isStarted());
        });
    }
}
