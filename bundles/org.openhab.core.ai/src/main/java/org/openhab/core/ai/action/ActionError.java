package org.openhab.core.ai.action;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Represents an error that occurred during action execution.
 * 
 * This class provides structured error information including error codes,
 * messages, and additional context for debugging and error handling.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ActionError {

    private final String errorCode;
    private final String errorMessage;
    private final String errorType;
    private final @Nullable Throwable cause;

    public ActionError(String errorCode, String errorMessage) {
        this(errorCode, errorMessage, "EXECUTION_ERROR", null);
    }

    public ActionError(String errorCode, String errorMessage, String errorType) {
        this(errorCode, errorMessage, errorType, null);
    }

    public ActionError(String errorCode, String errorMessage, String errorType, @Nullable Throwable cause) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.errorType = errorType;
        this.cause = cause;
    }

    // Getters
    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getErrorType() {
        return errorType;
    }

    public @Nullable Throwable getCause() {
        return cause;
    }

    @Override
    public String toString() {
        return String.format("ActionError{code='%s', message='%s', type='%s'}", errorCode, errorMessage, errorType);
    }
}
