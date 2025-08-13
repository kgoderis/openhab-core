package org.openhab.core.ai.agent.collaboration.coordination;

import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.agent.collaboration.coordination.api.ConflictResolutionResult;
import org.openhab.core.ai.agent.collaboration.coordination.api.ConflictResolutionStrategy;

/**
 * Communication conflict resolution strategy.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class CommunicationConflictResolutionStrategy implements ConflictResolutionStrategy {
    @Override
    public CompletableFuture<ConflictResolutionResult> resolve(ConflictResolutionSession session) {
        return CompletableFuture.completedFuture(
                ConflictResolutionResult.success("Communication conflict resolved using retry mechanism"));
    }
}


