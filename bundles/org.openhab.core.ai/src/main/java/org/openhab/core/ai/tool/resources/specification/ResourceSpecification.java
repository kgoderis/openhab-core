package org.openhab.core.ai.tool.resources.specification;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.tool.api.ResourceContext;
import org.openhab.core.ai.tool.api.ResourceResult;
import org.openhab.core.ai.tool.api.Specification;
import org.openhab.core.ai.tool.api.validation.ResourceMetadata;
import org.openhab.core.ai.tool.api.validation.ResourceValidationResult;
import org.openhab.core.ai.tool.specification.AbstractBaseSpecification;

/**
 * Resource Specification for MCP Resources
 * 
 * This abstract class extends AbstractBaseSpecification and provides resource-specific functionality
 * including URI patterns, MIME types, and resource execution.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public abstract class ResourceSpecification extends AbstractBaseSpecification {

    /**
     * Create a new resource specification.
     * 
     * @param id the resource ID
     * @param name the resource name
     * @param description the resource description
     * @param version the resource version
     * @param inputSchema the input schema
     * @param outputSchema the output schema
     * @param configuration the resource configuration
     * @param metadata the resource metadata
     */
    protected ResourceSpecification(String id, String name, String description, String version,
            Map<String, Object> inputSchema, Map<String, Object> outputSchema, Map<String, Object> configuration,
            Map<String, Object> metadata) {
        super(id, name, description, version, inputSchema, outputSchema, configuration, metadata);
    }

    /**
     * Get the URI pattern for this resource
     * 
     * @return The URI pattern (e.g., "openhab://items/{itemName}")
     */
    public abstract String getUriPattern();

    /**
     * Get the MIME type for this resource
     * 
     * @return The MIME type (e.g., "application/vnd.openhab.item+json")
     */
    public abstract String getMimeType();

    /**
     * Get metadata for this resource
     * 
     * @return The resource metadata
     */
    public abstract ResourceMetadata getResourceMetadata();

    /**
     * Validate parameters for this resource
     * 
     * @param parameters The parameters to validate
     * @return Validation result
     */
    public abstract ResourceValidationResult validateParameters(Map<String, Object> parameters);

    /**
     * Execute the resource with given parameters
     * 
     * @param parameters The parameters for execution
     * @param context The execution context
     * @return The execution result
     */
    public abstract ResourceResult execute(Map<String, Object> parameters, ResourceContext context);

    /**
     * Get the specification type (always RESOURCE).
     * 
     * @return SpecificationType.RESOURCE
     */
    @Override
    public Specification.SpecificationType getType() {
        return Specification.SpecificationType.RESOURCE;
    }
}
