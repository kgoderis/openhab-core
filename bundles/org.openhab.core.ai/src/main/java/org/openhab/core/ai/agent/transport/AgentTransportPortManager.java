package org.openhab.core.ai.agent.transport;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.agent.transport.api.TransportType;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Agent Transport Port Manager for A2A Protocol.
 * 
 * <p>
 * This class manages port assignments and conflict resolution for A2A transport
 * protocols, ensuring proper port allocation and avoiding conflicts with
 * existing openHAB services and stub framework.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@Component(service = AgentTransportPortManager.class)
@NonNullByDefault
public class AgentTransportPortManager {

    private static final Logger logger = LoggerFactory.getLogger(AgentTransportPortManager.class);

    // Default port assignments based on A2A_TRANSPORT_INTEGRATION_ANALYSIS.md
    private static final int DEFAULT_JSON_RPC_PORT = 8080; // AgentServlet endpoint
    private static final int DEFAULT_GRPC_PORT = 8083; // gRPC server port
    // Note: HTTP transport is client-side, no server port needed

    // Port ranges for fallback allocation
    private static final int MIN_FALLBACK_PORT = 8084;
    private static final int MAX_FALLBACK_PORT = 8099;

    private final Map<TransportType, Integer> assignedPorts = new ConcurrentHashMap<>();
    private final Map<TransportType, Boolean> portAvailability = new ConcurrentHashMap<>();
    private final AtomicInteger fallbackPortCounter = new AtomicInteger(MIN_FALLBACK_PORT);

    /**
     * Get the default port for a transport type.
     * 
     * @param transportType the transport type
     * @return the default port
     */
    public int getDefaultPort(TransportType transportType) {
        return switch (transportType) {
            case JSON_RPC -> DEFAULT_JSON_RPC_PORT;
            case REST -> 0; // HTTP transport is client-side, no server port
            case GRPC -> DEFAULT_GRPC_PORT;
        };
    }

    /**
     * Check if a port is available.
     * 
     * @param port the port to check
     * @return true if the port is available
     */
    public boolean isPortAvailable(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            return true;
        } catch (IOException e) {
            logger.debug("Port {} is not available: {}", port, e.getMessage());
            return false;
        }
    }

    /**
     * Find an available port for a transport type.
     * 
     * @param transportType the transport type
     * @return the available port
     * @throws IllegalStateException if no port is available
     */
    public int findAvailablePort(TransportType transportType) {
        // First try the default port
        int defaultPort = getDefaultPort(transportType);
        if (defaultPort != 0 && isPortAvailable(defaultPort)) {
            logger.info("Using default port {} for transport type {}", defaultPort, transportType);
            return defaultPort;
        }

        // Try fallback ports
        int fallbackPort = findFallbackPort();
        if (fallbackPort != -1) {
            logger.info("Using fallback port {} for transport type {}", fallbackPort, transportType);
            return fallbackPort;
        }

        throw new IllegalStateException("No available port found for transport type: " + transportType);
    }

    /**
     * Reserve a port for a transport type.
     * 
     * @param transportType the transport type
     * @param port the port to reserve
     * @return true if the port was successfully reserved
     */
    public boolean reservePort(TransportType transportType, int port) {
        if (port == 0) { // HTTP transport is client-side, no server port to reserve
            return true;
        }
        if (!isPortAvailable(port)) {
            logger.warn("Port {} is not available for transport type {}", port, transportType);
            return false;
        }

        Integer existingPort = assignedPorts.get(transportType);
        if (existingPort != null && existingPort.equals(port)) {
            logger.debug("Port {} already reserved for transport type {}", port, transportType);
            return true;
        }

        assignedPorts.put(transportType, port);
        portAvailability.put(transportType, true);
        logger.info("Reserved port {} for transport type {}", port, transportType);
        return true;
    }

    /**
     * Release a port for a transport type.
     * 
     * @param transportType the transport type
     */
    public void releasePort(TransportType transportType) {
        Integer port = assignedPorts.remove(transportType);
        portAvailability.remove(transportType);
        if (port != null) {
            logger.info("Released port {} for transport type {}", port, transportType);
        }
    }

    /**
     * Get the assigned port for a transport type.
     * 
     * @param transportType the transport type
     * @return the assigned port or null if not assigned
     */
    public Integer getAssignedPort(TransportType transportType) {
        return assignedPorts.get(transportType);
    }

    /**
     * Check if a transport type has an assigned port.
     * 
     * @param transportType the transport type
     * @return true if a port is assigned
     */
    public boolean hasAssignedPort(TransportType transportType) {
        return assignedPorts.containsKey(transportType);
    }

    /**
     * Get all assigned ports.
     * 
     * @return the map of transport types to assigned ports
     */
    public Map<TransportType, Integer> getAllAssignedPorts() {
        return new ConcurrentHashMap<>(assignedPorts);
    }

    /**
     * Validate port assignment for all transport types.
     * 
     * @return true if all assigned ports are valid
     */
    public boolean validateAllPortAssignments() {
        boolean allValid = true;
        for (Map.Entry<TransportType, Integer> entry : assignedPorts.entrySet()) {
            TransportType transportType = entry.getKey();
            Integer port = entry.getValue();

            if (port == null || (port == 0 && transportType != TransportType.REST)) { // HTTP transport
                                                                                      // is client-side
                logger.error("Invalid port assignment for transport type {}: port {}", transportType, port);
                allValid = false;
            } else if (port != 0 && !isPortAvailable(port)) {
                logger.error("Invalid port assignment for transport type {}: port {}", transportType, port);
                allValid = false;
            }
        }
        return allValid;
    }

    /**
     * Get port assignment status for all transport types.
     * 
     * @return the port assignment status
     */
    public Map<String, Object> getPortAssignmentStatus() {
        Map<String, Object> status = new ConcurrentHashMap<>();

        for (TransportType transportType : TransportType.values()) {
            Integer assignedPort = assignedPorts.get(transportType);
            int defaultPort = getDefaultPort(transportType);
            boolean isAvailable = portAvailability.getOrDefault(transportType, false);

            Map<String, Object> transportStatus = Map.of("assignedPort", assignedPort, "defaultPort", defaultPort,
                    "isAvailable", isAvailable, "isDefaultPort",
                    assignedPort != null && assignedPort.equals(defaultPort));

            status.put(transportType.getIdentifier(), transportStatus);
        }

        return status;
    }

    /**
     * Find a fallback port in the available range.
     * 
     * @return the available fallback port or -1 if none available
     */
    private int findFallbackPort() {
        int attempts = 0;
        int maxAttempts = MAX_FALLBACK_PORT - MIN_FALLBACK_PORT + 1;

        while (attempts < maxAttempts) {
            int port = fallbackPortCounter.getAndIncrement();
            if (port > MAX_FALLBACK_PORT) {
                fallbackPortCounter.set(MIN_FALLBACK_PORT);
                port = fallbackPortCounter.getAndIncrement();
            }

            if (isPortAvailable(port)) {
                return port;
            }

            attempts++;
        }

        logger.error("No available fallback ports found in range {}-{}", MIN_FALLBACK_PORT, MAX_FALLBACK_PORT);
        return -1;
    }

    /**
     * Reset port assignments (for testing purposes).
     */
    public void resetPortAssignments() {
        assignedPorts.clear();
        portAvailability.clear();
        fallbackPortCounter.set(MIN_FALLBACK_PORT);
        logger.info("Reset all port assignments");
    }
}
