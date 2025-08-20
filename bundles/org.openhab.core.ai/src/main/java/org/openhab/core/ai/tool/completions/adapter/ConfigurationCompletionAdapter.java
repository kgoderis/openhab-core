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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Completion adapter for openHAB configurations.
 *
 * Consolidated: logic folded into this adapter; no external factory used.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ConfigurationCompletionAdapter extends BaseAdapter
        implements Adapter<Completion, CompletionContext, CompletionResult> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationCompletionAdapter.class);
    private static final long DEFAULT_REFRESH_INTERVAL_MS = 5 * 60 * 1000; // 5 minutes

    private final Map<String, List<String>> cache = new ConcurrentHashMap<>();

    public ConfigurationCompletionAdapter() {
        super(DEFAULT_REFRESH_INTERVAL_MS);
    }

    @Override
    public @Nullable Completion createEntity(String identifier, CompletionContext context) {
        String type = (String) context.getProperty("type");
        if (type == null) {
            type = "keys";
        }
        return new Completion("config_" + type, "Configuration " + type + " suggestions for: " + identifier, List.of(),
                0, false);
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
            LOGGER.error("Error executing configuration completion: {} op:{}", identifier, operation, e);
            return CompletionResult.failure("Execution failed: " + e.getMessage(), System.currentTimeMillis() - start);
        }
    }

    @Override
    public void refresh(String identifier, CompletionContext context) {
        String type = (String) context.getProperty("type");
        if (type == null) {
            type = "keys";
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
        return "completions/configuration";
    }

    @Override
    public String getUriPattern() {
        return "openhab://completions/config/{configId}";
    }

    @Override
    public boolean canAdapt(Completion source) {
        return source != null && source.getDescription().toLowerCase().contains("config");
    }

    @Override
    public CompletionResult adapt(Completion source, CompletionContext context) {
        if (!canAdapt(source)) {
            return CompletionResult.failure("Cannot adapt source", 0);
        }
        return execute(source.getPromptReference(), "config", Map.of(), context);
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
    public void close() {
        cleanup();
    }

    private @Nullable List<String> getSuggestions(String identifier, CompletionContext context) {
        String type = (String) context.getProperty("type");
        if (type == null) {
            type = "keys";
        }
        String partial = (String) context.getProperty("partial");
        String key = type + "|" + (identifier != null ? identifier : "") + "|" + (partial != null ? partial : "");
        List<String> cached = cache.get(key);
        if (cached != null && !needsRefresh()) {
            return cached;
        }
        List<String> suggestions;
        switch (type) {
            case "keys":
                suggestions = List
                        .of("host", "port", "username", "password", "url", "apiKey", "token", "timeout", "retries",
                                "enabled", "pollingInterval", "updateInterval", "maxConnections", "bufferSize",
                                "encoding", "format", "protocol", "ssl", "tls", "certificate", "privateKey")
                        .stream().filter(k -> k.toLowerCase().contains(safeLower(partial)))
                        .collect(Collectors.toList());
                break;
            case "values":
                suggestions = List
                        .of("true", "false", "localhost", "127.0.0.1", "8080", "443", "admin", "user", "password",
                                "https://", "http://", "ws://", "wss://", "json", "xml", "text", "binary", "utf-8",
                                "ascii", "iso-8859-1", "30", "60", "300", "600", "3600")
                        .stream().filter(v -> v.toLowerCase().contains(safeLower(partial)))
                        .collect(Collectors.toList());
                break;
            case "types":
                suggestions = List
                        .of("String", "Integer", "Long", "Double", "Float", "Boolean", "BigDecimal", "BigInteger",
                                "Date", "LocalDate", "LocalTime", "LocalDateTime", "ZonedDateTime", "Duration",
                                "Period", "URL", "URI", "File", "Path", "List", "Map", "Set", "Array", "Object", "Enum")
                        .stream().filter(t -> t.toLowerCase().contains(safeLower(partial)))
                        .collect(Collectors.toList());
                break;
            case "sections":
                suggestions = List
                        .of("connection", "authentication", "security", "logging", "performance", "network", "database",
                                "cache", "monitoring", "alerts", "notifications", "scheduling", "backup", "restore",
                                "maintenance", "advanced", "experimental", "debug", "development", "production",
                                "testing", "staging")
                        .stream().filter(s -> s.toLowerCase().contains(safeLower(partial)))
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
