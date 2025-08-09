package org.openhab.core.ai.agent.transport;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for SharedSseManager.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
class SharedSseManagerTest {

    private SharedSseManager sseManager;

    @BeforeEach
    void setUp() {
        sseManager = new SharedSseManager();
    }

    @Test
    void testCreateMcpConnection() {
        String clientId = "test-mcp-client";
        SharedSseManager.SseConnection connection = sseManager.createMcpConnection(clientId);

        assertNotNull(connection);
        assertEquals("mcp", connection.getProtocol());
        assertEquals(clientId, connection.getClientId());
        assertTrue(connection.isActive());
        assertTrue(connection.getCreatedAt() > 0);
        assertTrue(connection.getLastActivity() > 0);
    }

    @Test
    void testCreateA2aConnection() {
        String clientId = "test-a2a-client";
        SharedSseManager.SseConnection connection = sseManager.createA2aConnection(clientId);

        assertNotNull(connection);
        assertEquals("a2a", connection.getProtocol());
        assertEquals(clientId, connection.getClientId());
        assertTrue(connection.isActive());
        assertTrue(connection.getCreatedAt() > 0);
        assertTrue(connection.getLastActivity() > 0);
    }

    @Test
    void testCloseConnection() {
        String clientId = "test-client";
        SharedSseManager.SseConnection connection = sseManager.createMcpConnection(clientId);
        String connectionId = connection.getConnectionId();

        // Close connection
        sseManager.closeConnection(connectionId, "mcp");

        // Connection should be inactive
        assertFalse(connection.isActive());
    }

    @Test
    void testBroadcastEvent() {
        String clientId = "test-client";
        SharedSseManager.SseConnection connection = sseManager.createMcpConnection(clientId);

        SharedSseManager.SseEvent event = new SharedSseManager.SseEvent("test", "test-data", "test-id");

        // Broadcast event
        sseManager.broadcastEvent("mcp", event);

        // Connection should have updated activity
        assertTrue(connection.getLastActivity() > connection.getCreatedAt());
    }

    @Test
    void testSendEventToConnection() {
        String clientId = "test-client";
        SharedSseManager.SseConnection connection = sseManager.createMcpConnection(clientId);
        String connectionId = connection.getConnectionId();

        SharedSseManager.SseEvent event = new SharedSseManager.SseEvent("test", "test-data", "test-id");

        // Send event to specific connection
        boolean success = sseManager.sendEventToConnection(connectionId, "mcp", event);

        assertTrue(success);
        assertTrue(connection.getLastActivity() > connection.getCreatedAt());
    }

    @Test
    void testSendEventToNonExistentConnection() {
        SharedSseManager.SseEvent event = new SharedSseManager.SseEvent("test", "test-data", "test-id");

        // Send event to non-existent connection
        boolean success = sseManager.sendEventToConnection("non-existent", "mcp", event);

        assertFalse(success);
    }

    @Test
    void testGetActiveConnections() {
        String clientId1 = "test-client-1";
        String clientId2 = "test-client-2";

        sseManager.createMcpConnection(clientId1);
        sseManager.createMcpConnection(clientId2);

        Map<String, SharedSseManager.SseConnection> activeConnections = sseManager.getActiveConnections("mcp");

        assertEquals(2, activeConnections.size());
    }

    @Test
    void testGetConnectionStatistics() {
        String clientId = "test-client";
        sseManager.createMcpConnection(clientId);
        sseManager.createA2aConnection(clientId);

        Map<String, Object> stats = sseManager.getConnectionStatistics();

        assertNotNull(stats);
        assertTrue(stats.containsKey("mcp"));
        assertTrue(stats.containsKey("a2a"));

        @SuppressWarnings("unchecked")
        Map<String, Object> mcpStats = (Map<String, Object>) stats.get("mcp");
        assertEquals(1L, mcpStats.get("activeConnections"));
        assertEquals(1L, mcpStats.get("totalConnections"));
        assertEquals("/mcp/events", mcpStats.get("endpoint"));

        @SuppressWarnings("unchecked")
        Map<String, Object> a2aStats = (Map<String, Object>) stats.get("a2a");
        assertEquals(1L, a2aStats.get("activeConnections"));
        assertEquals(1L, a2aStats.get("totalConnections"));
        assertEquals("/a2a/events", a2aStats.get("endpoint"));
    }

