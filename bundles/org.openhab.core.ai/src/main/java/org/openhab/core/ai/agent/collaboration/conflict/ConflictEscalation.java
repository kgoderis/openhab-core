package org.openhab.core.ai.agent.collaboration.conflict;

import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class ConflictEscalation {
    private final String conflictId;
    private final String escalationReason;
    private final EscalationLevel escalationLevel;
    private final Instant escalatedAt;

    ConflictEscalation(ConflictEscalationBuilder builder) {
        this.conflictId = builder.conflictId;
        this.escalationReason = builder.escalationReason;
        this.escalationLevel = builder.escalationLevel;
        this.escalatedAt = builder.escalatedAt;
    }

    public String getConflictId() { return conflictId; }
    public String getEscalationReason() { return escalationReason; }
    public EscalationLevel getEscalationLevel() { return escalationLevel; }
    public Instant getEscalatedAt() { return escalatedAt; }

    public static ConflictEscalationBuilder builder() { return new ConflictEscalationBuilder(); }
}


