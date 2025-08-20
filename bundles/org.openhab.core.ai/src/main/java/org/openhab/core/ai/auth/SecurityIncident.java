package org.openhab.core.ai.auth;

import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.security.SecuritySeverity;

/**
 * Comprehensive security incident details for audit and monitoring.
 *
 * <p>
 * This class represents security incidents across different contexts including
 * authentication, authorization, and general security management.
 * </p>
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class SecurityIncident {
    private final String incidentId;
    private final @Nullable String principalId;
    private final @Nullable String entityId;
    private final String incidentType;
    private final String description;
    private final @Nullable String protocol;
    private final Instant timestamp;
    private final @Nullable SecuritySeverity severity;

    /**
     * Create a new SecurityIncident for authentication/authorization context.
     * 
     * @param incidentId unique identifier for the incident
     * @param principalId the principal involved in the incident
     * @param incidentType type of security incident
     * @param description detailed description of the incident
     * @param protocol the protocol involved
     * @param timestamp when the incident occurred
     */
    public SecurityIncident(String incidentId, String principalId, String incidentType, String description,
            String protocol, Instant timestamp) {
        this.incidentId = incidentId;
        this.principalId = principalId;
        this.entityId = null;
        this.incidentType = incidentType;
        this.description = description;
        this.protocol = protocol;
        this.timestamp = timestamp;
        this.severity = null;
    }

    /**
     * Create a new SecurityIncident for entity security context.
     * 
     * @param incidentId unique identifier for the incident
     * @param entityId the entity involved in the incident (e.g., agent, service, component)
     * @param incidentType type of security incident
     * @param description detailed description of the incident
     * @param timestamp when the incident occurred
     * @param severity severity level of the incident
     */
    public SecurityIncident(String incidentId, String entityId, String incidentType, String description,
            Instant timestamp, SecuritySeverity severity) {
        this.incidentId = incidentId;
        this.principalId = null;
        this.entityId = entityId;
        this.incidentType = incidentType;
        this.description = description;
        this.protocol = null;
        this.timestamp = timestamp;
        this.severity = severity;
    }

    /**
     * Create a comprehensive SecurityIncident with all fields.
     * 
     * @param incidentId unique identifier for the incident
     * @param principalId the principal involved in the incident (optional)
     * @param entityId the entity involved in the incident (optional, e.g., agent, service, component)
     * @param incidentType type of security incident
     * @param description detailed description of the incident
     * @param protocol the protocol involved (optional)
     * @param timestamp when the incident occurred
     * @param severity severity level of the incident (optional)
     */
    public SecurityIncident(String incidentId, @Nullable String principalId, @Nullable String entityId,
            String incidentType, String description, @Nullable String protocol, Instant timestamp,
            @Nullable SecuritySeverity severity) {
        this.incidentId = incidentId;
        this.principalId = principalId;
        this.entityId = entityId;
        this.incidentType = incidentType;
        this.description = description;
        this.protocol = protocol;
        this.timestamp = timestamp;
        this.severity = severity;
    }

    /**
     * Get the unique incident identifier.
     * 
     * @return incident ID
     */
    public String getIncidentId() {
        return incidentId;
    }

    /**
     * Get the principal ID involved in the incident (auth context).
     * 
     * @return principal ID, or null if not applicable
     */
    public @Nullable String getPrincipalId() {
        return principalId;
    }

    /**
     * Get the entity ID involved in the incident (general context).
     * 
     * @return entity ID, or null if not applicable
     */
    public @Nullable String getEntityId() {
        return entityId;
    }

    /**
     * Get the type of security incident.
     * 
     * @return incident type
     */
    public String getIncidentType() {
        return incidentType;
    }

    /**
     * Get the detailed description of the incident.
     * 
     * @return incident description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Get the protocol involved in the incident (auth context).
     * 
     * @return protocol, or null if not applicable
     */
    public @Nullable String getProtocol() {
        return protocol;
    }

    /**
     * Get when the incident occurred.
     * 
     * @return incident timestamp
     */
    public Instant getTimestamp() {
        return timestamp;
    }

    /**
     * Get the severity level of the incident.
     * 
     * @return severity level, or null if not applicable
     */
    public @Nullable SecuritySeverity getSeverity() {
        return severity;
    }

    @Override
    public String toString() {
        return String.format(
                "SecurityIncident{incidentId='%s', principalId='%s', entityId='%s', incidentType='%s', description='%s', protocol='%s', timestamp=%s, severity=%s}",
                incidentId, principalId, entityId, incidentType, description, protocol, timestamp, severity);
    }
}
