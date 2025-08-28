package org.openhab.core.ai.agent.communication.conversation;

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
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.agent.communication.conversation.api.ConversationEndResult;
import org.openhab.core.ai.agent.communication.conversation.api.ConversationPattern;
import org.openhab.core.ai.agent.communication.conversation.api.ConversationTemplate;
import org.openhab.core.ai.agent.lifecycle.api.AgentRegistry;
import org.openhab.core.ai.common.communication.MessageDeliveryResult;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
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

    @Reference
    private @Nullable MetricsService metricsService;

    // Conversation storage and management
    private final Map<String, Conversation> activeConversations = new ConcurrentHashMap<>();
    private final Map<String, ConversationHistory> conversationHistories = new ConcurrentHashMap<>();
    private final Map<String, ConversationTemplate> conversationTemplates = new ConcurrentHashMap<>();
    private final Map<String, ConversationPattern> conversationPatterns = new ConcurrentHashMap<>();

    // Participant management
    private final Map<String, Set<String>> conversationParticipants = new ConcurrentHashMap<>();
    private final Map<String, ParticipantRole> participantRoles = new ConcurrentHashMap<>();

    // Configuration
    private final AtomicReference<ConversationConfiguration> configuration = new AtomicReference<>(
            new ConversationConfiguration());

    // Background processing
    private final ScheduledExecutorService timeoutProcessor = Executors.newSingleThreadScheduledExecutor();
    private final ScheduledExecutorService cleanupProcessor = Executors.newSingleThreadScheduledExecutor();
    private final ScheduledExecutorService analyticsProcessor = Executors.newSingleThreadScheduledExecutor();

    @Activate
    public void activate() {
        try {
            logger.info("Agent Conversation Service activated");

            // Start background processors with error handling
            try {
                timeoutProcessor.scheduleAtFixedRate(this::processTimeouts, 0, 1000, TimeUnit.MILLISECONDS);
                logger.debug("Timeout processor started");
            } catch (Exception e) {
                logger.error("Error starting timeout processor: {}", e.getMessage(), e);
            }

            try {
                cleanupProcessor.scheduleAtFixedRate(this::cleanupExpiredConversations, 0, 300, TimeUnit.SECONDS);
                logger.debug("Cleanup processor started");
            } catch (Exception e) {
                logger.error("Error starting cleanup processor: {}", e.getMessage(), e);
            }

            try {
                analyticsProcessor.scheduleAtFixedRate(this::updateAnalytics, 0, 60000, TimeUnit.MILLISECONDS);
                logger.debug("Analytics processor started");
            } catch (Exception e) {
                logger.error("Error starting analytics processor: {}", e.getMessage(), e);
            }

            recordMetrics("agent-conversation", "service-activated", true, Duration.ZERO);

        } catch (Exception e) {
            logger.error("Error during Agent Conversation Service activation: {}", e.getMessage(), e);
            recordMetrics("agent-conversation", "service-activated", false, Duration.ZERO);
            // Continue activation even if processors fail to start
        }
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

        // Input validation
        if (conversationId == null || conversationId.trim().isEmpty()) {
            logger.warn("Cannot start conversation: conversation ID is null or empty");
            recordMetrics("agent-conversation", "conversation-validation-failed", false, Duration.ZERO);
            return CompletableFuture
                    .failedFuture(new IllegalArgumentException("Conversation ID cannot be null or empty"));
        }

        if (participantIds == null || participantIds.isEmpty()) {
            logger.warn("Cannot start conversation: participant list is null or empty for conversation: {}",
                    conversationId);
            recordMetrics("agent-conversation", "conversation-validation-failed", false, Duration.ZERO);
            return CompletableFuture
                    .failedFuture(new IllegalArgumentException("Participant list cannot be null or empty"));
        }

        if (context == null) {
            logger.warn("Cannot start conversation: context is null for conversation: {}", conversationId);
            recordMetrics("agent-conversation", "conversation-validation-failed", false, Duration.ZERO);
            return CompletableFuture.failedFuture(new IllegalArgumentException("Context cannot be null"));
        }

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
        Conversation conversation = Conversation.builder().withConversationId(conversationId)
                .withParticipantIds(participantIds).withTemplateId(templateId).withContext(context)
                .withStartTime(Instant.now()).withState(ConversationState.ACTIVE).build();

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
        recordMetrics("agent-conversation", "conversation-started", true, Duration.ZERO);
        recordMetrics("agent-conversation", "participants-added", true, Duration.ZERO);

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
            String message, ConversationMessageType messageType) {
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
        ConversationMessage conversationMessage = ConversationMessage.builder().withMessageId(generateMessageId())
                .withConversationId(conversationId).withFromAgentId(fromAgentId).withContent(message)
                .withMessageType(messageType).withTimestamp(Instant.now()).build();

        // Add to conversation history
        ConversationHistory history = conversationHistories.get(conversationId);
        if (history != null) {
            history.addMessage(conversationMessage);
        }

        // Update conversation state
        conversation.setLastActivity(Instant.now());
        conversation.setMessageCount(conversation.getMessageCount() + 1);
        recordMetrics("agent-conversation", "message-sent", true, Duration.ZERO);

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
        recordMetrics("agent-conversation", "conversation-ended", true, duration);

        // Archive conversation
        archiveConversation(conversation);

        // Clean up active conversation
        activeConversations.remove(conversationId);
        conversationParticipants.remove(conversationId);
        recordMetrics("agent-conversation", "conversation-removed", true, Duration.ZERO);

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
        recordMetrics("agent-conversation", "participant-added", true, Duration.ZERO);

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
            recordMetrics("agent-conversation", "participant-removed", true, Duration.ZERO);
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
        MetricsService metrics = metricsService;
        if (metrics == null) {
            logger.warn("MetricsService not available, returning empty statistics");
            return new ConversationStatistics(0, 0, 0, 0, activeConversations.size(), conversationHistories.size(),
                    conversationTemplates.size(), conversationPatterns.size());
        }

        // For now, return basic statistics since we need to implement proper snapshot retrieval
        // TODO: Implement proper snapshot retrieval from MetricsService when domain-specific snapshots are available
        return new ConversationStatistics(0, // totalConversations - will be retrieved from snapshots
                0, // totalMessages - will be retrieved from snapshots
                0, // totalConversationTime - will be retrieved from snapshots
                0, // totalParticipants - will be retrieved from snapshots
                activeConversations.size(), conversationHistories.size(), conversationTemplates.size(),
                conversationPatterns.size());
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

    private final Map<String, Conversation> archivedConversations = new ConcurrentHashMap<>();
    private final Map<String, ConversationAnalytics> conversationAnalytics = new ConcurrentHashMap<>();

    private void archiveConversation(Conversation conversation) {
        try {
            logger.debug("Archiving conversation: {}", conversation.getConversationId());

            // Store in archived conversations
            archivedConversations.put(conversation.getConversationId(), conversation);

            // Create analytics for the conversation
            ConversationAnalytics analytics = createConversationAnalytics(conversation);
            conversationAnalytics.put(conversation.getConversationId(), analytics);

            // Log archiving event
            logger.info("Conversation archived: {} with {} messages, duration: {}ms", conversation.getConversationId(),
                    conversation.getMessageCount(),
                    conversation.getEndTime() != null
                            ? Duration.between(conversation.getStartTime(), conversation.getEndTime()).toMillis()
                            : 0);

        } catch (Exception e) {
            logger.error("Error archiving conversation {}: {}", conversation.getConversationId(), e.getMessage(), e);
        }
    }

    private @Nullable Conversation getArchivedConversation(String conversationId) {
        try {
            return archivedConversations.get(conversationId);
        } catch (Exception e) {
            logger.error("Error retrieving archived conversation {}: {}", conversationId, e.getMessage(), e);
            return null;
        }
    }

    private ConversationAnalytics createConversationAnalytics(Conversation conversation) {
        try {
            ConversationHistory history = conversationHistories.get(conversation.getConversationId());

            // Calculate analytics
            long duration = conversation.getEndTime() != null
                    ? Duration.between(conversation.getStartTime(), conversation.getEndTime()).toMillis()
                    : 0;

            int participantCount = conversation.getParticipantIds().size();
            int messageCount = conversation.getMessageCount();

            // Calculate message frequency
            double messagesPerMinute = duration > 0 ? (messageCount * 60000.0) / duration : 0.0;

            // Calculate participant activity
            Map<String, Integer> participantActivity = new ConcurrentHashMap<>();
            if (history != null) {
                for (ConversationMessage message : history.getMessages()) {
                    participantActivity.merge(message.getFromAgentId(), 1, Integer::sum);
                }
            }

            return new ConversationAnalytics(conversation.getConversationId(), conversation.getStartTime(),
                    conversation.getEndTime(), duration, participantCount, messageCount, messagesPerMinute,
                    participantActivity, conversation.getState(), conversation.getEndReason());

        } catch (Exception e) {
            logger.error("Error creating conversation analytics for {}: {}", conversation.getConversationId(),
                    e.getMessage(), e);
            return null;
        }
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
        try {
            logger.debug("Updating conversation analytics");

            // Process analytics for active conversations
            for (Conversation conversation : activeConversations.values()) {
                // Update real-time analytics for active conversations
                updateRealTimeAnalytics(conversation);
            }

            // Process analytics for archived conversations
            for (ConversationAnalytics analytics : conversationAnalytics.values()) {
                // Update historical analytics
                updateHistoricalAnalytics(analytics);
            }

            // Generate system-wide analytics
            generateSystemAnalytics();

        } catch (Exception e) {
            logger.error("Error updating analytics: {}", e.getMessage(), e);
        }
    }

    private void updateRealTimeAnalytics(Conversation conversation) {
        try {
            // Calculate real-time metrics for active conversations
            long currentDuration = Duration.between(conversation.getStartTime(), Instant.now()).toMillis();
            double currentMessagesPerMinute = currentDuration > 0
                    ? (conversation.getMessageCount() * 60000.0) / currentDuration
                    : 0.0;

            logger.debug("Real-time analytics for conversation {}: duration={}ms, messagesPerMinute={:.2f}",
                    conversation.getConversationId(), currentDuration, currentMessagesPerMinute);

        } catch (Exception e) {
            logger.error("Error updating real-time analytics for conversation {}: {}", conversation.getConversationId(),
                    e.getMessage());
        }
    }

    private void updateHistoricalAnalytics(ConversationAnalytics analytics) {
        try {
            // Process historical analytics data
            // This could include trend analysis, pattern recognition, etc.
            logger.debug("Historical analytics for conversation {}: {} messages, {} participants, {:.2f} msgs/min",
                    analytics.getConversationId(), analytics.getMessageCount(), analytics.getParticipantCount(),
                    analytics.getMessagesPerMinute());

        } catch (Exception e) {
            logger.error("Error updating historical analytics for conversation {}: {}", analytics.getConversationId(),
                    e.getMessage());
        }
    }

    private void generateSystemAnalytics() {
        try {
            // Generate system-wide analytics
            long totalActiveConversations = activeConversations.size();
            long totalArchivedConversations = archivedConversations.size();
            long totalAnalytics = conversationAnalytics.size();

            // Calculate average conversation metrics
            // TODO: Implement proper metrics retrieval when domain-specific snapshots are available
            double avgMessagesPerConversation = 0.0;
            double avgConversationDuration = 0.0;

            logger.info(
                    "System analytics: active={}, archived={}, analytics={}, avgMessages={:.2f}, avgDuration={:.2f}ms",
                    totalActiveConversations, totalArchivedConversations, totalAnalytics, avgMessagesPerConversation,
                    avgConversationDuration);

        } catch (Exception e) {
            logger.error("Error generating system analytics: {}", e.getMessage());
        }
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

    private void recordMetrics(String domain, String operation, boolean success, Duration duration) {
        try {
            if (domain == null || domain.trim().isEmpty()) {
                logger.warn("Cannot record metrics: domain is null or empty for operation: {}", operation);
                return;
            }

            if (operation == null || operation.trim().isEmpty()) {
                logger.warn("Cannot record metrics: operation is null or empty for domain: {}", domain);
                return;
            }

            if (duration == null) {
                logger.warn("Cannot record metrics: duration is null for {}.{}", domain, operation);
                return;
            }

            MetricsService metrics = metricsService;
            if (metrics != null) {
                try {
                    metrics.recordOperation(domain, operation, success, duration);
                } catch (Exception e) {
                    logger.error("Failed to record metrics for {}.{}: {}", domain, operation, e.getMessage(), e);
                }
            } else {
                logger.debug("MetricsService not available, cannot record metrics for operation: {} - {}", domain,
                        operation);
            }
        } catch (Exception e) {
            // Prevent recursive error recording
            logger.error("Error in metrics recording helper for {}.{}: {}", domain, operation, e.getMessage());
        }
    }

    // Inner classes and interfaces extracted to top-level types in this package
}
