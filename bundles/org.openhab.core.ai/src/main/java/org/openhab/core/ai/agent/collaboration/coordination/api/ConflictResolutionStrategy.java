package org.openhab.core.ai.agent.collaboration.coordination.api;

import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.agent.collaboration.coordination.ConflictResolutionSession;

/**
 * Interface for conflict resolution strategies.
 *
 * This interface defines the contract for conflict resolution strategies that can
 * resolve conflicts between agents.
 *
 * Author: Karel Goderis - Initial Contribution
 */
@NonNullByDefault
public interface ConflictResolutionStrategy {

    /**
     * Resolve a conflict using this strategy.
     *
     * @param session the conflict resolution session
     * @return a CompletableFuture containing the conflict resolution result
     */
    CompletableFuture<ConflictResolutionResult> resolve(ConflictResolutionSession session);
}
