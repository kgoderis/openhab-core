package org.openhab.core.ai.agent.collaboration.coordination.api;

import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.agent.collaboration.coordination.CoordinationSession;

/**
 * Interface for coordination protocols.
 *
 * This interface defines the contract for coordination protocols that can be
 * executed to coordinate activities between multiple agents.
 *
 * Author: Karel Goderis - Initial Contribution
 */
@NonNullByDefault
public interface CoordinationProtocol {

    /**
     * Execute the coordination protocol with the given session.
     *
     * @param session the coordination session to execute
     * @return a CompletableFuture containing the coordination result
     */
    CompletableFuture<CoordinationResult> execute(CoordinationSession session);
}
