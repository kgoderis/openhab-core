package org.openhab.core.ai.tool.adapter;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.tool.dto.AbstractPrompt;
import org.openhab.core.ai.tool.dto.Prompt;
import org.openhab.core.ai.tool.factory.PromptFactory;
import org.openhab.core.ai.tool.proxy.RulePromptProxy;
import org.openhab.core.automation.RuleRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Prompt adapter for openHAB rules.
 * 
 * This adapter provides MCP prompt access to openHAB rule-specific prompts,
 * allowing AI agents to get contextual prompts for rule operations.
 * 
 * Updated to use encapsulated architecture internally while maintaining
 * the same public interface for backward compatibility.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class RulePromptAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(RulePromptAdapter.class);
    private final RuleRegistry ruleRegistry;
    private final PromptFactory promptFactory;

    /**
     * Create a new RulePromptAdapter.
     *
     * @param ruleRegistry the rule registry
     */
    public RulePromptAdapter(RuleRegistry ruleRegistry) {
        this.ruleRegistry = ruleRegistry;
        this.promptFactory = new PromptFactory();
    }

    /**
     * Create a rule status prompt.
     *
     * @param ruleUID the rule UID
     * @return the rule status prompt
     */
    public Prompt createRuleStatusPrompt(String ruleUID) {
        String name = "Rule Status: " + ruleUID;
        String description = "Get status information for rule: " + ruleUID;

        List<Prompt.PromptArgument> arguments = List.of(new Prompt.PromptArgument("ruleUID", "UID of the rule", true),
                new Prompt.PromptArgument("includeExecutionHistory", "Include execution history", false),
                new Prompt.PromptArgument("includeTriggers", "Include trigger information", false));

        return new Prompt(name, description, arguments);
    }

    /**
     * Create a rule execution prompt.
     *
     * @param ruleUID the rule UID
     * @return the rule execution prompt
     */
    public Prompt createRuleExecutionPrompt(String ruleUID) {
        String name = "Rule Execution: " + ruleUID;
        String description = "Execute a rule manually: " + ruleUID;

        List<Prompt.PromptArgument> arguments = List.of(new Prompt.PromptArgument("ruleUID", "UID of the rule", true),
                new Prompt.PromptArgument("parameters", "Execution parameters (JSON)", false),
                new Prompt.PromptArgument("confirm", "Confirm the execution", false));

        return new Prompt(name, description, arguments);
    }

    /**
     * Create a rule configuration prompt.
     *
     * @param ruleUID the rule UID
     * @return the rule configuration prompt
     */
    public Prompt createRuleConfigPrompt(String ruleUID) {
        String name = "Rule Configuration: " + ruleUID;
        String description = "Configuration operations for rule: " + ruleUID;

        List<Prompt.PromptArgument> arguments = List.of(new Prompt.PromptArgument("ruleUID", "UID of the rule", true),
                new Prompt.PromptArgument("operation", "Operation (get, set, update)", true),
                new Prompt.PromptArgument("property", "Property to configure", false),
                new Prompt.PromptArgument("value", "Value to set", false));

        return new Prompt(name, description, arguments);
    }

    /**
     * Create a rule discovery prompt.
     *
     * @return the rule discovery prompt
     */
    public Prompt createRuleDiscoveryPrompt() {
        String name = "Rule Discovery";
        String description = "Discover and list available rules";

        List<Prompt.PromptArgument> arguments = List.of(
                new Prompt.PromptArgument("filter", "Filter rules by tag or status", false),
                new Prompt.PromptArgument("includeDisabled", "Include disabled rules", false),
                new Prompt.PromptArgument("format", "Output format (json, xml, text)", false));

        return new Prompt(name, description, arguments);
    }

    /**
     * Create a rule creation prompt.
     *
     * @return the rule creation prompt
     */
    public Prompt createRuleCreationPrompt() {
        String name = "Rule Creation";
        String description = "Create a new rule";

        List<Prompt.PromptArgument> arguments = List.of(
                new Prompt.PromptArgument("ruleName", "Name for the new rule", true),
                new Prompt.PromptArgument("description", "Rule description", false),
                new Prompt.PromptArgument("triggers", "Rule triggers (JSON)", true),
                new Prompt.PromptArgument("conditions", "Rule conditions (JSON)", false),
                new Prompt.PromptArgument("actions", "Rule actions (JSON)", true),
                new Prompt.PromptArgument("tags", "Rule tags", false));

        return new Prompt(name, description, arguments);
    }

    /**
     * Create a rule enable/disable prompt.
     *
     * @param ruleUID the rule UID
     * @return the rule enable/disable prompt
     */
    public Prompt createRuleEnableDisablePrompt(String ruleUID) {
        String name = "Rule Enable/Disable: " + ruleUID;
        String description = "Enable or disable rule: " + ruleUID;

        List<Prompt.PromptArgument> arguments = List.of(new Prompt.PromptArgument("ruleUID", "UID of the rule", true),
                new Prompt.PromptArgument("enabled", "Enable or disable the rule", true),
                new Prompt.PromptArgument("confirm", "Confirm the action", false));

        return new Prompt(name, description, arguments);
    }

    /**
     * Get or create an encapsulated rule prompt.
     *
     * @param ruleUID the rule UID
     * @param promptType the type of prompt (status, execution, config, discovery, creation, enable)
     * @return the encapsulated prompt or null if creation fails
     */
    public @Nullable AbstractPrompt getOrCreateEncapsulatedRulePrompt(String ruleUID, String promptType) {
        String promptName = "Rule " + promptType + ": " + ruleUID;

        return promptFactory.createPrompt(promptName, (name, refreshIntervalMs) -> {
            try {
                return new RulePromptProxy(ruleUID, ruleRegistry, promptType, refreshIntervalMs);
            } catch (Exception e) {
                LOGGER.error("Error creating encapsulated rule prompt: {} - {}", ruleUID, promptType, e);
                return null;
            }
        });
    }

    /**
     * Generate prompt text for a rule prompt.
     *
     * @param ruleUID the rule UID
     * @param promptType the type of prompt
     * @param arguments the prompt arguments
     * @return the generated prompt text or null if generation fails
     */
    public @Nullable String generateRulePromptText(String ruleUID, String promptType,
            @Nullable Map<String, Object> arguments) {
        AbstractPrompt prompt = getOrCreateEncapsulatedRulePrompt(ruleUID, promptType);
        return prompt != null ? prompt.generatePromptText(arguments) : null;
    }

    /**
     * Clean up resources managed by this adapter.
     */
    public void cleanup() {
        LOGGER.debug("Cleaning up RulePromptAdapter resources");
        promptFactory.cleanup();
    }
}
