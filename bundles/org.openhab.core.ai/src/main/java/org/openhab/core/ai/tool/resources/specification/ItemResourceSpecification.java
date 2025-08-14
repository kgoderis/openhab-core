package org.openhab.core.ai.tool.resources.specification;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.tool.api.ResourceContext;
import org.openhab.core.ai.tool.api.ResourceResult;
import org.openhab.core.ai.tool.api.validation.ResourceMetadata;
import org.openhab.core.ai.tool.api.validation.ResourceValidationResult;
import org.openhab.core.ai.tool.resources.adapter.ItemResourceAdapter;
import org.openhab.core.items.ItemRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Item Resource Specification for MCP Resources
 * 
 * This class implements MCP resource specification for openHAB Items
 * with URI pattern: openhab://items/{itemName} and MIME type: application/vnd.openhab.item+json
 * 
 * @author Karel Goderis - Initial Contribution
 */
@NonNullByDefault
public class ItemResourceSpecification extends ResourceSpecification {

    private static final Logger logger = LoggerFactory.getLogger(ItemResourceSpecification.class);

    private static final String ID = "items";
    private static final String NAME = "Items";
    private static final String DESCRIPTION = "Access to openHAB item state, configuration, and metadata";
    private static final String URI_PATTERN = "openhab://items/{itemName}";
    private static final String MIME_TYPE = "application/vnd.openhab.item+json";

    /**
     * Default constructor.
     */
    public ItemResourceSpecification() {
        super(ID, NAME, DESCRIPTION, "1.0.0", Map.of(), Map.of(), Map.of(),
                Map.of("category", "openhab", "entity", "item", "version", "1.0.0"));
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
        properties.put("itemName",
                Map.of("type", "string", "description", "The name of the openHAB item", "required", true));
        properties.put("action", Map.of("type", "string", "description", "The action to perform (get, set, update)",
                "enum", new String[] { "get", "set", "update" }, "default", "get"));
        properties.put("value",
                Map.of("type", "string", "description", "The value to set (required for set/update actions)"));

        schema.put("properties", properties);
        schema.put("required", new String[] { "itemName" });

        return schema;
    }

    @Override
    public Map<String, Object> getOutputSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();
        properties.put("success", Map.of("type", "boolean", "description", "Whether the operation was successful"));
        properties.put("itemName", Map.of("type", "string", "description", "The name of the item"));
        properties.put("state", Map.of("type", "string", "description", "The current state of the item"));
        properties.put("type", Map.of("type", "string", "description", "The type of the item"));
        properties.put("metadata", Map.of("type", "object", "description", "Additional metadata about the item"));
        properties.put("error", Map.of("type", "string", "description", "Error message if the operation failed"));

        schema.put("properties", properties);

        return schema;
    }

    @Override
    public ResourceMetadata getResourceMetadata() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("category", "openhab");
        properties.put("entity", "item");
        properties.put("version", "1.0.0");

        return new ResourceMetadata("1.0.0", "Karel Goderis - Initial Contribution", properties);
    }

    @Override
    public ResourceValidationResult validateParameters(Map<String, Object> parameters) {
        if (parameters == null || parameters.isEmpty()) {
            return ResourceValidationResult.failure("Parameters cannot be null or empty");
        }

        Object itemName = parameters.get("itemName");
        if (itemName == null || !(itemName instanceof String) || ((String) itemName).trim().isEmpty()) {
            return ResourceValidationResult.failure("itemName is required and must be a non-empty string");
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

        return ResourceValidationResult.success();
    }

    @Override
    public ResourceResult execute(Map<String, Object> parameters, ResourceContext context) {
        long startTime = System.currentTimeMillis();

        try {
            logger.debug("Executing item resource with parameters: {}", parameters);

            // Validate parameters
            ResourceValidationResult validation = validateParameters(parameters);
            if (!validation.isValid()) {
                return ResourceResult.failure(validation.getMessage(), System.currentTimeMillis() - startTime);
            }

            String itemName = (String) parameters.get("itemName");
            String action = (String) parameters.getOrDefault("action", "get");

            // ✅ Use Adapter directly for real OpenHAB operations
            ItemResourceAdapter adapter = getAdapter(context);
            if (adapter == null) {
                return ResourceResult.failure("Item adapter not available", System.currentTimeMillis() - startTime);
            }

            // Execute the operation using the adapter
            return adapter.execute(itemName, action, parameters, context);

        } catch (Exception e) {
            logger.error("Error executing item resource", e);
            return ResourceResult.failure("Execution error: " + e.getMessage(), System.currentTimeMillis() - startTime);
        }
    }

    /**
     * Get the ItemResourceAdapter for this specification.
     * 
     * @param context the resource context
     * @return the adapter or null if not available
     */
    private @Nullable ItemResourceAdapter getAdapter(ResourceContext context) {
        ItemRegistry itemRegistry = context.getProperty("itemRegistry");
        return itemRegistry != null ? new ItemResourceAdapter(itemRegistry) : null;
    }
}
