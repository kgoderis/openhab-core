package org.openhab.core.ai.tool.manager;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Error recovery statistics DTO for tools.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ErrorRecoveryStatistics {
    private final int totalErrors;
    private final int recoveredErrors;
    private final int unrecoveredErrors;
    private final int recoveryAttempts;

    public ErrorRecoveryStatistics(int totalErrors, int recoveredErrors, int unrecoveredErrors, int recoveryAttempts) {
        this.totalErrors = totalErrors;
        this.recoveredErrors = recoveredErrors;
        this.unrecoveredErrors = unrecoveredErrors;
        this.recoveryAttempts = recoveryAttempts;
    }

    public int getTotalErrors() {
        return totalErrors;
    }

    public int getRecoveredErrors() {
        return recoveredErrors;
    }

    public int getUnrecoveredErrors() {
        return unrecoveredErrors;
    }

    public int getRecoveryAttempts() {
        return recoveryAttempts;
    }
}


