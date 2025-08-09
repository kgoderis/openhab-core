package org.openhab.core.ai.reasoning;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Agent Model Selector for intelligent model selection.
 * 
 * This class provides intelligent model selection capabilities based on
 * task requirements, performance metrics, and availability.
 * 
 * @author Karel Goderis - Initial Contribution
 */
@Component(service = AgentModelSelector.class)
@NonNullByDefault
public class AgentModelSelector {

    private static final Logger logger = LoggerFactory.getLogger(AgentModelSelector.class);

    @Reference
    private SharedModelReasoningEngine reasoningEngine;

    @Reference
    private AgentModelContextBuilder contextBuilder;

    @Reference
    private AgentModelPromptBuilder promptBuilder;

    private final Map<String, ModelInfo> availableModels = new ConcurrentHashMap<>();
    private final Map<String, ModelPerformance> modelPerformance = new ConcurrentHashMap<>();

    /**
     * Select the best model for a given task.
     * 
     * @param task The task to select a model for
     * @param context The agent context
     * @return A CompletableFuture containing the selected model
     */
    public CompletableFuture<ModelSelection> selectModel(Task task,
            AgentModelContextBuilder.AgentModelContext context) {
        logger.debug("Selecting model for task: {}", task.getTaskId());

        return CompletableFuture.supplyAsync(() -> {
            try {
                // Evaluate available models
                Map<String, ModelScore> modelScores = evaluateModels(task, context);

                // Select the best model
                String bestModelId = selectBestModel(modelScores);

                // Create model selection result
                ModelSelection selection = new ModelSelection(generateSelectionId(), task.getTaskId(), bestModelId,
                        modelScores.get(bestModelId), modelScores, System.currentTimeMillis());

                logger.debug("Selected model: {} for task: {}", bestModelId, task.getTaskId());
                return selection;

            } catch (Exception e) {
                logger.error("Error selecting model: {}", e.getMessage(), e);
                return createErrorModelSelection(task, e);
            }
        });
    }

    /**
     * Register a model with the selector.
     * 
     * @param modelInfo The model information
     */
    public void registerModel(ModelInfo modelInfo) {
        availableModels.put(modelInfo.getModelId(), modelInfo);
        logger.debug("Registered model: {}", modelInfo.getModelId());
    }

    /**
     * Unregister a model from the selector.
     * 
     * @param modelId The model ID to unregister
     */
    public void unregisterModel(String modelId) {
        availableModels.remove(modelId);
        modelPerformance.remove(modelId);
        logger.debug("Unregistered model: {}", modelId);
    }

    /**
     * Update model performance metrics.
     * 
     * @param modelId The model ID
     * @param performance The performance metrics
     */
    public void updateModelPerformance(String modelId, ModelPerformance performance) {
        modelPerformance.put(modelId, performance);
        logger.debug("Updated performance for model: {}", modelId);
    }

    /**
     * Evaluate all available models for a task.
     * 
     * @param task The task to evaluate models for
     * @param context The agent context
     * @return Map of model scores
     */
    private Map<String, ModelScore> evaluateModels(Task task, AgentModelContextBuilder.AgentModelContext context) {
        Map<String, ModelScore> scores = new ConcurrentHashMap<>();

        for (Map.Entry<String, ModelInfo> entry : availableModels.entrySet()) {
            String modelId = entry.getKey();
            ModelInfo modelInfo = entry.getValue();

            ModelScore score = evaluateModel(modelInfo, task, context);
            scores.put(modelId, score);
        }

        return scores;
    }

