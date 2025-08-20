package org.openhab.core.ai.agent.lifecycle;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.response.MessageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Agent communication protocol for inter-agent communication
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class AgentCommunicationProtocol {
    private static final Logger logger = LoggerFactory.getLogger(AgentCommunicationProtocol.class);

    private final String agentId;
    private final List<AgentMessage> messageQueue = new CopyOnWriteArrayList<>();
    private final List<MessageHandler> messageHandlers = new CopyOnWriteArrayList<>();
    private final Map<String, MessageStatus> messageStatus = new ConcurrentHashMap<>();
    private final ScheduledExecutorService messageProcessor = Executors.newSingleThreadScheduledExecutor();

    public AgentCommunicationProtocol(String agentId) {
        this.agentId = agentId;
        // Start message processing
        messageProcessor.scheduleAtFixedRate(this::processMessageQueue, 100, 100, TimeUnit.MILLISECONDS);
    }

    public boolean sendMessage(String message, String senderId) {
        if (message == null || message.trim().isEmpty()) {
            return false;
        }

        String messageId = "msg-" + System.currentTimeMillis() + "-" + senderId.hashCode();
        AgentMessage agentMessage = new AgentMessage(messageId, senderId, message, System.currentTimeMillis());

        messageQueue.add(agentMessage);
        messageStatus.put(messageId, MessageStatus.QUEUED);

        logger.debug("Message queued for agent {}: {} from {}", agentId, messageId, senderId);
        return true;
    }

    public List<String> getMessages() {
        return messageQueue.stream().map(msg -> msg.senderId() + ": " + msg.content()).collect(Collectors.toList());
    }

    public List<AgentMessage> getAgentMessages() {
        return List.copyOf(messageQueue);
    }

    public void clearMessages() {
        messageQueue.clear();
        messageStatus.clear();
    }

    /**
     * Register a message handler for this agent.
     */
    public void addMessageHandler(MessageHandler handler) {
        messageHandlers.add(handler);
        logger.debug("Message handler registered for agent {}", agentId);
    }

    /**
     * Remove a message handler.
     */
    public void removeMessageHandler(MessageHandler handler) {
        messageHandlers.remove(handler);
        logger.debug("Message handler removed for agent {}", agentId);
    }

    /**
     * Process the message queue and deliver messages to handlers.
     */
    private void processMessageQueue() {
        List<AgentMessage> messagesToProcess = new ArrayList<>();

        // Collect unprocessed messages
        for (AgentMessage message : messageQueue) {
            if (messageStatus.get(message.messageId()) == MessageStatus.QUEUED) {
                messagesToProcess.add(message);
            }
        }

        // Deliver messages to handlers
        for (AgentMessage message : messagesToProcess) {
            deliverMessage(message);
        }
    }

    /**
     * Deliver a message to all registered handlers.
     */
    private void deliverMessage(AgentMessage message) {
        if (messageHandlers.isEmpty()) {
            logger.warn("No message handlers registered for agent {}, message {} will not be delivered", agentId,
                    message.messageId());
            messageStatus.put(message.messageId(), MessageStatus.NO_HANDLERS);
            return;
        }

        boolean delivered = false;
        for (MessageHandler handler : messageHandlers) {
            try {
                MessageResponse response = handler.handleMessage(message);
                if (response != null && response.isAcknowledged()) {
                    delivered = true;
                    messageStatus.put(message.messageId(), MessageStatus.DELIVERED);
                    logger.debug("Message {} delivered to handler for agent {}", message.messageId(), agentId);
                    break;
                }
            } catch (Exception e) {
                logger.error("Error delivering message {} to handler for agent {}", message.messageId(), agentId, e);
            }
        }

        if (!delivered) {
            messageStatus.put(message.messageId(), MessageStatus.DELIVERY_FAILED);
            logger.warn("Message {} could not be delivered to any handler for agent {}", message.messageId(), agentId);
        }
    }

    /**
     * Get message status.
     */
    public MessageStatus getMessageStatus(String messageId) {
        return messageStatus.getOrDefault(messageId, MessageStatus.UNKNOWN);
    }

    /**
     * Acknowledge message receipt.
     */
    public boolean acknowledgeMessage(String messageId) {
        MessageStatus status = messageStatus.get(messageId);
        if (status == MessageStatus.DELIVERED) {
            messageStatus.put(messageId, MessageStatus.ACKNOWLEDGED);
            return true;
        }
        return false;
    }

    /**
     * Shutdown the message processor.
     */
    public void shutdown() {
        messageProcessor.shutdown();
        try {
            if (!messageProcessor.awaitTermination(5, TimeUnit.SECONDS)) {
                messageProcessor.shutdownNow();
            }
        } catch (InterruptedException e) {
            messageProcessor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
