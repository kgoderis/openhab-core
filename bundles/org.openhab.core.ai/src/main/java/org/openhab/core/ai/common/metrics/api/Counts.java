package org.openhab.core.ai.common.metrics.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Immutable record for count-based metrics.
 * 
 * <p>
 * This record provides a thread-safe way to share count metrics between
 * collectors and snapshots.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record Counts(long total, long success, long failure) {

    /**
     * Create a new Counts record.
     * 
     * @param total total operations
     * @param success successful operations
     * @param failure failed operations
     */
    public Counts {
        if (total < 0) {
            throw new IllegalArgumentException("total must be non-negative");
        }
        if (success < 0) {
            throw new IllegalArgumentException("success must be non-negative");
        }
        if (failure < 0) {
            throw new IllegalArgumentException("failure must be non-negative");
        }
        if (success + failure > total) {
            throw new IllegalArgumentException("success + failure cannot exceed total");
        }
    }
}
