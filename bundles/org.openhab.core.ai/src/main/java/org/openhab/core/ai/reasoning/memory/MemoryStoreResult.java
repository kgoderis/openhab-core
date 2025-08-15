package org.openhab.core.ai.reasoning.memory;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

@NonNullByDefault
public class MemoryStoreResult {
    private final boolean success;
    private final @Nullable MemoryEntry entry;
    private final @Nullable String error;

    private MemoryStoreResult(boolean success, @Nullable MemoryEntry entry, @Nullable String error) {
        this.success = success;
        this.entry = entry;
        this.error = error;
    }

    public static MemoryStoreResult success(MemoryEntry entry) {
        return new MemoryStoreResult(true, entry, null);
    }

    public static MemoryStoreResult error(String error) {
        return new MemoryStoreResult(false, null, error);
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
