package org.openhab.core.ai.model;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.agent.core.AgentClientSession;
import org.openhab.core.ai.common.monitoring.api.MetricKey;
import org.openhab.core.ai.common.monitoring.api.MetricKeys;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
import org.openhab.core.ai.common.monitoring.snapshot.ExecutionMetricsSnapshot;
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
 * LLM Client Tracking Service
 * 
 * <p>
 * This service tracks and monitors LLM client usage across the AI system:
 * - Tracks which agents are using which LLM clients
 * - Monitors client usage patterns and performance
 * - Provides analytics on client utilization
 * - Tracks client health and availability
 * - Monitors cost and token usage
 * - Provides real-time usage statistics
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 */
@Component(service = ModelTrackingService.class)
@NonNullByDefault
public class ModelTrackingService {

    private static final Logger logger = LoggerFactory.getLogger(ModelTrackingService.class);

    // Client usage tracking
    private final Map<String, ClientUsageInfo> clientUsage = new ConcurrentHashMap<>();
    private final Map<String, AgentClientSession> agentSessions = new ConcurrentHashMap<>();
    // Performance monitoring - migrated to MetricsService
    // private final AtomicLong totalRequests = new AtomicLong(0);
    // private final AtomicLong totalTokensUsed = new AtomicLong(0);
    // private final AtomicLong totalCost = new AtomicLong(0);
    private final AtomicReference<Instant> lastRequestTime = new AtomicReference<>(Instant.now());

    // Provider-specific statistics - temporarily using Map until ProviderStats is implemented
    private final Map<String, Map<String, Object>> providerStats = new ConcurrentHashMap<>();
    private final Map<String, List<Long>> responseTimes = new ConcurrentHashMap<>();
    // private final Map<String, AtomicLong> errorCounts = new ConcurrentHashMap<>();

    private MetricsService metricsService;

    @Activate
    public void activate() {
        logger.debug("LLM Client Tracking Service activated");
        initializeProviderStats();
    }

    @Deactivate
    public void deactivate() {
        logger.debug("LLM Client Tracking Service deactivated");
        clientUsage.clear();
        agentSessions.clear();
        providerStats.clear();
    }

    @Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
    protected void setMetricsService(MetricsService metricsService) {
        this.metricsService = metricsService;
        logger.debug("MetricsService set for ModelTrackingService");
    }

    /**
     * Records a client usage event when an agent starts using an LLM client
     * 
     * @param agentId The ID of the agent using the client
     * @param providerType The type of LLM provider
     * @param modelName The specific model being used
     * @param sessionId Optional session ID for tracking
     */
    public void recordClientUsage(String agentId, ModelProviderType providerType, String modelName,
            @Nullable String sessionId) {
        try {
            String clientKey = generateClientKey(providerType, modelName);
            Instant now = Instant.now();

            // Update client usage info
            ClientUsageInfo usageInfo = clientUsage.computeIfAbsent(clientKey,
                    k -> new ClientUsageInfo(providerType, modelName));
            usageInfo.addAgent(agentId, now);

            // Create or update agent session
            String sessionKey = agentId + "_" + clientKey;
            AgentClientSession session = agentSessions.computeIfAbsent(sessionKey,
                    k -> new AgentClientSession(agentId, providerType, modelName, sessionId));
            session.updateLastUsed(now);

            // Update provider statistics - temporarily using Map until ProviderStats is implemented
            providerStats.computeIfAbsent(clientKey, k -> new ConcurrentHashMap<>());
            // TODO: Implement proper statistics recording with ProviderStats class

            logger.debug("Recorded client usage: agent={}, provider={}, model={}", agentId, providerType, modelName);

        } catch (Exception e) {
            logger.error("Error recording client usage for agent: {}", agentId, e);
        }
    }

    /**
     * Records a completed LLM request with performance metrics
     * 
     * @param agentId The ID of the agent that made the request
     * @param providerType The type of LLM provider
     * @param modelName The specific model used
     * @param requestId Unique request identifier
     * @param tokensUsed Number of tokens used
     * @param cost Estimated cost of the request
     * @param responseTimeMs Response time in milliseconds
     * @param success Whether the request was successful
     */
    public void recordRequestCompletion(String agentId, ModelProviderType providerType, String modelName,
            String requestId, int tokensUsed, double cost, long responseTimeMs, boolean success) {
        try {
            String clientKey = generateClientKey(providerType, modelName);
            Instant now = Instant.now();

            // Update global statistics using proper MetricsService calls
            Duration responseTime = Duration.ofMillis(responseTimeMs);
            metricsService.recordOperation("model", "completion").withSuccess(success)
                    .withDuration(responseTime.toNanos()).withData("modelId", clientKey).withData("inputTokens", 0)
                    .withData("outputTokens", tokensUsed).withData("cost", cost).record();
            lastRequestTime.set(now);

            // Update client usage info
            ClientUsageInfo usageInfo = clientUsage.get(clientKey);
            if (usageInfo != null) {
                usageInfo.recordRequest(tokensUsed, cost, responseTimeMs, success);
            }

            // Update provider statistics - temporarily disabled until ProviderStats is implemented
            // TODO: Implement proper provider statistics recording with ProviderStats class

            // Track response times for performance analysis
            responseTimes.computeIfAbsent(clientKey, k -> new ArrayList<>()).add(responseTimeMs);

            // Track error counts
            if (!success) {
                // errorCounts.computeIfAbsent(clientKey, k -> new AtomicLong(0)).incrementAndGet();
            }

            logger.debug("Recorded request completion: agent={}, provider={}, model={}, success={}, time={}ms", agentId,
                    providerType, modelName, success, responseTimeMs);

        } catch (Exception e) {
            logger.error("Error recording request completion for agent: {}", agentId, e);
        }
    }

