package org.openhab.core.ai.common.security;

import java.time.Instant;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Base implementation of SecurityStatistics interface.
 * 
 * <p>
 * This class provides common functionality for all security statistics implementations,
 * including basic properties and utility methods.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public abstract class BaseSecurityStatistics implements SecurityStatistics {

    private final long totalOperations;
    private final long successfulOperations;
    private final long failedOperations;
    private final long securityViolations;
    private final @Nullable Instant lastOperationTime;

    /**
     * Protected constructor for subclasses.
     * 
     * @param totalOperations Total number of security operations
     * @param successfulOperations Number of successful security operations
     * @param failedOperations Number of failed security operations
     * @param securityViolations Number of security violations
     * @param lastOperationTime Timestamp of last security operation
     */
    protected BaseSecurityStatistics(long totalOperations, long successfulOperations, long failedOperations,
            long securityViolations, @Nullable Instant lastOperationTime) {
        this.totalOperations = totalOperations;
        this.successfulOperations = successfulOperations;
        this.failedOperations = failedOperations;
        this.securityViolations = securityViolations;
        this.lastOperationTime = lastOperationTime;
    }

    @Override
    public long getTotalOperations() {
        return totalOperations;
    }

    @Override
    public long getSuccessfulOperations() {
        return successfulOperations;
    }

    @Override
    public long getFailedOperations() {
        return failedOperations;
    }

    @Override
    public long getSecurityViolations() {
        return securityViolations;
    }

    @Override
    public @Nullable Instant getLastOperationTime() {
        return lastOperationTime;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        BaseSecurityStatistics other = (BaseSecurityStatistics) obj;
        return totalOperations == other.totalOperations && successfulOperations == other.successfulOperations
                && failedOperations == other.failedOperations && securityViolations == other.securityViolations
                && Objects.equals(lastOperationTime, other.lastOperationTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(totalOperations, successfulOperations, failedOperations, securityViolations,
                lastOperationTime);
    }

    @Override
    public String toString() {
        return String.format(
                "BaseSecurityStatistics{totalOperations=%d, successfulOperations=%d, failedOperations=%d, securityViolations=%d, lastOperationTime=%s}",
                totalOperations, successfulOperations, failedOperations, securityViolations, lastOperationTime);
    }
}
