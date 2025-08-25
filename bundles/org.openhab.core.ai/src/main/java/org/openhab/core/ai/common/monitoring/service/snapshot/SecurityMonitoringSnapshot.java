package org.openhab.core.ai.common.monitoring.service.snapshot;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.api.MetricsSnapshot;
import org.openhab.core.ai.common.monitoring.api.SecurityMetrics;

/**
 * Security monitoring snapshot for tracking security-related metrics.
 * 
 * <p>
 * This snapshot provides a comprehensive view of security metrics including
 * authentication attempts, permission checks, security violations, and session management.
 * It implements capability interfaces for clean, type-safe metrics access.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record SecurityMonitoringSnapshot(Counts counts, Timing timing, long timestampMs, long securityViolations,
        int activeClients, int blockedClients, boolean authenticationEnabled, boolean requestValidationEnabled,
        int maxConnections, int rateLimitPerMinute,
        int activeSessions) implements MetricsSnapshot, CountsMetrics, LatencyMetrics, SecurityMetrics {

    /**
     * Counts record for basic counting metrics.
     */
    public record Counts(long total, long success, long failure) {
        public Counts {
            if (total < 0) {
                throw new IllegalArgumentException("total must be non-negative");
            }
            if (success < 0) {
                throw new IllegalArgumentException("success must be non-negative");
            }
            if (failure < 0) {
                throw new IllegalArgumentException("failure must be non-negative");
            }
            if (success + failure > total) {
                throw new IllegalArgumentException("success + failure cannot exceed total");
            }
        }
    }

    /**
     * Timing record for latency metrics.
     */
    public record Timing(long totalDurationNanos) {
        public Timing {
            if (totalDurationNanos < 0) {
                throw new IllegalArgumentException("totalDurationNanos must be non-negative");
            }
        }

        public double averageMs(long total) {
            if (total == 0) {
                return 0.0;
            }
            return totalDurationNanos / (total * 1_000_000.0);
        }
    }

    public SecurityMonitoringSnapshot {
        if (securityViolations < 0) {
            throw new IllegalArgumentException("securityViolations must be non-negative");
        }
        if (activeClients < 0) {
            throw new IllegalArgumentException("activeClients must be non-negative");
        }
        if (blockedClients < 0) {
            throw new IllegalArgumentException("blockedClients must be non-negative");
        }
        if (maxConnections < 0) {
            throw new IllegalArgumentException("maxConnections must be non-negative");
        }
        if (rateLimitPerMinute < 0) {
            throw new IllegalArgumentException("rateLimitPerMinute must be non-negative");
        }
        if (activeSessions < 0) {
            throw new IllegalArgumentException("activeSessions must be non-negative");
        }
    }

    // CountsMetrics implementation
    @Override
    public long total() {
        return counts.total();
    }

    @Override
    public long success() {
        return counts.success();
    }

    @Override
    public long failure() {
        return counts.failure();
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
        return timing.totalDurationNanos();
    }

    // Note: averageMs(long total) is a default method in LatencyMetrics interface
    // operationsPerSecond() is not part of LatencyMetrics interface

    // SecurityMetrics implementation
    @Override
    public long securityViolations() {
        return securityViolations;
    }

    @Override
    public int activeClients() {
        return activeClients;
    }

    @Override
    public int blockedClients() {
        return blockedClients;
    }

    @Override
    public boolean authenticationEnabled() {
        return authenticationEnabled;
    }

    @Override
    public boolean requestValidationEnabled() {
        return requestValidationEnabled;
    }

    @Override
    public int maxConnections() {
        return maxConnections;
    }

    @Override
    public int rateLimitPerMinute() {
        return rateLimitPerMinute;
    }

    @Override
    public int activeSessions() {
        return activeSessions;
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

    /**
     * Create an empty security monitoring snapshot.
     * 
     * @param securityId the security identifier
     * @return empty security monitoring snapshot
     */
    public static SecurityMonitoringSnapshot empty(String securityId) {
        return new SecurityMonitoringSnapshot(new Counts(0, 0, 0), new Timing(0), System.currentTimeMillis(), 0, 0, 0,
                true, true, 100, 60, 0);
    }
}
