package org.openhab.core.ai.reasoning.memory;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Result of memory consolidation operations.
 * 
 * <p>
 * This class represents the result of memory consolidation operations,
 * including success status, number of consolidated items, and any error messages.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class MemoryConsolidationResult {
    private final boolean success;
    private final int consolidatedCount;
    private final @Nullable String error;

    /**
     * Create a new memory consolidation result.
     * 
     * @param success whether the consolidation was successful
     * @param consolidatedCount number of items consolidated
     * @param error error message, or null if successful
     */
    public MemoryConsolidationResult(boolean success, int consolidatedCount, @Nullable String error) {
        this.success = success;
        this.consolidatedCount = consolidatedCount;
        this.error = error;
    }

    /**
     * Create a successful memory consolidation result.
     * 
     * @param consolidatedCount number of items consolidated
     * @return successful memory consolidation result
     */
    public static MemoryConsolidationResult success(int consolidatedCount) {
        return new MemoryConsolidationResult(true, consolidatedCount, null);
    }

    /**
     * Create a memory consolidation result for no data available.
     * 
     * @param error error message explaining why no data was available
     * @return memory consolidation result for no data
     */
    public static MemoryConsolidationResult noData(String error) {
        return new MemoryConsolidationResult(false, 0, error);
    }

    /**
     * Create an error memory consolidation result.
     * 
     * @param error error message
     * @return error memory consolidation result
     */
    public static MemoryConsolidationResult error(String error) {
        return new MemoryConsolidationResult(false, 0, error);
    }

    /**
     * Check if the consolidation was successful.
     * 
     * @return true if successful, false otherwise
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Get the number of items that were consolidated.
     * 
     * @return number of consolidated items
     */
    public int getConsolidatedCount() {
        return consolidatedCount;
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
