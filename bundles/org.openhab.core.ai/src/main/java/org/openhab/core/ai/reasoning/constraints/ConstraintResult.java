package org.openhab.core.ai.reasoning.constraints;

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
    private final ConstraintDefinition constraint;

    private ConstraintResult(boolean success, String message, ConstraintDefinition constraint) {
        this.success = success;
        this.message = message;
        this.constraint = constraint;
    }

    public static ConstraintResult success(ConstraintDefinition constraint) {
        return new ConstraintResult(true, "Constraint definition successful", constraint);
    }

    public static ConstraintResult disabled(String reason) {
        return new ConstraintResult(false, "Constraint operation disabled: " + reason, null);
    }

    public static ConstraintResult success(boolean ok, int count) {
        return new ConstraintResult(ok,
                ok ? "Constraint operation successful, count=" + count : "Constraint operation failed, count=" + count,
                null);
    }

    public static ConstraintResult notFound(String reason) {
        return new ConstraintResult(false, reason, null);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public ConstraintDefinition getConstraint() {
        return constraint;
    }
}
