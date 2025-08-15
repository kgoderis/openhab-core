package org.openhab.core.ai.tool.filter.validators;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Filter validation metrics summary.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class FilterValidationMetrics {
    private final boolean valid;
    private final int errorCount;
    private final int warningCount;
    private final int detailCount;
    private final long timestamp;

    public FilterValidationMetrics(boolean valid, int errorCount, int warningCount, int detailCount, long timestamp) {
        this.valid = valid;
        this.errorCount = errorCount;
        this.warningCount = warningCount;
        this.detailCount = detailCount;
        this.timestamp = timestamp;
    }

    public boolean isValid() {
        return valid;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public int getWarningCount() {
        return warningCount;
    }

    public int getDetailCount() {
        return detailCount;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
