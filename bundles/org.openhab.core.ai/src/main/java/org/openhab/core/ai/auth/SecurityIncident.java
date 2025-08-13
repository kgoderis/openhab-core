package org.openhab.core.ai.auth;

import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Security incident details for audit and monitoring.
 *
 * author Karel Goderis - Initial Contribution
 * since 1.0.0
 */
@NonNullByDefault
public class SecurityIncident {
	private final String incidentId;
	private final String principalId;
	private final String violationType;
	private final String description;
	private final String protocol;
	private final Instant timestamp;

	public SecurityIncident(String incidentId, String principalId, String violationType, String description,
			String protocol, Instant timestamp) {
		this.incidentId = incidentId;
		this.principalId = principalId;
		this.violationType = violationType;
		this.description = description;
		this.protocol = protocol;
		this.timestamp = timestamp;
	}

	public String getIncidentId() { return incidentId; }
	public String getPrincipalId() { return principalId; }
	public String getViolationType() { return violationType; }
	public String getDescription() { return description; }
	public String getProtocol() { return protocol; }
	public Instant getTimestamp() { return timestamp; }

	@Override
	public String toString() {
		return String.format(
				"SecurityIncident{incidentId='%s', principalId='%s', violationType='%s', description='%s', protocol='%s', timestamp=%s}",
				incidentId, principalId, violationType, description, protocol, timestamp);
	}
}
