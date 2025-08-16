package org.openhab.core.ai.tool.resources.api.specification;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.tool.api.SpecificationType;
import org.openhab.core.ai.tool.resources.api.ResourceContext;
import org.openhab.core.ai.tool.resources.api.ResourceResult;
import org.openhab.core.ai.tool.resources.api.validation.ResourceMetadata;
import org.openhab.core.ai.tool.resources.api.validation.ResourceValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Thing Resource Specification for MCP Resources
 * 
 * This class implements MCP resource specification for openHAB Things
 * with URI pattern: openhab://things/{thingUID} and MIME type: application/vnd.openhab.thing+json
 * 
 * @author Karel Goderis - Initial Contribution
 */
@NonNullByDefault
public class ThingResourceSpecification extends ResourceSpecification {

    private static final Logger logger = LoggerFactory.getLogger(ThingResourceSpecification.class);

    private static final String ID = "things";
    private static final String NAME = "Things";
    private static final String DESCRIPTION = "Access to openHAB thing status, configuration, and properties";
    private static final String URI_PATTERN = "openhab://things/{thingUID}";
    private static final String MIME_TYPE = "application/vnd.openhab.thing+json";

    /**
     * Default constructor.
     */
    public ThingResourceSpecification() {
        super(ID, NAME, DESCRIPTION, "1.0.0", Map.of(), Map.of(), Map.of(),
                Map.of("category", "openhab", "entity", "thing", "version", "1.0.0"));
    }

    @Override
    public SpecificationType getType() {
        return SpecificationType.RESOURCE;
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
        properties.put("thingUID",
                Map.of("type", "string", "description", "The UID of the openHAB thing", "required", true));
        properties.put("action",
                Map.of("type", "string", "description", "The action to perform (get, configure, enable, disable)",
                        "enum", new String[] { "get", "configure", "enable", "disable" }, "default", "get"));
        properties.put("configuration",
                Map.of("type", "object", "description", "Configuration parameters (required for configure action)"));

        schema.put("properties", properties);
        schema.put("required", new String[] { "thingUID" });

        return schema;
    }

    @Override
    public Map<String, Object> getOutputSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();
        properties.put("success", Map.of("type", "boolean", "description", "Whether the operation was successful"));
        properties.put("thingUID", Map.of("type", "string", "description", "The UID of the thing"));
        properties.put("status", Map.of("type", "string", "description", "The current status of the thing"));
        properties.put("bindingId", Map.of("type", "string", "description", "The binding ID of the thing"));
        properties.put("configuration", Map.of("type", "object", "description", "The configuration of the thing"));
        properties.put("properties", Map.of("type", "object", "description", "Additional properties of the thing"));
        properties.put("channels", Map.of("type", "array", "description", "List of channels available on the thing"));
        properties.put("error", Map.of("type", "string", "description", "Error message if the operation failed"));

        schema.put("properties", properties);

        return schema;
    }

    @Override
    public ResourceMetadata getResourceMetadata() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("category", "openhab");
        properties.put("entity", "thing");
        properties.put("version", "1.0.0");

        return new ResourceMetadata("1.0.0", "Karel Goderis - Initial Contribution", properties);
    }

    @Override
    public ResourceValidationResult validateParameters(Map<String, Object> parameters) {
        if (parameters == null || parameters.isEmpty()) {
            return ResourceValidationResult.failure("Parameters cannot be null or empty");
        }

        Object thingUID = parameters.get("thingUID");
        if (thingUID == null || !(thingUID instanceof String) || ((String) thingUID).trim().isEmpty()) {
            return ResourceValidationResult.failure("thingUID is required and must be a non-empty string");
        }

        Object action = parameters.get("action");
        if (action != null && !(action instanceof String)) {
            return ResourceValidationResult.failure("action must be a string");
        }

        if ("configure".equals(action)) {
            Object configuration = parameters.get("configuration");
            if (configuration == null) {
                return ResourceValidationResult.failure("configuration is required for configure action");
            }
        }

        return ResourceValidationResult.success();
    }

    @Override
    public ResourceResult execute(Map<String, Object> parameters, ResourceContext context) {
        long startTime = System.currentTimeMillis();

        try {
            logger.debug("Executing thing resource with parameters: {}", parameters);

            // Validate parameters
            ResourceValidationResult validation = validateParameters(parameters);
            if (!validation.isValid()) {
                return ResourceResult.failure(validation.getMessage(), System.currentTimeMillis() - startTime);
            }

            String thingUID = (String) parameters.get("thingUID");
            String action = (String) parameters.getOrDefault("action", "get");

            // TODO: Implement actual openHAB ThingRegistry integration
            // For now, return a mock result
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("thingUID", thingUID);
            result.put("action", action);
            result.put("status", "ONLINE");
            result.put("bindingId", "mock");
            result.put("configuration", Map.of("host", "localhost", "port", 8080));
            result.put("properties", Map.of("vendor", "Mock Vendor", "model", "Mock Model"));
            result.put("channels", new String[] { "channel1", "channel2" });

            long executionTime = System.currentTimeMillis() - startTime;
            logger.debug("Thing resource execution completed in {}ms", executionTime);

            return ResourceResult.success(result, executionTime);

        } catch (Exception e) {
            logger.error("Error executing thing resource", e);
            return ResourceResult.failure("Execution error: " + e.getMessage(), System.currentTimeMillis() - startTime);
        }
    }
}
