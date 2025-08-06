package org.openhab.core.ai.agent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.api.agent.Agent;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Enhanced agent registry for A2A agents, supporting registration, discovery, capability management,
 * lifecycle, performance monitoring, security, validation, communication protocols, and access controls.
 *
 * @author Karel Goderis - Initial Contribution
 */
@Component(service = AgentRegistry.class)
@NonNullByDefault
public class AgentRegistry {

    private final Logger logger = LoggerFactory.getLogger(AgentRegistry.class);

    // Agent storage
    private final Map<String, Agent> agents = new ConcurrentHashMap<>();
    // Capability mapping: agentId -> set of capabilities
    private final Map<String, Set<String>> agentCapabilities = new ConcurrentHashMap<>();
    // Capability reverse mapping: capability -> set of agentIds
    private final Map<String, Set<String>> capabilityAgents = new ConcurrentHashMap<>();
    // Agent status
    private final Map<String, Agent.AgentStatus> agentStatus = new ConcurrentHashMap<>();
    // Agent metrics
    private final Map<String, Agent.AgentMetrics> agentMetrics = new ConcurrentHashMap<>();

    // Security and access control
    private final Map<String, AgentSecurityContext> agentSecurityContexts = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> agentOwnership = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> agentPermissions = new ConcurrentHashMap<>();

    // Communication protocols
    private final Map<String, AgentCommunicationProtocol> agentProtocols = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> agentCommunicationChannels = new ConcurrentHashMap<>();

    // Validation and health monitoring
    private final Map<String, AgentValidationResult> agentValidationResults = new ConcurrentHashMap<>();
    private final ScheduledExecutorService healthMonitor = Executors.newSingleThreadScheduledExecutor();

    @Activate
    public void activate() {
        logger.info("A2A Agent Registry activated");
        // Start health monitoring
        healthMonitor.scheduleAtFixedRate(this::performHealthChecks, 30, 60, TimeUnit.SECONDS);
    }

