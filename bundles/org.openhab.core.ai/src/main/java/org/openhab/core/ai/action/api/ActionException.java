package org.openhab.core.ai.action.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Exception thrown when an action fails to execute.
 * 
 * This exception provides detailed error information including error codes,
 * messages, and context about what went wrong during action execution.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ActionException extends Exception {

    private static final long serialVersionUID = 1L;
    private final String actionId;
    private final String errorCode;

    public ActionException(String actionId, String message) {
        super(message);
        this.actionId = actionId;
        this.errorCode = "EXECUTION_ERROR";
    }

    public ActionException(String actionId, String message, String errorCode) {
        super(message);
        this.actionId = actionId;
        this.errorCode = errorCode;
    }

    public ActionException(String actionId, String message, Throwable cause) {
        super(message, cause);
        this.actionId = actionId;
        this.errorCode = "EXECUTION_ERROR";
    }

    public ActionException(String actionId, String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.actionId = actionId;
        this.errorCode = errorCode;
    }

    public String getActionId() {
        return actionId;
    }

    public String getErrorCode() {
        return errorCode;
    }

    @Override
    public String toString() {
        return String.format("ActionException{actionId='%s', errorCode='%s', message='%s'}", actionId, errorCode,
                getMessage());
    }
}
