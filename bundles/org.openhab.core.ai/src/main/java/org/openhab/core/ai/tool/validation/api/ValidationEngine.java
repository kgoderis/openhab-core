package org.openhab.core.ai.tool.validation.api;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

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

    // DefaultValidationEngine extracted to top-level org.openhab.core.ai.tool.api.validation.DefaultValidationEngine
}
