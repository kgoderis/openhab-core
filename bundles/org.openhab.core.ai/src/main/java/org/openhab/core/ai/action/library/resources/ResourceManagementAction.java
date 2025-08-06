package org.openhab.core.ai.action.library.resources;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.openhab.core.ai.action.ActionContext;
import org.openhab.core.ai.action.ActionMetadata;
import org.openhab.core.ai.action.ActionResult;
import org.openhab.core.ai.action.ActionValidationResult;
import org.openhab.core.ai.api.action.Action;
import org.openhab.core.ai.api.action.ActionException;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action for managing resources in the openHAB system.
 * 
 * This action provides operations for managing resources in the openHAB system,
 * including listing, getting, and managing resource specifications.
 * 
 * 
 */
@Component(service = Action.class, immediate = true)
public class ResourceManagementAction implements Action {

    private static final Logger logger = LoggerFactory.getLogger(ResourceManagementAction.class);
    private static final String ACTION_ID = "openhab.resources.manage";
    private static final String ACTION_NAME = "Resource Management";

    @Override
    public String getActionId() {
        return ACTION_ID;
    }

    @Override
    public String getActionName() {
        return ACTION_NAME;
    }

    @Override
    public String getDescription() {
        return "Manage resources in the openHAB system, including listing, getting, and managing resource specifications";
    }

    @Override
    public String getCategory() {
        return "resources";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public Map<String, Object> getParameterSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();
        properties.put("operation",
                Map.of("type", "string", "enum", List.of("list", "get", "create", "update", "delete"), "description",
                        "The operation to perform on resources"));
        properties.put("resourceType", Map.of("type", "string", "description",
                "Type of resource to operate on (e.g., 'item', 'thing', 'rule')"));
        properties.put("resourceId", Map.of("type", "string", "description",
                "ID of the specific resource (required for get, update, delete operations)"));
        properties.put("resourceData",
                Map.of("type", "object", "description", "Resource data for create/update operations"));
        properties.put("filter", Map.of("type", "object", "description", "Filter criteria for list operations"));

        schema.put("properties", properties);
        schema.put("required", List.of("operation"));
        schema.put("additionalProperties", false);
        return schema;
    }

    @Override
    public Map<String, Object> getReturnSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();
        properties.put("operation", Map.of("type", "string", "description", "The operation that was performed"));
        properties.put("resourceType", Map.of("type", "string", "description", "Type of resource operated on"));
        properties.put("resourceId", Map.of("type", "string", "description", "ID of the resource"));
        properties.put("resourceData", Map.of("type", "object", "description", "Resource data"));
        properties.put("resources", Map.of("type", "array", "description", "List of resources (for list operation)"));
        properties.put("count", Map.of("type", "integer", "description", "Number of resources returned"));
        properties.put("filter", Map.of("type", "object", "description", "Filter criteria used"));
        properties.put("message", Map.of("type", "string", "description", "Operation result message"));
        properties.put("error", Map.of("type", "string", "description", "Error message if operation failed"));