    /**
     * Evaluate a specific model for a task.
     * 
     * @param modelInfo The model information
     * @param task The task to evaluate for
     * @param context The agent context
     * @return The model score
     */
    private ModelScore evaluateModel(ModelInfo modelInfo, Task task,
            AgentModelContextBuilder.AgentModelContext context) {
        double capabilityScore = evaluateCapability(modelInfo, task);
        double performanceScore = evaluatePerformance(modelInfo);
        double availabilityScore = evaluateAvailability(modelInfo);
        double costScore = evaluateCost(modelInfo, task);
        double suitabilityScore = evaluateSuitability(modelInfo, task, context);

        // Calculate weighted score
        double totalScore = (capabilityScore * 0.3) + (performanceScore * 0.25) + (availabilityScore * 0.2)
                + (costScore * 0.15) + (suitabilityScore * 0.1);

        return new ModelScore(modelInfo.getModelId(), totalScore, capabilityScore, performanceScore, availabilityScore,
                costScore, suitabilityScore);
    }

    /**
     * Evaluate model capability for the task.
     * 
     * @param modelInfo The model information
     * @param task The task
     * @return The capability score
     */
    private double evaluateCapability(ModelInfo modelInfo, Task task) {
        // Check if model supports the required task type
        if (modelInfo.getSupportedTasks().contains(task.getTaskType())) {
            return 1.0;
        }

        // Check for partial capability
        if (modelInfo.getSupportedTasks().contains(TaskType.GENERAL)) {
            return 0.7;
        }

        return 0.0;
    }

    /**
     * Evaluate model performance.
     * 
     * @param modelInfo The model information
     * @return The performance score
     */
    private double evaluatePerformance(ModelInfo modelInfo) {
        ModelPerformance performance = modelPerformance.get(modelInfo.getModelId());
        if (performance == null) {
            return 0.5; // Default score for unknown performance
        }

        // Calculate performance score based on success rate and response time
        double successRate = performance.getSuccessRate();
        double avgResponseTime = performance.getAverageResponseTime();

        // Normalize response time (lower is better)
        double responseTimeScore = Math.max(0, 1.0 - (avgResponseTime / 10000.0)); // 10 seconds max

        return (successRate * 0.7) + (responseTimeScore * 0.3);
    }

    /**
     * Evaluate model availability.
     * 
     * @param modelInfo The model information
     * @return The availability score
     */
    private double evaluateAvailability(ModelInfo modelInfo) {
        if (modelInfo.getStatus() == ModelStatus.AVAILABLE) {
            return 1.0;
        } else if (modelInfo.getStatus() == ModelStatus.DEGRADED) {
            return 0.7;
        } else if (modelInfo.getStatus() == ModelStatus.MAINTENANCE) {
            return 0.3;
        } else {
            return 0.0; // UNAVAILABLE
        }
    }

    /**
     * Evaluate model cost for the task.
     * 
     * @param modelInfo The model information
     * @param task The task
     * @return The cost score (lower cost = higher score)
     */
    private double evaluateCost(ModelInfo modelInfo, Task task) {
        double estimatedTokens = estimateTokenUsage(task);
        double costPerToken = modelInfo.getCostPerToken();
        double totalCost = estimatedTokens * costPerToken;

        // Normalize cost (lower cost = higher score)
        // Assume $0.01 is the baseline for good cost
        return Math.max(0, 1.0 - (totalCost / 0.01));
    }

    /**
     * Evaluate model suitability for the task and context.
     * 
     * @param modelInfo The model information
     * @param task The task
     * @param context The agent context
     * @return The suitability score
     */
    private double evaluateSuitability(ModelInfo modelInfo, Task task,
            AgentModelContextBuilder.AgentModelContext context) {
        // Check if model is optimized for the agent type
        String agentType = (String) context.getContextData("agentType");
        if (modelInfo.getOptimizedFor().contains(agentType)) {
            return 1.0;
        }

        // Check if model is optimized for the domain
        String domain = (String) context.getContextData("domain");
        if (modelInfo.getOptimizedFor().contains(domain)) {
            return 0.8;
        }

        return 0.5; // General suitability
    }

