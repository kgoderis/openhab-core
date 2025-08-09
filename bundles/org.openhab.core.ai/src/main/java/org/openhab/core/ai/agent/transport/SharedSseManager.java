package org.openhab.core.ai.agent.transport;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Shared SSE Manager for A2A and MCP Protocols.
 * 
 * <p>
 * This class provides shared Server-Sent Events (SSE) infrastructure for both
 * MCP and A2A protocols, managing separate SSE endpoints and connection
 * lifecycle.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@Component(service = SharedSseManager.class)
@NonNullByDefault
public class SharedSseManager {

    private static final Logger logger = LoggerFactory.getLogger(SharedSseManager.class);

    // SSE endpoint paths
    private static final String MCP_EVENTS_PATH = "/mcp/events";
    private static final String A2A_EVENTS_PATH = "/a2a/events";

    // Connection management
    private final Map<String, SseConnection> mcpConnections = new ConcurrentHashMap<>();
    private final Map<String, SseConnection> a2aConnections = new ConcurrentHashMap<>();
    private final AtomicLong connectionIdCounter = new AtomicLong(0);

    /**
     * SSE Connection representation.
     */
    public static class SseConnection {
        private final String connectionId;
        private final String protocol;
        private final String clientId;
        private final long createdAt;
        private volatile boolean isActive;
        private volatile long lastActivity;

        public SseConnection(String connectionId, String protocol, String clientId) {
            this.connectionId = connectionId;
            this.protocol = protocol;
            this.clientId = clientId;
            this.createdAt = System.currentTimeMillis();
            this.lastActivity = this.createdAt;
            this.isActive = true;
        }

        public String getConnectionId() {
            return connectionId;
        }

        public String getProtocol() {
            return protocol;
        }

        public String getClientId() {
            return clientId;
        }

        public long getCreatedAt() {
            return createdAt;
        }

        public boolean isActive() {
            return isActive;
        }

        public void setActive(boolean active) {
            this.isActive = active;
        }

        public long getLastActivity() {
            return lastActivity;
        }

        public void updateActivity() {
            this.lastActivity = System.currentTimeMillis();
        }
    }

    /**
     * SSE Event representation.
     */
    public static class SseEvent {
        private final String eventType;
        private final String data;
        private final String id;
        private final long timestamp;

        public SseEvent(String eventType, String data, String id) {
            this.eventType = eventType;
            this.data = data;
            this.id = id;
            this.timestamp = System.currentTimeMillis();
        }

        public String getEventType() {
            return eventType;
        }

        public String getData() {
            return data;
        }

        public String getId() {
            return id;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public String toSseFormat() {
            StringBuilder sb = new StringBuilder();
            if (id != null) {
                sb.append("id: ").append(id).append("\n");
            }
            if (eventType != null) {
                sb.append("event: ").append(eventType).append("\n");
            }
            sb.append("data: ").append(data).append("\n\n");
            return sb.toString();
        }
    }

    /**
     * Create a new SSE connection for MCP.
     * 
     * @param clientId the client identifier
     * @return the SSE connection
     */
    public SseConnection createMcpConnection(String clientId) {
        String connectionId = generateConnectionId();
        SseConnection connection = new SseConnection(connectionId, "mcp", clientId);
        mcpConnections.put(connectionId, connection);
        logger.info("Created MCP SSE connection: {} for client: {}", connectionId, clientId);
        return connection;
    }

    /**
     * Create a new SSE connection for A2A.
     * 
     * @param clientId the client identifier
     * @return the SSE connection
     */
    public SseConnection createA2aConnection(String clientId) {
        String connectionId = generateConnectionId();
        SseConnection connection = new SseConnection(connectionId, "a2a", clientId);
        a2aConnections.put(connectionId, connection);
        logger.info("Created A2A SSE connection: {} for client: {}", connectionId, clientId);
        return connection;
    }

    /**
     * Close an SSE connection.
     * 
     * @param connectionId the connection identifier
     * @param protocol the protocol (mcp or a2a)
     */
    public void closeConnection(String connectionId, String protocol) {
        Map<String, SseConnection> connections = getConnectionsForProtocol(protocol);
        SseConnection connection = connections.remove(connectionId);
        if (connection != null) {
            connection.setActive(false);
            logger.info("Closed {} SSE connection: {}", protocol, connectionId);
        }
    }

    /**
     * Send an event to all active connections for a protocol.
     * 
     * @param protocol the protocol (mcp or a2a)
     * @param event the SSE event to send
     */
    public void broadcastEvent(String protocol, SseEvent event) {
        Map<String, SseConnection> connections = getConnectionsForProtocol(protocol);
        connections.values().stream().filter(SseConnection::isActive).forEach(connection -> {
            try {
                sendEventToConnection(connection, event);
                connection.updateActivity();
            } catch (Exception e) {
                logger.error("Failed to send event to connection: {}", connection.getConnectionId(), e);
                closeConnection(connection.getConnectionId(), protocol);
            }
        });
    }

