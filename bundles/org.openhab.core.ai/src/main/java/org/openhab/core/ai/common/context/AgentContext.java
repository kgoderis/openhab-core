package org.openhab.core.ai.common.context;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Context for autonomous agent operations.
 * 
 * <p>
 * This class provides a unified context for agent-related operations:
 * - Agent execution context management
 * - Agent-specific metadata and state
 * - Agent collaboration and coordination
 * - Thread-safe context access
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class AgentContext extends BaseContext {

    private final String agentId;
    private final String agentType;
    private final String agentVersion;
    private final @Nullable String specialization;
    private final @Nullable String domain;
    private final @Nullable String userId;
    private final @Nullable String sessionId;
    private final long sessionStartTime;
    private final boolean isActive;

    /**
     * Create a new agent context.
     * 
     * @param contextId the unique context identifier
     * @param agentId the agent identifier
     * @param agentType the agent type
     * @param agentVersion the agent version
     * @param specialization the agent specialization (optional)
     * @param domain the agent domain (optional)
     * @param userId the user identifier (optional)
     * @param sessionId the session identifier (optional)
     * @param values the context values
     * @param metadata the context metadata
     */
    public AgentContext(String contextId, String agentId, String agentType, String agentVersion,
            @Nullable String specialization, @Nullable String domain, @Nullable String userId,
            @Nullable String sessionId, @Nullable Map<String, Object> values, @Nullable Map<String, Object> metadata) {
        super(contextId, "agent", "1.0.0", values, metadata);
        this.agentId = Objects.requireNonNull(agentId, "Agent ID cannot be null");
        this.agentType = Objects.requireNonNull(agentType, "Agent type cannot be null");
        this.agentVersion = Objects.requireNonNull(agentVersion, "Agent version cannot be null");
        this.specialization = specialization;
        this.domain = domain;
        this.userId = userId;
        this.sessionId = sessionId;
        this.sessionStartTime = System.currentTimeMillis();
        this.isActive = true;
    }

    /**
     * Default constructor with sensible defaults.
     */
    public AgentContext() {
        super("default-agent-context", "agent", "1.0.0", null, null);
        this.agentId = "default-agent";
        this.agentType = "default";
        this.agentVersion = "1.0.0";
        this.specialization = null;
        this.domain = null;
        this.userId = null;
        this.sessionId = null;
        this.sessionStartTime = System.currentTimeMillis();
        this.isActive = true;
    }

    /**
     * Create a new agent context with custom timestamps.
     * 
     * @param contextId the unique context identifier
     * @param agentId the agent identifier
     * @param agentType the agent type
     * @param agentVersion the agent version
     * @param specialization the agent specialization (optional)
     * @param domain the agent domain (optional)
     * @param userId the user identifier (optional)
     * @param sessionId the session identifier (optional)
     * @param values the context values
     * @param metadata the context metadata
     * @param createdAt the creation timestamp
     * @param lastModifiedAt the last modification timestamp
     * @param sessionStartTime the session start time
     * @param isActive whether the agent is active
     */
    public AgentContext(String contextId, String agentId, String agentType, String agentVersion,
            @Nullable String specialization, @Nullable String domain, @Nullable String userId,
            @Nullable String sessionId, @Nullable Map<String, Object> values, @Nullable Map<String, Object> metadata,
            Instant createdAt, Instant lastModifiedAt, long sessionStartTime, boolean isActive) {
        super(contextId, "agent", "1.0.0", values, metadata, createdAt, lastModifiedAt);
        this.agentId = Objects.requireNonNull(agentId, "Agent ID cannot be null");
        this.agentType = Objects.requireNonNull(agentType, "Agent type cannot be null");
        this.agentVersion = Objects.requireNonNull(agentVersion, "Agent version cannot be null");
        this.specialization = specialization;
        this.domain = domain;
        this.userId = userId;
        this.sessionId = sessionId;
        this.sessionStartTime = sessionStartTime;
        this.isActive = isActive;
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
     * Get the agent type.
     * 
     * @return the agent type
     */
    public String getAgentType() {
        return agentType;
    }

    /**
     * Get the agent version.
     * 
     * @return the agent version
     */
    public String getAgentVersion() {
        return agentVersion;
    }

    /**
     * Get the agent specialization.
     * 
     * @return the agent specialization, or null if not set
     */
    public @Nullable String getSpecialization() {
        return specialization;
    }

    /**
     * Get the agent domain.
     * 
     * @return the agent domain, or null if not set
     */
    public @Nullable String getDomain() {
        return domain;
    }

    /**
     * Get the user identifier.
     * 
     * @return the user ID, or null if not set
     */
    public @Nullable String getUserId() {
        return userId;
    }

    /**
     * Get the session identifier.
     * 
     * @return the session ID, or null if not set
     */
    public @Nullable String getSessionId() {
        return sessionId;
    }

    /**
     * Get the session start time.
     * 
     * @return the session start time in milliseconds
     */
    public long getSessionStartTime() {
        return sessionStartTime;
    }

    /**
     * Get the session duration.
     * 
     * @return the session duration in milliseconds
     */
    public long getSessionDuration() {
        return System.currentTimeMillis() - sessionStartTime;
    }

    /**
     * Check if the agent is active.
     * 
     * @return true if the agent is active
     */
    public boolean isActive() {
        return isActive;
    }

    @Override
    protected BaseContext createCopy(Map<String, Object> newValues, Map<String, Object> newMetadata) {
        return new AgentContext(getContextId(), agentId, agentType, agentVersion, specialization, domain, userId,
                sessionId, newValues, newMetadata);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj) || getClass() != obj.getClass()) {
            return false;
        }
        AgentContext other = (AgentContext) obj;
        return Objects.equals(agentId, other.agentId) && Objects.equals(agentType, other.agentType)
                && Objects.equals(agentVersion, other.agentVersion)
                && Objects.equals(specialization, other.specialization) && Objects.equals(domain, other.domain)
                && Objects.equals(userId, other.userId) && Objects.equals(sessionId, other.sessionId)
                && sessionStartTime == other.sessionStartTime && isActive == other.isActive;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), agentId, agentType, agentVersion, specialization, domain, userId,
                sessionId, sessionStartTime, isActive);
    }

    @Override
    public String toString() {
        return String.format(
                "AgentContext{id='%s', agentId='%s', agentType='%s', agentVersion='%s', specialization='%s', domain='%s', userId='%s', sessionId='%s', sessionTime=%dms, active=%s}",
                getContextId(), agentId, agentType, agentVersion, specialization, domain, userId, sessionId,
                getSessionDuration(), isActive);
    }
}
