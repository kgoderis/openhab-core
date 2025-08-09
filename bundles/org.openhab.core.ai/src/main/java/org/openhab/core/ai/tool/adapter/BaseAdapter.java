package org.openhab.core.ai.tool.adapter;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for MCP Adapters with caching and lifecycle management.
 * 
 * This class provides a foundation for adapter implementations that need
 * to manage state, handle lifecycle events, and provide caching.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public abstract class BaseAdapter {

    protected static final Logger LOGGER = LoggerFactory.getLogger(BaseAdapter.class);

    private volatile boolean valid = true;
    private volatile long lastRefreshTime = 0;
    private final long refreshIntervalMs;

    /**
     * Create a new abstract adapter.
     * 
     * @param refreshIntervalMs Refresh interval in milliseconds (0 for no auto-refresh)
     */
    protected BaseAdapter(long refreshIntervalMs) {
        this.refreshIntervalMs = refreshIntervalMs;
    }

    /**
     * Check if the adapter is valid.
     * 
     * @return true if valid
     */
    public boolean isValid() {
        return valid;
    }

    /**
     * Get the last refresh time.
     * 
     * @return the last refresh time in milliseconds
     */
    public long getLastRefreshTime() {
        return lastRefreshTime;
    }

    /**
     * Check if the adapter needs refreshing.
     * 
     * @return true if refresh is needed
     */
    public boolean needsRefresh() {
        if (refreshIntervalMs <= 0) {
            return false;
        }
        return System.currentTimeMillis() - lastRefreshTime > refreshIntervalMs;
    }

    /**
     * Mark the adapter as invalid.
     */
    public void markInvalid() {
        valid = false;
        LOGGER.debug("Adapter marked as invalid");
    }

    /**
     * Update the refresh time.
     */
    protected void updateRefreshTime() {
        lastRefreshTime = System.currentTimeMillis();
    }

    /**
     * Close the adapter and clean up resources.
     */
    public abstract void close();
}
