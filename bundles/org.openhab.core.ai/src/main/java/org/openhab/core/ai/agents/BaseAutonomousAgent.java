package org.openhab.core.ai.agents;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.agent.core.AgentMetrics;
import org.openhab.core.ai.agent.core.AgentState;
import org.openhab.core.ai.agent.execution.api.AgentSkillManager;
import org.openhab.core.ai.agent.execution.api.AgentSkillResult;
import org.openhab.core.ai.common.context.AgentContext;
import org.openhab.core.ai.common.monitoring.api.MetricKey;
import org.openhab.core.ai.common.monitoring.api.MetricKeys;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
import org.openhab.core.ai.events.EventProcessingAnalytics;
import org.openhab.core.ai.reasoning.input.AutonomousReasoningInputManager;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base Autonomous Agent Framework
 * 
 * <p>
 * This component provides the foundational framework for all autonomous agents:
 * - Agent lifecycle management (initialization, activation, deactivation, cleanup)
 * - Context management and state persistence
 * - Skill execution with validation and safety checks
 * - Comprehensive error handling and recovery mechanisms
 * - Detailed logging and monitoring capabilities
 * - Performance metrics and health monitoring
 * - Configuration management and dynamic updates
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 */
@Component(service = BaseAutonomousAgent.class)
@NonNullByDefault
public abstract class BaseAutonomousAgent {

    private static final Logger logger = LoggerFactory.getLogger(BaseAutonomousAgent.class);

    // Configuration
    private static final Duration DEFAULT_SKILL_TIMEOUT = Duration.ofSeconds(30);
    private static final int DEFAULT_MAX_CONCURRENT_SKILLS = 5;
    private static final int DEFAULT_MAX_RETRY_ATTEMPTS = 3;
    private static final Duration DEFAULT_RETRY_DELAY = Duration.ofSeconds(5);

    // Agent state
    private final AtomicReference<AgentState> state = new AtomicReference<>(AgentState.INITIALIZING);
    private final AtomicReference<AgentContext> context = new AtomicReference<>(new AgentContext());
    private final Map<String, Object> persistentState = new ConcurrentHashMap<>();

    // Performance monitoring - now handled by MetricsService
    protected final AtomicLong totalProcessingTime = new AtomicLong(0);

    // Threading
    private final ExecutorService skillExecutor = Executors.newFixedThreadPool(DEFAULT_MAX_CONCURRENT_SKILLS);
    private volatile boolean isRunning = false;

    // Dependencies
    @Reference
    private @Nullable AutonomousReasoningInputManager inputManager;

    @Reference
    private @Nullable EventProcessingAnalytics analytics;

    @Reference
    private @Nullable AgentSkillManager skillManager;

    @Reference
    private @Nullable MetricsService metricsService;

    // Configuration
    private Duration skillTimeout = DEFAULT_SKILL_TIMEOUT;
    private int maxConcurrentSkills = DEFAULT_MAX_CONCURRENT_SKILLS;
    private int maxRetryAttempts = DEFAULT_MAX_RETRY_ATTEMPTS;
    private Duration retryDelay = DEFAULT_RETRY_DELAY;
    private boolean enableSafetyChecks = true;
    private boolean enablePerformanceMonitoring = true;
    private boolean enableContextPersistence = true;

    @Activate
    public void activate() {
        logger.debug("Base Autonomous Agent activated: {}", getAgentId());
        initializeAgent();
    }

    @Deactivate
    public void deactivate() {
        logger.debug("Base Autonomous Agent deactivated: {}", getAgentId());
        shutdownAgent();
    }

    /**
     * Initialize the agent
     */
    protected void initializeAgent() {
        try {
            logger.debug("Initializing agent: {}", getAgentId());

            // Set initial state
            state.set(AgentState.INITIALIZING);

            // Load persistent state if enabled
            if (enableContextPersistence) {
                loadPersistentState();
            }

            // Call abstract initialization
            onInitialize();

            // Set state to ready
            state.set(AgentState.READY);

            logger.info("Agent initialized successfully: {}", getAgentId());

        } catch (Exception e) {
            state.set(AgentState.ERROR);
            logger.error("Failed to initialize agent: {}", getAgentId(), e);
            throw new AgentInitializationException("Failed to initialize agent: " + getAgentId(), e);
        }
    }

