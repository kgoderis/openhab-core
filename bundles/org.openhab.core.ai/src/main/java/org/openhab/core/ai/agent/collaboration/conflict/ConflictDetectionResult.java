package org.openhab.core.ai.agent.collaboration.conflict;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Result interface for conflict detection.
 *
 * Provides factory helpers for common outcomes.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface ConflictDetectionResult {
    boolean isSuccess();
    String getMessage();
    @Nullable Conflict getConflict();
    @Nullable ConflictAnalysis getAnalysis();

    static ConflictDetectionResult conflictDetected(Conflict conflict, ConflictAnalysis analysis) {
        return new ConflictDetectionResult() {
            @Override public boolean isSuccess() { return true; }
            @Override public String getMessage() { return "Conflict detected successfully"; }
            @Override public Conflict getConflict() { return conflict; }
            @Override public ConflictAnalysis getAnalysis() { return analysis; }
        };
    }

    static ConflictDetectionResult noConflict(String message) {
        return new ConflictDetectionResult() {
            @Override public boolean isSuccess() { return true; }
            @Override public String getMessage() { return message; }
            @Override public Conflict getConflict() { return null; }
            @Override public ConflictAnalysis getAnalysis() { return null; }
        };
    }

    static ConflictDetectionResult prevented(String message) {
        return new ConflictDetectionResult() {
            @Override public boolean isSuccess() { return true; }
            @Override public String getMessage() { return message; }
            @Override public Conflict getConflict() { return null; }
            @Override public ConflictAnalysis getAnalysis() { return null; }
        };
    }

    static ConflictDetectionResult failure(String message) {
        return new ConflictDetectionResult() {
            @Override public boolean isSuccess() { return false; }
            @Override public String getMessage() { return message; }
            @Override public Conflict getConflict() { return null; }
            @Override public ConflictAnalysis getAnalysis() { return null; }
        };
    }
}
