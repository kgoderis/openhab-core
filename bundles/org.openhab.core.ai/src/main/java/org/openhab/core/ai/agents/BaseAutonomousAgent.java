package org.openhab.core.ai.agents;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.agent.api.AgentSkillManager;
import org.openhab.core.ai.agent.core.AgentContext;
import org.openhab.core.ai.agent.core.AgentMetrics;
import org.openhab.core.ai.agent.core.AgentState;
import org.openhab.core.ai.events.EventProcessingAnalytics;
import org.openhab.core.ai.reasoning.AutonomousReasoningInputManager;
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

    // Performance monitoring
    private final AtomicLong totalSkillsExecuted = new AtomicLong(0);
    private final AtomicLong totalSkillsSucceeded = new AtomicLong(0);
    private final AtomicLong totalSkillsFailed = new AtomicLong(0);
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
                throw new AgentStartupException("Agent is not ready to start: " + getAgentId(), null);
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
    public CompletableFuture<org.openhab.core.ai.agent.api.AgentSkillResult> executeSkill(String skillName,
            Map<String, Object> parameters) {
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
                    org.openhab.core.ai.agent.api.AgentSkillResult result = skillManager.executeSkill(skillName,
                            parameters);
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
    public CompletableFuture<org.openhab.core.ai.agent.api.AgentSkillResult> executeComposedSkills(
            List<SkillExecutionRequest> skillRequests) {
        long startTime = System.currentTimeMillis();
        String compositionId = generateCompositionId();

        logger.debug("Executing composed skills: {} skills", skillRequests.size());

        List<CompletableFuture<org.openhab.core.ai.agent.api.AgentSkillResult>> futures = new ArrayList<>();

        for (SkillExecutionRequest request : skillRequests) {
            CompletableFuture<org.openhab.core.ai.agent.api.AgentSkillResult> future = executeSkill(
                    request.getSkillName(), request.getParameters());
            futures.add(future);
        }

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenApply(v -> {
            List<org.openhab.core.ai.agent.api.AgentSkillResult> results = new ArrayList<>();
            for (CompletableFuture<org.openhab.core.ai.agent.api.AgentSkillResult> future : futures) {
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
     * @return the selected skill name
     */
    protected String decideSkillToExecute(Map<String, Object> context, List<String> availableSkills) {
        // TODO: Implement intelligent skill selection based on context
        // For now, return the first available skill
        return availableSkills.isEmpty() ? null : availableSkills.get(0);
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
        // TODO: Implement dynamic capability discovery
        return new ArrayList<>();
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
    private void recordSkillMetrics(String skillName, org.openhab.core.ai.agent.api.AgentSkillResult result,
            Duration duration) {
        totalSkillsExecuted.incrementAndGet();

        if (result.isSuccess()) {
            totalSkillsSucceeded.incrementAndGet();
        } else {
            totalSkillsFailed.incrementAndGet();
        }

        totalProcessingTime.addAndGet(duration.toMillis());

        // Record analytics if available
        if (analytics != null) {
            analytics.recordPerformanceMetric(getAgentId(), skillName, duration, result.isSuccess());
            if (!result.isSuccess()) {
                analytics.recordError(getAgentId(), skillName, result.getErrorMessage(), null);
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
    private org.openhab.core.ai.agent.api.AgentSkillResult createSkillErrorResult(String message, String skillId,
            long executionTime) {
        return org.openhab.core.ai.agent.api.AgentSkillResult.failure(message, executionTime);
    }

    /**
     * Create a composed skill result
     * 
     * @param results the individual skill results
     * @param compositionId the composition ID
     * @param totalExecutionTime the total execution time in milliseconds
     * @return the composed skill result
     */
    private org.openhab.core.ai.agent.api.AgentSkillResult createComposedSkillResult(
            List<org.openhab.core.ai.agent.api.AgentSkillResult> results, String compositionId,
            long totalExecutionTime) {
        // Aggregate results
        Map<String, Object> aggregatedData = new HashMap<>();
        boolean allSuccessful = true;
        StringBuilder errorMessages = new StringBuilder();

        for (int i = 0; i < results.size(); i++) {
            org.openhab.core.ai.agent.api.AgentSkillResult result = results.get(i);
            aggregatedData.put("skill_" + i, result.getData());

            if (!result.isSuccess()) {
                allSuccessful = false;
                errorMessages.append("Skill ").append(i).append(": ").append(result.getErrorMessage()).append("; ");
            }
        }

        if (allSuccessful) {
            return org.openhab.core.ai.agent.api.AgentSkillResult.success(aggregatedData, totalExecutionTime);
        } else {
            return org.openhab.core.ai.agent.api.AgentSkillResult.failure(errorMessages.toString(), "COMPOSITION_ERROR",
                    totalExecutionTime);
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
            currentContext.put(key, value);
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
        return new AgentMetrics(getAgentId(), state.get(), totalSkillsExecuted.get(), totalSkillsSucceeded.get(),
                totalSkillsFailed.get(), totalProcessingTime.get(), new ArrayList<>(), java.time.Instant.now());
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

    // Exception classes
    public static class AgentInitializationException extends RuntimeException {
        public AgentInitializationException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class AgentStartupException extends RuntimeException {
        public AgentStartupException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
