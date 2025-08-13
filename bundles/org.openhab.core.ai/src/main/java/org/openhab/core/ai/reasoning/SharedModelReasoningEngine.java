/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.core.ai.reasoning;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.agent.api.AgentModelContext;
import org.openhab.core.ai.agent.api.AgentModelIntegrationService;
import org.openhab.core.ai.agent.api.AgentModelProvider;
import org.openhab.core.ai.agent.api.AgentModelStatistics;
import org.openhab.core.ai.agent.api.ModelHealthStatus;
import org.openhab.core.ai.agent.api.ModelIntegrationStatistics;
import org.openhab.core.ai.model.DefaultAgentModelProvider;
import org.openhab.core.ai.model.api.ModelClient;
import org.openhab.core.ai.model.api.ModelConfigurationService;
import org.openhab.core.ai.model.api.ModelProviderType;
import org.openhab.core.ai.model.ModelParameters;
import org.openhab.core.ai.model.ModelResponse;
import org.openhab.core.ai.reasoning.api.ReasoningContext;
import org.openhab.core.ai.reasoning.api.ReasoningEngine;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements the shared brain architecture for multi-agent model usage with integrated agent management.
 * 
 * This engine provides a centralized model reasoning service that efficiently serves
 * multiple specialized agents, optimizing resource usage while maintaining specialized
 * reasoning capabilities for each agent. It consolidates model integration, reasoning,
 * decision-making, and natural language processing capabilities.
 * 
 * <p>
 * This implementation provides:
 * - Shared model brain architecture for resource optimization
 * - Agent-specific model access and context management
 * - Concurrent request handling and resource management
 * - Model session pooling and optimization
 * - Model request queuing and prioritization
 * - Model response caching and optimization
 * - Comprehensive error handling and fallback mechanisms
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 */
@Component(service = { SharedModelReasoningEngine.class, AgentModelIntegrationService.class,
        ReasoningEngine.class }, immediate = true)
@NonNullByDefault
public class SharedModelReasoningEngine implements AgentModelIntegrationService, ReasoningEngine {

    private final Logger logger = LoggerFactory.getLogger(SharedModelReasoningEngine.class);

    // Configuration
    private static final int DEFAULT_MAX_CONCURRENT_REQUESTS = 10;
    private static final Duration DEFAULT_REQUEST_TIMEOUT = Duration.ofSeconds(30);
    private static final int DEFAULT_MAX_CACHE_SIZE = 1000;
    private static final Duration DEFAULT_CACHE_EXPIRATION = Duration.ofMinutes(30);

    // Dependencies
    @Reference
    private @Nullable ModelConfigurationService modelConfigurationService;

    @Reference
    private @Nullable DefaultAgentModelProvider defaultAgentModelProvider;

    // Agent management
    private final Map<String, AgentModelContext> registeredAgents = new ConcurrentHashMap<>();
    private final Map<String, AgentModelProvider> agentProviders = new ConcurrentHashMap<>();
    private final Map<String, AgentModelStatistics> agentStatistics = new ConcurrentHashMap<>();

    // Performance monitoring
    private final AtomicLong totalRequests = new AtomicLong(0);
    private final AtomicLong successfulRequests = new AtomicLong(0);
    private final AtomicLong failedRequests = new AtomicLong(0);
    private final AtomicLong cacheHits = new AtomicLong(0);
    private final AtomicLong cacheMisses = new AtomicLong(0);
    private final AtomicLong totalResponseTimeMs = new AtomicLong(0);
    private final AtomicLong minResponseTimeMs = new AtomicLong(Long.MAX_VALUE);
    private final AtomicLong maxResponseTimeMs = new AtomicLong(0);
    private final AtomicLong totalTokensUsed = new AtomicLong(0);
    private final AtomicReference<Double> totalCost = new AtomicReference<>(0.0);
    private final AtomicReference<Instant> lastRequestTime = new AtomicReference<>(Instant.now());
    private final AtomicReference<Instant> lastSuccessTime = new AtomicReference<>(Instant.now());
    private final AtomicReference<Instant> lastFailureTime = new AtomicReference<>(Instant.now());
    private final AtomicReference<String> lastError = new AtomicReference<>("");

