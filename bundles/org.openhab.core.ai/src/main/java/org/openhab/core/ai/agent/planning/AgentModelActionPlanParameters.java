package org.openhab.core.ai.agent.planning;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.config.common.BaseConfiguration;

/**
 * Parameters for action plan generation and execution.
 *
 * <p>
 * This class extends BaseConfiguration to provide configuration parameters that control how action plans
 * are generated, validated, optimized, and executed.
 * </p>
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public final class AgentModelActionPlanParameters extends BaseConfiguration {

    // Plan generation parameters
    private final int defaultActionDurationMs;
    private final int maxActionsPerPlan;
    private final double minConfidenceThreshold;
    private final double maxConfidenceThreshold;

    // Optimization parameters
    private final boolean enableParallelExecution;
    private final boolean enableDependencyOptimization;
    private final boolean enableResourceOptimization;
    private final int maxParallelActions;

    // Validation parameters
    private final boolean enableStrictValidation;
    private final boolean enableParameterValidation;
    private final boolean enableDependencyValidation;

    // Execution parameters
    private final int executionTimeoutMs;
    private final int retryAttempts;
    private final int retryDelayMs;

    /**
     * Private constructor for builder pattern.
     */
    private AgentModelActionPlanParameters(Builder builder) {
        super(builder.id, builder.enabled, builder.name, builder.version, builder.customOptions);
        this.defaultActionDurationMs = builder.defaultActionDurationMs;
        this.maxActionsPerPlan = builder.maxActionsPerPlan;
        this.minConfidenceThreshold = builder.minConfidenceThreshold;
        this.maxConfidenceThreshold = builder.maxConfidenceThreshold;
        this.enableParallelExecution = builder.enableParallelExecution;
        this.enableDependencyOptimization = builder.enableDependencyOptimization;
        this.enableResourceOptimization = builder.enableResourceOptimization;
        this.maxParallelActions = builder.maxParallelActions;
        this.enableStrictValidation = builder.enableStrictValidation;
        this.enableParameterValidation = builder.enableParameterValidation;
        this.enableDependencyValidation = builder.enableDependencyValidation;
        this.executionTimeoutMs = builder.executionTimeoutMs;
        this.retryAttempts = builder.retryAttempts;
        this.retryDelayMs = builder.retryDelayMs;
    }

    /**
     * Create a new builder for action plan parameters.
     *
     * @return a new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Create a new builder for action plan parameters with required fields.
     *
     * @param id the configuration identifier
     * @param name the configuration name
     * @return a new builder instance
     */
    public static Builder builder(String id, String name) {
        return new Builder(id, name);
    }

    /**
     * Create a copy of this configuration with a new builder.
     *
     * @return a new builder pre-populated with current values
     */
    public Builder toBuilder() {
        return new Builder(this);
    }

    /**
     * Convert parameters to a map for serialization or logging.
     *
     * @return a map representation of the parameters
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", getId());
        map.put("enabled", isEnabled());
        map.put("name", getName());
        map.put("version", getVersion());
        map.put("defaultActionDurationMs", defaultActionDurationMs);
        map.put("maxActionsPerPlan", maxActionsPerPlan);
        map.put("minConfidenceThreshold", minConfidenceThreshold);
        map.put("maxConfidenceThreshold", maxConfidenceThreshold);
        map.put("enableParallelExecution", enableParallelExecution);
        map.put("enableDependencyOptimization", enableDependencyOptimization);
        map.put("enableResourceOptimization", enableResourceOptimization);
        map.put("maxParallelActions", maxParallelActions);
        map.put("enableStrictValidation", enableStrictValidation);
        map.put("enableParameterValidation", enableParameterValidation);
        map.put("enableDependencyValidation", enableDependencyValidation);
        map.put("executionTimeoutMs", executionTimeoutMs);
        map.put("retryAttempts", retryAttempts);
        map.put("retryDelayMs", retryDelayMs);
        map.putAll(getCustomOptions());
        return map;
    }

    // Getters
    public int getDefaultActionDurationMs() {
        return defaultActionDurationMs;
    }

    public int getMaxActionsPerPlan() {
        return maxActionsPerPlan;
    }

    public double getMinConfidenceThreshold() {
        return minConfidenceThreshold;
    }

    public double getMaxConfidenceThreshold() {
        return maxConfidenceThreshold;
    }

    public boolean isEnableParallelExecution() {
        return enableParallelExecution;
    }

    public boolean isEnableDependencyOptimization() {
        return enableDependencyOptimization;
    }

    public boolean isEnableResourceOptimization() {
        return enableResourceOptimization;
    }

    public int getMaxParallelActions() {
        return maxParallelActions;
    }

    public boolean isEnableStrictValidation() {
        return enableStrictValidation;
    }

    public boolean isEnableParameterValidation() {
        return enableParameterValidation;
    }

    public boolean isEnableDependencyValidation() {
        return enableDependencyValidation;
    }

    public int getExecutionTimeoutMs() {
        return executionTimeoutMs;
    }

    public int getRetryAttempts() {
        return retryAttempts;
    }

    public int getRetryDelayMs() {
        return retryDelayMs;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj) || getClass() != obj.getClass()) {
            return false;
        }
        AgentModelActionPlanParameters other = (AgentModelActionPlanParameters) obj;
        return defaultActionDurationMs == other.defaultActionDurationMs && maxActionsPerPlan == other.maxActionsPerPlan
                && Double.compare(minConfidenceThreshold, other.minConfidenceThreshold) == 0
                && Double.compare(maxConfidenceThreshold, other.maxConfidenceThreshold) == 0
                && enableParallelExecution == other.enableParallelExecution
                && enableDependencyOptimization == other.enableDependencyOptimization
                && enableResourceOptimization == other.enableResourceOptimization
                && maxParallelActions == other.maxParallelActions
                && enableStrictValidation == other.enableStrictValidation
                && enableParameterValidation == other.enableParameterValidation
                && enableDependencyValidation == other.enableDependencyValidation
                && executionTimeoutMs == other.executionTimeoutMs && retryAttempts == other.retryAttempts
                && retryDelayMs == other.retryDelayMs;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), defaultActionDurationMs, maxActionsPerPlan, minConfidenceThreshold,
                maxConfidenceThreshold, enableParallelExecution, enableDependencyOptimization,
                enableResourceOptimization, maxParallelActions, enableStrictValidation, enableParameterValidation,
                enableDependencyValidation, executionTimeoutMs, retryAttempts, retryDelayMs);
    }

    @Override
    public String toString() {
        return String.format(
                "AgentModelActionPlanParameters{id='%s', name='%s', defaultActionDurationMs=%d, maxActionsPerPlan=%d, minConfidenceThreshold=%.2f, maxConfidenceThreshold=%.2f, enableParallelExecution=%s, enableDependencyOptimization=%s, enableResourceOptimization=%s, maxParallelActions=%d, enableStrictValidation=%s, enableParameterValidation=%s, enableDependencyValidation=%s, executionTimeoutMs=%d, retryAttempts=%d, retryDelayMs=%d}",
                getId(), getName(), defaultActionDurationMs, maxActionsPerPlan, minConfidenceThreshold,
                maxConfidenceThreshold, enableParallelExecution, enableDependencyOptimization,
                enableResourceOptimization, maxParallelActions, enableStrictValidation, enableParameterValidation,
                enableDependencyValidation, executionTimeoutMs, retryAttempts, retryDelayMs);
    }

    /**
     * Builder for AgentModelActionPlanParameters.
     */
    public static final class Builder {
        private String id = "default-action-plan-parameters";
        private boolean enabled = true;
        private String name = "Default Action Plan Parameters";
        private String version = "1.0.0";
        private Map<String, Object> customOptions = new HashMap<>();

        // Plan generation parameters
        private int defaultActionDurationMs = 5000; // 5 seconds
        private int maxActionsPerPlan = 20;
        private double minConfidenceThreshold = 0.7;
        private double maxConfidenceThreshold = 0.95;

        // Optimization parameters
        private boolean enableParallelExecution = true;
        private boolean enableDependencyOptimization = true;
        private boolean enableResourceOptimization = true;
        private int maxParallelActions = 5;

        // Validation parameters
        private boolean enableStrictValidation = true;
        private boolean enableParameterValidation = true;
        private boolean enableDependencyValidation = true;

        // Execution parameters
        private int executionTimeoutMs = 300000; // 5 minutes
        private int retryAttempts = 3;
        private int retryDelayMs = 1000; // 1 second

        /**
         * Default constructor.
         */
        public Builder() {
        }

        /**
         * Constructor with required fields.
         *
         * @param id the configuration identifier
         * @param name the configuration name
         */
        public Builder(String id, String name) {
            this.id = Objects.requireNonNull(id, "Configuration ID cannot be null");
            this.name = Objects.requireNonNull(name, "Configuration name cannot be null");
        }

        /**
         * Copy constructor.
         *
         * @param source the source configuration to copy from
         */
        public Builder(AgentModelActionPlanParameters source) {
            this.id = source.getId();
            this.enabled = source.isEnabled();
            this.name = source.getName();
            this.version = source.getVersion();
            this.customOptions = new HashMap<>(source.getCustomOptions());
            this.defaultActionDurationMs = source.defaultActionDurationMs;
            this.maxActionsPerPlan = source.maxActionsPerPlan;
            this.minConfidenceThreshold = source.minConfidenceThreshold;
            this.maxConfidenceThreshold = source.maxConfidenceThreshold;
            this.enableParallelExecution = source.enableParallelExecution;
            this.enableDependencyOptimization = source.enableDependencyOptimization;
            this.enableResourceOptimization = source.enableResourceOptimization;
            this.maxParallelActions = source.maxParallelActions;
            this.enableStrictValidation = source.enableStrictValidation;
            this.enableParameterValidation = source.enableParameterValidation;
            this.enableDependencyValidation = source.enableDependencyValidation;
            this.executionTimeoutMs = source.executionTimeoutMs;
            this.retryAttempts = source.retryAttempts;
            this.retryDelayMs = source.retryDelayMs;
        }

        // Configuration base methods
        public Builder withId(String id) {
            this.id = Objects.requireNonNull(id, "Configuration ID cannot be null");
            return this;
        }

        public Builder withEnabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Builder withName(String name) {
            this.name = Objects.requireNonNull(name, "Configuration name cannot be null");
            return this;
        }

        public Builder withVersion(String version) {
            this.version = Objects.requireNonNull(version, "Configuration version cannot be null");
            return this;
        }

        public Builder withCustomOptions(Map<String, Object> customOptions) {
            this.customOptions = new HashMap<>(Objects.requireNonNull(customOptions, "Custom options cannot be null"));
            return this;
        }

        public Builder withCustomOption(String key, Object value) {
            this.customOptions.put(Objects.requireNonNull(key, "Option key cannot be null"), value);
            return this;
        }

        // Plan generation parameters
        public Builder withDefaultActionDurationMs(int defaultActionDurationMs) {
            this.defaultActionDurationMs = defaultActionDurationMs;
            return this;
        }

        public Builder withMaxActionsPerPlan(int maxActionsPerPlan) {
            this.maxActionsPerPlan = maxActionsPerPlan;
            return this;
        }

        public Builder withMinConfidenceThreshold(double minConfidenceThreshold) {
            this.minConfidenceThreshold = minConfidenceThreshold;
            return this;
        }

        public Builder withMaxConfidenceThreshold(double maxConfidenceThreshold) {
            this.maxConfidenceThreshold = maxConfidenceThreshold;
            return this;
        }

        // Optimization parameters
        public Builder withEnableParallelExecution(boolean enableParallelExecution) {
            this.enableParallelExecution = enableParallelExecution;
            return this;
        }

        public Builder withEnableDependencyOptimization(boolean enableDependencyOptimization) {
            this.enableDependencyOptimization = enableDependencyOptimization;
            return this;
        }

        public Builder withEnableResourceOptimization(boolean enableResourceOptimization) {
            this.enableResourceOptimization = enableResourceOptimization;
            return this;
        }

        public Builder withMaxParallelActions(int maxParallelActions) {
            this.maxParallelActions = maxParallelActions;
            return this;
        }

        // Validation parameters
        public Builder withEnableStrictValidation(boolean enableStrictValidation) {
            this.enableStrictValidation = enableStrictValidation;
            return this;
        }

        public Builder withEnableParameterValidation(boolean enableParameterValidation) {
            this.enableParameterValidation = enableParameterValidation;
            return this;
        }

        public Builder withEnableDependencyValidation(boolean enableDependencyValidation) {
            this.enableDependencyValidation = enableDependencyValidation;
            return this;
        }

        // Execution parameters
        public Builder withExecutionTimeoutMs(int executionTimeoutMs) {
            this.executionTimeoutMs = executionTimeoutMs;
            return this;
        }

        public Builder withRetryAttempts(int retryAttempts) {
            this.retryAttempts = retryAttempts;
            return this;
        }

        public Builder withRetryDelayMs(int retryDelayMs) {
            this.retryDelayMs = retryDelayMs;
            return this;
        }

        /**
         * Build the AgentModelActionPlanParameters instance.
         *
         * @return the built instance
         * @throws IllegalArgumentException if validation fails
         */
        public AgentModelActionPlanParameters build() {
            if (defaultActionDurationMs <= 0) {
                throw new IllegalArgumentException("Default action duration must be positive");
            }
            if (maxActionsPerPlan <= 0) {
                throw new IllegalArgumentException("Max actions per plan must be positive");
            }
            if (minConfidenceThreshold < 0.0 || minConfidenceThreshold > 1.0) {
                throw new IllegalArgumentException("Min confidence threshold must be between 0.0 and 1.0");
            }
            if (maxConfidenceThreshold < 0.0 || maxConfidenceThreshold > 1.0) {
                throw new IllegalArgumentException("Max confidence threshold must be between 0.0 and 1.0");
            }
            if (minConfidenceThreshold > maxConfidenceThreshold) {
                throw new IllegalArgumentException(
                        "Min confidence threshold cannot be greater than max confidence threshold");
            }
            if (maxParallelActions <= 0) {
                throw new IllegalArgumentException("Max parallel actions must be positive");
            }
            if (executionTimeoutMs <= 0) {
                throw new IllegalArgumentException("Execution timeout must be positive");
            }
            if (retryAttempts < 0) {
                throw new IllegalArgumentException("Retry attempts cannot be negative");
            }
            if (retryDelayMs < 0) {
                throw new IllegalArgumentException("Retry delay cannot be negative");
            }

            return new AgentModelActionPlanParameters(this);
        }
    }
}
