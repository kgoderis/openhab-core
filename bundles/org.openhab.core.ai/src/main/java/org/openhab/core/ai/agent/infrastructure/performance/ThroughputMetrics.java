package org.openhab.core.ai.agent.infrastructure.performance;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Throughput metrics for agent communication performance monitoring
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ThroughputMetrics {
    private final String agentId;
    private final List<Long> throughputSamples = new ArrayList<>();
    private final AtomicLong totalMessages = new AtomicLong(0);

    public ThroughputMetrics(String agentId) {
        this.agentId = agentId;
    }

    public void recordThroughput(long messagesPerSecond) {
        synchronized (throughputSamples) {
            throughputSamples.add(messagesPerSecond);
            if (throughputSamples.size() > 100) {
                throughputSamples.remove(0);
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

    public long getTotalMessages() {
        return totalMessages.get();
    }
}
