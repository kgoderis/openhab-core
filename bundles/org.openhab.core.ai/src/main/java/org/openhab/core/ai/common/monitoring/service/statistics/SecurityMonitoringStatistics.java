package org.openhab.core.ai.common.monitoring.service.statistics;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.BusinessMetrics;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.api.PercentileMetrics;
import org.openhab.core.ai.common.monitoring.api.SecurityMetrics;
import org.openhab.core.ai.common.monitoring.api.TrendMetrics;
import org.openhab.core.ai.common.monitoring.service.snapshot.SecurityMonitoringSnapshot;
import org.openhab.core.ai.common.security.SecurityStatistics;

/**
 * Security monitoring statistics for analyzing security metrics over time.
 * 
 * <p>
 * This class provides computed insights from security monitoring snapshots including
 * trend analysis, percentile calculations, and business intelligence metrics.
 * It implements capability interfaces for clean, type-safe statistics access.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record SecurityMonitoringStatistics(List<SecurityMonitoringSnapshot> snapshots, Duration timeRange,
        long timestampMs)
        implements
            StatisticsSnapshot,
            CountsMetrics,
            LatencyMetrics,
            SecurityMetrics,
            TrendMetrics,
            PercentileMetrics,
            BusinessMetrics,
            SecurityStatistics {

    public SecurityMonitoringStatistics {
        if (snapshots == null) {
            throw new IllegalArgumentException("snapshots cannot be null");
        }
        if (timeRange == null) {
            throw new IllegalArgumentException("timeRange cannot be null");
        }
        if (timeRange.isNegative() || timeRange.isZero()) {
            throw new IllegalArgumentException("timeRange must be positive");
        }
    }

    // CountsMetrics implementation
    @Override
    public long total() {
        return snapshots.stream().mapToLong(CountsMetrics::total).sum();
    }

    @Override
    public long success() {
        return snapshots.stream().mapToLong(CountsMetrics::success).sum();
    }

    @Override
    public long failure() {
        return snapshots.stream().mapToLong(CountsMetrics::failure).sum();
    }

    @Override
    public double successRate() {
        if (total() == 0) {
            return 0.0;
        }
        return (success() * 100.0) / total();
    }

    // LatencyMetrics implementation
    @Override
    public long totalDurationNanos() {
        return snapshots.stream().mapToLong(LatencyMetrics::totalDurationNanos).sum();
    }

    // SecurityMetrics implementation
    @Override
    public long securityViolations() {
        return snapshots.stream().mapToLong(SecurityMetrics::securityViolations).sum();
    }

    @Override
    public int activeClients() {
        if (snapshots.isEmpty()) {
            return 0;
        }
        return (int) snapshots.stream().mapToInt(SecurityMetrics::activeClients).average().orElse(0.0);
    }

    @Override
    public int blockedClients() {
        if (snapshots.isEmpty()) {
            return 0;
        }
        return (int) snapshots.stream().mapToInt(SecurityMetrics::blockedClients).average().orElse(0.0);
    }

    @Override
    public boolean authenticationEnabled() {
        if (snapshots.isEmpty()) {
            return false;
        }
        return snapshots.stream().allMatch(SecurityMetrics::authenticationEnabled);
    }

    @Override
    public boolean requestValidationEnabled() {
        if (snapshots.isEmpty()) {
            return false;
        }
        return snapshots.stream().allMatch(SecurityMetrics::requestValidationEnabled);
    }

    @Override
    public int maxConnections() {
        if (snapshots.isEmpty()) {
            return 0;
        }
        return snapshots.stream().mapToInt(SecurityMetrics::maxConnections).max().orElse(0);
    }

    @Override
    public int rateLimitPerMinute() {
        if (snapshots.isEmpty()) {
            return 0;
        }
        return snapshots.stream().mapToInt(SecurityMetrics::rateLimitPerMinute).max().orElse(0);
    }

    @Override
    public int activeSessions() {
        if (snapshots.isEmpty()) {
            return 0;
        }
        return (int) snapshots.stream().mapToInt(SecurityMetrics::activeSessions).average().orElse(0.0);
    }

    @Override
    public double securityViolationRate() {
        if (total() == 0) {
            return 0.0;
        }
        return (securityViolations() * 100.0) / total();
    }

    @Override
    public double clientUtilizationRate() {
        if (maxConnections() == 0) {
            return 0.0;
        }
        return (activeClients() * 100.0) / maxConnections();
    }

    @Override
    public double sessionUtilizationRate() {
        if (maxConnections() == 0) {
            return 0.0;
        }
        return (activeSessions() * 100.0) / maxConnections();
    }

    // TrendMetrics implementation
    @Override
    public double trendPercentage() {
        if (snapshots.size() < 2) {
            return 0.0;
        }

        // Calculate trend based on security violation rate over time
        double firstHalf = calculateAverageSecurityViolationRate(0, snapshots.size() / 2);
        double secondHalf = calculateAverageSecurityViolationRate(snapshots.size() / 2, snapshots.size());

        if (firstHalf == 0) {
            return 0.0;
        }
        return ((secondHalf - firstHalf) / firstHalf) * 100.0;
    }

    @Override
    public String trendDirection() {
        double trend = trendPercentage();
        if (trend > 1.0) {
            return "increasing";
        }
        if (trend < -1.0) {
            return "decreasing";
        }
        return "stable";
    }

    @Override
    public double changeRate() {
        return trendPercentage() / timeRange.toDays();
    }

    // SecurityStatistics implementation
    @Override
    public long getTotalOperations() {
        return total();
    }

    @Override
    public long getSuccessfulOperations() {
        return success();
    }

    @Override
    public long getFailedOperations() {
        return failure();
    }

    @Override
    public long getSecurityViolations() {
        return securityViolations();
    }

    @Override
    public java.time.Instant getLastOperationTime() {
        if (snapshots.isEmpty()) {
            return null;
        }
        return java.time.Instant.ofEpochMilli(snapshots.get(snapshots.size() - 1).getTimestampMs());
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

    // BusinessMetrics implementation
    @Override
    public double costEfficiency() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }

        // Calculate cost efficiency based on security violation rate
        // Lower violation rate = higher efficiency
        double avgViolationRate = snapshots.stream().mapToDouble(SecurityMetrics::securityViolationRate).average()
                .orElse(0.0);

        return Math.max(0.0, 100.0 - avgViolationRate);
    }

    @Override
    public double resourceUtilization() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }

        // Calculate resource utilization based on client and session utilization
        double avgClientUtilization = snapshots.stream().mapToDouble(SecurityMetrics::clientUtilizationRate).average()
                .orElse(0.0);

        double avgSessionUtilization = snapshots.stream().mapToDouble(SecurityMetrics::sessionUtilizationRate).average()
                .orElse(0.0);

        return (avgClientUtilization + avgSessionUtilization) / 2.0;
    }

    @Override
    public double throughputEfficiency() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }

        long totalOperations = snapshots.stream().mapToLong(CountsMetrics::total).sum();

        return (double) totalOperations / timeRange.toHours();
    }

    // Helper methods
    private double calculateAverageSecurityViolationRate(int start, int end) {
        return snapshots.subList(start, end).stream().mapToDouble(SecurityMetrics::securityViolationRate).average()
                .orElse(0.0);
    }

    private double calculatePercentile(double percentile) {
        List<Double> latencies = snapshots.stream()
                .flatMap(s -> Stream.generate(() -> s.averageMs(s.total())).limit(s.total())).sorted()
                .collect(Collectors.toList());

        if (latencies.isEmpty()) {
            return 0.0;
        }

        int index = (int) Math.ceil(percentile * latencies.size()) - 1;
        return latencies.get(Math.max(0, index));
    }
}
