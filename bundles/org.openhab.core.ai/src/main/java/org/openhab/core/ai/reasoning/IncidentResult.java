package org.openhab.core.ai.reasoning;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Result wrapper for incident reporting.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class IncidentResult {
	private final boolean success;
	private final String message;
	private final SafetyIncident incident;

	public static IncidentResult success(SafetyIncident incident) {
		return new IncidentResult(true, "Safety incident reported successfully", incident);
	}

	public static IncidentResult disabled(String reason) {
		return new IncidentResult(false, "Incident reporting disabled: " + reason, null);
	}

	public IncidentResult(boolean success, String message, SafetyIncident incident) {
		this.success = success;
		this.message = message;
		this.incident = incident;
	}

	public boolean isSuccess() {
		return success;
	}

	public String getMessage() {
		return message;
	}

	public SafetyIncident getIncident() {
		return incident;
	}
}
