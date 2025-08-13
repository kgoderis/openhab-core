package org.openhab.core.ai.agent.collaboration.conflict;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Strategy interface for conflict resolution algorithms.
 *
 * Implementations apply a specific resolution approach to a conflict.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface ConflictResolutionStrategy {
    ConflictResolution resolve(Conflict conflict, @Nullable ConflictMediator mediator);
}