    @Test
    void testCleanupInactiveConnections() {
        String clientId = "test-client";
        SharedSseManager.SseConnection connection = sseManager.createMcpConnection(clientId);

        // Deactivate connection
        connection.setActive(false);

        // Clean up inactive connections
        sseManager.cleanupInactiveConnections(1000);

        Map<String, SharedSseManager.SseConnection> activeConnections = sseManager.getActiveConnections("mcp");
        assertEquals(0, activeConnections.size());
    }

    @Test
    void testGetEventPaths() {
        assertEquals("/mcp/events", sseManager.getMcpEventsPath());
        assertEquals("/a2a/events", sseManager.getA2aEventsPath());
    }

    @Test
    void testSseEventCreation() {
        String eventType = "test-event";
        String data = "test-data";
        String id = "test-id";

        SharedSseManager.SseEvent event = new SharedSseManager.SseEvent(eventType, data, id);

        assertEquals(eventType, event.getEventType());
        assertEquals(data, event.getData());
        assertEquals(id, event.getId());
        assertTrue(event.getTimestamp() > 0);
    }

    @Test
    void testSseEventFormat() {
        String eventType = "test-event";
        String data = "test-data";
        String id = "test-id";

        SharedSseManager.SseEvent event = new SharedSseManager.SseEvent(eventType, data, id);
        String sseFormat = event.toSseFormat();

        assertTrue(sseFormat.contains("id: " + id));
        assertTrue(sseFormat.contains("event: " + eventType));
        assertTrue(sseFormat.contains("data: " + data));
        assertTrue(sseFormat.endsWith("\n\n"));
    }

    @Test
    void testSseEventWithoutId() {
        String eventType = "test-event";
        String data = "test-data";

        SharedSseManager.SseEvent event = new SharedSseManager.SseEvent(eventType, data, null);
        String sseFormat = event.toSseFormat();

        assertFalse(sseFormat.contains("id:"));
        assertTrue(sseFormat.contains("event: " + eventType));
        assertTrue(sseFormat.contains("data: " + data));
    }

    @Test
    void testSseEventWithoutEventType() {
        String data = "test-data";
        String id = "test-id";

        SharedSseManager.SseEvent event = new SharedSseManager.SseEvent(null, data, id);
        String sseFormat = event.toSseFormat();

        assertTrue(sseFormat.contains("id: " + id));
        assertFalse(sseFormat.contains("event:"));
        assertTrue(sseFormat.contains("data: " + data));
    }

    @Test
    void testConnectionActivityUpdate() {
        String clientId = "test-client";
        SharedSseManager.SseConnection connection = sseManager.createMcpConnection(clientId);

        long initialActivity = connection.getLastActivity();

        // Wait a bit
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Update activity
        connection.updateActivity();

        assertTrue(connection.getLastActivity() > initialActivity);
    }

    @Test
    void testMultipleProtocolConnections() {
        String mcpClientId = "mcp-client";
        String a2aClientId = "a2a-client";

        SharedSseManager.SseConnection mcpConnection = sseManager.createMcpConnection(mcpClientId);
        SharedSseManager.SseConnection a2aConnection = sseManager.createA2aConnection(a2aClientId);

        assertEquals("mcp", mcpConnection.getProtocol());
        assertEquals("a2a", a2aConnection.getProtocol());

        Map<String, SharedSseManager.SseConnection> mcpConnections = sseManager.getActiveConnections("mcp");
        Map<String, SharedSseManager.SseConnection> a2aConnections = sseManager.getActiveConnections("a2a");

        assertEquals(1, mcpConnections.size());
        assertEquals(1, a2aConnections.size());
    }
}