    /**
     * Select the best model based on scores.
     * 
     * @param modelScores The model scores
     * @return The best model ID
     */
    private String selectBestModel(Map<String, ModelScore> modelScores) {
        String bestModelId = null;
        double bestScore = -1.0;

        for (Map.Entry<String, ModelScore> entry : modelScores.entrySet()) {
            String modelId = entry.getKey();
            ModelScore score = entry.getValue();

            if (score.getTotalScore() > bestScore) {
                bestScore = score.getTotalScore();
                bestModelId = modelId;
            }
        }

        if (bestModelId == null) {
            // Fallback to first available model
            bestModelId = modelScores.keySet().iterator().next();
        }

        return bestModelId;
    }

    /**
     * Estimate token usage for a task.
     * 
     * @param task The task
     * @return Estimated token count
     */
    private double estimateTokenUsage(Task task) {
        // Simple estimation based on task complexity
        switch (task.getTaskType()) {
            case SIMPLE_QUERY:
                return 100;
            case COMPLEX_ANALYSIS:
                return 500;
            case DECISION_MAKING:
                return 300;
            case CREATIVE_GENERATION:
                return 800;
            case GENERAL:
            default:
                return 200;
        }
    }

    /**
     * Create error model selection.
     * 
     * @param task The task
     * @param error The error that occurred
     * @return The error model selection
     */
    private ModelSelection createErrorModelSelection(Task task, Exception error) {
        return new ModelSelection(generateSelectionId(), task.getTaskId(), "ERROR",
                new ModelScore("ERROR", 0.0, 0.0, 0.0, 0.0, 0.0, 0.0), new ConcurrentHashMap<>(),
                System.currentTimeMillis());
    }

    /**
     * Generate a unique selection ID.
     * 
     * @return A unique selection identifier
     */
    private String generateSelectionId() {
        return "selection_" + System.currentTimeMillis() + "_" + Thread.currentThread().getId();
    }

    /**
     * Task class.
     */
    public static class Task {
        private final String taskId;
        private final TaskType taskType;
        private final String description;
        private final Map<String, Object> parameters;
        private final Priority priority;

        public Task(String taskId, TaskType taskType, String description, Map<String, Object> parameters,
                Priority priority) {
            this.taskId = taskId;
            this.taskType = taskType;
            this.description = description;
            this.parameters = new ConcurrentHashMap<>(parameters);
            this.priority = priority;
        }

        public String getTaskId() {
            return taskId;
        }

        public TaskType getTaskType() {
            return taskType;
        }

        public String getDescription() {
            return description;
        }

        public Map<String, Object> getParameters() {
            return new ConcurrentHashMap<>(parameters);
        }

        public Priority getPriority() {
            return priority;
        }
    }

    /**
     * Task types.
     */
    public enum TaskType {
        SIMPLE_QUERY,
        COMPLEX_ANALYSIS,
        DECISION_MAKING,
        CREATIVE_GENERATION,
        GENERAL
    }

    /**
     * Priority levels.
     */
    public enum Priority {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }

    /**
     * Model Information class.
     */
    public static class ModelInfo {
        private final String modelId;
        private final String name;
        private final String provider;
        private final ModelStatus status;
        private final java.util.Set<TaskType> supportedTasks;
        private final java.util.Set<String> optimizedFor;
        private final double costPerToken;
        private final Map<String, Object> capabilities;

        public ModelInfo(String modelId, String name, String provider, ModelStatus status,
                java.util.Set<TaskType> supportedTasks, java.util.Set<String> optimizedFor, double costPerToken,
                Map<String, Object> capabilities) {
            this.modelId = modelId;
            this.name = name;
            this.provider = provider;
            this.status = status;
            this.supportedTasks = new java.util.HashSet<>(supportedTasks);
            this.optimizedFor = new java.util.HashSet<>(optimizedFor);
            this.costPerToken = costPerToken;
            this.capabilities = new ConcurrentHashMap<>(capabilities);
        }

        public String getModelId() {
            return modelId;
        }

        public String getName() {
            return name;
        }

        public String getProvider() {
            return provider;
        }

        public ModelStatus getStatus() {
            return status;
        }

        public java.util.Set<TaskType> getSupportedTasks() {
            return new java.util.HashSet<>(supportedTasks);
        }

