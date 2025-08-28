package org.openhab.core.ai.common.monitoring.service.snapshot;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.MetricsSnapshot;

/**
 * Snapshot record for audit logging metrics.
 * 
 * <p>
 * This record provides audit logging metrics including
 * security events, tool operations, authentication attempts,
 * authorization decisions, and security violations. It implements
 * CountsMetrics capability interface.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record AuditSnapshot(String auditCategory, String eventType, long totalEvents, long successfulEvents,
        long failedEvents, long securityViolations, long authenticationAttempts, long authorizationDecisions,
        long toolOperations, long systemEvents, long criticalEvents, long warningEvents, long infoEvents,
        long debugEvents, long totalDurationNanos, long averageEventProcessingTimeNanos,
        long maxEventProcessingTimeNanos, long minEventProcessingTimeNanos, boolean auditEnabled,
        long auditRetentionDays, long timestampMs) implements MetricsSnapshot, CountsMetrics {

    // CountsMetrics implementation
    @Override
    public long total() {
        return totalEvents;
    }

    @Override
    public long success() {
        return successfulEvents;
    }

    @Override
    public long failure() {
        return failedEvents;
    }

    /**
     * Calculate event processing success rate.
     * 
     * @return success rate between 0.0 and 1.0
     */
    public double eventSuccessRate() {
        return totalEvents > 0 ? (double) successfulEvents / totalEvents : 0.0;
    }

    /**
     * Calculate security violation rate.
     * 
     * @return violation rate between 0.0 and 1.0
     */
    public double securityViolationRate() {
        return totalEvents > 0 ? (double) securityViolations / totalEvents : 0.0;
    }

    /**
     * Calculate authentication success rate.
     * 
     * @return authentication success rate between 0.0 and 1.0
     */
    public double authenticationSuccessRate() {
        return authenticationAttempts > 0 ? (double) successfulEvents / authenticationAttempts : 0.0;
    }

    /**
     * Get average event processing time in milliseconds.
     * 
     * @return average processing time in ms
     */
    public double averageEventProcessingTimeMs() {
        return averageEventProcessingTimeNanos / 1_000_000.0;
    }

    /**
     * Get maximum event processing time in milliseconds.
     * 
     * @return maximum processing time in ms
     */
    public double maxEventProcessingTimeMs() {
        return maxEventProcessingTimeNanos / 1_000_000.0;
    }

    /**
     * Get minimum event processing time in milliseconds.
     * 
     * @return minimum processing time in ms
     */
    public double minEventProcessingTimeMs() {
        return minEventProcessingTimeNanos / 1_000_000.0;
    }

    /**
     * Calculate critical event rate.
     * 
     * @return critical event rate between 0.0 and 1.0
     */
    public double criticalEventRate() {
        return totalEvents > 0 ? (double) criticalEvents / totalEvents : 0.0;
    }

    /**
     * Calculate warning event rate.
     * 
     * @return warning event rate between 0.0 and 1.0
     */
    public double warningEventRate() {
        return totalEvents > 0 ? (double) warningEvents / totalEvents : 0.0;
    }

    /**
     * Get event severity distribution.
     * 
     * @return severity distribution as percentage
     */
    public String eventSeverityDistribution() {
        if (totalEvents == 0) {
            return "no-events";
        }

        double criticalPct = (double) criticalEvents / totalEvents * 100.0;
        double warningPct = (double) warningEvents / totalEvents * 100.0;
        double infoPct = (double) infoEvents / totalEvents * 100.0;

        return String.format("critical:%.1f%%, warning:%.1f%%, info:%.1f%%", criticalPct, warningPct, infoPct);
    }

    /**
     * Check if audit system is performing well.
     * 
     * @return true if success rate > 95% and audit is enabled
     */
    public boolean isHealthy() {
        return eventSuccessRate() > 0.95 && auditEnabled;
    }

    /**
     * Check if audit system is under stress.
     * 
     * @return true if failure rate > 5% or critical events > 10%
     */
    public boolean isStressed() {
        double failureRate = totalEvents > 0 ? (double) failedEvents / totalEvents : 0.0;
        return failureRate > 0.05 || criticalEventRate() > 0.10;
    }

    /**
     * Get audit system status.
     * 
     * @return audit system status string
     */
    public String auditStatus() {
        if (!auditEnabled) {
            return "disabled";
        } else if (isStressed()) {
            return "stressed";
        } else if (isHealthy()) {
            return "healthy";
        } else {
            return "degraded";
        }
    }

    /**
     * Calculate audit system reliability score.
     * 
     * @return reliability score between 0.0 and 1.0
     */
    public double reliabilityScore() {
        if (!auditEnabled) {
            return 0.0;
        }

        double successWeight = eventSuccessRate();
        double securityWeight = Math.max(0.0, 1.0 - securityViolationRate() * 2.0); // Penalize violations heavily
        double criticalWeight = Math.max(0.0, 1.0 - criticalEventRate() * 3.0); // Penalize critical events heavily
        double timeWeight = Math.max(0.0, 1.0 - (averageEventProcessingTimeMs() / 1000.0)); // 1 second baseline

        return (successWeight * 0.4) + (securityWeight * 0.3) + (criticalWeight * 0.2) + (timeWeight * 0.1);
    }

    /**
     * Get audit throughput (events per second).
     * 
     * @return throughput in events per second
     */
    public double auditThroughput() {
        // This would need to be calculated based on time window
        // For now, return a placeholder calculation
        return totalEvents > 0 ? (double) totalEvents / (auditRetentionDays * 24 * 60 * 60) : 0.0;
    }
}
