package org.openhab.core.ai.common.llm;

import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Rate limiting information for an LLM provider.
 * 
 * @author openHAB AI Team
 * @since 4.0.0
 */
@NonNullByDefault
public class LLMRateLimitInfo {

    private final int remainingRequests;
    private final int totalRequests;
    private final Instant resetTime;
    private final String window;

    public LLMRateLimitInfo(int remainingRequests, int totalRequests, Instant resetTime, String window) {
        this.remainingRequests = remainingRequests;
        this.totalRequests = totalRequests;
        this.resetTime = resetTime;
        this.window = window;
    }

    /**
     * Gets the number of remaining requests in the current window.
     * 
     * @return Remaining requests
     */
    public int getRemainingRequests() {
        return remainingRequests;
    }

    /**
     * Gets the total number of requests allowed in the window.
     * 
     * @return Total requests
     */
    public int getTotalRequests() {
        return totalRequests;
    }

    /**
     * Gets the time when the rate limit resets.
     * 
     * @return Reset time
     */
    public Instant getResetTime() {
        return resetTime;
    }

    /**
     * Gets the rate limit window description.
     * 
     * @return Window description (e.g., "per minute", "per hour")
     */
    public String getWindow() {
        return window;
    }

    /**
     * Checks if the rate limit has been exceeded.
     * 
     * @return true if rate limit exceeded
     */
    public boolean isExceeded() {
        return remainingRequests <= 0;
    }

    /**
     * Gets the percentage of requests used.
     * 
     * @return Percentage used (0.0 to 1.0)
     */
    public double getUsagePercentage() {
        if (totalRequests == 0) {
            return 0.0;
        }
        return (double) (totalRequests - remainingRequests) / totalRequests;
    }

    @Override
    public String toString() {
        return "LLMRateLimitInfo{remainingRequests=" + remainingRequests + ", totalRequests=" + totalRequests
                + ", resetTime=" + resetTime + ", window='" + window + "'}";
    }
}
