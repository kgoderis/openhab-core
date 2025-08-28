package org.openhab.core.ai.model.monitoring;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.agent.api.AgentModelProvider;
import org.openhab.core.ai.agent.core.AgentClientSession;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
import org.openhab.core.ai.common.monitoring.service.statistics.AgentBehaviorStatistics;
import org.openhab.core.ai.common.monitoring.service.statistics.SystemAggregatedStatistics;
import org.openhab.core.ai.model.ModelTrackingService;
import org.openhab.core.ai.model.api.ModelProviderType;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Model Statistics Aggregator Service
 * 
 * <p>
 * This service aggregates statistics from all AgentModelProvider instances and the ModelTrackingService
 * to provide comprehensive system-wide analytics and monitoring capabilities using the unified monitoring framework.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@Component(service = ModelStatisticsAggregatorService.class)
@NonNullByDefault
public class ModelStatisticsAggregatorService {

    private static final Logger logger = LoggerFactory.getLogger(ModelStatisticsAggregatorService.class);

    // Track all registered agent providers
    private final Map<String, AgentModelProvider> agentProviders = new ConcurrentHashMap<>();

    // Reference to the tracking service for system-wide data
    @Reference
    private @Nullable ModelTrackingService trackingService;

    // Reference to the metrics service for direct statistics access
    @Reference
    private @Nullable MetricsService metricsService;

    @Activate
    public void activate() {
        try {
            recordMetrics("model-statistics-aggregator", "service-activated", true, Duration.ZERO);
            logger.debug("Model Statistics Aggregator Service activated");
        } catch (Exception e) {
            logger.error("Error during Model Statistics Aggregator Service activation: {}", e.getMessage(), e);
            recordMetrics("model-statistics-aggregator", "service-activated", false, Duration.ZERO);
            // Continue with activation even if metrics recording fails
        }
    }

    @Deactivate
    public void deactivate() {
        try {
            agentProviders.clear();
            recordMetrics("model-statistics-aggregator", "service-deactivated", true, Duration.ZERO);
            logger.debug("Model Statistics Aggregator Service deactivated");
        } catch (Exception e) {
            logger.error("Error during Model Statistics Aggregator Service deactivation: {}", e.getMessage(), e);
            recordMetrics("model-statistics-aggregator", "service-deactivated", false, Duration.ZERO);
            // Continue with deactivation despite errors
        }
    }

    /**
     * Registers an agent model provider for statistics aggregation.
     */
    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addAgentModelProvider(AgentModelProvider provider) {
        try {
            if (provider == null) {
                logger.warn("Cannot register null agent model provider for aggregation");
                recordMetrics("model-statistics-aggregator", "provider-registration", false, Duration.ZERO);
                return;
            }

            String agentId = provider.getAgentId();
            if (agentId == null || agentId.trim().isEmpty()) {
                logger.warn("Cannot register agent model provider with null or empty agent ID for aggregation");
                recordMetrics("model-statistics-aggregator", "provider-registration", false, Duration.ZERO);
                return;
            }

            agentProviders.put(agentId, provider);
            recordMetrics("model-statistics-aggregator", "provider-registration", true, Duration.ZERO);
            logger.debug("Registered agent model provider for aggregation: {}", agentId);
        } catch (Exception e) {
            logger.error("Error registering agent model provider for aggregation: {}", e.getMessage(), e);
            recordMetrics("model-statistics-aggregator", "provider-registration", false, Duration.ZERO);
        }
    }

    /**
     * Unregisters an agent model provider from statistics aggregation.
     */
    public void removeAgentModelProvider(AgentModelProvider provider) {
        try {
            if (provider == null) {
                logger.warn("Cannot unregister null agent model provider from aggregation");
                recordMetrics("model-statistics-aggregator", "provider-unregistration", false, Duration.ZERO);
                return;
            }

            String agentId = provider.getAgentId();
            if (agentId == null || agentId.trim().isEmpty()) {
                logger.warn("Cannot unregister agent model provider with null or empty agent ID from aggregation");
                recordMetrics("model-statistics-aggregator", "provider-unregistration", false, Duration.ZERO);
                return;
            }

            agentProviders.remove(agentId);
            recordMetrics("model-statistics-aggregator", "provider-unregistration", true, Duration.ZERO);
            logger.debug("Unregistered agent model provider from aggregation: {}", agentId);
        } catch (Exception e) {
            logger.error("Error unregistering agent model provider from aggregation: {}", e.getMessage(), e);
            recordMetrics("model-statistics-aggregator", "provider-unregistration", false, Duration.ZERO);
        }
    }