    /**
     * Send an event to a specific connection.
     * 
     * @param connectionId the connection identifier
     * @param protocol the protocol (mcp or a2a)
     * @param event the SSE event to send
     * @return true if the event was sent successfully
     */
    public boolean sendEventToConnection(String connectionId, String protocol, SseEvent event) {
        Map<String, SseConnection> connections = getConnectionsForProtocol(protocol);
        SseConnection connection = connections.get(connectionId);
        if (connection != null && connection.isActive()) {
            try {
                sendEventToConnection(connection, event);
                connection.updateActivity();
                return true;
            } catch (Exception e) {
                logger.error("Failed to send event to connection: {}", connectionId, e);
                closeConnection(connectionId, protocol);
                return false;
            }
        }
        return false;
    }

    /**
     * Get all active connections for a protocol.
     * 
     * @param protocol the protocol (mcp or a2a)
     * @return the map of active connections
     */
    public Map<String, SseConnection> getActiveConnections(String protocol) {
        Map<String, SseConnection> connections = getConnectionsForProtocol(protocol);
        return connections.entrySet().stream().filter(entry -> entry.getValue().isActive()).collect(
                ConcurrentHashMap::new, (map, entry) -> map.put(entry.getKey(), entry.getValue()),
                ConcurrentHashMap::putAll);
    }

    /**
     * Get connection statistics.
     * 
     * @return the connection statistics
     */
    public Map<String, Object> getConnectionStatistics() {
        Map<String, Object> stats = new ConcurrentHashMap<>();

        // MCP statistics
        long mcpActiveCount = mcpConnections.values().stream().filter(SseConnection::isActive).count();
        long mcpTotalCount = mcpConnections.size();

        // A2A statistics
        long a2aActiveCount = a2aConnections.values().stream().filter(SseConnection::isActive).count();
        long a2aTotalCount = a2aConnections.size();

        stats.put("mcp", Map.of("activeConnections", mcpActiveCount, "totalConnections", mcpTotalCount, "endpoint",
                MCP_EVENTS_PATH));

        stats.put("a2a", Map.of("activeConnections", a2aActiveCount, "totalConnections", a2aTotalCount, "endpoint",
                A2A_EVENTS_PATH));

        return stats;
    }

    /**
     * Clean up inactive connections.
     * 
     * @param maxInactiveTimeMs the maximum inactive time in milliseconds
     */
    public void cleanupInactiveConnections(long maxInactiveTimeMs) {
        long currentTime = System.currentTimeMillis();

        // Clean up MCP connections
        cleanupConnections(mcpConnections, "mcp", currentTime, maxInactiveTimeMs);

        // Clean up A2A connections
        cleanupConnections(a2aConnections, "a2a", currentTime, maxInactiveTimeMs);
    }

    /**
     * Get the MCP events endpoint path.
     * 
     * @return the MCP events path
     */
    public String getMcpEventsPath() {
        return MCP_EVENTS_PATH;
    }

    /**
     * Get the A2A events endpoint path.
     * 
     * @return the A2A events path
     */
    public String getA2aEventsPath() {
        return A2A_EVENTS_PATH;
    }

    /**
     * Generate a unique connection ID.
     * 
     * @return the connection ID
     */
    private String generateConnectionId() {
        return "sse-" + connectionIdCounter.incrementAndGet();
    }

    /**
     * Get the connections map for a protocol.
     * 
     * @param protocol the protocol
     * @return the connections map
     */
    private Map<String, SseConnection> getConnectionsForProtocol(String protocol) {
        return switch (protocol.toLowerCase()) {
            case "mcp" -> mcpConnections;
            case "a2a" -> a2aConnections;
            default -> throw new IllegalArgumentException("Unsupported protocol: " + protocol);
        };
    }

    /**
     * Send an event to a specific connection.
     * 
     * @param connection the connection
     * @param event the event to send
     * @throws IOException if the event cannot be sent
     */
    private void sendEventToConnection(SseConnection connection, SseEvent event) throws IOException {
        // TODO: Implement actual SSE event sending
        // This would typically involve writing to the connection's output stream
        logger.debug("Sending event to connection {}: {}", connection.getConnectionId(), event.getEventType());
    }

    /**
     * Clean up inactive connections for a protocol.
     * 
     * @param connections the connections map
     * @param protocol the protocol name
     * @param currentTime the current time
     * @param maxInactiveTimeMs the maximum inactive time
     */
    private void cleanupConnections(Map<String, SseConnection> connections, String protocol, long currentTime,
            long maxInactiveTimeMs) {
        connections.entrySet().removeIf(entry -> {
            SseConnection connection = entry.getValue();
            if (!connection.isActive()) {
                return true;
            }

            long inactiveTime = currentTime - connection.getLastActivity();
            if (inactiveTime > maxInactiveTimeMs) {
                logger.info("Removing inactive {} SSE connection: {} (inactive for {}ms)", protocol,
                        connection.getConnectionId(), inactiveTime);
                return true;
            }

            return false;
        });
    }
}
