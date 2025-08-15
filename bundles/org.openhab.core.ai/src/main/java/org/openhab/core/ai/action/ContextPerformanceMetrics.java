package org.openhab.core.ai.action;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public interface ContextPerformanceMetrics {
    long getTotalContextsBuilt();

    long getCacheHits();

    long getCacheMisses();

    double getAverageBuildTime();

    double getCacheHitRate();
}
