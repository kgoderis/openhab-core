package org.openhab.core.ai.reasoning;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Result wrapper for constraint operations.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ConstraintResult {
	private final boolean success;
	private final String message;
	private final boolean added;
	private final int constraintCount;

	public static ConstraintResult success(boolean added, int constraintCount) {
		return new ConstraintResult(true, "Constraint operation successful", added, constraintCount);
	}

	public static ConstraintResult notFound(String reason) {
		return new ConstraintResult(false, reason, false, 0);
	}

	public static ConstraintResult disabled(String reason) {
		return new ConstraintResult(false, "Constraint enforcement disabled: " + reason, false, 0);
	}

	public ConstraintResult(boolean success, String message, boolean added, int constraintCount) {
		this.success = success;
		this.message = message;
		this.added = added;
		this.constraintCount = constraintCount;
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

	public int getConstraintCount() {
		return constraintCount;
	}
}
