package org.openhab.core.ai.common.monitoring.snapshot;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.Counts;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.api.MetricsSnapshot;
import org.openhab.core.ai.common.monitoring.api.TemplateMetrics;
import org.openhab.core.ai.common.monitoring.api.Timing;

/**
 * Immutable snapshot of template metrics data.
 * 
 * This class provides a thread-safe, immutable view of template-related metrics
 * including counts, latency, and template-specific statistics.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public record TemplateSnapshot(Counts counts, Timing timing, long totalTemplates, long usedTemplates,
        long parameterCompletions, long parameterValidations,
        long timestampMs) implements MetricsSnapshot, CountsMetrics, LatencyMetrics, TemplateMetrics {

    @Override
    public long total() {
        return counts.total();
    }

    @Override
    public long success() {
        return counts.success();
    }

    @Override
    public long failure() {
        return counts.failure();
    }

    @Override
    public double successRate() {
        return CountsMetrics.super.successRate();
    }

    @Override
    public long totalDurationNanos() {
        return timing.totalDurationNanos();
    }

    /**
     * Get the average response time in milliseconds.
     *
     * @return average response time in milliseconds
     */
    public double averageMs() {
        return LatencyMetrics.super.averageMs(total());
    }
}
