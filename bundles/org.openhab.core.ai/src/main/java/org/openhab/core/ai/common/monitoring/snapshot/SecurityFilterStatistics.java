/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.core.ai.common.monitoring.snapshot;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.PercentileMetrics;
import org.openhab.core.ai.common.monitoring.api.SecurityMetrics;
import org.openhab.core.ai.common.monitoring.api.TrendMetrics;

/**
 * Immutable statistics for security filter metrics analysis.
 * 
 * This class provides trend analysis, percentile calculations, and security
 * metrics for security filter operations over a specified time range.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public record SecurityFilterStatistics(List<SecurityFilterSnapshot> snapshots, Duration timeRange,
        long timestampMs) implements TrendMetrics, PercentileMetrics, SecurityMetrics {

    public SecurityFilterStatistics {
        Objects.requireNonNull(snapshots, "snapshots");
        Objects.requireNonNull(timeRange, "timeRange");
    }

    // Helper method for percentile calculations
    private double percentile(double p) {
        if (snapshots.isEmpty()) {
            return 0.0;
        }

        // Calculate percentile of security violation rates
        double[] values = snapshots.stream().mapToDouble(SecurityFilterSnapshot::securityViolationRate).sorted()
                .toArray();

        return calculatePercentile(values, p);
    }

    @Override
    public double percentile50() {
        return percentile(50.0);
    }

    @Override
    public double percentile90() {
        return percentile(90.0);
    }

    @Override
    public double percentile95() {
        return percentile(95.0);
    }

    @Override
    public double percentile99() {
        return percentile(99.0);
    }

    @Override
    public double trendPercentage() {
        if (snapshots.size() < 2) {
            return 0.0;
        }

        // Calculate percentage change from first to last snapshot
        double firstValue = snapshots.get(0).total();
        double lastValue = snapshots.get(snapshots.size() - 1).total();

        if (firstValue == 0) {
            return lastValue > 0 ? 100.0 : 0.0;
        }

        return ((lastValue - firstValue) / firstValue) * 100.0;
    }

    @Override
    public String trendDirection() {
        double percentage = trendPercentage();
        if (percentage > 5.0) {
            return "increasing";
        } else if (percentage < -5.0) {
            return "decreasing";
        } else {
            return "stable";
        }
    }

    @Override
    public double changeRate() {
        if (snapshots.size() < 2 || timeRange.isZero()) {
            return 0.0;
        }

        double firstValue = snapshots.get(0).total();
        double lastValue = snapshots.get(snapshots.size() - 1).total();
        double timeRangeSeconds = timeRange.toSeconds();

        return timeRangeSeconds > 0 ? (lastValue - firstValue) / timeRangeSeconds : 0.0;
    }

    // SecurityMetrics implementation - provide aggregate values across snapshots
    @Override
    public long securityViolations() {
        return snapshots.stream().mapToLong(SecurityFilterSnapshot::securityViolations).sum();
    }

    @Override
    public int activeClients() {
        if (snapshots.isEmpty()) {
            return 0;
        }
        // Return the most recent value
        return snapshots.get(snapshots.size() - 1).activeClients();
    }

    @Override
    public int blockedClients() {
        if (snapshots.isEmpty()) {
            return 0;
        }
        // Return the most recent value
        return snapshots.get(snapshots.size() - 1).blockedClients();
    }

    @Override
    public boolean authenticationEnabled() {
        if (snapshots.isEmpty()) {
            return false;
        }
        // Return the most recent value
        return snapshots.get(snapshots.size() - 1).authenticationEnabled();
    }

    @Override
    public boolean requestValidationEnabled() {
        if (snapshots.isEmpty()) {
            return false;
        }
        // Return the most recent value
        return snapshots.get(snapshots.size() - 1).requestValidationEnabled();
    }

    @Override
    public int maxConnections() {
        if (snapshots.isEmpty()) {
            return 0;
        }
        // Return the most recent value
        return snapshots.get(snapshots.size() - 1).maxConnections();
    }

    @Override
    public int rateLimitPerMinute() {
        if (snapshots.isEmpty()) {
            return 0;
        }
        // Return the most recent value
        return snapshots.get(snapshots.size() - 1).rateLimitPerMinute();
    }

    @Override
    public int activeSessions() {
        if (snapshots.isEmpty()) {
            return 0;
        }
        // Return the most recent value
        return snapshots.get(snapshots.size() - 1).activeSessions();
    }

    @Override
    public long total() {
        return snapshots.stream().mapToLong(SecurityFilterSnapshot::total).sum();
    }

    // Helper methods for statistical calculations
    private double calculatePercentile(double[] values, double p) {
        if (values.length == 0)
            return 0.0;

        int n = values.length;
        double index = (p / 100.0) * (n - 1);
        int lowerIndex = (int) Math.floor(index);
        int upperIndex = Math.min(lowerIndex + 1, n - 1);

        if (lowerIndex == upperIndex) {
            return values[lowerIndex];
        }

        double weight = index - lowerIndex;
        return values[lowerIndex] * (1 - weight) + values[upperIndex] * weight;
    }
}
