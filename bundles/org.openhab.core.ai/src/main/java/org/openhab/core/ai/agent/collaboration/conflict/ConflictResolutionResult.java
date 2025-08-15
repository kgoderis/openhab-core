package org.openhab.core.ai.agent.collaboration.conflict;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Result interface for conflict resolution.
 *
 * Provides factory helpers for common outcomes.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface ConflictResolutionResult {
    boolean isSuccess();

    String getMessage();

    @Nullable
    Conflict getConflict();

    @Nullable
    ConflictResolution getResolution();

    static ConflictResolutionResult success(Conflict conflict, ConflictResolution resolution) {
        return new ConflictResolutionResult() {
            @Override
            public boolean isSuccess() {
                return true;
            }

            @Override
            public String getMessage() {
                return "Conflict resolved successfully";
            }

            @Override
            public Conflict getConflict() {
                return conflict;
            }

            @Override
            public ConflictResolution getResolution() {
                return resolution;
            }
        };
    }

    static ConflictResolutionResult failure(String message) {
        return new ConflictResolutionResult() {
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
            public ConflictResolution getResolution() {
                return null;
            }
        };
    }

    static ConflictResolutionResult notFound(String message) {
        return new ConflictResolutionResult() {
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
            public ConflictResolution getResolution() {
                return null;
            }
        };
    }
}
