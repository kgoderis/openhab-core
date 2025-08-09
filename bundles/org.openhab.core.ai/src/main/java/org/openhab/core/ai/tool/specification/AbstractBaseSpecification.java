package org.openhab.core.ai.tool.specification;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.tool.api.Specification;

/**
 * Abstract base implementation for MCP specifications.
 * 
 * This class provides a concrete implementation of the common fields
 * and basic functionality shared by all specification types.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public abstract class AbstractBaseSpecification implements Specification {

    private final String id;
    private final String name;
    private final String description;
    private final String version;
    private final Map<String, Object> inputSchema;
    private final Map<String, Object> outputSchema;
    private final Map<String, Object> configuration;
    private final Map<String, Object> metadata;

    /**
     * Create a new abstract base specification.
     * 
     * @param id the specification ID
     * @param name the specification name
     * @param description the specification description
     * @param version the specification version
     * @param inputSchema the input schema
     * @param outputSchema the output schema
     * @param configuration the specification configuration
     * @param metadata the specification metadata
     */
    protected AbstractBaseSpecification(String id, String name, String description, String version,
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
    public String getVersion() {
        return version;
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
    public Map<String, Object> getConfiguration() {
        return configuration;
    }

    @Override
    public Map<String, Object> getMetadata() {
        return metadata;
    }

    @Override
    public boolean isValid() {
        return id != null && !id.isEmpty() && name != null && !name.isEmpty() && description != null
                && !description.isEmpty() && version != null && !version.isEmpty();
    }
}
