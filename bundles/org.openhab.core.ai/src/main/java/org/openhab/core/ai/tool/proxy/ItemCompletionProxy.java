package org.openhab.core.ai.tool.proxy;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.tool.dto.AbstractCompletion;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemRegistry;

/**
 * Encapsulated completion proxy for openHAB items.
 * 
 * This class extends AbstractCompletion to provide proper encapsulation,
 * lifecycle management, and suggestion generation for item-specific completions.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ItemCompletionProxy extends AbstractCompletion {

    private final String completionType;
    private final ItemRegistry itemRegistry;
    private final String context;
    private volatile @Nullable List<String> cachedSuggestions;

    /**
     * Create a new ItemCompletionProxy.
     *
     * @param completionType the type of completion (names, types, commands, categories, tags, groups)
     * @param itemRegistry the item registry
     * @param context the context for the completion (e.g., partial name, item name)
     * @param refreshIntervalMs the refresh interval in milliseconds
     */
    public ItemCompletionProxy(String completionType, ItemRegistry itemRegistry, String context,
            long refreshIntervalMs) {
        super("item_" + completionType, "Item " + completionType + " suggestions for: " + context, List.of(), 0, false,
                refreshIntervalMs);

        this.completionType = completionType;
        this.itemRegistry = itemRegistry;
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
                    return generateItemNameSuggestions();
                case "types":
                    return generateItemTypeSuggestions();
                case "commands":
                    return generateItemCommandSuggestions();
                case "categories":
                    return generateItemCategorySuggestions();
                case "tags":
                    return generateItemTagSuggestions();
                case "groups":
                    return generateItemGroupSuggestions();
                default:
                    LOGGER.warn("Unknown completion type: {}", completionType);
                    return List.of();
            }
        } catch (Exception e) {
            LOGGER.error("Error generating item completion suggestions: {} - {}", completionType, context, e);
            return List.of();
        }
    }

    private List<String> generateItemNameSuggestions() {
        try {
            List<String> suggestions = itemRegistry.getAll().stream()
                    .filter(item -> item.getName().toLowerCase().contains(context.toLowerCase())).limit(10)
                    .map(Item::getName).collect(Collectors.toList());

            cachedSuggestions = suggestions;
            return suggestions;
        } catch (Exception e) {
            LOGGER.error("Error generating item name suggestions for: {}", context, e);
            return List.of();
        }
    }

    private List<String> generateItemTypeSuggestions() {
        try {
            List<String> itemTypes = List.of("Switch", "Dimmer", "Color", "Number", "String", "Contact",
                    "Rollershutter", "DateTime", "Location");

            List<String> suggestions = itemTypes.stream()
                    .filter(type -> type.toLowerCase().contains(context.toLowerCase())).collect(Collectors.toList());

            cachedSuggestions = suggestions;
            return suggestions;
        } catch (Exception e) {
            LOGGER.error("Error generating item type suggestions for: {}", context, e);
            return List.of();
        }
    }

    private List<String> generateItemCommandSuggestions() {
        try {
            Item item = itemRegistry.get(context);
            if (item == null) {
                return List.of();
            }

            List<String> suggestions = item.getAcceptedCommandTypes().stream()
                    .map(commandType -> commandType.getSimpleName()).collect(Collectors.toList());

            cachedSuggestions = suggestions;
            return suggestions;
        } catch (Exception e) {
            LOGGER.error("Error generating item command suggestions for: {}", context, e);
            return List.of();
        }
    }

    private List<String> generateItemCategorySuggestions() {
        try {
            List<String> suggestions = itemRegistry.getAll().stream().map(Item::getCategory)
                    .filter(category -> category != null && !category.isEmpty()).distinct()
                    .filter(category -> category.toLowerCase().contains(context.toLowerCase())).limit(10)
                    .collect(Collectors.toList());

            cachedSuggestions = suggestions;
            return suggestions;
        } catch (Exception e) {
            LOGGER.error("Error generating item category suggestions for: {}", context, e);
            return List.of();
        }
    }

    private List<String> generateItemTagSuggestions() {
        try {
            List<String> suggestions = itemRegistry.getAll().stream().flatMap(item -> item.getTags().stream())
                    .distinct().filter(tag -> tag.toLowerCase().contains(context.toLowerCase())).limit(10)
                    .collect(Collectors.toList());

            cachedSuggestions = suggestions;
            return suggestions;
        } catch (Exception e) {
            LOGGER.error("Error generating item tag suggestions for: {}", context, e);
            return List.of();
        }
    }

    private List<String> generateItemGroupSuggestions() {
        try {
            List<String> suggestions = itemRegistry.getAll().stream().filter(item -> item.getGroupNames() != null)
                    .flatMap(item -> item.getGroupNames().stream()).distinct()
                    .filter(group -> group.toLowerCase().contains(context.toLowerCase())).limit(10)
                    .collect(Collectors.toList());

            cachedSuggestions = suggestions;
            return suggestions;
        } catch (Exception e) {
            LOGGER.error("Error generating item group suggestions for: {}", context, e);
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
                    generateItemNameSuggestions();
                    break;
                case "types":
                    generateItemTypeSuggestions();
                    break;
                case "commands":
                    generateItemCommandSuggestions();
                    break;
                case "categories":
                    generateItemCategorySuggestions();
                    break;
                case "tags":
                    generateItemTagSuggestions();
                    break;
                case "groups":
                    generateItemGroupSuggestions();
                    break;
                default:
                    LOGGER.warn("Unknown completion type during refresh: {}", completionType);
            }

            updateRefreshTime();
            LOGGER.debug("Refreshed item completion: {} - {}", completionType, context);
        } catch (Exception e) {
            LOGGER.error("Error refreshing item completion: {} - {}", completionType, context, e);
            markInvalid();
        }
    }

    @Override
    public void close() {
        LOGGER.debug("Closing item completion: {} - {}", completionType, context);
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
