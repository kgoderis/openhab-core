package org.openhab.core.ai.common.monitoring.service.statistics;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.api.PercentileMetrics;
import org.openhab.core.ai.common.monitoring.api.TrendMetrics;
import org.openhab.core.ai.common.monitoring.service.snapshot.AuditSnapshot;

/**
 * Statistics class for audit logging metrics.
 * 
 * <p>
 * This class provides comprehensive audit statistics including
 * security event analysis, authentication monitoring, authorization
 * tracking, and audit system performance. It aggregates multiple
 * AuditSnapshot instances to provide historical and statistical
 * analysis of audit patterns.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record AuditStatistics(List<AuditSnapshot> snapshots, Duration timeRange, long timestampMs)
        implements
            StatisticsSnapshot,
            CountsMetrics,
            LatencyMetrics,
            TrendMetrics,
            PercentileMetrics {

    // CountsMetrics implementation (aggregated across all snapshots)
    @Override
    public long total() {
        return snapshots.stream().mapToLong(AuditSnapshot::total).sum();
    }

    @Override
    public long success() {
        return snapshots.stream().mapToLong(AuditSnapshot::success).sum();
    }

    @Override
    public long failure() {
        return snapshots.stream().mapToLong(AuditSnapshot::failure).sum();
    }

    // LatencyMetrics implementation
    @Override
    public long totalDurationNanos() {
        return snapshots.stream().mapToLong(AuditSnapshot::totalDurationNanos).sum();
    }

    // TrendMetrics implementation
    @Override
    public double trendPercentage() {
        if (snapshots.size() < 2) {
            return 0.0;
        }
        // Calculate reliability trend from first to last snapshot
        double firstReliability = snapshots.get(0).reliabilityScore();
        double lastReliability = snapshots.get(snapshots.size() - 1).reliabilityScore();
        return firstReliability > 0 ? ((lastReliability - firstReliability) / firstReliability) * 100.0 : 0.0;
    }

    @Override
    public String trendDirection() {
        double percentage = trendPercentage();
        if (percentage > 5.0) {
            return "improving";
        } else if (percentage < -5.0) {
            return "declining";
        } else {
            return "stable";
        }
    }

    @Override
    public double changeRate() {
        if (snapshots.size() < 2) {
            return 0.0;
        }
        // Calculate change rate in events per second
        long totalEvents = total();
        long timeSpanMs = snapshots.get(snapshots.size() - 1).timestampMs() - snapshots.get(0).timestampMs();
        return timeSpanMs > 0 ? (double) totalEvents / (timeSpanMs / 1000.0) : 0.0;
    }

    // PercentileMetrics implementation
    @Override
    public double percentile50() {
        return calculatePercentile(0.5);
    }

    @Override
    public double percentile90() {
        return calculatePercentile(0.9);
    }

    @Override
    public double percentile95() {
        return calculatePercentile(0.95);
    }

    @Override
    public double percentile99() {
        return calculatePercentile(0.99);
    }

    private double calculatePercentile(double percentile) {
        if (snapshots.isEmpty()) {
            return 0.0;
        }

        List<Double> processingTimes = snapshots.stream().mapToDouble(s -> s.averageEventProcessingTimeMs()).sorted()
                .boxed().toList();

        if (processingTimes.isEmpty()) {
            return 0.0;
        }

        int index = (int) Math.ceil(percentile * processingTimes.size()) - 1;
        index = Math.max(0, Math.min(index, processingTimes.size() - 1));
        return processingTimes.get(index);
    }

    /**
     * Get total security violations across all snapshots.
     * 
     * @return total security violations
     */
    public long totalSecurityViolations() {
        return snapshots.stream().mapToLong(AuditSnapshot::securityViolations).sum();
    }

    /**
     * Get total authentication attempts across all snapshots.
     * 
     * @return total authentication attempts
     */
    public long totalAuthenticationAttempts() {
        return snapshots.stream().mapToLong(AuditSnapshot::authenticationAttempts).sum();
    }

    /**
     * Get total authorization decisions across all snapshots.
     * 
     * @return total authorization decisions
     */
    public long totalAuthorizationDecisions() {
        return snapshots.stream().mapToLong(AuditSnapshot::authorizationDecisions).sum();
    }

    /**
     * Get total tool operations across all snapshots.
     * 
     * @return total tool operations
     */
    public long totalToolOperations() {
        return snapshots.stream().mapToLong(AuditSnapshot::toolOperations).sum();
    }

    /**
     * Get total system events across all snapshots.
     * 
     * @return total system events
     */
    public long totalSystemEvents() {
        return snapshots.stream().mapToLong(AuditSnapshot::systemEvents).sum();
    }

    /**
     * Get total critical events across all snapshots.
     * 
     * @return total critical events
     */
    public long totalCriticalEvents() {
        return snapshots.stream().mapToLong(AuditSnapshot::criticalEvents).sum();
    }

    /**
     * Get total warning events across all snapshots.
     * 
     * @return total warning events
     */
    public long totalWarningEvents() {
        return snapshots.stream().mapToLong(AuditSnapshot::warningEvents).sum();
    }

    /**
     * Get total info events across all snapshots.
     * 
     * @return total info events
     */
    public long totalInfoEvents() {
        return snapshots.stream().mapToLong(AuditSnapshot::infoEvents).sum();
    }

    /**
     * Get total debug events across all snapshots.
     * 
     * @return total debug events
     */
    public long totalDebugEvents() {
        return snapshots.stream().mapToLong(AuditSnapshot::debugEvents).sum();
    }

    /**
     * Get average event processing success rate across all snapshots.
     * 
     * @return average success rate
     */
    public double averageEventSuccessRate() {
        return snapshots.stream().mapToDouble(AuditSnapshot::eventSuccessRate).average().orElse(0.0);
    }

    /**
     * Get average security violation rate across all snapshots.
     * 
     * @return average security violation rate
     */
    public double averageSecurityViolationRate() {
        return snapshots.stream().mapToDouble(AuditSnapshot::securityViolationRate).average().orElse(0.0);
    }

    /**
     * Get average authentication success rate across all snapshots.
     * 
     * @return average authentication success rate
     */
    public double averageAuthenticationSuccessRate() {
        return snapshots.stream().mapToDouble(AuditSnapshot::authenticationSuccessRate).average().orElse(0.0);
    }

    /**
     * Get average event processing time across all snapshots.
     * 
     * @return average processing time in milliseconds
     */
    public double averageEventProcessingTime() {
        return snapshots.stream().mapToDouble(AuditSnapshot::averageEventProcessingTimeMs).average().orElse(0.0);
    }

    /**
     * Get average critical event rate across all snapshots.
     * 
     * @return average critical event rate
     */
    public double averageCriticalEventRate() {
        return snapshots.stream().mapToDouble(AuditSnapshot::criticalEventRate).average().orElse(0.0);
    }

    /**
     * Get average warning event rate across all snapshots.
     * 
     * @return average warning event rate
     */
    public double averageWarningEventRate() {
        return snapshots.stream().mapToDouble(AuditSnapshot::warningEventRate).average().orElse(0.0);
    }

    /**
     * Get average reliability score across all snapshots.
     * 
     * @return average reliability score
     */
    public double averageReliabilityScore() {
        return snapshots.stream().mapToDouble(AuditSnapshot::reliabilityScore).average().orElse(0.0);
    }

    /**
     * Get audit category distribution.
     * 
     * @return map of audit categories to their event counts
     */
    public Map<String, Long> auditCategoryDistribution() {
        return snapshots.stream().collect(
                Collectors.groupingBy(AuditSnapshot::auditCategory, Collectors.summingLong(AuditSnapshot::total)));
    }

    /**
     * Get event type distribution.
     * 
     * @return map of event types to their counts
     */
    public Map<String, Long> eventTypeDistribution() {
        return snapshots.stream()
                .collect(Collectors.groupingBy(AuditSnapshot::eventType, Collectors.summingLong(AuditSnapshot::total)));
    }

    /**
     * Get audit status distribution.
     * 
     * @return map of audit statuses to their counts
     */
    public Map<String, Long> auditStatusDistribution() {
        return snapshots.stream().collect(Collectors.groupingBy(AuditSnapshot::auditStatus, Collectors.counting()));
    }

    /**
     * Get count of healthy audit systems.
     * 
     * @return number of healthy audit systems
     */
    public long healthyAuditSystemCount() {
        return snapshots.stream().mapToLong(s -> s.isHealthy() ? 1 : 0).sum();
    }

    /**
     * Get count of stressed audit systems.
     * 
     * @return number of stressed audit systems
     */
    public long stressedAuditSystemCount() {
        return snapshots.stream().mapToLong(s -> s.isStressed() ? 1 : 0).sum();
    }

    /**
     * Get count of enabled audit systems.
     * 
     * @return number of enabled audit systems
     */
    public long enabledAuditSystemCount() {
        return snapshots.stream().mapToLong(s -> s.auditEnabled() ? 1 : 0).sum();
    }

    /**
     * Get unique audit category count.
     * 
     * @return number of unique audit categories
     */
    public long uniqueAuditCategoryCount() {
        return snapshots.stream().map(AuditSnapshot::auditCategory).distinct().count();
    }

    /**
     * Check if overall audit system is healthy.
     * 
     * @return true if average reliability > 0.8 and most systems are healthy
     */
    public boolean isOverallHealthy() {
        double healthyRatio = snapshots.size() > 0 ? (double) healthyAuditSystemCount() / snapshots.size() : 0.0;
        return averageReliabilityScore() > 0.8 && healthyRatio > 0.8;
    }

    /**
     * Get most active audit category.
     * 
     * @return audit category with highest event count
     */
    public String mostActiveAuditCategory() {
        return auditCategoryDistribution().entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey)
                .orElse("unknown");
    }

    /**
     * Get most common event type.
     * 
     * @return event type with highest count
     */
    public String mostCommonEventType() {
        return eventTypeDistribution().entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey)
                .orElse("unknown");
    }

    /**
     * Get security risk level.
     * 
     * @return security risk level string
     */
    public String securityRiskLevel() {
        double violationRate = averageSecurityViolationRate();
        if (violationRate > 0.1) {
            return "high";
        } else if (violationRate > 0.05) {
            return "medium";
        } else if (violationRate > 0.01) {
            return "low";
        } else {
            return "minimal";
        }
    }

    /**
     * Get audit system utilization rate.
     * 
     * @return utilization rate as percentage of systems that are enabled
     */
    public double auditSystemUtilizationRate() {
        return snapshots.size() > 0 ? (double) enabledAuditSystemCount() / snapshots.size() * 100.0 : 0.0;
    }

    /**
     * Get average audit throughput.
     * 
     * @return average throughput in events per second
     */
    public double averageAuditThroughput() {
        return snapshots.stream().mapToDouble(AuditSnapshot::auditThroughput).average().orElse(0.0);
    }
}
