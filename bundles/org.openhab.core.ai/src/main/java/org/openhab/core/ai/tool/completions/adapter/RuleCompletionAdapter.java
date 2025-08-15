package org.openhab.core.ai.tool.completions.adapter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.tool.adapter.BaseAdapter;
import org.openhab.core.ai.tool.api.Adapter;
import org.openhab.core.ai.tool.completions.api.CompletionContext;
import org.openhab.core.ai.tool.completions.api.CompletionResult;
import org.openhab.core.ai.tool.completions.api.dto.Completion;
import org.openhab.core.automation.Rule;
import org.openhab.core.automation.RuleRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Completion adapter for openHAB rules.
 *
 * Consolidated: logic folded into this adapter; no external factory used.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class RuleCompletionAdapter extends BaseAdapter
        implements Adapter<Completion, CompletionContext, CompletionResult> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RuleCompletionAdapter.class);
    private static final long DEFAULT_REFRESH_INTERVAL_MS = 5 * 60 * 1000; // 5 minutes

    private final RuleRegistry ruleRegistry;
    private final Map<String, List<String>> cache = new ConcurrentHashMap<>();

    public RuleCompletionAdapter(RuleRegistry ruleRegistry) {
        super(DEFAULT_REFRESH_INTERVAL_MS);
        this.ruleRegistry = ruleRegistry;
    }

    @Override
    public @Nullable Completion createEntity(String identifier, CompletionContext context) {
        String type = (String) context.getProperty("type");
        if (type == null) {
            type = "names";
        }
        return new Completion("rule_" + type, "Rule " + type + " suggestions for: " + identifier, List.of(), 0, false);
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
            LOGGER.error("Error executing rule completion: {} op:{}", identifier, operation, e);
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
        return "completions/rules";
    }

    @Override
    public String getUriPattern() {
        return "openhab://completions/rules/{ruleUID}";
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
                suggestions = ruleRegistry.getAll().stream()
                        .filter(rule -> rule.getName().toLowerCase().contains(safeLower(partial))).limit(10)
                        .map(Rule::getName).collect(Collectors.toList());
                break;
            case "tags":
                suggestions = ruleRegistry.getAll().stream().flatMap(rule -> rule.getTags().stream()).distinct()
                        .filter(tag -> tag.toLowerCase().contains(safeLower(partial))).limit(10)
                        .collect(Collectors.toList());
                break;
            case "triggers":
                suggestions = List
                        .of("ItemStateChangeTrigger", "ItemCommandTrigger", "TimeTrigger", "CronTrigger",
                                "SystemStartlevelTrigger", "ThingStatusChangeTrigger", "ChannelEventTrigger")
                        .stream().filter(t -> t.toLowerCase().contains(safeLower(partial)))
                        .collect(Collectors.toList());
                break;
            case "actions":
                suggestions = List
                        .of("ItemCommandAction", "ItemStateAction", "LogAction", "HTTPAction", "EmailAction",
                                "PushNotificationAction", "VoiceAction", "ScriptAction")
                        .stream().filter(a -> a.toLowerCase().contains(safeLower(partial)))
                        .collect(Collectors.toList());
                break;
            case "conditions":
                suggestions = List
                        .of("ItemStateCondition", "TimeCondition", "ThingStatusCondition", "ScriptCondition",
                                "GenericCompareCondition")
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
