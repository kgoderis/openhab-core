package org.openhab.core.ai.agent.collaboration;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Builder for ContextOptions instances.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ContextOptionsBuilder {

    private long expectedVersion = 0;
    private boolean persistent = false;
    private Duration ttl = Duration.ofHours(1);
    private Map<String, Object> metadata = new HashMap<>();

    /**
     * Default constructor.
     */
    public ContextOptionsBuilder() {
        // Use default values
    }

    /**
     * Copy constructor for creating a builder from an existing ContextOptions.
     * 
     * @param source the source ContextOptions to copy from
     */
    public ContextOptionsBuilder(ContextOptions source) {
        this.expectedVersion = source.getExpectedVersion();
        this.persistent = source.isPersistent();
        this.ttl = source.getTtl();
        this.metadata = new HashMap<>(source.getMetadata());
    }

    /**
     * Set the expected version.
     * 
     * @param expectedVersion the expected version
     * @return this builder
     */
    public ContextOptionsBuilder expectedVersion(long expectedVersion) {
        this.expectedVersion = expectedVersion;
        return this;
    }

    /**
     * Set whether the context is persistent.
     * 
     * @param persistent true if persistent
     * @return this builder
     */
    public ContextOptionsBuilder persistent(boolean persistent) {
        this.persistent = persistent;
        return this;
    }

    /**
     * Set the time-to-live duration.
     * 
     * @param ttl the TTL duration
     * @return this builder
     */
    public ContextOptionsBuilder ttl(Duration ttl) {
        this.ttl = ttl;
        return this;
    }

    /**
     * Set the metadata.
     * 
     * @param metadata the metadata map
     * @return this builder
     */
    public ContextOptionsBuilder metadata(Map<String, Object> metadata) {
        this.metadata = new HashMap<>(metadata);
        return this;
    }

    /**
     * Build the ContextOptions.
     * 
     * @return the built ContextOptions
     */
    public ContextOptions build() {
        return new ContextOptions(expectedVersion, persistent, ttl, metadata);
    }
}
