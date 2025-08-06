package org.openhab.core.ai.agent.conversation;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.agent.AgentRegistry;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Agent Conversation Service - Multi-turn agent conversation management
 * 
 * This service provides comprehensive conversation capabilities for agent interactions:
 * - Multi-turn agent conversations
 * - Conversation state management
 * - Conversation threading and context
 * - Conversation timeout handling
 * - Conversation history and persistence
 * - Conversation participant management
 * - Conversation templates and patterns
 * - Conversation analytics and metrics
 * - Conversation security and access control
 * - Conversation export and backup
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@Component(service = AgentConversationService.class)
@NonNullByDefault
public class AgentConversationService {

    private final Logger logger = LoggerFactory.getLogger(AgentConversationService.class);

    @Reference
    private @Nullable AgentRegistry agentRegistry;

    // Conversation storage and management
    private final Map<String, Conversation> activeConversations = new ConcurrentHashMap<>();
    private final Map<String, ConversationHistory> conversationHistories = new ConcurrentHashMap<>();
    private final Map<String, ConversationTemplate> conversationTemplates = new ConcurrentHashMap<>();
    private final Map<String, ConversationPattern> conversationPatterns = new ConcurrentHashMap<>();

    // Participant management
    private final Map<String, Set<String>> conversationParticipants = new ConcurrentHashMap<>();
    private final Map<String, ParticipantRole> participantRoles = new ConcurrentHashMap<>();

    // Analytics and metrics
    private final AtomicLong totalConversations = new AtomicLong(0);
    private final AtomicLong totalMessages = new AtomicLong(0);
    private final AtomicLong totalConversationTime = new AtomicLong(0);
    private final AtomicLong totalParticipants = new AtomicLong(0);

    // Configuration
    private final AtomicReference<ConversationConfiguration> configuration = new AtomicReference<>(
            new ConversationConfiguration());

    // Background processing
    private final ScheduledExecutorService timeoutProcessor = Executors.newSingleThreadScheduledExecutor();
    private final ScheduledExecutorService cleanupProcessor = Executors.newSingleThreadScheduledExecutor();
    private final ScheduledExecutorService analyticsProcessor = Executors.newSingleThreadScheduledExecutor();

    @Activate
    public void activate() {
        logger.info("Agent Conversation Service activated");

        // Start background processors
        timeoutProcessor.scheduleAtFixedRate(this::processTimeouts, 0, 1000, TimeUnit.MILLISECONDS);
        cleanupProcessor.scheduleAtFixedRate(this::cleanupExpiredConversations, 0, 300, TimeUnit.SECONDS);
        analyticsProcessor.scheduleAtFixedRate(this::updateAnalytics, 0, 60000, TimeUnit.MILLISECONDS);
    }

    @Deactivate
    public void deactivate() {
        logger.info("Agent Conversation Service deactivated");

        // Shutdown background processors
        shutdownExecutor(timeoutProcessor);
        shutdownExecutor(cleanupProcessor);
        shutdownExecutor(analyticsProcessor);
    }

    /**
     * Start a new conversation between agents
     * 
     * @param conversationId Unique conversation identifier
     * @param participantIds List of agent IDs participating in the conversation
     * @param templateId Optional conversation template ID
     * @param context Initial conversation context
     * @return Conversation instance
     */
    public CompletableFuture<Conversation> startConversation(String conversationId, List<String> participantIds,
            @Nullable String templateId, Map<String, Object> context) {
        logger.debug("Starting conversation: {} with participants: {}", conversationId, participantIds);

        // Validate participants exist
        AgentRegistry registry = agentRegistry;
        if (registry == null) {
            return CompletableFuture.failedFuture(new IllegalStateException("Agent registry not available"));
        }

        for (String agentId : participantIds) {
            if (registry.getAgent(agentId, "system") == null) {
                return CompletableFuture
                        .failedFuture(new IllegalArgumentException("Participant agent not found: " + agentId));
            }
        }

        // Create conversation
        Conversation conversation = Conversation.builder().conversationId(conversationId).participantIds(participantIds)
                .templateId(templateId).context(context).startTime(Instant.now()).state(ConversationState.ACTIVE)
                .build();

        // Apply template if specified
        if (templateId != null) {
            ConversationTemplate template = conversationTemplates.get(templateId);
            if (template != null) {
                conversation = template.applyTo(conversation);
            }
        }

        // Store conversation
        activeConversations.put(conversationId, conversation);
        conversationParticipants.put(conversationId, Set.copyOf(participantIds));
        totalConversations.incrementAndGet();
        totalParticipants.addAndGet(participantIds.size());

        // Initialize conversation history
        ConversationHistory history = new ConversationHistory(conversationId);
        conversationHistories.put(conversationId, history);

        logger.debug("Conversation started: {}", conversationId);
        return CompletableFuture.completedFuture(conversation);
    }

