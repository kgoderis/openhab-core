package org.openhab.core.ai.tool.prompts.adapter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.adapter.PromptAdapter;
import org.openhab.core.ai.tool.prompts.api.PromptContext;
import org.openhab.core.ai.tool.prompts.api.PromptResult;
import org.openhab.core.ai.tool.prompts.api.dto.Prompt;
import org.openhab.core.ai.tool.prompts.api.dto.PromptArgument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Prompt adapter for openHAB configuration using the unified adapter hierarchy.
 *
 * This adapter provides prompt generation for openHAB configuration operations.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ConfigurationPromptAdapter extends PromptAdapter<Prompt, PromptContext, PromptResult> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationPromptAdapter.class);
    private static final long DEFAULT_REFRESH_INTERVAL_MS = 5 * 60 * 1000; // 5 minutes

    private final Map<String, String> cache = new ConcurrentHashMap<>();

    public ConfigurationPromptAdapter() {
        super(DEFAULT_REFRESH_INTERVAL_MS);
    }

    @Override
    public @Nullable Prompt createEntity(String identifier, PromptContext context) {
        String name = "Configuration: " + identifier;
        String description = "Configuration prompt for: " + identifier;
        PromptArgument[] args = new PromptArgument[] {
                new PromptArgument("operation", "Operation (get, set, update)", true),
                new PromptArgument("property", "Property to configure", false),
                new PromptArgument("value", "Value to set", false),
                new PromptArgument("confirm", "Confirm the change", false) };
        return new Prompt(name, description, List.of(args));
    }

    @Override
    public @Nullable String getContent(String identifier, PromptContext context) {
        String existing = cache.get(identifier);
        if (existing == null || needsRefresh()) {
            existing = buildConfigContent(identifier, context);
            cache.put(identifier, existing);
            updateRefreshTime();
        }
        return existing;
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
        return true;
    }

    @Override
    public PromptResult execute(String identifier, String operation, Map<String, Object> parameters,
            PromptContext context) {
        long start = System.currentTimeMillis();
        try {
            String content = buildConfigContent(identifier, context);
            return PromptResult.success(content, System.currentTimeMillis() - start);
        } catch (Exception e) {
            LOGGER.error("Error executing configuration prompt: {}", identifier, e);
            return PromptResult.failure("Execution failed: " + e.getMessage(), System.currentTimeMillis() - start);
        }
    }

    @Override
    public void refresh(String identifier, PromptContext context) {
        cache.remove(identifier);
        updateRefreshTime();
    }

    @Override
    public void cleanup() {
        cache.clear();
    }

    @Override
    public String getAdapterType() {
        return "prompts/configuration";
    }

    @Override
    public String getUriPattern() {
        return "openhab://prompts/config/{configId}";
    }

    @Override
    public @Nullable PromptResult adapt(Prompt source, PromptContext context) {
        // For prompt adapters, we typically don't adapt existing prompts
        // but rather create new ones or execute operations
        return null;
    }

    @Override
    public boolean canAdapt(Prompt source) {
        // Check if this adapter can handle the given prompt
        return source != null && source.getDescription().contains("Configuration");
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
    public String generatePrompt(String identifier, PromptContext context) {
        return buildConfigContent(identifier, context);
    }

    @Override
    protected void doRefresh(String identifier, PromptContext context) {
        refresh(identifier, context);
    }

    public void close() {
        cleanup();
    }

    private String buildConfigContent(String configId, PromptContext context) {
        String operation = (String) context.getProperty("operation");
        if (operation == null) {
            operation = "get";
        }
        switch (operation) {
            case "get":
                return "Config GET prompt for " + configId + ": property, format(optional)";
            case "set":
                return "Config SET prompt for " + configId + ": property, value, confirm(optional)";
            case "update":
                return "Config UPDATE prompt for " + configId + ": properties(JSON), confirm(optional)";
            case "reset":
                return "Config RESET prompt for " + configId + ": property(optional), confirm";
            default:
                return "Configuration prompt for " + configId + " (" + operation + ")";
        }
    }
}
