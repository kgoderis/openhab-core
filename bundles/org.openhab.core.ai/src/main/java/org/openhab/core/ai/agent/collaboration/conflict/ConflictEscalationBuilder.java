package org.openhab.core.ai.agent.collaboration.conflict;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Builder for {@link ConflictEscalation}.
 */
@NonNullByDefault
public class ConflictEscalationBuilder {
    String conflictId;
    String escalationReason;
    EscalationLevel escalationLevel;
    java.time.Instant escalatedAt;

    public ConflictEscalationBuilder conflictId(String conflictId) {
        this.conflictId = conflictId;
        return this;
    }

    public ConflictEscalationBuilder escalationReason(String escalationReason) {
        this.escalationReason = escalationReason;
        return this;
    }

    public ConflictEscalationBuilder escalationLevel(EscalationLevel escalationLevel) {
        this.escalationLevel = escalationLevel;
        return this;
    }

    public ConflictEscalationBuilder escalatedAt(java.time.Instant escalatedAt) {
        this.escalatedAt = escalatedAt;
        return this;
    }

    public ConflictEscalation build() {
        return ConflictEscalation.builder().conflictId(conflictId).escalationReason(escalationReason)
                .escalationLevel(escalationLevel).escalatedAt(escalatedAt).build();
    }
}
