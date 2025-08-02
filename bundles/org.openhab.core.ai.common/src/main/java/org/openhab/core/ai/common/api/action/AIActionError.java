package org.openhab.core.ai.common.api.action;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Represents an error that occurred during AI action execution.
 * 
 * This class provides structured error information including error codes,
 * messages, and additional context for debugging and error handling.
 * 
 * @author AI Assistant
 * @since 1.0.0
 */
@NonNullByDefault
public class AIActionError {

    private final String errorCode;
    private final String errorMessage;
    private final String errorType;
    private final @Nullable Throwable cause;

    public AIActionError(String errorCode, String errorMessage) {
        this(errorCode, errorMessage, "EXECUTION_ERROR", null);
    }

    public AIActionError(String errorCode, String errorMessage, String errorType) {
        this(errorCode, errorMessage, errorType, null);
    }

    public AIActionError(String errorCode, String errorMessage, String errorType, @Nullable Throwable cause) {
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
        return String.format("AIActionError{code='%s', message='%s', type='%s'}", errorCode, errorMessage, errorType);
    }
}
