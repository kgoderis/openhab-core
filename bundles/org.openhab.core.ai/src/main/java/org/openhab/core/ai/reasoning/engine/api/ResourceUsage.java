package org.openhab.core.ai.reasoning.engine.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Resource usage information for a reasoning step.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record ResourceUsage(long inputTokens, long outputTokens, double costUsd, long memoryUsageBytes,
        long processingTimeMs, @Nullable String resourceProvider) {

    /**
     * Create a new ResourceUsage instance.
     * 
     * @param inputTokens the number of input tokens used
     * @param outputTokens the number of output tokens generated
     * @param costUsd the cost in USD
     * @param memoryUsageBytes the memory usage in bytes
     * @param processingTimeMs the processing time in milliseconds
     * @param resourceProvider the resource provider identifier
     */
    public ResourceUsage {
        if (inputTokens < 0) {
            throw new IllegalArgumentException("inputTokens must be non-negative");
        }
        if (outputTokens < 0) {
            throw new IllegalArgumentException("outputTokens must be non-negative");
        }
        if (costUsd < 0.0) {
            throw new IllegalArgumentException("costUsd must be non-negative");
        }
        if (memoryUsageBytes < 0) {
            throw new IllegalArgumentException("memoryUsageBytes must be non-negative");
        }
        if (processingTimeMs < 0) {
            throw new IllegalArgumentException("processingTimeMs must be non-negative");
        }
    }

    /**
     * Get the total number of tokens used.
     * 
     * @return the total token count
     */
    public long getTotalTokens() {
        return inputTokens + outputTokens;
    }

    /**
     * Get the cost per token.
     * 
     * @return the cost per token, or 0.0 if no tokens were used
     */
    public double getCostPerToken() {
        long totalTokens = getTotalTokens();
        return totalTokens > 0 ? costUsd / totalTokens : 0.0;
    }

    /**
     * Create a builder for ResourceUsage.
     * 
     * @return a new ResourceUsageBuilder
     */
    public static ResourceUsageBuilder builder() {
        return new ResourceUsageBuilder();
    }

    /**
     * Builder for ResourceUsage.
     */
    public static final class ResourceUsageBuilder {
        private long inputTokens = 0;
        private long outputTokens = 0;
        private double costUsd = 0.0;
        private long memoryUsageBytes = 0;
        private long processingTimeMs = 0;
        private @Nullable String resourceProvider = null;

        public ResourceUsageBuilder withInputTokens(long inputTokens) {
            this.inputTokens = inputTokens;
            return this;
        }

        public ResourceUsageBuilder withOutputTokens(long outputTokens) {
            this.outputTokens = outputTokens;
            return this;
        }

        public ResourceUsageBuilder withCostUsd(double costUsd) {
            this.costUsd = costUsd;
            return this;
        }

        public ResourceUsageBuilder withMemoryUsageBytes(long memoryUsageBytes) {
            this.memoryUsageBytes = memoryUsageBytes;
            return this;
        }

        public ResourceUsageBuilder withProcessingTimeMs(long processingTimeMs) {
            this.processingTimeMs = processingTimeMs;
            return this;
        }

        public ResourceUsageBuilder withResourceProvider(@Nullable String resourceProvider) {
            this.resourceProvider = resourceProvider;
            return this;
        }

        public ResourceUsage build() {
            return new ResourceUsage(inputTokens, outputTokens, costUsd, memoryUsageBytes, processingTimeMs,
                    resourceProvider);
        }
    }
}
