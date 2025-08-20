package org.openhab.core.ai.agent.collaboration;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Options for shared context operations.
 * 
 * Immutable set of options controlling persistence, TTL and metadata for context operations.
 * This unified class combines functionality from both duplicate implementations.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ContextOptions {

    private final long expectedVersion;
    private final boolean persistent;
    private final Duration ttl;
    private final Map<String, Object> metadata;

    /**
     * Create a new ContextOptions.
     * 
     * @param expectedVersion the expected version for optimistic locking
     * @param persistent whether the context should be persisted
     * @param ttl the time-to-live duration
     * @param metadata additional metadata
     */
    public ContextOptions(long expectedVersion, boolean persistent, Duration ttl, Map<String, Object> metadata) {
        this.expectedVersion = expectedVersion;
        this.persistent = persistent;
        this.ttl = Objects.requireNonNull(ttl, "ttl");
        this.metadata = Objects.requireNonNull(metadata, "metadata");
    }

    /**
     * Get the expected version for optimistic locking.
     * 
     * @return the expected version
     */
    public long getExpectedVersion() {
        return expectedVersion;
    }

    /**
     * Check if the context should be persisted.
     * 
     * @return true if persistent
     */
    public boolean isPersistent() {
        return persistent;
    }

    /**
     * Get the time-to-live duration.
     * 
     * @return the TTL duration
     */
    public Duration getTtl() {
        return ttl;
    }

    /**
     * Get additional metadata.
     * 
     * @return the metadata map
     */
    public Map<String, Object> getMetadata() {
        return metadata;
    }

    /**
     * Create a new builder for ContextOptions.
     * 
     * @return a new builder instance
     */
    public static ContextOptionsBuilder builder() {
        return new ContextOptionsBuilder();
    }

    /**
     * Create a builder from this instance for modification.
     * 
     * @return a new builder instance initialized with current values
     */
    public ContextOptionsBuilder toBuilder() {
        return new ContextOptionsBuilder(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ContextOptions other = (ContextOptions) obj;
        return expectedVersion == other.expectedVersion && persistent == other.persistent
                && Objects.equals(ttl, other.ttl) && Objects.equals(metadata, other.metadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(expectedVersion, persistent, ttl, metadata);
    }

    @Override
    public String toString() {
        return String.format("ContextOptions{expectedVersion=%d, persistent=%s, ttl=%s, metadata=%s}", expectedVersion,
                persistent, ttl, metadata);
    }
}
