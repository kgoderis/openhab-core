package org.openhab.core.ai.tool.prompts;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.tool.prompts.adapter.ItemPromptAdapter;
import org.openhab.core.ai.tool.prompts.adapter.RulePromptAdapter;
import org.openhab.core.ai.tool.prompts.adapter.SystemPromptAdapter;
import org.openhab.core.ai.tool.prompts.api.PromptContext;
import org.openhab.core.ai.tool.prompts.api.PromptResult;
import org.openhab.core.automation.RuleRegistry;
import org.openhab.core.items.ItemRegistry;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Prompt Execution Service for MCP Prompts
 *
 * Delegates prompt generation/execution to specific prompt adapters
 * and tracks simple performance metrics.
 *
 * Author: Karel Goderis - Initial Contribution
 */
@NonNullByDefault
@Component(immediate = true)
public class PromptExecutionService {

    private static final Logger logger = LoggerFactory.getLogger(PromptExecutionService.class);

    private final Map<String, Long> lastExecutionMs = new ConcurrentHashMap<>();
    private final AtomicLong totalExecutions = new AtomicLong(0);
    private final AtomicLong totalTimeMs = new AtomicLong(0);

    private @Nullable ItemPromptAdapter itemPromptAdapter;
    private @Nullable RulePromptAdapter rulePromptAdapter;
    private final SystemPromptAdapter systemPromptAdapter = new SystemPromptAdapter();

    @Reference
    public void setItemRegistry(ItemRegistry itemRegistry) {
        this.itemPromptAdapter = new ItemPromptAdapter(itemRegistry);
    }

    public void unsetItemRegistry(ItemRegistry itemRegistry) {
        this.itemPromptAdapter = null;
    }

    @Reference
    public void setRuleRegistry(RuleRegistry ruleRegistry) {
        this.rulePromptAdapter = new RulePromptAdapter(ruleRegistry);
    }

    public void unsetRuleRegistry(RuleRegistry ruleRegistry) {
        this.rulePromptAdapter = null;
    }

    @Activate
    protected void activate() {
        logger.debug("Activating PromptExecutionService");
    }

    @Deactivate
    protected void deactivate() {
        logger.debug("Deactivating PromptExecutionService");
    }

    public PromptResult executeItemPrompt(String itemName, String operation, Map<String, Object> parameters,
            PromptContext context) {
        long start = System.currentTimeMillis();
        try {
            ItemPromptAdapter adapter = itemPromptAdapter;
            if (adapter == null) {
                return PromptResult.failure("ItemPromptAdapter not available", System.currentTimeMillis() - start);
            }
            PromptResult result = adapter.execute(itemName, operation, parameters, context);
            record("item:" + operation, start, result.isSuccess());
            return result;
        } catch (Exception e) {
            logger.error("Prompt execution failed for item: {} op:{}", itemName, operation, e);
            return PromptResult.failure("Execution error: " + e.getMessage(), System.currentTimeMillis() - start);
        }
    }

    public PromptResult executeRulePrompt(String ruleUID, String operation, Map<String, Object> parameters,
            PromptContext context) {
        long start = System.currentTimeMillis();
        try {
            RulePromptAdapter adapter = rulePromptAdapter;
            if (adapter == null) {
                return PromptResult.failure("RulePromptAdapter not available", System.currentTimeMillis() - start);
            }
            PromptResult result = adapter.execute(ruleUID, operation, parameters, context);
            record("rule:" + operation, start, result.isSuccess());
            return result;
        } catch (Exception e) {
            logger.error("Prompt execution failed for rule: {} op:{}", ruleUID, operation, e);
            return PromptResult.failure("Execution error: " + e.getMessage(), System.currentTimeMillis() - start);
        }
    }

    public PromptResult executeSystemPrompt(String promptId, String operation, Map<String, Object> parameters,
            PromptContext context) {
        long start = System.currentTimeMillis();
        try {
            PromptResult result = systemPromptAdapter.execute(promptId, operation, parameters, context);
            record("system:" + operation, start, result.isSuccess());
            return result;
        } catch (Exception e) {
            logger.error("Prompt execution failed for system: {} op:{}", promptId, operation, e);
            return PromptResult.failure("Execution error: " + e.getMessage(), System.currentTimeMillis() - start);
        }
    }

    private void record(String key, long start, boolean success) {
        long took = System.currentTimeMillis() - start;
        lastExecutionMs.put(key, took);
        totalExecutions.incrementAndGet();
        totalTimeMs.addAndGet(took);
        if (!success) {
            logger.warn("Prompt execution failed: {} ({} ms)", key, took);
        }
    }
}
