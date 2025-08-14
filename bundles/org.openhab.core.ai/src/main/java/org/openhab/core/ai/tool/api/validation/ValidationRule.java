package org.openhab.core.ai.tool.api.validation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Base validation rule interface for tool validation.
 * 
 * This interface defines the contract for validation rules that can be applied
 * to tool configurations, parameters, and schemas.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface ValidationRule {

    /**
     * Get the rule ID.
     * 
     * @return the rule ID
     */
    String getRuleId();

    /**
     * Get the rule name.
     * 
     * @return the rule name
     */
    String getRuleName();

    /**
     * Get the rule description.
     * 
     * @return the rule description
     */
    String getRuleDescription();

    /**
     * Get the rule priority.
     * 
     * @return the rule priority (higher values = higher priority)
     */
    int getPriority();

    /**
     * Check if the rule is enabled.
     * 
     * @return true if the rule is enabled
     */
    boolean isEnabled();

    /**
     * Validate the given data against this rule.
     * 
     * @param data the data to validate
     * @return validation result
     */
    ValidationResult validate(Map<String, Object> data);

    /**
     * Get the rule configuration.
     * 
     * @return the rule configuration
     */
    Map<String, Object> getConfiguration();

    /**
     * Get rule dependencies.
     * 
     * @return list of rule IDs that this rule depends on
     */
    List<String> getDependencies();

    /**
     * Check if all dependencies are satisfied.
     * 
     * @param completedRules list of completed rule IDs
     * @return true if all dependencies are satisfied
     */
    boolean areDependenciesSatisfied(List<String> completedRules);

    /**
     * Get rule performance metrics.
     * 
     * @return performance metrics
     */
    Map<String, Object> getPerformanceMetrics();

    /**
     * Get rule version.
     * 
     * @return rule version
     */
    String getVersion();

    /**
     * Check if rule version is compatible.
     * 
     * @param requiredVersion required version
     * @return true if compatible
     */
    boolean isVersionCompatible(String requiredVersion);

    /**
     * Get rule lifecycle state.
     * 
     * @return lifecycle state
     */
    RuleLifecycleState getLifecycleState();

    /**
     * Set rule lifecycle state.
     * 
     * @param state the new lifecycle state
     */
    void setLifecycleState(RuleLifecycleState state);

    /**
     * Abstract base implementation of ValidationRule.
     * 
     * @author Karel Goderis - Initial Contribution
     * @since 1.0.0
     */
    abstract class AbstractValidationRule implements ValidationRule {

        protected final String ruleId;
        protected final String ruleName;
        protected final String ruleDescription;
        protected final int priority;
        protected final String version;
        protected final List<String> dependencies;
        protected final Map<String, Object> configuration;
        protected final AtomicLong executionCount = new AtomicLong(0);
        protected final AtomicLong totalExecutionTimeMs = new AtomicLong(0);
        protected final AtomicLong lastExecutionTimeMs = new AtomicLong(0);
        protected final AtomicLong successCount = new AtomicLong(0);
        protected final AtomicLong failureCount = new AtomicLong(0);
        protected boolean enabled = true;
        protected RuleLifecycleState lifecycleState = RuleLifecycleState.ACTIVE;

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
        public ValidationResult validate(Map<String, Object> data) {
            long startTime = System.currentTimeMillis();
            executionCount.incrementAndGet();

            try {
                // Check lifecycle state
                if (lifecycleState != RuleLifecycleState.ACTIVE) {
                    return ValidationResult.invalid(List.of("Rule is not in active state: " + lifecycleState));
                }

                // Execute the actual validation logic
                ValidationResult result = executeValidation(data);
                long executionTime = System.currentTimeMillis() - startTime;
                lastExecutionTimeMs.set(executionTime);
                totalExecutionTimeMs.addAndGet(executionTime);

                if (result.isValid()) {
                    successCount.incrementAndGet();
                } else {
                    failureCount.incrementAndGet();
                }

                return result;
            } catch (Exception e) {
                long executionTime = System.currentTimeMillis() - startTime;
                lastExecutionTimeMs.set(executionTime);
                totalExecutionTimeMs.addAndGet(executionTime);
                failureCount.incrementAndGet();
                return ValidationResult.invalid(List.of("Rule execution failed: " + e.getMessage()));
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
            long totalExecutions = executionCount.get();

            metrics.put("executionCount", totalExecutions);
            metrics.put("totalExecutionTimeMs", totalExecutionTimeMs.get());
            metrics.put("lastExecutionTimeMs", lastExecutionTimeMs.get());
            metrics.put("successCount", successCount.get());
            metrics.put("failureCount", failureCount.get());
            metrics.put("lifecycleState", lifecycleState.name());

            if (totalExecutions > 0) {
                metrics.put("averageExecutionTimeMs", totalExecutionTimeMs.get() / totalExecutions);
                metrics.put("successRate", (double) successCount.get() / totalExecutions);
            } else {
                metrics.put("averageExecutionTimeMs", 0L);
                metrics.put("successRate", 0.0);
            }

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
        protected abstract ValidationResult executeValidation(Map<String, Object> data);
    }

    /**
     * Rule lifecycle states.
     */
    // enum extracted to top-level: org.openhab.core.ai.tool.api.validation.RuleLifecycleState
}
