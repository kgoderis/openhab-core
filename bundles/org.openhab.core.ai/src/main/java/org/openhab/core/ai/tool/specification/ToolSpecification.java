package org.openhab.core.ai.tool.specification;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Tool specification model for MCP tools.
 * 
 * This class represents the specification of a tool, including its capabilities,
 * configuration, and metadata.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ToolSpecification {

    private final String id;
    private final String name;
    private final String description;
    private final String version;
    private final Map<String, Object> inputSchema;
    private final Map<String, Object> outputSchema;
    private final Map<String, Object> configuration;
    private final Map<String, Object> metadata;

    /**
     * Create a new tool specification.
     * 
     * @param id the tool ID
     * @param name the tool name
     * @param description the tool description
     * @param version the tool version
     * @param inputSchema the input schema
     * @param outputSchema the output schema
     * @param configuration the tool configuration
     * @param metadata the tool metadata
     */
    public ToolSpecification(String id, String name, String description, String version,
            Map<String, Object> inputSchema, Map<String, Object> outputSchema, Map<String, Object> configuration,
            Map<String, Object> metadata) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.version = version;
        this.inputSchema = inputSchema;
        this.outputSchema = outputSchema;
        this.configuration = configuration;
        this.metadata = metadata;
    }

    /**
     * Get the tool ID.
     * 
     * @return the tool ID
     */
    public String getId() {
        return id;
    }

    /**
     * Get the tool name.
     * 
     * @return the tool name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the tool description.
     * 
     * @return the tool description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Get the tool version.
     * 
     * @return the tool version
     */
    public String getVersion() {
        return version;
    }

    /**
     * Get the input schema.
     * 
     * @return the input schema
     */
    public Map<String, Object> getInputSchema() {
        return inputSchema;
    }

    /**
     * Get the output schema.
     * 
     * @return the output schema
     */
    public Map<String, Object> getOutputSchema() {
        return outputSchema;
    }

    /**
     * Get the tool configuration.
     * 
     * @return the tool configuration
     */
    public Map<String, Object> getConfiguration() {
        return configuration;
    }

    /**
     * Get the tool metadata.
     * 
     * @return the tool metadata
     */
    public Map<String, Object> getMetadata() {
        return metadata;
    }

    // TODO: Implement tool specification validation
    // TODO: Add support for tool specification versioning
    // TODO: Implement tool specification serialization
    // TODO: Add support for tool specification comparison
}
