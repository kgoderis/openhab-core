package org.openhab.core.ai.tool.factory;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.tool.api.Tool;
import org.openhab.core.ai.tool.api.ToolContext;
import org.openhab.core.ai.tool.api.ToolException;
import org.openhab.core.ai.tool.api.ToolMetadata;
import org.openhab.core.ai.tool.api.ToolResult;
import org.openhab.core.ai.tool.api.ToolValidationResult;

/**
 * Factory for creating MCP tools.
 * 
 * This class provides a builder pattern for creating Tool instances
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
        // Validate the configuration before building
        if (!validate()) {
            throw new IllegalStateException("Tool configuration is invalid. Required fields: id, name");
        }

        // Create a concrete Tool implementation based on the configuration
        return new Tool() {
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
                return description != null ? description : "";
            }

            @Override
            public Map<String, Object> getInputSchema() {
                return inputSchema != null ? inputSchema : Map.of();
            }

            @Override
            public Map<String, Object> getOutputSchema() {
                return outputSchema != null ? outputSchema : Map.of();
            }

            @Override
            public ToolMetadata getMetadata() {
                String version = metadata != null && metadata.containsKey("version")
                        ? metadata.get("version").toString()
                        : "1.0.0";
                String author = metadata != null && metadata.containsKey("author") ? metadata.get("author").toString()
                        : "Unknown";
                String desc = description != null ? description : "No description provided";

                return ToolMetadata.builder().version(version).author(author).description(desc).build();
            }

            @Override
            public ToolValidationResult validateParameters(Map<String, Object> parameters) {
                // Basic validation - check if required parameters are present
                if (inputSchema != null && inputSchema.containsKey("required")) {
                    @SuppressWarnings("unchecked")
                    var requiredParams = (List<String>) inputSchema.get("required");
                    if (requiredParams != null) {
                        for (String requiredParam : requiredParams) {
                            if (!parameters.containsKey(requiredParam)) {
                                return ToolValidationResult.invalid("Missing required parameter: " + requiredParam);
                            }
                        }
                    }
                }
                return ToolValidationResult.valid();
            }

            @Override
            public ToolResult execute(Map<String, Object> parameters, ToolContext context) throws ToolException {
                long startTime = System.currentTimeMillis();

                try {
                    // Execute the tool based on configuration
                    // Check if there's a custom execution handler in configuration
                    if (configuration != null && configuration.containsKey("executionHandler")) {
                        // TODO: Implement custom execution handler logic
                        long executionTime = System.currentTimeMillis() - startTime;
                        return ToolResult.successJson(id, Map.of("result", "success"), executionTime);
                    }

                    // Default execution - return success with parameters
                    long executionTime = System.currentTimeMillis() - startTime;
                    return ToolResult.successJson(id, parameters, executionTime);

                } catch (Exception e) {
                    long executionTime = System.currentTimeMillis() - startTime;
                    return ToolResult.error(id, "Tool execution failed: " + e.getMessage(), executionTime);
                }
            }

            @Override
            public String toString() {
                return "Tool{id='" + id + "', name='" + name + "', description='" + description + "'}";
            }
        };
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
