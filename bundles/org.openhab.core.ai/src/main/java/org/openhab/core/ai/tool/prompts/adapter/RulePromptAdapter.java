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
import org.openhab.core.automation.Rule;
import org.openhab.core.automation.RuleRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adapter for creating prompts from openHAB Rules.
 * 
 * This adapter creates prompts based on openHAB Rules, providing
 * contextual information about rule status, triggers, and actions.
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
        PromptArgument[] args = new PromptArgument[] { new PromptArgument("ruleUID", "UID of the rule", true),
                new PromptArgument("promptType", "Type of prompt", false),
                new PromptArgument("includeTriggers", "Include trigger information", false) };
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
        return "prompts/rules";
    }

    @Override
    public String getUriPattern() {
        return "openhab://prompts/rules/{ruleUID}";
    }

    @Override
    public boolean canAdapt(org.openhab.core.ai.tool.prompts.api.dto.Prompt source) {
        return source != null && source.getName().toLowerCase().contains("rule");
    }

    @Override
    public org.openhab.core.ai.tool.prompts.api.PromptResult adapt(
            org.openhab.core.ai.tool.prompts.api.dto.Prompt source,
            org.openhab.core.ai.tool.prompts.api.PromptContext context) {
        if (!canAdapt(source)) {
            return PromptResult.failure("Cannot adapt source", 0);
        }
        return execute(source.getName(), "rule", Map.of(), context);
    }

    @Override
    public Class<org.openhab.core.ai.tool.prompts.api.dto.Prompt> getSourceType() {
        return Prompt.class;
    }

    @Override
    public Class<org.openhab.core.ai.tool.prompts.api.PromptResult> getResultType() {
        return PromptResult.class;
    }

    @Override
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

    private String buildPromptContent(String ruleUID, String promptType) {
        Rule rule = ruleRegistry.get(ruleUID);
        if (rule == null) {
            return "Rule not found: " + ruleUID;
        }

        StringBuilder content = new StringBuilder();
        content.append("Rule: ").append(rule.getName()).append(" (").append(ruleUID).append(")\n");
        content.append("Description: ").append(rule.getDescription() != null ? rule.getDescription() : "").append("\n");

        switch (promptType.toLowerCase()) {
            case "triggers":
                content.append("Triggers:\n");
                rule.getTriggers().forEach(trigger -> {
                    content.append("  - ").append(trigger.getTypeUID()).append(": ").append(trigger.getConfiguration())
                            .append("\n");
                });
                break;
            case "actions":
                content.append("Actions:\n");
                rule.getActions().forEach(action -> {
                    content.append("  - ").append(action.getTypeUID()).append(": ").append(action.getConfiguration())
                            .append("\n");
                });
                break;
            case "conditions":
                content.append("Conditions:\n");
                rule.getConditions().forEach(condition -> {
                    content.append("  - ").append(condition.getTypeUID()).append(": ")
                            .append(condition.getConfiguration()).append("\n");
                });
                break;
            default: // status
                content.append("Tags: ").append(rule.getTags()).append("\n");
                content.append("Visibility: ").append(rule.getVisibility()).append("\n");
                break;
        }

        return content.toString();
    }
}
