package org.openhab.core.ai.agent.communication.messaging.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Interface for broadcast results.
 *
 * This interface defines the contract for broadcast results that indicate
 * the success or failure of broadcast operations and provide delivery statistics.
 *
 * Author: Karel Goderis - Initial Contribution
 */
@NonNullByDefault
public interface BroadcastResult {

    /**
     * Check if the broadcast was successful.
     *
     * @return true if successful, false otherwise
     */
    boolean isSuccess();

    /**
     * Get the result message.
     *
     * @return the result message
     */
    String getMessage();

    /**
     * Get the number of successful deliveries.
     *
     * @return the number of successful deliveries
     */
    long getSuccessfulDeliveries();

    /**
     * Get the total number of subscribers.
     *
     * @return the total number of subscribers
     */
    long getTotalSubscribers();

    /**
     * Create a successful broadcast result.
     *
     * @param successfulDeliveries the number of successful deliveries
     * @param totalSubscribers the total number of subscribers
     * @return a successful broadcast result
     */
    static BroadcastResult success(long successfulDeliveries, long totalSubscribers) {
        return new BroadcastResult() {
            @Override
            public boolean isSuccess() {
                return true;
            }

            @Override
            public String getMessage() {
                return "Broadcast successful";
            }

            @Override
            public long getSuccessfulDeliveries() {
                return successfulDeliveries;
            }

            @Override
            public long getTotalSubscribers() {
                return totalSubscribers;
            }
        };
    }

    /**
     * Create a broadcast result with no subscribers.
     *
     * @param message the result message
     * @return a broadcast result with no subscribers
     */
    static BroadcastResult noSubscribers(String message) {
        return new BroadcastResult() {
            @Override
            public boolean isSuccess() {
                return false;
            }

            @Override
            public String getMessage() {
                return message;
            }

            @Override
            public long getSuccessfulDeliveries() {
                return 0;
            }

            @Override
            public long getTotalSubscribers() {
                return 0;
            }
        };
    }
}
