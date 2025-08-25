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
package org.openhab.core.ai.common.monitoring.service.statistics;

import java.time.Duration;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
import org.openhab.core.ai.common.monitoring.api.PercentileMetrics;
import org.openhab.core.ai.common.monitoring.api.TrendMetrics;
import org.osgi.service.component.annotations.Reference;

/**
 * Agent-Model Integration Statistics
 * 
 * <p>
 * Provides comprehensive statistics about agent-model integration performance,
 * including model usage patterns, integration efficiency, and cross-agent
 * collaboration metrics.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record AgentModelIntegrationStatistics(List<Object> agentStatistics, long totalIntegrations,
        long successfulIntegrations, long failedIntegrations, long totalIntegrationTimeMs,
        double averageIntegrationTimeMs, long totalModelSwitches, long totalFallbackActivations,
        double averageModelUtilization, double crossAgentCollaborationRate, Object modelUsageDistribution,
        Object integrationErrorDistribution, Object agentCollaborationPatterns, Duration timeRange, long timestampMs)
        implements
            StatisticsSnapshot,
            CountsMetrics,
            LatencyMetrics,
            TrendMetrics,
            PercentileMetrics {

    @Reference
    private static @Nullable MetricsService metricsService;

    // CountsMetrics implementation
    @Override
    public long total() {
        return totalIntegrations;
    }

    @Override
    public long success() {
        return successfulIntegrations;
    }

    @Override
    public long failure() {
        return failedIntegrations;
    }

    @Override
    public double successRate() {
        if (totalIntegrations == 0) {
            return 0.0;
        }
        return (successfulIntegrations * 100.0) / totalIntegrations;
    }

    // LatencyMetrics implementation
    @Override
    public long totalDurationNanos() {
        return totalIntegrationTimeMs * 1_000_000; // Convert to nanoseconds
    }

    public double averageMs() {
        if (totalIntegrations == 0) {
            return 0.0;
        }
        return averageIntegrationTimeMs;
    }

    public double operationsPerSecond() {
        if (timeRange.toNanos() == 0) {
            return 0.0;
        }
        return totalIntegrations / (timeRange.toNanos() / 1_000_000_000.0);
    }

    /**
     * Record integration statistics using MetricsService.
     */
    public static void recordIntegrationStatistics(boolean success, long durationMs) {
        MetricsService metrics = metricsService;
        if (metrics != null) {
            metrics.recordOperation("agent-model", "integration", success, java.time.Duration.ofMillis(durationMs));
        }
    }

    // TrendMetrics implementation
    @Override
    public double trendPercentage() {
        // Simplified trend calculation - in real implementation would analyze time-series data
        if (totalIntegrations == 0) {
            return 0.0;
        }
        double successTrend = successRate() > 80.0 ? 5.0 : -2.0; // Mock trend
        return successTrend;
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
        if (timeRange.toDays() == 0) {
            return 0.0;
        }
        return trendPercentage() / timeRange.toDays();
    }

    // PercentileMetrics implementation
    @Override
    public double percentile50() {
        return averageIntegrationTimeMs;
    }

    @Override
    public double percentile90() {
        return averageIntegrationTimeMs * 1.5; // Mock 90th percentile
    }

    @Override
    public double percentile95() {
        return averageIntegrationTimeMs * 2.0; // Mock 95th percentile
    }

    @Override
    public double percentile99() {
        return averageIntegrationTimeMs * 3.0; // Mock 99th percentile
    }

    /**
     * Create AgentModelIntegrationStatistics from integration data
     */
    public static AgentModelIntegrationStatistics fromIntegrationData(List<Object> agentStatistics,
            long totalIntegrations, long successfulIntegrations, long failedIntegrations, long totalIntegrationTimeMs,
            double averageIntegrationTimeMs, long totalModelSwitches, long totalFallbackActivations,
            double averageModelUtilization, double crossAgentCollaborationRate, Object modelUsageDistribution,
            Object integrationErrorDistribution, Object agentCollaborationPatterns, Duration timeRange) {
        return new AgentModelIntegrationStatistics(agentStatistics, totalIntegrations, successfulIntegrations,
                failedIntegrations, totalIntegrationTimeMs, averageIntegrationTimeMs, totalModelSwitches,
                totalFallbackActivations, averageModelUtilization, crossAgentCollaborationRate, modelUsageDistribution,
                integrationErrorDistribution, agentCollaborationPatterns, timeRange, System.currentTimeMillis());
    }
}
