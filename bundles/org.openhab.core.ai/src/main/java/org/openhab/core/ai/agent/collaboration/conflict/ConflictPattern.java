package org.openhab.core.ai.agent.collaboration.conflict;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Conflict pattern interface.
 *
 * Represents a pattern used for conflict analytics.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface ConflictPattern {
    String getPatternId();
    boolean matches(Conflict conflict);
    void recordOccurrence(Conflict conflict);
}
