package org.openhab.core.ai.tool.registry;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Result of prompt execution used by the registry facade.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class PromptExecutionResult {
    private final boolean success;
    private final @Nullable String errorMessage;
    private final @Nullable String content;

    public PromptExecutionResult(boolean success, @Nullable String errorMessage, @Nullable String content) {
        this.success = success;
        this.errorMessage = errorMessage;
        this.content = content;
    }

    public boolean isSuccess() {
        return success;
    }

    public @Nullable String getErrorMessage() {
        return errorMessage;
    }

    public @Nullable String getContent() {
        return content;
    }
}


