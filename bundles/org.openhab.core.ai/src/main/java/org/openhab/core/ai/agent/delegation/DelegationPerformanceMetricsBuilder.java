package org.openhab.core.ai.agent.delegation;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Builder for {@link DelegationPerformanceMetrics}.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public final class DelegationPerformanceMetricsBuilder {
    long totalDelegations;
    long successfulDelegations;
    long failedDelegations;
    long totalDelegationTime;
    int registeredAgents;

    public DelegationPerformanceMetricsBuilder totalDelegations(long v) {
        this.totalDelegations = v;
        return this;
    }

    public DelegationPerformanceMetricsBuilder successfulDelegations(long v) {
        this.successfulDelegations = v;
        return this;
    }

    public DelegationPerformanceMetricsBuilder failedDelegations(long v) {
        this.failedDelegations = v;
        return this;
    }

    public DelegationPerformanceMetricsBuilder totalDelegationTime(long v) {
        this.totalDelegationTime = v;
        return this;
    }

    public DelegationPerformanceMetricsBuilder registeredAgents(int v) {
        this.registeredAgents = v;
        return this;
    }

    public DelegationPerformanceMetrics build() {
        return new DelegationPerformanceMetrics(this);
    }
}
