package org.openhab.core.ai.tool.filter.validators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of {@link FilterValidator}.
 *
 * Author: Karel Goderis - Initial Contribution
 * 
 * @since 1.0.0
 */
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
    private final AtomicLong validationCount = new AtomicLong(0);
    private final AtomicLong cacheHitCount = new AtomicLong(0);
    private final AtomicLong totalValidationTimeMs = new AtomicLong(0);
    private final AtomicLong lastValidationTimeMs = new AtomicLong(0);
    private final AtomicLong successCount = new AtomicLong(0);
    private final AtomicLong failureCount = new AtomicLong(0);
    private boolean enabled = true;

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
        long startTime = System.currentTimeMillis();
        validationCount.incrementAndGet();

        try {
            String cacheKey = generateCacheKey(filterConfig);
            FilterValidationResult cachedResult = validationCache.get(cacheKey);
            if (cachedResult != null) {
                cacheHitCount.incrementAndGet();
                LOGGER.debug("Cache hit for filter validation: {}", cacheKey);
                return cachedResult;
            }

            FilterValidationResult result = performFilterValidation(filterConfig);
            long validationTime = System.currentTimeMillis() - startTime;
            lastValidationTimeMs.set(validationTime);
            totalValidationTimeMs.addAndGet(validationTime);

            if (result.isValid()) {
                successCount.incrementAndGet();
            } else {
                failureCount.incrementAndGet();
            }
            validationCache.put(cacheKey, result);
            LOGGER.debug("Filter validation completed in {}ms with result: {}", validationTime,
                    result.isValid() ? "valid" : "invalid");
            return result;

        } catch (Exception e) {
            long validationTime = System.currentTimeMillis() - startTime;
            lastValidationTimeMs.set(validationTime);
            totalValidationTimeMs.addAndGet(validationTime);
            failureCount.incrementAndGet();
            LOGGER.error("Filter validation failed", e);
            return FilterValidationResult.invalid(List.of("Filter validation failed: " + e.getMessage()));
        }
    }

    @Override
    public FilterValidationResult validateExpression(String filterExpression) {
        long startTime = System.currentTimeMillis();
        validationCount.incrementAndGet();

        try {
            String cacheKey = "expression:" + filterExpression;
            FilterValidationResult cachedResult = validationCache.get(cacheKey);
            if (cachedResult != null) {
                cacheHitCount.incrementAndGet();
                LOGGER.debug("Cache hit for expression validation: {}", cacheKey);
                return cachedResult;
            }

            FilterValidationResult result = performExpressionValidation(filterExpression);
            long validationTime = System.currentTimeMillis() - startTime;
            lastValidationTimeMs.set(validationTime);
            totalValidationTimeMs.addAndGet(validationTime);
            if (result.isValid()) {
                successCount.incrementAndGet();
            } else {
                failureCount.incrementAndGet();
            }
            validationCache.put(cacheKey, result);
            LOGGER.debug("Expression validation completed in {}ms with result: {}", validationTime,
                    result.isValid() ? "valid" : "invalid");
            return result;
        } catch (Exception e) {
            long validationTime = System.currentTimeMillis() - startTime;
            lastValidationTimeMs.set(validationTime);
            totalValidationTimeMs.addAndGet(validationTime);
            failureCount.incrementAndGet();
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
        Map<String, Object> metrics = new HashMap<>();
        long totalValidations = validationCount.get();
        metrics.put("validationCount", totalValidations);
        metrics.put("cacheHitCount", cacheHitCount.get());
        metrics.put("totalValidationTimeMs", totalValidationTimeMs.get());
        metrics.put("lastValidationTimeMs", lastValidationTimeMs.get());
        metrics.put("successCount", successCount.get());
        metrics.put("failureCount", failureCount.get());
        metrics.put("cacheSize", validationCache.size());
        metrics.put("customFilterTypes", customFilterTypes.size());
        metrics.put("averageValidationTimeMs",
                totalValidations > 0 ? totalValidationTimeMs.get() / totalValidations : 0L);
        metrics.put("successRate", totalValidations > 0 ? (double) successCount.get() / totalValidations : 0.0);
        metrics.put("cacheHitRate", totalValidations > 0 ? (double) cacheHitCount.get() / totalValidations : 0.0);
        return metrics;
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
}
