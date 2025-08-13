package org.openhab.core.ai.agent.communication.conversation.api;

import java.time.Duration;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Interface for conversation end results.
 *
 * This interface defines the contract for conversation end results that indicate
 * the success or failure of conversation ending operations and provide duration
 * information.
 *
 * Author: Karel Goderis - Initial Contribution
 */
@NonNullByDefault
public interface ConversationEndResult {

    /**
     * Check if the conversation ending was successful.
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
     * Get the conversation duration.
     *
     * @return the conversation duration
     */
    Duration getDuration();

    /**
     * Create a successful conversation end result.
     *
     * @param message the success message
     * @param duration the conversation duration
     * @return a successful conversation end result
     */
    static ConversationEndResult success(String message, Duration duration) {
        return new ConversationEndResult() {
            @Override
            public boolean isSuccess() {
                return true;
            }

            @Override
            public String getMessage() {
                return message;
            }

            @Override
            public Duration getDuration() {
                return duration;
            }
        };
    }

    /**
     * Create a failed conversation end result.
     *
     * @param message the failure message
     * @return a failed conversation end result
     */
    static ConversationEndResult failure(String message) {
        return new ConversationEndResult() {
            @Override
            public boolean isSuccess() {
                return false;
            }

            @Override
            public String getMessage() {
                return message;
            }

            @Override
            public Duration getDuration() {
                return Duration.ZERO;
            }
        };
    }
}
