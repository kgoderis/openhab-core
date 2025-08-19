package org.openhab.core.ai.tool.error.recovery;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Result of error recovery operations.
 * 
 * This class encapsulates the result of error recovery operations, including
 * success status, recovery details, and error information.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ErrorRecoveryResult {

    private final boolean recovered;
    private final String status;
    private final String message;
    private final Map<String, Object> details;
    private final long timestamp;

    /**
     * Create a new error recovery result.
     * 
     * @param recovered whether the error was recovered from
     * @param status the recovery status
     * @param message the recovery message
     * @param details additional recovery details
     * @param timestamp the timestamp of the recovery attempt
     */
    public ErrorRecoveryResult(boolean recovered, String status, String message, Map<String, Object> details,
            long timestamp) {
        this.recovered = recovered;
        this.status = status;
        this.message = message;
        this.details = details;
        this.timestamp = timestamp;
    }

    /**
     * Create a successful recovery result.
     * 
     * @param message the recovery message
     * @return successful recovery result
     */
    public static ErrorRecoveryResult success(String message) {
        return new ErrorRecoveryResult(true, "RECOVERED", message, Map.of(), System.currentTimeMillis());
    }

    /**
     * Create a failed recovery result.
     * 
     * @param message the recovery message
     * @return failed recovery result
     */
    public static ErrorRecoveryResult failure(String message) {
        return new ErrorRecoveryResult(false, "FAILED", message, Map.of(), System.currentTimeMillis());
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

    // Implement error recovery result caching
    private static final Map<String, ErrorRecoveryResult> resultCache = new ConcurrentHashMap<>();
    private static final long CACHE_TTL_MS = 300000; // 5 minutes

    /**
     * Cache a recovery result
     * 
     * @param cacheKey the cache key
     * @param result the result to cache
     */
    public static void cacheResult(String cacheKey, ErrorRecoveryResult result) {
        resultCache.put(cacheKey, result);
    }

    /**
     * Get a cached recovery result
     * 
     * @param cacheKey the cache key
     * @return the cached result or null if not found or expired
     */
    public static @Nullable ErrorRecoveryResult getCachedResult(String cacheKey) {
        ErrorRecoveryResult result = resultCache.get(cacheKey);
        if (result != null) {
            long age = System.currentTimeMillis() - result.getTimestamp();
            if (age < CACHE_TTL_MS) {
                return result;
            } else {
                resultCache.remove(cacheKey);
            }
        }
        return null;
    }

    /**
     * Clear the result cache
     */
    public static void clearCache() {
        resultCache.clear();
    }

    // Add support for error recovery result serialization
    /**
     * Serialize the recovery result to JSON
     * 
     * @return JSON string representation
     */
    public String toJson() {
        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("recovered", recovered);
        jsonMap.put("status", status);
        jsonMap.put("message", message);
        jsonMap.put("details", details);
        jsonMap.put("timestamp", timestamp);

        // Simple JSON serialization (in a real implementation, use a proper JSON library)
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"recovered\":").append(recovered).append(",");
        json.append("\"status\":\"").append(status).append("\",");
        json.append("\"message\":\"").append(message.replace("\"", "\"")).append("\",");
        json.append("\"timestamp\":").append(timestamp);

        if (!details.isEmpty()) {
            json.append(",\"details\":{");
            boolean first = true;
            for (Map.Entry<String, Object> entry : details.entrySet()) {
                if (!first)
                    json.append(",");
                json.append("\"").append(entry.getKey()).append("\":\"").append(entry.getValue()).append("\"");
                first = false;
            }
            json.append("}");
        }

        json.append("}");
        return json.toString();
    }

    /**
     * Create a recovery result from JSON
     * 
     * @param json the JSON string
     * @return the recovery result
     */
    public static ErrorRecoveryResult fromJson(String json) {
        // Simple JSON deserialization (in a real implementation, use a proper JSON library)
        // This is a basic implementation for demonstration purposes
        if (json.contains("\"recovered\":true")) {
            return success("Deserialized from JSON");
        } else {
            return failure("Deserialized from JSON");
        }
    }

    // Implement error recovery result comparison
    /**
     * Compare this result with another result
     * 
     * @param other the other result to compare with
     * @return comparison result
     */
    public int compareTo(ErrorRecoveryResult other) {
        // Compare by timestamp first (newer results first)
        int timeComparison = Long.compare(other.timestamp, this.timestamp);
        if (timeComparison != 0) {
            return timeComparison;
        }

        // Then compare by recovery status (successful recoveries first)
        if (this.recovered && !other.recovered) {
            return -1;
        } else if (!this.recovered && other.recovered) {
            return 1;
        }

        // Finally compare by status string
        return this.status.compareTo(other.status);
    }

    /**
     * Check if this result equals another result
     * 
     * @param obj the object to compare with
     * @return true if equal
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ErrorRecoveryResult other = (ErrorRecoveryResult) obj;
        return recovered == other.recovered && status.equals(other.status) && message.equals(other.message)
                && timestamp == other.timestamp;
    }

    /**
     * Get hash code for this result
     * 
     * @return hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(recovered, status, message, timestamp);
    }

    // Add support for error recovery result metrics
    private static final AtomicLong totalResults = new AtomicLong(0);
    private static final AtomicLong successfulResults = new AtomicLong(0);
    private static final AtomicLong failedResults = new AtomicLong(0);

    /**
     * Get metrics for all recovery results
     * 
     * @return metrics map
     */
    public static Map<String, Object> getMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        long total = totalResults.get();
        long successful = successfulResults.get();
        long failed = failedResults.get();

        metrics.put("totalResults", total);
        metrics.put("successfulResults", successful);
        metrics.put("failedResults", failed);
        metrics.put("successRate", total > 0 ? (double) successful / total : 0.0);
        metrics.put("cacheSize", resultCache.size());

        return metrics;
    }

    /**
     * Record metrics for this result
     */
    private void recordMetrics() {
        totalResults.incrementAndGet();
        if (recovered) {
            successfulResults.incrementAndGet();
        } else {
            failedResults.incrementAndGet();
        }
    }

    /**
     * Reset all metrics
     */
    public static void resetMetrics() {
        totalResults.set(0);
        successfulResults.set(0);
        failedResults.set(0);
        clearCache();
    }
}
