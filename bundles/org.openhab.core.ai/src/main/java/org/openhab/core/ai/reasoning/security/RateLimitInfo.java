package org.openhab.core.ai.reasoning.security;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Rate limiting information for an agent.
 *
 * Tracks per-minute and per-second request counts for a given agent.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class RateLimitInfo {
    final String agentId;
    final int maxRequestsPerMinute;
    final int maxRequestsPerSecond;
    int requestsThisMinute;
    int requestsThisSecond;
    long lastMinuteReset;
    long lastSecondReset;

    public RateLimitInfo(String agentId, int maxRequestsPerMinute, int maxRequestsPerSecond) {
        this.agentId = agentId;
        this.maxRequestsPerMinute = maxRequestsPerMinute;
        this.maxRequestsPerSecond = maxRequestsPerSecond;
        this.requestsThisMinute = 0;
        this.requestsThisSecond = 0;
        this.lastMinuteReset = System.currentTimeMillis();
        this.lastSecondReset = System.currentTimeMillis();
    }

    void incrementRequests() {
        requestsThisMinute++;
        requestsThisSecond++;
    }

    void resetMinuteWindow() {
        requestsThisMinute = 0;
        lastMinuteReset = System.currentTimeMillis();
    }

    void resetSecondWindow() {
        requestsThisSecond = 0;
        lastSecondReset = System.currentTimeMillis();
    }
}
