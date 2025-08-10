package org.openhab.core.ai.model;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.model.api.ModelProviderType;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
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
    private final Map<ModelProviderType, ProviderUsageStats> providerStats = new ConcurrentHashMap<>();

    // System-wide statistics
    private final AtomicLong totalRequests = new AtomicLong(0);
    private final AtomicLong totalTokensUsed = new AtomicLong(0);
    private final AtomicLong totalCost = new AtomicLong(0);
    private final AtomicReference<Instant> lastRequestTime = new AtomicReference<>(Instant.now());

    // Performance tracking
    private final Map<String, List<Long>> responseTimes = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> errorCounts = new ConcurrentHashMap<>();

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

            // Update provider statistics
            ProviderUsageStats stats = providerStats.computeIfAbsent(providerType, ProviderUsageStats::new);
            stats.recordUsage(agentId, modelName);

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

            // Update global statistics
            totalRequests.incrementAndGet();
            totalTokensUsed.addAndGet(tokensUsed);
            totalCost.addAndGet((long) (cost * 1000)); // Store as millicents
            lastRequestTime.set(now);

            // Update client usage info
            ClientUsageInfo usageInfo = clientUsage.get(clientKey);
            if (usageInfo != null) {
                usageInfo.recordRequest(tokensUsed, cost, responseTimeMs, success);
            }

            // Update provider statistics
            ProviderUsageStats stats = providerStats.get(providerType);
            if (stats != null) {
                stats.recordRequest(tokensUsed, cost, responseTimeMs, success);
            }

            // Track response times for performance analysis
            responseTimes.computeIfAbsent(clientKey, k -> new ArrayList<>()).add(responseTimeMs);

            // Track error counts
            if (!success) {
                errorCounts.computeIfAbsent(clientKey, k -> new AtomicLong(0)).incrementAndGet();
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
    public Map<ModelProviderType, ProviderUsageStats> getProviderStats() {
        return new HashMap<>(providerStats);
    }

    /**
     * Gets system-wide usage statistics
     * 
     * @return System usage statistics
     */
    public SystemUsageStats getSystemStats() {
        return new SystemUsageStats(totalRequests.get(), totalTokensUsed.get(), totalCost.get() / 1000.0, // Convert
                                                                                                          // from
                                                                                                          // millicents
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
     * Gets performance metrics for a specific client
     * 
     * @param providerType The provider type
     * @param modelName The model name
     * @return Performance metrics, or null if not found
     */
    public @Nullable ClientPerformanceMetrics getClientPerformance(ModelProviderType providerType, String modelName) {
        String clientKey = generateClientKey(providerType, modelName);
        List<Long> times = responseTimes.get(clientKey);
        AtomicLong errorCount = errorCounts.get(clientKey);

        if (times == null || times.isEmpty()) {
            return null;
        }

        long minTime = times.stream().mapToLong(Long::longValue).min().orElse(0);
        long maxTime = times.stream().mapToLong(Long::longValue).max().orElse(0);
        double avgTime = times.stream().mapToLong(Long::longValue).average().orElse(0.0);
        long errors = errorCount != null ? errorCount.get() : 0;
        double errorRate = (double) errors / times.size();

        return new ClientPerformanceMetrics(avgTime, minTime, maxTime, errorRate, errors, times.size());
    }

    // Helper methods
    private String generateClientKey(ModelProviderType providerType, String modelName) {
        return providerType.name() + ":" + modelName;
    }

    private void initializeProviderStats() {
        for (ModelProviderType type : ModelProviderType.values()) {
            providerStats.put(type, new ProviderUsageStats(type));
        }
    }

    private double calculateAverageResponseTime() {
        List<Long> allTimes = responseTimes.values().stream().flatMap(List::stream).collect(Collectors.toList());
        return allTimes.isEmpty() ? 0.0 : allTimes.stream().mapToLong(Long::longValue).average().orElse(0.0);
    }

    private double calculateErrorRate() {
        long totalErrors = errorCounts.values().stream().mapToLong(AtomicLong::get).sum();
        return totalRequests.get() > 0 ? (double) totalErrors / totalRequests.get() : 0.0;
    }

    // Data classes
    public static class ClientUsageInfo {
        private final ModelProviderType providerType;
        private final String modelName;
        private final Map<String, Instant> activeAgents = new ConcurrentHashMap<>();
        private final AtomicLong totalRequests = new AtomicLong(0);
        private final AtomicLong totalTokens = new AtomicLong(0);
        private final AtomicLong totalCost = new AtomicLong(0);
        private final AtomicLong totalResponseTime = new AtomicLong(0);
        private final AtomicLong successfulRequests = new AtomicLong(0);
        private final AtomicLong failedRequests = new AtomicLong(0);
        private final AtomicReference<Instant> lastUsed = new AtomicReference<>(Instant.now());

        public ClientUsageInfo(ModelProviderType providerType, String modelName) {
            this.providerType = providerType;
            this.modelName = modelName;
        }

        public void addAgent(String agentId, Instant time) {
            activeAgents.put(agentId, time);
            lastUsed.set(time);
        }

        public void removeAgent(String agentId) {
            activeAgents.remove(agentId);
        }

        public void recordRequest(int tokens, double cost, long responseTime, boolean success) {
            totalRequests.incrementAndGet();
            totalTokens.addAndGet(tokens);
            totalCost.addAndGet((long) (cost * 1000)); // Store as millicents
            totalResponseTime.addAndGet(responseTime);
            if (success) {
                successfulRequests.incrementAndGet();
            } else {
                failedRequests.incrementAndGet();
            }
            lastUsed.set(Instant.now());
        }

        // Getters
        public ModelProviderType getProviderType() {
            return providerType;
        }

        public String getModelName() {
            return modelName;
        }

        public Map<String, Instant> getActiveAgents() {
            return new HashMap<>(activeAgents);
        }

        public long getTotalRequests() {
            return totalRequests.get();
        }

        public long getTotalTokens() {
            return totalTokens.get();
        }

        public double getTotalCost() {
            return totalCost.get() / 1000.0; // Convert from millicents
        }

        public long getTotalResponseTime() {
            return totalResponseTime.get();
        }

        public long getSuccessfulRequests() {
            return successfulRequests.get();
        }

        public long getFailedRequests() {
            return failedRequests.get();
        }

        public Instant getLastUsed() {
            return lastUsed.get();
        }

        public double getSuccessRate() {
            long total = totalRequests.get();
            return total > 0 ? (double) successfulRequests.get() / total : 0.0;
        }

        public double getAverageResponseTime() {
            long total = totalRequests.get();
            return total > 0 ? (double) totalResponseTime.get() / total : 0.0;
        }
    }

    public static class AgentClientSession {
        private final String agentId;
        private final ModelProviderType providerType;
        private final String modelName;
        private final @Nullable String sessionId;
        private final AtomicReference<Instant> lastUsed = new AtomicReference<>(Instant.now());
        private final AtomicReference<Boolean> active = new AtomicReference<>(true);

        public AgentClientSession(String agentId, ModelProviderType providerType, String modelName,
                @Nullable String sessionId) {
            this.agentId = agentId;
            this.providerType = providerType;
            this.modelName = modelName;
            this.sessionId = sessionId;
        }

        public void updateLastUsed(Instant time) {
            lastUsed.set(time);
        }

        public void markInactive() {
            active.set(false);
        }

        // Getters
        public String getAgentId() {
            return agentId;
        }

        public ModelProviderType getProviderType() {
            return providerType;
        }

        public String getModelName() {
            return modelName;
        }

        public @Nullable String getSessionId() {
            return sessionId;
        }

        public Instant getLastUsed() {
            return lastUsed.get();
        }

        public boolean isActive() {
            return active.get();
        }

        public Duration getSessionDuration() {
            return Duration.between(lastUsed.get(), Instant.now());
        }
    }

    public static class ProviderUsageStats {
        private final ModelProviderType providerType;
        private final Map<String, Integer> modelUsage = new ConcurrentHashMap<>();
        private final Map<String, Integer> agentUsage = new ConcurrentHashMap<>();
        private final AtomicLong totalRequests = new AtomicLong(0);
        private final AtomicLong totalTokens = new AtomicLong(0);
        private final AtomicLong totalCost = new AtomicLong(0);
        private final AtomicLong totalResponseTime = new AtomicLong(0);
        private final AtomicLong successfulRequests = new AtomicLong(0);
        private final AtomicLong failedRequests = new AtomicLong(0);

        public ProviderUsageStats() {
            this.providerType = null;
        }

        public ProviderUsageStats(ModelProviderType providerType) {
            this.providerType = providerType;
        }

        public void recordUsage(String agentId, String modelName) {
            modelUsage.merge(modelName, 1, Integer::sum);
            agentUsage.merge(agentId, 1, Integer::sum);
        }

        public void recordRequest(int tokens, double cost, long responseTime, boolean success) {
            totalRequests.incrementAndGet();
            totalTokens.addAndGet(tokens);
            totalCost.addAndGet((long) (cost * 1000)); // Store as millicents
            totalResponseTime.addAndGet(responseTime);
            if (success) {
                successfulRequests.incrementAndGet();
            } else {
                failedRequests.incrementAndGet();
            }
        }

        // Getters
        public ModelProviderType getProviderType() {
            return providerType;
        }

        public Map<String, Integer> getModelUsage() {
            return new HashMap<>(modelUsage);
        }

        public Map<String, Integer> getAgentUsage() {
            return new HashMap<>(agentUsage);
        }

        public long getTotalRequests() {
            return totalRequests.get();
        }

        public long getTotalTokens() {
            return totalTokens.get();
        }

        public double getTotalCost() {
            return totalCost.get() / 1000.0; // Convert from millicents
        }

        public long getTotalResponseTime() {
            return totalResponseTime.get();
        }

        public long getSuccessfulRequests() {
            return successfulRequests.get();
        }

        public long getFailedRequests() {
            return failedRequests.get();
        }

        public double getSuccessRate() {
            long total = totalRequests.get();
            return total > 0 ? (double) successfulRequests.get() / total : 0.0;
        }

        public double getAverageResponseTime() {
            long total = totalRequests.get();
            return total > 0 ? (double) totalResponseTime.get() / total : 0.0;
        }
    }

    public static class SystemUsageStats {
        private final long totalRequests;
        private final long totalTokens;
        private final double totalCost;
        private final Instant lastRequestTime;
        private final double averageResponseTime;
        private final double errorRate;

        public SystemUsageStats(long totalRequests, long totalTokens, double totalCost, Instant lastRequestTime,
                double averageResponseTime, double errorRate) {
            this.totalRequests = totalRequests;
            this.totalTokens = totalTokens;
            this.totalCost = totalCost;
            this.lastRequestTime = lastRequestTime;
            this.averageResponseTime = averageResponseTime;
            this.errorRate = errorRate;
        }

        // Getters
        public long getTotalRequests() {
            return totalRequests;
        }

        public long getTotalTokens() {
            return totalTokens;
        }

        public double getTotalCost() {
            return totalCost;
        }

        public Instant getLastRequestTime() {
            return lastRequestTime;
        }

        public double getAverageResponseTime() {
            return averageResponseTime;
        }

        public double getErrorRate() {
            return errorRate;
        }
    }

    public static class ClientPerformanceMetrics {
        private final double averageResponseTime;
        private final long minResponseTime;
        private final long maxResponseTime;
        private final double errorRate;
        private final long totalErrors;
        private final long totalRequests;

        public ClientPerformanceMetrics(double averageResponseTime, long minResponseTime, long maxResponseTime,
                double errorRate, long totalErrors, long totalRequests) {
            this.averageResponseTime = averageResponseTime;
            this.minResponseTime = minResponseTime;
            this.maxResponseTime = maxResponseTime;
            this.errorRate = errorRate;
            this.totalErrors = totalErrors;
            this.totalRequests = totalRequests;
        }

        // Getters
        public double getAverageResponseTime() {
            return averageResponseTime;
        }

        public long getMinResponseTime() {
            return minResponseTime;
        }

        public long getMaxResponseTime() {
            return maxResponseTime;
        }

        public double getErrorRate() {
            return errorRate;
        }

        public long getTotalErrors() {
            return totalErrors;
        }

        public long getTotalRequests() {
            return totalRequests;
        }
    }
}
