package org.openhab.core.ai.common.context;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Context for reasoning operations.
 * 
 * <p>
 * This class provides a unified context for reasoning-related operations:
 * - Reasoning session management
 * - Context evolution and state tracking
 * - Domain-specific reasoning metadata
 * - Thread-safe context access
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ReasoningContext extends BaseContext {

    private final String initialContext;
    private final String currentContext;
    private final @Nullable String domain;
    private final @Nullable String userId;
    private final @Nullable String sessionId;
    private final long reasoningStartTime;
    private final int reasoningStepCount;
    private final boolean isActive;

    /**
     * Create a new reasoning context.
     * 
     * @param contextId the unique context identifier
     * @param initialContext the initial reasoning context
     * @param currentContext the current reasoning context
     * @param domain the reasoning domain (optional)
     * @param userId the user identifier (optional)
     * @param sessionId the session identifier (optional)
     * @param values the context values
     * @param metadata the context metadata
     */
    public ReasoningContext(String contextId, String initialContext, String currentContext, @Nullable String domain,
            @Nullable String userId, @Nullable String sessionId, @Nullable Map<String, Object> values,
            @Nullable Map<String, Object> metadata) {
        super(contextId, "reasoning", "1.0.0", values, metadata);
        this.initialContext = Objects.requireNonNull(initialContext, "Initial context cannot be null");
        this.currentContext = Objects.requireNonNull(currentContext, "Current context cannot be null");
        this.domain = domain;
        this.userId = userId;
        this.sessionId = sessionId;
        this.reasoningStartTime = System.currentTimeMillis();
        this.reasoningStepCount = 0;
        this.isActive = true;
    }

    /**
     * Create a new reasoning context with just context ID and initial context.
     * 
     * @param contextId the unique context identifier
     * @param initialContext the initial reasoning context
     */
    public ReasoningContext(String contextId, String initialContext) {
        super(contextId, "reasoning", "1.0.0", null, null);
        this.initialContext = Objects.requireNonNull(initialContext, "Initial context cannot be null");
        this.currentContext = initialContext;
        this.domain = null;
        this.userId = null;
        this.sessionId = null;
        this.reasoningStartTime = System.currentTimeMillis();
        this.reasoningStepCount = 0;
        this.isActive = true;
    }

    /**
     * Create a new reasoning context with custom timestamps.
     * 
     * @param contextId the unique context identifier
     * @param initialContext the initial reasoning context
     * @param currentContext the current reasoning context
     * @param domain the reasoning domain (optional)
     * @param userId the user identifier (optional)
     * @param sessionId the session identifier (optional)
     * @param values the context values
     * @param metadata the context metadata
     * @param createdAt the creation timestamp
     * @param lastModifiedAt the last modification timestamp
     * @param reasoningStartTime the reasoning start time
     * @param reasoningStepCount the number of reasoning steps
     * @param isActive whether the reasoning session is active
     */
    public ReasoningContext(String contextId, String initialContext, String currentContext, @Nullable String domain,
            @Nullable String userId, @Nullable String sessionId, @Nullable Map<String, Object> values,
            @Nullable Map<String, Object> metadata, Instant createdAt, Instant lastModifiedAt, long reasoningStartTime,
            int reasoningStepCount, boolean isActive) {
        super(contextId, "reasoning", "1.0.0", values, metadata, createdAt, lastModifiedAt);
        this.initialContext = Objects.requireNonNull(initialContext, "Initial context cannot be null");
        this.currentContext = Objects.requireNonNull(currentContext, "Current context cannot be null");
        this.domain = domain;
        this.userId = userId;
        this.sessionId = sessionId;
        this.reasoningStartTime = reasoningStartTime;
        this.reasoningStepCount = reasoningStepCount;
        this.isActive = isActive;
    }

    /**
     * Get the initial reasoning context.
     * 
     * @return the initial context
     */
    public String getInitialContext() {
        return initialContext;
    }

    /**
     * Get the current reasoning context.
     * 
     * @return the current context
     */
    public String getCurrentContext() {
        return currentContext;
    }

    /**
     * Get the reasoning domain.
     * 
     * @return the domain, or null if not set
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
     * Get the reasoning start time.
     * 
     * @return the reasoning start time in milliseconds
     */
    public long getReasoningStartTime() {
        return reasoningStartTime;
    }

    /**
     * Get the reasoning duration.
     * 
     * @return the reasoning duration in milliseconds
     */
    public long getReasoningDuration() {
        return System.currentTimeMillis() - reasoningStartTime;
    }

    /**
     * Get the number of reasoning steps.
     * 
     * @return the reasoning step count
     */
    public int getReasoningStepCount() {
        return reasoningStepCount;
    }

    /**
     * Check if the reasoning session is active.
     * 
     * @return true if the reasoning session is active
     */
    public boolean isActive() {
        return isActive;
    }

    /**
     * Check if the context has evolved from initial state.
     * 
     * @return true if the current context differs from initial context
     */
    public boolean hasEvolved() {
        return !initialContext.equals(currentContext);
    }

    /**
     * Create a new builder for ReasoningContext.
     * 
     * @return a new ReasoningContextBuilder instance
     */
    public static ReasoningContextBuilder builder() {
        return new ReasoningContextBuilder();
    }

    @Override
    protected BaseContext createCopy(Map<String, Object> newValues, Map<String, Object> newMetadata) {
        return new ReasoningContext(getContextId(), initialContext, currentContext, domain, userId, sessionId,
                newValues, newMetadata);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj) || getClass() != obj.getClass()) {
            return false;
        }
        ReasoningContext other = (ReasoningContext) obj;
        return Objects.equals(initialContext, other.initialContext)
                && Objects.equals(currentContext, other.currentContext) && Objects.equals(domain, other.domain)
                && Objects.equals(userId, other.userId) && Objects.equals(sessionId, other.sessionId)
                && reasoningStartTime == other.reasoningStartTime && reasoningStepCount == other.reasoningStepCount
                && isActive == other.isActive;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), initialContext, currentContext, domain, userId, sessionId,
                reasoningStartTime, reasoningStepCount, isActive);
    }

    @Override
    public String toString() {
        return String.format(
                "ReasoningContext{id='%s', initialContext='%s', currentContext='%s', domain='%s', userId='%s', sessionId='%s', reasoningTime=%dms, steps=%d, active=%s}",
                getContextId(), initialContext, currentContext, domain, userId, sessionId, getReasoningDuration(),
                reasoningStepCount, isActive);
    }
}
