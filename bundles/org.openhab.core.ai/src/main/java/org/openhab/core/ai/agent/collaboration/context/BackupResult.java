package org.openhab.core.ai.agent.collaboration.context;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Result of a backup operation.
 */
@NonNullByDefault
public interface BackupResult {
    boolean isSuccess();

    String getMessage();

    static BackupResult success(String message) {
        return new BackupResult() {
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

    static BackupResult failure(String message) {
        return new BackupResult() {
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

    static BackupResult notFound(String message) {
        return new BackupResult() {
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
