package org.openhab.core.ai.tool.library.resources;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.tool.api.ResourceContext;
import org.openhab.core.ai.tool.api.ResourceRegistry;
import org.openhab.core.ai.tool.api.ResourceResult;
import org.openhab.core.ai.tool.api.Tool;
import org.openhab.core.ai.tool.api.ToolContext;
import org.openhab.core.ai.tool.api.ToolMetadata;
import org.openhab.core.ai.tool.api.ToolResult;
import org.openhab.core.ai.tool.api.ToolValidationResult;
import org.openhab.core.ai.tool.api.validation.ResourceMetadata;
import org.openhab.core.ai.tool.api.validation.ResourceValidationResult;
import org.openhab.core.ai.tool.resources.dto.Resource;
import org.openhab.core.ai.tool.resources.specification.ResourceSpecification;
import org.openhab.core.ai.util.ValidationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resource management tool for MCP resources.
 * 
 * This class provides a tool interface for managing MCP resources, including
 * resource discovery, registration, and lifecycle management.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ResourceManagementTool implements Tool {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceManagementTool.class);

    private final String id;
    private final String name;
    private final String description;
    private final Map<String, Object> inputSchema;
    private final Map<String, Object> outputSchema;

    /** Resource registry for managing resources. */
    private @Nullable ResourceRegistry resourceRegistry;

    /**
     * Create a new resource management tool.
     */
    public ResourceManagementTool() {
        this.id = "resource-management";
        this.name = "Resource Management Tool";
        this.description = "Tool for managing MCP resources including discovery, registration, and lifecycle management";
        this.inputSchema = Map.of("action",
                Map.of("type", "string", "enum", new String[] { "discover", "register", "unregister", "list" }),
                "resourceType", Map.of("type", "string", "description", "Type of resource to manage"), "parameters",
                Map.of("type", "object", "description", "Additional parameters for the action"));
        this.outputSchema = Map.of("success",
                Map.of("type", "boolean", "description", "Whether the operation was successful"), "resources",
                Map.of("type", "array", "description", "List of resources"), "message",
                Map.of("type", "string", "description", "Result message"));
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public Map<String, Object> getInputSchema() {
        return inputSchema;
    }

    @Override
    public Map<String, Object> getOutputSchema() {
        return outputSchema;
    }

    @Override
    public ToolMetadata getMetadata() {
        return ToolMetadata.builder().version("1.0.0").author("Karel Goderis")
                .description("Resource management tool for MCP resources").build();
    }

    @Override
    public ToolValidationResult validateParameters(Map<String, Object> parameters) {
        try {
            // Validate parameters are not null
            ValidationUtils.requireNonNull(parameters, "Parameters");

            // Validate action parameter
            String action = (String) parameters.get("action");
            if (action == null || action.trim().isEmpty()) {
                return ToolValidationResult.invalid("Action parameter is required");
            }

            // Validate action value
            if (!"discover".equals(action) && !"register".equals(action) && !"unregister".equals(action)
                    && !"list".equals(action)) {
                return ToolValidationResult.invalid(
                        "Invalid action: " + action + ". Must be one of: discover, register, unregister, list");
            }

            // Validate resourceType for specific actions
            if ("register".equals(action) || "unregister".equals(action)) {
                String resourceType = (String) parameters.get("resourceType");
                if (resourceType == null || resourceType.trim().isEmpty()) {
                    return ToolValidationResult.invalid("ResourceType parameter is required for " + action + " action");
                }
            }

            // Validate parameters object
            Object parametersObj = parameters.get("parameters");
            if (parametersObj != null && !(parametersObj instanceof Map)) {
                return ToolValidationResult.invalid("Parameters must be an object");
            }

            return ToolValidationResult.valid();

        } catch (Exception e) {
            LOGGER.error("Parameter validation failed", e);
            return ToolValidationResult.invalid("Parameter validation error: " + e.getMessage());
        }
    }

    @Override
    public ToolResult execute(Map<String, Object> parameters, ToolContext context) {
        try {
            String action = (String) parameters.get("action");
            String resourceType = (String) parameters.get("resourceType");
            @SuppressWarnings("unchecked")
            Map<String, Object> actionParameters = (Map<String, Object>) parameters.get("parameters");

            if (actionParameters == null) {
                actionParameters = new HashMap<>();
            }

            ResourceRegistry registry = resourceRegistry;
            if (registry == null) {
                return ToolResult.error(id, "Resource registry not available", System.currentTimeMillis());
            }

            Map<String, Object> result = new HashMap<>();

            switch (action) {
                case "discover":
                    result = discoverResources(registry, resourceType, actionParameters);
                    break;
                case "register":
                    result = registerResource(registry, resourceType, actionParameters);
                    break;
                case "unregister":
                    result = unregisterResource(registry, resourceType, actionParameters);
                    break;
                case "list":
                    result = listResources(registry, resourceType, actionParameters);
                    break;
                default:
                    return ToolResult.error(id, "Invalid action: " + action, System.currentTimeMillis());
            }

            return ToolResult.successJson(id, result, System.currentTimeMillis());

        } catch (Exception e) {
            LOGGER.error("Resource management operation failed", e);
            return ToolResult.error(id, "Resource management operation failed: " + e.getMessage(),
                    System.currentTimeMillis());
        }
    }

    /**
     * Discover resources.
     * 
     * @param registry the resource registry
     * @param resourceType the resource type to discover
     * @param parameters additional parameters
     * @return discovery result
     */
    private Map<String, Object> discoverResources(ResourceRegistry registry, String resourceType,
            Map<String, Object> parameters) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> discoveredResources = new ArrayList<>();

        try {
            // Get all resource specifications
            List<ResourceSpecification> specifications = registry.getAllResourceSpecifications();

            // Filter by resource type if specified
            if (resourceType != null && !resourceType.trim().isEmpty()) {
                specifications = specifications.stream().filter(spec -> resourceType.equals(spec.getType().toString()))
                        .toList();
            }

            // Convert to result format
            for (ResourceSpecification spec : specifications) {
                Map<String, Object> resourceInfo = new HashMap<>();
                resourceInfo.put("id", spec.getId());
                resourceInfo.put("name", spec.getName());
                resourceInfo.put("description", spec.getDescription());
                resourceInfo.put("type", spec.getType().toString());
                resourceInfo.put("version", spec.getVersion());
                resourceInfo.put("uriPattern", spec.getUriPattern());
                resourceInfo.put("mimeType", spec.getMimeType());
                discoveredResources.add(resourceInfo);
            }

            result.put("success", true);
            result.put("resources", discoveredResources);
            result.put("message", "Discovered " + discoveredResources.size() + " resources");
            result.put("count", discoveredResources.size());

            LOGGER.debug("Discovered {} resources", discoveredResources.size());

        } catch (Exception e) {
            LOGGER.error("Resource discovery failed", e);
            result.put("success", false);
            result.put("message", "Resource discovery failed: " + e.getMessage());
            result.put("resources", new ArrayList<>());
            result.put("count", 0);
        }

        return result;
    }

    /**
     * Register a resource.
     * 
     * @param registry the resource registry
     * @param resourceType the resource type
     * @param parameters registration parameters
     * @return registration result
     */
    private Map<String, Object> registerResource(ResourceRegistry registry, String resourceType,
            Map<String, Object> parameters) {
        Map<String, Object> result = new HashMap<>();

        try {
            // Validate required parameters
            String resourceId = (String) parameters.get("resourceId");
            if (resourceId == null || resourceId.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "ResourceId parameter is required for registration");
                return result;
            }

            // Check if resource already exists
            if (registry.hasResource(resourceId)) {
                result.put("success", false);
                result.put("message", "Resource already exists: " + resourceId);
                return result;
            }

            // Create resource specification based on type
            ResourceSpecification specification = createResourceSpecification(resourceType, parameters);
            if (specification == null) {
                result.put("success", false);
                result.put("message", "Failed to create resource specification for type: " + resourceType);
                return result;
            }

            // Register the resource
            registry.registerResource(specification);

            result.put("success", true);
            result.put("message", "Resource registered successfully: " + resourceId);
            result.put("resourceId", resourceId);
            result.put("resourceType", resourceType);

            LOGGER.debug("Registered resource: {} of type: {}", resourceId, resourceType);

        } catch (Exception e) {
            LOGGER.error("Resource registration failed", e);
            result.put("success", false);
            result.put("message", "Resource registration failed: " + e.getMessage());
        }

        return result;
    }

    /**
     * Unregister a resource.
     * 
     * @param registry the resource registry
     * @param resourceType the resource type
     * @param parameters unregistration parameters
     * @return unregistration result
     */
    private Map<String, Object> unregisterResource(ResourceRegistry registry, String resourceType,
            Map<String, Object> parameters) {
        Map<String, Object> result = new HashMap<>();

        try {
            // Validate required parameters
            String resourceId = (String) parameters.get("resourceId");
            if (resourceId == null || resourceId.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "ResourceId parameter is required for unregistration");
                return result;
            }

            // Check if resource exists
            if (!registry.hasResource(resourceId)) {
                result.put("success", false);
                result.put("message", "Resource not found: " + resourceId);
                return result;
            }

            // Unregister the resource
            registry.unregisterResource(resourceId);

            result.put("success", true);
            result.put("message", "Resource unregistered successfully: " + resourceId);
            result.put("resourceId", resourceId);
            result.put("resourceType", resourceType);

            LOGGER.debug("Unregistered resource: {} of type: {}", resourceId, resourceType);

        } catch (Exception e) {
            LOGGER.error("Resource unregistration failed", e);
            result.put("success", false);
            result.put("message", "Resource unregistration failed: " + e.getMessage());
        }

        return result;
    }

    /**
     * List resources.
     * 
     * @param registry the resource registry
     * @param resourceType the resource type to list
     * @param parameters listing parameters
     * @return listing result
     */
    private Map<String, Object> listResources(ResourceRegistry registry, String resourceType,
            Map<String, Object> parameters) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> resourceList = new ArrayList<>();

        try {
            // Get all resources
            Map<String, Resource> allResources = registry.getAllResources();

            // Filter by resource type if specified
            if (resourceType != null && !resourceType.trim().isEmpty()) {
                allResources = allResources.entrySet().stream().filter(entry -> {
                    Resource resource = entry.getValue();
                    return resourceType.equals(resource.getMimeType());
                }).collect(HashMap::new, (map, entry) -> map.put(entry.getKey(), entry.getValue()), HashMap::putAll);
            }

            // Convert to result format
            for (Map.Entry<String, Resource> entry : allResources.entrySet()) {
                String uri = entry.getKey();
                Resource resource = entry.getValue();

                Map<String, Object> resourceInfo = new HashMap<>();
                resourceInfo.put("uri", uri);
                resourceInfo.put("mimeType", resource.getMimeType());
                resourceInfo.put("name", resource.getName());
                resourceInfo.put("description", resource.getDescription());
                resourceList.add(resourceInfo);
            }

            result.put("success", true);
            result.put("resources", resourceList);
            result.put("message", "Listed " + resourceList.size() + " resources");
            result.put("count", resourceList.size());

            LOGGER.debug("Listed {} resources", resourceList.size());

        } catch (Exception e) {
            LOGGER.error("Resource listing failed", e);
            result.put("success", false);
            result.put("message", "Resource listing failed: " + e.getMessage());
            result.put("resources", new ArrayList<>());
            result.put("count", 0);
        }

        return result;
    }

    /**
     * Create a resource specification based on type and parameters.
     * 
     * @param resourceType the resource type
     * @param parameters the parameters
     * @return the resource specification
     */
    private @Nullable ResourceSpecification createResourceSpecification(String resourceType,
            Map<String, Object> parameters) {
        try {
            String resourceId = (String) parameters.get("resourceId");
            String name = (String) parameters.get("name");
            String description = (String) parameters.get("description");
            String version = (String) parameters.getOrDefault("version", "1.0.0");

            // Validate required parameters
            if (resourceId == null || resourceId.trim().isEmpty()) {
                LOGGER.error("ResourceId is required for creating resource specification");
                return null;
            }

            if (name == null || name.trim().isEmpty()) {
                name = resourceId; // Use resourceId as name if not provided
            }

            if (description == null || description.trim().isEmpty()) {
                description = "Resource of type " + resourceType; // Default description
            }

            // Create basic schema
            Map<String, Object> inputSchema = new HashMap<>();
            Map<String, Object> outputSchema = new HashMap<>();
            Map<String, Object> configuration = new HashMap<>();
            Map<String, Object> metadata = new HashMap<>();

            // Create specification based on type
            // This is a simplified implementation - in a real scenario,
            // you would have specific specification classes for each type
            return new ResourceSpecification(resourceId, name, description, version, inputSchema, outputSchema,
                    configuration, metadata) {
                @Override
                public String getUriPattern() {
                    return "openhab://" + resourceType + "/{" + resourceId + "}";
                }

                @Override
                public String getMimeType() {
                    return "application/vnd.openhab." + resourceType + "+json";
                }

                @Override
                public ResourceMetadata getResourceMetadata() {
                    Map<String, Object> properties = new HashMap<>();
                    properties.put("uriPattern", getUriPattern());
                    properties.put("mimeType", getMimeType());
                    return new ResourceMetadata(version, "Karel Goderis", properties);
                }

                @Override
                public ResourceValidationResult validateParameters(Map<String, Object> parameters) {
                    // Basic validation - in a real implementation, this would be more comprehensive
                    if (parameters == null) {
                        return ResourceValidationResult.failure("Parameters cannot be null");
                    }
                    return ResourceValidationResult.success();
                }

                @Override
                public ResourceResult execute(Map<String, Object> parameters, ResourceContext context) {
                    // Basic execution - in a real implementation, this would perform actual resource operations
                    return ResourceResult.success("Resource executed successfully", System.currentTimeMillis());
                }

                @Override
                public org.openhab.core.ai.tool.api.SpecificationType getType() {
                    return org.openhab.core.ai.tool.api.SpecificationType.RESOURCE;
                }
            };

        } catch (Exception e) {
            LOGGER.error("Failed to create resource specification", e);
            return null;
        }
    }

    /**
     * Set the resource registry.
     * 
     * @param resourceRegistry the resource registry
     */
    public void setResourceRegistry(ResourceRegistry resourceRegistry) {
        this.resourceRegistry = resourceRegistry;
    }
}
