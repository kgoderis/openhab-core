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

    private ReasoningContext(Builder builder) {
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

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String initialContext = "";
        private String currentContext = "";
        private @Nullable String domain;
        private @Nullable String userId;
        private @Nullable String sessionId;
        private Instant timestamp = Instant.now();
        private Map<String, Object> metadata = Map.of();

        public Builder initialContext(String initialContext) {
            this.initialContext = initialContext;
            return this;
        }

        public Builder currentContext(String currentContext) {
            this.currentContext = currentContext;
            return this;
        }

        public Builder domain(@Nullable String domain) {
            this.domain = domain;
            return this;
        }

        public Builder userId(@Nullable String userId) {
            this.userId = userId;
            return this;
        }

        public Builder sessionId(@Nullable String sessionId) {
            this.sessionId = sessionId;
            return this;
        }

        public Builder timestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder metadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return this;
        }

        public ReasoningContext build() {
            return new ReasoningContext(this);
        }
    }
}
