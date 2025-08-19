package org.openhab.core.ai.tool.validation.api;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.validation.ToolValidationResult;

/**
 * Interface for validation rules that can be applied to tool data.
 * 
 * This interface defines the contract for validation rules that can be
 * registered with the ValidationRuleRegistry and applied to tool data
 * for validation purposes.
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
     * @return the rule priority
     */
    int getPriority();

    /**
     * Check if the rule is enabled.
     * 
     * @return true if enabled
     */
    boolean isEnabled();

    /**
     * Validate data against this rule.
     * 
     * @param data the data to validate
     * @return validation result
     */
    ToolValidationResult validate(Map<String, Object> data);

    /**
     * Get the rule configuration.
     * 
     * @return the configuration
     */
    Map<String, Object> getConfiguration();

    /**
     * Get the rule dependencies.
     * 
     * @return the dependencies
     */
    List<String> getDependencies();

    /**
     * Check if dependencies are satisfied.
     * 
     * @param completedRules list of completed rule IDs
     * @return true if dependencies are satisfied
     */
    boolean areDependenciesSatisfied(List<String> completedRules);

    /**
     * Get performance metrics.
     * 
     * @return performance metrics
     */
    Map<String, Object> getPerformanceMetrics();

    /**
     * Get the rule version.
     * 
     * @return the version
     */
    String getVersion();

    /**
     * Check if the rule version is compatible.
     * 
     * @param requiredVersion the required version
     * @return true if compatible
     */
    boolean isVersionCompatible(String requiredVersion);

    /**
     * Get the lifecycle state.
     * 
     * @return the lifecycle state
     */
    RuleLifecycleState getLifecycleState();

    /**
     * Set the lifecycle state.
     * 
     * @param state the new lifecycle state
     */
    void setLifecycleState(RuleLifecycleState state);

    /**
     * Rule lifecycle states.
     */
    // enum extracted to top-level: org.openhab.core.ai.tool.api.validation.RuleLifecycleState
}
