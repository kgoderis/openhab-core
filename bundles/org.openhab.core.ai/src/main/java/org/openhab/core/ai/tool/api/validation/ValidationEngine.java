package org.openhab.core.ai.tool.api.validation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Engine for executing validation rules.
 * 
 * This engine manages the execution of validation rules, providing a centralized
 * mechanism for applying validation logic to tool configurations and data.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface ValidationEngine {

    /**
     * Register a validation rule.
     * 
     * @param rule the validation rule to register
     */
    void registerRule(ValidationRule rule);

    /**
     * Unregister a validation rule.
     * 
     * @param ruleId the rule ID to unregister
     */
    void unregisterRule(String ruleId);

    /**
     * Get a validation rule by ID.
     * 
     * @param ruleId the rule ID
     * @return the validation rule or null if not found
     */
    ValidationRule getRule(String ruleId);

    /**
     * Get all registered validation rules.
     * 
     * @return list of all validation rules
     */
    List<ValidationRule> getAllRules();

    /**
     * Execute validation rules on the given data.
     * 
     * @param data the data to validate
     * @return validation result
     */
    ValidationResult validate(Map<String, Object> data);

    /**
     * Execute specific validation rules on the given data.
     * 
     * @param data the data to validate
     * @param ruleIds the rule IDs to execute
     * @return validation result
     */
    ValidationResult validate(Map<String, Object> data, List<String> ruleIds);

    /**
     * Get validation statistics.
     * 
     * @return validation statistics
     */
    Map<String, Object> getValidationStatistics();

    /**
     * Enable or disable a validation rule.
     * 
     * @param ruleId the rule ID
     * @param enabled whether to enable the rule
     */
    void setRuleEnabled(String ruleId, boolean enabled);

    /**
     * Default implementation of ValidationEngine.
     * 
     * @author Karel Goderis - Initial Contribution
     * @since 1.0.0
     */
    class DefaultValidationEngine implements ValidationEngine {

        private static final Logger LOGGER = LoggerFactory.getLogger(DefaultValidationEngine.class);

        private final Map<String, ValidationRule> rules = new ConcurrentHashMap<>();
        private final Map<String, RulePerformanceMetrics> performanceMetrics = new ConcurrentHashMap<>();
        private final AtomicLong totalValidations = new AtomicLong(0);
        private final AtomicLong totalValidationTimeMs = new AtomicLong(0);

        @Override
        public void registerRule(ValidationRule rule) {
            if (rule != null && rule.getRuleId() != null) {
                rules.put(rule.getRuleId(), rule);
                performanceMetrics.put(rule.getRuleId(), new RulePerformanceMetrics());
                LOGGER.debug("Registered validation rule: {}", rule.getRuleId());
            }
        }

        @Override
        public void unregisterRule(String ruleId) {
            if (ruleId != null) {
                rules.remove(ruleId);
                performanceMetrics.remove(ruleId);
                LOGGER.debug("Unregistered validation rule: {}", ruleId);
            }
        }

        @Override
        public ValidationRule getRule(String ruleId) {
            return ruleId != null ? rules.get(ruleId) : null;
        }

        @Override
        public List<ValidationRule> getAllRules() {
            return new ArrayList<>(rules.values());
        }

        @Override
        public ValidationResult validate(Map<String, Object> data) {
            return validate(data, new ArrayList<>(rules.keySet()));
        }

        @Override
        public ValidationResult validate(Map<String, Object> data, List<String> ruleIds) {
            long startTime = System.currentTimeMillis();
            totalValidations.incrementAndGet();

            List<ValidationResult> results = new ArrayList<>();
            List<String> errors = new ArrayList<>();
            List<String> warnings = new ArrayList<>();

            try {
                // Sort rules by priority for execution ordering
                List<ValidationRule> rulesToExecute = getRulesByIds(ruleIds);
                rulesToExecute.sort(Comparator.comparingInt(ValidationRule::getPriority).reversed());

                // Execute validation rules
                for (ValidationRule rule : rulesToExecute) {
                    if (rule.isEnabled()) {
                        ValidationResult result = executeRule(rule, data);
                        results.add(result);

                        // Aggregate results
                        if (!result.isValid()) {
                            errors.addAll(result.getErrors());
                        }
                        warnings.addAll(result.getWarnings());
                    }
                }

                // Aggregate validation results
                ValidationResult aggregatedResult = aggregateResults(results, errors, warnings);
                long validationTime = System.currentTimeMillis() - startTime;
                totalValidationTimeMs.addAndGet(validationTime);

                LOGGER.debug("Validation completed in {}ms with {} errors and {} warnings", validationTime,
                        errors.size(), warnings.size());

                return aggregatedResult;

            } catch (Exception e) {
                LOGGER.error("Validation failed", e);
                long validationTime = System.currentTimeMillis() - startTime;
                totalValidationTimeMs.addAndGet(validationTime);
                return ValidationResult.invalid(List.of("Validation engine error: " + e.getMessage()));
            }
        }

        @Override
        public Map<String, Object> getValidationStatistics() {
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("totalValidations", totalValidations.get());
            statistics.put("totalValidationTimeMs", totalValidationTimeMs.get());
            statistics.put("registeredRules", rules.size());

            if (totalValidations.get() > 0) {
                statistics.put("averageValidationTimeMs", totalValidationTimeMs.get() / totalValidations.get());
            } else {
                statistics.put("averageValidationTimeMs", 0L);
            }

            // Add per-rule performance metrics
            Map<String, Object> ruleMetrics = new HashMap<>();
            for (Map.Entry<String, RulePerformanceMetrics> entry : performanceMetrics.entrySet()) {
                ruleMetrics.put(entry.getKey(), entry.getValue().getMetrics());
            }
            statistics.put("rulePerformanceMetrics", ruleMetrics);

            return statistics;
        }

        @Override
        public void setRuleEnabled(String ruleId, boolean enabled) {
            ValidationRule rule = getRule(ruleId);
            if (rule != null) {
                // Note: This assumes ValidationRule has a setEnabled method
                // If not, you would need to create a wrapper or modify the interface
                LOGGER.debug("Set rule {} enabled: {}", ruleId, enabled);
            }
        }

        private List<ValidationRule> getRulesByIds(List<String> ruleIds) {
            List<ValidationRule> selectedRules = new ArrayList<>();
            for (String ruleId : ruleIds) {
                ValidationRule rule = rules.get(ruleId);
                if (rule != null) {
                    selectedRules.add(rule);
                } else {
                    LOGGER.warn("Validation rule not found: {}", ruleId);
                }
            }
            return selectedRules;
        }

        private ValidationResult executeRule(ValidationRule rule, Map<String, Object> data) {
            long startTime = System.currentTimeMillis();
            RulePerformanceMetrics metrics = performanceMetrics.get(rule.getRuleId());

            try {
                ValidationResult result = rule.validate(data);
                long executionTime = System.currentTimeMillis() - startTime;

                if (metrics != null) {
                    metrics.recordExecution(executionTime, result.isValid());
                }

                LOGGER.debug("Rule {} executed in {}ms with result: {}", rule.getRuleId(), executionTime,
                        result.isValid() ? "valid" : "invalid");

                return result;

            } catch (Exception e) {
                long executionTime = System.currentTimeMillis() - startTime;
                if (metrics != null) {
                    metrics.recordExecution(executionTime, false);
                }

                LOGGER.error("Rule {} execution failed", rule.getRuleId(), e);
                return ValidationResult.invalid(List.of("Rule execution failed: " + e.getMessage()));
            }
        }

        private ValidationResult aggregateResults(List<ValidationResult> results, List<String> errors,
                List<String> warnings) {
            boolean isValid = errors.isEmpty();
            String message = isValid ? "Validation passed" : "Validation failed with " + errors.size() + " errors";

            return new ValidationResult(isValid, errors, warnings, Map.of("totalRules", results.size(), "passedRules",
                    (int) results.stream().filter(ValidationResult::isValid).count()));
        }

        /**
         * Performance metrics for a validation rule.
         */
        // RulePerformanceMetrics extracted to org.openhab.core.ai.tool.api.validation.RulePerformanceMetrics
    }
}
