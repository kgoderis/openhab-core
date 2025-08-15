package org.openhab.core.ai.agent.collaboration.conflict;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Mediator interface for conflict resolution.
 *
 * Represents an entity that can mediate between participants in a conflict.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface ConflictMediator {
    String getMediatorId();

    ConflictResolution mediate(Conflict conflict, List<String> participants);
}
