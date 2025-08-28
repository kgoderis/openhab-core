package org.openhab.core.ai.tool.filter.validators;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of {@link FilterValidator}.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@Component(service = FilterValidator.class, immediate = true)
@NonNullByDefault
public class DefaultFilterValidator implements FilterValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultFilterValidator.class);

    private final String validatorId;
    private final String validatorName;
    private final String validatorDescription;
    private final String[] supportedFilterTypes;
    private final Map<String, Object> configuration;
    private final Map<String, FilterValidator> customFilterTypes;
    private final Map<String, FilterValidationResult> validationCache;
    private boolean enabled = true;

    // MetricsService for recording validation operations - migrated from AtomicLong counters
    private @Nullable MetricsService metricsService;

    @Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
    protected void setMetricsService(MetricsService metricsService) {
        this.metricsService = metricsService;
        LOGGER.debug("MetricsService set for DefaultFilterValidator");
    }

    protected void unsetMetricsService(MetricsService metricsService) {
        if (this.metricsService == metricsService) {
            this.metricsService = null;
            LOGGER.debug("MetricsService unset for DefaultFilterValidator");
        }
    }

    public DefaultFilterValidator(String validatorId, String validatorName, String validatorDescription,
            String[] supportedFilterTypes) {
        this.validatorId = validatorId;
        this.validatorName = validatorName;
        this.validatorDescription = validatorDescription;
        this.supportedFilterTypes = supportedFilterTypes;
        this.configuration = new HashMap<>();
        this.customFilterTypes = new ConcurrentHashMap<>();
        this.validationCache = new ConcurrentHashMap<>();
    }

    @Override
    public String getValidatorId() {
        return validatorId;
    }

    @Override
    public String getValidatorName() {
        return validatorName;
    }

    @Override
    public String getValidatorDescription() {
        return validatorDescription;
    }

    @Override
    public String[] getSupportedFilterTypes() {
        return supportedFilterTypes;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public FilterValidationResult validateFilter(Map<String, Object> filterConfig) {
        long startTimeNanos = System.nanoTime();

        try {
            String cacheKey = generateCacheKey(filterConfig);
            FilterValidationResult cachedResult = validationCache.get(cacheKey);
            if (cachedResult != null) {
                // Record cache hit - successful operation with zero duration
                recordMetrics("filter-validator", "cache-hit", true,
                        Duration.ofNanos(System.nanoTime() - startTimeNanos));
                LOGGER.debug("Cache hit for filter validation: {}", cacheKey);
                return cachedResult;
            }

            FilterValidationResult result = performFilterValidation(filterConfig);
            Duration validationDuration = Duration.ofNanos(System.nanoTime() - startTimeNanos);

            // Record validation operation with success/failure status
            recordMetrics("filter-validator", "validation", result.isValid(), validationDuration);

            validationCache.put(cacheKey, result);
            LOGGER.debug("Filter validation completed in {}ms with result: {}", validationDuration.toMillis(),
                    result.isValid() ? "valid" : "invalid");
            return result;

        } catch (Exception e) {
            Duration validationDuration = Duration.ofNanos(System.nanoTime() - startTimeNanos);

            // Record failed validation operation
            recordMetrics("filter-validator", "validation", false, validationDuration);

            LOGGER.error("Filter validation failed", e);
            return FilterValidationResult.invalid(List.of("Filter validation failed: " + e.getMessage()));
        }
    }

    @Override
    public FilterValidationResult validateExpression(String filterExpression) {
        long startTimeNanos = System.nanoTime();

        try {
            String cacheKey = "expression:" + filterExpression;
            FilterValidationResult cachedResult = validationCache.get(cacheKey);
            if (cachedResult != null) {
                // Record cache hit for expression validation
                recordMetrics("filter-validator", "expression-cache-hit", true,
                        Duration.ofNanos(System.nanoTime() - startTimeNanos));
                LOGGER.debug("Cache hit for expression validation: {}", cacheKey);
                return cachedResult;
            }

            FilterValidationResult result = performExpressionValidation(filterExpression);
            Duration validationDuration = Duration.ofNanos(System.nanoTime() - startTimeNanos);

            // Record expression validation operation
            recordMetrics("filter-validator", "expression-validation", result.isValid(), validationDuration);

            validationCache.put(cacheKey, result);
            LOGGER.debug("Expression validation completed in {}ms with result: {}", validationDuration.toMillis(),
                    result.isValid() ? "valid" : "invalid");
            return result;
        } catch (Exception e) {
            Duration validationDuration = Duration.ofNanos(System.nanoTime() - startTimeNanos);

            // Record failed expression validation
            recordMetrics("filter-validator", "expression-validation", false, validationDuration);

            LOGGER.error("Expression validation failed", e);
            return FilterValidationResult.invalid(List.of("Expression validation failed: " + e.getMessage()));
        }
    }

    @Override
    public Map<String, Object> getConfiguration() {
        return new HashMap<>(configuration);
    }

    @Override
    public void updateConfiguration(Map<String, Object> configuration) {
        this.configuration.clear();
        this.configuration.putAll(configuration);
    }

    @Override
    public void registerCustomFilterType(String filterType, FilterValidator validator) {
        if (filterType != null && validator != null) {
            customFilterTypes.put(filterType, validator);
            LOGGER.debug("Registered custom filter type: {}", filterType);
        }
    }

    @Override
    public Map<String, Object> getPerformanceMetrics() {
        MetricsService metrics = metricsService;
        if (metrics == null) {
            LOGGER.warn("MetricsService not available, returning basic cache metrics");
            Map<String, Object> basicMetrics = new HashMap<>();
            basicMetrics.put("cacheSize", validationCache.size());
            basicMetrics.put("customFilterTypes", customFilterTypes.size());
            basicMetrics.put("enabled", enabled);
            return basicMetrics;
        }

        try {
            // Get snapshots for different operation types
            FilterValidatorSnapshot validationSnapshot = getFilterValidatorSnapshot(metrics, "validation");
            FilterValidatorSnapshot cacheHitSnapshot = getFilterValidatorSnapshot(metrics, "cache-hit");
            FilterValidatorSnapshot expressionSnapshot = getFilterValidatorSnapshot(metrics, "expression-validation");

            Map<String, Object> performanceMetrics = new HashMap<>();

            // Overall validation metrics
            performanceMetrics.put("validationCount", validationSnapshot.total());
            performanceMetrics.put("successCount", validationSnapshot.success());
            performanceMetrics.put("failureCount", validationSnapshot.failure());
            performanceMetrics.put("successRate", validationSnapshot.successRate());
            performanceMetrics.put("averageValidationTimeMs", validationSnapshot.averageMs(validationSnapshot.total()));

            // Cache metrics
            performanceMetrics.put("cacheHitCount", cacheHitSnapshot.total());
            performanceMetrics.put("cacheHitRate", calculateCacheHitRate(validationSnapshot, cacheHitSnapshot));
            performanceMetrics.put("cacheSize", validationCache.size());

            // Expression validation metrics
            performanceMetrics.put("expressionValidationCount", expressionSnapshot.total());
            performanceMetrics.put("expressionSuccessRate", expressionSnapshot.successRate());

            // Additional metrics
            performanceMetrics.put("customFilterTypes", customFilterTypes.size());
            performanceMetrics.put("enabled", enabled);
            performanceMetrics.put("validatorId", validatorId);
            performanceMetrics.put("timestamp", System.currentTimeMillis());

            return performanceMetrics;
        } catch (Exception e) {
            LOGGER.error("Error retrieving performance metrics from MetricsService", e);
            // Fallback to basic metrics
            Map<String, Object> fallbackMetrics = new HashMap<>();
            fallbackMetrics.put("cacheSize", validationCache.size());
            fallbackMetrics.put("customFilterTypes", customFilterTypes.size());
            fallbackMetrics.put("enabled", enabled);
            fallbackMetrics.put("error", "Metrics retrieval failed: " + e.getMessage());
            return fallbackMetrics;
        }
    }

    @Override
    public void clearCache() {
        validationCache.clear();
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    private String generateCacheKey(Map<String, Object> filterConfig) {
        return "filter:" + filterConfig.hashCode();
    }

    private FilterValidationResult performFilterValidation(Map<String, Object> filterConfig) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        String filterType = (String) filterConfig.get("type");
        if (filterType == null || filterType.isEmpty()) {
            errors.add("Filter type is required");
        } else if (!isSupportedFilterType(filterType) && !customFilterTypes.containsKey(filterType)) {
            errors.add("Unsupported filter type: " + filterType);
        }

        Object parameters = filterConfig.get("parameters");
        if (parameters == null) {
            warnings.add("Filter parameters are missing");
        } else if (!(parameters instanceof Map)) {
            errors.add("Filter parameters must be a map");
        }

        Object conditions = filterConfig.get("conditions");
        if (conditions != null && !(conditions instanceof List)) {
            errors.add("Filter conditions must be a list");
        }

        if (filterType != null && customFilterTypes.containsKey(filterType)) {
            FilterValidator customValidator = customFilterTypes.get(filterType);
            FilterValidationResult customResult = customValidator.validateFilter(filterConfig);
            if (!customResult.isValid()) {
                errors.addAll(customResult.getErrors());
            }
            warnings.addAll(customResult.getWarnings());
        }

        boolean isValid = errors.isEmpty();
        return new FilterValidationResult(isValid, errors, warnings, Map.of("filterType", filterType));
    }

    private FilterValidationResult performExpressionValidation(String filterExpression) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        if (filterExpression == null || filterExpression.isEmpty()) {
            errors.add("Filter expression is required");
            return new FilterValidationResult(false, errors, warnings, Map.of());
        }

        if (!filterExpression.contains("=") && !filterExpression.contains(">") && !filterExpression.contains("<")
                && !filterExpression.contains("!=")) {
            warnings.add("Filter expression may not contain valid operators");
        }

        if (!hasBalancedParentheses(filterExpression)) {
            errors.add("Filter expression has unbalanced parentheses");
        }

        if (!hasValidFieldNames(filterExpression)) {
            warnings.add("Filter expression may contain invalid field names");
        }

        boolean isValid = errors.isEmpty();
        return new FilterValidationResult(isValid, errors, warnings, Map.of("expression", filterExpression));
    }

    private boolean isSupportedFilterType(String filterType) {
        for (String supportedType : supportedFilterTypes) {
            if (supportedType.equals(filterType)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasBalancedParentheses(String expression) {
        int count = 0;
        for (char c : expression.toCharArray()) {
            if (c == '(') {
                count++;
            } else if (c == ')') {
                count--;
                if (count < 0) {
                    return false;
                }
            }
        }
        return count == 0;
    }

    private boolean hasValidFieldNames(String expression) {
        return !expression.contains("invalid_field") && !expression.contains("__private");
    }

    /**
     * Record metrics with error handling and graceful degradation.
     * 
     * @param domain the operation domain
     * @param operation the operation name
     * @param success whether the operation was successful
     * @param duration the operation duration
     */
    private void recordMetrics(String domain, String operation, boolean success, Duration duration) {
        MetricsService metrics = metricsService;
        if (metrics == null) {
            LOGGER.debug("MetricsService not available, skipping metric recording for {}.{}", domain, operation);
            return;
        }

        try {
            metrics.recordOperation(domain, operation, success, duration);
        } catch (Exception e) {
            LOGGER.warn("Failed to record metrics for {}.{}: {}", domain, operation, e.getMessage());
        }
    }

    /**
     * Get a FilterValidatorSnapshot from the MetricsService.
     * 
     * @param metrics the MetricsService instance
     * @param operation the operation name
     * @return FilterValidatorSnapshot or empty snapshot if not available
     */
    private FilterValidatorSnapshot getFilterValidatorSnapshot(MetricsService metrics, String operation) {
        try {
            var genericSnapshot = metrics.getSnapshot("filter-validator", operation);
            if (genericSnapshot != null) {
                // Convert generic snapshot to FilterValidatorSnapshot
                return FilterValidatorSnapshot.of(genericSnapshot.getTotal(), genericSnapshot.getSuccess(),
                        genericSnapshot.getFailure(), genericSnapshot.getTotalDurationNanos(), 0, // Cache hits - will
                                                                                                  // be calculated
                                                                                                  // separately
                        validationCache.size(), customFilterTypes.size());
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to get snapshot for operation {}: {}", operation, e.getMessage());
        }
        return FilterValidatorSnapshot.empty();
    }

    /**
     * Calculate cache hit rate from validation and cache hit snapshots.
     * 
     * @param validationSnapshot the validation snapshot
     * @param cacheHitSnapshot the cache hit snapshot
     * @return cache hit rate as percentage (0.0 to 100.0)
     */
    private double calculateCacheHitRate(FilterValidatorSnapshot validationSnapshot,
            FilterValidatorSnapshot cacheHitSnapshot) {
        long totalValidations = validationSnapshot.total();
        long cacheHits = cacheHitSnapshot.total();

        if (totalValidations > 0) {
            return ((double) cacheHits / totalValidations) * 100.0;
        }
        return 0.0;
    }
}
