package org.openhab.core.ai.agent.execution;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Task schema definition.
 *
 * @author Karel Goderis - Initial Contribution
 */
@NonNullByDefault
public class TaskSchema {
    private final String actionId;
    private final String version;
    private final Map<String, SchemaParameter> parameters;
    private final List<String> requiredFields;
    private final List<String> optionalFields;
    private final Map<String, Object> constraints;
    private final List<SchemaExample> examples;
    private final String documentation;
    private final long createdAt;

    public TaskSchema(String actionId, String version, Map<String, SchemaParameter> parameters,
            List<String> requiredFields, List<String> optionalFields, Map<String, Object> constraints,
            List<SchemaExample> examples, String documentation, long createdAt) {
        this.actionId = actionId;
        this.version = version;
        this.parameters = parameters;
        this.requiredFields = requiredFields;
        this.optionalFields = optionalFields;
        this.constraints = constraints;
        this.examples = examples;
        this.documentation = documentation;
        this.createdAt = createdAt;
    }

    public String getActionId() { return actionId; }
    public String getVersion() { return version; }
    public Map<String, SchemaParameter> getParameters() { return parameters; }
    public List<String> getRequiredFields() { return requiredFields; }
    public List<String> getOptionalFields() { return optionalFields; }
    public Map<String, Object> getConstraints() { return constraints; }
    public List<SchemaExample> getExamples() { return examples; }
    public String getDocumentation() { return documentation; }
    public long getCreatedAt() { return createdAt; }
}


