package org.openhab.core.ai.agent.collaboration.coordination;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.agent.collaboration.coordination.api.ConflictResolutionResult;

/**
 * Conflict resolution session data extracted from {@link AgentCoordinationManager}.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ConflictResolutionSession {
    private final String conflictId;
    private final List<String> conflictingAgents;
    private final ConflictType conflictType;
    private final Map<String, Object> conflictData;
    private final Instant startTime;
    private ConflictResolutionState state;
    private @Nullable ConflictResolutionResult resolution;
    private @Nullable String error;

    ConflictResolutionSession(ConflictResolutionSessionBuilder builder) {
        this.conflictId = builder.conflictId;
        this.conflictingAgents = builder.conflictingAgents;
        this.conflictType = builder.conflictType;
        this.conflictData = builder.conflictData;
        this.startTime = builder.startTime;
        this.state = builder.state;
    }

    public String getConflictId() {
        return conflictId;
    }

    public List<String> getConflictingAgents() {
        return conflictingAgents;
    }

    public ConflictType getConflictType() {
        return conflictType;
    }

    public Map<String, Object> getConflictData() {
        return conflictData;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public ConflictResolutionState getState() {
        return state;
    }

    public void setState(ConflictResolutionState state) {
        this.state = state;
    }

    public @Nullable ConflictResolutionResult getResolution() {
        return resolution;
    }

    public void setResolution(ConflictResolutionResult resolution) {
        this.resolution = resolution;
    }

    public @Nullable String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public static ConflictResolutionSessionBuilder builder() { return new ConflictResolutionSessionBuilder(); }
}


