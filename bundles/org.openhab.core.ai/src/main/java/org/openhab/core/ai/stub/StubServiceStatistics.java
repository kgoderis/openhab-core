package org.openhab.core.ai.stub;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Statistics class for stub services.
 * 
 * This class holds performance and usage metrics for stub services.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class StubServiceStatistics {

    private long requestCount = 0;
    private long successCount = 0;
    private long errorCount = 0;
    private long totalProcessingTimeMs = 0;
    private long averageProcessingTimeMs = 0;

    /**
     * Get the total number of requests handled.
     * 
     * @return Request count
     */
    public long getRequestCount() {
        return requestCount;
    }

    /**
     * Set the total number of requests handled.
     * 
     * @param requestCount Request count
     */
    public void setRequestCount(long requestCount) {
        this.requestCount = requestCount;
    }

    /**
     * Get the number of successful requests.
     * 
     * @return Success count
     */
    public long getSuccessCount() {
        return successCount;
    }

    /**
     * Set the number of successful requests.
     * 
     * @param successCount Success count
     */
    public void setSuccessCount(long successCount) {
        this.successCount = successCount;
    }

    /**
     * Get the number of failed requests.
     * 
     * @return Error count
     */
    public long getErrorCount() {
        return errorCount;
    }

    /**
     * Set the number of failed requests.
     * 
     * @param errorCount Error count
     */
    public void setErrorCount(long errorCount) {
        this.errorCount = errorCount;
    }

    /**
     * Get the total processing time in milliseconds.
     * 
     * @return Total processing time
     */
    public long getTotalProcessingTimeMs() {
        return totalProcessingTimeMs;
    }

    /**
     * Set the total processing time in milliseconds.
     * 
     * @param totalProcessingTimeMs Total processing time
     */
    public void setTotalProcessingTimeMs(long totalProcessingTimeMs) {
        this.totalProcessingTimeMs = totalProcessingTimeMs;
    }

    /**
     * Get the average processing time in milliseconds.
     * 
     * @return Average processing time
     */
    public long getAverageProcessingTimeMs() {
        return averageProcessingTimeMs;
    }

    /**
     * Set the average processing time in milliseconds.
     * 
     * @param averageProcessingTimeMs Average processing time
     */
    public void setAverageProcessingTimeMs(long averageProcessingTimeMs) {
        this.averageProcessingTimeMs = averageProcessingTimeMs;
    }

    /**
     * Update the average processing time based on current totals.
     */
    public void updateAverageProcessingTime() {
        if (requestCount > 0) {
            averageProcessingTimeMs = totalProcessingTimeMs / requestCount;
        }
    }

    /**
     * Reset all statistics to zero.
     */
    public void reset() {
        requestCount = 0;
        successCount = 0;
        errorCount = 0;
        totalProcessingTimeMs = 0;
        averageProcessingTimeMs = 0;
    }
}
