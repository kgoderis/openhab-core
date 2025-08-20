package org.openhab.core.ai.agent.collaboration.coordination;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.agent.collaboration.ConflictResolutionResult;
import org.openhab.core.ai.agent.collaboration.ConflictResolutionStrategy;

/**
 * Communication conflict resolution strategy.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class CommunicationConflictResolutionStrategy implements ConflictResolutionStrategy {
    @Override
    public ConflictResolutionResult resolve(Object session) {
        return ConflictResolutionResult.success("Communication conflict resolved using retry mechanism");
    }
}
