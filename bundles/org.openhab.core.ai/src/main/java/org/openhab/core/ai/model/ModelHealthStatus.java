package org.openhab.core.ai.model;

import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Health status information for an LLM client.
 * 
 * @author openHAB AI Team
 * @since 4.0.0
 */
@NonNullByDefault
public class ModelHealthStatus {

    private final boolean available;
    private final Instant lastCheckTime;
    private final long averageResponseTimeMs;
    private final double successRate;
    private final int errorCount;
    private final @Nullable String lastError;
    private final @Nullable Instant lastErrorTime;

    public ModelHealthStatus(boolean available, Instant lastCheckTime, long averageResponseTimeMs, double successRate,
            int errorCount, @Nullable String lastError, @Nullable Instant lastErrorTime) {
        this.available = available;
        this.lastCheckTime = lastCheckTime;
        this.averageResponseTimeMs = averageResponseTimeMs;
        this.successRate = successRate;
        this.errorCount = errorCount;
        this.lastError = lastError;
        this.lastErrorTime = lastErrorTime;
    }

    /**
     * Checks if the client is available.
     * 
     * @return true if available
     */
    public boolean isAvailable() {
        return available;
    }

    /**
     * Gets the time of the last health check.
     * 
     * @return Last check time
     */
    public Instant getLastCheckTime() {
        return lastCheckTime;
    }

    /**
     * Gets the average response time in milliseconds.
     * 
     * @return Average response time in ms
     */
    public long getAverageResponseTimeMs() {
        return averageResponseTimeMs;
    }

    /**
     * Gets the success rate as a percentage (0.0 to 1.0).
     * 
     * @return Success rate
     */
    public double getSuccessRate() {
        return successRate;
    }

    /**
     * Gets the number of errors encountered.
     * 
     * @return Error count
     */
    public int getErrorCount() {
        return errorCount;
    }

    /**
     * Gets the last error message.
     * 
     * @return Last error message, or null if no errors
     */
    public @Nullable String getLastError() {
        return lastError;
    }

    /**
     * Gets the time of the last error.
     * 
     * @return Last error time, or null if no errors
     */
    public @Nullable Instant getLastErrorTime() {
        return lastErrorTime;
    }

    @Override
    public String toString() {
        return "ModelHealthStatus{available=" + available + ", lastCheckTime=" + lastCheckTime
                + ", averageResponseTimeMs=" + averageResponseTimeMs + ", successRate=" + successRate + ", errorCount="
                + errorCount + ", lastError='" + lastError + "', lastErrorTime=" + lastErrorTime + "}";
    }
}
