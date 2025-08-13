package org.openhab.core.ai.tool.compliance;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Compliance test metrics summary.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ComplianceTestMetrics {
    private final String testId;
    private final String category;
    private final boolean passed;
    private final long durationMs;
    private final int failureCount;
    private final int warningCount;
    private final int detailCount;
    private final long timestamp;

    public ComplianceTestMetrics(String testId, String category, boolean passed, long durationMs, int failureCount,
            int warningCount, int detailCount, long timestamp) {
        this.testId = testId;
        this.category = category;
        this.passed = passed;
        this.durationMs = durationMs;
        this.failureCount = failureCount;
        this.warningCount = warningCount;
        this.detailCount = detailCount;
        this.timestamp = timestamp;
    }

    public String getTestId() {
        return testId;
    }

    public String getCategory() {
        return category;
    }

    public boolean isPassed() {
        return passed;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public int getFailureCount() {
        return failureCount;
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


