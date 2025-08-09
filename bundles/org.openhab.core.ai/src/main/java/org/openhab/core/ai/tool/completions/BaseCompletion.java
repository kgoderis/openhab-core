package org.openhab.core.ai.tool.completions;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.tool.completions.dto.Completion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for MCP Completions with encapsulation and lifecycle management.
 * 
 * This class provides a foundation for completion implementations that need
 * to encapsulate behavior, manage state, and handle lifecycle events.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public abstract class BaseCompletion {

    protected static final Logger LOGGER = LoggerFactory.getLogger(BaseCompletion.class);

    protected final String promptReference;
    protected final String description;
    protected final List<String> suggestions;
    protected final int total;
    protected final boolean hasMore;

    private volatile boolean valid = true;
    private volatile long lastRefreshTime = 0;
    private final long refreshIntervalMs;

    /**
     * Create a new abstract completion.
     * 
     * @param promptReference Reference to the prompt
     * @param description Completion description
     * @param suggestions List of suggestions
     * @param total Total number of suggestions
     * @param hasMore Whether there are more suggestions available
     * @param refreshIntervalMs Refresh interval in milliseconds (0 for no auto-refresh)
     */
    protected BaseCompletion(String promptReference, String description, List<String> suggestions, int total,
            boolean hasMore, long refreshIntervalMs) {
        this.promptReference = promptReference;
        this.description = description;
        this.suggestions = new CopyOnWriteArrayList<>(suggestions);
        this.total = total;
        this.hasMore = hasMore;
        this.refreshIntervalMs = refreshIntervalMs;
    }

    /**
     * Get the prompt reference.
     * 
     * @return the prompt reference
     */
    public String getPromptReference() {
        return promptReference;
    }

    /**
     * Get the completion description.
     * 
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Get the suggestions.
     * 
     * @return the suggestions
     */
    public List<String> getSuggestions() {
        return new CopyOnWriteArrayList<>(suggestions);
    }

    /**
     * Get the total number of suggestions.
     * 
     * @return the total
     */
    public int getTotal() {
        return total;
    }

    /**
     * Check if there are more suggestions available.
     * 
     * @return true if there are more suggestions
     */
    public boolean hasMore() {
        return hasMore;
    }

    /**
     * Check if the completion is valid.
     * 
     * @return true if valid
     */
    public boolean isValid() {
        return valid;
    }

    /**
     * Get the last refresh time.
     * 
     * @return the last refresh time in milliseconds since epoch
     */
    public long getLastRefreshTime() {
        return lastRefreshTime;
    }

    /**
     * Check if the completion needs refreshing.
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
     * Get additional suggestions.
     * This method should be implemented by subclasses to provide additional suggestions.
     * 
     * @param context the context for additional suggestions
     * @return additional suggestions or null if not available
     */
    public abstract @Nullable List<String> getAdditionalSuggestions(@Nullable String context);

    /**
     * Refresh the completion data.
     * This method should be implemented by subclasses to refresh their internal state.
     */
    public abstract void refresh();

    /**
     * Close the completion and release any resources.
     * This method should be implemented by subclasses to perform cleanup.
     */
    public abstract void close();

    /**
     * Mark the completion as invalid.
     */
    public void markInvalid() {
        this.valid = false;
    }

    /**
     * Update the refresh timestamp.
     */
    protected void updateRefreshTime() {
        this.lastRefreshTime = System.currentTimeMillis();
    }

    /**
     * Create a Completion DTO from this abstract completion.
     * 
     * @return the Completion DTO
     */
    public Completion toCompletion() {
        return new Completion(promptReference, description, suggestions, total, hasMore);
    }

    @Override
    public String toString() {
        return "BaseCompletion{promptReference='" + promptReference + "', description='" + description
                + "', suggestions=" + suggestions + ", total=" + total + ", hasMore=" + hasMore + ", valid=" + valid
                + "}";
    }
}
