package org.openhab.core.ai.tool.security.api;

import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Rate limit information for tracking and enforcing rate limits.
 * 
 * <p>
 * This class provides information about rate limits including current usage,
 * limits, reset times, and whether limits have been exceeded.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class RateLimitInfo {
    private final String identifier;
    private final int limit;
    private final int currentCount;
    private final Instant resetTime;
    private final boolean exceeded;

    /**
     * Constructor for RateLimitInfo.
     * 
     * @param identifier rate limit identifier (e.g., user ID, IP address)
     * @param limit maximum allowed requests
     * @param currentCount current number of requests
     * @param resetTime time when the rate limit resets
     * @param exceeded whether the rate limit has been exceeded
     */
    public RateLimitInfo(String identifier, int limit, int currentCount, Instant resetTime, boolean exceeded) {
        this.identifier = identifier;
        this.limit = limit;
        this.currentCount = currentCount;
        this.resetTime = resetTime;
        this.exceeded = exceeded;
    }

    /**
     * Get rate limit identifier.
     * 
     * @return identifier
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Get maximum allowed requests.
     * 
     * @return limit
     */
    public int getLimit() {
        return limit;
    }

    /**
     * Get current number of requests.
     * 
     * @return current count
     */
    public int getCurrentCount() {
        return currentCount;
    }

    // Backward-compatible aliases
    public int currentRequests() {
        return currentCount;
    }

    public int maxRequests() {
        return limit;
    }

    /**
     * Get time when the rate limit resets.
     * 
     * @return reset time
     */
    public Instant getResetTime() {
        return resetTime;
    }

    /**
     * Check if the rate limit has been exceeded.
     * 
     * @return true if exceeded
     */
    public boolean isExceeded() {
        return exceeded;
    }

    /**
     * Get remaining requests allowed.
     * 
     * @return remaining requests
     */
    public int getRemainingRequests() {
        return Math.max(0, limit - currentCount);
    }
}
