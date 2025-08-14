package org.openhab.core.ai.rest;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Immutable snapshot for simple rate limit counters used by {@link RestSecurityFramework}.
 */
@NonNullByDefault
public final class RestRateLimitInfo {
    final int maxRequests;
    final int currentRequests;
    final long resetTime;

    public RestRateLimitInfo(int maxRequests, int currentRequests, long resetTime) {
        this.maxRequests = maxRequests;
        this.currentRequests = currentRequests;
        this.resetTime = resetTime;
    }
}


