package org.openhab.core.ai.agent.model;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.action.api.ActionResult;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
import org.openhab.core.ai.common.result.BaseResult;
import org.openhab.core.ai.model.api.ModelProviderType;
import org.osgi.service.component.annotations.Reference;

/**
 * Result of agent model evaluation.
 * 
 * <p>
 * This class extends BaseResult to represent the comprehensive evaluation result for an agent model,
 * including performance, capability, quality, and cost assessments.
 * It maintains compatibility with the ActionResult pattern through the toActionResult() method.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public final class AgentModelEvaluationResult extends BaseResult {

    @Reference
    private @Nullable MetricsService metricsService;

    private static final String RESULT_TYPE = "agent-model-evaluation";
    private static final String MODEL_ID_KEY = "modelId";
    private static final String PROVIDER_TYPE_KEY = "providerType";
    private static final String STATUS_KEY = "status";
    private static final String PERFORMANCE_SCORE_KEY = "performanceScore";
    private static final String CAPABILITY_SCORE_KEY = "capabilityScore";
    private static final String QUALITY_SCORE_KEY = "qualityScore";
    private static final String COST_SCORE_KEY = "costScore";
    private static final String OVERALL_SCORE_KEY = "overallScore";
    private static final String EVALUATION_DETAILS_KEY = "evaluationDetails";

    private AgentModelEvaluationResult(String resultId, boolean success, String message, @Nullable String errorMessage,
            long executionTimeMs, Instant timestamp, @Nullable Map<String, Object> metadata) {
        super(resultId, RESULT_TYPE, success, message, errorMessage, executionTimeMs, timestamp, metadata);
    }

    public static Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public String getModelId() {
        Object value = getMetadata(MODEL_ID_KEY);
        return value instanceof String ? (String) value : "";
    }

    public ModelProviderType getProviderType() {
        Object value = getMetadata(PROVIDER_TYPE_KEY);
        String providerName = value instanceof String ? (String) value : ModelProviderType.OPENAI.name();
        try {
            return ModelProviderType.valueOf(providerName);
        } catch (IllegalArgumentException e) {
            return ModelProviderType.OPENAI; // default fallback
        }
    }

    public AgentModelEvaluationStatus getStatus() {
        Object value = getMetadata(STATUS_KEY);
        String statusName = value instanceof String ? (String) value : AgentModelEvaluationStatus.PENDING.name();
        try {
            return AgentModelEvaluationStatus.valueOf(statusName);
        } catch (IllegalArgumentException e) {
            return AgentModelEvaluationStatus.PENDING; // default fallback
        }
    }

    public double getPerformanceScore() {
        Object value = getMetadata(PERFORMANCE_SCORE_KEY);
        return value instanceof Number ? ((Number) value).doubleValue() : 0.0;
    }

    public double getCapabilityScore() {
        Object value = getMetadata(CAPABILITY_SCORE_KEY);
        return value instanceof Number ? ((Number) value).doubleValue() : 0.0;
    }

    public double getQualityScore() {
        Object value = getMetadata(QUALITY_SCORE_KEY);
        return value instanceof Number ? ((Number) value).doubleValue() : 0.0;
    }

    public double getCostScore() {
        Object value = getMetadata(COST_SCORE_KEY);
        return value instanceof Number ? ((Number) value).doubleValue() : 0.0;
    }

    public double getOverallScore() {
        Object value = getMetadata(OVERALL_SCORE_KEY);
        return value instanceof Number ? ((Number) value).doubleValue() : 0.0;
    }

    /**
     * Record evaluation metrics using MetricsService.
     */
    public void recordEvaluation() {
        MetricsService metrics = metricsService;
        if (metrics != null) {
            long startTime = System.currentTimeMillis();
            boolean success = isSuccess();
            long duration = System.currentTimeMillis() - startTime;

            metrics.recordOperation("agent-model", "evaluation", success, java.time.Duration.ofMillis(duration));
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getEvaluationDetails() {
        Object value = getMetadata(EVALUATION_DETAILS_KEY);
        if (value instanceof Map) {
            return (Map<String, Object>) value;
        }
        return Map.of();
    }

    public boolean isSuccessful() {
        return getStatus() != AgentModelEvaluationStatus.ERROR && getStatus() != AgentModelEvaluationStatus.NOT_FOUND;
    }

    /**
     * Convert this evaluation result to an ActionResult.
     * 
     * @return the ActionResult representation
     */
    public ActionResult toActionResult() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("evaluationId", getResultId());
        payload.put("modelId", getModelId());
        payload.put("providerType", getProviderType().name());
        payload.put("status", getStatus().name());
        payload.put("performanceScore", getPerformanceScore());
        payload.put("capabilityScore", getCapabilityScore());
        payload.put("qualityScore", getQualityScore());
        payload.put("costScore", getCostScore());
        payload.put("overallScore", getOverallScore());
        payload.put("evaluationTime", getTimestamp().toString());
        payload.put("evaluationDurationMs", getExecutionTimeMs());

        if (isSuccessful()) {
            return ActionResult.success(payload, getExecutionTimeMs());
        } else {
            return ActionResult.error(getErrorMessage() != null ? getErrorMessage() : "Model evaluation failed", null,
                    getExecutionTimeMs());
        }
    }

    public static final class Builder {
        private String resultId = "";
        private boolean success = false;
        private String message = "Model evaluation result";
        private @Nullable String errorMessage = null;
        private long executionTimeMs = 0;
        private Instant timestamp = Instant.now();
        private final Map<String, Object> metadata = new HashMap<>();

        public Builder() {
        }

        public Builder(AgentModelEvaluationResult source) {
            this.resultId = source.getResultId();
            this.success = source.isSuccess();
            this.message = source.getMessage();
            this.errorMessage = source.getErrorMessage();
            this.executionTimeMs = source.getExecutionTimeMs();
            this.timestamp = source.getTimestamp();
            this.metadata.putAll(source.getMetadata());
        }

        public Builder withResultId(String resultId) {
            this.resultId = Objects.requireNonNull(resultId, "resultId");
            return this;
        }

        public Builder withSuccess(boolean success) {
            this.success = success;
            return this;
        }

        public Builder withMessage(String message) {
            this.message = Objects.requireNonNull(message, "message");
            return this;
        }

        public Builder withErrorMessage(@Nullable String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public Builder withExecutionTimeMs(long executionTimeMs) {
            this.executionTimeMs = executionTimeMs;
            return this;
        }

        public Builder withTimestamp(Instant timestamp) {
            this.timestamp = Objects.requireNonNull(timestamp, "timestamp");
            return this;
        }

        public Builder withModelId(String modelId) {
            this.metadata.put(MODEL_ID_KEY, Objects.requireNonNull(modelId, "modelId"));
            return this;
        }

        public Builder withProviderType(ModelProviderType providerType) {
            this.metadata.put(PROVIDER_TYPE_KEY, Objects.requireNonNull(providerType, "providerType").name());
            return this;
        }

        public Builder withStatus(AgentModelEvaluationStatus status) {
            this.metadata.put(STATUS_KEY, Objects.requireNonNull(status, "status").name());
            return this;
        }

        public Builder withPerformanceScore(double performanceScore) {
            this.metadata.put(PERFORMANCE_SCORE_KEY, performanceScore);
            return this;
        }

        public Builder withCapabilityScore(double capabilityScore) {
            this.metadata.put(CAPABILITY_SCORE_KEY, capabilityScore);
            return this;
        }

        public Builder withQualityScore(double qualityScore) {
            this.metadata.put(QUALITY_SCORE_KEY, qualityScore);
            return this;
        }

        public Builder withCostScore(double costScore) {
            this.metadata.put(COST_SCORE_KEY, costScore);
            return this;
        }

        public Builder withOverallScore(double overallScore) {
            this.metadata.put(OVERALL_SCORE_KEY, overallScore);
            return this;
        }

        public Builder withEvaluationDetails(Map<String, Object> evaluationDetails) {
            this.metadata.put(EVALUATION_DETAILS_KEY, Objects.requireNonNull(evaluationDetails, "evaluationDetails"));
            return this;
        }

        public AgentModelEvaluationResult build() {
            if (resultId.isBlank()) {
                throw new IllegalArgumentException("resultId must not be blank");
            }

            // Validate scores if they exist in metadata
            Number performanceScore = (Number) metadata.get(PERFORMANCE_SCORE_KEY);
            if (performanceScore != null
                    && (performanceScore.doubleValue() < 0.0 || performanceScore.doubleValue() > 1.0)) {
                throw new IllegalArgumentException("performanceScore must be between 0.0 and 1.0");
            }

            Number capabilityScore = (Number) metadata.get(CAPABILITY_SCORE_KEY);
            if (capabilityScore != null
                    && (capabilityScore.doubleValue() < 0.0 || capabilityScore.doubleValue() > 1.0)) {
                throw new IllegalArgumentException("capabilityScore must be between 0.0 and 1.0");
            }

            Number qualityScore = (Number) metadata.get(QUALITY_SCORE_KEY);
            if (qualityScore != null && (qualityScore.doubleValue() < 0.0 || qualityScore.doubleValue() > 1.0)) {
                throw new IllegalArgumentException("qualityScore must be between 0.0 and 1.0");
            }

            Number costScore = (Number) metadata.get(COST_SCORE_KEY);
            if (costScore != null && (costScore.doubleValue() < 0.0 || costScore.doubleValue() > 1.0)) {
                throw new IllegalArgumentException("costScore must be between 0.0 and 1.0");
            }

            Number overallScore = (Number) metadata.get(OVERALL_SCORE_KEY);
            if (overallScore != null && (overallScore.doubleValue() < 0.0 || overallScore.doubleValue() > 1.0)) {
                throw new IllegalArgumentException("overallScore must be between 0.0 and 1.0");
            }

            if (executionTimeMs < 0) {
                throw new IllegalArgumentException("executionTimeMs must be non-negative");
            }

            return new AgentModelEvaluationResult(resultId, success, message, errorMessage, executionTimeMs, timestamp,
                    metadata);
        }
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj) || getClass() != obj.getClass()) {
            return false;
        }
        AgentModelEvaluationResult other = (AgentModelEvaluationResult) obj;
        return Objects.equals(getModelId(), other.getModelId()) && getProviderType() == other.getProviderType()
                && getStatus() == other.getStatus()
                && Double.compare(getPerformanceScore(), other.getPerformanceScore()) == 0
                && Double.compare(getCapabilityScore(), other.getCapabilityScore()) == 0
                && Double.compare(getQualityScore(), other.getQualityScore()) == 0
                && Double.compare(getCostScore(), other.getCostScore()) == 0
                && Double.compare(getOverallScore(), other.getOverallScore()) == 0
                && Objects.equals(getEvaluationDetails(), other.getEvaluationDetails());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getModelId(), getProviderType(), getStatus(), getPerformanceScore(),
                getCapabilityScore(), getQualityScore(), getCostScore(), getOverallScore(), getEvaluationDetails());
    }

    @Override
    public String toString() {
        return super.toString().replace("}", "") + ", modelId='" + getModelId() + '\'' + ", providerType="
                + getProviderType() + ", status=" + getStatus() + ", performanceScore=" + getPerformanceScore()
                + ", capabilityScore=" + getCapabilityScore() + ", qualityScore=" + getQualityScore() + ", costScore="
                + getCostScore() + ", overallScore=" + getOverallScore() + ", evaluationDetails="
                + getEvaluationDetails() + '}';
    }
}
