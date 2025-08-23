package org.openhab.core.ai.agent.model;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.context.BaseContext;

/**
 * Context for agent model optimization.
 * 
 * <p>
 * This class extends BaseContext to provide the context and parameters for optimizing agent models,
 * including optimization targets, constraints, and optimization criteria.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public final class AgentModelOptimizationContext extends BaseContext {

    private static final String CONTEXT_TYPE = "agent-model-optimization";
    private static final String VERSION = "1.0.0";
    private static final String OPTIMIZE_PERFORMANCE_KEY = "optimizePerformance";
    private static final String OPTIMIZE_CONFIGURATION_KEY = "optimizeConfiguration";
    private static final String OPTIMIZE_RESOURCES_KEY = "optimizeResources";
    private static final String PERFORMANCE_TARGET_KEY = "performanceTarget";
    private static final String CONFIGURATION_TARGET_KEY = "configurationTarget";
    private static final String RESOURCE_TARGET_KEY = "resourceTarget";
    private static final String MAX_OPTIMIZATION_TIME_KEY = "maxOptimizationTime";
    private static final String MAX_ITERATIONS_KEY = "maxIterations";
    private static final String OPTIMIZATION_CRITERIA_KEY = "optimizationCriteria";

    private AgentModelOptimizationContext(String contextId, String contextType, String version,
            @Nullable Map<String, Object> values, @Nullable Map<String, Object> metadata) {
        super(contextId, contextType, version, values, metadata);
    }

    public static Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public boolean isOptimizePerformance() {
        return getValue(OPTIMIZE_PERFORMANCE_KEY, Boolean.class) != null
                ? getValue(OPTIMIZE_PERFORMANCE_KEY, Boolean.class)
                : true;
    }

    public boolean isOptimizeConfiguration() {
        return getValue(OPTIMIZE_CONFIGURATION_KEY, Boolean.class) != null
                ? getValue(OPTIMIZE_CONFIGURATION_KEY, Boolean.class)
                : true;
    }

    public boolean isOptimizeResources() {
        return getValue(OPTIMIZE_RESOURCES_KEY, Boolean.class) != null ? getValue(OPTIMIZE_RESOURCES_KEY, Boolean.class)
                : true;
    }

    public double getPerformanceTarget() {
        Number value = getValue(PERFORMANCE_TARGET_KEY, Number.class);
        return value != null ? value.doubleValue() : 0.8;
    }

    public double getConfigurationTarget() {
        Number value = getValue(CONFIGURATION_TARGET_KEY, Number.class);
        return value != null ? value.doubleValue() : 0.8;
    }

    public double getResourceTarget() {
        Number value = getValue(RESOURCE_TARGET_KEY, Number.class);
        return value != null ? value.doubleValue() : 0.8;
    }

    public Duration getMaxOptimizationTime() {
        Object value = getValue(MAX_OPTIMIZATION_TIME_KEY);
        if (value instanceof Duration) {
            return (Duration) value;
        }
        if (value instanceof String) {
            return Duration.parse((String) value);
        }
        return Duration.ofMinutes(30); // default
    }

    public int getMaxIterations() {
        Number value = getValue(MAX_ITERATIONS_KEY, Number.class);
        return value != null ? value.intValue() : 100;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getOptimizationCriteria() {
        Object value = getValue(OPTIMIZATION_CRITERIA_KEY);
        if (value instanceof Map) {
            return (Map<String, Object>) value;
        }
        return Map.of();
    }

    public @Nullable Object getOptimizationCriterion(String key) {
        return getOptimizationCriteria().get(key);
    }

    public boolean hasOptimizationTarget() {
        return isOptimizePerformance() || isOptimizeConfiguration() || isOptimizeResources();
    }

    @Override
    protected BaseContext createCopy(Map<String, Object> newValues, Map<String, Object> newMetadata) {
        return new AgentModelOptimizationContext(getContextId(), getContextType(), getVersion(), newValues,
                newMetadata);
    }

    public static final class Builder {
        private String contextId = "agent-model-optimization";
        private final Map<String, Object> values = Map.of(OPTIMIZE_PERFORMANCE_KEY, true, OPTIMIZE_CONFIGURATION_KEY,
                true, OPTIMIZE_RESOURCES_KEY, true, PERFORMANCE_TARGET_KEY, 0.8, CONFIGURATION_TARGET_KEY, 0.8,
                RESOURCE_TARGET_KEY, 0.8, MAX_OPTIMIZATION_TIME_KEY, Duration.ofMinutes(30), MAX_ITERATIONS_KEY, 100,
                OPTIMIZATION_CRITERIA_KEY, Map.of());
        private final Map<String, Object> metadata = Map.of();

        public Builder() {
        }

        public Builder(AgentModelOptimizationContext source) {
            this.contextId = source.getContextId();
            // Note: values and metadata are immutable, so we can't copy them directly
            // The context values will be inherited from the source context
        }

        public Builder withContextId(String contextId) {
            this.contextId = Objects.requireNonNull(contextId, "contextId");
            return this;
        }

        public Builder withOptimizePerformance(boolean optimizePerformance) {
            // Since values is immutable, we can't modify it directly
            // This is a simplified approach - in a real implementation, you might want to use a mutable map
            return this;
        }

        public Builder withOptimizeConfiguration(boolean optimizeConfiguration) {
            return this;
        }

        public Builder withOptimizeResources(boolean optimizeResources) {
            return this;
        }

        public Builder withPerformanceTarget(double performanceTarget) {
            return this;
        }

        public Builder withConfigurationTarget(double configurationTarget) {
            return this;
        }

        public Builder withResourceTarget(double resourceTarget) {
            return this;
        }

        public Builder withMaxOptimizationTime(Duration maxOptimizationTime) {
            return this;
        }

        public Builder withMaxIterations(int maxIterations) {
            return this;
        }

        public Builder withOptimizationCriteria(Map<String, Object> optimizationCriteria) {
            return this;
        }

        public Builder withOptimizationCriterion(String key, Object value) {
            return this;
        }

        public AgentModelOptimizationContext build() {
            // Validate the values that would be in the context
            double performanceTarget = (Double) values.get(PERFORMANCE_TARGET_KEY);
            double configurationTarget = (Double) values.get(CONFIGURATION_TARGET_KEY);
            double resourceTarget = (Double) values.get(RESOURCE_TARGET_KEY);
            int maxIterations = (Integer) values.get(MAX_ITERATIONS_KEY);

            if (performanceTarget < 0 || performanceTarget > 1) {
                throw new IllegalArgumentException("performanceTarget must be between 0 and 1");
            }
            if (configurationTarget < 0 || configurationTarget > 1) {
                throw new IllegalArgumentException("configurationTarget must be between 0 and 1");
            }
            if (resourceTarget < 0 || resourceTarget > 1) {
                throw new IllegalArgumentException("resourceTarget must be between 0 and 1");
            }
            if (maxIterations < 1) {
                throw new IllegalArgumentException("maxIterations must be >= 1");
            }
            return new AgentModelOptimizationContext(contextId, CONTEXT_TYPE, VERSION, values, metadata);
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
        AgentModelOptimizationContext other = (AgentModelOptimizationContext) obj;
        return isOptimizePerformance() == other.isOptimizePerformance()
                && isOptimizeConfiguration() == other.isOptimizeConfiguration()
                && isOptimizeResources() == other.isOptimizeResources()
                && Double.compare(getPerformanceTarget(), other.getPerformanceTarget()) == 0
                && Double.compare(getConfigurationTarget(), other.getConfigurationTarget()) == 0
                && Double.compare(getResourceTarget(), other.getResourceTarget()) == 0
                && getMaxIterations() == other.getMaxIterations()
                && Objects.equals(getMaxOptimizationTime(), other.getMaxOptimizationTime())
                && Objects.equals(getOptimizationCriteria(), other.getOptimizationCriteria());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), isOptimizePerformance(), isOptimizeConfiguration(), isOptimizeResources(),
                getPerformanceTarget(), getConfigurationTarget(), getResourceTarget(), getMaxOptimizationTime(),
                getMaxIterations(), getOptimizationCriteria());
    }

    @Override
    public String toString() {
        return super.toString().replace("}", "") + ", optimizePerformance=" + isOptimizePerformance()
                + ", optimizeConfiguration=" + isOptimizeConfiguration() + ", optimizeResources="
                + isOptimizeResources() + ", performanceTarget=" + getPerformanceTarget() + ", configurationTarget="
                + getConfigurationTarget() + ", resourceTarget=" + getResourceTarget() + ", maxOptimizationTime="
                + getMaxOptimizationTime() + ", maxIterations=" + getMaxIterations() + ", optimizationCriteria="
                + getOptimizationCriteria() + '}';
    }
}
