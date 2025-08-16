package org.openhab.core.ai.tool.prompts.adapter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.tool.adapter.BaseAdapter;
import org.openhab.core.ai.tool.api.Adapter;
import org.openhab.core.ai.tool.prompts.api.PromptContext;
import org.openhab.core.ai.tool.prompts.api.PromptResult;
import org.openhab.core.ai.tool.prompts.api.dto.Prompt;
import org.openhab.core.ai.tool.prompts.api.dto.PromptArgument;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Prompt adapter for openHAB items.
 *
 * This adapter provides MCP prompt access to openHAB item-specific prompts,
 * allowing AI agents to get contextual prompts for item operations.
 *
 * Consolidated: proxy logic folded into this adapter; no external factory used.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ItemPromptAdapter extends BaseAdapter implements Adapter<Prompt, PromptContext, PromptResult> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ItemPromptAdapter.class);
    private static final long DEFAULT_REFRESH_INTERVAL_MS = 5 * 60 * 1000; // 5 minutes

    private final ItemRegistry itemRegistry;
    private final Map<String, CachedPromptData> promptCache = new ConcurrentHashMap<>();

    private static final class CachedPromptData {
        final String itemName;
        final String promptType;
        volatile @Nullable String cachedContent;

        CachedPromptData(String itemName, String promptType) {
            this.itemName = itemName;
            this.promptType = promptType;
        }
    }

    public ItemPromptAdapter(ItemRegistry itemRegistry) {
        super(DEFAULT_REFRESH_INTERVAL_MS);
        this.itemRegistry = itemRegistry;
    }

    @Override
    public @Nullable Prompt createEntity(String identifier, PromptContext context) {
        String typeFromContext = (String) context.getProperty("promptType");
        final String promptType = typeFromContext == null ? "status" : typeFromContext;
        String name = "Item " + promptType + ": " + identifier;
        String description = "Prompt for item " + identifier + " (" + promptType + ")";
        PromptArgument[] args = new PromptArgument[] { new PromptArgument("itemName", "Name of the item", true),
                new PromptArgument("promptType", "Type of prompt", false),
                new PromptArgument("includeMetadata", "Include item metadata", false) };
        return new Prompt(name, description, List.of(args));
    }

    @Override
    public @Nullable String getContent(String identifier, PromptContext context) {
        CachedPromptData data = getOrCreateCachedData(identifier, context);
        if (data == null) {
            return null;
        }
        if (data.cachedContent == null || needsRefresh()) {
            data.cachedContent = buildPromptContent(data.itemName, data.promptType);
            updateRefreshTime();
        }
        return data.cachedContent;
    }

    @Override
    public boolean isWritable(String identifier, PromptContext context) {
        return false;
    }

    @Override
    public boolean writeContent(String identifier, @Nullable String content, PromptContext context) {
        return false;
    }

    @Override
    public boolean exists(String identifier, PromptContext context) {
        return itemRegistry.get(identifier) != null;
    }

    @Override
    public PromptResult execute(String identifier, String operation, Map<String, Object> parameters,
            PromptContext context) {
        long start = System.currentTimeMillis();
        try {
            String promptType = operation.isEmpty() ? "status" : operation;
            String content = buildPromptContent(identifier, promptType);
            return PromptResult.success(content, System.currentTimeMillis() - start);
        } catch (Exception e) {
            LOGGER.error("Error executing item prompt: {} op:{}", identifier, operation, e);
            return PromptResult.failure("Execution failed: " + e.getMessage(), System.currentTimeMillis() - start);
        }
    }

    @Override
    public void refresh(String identifier, PromptContext context) {
        CachedPromptData data = getOrCreateCachedData(identifier, context);
        if (data != null) {
            data.cachedContent = null;
            updateRefreshTime();
        }
    }

    @Override
    public void cleanup() {
        promptCache.clear();
    }

    @Override
    public String getAdapterType() {
        return "prompts/items";
    }

    @Override
    public String getUriPattern() {
        return "openhab://prompts/items/{itemName}";
    }

    @Override
    public void close() {
        cleanup();
    }

    private @Nullable CachedPromptData getOrCreateCachedData(String itemName, PromptContext context) {
        String typeFromContext = (String) context.getProperty("promptType");
        final String promptType = typeFromContext == null ? "status" : typeFromContext;
        final String cacheKey = itemName + "|" + promptType;
        return promptCache.computeIfAbsent(cacheKey, k -> new CachedPromptData(itemName, promptType));
    }

    private String buildPromptContent(String itemName, String promptType) {
        Item item = itemRegistry.get(itemName);
        if (item == null) {
            return "Item '" + itemName + "' not found";
        }
        switch (promptType) {
            case "status":
                return "Status of " + itemName + ": state=" + item.getState() + ", type="
                        + item.getClass().getSimpleName();
            case "control":
                return "Control prompt for " + itemName + ". Arguments: command, confirm(optional)";
            case "config":
                return "Config prompt for " + itemName + ". Arguments: operation(get|set|update), property, value";
            case "discovery":
                return "Discovery prompt: list items with filters (type, tag, group)";
            case "creation":
                return "Creation prompt: create item with type, name, label, category, groups";
            default:
                return "Prompt for " + itemName + " (" + promptType + ")";
        }
    }
}
