package org.openhab.core.ai.agent.infrastructure.performance;

import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.MetricKeys;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
import org.openhab.core.ai.common.monitoring.service.statistics.BandwidthStatistics;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bandwidth metrics for agent communication performance monitoring.
 * 
 * <p>
 * This class has been migrated to use the centralized MetricsService for all
 * statistics collection, eliminating direct counter usage and following the
 * centralized-only approach mandated by the metrics refactoring plan.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class BandwidthMetrics {
    private static final Logger logger = LoggerFactory.getLogger(BandwidthMetrics.class);

    private final String agentId;

    @Reference
    private @Nullable MetricsService metricsService;

    public BandwidthMetrics(String agentId) {
        this.agentId = agentId;
    }

    public void recordBandwidth(long bytesPerSecond) {
        // Record bandwidth transfer with MetricsService using builder pattern - centralized only approach
        MetricsService metrics = metricsService;
        if (metrics != null) {
            try {
                long startTime = System.nanoTime();
                boolean success = bytesPerSecond > 0; // Successful if any data transferred

                metrics.recordOperation("bandwidth", agentId).withSuccess(success)
                        .withDuration(System.nanoTime() - startTime).withData("bytesPerSecond", bytesPerSecond)
                        .withData("agentId", agentId).withData("transferType", "bandwidth_measurement").record();
            } catch (Exception e) {
                logger.warn("Failed to record bandwidth metrics for agent {}: {}", agentId, e.getMessage());
                // Graceful degradation - continue without metrics if recording fails
            }
        } else {
            logger.debug("MetricsService not available, cannot record bandwidth for agent {}", agentId);
        }
    }

    public long getAverageBandwidth() {
        // Get average bandwidth from StatisticsFactory - centralized only approach
        BandwidthStatistics statistics = getStatistics();
        return (long) statistics.getAverageBandwidth();
    }

    public String getAgentId() {
        return agentId;
    }

    /**
     * Get bandwidth statistics using StatisticsFactory.
     * 
     * <p>
     * This method now uses StatisticsFactory to create statistics from MetricsService
     * snapshots, following the centralized StatisticsFactory approach.
     * </p>
     * 
     * @return bandwidth statistics from StatisticsFactory, or empty statistics if service unavailable
     */
    public BandwidthStatistics getStatistics() {
        MetricsService metrics = metricsService;
        if (metrics != null) {
            try {
                // Use MetricKeys for standardized key generation
                var metricKey = MetricKeys.custom("bandwidth", Map.of("agentId", agentId),
                        Set.of("bandwidth", "counts"));
                
                // Get statistics using StatisticsFactory instead of creating snapshots directly
                return metrics.getStatistics(metricKey, BandwidthStatistics.class, java.time.Duration.ofHours(24));
            } catch (Exception e) {
                logger.warn("Failed to retrieve bandwidth statistics for agent {}: {}", agentId, e.getMessage());
                // Graceful degradation - return empty statistics if MetricsService fails
            }
        } else {
            logger.debug("MetricsService not available, returning empty bandwidth statistics for agent {}", agentId);
        }
        return BandwidthStatistics.empty(java.time.Duration.ofHours(24));
    }

    /**
     * Get total bytes transferred from the centralized MetricsService.
     * 
     * <p>
     * This method replaces the old direct AtomicLong counter access and now
     * sources data from the centralized MetricsService.
     * </p>
     * 
     * @return total bytes transferred from MetricsService
     */
    public long getTotalBytes() {
        BandwidthStatistics statistics = getStatistics();
        return statistics.getTotalBytesTransferred();
    }
}
