package org.openhab.core.ai.agent.collaboration;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Builder for SharedContext instances.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class SharedContextBuilder {

    private String contextId;
    private List<String> agentIds = new ArrayList<>();
    private Map<String, Object> contextData = new HashMap<>();
    private ContextAccessLevel accessLevel = ContextAccessLevel.PRIVATE;
    private String createdBy;
    private Instant createdAt = Instant.now();
    private String lastModifiedBy;
    private Instant lastModifiedAt = Instant.now();
    private int version = 1;
    private @Nullable ContextVersion currentVersion;
    private @Nullable ContextOptions options;

    /**
     * Set the context ID.
     * 
     * @param contextId the context ID
     * @return this builder
     */
    public SharedContextBuilder contextId(String contextId) {
        this.contextId = contextId;
        return this;
    }

    /**
     * Set the agent IDs.
     * 
     * @param agentIds the agent IDs
     * @return this builder
     */
    public SharedContextBuilder agentIds(List<String> agentIds) {
        this.agentIds = new ArrayList<>(agentIds);
        return this;
    }

    /**
     * Set the context data.
     * 
     * @param contextData the context data
     * @return this builder
     */
    public SharedContextBuilder contextData(Map<String, Object> contextData) {
        this.contextData = new HashMap<>(contextData);
        return this;
    }

    /**
     * Set the access level.
     * 
     * @param accessLevel the access level
     * @return this builder
     */
    public SharedContextBuilder accessLevel(ContextAccessLevel accessLevel) {
        this.accessLevel = accessLevel;
        return this;
    }

    /**
     * Set the creator.
     * 
     * @param createdBy the creator agent ID
     * @return this builder
     */
    public SharedContextBuilder createdBy(String createdBy) {
        this.createdBy = createdBy;
        return this;
    }

    /**
     * Set the creation timestamp.
     * 
     * @param createdAt the creation timestamp
     * @return this builder
     */
    public SharedContextBuilder createdAt(Instant createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    /**
     * Set the last modifier.
     * 
     * @param lastModifiedBy the last modifier agent ID
     * @return this builder
     */
    public SharedContextBuilder lastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
        return this;
    }

    /**
     * Set the last modification timestamp.
     * 
     * @param lastModifiedAt the last modification timestamp
     * @return this builder
     */
    public SharedContextBuilder lastModifiedAt(Instant lastModifiedAt) {
        this.lastModifiedAt = lastModifiedAt;
        return this;
    }

    /**
     * Set the version.
     * 
     * @param version the version number
     * @return this builder
     */
    public SharedContextBuilder version(int version) {
        this.version = version;
        return this;
    }

    /**
     * Set the current version.
     * 
     * @param currentVersion the current version
     * @return this builder
     */
    public SharedContextBuilder currentVersion(@Nullable ContextVersion currentVersion) {
        this.currentVersion = currentVersion;
        return this;
    }

    /**
     * Set the context options.
     * 
     * @param options the context options
     * @return this builder
     */
    public SharedContextBuilder options(@Nullable ContextOptions options) {
        this.options = options;
        return this;
    }

    /**
     * Build the SharedContext.
     * 
     * @return the built SharedContext
     */
    public SharedContext build() {
        return new SharedContext(contextId, agentIds, contextData, accessLevel, createdBy, createdAt, lastModifiedBy,
                lastModifiedAt, version, currentVersion, options);
    }
}
