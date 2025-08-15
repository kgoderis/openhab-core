package org.openhab.core.ai.tool.validation.api;

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
 * Default implementation of {@link ValidationEngine}.
 *
 * Provides registration and execution of validation rules with performance
 * metrics and result aggregation.
 *
 * Author: Karel Goderis - Initial Contribution
 * 
 * @since 1.0.0
 */
@NonNullByDefault
public class DefaultValidationEngine implements ValidationEngine {

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
            List<ValidationRule> rulesToExecute = getRulesByIds(ruleIds);
            rulesToExecute.sort(Comparator.comparingInt(ValidationRule::getPriority).reversed());

            for (ValidationRule rule : rulesToExecute) {
                if (rule.isEnabled()) {
                    ValidationResult result = executeRule(rule, data);
                    results.add(result);

                    if (!result.isValid()) {
                        errors.addAll(result.getErrors());
                    }
                    warnings.addAll(result.getWarnings());
                }
            }

            ValidationResult aggregatedResult = aggregateResults(results, errors, warnings);
            long validationTime = System.currentTimeMillis() - startTime;
            totalValidationTimeMs.addAndGet(validationTime);

            LOGGER.debug("Validation completed in {}ms with {} errors and {} warnings", validationTime, errors.size(),
                    warnings.size());

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
}
