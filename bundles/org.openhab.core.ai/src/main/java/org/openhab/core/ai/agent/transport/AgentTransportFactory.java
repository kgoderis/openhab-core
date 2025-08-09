package org.openhab.core.ai.agent.transport;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Agent Transport Factory for A2A Protocol.
 * 
 * <p>
 * This class implements the transport factory pattern with dynamic selection,
 * transport negotiation, and capability discovery for A2A protocol transports.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@Component(service = AgentTransportFactory.class)
@NonNullByDefault
public class AgentTransportFactory {

    private static final Logger logger = LoggerFactory.getLogger(AgentTransportFactory.class);

    @Reference
    private AgentTransportPortManager portManager;

    // Transport providers registry
    private final Map<String, AgentTransportProvider> transportProviders = new ConcurrentHashMap<>();
    private final Map<String, AgentTransport> activeTransports = new ConcurrentHashMap<>();
    private final AtomicInteger transportIdCounter = new AtomicInteger(0);

    /**
     * Transport selection strategy.
     */
    public enum TransportSelectionStrategy {
        /**
         * Select the first available transport.
         */
        FIRST_AVAILABLE,

        /**
         * Select the transport with the best performance.
         */
        BEST_PERFORMANCE,

        /**
         * Select the transport with the lowest latency.
         */
        LOWEST_LATENCY,

        /**
         * Select the transport with the highest reliability.
         */
        HIGHEST_RELIABILITY,

        /**
         * Select the transport based on client preference.
         */
        CLIENT_PREFERENCE
    }

    /**
     * Transport negotiation result.
     */
    public static class TransportNegotiationResult {
        private final AgentTransport.TransportType selectedTransport;
        private final Map<String, Object> negotiationData;
        private final boolean success;
        private final String reason;

        public TransportNegotiationResult(AgentTransport.TransportType selectedTransport,
                Map<String, Object> negotiationData, boolean success, String reason) {
            this.selectedTransport = selectedTransport;
            this.negotiationData = negotiationData;
            this.success = success;
            this.reason = reason;
        }

        public AgentTransport.TransportType getSelectedTransport() {
            return selectedTransport;
        }

        public Map<String, Object> getNegotiationData() {
            return negotiationData;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getReason() {
            return reason;
        }
    }

    /**
     * Register a transport provider.
     * 
     * @param provider the transport provider to register
     */
    public void registerTransportProvider(AgentTransportProvider provider) {
        String providerId = provider.getProviderId();
        transportProviders.put(providerId, provider);
        logger.info("Registered transport provider: {}", providerId);
    }

    /**
     * Unregister a transport provider.
     * 
     * @param providerId the provider identifier
     */
    public void unregisterTransportProvider(String providerId) {
        AgentTransportProvider provider = transportProviders.remove(providerId);
        if (provider != null) {
            logger.info("Unregistered transport provider: {}", providerId);
        }
    }

    /**
     * Get all registered transport providers.
     * 
     * @return the set of provider identifiers
     */
    public Set<String> getRegisteredProviders() {
        return transportProviders.keySet();
    }

    /**
     * Create a transport with dynamic selection.
     * 
     * @param strategy the selection strategy
     * @param clientPreferences the client preferences
     * @param configuration the transport configuration
     * @return a future that completes with the created transport
     */
    public CompletableFuture<AgentTransport> createTransport(TransportSelectionStrategy strategy,
            Map<String, Object> clientPreferences, Map<String, Object> configuration) {

        return CompletableFuture.supplyAsync(() -> {
            try {
                // Perform transport negotiation
                TransportNegotiationResult negotiation = negotiateTransport(strategy, clientPreferences);

                if (!negotiation.isSuccess()) {
                    throw new IllegalStateException("Transport negotiation failed: " + negotiation.getReason());
                }

                // Create the selected transport
                AgentTransport.TransportType selectedType = negotiation.getSelectedTransport();
                AgentTransportProvider provider = selectProvider(selectedType);

                if (provider == null) {
                    throw new IllegalStateException("No provider available for transport type: " + selectedType);
                }

                // Merge configuration with negotiation data
                Map<String, Object> finalConfig = mergeConfiguration(configuration, negotiation.getNegotiationData());

                // Create and register the transport
                AgentTransport transport = provider.createTransport(selectedType, finalConfig);
                String transportId = generateTransportId();
                activeTransports.put(transportId, transport);

                logger.info("Created transport {} with type {} using strategy {}", transportId, selectedType, strategy);

                return transport;

            } catch (Exception e) {
                logger.error("Failed to create transport with strategy: {}", strategy, e);
                throw new RuntimeException("Transport creation failed", e);
            }
        });
    }

