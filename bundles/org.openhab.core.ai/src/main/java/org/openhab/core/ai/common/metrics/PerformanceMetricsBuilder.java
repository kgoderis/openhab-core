package org.openhab.core.ai.common.metrics;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.builder.MetricsBuilder;

/**
 * Builder for PerformanceMetrics instances.
 * 
 * <p>
 * This builder provides a fluent interface for creating performance metrics
 * objects with all the necessary performance data including counts, timings,
 * and custom metrics.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public final class PerformanceMetricsBuilder extends MetricsBuilder<UnifiedPerformanceMetrics> {

    /**
     * Create a new PerformanceMetricsBuilder.
     */
    public PerformanceMetricsBuilder() {
        super();
    }

    /**
     * Create a new PerformanceMetricsBuilder from an existing PerformanceMetrics instance.
     */
    public PerformanceMetricsBuilder(PerformanceMetrics source) {
        super();
        if (source != null) {
            withTotalCount(source.getTotalOperations()).withSuccessCount(source.getSuccessfulOperations())
                    .withFailureCount(source.getFailedOperations()).withTotalDurationMs(source.getTotalProcessingTime())
                    .withLastExecution(source.getLastOperationTime());
            // Note: Interface doesn't provide min/max duration, first execution, or custom metrics
            // These will remain at their default values
        }
    }

    @Override
    public UnifiedPerformanceMetrics build() {
        validate();
        return new UnifiedPerformanceMetrics(this);
    }
}
