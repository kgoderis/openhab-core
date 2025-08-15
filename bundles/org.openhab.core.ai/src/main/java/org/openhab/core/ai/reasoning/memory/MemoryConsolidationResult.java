package org.openhab.core.ai.reasoning.memory;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

@NonNullByDefault
public class MemoryConsolidationResult {
    private final boolean success;
    private final int consolidatedCount;
    private final @Nullable String error;

    private MemoryConsolidationResult(boolean success, int consolidatedCount, @Nullable String error) {
        this.success = success;
        this.consolidatedCount = consolidatedCount;
        this.error = error;
    }

    public static MemoryConsolidationResult success(int consolidatedCount) {
        return new MemoryConsolidationResult(true, consolidatedCount, null);
    }

    public static MemoryConsolidationResult noData(String error) {
        return new MemoryConsolidationResult(false, 0, error);
    }

    public static MemoryConsolidationResult error(String error) {
        return new MemoryConsolidationResult(false, 0, error);
    }

    public boolean isSuccess() {
        return success;
    }

    public int getConsolidatedCount() {
        return consolidatedCount;
    }

    public @Nullable String getError() {
        return error;
    }
}
