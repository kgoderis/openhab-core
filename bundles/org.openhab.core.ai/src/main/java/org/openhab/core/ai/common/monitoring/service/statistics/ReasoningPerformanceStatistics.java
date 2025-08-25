package org.openhab.core.ai.common.monitoring.service.statistics;

import java.time.Duration;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.BusinessMetrics;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.EfficiencyMetrics;
import org.openhab.core.ai.common.monitoring.api.PercentileMetrics;
import org.openhab.core.ai.common.monitoring.api.TrendMetrics;

/**
 * Statistics class for reasoning performance analysis.
 * 
 * <p>
 * This class provides computed statistics and insights about reasoning performance
 * derived from metrics data over time ranges. It implements capability interfaces
 * for clean, type-safe statistics access.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record ReasoningPerformanceStatistics(long totalStepCount, long totalStorageSizeBytes, long activeStepCount,
        long archivedStepCount, long compressedStepCount, long totalTokensUsed, double totalCostUsd,
        long totalProcessingTimeMs, double averageQualityScore, double averageConfidence, double averageStepSizeBytes,
        double averageStepsPerSession, Object statusDistribution, Object typeDistribution,
        Object modelUsageDistribution, Object sessionDistribution, Duration timeRange, long timestampMs)
        implements
            StatisticsSnapshot,
            CountsMetrics,
            TrendMetrics,
            PercentileMetrics,
            BusinessMetrics,
            EfficiencyMetrics {

    // CountsMetrics implementation
    @Override
    public long total() {
        return totalStepCount;
    }

    @Override
    public long success() {
        // For reasoning, we consider steps with high quality score as successful
        return (long) (totalStepCount * (averageQualityScore / 100.0));
    }

    @Override
    public long failure() {
        return total() - success();
    }

    @Override
    public double successRate() {
        if (total() == 0)
            return 0.0;
        return (success() * 100.0) / total();
    }

    // TrendMetrics implementation
    @Override
    public double trendPercentage() {
        // For reasoning performance, we calculate trend based on quality score
        // This is a simplified implementation - in practice, you'd want historical data
        return 0.0; // Placeholder - would need historical data for proper trend calculation
    }

    @Override
    public String trendDirection() {
        double trend = trendPercentage();
        if (trend > 1.0)
            return "increasing";
        if (trend < -1.0)
            return "decreasing";
        return "stable";
    }

    @Override
    public double changeRate() {
        return trendPercentage() / timeRange.toDays();
    }

    // PercentileMetrics implementation
    @Override
    public double percentile50() {
        // For reasoning performance, percentiles would be calculated from processing times
        return totalProcessingTimeMs > 0 ? (double) totalProcessingTimeMs / total() : 0.0;
    }

    @Override
    public double percentile90() {
        // Simplified implementation - would need actual processing time distribution
        return percentile50() * 1.5;
    }

    @Override
    public double percentile95() {
        // Simplified implementation - would need actual processing time distribution
        return percentile50() * 2.0;
    }

    @Override
    public double percentile99() {
        // Simplified implementation - would need actual processing time distribution
        return percentile50() * 3.0;
    }

    // BusinessMetrics implementation
    @Override
    public double costEfficiency() {
        if (total() == 0)
            return 0.0;
        return totalCostUsd / total();
    }

    @Override
    public double resourceUtilization() {
        if (total() == 0)
            return 0.0;
        return (double) totalStorageSizeBytes / total();
    }

    @Override
    public double throughputEfficiency() {
        if (timeRange.toHours() == 0)
            return 0.0;
        return (double) total() / timeRange.toHours();
    }

    // EfficiencyMetrics implementation
    @Override
    public double resourceEfficiency() {
        if (total() == 0)
            return 0.0;
        return (double) totalStorageSizeBytes / total();
    }

    @Override
    public double timeEfficiency() {
        if (total() == 0)
            return 0.0;
        return (double) totalProcessingTimeMs / total();
    }

    @Override
    public double energyEfficiency() {
        // Simplified implementation - would need actual energy consumption data
        return resourceEfficiency(); // Use resource efficiency as proxy
    }

    /**
     * Create statistics from reasoning performance data.
     * 
     * @param totalStepCount total number of reasoning steps
     * @param totalStorageSizeBytes total storage size in bytes
     * @param activeStepCount number of active steps
     * @param archivedStepCount number of archived steps
     * @param compressedStepCount number of compressed steps
     * @param totalTokensUsed total tokens used
     * @param totalCostUsd total cost in USD
     * @param totalProcessingTimeMs total processing time in milliseconds
     * @param averageQualityScore average quality score
     * @param averageConfidence average confidence
     * @param averageStepSizeBytes average step size in bytes
     * @param averageStepsPerSession average steps per session
     * @param statusDistribution status distribution
     * @param typeDistribution type distribution
     * @param modelUsageDistribution model usage distribution
     * @param sessionDistribution session distribution
     * @param timeRange time range for statistics
     * @return reasoning performance statistics
     */
    public static ReasoningPerformanceStatistics fromReasoningData(long totalStepCount, long totalStorageSizeBytes,
            long activeStepCount, long archivedStepCount, long compressedStepCount, long totalTokensUsed,
            double totalCostUsd, long totalProcessingTimeMs, double averageQualityScore, double averageConfidence,
            double averageStepSizeBytes, double averageStepsPerSession, Object statusDistribution,
            Object typeDistribution, Object modelUsageDistribution, Object sessionDistribution, Duration timeRange) {
        return new ReasoningPerformanceStatistics(totalStepCount, totalStorageSizeBytes, activeStepCount,
                archivedStepCount, compressedStepCount, totalTokensUsed, totalCostUsd, totalProcessingTimeMs,
                averageQualityScore, averageConfidence, averageStepSizeBytes, averageStepsPerSession,
                statusDistribution, typeDistribution, modelUsageDistribution, sessionDistribution, timeRange,
                System.currentTimeMillis());
    }
}
