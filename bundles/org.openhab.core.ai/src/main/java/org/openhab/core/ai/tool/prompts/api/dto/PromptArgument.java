package org.openhab.core.ai.tool.prompts.api.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Prompt argument data model.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class PromptArgument {
    private final String name;
    private final String description;
    private final boolean required;

    public PromptArgument(String name, String description, boolean required) {
        this.name = name;
        this.description = description;
        this.required = required;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean isRequired() {
        return required;
    }

    @Override
    public String toString() {
        return "PromptArgument{name='" + name + "', description='" + description + "', required=" + required + "}";
    }
}
