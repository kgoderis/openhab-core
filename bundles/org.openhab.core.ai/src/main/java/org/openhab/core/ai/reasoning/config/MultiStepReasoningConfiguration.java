package org.openhab.core.ai.reasoning.config;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.config.common.BaseConfiguration;

/**
 * Unified Multi-Step Reasoning Configuration for building reasoning-specific configurations.
 * 
 * <p>
 * This class provides a comprehensive configuration for multi-step reasoning operations including:
 * - Step limits and timeouts
 * - Token and temperature settings
 * - Confidence thresholds and retry logic
 * - Guidance prompt configurations
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public final class MultiStepReasoningConfiguration extends BaseConfiguration {

    private final int maxSteps;
    private final long sessionTimeoutMs;
    private final long stepTimeoutMs;
    private final int maxTokensPerStep;
    private final double temperature;
    private final double confidenceThreshold;
    private final boolean guidancePromptsEnabled;
    private final int maxRetries;
    private final long retryDelayMs;

    private MultiStepReasoningConfiguration(Builder builder) {
        super(builder.id, builder.enabled, builder.name, builder.version, builder.customOptions);
        this.maxSteps = builder.maxSteps;
        this.sessionTimeoutMs = builder.sessionTimeoutMs;
        this.stepTimeoutMs = builder.stepTimeoutMs;
        this.maxTokensPerStep = builder.maxTokensPerStep;
        this.temperature = builder.temperature;
        this.confidenceThreshold = builder.confidenceThreshold;
        this.guidancePromptsEnabled = builder.guidancePromptsEnabled;
        this.maxRetries = builder.maxRetries;
        this.retryDelayMs = builder.retryDelayMs;
    }

    /**
     * Default constructor with sensible defaults.
     */
    public MultiStepReasoningConfiguration() {
        super("multi-step-reasoning", true, "Multi-Step Reasoning", "1.0.0", null);
        this.maxSteps = 10;
        this.sessionTimeoutMs = 300000; // 5 minutes
        this.stepTimeoutMs = 30000; // 30 seconds
        this.maxTokensPerStep = 1000;
        this.temperature = 0.7;
        this.confidenceThreshold = 0.8;
        this.guidancePromptsEnabled = true;
        this.maxRetries = 3;
        this.retryDelayMs = 1000; // 1 second
    }

    public int getMaxSteps() {
        return maxSteps;
    }

    public long getSessionTimeoutMs() {
        return sessionTimeoutMs;
    }

    public long getStepTimeoutMs() {
        return stepTimeoutMs;
    }

    public int getMaxTokensPerStep() {
        return maxTokensPerStep;
    }

    public double getTemperature() {
        return temperature;
    }

    public double getConfidenceThreshold() {
        return confidenceThreshold;
    }

    public boolean isGuidancePromptsEnabled() {
        return guidancePromptsEnabled;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public long getRetryDelayMs() {
        return retryDelayMs;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj) || getClass() != obj.getClass()) {
            return false;
        }
        MultiStepReasoningConfiguration other = (MultiStepReasoningConfiguration) obj;
        return maxSteps == other.maxSteps && sessionTimeoutMs == other.sessionTimeoutMs
                && stepTimeoutMs == other.stepTimeoutMs && maxTokensPerStep == other.maxTokensPerStep
                && Double.compare(temperature, other.temperature) == 0
                && Double.compare(confidenceThreshold, other.confidenceThreshold) == 0
                && guidancePromptsEnabled == other.guidancePromptsEnabled && maxRetries == other.maxRetries
                && retryDelayMs == other.retryDelayMs;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), maxSteps, sessionTimeoutMs, stepTimeoutMs, maxTokensPerStep, temperature,
                confidenceThreshold, guidancePromptsEnabled, maxRetries, retryDelayMs);
    }

    @Override
    public String toString() {
        return String.format(
                "MultiStepReasoningConfiguration{id='%s', maxSteps=%d, sessionTimeoutMs=%d, stepTimeoutMs=%d, maxTokensPerStep=%d, temperature=%.2f, confidenceThreshold=%.2f, enabled=%s}",
                getId(), maxSteps, sessionTimeoutMs, stepTimeoutMs, maxTokensPerStep, temperature, confidenceThreshold,
                isEnabled());
    }

    public static final class Builder {
        private String id = "";
        private boolean enabled = true;
        private String name = "Multi-Step Reasoning Configuration";
        private String version = "1.0.0";
        private final java.util.Map<String, Object> customOptions = new java.util.HashMap<>();
        private int maxSteps = 5;
        private long sessionTimeoutMs = 60000;
        private long stepTimeoutMs = 10000;
        private int maxTokensPerStep = 1000;
        private double temperature = 0.7;
        private double confidenceThreshold = 0.8;
        private boolean guidancePromptsEnabled = true;
        private int maxRetries = 3;
        private long retryDelayMs = 1000;

        public Builder() {
        }

        public Builder(MultiStepReasoningConfiguration source) {
            this.id = source.getId();
            this.enabled = source.isEnabled();
            this.name = source.getName();
            this.version = source.getVersion();
            this.maxSteps = source.maxSteps;
            this.sessionTimeoutMs = source.sessionTimeoutMs;
            this.stepTimeoutMs = source.stepTimeoutMs;
            this.maxTokensPerStep = source.maxTokensPerStep;
            this.temperature = source.temperature;
            this.confidenceThreshold = source.confidenceThreshold;
            this.guidancePromptsEnabled = source.guidancePromptsEnabled;
            this.maxRetries = source.maxRetries;
            this.retryDelayMs = source.retryDelayMs;
        }

        public Builder withId(String id) {
            this.id = Objects.requireNonNull(id, "id");
            return this;
        }

        public Builder withEnabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Builder withName(String name) {
            this.name = Objects.requireNonNull(name, "name");
            return this;
        }

        public Builder withVersion(String version) {
            this.version = Objects.requireNonNull(version, "version");
            return this;
        }

        public Builder withMaxSteps(int maxSteps) {
            this.maxSteps = maxSteps;
            return this;
        }

        public Builder withSessionTimeoutMs(long sessionTimeoutMs) {
            this.sessionTimeoutMs = sessionTimeoutMs;
            return this;
        }

        public Builder withStepTimeoutMs(long stepTimeoutMs) {
            this.stepTimeoutMs = stepTimeoutMs;
            return this;
        }

        public Builder withMaxTokensPerStep(int maxTokensPerStep) {
            this.maxTokensPerStep = maxTokensPerStep;
            return this;
        }

        public Builder withTemperature(double temperature) {
            this.temperature = temperature;
            return this;
        }

        public Builder withConfidenceThreshold(double confidenceThreshold) {
            this.confidenceThreshold = confidenceThreshold;
            return this;
        }

        public Builder withGuidancePromptsEnabled(boolean guidancePromptsEnabled) {
            this.guidancePromptsEnabled = guidancePromptsEnabled;
            return this;
        }

        public Builder withMaxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
            return this;
        }

        public Builder withRetryDelayMs(long retryDelayMs) {
            this.retryDelayMs = retryDelayMs;
            return this;
        }

        public MultiStepReasoningConfiguration build() {
            validate();
            return new MultiStepReasoningConfiguration(this);
        }

        private void validate() {
            if (id.isBlank()) {
                throw new IllegalArgumentException("id must not be blank");
            }
            if (name.isBlank()) {
                throw new IllegalArgumentException("name must not be blank");
            }
            if (version.isBlank()) {
                throw new IllegalArgumentException("version must not be blank");
            }
            if (maxSteps <= 0) {
                throw new IllegalArgumentException("maxSteps must be positive");
            }
            if (sessionTimeoutMs <= 0) {
                throw new IllegalArgumentException("sessionTimeoutMs must be positive");
            }
            if (stepTimeoutMs <= 0) {
                throw new IllegalArgumentException("stepTimeoutMs must be positive");
            }
            if (maxTokensPerStep <= 0) {
                throw new IllegalArgumentException("maxTokensPerStep must be positive");
            }
            if (temperature < 0.0 || temperature > 2.0) {
                throw new IllegalArgumentException("temperature must be between 0.0 and 2.0");
            }
            if (confidenceThreshold < 0.0 || confidenceThreshold > 1.0) {
                throw new IllegalArgumentException("confidenceThreshold must be between 0.0 and 1.0");
            }
            if (maxRetries < 0) {
                throw new IllegalArgumentException("maxRetries must be non-negative");
            }
            if (retryDelayMs < 0) {
                throw new IllegalArgumentException("retryDelayMs must be non-negative");
            }
        }
    }
}
