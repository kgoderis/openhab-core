package org.openhab.core.ai.tool.proxy;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.tool.dto.AbstractPrompt;
import org.openhab.core.ai.tool.dto.Prompt;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemRegistry;

/**
 * Encapsulated prompt proxy for openHAB items.
 * 
 * This class extends AbstractPrompt to provide proper encapsulation,
 * lifecycle management, and prompt generation for item-specific operations.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ItemPromptProxy extends AbstractPrompt {

    private final String itemName;
    private final ItemRegistry itemRegistry;
    private final String promptType;
    private volatile @Nullable String cachedPromptText;
    private volatile @Nullable Item cachedItem;

    /**
     * Create a new ItemPromptProxy.
     *
     * @param itemName the item name
     * @param itemRegistry the item registry
     * @param promptType the type of prompt (status, control, config, discovery, creation)
     * @param refreshIntervalMs the refresh interval in milliseconds
     */
    public ItemPromptProxy(String itemName, ItemRegistry itemRegistry, String promptType, long refreshIntervalMs) {
        super("Item " + promptType + ": " + itemName, "Item " + promptType + " prompt for: " + itemName,
                createArguments(promptType), refreshIntervalMs);

        this.itemName = itemName;
        this.itemRegistry = itemRegistry;
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
                return List.of(new Prompt.PromptArgument("itemName", "Name of the item", true),
                        new Prompt.PromptArgument("includeHistory", "Include historical data", false),
                        new Prompt.PromptArgument("includeMetadata", "Include item metadata", false));
            case "control":
                return List.of(new Prompt.PromptArgument("itemName", "Name of the item", true),
                        new Prompt.PromptArgument("command", "Command to send to the item", true),
                        new Prompt.PromptArgument("confirm", "Confirm the action before executing", false));
            case "config":
                return List.of(new Prompt.PromptArgument("itemName", "Name of the item", true),
                        new Prompt.PromptArgument("operation", "Operation (get, set, update)", true),
                        new Prompt.PromptArgument("property", "Property to configure", false),
                        new Prompt.PromptArgument("value", "Value to set", false));
            case "discovery":
                return List.of(new Prompt.PromptArgument("filter", "Filter items by type, tag, or group", false),
                        new Prompt.PromptArgument("includeGroups", "Include group information", false),
                        new Prompt.PromptArgument("format", "Output format (json, xml, text)", false));
            case "creation":
                return List.of(new Prompt.PromptArgument("itemType", "Type of item to create", true),
                        new Prompt.PromptArgument("itemName", "Name for the new item", true),
                        new Prompt.PromptArgument("label", "Human-readable label", false),
                        new Prompt.PromptArgument("category", "Item category", false),
                        new Prompt.PromptArgument("groups", "Groups to add the item to", false));
            default:
                return List.of(new Prompt.PromptArgument("itemName", "Name of the item", true));
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
                case "control":
                    generateControlPrompt(prompt, arguments);
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
                default:
                    prompt.append("Unknown prompt type: ").append(promptType);
            }

            cachedPromptText = prompt.toString();
            return cachedPromptText;
        } catch (Exception e) {
            LOGGER.error("Error generating prompt text for item prompt: {} - {}", itemName, promptType, e);
            return null;
        }
    }

    private void generateStatusPrompt(StringBuilder prompt, @Nullable Map<String, Object> arguments) {
        prompt.append("You are an AI assistant for openHAB home automation system.\n\n");
        prompt.append("Item Status Request:\n");
        prompt.append("- Item: ").append(itemName).append("\n");

        if (cachedItem != null) {
            prompt.append("- Type: ").append(cachedItem.getType()).append("\n");
            prompt.append("- Label: ").append(cachedItem.getLabel()).append("\n");
            prompt.append("- Category: ").append(cachedItem.getCategory()).append("\n");
        }

        if (arguments != null) {
            if (Boolean.TRUE.equals(arguments.get("includeHistory"))) {
                prompt.append("Please include historical data for this item.\n");
            }
            if (Boolean.TRUE.equals(arguments.get("includeMetadata"))) {
                prompt.append("Please include item metadata and configuration.\n");
            }
        }
    }

    private void generateControlPrompt(StringBuilder prompt, @Nullable Map<String, Object> arguments) {
        prompt.append("You are an AI assistant for openHAB home automation system.\n\n");
        prompt.append("Item Control Request:\n");
        prompt.append("- Item: ").append(itemName).append("\n");

        if (arguments != null) {
            String command = (String) arguments.get("command");
            if (command != null) {
                prompt.append("- Command: ").append(command).append("\n");
            }
            if (Boolean.TRUE.equals(arguments.get("confirm"))) {
                prompt.append("Please confirm this action before executing.\n");
            }
        }
    }

    private void generateConfigPrompt(StringBuilder prompt, @Nullable Map<String, Object> arguments) {
        prompt.append("You are an AI assistant for openHAB home automation system.\n\n");
        prompt.append("Item Configuration Request:\n");
        prompt.append("- Item: ").append(itemName).append("\n");

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
        prompt.append("Item Discovery Request:\n");

        if (arguments != null) {
            String filter = (String) arguments.get("filter");
            if (filter != null) {
                prompt.append("- Filter: ").append(filter).append("\n");
            }
            if (Boolean.TRUE.equals(arguments.get("includeGroups"))) {
                prompt.append("Please include group information in the results.\n");
            }
            String format = (String) arguments.get("format");
            if (format != null) {
                prompt.append("Please format the output as: ").append(format).append("\n");
            }
        }
    }

    private void generateCreationPrompt(StringBuilder prompt, @Nullable Map<String, Object> arguments) {
        prompt.append("You are an AI assistant for openHAB home automation system.\n\n");
        prompt.append("Item Creation Request:\n");

        if (arguments != null) {
            String itemType = (String) arguments.get("itemType");
            if (itemType != null) {
                prompt.append("- Item Type: ").append(itemType).append("\n");
            }
            String newItemName = (String) arguments.get("itemName");
            if (newItemName != null) {
                prompt.append("- Item Name: ").append(newItemName).append("\n");
            }
            String label = (String) arguments.get("label");
            if (label != null) {
                prompt.append("- Label: ").append(label).append("\n");
            }
            String category = (String) arguments.get("category");
            if (category != null) {
                prompt.append("- Category: ").append(category).append("\n");
            }
            String groups = (String) arguments.get("groups");
            if (groups != null) {
                prompt.append("- Groups: ").append(groups).append("\n");
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
                case "control":
                    return validateControlArguments(arguments);
                case "config":
                    return validateConfigArguments(arguments);
                case "discovery":
                    return validateDiscoveryArguments(arguments);
                case "creation":
                    return validateCreationArguments(arguments);
                default:
                    return true;
            }
        } catch (Exception e) {
            LOGGER.error("Error validating arguments for item prompt: {} - {}", itemName, promptType, e);
            return false;
        }
    }

    private boolean validateStatusArguments(Map<String, Object> arguments) {
        // itemName is required
        Object itemName = arguments.get("itemName");
        if (itemName == null || !(itemName instanceof String)) {
            return false;
        }

        // Optional boolean arguments
        for (String key : List.of("includeHistory", "includeMetadata")) {
            Object value = arguments.get(key);
            if (value != null && !(value instanceof Boolean)) {
                return false;
            }
        }

        return true;
    }

    private boolean validateControlArguments(Map<String, Object> arguments) {
        // itemName and command are required
        Object itemName = arguments.get("itemName");
        Object command = arguments.get("command");
        if (itemName == null || !(itemName instanceof String) || command == null || !(command instanceof String)) {
            return false;
        }

        // Optional boolean arguments
        Object confirm = arguments.get("confirm");
        if (confirm != null && !(confirm instanceof Boolean)) {
            return false;
        }

        return true;
    }

    private boolean validateConfigArguments(Map<String, Object> arguments) {
        // itemName and operation are required
        Object itemName = arguments.get("itemName");
        Object operation = arguments.get("operation");
        if (itemName == null || !(itemName instanceof String) || operation == null || !(operation instanceof String)) {
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

        Object includeGroups = arguments.get("includeGroups");
        if (includeGroups != null && !(includeGroups instanceof Boolean)) {
            return false;
        }

        return true;
    }

    private boolean validateCreationArguments(Map<String, Object> arguments) {
        // itemType and itemName are required
        Object itemType = arguments.get("itemType");
        Object itemName = arguments.get("itemName");
        if (itemType == null || !(itemType instanceof String) || itemName == null || !(itemName instanceof String)) {
            return false;
        }

        // Optional string arguments
        for (String key : List.of("label", "category", "groups")) {
            Object value = arguments.get(key);
            if (value != null && !(value instanceof String)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void refresh() {
        try {
            // Refresh item information
            if (itemName != null && !itemName.isEmpty()) {
                cachedItem = itemRegistry.get(itemName);
            }

            // Clear cached prompt text to force regeneration
            cachedPromptText = null;

            updateRefreshTime();
            LOGGER.debug("Refreshed item prompt: {} - {}", itemName, promptType);
        } catch (Exception e) {
            LOGGER.error("Error refreshing item prompt: {} - {}", itemName, promptType, e);
            markInvalid();
        }
    }

    @Override
    public void close() {
        LOGGER.debug("Closing item prompt: {} - {}", itemName, promptType);
        cachedPromptText = null;
        cachedItem = null;
        markInvalid();
    }

    /**
     * Get the item name.
     *
     * @return the item name
     */
    public String getItemName() {
        return itemName;
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
     * Get the underlying item.
     *
     * @return the item or null if not available
     */
    public @Nullable Item getItem() {
        if (needsRefresh()) {
            refresh();
        }
        return cachedItem;
    }
}
