package org.openhab.core.ai.tool.completions.api;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Completion Result for MCP Completions
 *
 * This class defines execution results for MCP completions
 * including success status, suggestions, and timings.
 *
 * @author Karel Goderis - Initial Contribution
 */
@NonNullByDefault
public class CompletionResult {

    private final boolean success;
    private final List<String> suggestions;
    private final int total;
    private final boolean hasMore;
    private final long executionTimeMs;
    private final @Nullable String errorMessage;

    public CompletionResult(boolean success, List<String> suggestions, int total, boolean hasMore, long executionTimeMs,
            @Nullable String errorMessage) {
        this.success = success;
        this.suggestions = suggestions;
        this.total = total;
        this.hasMore = hasMore;
        this.executionTimeMs = executionTimeMs;
        this.errorMessage = errorMessage;
    }

    public static CompletionResult success(List<String> suggestions, int total, boolean hasMore, long executionTimeMs) {
        return new CompletionResult(true, suggestions, total, hasMore, executionTimeMs, null);
    }

    public static CompletionResult failure(String errorMessage, long executionTimeMs) {
        return new CompletionResult(false, List.of(), 0, false, executionTimeMs, errorMessage);
    }

    public boolean isSuccess() {
        return success;
    }

    public List<String> getSuggestions() {
        return suggestions;
    }

    public int getTotal() {
        return total;
    }

    public boolean isHasMore() {
        return hasMore;
    }

    public long getExecutionTimeMs() {
        return executionTimeMs;
    }

    public @Nullable String getErrorMessage() {
        return errorMessage;
    }
}
