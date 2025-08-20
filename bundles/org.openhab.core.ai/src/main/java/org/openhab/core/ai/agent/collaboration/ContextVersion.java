package org.openhab.core.ai.agent.collaboration;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Context version snapshot.
 * 
 * Represents an immutable snapshot of a context's state at a particular time.
 * This unified class combines functionality from both duplicate implementations.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ContextVersion {

    private final String versionId;
    private final String contextId;
    private final String agentId;
    private final Instant timestamp;
    private final Map<String, Object> data;
    private final @Nullable ContextOptions options;

    /**
     * Create a new ContextVersion.
     * 
     * @param versionId the version identifier
     * @param contextId the context identifier
     * @param agentId the agent identifier
     * @param timestamp the version timestamp
     * @param data the version data
     * @param options the context options
     */
    public ContextVersion(String versionId, String contextId, String agentId, Instant timestamp,
            Map<String, Object> data, @Nullable ContextOptions options) {
        this.versionId = Objects.requireNonNull(versionId, "versionId");
        this.contextId = Objects.requireNonNull(contextId, "contextId");
        this.agentId = Objects.requireNonNull(agentId, "agentId");
        this.timestamp = Objects.requireNonNull(timestamp, "timestamp");
        this.data = Objects.requireNonNull(data, "data");
        this.options = options;
    }

    /**
     * Get the version identifier.
     * 
     * @return the version ID
     */
    public String getVersionId() {
        return versionId;
    }

    /**
     * Get the context identifier.
     * 
     * @return the context ID
     */
    public String getContextId() {
        return contextId;
    }

    /**
     * Get the agent identifier.
     * 
     * @return the agent ID
     */
    public String getAgentId() {
        return agentId;
    }

    /**
     * Get the version timestamp.
     * 
     * @return the timestamp
     */
    public Instant getTimestamp() {
        return timestamp;
    }

    /**
     * Get the version data.
     * 
     * @return the data map
     */
    public Map<String, Object> getData() {
        return data;
    }

    /**
     * Get the context options.
     * 
     * @return the options, or null if not available
     */
    public @Nullable ContextOptions getOptions() {
        return options;
    }

    /**
     * Create a new builder for ContextVersion.
     * 
     * @return a new builder instance
     */
    public static ContextVersionBuilder builder() {
        return new ContextVersionBuilder();
    }

    /**
     * Create a builder from this instance for modification.
     * 
     * @return a new builder instance initialized with current values
     */
    public ContextVersionBuilder toBuilder() {
        return new ContextVersionBuilder(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ContextVersion other = (ContextVersion) obj;
        return Objects.equals(versionId, other.versionId) && Objects.equals(contextId, other.contextId)
                && Objects.equals(agentId, other.agentId) && Objects.equals(timestamp, other.timestamp)
                && Objects.equals(data, other.data) && Objects.equals(options, other.options);
    }

    @Override
    public int hashCode() {
        return Objects.hash(versionId, contextId, agentId, timestamp, data, options);
    }

    @Override
    public String toString() {
        return String.format(
                "ContextVersion{versionId='%s', contextId='%s', agentId='%s', timestamp=%s, data=%s, options=%s}",
                versionId, contextId, agentId, timestamp, data, options);
    }
}