    /**
     * Gets comprehensive system-wide statistics combining agent and tracking data.
     * 
     * @return System-wide statistics using unified monitoring framework
     */
    public SystemAggregatedStatistics getSystemStatistics() {
        // Aggregate agent statistics
        List<Object> agentStats = new ArrayList<>();
        long totalAgentRequests = 0;
        long totalAgentSuccessfulRequests = 0;
        long totalAgentFailedRequests = 0;
        long totalAgentResponseTime = 0;
        long totalAgentTokens = 0;
        double totalAgentCost = 0.0;

        // Use MetricsService directly instead of provider.getStatistics()
        MetricsService metrics = metricsService;
        if (metrics != null) {
            for (AgentModelProvider provider : agentProviders.values()) {
                try {
                    // Get agent behavior statistics from MetricsService for each provider
                    AgentBehaviorStatistics stats = metrics.getStatistics(
                        org.openhab.core.ai.common.monitoring.api.MetricKeys.agentTask(provider.getAgentId()),
                        AgentBehaviorStatistics.class,
                        java.time.Duration.ofHours(24)
                    );
                    
                    if (stats != null) {
                        agentStats.add(stats);
                        
                        // Extract values from the statistics object using CountsMetrics interface
                        totalAgentRequests += stats.total();
                        totalAgentSuccessfulRequests += stats.success();
                        totalAgentFailedRequests += stats.failure();
                        totalAgentResponseTime += stats.totalDurationNanos() / 1_000_000; // Convert to milliseconds
                        // Note: AgentStatistics doesn't have token and cost tracking, using 0
                        totalAgentTokens += 0;
                        totalAgentCost += 0.0;
                    }
                } catch (Exception e) {
                    logger.warn("Failed to get statistics from MetricsService for agent {}: {}", 
                        provider.getAgentId(), e.getMessage());
                }
            }
        } else {
            logger.warn("MetricsService not available, cannot get agent statistics");
        }

        // Get tracking service statistics
        ModelTrackingService tracking = trackingService;
        Object trackingStats = null;
        if (tracking != null) {
            trackingStats = tracking.getSystemStats();
        }

        // Calculate average response time (commented out to avoid unused variable warning)
        // double averageResponseTime = totalAgentRequests > 0 ? (double) totalAgentResponseTime / totalAgentRequests
        // : 0.0;

        return SystemAggregatedStatistics.fromSystemData(agentStats, totalAgentRequests, totalAgentSuccessfulRequests,
                totalAgentFailedRequests, totalAgentResponseTime * 1_000_000, // Convert to nanoseconds
                totalAgentTokens, totalAgentCost, trackingStats, agentProviders.size(), java.time.Duration.ofDays(1) // Default
                                                                                                                     // time
                                                                                                                     // range
        );
    }

    /**
     * Gets statistics for a specific agent using MetricsService directly.
     * 
     * @param agentId The agent ID
     * @return Agent statistics, or null if not found
     */
    public @Nullable Object getAgentStatistics(String agentId) {
        MetricsService metrics = metricsService;
        if (metrics != null) {
            try {
                // Get agent behavior statistics from MetricsService
                AgentBehaviorStatistics stats = metrics.getStatistics(
                    org.openhab.core.ai.common.monitoring.api.MetricKeys.agentTask(agentId),
                    AgentBehaviorStatistics.class,
                    java.time.Duration.ofHours(24)
                );
                
                return stats;
            } catch (Exception e) {
                logger.warn("Failed to get statistics from MetricsService for agent: {}", agentId, e);
            }
        } else {
            logger.warn("MetricsService not available, cannot get statistics for agent: {}", agentId);
        }
        return null;
    }

    /**
     * Gets all registered agent IDs.
     * 
     * @return List of agent IDs
     */
    public List<String> getRegisteredAgentIds() {
        return new ArrayList<>(agentProviders.keySet());
    }

    /**
     * Gets the number of registered agents.
     * 
     * @return Number of registered agents
     */
    public int getRegisteredAgentCount() {
        return agentProviders.size();
    }

    /**
     * Gets client usage information from the tracking service.
     * 
     * @return Map of client usage information
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getClientUsage() {
        ModelTrackingService tracking = trackingService;
        if (tracking != null) {
            return new HashMap<>(tracking.getClientUsage());
        }
        return new HashMap<>();
    }

    /**
     * Gets active sessions from the tracking service.
     * 
     * @return List of active sessions
     */
    public List<AgentClientSession> getActiveSessions() {
        ModelTrackingService tracking = trackingService;
        if (tracking != null) {
            return tracking.getActiveSessions();
        }
        return new ArrayList<>();
    }