    @Deactivate
    public void deactivate() {
        logger.info("A2A Agent Registry deactivated");

        // Shutdown health monitor
        healthMonitor.shutdown();
        try {
            if (!healthMonitor.awaitTermination(5, TimeUnit.SECONDS)) {
                healthMonitor.shutdownNow();
            }
        } catch (InterruptedException e) {
            healthMonitor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        // Shutdown all message processors
        for (AgentCommunicationProtocol protocol : agentProtocols.values()) {
            protocol.shutdown();
        }
        agentProtocols.clear();
    }

    /**
     * Register an agent with the registry with security validation.
     */
    public AgentRegistrationResult registerAgent(String agentId, Agent agent, AgentSecurityContext securityContext) {
        // Validate agent registration
        AgentValidationResult validation = validateAgentRegistration(agentId, agent, securityContext);
        if (!validation.isValid()) {
            return AgentRegistrationResult.failure(validation.getErrors());
        }

        // Register agent
        agents.put(agentId, agent);
        agentCapabilities.putIfAbsent(agentId, new CopyOnWriteArraySet<>());
        agentStatus.put(agentId, Agent.AgentStatus.OFFLINE);
        agentMetrics.putIfAbsent(agentId, new AgentMetricsImpl(agentId));

        // Set up security context
        agentSecurityContexts.put(agentId, securityContext);
        agentOwnership.put(agentId, new CopyOnWriteArraySet<>(securityContext.getOwners()));
        agentPermissions.put(agentId, new CopyOnWriteArraySet<>(securityContext.getPermissions()));

        // Set up communication protocol
        AgentCommunicationProtocol protocol = new AgentCommunicationProtocol(agentId);
        agentProtocols.put(agentId, protocol);
        agentCommunicationChannels.put(agentId, new CopyOnWriteArraySet<>());

        logger.info("Agent registered successfully: {}", agentId);
        return AgentRegistrationResult.success(agentId);
    }

    /**
     * Unregister an agent from the registry with security validation.
     */
    public boolean unregisterAgent(String agentId, String requestingUserId) {
        // Check if user has permission to unregister
        if (!hasPermission(requestingUserId, agentId, "unregister")) {
            logger.warn("User {} attempted to unregister agent {} without permission", requestingUserId, agentId);
            return false;
        }

        agents.remove(agentId);
        agentCapabilities.remove(agentId);
        agentStatus.remove(agentId);
        agentMetrics.remove(agentId);
        agentSecurityContexts.remove(agentId);
        agentOwnership.remove(agentId);
        agentPermissions.remove(agentId);
        agentProtocols.remove(agentId);
        agentCommunicationChannels.remove(agentId);
        agentValidationResults.remove(agentId);

        // Remove from capability reverse mapping
        for (Set<String> agentSet : capabilityAgents.values()) {
            agentSet.remove(agentId);
        }

        logger.info("Agent unregistered: {}", agentId);
        return true;
    }

    /**
     * Get an agent by ID with access control.
     */
    public @Nullable Agent getAgent(String agentId, String requestingUserId) {
        if (!hasPermission(requestingUserId, agentId, "read")) {
            logger.warn("User {} attempted to access agent {} without permission", requestingUserId, agentId);
            return null;
        }
        return agents.get(agentId);
    }

    /**
     * Get all registered agent IDs with access control.
     */
    public List<String> getRegisteredAgentIds(String requestingUserId) {
        return agents.keySet().stream().filter(agentId -> hasPermission(requestingUserId, agentId, "read")).toList();
    }

    /**
     * Register a capability for an agent with validation.
     */
    public boolean registerCapability(String agentId, String capability, String requestingUserId) {
        if (!hasPermission(requestingUserId, agentId, "modify")) {
            logger.warn("User {} attempted to modify agent {} capabilities without permission", requestingUserId,
                    agentId);
            return false;
        }

        // Validate capability format
        if (!isValidCapability(capability)) {
            logger.warn("Invalid capability format: {}", capability);
            return false;
        }

        agentCapabilities.computeIfAbsent(agentId, k -> new CopyOnWriteArraySet<>()).add(capability);
        capabilityAgents.computeIfAbsent(capability, k -> new CopyOnWriteArraySet<>()).add(agentId);

        logger.debug("Capability {} registered for agent {}", capability, agentId);
        return true;
    }

    /**
     * Get all capabilities for an agent with access control.
     */
    public List<String> getAgentCapabilities(String agentId, String requestingUserId) {
        if (!hasPermission(requestingUserId, agentId, "read")) {
            return List.of();
        }
        Set<String> caps = agentCapabilities.get(agentId);
        return caps != null ? List.copyOf(caps) : List.of();
    }

    /**
     * Find all agents with a given capability with access control.
     */
    public List<String> findAgentsWithCapability(String capability, String requestingUserId) {
        Set<String> agentIds = capabilityAgents.get(capability);
        if (agentIds == null) {
            return List.of();
        }

        // Filter by access permissions
        return agentIds.stream().filter(agentId -> hasPermission(requestingUserId, agentId, "read")).toList();
    }

    /**
     * Start an agent with security validation.
     */
    public boolean startAgent(String agentId, String requestingUserId) {
        if (!hasPermission(requestingUserId, agentId, "control")) {
            logger.warn("User {} attempted to start agent {} without permission", requestingUserId, agentId);
            return false;
        }

        Agent agent = agents.get(agentId);
        if (agent == null) {
            logger.warn("Agent not found: {}", agentId);
            return false;
        }

        try {
            boolean startResult = agent.start();
            if (startResult) {
                agentStatus.put(agentId, Agent.AgentStatus.IDLE);
                logger.info("Agent started: {}", agentId);
                return true;
            } else {
                logger.warn("Agent start failed: {}", agentId);
                return false;
            }
        } catch (Exception e) {
            logger.error("Error starting agent: {}", agentId, e);
            return false;
        }
    }

    /**
     * Stop an agent with security validation.
     */
    public boolean stopAgent(String agentId, String requestingUserId) {
        if (!hasPermission(requestingUserId, agentId, "control")) {
            logger.warn("User {} attempted to stop agent {} without permission", requestingUserId, agentId);
            return false;
        }

        Agent agent = agents.get(agentId);
        if (agent == null) {
            logger.warn("Agent not found: {}", agentId);
            return false;
        }

        try {
            boolean stopResult = agent.stop();
            if (stopResult) {
                agentStatus.put(agentId, Agent.AgentStatus.OFFLINE);
                logger.info("Agent stopped: {}", agentId);
                return true;
            } else {
                logger.warn("Agent stop failed: {}", agentId);
                return false;
            }
        } catch (Exception e) {
            logger.error("Error stopping agent: {}", agentId, e);
            return false;
        }
    }

    /**
     * Get the status of an agent with access control.
     */
    public Agent.AgentStatus getAgentStatus(String agentId, String requestingUserId) {
        if (!hasPermission(requestingUserId, agentId, "read")) {
            return Agent.AgentStatus.OFFLINE;
        }
        return agentStatus.getOrDefault(agentId, Agent.AgentStatus.OFFLINE);
    }

    /**
     * Get metrics for an agent with access control.
     */
    public Agent.@Nullable AgentMetrics getAgentMetrics(String agentId, String requestingUserId) {
        if (!hasPermission(requestingUserId, agentId, "read")) {
            return null;
        }
        return agentMetrics.computeIfAbsent(agentId, k -> new AgentMetricsImpl(k));
    }

    /**
     * Record an agent execution for metrics.
     */
    public void recordAgentExecution(String agentId, long executionTime, boolean success) {
        AgentMetricsImpl metrics = (AgentMetricsImpl) agentMetrics.computeIfAbsent(agentId,
                k -> new AgentMetricsImpl(k));
        metrics.recordExecution(executionTime, success);
    }

    /**
     * Send a message to an agent using the communication protocol.
     */
    public boolean sendMessageToAgent(String agentId, String message, String requestingUserId) {
        if (!hasPermission(requestingUserId, agentId, "communicate")) {
            logger.warn("User {} attempted to communicate with agent {} without permission", requestingUserId, agentId);
            return false;
        }

        AgentCommunicationProtocol protocol = agentProtocols.get(agentId);
        if (protocol == null) {
            logger.warn("No communication protocol for agent: {}", agentId);
            return false;
        }

        return protocol.sendMessage(message, requestingUserId);
    }

    /**
     * Get communication channels for an agent.
     */
    public Set<String> getAgentCommunicationChannels(String agentId, String requestingUserId) {
        if (!hasPermission(requestingUserId, agentId, "read")) {
            return Set.of();
        }
        Set<String> channels = agentCommunicationChannels.get(agentId);
        return channels != null ? Set.copyOf(channels) : Set.of();
    }

    /**
     * Add a communication channel for an agent.
     */
    public boolean addCommunicationChannel(String agentId, String channel, String requestingUserId) {
        if (!hasPermission(requestingUserId, agentId, "modify")) {
            logger.warn("User {} attempted to modify agent {} communication without permission", requestingUserId,
                    agentId);
            return false;
        }

        agentCommunicationChannels.computeIfAbsent(agentId, k -> new CopyOnWriteArraySet<>()).add(channel);
        logger.debug("Communication channel {} added for agent {}", channel, agentId);
        return true;
    }

    /**
     * Register a message handler for an agent.
     */
    public boolean registerMessageHandler(String agentId, MessageHandler handler, String requestingUserId) {
        if (!hasPermission(requestingUserId, agentId, "modify")) {
            logger.warn("User {} attempted to register message handler for agent {} without permission",
                    requestingUserId, agentId);
            return false;
        }

        AgentCommunicationProtocol protocol = agentProtocols.get(agentId);
        if (protocol == null) {
            logger.warn("No communication protocol for agent: {}", agentId);
            return false;
        }

        protocol.addMessageHandler(handler);
        logger.debug("Message handler registered for agent {}", agentId);
        return true;
    }

    /**
     * Unregister a message handler for an agent.
     */
    public boolean unregisterMessageHandler(String agentId, MessageHandler handler, String requestingUserId) {
        if (!hasPermission(requestingUserId, agentId, "modify")) {
            logger.warn("User {} attempted to unregister message handler for agent {} without permission",
                    requestingUserId, agentId);
            return false;
        }

        AgentCommunicationProtocol protocol = agentProtocols.get(agentId);
        if (protocol == null) {
            logger.warn("No communication protocol for agent: {}", agentId);
            return false;
        }

        protocol.removeMessageHandler(handler);
        logger.debug("Message handler unregistered for agent {}", agentId);
        return true;
    }

    /**
     * Get message status for a specific message.
     */
    public MessageStatus getMessageStatus(String agentId, String messageId, String requestingUserId) {
        if (!hasPermission(requestingUserId, agentId, "read")) {
            return MessageStatus.UNKNOWN;
        }

        AgentCommunicationProtocol protocol = agentProtocols.get(agentId);
        if (protocol == null) {
            return MessageStatus.UNKNOWN;
        }

        return protocol.getMessageStatus(messageId);
    }

    /**
     * Acknowledge message receipt.
     */
    public boolean acknowledgeMessage(String agentId, String messageId, String requestingUserId) {
        if (!hasPermission(requestingUserId, agentId, "communicate")) {
            logger.warn("User {} attempted to acknowledge message for agent {} without permission", requestingUserId,
                    agentId);
            return false;
        }

        AgentCommunicationProtocol protocol = agentProtocols.get(agentId);
        if (protocol == null) {
            logger.warn("No communication protocol for agent: {}", agentId);
            return false;
        }

        return protocol.acknowledgeMessage(messageId);
    }

    /**
     * Get all messages for an agent.
     */
    public List<AgentMessage> getAgentMessages(String agentId, String requestingUserId) {
        if (!hasPermission(requestingUserId, agentId, "read")) {
            return List.of();
        }

        AgentCommunicationProtocol protocol = agentProtocols.get(agentId);
        if (protocol == null) {
            return List.of();
        }

        return protocol.getAgentMessages();
    }

    /**
     * Validate agent registration.
     */
    private AgentValidationResult validateAgentRegistration(String agentId, Agent agent,
            AgentSecurityContext securityContext) {
        List<String> errors = new CopyOnWriteArrayList<>();

        // Validate agent ID format
        if (agentId == null || agentId.trim().isEmpty()) {
            errors.add("Agent ID cannot be null or empty");
        } else if (!agentId.matches("^[a-zA-Z0-9_-]+$")) {
            errors.add("Agent ID must contain only alphanumeric characters, hyphens, and underscores");
        }

        // Check for duplicate agent ID
        if (agents.containsKey(agentId)) {
            errors.add("Agent ID already registered: " + agentId);
        }

        // Validate agent object
        if (agent == null) {
            errors.add("Agent object cannot be null");
        } else {
            // Validate agent properties
            if (agent.getAgentId() == null || !agent.getAgentId().equals(agentId)) {
                errors.add("Agent ID mismatch: expected " + agentId + ", got " + agent.getAgentId());
            }
            if (agent.getAgentName() == null || agent.getAgentName().trim().isEmpty()) {
                errors.add("Agent name cannot be null or empty");
            }
            // Agent version validation removed as it's not part of the interface
        }

        // Validate security context
        if (securityContext == null) {
            errors.add("Security context cannot be null");
        } else {
            if (securityContext.getOwners().isEmpty()) {
                errors.add("Agent must have at least one owner");
            }
            if (securityContext.getPermissions().isEmpty()) {
                errors.add("Agent must have at least basic permissions");
            }
        }

        return new AgentValidationResult(errors.isEmpty(), errors);
    }

    /**
     * Check if a capability string is valid.
     */
    private boolean isValidCapability(String capability) {
        return capability != null && capability.trim().length() > 0 && capability.matches("^[a-zA-Z0-9._-]+$");
    }

    /**
     * Check if a user has permission for an agent.
     */
    private boolean hasPermission(String userId, String agentId, String permission) {
        // Check ownership
        Set<String> owners = agentOwnership.get(agentId);
        if (owners != null && owners.contains(userId)) {
            return true; // Owners have all permissions
        }

        // Check specific permissions
        Set<String> permissions = agentPermissions.get(agentId);
        if (permissions != null) {
            String fullPermission = userId + ":" + permission;
            return permissions.contains(fullPermission) || permissions.contains("*:" + permission);
        }

        return false;
    }

    /**
     * Perform health checks on all agents.
     */
    private void performHealthChecks() {
        for (Map.Entry<String, Agent> entry : agents.entrySet()) {
            String agentId = entry.getKey();
            Agent agent = entry.getValue();

            try {
                // Use isRunning() as a health check since isHealthy() is not available
                boolean healthy = agent.isRunning();
                if (!healthy) {
                    logger.warn("Agent health check failed: {}", agentId);
                    // Could trigger recovery actions here
                }
            } catch (Exception e) {
                logger.error("Error during health check for agent: {}", agentId, e);
            }
        }
    }

    /**
     * Implementation of AgentMetrics interface.
     */
    private static class AgentMetricsImpl implements Agent.AgentMetrics {
        private final String agentId;
        private final List<Long> executionTimes = new CopyOnWriteArrayList<>();
        private int successCount = 0;
        private int failureCount = 0;

        public AgentMetricsImpl(String agentId) {
            this.agentId = agentId;
        }

        public void recordExecution(long time, boolean success) {
            executionTimes.add(time);
            if (success) {
                successCount++;
            } else {
                failureCount++;
            }
        }

        @Override
        public long[] getExecutionTimes() {
            return executionTimes.stream().mapToLong(Long::longValue).toArray();
        }

        @Override
        public long getSuccessCount() {
            return successCount;
        }

        @Override
        public long getFailureCount() {
            return failureCount;
        }

        @Override
        public double getAverageExecutionTime() {
            return executionTimes.isEmpty() ? 0.0
                    : executionTimes.stream().mapToLong(Long::longValue).average().orElse(0.0);
        }

        @Override
        public long getTotalExecutions() {
            return executionTimes.size();
        }
    }

    /**
     * Agent security context for access control.
     */
    public static class AgentSecurityContext {
        private final Set<String> owners;
        private final Set<String> permissions;

        public AgentSecurityContext(Set<String> owners, Set<String> permissions) {
            this.owners = new CopyOnWriteArraySet<>(owners);
            this.permissions = new CopyOnWriteArraySet<>(permissions);
        }

        public Set<String> getOwners() {
            return Set.copyOf(owners);
        }

        public Set<String> getPermissions() {
            return Set.copyOf(permissions);
        }
    }

    /**
     * Agent communication protocol for inter-agent communication.
     */
    public static class AgentCommunicationProtocol {
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
                    logger.error("Error delivering message {} to handler for agent {}", message.messageId(), agentId,
                            e);
                }
            }

