package org.openhab.core.ai.rest;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Immutable snapshot for simple rate limit counters used by {@link RestSecurityFramework}.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
final class RestRateLimitInfo {
    final int maxRequests;
    final int currentRequests;
    final long resetTime;

    RestRateLimitInfo(int maxRequests, int currentRequests, long resetTime) {
        this.maxRequests = maxRequests;
        this.currentRequests = currentRequests;
        this.resetTime = resetTime;
    }
}


