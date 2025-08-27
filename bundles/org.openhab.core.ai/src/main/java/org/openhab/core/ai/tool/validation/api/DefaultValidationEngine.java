package org.openhab.core.ai.tool.validation.api;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.time.Duration;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.validation.ToolValidationResult;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
import org.openhab.core.ai.common.monitoring.api.MetricKey;
import org.openhab.core.ai.common.monitoring.api.MetricKeys;
import org.openhab.core.ai.common.monitoring.service.snapshot.GenericMetricsSnapshot;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eclipse.jdt.annotation.Nullable;

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
    // Remove RulePerformanceMetrics - use MetricsService directly
    // private final Map<String, RulePerformanceMetrics> performanceMetrics = new ConcurrentHashMap<>();

    @Reference
    private @Nullable MetricsService metricsService;

    @Activate
    public DefaultValidationEngine() {
        // Remove RulePerformanceMetrics initialization
        // performanceMetrics.put(rule.getRuleId(), new RulePerformanceMetrics());
    }

    @Override
    public void registerRule(ValidationRule rule) {
        if (rule != null && rule.getRuleId() != null) {
            rules.put(rule.getRuleId(), rule);
            // Remove RulePerformanceMetrics initialization
            // performanceMetrics.put(rule.getRuleId(), new RulePerformanceMetrics());
            
            // Record rule registration metrics
            MetricsService metrics = metricsService;
            if (metrics != null) {
                try {
                    metrics.recordOperation("validation", "rule_registration")
                        .withSuccess(true)
                        .withDuration(0L)
                        .withData("ruleId", rule.getRuleId())
                        .withData("rulePriority", rule.getPriority())
                        .record();
                } catch (Exception e) {
                    LOGGER.warn("Failed to record rule registration metrics for rule {}: {}", rule.getRuleId(), e.getMessage());
                    // Graceful degradation: continue with rule registration even if metrics recording fails
                }
            }
            
            LOGGER.debug("Registered validation rule: {}", rule.getRuleId());
        }
    }

    @Override
    public void unregisterRule(String ruleId) {
        if (ruleId != null) {
            rules.remove(ruleId);
            // Remove RulePerformanceMetrics initialization
            // performanceMetrics.remove(ruleId);
            
            // Record rule unregistration metrics
            MetricsService metrics = metricsService;
            if (metrics != null) {
                try {
                    metrics.recordOperation("validation", "rule_unregistration")
                        .withSuccess(true)
                        .withDuration(0L)
                        .withData("ruleId", ruleId)
                        .record();
                } catch (Exception e) {
                    LOGGER.warn("Failed to record rule unregistration metrics for rule {}: {}", ruleId, e.getMessage());
                    // Graceful degradation: continue with rule unregistration even if metrics recording fails
                }
            }
            
            LOGGER.debug("Unregistered validation rule: {}", ruleId);
        }
    }

    @Override
    public ValidationRule getRule(String ruleId) {
        ValidationRule rule = ruleId != null ? rules.get(ruleId) : null;
        
        // Record rule lookup metrics
        MetricsService metrics = metricsService;
        if (metrics != null) {
            try {
                metrics.recordOperation("validation", "get_rule")
                    .withSuccess(rule != null)
                    .withDuration(0L)
                    .withData("ruleId", ruleId != null ? ruleId : "null")
                    .withData("found", rule != null)
                    .record();
            } catch (Exception e) {
                LOGGER.warn("Failed to record rule lookup metrics for rule {}: {}", ruleId, e.getMessage());
                // Graceful degradation: continue with rule lookup even if metrics recording fails
            }
        }
        
        return rule;
    }

    @Override
    public List<ValidationRule> getAllRules() {
        // Record rule listing metrics
        MetricsService metrics = metricsService;
        if (metrics != null) {
            try {
                metrics.recordOperation("validation", "list_rules")
                    .withSuccess(true)
                    .withDuration(0L)
                    .withData("ruleCount", rules.size())
                    .record();
            } catch (Exception e) {
                LOGGER.warn("Failed to record rule listing metrics: {}", e.getMessage());
                // Graceful degradation: continue with rule listing even if metrics recording fails
            }
        }
        
        return new ArrayList<>(rules.values());
    }

    @Override
    public ToolValidationResult validate(Map<String, Object> data) {
        // Record validation start metrics
        MetricsService metrics = metricsService;
        if (metrics != null) {
            try {
                metrics.recordOperation("validation", "start_all_rules")
                    .withSuccess(true)
                    .withDuration(0L)
                    .withData("ruleCount", rules.size())
                    .record();
            } catch (Exception e) {
                LOGGER.warn("Failed to record validation start metrics: {}", e.getMessage());
                // Graceful degradation: continue with validation even if metrics recording fails
            }
        }
        
        return validate(data, new ArrayList<>(rules.keySet()));
    }

    @Override
    public ToolValidationResult validate(Map<String, Object> data, List<String> ruleIds) {
        long startTime = System.currentTimeMillis();
        boolean success = false;
        
        // Record validation start metrics
        recordValidationMetrics("start", true, 0);

        List<ToolValidationResult> results = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        try {
            List<ValidationRule> rulesToExecute = getRulesByIds(ruleIds);
            rulesToExecute.sort(Comparator.comparingInt(ValidationRule::getPriority).reversed());

            for (ValidationRule rule : rulesToExecute) {
                if (rule.isEnabled()) {
                    ToolValidationResult result = executeRule(rule, data);
                    results.add(result);

                    if (!result.isValid()) {
                        errors.addAll(result.getErrors());
                    }
                    warnings.addAll(result.getWarnings());
                }
            }

            ToolValidationResult aggregatedResult = aggregateResults(results, errors, warnings);
            long validationTime = System.currentTimeMillis() - startTime;
            success = true;
            
            // Record validation completion metrics
            recordValidationMetrics("completion", success, validationTime);

            LOGGER.debug("Validation completed in {}ms with {} errors and {} warnings", validationTime, errors.size(),
                    warnings.size());

            return aggregatedResult;

        } catch (Exception e) {
            LOGGER.error("Validation failed", e);
            long validationTime = System.currentTimeMillis() - startTime;
            success = false;
            
            // Record validation failure metrics
            recordValidationMetrics("failure", success, validationTime);
            
            return ToolValidationResult.invalid(List.of("Validation engine error: " + e.getMessage()));
        }
    }

    @Override
    public Map<String, Object> getValidationStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        try {
            // statistics.put("totalValidations", totalValidations.get()); // Removed
            try {
                statistics.put("totalValidations", metricsService.getMetric("validation", "totalValidations"));
            } catch (Exception e) {
                LOGGER.warn("Failed to retrieve totalValidations metric: {}", e.getMessage());
                statistics.put("totalValidations", 0L); // Fallback
            }
            
            // statistics.put("totalValidationTimeMs", totalValidationTimeMs.get()); // Removed
            try {
                statistics.put("totalValidationTimeMs", metricsService.getMetric("validation", "totalValidationTimeMs"));
            } catch (Exception e) {
                LOGGER.warn("Failed to retrieve totalValidationTimeMs metric: {}", e.getMessage());
                statistics.put("totalValidationTimeMs", 0L); // Fallback
            }
            
            statistics.put("registeredRules", rules.size());

            // if (totalValidations.get() > 0) { // Removed
            //     statistics.put("averageValidationTimeMs", totalValidationTimeMs.get() / totalValidations.get());
            // } else {
            //     statistics.put("averageValidationTimeMs", 0L);
            // }
            if (statistics.get("totalValidations") != null && (long) statistics.get("totalValidations") > 0) {
                try {
                    statistics.put("averageValidationTimeMs", statistics.get("totalValidationTimeMs") / (long) statistics.get("totalValidations"));
                } catch (Exception e) {
                    LOGGER.warn("Failed to calculate averageValidationTimeMs: {}", e.getMessage());
                    statistics.put("averageValidationTimeMs", 0L); // Fallback
                }
            } else {
                statistics.put("averageValidationTimeMs", 0L);
            }

            // Collect rule-specific metrics directly from MetricsService
            Map<String, Object> ruleMetrics = new HashMap<>();
            for (String ruleId : rules.keySet()) {
                try {
                    MetricKey ruleKey = MetricKeys.custom("rule", Map.of("ruleId", ruleId), Set.of("counts", "latency"));
                    var snapshot = metricsService.getSnapshot(ruleKey, GenericMetricsSnapshot.class);
                    if (snapshot != null) {
                        Map<String, Object> metrics = new HashMap<>();
                        metrics.put("executionCount", snapshot.getLong("total"));
                        metrics.put("successCount", snapshot.getLong("success"));
                        metrics.put("failureCount", snapshot.getLong("failure"));
                        metrics.put("totalExecutionTimeMs", snapshot.getLong("totalDurationNanos") / 1_000_000);
                        metrics.put("averageExecutionTimeMs", snapshot.getAverageMs());
                        metrics.put("successRate", snapshot.getSuccessRate());
                        ruleMetrics.put(ruleId, metrics);
                    }
                } catch (Exception e) {
                    LOGGER.warn("Failed to get metrics for rule {}: {}", ruleId, e.getMessage());
                    // Graceful degradation: continue with statistics collection even if metrics retrieval fails
                }
            }
            statistics.put("rulePerformanceMetrics", ruleMetrics);
        } catch (Exception e) {
            LOGGER.warn("Failed to retrieve validation statistics: {}", e.getMessage());
            // Graceful degradation: fallback to basic statistics
            statistics.put("totalValidations", 0L);
            statistics.put("totalValidationTimeMs", 0L);
            statistics.put("registeredRules", rules.size());
            statistics.put("averageValidationTimeMs", 0L);
            statistics.put("rulePerformanceMetrics", new HashMap<>());
        }

        return statistics;
    }

    @Override
    public void setRuleEnabled(String ruleId, boolean enabled) {
        ValidationRule rule = getRule(ruleId);
        if (rule != null) {
            // Record rule state change metrics
            MetricsService metrics = metricsService;
            if (metrics != null) {
                try {
                    metrics.recordOperation("validation", "rule_state_change")
                        .withSuccess(true)
                        .withDuration(0L)
                        .withData("ruleId", ruleId)
                        .withData("enabled", enabled)
                        .record();
                } catch (Exception e) {
                    LOGGER.warn("Failed to record rule state change metrics for rule {}: {}", ruleId, e.getMessage());
                    // Graceful degradation: continue with rule state change even if metrics recording fails
                }
            }
            
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
                // Record missing rule metrics
                MetricsService metrics = metricsService;
                if (metrics != null) {
                    try {
                        metrics.recordOperation("validation", "missing_rule")
                            .withSuccess(false)
                            .withDuration(0L)
                            .withData("ruleId", ruleId)
                            .record();
                    } catch (Exception e) {
                        LOGGER.warn("Failed to record missing rule metrics for rule {}: {}", ruleId, e.getMessage());
                        // Graceful degradation: continue with rule lookup even if metrics recording fails
                    }
                }
                
                LOGGER.warn("Validation rule not found: {}", ruleId);
            }
        }
        return selectedRules;
    }

    private ToolValidationResult executeRule(ValidationRule rule, Map<String, Object> data) {
        long startTime = System.currentTimeMillis();

        try {
            ToolValidationResult result = rule.validate(data);
            long executionTime = System.currentTimeMillis() - startTime;

            // Record metrics directly with rule-specific key using builder pattern
            MetricsService metrics = metricsService;
            if (metrics != null) {
                try {
                    metrics.recordOperation("rule", "execution")
                        .withSuccess(result.isValid())
                        .withDuration(Duration.ofMillis(executionTime).toNanos())
                        .withData("ruleId", rule.getRuleId())
                        .withData("executionTimeMs", executionTime)
                        .withData("validationErrors", result.getErrors().size())
                        .withData("validationWarnings", result.getWarnings().size())
                        .record();
                } catch (Exception e) {
                    LOGGER.warn("Failed to record metrics for rule {}: {}", rule.getRuleId(), e.getMessage());
                    // Graceful degradation: continue with rule execution even if metrics recording fails
                }
            }

            LOGGER.debug("Rule {} executed in {}ms with result: {}", rule.getRuleId(), executionTime,
                    result.isValid() ? "valid" : "invalid");

            return result;

        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            
            // Record failure metrics directly using builder pattern
            MetricsService metrics = metricsService;
            if (metrics != null) {
                try {
                    metrics.recordOperation("rule", "execution")
                        .withSuccess(false)
                        .withDuration(Duration.ofMillis(executionTime).toNanos())
                        .withData("ruleId", rule.getRuleId())
                        .withData("executionTimeMs", executionTime)
                        .withData("exceptionType", e.getClass().getSimpleName())
                        .withData("errorMessage", e.getMessage() != null ? e.getMessage() : "Unknown error")
                        .record();
                } catch (Exception ex) {
                    LOGGER.warn("Failed to record failure metrics for rule {}: {}", rule.getRuleId(), ex.getMessage());
                    // Graceful degradation: continue with rule execution even if metrics recording fails
                }
            }

            LOGGER.error("Rule {} execution failed", rule.getRuleId(), e);
            return ToolValidationResult.invalid(List.of("Rule execution failed: " + e.getMessage()));
        }
    }

    /**
     * Record validation metrics with error handling.
     * 
     * @param validationType the type of validation being performed
     * @param success whether the validation was successful
     * @param durationMs the duration in milliseconds
     */
    private void recordValidationMetrics(String validationType, boolean success, long durationMs) {
        MetricsService metrics = metricsService;
        if (metrics != null) {
            try {
                metrics.recordOperation("validation", validationType)
                    .withSuccess(success)
                    .withDuration(Duration.ofMillis(durationMs).toNanos())
                    .withData("validationType", validationType)
                    .withData("durationMs", durationMs)
                    .record();
            } catch (Exception e) {
                LOGGER.warn("Failed to record validation metrics for type {}: {}", validationType, e.getMessage());
                // Graceful degradation: continue with validation even if metrics recording fails
            }
        }
    }

    private ToolValidationResult aggregateResults(List<ToolValidationResult> results, List<String> errors,
            List<String> warnings) {
        boolean isValid = errors.isEmpty();
        String message = isValid ? "Validation passed" : "Validation failed with " + errors.size() + " errors";

        // Record aggregation metrics
        MetricsService metrics = metricsService;
        if (metrics != null) {
            try {
                metrics.recordOperation("validation", "aggregation")
                    .withSuccess(isValid)
                    .withDuration(0L)
                    .withData("totalRules", results.size())
                    .withData("passedRules", (int) results.stream().filter(ToolValidationResult::isValid).count())
                    .withData("errorCount", errors.size())
                    .withData("warningCount", warnings.size())
                    .record();
            } catch (Exception e) {
                LOGGER.warn("Failed to record aggregation metrics: {}", e.getMessage());
                // Graceful degradation: continue with aggregation even if metrics recording fails
            }
        }

        return new ToolValidationResult(isValid, errors, warnings, Map.of("totalRules", results.size(), "passedRules",
                (int) results.stream().filter(ToolValidationResult::isValid).count()), Instant.now());
    }
}
