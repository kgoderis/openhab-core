package org.openhab.core.ai.agent.collaboration.coordination;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Builder for {@link ConflictResolutionSession}.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ConflictResolutionSessionBuilder {
    String conflictId;
    List<String> conflictingAgents;
    ConflictType conflictType;
    Map<String, Object> conflictData;
    Instant startTime;
    ConflictResolutionState state;

    public ConflictResolutionSessionBuilder conflictId(String conflictId) { this.conflictId = conflictId; return this; }
    public ConflictResolutionSessionBuilder conflictingAgents(List<String> conflictingAgents) { this.conflictingAgents = conflictingAgents; return this; }
    public ConflictResolutionSessionBuilder conflictType(ConflictType conflictType) { this.conflictType = conflictType; return this; }
    public ConflictResolutionSessionBuilder conflictData(Map<String, Object> conflictData) { this.conflictData = conflictData; return this; }
    public ConflictResolutionSessionBuilder startTime(Instant startTime) { this.startTime = startTime; return this; }
    public ConflictResolutionSessionBuilder state(ConflictResolutionState state) { this.state = state; return this; }

    public ConflictResolutionSession build() {
        ConflictResolutionSession session = ConflictResolutionSession.builder()
                .conflictId(conflictId)
                .conflictingAgents(conflictingAgents)
                .conflictType(conflictType)
                .conflictData(conflictData)
                .startTime(startTime)
                .state(state)
                .build();
        return session;
    }
}


