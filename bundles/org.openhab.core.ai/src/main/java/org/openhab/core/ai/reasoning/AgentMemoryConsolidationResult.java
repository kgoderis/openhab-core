package org.openhab.core.ai.reasoning;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

@NonNullByDefault
public class AgentMemoryConsolidationResult {
    private final boolean success;
    private final int consolidatedCount;
    private final @Nullable String error;

    private AgentMemoryConsolidationResult(boolean success, int consolidatedCount, @Nullable String error) {
        this.success = success;
        this.consolidatedCount = consolidatedCount;
        this.error = error;
    }

    public static AgentMemoryConsolidationResult success(int consolidatedCount) { return new AgentMemoryConsolidationResult(true, consolidatedCount, null); }
    public static AgentMemoryConsolidationResult noData(String error) { return new AgentMemoryConsolidationResult(false, 0, error); }
    public static AgentMemoryConsolidationResult error(String error) { return new AgentMemoryConsolidationResult(false, 0, error); }
    public boolean isSuccess() { return success; }
    public int getConsolidatedCount() { return consolidatedCount; }
    public @Nullable String getError() { return error; }
}