    /**
     * Records when an agent releases an LLM client
     * 
     * @param agentId The ID of the agent releasing the client
     * @param providerType The type of LLM provider
     * @param modelName The specific model being released
     */
    public void recordClientRelease(String agentId, ModelProviderType providerType, String modelName) {
        try {
            String clientKey = generateClientKey(providerType, modelName);
            String sessionKey = agentId + "_" + clientKey;

            // Remove agent from client usage
            ClientUsageInfo usageInfo = clientUsage.get(clientKey);
            if (usageInfo != null) {
                usageInfo.removeAgent(agentId);
            }

            // Mark session as inactive
            AgentClientSession session = agentSessions.get(sessionKey);
            if (session != null) {
                session.markInactive();
            }

            logger.debug("Recorded client release: agent={}, provider={}, model={}", agentId, providerType, modelName);

        } catch (Exception e) {
            logger.error("Error recording client release for agent: {}", agentId, e);
        }
    }

    /**
     * Gets all client usage information
     * 
     * @return Map of client usage information
     */
    public Map<String, ClientUsageInfo> getClientUsage() {
        return new HashMap<>(clientUsage);
    }

    /**
     * Gets usage information for a specific client
     * 
     * @param providerType The provider type
     * @param modelName The model name
     * @return Client usage information, or null if not found
     */
    public @Nullable ClientUsageInfo getClientUsage(ModelProviderType providerType, String modelName) {
        String clientKey = generateClientKey(providerType, modelName);
        return clientUsage.get(clientKey);
    }

    /**
     * Gets all active sessions
     * 
     * @return List of active sessions
     */
    public List<AgentClientSession> getActiveSessions() {
        return agentSessions.values().stream().filter(AgentClientSession::isActive).collect(Collectors.toList());
    }

    /**
     * Gets sessions for a specific agent
     * 
     * @param agentId The agent ID
     * @return List of agent sessions
     */
    public List<AgentClientSession> getAgentSessions(String agentId) {
        return agentSessions.values().stream().filter(session -> session.getAgentId().equals(agentId))
                .collect(Collectors.toList());
    }

    /**
     * Gets provider statistics
     * 
     * @return Map of provider statistics
     */
    public Map<ModelProviderType, Map<String, Object>> getProviderStats() {
        // TODO: Implement proper ProviderUsageStats conversion
        Map<ModelProviderType, Map<String, Object>> result = new HashMap<>();
        for (ModelProviderType type : ModelProviderType.values()) {
            result.put(type, providerStats.getOrDefault(type.name() + ":default", new HashMap<>()));
        }
        return result;
    }

    /**
     * Gets system-wide usage statistics
     * 
     * @return System usage statistics
     */
    public SystemUsageStats getSystemStats() {
        // TODO: Implement proper statistics retrieval from MetricsService
        // For now, return default/placeholder values
        return new SystemUsageStats(0L, // totalRequests - TODO: aggregate from MetricsService
                0L, // totalTokens - TODO: aggregate from MetricsService
                0.0, // totalCost - TODO: aggregate from MetricsService
                lastRequestTime.get(), calculateAverageResponseTime(), calculateErrorRate());
    }

    /**
     * Gets all clients used by a specific agent
     * 
     * @param agentId The agent ID
     * @return List of client usage information
     */
    public List<ClientUsageInfo> getAgentClients(String agentId) {
        return clientUsage.values().stream().filter(usage -> usage.getActiveAgents().containsKey(agentId))
                .collect(Collectors.toList());
    }

    /**
     * Gets all agents using a specific client
     * 
     * @param providerType The provider type
     * @param modelName The model name
     * @return List of agent IDs
     */
    public List<String> getClientAgents(ModelProviderType providerType, String modelName) {
        String clientKey = generateClientKey(providerType, modelName);
        ClientUsageInfo usage = clientUsage.get(clientKey);
        if (usage != null) {
            return new ArrayList<>(usage.getActiveAgents().keySet());
        }
        return Collections.emptyList();
    }

