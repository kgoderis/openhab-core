/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.core.ai.reasoning;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Agent Model Context Validator for context validation and optimization.
 * 
 * This class provides comprehensive validation capabilities for agent contexts,
 * including data quality checks, consistency validation, and optimization
 * recommendations.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@Component(service = AgentModelContextValidator.class)
@NonNullByDefault
public class AgentModelContextValidator {

    private static final Logger logger = LoggerFactory.getLogger(AgentModelContextValidator.class);

    private final Map<String, ValidationRule> validationRules = new ConcurrentHashMap<>();
    private final Map<String, OptimizationRule> optimizationRules = new ConcurrentHashMap<>();

    /**
     * Validate an agent model context for completeness and quality.
     * 
     * @param context The context to validate
     * @return Validation result with issues and recommendations
     */
    public ContextValidationResult validate(AgentModelContextBuilder.AgentModelContext context) {
        logger.debug("Validating context: {}", context.getContextId());

        ContextValidationResult result = new ContextValidationResult();

        // Check required fields
        validateRequiredFields(context, result);

        // Check data quality
        validateDataQuality(context, result);

        // Check consistency
        validateConsistency(context, result);

        // Check completeness
        validateCompleteness(context, result);

        // Apply custom validation rules
        applyCustomValidationRules(context, result);

        logger.debug("Context validation completed with {} issues", result.getIssues().size());
        return result;
    }

    /**
     * Optimize an agent model context for better performance and accuracy.
     * 
     * @param context The context to optimize
     * @return Optimization result with recommendations
     */
    public ContextOptimizationResult optimize(AgentModelContextBuilder.AgentModelContext context) {
        logger.debug("Optimizing context: {}", context.getContextId());

        ContextOptimizationResult result = new ContextOptimizationResult();

        // Optimize data structure
        optimizeDataStructure(context, result);

        // Optimize metadata
        optimizeMetadata(context, result);

        // Apply custom optimization rules
        applyCustomOptimizationRules(context, result);

        logger.debug("Context optimization completed with {} recommendations", result.getRecommendations().size());
        return result;
    }

    /**
     * Add a custom validation rule.
     * 
     * @param ruleName The name of the rule
     * @param rule The validation rule
     */
    public void addValidationRule(String ruleName, ValidationRule rule) {
        validationRules.put(ruleName, rule);
        logger.debug("Added validation rule: {}", ruleName);
    }

    /**
     * Add a custom optimization rule.
     * 
     * @param ruleName The name of the rule
     * @param rule The optimization rule
     */
    public void addOptimizationRule(String ruleName, OptimizationRule rule) {
        optimizationRules.put(ruleName, rule);
        logger.debug("Added optimization rule: {}", ruleName);
    }

    /**
     * Validate required fields in the context.
     * 
     * @param context The context to validate
     * @param result The validation result to update
     */
    private void validateRequiredFields(AgentModelContextBuilder.AgentModelContext context,
            ContextValidationResult result) {
        if (!context.hasContextData("agentId")) {
            result.addIssue("Missing required field: agentId");
        }

        if (!context.hasContextData("agentType")) {
            result.addIssue("Missing required field: agentType");
        }

        if (!context.hasContextData("domain")) {
            result.addIssue("Missing required field: domain");
        }

        // Validate agent ID format
        Object agentId = context.getContextData("agentId");
        if (agentId != null && !(agentId instanceof String)) {
            result.addIssue("Agent ID must be a string");
        }

        // Validate agent type format
        Object agentType = context.getContextData("agentType");
        if (agentType != null && !(agentType instanceof String)) {
            result.addIssue("Agent type must be a string");
        }
    }

    /**
     * Validate data quality in the context.
     * 
     * @param context The context to validate
     * @param result The validation result to update
     */
    private void validateDataQuality(AgentModelContextBuilder.AgentModelContext context,
            ContextValidationResult result) {
        // Check for null or empty values
        for (Map.Entry<String, Object> entry : context.getContextData().entrySet()) {
            if (entry.getValue() == null) {
                result.addIssue("Null value found for field: " + entry.getKey());
            }
        }

        // Check for empty strings
        for (Map.Entry<String, Object> entry : context.getContextData().entrySet()) {
            if (entry.getValue() instanceof String && ((String) entry.getValue()).trim().isEmpty()) {
                result.addIssue("Empty string found for field: " + entry.getKey());
            }
        }

        // Check for oversized data
        for (Map.Entry<String, Object> entry : context.getContextData().entrySet()) {
            if (entry.getValue() instanceof String && ((String) entry.getValue()).length() > 10000) {
                result.addIssue("Oversized string found for field: " + entry.getKey() + " (length: "
                        + ((String) entry.getValue()).length() + ")");
            }
        }
    }

