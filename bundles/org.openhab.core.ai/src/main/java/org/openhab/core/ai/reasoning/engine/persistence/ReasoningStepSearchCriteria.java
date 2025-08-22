package org.openhab.core.ai.reasoning.engine.persistence;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.reasoning.engine.api.ReasoningStepStatus;
import org.openhab.core.ai.reasoning.engine.api.ReasoningStepType;

/**
 * Search criteria for reasoning steps.
 * 
 * <p>
 * This class provides flexible search criteria for finding reasoning steps based on various attributes.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public final class ReasoningStepSearchCriteria {

    private final @Nullable String sessionId;
    private final @Nullable List<Integer> stepNumbers;
    private final @Nullable ReasoningStepType stepType;
    private final @Nullable ReasoningStepStatus status;
    private final @Nullable String modelId;
    private final @Nullable String modelVersion;
    private final @Nullable Instant startTime;
    private final @Nullable Instant endTime;
    private final @Nullable Double minConfidence;
    private final @Nullable Double maxConfidence;
    private final @Nullable Double minQualityScore;
    private final @Nullable Double maxQualityScore;
    private final @Nullable Long minTokens;
    private final @Nullable Long maxTokens;
    private final @Nullable Double minCost;
    private final @Nullable Double maxCost;
    private final @Nullable String errorPattern;
    private final @Nullable Map<String, Object> metadataFilters;
    private final @Nullable List<String> parentStepIds;
    private final @Nullable List<String> childStepIds;
    private final @Nullable String previousStepId;
    private final int limit;
    private final boolean includeArchived;

    private ReasoningStepSearchCriteria(Builder builder) {
        this.sessionId = builder.sessionId;
        this.stepNumbers = builder.stepNumbers != null ? List.copyOf(builder.stepNumbers) : null;
        this.stepType = builder.stepType;
        this.status = builder.status;
        this.modelId = builder.modelId;
        this.modelVersion = builder.modelVersion;
        this.startTime = builder.startTime;
        this.endTime = builder.endTime;
        this.minConfidence = builder.minConfidence;
        this.maxConfidence = builder.maxConfidence;
        this.minQualityScore = builder.minQualityScore;
        this.maxQualityScore = builder.maxQualityScore;
        this.minTokens = builder.minTokens;
        this.maxTokens = builder.maxTokens;
        this.minCost = builder.minCost;
        this.maxCost = builder.maxCost;
        this.errorPattern = builder.errorPattern;
        this.metadataFilters = builder.metadataFilters != null ? Map.copyOf(builder.metadataFilters) : null;
        this.parentStepIds = builder.parentStepIds != null ? List.copyOf(builder.parentStepIds) : null;
        this.childStepIds = builder.childStepIds != null ? List.copyOf(builder.childStepIds) : null;
        this.previousStepId = builder.previousStepId;
        this.limit = builder.limit;
        this.includeArchived = builder.includeArchived;
    }

    /**
     * Create a new builder for ReasoningStepSearchCriteria.
     * 
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Create a builder from this instance for modification.
     * 
     * @return a new builder with current values
     */
    public Builder toBuilder() {
        return new Builder(this);
    }

    // Getters
    public @Nullable String getSessionId() {
        return sessionId;
    }

    public @Nullable List<Integer> getStepNumbers() {
        return stepNumbers;
    }

    public @Nullable ReasoningStepType getStepType() {
        return stepType;
    }

    public @Nullable ReasoningStepStatus getStatus() {
        return status;
    }

    public @Nullable String getModelId() {
        return modelId;
    }

    public @Nullable String getModelVersion() {
        return modelVersion;
    }

    public @Nullable Instant getStartTime() {
        return startTime;
    }

    public @Nullable Instant getEndTime() {
        return endTime;
    }

    public @Nullable Double getMinConfidence() {
        return minConfidence;
    }

    public @Nullable Double getMaxConfidence() {
        return maxConfidence;
    }

    public @Nullable Double getMinQualityScore() {
        return minQualityScore;
    }

    public @Nullable Double getMaxQualityScore() {
        return maxQualityScore;
    }

    public @Nullable Long getMinTokens() {
        return minTokens;
    }

    public @Nullable Long getMaxTokens() {
        return maxTokens;
    }

    public @Nullable Double getMinCost() {
        return minCost;
    }

    public @Nullable Double getMaxCost() {
        return maxCost;
    }

    public @Nullable String getErrorPattern() {
        return errorPattern;
    }

    public @Nullable Map<String, Object> getMetadataFilters() {
        return metadataFilters;
    }

    public @Nullable List<String> getParentStepIds() {
        return parentStepIds;
    }

    public @Nullable List<String> getChildStepIds() {
        return childStepIds;
    }

    public @Nullable String getPreviousStepId() {
        return previousStepId;
    }

    public int getLimit() {
        return limit;
    }

    public boolean isIncludeArchived() {
        return includeArchived;
    }

    /**
     * Check if this search criteria has any filters set.
     * 
     * @return true if any filters are set
     */
    public boolean hasFilters() {
        return sessionId != null || stepNumbers != null || stepType != null || status != null || modelId != null
                || modelVersion != null || startTime != null || endTime != null || minConfidence != null
                || maxConfidence != null || minQualityScore != null || maxQualityScore != null || minTokens != null
                || maxTokens != null || minCost != null || maxCost != null || errorPattern != null
                || (metadataFilters != null && !metadataFilters.isEmpty()) || parentStepIds != null
                || childStepIds != null || previousStepId != null;
    }

    /**
     * Builder for ReasoningStepSearchCriteria.
     */
    public static final class Builder {
        private @Nullable String sessionId;
        private @Nullable List<Integer> stepNumbers;
        private @Nullable ReasoningStepType stepType;
        private @Nullable ReasoningStepStatus status;
        private @Nullable String modelId;
        private @Nullable String modelVersion;
        private @Nullable Instant startTime;
        private @Nullable Instant endTime;
        private @Nullable Double minConfidence;
        private @Nullable Double maxConfidence;
        private @Nullable Double minQualityScore;
        private @Nullable Double maxQualityScore;
        private @Nullable Long minTokens;
        private @Nullable Long maxTokens;
        private @Nullable Double minCost;
        private @Nullable Double maxCost;
        private @Nullable String errorPattern;
        private @Nullable Map<String, Object> metadataFilters;
        private @Nullable List<String> parentStepIds;
        private @Nullable List<String> childStepIds;
        private @Nullable String previousStepId;
        private int limit = 100;
        private boolean includeArchived = false;

        public Builder() {
        }

        public Builder(ReasoningStepSearchCriteria source) {
            this.sessionId = source.sessionId;
            this.stepNumbers = source.stepNumbers != null ? List.copyOf(source.stepNumbers) : null;
            this.stepType = source.stepType;
            this.status = source.status;
            this.modelId = source.modelId;
            this.modelVersion = source.modelVersion;
            this.startTime = source.startTime;
            this.endTime = source.endTime;
            this.minConfidence = source.minConfidence;
            this.maxConfidence = source.maxConfidence;
            this.minQualityScore = source.minQualityScore;
            this.maxQualityScore = source.maxQualityScore;
            this.minTokens = source.minTokens;
            this.maxTokens = source.maxTokens;
            this.minCost = source.minCost;
            this.maxCost = source.maxCost;
            this.errorPattern = source.errorPattern;
            this.metadataFilters = source.metadataFilters != null ? Map.copyOf(source.metadataFilters) : null;
            this.parentStepIds = source.parentStepIds != null ? List.copyOf(source.parentStepIds) : null;
            this.childStepIds = source.childStepIds != null ? List.copyOf(source.childStepIds) : null;
            this.previousStepId = source.previousStepId;
            this.limit = source.limit;
            this.includeArchived = source.includeArchived;
        }

        public Builder withSessionId(@Nullable String sessionId) {
            this.sessionId = sessionId;
            return this;
        }

        public Builder withStepNumbers(@Nullable List<Integer> stepNumbers) {
            this.stepNumbers = stepNumbers;
            return this;
        }

        public Builder withStepType(@Nullable ReasoningStepType stepType) {
            this.stepType = stepType;
            return this;
        }

        public Builder withStatus(@Nullable ReasoningStepStatus status) {
            this.status = status;
            return this;
        }

        public Builder withModelId(@Nullable String modelId) {
            this.modelId = modelId;
            return this;
        }

        public Builder withModelVersion(@Nullable String modelVersion) {
            this.modelVersion = modelVersion;
            return this;
        }

        public Builder withStartTime(@Nullable Instant startTime) {
            this.startTime = startTime;
            return this;
        }

        public Builder withEndTime(@Nullable Instant endTime) {
            this.endTime = endTime;
            return this;
        }

        public Builder withMinConfidence(@Nullable Double minConfidence) {
            this.minConfidence = minConfidence;
            return this;
        }

        public Builder withMaxConfidence(@Nullable Double maxConfidence) {
            this.maxConfidence = maxConfidence;
            return this;
        }

        public Builder withMinQualityScore(@Nullable Double minQualityScore) {
            this.minQualityScore = minQualityScore;
            return this;
        }

        public Builder withMaxQualityScore(@Nullable Double maxQualityScore) {
            this.maxQualityScore = maxQualityScore;
            return this;
        }

        public Builder withMinTokens(@Nullable Long minTokens) {
            this.minTokens = minTokens;
            return this;
        }

        public Builder withMaxTokens(@Nullable Long maxTokens) {
            this.maxTokens = maxTokens;
            return this;
        }

        public Builder withMinCost(@Nullable Double minCost) {
            this.minCost = minCost;
            return this;
        }

        public Builder withMaxCost(@Nullable Double maxCost) {
            this.maxCost = maxCost;
            return this;
        }

        public Builder withErrorPattern(@Nullable String errorPattern) {
            this.errorPattern = errorPattern;
            return this;
        }

        public Builder withMetadataFilters(@Nullable Map<String, Object> metadataFilters) {
            this.metadataFilters = metadataFilters;
            return this;
        }

        public Builder withParentStepIds(@Nullable List<String> parentStepIds) {
            this.parentStepIds = parentStepIds;
            return this;
        }

        public Builder withChildStepIds(@Nullable List<String> childStepIds) {
            this.childStepIds = childStepIds;
            return this;
        }

        public Builder withPreviousStepId(@Nullable String previousStepId) {
            this.previousStepId = previousStepId;
            return this;
        }

        public Builder withLimit(int limit) {
            this.limit = limit;
            return this;
        }

        public Builder withIncludeArchived(boolean includeArchived) {
            this.includeArchived = includeArchived;
            return this;
        }

        public ReasoningStepSearchCriteria build() {
            if (limit <= 0) {
                throw new IllegalArgumentException("limit must be positive");
            }
            if (minConfidence != null && (minConfidence < 0.0 || minConfidence > 1.0)) {
                throw new IllegalArgumentException("minConfidence must be between 0.0 and 1.0");
            }
            if (maxConfidence != null && (maxConfidence < 0.0 || maxConfidence > 1.0)) {
                throw new IllegalArgumentException("maxConfidence must be between 0.0 and 1.0");
            }
            if (minQualityScore != null && (minQualityScore < 0.0 || minQualityScore > 1.0)) {
                throw new IllegalArgumentException("minQualityScore must be between 0.0 and 1.0");
            }
            if (maxQualityScore != null && (maxQualityScore < 0.0 || maxQualityScore > 1.0)) {
                throw new IllegalArgumentException("maxQualityScore must be between 0.0 and 1.0");
            }
            if (startTime != null && endTime != null && startTime.isAfter(endTime)) {
                throw new IllegalArgumentException("startTime must be before endTime");
            }
            return new ReasoningStepSearchCriteria(this);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ReasoningStepSearchCriteria other = (ReasoningStepSearchCriteria) obj;
        return Objects.equals(sessionId, other.sessionId) && Objects.equals(stepNumbers, other.stepNumbers)
                && stepType == other.stepType && status == other.status && Objects.equals(modelId, other.modelId)
                && Objects.equals(modelVersion, other.modelVersion) && Objects.equals(startTime, other.startTime)
                && Objects.equals(endTime, other.endTime) && Objects.equals(minConfidence, other.minConfidence)
                && Objects.equals(maxConfidence, other.maxConfidence)
                && Objects.equals(minQualityScore, other.minQualityScore)
                && Objects.equals(maxQualityScore, other.maxQualityScore) && Objects.equals(minTokens, other.minTokens)
                && Objects.equals(maxTokens, other.maxTokens) && Objects.equals(minCost, other.minCost)
                && Objects.equals(maxCost, other.maxCost) && Objects.equals(errorPattern, other.errorPattern)
                && Objects.equals(metadataFilters, other.metadataFilters)
                && Objects.equals(parentStepIds, other.parentStepIds)
                && Objects.equals(childStepIds, other.childStepIds)
                && Objects.equals(previousStepId, other.previousStepId) && limit == other.limit
                && includeArchived == other.includeArchived;
    }

    @Override
    public int hashCode() {
        return Objects.hash(sessionId, stepNumbers, stepType, status, modelId, modelVersion, startTime, endTime,
                minConfidence, maxConfidence, minQualityScore, maxQualityScore, minTokens, maxTokens, minCost, maxCost,
                errorPattern, metadataFilters, parentStepIds, childStepIds, previousStepId, limit, includeArchived);
    }

    @Override
    public String toString() {
        return String.format(
                "ReasoningStepSearchCriteria{sessionId='%s', stepType=%s, status=%s, modelId='%s', limit=%d, includeArchived=%s}",
                sessionId, stepType, status, modelId, limit, includeArchived);
    }
}
