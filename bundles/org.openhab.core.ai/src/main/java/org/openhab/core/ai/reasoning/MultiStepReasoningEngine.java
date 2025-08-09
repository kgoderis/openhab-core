package org.openhab.core.ai.reasoning;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.action.ActionContext;
import org.openhab.core.ai.action.ActionError;
import org.openhab.core.ai.action.ActionRegistry;
import org.openhab.core.ai.action.ActionResult;
import org.openhab.core.ai.model.ModelResponseActionParser;
import org.openhab.core.ai.model.api.ModelClient;
import org.openhab.core.ai.model.api.ModelParameters;
import org.openhab.core.ai.model.api.ModelResponse;
import org.openhab.core.ai.reasoning.api.MultiStepReasoningConfiguration;
import org.openhab.core.ai.reasoning.api.MultiStepReasoningResult;
import org.openhab.core.ai.reasoning.api.ReasoningContext;
import org.openhab.core.ai.reasoning.api.ReasoningStep;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
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

    // Performance monitoring
    private final AtomicLong totalReasoningSessions = new AtomicLong(0);
    private final AtomicLong successfulReasoningSessions = new AtomicLong(0);
    private final AtomicLong failedReasoningSessions = new AtomicLong(0);
    private final AtomicLong totalReasoningSteps = new AtomicLong(0);
    private final AtomicLong totalActions = new AtomicLong(0);
    private final ConcurrentHashMap<String, Long> sessionDurations = new ConcurrentHashMap<>();

    // Configuration
    private @Nullable MultiStepReasoningConfiguration configuration;

    @Activate
    public void activate() {
        logger.debug("Multi-Step Reasoning Engine activated");
        this.configuration = new MultiStepReasoningConfiguration();
    }

    @Deactivate
    public void deactivate() {
        logger.debug("Multi-Step Reasoning Engine deactivated");
    }

    /**
     * Execute multi-step reasoning with the given context
     */
    public CompletableFuture<MultiStepReasoningResult> reasonAsync(ReasoningContext context) {
        return CompletableFuture.supplyAsync(() -> {
            String sessionId = "reasoning-" + System.currentTimeMillis();
            Instant startTime = Instant.now();

            logger.debug("Starting multi-step reasoning session: {}", sessionId);
            totalReasoningSessions.incrementAndGet();

            try {
                return executeReasoningSession(sessionId, context, startTime);
            } catch (Exception e) {
                logger.error("Multi-step reasoning session {} failed", sessionId, e);
                failedReasoningSessions.incrementAndGet();
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
                totalReasoningSteps.incrementAndGet();

                // Accumulate context from step
                accumulatedContext = accumulateContext(accumulatedContext, step);

                // Process actions if any
                if (step.getToolCalls() != null && !step.getToolCalls().isEmpty()) {
                    for (ActionContext action : step.getToolCalls()) {
                        ActionResult executedAction = executeAction(action, context);
                        actions.add(executedAction);
                        totalActions.incrementAndGet();

                        // Add action result to accumulated context
                        accumulatedContext = accumulateActionResult(accumulatedContext, executedAction);
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

        // Create final result
        MultiStepReasoningResult result = MultiStepReasoningResult.builder().sessionId(sessionId).steps(steps)
                .toolCalls(actions).finalReasoning(finalReasoning).confidence(confidence).completed(completed)
                .context(context).startTime(startTime).endTime(Instant.now()).build();

        // Record performance metrics
        recordPerformanceMetrics(sessionId, result);

        if (completed) {
            successfulReasoningSessions.incrementAndGet();
        } else {
            failedReasoningSessions.incrementAndGet();
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
        List<ActionContext> actions = parseResponseActions(response);
        double confidence = calculateStepConfidence(reasoning, actions);

        // Check if reasoning is complete
        boolean isComplete = isReasoningComplete(reasoning, actions);

        return ReasoningStep.builder().sessionId(sessionId).stepNumber(stepNumber).reasoning(reasoning)
                .toolCalls(actions).confidence(confidence).isComplete(isComplete).startTime(stepStartTime)
                .endTime(Instant.now()).build();
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

        ModelParameters parameters = ModelParameters.builder().maxTokens(configuration.getMaxTokensPerStep())
                .temperature(configuration.getTemperature()).timeoutMs((int) configuration.getStepTimeoutMs()).build();

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
    private List<ActionContext> parseResponseActions(ModelResponse response) {
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
    private ActionResult executeAction(ActionContext action, ReasoningContext context) {
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
    private ActionResult executeActionInternal(ActionContext action) {
        ActionRegistry registry = actionRegistry;
        if (registry == null) {
            throw new IllegalStateException("ActionRegistry not available");
        }

        // This would integrate with the unified action execution system
        // For now, throw an exception
        throw new UnsupportedOperationException("Action execution not yet implemented");
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
    private boolean isReasoningComplete(String reasoning, List<ActionContext> actions) {
        return reasoning.toLowerCase().contains("conclusion") || reasoning.toLowerCase().contains("final answer")
                || reasoning.toLowerCase().contains("reasoning complete");
    }

    /**
     * Calculate confidence for a step
     */
    private double calculateStepConfidence(String reasoning, List<ActionContext> actions) {
        double confidence = 0.5; // Base confidence

        // Increase confidence based on reasoning quality
        if (reasoning.length() > 100)
            confidence += 0.1;
        if (reasoning.contains("because"))
            confidence += 0.1;
        if (reasoning.contains("therefore"))
            confidence += 0.1;

        // For now, we don't have success/failure info in ActionContext
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
            for (ActionContext action : step.getToolCalls()) {
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
        return ReasoningStep.builder().sessionId(sessionId).stepNumber(stepNumber)
                .reasoning("Error occurred: " + error.getMessage()).confidence(0.0).isComplete(false)
                .error(error.getMessage()).startTime(Instant.now()).endTime(Instant.now()).build();
    }

    /**
     * Create error result
     */
    private MultiStepReasoningResult createErrorResult(String sessionId, ReasoningContext context, Exception error,
            Instant startTime) {
        return MultiStepReasoningResult.builder().sessionId(sessionId).steps(new ArrayList<>())
                .toolCalls(new ArrayList<>()).finalReasoning("Reasoning failed: " + error.getMessage()).confidence(0.0)
                .completed(false).context(context).startTime(startTime).endTime(Instant.now()).error(error.getMessage())
                .build();
    }

    /**
     * Record performance metrics
     */
    private void recordPerformanceMetrics(String sessionId, MultiStepReasoningResult result) {
        long duration = Duration.between(result.getStartTime(), result.getEndTime()).toMillis();
        sessionDurations.put(sessionId, duration);

        logger.debug("Reasoning session {} completed in {} ms with {} steps and {} actions", sessionId, duration,
                result.getSteps().size(), result.getToolCalls().size());
    }

    /**
     * Get performance metrics
     */
    public PerformanceMetrics getPerformanceMetrics() {
        return PerformanceMetrics.builder().totalSessions(totalReasoningSessions.get())
                .successfulSessions(successfulReasoningSessions.get()).failedSessions(failedReasoningSessions.get())
                .totalSteps(totalReasoningSteps.get()).totalActions(totalActions.get())
                .averageSessionDuration(calculateAverageSessionDuration()).build();
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
    public static class PerformanceMetrics {
        private final long totalSessions;
        private final long successfulSessions;
        private final long failedSessions;
        private final long totalSteps;
        private final long totalActions;
        private final double averageSessionDuration;

        private PerformanceMetrics(Builder builder) {
            this.totalSessions = builder.totalSessions;
            this.successfulSessions = builder.successfulSessions;
            this.failedSessions = builder.failedSessions;
            this.totalSteps = builder.totalSteps;
            this.totalActions = builder.totalActions;
            this.averageSessionDuration = builder.averageSessionDuration;
        }

        public long getTotalSessions() {
            return totalSessions;
        }

        public long getSuccessfulSessions() {
            return successfulSessions;
        }

        public long getFailedSessions() {
            return failedSessions;
        }

        public long getTotalSteps() {
            return totalSteps;
        }

        public long getTotalActions() {
            return totalActions;
        }

        public double getAverageSessionDuration() {
            return averageSessionDuration;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private long totalSessions;
            private long successfulSessions;
            private long failedSessions;
            private long totalSteps;
            private long totalActions;
            private double averageSessionDuration;

            public Builder totalSessions(long totalSessions) {
                this.totalSessions = totalSessions;
                return this;
            }

            public Builder successfulSessions(long successfulSessions) {
                this.successfulSessions = successfulSessions;
                return this;
            }

            public Builder failedSessions(long failedSessions) {
                this.failedSessions = failedSessions;
                return this;
            }

            public Builder totalSteps(long totalSteps) {
                this.totalSteps = totalSteps;
                return this;
            }

            public Builder totalActions(long totalActions) {
                this.totalActions = totalActions;
                return this;
            }

            public Builder averageSessionDuration(double averageSessionDuration) {
                this.averageSessionDuration = averageSessionDuration;
                return this;
            }

            public PerformanceMetrics build() {
                return new PerformanceMetrics(this);
            }
        }
    }
}
