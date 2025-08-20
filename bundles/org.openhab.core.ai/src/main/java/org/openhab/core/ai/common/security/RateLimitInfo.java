package org.openhab.core.ai.common.security;

import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Unified rate limit information for tracking and enforcing rate limits across the AI system.
 * 
 * <p>
 * This class provides comprehensive information about rate limits including current usage,
 * limits, reset times, and whether limits have been exceeded. It supports both agent-specific
 * and general rate limiting scenarios.
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
    private final RateLimitType type;

    /**
     * Rate limit types to distinguish between different rate limiting scenarios.
     */
    public enum RateLimitType {
        AGENT("agent", "Agent-specific rate limiting"),
        TOOL("tool", "Tool-specific rate limiting"),
        USER("user", "User-specific rate limiting"),
        IP("ip", "IP-based rate limiting"),
        GENERAL("general", "General rate limiting");

        private final String code;
        private final String description;

        RateLimitType(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }

        @Override
        public String toString() {
            return code + " (" + description + ")";
        }
    }

    /**
     * Constructor for RateLimitInfo.
     * 
     * @param identifier rate limit identifier (e.g., agent ID, user ID, IP address)
     * @param limit maximum allowed requests
     * @param currentCount current number of requests
     * @param resetTime time when the rate limit resets
     * @param exceeded whether the rate limit has been exceeded
     * @param type type of rate limit
     */
    public RateLimitInfo(String identifier, int limit, int currentCount, Instant resetTime, boolean exceeded,
            RateLimitType type) {
        this.identifier = identifier;
        this.limit = limit;
        this.currentCount = currentCount;
        this.resetTime = resetTime;
        this.exceeded = exceeded;
        this.type = type;
    }

    /**
     * Constructor for RateLimitInfo with default GENERAL type.
     * 
     * @param identifier rate limit identifier
     * @param limit maximum allowed requests
     * @param currentCount current number of requests
     * @param resetTime time when the rate limit resets
     * @param exceeded whether the rate limit has been exceeded
     */
    public RateLimitInfo(String identifier, int limit, int currentCount, Instant resetTime, boolean exceeded) {
        this(identifier, limit, currentCount, resetTime, exceeded, RateLimitType.GENERAL);
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
     * Get the rate limit type.
     * 
     * @return rate limit type
     */
    public RateLimitType getType() {
        return type;
    }

    /**
     * Get remaining requests allowed.
     * 
     * @return remaining requests
     */
    public int getRemainingRequests() {
        return Math.max(0, limit - currentCount);
    }

    /**
     * Get usage percentage.
     * 
     * @return usage percentage (0.0 to 1.0)
     */
    public double getUsagePercentage() {
        return limit > 0 ? (double) currentCount / limit : 0.0;
    }

    // Backward-compatible aliases for tool security API
    public int currentRequests() {
        return currentCount;
    }

    public int maxRequests() {
        return limit;
    }

    // Backward-compatible aliases for reasoning security
    public String getAgentId() {
        return type == RateLimitType.AGENT ? identifier : null;
    }

    public int getMaxRequestsPerMinute() {
        return limit;
    }

    public int getMaxRequestsPerSecond() {
        return limit; // Simplified - in practice this would need more complex logic
    }

    public int getRequestsThisMinute() {
        return currentCount;
    }

    public int getRequestsThisSecond() {
        return currentCount; // Simplified - in practice this would need more complex logic
    }

    public long getLastMinuteReset() {
        return resetTime.toEpochMilli();
    }

    public long getLastSecondReset() {
        return resetTime.toEpochMilli(); // Simplified - in practice this would need more complex logic
    }

    @Override
    public String toString() {
        return String.format("RateLimitInfo{identifier='%s', type=%s, limit=%d, current=%d, exceeded=%s, resetTime=%s}",
                identifier, type, limit, currentCount, exceeded, resetTime);
    }
}
