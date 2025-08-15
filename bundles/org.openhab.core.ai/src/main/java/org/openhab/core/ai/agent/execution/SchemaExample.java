package org.openhab.core.ai.agent.execution;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Schema example definition.
 */
@NonNullByDefault
public class SchemaExample {
    private final String name;
    private final String description;
    private final Map<String, Object> parameters;

    public SchemaExample(String name, String description, Map<String, Object> parameters) {
        this.name = name;
        this.description = description;
        this.parameters = parameters;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }
}
