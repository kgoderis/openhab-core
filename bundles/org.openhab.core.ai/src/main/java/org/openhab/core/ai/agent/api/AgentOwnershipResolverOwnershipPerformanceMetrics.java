package org.openhab.core.ai.agent.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public interface AgentOwnershipResolverOwnershipPerformanceMetrics {
    long getTotalResolutions();

    long getCacheHits();

    long getCacheMisses();

    double getAverageResolutionTime();

    double getCacheHitRate();
}
