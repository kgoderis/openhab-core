package org.openhab.core.ai.tool.api;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Base specification interface for MCP specifications (Resources, Prompts, Completions).
 * 
 * This interface provides the common contract shared by all specification types
 * in the MCP tool system.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface Specification {

    /**
     * Get the specification ID.
     * 
     * @return the specification ID
     */
    String getId();

    /**
     * Get the specification name.
     * 
     * @return the specification name
     */
    String getName();

    /**
     * Get the specification description.
     * 
     * @return the specification description
     */
    String getDescription();

    /**
     * Get the specification version.
     * 
     * @return the specification version
     */
    String getVersion();

    /**
     * Get the input schema.
     * 
     * @return the input schema
     */
    Map<String, Object> getInputSchema();

    /**
     * Get the output schema.
     * 
     * @return the output schema
     */
    Map<String, Object> getOutputSchema();

    /**
     * Get the specification configuration.
     * 
     * @return the specification configuration
     */
    Map<String, Object> getConfiguration();

    /**
     * Get the specification metadata.
     * 
     * @return the specification metadata
     */
    Map<String, Object> getMetadata();

    /**
     * Get the specification type.
     * 
     * @return the specification type (RESOURCE, PROMPT, COMPLETION)
     */
    SpecificationType getType();

    /**
     * Validate the specification.
     * 
     * @return true if the specification is valid
     */
    boolean isValid();

    /**
     * Specification types.
     */
    enum SpecificationType {
        RESOURCE,
        PROMPT,
        COMPLETION
    }
}
