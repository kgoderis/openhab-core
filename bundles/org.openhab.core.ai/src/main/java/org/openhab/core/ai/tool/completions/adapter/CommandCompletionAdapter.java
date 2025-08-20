package org.openhab.core.ai.tool.completions.adapter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.adapter.Adapter;
import org.openhab.core.ai.tool.adapter.BaseAdapter;
import org.openhab.core.ai.tool.completions.api.CompletionContext;
import org.openhab.core.ai.tool.completions.api.CompletionResult;
import org.openhab.core.ai.tool.completions.api.dto.Completion;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Completion adapter for openHAB commands.
 *
 * Consolidated: logic folded into this adapter; no external factory used.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class CommandCompletionAdapter extends BaseAdapter
        implements Adapter<Completion, CompletionContext, CompletionResult> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommandCompletionAdapter.class);
    private static final long DEFAULT_REFRESH_INTERVAL_MS = 5 * 60 * 1000; // 5 minutes

    private final ItemRegistry itemRegistry;
    private final Map<String, List<String>> cache = new ConcurrentHashMap<>();

    public CommandCompletionAdapter(ItemRegistry itemRegistry) {
        super(DEFAULT_REFRESH_INTERVAL_MS);
        this.itemRegistry = itemRegistry;
    }

    @Override
    public @Nullable Completion createEntity(String identifier, CompletionContext context) {
        String type = (String) context.getProperty("type");
        if (type == null) {
            type = "general";
        }
        return new Completion(type + "_commands", "Command " + type + " suggestions for: " + identifier, List.of(), 0,
                false);
    }

    @Override
    public @Nullable String getContent(String identifier, CompletionContext context) {
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
            LOGGER.error("Error executing command completion: {} op:{}", identifier, operation, e);
            return CompletionResult.failure("Execution failed: " + e.getMessage(), System.currentTimeMillis() - start);
        }
    }

    @Override
    public void refresh(String identifier, CompletionContext context) {
        String type = (String) context.getProperty("type");
        if (type == null) {
            type = "general";
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
        return "completions/commands";
    }

    @Override
    public String getUriPattern() {
        return "openhab://completions/commands/{context}";
    }

    @Override
    public boolean canAdapt(Completion source) {
        return source != null && source.getPromptReference() != null && source.getPromptReference().contains("command");
    }

    @Override
    public Class<Completion> getSourceType() {
        return Completion.class;
    }

    @Override
    public Class<CompletionResult> getResultType() {
        return CompletionResult.class;
    }

    @Override
    public boolean isValid() {
        return itemRegistry != null;
    }

    @Override
    public CompletionResult adapt(Completion source, CompletionContext context) {
        if (source == null) {
            return CompletionResult.failure("Source cannot be null", 0);
        }
        return execute(source.getPromptReference(), "adapt", Map.of(), context);
    }

    @Override
    public void close() {
        cleanup();
    }

    private @Nullable List<String> getSuggestions(String identifier, CompletionContext context) {
        String type = (String) context.getProperty("type");
        if (type == null) {
            type = "general";
        }
        String partial = (String) context.getProperty("partial");
        String key = type + "|" + (identifier != null ? identifier : "") + "|" + (partial != null ? partial : "");
        List<String> cached = cache.get(key);
        if (cached != null && !needsRefresh()) {
            return cached;
        }
        List<String> suggestions;
        switch (type) {
            case "item_commands": {
                Item item = itemRegistry.get(identifier);
                if (item == null) {
                    suggestions = List.of();
                } else {
                    suggestions = item.getAcceptedCommandTypes().stream().map(Class::getSimpleName)
                            .filter(cmd -> cmd.toLowerCase().contains(safeLower(partial))).collect(Collectors.toList());
                }
                break;
            }
            case "general":
                suggestions = List
                        .of("ON", "OFF", "TOGGLE", "REFRESH", "PLAY", "PAUSE", "STOP", "NEXT", "PREVIOUS", "VOLUME",
                                "MUTE", "UNMUTE", "UP", "DOWN", "OPEN", "CLOSE", "LOCK", "UNLOCK")
                        .stream().filter(cmd -> cmd.toLowerCase().contains(safeLower(partial)))
                        .collect(Collectors.toList());
                break;
            case "numeric":
                suggestions = List.of("0", "25", "50", "75", "100", "0.0", "0.25", "0.5", "0.75", "1.0");
                suggestions = suggestions.stream().filter(v -> v.contains(partial != null ? partial : ""))
                        .collect(Collectors.toList());
                break;
            case "color":
                suggestions = List
                        .of("RED", "GREEN", "BLUE", "WHITE", "BLACK", "YELLOW", "CYAN", "MAGENTA", "ORANGE", "PURPLE",
                                "PINK", "BROWN", "GRAY", "SILVER", "GOLD")
                        .stream().filter(c -> c.toLowerCase().contains(safeLower(partial)))
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
