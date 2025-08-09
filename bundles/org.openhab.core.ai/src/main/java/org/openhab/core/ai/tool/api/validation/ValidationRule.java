package org.openhab.core.ai.tool.api.validation;

import java.util.Map;

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

    // TODO: Implement validation rule lifecycle management
    // TODO: Add support for rule dependencies
    // TODO: Implement rule performance monitoring
    // TODO: Add support for rule versioning
}
