package org.openhab.core.ai.agent.model;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.agent.monitoring.AgentModelEvaluationMetrics;
import org.openhab.core.ai.agent.monitoring.AgentModelPerformanceMetrics;
import org.openhab.core.ai.model.api.ModelProviderType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Evaluator for agent model performance and capabilities.
 * 
 * <p>
 * This class provides comprehensive evaluation of agent models including
 * performance testing, capability assessment, and quality metrics.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public final class AgentModelEvaluator {

    private final Logger logger = LoggerFactory.getLogger(AgentModelEvaluator.class);

    private final AgentModelRegistry modelRegistry;
    private final Map<String, AgentModelEvaluationResult> evaluationCache = new HashMap<>();
    private final Duration cacheExpiration = Duration.ofHours(1);

    /**
     * Create a new model evaluator.
     * 
     * @param modelRegistry the model registry to evaluate models from
     */
    public AgentModelEvaluator(AgentModelRegistry modelRegistry) {
        this.modelRegistry = Objects.requireNonNull(modelRegistry, "modelRegistry");
    }

    /**
     * Evaluate a specific model.
     * 
     * @param modelId the model ID to evaluate
     * @param context the evaluation context
     * @return evaluation result
     */
    public CompletableFuture<AgentModelEvaluationResult> evaluateModel(String modelId,
            AgentModelEvaluationContext context) {
        Objects.requireNonNull(modelId, "modelId");
        Objects.requireNonNull(context, "context");

        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.debug("Evaluating model: {}", modelId);

                // Check cache first
                AgentModelEvaluationResult cachedResult = getCachedEvaluation(modelId);
                if (cachedResult != null && !isCacheExpired(cachedResult)) {
                    logger.debug("Using cached evaluation result for model: {}", modelId);
                    return cachedResult;
                }

                // Get model from registry
                var modelOpt = modelRegistry.getModel(modelId);
                if (modelOpt.isEmpty()) {
                    logger.warn("Model not found for evaluation: {}", modelId);
                    return AgentModelEvaluationResult.builder(modelId).withStatus(AgentModelEvaluationStatus.NOT_FOUND)
                            .withErrorMessage("Model not found in registry").build();
                }

                AgentModel model = modelOpt.get();
                AgentModelEvaluationResult result = performEvaluation(model, context);

                // Cache the result
                cacheEvaluationResult(modelId, result);

                logger.debug("Model evaluation completed: {} - status: {}", modelId, result.getStatus());
                return result;

            } catch (Exception e) {
                logger.error("Error evaluating model: {}", modelId, e);
                return AgentModelEvaluationResult.builder(modelId).withStatus(AgentModelEvaluationStatus.ERROR)
                        .withErrorMessage("Evaluation error: " + e.getMessage()).build();
            }
        });
    }

    /**
     * Evaluate multiple models.
     * 
     * @param modelIds list of model IDs to evaluate
     * @param context the evaluation context
     * @return list of evaluation results
     */
    public CompletableFuture<List<AgentModelEvaluationResult>> evaluateModels(List<String> modelIds,
            AgentModelEvaluationContext context) {
        Objects.requireNonNull(modelIds, "modelIds");
        Objects.requireNonNull(context, "context");

        List<CompletableFuture<AgentModelEvaluationResult>> futures = new ArrayList<>();
        for (String modelId : modelIds) {
            futures.add(evaluateModel(modelId, context));
        }

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenApply(v -> {
            List<AgentModelEvaluationResult> results = new ArrayList<>();
            for (CompletableFuture<AgentModelEvaluationResult> future : futures) {
                try {
                    results.add(future.get());
                } catch (Exception e) {
                    logger.error("Error getting evaluation result", e);
                }
            }
            return results;
        });
    }

    /**
     * Evaluate models by provider type.
     * 
     * @param providerType the provider type to evaluate
     * @param context the evaluation context
     * @return list of evaluation results
     */
    public CompletableFuture<List<AgentModelEvaluationResult>> evaluateModelsByProvider(ModelProviderType providerType,
            AgentModelEvaluationContext context) {
        Objects.requireNonNull(providerType, "providerType");
        Objects.requireNonNull(context, "context");

        List<AgentModel> models = modelRegistry.getModelsByProvider(providerType);
        List<String> modelIds = models.stream().map(AgentModel::getModelId).toList();

        return evaluateModels(modelIds, context);
    }

    /**
     * Evaluate models by capability.
     * 
     * @param capability the capability to evaluate
     * @param context the evaluation context
     * @return list of evaluation results
     */
    public CompletableFuture<List<AgentModelEvaluationResult>> evaluateModelsByCapability(String capability,
            AgentModelEvaluationContext context) {
        Objects.requireNonNull(capability, "capability");
        Objects.requireNonNull(context, "context");

        List<AgentModel> models = modelRegistry.getModelsByCapability(capability);
        List<String> modelIds = models.stream().map(AgentModel::getModelId).toList();

        return evaluateModels(modelIds, context);
    }

    /**
     * Get evaluation statistics.
     * 
     * @return evaluation statistics
     */
    public AgentModelEvaluationMetrics getEvaluationStatistics() {
        Map<AgentModelEvaluationStatus, Long> statusCounts = evaluationCache.values().stream()
                .collect(java.util.stream.Collectors.groupingBy(AgentModelEvaluationResult::getStatus,
                        java.util.stream.Collectors.counting()));

        double averageScore = evaluationCache.values().stream().mapToDouble(AgentModelEvaluationResult::getOverallScore)
                .average().orElse(0.0);

        return new AgentModelEvaluationMetrics("evaluation-stats", evaluationCache.size(), statusCounts, averageScore,
                0L, 0L, 0L, 0L, 0.0);
    }

    /**
     * Clear evaluation cache.
     */
    public void clearCache() {
        evaluationCache.clear();
        logger.debug("Evaluation cache cleared");
    }

    /**
     * Perform the actual evaluation of a model.
     */
    private AgentModelEvaluationResult performEvaluation(AgentModel model, AgentModelEvaluationContext context) {
        AgentModelEvaluationResult.Builder resultBuilder = AgentModelEvaluationResult.builder(model.getModelId())
                .withModelName(model.getName()).withProviderType(model.getProviderType())
                .withEvaluationTime(Instant.now());

        try {
            // 1. Performance evaluation
            AgentModelPerformanceEvaluation performanceEval = evaluatePerformance(model, context);
            resultBuilder.withPerformanceEvaluation(performanceEval);

            // 2. Capability evaluation
            AgentModelCapabilityEvaluation capabilityEval = evaluateCapabilities(model, context);
            resultBuilder.withCapabilityEvaluation(capabilityEval);

            // 3. Quality evaluation
            AgentModelQualityEvaluation qualityEval = evaluateQuality(model, context);
            resultBuilder.withQualityEvaluation(qualityEval);

            // 4. Cost evaluation
            AgentModelCostEvaluation costEval = evaluateCost(model, context);
            resultBuilder.withCostEvaluation(costEval);

            // 5. Calculate overall score
            double overallScore = calculateOverallScore(performanceEval, capabilityEval, qualityEval, costEval);
            resultBuilder.withOverallScore(overallScore);

            // 6. Determine status
            AgentModelEvaluationStatus status = determineStatus(overallScore, performanceEval, capabilityEval,
                    qualityEval, costEval);
            resultBuilder.withStatus(status);

            return resultBuilder.build();

        } catch (Exception e) {
            logger.error("Error during model evaluation: {}", model.getModelId(), e);
            return resultBuilder.withStatus(AgentModelEvaluationStatus.ERROR)
                    .withErrorMessage("Evaluation failed: " + e.getMessage()).build();
        }
    }

    /**
     * Evaluate model performance.
     */
    private AgentModelPerformanceEvaluation evaluatePerformance(AgentModel model, AgentModelEvaluationContext context) {
        AgentModelPerformanceMetrics metrics = model.getPerformanceMetrics();

        double responseTimeScore = calculateResponseTimeScore(metrics.getAverageDurationMs());
        double throughputScore = calculateThroughputScore(metrics.getTotalOperations());
        double successRateScore = metrics.getSuccessRate();
        double availabilityScore = calculateAvailabilityScore(metrics);

        double overallPerformanceScore = (responseTimeScore + throughputScore + successRateScore + availabilityScore)
                / 4.0;

        return AgentModelPerformanceEvaluation.builder().withResponseTimeScore(responseTimeScore)
                .withThroughputScore(throughputScore).withSuccessRateScore(successRateScore)
                .withAvailabilityScore(availabilityScore).withOverallScore(overallPerformanceScore).build();
    }

    /**
     * Evaluate model capabilities.
     */
    private AgentModelCapabilityEvaluation evaluateCapabilities(AgentModel model, AgentModelEvaluationContext context) {
        double capabilityScore = 0.0;
        int capabilityCount = 0;

        for (String requiredCapability : context.getRequiredCapabilities()) {
            if (model.hasCapability(requiredCapability)) {
                capabilityScore += 1.0;
            }
            capabilityCount++;
        }

        double overallCapabilityScore = capabilityCount > 0 ? capabilityScore / capabilityCount : 0.0;

        return AgentModelCapabilityEvaluation.builder().withRequiredCapabilities(context.getRequiredCapabilities())
                .withSupportedCapabilities(model.getCapabilities()).withCapabilityScore(overallCapabilityScore)
                .withOverallScore(overallCapabilityScore).build();
    }

    /**
     * Evaluate model quality.
     */
    private AgentModelQualityEvaluation evaluateQuality(AgentModel model, AgentModelEvaluationContext context) {
        double accuracyScore = model.getPerformanceMetrics().getSuccessRate();
        double consistencyScore = calculateConsistencyScore(model);
        double reliabilityScore = calculateReliabilityScore(model);

        double overallQualityScore = (accuracyScore + consistencyScore + reliabilityScore) / 3.0;

        return AgentModelQualityEvaluation.builder().withAccuracyScore(accuracyScore)
                .withConsistencyScore(consistencyScore).withReliabilityScore(reliabilityScore)
                .withOverallScore(overallQualityScore).build();
    }

    /**
     * Evaluate model cost.
     */
    private AgentModelCostEvaluation evaluateCost(AgentModel model, AgentModelEvaluationContext context) {
        double costPerToken = model.getPerformanceMetrics().getAverageDurationMs() * 0.001; // Simplified cost
                                                                                            // calculation
        double costScore = calculateCostScore(costPerToken, context.getBudget());

        return AgentModelCostEvaluation.builder().withCostPerToken(costPerToken).withBudget(context.getBudget())
                .withCostScore(costScore).withOverallScore(costScore).build();
    }

    /**
     * Calculate overall evaluation score.
     */
    private double calculateOverallScore(AgentModelPerformanceEvaluation performance,
            AgentModelCapabilityEvaluation capability, AgentModelQualityEvaluation quality,
            AgentModelCostEvaluation cost) {
        return (performance.getOverallScore() * 0.3 + capability.getOverallScore() * 0.3
                + quality.getOverallScore() * 0.25 + cost.getOverallScore() * 0.15);
    }

    /**
     * Determine evaluation status.
     */
    private AgentModelEvaluationStatus determineStatus(double overallScore, AgentModelPerformanceEvaluation performance,
            AgentModelCapabilityEvaluation capability, AgentModelQualityEvaluation quality,
            AgentModelCostEvaluation cost) {
        if (overallScore >= 0.8) {
            return AgentModelEvaluationStatus.EXCELLENT;
        } else if (overallScore >= 0.6) {
            return AgentModelEvaluationStatus.GOOD;
        } else if (overallScore >= 0.4) {
            return AgentModelEvaluationStatus.FAIR;
        } else {
            return AgentModelEvaluationStatus.POOR;
        }
    }

    // Helper methods for score calculations
    private double calculateResponseTimeScore(double responseTimeMs) {
        if (responseTimeMs <= 1000)
            return 1.0;
        if (responseTimeMs <= 5000)
            return 0.8;
        if (responseTimeMs <= 10000)
            return 0.6;
        if (responseTimeMs <= 30000)
            return 0.4;
        return 0.2;
    }

    private double calculateThroughputScore(double throughput) {
        if (throughput >= 100)
            return 1.0;
        if (throughput >= 50)
            return 0.8;
        if (throughput >= 20)
            return 0.6;
        if (throughput >= 10)
            return 0.4;
        return 0.2;
    }

    private double calculateAvailabilityScore(AgentModelPerformanceMetrics metrics) {
        return metrics.getSuccessRate();
    }

    private double calculateConsistencyScore(AgentModel model) {
        // Simplified consistency calculation
        return 0.8; // Placeholder
    }

    private double calculateReliabilityScore(AgentModel model) {
        // Simplified reliability calculation
        return model.getPerformanceMetrics().getSuccessRate();
    }

    private double calculateCostScore(double costPerToken, double budget) {
        if (costPerToken <= budget * 0.5)
            return 1.0;
        if (costPerToken <= budget)
            return 0.8;
        if (costPerToken <= budget * 1.5)
            return 0.6;
        if (costPerToken <= budget * 2.0)
            return 0.4;
        return 0.2;
    }

    // Cache management methods
    private @Nullable AgentModelEvaluationResult getCachedEvaluation(String modelId) {
        return evaluationCache.get(modelId);
    }

    private boolean isCacheExpired(AgentModelEvaluationResult result) {
        return Duration.between(result.getEvaluationTime(), Instant.now()).compareTo(cacheExpiration) > 0;
    }

    private void cacheEvaluationResult(String modelId, AgentModelEvaluationResult result) {
        evaluationCache.put(modelId, result);
    }
}
