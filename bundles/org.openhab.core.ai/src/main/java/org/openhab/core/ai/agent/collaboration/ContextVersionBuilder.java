package org.openhab.core.ai.agent.collaboration;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Builder for ContextVersion instances.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ContextVersionBuilder {

    private String versionId;
    private String contextId;
    private String agentId;
    private Instant timestamp = Instant.now();
    private Map<String, Object> data = new HashMap<>();
    private @Nullable ContextOptions options;

    /**
     * Default constructor.
     */
    public ContextVersionBuilder() {
        // Use default values
    }

    /**
     * Copy constructor for creating a builder from an existing ContextVersion.
     * 
     * @param source the source ContextVersion to copy from
     */
    public ContextVersionBuilder(ContextVersion source) {
        this.versionId = source.getVersionId();
        this.contextId = source.getContextId();
        this.agentId = source.getAgentId();
        this.timestamp = source.getTimestamp();
        this.data = new HashMap<>(source.getData());
        this.options = source.getOptions();
    }

    /**
     * Set the version ID.
     * 
     * @param versionId the version ID
     * @return this builder
     */
    public ContextVersionBuilder versionId(String versionId) {
        this.versionId = versionId;
        return this;
    }

    /**
     * Set the context ID.
     * 
     * @param contextId the context ID
     * @return this builder
     */
    public ContextVersionBuilder contextId(String contextId) {
        this.contextId = contextId;
        return this;
    }

    /**
     * Set the agent ID.
     * 
     * @param agentId the agent ID
     * @return this builder
     */
    public ContextVersionBuilder agentId(String agentId) {
        this.agentId = agentId;
        return this;
    }

    /**
     * Set the timestamp.
     * 
     * @param timestamp the timestamp
     * @return this builder
     */
    public ContextVersionBuilder timestamp(Instant timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    /**
     * Set the data.
     * 
     * @param data the data map
     * @return this builder
     */
    public ContextVersionBuilder data(Map<String, Object> data) {
        this.data = new HashMap<>(data);
        return this;
    }

    /**
     * Set the options.
     * 
     * @param options the context options
     * @return this builder
     */
    public ContextVersionBuilder options(@Nullable ContextOptions options) {
        this.options = options;
        return this;
    }

    /**
     * Build the ContextVersion.
     * 
     * @return the built ContextVersion
     */
    public ContextVersion build() {
        return new ContextVersion(versionId, contextId, agentId, timestamp, data, options);
    }
}
