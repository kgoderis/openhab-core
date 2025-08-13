package org.openhab.core.ai.model.api;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.model.ModelException;
import org.openhab.core.ai.model.ModelParameters;
import org.openhab.core.ai.model.ModelResponse;
import org.openhab.core.ai.reasoning.api.MultiStepReasoningResult;
import org.openhab.core.ai.reasoning.api.ReasoningContext;

/**
 * Enhanced interface for intelligent LLM clients with reasoning capabilities.
 * 
 * This interface extends the basic ModelClient with advanced intelligence features
 * including multi-step reasoning, context awareness, memory management, and
 * autonomous behavior capabilities.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public interface IntelligentToolClient extends ModelClient {

    /**
     * Performs multi-step reasoning with context awareness.
     * 
     * @param context The reasoning context containing initial state and goals
     * @param params Configuration parameters for the reasoning process
     * @return A CompletableFuture containing the multi-step reasoning result
     * @throws ModelException if the reasoning process fails
     */
    CompletableFuture<MultiStepReasoningResult> reasonWithContext(ReasoningContext context, ModelParameters params);

    /**
     * Generates a completion with enhanced context awareness.
     * 
     * @param prompt The input prompt
     * @param context The reasoning context for enhanced understanding
     * @param params Configuration parameters
     * @return A CompletableFuture containing the enhanced LLM response
     * @throws ModelException if the request fails
     */
    CompletableFuture<ModelResponse> completeWithContext(String prompt, ReasoningContext context,
            ModelParameters params);

    /**
     * Executes a reasoning step with memory integration.
     * 
     * @param stepId Unique identifier for the reasoning step
     * @param stepPrompt The prompt for this specific reasoning step
     * @param context The current reasoning context
     * @param memoryContext Memory context for learning and adaptation
     * @param params Configuration parameters
     * @return A CompletableFuture containing the step result
     * @throws ModelException if the step execution fails
     */
    CompletableFuture<ModelResponse> executeReasoningStep(String stepId, String stepPrompt, ReasoningContext context,
            Map<String, Object> memoryContext, ModelParameters params);

    /**
     * Performs autonomous reasoning with learning capabilities.
     * 
     * @param initialContext The initial reasoning context
     * @param learningConfig Configuration for learning and adaptation
     * @param params Configuration parameters
     * @return A CompletableFuture containing the autonomous reasoning result
     * @throws ModelException if the autonomous reasoning fails
     */
    CompletableFuture<MultiStepReasoningResult> reasonAutonomously(ReasoningContext initialContext,
            Map<String, Object> learningConfig, ModelParameters params);

    /**
     * Generates a completion with memory-enhanced capabilities.
     * 
     * @param prompt The input prompt
     * @param memoryContext Memory context for enhanced responses
     * @param params Configuration parameters
     * @return A CompletableFuture containing the memory-enhanced response
     * @throws ModelException if the request fails
     */
    CompletableFuture<ModelResponse> completeWithMemory(String prompt, Map<String, Object> memoryContext,
            ModelParameters params);

    /**
     * Learns from feedback and adapts behavior.
     * 
     * @param feedback The feedback data for learning
     * @param context The context in which the feedback occurred
     * @return A CompletableFuture that completes when learning is finished
     * @throws ModelException if the learning process fails
     */
    CompletableFuture<Boolean> learnFromFeedback(Map<String, Object> feedback, ReasoningContext context);

    /**
     * Optimizes performance based on usage patterns.
     * 
     * @param optimizationConfig Configuration for performance optimization
     * @return A CompletableFuture that completes when optimization is finished
     * @throws ModelException if the optimization fails
     */
    CompletableFuture<Boolean> optimizePerformance(Map<String, Object> optimizationConfig);

    /**
     * Validates safety constraints for a given reasoning context.
     * 
     * @param context The reasoning context to validate
     * @param safetyConstraints The safety constraints to apply
     * @return A CompletableFuture containing validation results
     * @throws ModelException if validation fails
     */
    CompletableFuture<SafetyValidationResult> validateSafetyConstraints(ReasoningContext context,
            List<SafetyConstraint> safetyConstraints);

    /**
     * Gets intelligence capabilities and features.
     * 
     * @return Information about the intelligence capabilities
     */
    IntelligenceCapabilities getIntelligenceCapabilities();

    /**
     * Gets the current learning and adaptation status.
     * 
     * @return Status information about learning and adaptation
     */
    LearningAdaptationStatus getLearningAdaptationStatus();

    /**
     * Gets reasoning performance metrics.
     * 
     * @return Performance metrics for reasoning operations
     */
    ReasoningPerformanceMetrics getReasoningPerformanceMetrics();

    /**
     * Checks if this client supports autonomous reasoning.
     * 
     * @return true if autonomous reasoning is supported
     */
    boolean supportsAutonomousReasoning();

    /**
     * Checks if this client supports learning and adaptation.
     * 
     * @return true if learning and adaptation is supported
     */
    boolean supportsLearningAndAdaptation();

    /**
     * Checks if this client supports safety constraint validation.
     * 
     * @return true if safety constraint validation is supported
     */
    boolean supportsSafetyConstraints();

    /**
     * Result of safety constraint validation.
     */
    interface SafetyValidationResult {
        /**
         * Whether the context passes all safety constraints.
         */
        boolean isValid();

        /**
         * List of constraint violations, if any.
         */
        List<ConstraintViolation> getViolations();

        /**
         * Recommendations for addressing violations.
         */
        List<String> getRecommendations();
    }

    /**
     * Represents a safety constraint.
     */
    interface SafetyConstraint {
        /**
         * The constraint identifier.
         */
        String getId();

        /**
         * The constraint type.
         */
        String getType();

        /**
         * The constraint parameters.
         */
        Map<String, Object> getParameters();

        /**
         * The severity level of the constraint.
         */
        Severity getSeverity();
    }

    /**
     * Represents a constraint violation.
     */
    interface ConstraintViolation {
        /**
         * The violated constraint ID.
         */
        String getConstraintId();

        /**
         * Description of the violation.
         */
        String getDescription();

        /**
         * The severity of the violation.
         */
        Severity getSeverity();

        /**
         * Suggested actions to resolve the violation.
         */
        List<String> getSuggestedActions();
    }

    /**
     * Severity levels for constraints and violations.
     */
    enum Severity {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }

    /**
     * Intelligence capabilities information.
     */
    interface IntelligenceCapabilities {
        /**
         * Whether multi-step reasoning is supported.
         */
        boolean supportsMultiStepReasoning();

        /**
         * Whether context memory is supported.
         */
        boolean supportsContextMemory();

        /**
         * Whether autonomous behavior is supported.
         */
        boolean supportsAutonomousBehavior();

        /**
         * Whether learning and adaptation is supported.
         */
        boolean supportsLearningAndAdaptation();

        /**
         * Whether safety constraints are supported.
         */
        boolean supportsSafetyConstraints();

        /**
         * Maximum number of reasoning steps supported.
         */
        int getMaxReasoningSteps();

        /**
         * Maximum context memory size.
         */
        long getMaxContextMemorySize();

        /**
         * Supported reasoning strategies.
         */
        List<String> getSupportedReasoningStrategies();
    }

    /**
     * Learning and adaptation status.
     */
    interface LearningAdaptationStatus {
        /**
         * Whether learning is currently active.
         */
        boolean isLearningActive();

        /**
         * Current learning rate.
         */
        double getLearningRate();

        /**
         * Number of learning iterations completed.
         */
        long getLearningIterations();

        /**
         * Last learning update timestamp.
         */
        long getLastLearningUpdate();

        /**
         * Learning performance metrics.
         */
        Map<String, Double> getLearningMetrics();
    }

    /**
     * Reasoning performance metrics.
     */
    interface ReasoningPerformanceMetrics {
        /**
         * Average reasoning time in milliseconds.
         */
        double getAverageReasoningTime();

        /**
         * Total number of reasoning operations.
         */
        long getTotalReasoningOperations();

        /**
         * Success rate of reasoning operations.
         */
        double getSuccessRate();

        /**
         * Average number of reasoning steps per operation.
         */
        double getAverageReasoningSteps();

        /**
         * Performance metrics by reasoning strategy.
         */
        Map<String, Double> getStrategyMetrics();
    }
}
