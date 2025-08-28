package org.openhab.core.ai.agent.lifecycle.api;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.agent.api.Agent;
import org.openhab.core.ai.agent.api.AgentMetrics;
import org.openhab.core.ai.agent.api.AgentStatus;
import org.openhab.core.ai.agent.lifecycle.AgentCommunicationProtocol;
import org.openhab.core.ai.agent.lifecycle.AgentMessage;
import org.openhab.core.ai.agent.lifecycle.AgentRegistrationResult;
import org.openhab.core.ai.agent.lifecycle.AgentSecurityContext;
import org.openhab.core.ai.agent.lifecycle.AgentValidationResult;

import org.openhab.core.ai.agent.lifecycle.MessageHandler;
import org.openhab.core.ai.agent.lifecycle.MessageStatus;
import org.openhab.core.ai.common.monitoring.service.MetricsService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
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
    private final Map<String, AgentStatus> agentStatus = new ConcurrentHashMap<>();
    // Agent metrics
    private final Map<String, AgentMetrics> agentMetrics = new ConcurrentHashMap<>();

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

    // Metrics service
    private @Nullable MetricsService metricsService;

    @Reference
    protected void setMetricsService(MetricsService metricsService) {
        this.metricsService = metricsService;
        logger.debug("MetricsService set for AgentRegistry");
    }

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
        agentStatus.put(agentId, AgentStatus.OFFLINE);
        // Legacy metrics removed - using MetricsService instead

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
                agentStatus.put(agentId, AgentStatus.IDLE);
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
                agentStatus.put(agentId, AgentStatus.OFFLINE);
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
    public AgentStatus getAgentStatus(String agentId, String requestingUserId) {
        if (!hasPermission(requestingUserId, agentId, "read")) {
            return AgentStatus.OFFLINE;
        }
        return agentStatus.getOrDefault(agentId, AgentStatus.OFFLINE);
    }

    /**
     * Get metrics for an agent with access control.
     */
    public AgentMetrics getAgentMetrics(String agentId, String requestingUserId) {
        if (!hasPermission(requestingUserId, agentId, "read")) {
            return null;
        }
        // Legacy metrics removed - use MetricsService.getStatistics() instead
        return null;
    }

    /**
     * Record an agent execution for metrics.
     */
    public void recordAgentExecution(String agentId, long executionTime, boolean success) {
        // Record metrics using MetricsService
        MetricsService metrics = metricsService;
        if (metrics != null) {
            try {
                Map<String, Object> context = Map.of(
                    "agentId", agentId,
                    "executionTimeMs", executionTime
                );
                metrics.recordOperationWithData("agent", "execution", success, 
                    java.time.Duration.ofMillis(executionTime), context);
            } catch (Exception e) {
                logger.warn("Failed to record agent execution metrics for agent {}: {}", agentId, e.getMessage());
            }
        }
        
        // Legacy metrics removed - all metrics now handled by MetricsService
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
}
