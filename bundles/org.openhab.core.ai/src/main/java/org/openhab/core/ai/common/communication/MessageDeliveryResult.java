package org.openhab.core.ai.common.communication;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Unified Interface for message delivery results.
 *
 * This interface defines the contract for message delivery results that indicate
 * the success or failure of message delivery operations across different
 * communication contexts (messaging, conversation, etc.).
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface MessageDeliveryResult {

    /**
     * Check if the message delivery was successful.
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
     * Create a successful message delivery result.
     *
     * @param message the success message
     * @return a successful message delivery result
     */
    static MessageDeliveryResult success(String message) {
        return new MessageDeliveryResult() {
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

    /**
     * Create a failed message delivery result.
     *
     * @param message the failure message
     * @return a failed message delivery result
     */
    static MessageDeliveryResult failure(String message) {
        return new MessageDeliveryResult() {
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

    /**
     * Create a filtered message delivery result.
     *
     * @param reason the filter reason
     * @return a filtered message delivery result
     */
    static MessageDeliveryResult filtered(String reason) {
        return new MessageDeliveryResult() {
            @Override
            public boolean isSuccess() {
                return false;
            }

            @Override
            public String getMessage() {
                return "Message filtered: " + reason;
            }
        };
    }
}
