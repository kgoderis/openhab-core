package org.openhab.core.ai.agent.transport;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Server statistics for Agent servlet.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public class AgentServerStatistics {
    private final boolean requestHandlerActive;
    private final int endpointCount;

    public AgentServerStatistics(boolean requestHandlerActive, int endpointCount) {
        this.requestHandlerActive = requestHandlerActive;
        this.endpointCount = endpointCount;
    }

    public boolean isRequestHandlerActive() { return requestHandlerActive; }
    public int getEndpointCount() { return endpointCount; }

    @Override
    public String toString() {
        return String.format("ServerStatistics{requestHandlerActive=%s, endpointCount=%d}", requestHandlerActive,
                endpointCount);
    }
}


