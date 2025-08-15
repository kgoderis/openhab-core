package org.openhab.core.ai.reasoning.results;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Quick security check result for low-risk operations.
 */
@NonNullByDefault
public class QuickSecurityResult {
    private final String agentId;
    private final String actionType;
    private boolean valid;
    private String reason;

    public QuickSecurityResult(String agentId, String actionType) {
        this.agentId = agentId;
        this.actionType = actionType;
    }

    public String getAgentId() {
        return agentId;
    }

    public String getActionType() {
        return actionType;
    }

    public boolean isValid() {
        return valid;
    }

    public String getReason() {
        return reason;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
