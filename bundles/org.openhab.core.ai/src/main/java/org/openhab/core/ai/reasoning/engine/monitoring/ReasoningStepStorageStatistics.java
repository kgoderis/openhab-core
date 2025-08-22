package org.openhab.core.ai.reasoning.engine.monitoring;

import java.time.Instant;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.base.AbstractMetrics;
import org.openhab.core.ai.reasoning.engine.api.ReasoningStepStatus;
import org.openhab.core.ai.reasoning.engine.api.ReasoningStepType;

/**
 * Statistics about reasoning step storage that extends the unified monitoring framework.
 * 
 * <p>
 * This class provides comprehensive statistics about the storage of reasoning steps including:
 * - Total counts and sizes
 * - Status distribution
 * - Type distribution
 * - Model usage statistics
 * - Performance metrics
 * - Storage health indicators
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ReasoningStepStorageStatistics extends AbstractMetrics implements CountsMetrics, LatencyMetrics {

    private final long totalStorageSizeBytes;
    private final long activeStepCount;
    private final long archivedStepCount;
    private final long compressedStepCount;
    private final Map<ReasoningStepStatus, Long> statusDistribution;
    private final Map<ReasoningStepType, Long> typeDistribution;
    private final Map<String, Long> modelUsageDistribution;
    private final Map<String, Long> sessionDistribution;
    private final double averageStepSizeBytes;
    private final double averageStepsPerSession;
    private final double averageQualityScore;
    private final double averageConfidence;
    private final long totalTokensUsed;
    private final double totalCostUsd;
    private final Instant oldestStepTime;
    private final Instant newestStepTime;
    private final Instant lastMaintenanceTime;
    private final boolean isHealthy;
    private final @Nullable String healthMessage;

    /**
     * Create a new ReasoningStepStorageStatistics instance.
     * 
     * @param id unique identifier for this statistics instance
     * @param timestamp timestamp when statistics were collected
     * @param totalStepCount total number of reasoning steps stored
     * @param successfulOperations number of successful storage operations
     * @param failedOperations number of failed storage operations
     * @param totalProcessingTime total processing time in nanoseconds
     * @param averageResponseTime average response time in milliseconds
     * @param lastOperationTime timestamp of last operation
     * @param totalStorageSizeBytes total storage size in bytes
     * @param activeStepCount number of active steps
     * @param archivedStepCount number of archived steps
     * @param compressedStepCount number of compressed steps
     * @param statusDistribution distribution of steps by status
     * @param typeDistribution distribution of steps by type
     * @param modelUsageDistribution distribution of steps by model
     * @param sessionDistribution distribution of steps by session
     * @param averageStepSizeBytes average step size in bytes
     * @param averageStepsPerSession average steps per session
     * @param averageQualityScore average quality score
     * @param averageConfidence average confidence score
     * @param totalTokensUsed total tokens used
     * @param totalCostUsd total cost in USD
     * @param oldestStepTime timestamp of oldest step
     * @param newestStepTime timestamp of newest step
     * @param lastMaintenanceTime timestamp of last maintenance
     * @param isHealthy storage health status
     * @param healthMessage health status message
     * @param data additional monitoring data
     */
    public ReasoningStepStorageStatistics(String id, Instant timestamp, long totalStepCount, long successfulOperations,
            long failedOperations, long totalProcessingTime, double averageResponseTime,
            @Nullable Instant lastOperationTime, long totalStorageSizeBytes, long activeStepCount,
            long archivedStepCount, long compressedStepCount, Map<ReasoningStepStatus, Long> statusDistribution,
            Map<ReasoningStepType, Long> typeDistribution, Map<String, Long> modelUsageDistribution,
            Map<String, Long> sessionDistribution, double averageStepSizeBytes, double averageStepsPerSession,
            double averageQualityScore, double averageConfidence, long totalTokensUsed, double totalCostUsd,
            Instant oldestStepTime, Instant newestStepTime, Instant lastMaintenanceTime, boolean isHealthy,
            @Nullable String healthMessage, @Nullable Map<String, Object> data) {
        super(id, timestamp, "reasoning", "step-storage", "Reasoning step storage statistics", data, totalStepCount,
                successfulOperations, failedOperations, totalProcessingTime, averageResponseTime, lastOperationTime);
        this.totalStorageSizeBytes = totalStorageSizeBytes;
        this.activeStepCount = activeStepCount;
        this.archivedStepCount = archivedStepCount;
        this.compressedStepCount = compressedStepCount;
        this.statusDistribution = Map.copyOf(statusDistribution);
        this.typeDistribution = Map.copyOf(typeDistribution);
        this.modelUsageDistribution = Map.copyOf(modelUsageDistribution);
        this.sessionDistribution = Map.copyOf(sessionDistribution);
        this.averageStepSizeBytes = averageStepSizeBytes;
        this.averageStepsPerSession = averageStepsPerSession;
        this.averageQualityScore = averageQualityScore;
        this.averageConfidence = averageConfidence;
        this.totalTokensUsed = totalTokensUsed;
        this.totalCostUsd = totalCostUsd;
        this.oldestStepTime = oldestStepTime;
        this.newestStepTime = newestStepTime;
        this.lastMaintenanceTime = lastMaintenanceTime;
        this.isHealthy = isHealthy;
        this.healthMessage = healthMessage;
    }

    /**
     * Create a new ReasoningStepStorageStatistics instance with current timestamp.
     * 
     * @param id unique identifier for this statistics instance
     * @param totalStepCount total number of reasoning steps stored
     * @param successfulOperations number of successful storage operations
     * @param failedOperations number of failed storage operations
     * @param totalProcessingTime total processing time in nanoseconds
     * @param averageResponseTime average response time in milliseconds
     * @param totalStorageSizeBytes total storage size in bytes
     * @param activeStepCount number of active steps
     * @param archivedStepCount number of archived steps
     * @param compressedStepCount number of compressed steps
     * @param statusDistribution distribution of steps by status
     * @param typeDistribution distribution of steps by type
     * @param modelUsageDistribution distribution of steps by model
     * @param sessionDistribution distribution of steps by session
     * @param averageStepSizeBytes average step size in bytes
     * @param averageStepsPerSession average steps per session
     * @param averageQualityScore average quality score
     * @param averageConfidence average confidence score
     * @param totalTokensUsed total tokens used
     * @param totalCostUsd total cost in USD
     * @param oldestStepTime timestamp of oldest step
     * @param newestStepTime timestamp of newest step
     * @param lastMaintenanceTime timestamp of last maintenance
     * @param isHealthy storage health status
     * @param healthMessage health status message
     */
    public ReasoningStepStorageStatistics(String id, long totalStepCount, long successfulOperations,
            long failedOperations, long totalProcessingTime, double averageResponseTime, long totalStorageSizeBytes,
            long activeStepCount, long archivedStepCount, long compressedStepCount,
            Map<ReasoningStepStatus, Long> statusDistribution, Map<ReasoningStepType, Long> typeDistribution,
            Map<String, Long> modelUsageDistribution, Map<String, Long> sessionDistribution,
            double averageStepSizeBytes, double averageStepsPerSession, double averageQualityScore,
            double averageConfidence, long totalTokensUsed, double totalCostUsd, Instant oldestStepTime,
            Instant newestStepTime, Instant lastMaintenanceTime, boolean isHealthy, @Nullable String healthMessage) {
        this(id, Instant.now(), totalStepCount, successfulOperations, failedOperations, totalProcessingTime,
                averageResponseTime, null, totalStorageSizeBytes, activeStepCount, archivedStepCount,
                compressedStepCount, statusDistribution, typeDistribution, modelUsageDistribution, sessionDistribution,
                averageStepSizeBytes, averageStepsPerSession, averageQualityScore, averageConfidence, totalTokensUsed,
                totalCostUsd, oldestStepTime, newestStepTime, lastMaintenanceTime, isHealthy, healthMessage, null);
    }

    // CountsMetrics interface implementation
    @Override
    public long total() {
        return getTotalOperations();
    }

    @Override
    public long success() {
        return getSuccessfulOperations();
    }

    @Override
    public long failure() {
        return getFailedOperations();
    }

    // LatencyMetrics interface implementation
    @Override
    public long totalDurationNanos() {
        return getTotalProcessingTime();
    }

    // Storage-specific getters
    public long getTotalStorageSizeBytes() {
        return totalStorageSizeBytes;
    }

    public long getActiveStepCount() {
        return activeStepCount;
    }

    public long getArchivedStepCount() {
        return archivedStepCount;
    }

    public long getCompressedStepCount() {
        return compressedStepCount;
    }

    public Map<ReasoningStepStatus, Long> getStatusDistribution() {
        return statusDistribution;
    }

    public Map<ReasoningStepType, Long> getTypeDistribution() {
        return typeDistribution;
    }

    public Map<String, Long> getModelUsageDistribution() {
        return modelUsageDistribution;
    }

    public Map<String, Long> getSessionDistribution() {
        return sessionDistribution;
    }

    public double getAverageStepSizeBytes() {
        return averageStepSizeBytes;
    }

    public double getAverageStepsPerSession() {
        return averageStepsPerSession;
    }

    public double getAverageQualityScore() {
        return averageQualityScore;
    }

    public double getAverageConfidence() {
        return averageConfidence;
    }

    public long getTotalTokensUsed() {
        return totalTokensUsed;
    }

    public double getTotalCostUsd() {
        return totalCostUsd;
    }

    public Instant getOldestStepTime() {
        return oldestStepTime;
    }

    public Instant getNewestStepTime() {
        return newestStepTime;
    }

    public Instant getLastMaintenanceTime() {
        return lastMaintenanceTime;
    }

    public boolean isHealthy() {
        return isHealthy;
    }

    public @Nullable String getHealthMessage() {
        return healthMessage;
    }

    /**
     * Get the storage efficiency score.
     * 
     * @return efficiency score between 0.0 and 1.0
     */
    public double getStorageEfficiency() {
        double successRate = successRate();
        double compressionRatio = getTotalOperations() > 0 ? (double) compressedStepCount / getTotalOperations() : 0.0;
        double healthScore = isHealthy ? 1.0 : 0.5;

        return (successRate * 0.4) + (compressionRatio * 0.3) + (healthScore * 0.3);
    }

    /**
     * Get the storage utilization percentage.
     * 
     * @return storage utilization as a percentage
     */
    public double getStorageUtilization() {
        return getTotalOperations() > 0 ? (double) activeStepCount / getTotalOperations() * 100.0 : 0.0;
    }
}
