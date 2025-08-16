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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Prompt adapter for system information.
 *
 * Consolidated: proxy logic folded into this adapter; no external factory used.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class SystemPromptAdapter extends BaseAdapter implements Adapter<Prompt, PromptContext, PromptResult> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SystemPromptAdapter.class);
    private static final long DEFAULT_REFRESH_INTERVAL_MS = 5 * 60 * 1000; // 5 minutes

    private final Map<String, String> cache = new ConcurrentHashMap<>();

    public SystemPromptAdapter() {
        super(DEFAULT_REFRESH_INTERVAL_MS);
    }

    @Override
    public @Nullable Prompt createEntity(String identifier, PromptContext context) {
        String name = "System: " + identifier;
        String description = "System information prompt: " + identifier;
        PromptArgument[] args = new PromptArgument[] {
                new PromptArgument("includeItems", "Include item status information", false),
                new PromptArgument("includeThings", "Include thing status information", false),
                new PromptArgument("includeRules", "Include rule status information", false),
                new PromptArgument("format", "Output format (json, xml, text)", false) };
        return new Prompt(name, description, List.of(args));
    }

    @Override
    public @Nullable String getContent(String identifier, PromptContext context) {
        String existing = cache.get(identifier);
        if (existing == null || needsRefresh()) {
            existing = buildSystemContent(identifier, context);
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
            String content = buildSystemContent(identifier, context);
            return PromptResult.success(content, System.currentTimeMillis() - start);
        } catch (Exception e) {
            LOGGER.error("Error executing system prompt: {}", identifier, e);
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
        return "prompts/system";
    }

    @Override
    public String getUriPattern() {
        return "openhab://prompts/system/{promptId}";
    }

    public void close() {
        cleanup();
    }

    private String buildSystemContent(String promptId, PromptContext context) {
        String version = "5.0.0";
        String status = "running";
        StringBuilder sb = new StringBuilder();
        sb.append("System Prompt ").append(promptId).append("\n");
        sb.append("system=openHAB, version=").append(version).append(", status=").append(status);
        return sb.toString();
    }
}
