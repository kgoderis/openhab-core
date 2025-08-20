package org.openhab.core.ai.reasoning.memory;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Result of memory storage operations.
 * 
 * <p>
 * This class represents the result of memory storage operations,
 * including success status, stored memory entry, memory ID, and any error messages.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class MemoryStoreResult {
    private final boolean success;
    private final @Nullable MemoryEntry entry;
    private final @Nullable String memoryId;
    private final @Nullable String error;

    /**
     * Create a new memory store result.
     * 
     * @param success whether the storage was successful
     * @param entry the stored memory entry, or null if failed
     * @param memoryId the memory ID, or null if failed
     * @param error error message, or null if successful
     */
    private MemoryStoreResult(boolean success, @Nullable MemoryEntry entry, @Nullable String memoryId,
            @Nullable String error) {
        this.success = success;
        this.entry = entry;
        this.memoryId = memoryId;
        this.error = error;
    }

    /**
     * Create a successful memory store result with entry.
     * 
     * @param entry the stored memory entry
     * @return successful memory store result
     */
    public static MemoryStoreResult success(MemoryEntry entry) {
        return new MemoryStoreResult(true, entry, entry != null ? entry.getId() : null, null);
    }

    /**
     * Create a successful memory store result with memory ID.
     * 
     * @param memoryId the memory ID
     * @return successful memory store result
     */
    public static MemoryStoreResult success(String memoryId) {
        return new MemoryStoreResult(true, null, memoryId, null);
    }

    /**
     * Create an error memory store result.
     * 
     * @param error error message
     * @return error memory store result
     */
    public static MemoryStoreResult error(String error) {
        return new MemoryStoreResult(false, null, null, error);
    }

    /**
     * Check if the storage was successful.
     * 
     * @return true if successful, false otherwise
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Get the stored memory entry.
     * 
     * @return memory entry, or null if not available
     */
    public @Nullable MemoryEntry getEntry() {
        return entry;
    }

    /**
     * Get the memory ID.
     * 
     * @return memory ID, or null if not available
     */
    public @Nullable String getMemoryId() {
        return memoryId;
    }

    /**
     * Get the error message.
     * 
     * @return error message, or null if successful
     */
    public @Nullable String getError() {
        return error;
    }
}
