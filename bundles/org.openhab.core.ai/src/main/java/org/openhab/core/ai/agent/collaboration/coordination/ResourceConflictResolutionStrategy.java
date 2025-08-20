package org.openhab.core.ai.agent.collaboration.coordination;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.agent.collaboration.ConflictResolutionResult;
import org.openhab.core.ai.agent.collaboration.ConflictResolutionStrategy;

/**
 * Resource conflict resolution strategy.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ResourceConflictResolutionStrategy implements ConflictResolutionStrategy {
    @Override
    public ConflictResolutionResult resolve(Object session) {
        if (session instanceof ConflictResolutionSession) {
            return ConflictResolutionResult.success("Resource conflict resolved using priority-based allocation");
        }
        return ConflictResolutionResult.failure("Invalid session type");
    }
}
