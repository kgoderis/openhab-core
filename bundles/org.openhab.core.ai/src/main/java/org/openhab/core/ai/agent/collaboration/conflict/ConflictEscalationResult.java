package org.openhab.core.ai.agent.collaboration.conflict;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Result interface for conflict escalation.
 *
 * Provides factory helpers for common outcomes.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface ConflictEscalationResult {
    boolean isSuccess();

    String getMessage();

    @Nullable
    Conflict getConflict();

    @Nullable
    ConflictEscalation getEscalation();

    static ConflictEscalationResult success(Conflict conflict, ConflictEscalation escalation) {
        return new ConflictEscalationResult() {
            @Override
            public boolean isSuccess() {
                return true;
            }

            @Override
            public String getMessage() {
                return "Conflict escalated successfully";
            }

            @Override
            public Conflict getConflict() {
                return conflict;
            }

            @Override
            public ConflictEscalation getEscalation() {
                return escalation;
            }
        };
    }

    static ConflictEscalationResult failure(String message) {
        return new ConflictEscalationResult() {
            @Override
            public boolean isSuccess() {
                return false;
            }

            @Override
            public String getMessage() {
                return message;
            }

            @Override
            public Conflict getConflict() {
                return null;
            }

            @Override
            public ConflictEscalation getEscalation() {
                return null;
            }
        };
    }

    static ConflictEscalationResult notFound(String message) {
        return new ConflictEscalationResult() {
            @Override
            public boolean isSuccess() {
                return false;
            }

            @Override
            public String getMessage() {
                return message;
            }

            @Override
            public Conflict getConflict() {
                return null;
            }

            @Override
            public ConflictEscalation getEscalation() {
                return null;
            }
        };
    }
}
