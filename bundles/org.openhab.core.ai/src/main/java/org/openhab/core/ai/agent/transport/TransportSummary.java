package org.openhab.core.ai.agent.transport;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Transport summary for agent transport management
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class TransportSummary {
    private final int totalActive;
    private final int restActive;

    public TransportSummary(int totalActive, int restActive) {
        this.totalActive = totalActive;
        this.restActive = restActive;
    }

    public int getTotalActive() {
        return totalActive;
    }

    public int getRestActive() {
        return restActive;
    }
}
