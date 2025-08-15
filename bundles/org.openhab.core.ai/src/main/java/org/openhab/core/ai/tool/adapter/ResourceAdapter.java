package org.openhab.core.ai.tool.adapter;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.tool.resources.api.specification.ResourceSpecification;
import org.openhab.core.ai.tool.resources.api.validation.ResourceValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resource Interface Adapter for MCP Resources
 * 
 * This utility class bridges the internal ResourceSpecification interface
 * with the MCP SDK's SyncResourceSpecification and AsyncResourceSpecification.
 * 
 * @author Karel Goderis - Initial Contribution
 */
@NonNullByDefault
public class ResourceAdapter {

    private static final Logger logger = LoggerFactory.getLogger(ResourceAdapter.class);

    /**
     * Create a sync resource specification for HTTP resources/list endpoint consumption.
     *
     * This returns a structured Map that can be serialized to JSON by the servlet.
     * When MCP SDK resource builders are available, this method can be adapted to
     * also produce proper SDK types while retaining this map for list endpoints.
     *
     * @param resource the internal resource specification
     * @return a serializable map representing the resource specification or null on error
     */
    public static Object createSyncResourceSpecification(ResourceSpecification resource) {
        try {
            logger.debug("Creating sync resource specification for: {}", resource.getId());

            return Map.of("id", resource.getId(), "name", resource.getName(), "description", resource.getDescription(),
                    "version", resource.getVersion(), "uriPattern", resource.getUriPattern(), "mimeType",
                    resource.getMimeType(), "inputSchema", resource.getInputSchema(), "outputSchema",
                    resource.getOutputSchema(), "configuration", resource.getConfiguration(), "metadata",
                    resource.getMetadata());
        } catch (Exception e) {
            logger.error("Failed to create sync resource specification: {}", resource.getId(), e);
            return null;
        }
    }

    /**
     * Create an async resource specification for HTTP resources/list endpoint consumption.
     *
     * Currently mirrors the sync representation. If/when async-specific metadata is
     * needed (e.g., streaming flags), extend this map accordingly.
     *
     * @param resource the internal resource specification
     * @return a serializable map representing the resource specification or null on error
     */
    public static Object createAsyncResourceSpecification(ResourceSpecification resource) {
        try {
            logger.debug("Creating async resource specification for: {}", resource.getId());
            return createSyncResourceSpecification(resource);
        } catch (Exception e) {
            logger.error("Failed to create async resource specification: {}", resource.getId(), e);
            return null;
        }
    }

    /**
     * Validate parameters for the given resource
     * 
     * @param resource The internal resource specification
     * @param parameters The parameters to validate
     * @return The validation result
     */
    public static ResourceValidationResult validateParameters(ResourceSpecification resource,
            Map<String, Object> parameters) {
        try {
            return resource.validateParameters(parameters);
        } catch (Exception e) {
            logger.error("Parameter validation failed for resource: {}", resource.getId(), e);
            return ResourceValidationResult.failure("Validation error: " + e.getMessage());
        }
    }
}
