package org.openhab.core.ai.agent.collaboration.conflict;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class ConflictData {
    private final String description;
    private final int severity;
    private final Map<String, Object> data;
    private final List<String> involvedAgents;
    private final String resourceId;
    private final String policyId;

    public ConflictData(String description, int severity, Map<String, Object> data, List<String> involvedAgents,
            String resourceId, String policyId) {
        this.description = description;
        this.severity = severity;
        this.data = data;
        this.involvedAgents = involvedAgents;
        this.resourceId = resourceId;
        this.policyId = policyId;
    }

    public String getDescription() {
        return description;
    }

    public int getSeverity() {
        return severity;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public List<String> getInvolvedAgents() {
        return involvedAgents;
    }

    public String getResourceId() {
        return resourceId;
    }

    public String getPolicyId() {
        return policyId;
    }
}
