package org.openhab.core.ai.agent.transport.api;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public interface TransportHealth {
    boolean isHealthy();

    String getHealthMessage();

    long getLastHealthCheck();

    Map<String, Object> getHealthMetrics();
}
