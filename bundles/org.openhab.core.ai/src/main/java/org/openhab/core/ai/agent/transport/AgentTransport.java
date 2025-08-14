package org.openhab.core.ai.agent.transport;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Agent Transport Interface for A2A Protocol.
 * 
 * <p>
 * This interface provides a transport abstraction layer for the A2A protocol,
 * following the MCP transport pattern for consistency. It supports multiple
 * transport protocols (JSON-RPC, gRPC, REST) with unified interface.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface AgentTransport {

    /**
     * Transport protocol types supported by the A2A implementation.
     */
    // Extracted to top-level: org.openhab.core.ai.agent.transport.TransportType

    /**
     * Transport capabilities and features.
     */
    // Extracted to top-level: org.openhab.core.ai.agent.transport.TransportCapabilities

    /**
     * Transport health status.
     */
    // Extracted to top-level: org.openhab.core.ai.agent.transport.TransportHealth

    /**
     * Get the transport identifier.
     * 
     * @return the transport identifier
     */
    String getTransportId();

    /**
     * Get the transport capabilities.
     * 
     * @return the transport capabilities
     */
    TransportCapabilities getCapabilities();

    /**
     * Check if the transport is running.
     * 
     * @return true if running
     */
    boolean isRunning();

    /**
     * Start the transport.
     * 
     * @return a future that completes when the transport is started
     */
    CompletableFuture<Void> start();

    /**
     * Stop the transport.
     * 
     * @return a future that completes when the transport is stopped
     */
    CompletableFuture<Void> stop();

    /**
     * Get the transport health status.
     * 
     * @return the health status
     */
    TransportHealth getHealth();

    /**
     * Send a message through the transport.
     * 
     * @param message the message to send
     * @return a future that completes with the response
     */
    CompletableFuture<Map<String, Object>> sendMessage(Map<String, Object> message);

    /**
     * Subscribe to a stream of messages.
     * 
     * @param subscription the subscription parameters
     * @return a future that completes with the subscription result
     */
    CompletableFuture<Map<String, Object>> subscribeToStream(Map<String, Object> subscription);

    /**
     * Get transport-specific metrics.
     * 
     * @return the metrics map
     */
    Map<String, Object> getMetrics();

    /**
     * Update transport configuration.
     * 
     * @param configuration the new configuration
     * @return a future that completes when the configuration is updated
     */
    CompletableFuture<Void> updateConfiguration(Map<String, Object> configuration);
}
