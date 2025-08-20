package org.openhab.core.ai.agent.collaboration.coordination;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.agent.collaboration.ConflictResolutionResult;
import org.openhab.core.ai.agent.collaboration.ConflictResolutionStrategy;
import org.openhab.core.ai.agent.collaboration.ConflictType;
import org.openhab.core.ai.agent.collaboration.ContextAccessLevel;
import org.openhab.core.ai.agent.collaboration.SharedContext;
import org.openhab.core.ai.agent.collaboration.coordination.api.CoordinationProtocol;
import org.openhab.core.ai.agent.lifecycle.api.AgentRegistry;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Agent Coordination Manager - Inter-agent coordination and communication
 * 
 * This service provides comprehensive coordination capabilities for multi-agent systems:
 * - Inter-agent communication protocols
 * - Conflict resolution mechanisms
 * - Coordination protocols and strategies
 * - Shared context management
 * - Priority handling and resource allocation
 * - Coordination monitoring and analytics
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@Component(service = AgentCoordinationManager.class)
@NonNullByDefault
public class AgentCoordinationManager {

    private final Logger logger = LoggerFactory.getLogger(AgentCoordinationManager.class);

    @Reference
    private @Nullable AgentRegistry agentRegistry;

    // Coordination state
    private final Map<String, CoordinationSession> activeSessions = new ConcurrentHashMap<>();
    private final Map<String, ConflictResolutionSession> conflictSessions = new ConcurrentHashMap<>();
    private final Map<String, SharedContext> sharedContexts = new ConcurrentHashMap<>();
    private final Map<String, CoordinationProtocol> protocols = new ConcurrentHashMap<>();

    // Metrics and monitoring
    private final AtomicLong totalCoordinationSessions = new AtomicLong(0);
    private final AtomicLong totalConflictResolutions = new AtomicLong(0);
    private final AtomicLong totalContextSharing = new AtomicLong(0);
    private final AtomicLong totalProtocolExecutions = new AtomicLong(0);

    // Configuration
    private final AtomicReference<CoordinationConfiguration> configuration = new AtomicReference<>(
            new CoordinationConfiguration());

    /**
     * Start a coordination session between multiple agents
     * 
     * @param sessionId Unique session identifier
     * @param agentIds List of agent IDs to coordinate
     * @param protocol Coordination protocol to use
     * @return Coordination session
     */
    public CompletableFuture<CoordinationSession> startCoordinationSession(String sessionId, List<String> agentIds,
            CoordinationProtocol protocol) {
        logger.debug("Starting coordination session: {} with agents: {}", sessionId, agentIds);

        // Validate agents exist
        AgentRegistry registry = agentRegistry;
        if (registry == null) {
            return CompletableFuture.failedFuture(new IllegalStateException("Agent registry not available"));
        }

        for (String agentId : agentIds) {
            if (registry.getAgent(agentId, "system") == null) {
                return CompletableFuture.failedFuture(new IllegalArgumentException("Agent not found: " + agentId));
            }
        }

        // Create coordination session
        CoordinationSession session = CoordinationSession.builder().sessionId(sessionId).agentIds(agentIds)
                .protocol(protocol).startTime(Instant.now()).state(CoordinationState.INITIATED).build();

        activeSessions.put(sessionId, session);
        totalCoordinationSessions.incrementAndGet();

        // Execute coordination protocol
        return protocol.execute(session).thenApply(result -> {
            session.setState(CoordinationState.COMPLETED);
            session.setResult(result);
            logger.debug("Coordination session completed: {}", sessionId);
            return session;
        }).exceptionally(throwable -> {
            session.setState(CoordinationState.FAILED);
            session.setError(throwable.getMessage());
            logger.error("Coordination session failed: {}", sessionId, throwable);
            return session;
        });
    }

    /**
     * Resolve conflicts between agents
     * 
     * @param conflictId Unique conflict identifier
     * @param conflictingAgents List of agents involved in conflict
     * @param conflictType Type of conflict
     * @param conflictData Conflict details
     * @return Conflict resolution result
     */
    public CompletableFuture<ConflictResolutionResult> resolveConflict(String conflictId,
            List<String> conflictingAgents, ConflictType conflictType, Map<String, Object> conflictData) {
        logger.debug("Resolving conflict: {} between agents: {}", conflictId, conflictingAgents);

        ConflictResolutionSession session = ConflictResolutionSession.builder().conflictId(conflictId)
                .conflictingAgents(conflictingAgents).conflictType(conflictType).conflictData(conflictData)
                .startTime(Instant.now()).state(ConflictResolutionState.INITIATED).build();

        conflictSessions.put(conflictId, session);
        totalConflictResolutions.incrementAndGet();

        // Apply conflict resolution strategy
        ConflictResolutionStrategy strategy = selectConflictResolutionStrategy(conflictType);
        try {
            ConflictResolutionResult result = strategy.resolve(session);
            session.setState(ConflictResolutionState.RESOLVED);
            session.setResolution(result);
            logger.debug("Conflict resolved: {}", conflictId);
            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            session.setState(ConflictResolutionState.FAILED);
            session.setError(e.getMessage());
            logger.error("Conflict resolution failed: {}", conflictId, e);
            return CompletableFuture.completedFuture(ConflictResolutionResult.failure(e.getMessage()));
        }
    }

