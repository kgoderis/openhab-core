package org.openhab.core.ai.tool.completions.adapter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.tool.adapter.BaseAdapter;
import org.openhab.core.ai.tool.api.Adapter;
import org.openhab.core.ai.tool.api.CompletionContext;
import org.openhab.core.ai.tool.api.CompletionResult;
import org.openhab.core.ai.tool.completions.dto.Completion;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Completion adapter for openHAB items.
 *
 * Consolidated: logic folded into this adapter; no external factory used.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ItemCompletionAdapter extends BaseAdapter
        implements Adapter<Completion, CompletionContext, CompletionResult> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ItemCompletionAdapter.class);
    private static final long DEFAULT_REFRESH_INTERVAL_MS = 5 * 60 * 1000; // 5 minutes

    private final ItemRegistry itemRegistry;
    private final Map<String, List<String>> cache = new ConcurrentHashMap<>();

    public ItemCompletionAdapter(ItemRegistry itemRegistry) {
        super(DEFAULT_REFRESH_INTERVAL_MS);
        this.itemRegistry = itemRegistry;
    }

    @Override
    public @Nullable Completion createEntity(String identifier, CompletionContext context) {
        String type = (String) context.getProperty("type");
        if (type == null) {
            type = "names";
        }
        return new Completion("item_" + type, "Item " + type + " suggestions for: " + identifier, List.of(), 0, false);
    }

    @Override
    public @Nullable String getContent(String identifier, CompletionContext context) {
        // Not used for completions; return JSON-ish string of suggestions for convenience
        List<String> suggestions = getSuggestions(identifier, context);
        return suggestions != null ? String.join(",", suggestions) : null;
    }

    @Override
    public boolean isWritable(String identifier, CompletionContext context) {
        return false;
    }

    @Override
    public boolean writeContent(String identifier, @Nullable String content, CompletionContext context) {
        return false;
    }

    @Override
    public boolean exists(String identifier, CompletionContext context) {
        // For name-based queries we allow existence regardless; for commands ensure item exists
        String type = (String) context.getProperty("type");
        if (type == null) {
            return true;
        }
        if (type.equals("commands") || type.equals("categories") || type.equals("tags") || type.equals("groups")) {
            return itemRegistry.get(identifier) != null;
        }
        return true;
    }

    @Override
    public CompletionResult execute(String identifier, String operation, Map<String, Object> parameters,
            CompletionContext context) {
        long start = System.currentTimeMillis();
        try {
            if (operation != null && !operation.isEmpty()) {
                context.setProperty("type", operation);
            }
            List<String> suggestions = getSuggestions(identifier, context);
            int total = suggestions != null ? suggestions.size() : 0;
            return CompletionResult.success(suggestions != null ? suggestions : List.of(), total, false,
                    System.currentTimeMillis() - start);
        } catch (Exception e) {
            LOGGER.error("Error executing item completion: {} op:{}", identifier, operation, e);
            return CompletionResult.failure("Execution failed: " + e.getMessage(), System.currentTimeMillis() - start);
        }
    }

    @Override
    public void refresh(String identifier, CompletionContext context) {
        String type = (String) context.getProperty("type");
        if (type == null) {
            type = "names";
        }
        cache.remove(type + "|" + identifier);
        updateRefreshTime();
    }

    @Override
    public void cleanup() {
        cache.clear();
    }

    @Override
    public String getAdapterType() {
        return "completions/items";
    }

    @Override
    public String getUriPattern() {
        return "openhab://completions/items/{itemName}";
    }

    public void close() {
        cleanup();
    }

    private @Nullable List<String> getSuggestions(String identifier, CompletionContext context) {
        String type = (String) context.getProperty("type");
        if (type == null) {
            type = "names";
        }
        String partial = (String) context.getProperty("partial");
        String key = type + "|" + (identifier != null ? identifier : "") + "|" + (partial != null ? partial : "");
        List<String> cached = cache.get(key);
        if (cached != null && !needsRefresh()) {
            return cached;
        }
        List<String> suggestions;
        switch (type) {
            case "names":
                suggestions = itemRegistry.getAll().stream()
                        .filter(item -> item.getName().toLowerCase().contains(safeLower(partial))).limit(10)
                        .map(Item::getName).collect(Collectors.toList());
                break;
            case "types":
                suggestions = List
                        .of("Switch", "Dimmer", "Color", "Number", "String", "Contact", "Rollershutter", "DateTime",
                                "Location")
                        .stream().filter(t -> t.toLowerCase().contains(safeLower(partial)))
                        .collect(Collectors.toList());
                break;
            case "commands": {
                Item item = itemRegistry.get(identifier);
                if (item == null) {
                    suggestions = List.of();
                } else {
                    suggestions = item.getAcceptedCommandTypes().stream().map(Class::getSimpleName)
                            .filter(cmd -> cmd.toLowerCase().contains(safeLower(partial))).collect(Collectors.toList());
                }
                break;
            }
            case "categories":
                suggestions = itemRegistry.getAll().stream().map(Item::getCategory)
                        .filter(cat -> cat != null && !cat.isEmpty()).distinct()
                        .filter(cat -> cat.toLowerCase().contains(safeLower(partial))).limit(10)
                        .collect(Collectors.toList());
                break;
            case "tags":
                suggestions = itemRegistry.getAll().stream().flatMap(i -> i.getTags().stream()).distinct()
                        .filter(tag -> tag.toLowerCase().contains(safeLower(partial))).limit(10)
                        .collect(Collectors.toList());
                break;
            case "groups":
                suggestions = itemRegistry.getAll().stream().filter(i -> i.getGroupNames() != null)
                        .flatMap(i -> i.getGroupNames().stream()).distinct()
                        .filter(g -> g.toLowerCase().contains(safeLower(partial))).limit(10)
                        .collect(Collectors.toList());
                break;
            default:
                suggestions = List.of();
        }
        cache.put(key, suggestions);
        updateRefreshTime();
        return suggestions;
    }

    private String safeLower(@Nullable String s) {
        return s == null ? "" : s.toLowerCase();
    }
}
