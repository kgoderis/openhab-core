package org.openhab.core.ai.tool.proxy;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.tool.dto.AbstractPrompt;
import org.openhab.core.ai.tool.dto.Prompt;
import org.openhab.core.automation.Rule;
import org.openhab.core.automation.RuleRegistry;

/**
 * Encapsulated prompt proxy for openHAB rules.
 * 
 * This class extends AbstractPrompt to provide proper encapsulation,
 * lifecycle management, and prompt generation for rule-specific operations.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class RulePromptProxy extends AbstractPrompt {

    private final String ruleUID;
    private final RuleRegistry ruleRegistry;
    private final String promptType;
    private volatile @Nullable String cachedPromptText;
    private volatile @Nullable Rule cachedRule;

    /**
     * Create a new RulePromptProxy.
     *
     * @param ruleUID the rule UID
     * @param ruleRegistry the rule registry
     * @param promptType the type of prompt (status, execution, config, discovery, creation, enable)
     * @param refreshIntervalMs the refresh interval in milliseconds
     */
    public RulePromptProxy(String ruleUID, RuleRegistry ruleRegistry, String promptType, long refreshIntervalMs) {
        super("Rule " + promptType + ": " + ruleUID, "Rule " + promptType + " prompt for: " + ruleUID,
                createArguments(promptType), refreshIntervalMs);

        this.ruleUID = ruleUID;
        this.ruleRegistry = ruleRegistry;
        this.promptType = promptType;
    }

    /**
     * Create prompt arguments based on the prompt type.
     *
     * @param promptType the type of prompt
     * @return the list of prompt arguments
     */
    private static List<Prompt.PromptArgument> createArguments(String promptType) {
        switch (promptType.toLowerCase()) {
            case "status":
                return List.of(new Prompt.PromptArgument("ruleUID", "UID of the rule", true),
                        new Prompt.PromptArgument("includeExecutionHistory", "Include execution history", false),
                        new Prompt.PromptArgument("includeTriggers", "Include trigger information", false));
            case "execution":
                return List.of(new Prompt.PromptArgument("ruleUID", "UID of the rule", true),
                        new Prompt.PromptArgument("parameters", "Execution parameters (JSON)", false),
                        new Prompt.PromptArgument("confirm", "Confirm the execution", false));
            case "config":
                return List.of(new Prompt.PromptArgument("ruleUID", "UID of the rule", true),
                        new Prompt.PromptArgument("operation", "Operation (get, set, update)", true),
                        new Prompt.PromptArgument("property", "Property to configure", false),
                        new Prompt.PromptArgument("value", "Value to set", false));
            case "discovery":
                return List.of(new Prompt.PromptArgument("filter", "Filter rules by tag or status", false),
                        new Prompt.PromptArgument("includeDisabled", "Include disabled rules", false),
                        new Prompt.PromptArgument("format", "Output format (json, xml, text)", false));
            case "creation":
                return List.of(new Prompt.PromptArgument("ruleName", "Name for the new rule", true),
                        new Prompt.PromptArgument("description", "Rule description", false),
                        new Prompt.PromptArgument("triggers", "Rule triggers (JSON)", true),
                        new Prompt.PromptArgument("conditions", "Rule conditions (JSON)", false),
                        new Prompt.PromptArgument("actions", "Rule actions (JSON)", true),
                        new Prompt.PromptArgument("tags", "Rule tags", false));
            case "enable":
                return List.of(new Prompt.PromptArgument("ruleUID", "UID of the rule", true),
                        new Prompt.PromptArgument("enabled", "Enable or disable the rule", true),
                        new Prompt.PromptArgument("confirm", "Confirm the action", false));
            default:
                return List.of(new Prompt.PromptArgument("ruleUID", "UID of the rule", true));
        }
    }

    @Override
    public @Nullable String generatePromptText(@Nullable Map<String, Object> arguments) {
        if (needsRefresh()) {
            refresh();
        }

        try {
            StringBuilder prompt = new StringBuilder();

            switch (promptType.toLowerCase()) {
                case "status":
                    generateStatusPrompt(prompt, arguments);
                    break;
                case "execution":
                    generateExecutionPrompt(prompt, arguments);
                    break;
                case "config":
                    generateConfigPrompt(prompt, arguments);
                    break;
                case "discovery":
                    generateDiscoveryPrompt(prompt, arguments);
                    break;
                case "creation":
                    generateCreationPrompt(prompt, arguments);
                    break;
                case "enable":
                    generateEnablePrompt(prompt, arguments);
                    break;
                default:
                    prompt.append("Unknown prompt type: ").append(promptType);
            }

            cachedPromptText = prompt.toString();
            return cachedPromptText;
        } catch (Exception e) {
            LOGGER.error("Error generating prompt text for rule prompt: {} - {}", ruleUID, promptType, e);
            return null;
        }
    }

    private void generateStatusPrompt(StringBuilder prompt, @Nullable Map<String, Object> arguments) {
        prompt.append("You are an AI assistant for openHAB home automation system.\n\n");
        prompt.append("Rule Status Request:\n");
        prompt.append("- Rule: ").append(ruleUID).append("\n");

        if (cachedRule != null) {
            prompt.append("- Name: ").append(cachedRule.getName()).append("\n");
            prompt.append("- Description: ").append(cachedRule.getDescription()).append("\n");
            prompt.append("- Tags: ").append(cachedRule.getTags()).append("\n");
        }

        if (arguments != null) {
            if (Boolean.TRUE.equals(arguments.get("includeExecutionHistory"))) {
                prompt.append("Please include execution history for this rule.\n");
            }
            if (Boolean.TRUE.equals(arguments.get("includeTriggers"))) {
                prompt.append("Please include trigger information for this rule.\n");
            }
        }
    }

    private void generateExecutionPrompt(StringBuilder prompt, @Nullable Map<String, Object> arguments) {
        prompt.append("You are an AI assistant for openHAB home automation system.\n\n");
        prompt.append("Rule Execution Request:\n");
        prompt.append("- Rule: ").append(ruleUID).append("\n");

        if (arguments != null) {
            String parameters = (String) arguments.get("parameters");
            if (parameters != null) {
                prompt.append("- Parameters: ").append(parameters).append("\n");
            }
            if (Boolean.TRUE.equals(arguments.get("confirm"))) {
                prompt.append("Please confirm this execution before proceeding.\n");
            }
        }
    }

    private void generateConfigPrompt(StringBuilder prompt, @Nullable Map<String, Object> arguments) {
        prompt.append("You are an AI assistant for openHAB home automation system.\n\n");
        prompt.append("Rule Configuration Request:\n");
        prompt.append("- Rule: ").append(ruleUID).append("\n");

        if (arguments != null) {
            String operation = (String) arguments.get("operation");
            if (operation != null) {
                prompt.append("- Operation: ").append(operation).append("\n");
            }
            String property = (String) arguments.get("property");
            if (property != null) {
                prompt.append("- Property: ").append(property).append("\n");
            }
            String value = (String) arguments.get("value");
            if (value != null) {
                prompt.append("- Value: ").append(value).append("\n");
            }
        }
    }

    private void generateDiscoveryPrompt(StringBuilder prompt, @Nullable Map<String, Object> arguments) {
        prompt.append("You are an AI assistant for openHAB home automation system.\n\n");
        prompt.append("Rule Discovery Request:\n");

        if (arguments != null) {
            String filter = (String) arguments.get("filter");
            if (filter != null) {
                prompt.append("- Filter: ").append(filter).append("\n");
            }
            if (Boolean.TRUE.equals(arguments.get("includeDisabled"))) {
                prompt.append("Please include disabled rules in the results.\n");
            }
            String format = (String) arguments.get("format");
            if (format != null) {
                prompt.append("Please format the output as: ").append(format).append("\n");
            }
        }
    }

    private void generateCreationPrompt(StringBuilder prompt, @Nullable Map<String, Object> arguments) {
        prompt.append("You are an AI assistant for openHAB home automation system.\n\n");
        prompt.append("Rule Creation Request:\n");

        if (arguments != null) {
            String ruleName = (String) arguments.get("ruleName");
            if (ruleName != null) {
                prompt.append("- Rule Name: ").append(ruleName).append("\n");
            }
            String description = (String) arguments.get("description");
            if (description != null) {
                prompt.append("- Description: ").append(description).append("\n");
            }
            String triggers = (String) arguments.get("triggers");
            if (triggers != null) {
                prompt.append("- Triggers: ").append(triggers).append("\n");
            }
            String conditions = (String) arguments.get("conditions");
            if (conditions != null) {
                prompt.append("- Conditions: ").append(conditions).append("\n");
            }
            String actions = (String) arguments.get("actions");
            if (actions != null) {
                prompt.append("- Actions: ").append(actions).append("\n");
            }
            String tags = (String) arguments.get("tags");
            if (tags != null) {
                prompt.append("- Tags: ").append(tags).append("\n");
            }
        }
    }

    private void generateEnablePrompt(StringBuilder prompt, @Nullable Map<String, Object> arguments) {
        prompt.append("You are an AI assistant for openHAB home automation system.\n\n");
        prompt.append("Rule Enable/Disable Request:\n");
        prompt.append("- Rule: ").append(ruleUID).append("\n");

        if (arguments != null) {
            Boolean enabled = (Boolean) arguments.get("enabled");
            if (enabled != null) {
                prompt.append("- Action: ").append(enabled ? "Enable" : "Disable").append("\n");
            }
            if (Boolean.TRUE.equals(arguments.get("confirm"))) {
                prompt.append("Please confirm this action before proceeding.\n");
            }
        }
    }

    @Override
    public boolean validateArguments(@Nullable Map<String, Object> arguments) {
        if (arguments == null) {
            return true; // No arguments required for some prompt types
        }

        try {
            // Validate based on prompt type
            switch (promptType.toLowerCase()) {
                case "status":
                    return validateStatusArguments(arguments);
                case "execution":
                    return validateExecutionArguments(arguments);
                case "config":
                    return validateConfigArguments(arguments);
                case "discovery":
                    return validateDiscoveryArguments(arguments);
                case "creation":
                    return validateCreationArguments(arguments);
                case "enable":
                    return validateEnableArguments(arguments);
                default:
                    return true;
            }
        } catch (Exception e) {
            LOGGER.error("Error validating arguments for rule prompt: {} - {}", ruleUID, promptType, e);
            return false;
        }
    }

    private boolean validateStatusArguments(Map<String, Object> arguments) {
        // ruleUID is required
        Object ruleUID = arguments.get("ruleUID");
        if (ruleUID == null || !(ruleUID instanceof String)) {
            return false;
        }

        // Optional boolean arguments
        for (String key : List.of("includeExecutionHistory", "includeTriggers")) {
            Object value = arguments.get(key);
            if (value != null && !(value instanceof Boolean)) {
                return false;
            }
        }

        return true;
    }

    private boolean validateExecutionArguments(Map<String, Object> arguments) {
        // ruleUID is required
        Object ruleUID = arguments.get("ruleUID");
        if (ruleUID == null || !(ruleUID instanceof String)) {
            return false;
        }

        // Optional string arguments
        for (String key : List.of("parameters")) {
            Object value = arguments.get(key);
            if (value != null && !(value instanceof String)) {
                return false;
            }
        }

        // Optional boolean arguments
        Object confirm = arguments.get("confirm");
        if (confirm != null && !(confirm instanceof Boolean)) {
            return false;
        }

        return true;
    }

    private boolean validateConfigArguments(Map<String, Object> arguments) {
        // ruleUID and operation are required
        Object ruleUID = arguments.get("ruleUID");
        Object operation = arguments.get("operation");
        if (ruleUID == null || !(ruleUID instanceof String) || operation == null || !(operation instanceof String)) {
            return false;
        }

        // Validate operation value
        String op = (String) operation;
        if (!op.equals("get") && !op.equals("set") && !op.equals("update")) {
            return false;
        }

        // Optional string arguments
        for (String key : List.of("property", "value")) {
            Object value = arguments.get(key);
            if (value != null && !(value instanceof String)) {
                return false;
            }
        }

        return true;
    }

    private boolean validateDiscoveryArguments(Map<String, Object> arguments) {
        // All arguments are optional for discovery
        for (String key : List.of("filter", "format")) {
            Object value = arguments.get(key);
            if (value != null && !(value instanceof String)) {
                return false;
            }
        }

        Object includeDisabled = arguments.get("includeDisabled");
        if (includeDisabled != null && !(includeDisabled instanceof Boolean)) {
            return false;
        }

        return true;
    }

    private boolean validateCreationArguments(Map<String, Object> arguments) {
        // ruleName, triggers, and actions are required
        Object ruleName = arguments.get("ruleName");
        Object triggers = arguments.get("triggers");
        Object actions = arguments.get("actions");
        if (ruleName == null || !(ruleName instanceof String) || triggers == null || !(triggers instanceof String)
                || actions == null || !(actions instanceof String)) {
            return false;
        }

        // Optional string arguments
        for (String key : List.of("description", "conditions", "tags")) {
            Object value = arguments.get(key);
            if (value != null && !(value instanceof String)) {
                return false;
            }
        }

        return true;
    }

    private boolean validateEnableArguments(Map<String, Object> arguments) {
        // ruleUID and enabled are required
        Object ruleUID = arguments.get("ruleUID");
        Object enabled = arguments.get("enabled");
        if (ruleUID == null || !(ruleUID instanceof String) || enabled == null || !(enabled instanceof Boolean)) {
            return false;
        }

        // Optional boolean arguments
        Object confirm = arguments.get("confirm");
        if (confirm != null && !(confirm instanceof Boolean)) {
            return false;
        }

        return true;
    }

    @Override
    public void refresh() {
        try {
            // Refresh rule information
            if (ruleUID != null && !ruleUID.isEmpty()) {
                cachedRule = ruleRegistry.get(ruleUID);
            }

            // Clear cached prompt text to force regeneration
            cachedPromptText = null;

            updateRefreshTime();
            LOGGER.debug("Refreshed rule prompt: {} - {}", ruleUID, promptType);
        } catch (Exception e) {
            LOGGER.error("Error refreshing rule prompt: {} - {}", ruleUID, promptType, e);
            markInvalid();
        }
    }

    @Override
    public void close() {
        LOGGER.debug("Closing rule prompt: {} - {}", ruleUID, promptType);
        cachedPromptText = null;
        cachedRule = null;
        markInvalid();
    }

    /**
     * Get the rule UID.
     *
     * @return the rule UID
     */
    public String getRuleUID() {
        return ruleUID;
    }

    /**
     * Get the prompt type.
     *
     * @return the prompt type
     */
    public String getPromptType() {
        return promptType;
    }

    /**
     * Get the underlying rule.
     *
     * @return the rule or null if not available
     */
    public @Nullable Rule getRule() {
        if (needsRefresh()) {
            refresh();
        }
        return cachedRule;
    }
}
