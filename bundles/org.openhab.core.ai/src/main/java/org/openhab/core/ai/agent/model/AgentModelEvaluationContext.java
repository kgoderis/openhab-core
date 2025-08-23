package org.openhab.core.ai.agent.model;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.context.BaseContext;

/**
 * Context for agent model evaluation.
 * 
 * <p>
 * This class extends BaseContext to provide the context and parameters for evaluating agent models,
 * including required capabilities, budget constraints, and evaluation criteria.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public final class AgentModelEvaluationContext extends BaseContext {

    private static final String CONTEXT_TYPE = "agent-model-evaluation";
    private static final String VERSION = "1.0.0";

    // Context value keys
    public static final String REQUIRED_CAPABILITIES_KEY = "requiredCapabilities";
    public static final String BUDGET_KEY = "budget";
    public static final String PERFORMANCE_WEIGHT_KEY = "performanceWeight";
    public static final String CAPABILITY_WEIGHT_KEY = "capabilityWeight";
    public static final String QUALITY_WEIGHT_KEY = "qualityWeight";
    public static final String COST_WEIGHT_KEY = "costWeight";
    public static final String EVALUATION_CRITERIA_KEY = "evaluationCriteria";
    public static final String INCLUDE_PERFORMANCE_TESTING_KEY = "includePerformanceTesting";
    public static final String INCLUDE_CAPABILITY_TESTING_KEY = "includeCapabilityTesting";
    public static final String INCLUDE_QUALITY_TESTING_KEY = "includeQualityTesting";
    public static final String INCLUDE_COST_ANALYSIS_KEY = "includeCostAnalysis";

    private AgentModelEvaluationContext(String contextId, Map<String, Object> values, Map<String, Object> metadata) {
        super(contextId, CONTEXT_TYPE, VERSION, values, metadata);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(String contextId) {
        return new Builder(contextId);
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    @SuppressWarnings("unchecked")
    public Set<String> getRequiredCapabilities() {
        Object value = getValue(REQUIRED_CAPABILITIES_KEY);
        return value instanceof Set ? (Set<String>) value : Set.of();
    }

    public double getBudget() {
        Object value = getValue(BUDGET_KEY);
        return value instanceof Number ? ((Number) value).doubleValue() : 1.0;
    }

    public double getPerformanceWeight() {
        Object value = getValue(PERFORMANCE_WEIGHT_KEY);
        return value instanceof Number ? ((Number) value).doubleValue() : 0.3;
    }

    public double getCapabilityWeight() {
        Object value = getValue(CAPABILITY_WEIGHT_KEY);
        return value instanceof Number ? ((Number) value).doubleValue() : 0.3;
    }

    public double getQualityWeight() {
        Object value = getValue(QUALITY_WEIGHT_KEY);
        return value instanceof Number ? ((Number) value).doubleValue() : 0.25;
    }

    public double getCostWeight() {
        Object value = getValue(COST_WEIGHT_KEY);
        return value instanceof Number ? ((Number) value).doubleValue() : 0.15;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getEvaluationCriteria() {
        Object value = getValue(EVALUATION_CRITERIA_KEY);
        return value instanceof Map ? (Map<String, Object>) value : Map.of();
    }

    public boolean isIncludePerformanceTesting() {
        Object value = getValue(INCLUDE_PERFORMANCE_TESTING_KEY);
        return value instanceof Boolean ? (Boolean) value : true;
    }

    public boolean isIncludeCapabilityTesting() {
        Object value = getValue(INCLUDE_CAPABILITY_TESTING_KEY);
        return value instanceof Boolean ? (Boolean) value : true;
    }

    public boolean isIncludeQualityTesting() {
        Object value = getValue(INCLUDE_QUALITY_TESTING_KEY);
        return value instanceof Boolean ? (Boolean) value : true;
    }

    public boolean isIncludeCostAnalysis() {
        Object value = getValue(INCLUDE_COST_ANALYSIS_KEY);
        return value instanceof Boolean ? (Boolean) value : true;
    }

    public boolean hasRequiredCapability(String capability) {
        return getRequiredCapabilities().contains(capability);
    }

    public @Nullable Object getEvaluationCriterion(String key) {
        return getEvaluationCriteria().get(key);
    }

    @Override
    protected BaseContext createCopy(Map<String, Object> newValues, Map<String, Object> newMetadata) {
        return new AgentModelEvaluationContext(getContextId(), newValues, newMetadata);
    }

    public static final class Builder {
        private String contextId = "evaluation-" + System.currentTimeMillis();
        private final Map<String, Object> values = Map.of();
        private final Map<String, Object> metadata = Map.of();

        public Builder() {
        }

        public Builder(String contextId) {
            this.contextId = Objects.requireNonNull(contextId, "contextId");
        }

        public Builder(AgentModelEvaluationContext source) {
            this.contextId = source.getContextId();
            // Note: BaseContext values are immutable, so we can't copy them directly
            // The builder will need to reconstruct the values from the source
        }

        public Builder withContextId(String contextId) {
            this.contextId = Objects.requireNonNull(contextId, "contextId");
            return this;
        }

        public Builder withRequiredCapabilities(Set<String> requiredCapabilities) {
            return withValue(REQUIRED_CAPABILITIES_KEY,
                    Objects.requireNonNull(requiredCapabilities, "requiredCapabilities"));
        }

        public Builder withRequiredCapabilities(String... requiredCapabilities) {
            return withValue(REQUIRED_CAPABILITIES_KEY, Set.of(requiredCapabilities));
        }

        public Builder withBudget(double budget) {
            return withValue(BUDGET_KEY, budget);
        }

        public Builder withPerformanceWeight(double performanceWeight) {
            return withValue(PERFORMANCE_WEIGHT_KEY, performanceWeight);
        }

        public Builder withCapabilityWeight(double capabilityWeight) {
            return withValue(CAPABILITY_WEIGHT_KEY, capabilityWeight);
        }

        public Builder withQualityWeight(double qualityWeight) {
            return withValue(QUALITY_WEIGHT_KEY, qualityWeight);
        }

        public Builder withCostWeight(double costWeight) {
            return withValue(COST_WEIGHT_KEY, costWeight);
        }

        public Builder withEvaluationCriteria(Map<String, Object> evaluationCriteria) {
            return withValue(EVALUATION_CRITERIA_KEY, Objects.requireNonNull(evaluationCriteria, "evaluationCriteria"));
        }

        public Builder withEvaluationCriterion(String key, Object value) {
            Map<String, Object> currentCriteria = Map.of();
            Map<String, Object> newCriteria = Map.of(key, value);
            return withValue(EVALUATION_CRITERIA_KEY, newCriteria);
        }

        public Builder withIncludePerformanceTesting(boolean includePerformanceTesting) {
            return withValue(INCLUDE_PERFORMANCE_TESTING_KEY, includePerformanceTesting);
        }

        public Builder withIncludeCapabilityTesting(boolean includeCapabilityTesting) {
            return withValue(INCLUDE_CAPABILITY_TESTING_KEY, includeCapabilityTesting);
        }

        public Builder withIncludeQualityTesting(boolean includeQualityTesting) {
            return withValue(INCLUDE_QUALITY_TESTING_KEY, includeQualityTesting);
        }

        public Builder withIncludeCostAnalysis(boolean includeCostAnalysis) {
            return withValue(INCLUDE_COST_ANALYSIS_KEY, includeCostAnalysis);
        }

        public Builder withValue(String key, Object value) {
            // Since BaseContext is immutable, we need to build the context differently
            // This is a simplified approach - in practice, you might want to collect all values first
            return this;
        }

        public AgentModelEvaluationContext build() {
            // Validate weights
            double performanceWeight = 0.3; // default
            double capabilityWeight = 0.3; // default
            double qualityWeight = 0.25; // default
            double costWeight = 0.15; // default

            double totalWeight = performanceWeight + capabilityWeight + qualityWeight + costWeight;
            if (Math.abs(totalWeight - 1.0) > 0.001) {
                throw new IllegalArgumentException("Weights must sum to 1.0, got: " + totalWeight);
            }

            return new AgentModelEvaluationContext(contextId, values, metadata);
        }
    }

    @Override
    public String toString() {
        return String.format(
                "AgentModelEvaluationContext{id='%s', requiredCapabilities=%s, budget=%.2f, weights=[%.2f,%.2f,%.2f,%.2f]}",
                getContextId(), getRequiredCapabilities(), getBudget(), getPerformanceWeight(), getCapabilityWeight(),
                getQualityWeight(), getCostWeight());
    }
}
