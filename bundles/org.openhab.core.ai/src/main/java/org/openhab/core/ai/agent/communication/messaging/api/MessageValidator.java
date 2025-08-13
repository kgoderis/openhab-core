package org.openhab.core.ai.agent.communication.messaging.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

import io.a2a.spec.Message;

/**
 * Interface for message validators.
 *
 * This interface defines the contract for message validators that can validate
 * messages to ensure they meet specific requirements before delivery.
 *
 * Author: Karel Goderis - Initial Contribution
 */
@NonNullByDefault
public interface MessageValidator {

    /**
     * Validate a message.
     *
     * @param message the message to validate
     * @return true if the message is valid, false otherwise
     */
    boolean validate(Message message);
}
