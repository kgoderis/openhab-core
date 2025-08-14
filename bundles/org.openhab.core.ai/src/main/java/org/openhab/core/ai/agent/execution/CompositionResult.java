package org.openhab.core.ai.agent.execution;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.agent.api.AgentSkillResult;

@NonNullByDefault
public final class CompositionResult {
    private final String compositionId;
    private final boolean success;
    private final @Nullable List<AgentSkillResult> results;
    private final @Nullable String message;
    private final long executionTime;

    private CompositionResult(
            String compositionId,
            boolean success,
            @Nullable List<AgentSkillResult> results,
            @Nullable String message,
            long executionTime) {
        this.compositionId = compositionId;
        this.success = success;
        this.results = results;
        this.message = message;
        this.executionTime = executionTime;
    }

    public static CompositionResult success(String compositionId, List<AgentSkillResult> results, long executionTime) {
        return new CompositionResult(compositionId, true, results, null, executionTime);
    }

    public static CompositionResult error(String compositionId, String message, long executionTime) {
        return new CompositionResult(compositionId, false, null, message, executionTime);
    }

    public String getCompositionId() { return compositionId; }
    public boolean isSuccess() { return success; }
    public @Nullable List<AgentSkillResult> getResults() { return results; }
    public @Nullable String getMessage() { return message; }
    public long getExecutionTime() { return executionTime; }
}