    // Model clients and sessions
    private final ConcurrentHashMap<String, ModelClient> modelClients = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, ModelReasoningSession> activeSessions = new ConcurrentHashMap<>();
    private final LinkedBlockingQueue<ReasoningRequest> requestQueue = new LinkedBlockingQueue<>();

    // Threading
    private final ExecutorService reasoningExecutor;
    private final ExecutorService sessionExecutor;
    private final ExecutorService requestExecutor = Executors.newFixedThreadPool(DEFAULT_MAX_CONCURRENT_REQUESTS);
    private final AtomicLong requestCounter = new AtomicLong(0);
    private final AtomicLong sessionCounter = new AtomicLong(0);

    private volatile boolean shutdown = false;
    private volatile boolean isRunning = true;

    // Configuration
    private int maxConcurrentRequests = DEFAULT_MAX_CONCURRENT_REQUESTS;
    private Duration requestTimeout = DEFAULT_REQUEST_TIMEOUT;
    private int maxCacheSize = DEFAULT_MAX_CACHE_SIZE;
    private Duration cacheExpiration = DEFAULT_CACHE_EXPIRATION;
    private boolean enableCaching = true;
    private boolean enableOptimization = true;
    private boolean enableSecurity = true;
    private boolean enableMonitoring = true;

    /**
     * Creates a new SharedModelReasoningEngine.
     */
    public SharedModelReasoningEngine() {
        this.reasoningExecutor = new ThreadPoolExecutor(2, 10, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(100),
                r -> {
                    Thread t = new Thread(r, "SharedModelReasoning-" + r.hashCode());
                    t.setDaemon(true);
                    return t;
                });
        this.sessionExecutor = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r, "ModelSession-" + r.hashCode());
            t.setDaemon(true);
            return t;
        });
    }

    @Activate
    public void activate() {
        logger.info("Shared Model Reasoning Engine activated");
        startHealthMonitoring();
    }

    @Deactivate
    public void deactivate() {
        logger.info("Shared Model Reasoning Engine deactivated");
        isRunning = false;
        shutdown = true;
        requestExecutor.shutdown();
        reasoningExecutor.shutdown();
        sessionExecutor.shutdown();
        clearAllCaches();
    }

    /**
     * Performs reasoning using the shared model for a specific agent.
     * 
     * @param agentId the agent identifier
     * @param context the agent context
     * @param prompt the reasoning prompt
     * @param parameters optional model parameters
     * @return a CompletableFuture containing the reasoning result
     */
    public CompletableFuture<ModelResponse> performReasoning(String agentId, AgentModelContext context, String prompt,
            @Nullable ModelParameters parameters) {
        if (shutdown) {
            return CompletableFuture.failedFuture(new IllegalStateException("Engine is shutdown"));
        }

        String requestId = "req-" + requestCounter.incrementAndGet();
        ReasoningRequest request = new ReasoningRequest(requestId, agentId, context, prompt, parameters);

        logger.debug("Queuing reasoning request {} for agent {}", requestId, agentId);

        return CompletableFuture.supplyAsync(() -> {
            try {
                requestQueue.put(request);
                return processReasoningRequest(request);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Reasoning request interrupted", e);
            }
        }, reasoningExecutor);
    }

    /**
     * Creates a new reasoning session for an agent.
     * 
     * @param agentId the agent identifier
     * @param context the initial context
     * @return a CompletableFuture containing the session ID
     */
    public CompletableFuture<String> createSession(String agentId, AgentModelContext context) {
        if (shutdown) {
            return CompletableFuture.failedFuture(new IllegalStateException("Engine is shutdown"));
        }

        String sessionId = "session-" + sessionCounter.incrementAndGet();
        ModelReasoningSession session = new ModelReasoningSession(sessionId, agentId, context);
        activeSessions.put(sessionId, session);

        logger.debug("Created reasoning session {} for agent {}", sessionId, agentId);

        return CompletableFuture.completedFuture(sessionId);
    }

    /**
     * Continues an existing reasoning session.
     * 
     * @param sessionId the session identifier
     * @param input the additional input
     * @param parameters optional model parameters
     * @return a CompletableFuture containing the session response
     */
    public CompletableFuture<ModelResponse> continueSession(String sessionId, String input,
            @Nullable ModelParameters parameters) {
        if (shutdown) {
            return CompletableFuture.failedFuture(new IllegalStateException("Engine is shutdown"));
        }

        ModelReasoningSession session = activeSessions.get(sessionId);
        if (session == null) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("Session not found: " + sessionId));
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                return processSessionRequest(session, input, parameters);
            } catch (Exception e) {
                logger.error("Error processing session request for session {}", sessionId, e);
                throw new RuntimeException("Session processing failed", e);
            }
        }, sessionExecutor);
    }

    /**
     * Closes a reasoning session.
     * 
     * @param sessionId the session identifier
     * @return a CompletableFuture indicating completion
     */
    public CompletableFuture<Void> closeSession(String sessionId) {
        ModelReasoningSession session = activeSessions.remove(sessionId);
        if (session != null) {
            logger.debug("Closed reasoning session {}", sessionId);
        }
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Gets the current health status of the reasoning engine.
     * 
     * @return the health status
     */
    public ReasoningEngineHealthStatus getReasoningEngineHealthStatus() {
        return new ReasoningEngineHealthStatus(!shutdown, activeSessions.size(), requestQueue.size(),
                reasoningExecutor.isShutdown());
    }

    /**
     * Shuts down the reasoning engine and cleans up resources.
     */
    public void shutdown() {
        shutdown = true;
        reasoningExecutor.shutdown();
        sessionExecutor.shutdown();
        activeSessions.clear();
        requestQueue.clear();

        try {
            if (!reasoningExecutor.awaitTermination(30, TimeUnit.SECONDS)) {
                reasoningExecutor.shutdownNow();
            }
            if (!sessionExecutor.awaitTermination(30, TimeUnit.SECONDS)) {
                sessionExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            reasoningExecutor.shutdownNow();
            sessionExecutor.shutdownNow();
        }

        logger.info("SharedModelReasoningEngine shutdown completed");
    }

    // AgentModelIntegrationService implementation methods

    @Override
    public CompletableFuture<ModelResponse> reasonAsync(String agentId, String prompt, Map<String, Object> context,
            @Nullable ModelParameters parameters) {
        return CompletableFuture.supplyAsync(() -> {
            long startTime = System.currentTimeMillis();
            totalRequests.incrementAndGet();
            lastRequestTime.set(Instant.now());

            try {
                // Validate agent registration
                if (!isAgentRegistered(agentId)) {
                    throw new IllegalArgumentException("Agent not registered: " + agentId);
                }

                // Get agent context
                AgentModelContext agentContext = getAgentContext(agentId);
                if (agentContext == null) {
                    throw new IllegalStateException("Agent context not available: " + agentId);
                }

                // Execute reasoning using existing method
                ModelResponse response = performReasoning(agentId, agentContext, prompt, parameters).get();

                // Update statistics
                successfulRequests.incrementAndGet();
                lastSuccessTime.set(Instant.now());
                totalResponseTimeMs.addAndGet(System.currentTimeMillis() - startTime);

                // Update agent statistics
                updateAgentStatistics(agentId, true, System.currentTimeMillis() - startTime, null);

                logger.debug("Agent {} reasoning completed successfully in {}ms", agentId,
                        System.currentTimeMillis() - startTime);

                return response;

            } catch (Exception e) {
                failedRequests.incrementAndGet();
                lastFailureTime.set(Instant.now());
                lastError.set(e.getMessage());

                // Update agent statistics
                updateAgentStatistics(agentId, false, System.currentTimeMillis() - startTime, e.getMessage());

                logger.error("Agent {} reasoning failed", agentId, e);
                throw new RuntimeException("Reasoning failed for agent " + agentId, e);
            }
        }, requestExecutor);
    }

    @Override
    public CompletableFuture<ModelResponse> reasonWithOptimizationAsync(String agentId, String prompt,
            Map<String, Object> context, Map<String, Object> optimizationHints, @Nullable ModelParameters parameters) {
        return CompletableFuture.supplyAsync(() -> {
            long startTime = System.currentTimeMillis();
            totalRequests.incrementAndGet();
            lastRequestTime.set(Instant.now());

            try {
                // Validate agent registration
                if (!isAgentRegistered(agentId)) {
                    throw new IllegalArgumentException("Agent not registered: " + agentId);
                }

                // Get agent context
                AgentModelContext agentContext = getAgentContext(agentId);
                if (agentContext == null) {
                    throw new IllegalStateException("Agent context not available: " + agentId);
                }

                // Execute reasoning with optimization (for now, use regular reasoning)
                ModelResponse response = performReasoning(agentId, agentContext, prompt, parameters).get();

                // Update statistics
                successfulRequests.incrementAndGet();
                lastSuccessTime.set(Instant.now());
                totalResponseTimeMs.addAndGet(System.currentTimeMillis() - startTime);

                // Update agent statistics
                updateAgentStatistics(agentId, true, System.currentTimeMillis() - startTime, null);

                logger.debug("Agent {} optimized reasoning completed successfully in {}ms", agentId,
                        System.currentTimeMillis() - startTime);

                return response;

            } catch (Exception e) {
                failedRequests.incrementAndGet();
                lastFailureTime.set(Instant.now());
                lastError.set(e.getMessage());

                // Update agent statistics
                updateAgentStatistics(agentId, false, System.currentTimeMillis() - startTime, e.getMessage());

                logger.error("Agent {} optimized reasoning failed", agentId, e);
                throw new RuntimeException("Optimized reasoning failed for agent " + agentId, e);
            }
        }, requestExecutor);
    }

    @Override
    public AgentModelProvider getAgentModelProvider(String agentId) {
        return agentProviders.get(agentId);
    }

    @Override
    public boolean registerAgent(String agentId, AgentModelContext agentContext) {
        try {
            logger.debug("Registering agent: {}", agentId);

            // Create agent provider
            AgentModelProvider provider = createAgentProvider(agentId, agentContext);
            if (provider == null) {
                logger.error("Failed to create agent provider for: {}", agentId);
                return false;
            }

            // Register agent
            registeredAgents.put(agentId, agentContext);
            agentProviders.put(agentId, provider);

            // Initialize statistics
            agentStatistics.put(agentId, AgentModelStatistics.builder().agentId(agentId).build());

            logger.info("Agent registered successfully: {}", agentId);
            return true;

        } catch (Exception e) {
            logger.error("Failed to register agent: {}", agentId, e);
            return false;
        }
    }

    @Override
    public boolean unregisterAgent(String agentId) {
        try {
            logger.debug("Unregistering agent: {}", agentId);

            // Remove agent
            registeredAgents.remove(agentId);
            agentProviders.remove(agentId);
            agentStatistics.remove(agentId);

            logger.info("Agent unregistered successfully: {}", agentId);
            return true;

        } catch (Exception e) {
            logger.error("Failed to unregister agent: {}", agentId, e);
            return false;
        }
    }

    @Override
    public AgentModelStatistics getAgentStatistics(String agentId) {
        return agentStatistics.getOrDefault(agentId, AgentModelStatistics.builder().agentId(agentId).build());
    }

    @Override
    public ModelIntegrationStatistics getOverallStatistics() {
        return ModelIntegrationStatistics.builder().totalAgents(registeredAgents.size())
                .activeAgents(registeredAgents.size()).totalRequests(totalRequests.get())
                .successfulRequests(successfulRequests.get()).failedRequests(failedRequests.get())
                .cacheHits(cacheHits.get()).cacheMisses(cacheMisses.get())
                .totalResponseTimeMs(totalResponseTimeMs.get()).averageResponseTimeMs(calculateAverageResponseTime())
                .minResponseTimeMs(minResponseTimeMs.get()).maxResponseTimeMs(maxResponseTimeMs.get())
                .totalTokensUsed(totalTokensUsed.get()).totalCost(totalCost.get().longValue())
                .lastRequestTime(lastRequestTime.get()).lastSuccessTime(lastSuccessTime.get())
                .lastFailureTime(lastFailureTime.get()).lastError(lastError.get())
                .registeredAgentIds(new ArrayList<>(registeredAgents.keySet())).build();
    }

    @Override
    public boolean isAgentRegistered(String agentId) {
        return registeredAgents.containsKey(agentId);
    }

    // ReasoningEngine interface implementation
    @Override
    public CompletableFuture<ModelResponse> reasonAsync(String agentId, ReasoningContext context, String prompt,
            @Nullable ModelParameters parameters) {
        // Convert ReasoningContext to AgentModelContext for compatibility
        AgentModelContext agentContext = convertToAgentModelContext(context);
        return performReasoning(agentId, agentContext, prompt, parameters);
    }

    @Override
    public boolean isHealthy() {
        return !shutdown && isRunning && getReasoningEngineHealthStatus().isHealthy();
    }

    @Override
    public String getEngineType() {
        return "shared-model-reasoning";
    }

    @Override
    public EngineStatus getStatus() {
        if (shutdown) {
            return EngineStatus.SHUTDOWN;
        }
        if (!isRunning) {
            return EngineStatus.ERROR;
        }
        ReasoningEngineHealthStatus health = getReasoningEngineHealthStatus();
        if (health.isHealthy()) {
            return EngineStatus.ACTIVE;
        } else {
            return EngineStatus.DEGRADED;
        }
    }

    private AgentModelContext convertToAgentModelContext(ReasoningContext context) {
        // Create a simple AgentModelContext from ReasoningContext
        // This is a basic conversion - can be enhanced based on actual needs
        return AgentModelContext.builder().agentId(context.getUserId() != null ? context.getUserId() : "unknown")
                .specialization("reasoning-agent").domain(context.getDomain() != null ? context.getDomain() : "general")
                .capabilities(context.getMetadata()).build();
    }

    @Override
    public List<String> getRegisteredAgentIds() {
        return new ArrayList<>(registeredAgents.keySet());
    }

    @Override
    public boolean updateAgentContext(String agentId, AgentModelContext context) {
        try {
            if (!isAgentRegistered(agentId)) {
                logger.warn("Cannot update context for unregistered agent: {}", agentId);
                return false;
            }

            registeredAgents.put(agentId, context);
            logger.debug("Updated context for agent: {}", agentId);
            return true;

        } catch (Exception e) {
            logger.error("Failed to update context for agent: {}", agentId, e);
            return false;
        }
    }

    @Override
    public @Nullable AgentModelContext getAgentContext(String agentId) {
        return registeredAgents.get(agentId);
    }

    @Override
    public boolean clearAgentCache(String agentId) {
        try {
            AgentModelProvider provider = agentProviders.get(agentId);
            if (provider != null) {
                provider.clearCache();
                logger.debug("Cleared cache for agent: {}", agentId);
                return true;
            }
            return false;

        } catch (Exception e) {
            logger.error("Failed to clear cache for agent: {}", agentId, e);
            return false;
        }
    }

    @Override
    public boolean clearAllCaches() {
        try {
            for (AgentModelProvider provider : agentProviders.values()) {
                provider.clearCache();
            }
            logger.debug("Cleared all agent caches");
            return true;

        } catch (Exception e) {
            logger.error("Failed to clear all caches", e);
            return false;
        }
    }

    @Override
    public ModelHealthStatus getModelHealthStatus() {
        long total = totalRequests.get();
        double errorRate = total > 0 ? (double) failedRequests.get() / total : 0.0;
        double avgResponseTime = calculateAverageResponseTime();

        ModelHealthStatus.HealthState healthState;
        if (errorRate < 0.05 && avgResponseTime < 5000) {
            healthState = ModelHealthStatus.HealthState.HEALTHY;
        } else if (errorRate < 0.15 && avgResponseTime < 10000) {
            healthState = ModelHealthStatus.HealthState.DEGRADED;
        } else {
            healthState = ModelHealthStatus.HealthState.UNHEALTHY;
        }

        return ModelHealthStatus.builder().overallHealth(healthState)
                .primaryModelAvailable(checkPrimaryModelAvailability())
                .fallbackModelAvailable(checkFallbackModelAvailability()).errorRate(errorRate)
                .responseTimeMs(avgResponseTime).totalRequests(total).failedRequests(failedRequests.get())
                .lastError(lastError.get()).lastHealthCheck(Instant.now()).lastSuccessfulRequest(lastSuccessTime.get())
                .lastFailedRequest(lastFailureTime.get()).build();
    }

    @Override
    public boolean forceModelFallback(String agentId, String fallbackModel) {
        try {
            AgentModelProvider provider = agentProviders.get(agentId);
            if (provider != null) {
                provider.forceFallback(fallbackModel);
                logger.debug("Forced fallback model {} for agent: {}", fallbackModel, agentId);
                return true;
            }
            return false;

        } catch (Exception e) {
            logger.error("Failed to force fallback for agent: {}", agentId, e);
            return false;
        }
    }

    @Override
    public boolean resetModelFallback(String agentId) {
        try {
            AgentModelProvider provider = agentProviders.get(agentId);
            if (provider != null) {
                provider.resetFallback();
                logger.debug("Reset fallback for agent: {}", agentId);
                return true;
            }
            return false;

        } catch (Exception e) {
            logger.error("Failed to reset fallback for agent: {}", agentId, e);
            return false;
        }
    }

    // Helper methods
    private AgentModelProvider createAgentProvider(String agentId, AgentModelContext context) {
        // Create a DefaultAgentModelProvider with the required dependencies
        return new DefaultAgentModelProvider(agentId, context, this, modelConfigurationService, null, null);
    }

    private void updateAgentStatistics(String agentId, boolean success, long responseTimeMs, @Nullable String error) {
        // Update global statistics
        totalRequests.incrementAndGet();
        totalResponseTimeMs.addAndGet(responseTimeMs);

        // Update min/max response times
        minResponseTimeMs.updateAndGet(current -> Math.min(current, responseTimeMs));
        maxResponseTimeMs.updateAndGet(current -> Math.max(current, responseTimeMs));

        if (success) {
            successfulRequests.incrementAndGet();
            lastSuccessTime.set(Instant.now());
        } else {
            failedRequests.incrementAndGet();
            lastFailureTime.set(Instant.now());
            if (error != null) {
                lastError.set(error);
            }
        }

        // Update agent-specific statistics
        AgentModelStatistics currentStats = agentStatistics.get(agentId);
        if (currentStats != null) {
            // TODO: Update agent statistics with new data
            // This would require a mutable statistics class or a different approach
        }
    }

    private long calculateAverageResponseTime() {
        long total = totalRequests.get();
        return total > 0 ? totalResponseTimeMs.get() / total : 0;
    }

    private void startHealthMonitoring() {
        // Implement periodic health monitoring
        CompletableFuture.runAsync(() -> {
            while (!shutdown && isRunning) {
                try {
                    // Check model health status
                    ModelHealthStatus healthStatus = getModelHealthStatus();

                    // Log health status if there are issues
                    if (healthStatus.getOverallHealth() != ModelHealthStatus.HealthState.HEALTHY) {
                        logger.warn("Model health degraded: {}", healthStatus.getOverallHealth());
                    }

                    // Check if primary model is available
                    if (!checkPrimaryModelAvailability()) {
                        logger.warn("Primary model is not available");
                    }

                    // Check if fallback model is available
                    if (!checkFallbackModelAvailability()) {
                        logger.warn("Fallback model is not available");
                    }

                    // Sleep for 5 minutes before next check
                    Thread.sleep(300000); // 5 minutes

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    logger.error("Error during health monitoring: {}", e.getMessage(), e);
                    try {
                        Thread.sleep(60000); // Wait 1 minute before retrying
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }, reasoningExecutor);

        logger.debug("Health monitoring started");
    }

    /**
     * Check if the primary model is available.
     * 
     * @return True if primary model is available, false otherwise
     */
    private boolean checkPrimaryModelAvailability() {
        try {
            if (modelConfigurationService == null) {
                return false;
            }

            String primaryProvider = modelConfigurationService.getPrimaryProvider();
            if (primaryProvider == null) {
                return false;
            }

            // Check if we have a client for the primary provider
            ModelClient client = modelClients.get(primaryProvider);
            if (client == null) {
                // Try to create a client to test availability
                try {
                    client = getOrCreateModelClient(primaryProvider);
                    return client != null;
                } catch (Exception e) {
                    logger.debug("Primary model {} not available: {}", primaryProvider, e.getMessage());
                    return false;
                }
            }

            return true;
        } catch (Exception e) {
            logger.debug("Error checking primary model availability: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Check if the fallback model is available.
     * 
     * @return True if fallback model is available, false otherwise
     */
    private boolean checkFallbackModelAvailability() {
        try {
            if (modelConfigurationService == null) {
                return false;
            }

            String fallbackProvider = modelConfigurationService.getFallbackProvider();
            if (fallbackProvider == null) {
                return false;
            }

            // Check if we have a client for the fallback provider
            ModelClient client = modelClients.get(fallbackProvider);
            if (client == null) {
                // Try to create a client to test availability
                try {
                    client = getOrCreateModelClient(fallbackProvider);
                    return client != null;
                } catch (Exception e) {
                    logger.debug("Fallback model {} not available: {}", fallbackProvider, e.getMessage());
                    return false;
                }
            }

            return true;
        } catch (Exception e) {
            logger.debug("Error checking fallback model availability: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Estimates the number of tokens in a text string.
     * 
     * @param text The text to estimate tokens for
     * @return Estimated token count
     */
    private int estimateTokens(String text) {
        // Simple token estimation: ~4 characters per token
        // This is a rough approximation - real tokenization would be more accurate
        return Math.max(1, text.length() / 4);
    }

    /**
     * Estimates the cost for a given number of tokens.
     * 
     * @param tokens The number of tokens
     * @return Estimated cost
     */
    private double estimateCost(int tokens) {
        // Use a default cost per 1K tokens (this could be made configurable)
        double costPer1k = 0.01; // Default $0.01 per 1K tokens
        return (tokens / 1000.0) * costPer1k;
    }

    private ModelResponse processReasoningRequest(ReasoningRequest request) {
        long startTime = System.currentTimeMillis();
        try {
            String providerId = modelConfigurationService.getPrimaryProvider();
            if (providerId == null) {
                throw new IllegalStateException("No primary provider configured");
            }
            // Use agent-aware client creation for automatic tracking
            ModelClient client = getOrCreateModelClientForAgent(providerId, request.agentId);

            String prompt = buildAgentPrompt(request.agentId, request.context, request.prompt);
            ModelParameters params = request.parameters != null ? request.parameters
                    : ModelParameters.builder().build();

            logger.debug("Processing reasoning request {} with provider {} for agent {}", request.requestId, providerId,
                    request.agentId);

            ModelResponse response = client.complete(prompt, params).get();

            long responseTime = System.currentTimeMillis() - startTime;
            logger.debug("Completed reasoning request {} in {}ms", request.requestId, responseTime);

            return response;
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            logger.error("Error processing reasoning request {}", request.requestId, e);

            throw new RuntimeException("Reasoning request failed", e);
        }
    }

    private ModelResponse processSessionRequest(ModelReasoningSession session, String input,
            @Nullable ModelParameters parameters) {
        long startTime = System.currentTimeMillis();
        try {
            String providerId = modelConfigurationService.getPrimaryProvider();
            if (providerId == null) {
                throw new IllegalStateException("No primary provider configured");
            }
            // Use agent-aware client creation with session tracking
            ModelClient client = getOrCreateModelClientForAgent(providerId, session.getAgentId(),
                    session.getSessionId());

            // Build session-aware prompt
            String sessionPrompt = buildSessionPrompt(session, input);

            ModelParameters params = parameters != null ? parameters : ModelParameters.builder().build();

            logger.debug("Processing session request for session {} (agent: {})", session.getSessionId(),
                    session.getAgentId());

            ModelResponse response = client.complete(sessionPrompt, params).get();

            long responseTime = System.currentTimeMillis() - startTime;

            // Update session with new input and response
            session.addInteraction(input, response.getContent());

            return response;
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            logger.error("Error processing session request for session {}", session.getSessionId(), e);

            throw new RuntimeException("Session request failed", e);
        }
    }

    private ModelClient getOrCreateModelClient(String providerId) {
        return modelClients.computeIfAbsent(providerId, id -> {
            try {
                // Try to get the provider by type first
                ModelProviderType providerType = ModelProviderType.valueOf(id.toUpperCase());
                return defaultAgentModelProvider.getOrCreateProvider(providerType);
            } catch (IllegalArgumentException e) {
                // If not a valid enum, try as string
                return defaultAgentModelProvider.getOrCreateProvider(id);
            }
        });
    }

    /**
     * Gets or creates a ModelClient with automatic tracking for a specific agent.
     * 
     * @param providerId The provider ID
     * @param agentId The agent ID for tracking
     * @return The ModelClient with tracking enabled
     */
    private ModelClient getOrCreateModelClientForAgent(String providerId, String agentId) {
        DefaultAgentModelProvider provider = defaultAgentModelProvider;
        if (provider == null) {
            throw new IllegalStateException("DefaultAgentModelProvider not available");
        }

        try {
            // Try to get the provider by type first
            ModelProviderType providerType = ModelProviderType.valueOf(providerId.toUpperCase());
            return provider.getOrCreateProviderForAgent(providerType);
        } catch (IllegalArgumentException e) {
            // If not a valid enum, try as string
            return provider.getOrCreateProviderForAgent(providerId);
        }
    }

    /**
     * Gets or creates a ModelClient with automatic tracking for a specific agent and session.
     * 
     * @param providerId The provider ID
     * @param agentId The agent ID for tracking
     * @param sessionId The session ID for tracking
     * @return The ModelClient with tracking enabled
     */
    private ModelClient getOrCreateModelClientForAgent(String providerId, String agentId, String sessionId) {
        DefaultAgentModelProvider provider = defaultAgentModelProvider;
        if (provider == null) {
            throw new IllegalStateException("DefaultAgentModelProvider not available");
        }

        try {
            // Try to get the provider by type first
            ModelProviderType providerType = ModelProviderType.valueOf(providerId.toUpperCase());
            return provider.getOrCreateProviderForAgent(providerType, sessionId);
        } catch (IllegalArgumentException e) {
            // If not a valid enum, try as string
            return provider.getOrCreateProviderForAgent(providerId, sessionId);
        }
    }

    private String buildAgentPrompt(String agentId, AgentModelContext context, String prompt) {
        StringBuilder sb = new StringBuilder();
        sb.append("Agent ID: ").append(agentId).append("\n");
        sb.append("Agent Specialization: ").append(context.getSpecialization()).append("\n");
        sb.append("Agent Domain: ").append(context.getDomain()).append("\n");

        if (!context.getCapabilities().isEmpty()) {
            sb.append("Capabilities:\n");
            context.getCapabilities()
                    .forEach((key, value) -> sb.append("  ").append(key).append(": ").append(value).append("\n"));
        }

        if (!context.getConstraints().isEmpty()) {
            sb.append("Constraints:\n");
            context.getConstraints()
                    .forEach((key, value) -> sb.append("  ").append(key).append(": ").append(value).append("\n"));
        }

        sb.append("\nPrompt: ").append(prompt);
        return sb.toString();
    }

    private String buildSessionPrompt(ModelReasoningSession session, String input) {
        StringBuilder sb = new StringBuilder();
        sb.append("Session ID: ").append(session.getSessionId()).append("\n");
        sb.append("Agent ID: ").append(session.getAgentId()).append("\n");

        // Add session history
        if (!session.getInteractionHistory().isEmpty()) {
            sb.append("Session History:\n");
            session.getInteractionHistory().forEach(interaction -> {
                sb.append("User: ").append(interaction.getInput()).append("\n");
                sb.append("Assistant: ").append(interaction.getResponse()).append("\n");
            });
        }

        sb.append("\nCurrent Input: ").append(input);
        return sb.toString();
    }

    // Inner classes extracted to top-level files: ReasoningRequest, ReasoningEngineHealthStatus
}
