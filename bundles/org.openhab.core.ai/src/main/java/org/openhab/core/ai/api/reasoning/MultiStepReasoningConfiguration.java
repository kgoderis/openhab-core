package org.openhab.core.ai.api.reasoning;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Configuration for Multi-Step Reasoning Engine
 * 
 * @author Karel Goderis - Initial Contribution
 */
@NonNullByDefault
public class MultiStepReasoningConfiguration {

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

    public MultiStepReasoningConfiguration() {
        // Default configuration
        this.maxSteps = 5;
        this.sessionTimeoutMs = 60000; // 60 seconds
        this.stepTimeoutMs = 10000; // 10 seconds
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

    public static class Builder {
        private int maxSteps = 5;
        private long sessionTimeoutMs = 60000;
        private long stepTimeoutMs = 10000;
        private int maxTokensPerStep = 1000;
        private double temperature = 0.7;
        private double confidenceThreshold = 0.8;
        private boolean guidancePromptsEnabled = true;
        private int maxRetries = 3;
        private long retryDelayMs = 1000;

        public Builder maxSteps(int maxSteps) {
            this.maxSteps = maxSteps;
            return this;
        }

        public Builder sessionTimeoutMs(long sessionTimeoutMs) {
            this.sessionTimeoutMs = sessionTimeoutMs;
            return this;
        }

        public Builder stepTimeoutMs(long stepTimeoutMs) {
            this.stepTimeoutMs = stepTimeoutMs;
            return this;
        }

        public Builder maxTokensPerStep(int maxTokensPerStep) {
            this.maxTokensPerStep = maxTokensPerStep;
            return this;
        }

        public Builder temperature(double temperature) {
            this.temperature = temperature;
            return this;
        }

        public Builder confidenceThreshold(double confidenceThreshold) {
            this.confidenceThreshold = confidenceThreshold;
            return this;
        }

        public Builder guidancePromptsEnabled(boolean guidancePromptsEnabled) {
            this.guidancePromptsEnabled = guidancePromptsEnabled;
            return this;
        }

        public Builder maxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
            return this;
        }

        public Builder retryDelayMs(long retryDelayMs) {
            this.retryDelayMs = retryDelayMs;
            return this;
        }

        public MultiStepReasoningConfiguration build() {
            return new MultiStepReasoningConfiguration(this);
        }
    }
}
