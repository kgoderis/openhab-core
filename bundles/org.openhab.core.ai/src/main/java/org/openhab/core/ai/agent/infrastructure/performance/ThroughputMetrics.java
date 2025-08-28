package org.openhab.core.ai.agent.infrastructure.performance;

import java.time.Duration;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
import org.openhab.core.ai.common.monitoring.snapshot.ThroughputSnapshot;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Throughput metrics for agent communication performance monitoring.
 * 
 * <p>
 * This service provides throughput tracking and metrics collection using
 * the centralized MetricsService for consistent monitoring and reporting.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@Component(service = ThroughputMetrics.class)
@NonNullByDefault
public class ThroughputMetrics {

    private static final Logger logger = LoggerFactory.getLogger(ThroughputMetrics.class);

    private volatile String agentId = "default";
    private final java.util.List<Long> throughputSamples = new CopyOnWriteArrayList<>();

    @Reference
    private @Nullable MetricsService metricsService;

    public ThroughputMetrics() {
        // Default constructor for OSGi
    }

    public ThroughputMetrics(String agentId) {
        this.agentId = agentId;
    }

    /**
     * Set the agent ID for this metrics instance.
     * 
     * @param agentId the agent identifier
     */
    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public void recordThroughput(long messagesPerSecond) {
        synchronized (throughputSamples) {
            throughputSamples.add(messagesPerSecond);
            if (throughputSamples.size() > 100) {
                throughputSamples.remove(0);
            }
        }

        // Record throughput using MetricsService
        if (metricsService != null) {
            try {
                metricsService.recordOperation("throughput", "message", true, Duration.ofMillis(1));
            } catch (Exception e) {
                logger.warn("Failed to record throughput metrics for agent: {}", agentId, e);
            }
        }
    }

    /**
     * Record a message for throughput calculation.
     * 
     * @param success whether the message was successfully processed
     * @param duration processing duration
     */
    public void recordMessage(boolean success, Duration duration) {
        // Record message using MetricsService
        if (metricsService != null) {
            try {
                metricsService.recordOperation("throughput", "message", success, duration);
            } catch (Exception e) {
                logger.warn("Failed to record message for throughput metrics for agent: {}", agentId, e);
            }
        }
    }

    public long getAverageThroughput() {
        synchronized (throughputSamples) {
            if (throughputSamples.isEmpty()) {
                return 0L;
            }
            return throughputSamples.stream().mapToLong(Long::longValue).sum() / throughputSamples.size();
        }
    }

    public String getAgentId() {
        return agentId;
    }

    /**
     * Get throughput statistics using StatisticsFactory.
     * 
     * <p>
     * This method now uses StatisticsFactory to create statistics from MetricsService
     * snapshots, following the centralized StatisticsFactory approach.
     * </p>
     * 
     * @return throughput statistics from StatisticsFactory, or empty statistics if service unavailable
     */

    /**
     * Create a default throughput snapshot based on current local data.
     * 
     * @return default throughput snapshot
     */
    private ThroughputSnapshot createDefaultSnapshot() {
        org.openhab.core.ai.common.monitoring.api.Counts counts = new org.openhab.core.ai.common.monitoring.api.Counts(
                0L, 0L, 0L);
        org.openhab.core.ai.common.monitoring.api.Timing timing = new org.openhab.core.ai.common.monitoring.api.Timing(
                0L);

        return new ThroughputSnapshot(counts, timing, System.currentTimeMillis(), 0L, 0L, 0L, 0L, 0, 100, 60); // Default
                                                                                                               // values
                                                                                                               // for
                                                                                                               // missing
                                                                                                               // data
    }
}
