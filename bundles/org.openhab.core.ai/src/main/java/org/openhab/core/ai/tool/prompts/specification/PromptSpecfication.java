package org.openhab.core.ai.tool.prompts.specification;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.tool.api.Specification;
import org.openhab.core.ai.tool.specification.AbstractBaseSpecification;

/**
 * Prompt specification model for MCP prompts.
 * 
 * This class represents the specification of a prompt, including its capabilities,
 * configuration, and metadata.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class PromptSpecfication extends AbstractBaseSpecification {

    /**
     * Create a new prompt specification.
     * 
     * @param id the prompt ID
     * @param name the prompt name
     * @param description the prompt description
     * @param version the prompt version
     * @param inputSchema the input schema
     * @param outputSchema the output schema
     * @param configuration the prompt configuration
     * @param metadata the prompt metadata
     */
    public PromptSpecfication(String id, String name, String description, String version,
            Map<String, Object> inputSchema, Map<String, Object> outputSchema, Map<String, Object> configuration,
            Map<String, Object> metadata) {
        super(id, name, description, version, inputSchema, outputSchema, configuration, metadata);
    }

    @Override
    public Specification.SpecificationType getType() {
        return Specification.SpecificationType.PROMPT;
    }

    // TODO: Implement prompt specification validation
    // TODO: Add support for prompt specification versioning
    // TODO: Implement prompt specification serialization
    // TODO: Add support for prompt specification comparison
}