    /**
     * Send a message in a conversation
     * 
     * @param conversationId Conversation identifier
     * @param fromAgentId Source agent ID
     * @param message Message content
     * @param messageType Type of message
     * @return Message delivery result
     */
    public CompletableFuture<MessageDeliveryResult> sendMessage(String conversationId, String fromAgentId,
            String message, MessageType messageType) {
        logger.debug("Sending message in conversation {} from {}: {}", conversationId, fromAgentId, message);

        Conversation conversation = activeConversations.get(conversationId);
        if (conversation == null) {
            return CompletableFuture
                    .failedFuture(new IllegalArgumentException("Conversation not found: " + conversationId));
        }

        // Verify participant
        if (!conversation.getParticipantIds().contains(fromAgentId)) {
            return CompletableFuture.failedFuture(
                    new IllegalArgumentException("Agent not a participant in conversation: " + fromAgentId));
        }

        // Create conversation message
        ConversationMessage conversationMessage = ConversationMessage.builder().messageId(generateMessageId())
                .conversationId(conversationId).fromAgentId(fromAgentId).content(message).messageType(messageType)
                .timestamp(Instant.now()).build();

        // Add to conversation history
        ConversationHistory history = conversationHistories.get(conversationId);
        if (history != null) {
            history.addMessage(conversationMessage);
        }

        // Update conversation state
        conversation.setLastActivity(Instant.now());
        conversation.setMessageCount(conversation.getMessageCount() + 1);
        totalMessages.incrementAndGet();

        // Apply conversation patterns
        applyConversationPatterns(conversation, conversationMessage);

        // Notify other participants
        return notifyParticipants(conversation, conversationMessage);
    }

    /**
     * End a conversation
     * 
     * @param conversationId Conversation identifier
     * @param reason Reason for ending the conversation
     * @return Conversation end result
     */
    public CompletableFuture<ConversationEndResult> endConversation(String conversationId, String reason) {
        logger.debug("Ending conversation: {} with reason: {}", conversationId, reason);

        Conversation conversation = activeConversations.get(conversationId);
        if (conversation == null) {
            return CompletableFuture
                    .failedFuture(new IllegalArgumentException("Conversation not found: " + conversationId));
        }

        // Update conversation state
        conversation.setState(ConversationState.ENDED);
        conversation.setEndTime(Instant.now());
        conversation.setEndReason(reason);

        // Calculate conversation duration
        Duration duration = Duration.between(conversation.getStartTime(), conversation.getEndTime());
        totalConversationTime.addAndGet(duration.toMillis());

        // Archive conversation
        archiveConversation(conversation);

        // Clean up active conversation
        activeConversations.remove(conversationId);
        conversationParticipants.remove(conversationId);

        logger.debug("Conversation ended: {}", conversationId);
        return CompletableFuture
                .completedFuture(ConversationEndResult.success("Conversation ended successfully", duration));
    }

    /**
     * Get conversation by ID
     * 
     * @param conversationId Conversation identifier
     * @param requestingAgentId Agent requesting the conversation
     * @return Conversation or null if not found or access denied
     */
    public @Nullable Conversation getConversation(String conversationId, String requestingAgentId) {
        Conversation conversation = activeConversations.get(conversationId);
        if (conversation == null) {
            return null;
        }

        // Check access permissions
        if (!conversation.getParticipantIds().contains(requestingAgentId)) {
            logger.warn("Agent {} denied access to conversation: {}", requestingAgentId, conversationId);
            return null;
        }

        return conversation;
    }

