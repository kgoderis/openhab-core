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

    // TransportSelectionStrategy extracted to top-level enum in this package

    /**
     * Transport negotiation result.
     */
    // Inner class extracted to top-level: org.openhab.core.ai.agent.transport.TransportNegotiationResult

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

                // For HTTP transport, we need to ensure AgentServlet is running
                if (selectedType == AgentTransport.TransportType.REST) {
                    validateHttpTransportAvailability();
                }

                // For gRPC transport, we need to reserve a port
                if (selectedType == AgentTransport.TransportType.GRPC) {
                    int port = portManager.findAvailablePort(selectedType);
                    portManager.reservePort(selectedType, port);
                    configuration.put("port", port);
                }

                // Create transport instance based on type
                AgentTransport transport = createTransportInstance(selectedType, configuration);
                String transportId = generateTransportId();
                activeTransports.put(transportId, transport);

                logger.info("Created transport {} with type {} using strategy {}", transportId, selectedType, strategy);

                return transport;

            } catch (Exception e) {
                logger.error("Failed to create transport with strategy {}", strategy, e);
                throw new RuntimeException("Transport creation failed", e);
            }
        });
    }

    /**
     * Create a transport instance based on type.
     * 
     * @param transportType the transport type
     * @param configuration the configuration
     * @return the transport instance
     */
    private AgentTransport createTransportInstance(AgentTransport.TransportType transportType,
            Map<String, Object> configuration) {
        switch (transportType) {
            case REST:
                // HTTP transport is client-side, communicates with AgentServlet
                return new AgentHttpTransport();
            case GRPC:
                // gRPC transport is server-side, needs port allocation
                return new AgentGrpcTransport();
            case JSON_RPC:
                // JSON-RPC transport (if implemented)
                throw new UnsupportedOperationException("JSON-RPC transport not yet implemented");
            default:
                throw new IllegalArgumentException("Unsupported transport type: " + transportType);
        }
    }

    /**
     * Validate that HTTP transport (AgentServlet) is available.
     */
    private void validateHttpTransportAvailability() {
        // TODO: Check if AgentServlet is registered and running
        // This could be done by checking OSGi service registry or making a test request
        logger.debug("Validating HTTP transport availability");
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
        // Implement performance-based selection
        if (availableTypes.isEmpty()) {
            return null;
        }

        AgentTransport.TransportType bestTransport = null;
        double bestPerformance = -1.0;

        for (AgentTransport.TransportType transportType : availableTypes) {
            double performance = calculateTransportPerformance(transportType);
            if (performance > bestPerformance) {
                bestPerformance = performance;
                bestTransport = transportType;
            }
        }

        return bestTransport != null ? bestTransport : availableTypes.iterator().next();
    }

    /**
     * Calculate performance score for a transport type
     * 
     * @param transportType the transport type to evaluate
     * @return the performance score (higher is better)
     */
    private double calculateTransportPerformance(AgentTransport.TransportType transportType) {
        double performance = 0.0;

        // Get performance metrics for this transport type
        Map<String, Object> metrics = getTransportPerformanceMonitoring();

        // Extract relevant metrics for this transport type
        @SuppressWarnings("unchecked")
        Map<String, Object> transportMetrics = (Map<String, Object>) metrics.get(transportType.name());
        if (transportMetrics != null) {
            // Score based on throughput
            Double throughput = (Double) transportMetrics.get("throughput");
            if (throughput != null) {
                performance += throughput * 0.4; // 40% weight for throughput
            }

            // Score based on response time
            Double responseTime = (Double) transportMetrics.get("responseTime");
            if (responseTime != null) {
                performance += (1000.0 / responseTime) * 0.3; // 30% weight for response time (inverse)
            }

            // Score based on success rate
            Double successRate = (Double) transportMetrics.get("successRate");
            if (successRate != null) {
                performance += successRate * 0.2; // 20% weight for success rate
            }

            // Score based on resource usage
            Double resourceUsage = (Double) transportMetrics.get("resourceUsage");
            if (resourceUsage != null) {
                performance += (1.0 - resourceUsage) * 0.1; // 10% weight for resource efficiency
            }
        }

        return performance;
    }

    /**
     * Select transport by latency.
     * 
     * @param availableTypes the available transport types
     * @return the selected transport type
     */
    private AgentTransport.TransportType selectByLatency(Set<AgentTransport.TransportType> availableTypes) {
        // Implement latency-based selection
        if (availableTypes.isEmpty()) {
            return null;
        }

        AgentTransport.TransportType bestTransport = null;
        double lowestLatency = Double.MAX_VALUE;

        for (AgentTransport.TransportType transportType : availableTypes) {
            double latency = measureTransportLatency(transportType);
            if (latency < lowestLatency) {
                lowestLatency = latency;
                bestTransport = transportType;
            }
        }

        return bestTransport != null ? bestTransport : availableTypes.iterator().next();
    }

    /**
     * Measure latency for a transport type
     * 
     * @param transportType the transport type to measure
     * @return the latency in milliseconds
     */
    private double measureTransportLatency(AgentTransport.TransportType transportType) {
        // Get latency metrics for this transport type
        Map<String, Object> monitoring = getTransportPerformanceMonitoring();

        // Extract latency information
        @SuppressWarnings("unchecked")
        Map<String, Object> transportMetrics = (Map<String, Object>) monitoring.get(transportType.name());
        if (transportMetrics != null) {
            Double latency = (Double) transportMetrics.get("latency");
            if (latency != null) {
                return latency;
            }
        }

        // Return default latency values based on transport type
        switch (transportType) {
            case REST:
                return 50.0; // Typical REST latency
            case JSON_RPC:
                return 30.0; // JSON-RPC latency
            case GRPC:
                return 5.0; // gRPC typically has low latency
            default:
                return 100.0; // Default high latency for unknown types
        }
    }

    /**
     * Select transport by reliability.
     * 
     * @param availableTypes the available transport types
     * @return the selected transport type
     */
    private AgentTransport.TransportType selectByReliability(Set<AgentTransport.TransportType> availableTypes) {
        // Implement reliability-based selection
        if (availableTypes.isEmpty()) {
            return null;
        }

        AgentTransport.TransportType bestTransport = null;
        double highestReliability = -1.0;

        for (AgentTransport.TransportType transportType : availableTypes) {
            double reliability = calculateTransportReliability(transportType);
            if (reliability > highestReliability) {
                highestReliability = reliability;
                bestTransport = transportType;
            }
        }

        return bestTransport != null ? bestTransport : availableTypes.iterator().next();
    }

    /**
     * Calculate reliability score for a transport type
     * 
     * @param transportType the transport type to evaluate
     * @return the reliability score (0.0 to 1.0, higher is better)
     */
    private double calculateTransportReliability(AgentTransport.TransportType transportType) {
        double reliability = 0.0;

        // Get reliability metrics for this transport type
        Map<String, Object> monitoring = getTransportPerformanceMonitoring();

        // Extract reliability information
        @SuppressWarnings("unchecked")
        Map<String, Object> transportMetrics = (Map<String, Object>) monitoring.get(transportType.name());
        if (transportMetrics != null) {
            // Score based on success rate
            Double successRate = (Double) transportMetrics.get("successRate");
            if (successRate != null) {
                reliability += successRate * 0.4; // 40% weight for success rate
            }

            // Score based on error rate
            Double errorRate = (Double) transportMetrics.get("errorRate");
            if (errorRate != null) {
                reliability += (1.0 - errorRate) * 0.3; // 30% weight for error rate (inverse)
            }

            // Score based on uptime
            Double uptime = (Double) transportMetrics.get("uptime");
            if (uptime != null) {
                reliability += uptime * 0.2; // 20% weight for uptime
            }

            // Score based on connection stability
            Double connectionStability = (Double) transportMetrics.get("connectionStability");
            if (connectionStability != null) {
                reliability += connectionStability * 0.1; // 10% weight for connection stability
            }
        }

        // If no metrics available, use default reliability values
        if (reliability == 0.0) {
            switch (transportType) {
                case GRPC:
                    reliability = 0.95; // gRPC is typically very reliable
                    break;
                case JSON_RPC:
                    reliability = 0.90; // JSON-RPC is reliable
                    break;
                case REST:
                    reliability = 0.85; // REST is generally reliable
                    break;
                default:
                    reliability = 0.80; // Default reliability
                    break;
            }
        }

        return Math.min(reliability, 1.0); // Ensure reliability is between 0.0 and 1.0
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

        // Implement actual performance monitoring
        monitoring.put("activeTransports", activeTransports.size());
        monitoring.put("registeredProviders", transportProviders.size());
        monitoring.put("timestamp", System.currentTimeMillis());

        // Collect metrics from active transports
        Map<String, Object> transportMetrics = new ConcurrentHashMap<>();
        for (Map.Entry<String, AgentTransport> entry : activeTransports.entrySet()) {
            String transportId = entry.getKey();
            AgentTransport transport = entry.getValue();

            try {
                Map<String, Object> metrics = transport.getMetrics();
                transportMetrics.put(transportId, metrics);
            } catch (Exception e) {
                logger.warn("Failed to get metrics for transport {}: {}", transportId, e.getMessage());
            }
        }
        monitoring.put("transportMetrics", transportMetrics);

        // Collect provider metrics
        Map<String, Object> providerMetrics = new ConcurrentHashMap<>();
        for (Map.Entry<String, AgentTransportProvider> entry : transportProviders.entrySet()) {
            String providerId = entry.getKey();
            AgentTransportProvider provider = entry.getValue();

            try {
                Map<String, Object> metrics = provider.getProviderMetrics();
                providerMetrics.put(providerId, metrics);
            } catch (Exception e) {
                logger.warn("Failed to get metrics for provider {}: {}", providerId, e.getMessage());
            }
        }
        monitoring.put("providerMetrics", providerMetrics);

        // Calculate aggregate metrics
        Map<String, Object> aggregateMetrics = calculateAggregateMetrics(transportMetrics, providerMetrics);
        monitoring.put("aggregateMetrics", aggregateMetrics);

        return monitoring;
    }

    /**
     * Calculate aggregate metrics from transport and provider metrics
     * 
     * @param transportMetrics the transport metrics
     * @param providerMetrics the provider metrics
     * @return the aggregate metrics
     */
    private Map<String, Object> calculateAggregateMetrics(Map<String, Object> transportMetrics,
            Map<String, Object> providerMetrics) {
        Map<String, Object> aggregate = new ConcurrentHashMap<>();

        // Calculate total throughput
        double totalThroughput = 0.0;
        int transportCount = 0;

        for (Object metrics : transportMetrics.values()) {
            if (metrics instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> metricMap = (Map<String, Object>) metrics;
                Double throughput = (Double) metricMap.get("throughput");
                if (throughput != null) {
                    totalThroughput += throughput;
                    transportCount++;
                }
            }
        }

        aggregate.put("totalThroughput", totalThroughput);
        aggregate.put("averageThroughput", transportCount > 0 ? totalThroughput / transportCount : 0.0);
        aggregate.put("activeTransportCount", transportCount);

        return aggregate;
    }

    /**
     * Get transport load balancing information.
     * 
     * @return the load balancing data
     */
    public Map<String, Object> getTransportLoadBalancing() {
        Map<String, Object> loadBalancing = new ConcurrentHashMap<>();

        // Implement actual load balancing logic
        loadBalancing.put("totalTransports", activeTransports.size());

        // Calculate load distribution across transports
        Map<String, Object> loadDistribution = calculateLoadDistribution();
        loadBalancing.put("loadDistribution", loadDistribution);

        // Determine optimal balancing strategy
        String balancingStrategy = determineOptimalBalancingStrategy(loadDistribution);
        loadBalancing.put("balancingStrategy", balancingStrategy);

        // Calculate load balancing metrics
        Map<String, Object> balancingMetrics = calculateBalancingMetrics(loadDistribution);
        loadBalancing.put("balancingMetrics", balancingMetrics);

        return loadBalancing;
    }

    /**
     * Calculate load distribution across active transports
     * 
     * @return the load distribution map
     */
    private Map<String, Object> calculateLoadDistribution() {
        Map<String, Object> distribution = new ConcurrentHashMap<>();

        if (activeTransports.isEmpty()) {
            return distribution;
        }

        double totalLoad = 0.0;
        Map<String, Double> transportLoads = new ConcurrentHashMap<>();

        // Calculate individual transport loads
        for (Map.Entry<String, AgentTransport> entry : activeTransports.entrySet()) {
            String transportId = entry.getKey();
            AgentTransport transport = entry.getValue();

            try {
                Map<String, Object> metrics = transport.getMetrics();
                Double load = (Double) metrics.get("currentLoad");
                if (load == null) {
                    load = 0.0; // Default load if not available
                }

                transportLoads.put(transportId, load);
                totalLoad += load;
            } catch (Exception e) {
                logger.warn("Failed to get load for transport {}: {}", transportId, e.getMessage());
                transportLoads.put(transportId, 0.0);
            }
        }

        // Calculate load percentages
        Map<String, Double> loadPercentages = new ConcurrentHashMap<>();
        for (Map.Entry<String, Double> entry : transportLoads.entrySet()) {
            String transportId = entry.getKey();
            Double load = entry.getValue();
            double percentage = totalLoad > 0 ? (load / totalLoad) * 100.0 : 0.0;
            loadPercentages.put(transportId, percentage);
        }

        distribution.put("totalLoad", totalLoad);
        distribution.put("transportLoads", transportLoads);
        distribution.put("loadPercentages", loadPercentages);

        return distribution;
    }

    /**
     * Determine optimal balancing strategy based on load distribution
     * 
     * @param loadDistribution the load distribution data
     * @return the optimal balancing strategy
     */
    private String determineOptimalBalancingStrategy(Map<String, Object> loadDistribution) {
        @SuppressWarnings("unchecked")
        Map<String, Double> loadPercentages = (Map<String, Double>) loadDistribution.get("loadPercentages");

        if (loadPercentages == null || loadPercentages.isEmpty()) {
            return "round-robin";
        }

        // Check if load is evenly distributed
        double maxLoad = loadPercentages.values().stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
        double minLoad = loadPercentages.values().stream().mapToDouble(Double::doubleValue).min().orElse(0.0);
        double loadVariance = maxLoad - minLoad;

        if (loadVariance < 10.0) {
            return "round-robin"; // Load is fairly even
        } else if (loadVariance < 30.0) {
            return "weighted-round-robin"; // Moderate load variance
        } else {
            return "least-connections"; // High load variance
        }
    }

    /**
     * Calculate load balancing metrics
     * 
     * @param loadDistribution the load distribution data
     * @return the balancing metrics
     */
    private Map<String, Object> calculateBalancingMetrics(Map<String, Object> loadDistribution) {
        Map<String, Object> metrics = new ConcurrentHashMap<>();

        @SuppressWarnings("unchecked")
        Map<String, Double> loadPercentages = (Map<String, Double>) loadDistribution.get("loadPercentages");

        if (loadPercentages != null && !loadPercentages.isEmpty()) {
            double maxLoad = loadPercentages.values().stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
            double minLoad = loadPercentages.values().stream().mapToDouble(Double::doubleValue).min().orElse(0.0);
            double avgLoad = loadPercentages.values().stream().mapToDouble(Double::doubleValue).average().orElse(0.0);

            metrics.put("maxLoadPercentage", maxLoad);
            metrics.put("minLoadPercentage", minLoad);
            metrics.put("averageLoadPercentage", avgLoad);
            metrics.put("loadVariance", maxLoad - minLoad);
            metrics.put("loadBalanceEfficiency", 100.0 - (maxLoad - minLoad)); // Higher is better
        }

        return metrics;
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
