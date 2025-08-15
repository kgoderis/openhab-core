package org.openhab.core.ai.agent.collaboration.context;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Result of a context mutation operation.
 */
@NonNullByDefault
public interface ContextOperationResult {
    boolean isSuccess();

    String getMessage();

    static ContextOperationResult success(String message) {
        return new ContextOperationResult() {
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

    static ContextOperationResult failure(String message) {
        return new ContextOperationResult() {
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

    static ContextOperationResult permissionDenied(String message) {
        return new ContextOperationResult() {
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

    static ContextOperationResult validationFailed(String message) {
        return new ContextOperationResult() {
            @Override
            public boolean isSuccess() {
                return false;
            }

            @Override
            public String getMessage() {
                return "Validation failed: " + message;
            }
        };
    }

    static ContextOperationResult conflict(String message) {
        return new ContextOperationResult() {
            @Override
            public boolean isSuccess() {
                return false;
            }

            @Override
            public String getMessage() {
                return "Conflict: " + message;
            }
        };
    }

    static ContextOperationResult notFound(String message) {
        return new ContextOperationResult() {
            @Override
            public boolean isSuccess() {
                return false;
            }

            @Override
            public String getMessage() {
                return "Not found: " + message;
            }
        };
    }
}
