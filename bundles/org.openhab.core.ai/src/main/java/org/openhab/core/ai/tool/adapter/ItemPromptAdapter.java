package org.openhab.core.ai.tool.adapter;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.tool.dto.AbstractPrompt;
import org.openhab.core.ai.tool.dto.Prompt;
import org.openhab.core.ai.tool.factory.PromptFactory;
import org.openhab.core.ai.tool.proxy.ItemPromptProxy;
import org.openhab.core.items.ItemRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Prompt adapter for openHAB items.
 * 
 * This adapter provides MCP prompt access to openHAB item-specific prompts,
 * allowing AI agents to get contextual prompts for item operations.
 * 
 * Updated to use encapsulated architecture internally while maintaining
 * the same public interface for backward compatibility.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ItemPromptAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ItemPromptAdapter.class);
    private final ItemRegistry itemRegistry;
    private final PromptFactory promptFactory;

    /**
     * Create a new ItemPromptAdapter.
     *
     * @param itemRegistry the item registry
     */
    public ItemPromptAdapter(ItemRegistry itemRegistry) {
        this.itemRegistry = itemRegistry;
        this.promptFactory = new PromptFactory();
    }

    /**
     * Create an item status prompt.
     *
     * @param itemName the item name
     * @return the item status prompt
     */
    public Prompt createItemStatusPrompt(String itemName) {
        String name = "Item Status: " + itemName;
        String description = "Get status information for item: " + itemName;

        List<Prompt.PromptArgument> arguments = List.of(new Prompt.PromptArgument("itemName", "Name of the item", true),
                new Prompt.PromptArgument("includeHistory", "Include historical data", false),
                new Prompt.PromptArgument("includeMetadata", "Include item metadata", false));

        return new Prompt(name, description, arguments);
    }

    /**
     * Create an item control prompt.
     *
     * @param itemName the item name
     * @return the item control prompt
     */
    public Prompt createItemControlPrompt(String itemName) {
        String name = "Item Control: " + itemName;
        String description = "Control operations for item: " + itemName;

        List<Prompt.PromptArgument> arguments = List.of(new Prompt.PromptArgument("itemName", "Name of the item", true),
                new Prompt.PromptArgument("command", "Command to send to the item", true),
                new Prompt.PromptArgument("confirm", "Confirm the action before executing", false));

        return new Prompt(name, description, arguments);
    }

    /**
     * Create an item configuration prompt.
     *
     * @param itemName the item name
     * @return the item configuration prompt
     */
    public Prompt createItemConfigPrompt(String itemName) {
        String name = "Item Configuration: " + itemName;
        String description = "Configuration operations for item: " + itemName;

        List<Prompt.PromptArgument> arguments = List.of(new Prompt.PromptArgument("itemName", "Name of the item", true),
                new Prompt.PromptArgument("operation", "Operation (get, set, update)", true),
                new Prompt.PromptArgument("property", "Property to configure", false),
                new Prompt.PromptArgument("value", "Value to set", false));

        return new Prompt(name, description, arguments);
    }

    /**
     * Create an item discovery prompt.
     *
     * @return the item discovery prompt
     */
    public Prompt createItemDiscoveryPrompt() {
        String name = "Item Discovery";
        String description = "Discover and list available items";

        List<Prompt.PromptArgument> arguments = List.of(
                new Prompt.PromptArgument("filter", "Filter items by type, tag, or group", false),
                new Prompt.PromptArgument("includeGroups", "Include group information", false),
                new Prompt.PromptArgument("format", "Output format (json, xml, text)", false));

        return new Prompt(name, description, arguments);
    }

    /**
     * Create an item creation prompt.
     *
     * @return the item creation prompt
     */
    public Prompt createItemCreationPrompt() {
        String name = "Item Creation";
        String description = "Create a new item";

        List<Prompt.PromptArgument> arguments = List.of(
                new Prompt.PromptArgument("itemType", "Type of item to create", true),
                new Prompt.PromptArgument("itemName", "Name for the new item", true),
                new Prompt.PromptArgument("label", "Human-readable label", false),
                new Prompt.PromptArgument("category", "Item category", false),
                new Prompt.PromptArgument("groups", "Groups to add the item to", false));

        return new Prompt(name, description, arguments);
    }

    /**
     * Get or create an encapsulated item prompt.
     *
     * @param itemName the item name
     * @param promptType the type of prompt (status, control, config, discovery, creation)
     * @return the encapsulated prompt or null if creation fails
     */
    public @Nullable AbstractPrompt getOrCreateEncapsulatedItemPrompt(String itemName, String promptType) {
        String promptName = "Item " + promptType + ": " + itemName;

        return promptFactory.createPrompt(promptName, (name, refreshIntervalMs) -> {
            try {
                return new ItemPromptProxy(itemName, itemRegistry, promptType, refreshIntervalMs);
            } catch (Exception e) {
                LOGGER.error("Error creating encapsulated item prompt: {} - {}", itemName, promptType, e);
                return null;
            }
        });
    }

    /**
     * Generate prompt text for an item prompt.
     *
     * @param itemName the item name
     * @param promptType the type of prompt
     * @param arguments the prompt arguments
     * @return the generated prompt text or null if generation fails
     */
    public @Nullable String generateItemPromptText(String itemName, String promptType,
            @Nullable Map<String, Object> arguments) {
        AbstractPrompt prompt = getOrCreateEncapsulatedItemPrompt(itemName, promptType);
        return prompt != null ? prompt.generatePromptText(arguments) : null;
    }

    /**
     * Clean up resources managed by this adapter.
     */
    public void cleanup() {
        LOGGER.debug("Cleaning up ItemPromptAdapter resources");
        promptFactory.cleanup();
    }
}
