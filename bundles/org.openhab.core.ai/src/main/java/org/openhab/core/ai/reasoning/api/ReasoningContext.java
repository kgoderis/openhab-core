package org.openhab.core.ai.reasoning.api;

import java.time.Instant;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Context for reasoning operations
 * 
 * @author Karel Goderis - Initial Contribution
 */
@NonNullByDefault
public class ReasoningContext {

    private final String initialContext;
    private final String currentContext;
    private final @Nullable String domain;
    private final @Nullable String userId;
    private final @Nullable String sessionId;
    private final Instant timestamp;
    private final Map<String, Object> metadata;

    ReasoningContext(ReasoningContextBuilder builder) {
        this.initialContext = builder.initialContext;
        this.currentContext = builder.currentContext;
        this.domain = builder.domain;
        this.userId = builder.userId;
        this.sessionId = builder.sessionId;
        this.timestamp = builder.timestamp;
        this.metadata = builder.metadata;
    }

    public ReasoningContext(String initialContext, String currentContext) {
        this.initialContext = initialContext;
        this.currentContext = currentContext;
        this.domain = null;
        this.userId = null;
        this.sessionId = null;
        this.timestamp = Instant.now();
        this.metadata = Map.of();
    }

    public String getInitialContext() {
        return initialContext;
    }

    public String getCurrentContext() {
        return currentContext;
    }

    public @Nullable String getDomain() {
        return domain;
    }

    public @Nullable String getUserId() {
        return userId;
    }

    public @Nullable String getSessionId() {
        return sessionId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public static ReasoningContextBuilder builder() {
        return new ReasoningContextBuilder();
    }

    /* Extracted: org.openhab.core.ai.reasoning.api.ReasoningContextBuilder */
}