    /**
     * Get conversation history
     * 
     * @param conversationId Conversation identifier
     * @param requestingAgentId Agent requesting the history
     * @return Conversation history or null if not found or access denied
     */
    public @Nullable ConversationHistory getConversationHistory(String conversationId, String requestingAgentId) {
        Conversation conversation = activeConversations.get(conversationId);
        if (conversation == null) {
            // Check archived conversations
            conversation = getArchivedConversation(conversationId);
        }

        if (conversation == null || !conversation.getParticipantIds().contains(requestingAgentId)) {
            return null;
        }

        return conversationHistories.get(conversationId);
    }

    /**
     * Add participant to conversation
     * 
     * @param conversationId Conversation identifier
     * @param agentId Agent ID to add
     * @param role Participant role
     * @return Addition result
     */
    public boolean addParticipant(String conversationId, String agentId, ParticipantRole role) {
        logger.debug("Adding participant {} to conversation {} with role: {}", agentId, conversationId, role);

        Conversation conversation = activeConversations.get(conversationId);
        if (conversation == null) {
            return false;
        }

        // Validate agent exists
        AgentRegistry registry = agentRegistry;
        if (registry == null || registry.getAgent(agentId, "system") == null) {
            return false;
        }

        // Add participant
        conversation.getParticipantIds().add(agentId);
        conversationParticipants.computeIfAbsent(conversationId, k -> ConcurrentHashMap.newKeySet()).add(agentId);
        participantRoles.put(conversationId + ":" + agentId, role);
        totalParticipants.incrementAndGet();

        return true;
    }

    /**
     * Remove participant from conversation
     * 
     * @param conversationId Conversation identifier
     * @param agentId Agent ID to remove
     * @return Removal result
     */
    public boolean removeParticipant(String conversationId, String agentId) {
        logger.debug("Removing participant {} from conversation {}", agentId, conversationId);

        Conversation conversation = activeConversations.get(conversationId);
        if (conversation == null) {
            return false;
        }

        // Remove participant
        boolean removed = conversation.getParticipantIds().remove(agentId);
        if (removed) {
            Set<String> participants = conversationParticipants.get(conversationId);
            if (participants != null) {
                participants.remove(agentId);
            }
            participantRoles.remove(conversationId + ":" + agentId);
        }

        return removed;
    }

    /**
     * Register a conversation template
     * 
     * @param templateId Template identifier
     * @param template Conversation template
     */
    public void registerConversationTemplate(String templateId, ConversationTemplate template) {
        conversationTemplates.put(templateId, template);
        logger.debug("Registered conversation template: {}", templateId);
    }

    /**
     * Register a conversation pattern
     * 
     * @param patternId Pattern identifier
     * @param pattern Conversation pattern
     */
    public void registerConversationPattern(String patternId, ConversationPattern pattern) {
        conversationPatterns.put(patternId, pattern);
        logger.debug("Registered conversation pattern: {}", patternId);
    }

    /**
     * Get conversation statistics
     * 
     * @return Conversation statistics
     */
    public ConversationStatistics getStatistics() {
        return new ConversationStatistics(totalConversations.get(), totalMessages.get(), totalConversationTime.get(),
                totalParticipants.get(), activeConversations.size(), conversationHistories.size(),
                conversationTemplates.size(), conversationPatterns.size());
    }

    /**
     * Export conversation data
     * 
     * @param conversationId Conversation identifier
     * @param requestingAgentId Agent requesting the export
     * @return Exported conversation data
     */
    public @Nullable ConversationExport exportConversation(String conversationId, String requestingAgentId) {
        Conversation conversation = getConversation(conversationId, requestingAgentId);
        if (conversation == null) {
            conversation = getArchivedConversation(conversationId);
        }

        if (conversation == null || !conversation.getParticipantIds().contains(requestingAgentId)) {
            return null;
        }

        ConversationHistory history = conversationHistories.get(conversationId);
        if (history == null) {
            return null;
        }

        return ConversationExport.builder().conversation(conversation).history(history).exportTime(Instant.now())
                .build();
    }

    // Private helper methods

    private String generateMessageId() {
        return "conv_msg_" + System.currentTimeMillis() + "_" + Thread.currentThread().getId();
    }

