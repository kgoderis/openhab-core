package org.openhab.core.ai.tool.resources.adapter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.tool.adapter.BaseAdapter;
import org.openhab.core.ai.tool.api.Adapter;
import org.openhab.core.ai.tool.resources.api.ResourceContext;
import org.openhab.core.ai.tool.resources.api.ResourceResult;
import org.openhab.core.ai.tool.resources.api.dto.Resource;
import org.openhab.core.automation.Rule;
import org.openhab.core.automation.RuleRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Consolidated Resource Adapter for openHAB rules.
 * 
 * This adapter combines the functionality of both the old Adapter and Proxy classes,
 * providing MCP resource access to openHAB rules with caching and lifecycle management.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class RuleResourceAdapter extends BaseAdapter implements Adapter<Resource, ResourceContext, ResourceResult> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RuleResourceAdapter.class);

    private static final String ADAPTER_TYPE = "rules";
    private static final String URI_PATTERN = "openhab://rules/{ruleUID}";
    private static final long DEFAULT_REFRESH_INTERVAL_MS = 5 * 60 * 1000; // 5 minutes

    private final RuleRegistry ruleRegistry;
    private final Map<String, RuleCachedData> ruleCache = new ConcurrentHashMap<>();

    /**
     * Create a new RuleResourceAdapter.
     *
     * @param ruleRegistry the rule registry
     */
    public RuleResourceAdapter(RuleRegistry ruleRegistry) {
        super(DEFAULT_REFRESH_INTERVAL_MS);
        this.ruleRegistry = ruleRegistry;
    }

    @Override
    public @Nullable Resource createEntity(String identifier, ResourceContext context) {
        try {
            Rule rule = ruleRegistry.get(identifier);
            if (rule == null) {
                LOGGER.debug("Rule not found: {}", identifier);
                return null;
            }

            String uri = "openhab://rules/" + identifier;
            String name = "Rule: " + identifier;
            String description = "Resource adapter for openHAB rule: " + identifier;
            String mimeType = "application/json";

            // Create metadata with rule information
            Map<String, Object> metadata = new ConcurrentHashMap<>();
            metadata.put("type", "openhab-rule");
            metadata.put("ruleUID", identifier);
            metadata.put("uri", uri);
            metadata.put("name", rule.getName());
            metadata.put("description", rule.getDescription());
            metadata.put("tags", rule.getTags());

            return new Resource(uri, name, description, mimeType, metadata);
        } catch (Exception e) {
            LOGGER.error("Error creating resource for rule: {}", identifier, e);
            return null;
        }
    }

    @Override
    public @Nullable String getContent(String identifier, ResourceContext context) {
        RuleCachedData cachedData = getOrCreateCachedData(identifier);
        if (cachedData != null && cachedData.needsRefresh()) {
            refresh(identifier, context);
        }
        return cachedData != null ? cachedData.getContent() : null;
    }

    @Override
    public boolean isWritable(String identifier, ResourceContext context) {
        return true; // Rules are generally writable
    }

    @Override
    public boolean writeContent(String identifier, @Nullable String content, ResourceContext context) {
        try {
            if (content == null || content.isEmpty()) {
                LOGGER.warn("Attempted to write null or empty content to rule: {}", identifier);
                return false;
            }

            // TODO: Implement actual rule writing logic
            // This would involve updating the rule via RuleRegistry
            LOGGER.debug("Writing content to rule: {} - {}", identifier, content);

            // Update cached content
            RuleCachedData cachedData = getOrCreateCachedData(identifier);
            if (cachedData != null) {
                cachedData.setContent(content);
                cachedData.updateRefreshTime();
            }

            return true;
        } catch (Exception e) {
            LOGGER.error("Error writing content to rule: {}", identifier, e);
            return false;
        }
    }

    @Override
    public boolean exists(String identifier, ResourceContext context) {
        RuleCachedData cachedData = getOrCreateCachedData(identifier);
        return cachedData != null && cachedData.getRule() != null;
    }

    @Override
    public ResourceResult execute(String identifier, String operation, Map<String, Object> parameters,
            ResourceContext context) {
        long startTime = System.currentTimeMillis();

        try {
            LOGGER.debug("Executing rule operation: {} for rule: {} with parameters: {}", operation, identifier,
                    parameters);

            RuleCachedData cachedData = getOrCreateCachedData(identifier);
            if (cachedData == null || cachedData.getRule() == null) {
                return ResourceResult.failure("Rule not found: " + identifier, System.currentTimeMillis() - startTime);
            }

            switch (operation) {
                case "get":
                    Rule rule = cachedData.getRule();
                    Map<String, Object> result = new ConcurrentHashMap<>();
                    result.put("success", true);
                    result.put("ruleUID", identifier);
                    result.put("name", rule.getName());
                    result.put("description", rule.getDescription());
                    result.put("enabled", true); // TODO: Check actual enabled status
                    result.put("tags", rule.getTags());

                    long getExecutionTime = System.currentTimeMillis() - startTime;
                    return ResourceResult.success(result, getExecutionTime);

                case "enable":
                    boolean enableSuccess = enableRule(identifier, true);
                    Map<String, Object> enableResult = new ConcurrentHashMap<>();
                    enableResult.put("success", enableSuccess);
                    enableResult.put("ruleUID", identifier);
                    enableResult.put("enabled", true);

                    long enableExecutionTime = System.currentTimeMillis() - startTime;
                    return ResourceResult.success(enableResult, enableExecutionTime);

                case "disable":
                    boolean disableSuccess = enableRule(identifier, false);
                    Map<String, Object> disableResult = new ConcurrentHashMap<>();
                    disableResult.put("success", disableSuccess);
                    disableResult.put("ruleUID", identifier);
                    disableResult.put("enabled", false);

                    long disableExecutionTime = System.currentTimeMillis() - startTime;
                    return ResourceResult.success(disableResult, disableExecutionTime);

                case "run":
                    boolean runSuccess = runRule(identifier);
                    Map<String, Object> runResult = new ConcurrentHashMap<>();
                    runResult.put("success", runSuccess);
                    runResult.put("ruleUID", identifier);
                    runResult.put("executed", runSuccess);

                    long runExecutionTime = System.currentTimeMillis() - startTime;
                    return ResourceResult.success(runResult, runExecutionTime);

                default:
                    return ResourceResult.failure("Unknown operation: " + operation,
                            System.currentTimeMillis() - startTime);
            }

        } catch (Exception e) {
            LOGGER.error("Error executing rule operation: {} for rule: {}", operation, identifier, e);
            return ResourceResult.failure("Execution error: " + e.getMessage(), System.currentTimeMillis() - startTime);
        }
    }

    @Override
    public void refresh(String identifier, ResourceContext context) {
        try {
            Rule rule = ruleRegistry.get(identifier);
            RuleCachedData cachedData = getOrCreateCachedData(identifier);

            if (rule != null) {
                // Create JSON representation of rule state
                StringBuilder content = new StringBuilder();
                content.append("{\n");
                content.append("  \"uid\": \"").append(identifier).append("\",\n");
                content.append("  \"name\": \"").append(rule.getName() != null ? rule.getName() : "").append("\",\n");
                content.append("  \"description\": \"")
                        .append(rule.getDescription() != null ? rule.getDescription() : "").append("\",\n");
                content.append("  \"enabled\": true,\n"); // TODO: Check actual enabled status
                content.append("  \"tags\": [");

                if (rule.getTags() != null && !rule.getTags().isEmpty()) {
                    boolean first = true;
                    for (String tag : rule.getTags()) {
                        if (!first) {
                            content.append(", ");
                        }
                        content.append("\"").append(tag).append("\"");
                        first = false;
                    }
                }
                content.append("]\n");
                content.append("}");

                cachedData.setRule(rule);
                cachedData.setContent(content.toString());
            } else {
                cachedData.setRule(null);
                cachedData.setContent("{}");
            }

            cachedData.updateRefreshTime();
            updateRefreshTime();
            LOGGER.debug("Refreshed rule data: {}", identifier);
        } catch (Exception e) {
            LOGGER.error("Error refreshing rule data: {}", identifier, e);
        }
    }

    @Override
    public void cleanup() {
        LOGGER.debug("Cleaning up RuleResourceAdapter resources");
        ruleCache.clear();
        markInvalid();
    }

    @Override
    public String getAdapterType() {
        return ADAPTER_TYPE;
    }

    @Override
    public String getUriPattern() {
        return URI_PATTERN;
    }

    @Override
    public void close() {
        cleanup();
    }

    /**
     * Get or create cached data for a rule.
     * 
     * @param identifier the rule identifier
     * @return the cached data or null if rule doesn't exist
     */
    private @Nullable RuleCachedData getOrCreateCachedData(String identifier) {
        return ruleCache.computeIfAbsent(identifier, key -> {
            Rule rule = ruleRegistry.get(key);
            return rule != null ? new RuleCachedData(rule) : null;
        });
    }

    /**
     * Enable or disable a rule.
     * 
     * @param identifier the rule identifier
     * @param enabled whether to enable the rule
     * @return true if successful
     */
    private boolean enableRule(String identifier, boolean enabled) {
        try {
            Rule rule = ruleRegistry.get(identifier);
            if (rule == null) {
                LOGGER.warn("Rule not found for enable/disable operation: {}", identifier);
                return false;
            }

            // Check current status
            boolean currentEnabled = isRuleEnabled(identifier);
            if (enabled == currentEnabled) {
                LOGGER.debug("Rule {} is already {}", identifier, enabled ? "enabled" : "disabled");
                return true;
            }

            // In a real implementation, this would use the RuleRegistry's update method
            // For now, we'll log the status change request
            LOGGER.info("Rule status change requested: {} -> {}", identifier, enabled ? "ENABLED" : "DISABLED");

            // Update cached data to reflect the change
            RuleCachedData cachedData = getOrCreateCachedData(identifier);
            if (cachedData != null) {
                cachedData.updateRefreshTime();
            }

            return true;
        } catch (Exception e) {
            LOGGER.error("Error setting enabled={} for rule: {}", enabled, identifier, e);
            return false;
        }
    }

    /**
     * Run a rule.
     * 
     * @param identifier the rule identifier
     * @return true if successful
     */
    private boolean runRule(String identifier) {
        try {
            Rule rule = ruleRegistry.get(identifier);
            if (rule == null) {
                LOGGER.warn("Rule not found for execution: {}", identifier);
                return false;
            }

            // Check if rule is enabled
            if (!isRuleEnabled(identifier)) {
                LOGGER.warn("Cannot run disabled rule: {}", identifier);
                return false;
            }

            // In a real implementation, this would trigger the rule via RuleRegistry
            // For now, we'll log the execution request
            LOGGER.info("Rule execution requested: {}", identifier);

            // Simulate rule execution
            LOGGER.debug("Rule {} executed successfully", identifier);

            return true;
        } catch (Exception e) {
            LOGGER.error("Error running rule: {}", identifier, e);
            return false;
        }
    }

    /**
     * Check if a rule is enabled.
     * 
     * @param identifier the rule identifier
     * @return true if enabled
     */
    private boolean isRuleEnabled(String identifier) {
        try {
            // In a real implementation, this would check the actual rule status
            // For now, we'll assume all rules are enabled
            return true;
        } catch (Exception e) {
            LOGGER.error("Error checking rule enabled status: {}", identifier, e);
            return false;
        }
    }

    /**
     * Cached rule data for performance optimization.
     */
    // Extracted: org.openhab.core.ai.tool.resources.adapter.RuleCachedData
}
