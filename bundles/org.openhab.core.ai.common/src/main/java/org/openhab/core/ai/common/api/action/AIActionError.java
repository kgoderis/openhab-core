package org.openhab.core.ai.common.api.action;

/**
 * Error information for AI action execution failures.
 * 
 * 
 */
public class AIActionError {

    private final String errorCode;
    private final String errorMessage;
    private final String errorType;
    private final Throwable cause;

    public AIActionError(String errorCode, String errorMessage) {
        this(errorCode, errorMessage, "EXECUTION_ERROR", null);
    }

    public AIActionError(String errorCode, String errorMessage, String errorType) {
        this(errorCode, errorMessage, errorType, null);
    }

    public AIActionError(String errorCode, String errorMessage, String errorType, Throwable cause) {
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

    public Throwable getCause() {
        return cause;
    }

    @Override
    public String toString() {
        return String.format("AIActionError{code='%s', message='%s', type='%s'}", errorCode, errorMessage, errorType);
    }
}
