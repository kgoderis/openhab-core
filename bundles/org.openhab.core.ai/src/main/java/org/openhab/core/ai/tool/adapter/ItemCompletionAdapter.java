package org.openhab.core.ai.tool.adapter;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.tool.dto.AbstractCompletion;
import org.openhab.core.ai.tool.dto.Completion;
import org.openhab.core.ai.tool.factory.CompletionFactory;
import org.openhab.core.ai.tool.proxy.ItemCompletionProxy;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Completion adapter for openHAB items.
 * 
 * This adapter provides MCP completion access to openHAB item suggestions,
 * allowing AI agents to get contextual completions for item operations.
 * 
 * Updated to use encapsulated architecture internally while maintaining
 * the same public interface for backward compatibility.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ItemCompletionAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ItemCompletionAdapter.class);
    private final ItemRegistry itemRegistry;
    private final CompletionFactory completionFactory;

    /**
     * Create a new ItemCompletionAdapter.
     *
     * @param itemRegistry the item registry
     */
    public ItemCompletionAdapter(ItemRegistry itemRegistry) {
        this.itemRegistry = itemRegistry;
        this.completionFactory = new CompletionFactory();
    }

    /**
     * Create item name completions.
     *
     * @param partialName the partial item name
     * @return the completion suggestions
     */
    public Completion createItemNameCompletions(String partialName) {
        try {
            List<String> suggestions = itemRegistry.getAll().stream()
                    .filter(item -> item.getName().toLowerCase().contains(partialName.toLowerCase())).limit(10)
                    .map(item -> item.getName()).collect(Collectors.toList());

            return new Completion("item_names", "Item name suggestions for: " + partialName, suggestions,
                    suggestions.size(), false);
        } catch (Exception e) {
            LOGGER.error("Error creating item name completions for: {}", partialName, e);
            return new Completion("item_names", "Item name suggestions", List.of(), 0, false);
        }
    }

    /**
     * Create item type completions.
     *
     * @param partialType the partial item type
     * @return the completion suggestions
     */
    public Completion createItemTypeCompletions(String partialType) {
        try {
            List<String> itemTypes = List.of("Switch", "Dimmer", "Color", "Number", "String", "Contact",
                    "Rollershutter", "DateTime", "Location");

            List<String> suggestions = itemTypes.stream()
                    .filter(type -> type.toLowerCase().contains(partialType.toLowerCase()))
                    .collect(Collectors.toList());

            return new Completion("item_types", "Item type suggestions for: " + partialType, suggestions,
                    suggestions.size(), false);
        } catch (Exception e) {
            LOGGER.error("Error creating item type completions for: {}", partialType, e);
            return new Completion("item_types", "Item type suggestions", List.of(), 0, false);
        }
    }

    /**
     * Create item command completions.
     *
     * @param itemName the item name
     * @param partialCommand the partial command
     * @return the completion suggestions
     */
    public Completion createItemCommandCompletions(String itemName, String partialCommand) {
        try {
            Item item = itemRegistry.get(itemName);
            if (item == null) {
                return new Completion("item_commands", "No commands available", List.of(), 0, false);
            }

            List<String> suggestions = item.getAcceptedCommandTypes().stream()
                    .map(commandType -> commandType.getSimpleName())
                    .filter(command -> command.toLowerCase().contains(partialCommand.toLowerCase()))
                    .collect(Collectors.toList());

            return new Completion("item_commands", "Command suggestions for " + itemName, suggestions,
                    suggestions.size(), false);
        } catch (Exception e) {
            LOGGER.error("Error creating item command completions for: {} - {}", itemName, partialCommand, e);
            return new Completion("item_commands", "Command suggestions", List.of(), 0, false);
        }
    }

    /**
     * Create item category completions.
     *
     * @param partialCategory the partial category
     * @return the completion suggestions
     */
    public Completion createItemCategoryCompletions(String partialCategory) {
        try {
            List<String> suggestions = itemRegistry.getAll().stream().map(Item::getCategory)
                    .filter(category -> category != null && !category.isEmpty()).distinct()
                    .filter(category -> category.toLowerCase().contains(partialCategory.toLowerCase())).limit(10)
                    .collect(Collectors.toList());

            return new Completion("item_categories", "Category suggestions for: " + partialCategory, suggestions,
                    suggestions.size(), false);
        } catch (Exception e) {
            LOGGER.error("Error creating item category completions for: {}", partialCategory, e);
            return new Completion("item_categories", "Category suggestions", List.of(), 0, false);
        }
    }

    /**
     * Create item tag completions.
     *
     * @param partialTag the partial tag
     * @return the completion suggestions
     */
    public Completion createItemTagCompletions(String partialTag) {
        try {
            List<String> suggestions = itemRegistry.getAll().stream().flatMap(item -> item.getTags().stream())
                    .distinct().filter(tag -> tag.toLowerCase().contains(partialTag.toLowerCase())).limit(10)
                    .collect(Collectors.toList());

            return new Completion("item_tags", "Tag suggestions for: " + partialTag, suggestions, suggestions.size(),
                    false);
        } catch (Exception e) {
            LOGGER.error("Error creating item tag completions for: {}", partialTag, e);
            return new Completion("item_tags", "Tag suggestions", List.of(), 0, false);
        }
    }

    /**
     * Create item group completions.
     *
     * @param partialGroup the partial group
     * @return the completion suggestions
     */
    public Completion createItemGroupCompletions(String partialGroup) {
        try {
            List<String> suggestions = itemRegistry.getAll().stream().filter(item -> item.getGroupNames() != null)
                    .flatMap(item -> item.getGroupNames().stream()).distinct()
                    .filter(group -> group.toLowerCase().contains(partialGroup.toLowerCase())).limit(10)
                    .collect(Collectors.toList());

            return new Completion("item_groups", "Group suggestions for: " + partialGroup, suggestions,
                    suggestions.size(), false);
        } catch (Exception e) {
            LOGGER.error("Error creating item group completions for: {}", partialGroup, e);
            return new Completion("item_groups", "Group suggestions", List.of(), 0, false);
        }
    }

    /**
     * Get or create an encapsulated item completion.
     *
     * @param completionType the type of completion (names, types, commands, categories, tags, groups)
     * @param context the context for the completion (e.g., partial name, item name)
     * @return the encapsulated completion or null if creation fails
     */
    public @Nullable AbstractCompletion getOrCreateEncapsulatedItemCompletion(String completionType, String context) {
        String completionName = "item_" + completionType + "_" + context;

        return completionFactory.createCompletion(completionName, (name, refreshIntervalMs) -> {
            try {
                return new ItemCompletionProxy(completionType, itemRegistry, context, refreshIntervalMs);
            } catch (Exception e) {
                LOGGER.error("Error creating encapsulated item completion: {} - {}", completionType, context, e);
                return null;
            }
        });
    }

    /**
     * Get additional suggestions for an item completion.
     *
     * @param completionType the type of completion
     * @param context the context for the completion
     * @param additionalContext additional context for suggestions
     * @return the additional suggestions or null if not available
     */
    public @Nullable List<String> getAdditionalItemSuggestions(String completionType, String context,
            @Nullable String additionalContext) {
        AbstractCompletion completion = getOrCreateEncapsulatedItemCompletion(completionType, context);
        return completion != null ? completion.getAdditionalSuggestions(additionalContext) : null;
    }

    /**
     * Clean up resources managed by this adapter.
     */
    public void cleanup() {
        LOGGER.debug("Cleaning up ItemCompletionAdapter resources");
        completionFactory.cleanup();
    }
}
