package org.openhab.core.ai.agent.api;

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
 * Dialogue message for autonomous agents
 * 
 * <p>
 * This class provides:
 * - Dialogue message representation and content
 * - Message metadata and context
 * - Message processing and analysis
 * - Message validation and safety
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public final class AgentModelDialogueMessage {

    // Message identification
    private final String messageId;
    private final String sessionId;
    private final MessageType type;
    private final String sender;
    private final String recipient;
    private final Instant timestamp;

    // Message content
    private final String content;
    private final String language;
    private final Map<String, Object> metadata;
    private final Map<String, Object> context;

    // Message processing
    private final @Nullable String intent;
    private final @Nullable Map<String, Object> entities;
    private final @Nullable Double sentiment;
    private final @Nullable Double confidence;
    private final @Nullable List<String> actions;

    // Message validation
    private final boolean isValid;
    private final @Nullable String validationMessage;
    private final boolean isSafe;
    private final @Nullable List<String> safetyIssues;

    // Message response
    private final @Nullable String responseContent;
    private final @Nullable Instant responseTimestamp;
    private final @Nullable Double responseConfidence;

    private AgentModelDialogueMessage(Builder builder) {
        this.messageId = builder.messageId;
        this.sessionId = builder.sessionId;
        this.type = builder.type;
        this.sender = builder.sender;
        this.recipient = builder.recipient;
        this.timestamp = builder.timestamp;
        this.content = builder.content;
        this.language = builder.language;
        this.metadata = Map.copyOf(builder.metadata);
        this.context = Map.copyOf(builder.context);
        this.intent = builder.intent;
        this.entities = builder.entities != null ? Map.copyOf(builder.entities) : null;
        this.sentiment = builder.sentiment;
        this.confidence = builder.confidence;
        this.actions = builder.actions != null ? List.copyOf(builder.actions) : null;
        this.isValid = builder.isValid;
        this.validationMessage = builder.validationMessage;
        this.isSafe = builder.isSafe;
        this.safetyIssues = builder.safetyIssues != null ? List.copyOf(builder.safetyIssues) : null;
        this.responseContent = builder.responseContent;
        this.responseTimestamp = builder.responseTimestamp;
        this.responseConfidence = builder.responseConfidence;
    }

    /**
     * Create a new dialogue message builder
     * 
     * @param sessionId the session ID
     * @param type the message type
     * @param sender the sender
     * @param recipient the recipient
     * @param content the message content
     * @return the builder
     */
    public static Builder builder(String sessionId, MessageType type, String sender, String recipient, String content) {
        return new Builder(sessionId, type, sender, recipient, content);
    }

    /**
     * Create a copy of this message with response
     * 
     * @param responseContent the response content
     * @param responseConfidence the response confidence
     * @return the updated message
     */
    public AgentModelDialogueMessage withResponse(String responseContent, @Nullable Double responseConfidence) {
        return new Builder(this).responseContent(responseContent).responseTimestamp(Instant.now())
                .responseConfidence(responseConfidence).build();
    }

    /**
     * Create a copy of this message with processing results
     * 
     * @param intent the detected intent
     * @param entities the extracted entities
     * @param sentiment the sentiment score
     * @param confidence the confidence score
     * @return the updated message
     */
    public AgentModelDialogueMessage withProcessing(String intent, Map<String, Object> entities,
            @Nullable Double sentiment, @Nullable Double confidence) {
        return new Builder(this).intent(intent).entities(entities).sentiment(sentiment).confidence(confidence).build();
    }

    // Getters
    public String getMessageId() {
        return messageId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public MessageType getType() {
        return type;
    }

    public String getSender() {
        return sender;
    }

    public String getRecipient() {
        return recipient;
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

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public Map<String, Object> getContext() {
        return context;
    }

    public @Nullable String getIntent() {
        return intent;
    }

    public @Nullable Map<String, Object> getEntities() {
        return entities;
    }

    public @Nullable Double getSentiment() {
        return sentiment;
    }

    public @Nullable Double getConfidence() {
        return confidence;
    }

    public @Nullable List<String> getActions() {
        return actions;
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

    public @Nullable String getResponseContent() {
        return responseContent;
    }

    public @Nullable Instant getResponseTimestamp() {
        return responseTimestamp;
    }

    public @Nullable Double getResponseConfidence() {
        return responseConfidence;
    }

    /**
     * Check if this is a user message
     * 
     * @return true if user message
     */
    public boolean isUserMessage() {
        return type == MessageType.USER;
    }

    /**
     * Check if this is an agent message
     * 
     * @return true if agent message
     */
    public boolean isAgentMessage() {
        return type == MessageType.AGENT;
    }

    /**
     * Check if this is a system message
     * 
     * @return true if system message
     */
    public boolean isSystemMessage() {
        return type == MessageType.SYSTEM;
    }

    /**
     * Check if the message has been processed
     * 
     * @return true if processed
     */
    public boolean isProcessed() {
        return intent != null || entities != null || sentiment != null || confidence != null;
    }

    /**
     * Check if the message has a response
     * 
     * @return true if has response
     */
    public boolean hasResponse() {
        return responseContent != null && responseTimestamp != null;
    }

    /**
     * Check if the message has safety issues
     * 
     * @return true if has safety issues
     */
    public boolean hasSafetyIssues() {
        return !isSafe || (safetyIssues != null && !safetyIssues.isEmpty());
    }

    /**
     * Get the message age
     * 
     * @return the message age
     */
    public java.time.Duration getAge() {
        return java.time.Duration.between(timestamp, Instant.now());
    }

    /**
     * Get the response time
     * 
     * @return the response time
     */
    public java.time.@Nullable Duration getResponseTime() {
        if (responseTimestamp == null) {
            return null;
        }
        return java.time.Duration.between(timestamp, responseTimestamp);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        AgentModelDialogueMessage that = (AgentModelDialogueMessage) obj;
        return Objects.equals(messageId, that.messageId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(messageId);
    }

    @Override
    public String toString() {
        return String.format(
                "AgentModelDialogueMessage{messageId='%s', sessionId='%s', type=%s, sender='%s', content='%s'}",
                messageId, sessionId, type, sender, content.length() > 50 ? content.substring(0, 50) + "..." : content);
    }

    /**
     * Message type enum
     */
    public enum MessageType {
        USER,
        AGENT,
        SYSTEM,
        ERROR,
        WARNING,
        DEBUG
    }

    /**
     * Builder for AgentModelDialogueMessage
     */
    public static final class Builder {
        private String messageId;
        private String sessionId;
        private MessageType type;
        private String sender;
        private String recipient;
        private Instant timestamp;
        private String content;
        private String language;
        private Map<String, Object> metadata;
        private Map<String, Object> context;
        private @Nullable String intent;
        private @Nullable Map<String, Object> entities;
        private @Nullable Double sentiment;
        private @Nullable Double confidence;
        private @Nullable List<String> actions;
        private boolean isValid;
        private @Nullable String validationMessage;
        private boolean isSafe;
        private @Nullable List<String> safetyIssues;
        private @Nullable String responseContent;
        private @Nullable Instant responseTimestamp;
        private @Nullable Double responseConfidence;

        public Builder(String sessionId, MessageType type, String sender, String recipient, String content) {
            this.messageId = "msg_" + UUID.randomUUID().toString().replace("-", "");
            this.sessionId = Objects.requireNonNull(sessionId, "sessionId");
            this.type = Objects.requireNonNull(type, "type");
            this.sender = Objects.requireNonNull(sender, "sender");
            this.recipient = Objects.requireNonNull(recipient, "recipient");
            this.timestamp = Instant.now();
            this.content = Objects.requireNonNull(content, "content");
            this.language = "en";
            this.metadata = new HashMap<>();
            this.context = new HashMap<>();
            this.isValid = true;
            this.isSafe = true;
        }

        public Builder(AgentModelDialogueMessage source) {
            this.messageId = source.messageId;
            this.sessionId = source.sessionId;
            this.type = source.type;
            this.sender = source.sender;
            this.recipient = source.recipient;
            this.timestamp = source.timestamp;
            this.content = source.content;
            this.language = source.language;
            this.metadata = new HashMap<>(source.metadata);
            this.context = new HashMap<>(source.context);
            this.intent = source.intent;
            this.entities = source.entities != null ? new HashMap<>(source.entities) : null;
            this.sentiment = source.sentiment;
            this.confidence = source.confidence;
            this.actions = source.actions != null ? new ArrayList<>(source.actions) : null;
            this.isValid = source.isValid;
            this.validationMessage = source.validationMessage;
            this.isSafe = source.isSafe;
            this.safetyIssues = source.safetyIssues != null ? new ArrayList<>(source.safetyIssues) : null;
            this.responseContent = source.responseContent;
            this.responseTimestamp = source.responseTimestamp;
            this.responseConfidence = source.responseConfidence;
        }

        public Builder messageId(String messageId) {
            this.messageId = Objects.requireNonNull(messageId, "messageId");
            return this;
        }

        public Builder sessionId(String sessionId) {
            this.sessionId = Objects.requireNonNull(sessionId, "sessionId");
            return this;
        }

        public Builder type(MessageType type) {
            this.type = Objects.requireNonNull(type, "type");
            return this;
        }

        public Builder sender(String sender) {
            this.sender = Objects.requireNonNull(sender, "sender");
            return this;
        }

        public Builder recipient(String recipient) {
            this.recipient = Objects.requireNonNull(recipient, "recipient");
            return this;
        }

        public Builder timestamp(Instant timestamp) {
            this.timestamp = Objects.requireNonNull(timestamp, "timestamp");
            return this;
        }

        public Builder content(String content) {
            this.content = Objects.requireNonNull(content, "content");
            return this;
        }

        public Builder language(String language) {
            this.language = Objects.requireNonNull(language, "language");
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

        public Builder entities(@Nullable Map<String, Object> entities) {
            this.entities = entities;
            return this;
        }

        public Builder addEntity(String key, Object value) {
            if (this.entities == null) {
                this.entities = new HashMap<>();
            }
            this.entities.put(Objects.requireNonNull(key, "key"), value);
            return this;
        }

        public Builder sentiment(@Nullable Double sentiment) {
            this.sentiment = sentiment;
            return this;
        }

        public Builder confidence(@Nullable Double confidence) {
            this.confidence = confidence;
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

        public Builder responseContent(@Nullable String responseContent) {
            this.responseContent = responseContent;
            return this;
        }

        public Builder responseTimestamp(@Nullable Instant responseTimestamp) {
            this.responseTimestamp = responseTimestamp;
            return this;
        }

        public Builder responseConfidence(@Nullable Double responseConfidence) {
            this.responseConfidence = responseConfidence;
            return this;
        }

        public AgentModelDialogueMessage build() {
            return new AgentModelDialogueMessage(this);
        }
    }
}