            if (!delivered) {
                messageStatus.put(message.messageId(), MessageStatus.DELIVERY_FAILED);
                logger.warn("Message {} could not be delivered to any handler for agent {}", message.messageId(),
                        agentId);
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

    /**
     * Agent message with metadata.
     */
    public static record AgentMessage(String messageId, String senderId, String content, long timestamp) {
        public AgentMessage {
            if (messageId == null || messageId.trim().isEmpty()) {
                throw new IllegalArgumentException("Message ID cannot be null or empty");
            }
            if (senderId == null || senderId.trim().isEmpty()) {
                throw new IllegalArgumentException("Sender ID cannot be null or empty");
            }
            if (content == null) {
                throw new IllegalArgumentException("Message content cannot be null");
            }
        }
    }

    /**
     * Message handler interface for agents.
     */
    public interface MessageHandler {
        /**
         * Handle an incoming message.
         * 
         * @param message the message to handle
         * @return response indicating if message was processed successfully
         */
        MessageResponse handleMessage(AgentMessage message);
    }

    /**
     * Message response indicating processing status.
     */
    public static class MessageResponse {
        private final boolean acknowledged;
        private final String response;
        private final long timestamp;

        public MessageResponse(boolean acknowledged, String response) {
            this.acknowledged = acknowledged;
            this.response = response;
            this.timestamp = System.currentTimeMillis();
        }

        public boolean isAcknowledged() {
            return acknowledged;
        }

        public String getResponse() {
            return response;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public static MessageResponse success(String response) {
            return new MessageResponse(true, response);
        }

        public static MessageResponse failure(String error) {
            return new MessageResponse(false, error);
        }
    }

    /**
     * Message status enumeration.
     */
    public enum MessageStatus {
        QUEUED,
        DELIVERED,
        ACKNOWLEDGED,
        DELIVERY_FAILED,
        NO_HANDLERS,
        UNKNOWN
    }

    /**
     * Agent validation result.
     */
    public static class AgentValidationResult {
        private final boolean valid;
        private final List<String> errors;

        public AgentValidationResult(boolean valid, List<String> errors) {
            this.valid = valid;
            this.errors = List.copyOf(errors);
        }

        public boolean isValid() {
            return valid;
        }

        public List<String> getErrors() {
            return errors;
        }
    }

    /**
     * Agent registration result.
     */
    public static class AgentRegistrationResult {
        private final boolean success;
        private final String agentId;
        private final List<String> errors;

        private AgentRegistrationResult(boolean success, String agentId, List<String> errors) {
            this.success = success;
            this.agentId = agentId;
            this.errors = errors;
        }

        public static AgentRegistrationResult success(String agentId) {
            return new AgentRegistrationResult(true, agentId, List.of());
        }

        public static AgentRegistrationResult failure(List<String> errors) {
            return new AgentRegistrationResult(false, "", errors);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getAgentId() {
            return agentId;
        }

        public List<String> getErrors() {
            return errors;
        }
    }
}
