package org.openhab.core.ai.agent.transport;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Top-level gRPC transport health DTO extracted from AgentGrpcTransport.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class GrpcTransportHealth implements TransportHealth {

    private final boolean healthy;
    private final String healthMessage;
    private final long lastHealthCheck;
    private final Map<String, Object> healthMetrics;

    public GrpcTransportHealth(boolean healthy, String healthMessage, long lastHealthCheck,
            Map<String, Object> healthMetrics) {
        this.healthy = healthy;
        this.healthMessage = healthMessage;
        this.lastHealthCheck = lastHealthCheck;
        this.healthMetrics = healthMetrics;
    }

    @Override
    public boolean isHealthy() {
        return healthy;
    }

    @Override
    public String getHealthMessage() {
        return healthMessage;
    }

    @Override
    public long getLastHealthCheck() {
        return lastHealthCheck;
    }

    @Override
    public Map<String, Object> getHealthMetrics() {
        return healthMetrics;
    }
}


