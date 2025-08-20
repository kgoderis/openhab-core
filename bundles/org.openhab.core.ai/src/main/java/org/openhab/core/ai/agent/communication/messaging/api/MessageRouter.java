package org.openhab.core.ai.agent.communication.messaging.api;

import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.communication.MessageDeliveryResult;

import io.a2a.spec.Message;

/**
 * Interface for message routers.
 *
 * This interface defines the contract for message routers that can route
 * messages to their intended destinations.
 *
 * Author: Karel Goderis - Initial Contribution
 */
@NonNullByDefault
public interface MessageRouter {

    /**
     * Route a message to its destination.
     *
     * @param message the message to route
     * @return a CompletableFuture containing the delivery result
     */
    CompletableFuture<MessageDeliveryResult> routeMessage(Message message);
}
