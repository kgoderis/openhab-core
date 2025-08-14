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
 * Agent Model Prompt Optimizer for prompt optimization and validation.
 * 
 * This class provides comprehensive prompt optimization capabilities,
 * including length optimization, token estimation, and performance tuning.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@Component(service = AgentModelPromptOptimizer.class)
@NonNullByDefault
public class AgentModelPromptOptimizer {

    private static final Logger logger = LoggerFactory.getLogger(AgentModelPromptOptimizer.class);

    // Configuration constants
    private static final int MAX_PROMPT_LENGTH = 8000;
    private static final int OPTIMAL_PROMPT_LENGTH = 4000;
    private static final double TOKENS_PER_CHAR = 0.25; // Rough estimation
    private static final int MAX_TOKENS = 2000;

    private final Map<String, OptimizationRule> optimizationRules = new ConcurrentHashMap<>();

    /**
     * Optimize a prompt for better performance and accuracy.
     * 
     * @param prompt The prompt to optimize
     * @return The optimized prompt
     */
    public AgentModelPrompt optimize(AgentModelPrompt prompt) {
        logger.debug("Optimizing prompt with {} characters", prompt.getPromptText().length());

        String optimizedText = prompt.getPromptText();
        List<String> optimizations = new ArrayList<>();

        // Length optimization
        if (optimizedText.length() > MAX_PROMPT_LENGTH) {
            optimizedText = truncatePrompt(optimizedText);
            optimizations.add("Truncated prompt to fit length limits");
        }

        // Token optimization
        int estimatedTokens = estimateTokens(optimizedText);
        if (estimatedTokens > MAX_TOKENS) {
            optimizedText = reduceTokens(optimizedText);
            optimizations.add("Reduced tokens to fit model limits");
        }

        // Content optimization
        optimizedText = optimizeContent(optimizedText);
        if (!optimizedText.equals(prompt.getPromptText())) {
            optimizations.add("Optimized content structure");
        }

        // Apply custom optimization rules
        List<String> customOptimizations = applyCustomOptimizationRules(prompt);
        optimizations.addAll(customOptimizations);

        // Create optimized prompt
        AgentModelPromptBuilder optimizedBuilder = AgentModelPromptBuilder.create();
        optimizedBuilder.withSystemRole(extractSystemRole(optimizedText));
        optimizedBuilder.withTask(extractTask(optimizedText));
        optimizedBuilder.withExpectedOutput(extractExpectedOutput(optimizedText));
        optimizedBuilder.withType(prompt.getType());
        optimizedBuilder.withPriority(prompt.getPriority());
        optimizedBuilder.withVersion(prompt.getVersion());

        // Add optimization metadata
        optimizedBuilder.withMetadata("optimized", true);
        optimizedBuilder.withMetadata("originalLength", prompt.getPromptText().length());
        optimizedBuilder.withMetadata("optimizedLength", optimizedText.length());
        optimizedBuilder.withMetadata("optimizations", String.join(", ", optimizations));

        AgentModelPrompt optimizedPrompt = optimizedBuilder.build();

        logger.debug("Prompt optimization completed: {} -> {} characters, {} optimizations",
                prompt.getPromptText().length(), optimizedText.length(), optimizations.size());

        return optimizedPrompt;
    }

    /**
     * Validate a prompt for safety and quality.
     * 
     * @param prompt The prompt to validate
     * @return Validation result with issues and recommendations
     */
    public PromptValidationResult validate(AgentModelPrompt prompt) {
        logger.debug("Validating prompt: {}",
                prompt.getPromptText().substring(0, Math.min(100, prompt.getPromptText().length())));

        PromptValidationResult result = new PromptValidationResult();

        // Check length
        validateLength(prompt, result);

        // Check content safety
        validateContentSafety(prompt, result);

        // Check structure
        validateStructure(prompt, result);

        // Check token count
        validateTokenCount(prompt, result);

        // Apply custom validation rules
        applyCustomValidationRules(prompt, result);

        logger.debug("Prompt validation completed with {} issues", result.getIssues().size());
        return result;
    }

