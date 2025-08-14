package org.openhab.core.ai.reasoning.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class MemoryStoreResult {
    private final boolean success;
    private final String memoryId;
    private final String error;

    public MemoryStoreResult(boolean success, String memoryId, String error) {
        this.success = success;
        this.memoryId = memoryId;
        this.error = error;
    }

    public boolean isSuccess() { return success; }
    public String getMemoryId() { return memoryId; }
    public String getError() { return error; }
}


