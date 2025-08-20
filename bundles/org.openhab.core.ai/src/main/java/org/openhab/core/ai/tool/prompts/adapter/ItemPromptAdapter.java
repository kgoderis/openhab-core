package org.openhab.core.ai.tool.prompts.adapter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.adapter.Adapter;
import org.openhab.core.ai.tool.adapter.BaseAdapter;
import org.openhab.core.ai.tool.prompts.api.PromptContext;
import org.openhab.core.ai.tool.prompts.api.PromptResult;
import org.openhab.core.ai.tool.prompts.api.dto.Prompt;
import org.openhab.core.ai.tool.prompts.api.dto.PromptArgument;
import org.openhab.core.ai.tool.prompts.cache.CachedPromptData;
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
        if (data.getCachedContent() == null || needsRefresh()) {
            data.setCachedContent(buildPromptContent(data.getEntityId(), data.getPromptType()));
            updateRefreshTime();
        }
        return data.getCachedContent();
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
            data.setCachedContent(null);
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
    public @Nullable PromptResult adapt(Prompt source, PromptContext context) {
        return execute(source.getName(), "adapt", Map.of(), context);
    }

    @Override
    public boolean canAdapt(Prompt source) {
        return source != null && source.getName() != null;
    }

    @Override
    public Class<Prompt> getSourceType() {
        return Prompt.class;
    }

    @Override
    public Class<PromptResult> getResultType() {
        return PromptResult.class;
    }

    @Override
    public String getUriPattern() {
        return "openhab://prompts/items/{itemName}";
    }

    public void close() {
        cleanup();
    }

    private @Nullable CachedPromptData getOrCreateCachedData(String identifier, PromptContext context) {
        return promptCache.computeIfAbsent(identifier, key -> {
            String typeFromContext = (String) context.getProperty("promptType");
            String promptType = typeFromContext == null ? "status" : typeFromContext;
            return new CachedPromptData(identifier, promptType);
        });
    }

    private String buildPromptContent(String itemName, String promptType) {
        Item item = itemRegistry.get(itemName);
        if (item == null) {
            return "Item not found: " + itemName;
        }

        StringBuilder content = new StringBuilder();
        content.append("Item: ").append(item.getName()).append(" (").append(item.getType()).append(")\n");
        content.append("Label: ").append(item.getLabel() != null ? item.getLabel() : "").append("\n");

        switch (promptType.toLowerCase()) {
            case "state":
                content.append("Current State: ").append(item.getState()).append("\n");
                content.append("State Type: ").append(item.getState().getClass().getSimpleName()).append("\n");
                break;
            case "commands":
                content.append("Accepted Commands:\n");
                item.getAcceptedCommandTypes().forEach(commandType -> {
                    content.append("  - ").append(commandType.getSimpleName()).append("\n");
                });
                break;
            case "metadata":
                content.append("Category: ").append(item.getCategory() != null ? item.getCategory() : "").append("\n");
                content.append("Groups: ").append(item.getGroupNames()).append("\n");
                break;
            default: // status
                content.append("Category: ").append(item.getCategory() != null ? item.getCategory() : "").append("\n");
                content.append("Groups: ").append(item.getGroupNames()).append("\n");
                break;
        }

        return content.toString();
    }
}
