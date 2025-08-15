package org.openhab.core.ai.agent.execution;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Schema parameter definition.
 */
@NonNullByDefault
public class SchemaParameter {
    private final String name;
    private final String type;
    private final String description;
    private final boolean required;
    private final Object defaultValue;
    private final Map<String, Object> constraints;

    public SchemaParameter(String name, String type, String description, boolean required, Object defaultValue,
            Map<String, Object> constraints) {
        this.name = name;
        this.type = type;
        this.description = description;
        this.required = required;
        this.defaultValue = defaultValue;
        this.constraints = constraints;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public boolean isRequired() {
        return required;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public Map<String, Object> getConstraints() {
        return constraints;
    }
}
