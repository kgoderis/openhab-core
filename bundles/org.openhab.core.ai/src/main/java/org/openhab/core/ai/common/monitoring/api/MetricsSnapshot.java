package org.openhab.core.ai.common.monitoring.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Marker interface for metrics snapshots.
 * 
 * <p>
 * This interface marks immutable snapshot objects that provide a view of metrics
 * at a specific point in time. Snapshots are thread-safe and can be shared
 * across threads.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface MetricsSnapshot {

    /**
     * Get the timestamp when this snapshot was created.
     * 
     * @return timestamp in milliseconds since epoch
     */
    default long timestampMs() {
        return System.currentTimeMillis();
    }
}
