package org.openhab.core.ai.reasoning.error.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Result of handling an error in the reasoning system.
 *
 * @author Karel Goderis - Initial Contribution
 */
@NonNullByDefault
public class ErrorHandlingResult {
    private final boolean handled;
    private final String action;
    private final String message;
    private final boolean shouldRetry;

    public ErrorHandlingResult(boolean handled, String action, String message, boolean shouldRetry) {
        this.handled = handled;
        this.action = action;
        this.message = message;
        this.shouldRetry = shouldRetry;
    }

    public boolean isHandled() {
        return handled;
    }

    public String getAction() {
        return action;
    }

    public String getMessage() {
        return message;
    }

    public boolean shouldRetry() {
        return shouldRetry;
    }
}
