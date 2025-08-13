package org.openhab.core.ai.reasoning.api;

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

    MultiStepReasoningConfiguration(MultiStepReasoningConfigurationBuilder builder) {
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

    public static MultiStepReasoningConfigurationBuilder builder() {
        return new MultiStepReasoningConfigurationBuilder();
    }

    /* Extracted: org.openhab.core.ai.reasoning.api.MultiStepReasoningConfigurationBuilder */
}