    /**
     * Estimate the number of tokens in a prompt.
     * 
     * @param promptText The prompt text
     * @return Estimated token count
     */
    public int estimateTokens(String promptText) {
        if (promptText == null || promptText.isEmpty()) {
            return 0;
        }

        // Simple token estimation (words + punctuation)
        String[] words = promptText.split("\\s+");
        int wordTokens = words.length;

        // Count punctuation tokens
        int punctuationTokens = (int) promptText.chars().mapToObj(ch -> (char) ch)
                .filter(ch -> ".,!?;:()[]{}\"'`".indexOf(ch) >= 0).count();

        return wordTokens + punctuationTokens;
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
     * Truncate a prompt to fit length limits.
     * 
     * @param promptText The prompt text to truncate
     * @return The truncated prompt text
     */
    private String truncatePrompt(String promptText) {
        if (promptText.length() <= MAX_PROMPT_LENGTH) {
            return promptText;
        }

        // Try to truncate at sentence boundaries
        String[] sentences = promptText.split("(?<=[.!?])\\s+");
        StringBuilder truncated = new StringBuilder();

        for (String sentence : sentences) {
            if (truncated.length() + sentence.length() + 1 <= MAX_PROMPT_LENGTH) {
                if (truncated.length() > 0) {
                    truncated.append(" ");
                }
                truncated.append(sentence);
            } else {
                break;
            }
        }

        // If still too long, truncate at word boundaries
        if (truncated.length() == 0) {
            String[] words = promptText.split("\\s+");
            for (String word : words) {
                if (truncated.length() + word.length() + 1 <= MAX_PROMPT_LENGTH) {
                    if (truncated.length() > 0) {
                        truncated.append(" ");
                    }
                    truncated.append(word);
                } else {
                    break;
                }
            }
        }

        return truncated.toString();
    }

    /**
     * Reduce tokens in a prompt.
     * 
     * @param promptText The prompt text to reduce
     * @return The reduced prompt text
     */
    private String reduceTokens(String promptText) {
        int currentTokens = estimateTokens(promptText);
        if (currentTokens <= MAX_TOKENS) {
            return promptText;
        }

        // Remove redundant words and phrases
        String reduced = promptText.replaceAll("\\b(very|really|quite|extremely)\\b", "")
                .replaceAll("\\b(in order to|so as to)\\b", "to")
                .replaceAll("\\b(due to the fact that|because of the fact that)\\b", "because")
                .replaceAll("\\b(at this point in time)\\b", "now").replaceAll("\\b(in the event that)\\b", "if")
                .replaceAll("\\b(prior to)\\b", "before").replaceAll("\\b(subsequent to)\\b", "after");

        // If still too many tokens, truncate
        if (estimateTokens(reduced) > MAX_TOKENS) {
            reduced = truncatePrompt(reduced);
        }

        return reduced;
    }

    /**
     * Optimize content structure.
     * 
     * @param promptText The prompt text to optimize
     * @return The optimized prompt text
     */
    private String optimizeContent(String promptText) {
        // Remove excessive whitespace
        String optimized = promptText.replaceAll("\\s+", " ").trim();

        // Ensure proper sentence structure
        optimized = optimized.replaceAll("\\s+([.!?])", "$1");

        // Add line breaks for better readability
        optimized = optimized.replaceAll("(System:|Task:|Expected Output:|Examples:|Constraints:)", "\n$1");

        return optimized;
    }

    /**
     * Apply custom optimization rules.
     * 
     * @param prompt The prompt to optimize
     * @return List of applied optimizations
     */
    private List<String> applyCustomOptimizationRules(AgentModelPrompt prompt) {
        List<String> optimizations = new ArrayList<>();

        for (Map.Entry<String, OptimizationRule> entry : optimizationRules.entrySet()) {
            try {
                OptimizationRule rule = entry.getValue();
                List<String> ruleOptimizations = rule.optimize(prompt);
                optimizations.addAll(ruleOptimizations);
            } catch (Exception e) {
                logger.error("Error applying optimization rule: {}", entry.getKey(), e);
            }
        }

        return optimizations;
    }

    /**
     * Validate prompt length.
     * 
     * @param prompt The prompt to validate
     * @param result The validation result to update
     */
    private void validateLength(AgentModelPrompt prompt, PromptValidationResult result) {
        int length = prompt.getPromptText().length();

        if (length > MAX_PROMPT_LENGTH) {
            result.addIssue("Prompt too long: " + length + " characters (max: " + MAX_PROMPT_LENGTH + ")");
        } else if (length < 10) {
            result.addIssue("Prompt too short: " + length + " characters (min: 10)");
        }

        if (length > OPTIMAL_PROMPT_LENGTH) {
            result.addWarning(
                    "Prompt longer than optimal: " + length + " characters (optimal: " + OPTIMAL_PROMPT_LENGTH + ")");
        }
    }

    /**
     * Validate content safety.
     * 
     * @param prompt The prompt to validate
     * @param result The validation result to update
     */
    private void validateContentSafety(AgentModelPrompt prompt, PromptValidationResult result) {
        String text = prompt.getPromptText().toLowerCase();

        // Check for potentially harmful content
        if (text.contains("ignore previous instructions") || text.contains("ignore all previous")) {
            result.addIssue("Potential prompt injection detected");
        }

        if (text.contains("system prompt") && text.contains("override")) {
            result.addIssue("Potential system prompt override detected");
        }

        // Check for sensitive information patterns
        Pattern sensitivePattern = Pattern.compile("\\b(password|secret|key|token)\\b", Pattern.CASE_INSENSITIVE);
        if (sensitivePattern.matcher(text).find()) {
            result.addWarning("Potential sensitive information in prompt");
        }
    }

    /**
     * Validate prompt structure.
     * 
     * @param prompt The prompt to validate
     * @param result The validation result to update
     */
    private void validateStructure(AgentModelPrompt prompt, PromptValidationResult result) {
        String text = prompt.getPromptText();

        if (!text.contains("System:") && !text.contains("Task:")) {
            result.addWarning("Prompt missing clear system role or task definition");
        }

        if (!text.contains("Expected Output:") && !text.contains("Output:")) {
            result.addWarning("Prompt missing expected output specification");
        }

        if (text.split("\\n").length < 3) {
            result.addWarning("Prompt structure could be improved with better formatting");
        }
    }

    /**
     * Validate token count.
     * 
     * @param prompt The prompt to validate
     * @param result The validation result to update
     */
    private void validateTokenCount(AgentModelPrompt prompt, PromptValidationResult result) {
        int tokens = estimateTokens(prompt.getPromptText());

        if (tokens > MAX_TOKENS) {
            result.addIssue("Too many tokens: " + tokens + " (max: " + MAX_TOKENS + ")");
        } else if (tokens < 10) {
            result.addIssue("Too few tokens: " + tokens + " (min: 10)");
        }

        if (tokens > MAX_TOKENS * 0.8) {
            result.addWarning("Token count approaching limit: " + tokens + "/" + MAX_TOKENS);
        }
    }

    /**
     * Apply custom validation rules.
     * 
     * @param prompt The prompt to validate
     * @param result The validation result to update
     */
    private void applyCustomValidationRules(AgentModelPrompt prompt,
            PromptValidationResult result) {
        // Custom validation rules can be added here
        // For now, we'll implement basic checks
    }

    /**
     * Extract system role from prompt text.
     * 
     * @param promptText The prompt text
     * @return The extracted system role
     */
    private String extractSystemRole(String promptText) {
        if (promptText.contains("System:")) {
            int start = promptText.indexOf("System:") + 7;
            int end = promptText.indexOf("\n", start);
            if (end == -1)
                end = promptText.length();
            return promptText.substring(start, end).trim();
        }
        return "";
    }

    /**
     * Extract task from prompt text.
     * 
     * @param promptText The prompt text
     * @return The extracted task
     */
    private String extractTask(String promptText) {
        if (promptText.contains("Task:")) {
            int start = promptText.indexOf("Task:") + 5;
            int end = promptText.indexOf("\n", start);
            if (end == -1)
                end = promptText.length();
            return promptText.substring(start, end).trim();
        }
        return "";
    }

    /**
     * Extract expected output from prompt text.
     * 
     * @param promptText The prompt text
     * @return The extracted expected output
     */
    private String extractExpectedOutput(String promptText) {
        if (promptText.contains("Expected Output:")) {
            int start = promptText.indexOf("Expected Output:") + 16;
            int end = promptText.indexOf("\n", start);
            if (end == -1)
                end = promptText.length();
            return promptText.substring(start, end).trim();
        }
        return "";
    }

    /**
     * Prompt Validation Result class.
     */
    // classes extracted to top-level: PromptValidationResult, OptimizationRule
}
