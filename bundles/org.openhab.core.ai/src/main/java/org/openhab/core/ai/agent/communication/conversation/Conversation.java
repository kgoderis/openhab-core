package org.openhab.core.ai.agent.communication.conversation;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Conversation aggregate.
 *
 * Represents a live or archived agent conversation.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class Conversation {
    private final String conversationId;
    private final Set<String> participantIds;
    private final @Nullable String templateId;
    private final Map<String, Object> context;
    private final Instant startTime;
    private ConversationState state;
    private @Nullable Instant endTime;
    private @Nullable String endReason;
    private Instant lastActivity;
    private int messageCount;

    /**
     * Private constructor for builder pattern.
     */
    private Conversation(Builder builder) {
        this.conversationId = builder.conversationId;
        this.participantIds = Set.copyOf(builder.participantIds);
        this.templateId = builder.templateId;
        this.context = Map.copyOf(builder.context);
        this.startTime = builder.startTime;
        this.state = builder.state;
        this.lastActivity = builder.lastActivity;
        this.messageCount = builder.messageCount;
    }

    public String getConversationId() {
        return conversationId;
    }

    public Set<String> getParticipantIds() {
        return participantIds;
    }

    public @Nullable String getTemplateId() {
        return templateId;
    }

    public Map<String, Object> getContext() {
        return context;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public ConversationState getState() {
        return state;
    }

    public void setState(ConversationState state) {
        this.state = state;
    }

    public @Nullable Instant getEndTime() {
        return endTime;
    }

    public void setEndTime(Instant endTime) {
        this.endTime = endTime;
    }

    public @Nullable String getEndReason() {
        return endReason;
    }

    public void setEndReason(String endReason) {
        this.endReason = endReason;
    }

    public Instant getLastActivity() {
        return lastActivity;
    }

    public void setLastActivity(Instant lastActivity) {
        this.lastActivity = lastActivity;
    }

    public int getMessageCount() {
        return messageCount;
    }

    public void setMessageCount(int messageCount) {
        this.messageCount = messageCount;
    }

    /**
     * Create a new builder for Conversation.
     *
     * @return a new Builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Create a builder from this Conversation for modification.
     *
     * @return a new Builder with current values
     */
    public Builder toBuilder() {
        return new Builder(this);
    }

    /**
     * Builder for creating Conversation instances.
     *
     * @author Karel Goderis - Initial Contribution
     * @since 1.0.0
     */
    public static final class Builder {
        private String conversationId = "";
        private Set<String> participantIds = Set.of();
        private @Nullable String templateId;
        private Map<String, Object> context = Map.of();
        private Instant startTime = Instant.now();
        private ConversationState state = ConversationState.ACTIVE;
        private Instant lastActivity = Instant.now();
        private int messageCount = 0;

        /**
         * Default constructor.
         */
        public Builder() {
        }

        /**
         * Copy constructor.
         *
         * @param source the source Conversation
         */
        public Builder(Conversation source) {
            this.conversationId = source.conversationId;
            this.participantIds = new HashSet<>(source.participantIds);
            this.templateId = source.templateId;
            this.context = new HashMap<>(source.context);
            this.startTime = source.startTime;
            this.state = source.state;
            this.lastActivity = source.lastActivity;
            this.messageCount = source.messageCount;
        }

        public Builder withConversationId(String conversationId) {
            this.conversationId = Objects.requireNonNull(conversationId, "conversationId");
            return this;
        }

        public Builder withParticipantIds(List<String> participantIds) {
            this.participantIds = new HashSet<>(Objects.requireNonNull(participantIds, "participantIds"));
            return this;
        }

        public Builder withTemplateId(@Nullable String templateId) {
            this.templateId = templateId;
            return this;
        }

        public Builder withContext(Map<String, Object> context) {
            this.context = Objects.requireNonNull(context, "context");
            return this;
        }

        public Builder withStartTime(Instant startTime) {
            this.startTime = Objects.requireNonNull(startTime, "startTime");
            return this;
        }

        public Builder withState(ConversationState state) {
            this.state = Objects.requireNonNull(state, "state");
            return this;
        }

        public Builder withLastActivity(Instant lastActivity) {
            this.lastActivity = Objects.requireNonNull(lastActivity, "lastActivity");
            return this;
        }

        public Builder withMessageCount(int messageCount) {
            this.messageCount = messageCount;
            return this;
        }

        /**
         * Build the Conversation.
         *
         * @return the new Conversation
         */
        public Conversation build() {
            if (conversationId.isBlank()) {
                throw new IllegalArgumentException("conversationId must not be blank");
            }
            if (participantIds.isEmpty()) {
                throw new IllegalArgumentException("participantIds must not be empty");
            }
            if (messageCount < 0) {
                throw new IllegalArgumentException("messageCount must be non-negative");
            }
            return new Conversation(this);
        }
    }
}
