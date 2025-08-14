package org.openhab.core.ai.reasoning;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Result wrapper for policy operations.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class PolicyResult {
	private final boolean success;
	private final String message;
	private final AutonomousBehaviorConfig.BehaviorPolicy policy;

	private PolicyResult(boolean success, String message, AutonomousBehaviorConfig.BehaviorPolicy policy) {
		this.success = success;
		this.message = message;
		this.policy = policy;
	}

	public static PolicyResult success(AutonomousBehaviorConfig.BehaviorPolicy policy) {
		return new PolicyResult(true, "Policy operation successful", policy);
	}

    public static PolicyResult success(boolean ok, int count) {
        return new PolicyResult(ok, ok ? "Policy operation successful, count=" + count
                : "Policy operation failed, count=" + count, null);
    }

	public boolean isSuccess() { return success; }
	public String getMessage() { return message; }
	public AutonomousBehaviorConfig.BehaviorPolicy getPolicy() { return policy; }
}
