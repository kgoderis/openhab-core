package org.openhab.core.ai.common.adapter;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for all AI component adapters.
 * 
 * This class provides common functionality for adapter implementations including
 * lifecycle management, caching, and state tracking.
 * 
 * @param <T> The source type to be adapted
 * @param <C> The context type for the adaptation
 * @param <R> The result type after adaptation
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public abstract class BaseAdapter<T, C, R> implements Adapter<T, C, R> {

    protected static final Logger LOGGER = LoggerFactory.getLogger(BaseAdapter.class);

    private volatile boolean valid = true;
    private volatile long lastRefreshTime = 0;
    private final long refreshIntervalMs;

    /**
     * Create a new base adapter.
     * 
     * @param refreshIntervalMs Refresh interval in milliseconds (0 for no auto-refresh)
     */
    protected BaseAdapter(long refreshIntervalMs) {
        this.refreshIntervalMs = refreshIntervalMs;
    }

    @Override
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

    @Override
    public void refresh(String identifier, C context) {
        try {
            doRefresh(identifier, context);
            updateRefreshTime();
            LOGGER.debug("Refreshed adapter data for: {}", identifier);
        } catch (Exception e) {
            LOGGER.error("Error refreshing adapter data for: {}", identifier, e);
        }
    }

    @Override
    public void cleanup() {
        try {
            doCleanup();
            LOGGER.debug("Cleaned up adapter resources");
        } catch (Exception e) {
            LOGGER.error("Error cleaning up adapter resources", e);
        }
    }

    @Override
    public void close() {
        cleanup();
        markInvalid();
    }

    /**
     * Perform the actual refresh operation.
     * 
     * @param identifier the identifier to refresh
     * @param context the context for refresh
     */
    protected abstract void doRefresh(String identifier, C context);

    /**
     * Perform the actual cleanup operation.
     */
    protected abstract void doCleanup();
}