    /**
     * Negotiate transport selection based on strategy and preferences.
     * 
     * @param strategy the selection strategy
     * @param clientPreferences the client preferences
     * @return the negotiation result
     */
    private TransportNegotiationResult negotiateTransport(TransportSelectionStrategy strategy,
            Map<String, Object> clientPreferences) {

        // Get available transport types from all providers
        Set<AgentTransport.TransportType> availableTypes = getAvailableTransportTypes();

        if (availableTypes.isEmpty()) {
            return new TransportNegotiationResult(null, Map.of(), false, "No transport types available");
        }

        AgentTransport.TransportType selectedType = null;
        Map<String, Object> negotiationData = new ConcurrentHashMap<>();

        switch (strategy) {
            case FIRST_AVAILABLE:
                selectedType = availableTypes.iterator().next();
                break;

            case CLIENT_PREFERENCE:
                selectedType = selectByClientPreference(availableTypes, clientPreferences);
                break;

            case BEST_PERFORMANCE:
                selectedType = selectByPerformance(availableTypes);
                break;

            case LOWEST_LATENCY:
                selectedType = selectByLatency(availableTypes);
                break;

            case HIGHEST_RELIABILITY:
                selectedType = selectByReliability(availableTypes);
                break;
        }

        if (selectedType == null) {
            return new TransportNegotiationResult(null, negotiationData, false,
                    "No suitable transport found for strategy: " + strategy);
        }

        // Add negotiation data
        negotiationData.put("strategy", strategy.name());
        negotiationData.put("availableTypes", availableTypes);
        negotiationData.put("clientPreferences", clientPreferences);
        negotiationData.put("selectedType", selectedType.getIdentifier());

        return new TransportNegotiationResult(selectedType, negotiationData, true, "Transport selected successfully");
    }

    /**
     * Select transport by client preference.
     * 
     * @param availableTypes the available transport types
     * @param clientPreferences the client preferences
     * @return the selected transport type
     */
    private AgentTransport.TransportType selectByClientPreference(Set<AgentTransport.TransportType> availableTypes,
            Map<String, Object> clientPreferences) {

        String preferredType = (String) clientPreferences.get("preferredTransport");
        if (preferredType != null) {
            for (AgentTransport.TransportType type : availableTypes) {
                if (type.getIdentifier().equals(preferredType)) {
                    return type;
                }
            }
        }

        // Fallback to first available
        return availableTypes.iterator().next();
    }

    /**
     * Select transport by performance.
     * 
     * @param availableTypes the available transport types
     * @return the selected transport type
     */
    private AgentTransport.TransportType selectByPerformance(Set<AgentTransport.TransportType> availableTypes) {
        // TODO: Implement performance-based selection
        // This would involve measuring actual performance metrics
        return availableTypes.iterator().next();
    }

    /**
     * Select transport by latency.
     * 
     * @param availableTypes the available transport types
     * @return the selected transport type
     */
    private AgentTransport.TransportType selectByLatency(Set<AgentTransport.TransportType> availableTypes) {
        // TODO: Implement latency-based selection
        // This would involve measuring actual latency metrics
        return availableTypes.iterator().next();
    }

    /**
     * Select transport by reliability.
     * 
     * @param availableTypes the available transport types
     * @return the selected transport type
     */
    private AgentTransport.TransportType selectByReliability(Set<AgentTransport.TransportType> availableTypes) {
        // TODO: Implement reliability-based selection
        // This would involve measuring actual reliability metrics
        return availableTypes.iterator().next();
    }

    /**
     * Get available transport types from all providers.
     * 
     * @return the set of available transport types
     */
    private Set<AgentTransport.TransportType> getAvailableTransportTypes() {
        return transportProviders.values().stream().flatMap(provider -> provider.getSupportedTransportTypes().stream())
                .collect(java.util.stream.Collectors.toSet());
    }

