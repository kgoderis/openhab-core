package org.openhab.core.ai.tool.prompts.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Prompt Result for MCP Prompts
 *
 * This class defines execution results for MCP prompts
 * including success status, generated content, and timings.
 *
 * @author Karel Goderis - Initial Contribution
 */
@NonNullByDefault
public class PromptResult {

    private final boolean success;
    private final @Nullable String content;
    private final long executionTimeMs;
    private final @Nullable String errorMessage;

    public PromptResult(boolean success, @Nullable String content, long executionTimeMs,
            @Nullable String errorMessage) {
        this.success = success;
        this.content = content;
        this.executionTimeMs = executionTimeMs;
        this.errorMessage = errorMessage;
    }

    public static PromptResult success(@Nullable String content, long executionTimeMs) {
        return new PromptResult(true, content, executionTimeMs, null);
    }

    public static PromptResult failure(String errorMessage, long executionTimeMs) {
        return new PromptResult(false, null, executionTimeMs, errorMessage);
    }

    public boolean isSuccess() {
        return success;
    }

    public @Nullable String getContent() {
        return content;
    }

    public long getExecutionTimeMs() {
        return executionTimeMs;
    }

    public @Nullable String getErrorMessage() {
        return errorMessage;
    }
}
