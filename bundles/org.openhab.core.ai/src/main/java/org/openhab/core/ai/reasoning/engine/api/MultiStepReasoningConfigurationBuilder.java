package org.openhab.core.ai.reasoning.engine.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Builder for MultiStepReasoningConfiguration.
 *
 * @author Karel Goderis - Initial Contribution
 */
@NonNullByDefault
public class MultiStepReasoningConfigurationBuilder {
    int maxSteps = 5;
    long sessionTimeoutMs = 60000;
    long stepTimeoutMs = 10000;
    int maxTokensPerStep = 1000;
    double temperature = 0.7;
    double confidenceThreshold = 0.8;
    boolean guidancePromptsEnabled = true;
    int maxRetries = 3;
    long retryDelayMs = 1000;

    public MultiStepReasoningConfigurationBuilder maxSteps(int maxSteps) {
        this.maxSteps = maxSteps;
        return this;
    }

    public MultiStepReasoningConfigurationBuilder sessionTimeoutMs(long sessionTimeoutMs) {
        this.sessionTimeoutMs = sessionTimeoutMs;
        return this;
    }

    public MultiStepReasoningConfigurationBuilder stepTimeoutMs(long stepTimeoutMs) {
        this.stepTimeoutMs = stepTimeoutMs;
        return this;
    }

    public MultiStepReasoningConfigurationBuilder maxTokensPerStep(int maxTokensPerStep) {
        this.maxTokensPerStep = maxTokensPerStep;
        return this;
    }

    public MultiStepReasoningConfigurationBuilder temperature(double temperature) {
        this.temperature = temperature;
        return this;
    }

    public MultiStepReasoningConfigurationBuilder confidenceThreshold(double confidenceThreshold) {
        this.confidenceThreshold = confidenceThreshold;
        return this;
    }

    public MultiStepReasoningConfigurationBuilder guidancePromptsEnabled(boolean guidancePromptsEnabled) {
        this.guidancePromptsEnabled = guidancePromptsEnabled;
        return this;
    }

    public MultiStepReasoningConfigurationBuilder maxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
        return this;
    }

    public MultiStepReasoningConfigurationBuilder retryDelayMs(long retryDelayMs) {
        this.retryDelayMs = retryDelayMs;
        return this;
    }

    public MultiStepReasoningConfiguration build() {
        return new MultiStepReasoningConfiguration(this);
    }
}
