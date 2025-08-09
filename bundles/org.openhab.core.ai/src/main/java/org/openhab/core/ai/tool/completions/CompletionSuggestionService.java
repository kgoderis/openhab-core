package org.openhab.core.ai.tool.completions;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.tool.api.CompletionContext;
import org.openhab.core.ai.tool.api.CompletionResult;
import org.openhab.core.ai.tool.completions.adapter.CommandCompletionAdapter;
import org.openhab.core.ai.tool.completions.adapter.ConfigurationCompletionAdapter;
import org.openhab.core.ai.tool.completions.adapter.ItemCompletionAdapter;
import org.openhab.core.ai.tool.completions.adapter.RuleCompletionAdapter;
import org.openhab.core.automation.RuleRegistry;
import org.openhab.core.items.ItemRegistry;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Completion Suggestion Service for MCP Completions
 *
 * Delegates completion suggestion generation to specific completion adapters
 * and tracks simple performance metrics.
 *
 * Author: Karel Goderis - Initial Contribution
 */
@NonNullByDefault
@Component(immediate = true)
public class CompletionSuggestionService {

    private static final Logger logger = LoggerFactory.getLogger(CompletionSuggestionService.class);

    private final Map<String, Long> lastExecutionMs = new ConcurrentHashMap<>();
    private final AtomicLong totalExecutions = new AtomicLong(0);
    private final AtomicLong totalTimeMs = new AtomicLong(0);

    private @Nullable ItemCompletionAdapter itemCompletionAdapter;
    private @Nullable RuleCompletionAdapter ruleCompletionAdapter;
    private @Nullable CommandCompletionAdapter commandCompletionAdapter;
    private final ConfigurationCompletionAdapter configurationCompletionAdapter = new ConfigurationCompletionAdapter();

    @Reference
    public void setItemRegistry(ItemRegistry itemRegistry) {
        this.itemCompletionAdapter = new ItemCompletionAdapter(itemRegistry);
        this.commandCompletionAdapter = new CommandCompletionAdapter(itemRegistry);
    }

    public void unsetItemRegistry(ItemRegistry itemRegistry) {
        this.itemCompletionAdapter = null;
        this.commandCompletionAdapter = null;
    }

    @Reference
    public void setRuleRegistry(RuleRegistry ruleRegistry) {
        this.ruleCompletionAdapter = new RuleCompletionAdapter(ruleRegistry);
    }

    public void unsetRuleRegistry(RuleRegistry ruleRegistry) {
        this.ruleCompletionAdapter = null;
    }

    @Activate
    protected void activate() {
        logger.debug("Activating CompletionSuggestionService");
    }

    @Deactivate
    protected void deactivate() {
        logger.debug("Deactivating CompletionSuggestionService");
    }

    public CompletionResult suggestItem(String itemName, String operation, Map<String, Object> parameters,
            CompletionContext context) {
        long start = System.currentTimeMillis();
        try {
            ItemCompletionAdapter adapter = itemCompletionAdapter;
            if (adapter == null) {
                return CompletionResult.failure("ItemCompletionAdapter not available",
                        System.currentTimeMillis() - start);
            }
            CompletionResult result = adapter.execute(itemName, operation, parameters, context);
            record("item:" + operation, start, result.isSuccess());
            return result;
        } catch (Exception e) {
            logger.error("Completion suggestion failed for item: {} op:{}", itemName, operation, e);
            return CompletionResult.failure("Suggestion error: " + e.getMessage(), System.currentTimeMillis() - start);
        }
    }

    public CompletionResult suggestRule(String ruleUID, String operation, Map<String, Object> parameters,
            CompletionContext context) {
        long start = System.currentTimeMillis();
        try {
            RuleCompletionAdapter adapter = ruleCompletionAdapter;
            if (adapter == null) {
                return CompletionResult.failure("RuleCompletionAdapter not available",
                        System.currentTimeMillis() - start);
            }
            CompletionResult result = adapter.execute(ruleUID, operation, parameters, context);
            record("rule:" + operation, start, result.isSuccess());
            return result;
        } catch (Exception e) {
            logger.error("Completion suggestion failed for rule: {} op:{}", ruleUID, operation, e);
            return CompletionResult.failure("Suggestion error: " + e.getMessage(), System.currentTimeMillis() - start);
        }
    }

    public CompletionResult suggestCommands(String contextKey, String operation, Map<String, Object> parameters,
            CompletionContext context) {
        long start = System.currentTimeMillis();
        try {
            CommandCompletionAdapter adapter = commandCompletionAdapter;
            if (adapter == null) {
                return CompletionResult.failure("CommandCompletionAdapter not available",
                        System.currentTimeMillis() - start);
            }
            CompletionResult result = adapter.execute(contextKey, operation, parameters, context);
            record("commands:" + operation, start, result.isSuccess());
            return result;
        } catch (Exception e) {
            logger.error("Completion suggestion failed for commands: {} op:{}", contextKey, operation, e);
            return CompletionResult.failure("Suggestion error: " + e.getMessage(), System.currentTimeMillis() - start);
        }
    }

    public CompletionResult suggestConfiguration(String configId, String operation, Map<String, Object> parameters,
            CompletionContext context) {
        long start = System.currentTimeMillis();
        try {
            CompletionResult result = configurationCompletionAdapter.execute(configId, operation, parameters, context);
            record("config:" + operation, start, result.isSuccess());
            return result;
        } catch (Exception e) {
            logger.error("Completion suggestion failed for config: {} op:{}", configId, operation, e);
            return CompletionResult.failure("Suggestion error: " + e.getMessage(), System.currentTimeMillis() - start);
        }
    }

    private void record(String key, long start, boolean success) {
        long took = System.currentTimeMillis() - start;
        lastExecutionMs.put(key, took);
        totalExecutions.incrementAndGet();
        totalTimeMs.addAndGet(took);
        if (!success) {
            logger.warn("Completion suggestion failed: {} ({} ms)", key, took);
        }
    }
}