    private void applyConversationPatterns(Conversation conversation, ConversationMessage message) {
        // Apply registered conversation patterns
        for (ConversationPattern pattern : conversationPatterns.values()) {
            pattern.apply(conversation, message);
        }
    }

    private CompletableFuture<MessageDeliveryResult> notifyParticipants(Conversation conversation,
            ConversationMessage message) {
        // Notify all participants except the sender
        List<CompletableFuture<Boolean>> notifications = conversation.getParticipantIds().stream()
                .filter(agentId -> !agentId.equals(message.getFromAgentId()))
                .map(agentId -> notifyAgent(agentId, message)).toList();

        return CompletableFuture.allOf(notifications.toArray(new CompletableFuture[0])).thenApply(v -> {
            long successful = notifications.stream().mapToLong(future -> {
                try {
                    return future.get() ? 1 : 0;
                } catch (Exception e) {
                    return 0;
                }
            }).sum();

            return MessageDeliveryResult.success("Message delivered to " + successful + " participants");
        });
    }

    private CompletableFuture<Boolean> notifyAgent(String agentId, ConversationMessage message) {
        // Placeholder for agent notification
        // In a real implementation, this would send the message to the agent
        return CompletableFuture.completedFuture(true);
    }

    private void archiveConversation(Conversation conversation) {
        // Archive conversation for later retrieval
        // This is a placeholder for actual archiving logic
        logger.debug("Archiving conversation: {}", conversation.getConversationId());
    }

    private @Nullable Conversation getArchivedConversation(String conversationId) {
        // Retrieve archived conversation
        // This is a placeholder for actual archived conversation retrieval
        return null;
    }

    private void processTimeouts() {
        Instant now = Instant.now();
        Duration timeout = configuration.get().getConversationTimeout();

        activeConversations.entrySet().removeIf(entry -> {
            Conversation conversation = entry.getValue();
            if (conversation.getLastActivity().plus(timeout).isBefore(now)) {
                logger.debug("Conversation timed out: {}", conversation.getConversationId());
                endConversation(conversation.getConversationId(), "Timeout");
                return true;
            }
            return false;
        });
    }

    private void cleanupExpiredConversations() {
        Instant cutoff = Instant.now().minus(Duration.ofDays(30));

        conversationHistories.entrySet().removeIf(entry -> {
            ConversationHistory history = entry.getValue();
            return history.getLastActivity().isBefore(cutoff);
        });
    }

    private void updateAnalytics() {
        // Update analytics and metrics
        // This is a placeholder for actual analytics processing
    }

    private void shutdownExecutor(ScheduledExecutorService executor) {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    // Inner classes and interfaces

    /**
     * Conversation data
     */
    public static class Conversation {
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

        private Conversation(Builder builder) {
            this.conversationId = builder.conversationId;
            this.participantIds = builder.participantIds;
            this.templateId = builder.templateId;
            this.context = builder.context;
            this.startTime = builder.startTime;
            this.state = builder.state;
            this.lastActivity = builder.lastActivity;
            this.messageCount = builder.messageCount;
        }

        // Getters and setters
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

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private String conversationId;
            private Set<String> participantIds;
            private @Nullable String templateId;
            private Map<String, Object> context;
            private Instant startTime;
            private ConversationState state;
            private Instant lastActivity;
            private int messageCount;

            public Builder conversationId(String conversationId) {
                this.conversationId = conversationId;
                return this;
            }

            public Builder participantIds(List<String> participantIds) {
                this.participantIds = Set.copyOf(participantIds);
                return this;
            }

            public Builder templateId(@Nullable String templateId) {
                this.templateId = templateId;
                return this;
            }

            public Builder context(Map<String, Object> context) {
                this.context = context;
                return this;
            }

            public Builder startTime(Instant startTime) {
                this.startTime = startTime;
                return this;
            }

            public Builder state(ConversationState state) {
                this.state = state;
                return this;
            }

            public Builder lastActivity(Instant lastActivity) {
                this.lastActivity = lastActivity;
                return this;
            }

            public Builder messageCount(int messageCount) {
                this.messageCount = messageCount;
                return this;
            }

            public Conversation build() {
                return new Conversation(this);
            }
        }
    }

