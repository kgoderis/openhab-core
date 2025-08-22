package org.openhab.core.ai.agent.api;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Dialogue response for autonomous agents
 * 
 * <p>
 * This class provides:
 * - Dialogue response representation and content
 * - Response metadata and context
 * - Response processing and analysis
 * - Response validation and safety
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public final class AgentModelDialogueResponse {

    // Response identification
    private final String responseId;
    private final String sessionId;
    private final String messageId;
    private final String agentId;
    private final Instant timestamp;

    // Response content
    private final String content;
    private final String language;
    private final ResponseType type;
    private final Map<String, Object> metadata;
    private final Map<String, Object> context;

    // Response processing
    private final @Nullable String intent;
    private final @Nullable List<String> actions;
    private final @Nullable Double confidence;
    private final @Nullable Double sentiment;

    // Response validation
    private final boolean isValid;
    private final @Nullable String validationMessage;
    private final boolean isSafe;
    private final @Nullable List<String> safetyIssues;

    // Response metrics
    private final Duration responseTime;
    private final int contentLength;
    private final @Nullable Double relevanceScore;

    private AgentModelDialogueResponse(Builder builder) {
        this.responseId = builder.responseId;
        this.sessionId = builder.sessionId;
        this.messageId = builder.messageId;
        this.agentId = builder.agentId;
        this.timestamp = builder.timestamp;
        this.content = builder.content;
        this.language = builder.language;
        this.type = builder.type;
        this.metadata = Map.copyOf(builder.metadata);
        this.context = Map.copyOf(builder.context);
        this.intent = builder.intent;
        this.actions = builder.actions != null ? List.copyOf(builder.actions) : null;
        this.confidence = builder.confidence;
        this.sentiment = builder.sentiment;
        this.isValid = builder.isValid;
        this.validationMessage = builder.validationMessage;
        this.isSafe = builder.isSafe;
        this.safetyIssues = builder.safetyIssues != null ? List.copyOf(builder.safetyIssues) : null;
        this.responseTime = builder.responseTime;
        this.contentLength = builder.contentLength;
        this.relevanceScore = builder.relevanceScore;
    }

    /**
     * Create a new dialogue response builder
     * 
     * @param sessionId the session ID
     * @param messageId the message ID
     * @param agentId the agent ID
     * @param content the response content
     * @return the builder
     */
    public static Builder builder(String sessionId, String messageId, String agentId, String content) {
        return new Builder(sessionId, messageId, agentId, content);
    }

    // Getters
    public String getResponseId() {
        return responseId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getMessageId() {
        return messageId;
    }

    public String getAgentId() {
        return agentId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public String getContent() {
        return content;
    }

    public String getLanguage() {
        return language;
    }

    public ResponseType getType() {
        return type;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public Map<String, Object> getContext() {
        return context;
    }

    public @Nullable String getIntent() {
        return intent;
    }

    public @Nullable List<String> getActions() {
        return actions;
    }

    public @Nullable Double getConfidence() {
        return confidence;
    }

    public @Nullable Double getSentiment() {
        return sentiment;
    }

    public boolean isValid() {
        return isValid;
    }

    public @Nullable String getValidationMessage() {
        return validationMessage;
    }

    public boolean isSafe() {
        return isSafe;
    }

    public @Nullable List<String> getSafetyIssues() {
        return safetyIssues;
    }

    public Duration getResponseTime() {
        return responseTime;
    }

    public int getContentLength() {
        return contentLength;
    }

    public @Nullable Double getRelevanceScore() {
        return relevanceScore;
    }

    /**
     * Check if the response is informative
     * 
     * @return true if informative
     */
    public boolean isInformative() {
        return type == ResponseType.INFORMATIVE || type == ResponseType.EDUCATIONAL;
    }

    /**
     * Check if the response is actionable
     * 
     * @return true if actionable
     */
    public boolean isActionable() {
        return type == ResponseType.ACTIONABLE || (actions != null && !actions.isEmpty());
    }

    /**
     * Check if the response is conversational
     * 
     * @return true if conversational
     */
    public boolean isConversational() {
        return type == ResponseType.CONVERSATIONAL || type == ResponseType.CHAT;
    }

    /**
     * Check if the response has safety issues
     * 
     * @return true if has safety issues
     */
    public boolean hasSafetyIssues() {
        return !isSafe || (safetyIssues != null && !safetyIssues.isEmpty());
    }

    /**
     * Check if the response is high quality
     * 
     * @return true if high quality
     */
    public boolean isHighQuality() {
        return isValid && isSafe && confidence != null && confidence >= 0.8 && relevanceScore != null
                && relevanceScore >= 0.7;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        AgentModelDialogueResponse that = (AgentModelDialogueResponse) obj;
        return Objects.equals(responseId, that.responseId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(responseId);
    }

    @Override
    public String toString() {
        return String.format("AgentModelDialogueResponse{responseId='%s', sessionId='%s', type=%s, content='%s'}",
                responseId, sessionId, type, content.length() > 50 ? content.substring(0, 50) + "..." : content);
    }

    /**
     * Response type enum
     */
    public enum ResponseType {
        INFORMATIVE,
        ACTIONABLE,
        CONVERSATIONAL,
        EDUCATIONAL,
        CHAT,
        ERROR,
        WARNING,
        DEBUG
    }

    /**
     * Builder for AgentModelDialogueResponse
     */
    public static final class Builder {
        private String responseId;
        private String sessionId;
        private String messageId;
        private String agentId;
        private Instant timestamp;
        private String content;
        private String language;
        private ResponseType type;
        private Map<String, Object> metadata;
        private Map<String, Object> context;
        private @Nullable String intent;
        private @Nullable List<String> actions;
        private @Nullable Double confidence;
        private @Nullable Double sentiment;
        private boolean isValid;
        private @Nullable String validationMessage;
        private boolean isSafe;
        private @Nullable List<String> safetyIssues;
        private Duration responseTime;
        private int contentLength;
        private @Nullable Double relevanceScore;

        public Builder(String sessionId, String messageId, String agentId, String content) {
            this.responseId = "resp_" + UUID.randomUUID().toString().replace("-", "");
            this.sessionId = Objects.requireNonNull(sessionId, "sessionId");
            this.messageId = Objects.requireNonNull(messageId, "messageId");
            this.agentId = Objects.requireNonNull(agentId, "agentId");
            this.timestamp = Instant.now();
            this.content = Objects.requireNonNull(content, "content");
            this.language = "en";
            this.type = ResponseType.CONVERSATIONAL;
            this.metadata = new HashMap<>();
            this.context = new HashMap<>();
            this.isValid = true;
            this.isSafe = true;
            this.responseTime = Duration.ZERO;
            this.contentLength = content.length();
        }

        public Builder responseId(String responseId) {
            this.responseId = Objects.requireNonNull(responseId, "responseId");
            return this;
        }

        public Builder sessionId(String sessionId) {
            this.sessionId = Objects.requireNonNull(sessionId, "sessionId");
            return this;
        }

        public Builder messageId(String messageId) {
            this.messageId = Objects.requireNonNull(messageId, "messageId");
            return this;
        }

        public Builder agentId(String agentId) {
            this.agentId = Objects.requireNonNull(agentId, "agentId");
            return this;
        }

        public Builder timestamp(Instant timestamp) {
            this.timestamp = Objects.requireNonNull(timestamp, "timestamp");
            return this;
        }

        public Builder content(String content) {
            this.content = Objects.requireNonNull(content, "content");
            this.contentLength = content.length();
            return this;
        }

        public Builder language(String language) {
            this.language = Objects.requireNonNull(language, "language");
            return this;
        }

        public Builder type(ResponseType type) {
            this.type = Objects.requireNonNull(type, "type");
            return this;
        }

        public Builder metadata(Map<String, Object> metadata) {
            this.metadata = new HashMap<>(Objects.requireNonNull(metadata, "metadata"));
            return this;
        }

        public Builder addMetadata(String key, Object value) {
            if (this.metadata == null) {
                this.metadata = new HashMap<>();
            }
            this.metadata.put(Objects.requireNonNull(key, "key"), value);
            return this;
        }

        public Builder context(Map<String, Object> context) {
            this.context = new HashMap<>(Objects.requireNonNull(context, "context"));
            return this;
        }

        public Builder addContext(String key, Object value) {
            if (this.context == null) {
                this.context = new HashMap<>();
            }
            this.context.put(Objects.requireNonNull(key, "key"), value);
            return this;
        }

        public Builder intent(@Nullable String intent) {
            this.intent = intent;
            return this;
        }

        public Builder actions(@Nullable List<String> actions) {
            this.actions = actions;
            return this;
        }

        public Builder addAction(String action) {
            if (this.actions == null) {
                this.actions = new ArrayList<>();
            }
            this.actions.add(Objects.requireNonNull(action, "action"));
            return this;
        }

        public Builder confidence(@Nullable Double confidence) {
            this.confidence = confidence;
            return this;
        }

        public Builder sentiment(@Nullable Double sentiment) {
            this.sentiment = sentiment;
            return this;
        }

        public Builder isValid(boolean isValid) {
            this.isValid = isValid;
            return this;
        }

        public Builder validationMessage(@Nullable String validationMessage) {
            this.validationMessage = validationMessage;
            return this;
        }

        public Builder isSafe(boolean isSafe) {
            this.isSafe = isSafe;
            return this;
        }

        public Builder safetyIssues(@Nullable List<String> safetyIssues) {
            this.safetyIssues = safetyIssues;
            return this;
        }

        public Builder addSafetyIssue(String issue) {
            if (this.safetyIssues == null) {
                this.safetyIssues = new ArrayList<>();
            }
            this.safetyIssues.add(Objects.requireNonNull(issue, "issue"));
            return this;
        }

        public Builder responseTime(Duration responseTime) {
            this.responseTime = Objects.requireNonNull(responseTime, "responseTime");
            return this;
        }

        public Builder contentLength(int contentLength) {
            this.contentLength = contentLength;
            return this;
        }

        public Builder relevanceScore(@Nullable Double relevanceScore) {
            this.relevanceScore = relevanceScore;
            return this;
        }

        public AgentModelDialogueResponse build() {
            return new AgentModelDialogueResponse(this);
        }
    }
}
