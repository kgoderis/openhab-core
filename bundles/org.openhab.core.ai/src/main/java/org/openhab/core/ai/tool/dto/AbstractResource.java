package org.openhab.core.ai.tool.dto;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for MCP Resources with encapsulation and lifecycle management.
 * 
 * This class provides a foundation for resource implementations that need
 * to encapsulate behavior, manage state, and handle lifecycle events.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public abstract class AbstractResource {

    protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractResource.class);

    protected final String uri;
    protected final String name;
    protected final String description;
    protected final String mimeType;
    protected final Map<String, Object> metadata;

    private volatile boolean valid = true;
    private volatile long lastRefreshTime = 0;
    private final long refreshIntervalMs;

    /**
     * Create a new abstract resource.
     * 
     * @param uri Resource URI
     * @param name Resource name
     * @param description Resource description
     * @param mimeType MIME type
     * @param metadata Optional metadata
     * @param refreshIntervalMs Refresh interval in milliseconds (0 for no auto-refresh)
     */
    protected AbstractResource(String uri, String name, String description, String mimeType,
            @Nullable Map<String, Object> metadata, long refreshIntervalMs) {
        this.uri = uri;
        this.name = name;
        this.description = description;
        this.mimeType = mimeType;
        this.metadata = metadata != null ? new ConcurrentHashMap<>(metadata) : new ConcurrentHashMap<>();
        this.refreshIntervalMs = refreshIntervalMs;
    }

    /**
     * Get the resource URI.
     * 
     * @return the URI
     */
    public String getUri() {
        return uri;
    }

    /**
     * Get the resource name.
     * 
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the resource description.
     * 
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Get the MIME type.
     * 
     * @return the MIME type
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * Get the metadata.
     * 
     * @return the metadata
     */
    public Map<String, Object> getMetadata() {
        return new ConcurrentHashMap<>(metadata);
    }

    /**
     * Check if the resource is valid.
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
     * Check if the resource needs refreshing.
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
     * Get the resource content.
     * This method should be implemented by subclasses to provide the actual content.
     * 
     * @return the content as a string or null if not available
     */
    public abstract @Nullable String getContent();

    /**
     * Check if the resource is writable.
     * 
     * @return true if writable
     */
    public abstract boolean isWritable();

    /**
     * Write content to the resource.
     * 
     * @param content the content to write
     * @return true if successful
     */
    public abstract boolean writeContent(@Nullable String content);

    /**
     * Check if the resource exists.
     * 
     * @return true if exists
     */
    public abstract boolean exists();

    /**
     * Refresh the resource data.
     * This method should be implemented by subclasses to refresh their internal state.
     */
    public abstract void refresh();

    /**
     * Close the resource and release any resources.
     * This method should be implemented by subclasses to perform cleanup.
     */
    public abstract void close();

    /**
     * Mark the resource as invalid.
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
     * Create a Resource DTO from this abstract resource.
     * 
     * @return the Resource DTO
     */
    public Resource toResource() {
        return new Resource(uri, name, description, mimeType, metadata);
    }

    @Override
    public String toString() {
        return "AbstractResource{uri='" + uri + "', name='" + name + "', description='" + description + "', mimeType='"
                + mimeType + "', valid=" + valid + "}";
    }
}
