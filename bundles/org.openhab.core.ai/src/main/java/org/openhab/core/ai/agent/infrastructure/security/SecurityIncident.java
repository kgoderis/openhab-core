package org.openhab.core.ai.agent.infrastructure.security;

import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Security incident for agent security management
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class SecurityIncident {
    private final String incidentId;
    private final String agentId;
    private final String incidentType;
    private final String description;
    private final Instant timestamp;
    private final SecuritySeverity severity;

    public SecurityIncident(String incidentId, String agentId, String incidentType, String description,
            Instant timestamp, SecuritySeverity severity) {
        this.incidentId = incidentId;
        this.agentId = agentId;
        this.incidentType = incidentType;
        this.description = description;
        this.timestamp = timestamp;
        this.severity = severity;
    }

    // Getters
    public String getIncidentId() {
        return incidentId;
    }

    public String getAgentId() {
        return agentId;
    }

    public String getIncidentType() {
        return incidentType;
    }

    public String getDescription() {
        return description;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public SecuritySeverity getSeverity() {
        return severity;
    }

    // enum extracted to top-level: org.openhab.core.ai.agent.infrastructure.security.SecuritySeverity
}
