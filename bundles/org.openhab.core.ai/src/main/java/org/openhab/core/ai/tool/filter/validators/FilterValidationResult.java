package org.openhab.core.ai.tool.filter.validators;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Result of filter validation operations.
 * 
 * This class encapsulates the result of filter validation operations, including
 * success status, error messages, and validation details.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class FilterValidationResult {

    private final boolean valid;
    private final List<String> errors;
    private final List<String> warnings;
    private final Map<String, Object> details;

    /**
     * Create a new filter validation result.
     * 
     * @param valid whether the filter is valid
     * @param errors list of error messages
     * @param warnings list of warning messages
     * @param details additional validation details
     */
    public FilterValidationResult(boolean valid, List<String> errors, List<String> warnings,
            Map<String, Object> details) {
        this.valid = valid;
        this.errors = errors;
        this.warnings = warnings;
        this.details = details;
    }

    /**
     * Create a valid filter validation result.
     * 
     * @return valid filter validation result
     */
    public static FilterValidationResult valid() {
        return new FilterValidationResult(true, List.of(), List.of(), Map.of());
    }

    /**
     * Create an invalid filter validation result.
     * 
     * @param errors list of error messages
     * @return invalid filter validation result
     */
    public static FilterValidationResult invalid(List<String> errors) {
        return new FilterValidationResult(false, errors, List.of(), Map.of());
    }

    /**
     * Check if the filter is valid.
     * 
     * @return true if valid, false otherwise
     */
    public boolean isValid() {
        return valid;
    }

    /**
     * Get the list of error messages.
     * 
     * @return list of error messages
     */
    public List<String> getErrors() {
        return errors;
    }

    /**
     * Get the list of warning messages.
     * 
     * @return list of warning messages
     */
    public List<String> getWarnings() {
        return warnings;
    }

    /**
     * Get additional validation details.
     * 
     * @return validation details
     */
    public Map<String, Object> getDetails() {
        return details;
    }

    // ===== CACHING SUPPORT =====

    private static final ConcurrentHashMap<String, FilterValidationResult> cache = new ConcurrentHashMap<>();
    private static final long CACHE_TTL_MS = 5 * 60 * 1000; // 5 minutes
    private final long cacheTimestamp = System.currentTimeMillis();

    /**
     * Cache this filter validation result with the given key.
     * 
     * @param cacheKey the cache key
     */
    public void cache(String cacheKey) {
        cache.put(cacheKey, this);
    }

    /**
     * Get a cached filter validation result.
     * 
     * @param cacheKey the cache key
     * @return cached result or null if not found or expired
     */
    public static FilterValidationResult getCached(String cacheKey) {
        FilterValidationResult result = cache.get(cacheKey);
        if (result != null && System.currentTimeMillis() - result.cacheTimestamp < CACHE_TTL_MS) {
            return result;
        }
        if (result != null) {
            cache.remove(cacheKey); // Remove expired entry
        }
        return null;
    }

    /**
     * Clear the filter validation result cache.
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
        Map<String, Object> stats = new HashMap<>();
        stats.put("size", cache.size());
        stats.put("ttlMs", CACHE_TTL_MS);
        return stats;
    }

    // ===== SERIALIZATION SUPPORT =====

    /**
     * Convert this filter validation result to a JSON-serializable map.
     * 
     * @return serializable map representation
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("valid", valid);
        map.put("errors", errors);
        map.put("warnings", warnings);
        map.put("details", details);
        map.put("timestamp", System.currentTimeMillis());
        return map;
    }

    /**
     * Create a filter validation result from a map representation.
     * 
     * @param map the map representation
     * @return filter validation result
     */
    @SuppressWarnings("unchecked")
    public static FilterValidationResult fromMap(Map<String, Object> map) {
        boolean valid = (Boolean) map.get("valid");
        List<String> errors = (List<String>) map.get("errors");
        List<String> warnings = (List<String>) map.get("warnings");
        Map<String, Object> details = (Map<String, Object>) map.get("details");

        return new FilterValidationResult(valid, errors, warnings, details);
    }

    // ===== COMPARISON METHODS =====

    /**
     * Compare this filter validation result with another.
     * 
     * @param other the other filter validation result
     * @return comparison result
     */
    public FilterValidationComparisonResult compare(FilterValidationResult other) {
        boolean sameValidity = this.valid == other.valid;
        boolean sameErrors = this.errors.equals(other.errors);
        boolean sameWarnings = this.warnings.equals(other.warnings);

        return new FilterValidationComparisonResult(sameValidity, sameErrors, sameWarnings);
    }

    /**
     * Check if this filter validation result is equivalent to another.
     * 
     * @param other the other filter validation result
     * @return true if equivalent
     */
    public boolean isEquivalent(FilterValidationResult other) {
        return this.valid == other.valid && this.errors.equals(other.errors) && this.warnings.equals(other.warnings);
    }

    // ===== METRICS SUPPORT =====

    /**
     * Get filter validation metrics for this result.
     * 
     * @return filter validation metrics
     */
    public FilterValidationMetrics getMetrics() {
        return new FilterValidationMetrics(valid, errors.size(), warnings.size(), details.size(),
                System.currentTimeMillis());
    }

    // ===== INNER CLASSES =====

    /**
     * Result of filter validation comparison.
     */
    // FilterValidationComparisonResult extracted to
    // org.openhab.core.ai.tool.filter.validators.FilterValidationComparisonResult

    /**
     * Filter validation metrics.
     */
    // FilterValidationMetrics extracted to org.openhab.core.ai.tool.filter.validators.FilterValidationMetrics
}
