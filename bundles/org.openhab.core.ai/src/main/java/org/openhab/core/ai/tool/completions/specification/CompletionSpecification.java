package org.openhab.core.ai.tool.completions.specification;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.tool.api.Specification;
import org.openhab.core.ai.tool.specification.AbstractBaseSpecification;

/**
 * Completion specification model for MCP completions.
 * 
 * This class represents the specification of a completion, including its capabilities,
 * configuration, and metadata.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class CompletionSpecification extends AbstractBaseSpecification {

    /**
     * Create a new completion specification.
     * 
     * @param id the completion ID
     * @param name the completion name
     * @param description the completion description
     * @param version the completion version
     * @param inputSchema the input schema
     * @param outputSchema the output schema
     * @param configuration the completion configuration
     * @param metadata the completion metadata
     */
    public CompletionSpecification(String id, String name, String description, String version,
            Map<String, Object> inputSchema, Map<String, Object> outputSchema, Map<String, Object> configuration,
            Map<String, Object> metadata) {
        super(id, name, description, version, inputSchema, outputSchema, configuration, metadata);
    }

    @Override
    public Specification.SpecificationType getType() {
        return Specification.SpecificationType.COMPLETION;
    }

    // TODO: Implement completion specification validation
    // TODO: Add support for completion specification versioning
    // TODO: Implement completion specification serialization
    // TODO: Add support for completion specification comparison
}
