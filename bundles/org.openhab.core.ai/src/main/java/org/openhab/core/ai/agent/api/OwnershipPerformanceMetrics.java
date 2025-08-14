package org.openhab.core.ai.agent.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public interface OwnershipPerformanceMetrics {
    long getTotalResolutions();
    long getCacheHits();
    long getCacheMisses();
    double getAverageResolutionTime();
    double getCacheHitRate();
}


