package org.openhab.core.ai.common.metrics.collector;

import java.util.concurrent.atomic.LongAdder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.metrics.api.Counts;
import org.openhab.core.ai.common.metrics.api.Timing;
import org.openhab.core.ai.common.metrics.snapshot.ExecutionMetricsSnapshot;

/**
 * Thread-safe collector for execution metrics.
 * 
 * <p>
 * This collector uses LongAdder for efficient thread-safe metrics collection.
 * It provides methods for recording execution results and generating snapshots.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public final class ExecutionMetricsCollector {

    private final LongAdder total = new LongAdder();
    private final LongAdder success = new LongAdder();
    private final LongAdder totalDurationNanos = new LongAdder();

    /**
     * Record an execution result.
     * 
     * @param ok whether the execution was successful
     * @param durationNanos execution duration in nanoseconds
     */
    public void recordExecution(boolean ok, long durationNanos) {
        total.increment();
        totalDurationNanos.add(durationNanos);
        if (ok) {
            success.increment();
        }
    }

    /**
     * Create a snapshot of the current metrics.
     * 
     * @return metrics snapshot
     */
    public ExecutionMetricsSnapshot snapshot() {
        long t = total.sum();
        long s = success.sum();
        long d = totalDurationNanos.sum();
        return new ExecutionMetricsSnapshot(new Counts(t, s, Math.max(0, t - s)), new Timing(d),
                System.currentTimeMillis());
    }
}
