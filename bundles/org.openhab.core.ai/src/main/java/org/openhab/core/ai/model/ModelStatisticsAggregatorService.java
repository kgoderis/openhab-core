package org.openhab.core.ai.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.agent.api.AgentModelProvider;
import org.openhab.core.ai.agent.core.AgentClientSession;
import org.openhab.core.ai.common.statistics.AgentModelStatistics;
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
 * to provide comprehensive system-wide analytics and monitoring capabilities.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
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

    @Activate
    public void activate() {
        logger.debug("Model Statistics Aggregator Service activated");
    }

    @Deactivate
    public void deactivate() {
        logger.debug("Model Statistics Aggregator Service deactivated");
        agentProviders.clear();
    }

    /**
     * Registers an agent model provider for statistics aggregation.
     */
    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addAgentModelProvider(AgentModelProvider provider) {
        String agentId = provider.getAgentId();
        agentProviders.put(agentId, provider);
        logger.debug("Registered agent model provider for aggregation: {}", agentId);
    }

    /**
     * Unregisters an agent model provider from statistics aggregation.
     */
    public void removeAgentModelProvider(AgentModelProvider provider) {
        String agentId = provider.getAgentId();
        agentProviders.remove(agentId);
        logger.debug("Unregistered agent model provider from aggregation: {}", agentId);
    }

    /**
     * Gets comprehensive system-wide statistics combining agent and tracking data.
     * 
     * @return System-wide statistics
     */
    public SystemAggregatedStatistics getSystemStatistics() {
        // Aggregate agent statistics
        List<AgentModelStatistics> agentStats = new ArrayList<>();
        long totalAgentRequests = 0;
        long totalAgentSuccessfulRequests = 0;
        long totalAgentFailedRequests = 0;
        long totalAgentResponseTime = 0;
        long totalAgentTokens = 0;
        double totalAgentCost = 0.0;

        for (AgentModelProvider provider : agentProviders.values()) {
            try {
                AgentModelStatistics stats = provider.getStatistics();
                agentStats.add(stats);

                totalAgentRequests += stats.getTotalRequests();
                totalAgentSuccessfulRequests += stats.getSuccessfulRequests();
                totalAgentFailedRequests += stats.getFailedRequests();
                totalAgentResponseTime += stats.getTotalResponseTimeMs();
                totalAgentTokens += stats.getTotalTokensUsed();
                totalAgentCost += stats.getTotalCost();
            } catch (Exception e) {
                logger.warn("Failed to get statistics for agent: {}", provider.getAgentId(), e);
            }
        }

        // Get tracking service statistics
        ModelTrackingService tracking = trackingService;
        SystemUsageStats trackingStats = null;
        if (tracking != null) {
            trackingStats = tracking.getSystemStats();
        }

        return new SystemAggregatedStatistics(agentStats, totalAgentRequests, totalAgentSuccessfulRequests,
                totalAgentFailedRequests, totalAgentResponseTime, totalAgentTokens, totalAgentCost, trackingStats,
                agentProviders.size(), Instant.now());
    }

    /**
     * Gets statistics for a specific agent.
     * 
     * @param agentId The agent ID
     * @return Agent statistics, or null if not found
     */
    public @Nullable AgentModelStatistics getAgentStatistics(String agentId) {
        AgentModelProvider provider = agentProviders.get(agentId);
        if (provider != null) {
            try {
                return provider.getStatistics();
            } catch (Exception e) {
                logger.warn("Failed to get statistics for agent: {}", agentId, e);
            }
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
    public Map<String, ClientUsageInfo> getClientUsage() {
        ModelTrackingService tracking = trackingService;
        if (tracking != null) {
            return tracking.getClientUsage();
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
    public Map<ModelProviderType, ProviderUsageStats> getProviderStats() {
        ModelTrackingService tracking = trackingService;
        if (tracking != null) {
            return tracking.getProviderStats();
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
    public List<ClientUsageInfo> getClientsUsedByAgent(String agentId) {
        ModelTrackingService tracking = trackingService;
        if (tracking != null) {
            return tracking.getAgentClients(agentId);
        }
        return new ArrayList<>();
    }

    /**
     * Comprehensive system-wide statistics combining agent and tracking data.
     */
    // Inner class extracted to top-level: org.openhab.core.ai.model.SystemAggregatedStatistics
}
