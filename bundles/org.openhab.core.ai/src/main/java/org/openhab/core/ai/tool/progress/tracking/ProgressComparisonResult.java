package org.openhab.core.ai.tool.progress.tracking;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Result of progress comparison.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ProgressComparisonResult {
    private final boolean sameOperation;
    private final boolean sameProgress;
    private final boolean sameStatus;
    private final boolean sameMessage;
    private final boolean sameTimestamp;

    public ProgressComparisonResult(boolean sameOperation, boolean sameProgress, boolean sameStatus,
            boolean sameMessage, boolean sameTimestamp) {
        this.sameOperation = sameOperation;
        this.sameProgress = sameProgress;
        this.sameStatus = sameStatus;
        this.sameMessage = sameMessage;
        this.sameTimestamp = sameTimestamp;
    }

    public boolean isSameOperation() {
        return sameOperation;
    }

    public boolean isSameProgress() {
        return sameProgress;
    }

    public boolean isSameStatus() {
        return sameStatus;
    }

    public boolean isSameMessage() {
        return sameMessage;
    }

    public boolean isSameTimestamp() {
        return sameTimestamp;
    }

    public boolean isIdentical() {
        return sameOperation && sameProgress && sameStatus && sameMessage && sameTimestamp;
    }
}