    /**
     * Validate consistency in the context.
     * 
     * @param context The context to validate
     * @param result The validation result to update
     */
    private void validateConsistency(AgentModelContextBuilder.AgentModelContext context,
            ContextValidationResult result) {
        String agentType = (String) context.getContextData("agentType");
        String domain = (String) context.getContextData("domain");

        // Check agent type and domain consistency
        if (agentType != null && domain != null) {
            if ("energy".equals(agentType) && !"energy_management".equals(domain)) {
                result.addIssue("Energy agent should have energy_management domain");
            }
            if ("security".equals(agentType) && !"security_management".equals(domain)) {
                result.addIssue("Security agent should have security_management domain");
            }
            if ("comfort".equals(agentType) && !"comfort_management".equals(domain)) {
                result.addIssue("Comfort agent should have comfort_management domain");
            }
        }

        // Check capabilities consistency
        Object capabilities = context.getContextData("capabilities");
        if (capabilities instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> caps = (Map<String, Object>) capabilities;
            if (caps.containsKey("reasoning") && !(caps.get("reasoning") instanceof Boolean)) {
                result.addIssue("Capability 'reasoning' should be a boolean value");
            }
        }
    }

    /**
     * Validate completeness in the context.
     * 
     * @param context The context to validate
     * @param result The validation result to update
     */
    private void validateCompleteness(AgentModelContextBuilder.AgentModelContext context,
            ContextValidationResult result) {
        // Check for essential capabilities
        if (!context.hasContextData("capabilities")) {
            result.addIssue("Missing capabilities information");
        }

        // Check for current state
        if (!context.hasContextData("currentState")) {
            result.addIssue("Missing current state information");
        }

        // Check for user preferences
        if (!context.hasContextData("userPreferences")) {
            result.addIssue("Missing user preferences information");
        }
    }

    /**
     * Apply custom validation rules.
     * 
     * @param context The context to validate
     * @param result The validation result to update
     */
    private void applyCustomValidationRules(AgentModelContextBuilder.AgentModelContext context,
            ContextValidationResult result) {
        for (Map.Entry<String, ValidationRule> entry : validationRules.entrySet()) {
            try {
                ValidationRule rule = entry.getValue();
                List<String> ruleIssues = rule.validate(context);
                for (String issue : ruleIssues) {
                    result.addIssue("Rule '" + entry.getKey() + "': " + issue);
                }
            } catch (Exception e) {
                logger.error("Error applying validation rule: {}", entry.getKey(), e);
                result.addIssue("Error applying validation rule: " + entry.getKey());
            }
        }
    }

    /**
     * Optimize data structure in the context.
     * 
     * @param context The context to optimize
     * @param result The optimization result to update
     */
    private void optimizeDataStructure(AgentModelContextBuilder.AgentModelContext context,
            ContextOptimizationResult result) {
        // Optimize map structures
        Map<String, Object> contextData = context.getContextData();
        if (contextData.size() > 50) {
            result.addRecommendation(
                    "Consider reducing context data size (current: " + contextData.size() + " entries)");
        }

        // Optimize string values
        for (Map.Entry<String, Object> entry : contextData.entrySet()) {
            if (entry.getValue() instanceof String) {
                String value = (String) entry.getValue();
                if (value.length() > 1000) {
                    result.addRecommendation("Consider truncating long string in field: " + entry.getKey());
                }
            }
        }
    }

    /**
     * Optimize metadata in the context.
     * 
     * @param context The context to optimize
     * @param result The optimization result to update
     */
    private void optimizeMetadata(AgentModelContextBuilder.AgentModelContext context,
            ContextOptimizationResult result) {
        Map<String, Object> metadata = context.getMetadata();

        // Check for missing priority
        if (!metadata.containsKey("priority")) {
            result.addRecommendation("Add priority level to context metadata");
        }

        // Check for missing timestamp
        if (!metadata.containsKey("timestamp")) {
            result.addRecommendation("Add timestamp to context metadata");
        }

        // Check for missing source
        if (!metadata.containsKey("source")) {
            result.addRecommendation("Add source information to context metadata");
        }
    }

    /**
     * Apply custom optimization rules.
     * 
     * @param context The context to optimize
     * @param result The optimization result to update
     */
    private void applyCustomOptimizationRules(AgentModelContextBuilder.AgentModelContext context,
            ContextOptimizationResult result) {
        for (Map.Entry<String, OptimizationRule> entry : optimizationRules.entrySet()) {
            try {
                OptimizationRule rule = entry.getValue();
                List<String> ruleRecommendations = rule.optimize(context);
                for (String recommendation : ruleRecommendations) {
                    result.addRecommendation("Rule '" + entry.getKey() + "': " + recommendation);
                }
            } catch (Exception e) {
                logger.error("Error applying optimization rule: {}", entry.getKey(), e);
                result.addRecommendation("Error applying optimization rule: " + entry.getKey());
            }
        }
    }

    /**
     * Context Validation Result class.
     */
    // ContextValidationResult extracted to org.openhab.core.ai.reasoning.ContextValidationResult

    /**
     * Context Optimization Result class.
     */
    // ContextOptimizationResult extracted to org.openhab.core.ai.reasoning.ContextOptimizationResult

    /**
     * Validation Rule interface.
     */
    @FunctionalInterface
    public interface ValidationRule {
        List<String> validate(AgentModelContextBuilder.AgentModelContext context);
    }

    /**
     * Optimization Rule interface.
     */
    @FunctionalInterface
    public interface OptimizationRule {
        List<String> optimize(AgentModelContextBuilder.AgentModelContext context);
    }
}
