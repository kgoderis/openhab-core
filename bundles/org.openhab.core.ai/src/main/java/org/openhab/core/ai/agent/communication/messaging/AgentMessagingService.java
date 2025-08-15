package org.openhab.core.ai.agent.communication.messaging;

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
import org.openhab.core.ai.agent.communication.messaging.api.BroadcastResult;
import org.openhab.core.ai.agent.communication.messaging.api.MessageDeliveryResult;
import org.openhab.core.ai.agent.communication.messaging.api.MessageFilter;
import org.openhab.core.ai.agent.communication.messaging.api.MessageRouter;
import org.openhab.core.ai.agent.communication.messaging.api.MessageValidator;
import org.openhab.core.ai.agent.lifecycle.api.AgentRegistry;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.a2a.spec.Message;
import io.a2a.spec.Part;
import io.a2a.spec.TextPart;

/**
 * Agent Messaging Service - Advanced inter-agent messaging system
 * 
 * This service provides comprehensive messaging capabilities for agent communication:
 * - Direct message passing between agents using A2A SDK Message class
 * - Message routing and delivery
 * - Message acknowledgment system
 * - Message priority handling
 * - Message filtering and validation
 * - Message persistence and replay
 * - Message encryption and security
 * - Message performance monitoring
 * - Message retry mechanisms
 * - Message broadcasting capabilities
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@Component(service = AgentMessagingService.class)
@NonNullByDefault
public class AgentMessagingService {

    private final Logger logger = LoggerFactory.getLogger(AgentMessagingService.class);

    @Reference
    private @Nullable AgentRegistry agentRegistry;

    // Message storage and routing using A2A SDK Message class
    private final Map<String, Message> messageStore = new ConcurrentHashMap<>();
    private final Map<String, MessageDeliveryStatus> deliveryStatus = new ConcurrentHashMap<>();
    private final Map<String, MessageAcknowledgment> acknowledgments = new ConcurrentHashMap<>();
    private final Map<String, MessageRetryInfo> retryInfo = new ConcurrentHashMap<>();

    // Message routing and filtering
    private final Map<String, MessageFilter> messageFilters = new ConcurrentHashMap<>();
    private final Map<String, MessageValidator> messageValidators = new ConcurrentHashMap<>();
    private final Map<String, MessageRouter> messageRouters = new ConcurrentHashMap<>();

    // Broadcasting and subscriptions
    private final Map<String, Set<String>> broadcastSubscriptions = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> topicSubscriptions = new ConcurrentHashMap<>();

    // Security and encryption
    private final MessageEncryptionService encryptionService = new MessageEncryptionService();
    private final MessageSecurityManager securityManager = new MessageSecurityManager();

    // Performance monitoring
    private final AtomicLong totalMessagesSent = new AtomicLong(0);
    private final AtomicLong totalMessagesDelivered = new AtomicLong(0);
    private final AtomicLong totalMessagesAcknowledged = new AtomicLong(0);
    private final AtomicLong totalMessagesFailed = new AtomicLong(0);
    private final AtomicLong totalBroadcastMessages = new AtomicLong(0);

    // Configuration
    private final AtomicReference<MessagingConfiguration> configuration = new AtomicReference<>(
            new MessagingConfiguration());

    // Background processing
    private final ScheduledExecutorService messageProcessor = Executors.newSingleThreadScheduledExecutor();
    private final ScheduledExecutorService retryProcessor = Executors.newSingleThreadScheduledExecutor();
    private final ScheduledExecutorService cleanupProcessor = Executors.newSingleThreadScheduledExecutor();

    @Activate
    public void activate() {
        logger.info("Agent Messaging Service activated");

        // Start background processors
        messageProcessor.scheduleAtFixedRate(this::processMessageQueue, 0, 100, TimeUnit.MILLISECONDS);
        retryProcessor.scheduleAtFixedRate(this::processRetryQueue, 0, 1000, TimeUnit.MILLISECONDS);
        cleanupProcessor.scheduleAtFixedRate(this::cleanupExpiredMessages, 0, 300, TimeUnit.SECONDS);
    }

    @Deactivate
    public void deactivate() {
        logger.info("Agent Messaging Service deactivated");

        // Shutdown background processors
        shutdownExecutor(messageProcessor);
        shutdownExecutor(retryProcessor);
        shutdownExecutor(cleanupProcessor);
    }

    /**
     * Send a message from one agent to another using A2A SDK Message class
     * 
     * @param fromAgentId Source agent ID
     * @param toAgentId Destination agent ID
     * @param content Message content
     * @param priority Message priority
     * @param options Message options
     * @return Message delivery result
     */
    public CompletableFuture<MessageDeliveryResult> sendMessage(String fromAgentId, String toAgentId, String content,
            MessagePriority priority, MessageOptions options) {
        logger.debug("Sending message from {} to {}: {}", fromAgentId, toAgentId, content);

        // Validate agents exist
        AgentRegistry registry = agentRegistry;
        if (registry == null) {
            return CompletableFuture.failedFuture(new IllegalStateException("Agent registry not available"));
        }

        if (registry.getAgent(fromAgentId, "system") == null) {
            return CompletableFuture
                    .failedFuture(new IllegalArgumentException("Source agent not found: " + fromAgentId));
        }

        if (registry.getAgent(toAgentId, "system") == null) {
            return CompletableFuture
                    .failedFuture(new IllegalArgumentException("Destination agent not found: " + toAgentId));
        }

        // Create A2A SDK Message
        String messageId = generateMessageId();
        List<Part<?>> parts = List.of(new TextPart(content, null));
        Map<String, Object> metadata = createMessageMetadata(fromAgentId, toAgentId, priority, options);

        Message message = new Message(Message.Role.AGENT, parts, null, null, null, null, metadata);

        // Validate message
        if (!validateMessage(message)) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("Message validation failed"));
        }

        // Apply filters
        if (!applyMessageFilters(message)) {
            return CompletableFuture.completedFuture(MessageDeliveryResult.filtered("Message filtered out"));
        }

        // Encrypt message if required
        if (options.isEncrypted()) {
            message = encryptionService.encryptMessage(message);
        }

        // Store message
        messageStore.put(messageId, message);
        totalMessagesSent.incrementAndGet();

        // Route message
        return routeMessage(message, messageId);
    }

    /**
     * Broadcast a message to multiple agents using A2A SDK Message class
     * 
     * @param fromAgentId Source agent ID
     * @param topic Broadcast topic
     * @param content Message content
     * @param priority Message priority
     * @param options Message options
     * @return Broadcast result
     */
    public CompletableFuture<BroadcastResult> broadcastMessage(String fromAgentId, String topic, String content,
            MessagePriority priority, MessageOptions options) {
        logger.debug("Broadcasting message from {} to topic {}: {}", fromAgentId, topic, content);

        // Get subscribers
        Set<String> subscribers = topicSubscriptions.getOrDefault(topic, Set.of());
        if (subscribers.isEmpty()) {
            return CompletableFuture
                    .completedFuture(BroadcastResult.noSubscribers("No subscribers for topic: " + topic));
        }

        // Create broadcast message using A2A SDK
        String messageId = generateMessageId();
        List<Part<?>> parts = List.of(new TextPart(content, null));
        Map<String, Object> metadata = createMessageMetadata(fromAgentId, "BROADCAST", priority, options);
        metadata.put("topic", topic);

        Message broadcastMessage = new Message(Message.Role.AGENT, parts, null, null, null, null, metadata);

        // Store broadcast message
        messageStore.put(messageId, broadcastMessage);
        totalBroadcastMessages.incrementAndGet();

        // Send to all subscribers
        List<CompletableFuture<MessageDeliveryResult>> deliveries = subscribers.stream()
                .map(subscriberId -> sendMessage(fromAgentId, subscriberId, content, priority, options)).toList();

        return CompletableFuture.allOf(deliveries.toArray(new CompletableFuture[0])).thenApply(v -> {
            long successful = deliveries.stream().mapToLong(future -> {
                try {
                    return future.get().isSuccess() ? 1 : 0;
                } catch (Exception e) {
                    return 0;
                }
            }).sum();

            return BroadcastResult.success(successful, subscribers.size());
        });
    }

    /**
     * Subscribe to a broadcast topic
     * 
     * @param agentId Agent ID to subscribe
     * @param topic Topic to subscribe to
     * @return Subscription result
     */
    public boolean subscribeToTopic(String agentId, String topic) {
        logger.debug("Agent {} subscribing to topic: {}", agentId, topic);

        // Validate agent exists
        AgentRegistry registry = agentRegistry;
        if (registry == null || registry.getAgent(agentId, "system") == null) {
            logger.warn("Cannot subscribe agent {} to topic {}: agent not found", agentId, topic);
            return false;
        }

        topicSubscriptions.computeIfAbsent(topic, k -> ConcurrentHashMap.newKeySet()).add(agentId);
        return true;
    }

    /**
     * Unsubscribe from a broadcast topic
     * 
     * @param agentId Agent ID to unsubscribe
     * @param topic Topic to unsubscribe from
     * @return Unsubscription result
     */
    public boolean unsubscribeFromTopic(String agentId, String topic) {
        logger.debug("Agent {} unsubscribing from topic: {}", agentId, topic);

        Set<String> subscribers = topicSubscriptions.get(topic);
        if (subscribers != null) {
            return subscribers.remove(agentId);
        }
        return false;
    }

    /**
     * Acknowledge message receipt
     * 
     * @param messageId Message ID to acknowledge
     * @param agentId Agent acknowledging the message
     * @param acknowledgmentType Type of acknowledgment
     * @return Acknowledgment result
     */
    public boolean acknowledgeMessage(String messageId, String agentId, AcknowledgmentType acknowledgmentType) {
        logger.debug("Agent {} acknowledging message {} with type: {}", agentId, messageId, acknowledgmentType);

        Message message = messageStore.get(messageId);
        if (message == null) {
            logger.warn("Cannot acknowledge message {}: message not found", messageId);
            return false;
        }

        // Verify agent is the intended recipient
        Map<String, Object> metadata = message.getMetadata();
        String toAgentId = metadata != null ? (String) metadata.get("toAgentId") : null;
        if (toAgentId == null || (!toAgentId.equals(agentId) && !toAgentId.equals("BROADCAST"))) {
            logger.warn("Agent {} cannot acknowledge message {}: not the intended recipient", agentId, messageId);
            return false;
        }

        // Create acknowledgment
        MessageAcknowledgment acknowledgment = MessageAcknowledgment.builder().messageId(messageId).agentId(agentId)
                .acknowledgmentType(acknowledgmentType).timestamp(Instant.now()).build();

        acknowledgments.put(messageId + ":" + agentId, acknowledgment);
        totalMessagesAcknowledged.incrementAndGet();

        // Update delivery status
        deliveryStatus.put(messageId, MessageDeliveryStatus.ACKNOWLEDGED);

        return true;
    }

    /**
     * Get message delivery status
     * 
     * @param messageId Message ID
     * @return Delivery status
     */
    public @Nullable MessageDeliveryStatus getMessageDeliveryStatus(String messageId) {
        return deliveryStatus.get(messageId);
    }

    /**
     * Get message acknowledgments
     * 
     * @param messageId Message ID
     * @return List of acknowledgments
     */
    public List<MessageAcknowledgment> getMessageAcknowledgments(String messageId) {
        return acknowledgments.entrySet().stream().filter(entry -> entry.getKey().startsWith(messageId + ":"))
                .map(Map.Entry::getValue).toList();
    }

    /**
     * Get messaging statistics
     * 
     * @return Messaging statistics
     */
    public MessagingStatistics getStatistics() {
        return new MessagingStatistics(totalMessagesSent.get(), totalMessagesDelivered.get(),
                totalMessagesAcknowledged.get(), totalMessagesFailed.get(), totalBroadcastMessages.get(),
                messageStore.size(), deliveryStatus.size(), acknowledgments.size(), topicSubscriptions.size());
    }

    /**
     * Register a message filter
     * 
     * @param filterId Filter identifier
     * @param filter Message filter implementation
     */
    public void registerMessageFilter(String filterId, MessageFilter filter) {
        messageFilters.put(filterId, filter);
        logger.debug("Registered message filter: {}", filterId);
    }

    /**
     * Register a message validator
     * 
     * @param validatorId Validator identifier
     * @param validator Message validator implementation
     */
    public void registerMessageValidator(String validatorId, MessageValidator validator) {
        messageValidators.put(validatorId, validator);
        logger.debug("Registered message validator: {}", validatorId);
    }

    /**
     * Register a message router
     * 
     * @param routerId Router identifier
     * @param router Message router implementation
     */
    public void registerMessageRouter(String routerId, MessageRouter router) {
        messageRouters.put(routerId, router);
        logger.debug("Registered message router: {}", routerId);
    }

    // Private helper methods

    private String generateMessageId() {
        return "msg_" + System.currentTimeMillis() + "_" + Thread.currentThread().getId();
    }

    private Map<String, Object> createMessageMetadata(String fromAgentId, String toAgentId, MessagePriority priority,
            MessageOptions options) {
        Map<String, Object> metadata = new ConcurrentHashMap<>();
        metadata.put("fromAgentId", fromAgentId);
        metadata.put("toAgentId", toAgentId);
        metadata.put("priority", priority.name());
        metadata.put("encrypted", options.isEncrypted());
        metadata.put("persistent", options.isPersistent());
        metadata.put("timeout", options.getTimeout().toMillis());
        metadata.put("maxRetries", options.getMaxRetries());
        metadata.put("timestamp", System.currentTimeMillis());
        metadata.putAll(options.getMetadata());
        return metadata;
    }

    private boolean validateMessage(Message message) {
        // Apply all registered validators
        for (MessageValidator validator : messageValidators.values()) {
            if (!validator.validate(message)) {
                logger.warn("Message validation failed by validator: {}", validator.getClass().getSimpleName());
                return false;
            }
        }
        return true;
    }

    private boolean applyMessageFilters(Message message) {
        // Apply all registered filters
        for (MessageFilter filter : messageFilters.values()) {
            if (!filter.shouldDeliver(message)) {
                logger.debug("Message filtered out by filter: {}", filter.getClass().getSimpleName());
                return false;
            }
        }
        return true;
    }

    private CompletableFuture<MessageDeliveryResult> routeMessage(Message message, String messageId) {
        // Use registered router or default routing
        MessageRouter router = messageRouters.get("default");
        if (router != null) {
            return router.routeMessage(message);
        }

        // Default routing logic
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Simulate message delivery
                Thread.sleep(10);

                deliveryStatus.put(messageId, MessageDeliveryStatus.DELIVERED);
                totalMessagesDelivered.incrementAndGet();

                return MessageDeliveryResult.success("Message delivered successfully");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                totalMessagesFailed.incrementAndGet();
                return MessageDeliveryResult.failure("Message delivery interrupted");
            } catch (Exception e) {
                totalMessagesFailed.incrementAndGet();
                return MessageDeliveryResult.failure("Message delivery failed: " + e.getMessage());
            }
        });
    }

    private final Map<String, Message> pendingMessages = new ConcurrentHashMap<>();
    private final Map<String, MessageRetryInfo> retryQueue = new ConcurrentHashMap<>();

    private void processMessageQueue() {
        try {
            logger.debug("Processing message queue");

            // Process pending messages
            for (Map.Entry<String, Message> entry : pendingMessages.entrySet()) {
                String messageId = entry.getKey();
                Message message = entry.getValue();

                try {
                    // Attempt to deliver the message
                    CompletableFuture<MessageDeliveryResult> deliveryFuture = routeMessage(message, messageId);
                    MessageDeliveryResult result = deliveryFuture.get();

                    if (result.isSuccess()) {
                        // Message delivered successfully
                        pendingMessages.remove(messageId);
                        deliveryStatus.put(messageId, MessageDeliveryStatus.DELIVERED);
                        totalMessagesDelivered.incrementAndGet();

                        logger.debug("Message {} delivered successfully", messageId);
                    } else {
                        // Message delivery failed, add to retry queue
                        addToRetryQueue(messageId, message);
                        deliveryStatus.put(messageId, MessageDeliveryStatus.FAILED);
                        totalMessagesFailed.incrementAndGet();

                        logger.warn("Message {} delivery failed: {}", messageId, result.getMessage());
                    }

                } catch (Exception e) {
                    logger.error("Error processing message {}: {}", messageId, e.getMessage());

                    // Add to retry queue on error
                    addToRetryQueue(messageId, message);
                    deliveryStatus.put(messageId, MessageDeliveryStatus.FAILED);
                    totalMessagesFailed.incrementAndGet();
                }
            }

        } catch (Exception e) {
            logger.error("Error in message queue processing: {}", e.getMessage(), e);
        }
    }

    private void processRetryQueue() {
        try {
            logger.debug("Processing retry queue");

            Instant now = Instant.now();

            // Process retry attempts for failed messages
            for (Map.Entry<String, MessageRetryInfo> entry : retryQueue.entrySet()) {
                String messageId = entry.getKey();
                MessageRetryInfo retryInfo = entry.getValue();

                // Check if it's time to retry
                if (retryInfo.getNextRetryTime().isBefore(now)) {
                    Message message = messageStore.get(messageId);
                    if (message != null) {
                        // Check if we haven't exceeded max retries
                        Map<String, Object> metadata = message.getMetadata();
                        int maxRetries = metadata != null && metadata.containsKey("maxRetries")
                                ? (Integer) metadata.get("maxRetries")
                                : configuration.get().getMaxRetries();

                        if (retryInfo.getRetryCount() < maxRetries) {
                            // Attempt retry
                            try {
                                CompletableFuture<MessageDeliveryResult> retryFuture = routeMessage(message, messageId);
                                MessageDeliveryResult result = retryFuture.get();

                                if (result.isSuccess()) {
                                    // Retry successful
                                    retryQueue.remove(messageId);
                                    deliveryStatus.put(messageId, MessageDeliveryStatus.DELIVERED);
                                    totalMessagesDelivered.incrementAndGet();

                                    logger.info("Message {} retry successful after {} attempts", messageId,
                                            retryInfo.getRetryCount() + 1);
                                } else {
                                    // Retry failed, update retry info
                                    updateRetryInfo(messageId, retryInfo);
                                    deliveryStatus.put(messageId, MessageDeliveryStatus.RETRYING);

                                    logger.warn("Message {} retry failed (attempt {}): {}", messageId,
                                            retryInfo.getRetryCount() + 1, result.getMessage());
                                }

                            } catch (Exception e) {
                                logger.error("Error during retry for message {}: {}", messageId, e.getMessage());
                                updateRetryInfo(messageId, retryInfo);
                                deliveryStatus.put(messageId, MessageDeliveryStatus.RETRYING);
                            }
                        } else {
                            // Max retries exceeded, mark as failed
                            retryQueue.remove(messageId);
                            deliveryStatus.put(messageId, MessageDeliveryStatus.FAILED);

                            logger.error("Message {} failed after {} retry attempts", messageId,
                                    retryInfo.getRetryCount());
                        }
                    } else {
                        // Message no longer exists, remove from retry queue
                        retryQueue.remove(messageId);
                        logger.warn("Message {} no longer exists, removing from retry queue", messageId);
                    }
                }
            }

        } catch (Exception e) {
            logger.error("Error in retry queue processing: {}", e.getMessage(), e);
        }
    }

    private void addToRetryQueue(String messageId, Message message) {
        try {
            Instant now = Instant.now();
            Duration retryInterval = configuration.get().getRetryInterval();
            Instant nextRetryTime = now.plus(retryInterval);

            MessageRetryInfo retryInfo = new MessageRetryInfo(messageId, 1, now, nextRetryTime);
            retryQueue.put(messageId, retryInfo);

            logger.debug("Added message {} to retry queue, next retry at {}", messageId, nextRetryTime);

        } catch (Exception e) {
            logger.error("Error adding message {} to retry queue: {}", messageId, e.getMessage());
        }
    }

    private void updateRetryInfo(String messageId, MessageRetryInfo currentRetryInfo) {
        try {
            Instant now = Instant.now();
            Duration retryInterval = configuration.get().getRetryInterval();

            // Exponential backoff: increase retry interval with each attempt
            long backoffMultiplier = Math.min(currentRetryInfo.getRetryCount() + 1, 4); // Cap at 4x
            Duration nextRetryInterval = Duration.ofMillis(retryInterval.toMillis() * backoffMultiplier);
            Instant nextRetryTime = now.plus(nextRetryInterval);

            MessageRetryInfo newRetryInfo = new MessageRetryInfo(messageId, currentRetryInfo.getRetryCount() + 1, now,
                    nextRetryTime);

            retryQueue.put(messageId, newRetryInfo);

            logger.debug("Updated retry info for message {}: attempt {}, next retry at {}", messageId,
                    newRetryInfo.getRetryCount(), nextRetryTime);

        } catch (Exception e) {
            logger.error("Error updating retry info for message {}: {}", messageId, e.getMessage());
        }
    }

    private void cleanupExpiredMessages() {
        Instant cutoff = Instant.now().minus(Duration.ofDays(7));

        messageStore.entrySet().removeIf(entry -> {
            Message message = entry.getValue();
            Map<String, Object> metadata = message.getMetadata();
            if (metadata != null && metadata.containsKey("timestamp")) {
                Object timestampObj = metadata.get("timestamp");
                if (timestampObj instanceof Long) {
                    Instant messageTime = Instant.ofEpochMilli((Long) timestampObj);
                    return messageTime.isBefore(cutoff);
                }
            }
            return false;
        });

        deliveryStatus.entrySet().removeIf(entry -> {
            // Remove delivery status for messages that no longer exist
            return !messageStore.containsKey(entry.getKey());
        });
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

    // Inner classes extracted: MessageEncryptionService, MessageSecurityManager
}
