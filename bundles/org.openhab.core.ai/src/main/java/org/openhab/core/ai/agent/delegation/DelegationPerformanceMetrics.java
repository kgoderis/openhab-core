package org.openhab.core.ai.agent.delegation;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Performance metrics for agent action delegation.
 *
 * <p>
 * Immutable data object with a builder for convenient construction.
 * </p>
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class DelegationPerformanceMetrics {
    private final long totalDelegations;
    private final long successfulDelegations;
    private final long failedDelegations;
    private final long totalDelegationTime;
    private final int registeredAgents;

    /* package */ DelegationPerformanceMetrics(DelegationPerformanceMetricsBuilder builder) {
        this.totalDelegations = builder.totalDelegations;
        this.successfulDelegations = builder.successfulDelegations;
        this.failedDelegations = builder.failedDelegations;
        this.totalDelegationTime = builder.totalDelegationTime;
        this.registeredAgents = builder.registeredAgents;
    }

    public long getTotalDelegations() {
        return totalDelegations;
    }

    public long getSuccessfulDelegations() {
        return successfulDelegations;
    }

    public long getFailedDelegations() {
        return failedDelegations;
    }

    public long getTotalDelegationTime() {
        return totalDelegationTime;
    }

    public int getRegisteredAgents() {
        return registeredAgents;
    }

    public double getSuccessRate() {
        return totalDelegations > 0 ? (double) successfulDelegations / totalDelegations : 0.0;
    }

    public double getAverageDelegationTime() {
        return totalDelegations > 0 ? (double) totalDelegationTime / totalDelegations : 0.0;
    }

    public static DelegationPerformanceMetricsBuilder builder() {
        return new DelegationPerformanceMetricsBuilder();
    }
}
