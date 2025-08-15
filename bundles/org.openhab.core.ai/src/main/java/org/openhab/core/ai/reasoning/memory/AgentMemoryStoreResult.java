package org.openhab.core.ai.reasoning.memory;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

@NonNullByDefault
public class AgentMemoryStoreResult {
    private final boolean success;
    private final @Nullable MemoryEntry entry;
    private final @Nullable String error;

    private AgentMemoryStoreResult(boolean success, @Nullable MemoryEntry entry, @Nullable String error) {
        this.success = success;
        this.entry = entry;
        this.error = error;
    }

    public static AgentMemoryStoreResult success(MemoryEntry entry) {
        return new AgentMemoryStoreResult(true, entry, null);
    }

    public static AgentMemoryStoreResult error(String error) {
        return new AgentMemoryStoreResult(false, null, error);
    }

    public boolean isSuccess() {
        return success;
    }

    public @Nullable MemoryEntry getEntry() {
        return entry;
    }

    public @Nullable String getError() {
        return error;
    }
}
