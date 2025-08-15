package org.openhab.core.ai.reasoning.memory.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class MemoryConsolidationResult {
    private final boolean success;
    private final int consolidatedCount;
    private final String error;

    public MemoryConsolidationResult(boolean success, int consolidatedCount, String error) {
        this.success = success;
        this.consolidatedCount = consolidatedCount;
        this.error = error;
    }

    public boolean isSuccess() {
        return success;
    }

    public int getConsolidatedCount() {
        return consolidatedCount;
    }

    public String getError() {
        return error;
    }
}
