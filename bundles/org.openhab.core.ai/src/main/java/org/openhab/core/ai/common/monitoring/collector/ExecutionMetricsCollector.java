package org.openhab.core.ai.common.monitoring.collector;

import java.util.concurrent.atomic.LongAdder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.Counts;
import org.openhab.core.ai.common.monitoring.api.Timing;
import org.openhab.core.ai.common.monitoring.snapshot.ExecutionMetricsSnapshot;

/**
 * Thread-safe collector for execution metrics using LongAdder for optimal performance.
 * 
 * This collector records execution statistics in a thread-safe manner and provides
 * immutable snapshots for analysis and reporting.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public final class ExecutionMetricsCollector {
    private final LongAdder total = new LongAdder();
    private final LongAdder success = new LongAdder();
    private final LongAdder totalDurationNanos = new LongAdder();

    /**
     * Records an execution with the given outcome and duration.
     * 
     * @param ok true if the execution was successful, false otherwise
     * @param durationNanos the duration of the execution in nanoseconds
     */
    public void recordExecution(boolean ok, long durationNanos) {
        total.increment();
        totalDurationNanos.add(durationNanos);
        if (ok) {
            success.increment();
        }
    }

    /**
     * Creates an immutable snapshot of the current metrics state.
     * 
     * @return an immutable ExecutionMetricsSnapshot containing the current metrics
     */
    public ExecutionMetricsSnapshot snapshot() {
        long t = total.sum();
        long s = success.sum();
        long d = totalDurationNanos.sum();
        return new ExecutionMetricsSnapshot(new Counts(t, s, Math.max(0, t - s)), new Timing(d),
                System.currentTimeMillis());
    }

    /**
     * Reset all counters to zero.
     * This method is useful for periodic resets or testing.
     */
    public void reset() {
        total.reset();
        success.reset();
        totalDurationNanos.reset();
    }

    /**
     * Get the current total count without creating a full snapshot.
     * 
     * @return current total count
     */
    public long getCurrentTotal() {
        return total.sum();
    }

    /**
     * Get the current success count without creating a full snapshot.
     * 
     * @return current success count
     */
    public long getCurrentSuccess() {
        return success.sum();
    }

    /**
     * Check if this collector has recorded any operations.
     * 
     * @return true if any operations have been recorded
     */
    public boolean hasData() {
        return total.sum() > 0;
    }
}
