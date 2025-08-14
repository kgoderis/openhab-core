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
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Agent Model Prompt Validator for prompt validation and safety checks.
 * 
 * This class provides comprehensive prompt validation capabilities,
 * including safety checks, content validation, and security scanning.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@Component(service = AgentModelPromptValidator.class)
@NonNullByDefault
public class AgentModelPromptValidator {

    private static final Logger logger = LoggerFactory.getLogger(AgentModelPromptValidator.class);

    // Safety patterns
    private static final Pattern PROMPT_INJECTION_PATTERNS = Pattern.compile(
            "\\b(ignore|forget|disregard|skip|bypass|override|previous|instructions|prompt|system)\\b",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern SENSITIVE_INFO_PATTERNS = Pattern.compile(
            "\\b(password|secret|key|token|api[_-]?key|auth[_-]?token|private[_-]?key)\\b", Pattern.CASE_INSENSITIVE);

    private static final Pattern MALICIOUS_PATTERNS = Pattern.compile(
            "\\b(delete|remove|destroy|corrupt|hack|exploit|vulnerability|backdoor|malware|virus)\\b",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern PERSONAL_INFO_PATTERNS = Pattern.compile(
            "\\b(ssn|social[_-]?security|credit[_-]?card|bank[_-]?account|phone[_-]?number|address|email)\\b",
            Pattern.CASE_INSENSITIVE);

    private final Map<String, PromptValidationRule> validationRules = new ConcurrentHashMap<>();

    /**
     * Validate a prompt for safety and compliance.
     * 
     * @param prompt The prompt to validate
     * @return Validation result with safety issues and recommendations
     */
    public PromptSafetyResult validateSafety(AgentModelPrompt prompt) {
        logger.debug("Validating prompt safety: {}",
                prompt.getPromptText().substring(0, Math.min(100, prompt.getPromptText().length())));

        PromptSafetyResult result = new PromptSafetyResult();

        // Check for prompt injection attempts
        validatePromptInjection(prompt, result);

        // Check for sensitive information
        validateSensitiveInformation(prompt, result);

        // Check for malicious content
        validateMaliciousContent(prompt, result);

        // Check for personal information
        validatePersonalInformation(prompt, result);

        // Check for inappropriate content
        validateInappropriateContent(prompt, result);

        // Apply custom validation rules
        applyCustomValidationRules(prompt, result);

        logger.debug("Prompt safety validation completed with {} issues", result.getSafetyIssues().size());
        return result;
    }

    /**
     * Validate prompt content quality.
     * 
     * @param prompt The prompt to validate
     * @return Validation result with quality issues and recommendations
     */
    public PromptQualityResult validateQuality(AgentModelPrompt prompt) {
        logger.debug("Validating prompt quality: {}",
                prompt.getPromptText().substring(0, Math.min(100, prompt.getPromptText().length())));

        PromptQualityResult result = new PromptQualityResult();

        // Check clarity
        validateClarity(prompt, result);

        // Check completeness
        validateCompleteness(prompt, result);

        // Check consistency
        validateConsistency(prompt, result);

        // Check relevance
        validateRelevance(prompt, result);

        // Apply custom quality rules
        applyCustomQualityRules(prompt, result);

        logger.debug("Prompt quality validation completed with {} issues", result.getQualityIssues().size());
        return result;
    }

    /**
     * Add a custom validation rule.
     * 
     * @param ruleName The name of the rule
     * @param rule The validation rule
     */
    public void addValidationRule(String ruleName, PromptValidationRule rule) {
        validationRules.put(ruleName, rule);
        logger.debug("Added validation rule: {}", ruleName);
    }

    /**
     * Check if a prompt is safe for use.
     * 
     * @param prompt The prompt to check
     * @return True if the prompt is safe, false otherwise
     */
    public boolean isSafe(AgentModelPrompt prompt) {
        PromptSafetyResult safetyResult = validateSafety(prompt);
        return safetyResult.getSafetyIssues().isEmpty();
    }

    /**
     * Check if a prompt meets quality standards.
     * 
     * @param prompt The prompt to check
     * @return True if the prompt meets quality standards, false otherwise
     */
    public boolean meetsQualityStandards(AgentModelPrompt prompt) {
        PromptQualityResult qualityResult = validateQuality(prompt);
        return qualityResult.getQualityIssues().isEmpty();
    }

    /**
     * Validate for prompt injection attempts.
     * 
     * @param prompt The prompt to validate
     * @param result The validation result to update
     */
    private void validatePromptInjection(AgentModelPrompt prompt, PromptSafetyResult result) {
        String text = prompt.getPromptText().toLowerCase();

        // Check for common prompt injection patterns
        if (text.contains("ignore previous instructions") || text.contains("ignore all previous")) {
            result.addSafetyIssue("Potential prompt injection detected: ignore previous instructions");
        }

        if (text.contains("system prompt") && (text.contains("override") || text.contains("ignore"))) {
            result.addSafetyIssue("Potential system prompt override detected");
        }

        if (text.contains("forget everything") || text.contains("start over")) {
            result.addSafetyIssue("Potential prompt injection detected: forget everything");
        }

        if (text.contains("new instructions") && text.contains("ignore")) {
            result.addSafetyIssue("Potential prompt injection detected: new instructions");
        }

        // Check for role confusion attempts
        if (text.contains("you are now") && text.contains("ignore")) {
            result.addSafetyIssue("Potential role confusion attempt detected");
        }

        // Check for instruction manipulation
        if (PROMPT_INJECTION_PATTERNS.matcher(text).find()) {
            result.addSafetyWarning("Potential prompt manipulation detected");
        }
    }

    /**
     * Validate for sensitive information.
     * 
     * @param prompt The prompt to validate
     * @param result The validation result to update
     */
    private void validateSensitiveInformation(AgentModelPrompt prompt,
            PromptSafetyResult result) {
        String text = prompt.getPromptText();

        // Check for API keys, tokens, passwords
        if (SENSITIVE_INFO_PATTERNS.matcher(text).find()) {
            result.addSafetyIssue("Potential sensitive information detected in prompt");
        }

        // Check for base64 encoded data that might contain secrets
        if (text.matches(".*[A-Za-z0-9+/]{20,}={0,2}.*")) {
            result.addSafetyWarning("Potential encoded sensitive data detected");
        }

        // Check for UUID patterns that might be tokens
        if (text.matches(".*[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}.*")) {
            result.addSafetyWarning("Potential token/UUID detected in prompt");
        }
    }

    /**
     * Validate for malicious content.
     * 
     * @param prompt The prompt to validate
     * @param result The validation result to update
     */
    private void validateMaliciousContent(AgentModelPrompt prompt, PromptSafetyResult result) {
        String text = prompt.getPromptText().toLowerCase();

        // Check for destructive commands
        if (text.contains("delete all") || text.contains("remove everything")) {
            result.addSafetyIssue("Potential destructive command detected");
        }

        if (text.contains("corrupt") || text.contains("destroy")) {
            result.addSafetyIssue("Potential destructive action requested");
        }

        // Check for security exploitation attempts
        if (text.contains("hack") || text.contains("exploit") || text.contains("vulnerability")) {
            result.addSafetyIssue("Potential security exploitation attempt detected");
        }

        // Check for malicious software references
        if (text.contains("malware") || text.contains("virus") || text.contains("backdoor")) {
            result.addSafetyIssue("Potential malicious software reference detected");
        }

        // Check for general malicious patterns
        if (MALICIOUS_PATTERNS.matcher(text).find()) {
            result.addSafetyWarning("Potential malicious content detected");
        }
    }

    /**
     * Validate for personal information.
     * 
     * @param prompt The prompt to validate
     * @param result The validation result to update
     */
    private void validatePersonalInformation(AgentModelPrompt prompt,
            PromptSafetyResult result) {
        String text = prompt.getPromptText();

        // Check for personal identification information
        if (PERSONAL_INFO_PATTERNS.matcher(text).find()) {
            result.addSafetyIssue("Potential personal information detected in prompt");
        }

        // Check for email addresses
        if (text.matches(".*\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b.*")) {
            result.addSafetyWarning("Email address detected in prompt");
        }

        // Check for phone numbers
        if (text.matches(".*\\b\\d{3}[-.]?\\d{3}[-.]?\\d{4}\\b.*")) {
            result.addSafetyWarning("Phone number detected in prompt");
        }

        // Check for credit card numbers
        if (text.matches(".*\\b\\d{4}[- ]?\\d{4}[- ]?\\d{4}[- ]?\\d{4}\\b.*")) {
            result.addSafetyIssue("Potential credit card number detected in prompt");
        }
    }

    /**
     * Validate for inappropriate content.
     * 
     * @param prompt The prompt to validate
     * @param result The validation result to update
     */
    private void validateInappropriateContent(AgentModelPrompt prompt,
            PromptSafetyResult result) {
        String text = prompt.getPromptText().toLowerCase();

        // Check for offensive language
        String[] offensiveTerms = { "hate", "discriminate", "racist", "sexist", "homophobic", "transphobic", "violent",
                "threat", "kill", "harm", "hurt", "abuse" };

        for (String term : offensiveTerms) {
            if (text.contains(term)) {
                result.addSafetyWarning("Potential inappropriate content detected: " + term);
            }
        }

        // Check for adult content references
        if (text.contains("adult") || text.contains("porn") || text.contains("explicit")) {
            result.addSafetyIssue("Potential adult content reference detected");
        }
    }

    /**
     * Validate prompt clarity.
     * 
     * @param prompt The prompt to validate
     * @param result The validation result to update
     */
    private void validateClarity(AgentModelPrompt prompt, PromptQualityResult result) {
        String text = prompt.getPromptText();

        // Check for ambiguous language
        if (text.contains("maybe") || text.contains("possibly") || text.contains("perhaps")) {
            result.addQualityIssue("Ambiguous language detected in prompt");
        }

        // Check for vague instructions
        if (text.contains("do something") || text.contains("handle it") || text.contains("figure it out")) {
            result.addQualityIssue("Vague instructions detected in prompt");
        }

        // Check for run-on sentences
        String[] sentences = text.split("[.!?]");
        for (String sentence : sentences) {
            if (sentence.split("\\s+").length > 50) {
                result.addQualityWarning("Very long sentence detected, may affect clarity");
            }
        }
    }

    /**
     * Validate prompt completeness.
     * 
     * @param prompt The prompt to validate
     * @param result The validation result to update
     */
    private void validateCompleteness(AgentModelPrompt prompt, PromptQualityResult result) {
        String text = prompt.getPromptText();

        // Check for missing essential components
        if (!text.contains("System:") && !text.contains("Task:")) {
            result.addQualityIssue("Missing system role or task definition");
        }

        if (!text.contains("Expected Output:") && !text.contains("Output:")) {
            result.addQualityIssue("Missing expected output specification");
        }

        // Check for incomplete instructions
        if (text.contains("TODO") || text.contains("FIXME") || text.contains("TBD")) {
            result.addQualityIssue("Incomplete instructions detected in prompt");
        }
    }

    /**
     * Validate prompt consistency.
     * 
     * @param prompt The prompt to validate
     * @param result The validation result to update
     */
    private void validateConsistency(AgentModelPrompt prompt, PromptQualityResult result) {
        String text = prompt.getPromptText();

        // Check for contradictory instructions
        if (text.contains("do not") && text.contains("must")) {
            result.addQualityWarning("Potential contradictory instructions detected");
        }

        if (text.contains("always") && text.contains("never")) {
            result.addQualityWarning("Potential contradictory instructions detected");
        }

        // Check for inconsistent formatting
        if (text.contains("System:") && !text.contains("Task:")) {
            result.addQualityWarning("Inconsistent prompt structure detected");
        }
    }

    /**
     * Validate prompt relevance.
     * 
     * @param prompt The prompt to validate
     * @param result The validation result to update
     */
    private void validateRelevance(AgentModelPrompt prompt, PromptQualityResult result) {
        String text = prompt.getPromptText();

        // Check for off-topic content
        if (text.contains("unrelated") || text.contains("irrelevant")) {
            result.addQualityWarning("Potential off-topic content detected");
        }

        // Check for context mismatch
        String agentType = (String) prompt.getMetadata("agentType");
        if (agentType != null) {
            if ("energy".equals(agentType) && text.contains("security")) {
                result.addQualityWarning("Potential context mismatch: energy agent with security content");
            }
            if ("security".equals(agentType) && text.contains("energy")) {
                result.addQualityWarning("Potential context mismatch: security agent with energy content");
            }
        }
    }

    /**
     * Apply custom validation rules.
     * 
     * @param prompt The prompt to validate
     * @param result The validation result to update
     */
    private void applyCustomValidationRules(AgentModelPrompt prompt,
            PromptSafetyResult result) {
        for (Map.Entry<String, PromptValidationRule> entry : validationRules.entrySet()) {
            try {
                PromptValidationRule rule = entry.getValue();
                List<String> ruleIssues = rule.validate(prompt);
                for (String issue : ruleIssues) {
                    result.addSafetyIssue("Rule '" + entry.getKey() + "': " + issue);
                }
            } catch (Exception e) {
                logger.error("Error applying validation rule: {}", entry.getKey(), e);
                result.addSafetyIssue("Error applying validation rule: " + entry.getKey());
            }
        }
    }

    /**
     * Apply custom quality rules.
     * 
     * @param prompt The prompt to validate
     * @param result The validation result to update
     */
    private void applyCustomQualityRules(AgentModelPrompt prompt, PromptQualityResult result) {
        // Custom quality rules can be added here
        // For now, we'll implement basic checks
    }

    // PromptSafetyResult extracted to org.openhab.core.ai.reasoning.PromptSafetyResult

    // PromptQualityResult extracted to org.openhab.core.ai.reasoning.PromptQualityResult

    // ValidationRule extracted to top-level: org.openhab.core.ai.reasoning.PromptValidationRule
}
