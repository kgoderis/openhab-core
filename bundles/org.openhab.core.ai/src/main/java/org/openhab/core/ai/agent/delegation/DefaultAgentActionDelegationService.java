package org.openhab.core.ai.agent.delegation;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.action.ActionRegistry;
import org.openhab.core.ai.action.api.ActionError;
import org.openhab.core.ai.action.api.ActionKeys;
import org.openhab.core.ai.action.api.ActionResult;
import org.openhab.core.ai.agent.delegation.api.AgentActionDelegationService;
import org.openhab.core.ai.common.configuration.ActionExecutionConfiguration;
import org.openhab.core.ai.common.context.ExecutionContext;
import org.openhab.core.ai.common.metrics.api.Counts;
import org.openhab.core.ai.common.metrics.api.MetricKeys;
import org.openhab.core.ai.common.metrics.api.Timing;
import org.openhab.core.ai.common.metrics.registry.MetricsRegistry;
import org.openhab.core.ai.common.metrics.snapshot.DelegationMetricsSnapshot;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Agent Action Delegation Service Implementation
 * 
 * <p>
 * This implementation provides:
 * - Agent-based action execution for local LLMs
 * - Action routing to appropriate agent instances
 * - Action execution context management
 * - Agent capability discovery and validation
 * - Agent load balancing and failover
 * - Agent action execution monitoring
 * - Agent action result aggregation
 * - Agent action execution security controls
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@Component(service = AgentActionDelegationService.class)
@NonNullByDefault
public class DefaultAgentActionDelegationService implements AgentActionDelegationService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultAgentActionDelegationService.class);

    // Performance monitoring
    @Reference
    private @Nullable MetricsRegistry metricsRegistry;

    // Agent registry and load balancing
    private final ConcurrentHashMap<String, AgentInfo> agentRegistry = new ConcurrentHashMap<>();
    private final AtomicReference<LoadBalancingStrategy> loadBalancingStrategy = new AtomicReference<>(
            LoadBalancingStrategy.ROUND_ROBIN);

    // Unified configuration
    private final AtomicReference<ActionExecutionConfiguration> configuration = new AtomicReference<>(
            ActionExecutionConfiguration.builder().withConfigId("default-delegation-config")
                    .withExecutionTimeout(Duration.ofSeconds(30)).withMaxRetryAttempts(2)
                    .withRetryDelay(Duration.ofSeconds(5)).withEnableAsyncExecution(true)
                    .withMaxConcurrentExecutions(10).build());

    @Reference
    private @Nullable ActionRegistry actionRegistry;

    @Override
    public CompletableFuture<ActionResult> delegateAction(ExecutionContext actionContext) {
        Instant startTime = Instant.now();
        long startNanos = System.nanoTime();

        try {
            // Find appropriate agent for the action
            @Nullable
            String agentId = selectAgentForAction(actionContext);
            if (agentId == null) {
                updateMetrics(actionContext, false, System.nanoTime() - startNanos);
                return CompletableFuture.completedFuture(ActionResult.error("No suitable agent found for action",
                        new ActionError("NO_AGENT_AVAILABLE", "No agent available to handle this action"),
                        Duration.between(startTime, Instant.now()).toMillis()));
            }

            // Execute action on selected agent
            return executeActionOnAgent(agentId, actionContext, startTime, startNanos);

        } catch (Exception e) {
            updateMetrics(actionContext, false, System.nanoTime() - startNanos);
            logger.error("Error delegating action: {}", actionContext.getCorrelationId(), e);
            return CompletableFuture.completedFuture(
                    ActionResult.error("Action delegation failed", new ActionError("DELEGATION_ERROR", e.getMessage()),
                            Duration.between(startTime, Instant.now()).toMillis()));
        }
    }

    @Override
    public boolean isAvailable() {
        return !agentRegistry.isEmpty();
    }

    @Override
    public int getAvailableAgentCount() {
        return agentRegistry.size();
    }

    /**
     * Update the delegation configuration
     * 
     * @param newConfiguration the new configuration to apply
     */
    public void updateConfiguration(ActionExecutionConfiguration newConfiguration) {
        configuration.set(newConfiguration);
        logger.info("Delegation configuration updated: {}", newConfiguration.getId());
    }

    /**
     * Get the current delegation configuration
     * 
     * @return the current configuration
     */
    public ActionExecutionConfiguration getConfiguration() {
        return configuration.get();
    }

    /**
     * Select an appropriate agent for the action
     */
    private @Nullable String selectAgentForAction(ExecutionContext actionContext) {
        String actionName = actionContext.getValue(ActionKeys.ACTION_NAME.getKey(), String.class);

        if (actionName == null) {
            logger.warn("Action name not found in context");
            return null;
        }

        // Find agents that can handle this action
        final String finalActionName = actionName; // Make final for stream operation
        List<String> capableAgents = agentRegistry.entrySet().stream()
                .filter(entry -> entry.getValue().canHandleAction(finalActionName)).map(Map.Entry::getKey).toList();

        if (capableAgents.isEmpty()) {
            logger.warn("No agents found capable of handling action: {}", actionName);
            return null;
        }

        // Apply load balancing strategy
        return applyLoadBalancingStrategy(capableAgents, actionContext);
    }

    /**
     * Apply load balancing strategy to select agent
     */
    private @Nullable String applyLoadBalancingStrategy(List<String> capableAgents, ExecutionContext actionContext) {
        LoadBalancingStrategy strategy = loadBalancingStrategy.get();

        if (strategy == null) {
            return capableAgents.get(0); // Default to first available
        }

        switch (strategy) {
            case ROUND_ROBIN:
                return selectRoundRobin(capableAgents);
            case LEAST_LOADED:
                return selectLeastLoaded(capableAgents);
            case CAPABILITY_BASED:
                return selectCapabilityBased(capableAgents, actionContext);
            case RANDOM:
                return selectRandom(capableAgents);
            default:
                return capableAgents.get(0); // Default to first available
        }
    }

    /**
     * Round-robin selection
     */
    private @Nullable String selectRoundRobin(List<String> capableAgents) {
        // Simple round-robin implementation
        long currentTime = System.currentTimeMillis();
        int index = (int) (currentTime / 1000) % capableAgents.size();
        return capableAgents.get(index);
    }

    /**
     * Least loaded selection
     */
    private @Nullable String selectLeastLoaded(List<String> capableAgents) {
        return capableAgents.stream().min(
                (a, b) -> Integer.compare(agentRegistry.get(a).getCurrentLoad(), agentRegistry.get(b).getCurrentLoad()))
                .orElse(capableAgents.get(0));
    }

    /**
     * Capability-based selection
     */
    private @Nullable String selectCapabilityBased(List<String> capableAgents, ExecutionContext actionContext) {
        // Select agent with highest capability score for this action
        return capableAgents.stream()
                .max((a, b) -> Double.compare(agentRegistry.get(a).getCapabilityScore(actionContext),
                        agentRegistry.get(b).getCapabilityScore(actionContext)))
                .orElse(capableAgents.get(0));
    }

    /**
     * Random selection
     */
    private @Nullable String selectRandom(List<String> capableAgents) {
        int index = (int) (Math.random() * capableAgents.size());
        return capableAgents.get(index);
    }

    /**
     * Execute action on selected agent
     */
    private CompletableFuture<ActionResult> executeActionOnAgent(@Nullable String agentId,
            ExecutionContext actionContext, Instant startTime, long startNanos) {
        if (agentId == null) {
            updateMetrics(actionContext, false, System.nanoTime() - startNanos);
            return CompletableFuture.completedFuture(ActionResult.error("No agent selected",
                    new ActionError("NO_AGENT_SELECTED", "No agent was selected for execution"),
                    Duration.between(startTime, Instant.now()).toMillis()));
        }

        AgentInfo agentInfo = agentRegistry.get(agentId);
        if (agentInfo == null) {
            updateMetrics(actionContext, false, System.nanoTime() - startNanos);
            return CompletableFuture.completedFuture(ActionResult.error("Selected agent not found",
                    new ActionError("AGENT_NOT_FOUND", "Agent " + agentId + " not found in registry"),
                    Duration.between(startTime, Instant.now()).toMillis()));
        }

        // Update agent load
        agentInfo.incrementLoad();

        // Execute action with timeout and retry logic
        CompletableFuture<ActionResult> executionFuture = agentInfo.executeAction(actionContext);

        // Get timeout from configuration
        ActionExecutionConfiguration config = configuration.get();
        Duration timeout = config != null && config.getExecutionTimeout() != null ? config.getExecutionTimeout()
                : Duration.ofSeconds(30);

        return executionFuture.orTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS).handle((result, throwable) -> {
            agentInfo.decrementLoad();

            if (throwable != null) {
                logger.warn("Action execution failed on agent {}: {}", agentId, throwable.getMessage());

                // Try failover if enabled (using configuration)
                Boolean failoverEnabled = config != null && config.isEnableSecurityValidation() != null
                        ? config.isEnableSecurityValidation()
                        : true;
                if (failoverEnabled) {
                    return attemptFailover(actionContext, agentId, startTime, startNanos);
                }

                updateMetrics(actionContext, false, System.nanoTime() - startNanos);
                return ActionResult.error("Action execution failed on agent",
                        new ActionError("AGENT_EXECUTION_ERROR", throwable.getMessage()),
                        Duration.between(startTime, Instant.now()).toMillis());
            }

            boolean success = result != null && result.isSuccess();
            updateMetrics(actionContext, success, System.nanoTime() - startNanos);

            long executionTime = Duration.between(startTime, Instant.now()).toMillis();

            logger.debug("Action delegation completed in {} ms on agent {}", executionTime, agentId);
            return result;
        });
    }

    /**
     * Attempt failover to another agent
     */
    private ActionResult attemptFailover(ExecutionContext actionContext, String failedAgentId, Instant startTime,
            long startNanos) {
        logger.info("Attempting failover for action: {}", actionContext.getCorrelationId());

        // Find alternative agents
        @Nullable
        String actionName = actionContext.getValue(ActionKeys.ACTION_NAME.getKey(), String.class);

        if (actionName == null) {
            return ActionResult.error("Action name not found for failover",
                    new ActionError("FAILOVER_NO_ACTION", "Action name not found in context"),
                    Duration.between(startTime, Instant.now()).toMillis());
        }

        // At this point, actionName is guaranteed to be non-null
        final String finalActionName = actionName;
        List<String> alternativeAgents = agentRegistry.entrySet().stream().filter(
                entry -> !entry.getKey().equals(failedAgentId) && entry.getValue().canHandleAction(finalActionName))
                .map(Map.Entry::getKey).toList();

        if (alternativeAgents.isEmpty()) {
            logger.warn("No alternative agents available for failover");
            return ActionResult.error("No alternative agents available for failover",
                    new ActionError("FAILOVER_NO_AGENTS", "No alternative agents available"),
                    Duration.between(startTime, Instant.now()).toMillis());
        }

        // Get retry configuration
        ActionExecutionConfiguration config = configuration.get();
        Integer maxRetries = config != null && config.getMaxRetryAttempts() != null ? config.getMaxRetryAttempts() : 2;

        // Try alternative agents
        for (int attempt = 0; attempt < Math.min(maxRetries, alternativeAgents.size()); attempt++) {
            String alternativeAgentId = alternativeAgents.get(attempt);
            try {
                AgentInfo agentInfo = agentRegistry.get(alternativeAgentId);
                if (agentInfo != null) {
                    agentInfo.incrementLoad();
                    ActionResult result = agentInfo.executeAction(actionContext).get();
                    agentInfo.decrementLoad();

                    if (result.isSuccess()) {
                        updateMetrics(actionContext, true, System.nanoTime() - startNanos);
                        logger.info("Failover successful to agent: {}", alternativeAgentId);
                        return result;
                    }
                }
            } catch (Exception e) {
                logger.warn("Failover attempt to agent {} failed: {}", alternativeAgentId, e.getMessage());
            }
        }

        updateMetrics(actionContext, false, System.nanoTime() - startNanos);
        return ActionResult.error("All failover attempts failed",
                new ActionError("FAILOVER_FAILED", "All alternative agents failed"),
                Duration.between(startTime, Instant.now()).toMillis());
    }

    /**
     * Register an agent
     */
    public void registerAgent(String agentId, AgentInfo agentInfo) {
        agentRegistry.put(agentId, agentInfo);
        logger.info("Agent registered: {} with {} capabilities", agentId, agentInfo.getCapabilities().size());
    }

    /**
     * Unregister an agent
     */
    public void unregisterAgent(String agentId) {
        agentRegistry.remove(agentId);
        logger.info("Agent unregistered: {}", agentId);
    }

    /**
     * Update load balancing strategy
     */
    public void setLoadBalancingStrategy(LoadBalancingStrategy strategy) {
        loadBalancingStrategy.set(strategy);
        logger.info("Load balancing strategy updated to: {}", strategy);
    }

    /**
     * Update metrics for delegation operations.
     * 
     * @param actionContext execution context
     * @param ok whether the operation was successful
     * @param durationNanos duration in nanoseconds
     */
    private void updateMetrics(ExecutionContext actionContext, boolean ok, long durationNanos) {
        if (metricsRegistry != null) {
            String actionName = actionContext.getValue(ActionKeys.ACTION_NAME.getKey(), String.class);
            if (actionName != null && !actionName.isBlank()) {
                metricsRegistry.executionCollector(MetricKeys.action(actionName)).recordExecution(ok, durationNanos);
            }
            metricsRegistry.executionCollector(MetricKeys.delegation("agent")).recordExecution(ok, durationNanos);
        }
    }

    /**
     * Get performance metrics
     */
    public DelegationMetricsSnapshot getPerformanceMetrics() {
        if (metricsRegistry != null) {
            var baseSnapshot = metricsRegistry.executionCollector(MetricKeys.delegation("agent")).snapshot();
            return new DelegationMetricsSnapshot(baseSnapshot.counts(), baseSnapshot.timing(),
                    baseSnapshot.timestampMs(), agentRegistry.size());
        }
        return new DelegationMetricsSnapshot(new Counts(0, 0, 0), new Timing(0), System.currentTimeMillis(),
                agentRegistry.size());
    }

    /**
     * Load balancing strategies
     */
    // LoadBalancingStrategy extracted to top-level enum in same package

    /**
     * Agent information
     */
    // extracted to top-level class: AgentInfo

    /**
     * Performance metrics data class
     */
    // PerformanceMetrics extracted to top-level class: DelegationPerformanceMetrics
}
