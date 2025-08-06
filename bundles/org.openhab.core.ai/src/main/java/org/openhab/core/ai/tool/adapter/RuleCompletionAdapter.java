package org.openhab.core.ai.tool.adapter;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.tool.dto.AbstractCompletion;
import org.openhab.core.ai.tool.dto.Completion;
import org.openhab.core.ai.tool.factory.CompletionFactory;
import org.openhab.core.ai.tool.proxy.RuleCompletionProxy;
import org.openhab.core.automation.Rule;
import org.openhab.core.automation.RuleRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Completion adapter for openHAB rules.
 * 
 * This adapter provides MCP completion access to openHAB rule suggestions,
 * allowing AI agents to get contextual completions for rule operations.
 * 
 * Updated to use encapsulated architecture internally while maintaining
 * the same public interface for backward compatibility.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class RuleCompletionAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(RuleCompletionAdapter.class);
    private final RuleRegistry ruleRegistry;
    private final CompletionFactory completionFactory;

    /**
     * Create a new RuleCompletionAdapter.
     *
     * @param ruleRegistry the rule registry
     */
    public RuleCompletionAdapter(RuleRegistry ruleRegistry) {
        this.ruleRegistry = ruleRegistry;
        this.completionFactory = new CompletionFactory();
    }

    /**
     * Create rule name completions.
     *
     * @param partialName the partial rule name
     * @return the completion suggestions
     */
    public Completion createRuleNameCompletions(String partialName) {
        try {
            List<String> suggestions = ruleRegistry.getAll().stream()
                    .filter(rule -> rule.getName().toLowerCase().contains(partialName.toLowerCase())).limit(10)
                    .map(Rule::getName).collect(Collectors.toList());

            return new Completion("rule_names", "Rule name suggestions for: " + partialName, suggestions,
                    suggestions.size(), false);
        } catch (Exception e) {
            LOGGER.error("Error creating rule name completions for: {}", partialName, e);
            return new Completion("rule_names", "Rule name suggestions", List.of(), 0, false);
        }
    }

    /**
     * Create rule tag completions.
     *
     * @param partialTag the partial rule tag
     * @return the completion suggestions
     */
    public Completion createRuleTagCompletions(String partialTag) {
        try {
            List<String> suggestions = ruleRegistry.getAll().stream().flatMap(rule -> rule.getTags().stream())
                    .distinct().filter(tag -> tag.toLowerCase().contains(partialTag.toLowerCase())).limit(10)
                    .collect(Collectors.toList());

            return new Completion("rule_tags", "Rule tag suggestions for: " + partialTag, suggestions,
                    suggestions.size(), false);
        } catch (Exception e) {
            LOGGER.error("Error creating rule tag completions for: {}", partialTag, e);
            return new Completion("rule_tags", "Rule tag suggestions", List.of(), 0, false);
        }
    }

    /**
     * Create rule trigger completions.
     *
     * @param partialTrigger the partial trigger type
     * @return the completion suggestions
     */
    public Completion createRuleTriggerCompletions(String partialTrigger) {
        try {
            List<String> triggerTypes = List.of("ItemStateChangeTrigger", "ItemCommandTrigger", "TimeTrigger",
                    "CronTrigger", "SystemStartlevelTrigger", "ThingStatusChangeTrigger", "ChannelEventTrigger");

            List<String> suggestions = triggerTypes.stream()
                    .filter(type -> type.toLowerCase().contains(partialTrigger.toLowerCase()))
                    .collect(Collectors.toList());

            return new Completion("rule_triggers", "Rule trigger suggestions for: " + partialTrigger, suggestions,
                    suggestions.size(), false);
        } catch (Exception e) {
            LOGGER.error("Error creating rule trigger completions for: {}", partialTrigger, e);
            return new Completion("rule_triggers", "Rule trigger suggestions", List.of(), 0, false);
        }
    }

    /**
     * Create rule action completions.
     *
     * @param partialAction the partial action type
     * @return the completion suggestions
     */
    public Completion createRuleActionCompletions(String partialAction) {
        try {
            List<String> actionTypes = List.of("ItemCommandAction", "ItemStateAction", "LogAction", "HTTPAction",
                    "EmailAction", "PushNotificationAction", "VoiceAction", "ScriptAction");

            List<String> suggestions = actionTypes.stream()
                    .filter(type -> type.toLowerCase().contains(partialAction.toLowerCase()))
                    .collect(Collectors.toList());

            return new Completion("rule_actions", "Rule action suggestions for: " + partialAction, suggestions,
                    suggestions.size(), false);
        } catch (Exception e) {
            LOGGER.error("Error creating rule action completions for: {}", partialAction, e);
            return new Completion("rule_actions", "Rule action suggestions", List.of(), 0, false);
        }
    }

    /**
     * Create rule condition completions.
     *
     * @param partialCondition the partial condition type
     * @return the completion suggestions
     */
    public Completion createRuleConditionCompletions(String partialCondition) {
        try {
            List<String> conditionTypes = List.of("ItemStateCondition", "TimeCondition", "ThingStatusCondition",
                    "ScriptCondition", "GenericCompareCondition");

            List<String> suggestions = conditionTypes.stream()
                    .filter(type -> type.toLowerCase().contains(partialCondition.toLowerCase()))
                    .collect(Collectors.toList());

            return new Completion("rule_conditions", "Rule condition suggestions for: " + partialCondition, suggestions,
                    suggestions.size(), false);
        } catch (Exception e) {
            LOGGER.error("Error creating rule condition completions for: {}", partialCondition, e);
            return new Completion("rule_conditions", "Rule condition suggestions", List.of(), 0, false);
        }
    }

    /**
     * Get or create an encapsulated rule completion.
     *
     * @param completionType the type of completion (names, tags, triggers, actions, conditions)
     * @param context the context for the completion (e.g., partial name, rule UID)
     * @return the encapsulated completion or null if creation fails
     */
    public @Nullable AbstractCompletion getOrCreateEncapsulatedRuleCompletion(String completionType, String context) {
        String completionName = "rule_" + completionType + "_" + context;

        return completionFactory.createCompletion(completionName, (name, refreshIntervalMs) -> {
            try {
                return new RuleCompletionProxy(completionType, ruleRegistry, context, refreshIntervalMs);
            } catch (Exception e) {
                LOGGER.error("Error creating encapsulated rule completion: {} - {}", completionType, context, e);
                return null;
            }
        });
    }

    /**
     * Get additional suggestions for a rule completion.
     *
     * @param completionType the type of completion
     * @param context the context for the completion
     * @param additionalContext additional context for suggestions
     * @return the additional suggestions or null if not available
     */
    public @Nullable List<String> getAdditionalRuleSuggestions(String completionType, String context,
            @Nullable String additionalContext) {
        AbstractCompletion completion = getOrCreateEncapsulatedRuleCompletion(completionType, context);
        return completion != null ? completion.getAdditionalSuggestions(additionalContext) : null;
    }

    /**
     * Clean up resources managed by this adapter.
     */
    public void cleanup() {
        LOGGER.debug("Cleaning up RuleCompletionAdapter resources");
        completionFactory.cleanup();
    }
}
