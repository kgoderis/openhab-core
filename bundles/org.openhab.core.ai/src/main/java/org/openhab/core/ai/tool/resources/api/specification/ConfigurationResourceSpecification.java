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
 * Configuration Resource Specification for MCP Resources
 * 
 * This class implements MCP resource specification for openHAB Configuration
 * with URI pattern: openhab://config/{configPath} and MIME type: application/vnd.openhab.config+json
 * 
 * @author Karel Goderis - Initial Contribution
 */
@NonNullByDefault
public class ConfigurationResourceSpecification extends ResourceSpecification {

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationResourceSpecification.class);

    private static final String ID = "configuration";
    private static final String NAME = "Configuration";
    private static final String DESCRIPTION = "Access to openHAB configuration files, settings, and system properties";
    private static final String URI_PATTERN = "openhab://config/{configPath}";
    private static final String MIME_TYPE = "application/vnd.openhab.config+json";

    /**
     * Default constructor.
     */
    public ConfigurationResourceSpecification() {
        super(ID, NAME, DESCRIPTION, "1.0.0", Map.of(), Map.of(), Map.of(),
                Map.of("category", "openhab", "entity", "configuration", "version", "1.0.0"));
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
        properties.put("configPath",
                Map.of("type", "string", "description", "The configuration path to access", "required", true));
        properties.put("action",
                Map.of("type", "string", "description",
                        "The action to perform (get, set, update, delete, backup, restore)", "enum",
                        new String[] { "get", "set", "update", "delete", "backup", "restore" }, "default", "get"));
        properties.put("value",
                Map.of("type", "object", "description", "Configuration value (required for set/update actions)"));
        properties.put("backupPath", Map.of("type", "string", "description", "Path for backup/restore operations"));

        schema.put("properties", properties);
        schema.put("required", new String[] { "configPath" });

        return schema;
    }

    @Override
    public Map<String, Object> getOutputSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();
        properties.put("success", Map.of("type", "boolean", "description", "Whether the operation was successful"));
        properties.put("configPath", Map.of("type", "string", "description", "The configuration path"));
        properties.put("action", Map.of("type", "string", "description", "The action that was performed"));
        properties.put("value", Map.of("type", "object", "description", "The configuration value"));
        properties.put("metadata",
                Map.of("type", "object", "description", "Additional metadata about the configuration"));
        properties.put("lastModified", Map.of("type", "string", "description", "Timestamp of last modification"));
        properties.put("size", Map.of("type", "integer", "description", "Size of the configuration in bytes"));
        properties.put("error", Map.of("type", "string", "description", "Error message if the operation failed"));

        schema.put("properties", properties);

        return schema;
    }

    @Override
    public ResourceMetadata getResourceMetadata() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("category", "openhab");
        properties.put("entity", "configuration");
        properties.put("version", "1.0.0");

        return new ResourceMetadata("1.0.0", "Karel Goderis - Initial Contribution", properties);
    }

    @Override
    public ResourceValidationResult validateParameters(Map<String, Object> parameters) {
        if (parameters == null || parameters.isEmpty()) {
            return ResourceValidationResult.failure("Parameters cannot be null or empty");
        }

        Object configPath = parameters.get("configPath");
        if (configPath == null || !(configPath instanceof String) || ((String) configPath).trim().isEmpty()) {
            return ResourceValidationResult.failure("configPath is required and must be a non-empty string");
        }

        Object action = parameters.get("action");
        if (action != null && !(action instanceof String)) {
            return ResourceValidationResult.failure("action must be a string");
        }

        if ("set".equals(action) || "update".equals(action)) {
            Object value = parameters.get("value");
            if (value == null) {
                return ResourceValidationResult.failure("value is required for set/update actions");
            }
        }

        if ("backup".equals(action) || "restore".equals(action)) {
            Object backupPath = parameters.get("backupPath");
            if (backupPath == null) {
                return ResourceValidationResult.failure("backupPath is required for backup/restore actions");
            }
        }

        return ResourceValidationResult.success();
    }

    @Override
    public ResourceResult execute(Map<String, Object> parameters, ResourceContext context) {
        long startTime = System.currentTimeMillis();

        try {
            logger.debug("Executing configuration resource with parameters: {}", parameters);

            // Validate parameters
            ResourceValidationResult validation = validateParameters(parameters);
            if (!validation.isValid()) {
                return ResourceResult.failure(validation.getMessage(), System.currentTimeMillis() - startTime);
            }

            String configPath = (String) parameters.get("configPath");
            String action = (String) parameters.getOrDefault("action", "get");

            // TODO: Implement actual openHAB ConfigurationService integration
            // For now, return a mock result
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("configPath", configPath);
            result.put("action", action);
            result.put("value", Map.of("setting1", "value1", "setting2", "value2"));
            result.put("metadata", Map.of("type", "json", "encoding", "UTF-8"));
            result.put("lastModified", "2024-01-01T12:00:00Z");
            result.put("size", 1024);

            long executionTime = System.currentTimeMillis() - startTime;
            logger.debug("Configuration resource execution completed in {}ms", executionTime);

            return ResourceResult.success(result, executionTime);

        } catch (Exception e) {
            logger.error("Error executing configuration resource", e);
            return ResourceResult.failure("Execution error: " + e.getMessage(), System.currentTimeMillis() - startTime);
        }
    }
}
