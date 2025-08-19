package org.openhab.core.ai.agent.collaboration;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Unified shared context for agent collaboration.
 * 
 * This class combines functionality from both context and coordination packages,
 * providing a comprehensive shared context system for agent collaboration.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class SharedContext {

    private final String contextId;
    private final List<String> agentIds;
    private final Map<String, Object> contextData;
    private final ContextAccessLevel accessLevel;
    private final String createdBy;
    private final Instant createdAt;
    private final String lastModifiedBy;
    private final Instant lastModifiedAt;
    private final int version;
    private final @Nullable ContextVersion currentVersion;
    private final @Nullable ContextOptions options;

    /**
     * Create a new SharedContext.
     * 
     * @param contextId the context identifier
     * @param agentIds the list of agent IDs that share this context
     * @param contextData the context data
     * @param accessLevel the access level for the context
     * @param createdBy the agent that created this context
     * @param createdAt the creation timestamp
     * @param lastModifiedBy the agent that last modified this context
     * @param lastModifiedAt the last modification timestamp
     * @param version the context version
     * @param currentVersion the current version information
     * @param options the context options
     */
    public SharedContext(String contextId, List<String> agentIds, Map<String, Object> contextData,
            ContextAccessLevel accessLevel, String createdBy, Instant createdAt, String lastModifiedBy,
            Instant lastModifiedAt, int version, @Nullable ContextVersion currentVersion,
            @Nullable ContextOptions options) {
        this.contextId = Objects.requireNonNull(contextId, "contextId");
        this.agentIds = Objects.requireNonNull(agentIds, "agentIds");
        this.contextData = Objects.requireNonNull(contextData, "contextData");
        this.accessLevel = Objects.requireNonNull(accessLevel, "accessLevel");
        this.createdBy = Objects.requireNonNull(createdBy, "createdBy");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
        this.lastModifiedBy = Objects.requireNonNull(lastModifiedBy, "lastModifiedBy");
        this.lastModifiedAt = Objects.requireNonNull(lastModifiedAt, "lastModifiedAt");
        this.version = version;
        this.currentVersion = currentVersion;
        this.options = options;
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
     * Get the list of agent IDs that share this context.
     * 
     * @return the agent IDs
     */
    public List<String> getAgentIds() {
        return agentIds;
    }

    /**
     * Get the context data.
     * 
     * @return the context data
     */
    public Map<String, Object> getContextData() {
        return contextData;
    }

    /**
     * Get the access level for this context.
     * 
     * @return the access level
     */
    public ContextAccessLevel getAccessLevel() {
        return accessLevel;
    }

    /**
     * Get the agent that created this context.
     * 
     * @return the creator agent ID
     */
    public String getCreatedBy() {
        return createdBy;
    }

    /**
     * Get the creation timestamp.
     * 
     * @return the creation timestamp
     */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /**
     * Get the agent that last modified this context.
     * 
     * @return the last modifier agent ID
     */
    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    /**
     * Get the last modification timestamp.
     * 
     * @return the last modification timestamp
     */
    public Instant getLastModifiedAt() {
        return lastModifiedAt;
    }

    /**
     * Get the context version.
     * 
     * @return the version number
     */
    public int getVersion() {
        return version;
    }

    /**
     * Get the current version information.
     * 
     * @return the current version, or null if not available
     */
    public @Nullable ContextVersion getCurrentVersion() {
        return currentVersion;
    }

    /**
     * Get the context options.
     * 
     * @return the context options, or null if not available
     */
    public @Nullable ContextOptions getOptions() {
        return options;
    }

    /**
     * Check if an agent has access to this context.
     * 
     * @param agentId the agent ID to check
     * @return true if the agent has access
     */
    public boolean hasAccess(String agentId) {
        return agentIds.contains(agentId) || accessLevel == ContextAccessLevel.PUBLIC;
    }

    /**
     * Check if an agent has write access to this context.
     * 
     * @param agentId the agent ID to check
     * @return true if the agent has write access
     */
    public boolean hasWriteAccess(String agentId) {
        return agentIds.contains(agentId);
    }

    /**
     * Create a new builder for SharedContext.
     * 
     * @return a new builder instance
     */
    public static SharedContextBuilder builder() {
        return new SharedContextBuilder();
    }

    /**
     * Create a new builder from this context.
     * 
     * @return a new builder instance with this context's data
     */
    public SharedContextBuilder toBuilder() {
        return new SharedContextBuilder().contextId(contextId).agentIds(agentIds).contextData(contextData)
                .accessLevel(accessLevel).createdBy(createdBy).createdAt(createdAt).lastModifiedBy(lastModifiedBy)
                .lastModifiedAt(lastModifiedAt).version(version).currentVersion(currentVersion).options(options);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        SharedContext other = (SharedContext) obj;
        return Objects.equals(contextId, other.contextId) && Objects.equals(agentIds, other.agentIds)
                && Objects.equals(contextData, other.contextData) && accessLevel == other.accessLevel
                && Objects.equals(createdBy, other.createdBy) && Objects.equals(createdAt, other.createdAt)
                && Objects.equals(lastModifiedBy, other.lastModifiedBy)
                && Objects.equals(lastModifiedAt, other.lastModifiedAt) && version == other.version
                && Objects.equals(currentVersion, other.currentVersion) && Objects.equals(options, other.options);
    }

    @Override
    public int hashCode() {
        return Objects.hash(contextId, agentIds, contextData, accessLevel, createdBy, createdAt, lastModifiedBy,
                lastModifiedAt, version, currentVersion, options);
    }

    @Override
    public String toString() {
        return "SharedContext{" + "contextId='" + contextId + '\'' + ", agentIds=" + agentIds + ", contextData="
                + contextData + ", accessLevel=" + accessLevel + ", createdBy='" + createdBy + '\'' + ", createdAt="
                + createdAt + ", lastModifiedBy='" + lastModifiedBy + '\'' + ", lastModifiedAt=" + lastModifiedAt
                + ", version=" + version + ", currentVersion=" + currentVersion + ", options=" + options + '}';
    }
}