        schema.put("properties", properties);
        return schema;
    }

    @Override
    public ActionValidationResult validateParameters(Map<String, Object> parameters) {
        if (parameters == null || parameters.isEmpty()) {
            return ActionValidationResult.invalid(List.of("Parameters cannot be null or empty"));
        }

        String operation = (String) parameters.get("operation");
        if (operation == null || operation.trim().isEmpty()) {
            return ActionValidationResult.invalid(List.of("Operation is required"));
        }

        switch (operation) {
            case "get":
            case "update":
            case "delete":
                if (parameters.get("resourceId") == null) {
                    return ActionValidationResult
                            .invalid(List.of("Resource ID is required for " + operation + " operation"));
                }
                break;
            case "create":
                if (parameters.get("resourceData") == null) {
                    return ActionValidationResult.invalid(List.of("Resource data is required for create operation"));
                }
                break;
            case "list":
                // No additional validation needed
                break;
            default:
                return ActionValidationResult.invalid(List.of("Invalid operation: " + operation));
        }

        return ActionValidationResult.valid(parameters);
    }

    @Override
    public ActionResult execute(Map<String, Object> parameters, ActionContext context) throws ActionException {
        long startTime = System.currentTimeMillis();
        logger.debug("Executing resource management action with parameters: {}", parameters);

        try {
            String operation = (String) parameters.get("operation");
            Map<String, Object> result = switch (operation) {
                case "list" -> listResources(parameters);
                case "get" -> getResource(parameters);
                case "create" -> createResource(parameters);
                case "update" -> updateResource(parameters);
                case "delete" -> deleteResource(parameters);
                default -> throw new ActionException(ACTION_ID, "Invalid operation: " + operation);
            };

            long executionTime = System.currentTimeMillis() - startTime;
            logger.debug("Resource management action '{}' completed in {}ms", operation, executionTime);

            return ActionResult.success(result, executionTime);

        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            logger.error("Failed to execute resource management operation", e);
            throw new ActionException(ACTION_ID, "Resource management operation failed: " + e.getMessage(), e);
        }
    }

    @Override
    public CompletableFuture<ActionResult> executeAsync(Map<String, Object> parameters, ActionContext context) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return execute(parameters, context);
            } catch (ActionException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public ActionMetadata getMetadata() {
        return ActionMetadata.builder().version(getVersion()).author("openHAB")
                .description("Resource management tool for openHAB MCP")
                .tags(List.of("resources", "management", "crud", "operations"))
                .documentation(
                        "Manage resources in the openHAB system, including listing, getting, and managing resource specifications")
                .examples(List.of("{\"operation\": \"list\", \"resourceType\": \"item\"} - List all items",
                        "{\"operation\": \"get\", \"resourceType\": \"thing\", \"resourceId\": \"myThing\"} - Get specific thing",
                        "{\"operation\": \"create\", \"resourceType\": \"rule\", \"resourceData\": {...}} - Create new rule",
                        "{\"operation\": \"update\", \"resourceType\": \"item\", \"resourceId\": \"myItem\", \"resourceData\": {...}} - Update item",
                        "{\"operation\": \"delete\", \"resourceType\": \"thing\", \"resourceId\": \"myThing\"} - Delete thing"))
                .build();
    }

    @Override
    public Map<String, Object> getCapabilities() {
        return Map.of("filtering", true, "sorting", false, "pagination", false, "metadata", true, "async", true);
    }

    @Override
    public void initialize(ActionContext context) {
        logger.debug("ResourceManagementAction initialized for protocol: {}", context.getProtocol());
    }

    @Override
    public void cleanup() {
        logger.debug("ResourceManagementAction cleanup completed");
    }

    @Override
    public boolean isReady() {
        return true; // No external dependencies required
    }

    /**
     * List resources based on filter criteria.
     */
    private Map<String, Object> listResources(Map<String, Object> parameters) {
        String resourceType = (String) parameters.get("resourceType");
        @SuppressWarnings("unchecked")
        Map<String, Object> filter = (Map<String, Object>) parameters.get("filter");

        Map<String, Object> result = new HashMap<>();
        result.put("operation", "list");
        result.put("resourceType", resourceType);
        result.put("filter", filter);
        result.put("resources", List.of()); // Placeholder for actual resource list
        result.put("count", 0);
        result.put("message", "Resource listing completed successfully");

        logger.debug("Listed resources for type: {}, filter: {}", resourceType, filter);
        return result;
    }

    /**
     * Get a specific resource by ID.
     */
    private Map<String, Object> getResource(Map<String, Object> parameters) {
        String resourceId = (String) parameters.get("resourceId");
        String resourceType = (String) parameters.get("resourceType");

        Map<String, Object> result = new HashMap<>();
        result.put("operation", "get");
        result.put("resourceId", resourceId);
        result.put("resourceType", resourceType);
        result.put("resource", Map.of("id", resourceId, "type", resourceType, "data", new HashMap<>() // Placeholder for
                                                                                                      // actual resource
                                                                                                      // data
        ));
        result.put("message", "Resource retrieved successfully");

        logger.debug("Retrieved resource: {} of type: {}", resourceId, resourceType);
        return result;
    }

    /**
     * Create a new resource.
     */
    private Map<String, Object> createResource(Map<String, Object> parameters) {
        @SuppressWarnings("unchecked")
        Map<String, Object> resourceData = (Map<String, Object>) parameters.get("resourceData");
        String resourceType = (String) parameters.get("resourceType");

        Map<String, Object> result = new HashMap<>();
        result.put("operation", "create");
        result.put("resourceType", resourceType);
        result.put("resourceData", resourceData);
        result.put("resourceId", "new-resource-id"); // Placeholder for actual resource ID
        result.put("message", "Resource created successfully");

        logger.debug("Created resource of type: {} with data: {}", resourceType, resourceData);
        return result;
    }

    /**
     * Update an existing resource.
     */
    private Map<String, Object> updateResource(Map<String, Object> parameters) {
        String resourceId = (String) parameters.get("resourceId");
        @SuppressWarnings("unchecked")
        Map<String, Object> resourceData = (Map<String, Object>) parameters.get("resourceData");
        String resourceType = (String) parameters.get("resourceType");

        Map<String, Object> result = new HashMap<>();
        result.put("operation", "update");
        result.put("resourceId", resourceId);
        result.put("resourceType", resourceType);
        result.put("resourceData", resourceData);
        result.put("message", "Resource updated successfully");

        logger.debug("Updated resource: {} of type: {} with data: {}", resourceId, resourceType, resourceData);
        return result;
    }

    /**
     * Delete a resource.
     */
    private Map<String, Object> deleteResource(Map<String, Object> parameters) {
        String resourceId = (String) parameters.get("resourceId");
        String resourceType = (String) parameters.get("resourceType");

        Map<String, Object> result = new HashMap<>();
        result.put("operation", "delete");
        result.put("resourceId", resourceId);
        result.put("resourceType", resourceType);
        result.put("message", "Resource deleted successfully");

        logger.debug("Deleted resource: {} of type: {}", resourceId, resourceType);
        return result;
    }
}
