package org.openhab.core.ai.agent.infrastructure.performance;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Message latency metrics for agent communication performance monitoring
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class MessageLatencyMetrics {
    private final String agentId;
    private final Map<String, List<Duration>> latencies = new ConcurrentHashMap<>();
    private final AtomicLong totalMessages = new AtomicLong(0);

    public MessageLatencyMetrics(String agentId) {
        this.agentId = agentId;
    }

    public void recordLatency(String messageType, Duration latency) {
        latencies.computeIfAbsent(messageType, k -> new java.util.ArrayList<>()).add(latency);
        totalMessages.incrementAndGet();
    }

    public @Nullable Duration getAverageLatency() {
        if (latencies.isEmpty()) {
            return null;
        }

        long totalNanos = latencies.values().stream().flatMap(List::stream).mapToLong(Duration::toNanos).sum();
        long count = latencies.values().stream().mapToLong(List::size).sum();

        return count > 0 ? Duration.ofNanos(totalNanos / count) : null;
    }

    public String getAgentId() {
        return agentId;
    }

    public long getTotalMessages() {
        return totalMessages.get();
    }
}
