package org.openhab.core.ai.common.monitoring.base;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.Metrics;
import org.openhab.core.ai.common.monitoring.api.MonitoringType;
import org.openhab.core.ai.common.monitoring.api.ValidationUtils;

/**
 * Abstract base class for performance monitoring data implementations.
 * 
 * <p>
 * This class extends AbstractMonitoring and implements PerformanceData
 * to provide common functionality for performance-related monitoring,
 * focusing on operations, timing, rates, and efficiency metrics.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public abstract class AbstractMetrics extends AbstractMonitoring implements Metrics {

    private final long totalOperations;
    private final long successfulOperations;
    private final long failedOperations;
    private final long totalProcessingTime;
    private final double averageResponseTime;
    private final @Nullable Instant lastOperationTime;

    /**
     * Create a new AbstractMetrics instance.
     * 
     * @param id the unique identifier
     * @param timestamp the timestamp when data was created
     * @param domain the domain this data belongs to
     * @param source the source component, or null if not specified
     * @param description the description, or null if not available
     * @param data the raw data map, or null if no raw data
     * @param totalOperations the total number of operations
     * @param successfulOperations the number of successful operations
     * @param failedOperations the number of failed operations
     * @param totalProcessingTime the total processing time in milliseconds
     * @param averageResponseTime the average response time in milliseconds
     * @param lastOperationTime the timestamp of the last operation, or null if none
     */
    protected AbstractMetrics(String id, Instant timestamp, String domain, @Nullable String source,
            @Nullable String description, @Nullable Map<String, Object> data, long totalOperations,
            long successfulOperations, long failedOperations, long totalProcessingTime, double averageResponseTime,
            @Nullable Instant lastOperationTime) {
        super(id, timestamp, domain, source, description, data);
        this.totalOperations = totalOperations;
        this.successfulOperations = successfulOperations;
        this.failedOperations = failedOperations;
        this.totalProcessingTime = totalProcessingTime;
        this.averageResponseTime = averageResponseTime;
        this.lastOperationTime = lastOperationTime;

        // Validate monitoring data first, then performance-specific data
        validateMonitoringData();
        validatePerformanceData();
    }

    @Override
    public long getTotalOperations() {
        return totalOperations;
    }

    @Override
    public long getSuccessfulOperations() {
        return successfulOperations;
    }

    @Override
    public long getFailedOperations() {
        return failedOperations;
    }

    @Override
    public long getTotalProcessingTime() {
        return totalProcessingTime;
    }

    @Override
    public double getAverageResponseTime() {
        return averageResponseTime;
    }

    @Override
    public @Nullable Instant getLastOperationTime() {
        return lastOperationTime;
    }

    @Override
    public MonitoringType getType() {
        return MonitoringType.PERFORMANCE;
    }

    /**
     * Validate the performance data for consistency.
     * 
     * @throws IllegalArgumentException if the data is invalid
     */
    protected void validatePerformanceData() {
        ValidationUtils.validateNonNegative(totalOperations, "Total operations");
        ValidationUtils.validateNonNegative(successfulOperations, "Successful operations");
        ValidationUtils.validateNonNegative(failedOperations, "Failed operations");
        ValidationUtils.validateNonNegative(totalProcessingTime, "Total processing time");
        ValidationUtils.validateNonNegative(averageResponseTime, "Average response time");

        // Check that total operations equals successful + failed
        ValidationUtils.validateOperationCounts(totalOperations, successfulOperations, failedOperations,
                "Total operations", "Successful operations", "Failed operations");
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        AbstractMetrics other = (AbstractMetrics) obj;
        return totalOperations == other.totalOperations && successfulOperations == other.successfulOperations
                && failedOperations == other.failedOperations && totalProcessingTime == other.totalProcessingTime
                && Double.compare(averageResponseTime, other.averageResponseTime) == 0
                && Objects.equals(lastOperationTime, other.lastOperationTime);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (int) (totalOperations ^ (totalOperations >>> 32));
        result = prime * result + (int) (successfulOperations ^ (successfulOperations >>> 32));
        result = prime * result + (int) (failedOperations ^ (failedOperations >>> 32));
        result = prime * result + (int) (totalProcessingTime ^ (totalProcessingTime >>> 32));
        result = prime * result + Double.hashCode(averageResponseTime);
        result = prime * result + Objects.hashCode(lastOperationTime);
        return result;
    }

    @Override
    public String toString() {
        return super.toString().replace("}", "") + ", totalOperations=" + totalOperations + ", successfulOperations="
                + successfulOperations + ", failedOperations=" + failedOperations + ", totalProcessingTime="
                + totalProcessingTime + ", averageResponseTime=" + averageResponseTime + ", lastOperationTime="
                + lastOperationTime + '}';
    }
}