        public java.util.Set<String> getOptimizedFor() {
            return new java.util.HashSet<>(optimizedFor);
        }

        public double getCostPerToken() {
            return costPerToken;
        }

        public Map<String, Object> getCapabilities() {
            return new ConcurrentHashMap<>(capabilities);
        }
    }

    /**
     * Model status enum.
     */
    public enum ModelStatus {
        AVAILABLE,
        DEGRADED,
        MAINTENANCE,
        UNAVAILABLE
    }

    /**
     * Model Performance class.
     */
    public static class ModelPerformance {
        private final String modelId;
        private final double successRate;
        private final double averageResponseTime;
        private final long totalRequests;
        private final long successfulRequests;
        private final long failedRequests;
        private final long lastUpdated;

        public ModelPerformance(String modelId, double successRate, double averageResponseTime, long totalRequests,
                long successfulRequests, long failedRequests, long lastUpdated) {
            this.modelId = modelId;
            this.successRate = successRate;
            this.averageResponseTime = averageResponseTime;
            this.totalRequests = totalRequests;
            this.successfulRequests = successfulRequests;
            this.failedRequests = failedRequests;
            this.lastUpdated = lastUpdated;
        }

        public String getModelId() {
            return modelId;
        }

        public double getSuccessRate() {
            return successRate;
        }

        public double getAverageResponseTime() {
            return averageResponseTime;
        }

        public long getTotalRequests() {
            return totalRequests;
        }

        public long getSuccessfulRequests() {
            return successfulRequests;
        }

        public long getFailedRequests() {
            return failedRequests;
        }

        public long getLastUpdated() {
            return lastUpdated;
        }
    }

    /**
     * Model Score class.
     */
    public static class ModelScore {
        private final String modelId;
        private final double totalScore;
        private final double capabilityScore;
        private final double performanceScore;
        private final double availabilityScore;
        private final double costScore;
        private final double suitabilityScore;

        public ModelScore(String modelId, double totalScore, double capabilityScore, double performanceScore,
                double availabilityScore, double costScore, double suitabilityScore) {
            this.modelId = modelId;
            this.totalScore = totalScore;
            this.capabilityScore = capabilityScore;
            this.performanceScore = performanceScore;
            this.availabilityScore = availabilityScore;
            this.costScore = costScore;
            this.suitabilityScore = suitabilityScore;
        }

        public String getModelId() {
            return modelId;
        }

        public double getTotalScore() {
            return totalScore;
        }

        public double getCapabilityScore() {
            return capabilityScore;
        }

        public double getPerformanceScore() {
            return performanceScore;
        }

        public double getAvailabilityScore() {
            return availabilityScore;
        }

        public double getCostScore() {
            return costScore;
        }

        public double getSuitabilityScore() {
            return suitabilityScore;
        }
    }

    /**
     * Model Selection class.
     */
    public static class ModelSelection {
        private final String selectionId;
        private final String taskId;
        private final String selectedModelId;
        private final ModelScore selectedModelScore;
        private final Map<String, ModelScore> allScores;
        private final long timestamp;

        public ModelSelection(String selectionId, String taskId, String selectedModelId, ModelScore selectedModelScore,
                Map<String, ModelScore> allScores, long timestamp) {
            this.selectionId = selectionId;
            this.taskId = taskId;
            this.selectedModelId = selectedModelId;
            this.selectedModelScore = selectedModelScore;
            this.allScores = new ConcurrentHashMap<>(allScores);
            this.timestamp = timestamp;
        }

        public String getSelectionId() {
            return selectionId;
        }

        public String getTaskId() {
            return taskId;
        }

        public String getSelectedModelId() {
            return selectedModelId;
        }

        public ModelScore getSelectedModelScore() {
            return selectedModelScore;
        }

        public Map<String, ModelScore> getAllScores() {
            return new ConcurrentHashMap<>(allScores);
        }

        public long getTimestamp() {
            return timestamp;
        }
    }
}
