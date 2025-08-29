package org.openhab.core.ai.common.error;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;


import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Unified result of error recovery operations.
 * 
 * This class encapsulates the result of error recovery operations, including
 * success status, recovery details, error information, and caching capabilities.
 * This unified class combines functionality from both tool and reasoning implementations.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ErrorRecoveryResult {

    private final boolean recovered;
    private final String status;
    private final String recoveryAction;
    private final String message;
    private final Map<String, Object> details;
    private final long timestamp;
    private final long recoveryTime;

    /**
     * Create a new error recovery result.
     * 
     * @param recovered whether the error was recovered from
     * @param status the recovery status
     * @param recoveryAction the recovery action taken
     * @param message the recovery message
     * @param details additional recovery details
     * @param timestamp the timestamp of the recovery attempt
     * @param recoveryTime the time taken for recovery
     */
    public ErrorRecoveryResult(boolean recovered, String status, String recoveryAction, String message,
            Map<String, Object> details, long timestamp, long recoveryTime) {
        this.recovered = recovered;
        this.status = Objects.requireNonNull(status, "status");
        this.recoveryAction = Objects.requireNonNull(recoveryAction, "recoveryAction");
        this.message = Objects.requireNonNull(message, "message");
        this.details = Objects.requireNonNull(details, "details");
        this.timestamp = timestamp;
        this.recoveryTime = recoveryTime;
    }

    /**
     * Create a successful recovery result.
     * 
     * @param message the recovery message
     * @return successful recovery result
     */
    public static ErrorRecoveryResult success(String message) {
        return new ErrorRecoveryResult(true, "RECOVERED", "SUCCESS", message, Map.of(), System.currentTimeMillis(), 0);
    }

    /**
     * Create a failed recovery result.
     * 
     * @param message the recovery message
     * @return failed recovery result
     */
    public static ErrorRecoveryResult failure(String message) {
        return new ErrorRecoveryResult(false, "FAILED", "NONE", message, Map.of(), System.currentTimeMillis(), 0);
    }

    /**
     * Check if the error was recovered from.
     * 
     * @return true if recovered, false otherwise
     */
    public boolean isRecovered() {
        return recovered;
    }

    /**
     * Get the recovery status.
     * 
     * @return the recovery status
     */
    public String getStatus() {
        return status;
    }

    /**
     * Get the recovery action taken.
     * 
     * @return the recovery action
     */
    public String getRecoveryAction() {
        return recoveryAction;
    }

    /**
     * Get the recovery message.
     * 
     * @return the recovery message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Get additional recovery details.
     * 
     * @return recovery details
     */
    public Map<String, Object> getDetails() {
        return details;
    }

    /**
     * Get the timestamp of the recovery attempt.
     * 
     * @return the timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Get the time taken for recovery.
     * 
     * @return the recovery time
     */
    public long getRecoveryTime() {
        return recoveryTime;
    }

    // Caching functionality from tool version
    private static final Map<String, ErrorRecoveryResult> resultCache = new ConcurrentHashMap<>();
    // Performance monitoring - migrated to MetricsService
    // private static final AtomicLong cacheHits = new AtomicLong();
    // private static final AtomicLong cacheMisses = new AtomicLong();

    /**
     * Cache a recovery result for future use.
     * 
     * @param cacheKey the cache key
     * @param result the result to cache
     */
    public static void cacheResult(String cacheKey, ErrorRecoveryResult result) {
        if (cacheKey != null && result != null) {
            resultCache.put(cacheKey, result);
        }
    }

    /**
     * Get a cached recovery result.
     * 
     * @param cacheKey the cache key
     * @return the cached result or null if not found
     */
    public static @Nullable ErrorRecoveryResult getCachedResult(String cacheKey) {
        ErrorRecoveryResult result = resultCache.get(cacheKey);
        // Performance monitoring migrated to MetricsService
        // if (result != null) {
        //     cacheHits.incrementAndGet();
        // } else {
        //     cacheMisses.incrementAndGet();
        // }
        return result;
    }

    /**
     * Clear the result cache.
     */
    public static void clearCache() {
        resultCache.clear();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ErrorRecoveryResult other = (ErrorRecoveryResult) obj;
        return recovered == other.recovered && Objects.equals(status, other.status)
                && Objects.equals(recoveryAction, other.recoveryAction) && Objects.equals(message, other.message)
                && Objects.equals(details, other.details) && timestamp == other.timestamp
                && recoveryTime == other.recoveryTime;
    }

    @Override
    public int hashCode() {
        return Objects.hash(recovered, status, recoveryAction, message, details, timestamp, recoveryTime);
    }

    @Override
    public String toString() {
        return "ErrorRecoveryResult{" + "recovered=" + recovered + ", status='" + status + '\'' + ", recoveryAction='"
                + recoveryAction + '\'' + ", message='" + message + '\'' + ", details=" + details + ", timestamp="
                + timestamp + ", recoveryTime=" + recoveryTime + '}';
    }
}
