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
	private final boolean added;
	private final int policyCount;

	public static PolicyResult success(boolean added, int policyCount) {
		return new PolicyResult(true, "Policy operation successful", added, policyCount);
	}

	public PolicyResult(boolean success, String message, boolean added, int policyCount) {
		this.success = success;
		this.message = message;
		this.added = added;
		this.policyCount = policyCount;
	}

	public boolean isSuccess() {
		return success;
	}

	public String getMessage() {
		return message;
	}

	public boolean isAdded() {
		return added;
	}

	public int getPolicyCount() {
		return policyCount;
	}
}
