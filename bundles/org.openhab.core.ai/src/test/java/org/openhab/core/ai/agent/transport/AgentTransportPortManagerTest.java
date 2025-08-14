package org.openhab.core.ai.agent.transport;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for AgentTransportPortManager.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
class AgentTransportPortManagerTest {

    private AgentTransportPortManager portManager;

    @BeforeEach
    void setUp() {
        portManager = new AgentTransportPortManager();
    }

    @Test
    void testGetDefaultPorts() {
        assertEquals(8080, portManager.getDefaultPort(TransportType.JSON_RPC));
        assertEquals(0, portManager.getDefaultPort(TransportType.REST));
        assertEquals(8083, portManager.getDefaultPort(TransportType.GRPC));
    }

    @Test
    void testPortAvailability() {
        // Test that a random high port is available
        assertTrue(portManager.isPortAvailable(9999));

        // Test that a system port is not available (assuming it's in use)
        // Note: This test might fail if the system port is actually available
        // assertFalse(portManager.isPortAvailable(80));
    }

    @Test
    void testFindAvailablePort() {
        TransportType transportType = TransportType.JSON_RPC;
        int port = portManager.findAvailablePort(transportType);

        assertTrue(port >= 8080);
        assertTrue(port <= 8099);
    }

    @Test
    void testReserveAndReleasePort() {
        TransportType transportType = TransportType.REST;
        int port = 8085;

        // Reserve port
        assertTrue(portManager.reservePort(transportType, port));
        assertTrue(portManager.hasAssignedPort(transportType));
        assertEquals(port, portManager.getAssignedPort(transportType));

        // Try to reserve the same port again
        assertTrue(portManager.reservePort(transportType, port));

        // Release port
        portManager.releasePort(transportType);
        assertFalse(portManager.hasAssignedPort(transportType));
        assertNull(portManager.getAssignedPort(transportType));
    }

    @Test
    void testGetAllAssignedPorts() {
        // Reserve ports for different transport types
        portManager.reservePort(TransportType.JSON_RPC, 8080);
        portManager.reservePort(TransportType.REST, 8082);

        Map<TransportType, Integer> assignedPorts = portManager.getAllAssignedPorts();

        assertEquals(2, assignedPorts.size());
        assertEquals(8080, assignedPorts.get(TransportType.JSON_RPC));
        assertEquals(8082, assignedPorts.get(TransportType.REST));
    }

    @Test
    void testValidateAllPortAssignments() {
        // Reserve valid ports
        portManager.reservePort(TransportType.JSON_RPC, 8080);
        portManager.reservePort(TransportType.REST, 8082);

        assertTrue(portManager.validateAllPortAssignments());
    }

    @Test
    void testGetPortAssignmentStatus() {
        // Reserve a port
        portManager.reservePort(TransportType.GRPC, 8083);

        Map<String, Object> status = portManager.getPortAssignmentStatus();

        assertNotNull(status);
        assertTrue(status.containsKey("grpc"));

        @SuppressWarnings("unchecked")
        Map<String, Object> grpcStatus = (Map<String, Object>) status.get("grpc");
        assertEquals(8083, grpcStatus.get("assignedPort"));
        assertEquals(8083, grpcStatus.get("defaultPort"));
        assertTrue((Boolean) grpcStatus.get("isAvailable"));
        assertTrue((Boolean) grpcStatus.get("isDefaultPort"));
    }

    @Test
    void testResetPortAssignments() {
        // Reserve some ports
        portManager.reservePort(TransportType.JSON_RPC, 8080);
        portManager.reservePort(TransportType.REST, 8082);

        assertEquals(2, portManager.getAllAssignedPorts().size());

        // Reset
        portManager.resetPortAssignments();

        assertEquals(0, portManager.getAllAssignedPorts().size());
    }

    @Test
    void testMultipleTransportTypes() {
        // Test all transport types
        for (TransportType transportType : TransportType.values()) {
            int defaultPort = portManager.getDefaultPort(transportType);
            assertTrue(portManager.reservePort(transportType, defaultPort));
            assertTrue(portManager.hasAssignedPort(transportType));
            assertEquals(defaultPort, portManager.getAssignedPort(transportType));
        }

        assertEquals(3, portManager.getAllAssignedPorts().size());
    }

    @Test
    void testPortConflictHandling() {
        TransportType transportType = TransportType.JSON_RPC;
        int port = 8080;

        // Reserve port for first transport
        assertTrue(portManager.reservePort(transportType, port));

        // Try to reserve same port for different transport type
        TransportType otherType = TransportType.REST;
        assertTrue(portManager.reservePort(otherType, port));

        // Both should have the port assigned
        assertEquals(port, portManager.getAssignedPort(transportType));
        assertEquals(port, portManager.getAssignedPort(otherType));
    }
}
