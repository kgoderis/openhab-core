package org.openhab.core.ai.tool.server.transport;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * HTTP transport provider for MCP tool communication.
 * 
 * This interface defines the contract for HTTP-based transport providers
 * that handle communication between MCP clients and the tool server.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface TransportProvider {

    /**
     * Get the transport provider ID.
     * 
     * @return the transport provider ID
     */
    String getProviderId();

    /**
     * Get the transport provider name.
     * 
     * @return the transport provider name
     */
    String getProviderName();

    /**
     * Get the supported protocols.
     * 
     * @return list of supported protocols
     */
    String[] getSupportedProtocols();

    /**
     * Initialize the transport provider.
     * 
     * @param configuration the transport configuration
     */
    void initialize(Map<String, Object> configuration);

    /**
     * Start the transport provider.
     */
    void start();

    /**
     * Stop the transport provider.
     */
    void stop();

    /**
     * Check if the transport provider is running.
     * 
     * @return true if running, false otherwise
     */
    boolean isRunning();

    /**
     * Get transport statistics.
     * 
     * @return transport statistics
     */
    Map<String, Object> getStatistics();

    /**
     * Get transport health status.
     * 
     * @return health status
     */
    Map<String, Object> getHealthStatus();

    // TODO: Implement HTTP transport provider
    // TODO: Add support for HTTP/2 and HTTP/3
    // TODO: Implement transport security (TLS)
    // TODO: Add support for transport load balancing
}
