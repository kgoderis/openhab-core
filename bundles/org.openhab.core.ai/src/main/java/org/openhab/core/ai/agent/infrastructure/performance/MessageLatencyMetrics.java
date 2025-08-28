package org.openhab.core.ai.agent.infrastructure.performance;

import java.time.Duration;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
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
}