    /**
     * Gets provider statistics from the tracking service.
     * 
     * @return Map of provider statistics
     */
    @SuppressWarnings("unchecked")
    public Map<ModelProviderType, Object> getProviderStats() {
        ModelTrackingService tracking = trackingService;
        if (tracking != null) {
            return new HashMap<>(tracking.getProviderStats());
        }
        return new HashMap<>();
    }

    /**
     * Gets agents using a specific client.
     * 
     * @param providerType The provider type
     * @param modelName The model name
     * @return List of agent IDs
     */
    public List<String> getAgentsUsingClient(ModelProviderType providerType, String modelName) {
        ModelTrackingService tracking = trackingService;
        if (tracking != null) {
            return tracking.getClientAgents(providerType, modelName);
        }
        return new ArrayList<>();
    }

    /**
     * Gets clients used by a specific agent.
     * 
     * @param agentId The agent ID
     * @return List of client usage information
     */
    @SuppressWarnings("unchecked")
    public List<Object> getClientsUsedByAgent(String agentId) {
        ModelTrackingService tracking = trackingService;
        if (tracking != null) {
            return new ArrayList<>(tracking.getAgentClients(agentId));
        }
        return new ArrayList<>();
    }

    /**
     * Gets client performance metrics for a specific provider and model.
     * 
     * @param providerType The provider type
     * @param modelName The model name
     * @return Client performance metrics, or null if not available
     */
    public org.openhab.core.ai.common.monitoring.service.statistics.@Nullable ClientPerformanceStatistics getClientPerformanceMetrics(
            ModelProviderType providerType, String modelName) {
        MetricsService metrics = metricsService;
        if (metrics != null) {
            try {
                // Create a unique identifier for this client combination
                String clientId = providerType.name() + ":" + modelName;

                // Get client performance snapshots from the metrics service
                // Since there's no direct getClientPerformanceStatistics method, we'll aggregate from snapshots
                List<org.openhab.core.ai.common.monitoring.service.snapshot.ClientPerformanceSnapshot> snapshots = metrics
                        .getAllSnapshots(
                                org.openhab.core.ai.common.monitoring.service.snapshot.ClientPerformanceSnapshot.class);

                // Filter snapshots for this specific client
                List<org.openhab.core.ai.common.monitoring.service.snapshot.ClientPerformanceSnapshot> clientSnapshots = snapshots
                        .stream().filter(snapshot -> {
                            // For now, we'll use all snapshots since ClientPerformanceSnapshot doesn't have
                            // provider/model info
                            // In a real implementation, we'd need to extend ClientPerformanceSnapshot to include this
                            // data
                            return true;
                        }).toList();

                if (!clientSnapshots.isEmpty()) {
                    // Create statistics from the snapshots with a reasonable time range
                    return new org.openhab.core.ai.common.monitoring.service.statistics.ClientPerformanceStatistics(
                            clientSnapshots, java.time.Duration.ofHours(1), System.currentTimeMillis());
                }
            } catch (Exception e) {
                logger.warn("Failed to get client performance metrics for {}:{}", providerType, modelName, e);
            }
        }

        // Fallback to tracking service if metrics service is not available
        ModelTrackingService tracking = trackingService;
        if (tracking != null) {
            try {
                // Note: getClientPerformance method may not exist in current ModelTrackingService implementation
                // This is a placeholder for future implementation
                logger.debug("Client performance metrics not available from tracking service for {}:{}", providerType,
                        modelName);
                // Convert legacy metrics to new format when available
                // This is a temporary bridge until full migration is complete
                return org.openhab.core.ai.common.monitoring.service.statistics.ClientPerformanceStatistics
                        .fromClientData(0, 0, 0, 0, java.time.Duration.ofHours(1));
            } catch (Exception e) {
                logger.warn("Failed to get client performance metrics from tracking service for {}:{}", providerType,
                        modelName, e);
            }
        }

        return null;
    }

    /**
     * Record metrics for an operation.
     * 
     * @param domain the operation domain
     * @param operation the operation name
     * @param success whether the operation was successful
     * @param duration the operation duration
     */
    private void recordMetrics(String domain, String operation, boolean success, Duration duration) {
        try {
            MetricsService metrics = metricsService;
            if (metrics != null) {
                try {
                    metrics.recordOperation(domain, operation, success, duration);
                } catch (Exception e) {
                    logger.error("Failed to record metrics for {}.{}: {}", domain, operation, e.getMessage(), e);
                }
            } else {
                logger.debug("MetricsService not available, cannot record metrics for operation: {} - {}", domain,
                        operation);
            }
        } catch (Exception e) {
            logger.error("Unexpected error recording metrics for {}.{}: {}", domain, operation, e.getMessage(), e);
        }
    }
}
