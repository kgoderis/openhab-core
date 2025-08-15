package org.openhab.core.ai.agent.communication.events;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Dead-letter queue processing result contract.
 *
 * Provides helpers for success/no-events outcomes.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface DeadLetterQueueResult {
    boolean isSuccess();

    String getMessage();

    long getSuccessfulRetries();

    long getTotalRetries();

    static DeadLetterQueueResult success(long successfulRetries, long totalRetries) {
        return new DeadLetterQueueResult() {
            @Override
            public boolean isSuccess() {
                return true;
            }

            @Override
            public String getMessage() {
                return "Dead letter queue processing successful";
            }

            @Override
            public long getSuccessfulRetries() {
                return successfulRetries;
            }

            @Override
            public long getTotalRetries() {
                return totalRetries;
            }
        };
    }

    static DeadLetterQueueResult noEventsToRetry() {
        return new DeadLetterQueueResult() {
            @Override
            public boolean isSuccess() {
                return true;
            }

            @Override
            public String getMessage() {
                return "No events to retry";
            }

            @Override
            public long getSuccessfulRetries() {
                return 0;
            }

            @Override
            public long getTotalRetries() {
                return 0;
            }
        };
    }
}
