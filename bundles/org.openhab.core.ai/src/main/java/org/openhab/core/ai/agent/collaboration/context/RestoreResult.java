package org.openhab.core.ai.agent.collaboration.context;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Result of a restore operation.
 */
@NonNullByDefault
public interface RestoreResult {
    boolean isSuccess();

    String getMessage();

    static RestoreResult success(String message) {
        return new RestoreResult() {
            @Override
            public boolean isSuccess() {
                return true;
            }

            @Override
            public String getMessage() {
                return message;
            }
        };
    }

    static RestoreResult failure(String message) {
        return new RestoreResult() {
            @Override
            public boolean isSuccess() {
                return false;
            }

            @Override
            public String getMessage() {
                return message;
            }
        };
    }

    static RestoreResult permissionDenied(String message) {
        return new RestoreResult() {
            @Override
            public boolean isSuccess() {
                return false;
            }

            @Override
            public String getMessage() {
                return "Permission denied: " + message;
            }
        };
    }
}
