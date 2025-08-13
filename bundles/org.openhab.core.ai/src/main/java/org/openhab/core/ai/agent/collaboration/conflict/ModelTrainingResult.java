package org.openhab.core.ai.agent.collaboration.conflict;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Result interface for conflict-learning model training.
 *
 * Provides helpers for success/failure outcomes.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface ModelTrainingResult {
    boolean isSuccess();
    String getMessage();

    static ModelTrainingResult success(String message) {
        return new ModelTrainingResult() {
            @Override public boolean isSuccess() { return true; }
            @Override public String getMessage() { return message; }
        };
    }

    static ModelTrainingResult failure(String message) {
        return new ModelTrainingResult() {
            @Override public boolean isSuccess() { return false; }
            @Override public String getMessage() { return message; }
        };
    }
}
