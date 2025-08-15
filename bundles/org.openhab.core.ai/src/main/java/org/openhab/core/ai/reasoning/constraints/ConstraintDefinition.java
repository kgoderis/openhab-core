package org.openhab.core.ai.reasoning.constraints;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class ConstraintDefinition {
    private final String constraintId;
    private final String name;
    private final String description;
    private final String constraintType;
    private final Map<String, Object> parameters;
    private final boolean enabled;
    private final int severity;

    public ConstraintDefinition(String constraintId, String name, String description, String constraintType,
            Map<String, Object> parameters, boolean enabled, int severity) {
        this.constraintId = constraintId;
        this.name = name;
        this.description = description;
        this.constraintType = constraintType;
        this.parameters = parameters;
        this.enabled = enabled;
        this.severity = severity;
    }

    public String getConstraintId() {
        return constraintId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getConstraintType() {
        return constraintType;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getSeverity() {
        return severity;
    }
}
