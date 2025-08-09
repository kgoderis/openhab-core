package org.openhab.core.ai.agent.transport;

import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Agent Transport Provider Interface for A2A Protocol.
 * 
 * <p>
 * This interface provides a factory pattern for creating and managing
 * AgentTransport instances, following the MCP transport provider pattern
 * for consistency.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface AgentTransportProvider {

    /**
     * Get the provider identifier.
     * 
     * @return the provider identifier
     */
    String getProviderId();

    /**
     * Get the supported transport types.
     * 
     * @return the set of supported transport types
     */
    Set<AgentTransport.TransportType> getSupportedTransportTypes();

    /**
     * Check if the provider supports a specific transport type.
     * 
     * @param transportType the transport type to check
     * @return true if supported
     */
    boolean supportsTransportType(AgentTransport.TransportType transportType);

    /**
     * Create a new transport instance.
     * 
     * @param transportType the transport type to create
     * @param configuration the transport configuration
     * @return the created transport instance
     * @throws IllegalArgumentException if the transport type is not supported
     */
    AgentTransport createTransport(AgentTransport.TransportType transportType, Map<String, Object> configuration);

    /**
     * Get the default configuration for a transport type.
     * 
     * @param transportType the transport type
     * @return the default configuration
     * @throws IllegalArgumentException if the transport type is not supported
     */
    Map<String, Object> getDefaultConfiguration(AgentTransport.TransportType transportType);

    /**
     * Validate configuration for a transport type.
     * 
     * @param transportType the transport type
     * @param configuration the configuration to validate
     * @return true if the configuration is valid
     */
    boolean validateConfiguration(AgentTransport.TransportType transportType, Map<String, Object> configuration);

    /**
     * Get provider-specific capabilities and features.
     * 
     * @return the provider capabilities
     */
    Map<String, Object> getProviderCapabilities();

    /**
     * Get provider health status.
     * 
     * @return the provider health status
     */
    AgentTransport.TransportHealth getProviderHealth();

    /**
     * Get provider metrics.
     * 
     * @return the provider metrics
     */
    Map<String, Object> getProviderMetrics();
}
