package org.openhab.core.ai.reasoning.results;

import java.time.Instant;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.reasoning.constraints.ConstraintViolation;

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
        return new OverrideResult(false, message,
                new ConstraintViolation("unknown", "unknown", Map.of(), "unknown", message, Instant.now()));
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
