package org.openhab.core.ai.tool.compliance;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Result of compliance test comparison.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ComplianceTestComparisonResult {
    private final boolean sameTestId;
    private final boolean sameCategory;
    private final boolean samePassed;
    private final boolean sameStatus;
    private final boolean sameDuration;

    public ComplianceTestComparisonResult(boolean sameTestId, boolean sameCategory, boolean samePassed,
            boolean sameStatus, boolean sameDuration) {
        this.sameTestId = sameTestId;
        this.sameCategory = sameCategory;
        this.samePassed = samePassed;
        this.sameStatus = sameStatus;
        this.sameDuration = sameDuration;
    }

    public boolean isSameTestId() {
        return sameTestId;
    }

    public boolean isSameCategory() {
        return sameCategory;
    }

    public boolean isSamePassed() {
        return samePassed;
    }

    public boolean isSameStatus() {
        return sameStatus;
    }

    public boolean isSameDuration() {
        return sameDuration;
    }

    public boolean isIdentical() {
        return sameTestId && sameCategory && samePassed && sameStatus && sameDuration;
    }
}