    /**
     * Conversation message
     */
    public static class ConversationMessage {
        private final String messageId;
        private final String conversationId;
        private final String fromAgentId;
        private final String content;
        private final MessageType messageType;
        private final Instant timestamp;

        private ConversationMessage(Builder builder) {
            this.messageId = builder.messageId;
            this.conversationId = builder.conversationId;
            this.fromAgentId = builder.fromAgentId;
            this.content = builder.content;
            this.messageType = builder.messageType;
            this.timestamp = builder.timestamp;
        }

        // Getters
        public String getMessageId() {
            return messageId;
        }

        public String getConversationId() {
            return conversationId;
        }

        public String getFromAgentId() {
            return fromAgentId;
        }

        public String getContent() {
            return content;
        }

        public MessageType getMessageType() {
            return messageType;
        }

        public Instant getTimestamp() {
            return timestamp;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private String messageId;
            private String conversationId;
            private String fromAgentId;
            private String content;
            private MessageType messageType;
            private Instant timestamp;

            public Builder messageId(String messageId) {
                this.messageId = messageId;
                return this;
            }

            public Builder conversationId(String conversationId) {
                this.conversationId = conversationId;
                return this;
            }

            public Builder fromAgentId(String fromAgentId) {
                this.fromAgentId = fromAgentId;
                return this;
            }

            public Builder content(String content) {
                this.content = content;
                return this;
            }

            public Builder messageType(MessageType messageType) {
                this.messageType = messageType;
                return this;
            }

            public Builder timestamp(Instant timestamp) {
                this.timestamp = timestamp;
                return this;
            }

            public ConversationMessage build() {
                return new ConversationMessage(this);
            }
        }
    }

    /**
     * Conversation history
     */
    public static class ConversationHistory {
        private final String conversationId;
        private final List<ConversationMessage> messages;
        private Instant lastActivity;

        public ConversationHistory(String conversationId) {
            this.conversationId = conversationId;
            this.messages = new java.util.concurrent.CopyOnWriteArrayList<>();
            this.lastActivity = Instant.now();
        }

        public void addMessage(ConversationMessage message) {
            messages.add(message);
            lastActivity = Instant.now();
        }

        // Getters
        public String getConversationId() {
            return conversationId;
        }

        public List<ConversationMessage> getMessages() {
            return List.copyOf(messages);
        }

        public Instant getLastActivity() {
            return lastActivity;
        }
    }

    /**
     * Conversation export
     */
    public static class ConversationExport {
        private final Conversation conversation;
        private final ConversationHistory history;
        private final Instant exportTime;

        private ConversationExport(Builder builder) {
            this.conversation = builder.conversation;
            this.history = builder.history;
            this.exportTime = builder.exportTime;
        }

        // Getters
        public Conversation getConversation() {
            return conversation;
        }

        public ConversationHistory getHistory() {
            return history;
        }

        public Instant getExportTime() {
            return exportTime;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private Conversation conversation;
            private ConversationHistory history;
            private Instant exportTime;

            public Builder conversation(Conversation conversation) {
                this.conversation = conversation;
                return this;
            }

            public Builder history(ConversationHistory history) {
                this.history = history;
                return this;
            }

            public Builder exportTime(Instant exportTime) {
                this.exportTime = exportTime;
                return this;
            }

            public ConversationExport build() {
                return new ConversationExport(this);
            }
        }
    }

    /**
     * Conversation statistics
     */
    public static class ConversationStatistics {
        private final long totalConversations;
        private final long totalMessages;
        private final long totalConversationTime;
        private final long totalParticipants;
        private final int activeConversations;
        private final int conversationHistories;
        private final int conversationTemplates;
        private final int conversationPatterns;

        public ConversationStatistics(long totalConversations, long totalMessages, long totalConversationTime,
                long totalParticipants, int activeConversations, int conversationHistories, int conversationTemplates,
                int conversationPatterns) {
            this.totalConversations = totalConversations;
            this.totalMessages = totalMessages;
            this.totalConversationTime = totalConversationTime;
            this.totalParticipants = totalParticipants;
            this.activeConversations = activeConversations;
            this.conversationHistories = conversationHistories;
            this.conversationTemplates = conversationTemplates;
            this.conversationPatterns = conversationPatterns;
        }

