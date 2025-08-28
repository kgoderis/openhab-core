package org.openhab.core.ai.agent.infrastructure.performance;

import java.time.Duration;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.MetricsService;

import org.openhab.core.ai.common.monitoring.service.statistics.MessageLatencyStatistics;
import org.openhab.core.ai.common.monitoring.api.MetricKeys;
import java.util.Map;
import java.util.Set;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Message latency metrics for agent communication performance monitoring
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class MessageLatencyMetrics {
    private static final Logger logger = LoggerFactory.getLogger(MessageLatencyMetrics.class);

    private final String agentId;

    private @Reference @Nullable MetricsService metricsService;

    public MessageLatencyMetrics(String agentId) {
        this.agentId = agentId;
    }

    public void recordLatency(String messageType, Duration latency) {
        // Record message latency with MetricsService using builder pattern - centralized only approach
        MetricsService metrics = metricsService;
        if (metrics != null) {
            try {
                boolean success = latency.toMillis() < 5000; // Successful if latency < 5 seconds
                long durationNanos = latency.toNanos();

                metrics.recordOperation("message-latency", agentId).withSuccess(success).withDuration(durationNanos)
                        .withData("messageType", messageType).withData("latencyMs", latency.toMillis())
                        .withData("messageQueueDepth", 0L) // No queue in this implementation
                        .record();
            } catch (Exception e) {
                logger.warn("Failed to record message latency metrics for agent {}: {}", agentId, e.getMessage());
                // Graceful degradation - continue without metrics if recording fails
            }
        } else {
            logger.debug("MetricsService not available, cannot record latency for agent {}", agentId);
        }
    }

    public @Nullable Duration getAverageLatency() {
        // Get average latency from StatisticsFactory - centralized only approach
        MessageLatencyStatistics statistics = getStatistics();
        double avgLatencyMs = statistics.communicationLatency();
        return avgLatencyMs > 0 ? Duration.ofMillis((long) avgLatencyMs) : null;
    }

    public String getAgentId() {
        return agentId;
    }

    /**
     * Get message latency statistics using StatisticsFactory.
     * 
     * <p>
     * This method now uses StatisticsFactory to create statistics from MetricsService
     * snapshots, following the centralized StatisticsFactory approach.
     * </p>
     * 
     * @return message latency statistics from StatisticsFactory, or empty statistics if service unavailable
     */
    public MessageLatencyStatistics getStatistics() {
        MetricsService metrics = metricsService;
        if (metrics != null) {
            try {
                // Use MetricKeys for standardized key generation
                var metricKey = MetricKeys.custom("message-latency", Map.of("agentId", agentId),
                        Set.of("latency", "counts"));
                
                // Get statistics using StatisticsFactory instead of creating snapshots directly
                return metrics.getStatistics(metricKey, MessageLatencyStatistics.class, java.time.Duration.ofHours(24));
            } catch (Exception e) {
                logger.warn("Failed to retrieve message latency statistics for agent {}: {}", agentId, e.getMessage());
                // Graceful degradation - return empty statistics if MetricsService fails
            }
        } else {
            logger.debug("MetricsService not available, returning empty message latency statistics for agent {}", agentId);
        }
        return MessageLatencyStatistics.empty(agentId);
    }

    /**
     * Get total message count from the statistics.
     * 
     * @return total message count
     */
    public long getTotalMessages() {
        return getStatistics().total();
    }

    /**
     * Get message throughput from the statistics.
     * 
     * @return message throughput per second from StatisticsFactory
     */
    public double getThroughput() {
        MessageLatencyStatistics statistics = getStatistics();
        return statistics.messageThroughput();
    }
}
