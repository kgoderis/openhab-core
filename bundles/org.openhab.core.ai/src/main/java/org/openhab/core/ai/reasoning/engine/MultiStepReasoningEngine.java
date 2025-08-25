package org.openhab.core.ai.reasoning.engine;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.action.ActionRegistry;
import org.openhab.core.ai.action.api.Action;
import org.openhab.core.ai.action.api.ActionError;
import org.openhab.core.ai.action.api.ActionResult;
import org.openhab.core.ai.common.context.ExecutionContext;
import org.openhab.core.ai.common.context.ReasoningContext;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
import org.openhab.core.ai.common.monitoring.service.statistics.ReasoningPerformanceStatistics;
import org.openhab.core.ai.common.response.ModelResponse;
import org.openhab.core.ai.model.ModelParameters;
import org.openhab.core.ai.model.ModelResponseActionParser;
import org.openhab.core.ai.model.api.ModelClient;
import org.openhab.core.ai.reasoning.api.MultiStepReasoningResult;
import org.openhab.core.ai.reasoning.config.MultiStepReasoningConfiguration;
import org.openhab.core.ai.reasoning.engine.analysis.ReasoningStepAnalysisService;
import org.openhab.core.ai.reasoning.engine.api.ReasoningStep;
import org.openhab.core.ai.reasoning.engine.persistence.ReasoningStepPersistenceService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Multi-Step Reasoning Engine - Orchestrates complex reasoning processes across multiple steps
 * 
 * <p>
 * This engine provides:
 * - Step-by-step reasoning orchestration with timeout handling
 * - Context accumulation across reasoning steps
 * - Guidance prompts for LLM direction
 * - Step limit configuration and enforcement
 * - Comprehensive error handling and recovery
 * - Performance monitoring and optimization
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 */
@Component(service = MultiStepReasoningEngine.class)
@NonNullByDefault
public class MultiStepReasoningEngine {

    private static final Logger logger = LoggerFactory.getLogger(MultiStepReasoningEngine.class);

    @Reference
    private @Nullable ModelClient llmClient;

    @Reference
    private @Nullable ActionRegistry actionRegistry;

    @Reference
    private @Nullable ModelResponseActionParser actionCallParser;

    @Reference
    private @Nullable ReasoningStepPersistenceService persistenceService;

    @Reference
    private @Nullable ReasoningStepAnalysisService analysisService;

    // Metrics service for centralized metrics collection
    private @Nullable MetricsService metricsService;

    // Session durations tracking
    private final ConcurrentHashMap<String, Long> sessionDurations = new ConcurrentHashMap<>();

    // Configuration
    private @Nullable MultiStepReasoningConfiguration configuration;

    @Activate
    public void activate() {
        logger.debug("Multi-Step Reasoning Engine activated");
        this.configuration = new MultiStepReasoningConfiguration();
    }

    @Modified
    public void modified() {
        logger.debug("Multi-Step Reasoning Engine configuration modified");
    }

    @Deactivate
    public void deactivate() {
        logger.debug("Multi-Step Reasoning Engine deactivated");
    }

