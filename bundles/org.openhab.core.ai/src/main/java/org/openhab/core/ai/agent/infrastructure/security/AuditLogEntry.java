package org.openhab.core.ai.agent.infrastructure.security;

import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Audit log entry for agent security management
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class AuditLogEntry {
    private final String event;
    private final String agentId;
    private final String target;
    private final String description;
    private final Instant timestamp;

    public AuditLogEntry(String event, String agentId, String target, String description, Instant timestamp) {
        this.event = event;
        this.agentId = agentId;
        this.target = target;
        this.description = description;
        this.timestamp = timestamp;
    }

    // Getters
    public String getEvent() {
        return event;
    }

    public String getAgentId() {
        return agentId;
    }

    public String getTarget() {
        return target;
    }

    public String getDescription() {
        return description;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}
