package org.openhab.core.ai.common.context;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Builder for ReasoningContext instances.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public final class ReasoningContextBuilder {

    private String contextId = UUID.randomUUID().toString();
    private String initialContext = "";
    private String currentContext = "";
    private @Nullable String domain;
    private @Nullable String userId;
    private @Nullable String sessionId;
    private Map<String, Object> values = new HashMap<>();
    private Map<String, Object> metadata = new HashMap<>();
    private Instant createdAt = Instant.now();
    private Instant lastModifiedAt = Instant.now();
    private long reasoningStartTime = System.currentTimeMillis();
    private int reasoningStepCount = 0;
    private boolean isActive = true;

    public ReasoningContextBuilder withContextId(String contextId) {
        this.contextId = contextId;
        return this;
    }

    public ReasoningContextBuilder withInitialContext(String initialContext) {
        this.initialContext = initialContext;
        return this;
    }

    public ReasoningContextBuilder withCurrentContext(String currentContext) {
        this.currentContext = currentContext;
        return this;
    }

    public ReasoningContextBuilder withDomain(@Nullable String domain) {
        this.domain = domain;
        return this;
    }

    public ReasoningContextBuilder withUserId(@Nullable String userId) {
        this.userId = userId;
        return this;
    }

    public ReasoningContextBuilder withSessionId(@Nullable String sessionId) {
        this.sessionId = sessionId;
        return this;
    }

    public ReasoningContextBuilder withValues(Map<String, Object> values) {
        this.values = new HashMap<>(values);
        return this;
    }

    public ReasoningContextBuilder withValue(String key, Object value) {
        this.values.put(key, value);
        return this;
    }

    public ReasoningContextBuilder withMetadata(Map<String, Object> metadata) {
        this.metadata = new HashMap<>(metadata);
        return this;
    }

    public ReasoningContextBuilder withMetadata(String key, Object value) {
        this.metadata.put(key, value);
        return this;
    }

    public ReasoningContextBuilder withCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public ReasoningContextBuilder withLastModifiedAt(Instant lastModifiedAt) {
        this.lastModifiedAt = lastModifiedAt;
        return this;
    }

    public ReasoningContextBuilder withReasoningStartTime(long reasoningStartTime) {
        this.reasoningStartTime = reasoningStartTime;
        return this;
    }

    public ReasoningContextBuilder withReasoningStepCount(int reasoningStepCount) {
        this.reasoningStepCount = reasoningStepCount;
        return this;
    }

    public ReasoningContextBuilder withActive(boolean isActive) {
        this.isActive = isActive;
        return this;
    }

    public ReasoningContext build() {
        return new ReasoningContext(contextId, initialContext, currentContext, domain, userId, sessionId, values,
                metadata, createdAt, lastModifiedAt, reasoningStartTime, reasoningStepCount, isActive);
    }

    public static ReasoningContextBuilder builder() {
        return new ReasoningContextBuilder();
    }
}
