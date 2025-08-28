package org.openhab.core.ai.common.monitoring.service.snapshot;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.CommunicationMetrics;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.service.statistics.StatisticsSnapshot;

/**
 * Snapshot for message latency metrics.
 * 
 * <p>
 * This snapshot captures message latency performance metrics including message processing times,
 * communication latencies, and message throughput statistics. It implements multiple
 * capability interfaces to provide comprehensive message latency analysis.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record MessageLatencySnapshot(long total, long success, long failure, long totalDurationNanos,
        double averageLatencyMs, double messageThroughput, long messageQueueDepth, String agentId,
        long timestampMs) implements StatisticsSnapshot, CountsMetrics, LatencyMetrics, CommunicationMetrics {

    /**
     * Create an empty message latency snapshot.
     * 
     * @param agentId the agent identifier
     * @return empty snapshot
     */
    public static MessageLatencySnapshot empty(String agentId) {
        return new MessageLatencySnapshot(0L, 0L, 0L, 0L, 0.0, 0.0, 0L, agentId, System.currentTimeMillis());
    }

    /**
     * Create a message latency snapshot from a GenericMetricsSnapshot.
     * 
     * @param genericSnapshot the generic snapshot to convert
     * @param agentId the agent identifier
     * @return message latency snapshot
     */
    public static MessageLatencySnapshot from(GenericMetricsSnapshot genericSnapshot, String agentId) {
        if (genericSnapshot == null) {
            return empty(agentId);
        }

        double avgLatencyMs = genericSnapshot.getDouble("averageLatencyMs", 0.0);
        double throughput = genericSnapshot.getDouble("messageThroughput", 0.0);
        long queueDepth = genericSnapshot.getLong("messageQueueDepth", 0L);

        return new MessageLatencySnapshot(genericSnapshot.getTotal(), genericSnapshot.getSuccess(),
                genericSnapshot.getFailure(), genericSnapshot.getTotalDurationNanos(), avgLatencyMs, throughput,
                queueDepth, agentId, genericSnapshot.getTimestampMs());
    }

    /**
     * Create a message latency snapshot with specific values.
     * 
     * @param total total message count
     * @param success successful message count
     * @param failure failed message count
     * @param totalDurationNanos total processing duration in nanoseconds
     * @param averageLatencyMs average latency in milliseconds
     * @param messageThroughput message throughput
     * @param messageQueueDepth message queue depth
     * @param agentId agent identifier
     * @return message latency snapshot
     */
    public static MessageLatencySnapshot of(long total, long success, long failure, long totalDurationNanos,
            double averageLatencyMs, double messageThroughput, long messageQueueDepth, String agentId) {
        return new MessageLatencySnapshot(total, success, failure, totalDurationNanos, averageLatencyMs,
                messageThroughput, messageQueueDepth, agentId, System.currentTimeMillis());
    }

    // CountsMetrics implementation
    @Override
    public long total() {
        return total;
    }

    @Override
    public long success() {
        return success;
    }

    @Override
    public long failure() {
        return failure;
    }

    // LatencyMetrics implementation
    @Override
    public long totalDurationNanos() {
        return totalDurationNanos;
    }

    // CommunicationMetrics implementation
    @Override
    public double communicationSuccessRate() {
        return total > 0 ? (double) success / total * 100.0 : 0.0;
    }

    @Override
    public double messageThroughput() {
        return messageThroughput;
    }

    @Override
    public double communicationLatency() {
        return averageLatencyMs;
    }

    @Override
    public double communicationReliability() {
        // Calculate reliability as inverse of failure rate with some smoothing
        double failureRate = total > 0 ? (double) failure / total : 0.0;
        return Math.max(0.0, 100.0 - (failureRate * 100.0));
    }

    @Override
    public double messageDeliveryRate() {
        return communicationSuccessRate(); // Same as success rate for message delivery
    }

    @Override
    public double communicationErrorRate() {
        return total > 0 ? (double) failure / total * 100.0 : 0.0;
    }

    @Override
    public long messageQueueDepth() {
        return messageQueueDepth;
    }

    @Override
    public double bandwidthUtilization() {
        // Estimate bandwidth utilization based on throughput
        // This is a simplified calculation - could be enhanced with actual bandwidth data
        return Math.min(100.0, messageThroughput * 0.1); // Simple heuristic
    }

    @Override
    public double communicationRetryRate() {
        // Estimate retry rate based on failure patterns
        return communicationErrorRate() * 0.8; // Assume 80% of errors result in retries
    }

    @Override
    public double communicationTimeoutRate() {
        // Estimate timeout rate as a subset of communication errors
        return communicationErrorRate() * 0.3; // Assume 30% of errors are timeouts
    }

    /**
     * Get the agent identifier.
     * 
     * @return agent ID
     */
    public String getAgentId() {
        return agentId;
    }

    /**
     * Get the average latency in milliseconds.
     * 
     * @return average latency
     */
    public double getAverageLatencyMs() {
        return averageLatencyMs;
    }
}