    /**
     * Start the agent
     */
    public void startAgent() {
        try {
            logger.debug("Starting agent: {}", getAgentId());

            if (state.get() != AgentState.READY) {
                throw new AgentStartupException("Agent is not ready to start: " + getAgentId(),
                        new IllegalStateException("Agent is not ready to start"));
            }

            // Set state to starting
            state.set(AgentState.STARTING);

            // Call abstract start method
            onStart();

            // Start background processing
            isRunning = true;
            startBackgroundProcessing();

            // Set state to running
            state.set(AgentState.RUNNING);

            logger.info("Agent started successfully: {}", getAgentId());

        } catch (Exception e) {
            state.set(AgentState.ERROR);
            logger.error("Failed to start agent: {}", getAgentId(), e);
            throw new AgentStartupException("Failed to start agent: " + getAgentId(), e);
        }
    }

    /**
     * Stop the agent
     */
    public void stopAgent() {
        try {
            logger.debug("Stopping agent: {}", getAgentId());

            if (state.get() != AgentState.RUNNING) {
                logger.warn("Agent is not running: {}", getAgentId());
                return;
            }

            // Set state to stopping
            state.set(AgentState.STOPPING);

            // Stop background processing
            isRunning = false;

            // Call abstract stop method
            onStop();

            // Set state to stopped
            state.set(AgentState.STOPPED);

            logger.info("Agent stopped successfully: {}", getAgentId());

        } catch (Exception e) {
            state.set(AgentState.ERROR);
            logger.error("Failed to stop agent: {}", getAgentId(), e);
            throw new AgentStartupException("Failed to stop agent: " + getAgentId(), e);
        }
    }

    /**
     * Shutdown the agent
     */
    protected void shutdownAgent() {
        try {
            logger.debug("Shutting down agent: {}", getAgentId());

            // Stop if running
            if (state.get() == AgentState.RUNNING) {
                stopAgent();
            }

            // Call abstract shutdown method
            onShutdown();

            // Save persistent state if enabled
            if (enableContextPersistence) {
                savePersistentState();
            }

            // Shutdown executor
            skillExecutor.shutdown();

            logger.info("Agent shutdown completed: {}", getAgentId());

        } catch (Exception e) {
            logger.error("Error during agent shutdown: {}", getAgentId(), e);
        }
    }

    /**
     * Execute a skill with the given parameters
     * 
     * @param skillName the skill name to execute
     * @param parameters the parameters for the skill execution
     * @return CompletableFuture with the skill execution result
     */
    public CompletableFuture<AgentSkillResult> executeSkill(String skillName, Map<String, Object> parameters) {
        long startTime = System.currentTimeMillis();
        String skillId = generateSkillId();

        logger.debug("Executing skill: {} with parameters: {}", skillName, parameters);

        return CompletableFuture.supplyAsync(() -> {
            try {
                // Perform safety checks if enabled
                if (enableSafetyChecks && !performSkillSafetyChecks(skillName, parameters)) {
                    return createSkillErrorResult("Safety check failed for skill: " + skillName, skillId,
                            System.currentTimeMillis() - startTime);
                }

                // Execute the skill via skill manager
                if (skillManager != null) {
                    AgentSkillResult result = skillManager.executeSkill(skillName, parameters);
                    Duration duration = Duration.ofMillis(System.currentTimeMillis() - startTime);

                    // Record metrics
                    recordSkillMetrics(skillName, result, duration);

                    return result;
                } else {
                    return createSkillErrorResult("Skill manager not available", skillId,
                            System.currentTimeMillis() - startTime);
                }

            } catch (Exception e) {
                logger.error("Error executing skill: {}", skillName, e);
                return createSkillErrorResult("Error executing skill: " + e.getMessage(), skillId,
                        System.currentTimeMillis() - startTime);
            }
        }, skillExecutor);
    }

