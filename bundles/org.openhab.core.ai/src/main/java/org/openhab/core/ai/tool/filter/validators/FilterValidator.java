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
 * Validator for tool filters.
 * 
 * This interface defines the contract for filter validators that can validate
 * filter configurations and expressions.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface FilterValidator {

    /**
     * Get the validator ID.
     * 
     * @return the validator ID
     */
    String getValidatorId();

    /**
     * Get the validator name.
     * 
     * @return the validator name
     */
    String getValidatorName();

    /**
     * Get the validator description.
     * 
     * @return the validator description
     */
    String getValidatorDescription();

    /**
     * Get the supported filter types.
     * 
     * @return list of supported filter types
     */
    String[] getSupportedFilterTypes();

    /**
     * Check if the validator is enabled.
     * 
     * @return true if the validator is enabled
     */
    boolean isEnabled();

    /**
     * Validate a filter configuration.
     * 
     * @param filterConfig the filter configuration to validate
     * @return validation result
     */
    FilterValidationResult validateFilter(Map<String, Object> filterConfig);

    /**
     * Validate a filter expression.
     * 
     * @param filterExpression the filter expression to validate
     * @return validation result
     */
    FilterValidationResult validateExpression(String filterExpression);

    /**
     * Get the validator configuration.
     * 
     * @return the validator configuration
     */
    Map<String, Object> getConfiguration();

    /**
     * Update the validator configuration.
     * 
     * @param configuration the new configuration
     */
    void updateConfiguration(Map<String, Object> configuration);

    /**
     * Register a custom filter type.
     * 
     * @param filterType the filter type to register
     * @param validator the validator for this filter type
     */
    void registerCustomFilterType(String filterType, FilterValidator validator);

    /**
     * Get performance metrics.
     * 
     * @return performance metrics
     */
    Map<String, Object> getPerformanceMetrics();

    /**
     * Clear validation cache.
     */
    void clearCache();

    /**
     * Default implementation of FilterValidator.
     * 
     * @author Karel Goderis - Initial Contribution
     * @since 1.0.0
     */
    class DefaultFilterValidator implements FilterValidator {

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
                // Check cache first for performance optimization
                String cacheKey = generateCacheKey(filterConfig);
                FilterValidationResult cachedResult = validationCache.get(cacheKey);
                if (cachedResult != null) {
                    cacheHitCount.incrementAndGet();
                    LOGGER.debug("Cache hit for filter validation: {}", cacheKey);
                    return cachedResult;
                }

                // Perform actual validation
                FilterValidationResult result = performFilterValidation(filterConfig);
                long validationTime = System.currentTimeMillis() - startTime;
                lastValidationTimeMs.set(validationTime);
                totalValidationTimeMs.addAndGet(validationTime);

                if (result.isValid()) {
                    successCount.incrementAndGet();
                } else {
                    failureCount.incrementAndGet();
                }

                // Cache the result for performance optimization
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
                // Check cache first for performance optimization
                String cacheKey = "expression:" + filterExpression;
                FilterValidationResult cachedResult = validationCache.get(cacheKey);
                if (cachedResult != null) {
                    cacheHitCount.incrementAndGet();
                    LOGGER.debug("Cache hit for expression validation: {}", cacheKey);
                    return cachedResult;
                }

                // Perform actual validation
                FilterValidationResult result = performExpressionValidation(filterExpression);
                long validationTime = System.currentTimeMillis() - startTime;
                lastValidationTimeMs.set(validationTime);
                totalValidationTimeMs.addAndGet(validationTime);

                if (result.isValid()) {
                    successCount.incrementAndGet();
                } else {
                    failureCount.incrementAndGet();
                }

                // Cache the result for performance optimization
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

            if (totalValidations > 0) {
                metrics.put("averageValidationTimeMs", totalValidationTimeMs.get() / totalValidations);
                metrics.put("successRate", (double) successCount.get() / totalValidations);
                metrics.put("cacheHitRate", (double) cacheHitCount.get() / totalValidations);
            } else {
                metrics.put("averageValidationTimeMs", 0L);
                metrics.put("successRate", 0.0);
                metrics.put("cacheHitRate", 0.0);
            }

            return metrics;
        }

        @Override
        public void clearCache() {
            validationCache.clear();
            LOGGER.debug("Validation cache cleared");
        }

        /**
         * Set the enabled state of this validator.
         * 
         * @param enabled whether the validator is enabled
         */
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        /**
         * Generate a cache key for filter configuration.
         * 
         * @param filterConfig the filter configuration
         * @return cache key
         */
        private String generateCacheKey(Map<String, Object> filterConfig) {
            return "filter:" + filterConfig.hashCode();
        }

        /**
         * Perform actual filter validation logic.
         * 
         * @param filterConfig the filter configuration to validate
         * @return validation result
         */
        private FilterValidationResult performFilterValidation(Map<String, Object> filterConfig) {
            List<String> errors = new ArrayList<>();
            List<String> warnings = new ArrayList<>();

            try {
                // Validate filter type
                String filterType = (String) filterConfig.get("type");
                if (filterType == null || filterType.isEmpty()) {
                    errors.add("Filter type is required");
                } else if (!isSupportedFilterType(filterType) && !customFilterTypes.containsKey(filterType)) {
                    errors.add("Unsupported filter type: " + filterType);
                }

                // Validate filter parameters
                Object parameters = filterConfig.get("parameters");
                if (parameters == null) {
                    warnings.add("Filter parameters are missing");
                } else if (!(parameters instanceof Map)) {
                    errors.add("Filter parameters must be a map");
                }

                // Validate filter conditions
                Object conditions = filterConfig.get("conditions");
                if (conditions != null && !(conditions instanceof List)) {
                    errors.add("Filter conditions must be a list");
                }

                // Validate custom filter types
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

            } catch (Exception e) {
                errors.add("Filter validation error: " + e.getMessage());
                return new FilterValidationResult(false, errors, warnings, Map.of());
            }
        }

        /**
         * Perform actual expression validation logic.
         * 
         * @param filterExpression the filter expression to validate
         * @return validation result
         */
        private FilterValidationResult performExpressionValidation(String filterExpression) {
            List<String> errors = new ArrayList<>();
            List<String> warnings = new ArrayList<>();

            try {
                if (filterExpression == null || filterExpression.isEmpty()) {
                    errors.add("Filter expression is required");
                    return new FilterValidationResult(false, errors, warnings, Map.of());
                }

                // Basic syntax validation
                if (!filterExpression.contains("=") && !filterExpression.contains(">")
                        && !filterExpression.contains("<") && !filterExpression.contains("!=")) {
                    warnings.add("Filter expression may not contain valid operators");
                }

                // Check for balanced parentheses
                if (!hasBalancedParentheses(filterExpression)) {
                    errors.add("Filter expression has unbalanced parentheses");
                }

                // Check for valid field names
                if (!hasValidFieldNames(filterExpression)) {
                    warnings.add("Filter expression may contain invalid field names");
                }

                boolean isValid = errors.isEmpty();
                return new FilterValidationResult(isValid, errors, warnings, Map.of("expression", filterExpression));

            } catch (Exception e) {
                errors.add("Expression validation error: " + e.getMessage());
                return new FilterValidationResult(false, errors, warnings, Map.of());
            }
        }

        /**
         * Check if the filter type is supported.
         * 
         * @param filterType the filter type to check
         * @return true if supported
         */
        private boolean isSupportedFilterType(String filterType) {
            for (String supportedType : supportedFilterTypes) {
                if (supportedType.equals(filterType)) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Check if the expression has balanced parentheses.
         * 
         * @param expression the expression to check
         * @return true if balanced
         */
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

        /**
         * Check if the expression has valid field names.
         * 
         * @param expression the expression to check
         * @return true if valid
         */
        private boolean hasValidFieldNames(String expression) {
            // Basic validation - in a real implementation, you would check against
            // a schema or list of valid field names
            return !expression.contains("invalid_field") && !expression.contains("__private");
        }
    }
}