    /**
     * Checks if a client is currently in use
     * 
     * @param providerType The provider type
     * @param modelName The model name
     * @return True if the client is in use
     */
    public boolean isClientInUse(ModelProviderType providerType, String modelName) {
        String clientKey = generateClientKey(providerType, modelName);
        ClientUsageInfo usage = clientUsage.get(clientKey);
        return usage != null && !usage.getActiveAgents().isEmpty();
    }

    /**
     * Gets the number of active agents using a specific client
     * 
     * @param providerType The provider type
     * @param modelName The model name
     * @return Number of active agents
     */
    public int getActiveAgentCount(ModelProviderType providerType, String modelName) {
        String clientKey = generateClientKey(providerType, modelName);
        ClientUsageInfo usage = clientUsage.get(clientKey);
        return usage != null ? usage.getActiveAgents().size() : 0;
    }

    /**
     * Gets performance metrics data for a specific client from centralized MetricsService
     * 
     * @param providerType The provider type
     * @param modelName The model name
     * @return Performance metrics map, or empty map if not found
     */
    public Map<String, Object> getClientPerformanceData(ModelProviderType providerType, String modelName) {
        if (metricsService != null) {
            try {
                String clientKey = generateClientKey(providerType, modelName);
                // Use MetricsService to get current snapshot
                MetricKey key = MetricKeys.modelCompletion(clientKey);
                ExecutionMetricsSnapshot snapshot = metricsService.getSnapshot(key, ExecutionMetricsSnapshot.class);

                if (snapshot != null) {
                    Map<String, Object> metrics = new HashMap<>();
                    metrics.put("averageResponseTime", snapshot.averageMs());
                    metrics.put("minResponseTime", 0L); // TODO: Add min/max to ExecutionMetricsSnapshot
                    metrics.put("maxResponseTime", 0L); // TODO: Add min/max to ExecutionMetricsSnapshot
                    metrics.put("errorRate", (1.0 - snapshot.successRate() / 100.0));
                    metrics.put("totalErrors", snapshot.failure());
                    metrics.put("totalRequests", snapshot.total());
                    metrics.put("successfulRequests", snapshot.success());
                    return metrics;
                }
                return new HashMap<>();
            } catch (Exception e) {
                logger.warn("Failed to get client performance data from MetricsService", e);
            }
        }

        // Fallback: Use legacy local data if MetricsService unavailable
        String clientKey = generateClientKey(providerType, modelName);
        List<Long> times = responseTimes.get(clientKey);

        if (times == null || times.isEmpty()) {
            return Map.of();
        }

        long minTime = times.stream().mapToLong(Long::longValue).min().orElse(0);
        long maxTime = times.stream().mapToLong(Long::longValue).max().orElse(0);
        double avgTime = times.stream().mapToLong(Long::longValue).average().orElse(0.0);
        long errors = 0;
        double errorRate = (double) errors / times.size();

        Map<String, Object> metrics = new HashMap<>();
        metrics.put("averageResponseTime", avgTime);
        metrics.put("minResponseTime", minTime);
        metrics.put("maxResponseTime", maxTime);
        metrics.put("errorRate", errorRate);
        metrics.put("totalErrors", errors);
        metrics.put("totalRequests", (long) times.size());
        metrics.put("successfulRequests", (long) times.size() - errors);
        return metrics;
    }

    // Helper methods
    private String generateClientKey(ModelProviderType providerType, String modelName) {
        return providerType.name() + ":" + modelName;
    }

    private void initializeProviderStats() {
        for (ModelProviderType type : ModelProviderType.values()) {
            // TODO: Implement proper ProviderStats initialization
            providerStats.put(type.name() + ":default", new ConcurrentHashMap<>());
        }
    }

    private double calculateAverageResponseTime() {
        List<Long> allTimes = responseTimes.values().stream().flatMap(List::stream).collect(Collectors.toList());
        return allTimes.isEmpty() ? 0.0 : allTimes.stream().mapToLong(Long::longValue).average().orElse(0.0);
    }

    private double calculateErrorRate() {
        // long totalErrors = errorCounts.values().stream().mapToLong(AtomicLong::get).sum();
        return 0.0; // No error counts tracked directly anymore
    }

    // Data classes extracted to top-level:
    // - org.openhab.core.ai.model.ClientUsageInfo
    // - org.openhab.core.ai.model.AgentClientSession
    // - org.openhab.core.ai.model.ProviderUsageStats
    // - org.openhab.core.ai.model.SystemUsageStats
    // - org.openhab.core.ai.model.ClientPerformanceMetrics (removed - replaced with MetricsService integration)
}