    /**
     * Execute multiple composed skills
     * 
     * @param skillRequests list of skill execution requests
     * @return CompletableFuture with the composed skill execution result
     */
    public CompletableFuture<AgentSkillResult> executeComposedSkills(List<SkillExecutionRequest> skillRequests) {
        long startTime = System.currentTimeMillis();
        String compositionId = generateCompositionId();

        logger.debug("Executing composed skills: {} skills", skillRequests.size());

        List<CompletableFuture<AgentSkillResult>> futures = new ArrayList<>();

        for (SkillExecutionRequest request : skillRequests) {
            CompletableFuture<AgentSkillResult> future = executeSkill(request.getSkillName(), request.getParameters());
            futures.add(future);
        }

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenApply(v -> {
            List<AgentSkillResult> results = new ArrayList<>();
            for (CompletableFuture<AgentSkillResult> future : futures) {
                try {
                    results.add(future.get());
                } catch (Exception e) {
                    logger.error("Error getting skill result", e);
                    results.add(createSkillErrorResult("Error getting skill result: " + e.getMessage(), compositionId,
                            System.currentTimeMillis() - startTime));
                }
            }

            long totalExecutionTime = System.currentTimeMillis() - startTime;
            return createComposedSkillResult(results, compositionId, totalExecutionTime);
        });
    }

