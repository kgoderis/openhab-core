package org.openhab.core.ai.agent.transport;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
import org.osgi.service.component.annotations.Reference;

/**
 * Server statistics for Agent servlet.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public class AgentServerStatistics {

    @Reference
    private @Nullable MetricsService metricsService;
    private final boolean requestHandlerActive;
    private final int endpointCount;

    public AgentServerStatistics(boolean requestHandlerActive, int endpointCount) {
        this.requestHandlerActive = requestHandlerActive;
        this.endpointCount = endpointCount;
    }

    public boolean isRequestHandlerActive() {
        return requestHandlerActive;
    }

    public int getEndpointCount() {
        return endpointCount;
    }

    /**
     * Record server statistics using MetricsService.
     */
    public void recordServerStatistics() {
        MetricsService metrics = metricsService;
        if (metrics != null) {
            long startTime = System.currentTimeMillis();
            boolean success = requestHandlerActive && endpointCount > 0;
            long duration = System.currentTimeMillis() - startTime;

            metrics.recordOperation("agent-server", "statistics", success, java.time.Duration.ofMillis(duration));
        }
    }

    @Override
    public String toString() {
        return String.format("ServerStatistics{requestHandlerActive=%s, endpointCount=%d}", requestHandlerActive,
                endpointCount);
    }
}
