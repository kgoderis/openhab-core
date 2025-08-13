package org.openhab.core.ai.tool.prompts.dto;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * MCP Prompt data model.
 * 
 * This class represents a prompt in the MCP protocol, providing structured
 * prompts with arguments for AI model interactions.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class Prompt {

    private final String name;
    private final String description;
    private final List<PromptArgument> arguments;

    /**
     * Create a new prompt.
     * 
     * @param name Prompt name
     * @param description Prompt description
     * @param arguments List of prompt arguments
     */
    public Prompt(String name, String description, List<PromptArgument> arguments) {
        this.name = name;
        this.description = description;
        this.arguments = arguments;
    }

    /**
     * Get the prompt name.
     * 
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the prompt description.
     * 
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Get the prompt arguments.
     * 
     * @return the arguments
     */
    public List<PromptArgument> getArguments() {
        return arguments;
    }

    @Override
    public String toString() {
        return "Prompt{name='" + name + "', description='" + description + "', arguments=" + arguments + "}";
    }

    // PromptArgument extracted to org.openhab.core.ai.tool.prompts.dto.PromptArgument
}
