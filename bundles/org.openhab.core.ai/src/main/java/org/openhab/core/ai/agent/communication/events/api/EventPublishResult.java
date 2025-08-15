package org.openhab.core.ai.agent.communication.events.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Event publish result contract with helpers.
 *
 * Provides success/failure/static helpers for publishing results.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface EventPublishResult {
    boolean isSuccess();

    String getMessage();

    static EventPublishResult success(String message) {
        return new EventPublishResult() {
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

    static EventPublishResult failure(String message) {
        return new EventPublishResult() {
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

    static EventPublishResult filtered(String reason) {
        return new EventPublishResult() {
            @Override
            public boolean isSuccess() {
                return false;
            }

            @Override
            public String getMessage() {
                return "Event filtered: " + reason;
            }
        };
    }

    static EventPublishResult schemaValidationFailed(String reason) {
        return new EventPublishResult() {
            @Override
            public boolean isSuccess() {
                return false;
            }

            @Override
            public String getMessage() {
                return "Schema validation failed: " + reason;
            }
        };
    }
}