    /**
     * Select a provider for a transport type.
     * 
     * @param transportType the transport type
     * @return the selected provider or null if none available
     */
    private AgentTransportProvider selectProvider(AgentTransport.TransportType transportType) {
        return transportProviders.values().stream().filter(provider -> provider.supportsTransportType(transportType))
                .findFirst().orElse(null);
    }

    /**
     * Merge configuration with negotiation data.
     * 
     * @param baseConfig the base configuration
     * @param negotiationData the negotiation data
     * @return the merged configuration
     */
    private Map<String, Object> mergeConfiguration(Map<String, Object> baseConfig,
            Map<String, Object> negotiationData) {
        Map<String, Object> merged = new ConcurrentHashMap<>(baseConfig);
        merged.putAll(negotiationData);
        return merged;
    }

    /**
     * Generate a unique transport ID.
     * 
     * @return the transport ID
     */
    private String generateTransportId() {
        return "transport-" + transportIdCounter.incrementAndGet();
    }

    /**
     * Get transport capability discovery for all providers.
     * 
     * @return the capability discovery data
     */
    public Map<String, Object> getTransportCapabilityDiscovery() {
        Map<String, Object> discovery = new ConcurrentHashMap<>();

        for (Map.Entry<String, AgentTransportProvider> entry : transportProviders.entrySet()) {
            String providerId = entry.getKey();
            AgentTransportProvider provider = entry.getValue();

            Map<String, Object> providerCapabilities = Map.of("providerId", providerId, "supportedTypes",
                    provider.getSupportedTransportTypes(), "capabilities", provider.getProviderCapabilities(), "health",
                    provider.getProviderHealth(), "metrics", provider.getProviderMetrics());

            discovery.put(providerId, providerCapabilities);
        }

        return discovery;
    }

    /**
     * Get transport performance monitoring data.
     * 
     * @return the performance monitoring data
     */
    public Map<String, Object> getTransportPerformanceMonitoring() {
        Map<String, Object> monitoring = new ConcurrentHashMap<>();

        // TODO: Implement actual performance monitoring
        // This would involve collecting metrics from active transports

        monitoring.put("activeTransports", activeTransports.size());
        monitoring.put("registeredProviders", transportProviders.size());
        monitoring.put("timestamp", System.currentTimeMillis());

        return monitoring;
    }

    /**
     * Get transport load balancing information.
     * 
     * @return the load balancing data
     */
    public Map<String, Object> getTransportLoadBalancing() {
        Map<String, Object> loadBalancing = new ConcurrentHashMap<>();

        // TODO: Implement actual load balancing logic
        // This would involve distributing load across available transports

        loadBalancing.put("totalTransports", activeTransports.size());
        loadBalancing.put("loadDistribution", Map.of()); // Placeholder
        loadBalancing.put("balancingStrategy", "round-robin"); // Placeholder

        return loadBalancing;
    }

    /**
     * Update transport configuration.
     * 
     * @param transportId the transport identifier
     * @param configuration the new configuration
     * @return a future that completes when the configuration is updated
     */
    public CompletableFuture<Void> updateTransportConfiguration(String transportId, Map<String, Object> configuration) {
        return CompletableFuture.runAsync(() -> {
            AgentTransport transport = activeTransports.get(transportId);
            if (transport != null) {
                transport.updateConfiguration(configuration);
                logger.info("Updated configuration for transport: {}", transportId);
            } else {
                throw new IllegalArgumentException("Transport not found: " + transportId);
            }
        });
    }

    /**
     * Get all active transports.
     * 
     * @return the map of active transports
     */
    public Map<String, AgentTransport> getActiveTransports() {
        return new ConcurrentHashMap<>(activeTransports);
    }

    /**
     * Remove a transport.
     * 
     * @param transportId the transport identifier
     */
    public void removeTransport(String transportId) {
        AgentTransport transport = activeTransports.remove(transportId);
        if (transport != null) {
            transport.stop();
            logger.info("Removed transport: {}", transportId);
        }
    }

    /**
     * Reset the transport factory (for testing purposes).
     */
    public void resetFactory() {
        // Stop and remove all active transports
        activeTransports.values().forEach(transport -> {
            try {
                transport.stop();
            } catch (Exception e) {
                logger.warn("Error stopping transport during reset", e);
            }
        });
        activeTransports.clear();

        // Reset counter
        transportIdCounter.set(0);

        logger.info("Reset transport factory");
    }
}
