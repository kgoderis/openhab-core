package org.openhab.core.ai.agent.collaboration.context;

import java.time.Duration;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Builder for {@link ContextOptions}.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ContextOptionsBuilder {

    private long expectedVersion = -1;
    private boolean persistent = false;
    private Duration ttl = Duration.ofHours(24);
    private Map<String, Object> metadata = Map.of();

    public ContextOptionsBuilder expectedVersion(long expectedVersion) {
        this.expectedVersion = expectedVersion;
        return this;
    }

    public ContextOptionsBuilder persistent(boolean persistent) {
        this.persistent = persistent;
        return this;
    }

    public ContextOptionsBuilder ttl(Duration ttl) {
        this.ttl = ttl;
        return this;
    }

    public ContextOptionsBuilder metadata(Map<String, Object> metadata) {
        this.metadata = metadata;
        return this;
    }

    public ContextOptions build() {
        return new ContextOptions(this);
    }

    long getExpectedVersion() {
        return expectedVersion;
    }

    boolean isPersistent() {
        return persistent;
    }

    Duration getTtl() {
        return ttl;
    }

    Map<String, Object> getMetadata() {
        return metadata;
    }
}
