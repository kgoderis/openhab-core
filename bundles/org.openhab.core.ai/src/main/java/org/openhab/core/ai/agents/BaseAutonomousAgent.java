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
import org.openhab.core.ai.action.ActionDefinition;
import org.openhab.core.ai.action.ActionMetric;
import org.openhab.core.ai.action.ActionResult;
import org.openhab.core.ai.agent.AgentContext;
import org.openhab.core.ai.agent.AgentMetrics;
import org.openhab.core.ai.agent.AgentState;
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
 * - Action execution with validation and safety checks
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
    private static final Duration DEFAULT_ACTION_TIMEOUT = Duration.ofSeconds(30);
    private static final int DEFAULT_MAX_CONCURRENT_ACTIONS = 5;
    private static final int DEFAULT_MAX_RETRY_ATTEMPTS = 3;
    private static final Duration DEFAULT_RETRY_DELAY = Duration.ofSeconds(5);

    // Agent state
    private final AtomicReference<AgentState> state = new AtomicReference<>(AgentState.INITIALIZING);
    private final AtomicReference<AgentContext> context = new AtomicReference<>(new AgentContext());
    private final Map<String, Object> persistentState = new ConcurrentHashMap<>();
    private final Map<String, ActionDefinition> registeredActions = new ConcurrentHashMap<>();

    // Performance monitoring
    private final AtomicLong totalActionsExecuted = new AtomicLong(0);
    private final AtomicLong totalActionsSucceeded = new AtomicLong(0);
    private final AtomicLong totalActionsFailed = new AtomicLong(0);
    protected final AtomicLong totalProcessingTime = new AtomicLong(0);
    private final List<ActionMetric> metrics = new ArrayList<>();

    // Threading
    private final ExecutorService actionExecutor = Executors.newFixedThreadPool(DEFAULT_MAX_CONCURRENT_ACTIONS);
    private volatile boolean isRunning = false;

    // Dependencies
    @Reference
    private @Nullable AutonomousReasoningInputManager inputManager;

    @Reference
    private @Nullable EventProcessingAnalytics analytics;

    // Configuration
    private Duration actionTimeout = DEFAULT_ACTION_TIMEOUT;
    private int maxConcurrentActions = DEFAULT_MAX_CONCURRENT_ACTIONS;
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
            logger.info("Initializing autonomous agent: {}", getAgentId());

            // Set initial state
            state.set(AgentState.INITIALIZING);

            // Initialize agent-specific components
            onInitialize();

            // Register default actions
            registerDefaultActions();

            // Load persistent state if enabled
            if (enableContextPersistence) {
                loadPersistentState();
            }

            // Set state to ready
            state.set(AgentState.READY);

            logger.info("Autonomous agent initialized successfully: {}", getAgentId());

        } catch (Exception e) {
            logger.error("Failed to initialize autonomous agent: {}", getAgentId(), e);
            state.set(AgentState.ERROR);
            throw new AgentInitializationException("Failed to initialize agent: " + getAgentId(), e);
        }
    }

    /**
     * Start the agent
     */
    public void startAgent() {
        if (isRunning) {
            logger.warn("Agent {} is already running", getAgentId());
            return;
        }

        try {
            logger.info("Starting autonomous agent: {}", getAgentId());

            isRunning = true;
            state.set(AgentState.RUNNING);

            // Start agent-specific processing
            onStart();

            // Start background processing
            startBackgroundProcessing();

            logger.info("Autonomous agent started successfully: {}", getAgentId());

        } catch (Exception e) {
            logger.error("Failed to start autonomous agent: {}", getAgentId(), e);
            state.set(AgentState.ERROR);
            isRunning = false;
            throw new AgentStartupException("Failed to start agent: " + getAgentId(), e);
        }
    }

    /**
     * Stop the agent
     */
    public void stopAgent() {
        if (!isRunning) {
            return;
        }

        try {
            logger.info("Stopping autonomous agent: {}", getAgentId());

            isRunning = false;
            state.set(AgentState.STOPPING);

            // Stop agent-specific processing
            onStop();

            // Shutdown executor
            actionExecutor.shutdown();

            // Save persistent state if enabled
            if (enableContextPersistence) {
                savePersistentState();
            }

            state.set(AgentState.STOPPED);

            logger.info("Autonomous agent stopped successfully: {}", getAgentId());

        } catch (Exception e) {
            logger.error("Error stopping autonomous agent: {}", getAgentId(), e);
            state.set(AgentState.ERROR);
        }
    }

    /**
     * Shutdown the agent
     */
    protected void shutdownAgent() {
        stopAgent();
        onShutdown();
    }

    /**
     * Execute an action
     */
    public CompletableFuture<ActionResult> executeAction(String actionName, Map<String, Object> parameters) {
        return CompletableFuture.supplyAsync(() -> {
            Instant startTime = Instant.now();
            String actionId = generateActionId();

            try {
                // Validate agent state
                if (!isRunning || state.get() != AgentState.RUNNING) {
                    return ActionResult.error("Agent is not running", null, 0);
                }

                // Validate action
                ActionDefinition actionDef = registeredActions.get(actionName);
                if (actionDef == null) {
                    return ActionResult.error("Action not found: " + actionName, null, 0);
                }

                // Validate parameters
                if (!validateActionParameters(actionDef, parameters)) {
                    return ActionResult.error("Invalid parameters for action: " + actionName, null, 0);
                }

                // Perform safety checks
                if (enableSafetyChecks && !performSafetyChecks(actionName, parameters)) {
                    return ActionResult.error("Safety check failed for action: " + actionName, null, 0);
                }

                // Execute action with retry logic
                ActionResult result = executeActionWithRetry(actionName, parameters, actionId);

                // Record metrics
                recordActionMetrics(actionName, result, Duration.between(startTime, Instant.now()));

                return result;

            } catch (Exception e) {
                logger.error("Error executing action {}: {}", actionName, e.getMessage(), e);
                totalActionsFailed.incrementAndGet();
                return ActionResult.error("Error executing action: " + e.getMessage(), null, 0);
            } finally {
                totalProcessingTime.addAndGet(Duration.between(startTime, Instant.now()).toMillis());
            }
        }, actionExecutor);
    }

    /**
     * Execute action with retry logic
     */
    private ActionResult executeActionWithRetry(String actionName, Map<String, Object> parameters, String actionId) {
        Exception lastException = null;

        for (int attempt = 1; attempt <= maxRetryAttempts; attempt++) {
            try {
                // Execute the action
                ActionResult result = onExecuteAction(actionName, parameters, actionId);

                if (result.isSuccess()) {
                    totalActionsSucceeded.incrementAndGet();
                    return result;
                } else {
                    logger.warn("Action {} failed (attempt {}/{}): {}", actionName, attempt, maxRetryAttempts,
                            result.getMessage());
                }

            } catch (Exception e) {
                lastException = e;
                logger.warn("Action {} threw exception (attempt {}/{}): {}", actionName, attempt, maxRetryAttempts,
                        e.getMessage());
            }

            // Wait before retry (except on last attempt)
            if (attempt < maxRetryAttempts) {
                try {
                    Thread.sleep(retryDelay.toMillis());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        totalActionsFailed.incrementAndGet();
        String errorMessage = lastException != null ? lastException.getMessage()
                : "Action failed after " + maxRetryAttempts + " attempts";
        return ActionResult.error(errorMessage, null, 0);
    }

    /**
     * Register an action
     */
    protected void registerAction(String name, String description, List<String> requiredParameters,
            List<String> optionalParameters) {
        ActionDefinition actionDef = new ActionDefinition(name, description, requiredParameters, optionalParameters);
        registeredActions.put(name, actionDef);
        logger.debug("Registered action {} for agent {}", name, getAgentId());
    }

    /**
     * Update agent context
     */
    protected void updateContext(String key, Object value) {
        AgentContext currentContext = context.get();
        AgentContext newContext = new AgentContext(currentContext);
        newContext.put(key, value);
        context.set(newContext);
    }

    /**
     * Get agent context
     */
    protected AgentContext getContext() {
        return context.get();
    }

    /**
     * Get persistent state
     */
    protected Map<String, Object> getPersistentState() {
        return new HashMap<>(persistentState);
    }

    /**
     * Set persistent state
     */
    protected void setPersistentState(String key, Object value) {
        persistentState.put(key, value);
    }

    /**
     * Get agent state
     */
    public AgentState getState() {
        return state.get();
    }

    /**
     * Get agent metrics
     */
    public AgentMetrics getMetrics() {
        return new AgentMetrics(getAgentId(), state.get(), totalActionsExecuted.get(), totalActionsSucceeded.get(),
                totalActionsFailed.get(), totalProcessingTime.get(), new ArrayList<>(metrics), Instant.now());
    }

    /**
     * Get registered actions
     */
    public List<ActionDefinition> getRegisteredActions() {
        return new ArrayList<>(registeredActions.values());
    }

    /**
     * Validate action parameters
     */
    private boolean validateActionParameters(ActionDefinition actionDef, Map<String, Object> parameters) {
        // Check required parameters
        for (String requiredParam : actionDef.getRequiredParameters()) {
            if (!parameters.containsKey(requiredParam)) {
                logger.warn("Missing required parameter {} for action {}", requiredParam, actionDef.getName());
                return false;
            }
        }

        // Check parameter types (basic validation)
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            if (entry.getValue() == null) {
                logger.warn("Parameter {} cannot be null for action {}", entry.getKey(), actionDef.getName());
                return false;
            }
        }

        return true;
    }

    /**
     * Perform safety checks
     */
    private boolean performSafetyChecks(String actionName, Map<String, Object> parameters) {
        try {
            return onSafetyCheck(actionName, parameters);
        } catch (Exception e) {
            logger.error("Error during safety check for action {}: {}", actionName, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Record action metrics
     */
    private void recordActionMetrics(String actionName, ActionResult result, Duration duration) {
        totalActionsExecuted.incrementAndGet();

        ActionMetric metric = new ActionMetric(actionName, result.isSuccess(), duration, result.getMessage(),
                Instant.now());

        metrics.add(metric);

        // Keep only recent metrics
        if (metrics.size() > 1000) {
            metrics.remove(0);
        }

        // Record analytics if available
        if (analytics != null) {
            analytics.recordPerformanceMetric(getAgentId(), actionName, duration, result.isSuccess());
            if (!result.isSuccess()) {
                analytics.recordError(getAgentId(), actionName, result.getMessage(), null);
            }
        }
    }

    /**
     * Start background processing
     */
    private void startBackgroundProcessing() {
        actionExecutor.submit(this::backgroundProcessingLoop);
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
            logger.debug("Loaded persistent state for agent {}", getAgentId());
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
            logger.debug("Saved persistent state for agent {}", getAgentId());
        } catch (Exception e) {
            logger.error("Error saving persistent state for agent {}: {}", getAgentId(), e.getMessage(), e);
        }
    }

    /**
     * Register default actions
     */
    private void registerDefaultActions() {
        registerAction("getStatus", "Get agent status", new ArrayList<>(), new ArrayList<>());
        registerAction("getMetrics", "Get agent metrics", new ArrayList<>(), new ArrayList<>());
        registerAction("updateConfig", "Update agent configuration", List.of("config"), new ArrayList<>());
    }

    /**
     * Generate unique action ID
     */
    protected String generateActionId() {
        return getAgentId() + "_action_" + System.currentTimeMillis() + "_" + Thread.currentThread().getId();
    }

    // Configuration methods
    public void setActionTimeout(Duration actionTimeout) {
        this.actionTimeout = actionTimeout;
    }

    public void setMaxConcurrentActions(int maxConcurrentActions) {
        this.maxConcurrentActions = maxConcurrentActions;
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

    // Abstract methods to be implemented by subclasses
    protected abstract String getAgentId();

    protected abstract void onInitialize() throws Exception;

    protected abstract void onStart() throws Exception;

    protected abstract void onStop() throws Exception;

    protected abstract void onShutdown();

    protected abstract ActionResult onExecuteAction(String actionName, Map<String, Object> parameters, String actionId)
            throws Exception;

    protected abstract boolean onSafetyCheck(String actionName, Map<String, Object> parameters) throws Exception;

    protected abstract void onBackgroundProcessing() throws Exception;

    protected abstract void onLoadPersistentState(Map<String, Object> state) throws Exception;

    protected abstract void onSavePersistentState(Map<String, Object> state) throws Exception;

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
