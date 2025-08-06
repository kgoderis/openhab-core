package org.openhab.core.ai.tool.proxy;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.tool.dto.AbstractCompletion;
import org.openhab.core.automation.Rule;
import org.openhab.core.automation.RuleRegistry;

/**
 * Encapsulated completion proxy for openHAB rules.
 * 
 * This class extends AbstractCompletion to provide proper encapsulation,
 * lifecycle management, and suggestion generation for rule-specific completions.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class RuleCompletionProxy extends AbstractCompletion {

    private final String completionType;
    private final RuleRegistry ruleRegistry;
    private final String context;
    private volatile @Nullable List<String> cachedSuggestions;

    /**
     * Create a new RuleCompletionProxy.
     *
     * @param completionType the type of completion (names, tags, triggers, actions, conditions)
     * @param ruleRegistry the rule registry
     * @param context the context for the completion (e.g., partial name, rule UID)
     * @param refreshIntervalMs the refresh interval in milliseconds
     */
    public RuleCompletionProxy(String completionType, RuleRegistry ruleRegistry, String context,
            long refreshIntervalMs) {
        super("rule_" + completionType, "Rule " + completionType + " suggestions for: " + context, List.of(), 0, false,
                refreshIntervalMs);

        this.completionType = completionType;
        this.ruleRegistry = ruleRegistry;
        this.context = context;
    }

    @Override
    public @Nullable List<String> getAdditionalSuggestions(@Nullable String additionalContext) {
        if (needsRefresh()) {
            refresh();
        }

        try {
            switch (completionType.toLowerCase()) {
                case "names":
                    return generateRuleNameSuggestions();
                case "tags":
                    return generateRuleTagSuggestions();
                case "triggers":
                    return generateRuleTriggerSuggestions();
                case "actions":
                    return generateRuleActionSuggestions();
                case "conditions":
                    return generateRuleConditionSuggestions();
                default:
                    LOGGER.warn("Unknown completion type: {}", completionType);
                    return List.of();
            }
        } catch (Exception e) {
            LOGGER.error("Error generating rule completion suggestions: {} - {}", completionType, context, e);
            return List.of();
        }
    }

    private List<String> generateRuleNameSuggestions() {
        try {
            List<String> suggestions = ruleRegistry.getAll().stream()
                    .filter(rule -> rule.getName().toLowerCase().contains(context.toLowerCase())).limit(10)
                    .map(Rule::getName).collect(Collectors.toList());

            cachedSuggestions = suggestions;
            return suggestions;
        } catch (Exception e) {
            LOGGER.error("Error generating rule name suggestions for: {}", context, e);
            return List.of();
        }
    }

    private List<String> generateRuleTagSuggestions() {
        try {
            List<String> suggestions = ruleRegistry.getAll().stream().flatMap(rule -> rule.getTags().stream())
                    .distinct().filter(tag -> tag.toLowerCase().contains(context.toLowerCase())).limit(10)
                    .collect(Collectors.toList());

            cachedSuggestions = suggestions;
            return suggestions;
        } catch (Exception e) {
            LOGGER.error("Error generating rule tag suggestions for: {}", context, e);
            return List.of();
        }
    }

    private List<String> generateRuleTriggerSuggestions() {
        try {
            List<String> triggerTypes = List.of("ItemStateChangeTrigger", "ItemCommandTrigger", "TimeTrigger",
                    "CronTrigger", "SystemStartlevelTrigger", "ThingStatusChangeTrigger", "ChannelEventTrigger");

            List<String> suggestions = triggerTypes.stream()
                    .filter(type -> type.toLowerCase().contains(context.toLowerCase())).collect(Collectors.toList());

            cachedSuggestions = suggestions;
            return suggestions;
        } catch (Exception e) {
            LOGGER.error("Error generating rule trigger suggestions for: {}", context, e);
            return List.of();
        }
    }

    private List<String> generateRuleActionSuggestions() {
        try {
            List<String> actionTypes = List.of("ItemCommandAction", "ItemStateAction", "LogAction", "HTTPAction",
                    "EmailAction", "PushNotificationAction", "VoiceAction", "ScriptAction");

            List<String> suggestions = actionTypes.stream()
                    .filter(type -> type.toLowerCase().contains(context.toLowerCase())).collect(Collectors.toList());

            cachedSuggestions = suggestions;
            return suggestions;
        } catch (Exception e) {
            LOGGER.error("Error generating rule action suggestions for: {}", context, e);
            return List.of();
        }
    }

    private List<String> generateRuleConditionSuggestions() {
        try {
            List<String> conditionTypes = List.of("ItemStateCondition", "TimeCondition", "ThingStatusCondition",
                    "ScriptCondition", "GenericCompareCondition");

            List<String> suggestions = conditionTypes.stream()
                    .filter(type -> type.toLowerCase().contains(context.toLowerCase())).collect(Collectors.toList());

            cachedSuggestions = suggestions;
            return suggestions;
        } catch (Exception e) {
            LOGGER.error("Error generating rule condition suggestions for: {}", context, e);
            return List.of();
        }
    }

    @Override
    public void refresh() {
        try {
            // Clear cached suggestions to force regeneration
            cachedSuggestions = null;

            // Regenerate suggestions based on completion type
            switch (completionType.toLowerCase()) {
                case "names":
                    generateRuleNameSuggestions();
                    break;
                case "tags":
                    generateRuleTagSuggestions();
                    break;
                case "triggers":
                    generateRuleTriggerSuggestions();
                    break;
                case "actions":
                    generateRuleActionSuggestions();
                    break;
                case "conditions":
                    generateRuleConditionSuggestions();
                    break;
                default:
                    LOGGER.warn("Unknown completion type during refresh: {}", completionType);
            }

            updateRefreshTime();
            LOGGER.debug("Refreshed rule completion: {} - {}", completionType, context);
        } catch (Exception e) {
            LOGGER.error("Error refreshing rule completion: {} - {}", completionType, context, e);
            markInvalid();
        }
    }

    @Override
    public void close() {
        LOGGER.debug("Closing rule completion: {} - {}", completionType, context);
        cachedSuggestions = null;
        markInvalid();
    }

    /**
     * Get the completion type.
     *
     * @return the completion type
     */
    public String getCompletionType() {
        return completionType;
    }

    /**
     * Get the context.
     *
     * @return the context
     */
    public String getContext() {
        return context;
    }

    /**
     * Get the cached suggestions.
     *
     * @return the cached suggestions or null if not available
     */
    public @Nullable List<String> getCachedSuggestions() {
        if (needsRefresh()) {
            refresh();
        }
        return cachedSuggestions;
    }
}
