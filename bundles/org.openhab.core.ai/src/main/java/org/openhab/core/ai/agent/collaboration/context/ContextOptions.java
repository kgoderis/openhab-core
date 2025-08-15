package org.openhab.core.ai.agent.collaboration.context;

import java.time.Duration;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Options for shared context operations.
 *
 * Immutable set of options controlling persistence, TTL and metadata for context operations.
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

    ContextOptions(ContextOptionsBuilder builder) {
        this.expectedVersion = builder.getExpectedVersion();
        this.persistent = builder.isPersistent();
        this.ttl = builder.getTtl();
        this.metadata = builder.getMetadata();
    }

    public long getExpectedVersion() {
        return expectedVersion;
    }

    public boolean isPersistent() {
        return persistent;
    }

    public Duration getTtl() {
        return ttl;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public static ContextOptionsBuilder builder() {
        return new ContextOptionsBuilder();
    }
}