        // Getters
        public long getTotalConversations() {
            return totalConversations;
        }

        public long getTotalMessages() {
            return totalMessages;
        }

        public long getTotalConversationTime() {
            return totalConversationTime;
        }

        public long getTotalParticipants() {
            return totalParticipants;
        }

        public int getActiveConversations() {
            return activeConversations;
        }

        public int getConversationHistories() {
            return conversationHistories;
        }

        public int getConversationTemplates() {
            return conversationTemplates;
        }

        public int getConversationPatterns() {
            return conversationPatterns;
        }
    }

    /**
     * Conversation configuration
     */
    public static class ConversationConfiguration {
        private Duration conversationTimeout = Duration.ofMinutes(30);
        private Duration historyRetentionPeriod = Duration.ofDays(30);
        private int maxParticipants = 10;
        private int maxMessageLength = 1000;
        private boolean enableAnalytics = true;
        private boolean enableExport = true;

        // Getters and setters
        public Duration getConversationTimeout() {
            return conversationTimeout;
        }

        public void setConversationTimeout(Duration conversationTimeout) {
            this.conversationTimeout = conversationTimeout;
        }

        public Duration getHistoryRetentionPeriod() {
            return historyRetentionPeriod;
        }

        public void setHistoryRetentionPeriod(Duration historyRetentionPeriod) {
            this.historyRetentionPeriod = historyRetentionPeriod;
        }

        public int getMaxParticipants() {
            return maxParticipants;
        }

        public void setMaxParticipants(int maxParticipants) {
            this.maxParticipants = maxParticipants;
        }

        public int getMaxMessageLength() {
            return maxMessageLength;
        }

        public void setMaxMessageLength(int maxMessageLength) {
            this.maxMessageLength = maxMessageLength;
        }

        public boolean isEnableAnalytics() {
            return enableAnalytics;
        }

        public void setEnableAnalytics(boolean enableAnalytics) {
            this.enableAnalytics = enableAnalytics;
        }

        public boolean isEnableExport() {
            return enableExport;
        }

        public void setEnableExport(boolean enableExport) {
            this.enableExport = enableExport;
        }
    }

    // Enums
    public enum ConversationState {
        ACTIVE,
        PAUSED,
        ENDED,
        ARCHIVED
    }

    public enum MessageType {
        TEXT,
        COMMAND,
        QUERY,
        RESPONSE,
        NOTIFICATION,
        SYSTEM
    }

    public enum ParticipantRole {
        HOST,
        PARTICIPANT,
        OBSERVER,
        MODERATOR
    }

    // Interfaces
    public interface ConversationTemplate {
        Conversation applyTo(Conversation conversation);
    }

    public interface ConversationPattern {
        void apply(Conversation conversation, ConversationMessage message);
    }

    public interface MessageDeliveryResult {
        boolean isSuccess();

        String getMessage();

        static MessageDeliveryResult success(String message) {
            return new MessageDeliveryResult() {
                @Override
                public boolean isSuccess() {
                    return true;
                }

                @Override
                public String getMessage() {
                    return message;
                }
            };
        }

        static MessageDeliveryResult failure(String message) {
            return new MessageDeliveryResult() {
                @Override
                public boolean isSuccess() {
                    return false;
                }

                @Override
                public String getMessage() {
                    return message;
                }
            };
        }
    }

    public interface ConversationEndResult {
        boolean isSuccess();

        String getMessage();

        Duration getDuration();

        static ConversationEndResult success(String message, Duration duration) {
            return new ConversationEndResult() {
                @Override
                public boolean isSuccess() {
                    return true;
                }

                @Override
                public String getMessage() {
                    return message;
                }

                @Override
                public Duration getDuration() {
                    return duration;
                }
            };
        }

        static ConversationEndResult failure(String message) {
            return new ConversationEndResult() {
                @Override
                public boolean isSuccess() {
                    return false;
                }

                @Override
                public String getMessage() {
                    return message;
                }

                @Override
                public Duration getDuration() {
                    return Duration.ZERO;
                }
            };
        }
    }
}
