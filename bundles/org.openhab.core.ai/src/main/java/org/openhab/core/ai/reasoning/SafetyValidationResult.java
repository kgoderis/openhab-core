package org.openhab.core.ai.reasoning;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Result of safety validation for an action.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class SafetyValidationResult {
	private final boolean valid;
	private final String reason;
	private final double confidence;

	public static SafetyValidationResult valid() {
		return new SafetyValidationResult(true, "Action is safe", 1.0);
	}

	public static SafetyValidationResult invalid(String reason) {
		return new SafetyValidationResult(false, reason, 0.0);
	}

	public static SafetyValidationResult disabled(String reason) {
		return new SafetyValidationResult(false, "Safety validation disabled: " + reason, 0.0);
	}

	public SafetyValidationResult(boolean valid, String reason, double confidence) {
		this.valid = valid;
		this.reason = reason;
		this.confidence = confidence;
	}

	public boolean isValid() {
		return valid;
	}

	public String getReason() {
		return reason;
	}

	public double getConfidence() {
		return confidence;
	}
}
