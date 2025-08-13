package org.openhab.core.ai.reasoning;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Result wrapper for safety overrides.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class OverrideResult {
	private final boolean success;
	private final String message;
	private final ConstraintViolation violation;

    public static OverrideResult success(ConstraintViolation violation) {
        return new OverrideResult(true, "Safety constraint overridden successfully", violation);
    }

    public static OverrideResult notFound(String message) {
        return new OverrideResult(false, message, new ConstraintViolation("unknown", "unknown",
                java.util.Map.of(), "unknown", message, java.time.Instant.now()));
    }

	public OverrideResult(boolean success, String message, ConstraintViolation violation) {
		this.success = success;
		this.message = message;
		this.violation = violation;
	}

	public boolean isSuccess() {
		return success;
	}

	public String getMessage() {
		return message;
	}

	public ConstraintViolation getViolation() {
		return violation;
	}
}
