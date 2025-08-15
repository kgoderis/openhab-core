package org.openhab.core.ai.agent.lifecycle.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public interface OwnershipPerformanceMetrics {
    long getTotalResolutions();

    long getCacheHits();

    long getCacheMisses();

    double getAverageResolutionTime();

    double getCacheHitRate();
}