    /**
     * Set the metrics service for centralized metrics recording.
     * 
     * @param metricsService the metrics service to use
     */
    public void setMetricsService(@Nullable MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    /**
     * Unset the metrics service.
     * 
     * @param metricsService the metrics service to unset
     */
    public void unsetMetricsService(@Nullable MetricsService metricsService) {
        this.metricsService = null;
    }

    /**
     * Record metrics for a reasoning operation.
     * 
     * @param operation the operation name
     * @param success whether the operation was successful
     * @param durationNanos the operation duration in nanoseconds
     */
    private void recordMetrics(String operation, boolean success, long durationNanos) {
        MetricsService metrics = metricsService;
        if (metrics != null) {
            try {
                metrics.recordOperation("reasoning", operation, success, Duration.ofNanos(durationNanos));
            } catch (Exception e) {
                logger.debug("Failed to record metrics for {}.{}: {}", "reasoning", operation, e.getMessage());
            }
        } else {
            logger.debug("MetricsService not available, cannot record metrics for operation: {}", operation);
        }
    }

    /**
     * Execute multi-step reasoning with the given context
     */
    public CompletableFuture<MultiStepReasoningResult> reasonAsync(ReasoningContext context) {
        return CompletableFuture.supplyAsync(() -> {
            String sessionId = "reasoning-" + System.currentTimeMillis();
            Instant startTime = Instant.now();

            logger.debug("Starting multi-step reasoning session: {}", sessionId);
            recordMetrics("reasoning_session_start", true, Duration.between(startTime, Instant.now()).toNanos());

            try {
                return executeReasoningSession(sessionId, context, startTime);
            } catch (Exception e) {
                logger.error("Multi-step reasoning session {} failed", sessionId, e);
                recordMetrics("reasoning_session_error", false, Duration.between(startTime, Instant.now()).toNanos());
                return createErrorResult(sessionId, context, e, startTime);
            }
        });
    }

    /**
     * Execute a complete reasoning session with multiple steps
     */
    private MultiStepReasoningResult executeReasoningSession(String sessionId, ReasoningContext context,
            Instant startTime) {
        List<ReasoningStep> steps = new ArrayList<>();
        List<ActionResult> actions = new ArrayList<>();
        String accumulatedContext = context.getInitialContext();
        double confidence = 0.0;
        String finalReasoning = "";
        boolean completed = false;

        // Step-by-step reasoning loop
        for (int stepNumber = 1; stepNumber <= configuration.getMaxSteps(); stepNumber++) {
            Instant stepStartTime = Instant.now();

            try {
                // Check timeout
                if (Duration.between(startTime, stepStartTime).toMillis() > configuration.getSessionTimeoutMs()) {
                    logger.warn("Reasoning session {} timed out after {} ms", sessionId,
                            configuration.getSessionTimeoutMs());
                    break;
                }

                // Execute single reasoning step
                ReasoningStep step = executeReasoningStep(sessionId, stepNumber, accumulatedContext, context);
                steps.add(step);
                recordMetrics("reasoning_step_execution", true,
                        Duration.between(stepStartTime, Instant.now()).toNanos());

                // Accumulate context from step
                accumulatedContext = accumulateContext(accumulatedContext, step);

                // Process actions if any
                if (step.getToolCalls() != null && !step.getToolCalls().isEmpty()) {
                    for (ExecutionContext action : step.getToolCalls()) {
                        ActionResult executedAction = executeAction(action, context);
                        actions.add(executedAction);
                        recordMetrics("action_execution", true,
                                Duration.between(Instant.now(), Instant.now()).toNanos());
                    }
                }

                // Check if reasoning is complete
                if (isReasoningComplete(step)) {
                    finalReasoning = step.getReasoning();
                    confidence = calculateConfidence(steps);
                    completed = true;
                    break;
                }

                // Check confidence threshold
                if (step.getConfidence() >= configuration.getConfidenceThreshold()) {
                    finalReasoning = step.getReasoning();
                    confidence = step.getConfidence();
                    completed = true;
                    break;
                }

            } catch (Exception e) {
                logger.error("Step {} failed in reasoning session {}", stepNumber, sessionId, e);
                ReasoningStep errorStep = createErrorStep(sessionId, stepNumber, e);
                steps.add(errorStep);

                // Check if we should retry or abort
                if (shouldRetryStep(stepNumber, e)) {
                    stepNumber--; // Retry this step
                    continue;
                } else {
                    break; // Abort reasoning
                }
            }
        }

        // Create final result using the engine ReasoningStep directly
        MultiStepReasoningResult result = MultiStepReasoningResult.builder().withSessionId(sessionId).withSteps(steps)
                .withToolCalls(actions).withFinalReasoning(finalReasoning).withConfidence(confidence)
                .withCompleted(completed).withContext(context).withStartTime(startTime).withEndTime(Instant.now())
                .build();

        // Record performance metrics
        recordPerformanceMetrics(sessionId, result);

        if (completed) {
            recordMetrics("reasoning_session_success", true, Duration.between(startTime, Instant.now()).toNanos());
        } else {
            recordMetrics("reasoning_session_failure", false, Duration.between(startTime, Instant.now()).toNanos());
        }

        return result;
    }

    /**
     * Execute a single reasoning step
     */
    private ReasoningStep executeReasoningStep(String sessionId, int stepNumber, String accumulatedContext,
            ReasoningContext context) {
        Instant stepStartTime = Instant.now();

        // Build prompt with guidance
        String prompt = buildReasoningPrompt(stepNumber, accumulatedContext, context);

        // Execute LLM reasoning
        ModelResponse response = executeLLMReasoning(prompt, context);

        // Parse reasoning and actions
        String reasoning = extractReasoning(response);
        List<ExecutionContext> actions = parseResponseActions(response);
        double confidence = calculateStepConfidence(reasoning, actions);

        // Check if reasoning is complete
        boolean isComplete = isReasoningComplete(reasoning, actions);

        return ReasoningStep.builder().withSessionId(sessionId).withReasoning(reasoning).withError(null)
                .withComplete(isComplete).build();
    }

    /**
     * Build reasoning prompt with guidance
     */
    private String buildReasoningPrompt(int stepNumber, String accumulatedContext, ReasoningContext context) {
        StringBuilder prompt = new StringBuilder();

        // Add guidance prompt if enabled
        if (configuration.isGuidancePromptsEnabled()) {
            prompt.append(buildGuidancePrompt(stepNumber, context));
        }

        // Add accumulated context
        prompt.append("\n\nAccumulated Context:\n").append(accumulatedContext);

        // Add current context
        prompt.append("\n\nCurrent Context:\n").append(context.getCurrentContext());

        // Add reasoning instructions
        prompt.append("\n\nReasoning Instructions:\n");
        prompt.append("- Analyze the context step by step\n");
        prompt.append("- Use available actions if needed\n");
        prompt.append("- Provide clear reasoning for your conclusions\n");
        prompt.append("- Indicate when reasoning is complete\n");

        return prompt.toString();
    }

    /**
     * Build guidance prompt for LLM direction
     */
    private String buildGuidancePrompt(int stepNumber, ReasoningContext context) {
        StringBuilder guidance = new StringBuilder();

        guidance.append("Guidance for Step ").append(stepNumber).append(":\n");

        // Add step-specific guidance
        switch (stepNumber) {
            case 1:
                guidance.append("- Start by understanding the current situation\n");
                guidance.append("- Identify key factors and constraints\n");
                break;
            case 2:
                guidance.append("- Analyze the relationships between factors\n");
                guidance.append("- Consider potential actions and their implications\n");
                break;
            case 3:
                guidance.append("- Evaluate options and their trade-offs\n");
                guidance.append("- Consider long-term consequences\n");
                break;
            default:
                guidance.append("- Refine your analysis based on previous steps\n");
                guidance.append("- Focus on reaching a conclusion\n");
                break;
        }

        // Add context-specific guidance
        if (context.getDomain() != null) {
            guidance.append("- Consider domain-specific factors: ").append(context.getDomain()).append("\n");
        }

        return guidance.toString();
    }

    /**
     * Execute LLM reasoning
     */
    private ModelResponse executeLLMReasoning(String prompt, ReasoningContext context) {
        ModelClient client = llmClient;
        if (client == null) {
            throw new IllegalStateException("LLM client not available");
        }

        ModelParameters parameters = ModelParameters.builder().withMaxTokens(configuration.getMaxTokensPerStep())
                .withTemperature(configuration.getTemperature()).withTimeoutMs((int) configuration.getStepTimeoutMs())
                .build();

        return client.complete(prompt, parameters).join();
    }

    /**
     * Extract reasoning from LLM response
     */
    private String extractReasoning(ModelResponse response) {
        if (response.getContent() == null || response.getContent().isEmpty()) {
            return "No reasoning provided";
        }
        return response.getContent();
    }

    /**
     * Parse actions from LLM response
     */
    private List<ExecutionContext> parseResponseActions(ModelResponse response) {
        ModelResponseActionParser parser = actionCallParser;
        if (parser == null) {
            logger.warn("ActionCallParser not available, returning empty action list");
            return new ArrayList<>();
        }

        // Use the ActionCallParser to parse actions from the response
        String sessionId = "reasoning-" + System.currentTimeMillis();
        return parser.parseActionCalls(response, sessionId);
    }

    /**
     * Execute an action
     */
    private ActionResult executeAction(ExecutionContext action, ReasoningContext context) {
        Instant startTime = Instant.now();

        try {
            // Execute the action
            ActionResult result = executeActionInternal(action);
            return result;

        } catch (Exception e) {
            logger.error("Action {} failed", action.getCorrelationId(), e);
            String errorMessage = e.getMessage() != null ? e.getMessage() : "Unknown error";
            return ActionResult.error("Action execution failed: " + errorMessage,
                    new ActionError("EXECUTION_ERROR", errorMessage),
                    Duration.between(startTime, Instant.now()).toMillis());
        }
    }

    /**
     * Execute an action using ActionRegistry
     */
    private ActionResult executeActionInternal(ExecutionContext action) {
        ActionRegistry registry = actionRegistry;
        if (registry == null) {
            throw new IllegalStateException("ActionRegistry not available");
        }

        try {
            // Extract action name from the execution context
            String actionName = action.getValue("protocol.action", String.class);
            if (actionName == null || actionName.isEmpty()) {
                return ActionResult.error("Action name not found in execution context",
                        new ActionError("MISSING_ACTION_NAME", "Action name not found in execution context"), 0);
            }

            // Get the action from registry
            Action targetAction = registry.getAction(actionName);
            if (targetAction == null) {
                return ActionResult.error("Action not found: " + actionName,
                        new ActionError("ACTION_NOT_FOUND", "Action not found: " + actionName), 0);
            }

            // Extract parameters from the execution context
            @SuppressWarnings("unchecked")
            Map<String, Object> parameters = action.getValue("protocol.arguments", Map.class);
            if (parameters == null) {
                parameters = Map.of();
            }

            // Execute the action
            ActionResult result = targetAction.execute(parameters, action);
            return result;

        } catch (Exception e) {
            logger.error("Error executing action: {}", action.getCorrelationId(), e);
            String errorMessage = e.getMessage() != null ? e.getMessage() : "Unknown error";
            return ActionResult.error("Action execution failed: " + errorMessage,
                    new ActionError("EXECUTION_ERROR", errorMessage), 0);
        }
    }

    /**
     * Check if reasoning is complete
     */
    private boolean isReasoningComplete(ReasoningStep step) {
        return step.isComplete() || step.getConfidence() >= configuration.getConfidenceThreshold()
                || step.getReasoning().toLowerCase().contains("conclusion")
                || step.getReasoning().toLowerCase().contains("final answer");
    }

    /**
     * Check if reasoning is complete based on reasoning and actions
     */
    private boolean isReasoningComplete(String reasoning, List<ExecutionContext> actions) {
        return reasoning.toLowerCase().contains("conclusion") || reasoning.toLowerCase().contains("final answer")
                || reasoning.toLowerCase().contains("reasoning complete");
    }

    /**
     * Calculate confidence for a step
     */
    private double calculateStepConfidence(String reasoning, List<ExecutionContext> actions) {
        double confidence = 0.5; // Base confidence

        // Increase confidence based on reasoning quality
        if (reasoning.length() > 100)
            confidence += 0.1;
        if (reasoning.contains("because"))
            confidence += 0.1;
        if (reasoning.contains("therefore"))
            confidence += 0.1;

        // For now, we don't have success/failure info in ExecutionContext
        // This would need to be enhanced when action execution is implemented
        // confidence -= failedActions * 0.1;

        return Math.max(0.0, Math.min(1.0, confidence));
    }

    /**
     * Calculate overall confidence from all steps
     */
    private double calculateConfidence(List<ReasoningStep> steps) {
        if (steps.isEmpty())
            return 0.0;

        double totalConfidence = steps.stream().mapToDouble(ReasoningStep::getConfidence).sum();

        return totalConfidence / steps.size();
    }

    /**
     * Accumulate context from a reasoning step
     */
    private String accumulateContext(String currentContext, ReasoningStep step) {
        StringBuilder accumulated = new StringBuilder(currentContext);
        accumulated.append("\n\nStep ").append(step.getStepNumber()).append(":\n");
        accumulated.append(step.getReasoning());

        if (step.getToolCalls() != null && !step.getToolCalls().isEmpty()) {
            accumulated.append("\n\nActions:\n");
            for (ExecutionContext action : step.getToolCalls()) {
                accumulated.append("- ").append(action.getCorrelationId()).append(": ");
                accumulated.append("Context available");
                accumulated.append("\n");
            }
        }

        return accumulated.toString();
    }

    /**
     * Accumulate action result into context
     */
    private String accumulateActionResult(String currentContext, ActionResult action) {
        return currentContext + "\n\nAction Result (" + action.getMessage() + "): "
                + (action.isSuccess() ? action.getData() : "FAILED: " + action.getError());
    }

    /**
     * Check if step should be retried
     */
    private boolean shouldRetryStep(int stepNumber, Exception error) {
        // Retry on certain types of errors, but not indefinitely
        return stepNumber < configuration.getMaxSteps() && error instanceof RuntimeException; // Example retry condition
    }

    /**
     * Create error step
     */
    private ReasoningStep createErrorStep(String sessionId, int stepNumber, Exception error) {
        return ReasoningStep.builder().withSessionId(sessionId).withReasoning("Error occurred: " + error.getMessage())
                .withError(error.getMessage()).withComplete(false).build();
    }

    /**
     * Create error result
     */
    private MultiStepReasoningResult createErrorResult(String sessionId, ReasoningContext context, Exception error,
            Instant startTime) {
        return MultiStepReasoningResult.builder().withSessionId(sessionId).withSteps(new ArrayList<>())
                .withToolCalls(new ArrayList<>()).withFinalReasoning("Reasoning failed: " + error.getMessage())
                .withConfidence(0.0).withCompleted(false).withContext(context).withStartTime(startTime)
                .withEndTime(Instant.now()).withErrorMessage(error.getMessage()).build();
    }

    /**
     * Record performance metrics
     */
    private void recordPerformanceMetrics(String sessionId, MultiStepReasoningResult result) {
        long duration = Duration.between(result.getStartTime(), result.getEndTime()).toMillis();
        sessionDurations.put(sessionId, duration);

        logger.debug("Reasoning session {} completed in {} ms with {} steps and {} actions", sessionId, duration,
                result.getSteps().size(), result.getToolCalls().size());

        // Persist reasoning steps for analysis
        persistReasoningSteps(result.getSteps());

        // Trigger analysis for completed sessions (async)
        if (result.isCompleted() && !result.getSteps().isEmpty()) {
            triggerSessionAnalysis(sessionId, result.getSteps());
        }
    }

    /**
     * Persist reasoning steps to the persistence service
     */
    private void persistReasoningSteps(List<ReasoningStep> steps) {
        ReasoningStepPersistenceService persistence = persistenceService;
        if (persistence != null && !steps.isEmpty()) {
            try {
                int storedCount = persistence.storeSteps(steps);
                logger.debug("Stored {} reasoning steps for analysis", storedCount);
            } catch (Exception e) {
                logger.warn("Failed to persist reasoning steps for analysis", e);
            }
        }
    }

    /**
     * Trigger analysis for a completed reasoning session
     */
    private void triggerSessionAnalysis(String sessionId, List<ReasoningStep> steps) {
        ReasoningStepAnalysisService analysis = analysisService;
        if (analysis != null && steps.size() >= 2) { // Only analyze sessions with multiple steps
            CompletableFuture.runAsync(() -> {
                try {
                    logger.debug("Starting analysis for session {}", sessionId);

                    // Perform pattern analysis
                    var patternAnalysis = analysis.analyzePatterns(steps);
                    logger.debug("Pattern analysis completed for session {}: {} patterns identified", sessionId,
                            patternAnalysis.getStepTypeFrequency().size());

                    // Perform performance analysis
                    var performanceAnalysis = analysis.analyzePerformance(steps);
                    logger.debug("Performance analysis completed for session {}: avg time {:.1f}ms", sessionId,
                            performanceAnalysis.getAverageProcessingTime());

                    // Generate recommendations if issues are detected
                    var recommendations = analysis.generateRecommendations(steps);
                    if (!recommendations.isEmpty()) {
                        logger.info("Generated {} recommendations for session {}", recommendations.size(), sessionId);
                        recommendations.forEach(
                                rec -> logger.debug("Recommendation: {} - {}", rec.getTitle(), rec.getDescription()));
                    }

                } catch (Exception e) {
                    logger.warn("Failed to analyze reasoning session {}", sessionId, e);
                }
            }).exceptionally(throwable -> {
                logger.warn("Async analysis failed for session {}", sessionId, throwable);
                return null;
            });
        }
    }

    /**
     * Get performance metrics
     */
    public ReasoningPerformanceStatistics getPerformanceMetrics() {
        MetricsService metricsService = this.metricsService;
        if (metricsService != null) {
            try {
                return metricsService.getReasoningPerformanceStatistics("default", Duration.ofHours(1));
            } catch (Exception e) {
                logger.warn("Error retrieving reasoning performance statistics: {}", e.getMessage());
                // Fallback to empty statistics
                return ReasoningPerformanceStatistics.fromReasoningData(0L, 0L, 0L, 0L, 0L, 0L, 0.0, 0L, 0.0, 0.0, 0.0,
                        0.0, Map.of(), Map.of(), Map.of(), Map.of(), Duration.ofHours(1));
            }
        } else {
            // Fallback to empty statistics when MetricsService is not available
            return ReasoningPerformanceStatistics.fromReasoningData(0L, 0L, 0L, 0L, 0L, 0L, 0.0, 0L, 0.0, 0.0, 0.0, 0.0,
                    Map.of(), Map.of(), Map.of(), Map.of(), Duration.ofHours(1));
        }
    }

    /**
     * Reset performance metrics
     */
    public void resetMetrics() {
        // Clear session durations cache
        sessionDurations.clear();
        logger.debug("Reasoning performance metrics reset");
    }

    /**
     * Calculate average session duration
     */
    private double calculateAverageSessionDuration() {
        if (sessionDurations.isEmpty())
            return 0.0;

        long totalDuration = sessionDurations.values().stream().mapToLong(Long::longValue).sum();
        return (double) totalDuration / sessionDurations.size();
    }

    /**
     * Update configuration
     */
    public void updateConfiguration(MultiStepReasoningConfiguration newConfiguration) {
        this.configuration = newConfiguration;
        logger.debug("Multi-Step Reasoning Engine configuration updated");
    }

    /**
     * Performance metrics data class
     */
    // Inner class extracted to top-level: org.openhab.core.ai.reasoning.PerformanceMetrics
}
