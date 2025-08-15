package org.openhab.core.ai.reasoning.security;

import java.time.Instant;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Recorded safety incident.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class SafetyIncident {
    private final String id;
    private final String agentId;
    private final String incidentType;
    private final String description;
    private final Map<String, Object> incidentData;
    private final String reporterId;
    private final Instant timestamp;

    public SafetyIncident(String agentId, String incidentType, String description, Map<String, Object> incidentData,
            String reporterId, Instant timestamp) {
        this.id = "incident-" + timestamp.toEpochMilli() + "-" + agentId;
        this.agentId = agentId;
        this.incidentType = incidentType;
        this.description = description;
        this.incidentData = incidentData;
        this.reporterId = reporterId;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
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

    public Map<String, Object> getIncidentData() {
        return incidentData;
    }

    public String getReporterId() {
        return reporterId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}
