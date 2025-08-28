package org.openhab.core.ai.tool.validation.base;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
import org.openhab.core.ai.common.validation.ToolValidationResult;
import org.openhab.core.ai.tool.validation.api.RuleLifecycleState;
import org.openhab.core.ai.tool.validation.api.ValidationRule;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

/**
 * Abstract base implementation of ValidationRule.
 * 
 * This class provides a foundation for validation rule implementations with
 * common functionality including lifecycle management, performance tracking,
 * and dependency handling.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public abstract class AbstractValidationRule implements ValidationRule {

    protected final String ruleId;
    protected final String ruleName;
    protected final String ruleDescription;
    protected final int priority;
    protected final String version;
    protected final List<String> dependencies;
    protected final Map<String, Object> configuration;
    // Performance monitoring - migrated to MetricsService
    // protected final AtomicLong executionCount = new AtomicLong(0);
    // protected final AtomicLong totalExecutionTimeMs = new AtomicLong(0);
    // protected final AtomicLong lastExecutionTimeMs = new AtomicLong(0);
    // protected final AtomicLong successCount = new AtomicLong(0);
    // protected final AtomicLong failureCount = new AtomicLong(0);
    protected boolean enabled = true;
    protected RuleLifecycleState lifecycleState = RuleLifecycleState.ACTIVE;

    private MetricsService metricsService;

    @Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
    protected void setMetricsService(MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    /**
     * Create a new abstract validation rule.
     * 
     * @param ruleId the rule ID
     * @param ruleName the rule name
     * @param ruleDescription the rule description
     * @param priority the rule priority
     * @param version the rule version
     */
    protected AbstractValidationRule(String ruleId, String ruleName, String ruleDescription, int priority,
            String version) {
        this.ruleId = ruleId;
        this.ruleName = ruleName;
        this.ruleDescription = ruleDescription;
        this.priority = priority;
        this.version = version;
        this.dependencies = new ArrayList<>();
        this.configuration = new HashMap<>();
    }

    @Override
    public String getRuleId() {
        return ruleId;
    }

    @Override
    public String getRuleName() {
        return ruleName;
    }

    @Override
    public String getRuleDescription() {
        return ruleDescription;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public ToolValidationResult validate(Map<String, Object> data) {
        long startTime = System.currentTimeMillis();
        metricsService.recordOperation("validation_rule", "execution").withSuccess(true).withDuration(0L)
                .withData("ruleId", ruleId).withData("ruleType", this.getClass().getSimpleName()).record();

        try {
            // Check lifecycle state
            if (lifecycleState != RuleLifecycleState.ACTIVE) {
                return ToolValidationResult.invalid(List.of("Rule is not in active state: " + lifecycleState));
            }

            // Check if rule is enabled
            if (!enabled) {
                return ToolValidationResult.invalid(List.of("Rule is disabled"));
            }

            // Execute the actual validation logic
            ToolValidationResult result = executeValidation(data);
            long executionTime = System.currentTimeMillis() - startTime;

            if (result.isValid()) {
                metricsService.recordOperation("validation_rule", "success").withSuccess(true)
                        .withDuration(Duration.ofMillis(executionTime).toNanos()).withData("ruleId", ruleId)
                        .withData("executionTimeMs", executionTime)
                        .withData("validationErrors", result.getErrors().size()).record();
            } else {
                metricsService.recordOperation("validation_rule", "failure").withSuccess(false)
                        .withDuration(Duration.ofMillis(executionTime).toNanos()).withData("ruleId", ruleId)
                        .withData("executionTimeMs", executionTime)
                        .withData("validationErrors", result.getErrors().size())
                        .withData("errorDetails", result.getErrors().toString()).record();
            }

            return result;
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            metricsService.recordOperation("validation_rule", "error").withSuccess(false)
                    .withDuration(Duration.ofMillis(executionTime).toNanos()).withData("ruleId", ruleId)
                    .withData("executionTimeMs", executionTime).withData("exceptionType", e.getClass().getSimpleName())
                    .withData("errorMessage", e.getMessage() != null ? e.getMessage() : "Unknown error").record();
            return ToolValidationResult.invalid(List.of("Rule execution failed: " + e.getMessage()));
        }
    }

    @Override
    public Map<String, Object> getConfiguration() {
        return new HashMap<>(configuration);
    }

    @Override
    public List<String> getDependencies() {
        return new ArrayList<>(dependencies);
    }

    @Override
    public boolean areDependenciesSatisfied(List<String> completedRules) {
        return completedRules.containsAll(dependencies);
    }

    @Override
    public Map<String, Object> getPerformanceMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        // long totalExecutions = executionCount.get(); // Removed AtomicLong

        // metrics.put("executionCount", totalExecutions); // Removed AtomicLong
        // metrics.put("totalExecutionTimeMs", totalExecutionTimeMs.get()); // Removed AtomicLong
        // metrics.put("lastExecutionTimeMs", lastExecutionTimeMs.get()); // Removed AtomicLong
        // metrics.put("successCount", successCount.get()); // Removed AtomicLong
        // metrics.put("failureCount", failureCount.get()); // Removed AtomicLong
        metrics.put("lifecycleState", lifecycleState.name());

        // if (totalExecutions > 0) { // Removed AtomicLong
        // metrics.put("averageExecutionTimeMs", totalExecutionTimeMs.get() / totalExecutions); // Removed AtomicLong
        // metrics.put("successRate", (double) successCount.get() / totalExecutions); // Removed AtomicLong
        // } else { // Removed AtomicLong
        // metrics.put("averageExecutionTimeMs", 0L); // Removed AtomicLong
        // metrics.put("successRate", 0.0); // Removed AtomicLong
        // }

        return metrics;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public boolean isVersionCompatible(String requiredVersion) {
        // Simple version compatibility check
        // In a real implementation, you would use proper semantic versioning
        return version.equals(requiredVersion) || version.startsWith(requiredVersion);
    }

    @Override
    public RuleLifecycleState getLifecycleState() {
        return lifecycleState;
    }

    @Override
    public void setLifecycleState(RuleLifecycleState state) {
        this.lifecycleState = state;
    }

    /**
     * Add a dependency to this rule.
     * 
     * @param dependencyRuleId the rule ID this rule depends on
     */
    public void addDependency(String dependencyRuleId) {
        if (!dependencies.contains(dependencyRuleId)) {
            dependencies.add(dependencyRuleId);
        }
    }

    /**
     * Remove a dependency from this rule.
     * 
     * @param dependencyRuleId the rule ID to remove as dependency
     */
    public void removeDependency(String dependencyRuleId) {
        dependencies.remove(dependencyRuleId);
    }

    /**
     * Set the enabled state of this rule.
     * 
     * @param enabled whether the rule is enabled
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Update the rule configuration.
     * 
     * @param configuration the new configuration
     */
    public void updateConfiguration(Map<String, Object> configuration) {
        this.configuration.clear();
        this.configuration.putAll(configuration);
    }

    /**
     * Execute the actual validation logic.
     * 
     * @param data the data to validate
     * @return validation result
     */
    protected abstract ToolValidationResult executeValidation(Map<String, Object> data);
}
