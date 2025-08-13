package org.openhab.core.ai.reasoning;

import java.time.Instant;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Recorded constraint violation.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ConstraintViolation {
	private final String id;
	private final String agentId;
	private final String actionType;
	private final Map<String, Object> actionParameters;
	private final String userId;
	private final String reason;
	private final Instant timestamp;

	public ConstraintViolation(String agentId, String actionType, Map<String, Object> actionParameters, String userId,
			String reason, Instant timestamp) {
		this.id = "violation-" + timestamp.toEpochMilli() + "-" + agentId;
		this.agentId = agentId;
		this.actionType = actionType;
		this.actionParameters = actionParameters;
		this.userId = userId;
		this.reason = reason;
		this.timestamp = timestamp;
	}

	public String getId() { return id; }
	public String getAgentId() { return agentId; }
	public String getActionType() { return actionType; }
	public Map<String, Object> getActionParameters() { return actionParameters; }
	public String getUserId() { return userId; }
	public String getReason() { return reason; }
	public Instant getTimestamp() { return timestamp; }
}
