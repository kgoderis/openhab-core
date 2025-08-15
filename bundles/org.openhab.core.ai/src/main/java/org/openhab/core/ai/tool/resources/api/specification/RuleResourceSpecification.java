package org.openhab.core.ai.tool.resources.api.specification;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.tool.resources.api.ResourceContext;
import org.openhab.core.ai.tool.resources.api.ResourceResult;
import org.openhab.core.ai.tool.resources.api.validation.ResourceMetadata;
import org.openhab.core.ai.tool.resources.api.validation.ResourceValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Rule Resource Specification for MCP Resources
 * 
 * This class implements MCP resource specification for openHAB Rules
 * with URI pattern: openhab://rules/{ruleUID} and MIME type: application/vnd.openhab.rule+json
 * 
 * @author Karel Goderis - Initial Contribution
 */
@NonNullByDefault
public class RuleResourceSpecification extends ResourceSpecification {

    private static final Logger logger = LoggerFactory.getLogger(RuleResourceSpecification.class);

    private static final String ID = "rules";
    private static final String NAME = "Rules";
    private static final String DESCRIPTION = "Access to openHAB rule configuration, execution history, and status";
    private static final String URI_PATTERN = "openhab://rules/{ruleUID}";
    private static final String MIME_TYPE = "application/vnd.openhab.rule+json";

    /**
     * Default constructor.
     */
    public RuleResourceSpecification() {
        super(ID, NAME, DESCRIPTION, "1.0.0", Map.of(), Map.of(), Map.of(),
                Map.of("category", "openhab", "entity", "rule", "version", "1.0.0"));
    }

    @Override
    public org.openhab.core.ai.tool.api.SpecificationType getType() {
        return org.openhab.core.ai.tool.api.SpecificationType.RESOURCE;
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    @Override
    public String getUriPattern() {
        return URI_PATTERN;
    }

    @Override
    public String getMimeType() {
        return MIME_TYPE;
    }

    @Override
    public Map<String, Object> getInputSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();
        properties.put("ruleUID",
                Map.of("type", "string", "description", "The UID of the openHAB rule", "required", true));
        properties.put("action",
                Map.of("type", "string", "description", "The action to perform (get, enable, disable, run, update)",
                        "enum", new String[] { "get", "enable", "disable", "run", "update" }, "default", "get"));
        properties.put("configuration",
                Map.of("type", "object", "description", "Rule configuration (required for update action)"));

        schema.put("properties", properties);
        schema.put("required", new String[] { "ruleUID" });

        return schema;
    }

    @Override
    public Map<String, Object> getOutputSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();
        properties.put("success", Map.of("type", "boolean", "description", "Whether the operation was successful"));
        properties.put("ruleUID", Map.of("type", "string", "description", "The UID of the rule"));
        properties.put("name", Map.of("type", "string", "description", "The name of the rule"));
        properties.put("enabled", Map.of("type", "boolean", "description", "Whether the rule is enabled"));
        properties.put("triggers", Map.of("type", "array", "description", "List of triggers for the rule"));
        properties.put("conditions", Map.of("type", "array", "description", "List of conditions for the rule"));
        properties.put("actions", Map.of("type", "array", "description", "List of actions for the rule"));
        properties.put("executionCount",
                Map.of("type", "integer", "description", "Number of times the rule has been executed"));
        properties.put("lastExecution", Map.of("type", "string", "description", "Timestamp of the last execution"));
        properties.put("error", Map.of("type", "string", "description", "Error message if the operation failed"));

        schema.put("properties", properties);

        return schema;
    }

    @Override
    public ResourceMetadata getResourceMetadata() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("category", "openhab");
        properties.put("entity", "rule");
        properties.put("version", "1.0.0");

        return new ResourceMetadata("1.0.0", "Karel Goderis - Initial Contribution", properties);
    }

    @Override
    public ResourceValidationResult validateParameters(Map<String, Object> parameters) {
        if (parameters == null || parameters.isEmpty()) {
            return ResourceValidationResult.failure("Parameters cannot be null or empty");
        }

        Object ruleUID = parameters.get("ruleUID");
        if (ruleUID == null || !(ruleUID instanceof String) || ((String) ruleUID).trim().isEmpty()) {
            return ResourceValidationResult.failure("ruleUID is required and must be a non-empty string");
        }

        Object action = parameters.get("action");
        if (action != null && !(action instanceof String)) {
            return ResourceValidationResult.failure("action must be a string");
        }

        if ("update".equals(action)) {
            Object configuration = parameters.get("configuration");
            if (configuration == null) {
                return ResourceValidationResult.failure("configuration is required for update action");
            }
        }

        return ResourceValidationResult.success();
    }

    @Override
    public ResourceResult execute(Map<String, Object> parameters, ResourceContext context) {
        long startTime = System.currentTimeMillis();

        try {
            logger.debug("Executing rule resource with parameters: {}", parameters);

            // Validate parameters
            ResourceValidationResult validation = validateParameters(parameters);
            if (!validation.isValid()) {
                return ResourceResult.failure(validation.getMessage(), System.currentTimeMillis() - startTime);
            }

            String ruleUID = (String) parameters.get("ruleUID");
            String action = (String) parameters.getOrDefault("action", "get");

            // TODO: Implement actual openHAB RuleRegistry integration
            // For now, return a mock result
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("ruleUID", ruleUID);
            result.put("action", action);
            result.put("name", "Mock Rule");
            result.put("enabled", true);
            result.put("triggers", new String[] { "ItemStateChangeTrigger" });
            result.put("conditions", new String[] { "ItemStateCondition" });
            result.put("actions", new String[] { "ItemCommandAction" });
            result.put("executionCount", 42);
            result.put("lastExecution", "2024-01-01T12:00:00Z");

            long executionTime = System.currentTimeMillis() - startTime;
            logger.debug("Rule resource execution completed in {}ms", executionTime);

            return ResourceResult.success(result, executionTime);

        } catch (Exception e) {
            logger.error("Error executing rule resource", e);
            return ResourceResult.failure("Execution error: " + e.getMessage(), System.currentTimeMillis() - startTime);
        }
    }
}
