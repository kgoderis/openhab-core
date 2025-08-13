package org.openhab.core.ai.agent.collaboration.conflict;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Builder for {@link ConflictEscalation}.
 */
@NonNullByDefault
public class ConflictEscalationBuilder {
    private String conflictId;
    private String escalationReason;
    private EscalationLevel escalationLevel;

    public ConflictEscalationBuilder conflictId(String conflictId) { this.conflictId = conflictId; return this; }
    public ConflictEscalationBuilder escalationReason(String escalationReason) { this.escalationReason = escalationReason; return this; }
    public ConflictEscalationBuilder escalationLevel(EscalationLevel escalationLevel) { this.escalationLevel = escalationLevel; return this; }

    public ConflictEscalation build() { return ConflictEscalation.builder().conflictId(conflictId).escalationReason(escalationReason).escalationLevel(escalationLevel).build(); }
}