    /**
     * Decide which skill to execute based on context
     * 
     * @param context the execution context
     * @param availableSkills list of available skills
     * @return the selected skill name, or null if no skills available
     */
    protected @Nullable String decideSkillToExecute(Map<String, Object> context, List<String> availableSkills) {
        // Implement intelligent skill selection based on context
        if (availableSkills.isEmpty()) {
            return null;
        }

        // If only one skill available, return it
        if (availableSkills.size() == 1) {
            return availableSkills.get(0);
        }

        // Score each skill based on context relevance
        Map<String, Double> skillScores = new HashMap<>();
        for (String skill : availableSkills) {
            double score = calculateSkillRelevanceScore(skill, context);
            skillScores.put(skill, score);
        }

        // Return the skill with the highest score
        return skillScores.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey)
                .orElse(availableSkills.get(0));
    }

    /**
     * Calculate relevance score for a skill based on context
     * 
     * @param skillName the skill name
     * @param context the execution context
     * @return the relevance score (higher is better)
     */
    private double calculateSkillRelevanceScore(String skillName, Map<String, Object> context) {
        double score = 0.0;

        // Check if skill name matches context keywords
        String contextStr = context.toString().toLowerCase();
        String skillLower = skillName.toLowerCase();

        // Score based on keyword matching
        if (contextStr.contains(skillLower)) {
            score += 10.0;
        }

        // Score based on skill type matching context type
        String contextType = (String) context.get("type");
        if (contextType != null && skillName.toLowerCase().contains(contextType.toLowerCase())) {
            score += 5.0;
        }

        // Score based on priority
        Integer priority = (Integer) context.get("priority");
        if (priority != null && priority > 0) {
            score += priority * 0.1;
        }

        // Score based on recent usage (prefer less recently used skills)
        Long lastUsed = (Long) context.get("lastUsed_" + skillName);
        if (lastUsed != null) {
            long timeSinceLastUse = System.currentTimeMillis() - lastUsed;
            score += Math.min(timeSinceLastUse / 60000.0, 2.0); // Max 2 points for time-based scoring
        }

        // Score based on success rate (if available)
        Double successRate = (Double) context.get("successRate_" + skillName);
        if (successRate != null) {
            score += successRate * 3.0; // Up to 3 points for success rate
        }

        return score;
    }

    /**
     * Convert a skill to a task request
     * 
     * @param skillName the skill name
     * @param parameters the skill parameters
     * @return the task request
     */
    protected SkillExecutionRequest convertSkillToTask(String skillName, Map<String, Object> parameters) {
        return new SkillExecutionRequest(skillName, parameters);
    }

    /**
     * Enhance context for skill execution
     * 
     * @param skillContext the skill context to enhance
     */
    protected void enhanceContextForSkills(Map<String, Object> skillContext) {
        // Add agent-specific context
        skillContext.put("agentId", getAgentId());
        skillContext.put("agentState", getState().name());
        skillContext.put("timestamp", Instant.now().toEpochMilli());

        // Add persistent state
        skillContext.putAll(persistentState);

        // Add current context
        AgentContext currentContext = getContext();
        skillContext.put("currentContext", currentContext);
    }

    /**
     * Get agent capabilities
     * 
     * @return list of agent capabilities
     */
    protected List<String> getAgentCapabilities() {
        // Implement dynamic capability discovery
        List<String> capabilities = new ArrayList<>();

        // Get capabilities from skill manager if available
        if (skillManager != null) {
            try {
                String agentId = getAgentId();
                if (agentId != null) {
                    List<String> availableSkills = skillManager.getAgentSkills(agentId);
                    capabilities.addAll(availableSkills);
                }
            } catch (Exception e) {
                logger.warn("Failed to get capabilities from skill manager: {}", e.getMessage());
            }
        }

        // Add agent-specific capabilities based on agent type
        String agentId = getAgentId();
        if (agentId != null) {
            capabilities.addAll(getAgentTypeCapabilities(agentId));
        }

        // Add capabilities based on current state
        AgentState currentState = getState();
        capabilities.addAll(getStateBasedCapabilities(currentState));

        // Add capabilities based on context
        AgentContext currentContext = getContext();
        capabilities.addAll(getContextBasedCapabilities(currentContext));

        // Remove duplicates and return
        return capabilities.stream().distinct().collect(Collectors.toList());
    }

    /**
     * Get capabilities based on agent type
     * 
     * @param agentId the agent ID
     * @return list of capabilities for the agent type
     */
    private List<String> getAgentTypeCapabilities(String agentId) {
        List<String> capabilities = new ArrayList<>();

        String agentType = agentId.toLowerCase();

        if (agentType.contains("energy")) {
            capabilities.addAll(List.of("energy_monitoring", "power_optimization", "battery_management"));
        } else if (agentType.contains("security")) {
            capabilities.addAll(List.of("security_monitoring", "access_control", "alarm_management"));
        } else if (agentType.contains("comfort")) {
            capabilities.addAll(List.of("climate_control", "lighting_control", "entertainment_control"));
        } else if (agentType.contains("automation")) {
            capabilities.addAll(List.of("scheduling", "rule_execution", "workflow_management"));
        } else if (agentType.contains("health")) {
            capabilities.addAll(List.of("health_monitoring", "medication_reminder", "wellness_tracking"));
        } else {
            // General capabilities for unknown agent types
            capabilities.addAll(List.of("basic_monitoring", "data_collection", "status_reporting"));
        }

        return capabilities;
    }

    /**
     * Get capabilities based on current state
     * 
     * @param state the current agent state
     * @return list of state-based capabilities
     */
    private List<String> getStateBasedCapabilities(AgentState state) {
        List<String> capabilities = new ArrayList<>();

        switch (state) {
            case RUNNING:
                capabilities.addAll(List.of("active_monitoring", "real_time_processing", "event_handling"));
                break;
            case READY:
                capabilities.addAll(List.of("passive_monitoring", "data_collection"));
                break;
            case ERROR:
                capabilities.addAll(List.of("error_recovery", "diagnostic_analysis"));
                break;
            case INITIALIZING:
            case STOPPING:
                capabilities.addAll(List.of("status_reporting", "configuration_management"));
                break;
            default:
                break;
        }

        return capabilities;
    }

    /**
     * Get capabilities based on current context
     * 
     * @param context the current agent context
     * @return list of context-based capabilities
     */
    private List<String> getContextBasedCapabilities(AgentContext context) {
        List<String> capabilities = new ArrayList<>();

        // Add capabilities based on available resources
        if (context.hasValue("network")) {
            capabilities.add("network_communication");
        }
        if (context.hasValue("database")) {
            capabilities.add("data_persistence");
        }
        if (context.hasValue("sensors")) {
            capabilities.add("sensor_data_processing");
        }
        if (context.hasValue("actuators")) {
            capabilities.add("actuator_control");
        }

        return capabilities;
    }

    /**
     * Perform safety checks for skill execution
     * 
     * @param skillName the skill name
     * @param parameters the skill parameters
     * @return true if safety checks pass
     */
    private boolean performSkillSafetyChecks(String skillName, Map<String, Object> parameters) {
        try {
            return onSkillSafetyCheck(skillName, parameters);
        } catch (Exception e) {
            logger.error("Error during safety check for skill {}: {}", skillName, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Record skill execution metrics
     * 
     * @param skillName the skill name
     * @param result the skill result
     * @param duration the execution duration
     */
    private void recordSkillMetrics(String skillName, AgentSkillResult result, Duration duration) {
        // Record metrics using MetricsService with builder pattern
        MetricsService metrics = metricsService;
        if (metrics != null) {
            try {
                metrics.recordOperation("agent", "skill-execution").withSuccess(result.isSuccess())
                        .withDuration(duration.toNanos()).withData("agentId", getAgentId())
                        .withData("skillName", skillName).withData("executionTimeMs", duration.toMillis())
                        .withData("errorMessage", result.getErrorMessage() != null ? result.getErrorMessage() : "null")
                        .record();
            } catch (Exception e) {
                logger.warn("Failed to record skill execution metrics for agent {} skill {}: {}", getAgentId(),
                        skillName, e.getMessage());
                // Graceful degradation: continue with skill execution even if metrics recording fails
            }
        }

        totalProcessingTime.addAndGet(duration.toMillis());

        // Record analytics if available
        if (analytics != null) {
            try {
                analytics.recordPerformanceMetric(getAgentId(), skillName, duration, result.isSuccess());
                if (!result.isSuccess()) {
                    analytics.recordError(getAgentId(), skillName, result.getErrorMessage(),
                            new RuntimeException(result.getErrorMessage()));
                }
            } catch (Exception e) {
                logger.warn("Failed to record analytics for agent {} skill {}: {}", getAgentId(), skillName,
                        e.getMessage());
                // Graceful degradation: continue with skill execution even if analytics recording fails
            }
        }
    }

    /**
     * Generate a unique skill ID
     * 
     * @return the skill ID
     */
    private String generateSkillId() {
        return getAgentId() + "_skill_" + System.currentTimeMillis() + "_" + Thread.currentThread().getId();
    }

    /**
     * Generate a unique composition ID
     * 
     * @return the composition ID
     */
    private String generateCompositionId() {
        return getAgentId() + "_composition_" + System.currentTimeMillis() + "_" + Thread.currentThread().getId();
    }

    /**
     * Create a skill error result
     * 
     * @param message the error message
     * @param skillId the skill ID
     * @param executionTime the execution time in milliseconds
     * @return the skill error result
     */
    private AgentSkillResult createSkillErrorResult(String message, String skillId, long executionTime) {
        return AgentSkillResult.failure(message, executionTime);
    }

    /**
     * Create a composed skill result
     * 
     * @param results the individual skill results
     * @param compositionId the composition ID
     * @param totalExecutionTime the total execution time in milliseconds
     * @return the composed skill result
     */
    private AgentSkillResult createComposedSkillResult(List<AgentSkillResult> results, String compositionId,
            long totalExecutionTime) {
        // Aggregate results
        Map<String, Object> aggregatedData = new HashMap<>();
        boolean allSuccessful = true;
        StringBuilder errorMessages = new StringBuilder();

        for (int i = 0; i < results.size(); i++) {
            AgentSkillResult result = results.get(i);
            Object data = result.getData();
            aggregatedData.put("skill_" + i, data != null ? data : "null");

            if (!result.isSuccess()) {
                allSuccessful = false;
                errorMessages.append("Skill ").append(i).append(": ").append(result.getErrorMessage()).append("; ");
            }
        }

        if (allSuccessful) {
            return AgentSkillResult.success(aggregatedData, totalExecutionTime);
        } else {
            return AgentSkillResult.failure(errorMessages.toString(), "COMPOSITION_ERROR", totalExecutionTime);
        }
    }

    /**
     * Update the agent context
     * 
     * @param key the context key
     * @param value the context value
     */
    protected void updateContext(String key, Object value) {
        AgentContext currentContext = context.get();
        if (currentContext != null) {
            context.set((AgentContext) currentContext.setValue(key, value));
        }
    }

    /**
     * Get the current agent context
     * 
     * @return the agent context
     */
    protected AgentContext getContext() {
        return context.get();
    }

    /**
     * Get the persistent state
     * 
     * @return the persistent state map
     */
    protected Map<String, Object> getPersistentState() {
        return new HashMap<>(persistentState);
    }

    /**
     * Set a persistent state value
     * 
     * @param key the state key
     * @param value the state value
     */
    protected void setPersistentState(String key, Object value) {
        persistentState.put(key, value);
    }

    /**
     * Get the current agent state
     * 
     * @return the agent state
     */
    public AgentState getState() {
        return state.get();
    }

    /**
     * Get agent metrics
     * 
     * @return the agent metrics
     */
    public AgentMetrics getMetrics() {
        // Get metrics from MetricsService
        MetricsService metrics = metricsService;
        long totalSkillsExecuted = 0;
        long totalSkillsSucceeded = 0;
        long totalSkillsFailed = 0;

        if (metrics != null) {
            try {
                MetricKey agentKey = MetricKeys.custom("agent", Map.of(), Set.of("counts", "latency"));
                var snapshot = metrics.getSnapshot(agentKey,
                        org.openhab.core.ai.common.monitoring.service.snapshot.GenericMetricsSnapshot.class);
                if (snapshot != null) {
                    totalSkillsExecuted = snapshot.getLong("total");
                    totalSkillsSucceeded = snapshot.getLong("total") - snapshot.getLong("failure");
                    totalSkillsFailed = snapshot.getLong("failure");
                }
            } catch (Exception e) {
                logger.warn("Failed to retrieve agent metrics for agent {}: {}", getAgentId(), e.getMessage());
                // Graceful degradation: return default values if metrics retrieval fails
                totalSkillsExecuted = 0;
                totalSkillsSucceeded = 0;
                totalSkillsFailed = 0;
            }
        } else {
            logger.warn("MetricsService not available, returning empty metrics for agent {}", getAgentId());
        }

        return new AgentMetrics(getAgentId(), state.get(), totalSkillsExecuted, totalSkillsSucceeded, totalSkillsFailed,
                totalProcessingTime.get(), new ArrayList<>(), Instant.now());
    }

    /**
     * Start background processing
     */
    private void startBackgroundProcessing() {
        skillExecutor.submit(this::backgroundProcessingLoop);
    }

    /**
     * Background processing loop
     */
    private void backgroundProcessingLoop() {
        while (isRunning && !Thread.currentThread().isInterrupted()) {
            try {
                // Perform background tasks
                onBackgroundProcessing();

                // Sleep for a short interval
                Thread.sleep(1000);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                logger.error("Error in background processing for agent {}: {}", getAgentId(), e.getMessage(), e);
            }
        }
    }

    /**
     * Load persistent state
     */
    private void loadPersistentState() {
        try {
            onLoadPersistentState(persistentState);
        } catch (Exception e) {
            logger.error("Error loading persistent state for agent {}: {}", getAgentId(), e.getMessage(), e);
        }
    }

    /**
     * Save persistent state
     */
    private void savePersistentState() {
        try {
            onSavePersistentState(persistentState);
        } catch (Exception e) {
            logger.error("Error saving persistent state for agent {}: {}", getAgentId(), e.getMessage(), e);
        }
    }

    // Configuration setters
    public void setSkillTimeout(Duration skillTimeout) {
        this.skillTimeout = skillTimeout;
    }

    public void setMaxConcurrentSkills(int maxConcurrentSkills) {
        this.maxConcurrentSkills = maxConcurrentSkills;
    }

    public void setMaxRetryAttempts(int maxRetryAttempts) {
        this.maxRetryAttempts = maxRetryAttempts;
    }

    public void setRetryDelay(Duration retryDelay) {
        this.retryDelay = retryDelay;
    }

    public void setEnableSafetyChecks(boolean enableSafetyChecks) {
        this.enableSafetyChecks = enableSafetyChecks;
    }

    public void setEnablePerformanceMonitoring(boolean enablePerformanceMonitoring) {
        this.enablePerformanceMonitoring = enablePerformanceMonitoring;
    }

    public void setEnableContextPersistence(boolean enableContextPersistence) {
        this.enableContextPersistence = enableContextPersistence;
    }

    // Abstract methods
    protected abstract String getAgentId();

    protected abstract void onInitialize() throws Exception;

    protected abstract void onStart() throws Exception;

    protected abstract void onStop() throws Exception;

    protected abstract void onShutdown();

    protected abstract void onBackgroundProcessing() throws Exception;

    protected abstract void onLoadPersistentState(Map<String, Object> state) throws Exception;

    protected abstract void onSavePersistentState(Map<String, Object> state) throws Exception;

    protected abstract boolean onSkillSafetyCheck(String skillName, Map<String, Object> parameters) throws Exception;

    // Exception classes moved to top-level
}
