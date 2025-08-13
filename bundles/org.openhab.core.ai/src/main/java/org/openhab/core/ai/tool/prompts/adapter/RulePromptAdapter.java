package org.openhab.core.ai.tool.prompts.adapter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.tool.adapter.BaseAdapter;
import org.openhab.core.ai.tool.api.Adapter;
import org.openhab.core.ai.tool.api.PromptContext;
import org.openhab.core.ai.tool.api.PromptResult;
import org.openhab.core.ai.tool.prompts.dto.Prompt;
import org.openhab.core.ai.tool.prompts.dto.PromptArgument;
import org.openhab.core.automation.Rule;
import org.openhab.core.automation.RuleRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Prompt adapter for openHAB rules.
 *
 * Consolidated: proxy logic folded into this adapter; no external factory used.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class RulePromptAdapter extends BaseAdapter implements Adapter<Prompt, PromptContext, PromptResult> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RulePromptAdapter.class);
    private static final long DEFAULT_REFRESH_INTERVAL_MS = 5 * 60 * 1000; // 5 minutes

    private final RuleRegistry ruleRegistry;
    private final Map<String, CachedPromptData> promptCache = new ConcurrentHashMap<>();

    private static final class CachedPromptData {
        final String ruleUID;
        final String promptType;
        volatile @Nullable String cachedContent;

        CachedPromptData(String ruleUID, String promptType) {
            this.ruleUID = ruleUID;
            this.promptType = promptType;
        }
    }

    public RulePromptAdapter(RuleRegistry ruleRegistry) {
        super(DEFAULT_REFRESH_INTERVAL_MS);
        this.ruleRegistry = ruleRegistry;
    }

    @Override
    public @Nullable Prompt createEntity(String identifier, PromptContext context) {
        String typeFromContext = (String) context.getProperty("promptType");
        final String promptType = typeFromContext == null ? "status" : typeFromContext;
        String name = "Rule " + promptType + ": " + identifier;
        String description = "Prompt for rule " + identifier + " (" + promptType + ")";
        PromptArgument[] args = new PromptArgument[] {
                new PromptArgument("ruleUID", "UID of the rule", true),
                new PromptArgument("promptType", "Type of prompt", false),
                new PromptArgument("includeTriggers", "Include trigger information", false) };
        return new Prompt(name, description, java.util.List.of(args));
    }

    @Override
    public @Nullable String getContent(String identifier, PromptContext context) {
        CachedPromptData data = getOrCreateCachedData(identifier, context);
        if (data == null) {
            return null;
        }
        if (data.cachedContent == null || needsRefresh()) {
            data.cachedContent = buildPromptContent(data.ruleUID, data.promptType);
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
        return ruleRegistry.get(identifier) != null;
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
            LOGGER.error("Error executing rule prompt: {} op:{}", identifier, operation, e);
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
        return "prompts/rules";
    }

    @Override
    public String getUriPattern() {
        return "openhab://prompts/rules/{ruleUID}";
    }

    public void close() {
        cleanup();
    }

    private @Nullable CachedPromptData getOrCreateCachedData(String ruleUID, PromptContext context) {
        String typeFromContext = (String) context.getProperty("promptType");
        final String promptType = typeFromContext == null ? "status" : typeFromContext;
        final String cacheKey = ruleUID + "|" + promptType;
        return promptCache.computeIfAbsent(cacheKey, k -> new CachedPromptData(ruleUID, promptType));
    }

    private String buildPromptContent(String ruleUID, String promptType) {
        Rule rule = ruleRegistry.get(ruleUID);
        if (rule == null) {
            return "Rule '" + ruleUID + "' not found";
        }
        switch (promptType) {
            case "status":
                return "Status of rule " + ruleUID + ": name=" + rule.getName() + ", tags=" + rule.getTags();
            case "execution":
                return "Execution prompt for rule " + ruleUID + ". Arguments: parameters(JSON), confirm(optional)";
            case "config":
                return "Config prompt for rule " + ruleUID + ". Arguments: operation(get|set|update), property, value";
            case "discovery":
                return "Discovery prompt: list rules with filters (tag, status)";
            case "creation":
                return "Creation prompt: create rule with name, triggers, conditions, actions";
            case "enable":
                return "Enable/Disable prompt: set enabled flag for rule " + ruleUID;
            default:
                return "Prompt for rule " + ruleUID + " (" + promptType + ")";
        }
    }
}
