package org.openhab.core.ai.agent.collaboration.context;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Context manager statistics snapshot.
 *
 * Provides counters for operations and sizes used for monitoring.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ContextManagerStatistics {

    private final long totalContextReads;
    private final long totalContextWrites;
    private final long totalContextConflicts;
    private final long totalContextCacheHits;
    private final long totalContextCacheMisses;
    private final int storedContexts;
    private final int storedVersions;
    private final int cachedContexts;
    private final int activeListeners;

    public ContextManagerStatistics(long totalContextReads, long totalContextWrites, long totalContextConflicts,
            long totalContextCacheHits, long totalContextCacheMisses, int storedContexts, int storedVersions,
            int cachedContexts, int activeListeners) {
        this.totalContextReads = totalContextReads;
        this.totalContextWrites = totalContextWrites;
        this.totalContextConflicts = totalContextConflicts;
        this.totalContextCacheHits = totalContextCacheHits;
        this.totalContextCacheMisses = totalContextCacheMisses;
        this.storedContexts = storedContexts;
        this.storedVersions = storedVersions;
        this.cachedContexts = cachedContexts;
        this.activeListeners = activeListeners;
    }

    public long getTotalContextReads() {
        return totalContextReads;
    }

    public long getTotalContextWrites() {
        return totalContextWrites;
    }

    public long getTotalContextConflicts() {
        return totalContextConflicts;
    }

    public long getTotalContextCacheHits() {
        return totalContextCacheHits;
    }

    public long getTotalContextCacheMisses() {
        return totalContextCacheMisses;
    }

    public int getStoredContexts() {
        return storedContexts;
    }

    public int getStoredVersions() {
        return storedVersions;
    }

    public int getCachedContexts() {
        return cachedContexts;
    }

    public int getActiveListeners() {
        return activeListeners;
    }
}


