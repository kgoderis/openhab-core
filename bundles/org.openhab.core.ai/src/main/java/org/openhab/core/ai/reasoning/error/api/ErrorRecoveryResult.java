package org.openhab.core.ai.reasoning.error.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Result of attempting to recover from an error.
 *
 * @author Karel Goderis - Initial Contribution
 */
@NonNullByDefault
public class ErrorRecoveryResult {
    private final boolean recovered;
    private final String recoveryAction;
    private final String message;
    private final long recoveryTime;

    public ErrorRecoveryResult(boolean recovered, String recoveryAction, String message, long recoveryTime) {
        this.recovered = recovered;
        this.recoveryAction = recoveryAction;
        this.message = message;
        this.recoveryTime = recoveryTime;
    }

    public boolean isRecovered() {
        return recovered;
    }

    public String getRecoveryAction() {
        return recoveryAction;
    }

    public String getMessage() {
        return message;
    }

    public long getRecoveryTime() {
        return recoveryTime;
    }
}
