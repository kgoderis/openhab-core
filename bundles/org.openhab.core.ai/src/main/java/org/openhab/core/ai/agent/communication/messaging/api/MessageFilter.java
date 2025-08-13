package org.openhab.core.ai.agent.communication.messaging.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

import io.a2a.spec.Message;

/**
 * Interface for message filters.
 *
 * This interface defines the contract for message filters that can determine
 * whether a message should be delivered or filtered out based on specific criteria.
 *
 * Author: Karel Goderis - Initial Contribution
 */
@NonNullByDefault
public interface MessageFilter {

    /**
     * Determine if a message should be delivered.
     *
     * @param message the message to evaluate
     * @return true if the message should be delivered, false if it should be filtered out
     */
    boolean shouldDeliver(Message message);
}
