package org.openhab.core.ai.tool.compliance;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Result of compliance test operations.
 * 
 * This class encapsulates the result of compliance test operations, including
 * success status, test details, and compliance information.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ComplianceTestResult {

    private final String testId;
    private final String category;
    private final String description;
    private final boolean passed;
    private final String status;
    private final String message;
    private final List<String> failures;
    private final List<String> warnings;
    private final Map<String, Object> details;
    private final long timestamp;
    private final long durationMs;
    private String errorMessage;

    /**
     * Create a new compliance test result.
     * 
     * @param passed whether the test passed
     * @param status the test status
     * @param message the test message
     * @param failures list of test failures
     * @param warnings list of test warnings
     * @param details additional test details
     * @param timestamp the timestamp of the test
     */
    public ComplianceTestResult(boolean passed, String status, String message, List<String> failures,
            List<String> warnings, Map<String, Object> details, long timestamp) {
        this.testId = "";
        this.category = "";
        this.description = "";
        this.passed = passed;
        this.status = status;
        this.message = message;
        this.failures = failures;
        this.warnings = warnings;
        this.details = details;
        this.timestamp = timestamp;
        this.durationMs = 0L;
        this.errorMessage = "";
    }

    /**
     * Create a new compliance test result with full context expected by the validator.
     */
    public ComplianceTestResult(String testId, String category, String description, boolean passed, long durationMs,
            long timestamp) {
        this.testId = testId;
        this.category = category;
        this.description = description;
        this.passed = passed;
        this.status = passed ? "PASSED" : "FAILED";
        this.message = description;
        this.failures = List.of();
        this.warnings = List.of();
        this.details = Map.of();
        this.timestamp = timestamp;
        this.durationMs = durationMs;
        this.errorMessage = "";
    }

    /**
     * Create a passed compliance test result.
     * 
     * @param message the test message
     * @return passed compliance test result
     */
    public static ComplianceTestResult passed(String message) {
        return new ComplianceTestResult(true, "PASSED", message, List.of(), List.of(), Map.of(),
                System.currentTimeMillis());
    }

    /**
     * Create a failed compliance test result.
     * 
     * @param message the test message
     * @param failures list of test failures
     * @return failed compliance test result
     */
    public static ComplianceTestResult failed(String message, List<String> failures) {
        return new ComplianceTestResult(false, "FAILED", message, failures, List.of(), Map.of(),
                System.currentTimeMillis());
    }

    /**
     * Check if the test passed.
     * 
     * @return true if passed, false otherwise
     */
    public boolean isPassed() {
        return passed;
    }

    /**
     * Get the test category.
     */
    public String getCategory() {
        return category;
    }

    /**
     * Get the test status.
     * 
     * @return the test status
     */
    public String getStatus() {
        return status;
    }

    /**
     * Get the test message.
     * 
     * @return the test message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Get the list of test failures.
     * 
     * @return list of test failures
     */
    public List<String> getFailures() {
        return failures;
    }

    /**
     * Get the list of test warnings.
     * 
     * @return list of test warnings
     */
    public List<String> getWarnings() {
        return warnings;
    }

    /**
     * Get additional test details.
     * 
     * @return test details
     */
    public Map<String, Object> getDetails() {
        return details;
    }

    /**
     * Get the timestamp of the test.
     * 
     * @return the timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Get the test duration in milliseconds.
     */
    public long getDurationMs() {
        return durationMs;
    }

    /**
     * Set an error message for the test.
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * Get the error message if any.
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    // ===== CACHING SUPPORT =====

    private static final java.util.concurrent.ConcurrentHashMap<String, ComplianceTestResult> cache = new java.util.concurrent.ConcurrentHashMap<>();
    private static final long CACHE_TTL_MS = 10 * 60 * 1000; // 10 minutes
    private final long cacheTimestamp = System.currentTimeMillis();

    /**
     * Cache this compliance test result with the given key.
     * 
     * @param cacheKey the cache key
     */
    public void cache(String cacheKey) {
        cache.put(cacheKey, this);
    }

    /**
     * Get a cached compliance test result.
     * 
     * @param cacheKey the cache key
     * @return cached result or null if not found or expired
     */
    public static ComplianceTestResult getCached(String cacheKey) {
        ComplianceTestResult result = cache.get(cacheKey);
        if (result != null && System.currentTimeMillis() - result.cacheTimestamp < CACHE_TTL_MS) {
            return result;
        }
        if (result != null) {
            cache.remove(cacheKey); // Remove expired entry
        }
        return null;
    }

    /**
     * Clear the compliance test result cache.
     */
    public static void clearCache() {
        cache.clear();
    }

    /**
     * Get cache statistics.
     * 
     * @return cache statistics
     */
    public static Map<String, Object> getCacheStats() {
        Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("size", cache.size());
        stats.put("ttlMs", CACHE_TTL_MS);
        return stats;
    }

    // ===== SERIALIZATION SUPPORT =====

    /**
     * Convert this compliance test result to a JSON-serializable map.
     * 
     * @return serializable map representation
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new java.util.HashMap<>();
        map.put("testId", testId);
        map.put("category", category);
        map.put("description", description);
        map.put("passed", passed);
        map.put("status", status);
        map.put("message", message);
        map.put("failures", failures);
        map.put("warnings", warnings);
        map.put("details", details);
        map.put("timestamp", timestamp);
        map.put("durationMs", durationMs);
        map.put("errorMessage", errorMessage);
        return map;
    }

    /**
     * Create a compliance test result from a map representation.
     * 
     * @param map the map representation
     * @return compliance test result
     */
    @SuppressWarnings("unchecked")
    public static ComplianceTestResult fromMap(Map<String, Object> map) {
        String testId = (String) map.get("testId");
        String category = (String) map.get("category");
        String description = (String) map.get("description");
        boolean passed = (Boolean) map.get("passed");
        long timestamp = (Long) map.get("timestamp");
        long durationMs = (Long) map.get("durationMs");
        String errorMessage = (String) map.get("errorMessage");

        ComplianceTestResult result = new ComplianceTestResult(testId, category, description, passed, durationMs,
                timestamp);
        result.setErrorMessage(errorMessage);
        return result;
    }

    // ===== COMPARISON METHODS =====

    /**
     * Compare this compliance test result with another.
     * 
     * @param other the other compliance test result
     * @return comparison result
     */
    public ComplianceTestComparisonResult compare(ComplianceTestResult other) {
        boolean sameTestId = this.testId.equals(other.testId);
        boolean sameCategory = this.category.equals(other.category);
        boolean samePassed = this.passed == other.passed;
        boolean sameStatus = this.status.equals(other.status);
        boolean sameDuration = this.durationMs == other.durationMs;

        return new ComplianceTestComparisonResult(sameTestId, sameCategory, samePassed, sameStatus, sameDuration);
    }

    /**
     * Check if this compliance test result is equivalent to another.
     * 
     * @param other the other compliance test result
     * @return true if equivalent
     */
    public boolean isEquivalent(ComplianceTestResult other) {
        return this.testId.equals(other.testId) && this.category.equals(other.category) && this.passed == other.passed
                && this.status.equals(other.status);
    }

    // ===== METRICS SUPPORT =====

    /**
     * Get compliance test metrics for this result.
     * 
     * @return compliance test metrics
     */
    public ComplianceTestMetrics getMetrics() {
        return new ComplianceTestMetrics(testId, category, passed, durationMs, failures.size(), warnings.size(),
                details.size(), timestamp);
    }

    // ===== INNER CLASSES =====

    /**
     * Result of compliance test comparison.
     */
    // ComplianceTestComparisonResult extracted to org.openhab.core.ai.tool.compliance.ComplianceTestComparisonResult

    /**
     * Compliance test metrics.
     */
    // ComplianceTestMetrics extracted to org.openhab.core.ai.tool.compliance.ComplianceTestMetrics
}
