package org.openhab.core.ai.tool.proxy;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.tool.dto.AbstractResource;
import org.openhab.core.automation.Rule;
import org.openhab.core.automation.RuleRegistry;

/**
 * Encapsulated resource proxy for openHAB rules.
 * 
 * This class extends AbstractResource to provide proper encapsulation,
 * lifecycle management, and state caching for openHAB rules.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class RuleResourceProxy extends AbstractResource {

    private final RuleRegistry ruleRegistry;
    private final String ruleUID;
    private volatile @Nullable Rule cachedRule;
    private volatile @Nullable String cachedContent;

    /**
     * Create a new RuleResourceProxy.
     *
     * @param ruleRegistry the rule registry
     * @param ruleUID the UID of the rule
     * @param refreshIntervalMs the refresh interval in milliseconds
     */
    public RuleResourceProxy(RuleRegistry ruleRegistry, String ruleUID, long refreshIntervalMs) {
        super("openhab://rules/" + ruleUID, "Rule: " + ruleUID, "Resource adapter for openHAB rule: " + ruleUID,
                "application/json", createMetadata(ruleUID), refreshIntervalMs);

        this.ruleRegistry = ruleRegistry;
        this.ruleUID = ruleUID;
    }

    /**
     * Create metadata for the rule.
     *
     * @param ruleUID the rule UID
     * @return the metadata map
     */
    private static Map<String, Object> createMetadata(String ruleUID) {
        Map<String, Object> metadata = new ConcurrentHashMap<>();
        metadata.put("type", "openhab-rule");
        metadata.put("ruleUID", ruleUID);
        metadata.put("uri", "openhab://rules/" + ruleUID);
        return metadata;
    }

    @Override
    public @Nullable String getContent() {
        if (needsRefresh()) {
            refresh();
        }
        return cachedContent;
    }

    @Override
    public boolean isWritable() {
        if (needsRefresh()) {
            refresh();
        }
        // Rules are generally writable through the rule registry
        return cachedRule != null;
    }

    @Override
    public boolean writeContent(@Nullable String content) {
        if (!isWritable()) {
            LOGGER.warn("Rule is not writable: {}", ruleUID);
            return false;
        }

        if (content == null || content.trim().isEmpty()) {
            LOGGER.warn("Content is null or empty for rule: {}", ruleUID);
            return false;
        }

        try {
            // Parse JSON content and update rule configuration
            // This is a simplified implementation - in practice, you'd want to use a proper JSON parser
            if (content.contains("\"configuration\":")) {
                // Note: This would need to be implemented with proper openHAB rule configuration handling
                LOGGER.info("Would update rule {} configuration", ruleUID);

                // Refresh the cache after write
                refresh();
                return true;
            }

            LOGGER.warn("Invalid content format for rule: {}", ruleUID);
            return false;
        } catch (Exception e) {
            LOGGER.error("Error writing content to rule: {}", ruleUID, e);
            return false;
        }
    }

    @Override
    public boolean exists() {
        if (needsRefresh()) {
            refresh();
        }
        return cachedRule != null;
    }

    @Override
    public void refresh() {
        try {
            Rule rule = ruleRegistry.get(ruleUID);
            if (rule == null) {
                LOGGER.debug("Rule not found during refresh: {}", ruleUID);
                cachedRule = null;
                cachedContent = null;
                markInvalid();
                return;
            }

            cachedRule = rule;
            cachedContent = createRuleJson(rule);

            // Update metadata with current rule information
            metadata.put("name", rule.getName());
            metadata.put("description", rule.getDescription());
            metadata.put("tags", rule.getTags());
            metadata.put("enabled", true); // TODO: Check actual enabled status

            updateRefreshTime();
            LOGGER.debug("Refreshed rule resource: {}", ruleUID);
        } catch (Exception e) {
            LOGGER.error("Error refreshing rule resource: {}", ruleUID, e);
            markInvalid();
        }
    }

    @Override
    public void close() {
        LOGGER.debug("Closing rule resource: {}", ruleUID);
        cachedRule = null;
        cachedContent = null;
        markInvalid();
    }

    /**
     * Create JSON representation of rule configuration and metadata.
     *
     * @param rule the rule to convert to JSON
     * @return the JSON string
     */
    private String createRuleJson(Rule rule) {
        StringBuilder content = new StringBuilder();
        content.append("{");
        content.append("\"uid\":\"").append(rule.getUID()).append("\",");
        content.append("\"name\":\"").append(rule.getName()).append("\",");
        content.append("\"enabled\":").append(true).append(","); // TODO: Check actual enabled status

        if (rule.getDescription() != null) {
            content.append("\"description\":\"").append(rule.getDescription()).append("\",");
        }

        content.append("\"tags\":[");
        boolean first = true;
        for (String tag : rule.getTags()) {
            if (!first) {
                content.append(",");
            }
            content.append("\"").append(tag).append("\"");
            first = false;
        }
        content.append("]");
        content.append("}");

        return content.toString();
    }

    /**
     * Get the underlying rule.
     *
     * @return the rule or null if not available
     */
    public @Nullable Rule getRule() {
        if (needsRefresh()) {
            refresh();
        }
        return cachedRule;
    }

    /**
     * Get the rule UID.
     *
     * @return the rule UID
     */
    public String getRuleUID() {
        return ruleUID;
    }
}