    /**
     * Create or update shared context for agents
     * 
     * @param contextId Unique context identifier
     * @param agentIds Agents that share this context
     * @param contextData Context data
     * @param accessLevel Access level for the context
     * @return Shared context
     */
    public SharedContext createSharedContext(String contextId, List<String> agentIds, Map<String, Object> contextData,
            ContextAccessLevel accessLevel) {
        logger.debug("Creating shared context: {} for agents: {}", contextId, agentIds);

        SharedContext context = SharedContext.builder().contextId(contextId).agentIds(agentIds).contextData(contextData)
                .accessLevel(accessLevel).createdAt(Instant.now()).lastModifiedAt(Instant.now()).version(1).build();

        sharedContexts.put(contextId, context);
        totalContextSharing.incrementAndGet();

        return context;
    }

    /**
     * Get shared context by ID
     * 
     * @param contextId Context identifier
     * @param requestingAgentId Agent requesting the context
     * @return Shared context or null if not found or access denied
     */
    public @Nullable SharedContext getSharedContext(String contextId, String requestingAgentId) {
        SharedContext context = sharedContexts.get(contextId);
        if (context == null) {
            return null;
        }

        // Check access permissions
        if (!context.getAgentIds().contains(requestingAgentId)
                && context.getAccessLevel() != ContextAccessLevel.PUBLIC) {
            logger.warn("Agent {} denied access to shared context: {}", requestingAgentId, contextId);
            return null;
        }

        return context;
    }

    /**
     * Update shared context
     * 
     * @param contextId Context identifier
     * @param updates Context updates
     * @param updatingAgentId Agent updating the context
     * @return Updated context or null if not found or access denied
     */
    public @Nullable SharedContext updateSharedContext(String contextId, Map<String, Object> updates,
            String updatingAgentId) {
        SharedContext context = sharedContexts.get(contextId);
        if (context == null) {
            return null;
        }

        // Check write permissions
        if (!context.getAgentIds().contains(updatingAgentId)) {
            logger.warn("Agent {} denied write access to shared context: {}", updatingAgentId, contextId);
            return null;
        }

        // Update context
        Map<String, Object> newData = new ConcurrentHashMap<>(context.getContextData());
        newData.putAll(updates);

        SharedContext updatedContext = context.toBuilder().contextData(newData).lastModifiedAt(Instant.now())
                .version(context.getVersion() + 1).build();

        sharedContexts.put(contextId, updatedContext);
        return updatedContext;
    }

    /**
     * Register a coordination protocol
     * 
     * @param protocolId Protocol identifier
     * @param protocol Coordination protocol implementation
     */
    public void registerProtocol(String protocolId, CoordinationProtocol protocol) {
        protocols.put(protocolId, protocol);
        logger.debug("Registered coordination protocol: {}", protocolId);
    }

    /**
     * Get coordination statistics
     * 
     * @return Coordination statistics
     */
    public CoordinationStatistics getStatistics() {
        return new CoordinationStatistics(totalCoordinationSessions.get(), totalConflictResolutions.get(),
                totalContextSharing.get(), totalProtocolExecutions.get(), activeSessions.size(),
                conflictSessions.size(), sharedContexts.size(), protocols.size());
    }

    /**
     * Get active coordination sessions
     * 
     * @return List of active sessions
     */
    public List<CoordinationSession> getActiveSessions() {
        return List.copyOf(activeSessions.values());
    }

    /**
     * Get active conflict resolution sessions
     * 
     * @return List of active conflict sessions
     */
    public List<ConflictResolutionSession> getActiveConflictSessions() {
        return List.copyOf(conflictSessions.values());
    }

    /**
     * Clean up completed sessions
     */
    public void cleanupCompletedSessions() {
        Instant cutoff = Instant.now().minus(Duration.ofHours(24));

        activeSessions.entrySet().removeIf(entry -> {
            CoordinationSession session = entry.getValue();
            return session.getState() == CoordinationState.COMPLETED || session.getState() == CoordinationState.FAILED
                    || session.getStartTime().isBefore(cutoff);
        });

        conflictSessions.entrySet().removeIf(entry -> {
            ConflictResolutionSession session = entry.getValue();
            return session.getState() == ConflictResolutionState.RESOLVED
                    || session.getState() == ConflictResolutionState.FAILED || session.getStartTime().isBefore(cutoff);
        });
    }

    /**
     * Select appropriate conflict resolution strategy
     */
    private ConflictResolutionStrategy selectConflictResolutionStrategy(ConflictType conflictType) {
        switch (conflictType) {
            case RESOURCE_CONFLICT:
                return new ResourceConflictResolutionStrategy();
            case PRIORITY_CONFLICT:
                return new PriorityConflictResolutionStrategy();
            case POLICY_CONFLICT:
                return new PolicyConflictResolutionStrategy();
            case COMMUNICATION_CONFLICT:
                return new CommunicationConflictResolutionStrategy();
            default:
                return new DefaultConflictResolutionStrategy();
        }
    }

    // Inner classes and interfaces extracted to top-level: SharedContext

    /**
     * Coordination statistics
     */

    // Enums extracted to top-level: CoordinationState, ConflictResolutionState

    // Default implementations moved to top-level strategy classes
}
