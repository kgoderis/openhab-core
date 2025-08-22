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
 * Dialogue session for autonomous agents
 * 
 * <p>
 * This class provides:
 * - Dialogue session representation and management
 * - Session state tracking and context management
 * - Message history and conversation flow
 * - Session metadata and configuration
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public final class AgentModelDialogueSession {

    // Session identification
    private final String sessionId;
    private final String agentId;
    private final String userId;
    private final Instant createdAt;
    private final String version;

    // Session state
    private final DialogueSessionState state;
    private final Map<String, Object> context;
    private final Map<String, Object> metadata;
    private final List<AgentModelDialogueMessage> messages;

    // Session configuration
    private final String dialogueStrategy;
    private final Duration sessionTimeout;
    private final int maxMessages;
    private final boolean persistent;

    // Session metrics
    private final int messageCount;
    private final Duration sessionDuration;
    private final @Nullable Instant lastActivityTime;
    private final @Nullable Instant endedAt;

    // Session validation
    private final boolean isValid;
    private final @Nullable String validationMessage;
    private final double confidenceScore;

    private AgentModelDialogueSession(Builder builder) {
        this.sessionId = builder.sessionId;
        this.agentId = builder.agentId;
        this.userId = builder.userId;
        this.createdAt = builder.createdAt;
        this.version = builder.version;
        this.state = builder.state;
        this.context = Map.copyOf(builder.context);
        this.metadata = Map.copyOf(builder.metadata);
        this.messages = List.copyOf(builder.messages);
        this.dialogueStrategy = builder.dialogueStrategy;
        this.sessionTimeout = builder.sessionTimeout;
        this.maxMessages = builder.maxMessages;
        this.persistent = builder.persistent;
        this.messageCount = builder.messageCount;
        this.sessionDuration = builder.sessionDuration;
        this.lastActivityTime = builder.lastActivityTime;
        this.endedAt = builder.endedAt;
        this.isValid = builder.isValid;
        this.validationMessage = builder.validationMessage;
        this.confidenceScore = builder.confidenceScore;
    }

    /**
     * Create a new dialogue session builder
     * 
     * @param agentId the agent ID
     * @param userId the user ID
     * @return the builder
     */
    public static Builder builder(String agentId, String userId) {
        return new Builder(agentId, userId);
    }

    /**
     * Create a copy of this session with updated state
     * 
     * @param newState the new state
     * @return the updated session
     */
    public AgentModelDialogueSession withState(DialogueSessionState newState) {
        return new Builder(this).state(newState).build();
    }

    /**
     * Create a copy of this session with a new message
     * 
     * @param message the new message
     * @return the updated session
     */
    public AgentModelDialogueSession withMessage(AgentModelDialogueMessage message) {
        return new Builder(this).addMessage(message).messageCount(messageCount + 1).lastActivityTime(Instant.now())
                .build();
    }

    /**
     * Create a copy of this session with updated context
     * 
     * @param contextUpdates the context updates
     * @return the updated session
     */
    public AgentModelDialogueSession withContextUpdates(Map<String, Object> contextUpdates) {
        Map<String, Object> newContext = new HashMap<>(context);
        newContext.putAll(contextUpdates);
        return new Builder(this).context(newContext).lastActivityTime(Instant.now()).build();
    }

    /**
     * Create a copy of this session marked as ended
     * 
     * @param reason the reason for ending
     * @return the updated session
     */
    public AgentModelDialogueSession withEnded(String reason) {
        return new Builder(this).state(DialogueSessionState.ENDED).endedAt(Instant.now())
                .metadata(Map.of("endReason", reason)).build();
    }

    // Getters
    public String getSessionId() {
        return sessionId;
    }

    public String getAgentId() {
        return agentId;
    }

    public String getUserId() {
        return userId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getVersion() {
        return version;
    }

    public DialogueSessionState getState() {
        return state;
    }

    public Map<String, Object> getContext() {
        return context;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public List<AgentModelDialogueMessage> getMessages() {
        return messages;
    }

    public String getDialogueStrategy() {
        return dialogueStrategy;
    }

    public Duration getSessionTimeout() {
        return sessionTimeout;
    }

    public int getMaxMessages() {
        return maxMessages;
    }

    public boolean isPersistent() {
        return persistent;
    }

    public int getMessageCount() {
        return messageCount;
    }

    public Duration getSessionDuration() {
        return sessionDuration;
    }

    public @Nullable Instant getLastActivityTime() {
        return lastActivityTime;
    }

    public @Nullable Instant getEndedAt() {
        return endedAt;
    }

    public boolean isValid() {
        return isValid;
    }

    public @Nullable String getValidationMessage() {
        return validationMessage;
    }

    public double getConfidenceScore() {
        return confidenceScore;
    }

    /**
     * Check if the session is active
     * 
     * @return true if active
     */
    public boolean isActive() {
        return state == DialogueSessionState.ACTIVE;
    }

    /**
     * Check if the session has ended
     * 
     * @return true if ended
     */
    public boolean isEnded() {
        return state == DialogueSessionState.ENDED;
    }

    /**
     * Check if the session has timed out
     * 
     * @return true if timed out
     */
    public boolean isTimedOut() {
        if (lastActivityTime == null) {
            return false;
        }
        return Duration.between(lastActivityTime, Instant.now()).compareTo(sessionTimeout) > 0;
    }

    /**
     * Check if the session has reached the maximum message limit
     * 
     * @return true if at message limit
     */
    public boolean isAtMessageLimit() {
        return messageCount >= maxMessages;
    }

    /**
     * Get the session age
     * 
     * @return the session age
     */
    public Duration getSessionAge() {
        return Duration.between(createdAt, Instant.now());
    }

    /**
     * Get the time since last activity
     * 
     * @return the time since last activity
     */
    public @Nullable Duration getTimeSinceLastActivity() {
        if (lastActivityTime == null) {
            return null;
        }
        return Duration.between(lastActivityTime, Instant.now());
    }

    /**
     * Get the end reason
     * 
     * @return the end reason
     */
    public @Nullable String getEndReason() {
        if (metadata.containsKey("endReason")) {
            return metadata.get("endReason").toString();
        }
        return null;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        AgentModelDialogueSession that = (AgentModelDialogueSession) obj;
        return Objects.equals(sessionId, that.sessionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sessionId);
    }

    @Override
    public String toString() {
        return String.format(
                "AgentModelDialogueSession{sessionId='%s', agentId='%s', userId='%s', state=%s, messages=%d}",
                sessionId, agentId, userId, state, messageCount);
    }

    /**
     * Dialogue session state enum
     */
    public enum DialogueSessionState {
        CREATED,
        ACTIVE,
        PAUSED,
        ENDED,
        TIMED_OUT,
        ERROR
    }

    /**
     * Builder for AgentModelDialogueSession
     */
    public static final class Builder {
        private String sessionId;
        private String agentId;
        private String userId;
        private Instant createdAt;
        private String version;
        private DialogueSessionState state;
        private Map<String, Object> context;
        private Map<String, Object> metadata;
        private List<AgentModelDialogueMessage> messages;
        private String dialogueStrategy;
        private Duration sessionTimeout;
        private int maxMessages;
        private boolean persistent;
        private int messageCount;
        private Duration sessionDuration;
        private @Nullable Instant lastActivityTime;
        private @Nullable Instant endedAt;
        private boolean isValid;
        private @Nullable String validationMessage;
        private double confidenceScore;

        public Builder(String agentId, String userId) {
            this.sessionId = "dialogue_" + UUID.randomUUID().toString().replace("-", "");
            this.agentId = Objects.requireNonNull(agentId, "agentId");
            this.userId = Objects.requireNonNull(userId, "userId");
            this.createdAt = Instant.now();
            this.version = "1.0.0";
            this.state = DialogueSessionState.CREATED;
            this.context = new HashMap<>();
            this.metadata = new HashMap<>();
            this.messages = new ArrayList<>();
            this.dialogueStrategy = "default";
            this.sessionTimeout = Duration.ofMinutes(30);
            this.maxMessages = 100;
            this.persistent = false;
            this.messageCount = 0;
            this.sessionDuration = Duration.ZERO;
            this.isValid = true;
            this.confidenceScore = 1.0;
        }

        public Builder(AgentModelDialogueSession source) {
            this.sessionId = source.sessionId;
            this.agentId = source.agentId;
            this.userId = source.userId;
            this.createdAt = source.createdAt;
            this.version = source.version;
            this.state = source.state;
            this.context = new HashMap<>(source.context);
            this.metadata = new HashMap<>(source.metadata);
            this.messages = new ArrayList<>(source.messages);
            this.dialogueStrategy = source.dialogueStrategy;
            this.sessionTimeout = source.sessionTimeout;
            this.maxMessages = source.maxMessages;
            this.persistent = source.persistent;
            this.messageCount = source.messageCount;
            this.sessionDuration = source.sessionDuration;
            this.lastActivityTime = source.lastActivityTime;
            this.endedAt = source.endedAt;
            this.isValid = source.isValid;
            this.validationMessage = source.validationMessage;
            this.confidenceScore = source.confidenceScore;
        }

        public Builder sessionId(String sessionId) {
            this.sessionId = Objects.requireNonNull(sessionId, "sessionId");
            return this;
        }

        public Builder agentId(String agentId) {
            this.agentId = Objects.requireNonNull(agentId, "agentId");
            return this;
        }

        public Builder userId(String userId) {
            this.userId = Objects.requireNonNull(userId, "userId");
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
            return this;
        }

        public Builder version(String version) {
            this.version = Objects.requireNonNull(version, "version");
            return this;
        }

        public Builder state(DialogueSessionState state) {
            this.state = Objects.requireNonNull(state, "state");
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

        public Builder messages(List<AgentModelDialogueMessage> messages) {
            this.messages = new ArrayList<>(Objects.requireNonNull(messages, "messages"));
            return this;
        }

        public Builder addMessage(AgentModelDialogueMessage message) {
            if (this.messages == null) {
                this.messages = new ArrayList<>();
            }
            this.messages.add(Objects.requireNonNull(message, "message"));
            return this;
        }

        public Builder dialogueStrategy(String dialogueStrategy) {
            this.dialogueStrategy = Objects.requireNonNull(dialogueStrategy, "dialogueStrategy");
            return this;
        }

        public Builder sessionTimeout(Duration sessionTimeout) {
            this.sessionTimeout = Objects.requireNonNull(sessionTimeout, "sessionTimeout");
            return this;
        }

        public Builder maxMessages(int maxMessages) {
            this.maxMessages = maxMessages;
            return this;
        }

        public Builder persistent(boolean persistent) {
            this.persistent = persistent;
            return this;
        }

        public Builder messageCount(int messageCount) {
            this.messageCount = messageCount;
            return this;
        }

        public Builder sessionDuration(Duration sessionDuration) {
            this.sessionDuration = Objects.requireNonNull(sessionDuration, "sessionDuration");
            return this;
        }

        public Builder lastActivityTime(@Nullable Instant lastActivityTime) {
            this.lastActivityTime = lastActivityTime;
            return this;
        }

        public Builder endedAt(@Nullable Instant endedAt) {
            this.endedAt = endedAt;
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

        public Builder confidenceScore(double confidenceScore) {
            this.confidenceScore = confidenceScore;
            return this;
        }

        public AgentModelDialogueSession build() {
            return new AgentModelDialogueSession(this);
        }
    }
}
