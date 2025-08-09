package org.openhab.core.ai.tool.factory;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.tool.api.Tool;

/**
 * Factory for creating tool instances.
 * 
 * This class provides a fluent builder pattern for creating tool instances
 * with proper configuration and validation.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ToolFactory {

    private String id;
    private String name;
    private String description;
    private Map<String, Object> inputSchema;
    private Map<String, Object> outputSchema;
    private Map<String, Object> configuration;
    private Map<String, Object> metadata;

    /**
     * Set the tool ID.
     * 
     * @param id the tool ID
     * @return this builder
     */
    public ToolFactory withId(String id) {
        this.id = id;
        return this;
    }

    /**
     * Set the tool name.
     * 
     * @param name the tool name
     * @return this builder
     */
    public ToolFactory withName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Set the tool description.
     * 
     * @param description the tool description
     * @return this builder
     */
    public ToolFactory withDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * Set the input schema.
     * 
     * @param inputSchema the input schema
     * @return this builder
     */
    public ToolFactory withInputSchema(Map<String, Object> inputSchema) {
        this.inputSchema = inputSchema;
        return this;
    }

    /**
     * Set the output schema.
     * 
     * @param outputSchema the output schema
     * @return this builder
     */
    public ToolFactory withOutputSchema(Map<String, Object> outputSchema) {
        this.outputSchema = outputSchema;
        return this;
    }

    /**
     * Set the tool configuration.
     * 
     * @param configuration the tool configuration
     * @return this builder
     */
    public ToolFactory withConfiguration(Map<String, Object> configuration) {
        this.configuration = configuration;
        return this;
    }

    /**
     * Set the tool metadata.
     * 
     * @param metadata the tool metadata
     * @return this builder
     */
    public ToolFactory withMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
        return this;
    }

    /**
     * Build the tool instance.
     * 
     * @return the built tool
     */
    public Tool build() {
        // TODO: Implement tool creation logic
        // This should create a concrete Tool implementation based on the configuration
        throw new UnsupportedOperationException("Tool creation not yet implemented");
    }

    /**
     * Validate the current configuration.
     * 
     * @return true if valid, false otherwise
     */
    public boolean validate() {
        return id != null && !id.isEmpty() && name != null && !name.isEmpty();
    }
}
