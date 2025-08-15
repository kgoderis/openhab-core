package org.openhab.core.ai.agent.execution;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class SchemaVersion {
    private final String actionId;
    private final String version;
    private final TaskSchema schema;
    private final long createdAt;

    public SchemaVersion(String actionId, String version, TaskSchema schema, long createdAt) {
        this.actionId = actionId;
        this.version = version;
        this.schema = schema;
        this.createdAt = createdAt;
    }

    public String getActionId() {
        return actionId;
    }

    public String getVersion() {
        return version;
    }

    public TaskSchema getSchema() {
        return schema;
    }

    public long getCreatedAt() {
        return createdAt;
    }
}
