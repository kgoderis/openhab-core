package org.openhab.core.ai.reasoning.security;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.security.SecurityIssue;

/**
 * Security event captured during validation.
 */
@NonNullByDefault
public class SecurityEvent {
    private final String eventId;
    private final String requestId;
    private final String agentId;
    private final boolean valid;
    private final List<SecurityIssue> issues;
    private final long timestamp;

    public SecurityEvent(String eventId, String requestId, String agentId, boolean valid, List<SecurityIssue> issues,
            long timestamp) {
        this.eventId = eventId;
        this.requestId = requestId;
        this.agentId = agentId;
        this.valid = valid;
        this.issues = new ArrayList<>(issues);
        this.timestamp = timestamp;
    }

    public String getEventId() {
        return eventId;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getAgentId() {
        return agentId;
    }

    public boolean isValid() {
        return valid;
    }

    public List<SecurityIssue> getIssues() {
        return new ArrayList<>(issues);
    }

    public long getTimestamp() {
        return timestamp;
    }
}
