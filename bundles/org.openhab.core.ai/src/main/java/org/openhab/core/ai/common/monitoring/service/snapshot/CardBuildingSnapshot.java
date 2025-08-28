package org.openhab.core.ai.common.monitoring.service.snapshot;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.api.MetricsSnapshot;

/**
 * Snapshot record for agent card building metrics.
 * 
 * <p>
 * This record provides agent card building metrics including
 * card generation, template processing, skill discovery,
 * and card validation. It implements CountsMetrics and
 * LatencyMetrics capability interfaces.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record CardBuildingSnapshot(String agentId, String cardType, long totalCardBuilds, long successfulCardBuilds,
        long failedCardBuilds, long templateProcessings, long skillDiscoveries, long cardValidations, long totalSkills,
        long discoveredSkills, long validatedSkills, long totalDurationNanos, long averageBuildTimeNanos,
        long averageTemplateTimeNanos, long averageDiscoveryTimeNanos, long averageValidationTimeNanos,
        long maxBuildTimeNanos, long minBuildTimeNanos, long cardSizeBytes, long templateSizeBytes,
        boolean cardCachingEnabled, long cacheHits, long cacheMisses,
        long timestampMs) implements MetricsSnapshot, CountsMetrics, LatencyMetrics {

    // CountsMetrics implementation
    @Override
    public long total() {
        return totalCardBuilds;
    }

    @Override
    public long success() {
        return successfulCardBuilds;
    }

    @Override
    public long failure() {
        return failedCardBuilds;
    }

    // LatencyMetrics implementation
    @Override
    public long totalDurationNanos() {
        return totalDurationNanos;
    }

    /**
     * Calculate card building success rate.
     * 
     * @return success rate between 0.0 and 1.0
     */
    public double buildSuccessRate() {
        return totalCardBuilds > 0 ? (double) successfulCardBuilds / totalCardBuilds : 0.0;
    }

    /**
     * Calculate skill discovery success rate.
     * 
     * @return discovery success rate between 0.0 and 1.0
     */
    public double skillDiscoveryRate() {
        return totalSkills > 0 ? (double) discoveredSkills / totalSkills : 0.0;
    }

    /**
     * Calculate skill validation success rate.
     * 
     * @return validation success rate between 0.0 and 1.0
     */
    public double skillValidationRate() {
        return discoveredSkills > 0 ? (double) validatedSkills / discoveredSkills : 0.0;
    }

    /**
     * Calculate cache hit rate.
     * 
     * @return cache hit rate between 0.0 and 1.0
     */
    public double cacheHitRate() {
        long totalCacheRequests = cacheHits + cacheMisses;
        return totalCacheRequests > 0 ? (double) cacheHits / totalCacheRequests : 0.0;
    }

    /**
     * Calculate cache miss rate.
     * 
     * @return cache miss rate between 0.0 and 1.0
     */
    public double cacheMissRate() {
        long totalCacheRequests = cacheHits + cacheMisses;
        return totalCacheRequests > 0 ? (double) cacheMisses / totalCacheRequests : 0.0;
    }

    /**
     * Get average build time in milliseconds.
     * 
     * @return average build time in ms
     */
    public double averageBuildTimeMs() {
        return averageBuildTimeNanos / 1_000_000.0;
    }

    /**
     * Get average template processing time in milliseconds.
     * 
     * @return average template time in ms
     */
    public double averageTemplateTimeMs() {
        return averageTemplateTimeNanos / 1_000_000.0;
    }

    /**
     * Get average skill discovery time in milliseconds.
     * 
     * @return average discovery time in ms
     */
    public double averageDiscoveryTimeMs() {
        return averageDiscoveryTimeNanos / 1_000_000.0;
    }

    /**
     * Get average validation time in milliseconds.
     * 
     * @return average validation time in ms
     */
    public double averageValidationTimeMs() {
        return averageValidationTimeNanos / 1_000_000.0;
    }

    /**
     * Get maximum build time in milliseconds.
     * 
     * @return maximum build time in ms
     */
    public double maxBuildTimeMs() {
        return maxBuildTimeNanos / 1_000_000.0;
    }

    /**
     * Get minimum build time in milliseconds.
     * 
     * @return minimum build time in ms
     */
    public double minBuildTimeMs() {
        return minBuildTimeNanos / 1_000_000.0;
    }

    /**
     * Get card size in kilobytes.
     * 
     * @return card size in KB
     */
    public double cardSizeKB() {
        return cardSizeBytes / 1024.0;
    }

    /**
     * Get template size in kilobytes.
     * 
     * @return template size in KB
     */
    public double templateSizeKB() {
        return templateSizeBytes / 1024.0;
    }

    /**
     * Calculate card building throughput (cards per second).
     * 
     * @return throughput in cards per second
     */
    public double buildThroughput() {
        // This would need to be calculated based on time window
        // For now, return a placeholder calculation
        return totalCardBuilds > 0 ? (double) totalCardBuilds / (averageBuildTimeMs() / 1000.0) : 0.0;
    }

    /**
     * Calculate skill discovery efficiency.
     * 
     * @return discovery efficiency score between 0.0 and 1.0
     */
    public double skillDiscoveryEfficiency() {
        if (totalSkills == 0) {
            return 1.0; // No skills to discover, perfect efficiency
        }

        double discoveryRate = skillDiscoveryRate();
        double timeEfficiency = Math.max(0.0, 1.0 - (averageDiscoveryTimeMs() / 10000.0)); // 10 second baseline

        return (discoveryRate * 0.7) + (timeEfficiency * 0.3);
    }

    /**
     * Calculate card building efficiency.
     * 
     * @return building efficiency score between 0.0 and 1.0
     */
    public double cardBuildingEfficiency() {
        double successRate = buildSuccessRate();
        double timeEfficiency = Math.max(0.0, 1.0 - (averageBuildTimeMs() / 5000.0)); // 5 second baseline
        double cacheEfficiency = cardCachingEnabled ? cacheHitRate() : 1.0; // Assume perfect if no caching

        return (successRate * 0.5) + (timeEfficiency * 0.3) + (cacheEfficiency * 0.2);
    }

    /**
     * Check if card building is performing well.
     * 
     * @return true if success rate > 95% and build time is reasonable
     */
    public boolean isHealthy() {
        return buildSuccessRate() > 0.95 && averageBuildTimeMs() < 3000.0; // 3 second threshold
    }

    /**
     * Check if card building is under stress.
     * 
     * @return true if failure rate > 5% or build time is too high
     */
    public boolean isStressed() {
        double failureRate = totalCardBuilds > 0 ? (double) failedCardBuilds / totalCardBuilds : 0.0;
        return failureRate > 0.05 || averageBuildTimeMs() > 10000.0; // 10 second threshold
    }

    /**
     * Get card building status.
     * 
     * @return building status string
     */
    public String buildingStatus() {
        if (isStressed()) {
            return "stressed";
        } else if (isHealthy()) {
            return "healthy";
        } else {
            return "degraded";
        }
    }

    /**
     * Calculate overall card building reliability score.
     * 
     * @return reliability score between 0.0 and 1.0
     */
    public double reliabilityScore() {
        double buildWeight = buildSuccessRate();
        double discoveryWeight = skillDiscoveryEfficiency();
        double validationWeight = skillValidationRate();
        double timeWeight = Math.max(0.0, 1.0 - (averageBuildTimeMs() / 15000.0)); // 15 second baseline

        return (buildWeight * 0.4) + (discoveryWeight * 0.3) + (validationWeight * 0.2) + (timeWeight * 0.1);
    }

    /**
     * Get skill coverage percentage.
     * 
     * @return skill coverage as percentage
     */
    public double skillCoverage() {
        return totalSkills > 0 ? (double) discoveredSkills / totalSkills * 100.0 : 0.0;
    }

    /**
     * Get validation coverage percentage.
     * 
     * @return validation coverage as percentage
     */
    public double validationCoverage() {
        return discoveredSkills > 0 ? (double) validatedSkills / discoveredSkills * 100.0 : 0.0;
    }
}
