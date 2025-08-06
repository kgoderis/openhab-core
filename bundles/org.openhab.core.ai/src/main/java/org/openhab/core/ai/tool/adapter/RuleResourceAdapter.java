package org.openhab.core.ai.tool.adapter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.tool.dto.AbstractResource;
import org.openhab.core.ai.tool.dto.Resource;
import org.openhab.core.ai.tool.factory.ResourceFactory;
import org.openhab.core.ai.tool.proxy.RuleResourceProxy;
import org.openhab.core.automation.Rule;
import org.openhab.core.automation.RuleRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resource adapter for openHAB rules.
 * 
 * This adapter provides MCP resource access to openHAB rules, allowing
 * reading and writing of rule configurations and metadata.
 * 
 * Updated to use encapsulated architecture internally while maintaining
 * the same public interface for backward compatibility.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class RuleResourceAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(RuleResourceAdapter.class);

    private final RuleRegistry ruleRegistry;
    private final ResourceFactory resourceFactory;

    /**
     * Create a new RuleResourceAdapter.
     *
     * @param ruleRegistry the rule registry
     */
    public RuleResourceAdapter(RuleRegistry ruleRegistry) {
        this.ruleRegistry = ruleRegistry;
        this.resourceFactory = new ResourceFactory();
    }

    /**
     * Create a resource for an openHAB rule.
     *
     * @param ruleUID the UID of the rule
     * @return the resource or null if the rule doesn't exist
     */
    public @Nullable Resource createRuleResource(String ruleUID) {
        try {
            Rule rule = ruleRegistry.get(ruleUID);
            if (rule == null) {
                LOGGER.debug("Rule not found: {}", ruleUID);
                return null;
            }

            String uri = "openhab://rules/" + ruleUID;
            String name = "Rule: " + rule.getName();
            String description = "Resource adapter for openHAB rule: " + rule.getName();
            String mimeType = "application/json";

            // Create metadata with rule information
            Map<String, Object> metadata = new ConcurrentHashMap<>();
            metadata.put("type", "openhab-rule");
            metadata.put("ruleUID", ruleUID);
            metadata.put("uri", uri);
            metadata.put("name", rule.getName());
            metadata.put("description", rule.getDescription());
            metadata.put("tags", rule.getTags());
            metadata.put("enabled", true); // TODO: Check actual enabled status

            return new Resource(uri, name, description, mimeType, metadata);
        } catch (Exception e) {
            LOGGER.error("Error creating resource for rule: {}", ruleUID, e);
            return null;
        }
    }

    /**
     * Get resource content for a rule.
     *
     * @param ruleUID the UID of the rule
     * @return the rule content as a string or null if the rule doesn't exist
     */
    public @Nullable String getRuleContent(String ruleUID) {
        AbstractResource resource = getOrCreateEncapsulatedResource(ruleUID);
        return resource != null ? resource.getContent() : null;
    }

    /**
     * Check if a rule is writable.
     *
     * @param ruleUID the UID of the rule
     * @return true if the rule is writable
     */
    public boolean isRuleWritable(String ruleUID) {
        AbstractResource resource = getOrCreateEncapsulatedResource(ruleUID);
        return resource != null && resource.isWritable();
    }

    /**
     * Write content to a rule.
     *
     * @param ruleUID the UID of the rule
     * @param content the content to write
     * @return true if successful
     */
    public boolean writeRuleContent(String ruleUID, @Nullable String content) {
        AbstractResource resource = getOrCreateEncapsulatedResource(ruleUID);
        return resource != null && resource.writeContent(content);
    }

    /**
     * Check if a rule exists.
     *
     * @param ruleUID the UID of the rule
     * @return true if the rule exists
     */
    public boolean ruleExists(String ruleUID) {
        AbstractResource resource = getOrCreateEncapsulatedResource(ruleUID);
        return resource != null && resource.exists();
    }

    /**
     * Get or create an encapsulated resource for the rule.
     *
     * @param ruleUID the UID of the rule
     * @return the encapsulated resource or null if the rule doesn't exist
     */
    private @Nullable AbstractResource getOrCreateEncapsulatedResource(String ruleUID) {
        String uri = "openhab://rules/" + ruleUID;

        return resourceFactory.createResource(uri, (resourceUri, refreshIntervalMs) -> {
            try {
                // Check if rule exists before creating proxy
                if (ruleRegistry.get(ruleUID) == null) {
                    LOGGER.debug("Rule not found: {}", ruleUID);
                    return null;
                }

                return new RuleResourceProxy(ruleRegistry, ruleUID, refreshIntervalMs);
            } catch (Exception e) {
                LOGGER.error("Error creating resource for rule: {}", ruleUID, e);
                return null;
            }
        });
    }

    /**
     * Clean up resources managed by this adapter.
     */
    public void cleanup() {
        LOGGER.debug("Cleaning up RuleResourceAdapter resources");
        resourceFactory.cleanup();
    }
}
