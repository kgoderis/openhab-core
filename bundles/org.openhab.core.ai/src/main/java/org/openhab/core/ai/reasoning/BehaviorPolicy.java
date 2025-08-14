package org.openhab.core.ai.reasoning;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class BehaviorPolicy {
    private final String policyId;
    private final String name;
    private final String description;
    private final String actionType;
    private final Map<String, Object> parameters;
    private final boolean enabled;
    private final int priority;

    public BehaviorPolicy(String policyId, String name, String description, String actionType,
            Map<String, Object> parameters, boolean enabled, int priority) {
        this.policyId = policyId;
        this.name = name;
        this.description = description;
        this.actionType = actionType;
        this.parameters = parameters;
        this.enabled = enabled;
        this.priority = priority;
    }

    public String getPolicyId() { return policyId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getActionType() { return actionType; }
    public Map<String, Object> getParameters() { return parameters; }
    public boolean isEnabled() { return enabled; }
    public int getPriority() { return priority; }
}


