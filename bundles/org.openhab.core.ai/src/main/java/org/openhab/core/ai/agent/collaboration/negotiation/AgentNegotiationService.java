package org.openhab.core.ai.agent.collaboration.negotiation;

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


import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.agent.collaboration.negotiation.api.LearningNegotiationStrategy;
import org.openhab.core.ai.agent.collaboration.negotiation.api.NegotiationStrategy;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
import org.openhab.core.ai.common.monitoring.patterns.AgentCommunicationMetrics;
import org.openhab.core.ai.agent.lifecycle.api.AgentRegistry;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for managing agent negotiation sessions, protocols, and strategies
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@Component(service = AgentNegotiationService.class)
@NonNullByDefault
public class AgentNegotiationService {

    private final Logger logger = LoggerFactory.getLogger(AgentNegotiationService.class);

    // Core services
    private @Nullable AgentRegistry agentRegistry;

    // Session management
    private final Map<String, NegotiationSession> activeSessions = new ConcurrentHashMap<>();
    private final Map<String, NegotiationTemplate> templates = new ConcurrentHashMap<>();
    private final Map<String, NegotiationStrategy> strategies = new ConcurrentHashMap<>();

    // Performance monitoring - now handled by MetricsService

    // Background processors
    private final ScheduledExecutorService sessionProcessor = Executors.newScheduledThreadPool(2);
    private final ScheduledExecutorService learningProcessor = Executors.newScheduledThreadPool(1);
    private final ScheduledExecutorService analyticsProcessor = Executors.newScheduledThreadPool(1);

    @Activate
    public AgentNegotiationService() {
        initializeDefaultStrategies();
        initializeDefaultTemplates();
        startBackgroundProcessors();
    }

    @Deactivate
    public void deactivate() {
        sessionProcessor.shutdown();
        learningProcessor.shutdown();
        analyticsProcessor.shutdown();
        activeSessions.clear();
    }

    /**
     * Start a new negotiation session between agents
     */
    public CompletableFuture<NegotiationSession> startNegotiation(String sessionId, String initiatorId,
            Set<String> participantIds, String templateId, String strategyId, Map<String, Object> initialProposal) {
        try {
            // Validate participants
            AgentRegistry registry = agentRegistry;
            if (registry == null) {
                return CompletableFuture.completedFuture(null);
            }

            for (String participantId : participantIds) {
                if (registry.getAgent(participantId, "system") == null) {
                    return CompletableFuture.completedFuture(null);
                }
            }

            // Get template and strategy
            NegotiationTemplate template = templates.get(templateId);
            NegotiationStrategy strategy = strategies.get(strategyId);
            if (template == null || strategy == null) {
                return CompletableFuture.completedFuture(null);
            }

            // Create session
            NegotiationSession session = NegotiationSession.builder().withSessionId(sessionId)
                    .withInitiatorId(initiatorId).withParticipantIds(participantIds).withTemplateId(templateId)
                    .withStrategyId(strategyId).withInitialProposal(initialProposal)
                    .withStatus(NegotiationStatus.ACTIVE).withCreatedAt(Instant.now())
                    .withTimeoutAt(Instant.now().plus(template.getTimeout())).build();

            activeSessions.put(sessionId, session);
            recordTotalNegotiation(sessionId);

            logger.debug("Started negotiation session: {} with {} participants", sessionId, participantIds.size());
            return CompletableFuture.completedFuture(session);

        } catch (Exception e) {
            logger.error("Error starting negotiation session: {}", sessionId, e);
            return CompletableFuture.completedFuture(null);
        }
    }

    /**
     * Submit a proposal in an active negotiation session
     */
    public CompletableFuture<NegotiationResult> submitProposal(String sessionId, String agentId,
            Map<String, Object> proposal) {
        try {
            NegotiationSession session = activeSessions.get(sessionId);
            if (session == null) {
                return CompletableFuture.completedFuture(NegotiationResult.notFound("Session not found: " + sessionId));
            }

            if (session.getStatus() != NegotiationStatus.ACTIVE) {
                return CompletableFuture
                        .completedFuture(NegotiationResult.failure("Session is not active: " + session.getStatus()));
            }

            if (!session.getParticipantIds().contains(agentId)) {
                return CompletableFuture
                        .completedFuture(NegotiationResult.failure("Agent not a participant: " + agentId));
            }

            // Add proposal to session
            NegotiationProposal negotiationProposal = NegotiationProposal.builder().withSessionId(sessionId)
                    .withAgentId(agentId).withProposal(proposal).withSubmittedAt(Instant.now()).build();

            session.addProposal(negotiationProposal);

            // Apply negotiation strategy
            NegotiationStrategy strategy = strategies.get(session.getStrategyId());
            if (strategy != null) {
                NegotiationOutcome outcome = strategy.evaluateProposal(session, negotiationProposal);
                if (outcome.agreementReached()) {
                    session.setStatus(NegotiationStatus.AGREED);
                    session.setFinalAgreement(outcome.agreement());
                    session.setCompletedAt(Instant.now());
                    recordSuccessfulNegotiation(sessionId);
                    return CompletableFuture.completedFuture(NegotiationResult.agreement(session, outcome.agreement()));
                }
            }

            return CompletableFuture.completedFuture(NegotiationResult.proposalAccepted(session, negotiationProposal));

        } catch (Exception e) {
            logger.error("Error submitting proposal for session: {}", sessionId, e);
            return CompletableFuture.completedFuture(NegotiationResult.failure("Error: " + e.getMessage()));
        }
    }

    /**
     * Get current state of a negotiation session
     */
    public CompletableFuture<NegotiationSession> getSessionState(String sessionId) {
        NegotiationSession session = activeSessions.get(sessionId);
        if (session == null) {
            return CompletableFuture.completedFuture(null);
        }
        return CompletableFuture.completedFuture(session);
    }

    /**
     * Abort an active negotiation session
     */
    public CompletableFuture<NegotiationResult> abortNegotiation(String sessionId, String agentId, String reason) {
        try {
            NegotiationSession session = activeSessions.get(sessionId);
            if (session == null) {
                return CompletableFuture.completedFuture(NegotiationResult.notFound("Session not found: " + sessionId));
            }

            if (!session.getParticipantIds().contains(agentId)) {
                return CompletableFuture
                        .completedFuture(NegotiationResult.failure("Agent not a participant: " + agentId));
            }

            session.setStatus(NegotiationStatus.ABORTED);
            session.setAbortReason(reason);
            session.setCompletedAt(Instant.now());
            recordAbortedNegotiation(sessionId);

            logger.debug("Negotiation session aborted: {} by {} - {}", sessionId, agentId, reason);
            return CompletableFuture.completedFuture(NegotiationResult.aborted(session, reason));

        } catch (Exception e) {
            logger.error("Error aborting negotiation session: {}", sessionId, e);
            return CompletableFuture.completedFuture(NegotiationResult.failure("Error: " + e.getMessage()));
        }
    }

    /**
     * Get negotiation statistics
     */
    public NegotiationStatistics getStatistics() {
        return new NegotiationStatistics(0, 0, 0, 0, 0, activeSessions.size(),
                templates.size(), strategies.size()); // All metrics now come from MetricsService
    }

    /**
     * Register a new negotiation template
     */
    public void registerTemplate(NegotiationTemplate template) {
        templates.put(template.getTemplateId(), template);
        logger.debug("Registered negotiation template: {}", template.getTemplateId());
    }

    /**
     * Register a new negotiation strategy
     */
    public void registerStrategy(NegotiationStrategy strategy) {
        strategies.put(strategy.getStrategyId(), strategy);
        logger.debug("Registered negotiation strategy: {}", strategy.getStrategyId());
    }

    /**
     * Get negotiation history for an agent
     */
    public List<NegotiationSession> getAgentHistory(String agentId, int limit) {
        return activeSessions.values().stream().filter(session -> session.getParticipantIds().contains(agentId))
                .filter(session -> session.getStatus() != NegotiationStatus.ACTIVE)
                .sorted((s1, s2) -> s2.getCreatedAt().compareTo(s1.getCreatedAt())).limit(limit).toList();
    }

    /**
     * Get active sessions for an agent
     */
    public List<NegotiationSession> getActiveSessions(String agentId) {
        return activeSessions.values().stream().filter(session -> session.getParticipantIds().contains(agentId))
                .filter(session -> session.getStatus() == NegotiationStatus.ACTIVE).toList();
    }

    // Background processing methods
    private void processSessions() {
        Instant now = Instant.now();
        activeSessions.values().stream().filter(session -> session.getStatus() == NegotiationStatus.ACTIVE)
                .filter(session -> session.getTimeoutAt().isBefore(now)).forEach(session -> {
                    session.setStatus(NegotiationStatus.TIMEOUT);
                    session.setCompletedAt(now);
                    recordTimeoutNegotiation(session.getSessionId());
                    logger.debug("Negotiation session timed out: {}", session.getSessionId());
                });
    }

    private void updateLearningModels() {
        // Update learning models based on completed negotiations
        activeSessions.values().stream().filter(session -> session.getStatus() != NegotiationStatus.ACTIVE)
                .forEach(session -> {
                    NegotiationStrategy strategy = strategies.get(session.getStrategyId());
                    if (strategy instanceof LearningNegotiationStrategy learningStrategy) {
                        learningStrategy.learn(session);
                    }
                });
    }

    private void generateAnalytics() {
        // Generate analytics and performance metrics
        NegotiationStatistics stats = getStatistics();
        logger.debug("Negotiation statistics: {}", stats);
    }

    private void startBackgroundProcessors() {
        sessionProcessor.scheduleAtFixedRate(this::processSessions, 0, 30000, TimeUnit.MILLISECONDS); // 30 seconds
        learningProcessor.scheduleAtFixedRate(this::updateLearningModels, 0, 300000, TimeUnit.MILLISECONDS); // 5
                                                                                                             // minutes
        analyticsProcessor.scheduleAtFixedRate(this::generateAnalytics, 0, 600000, TimeUnit.MILLISECONDS); // 10 minutes
    }

    private void initializeDefaultStrategies() {
        registerStrategy(new DefaultNegotiationStrategy("default"));
        registerStrategy(new CompromiseNegotiationStrategy("compromise"));
        registerStrategy(new CompetitiveNegotiationStrategy("competitive"));
    }

    private void initializeDefaultTemplates() {
        registerTemplate(NegotiationTemplate.builder().withTemplateId("resource-allocation")
                .withName("Resource Allocation").withDescription("Template for resource allocation negotiations")
                .withTimeout(Duration.ofMinutes(10)).withMaxRounds(5).build());

        registerTemplate(NegotiationTemplate.builder().withTemplateId("task-delegation").withName("Task Delegation")
                .withDescription("Template for task delegation negotiations").withTimeout(Duration.ofMinutes(5))
                .withMaxRounds(3).build());

        registerTemplate(NegotiationTemplate.builder().withTemplateId("conflict-resolution")
                .withName("Conflict Resolution").withDescription("Template for conflict resolution negotiations")
                .withTimeout(Duration.ofMinutes(15)).withMaxRounds(7).build());
    }

    @Reference
    public void setAgentRegistry(AgentRegistry agentRegistry) {
        this.agentRegistry = agentRegistry;
    }

    @Reference
    private @Nullable MetricsService metricsService;

    public void unsetAgentRegistry(AgentRegistry agentRegistry) {
        this.agentRegistry = null;
    }

    // Data classes
    // extracted to top-level class

    // extracted to top-level class

    // Default strategy implementations
    // Default strategy implementations moved to top-level classes

    // Metrics recording methods - replacing removed AtomicLong fields using AgentCommunicationMetrics pattern

    /**
     * Record total negotiation - replaces totalNegotiations.incrementAndGet()
     */
    private void recordTotalNegotiation(String sessionId) {
        try {
            MetricsService metrics = metricsService;
            if (metrics != null) {
                // Use AgentCommunicationMetrics pattern for total negotiation
                AgentCommunicationMetrics.recordAgentCommunication(metrics, "negotiation-service", "total-negotiation", 
                        true, java.time.Duration.ZERO, 1, sessionId);
            }
        } catch (Exception e) {
            logger.warn("Failed to record total negotiation metric for session {}: {}", sessionId, e.getMessage());
        }
    }

    /**
     * Record successful negotiation - replaces successfulNegotiations.incrementAndGet()
     */
    private void recordSuccessfulNegotiation(String sessionId) {
        try {
            MetricsService metrics = metricsService;
            if (metrics != null) {
                // Use AgentCommunicationMetrics pattern for successful negotiation
                AgentCommunicationMetrics.recordAgentCommunication(metrics, "negotiation-service", "successful-negotiation", 
                        true, java.time.Duration.ZERO, 1, sessionId);
            }
        } catch (Exception e) {
            logger.warn("Failed to record successful negotiation metric for session {}: {}", sessionId, e.getMessage());
        }
    }

    /**
     * Record aborted negotiation - replaces abortedNegotiations.incrementAndGet()
     */
    private void recordAbortedNegotiation(String sessionId) {
        try {
            MetricsService metrics = metricsService;
            if (metrics != null) {
                // Use AgentCommunicationMetrics pattern for aborted negotiation
                AgentCommunicationMetrics.recordAgentCommunication(metrics, "negotiation-service", "aborted-negotiation", 
                        true, java.time.Duration.ZERO, 1, sessionId);
            }
        } catch (Exception e) {
            logger.warn("Failed to record aborted negotiation metric for session {}: {}", sessionId, e.getMessage());
        }
    }

    /**
     * Record timeout negotiation - replaces timeoutNegotiations.incrementAndGet()
     */
    private void recordTimeoutNegotiation(String sessionId) {
        try {
            MetricsService metrics = metricsService;
            if (metrics != null) {
                // Use AgentCommunicationMetrics pattern for timeout negotiation
                AgentCommunicationMetrics.recordAgentCommunication(metrics, "negotiation-service", "timeout-negotiation", 
                        true, java.time.Duration.ZERO, 1, sessionId);
            }
        } catch (Exception e) {
            logger.warn("Failed to record timeout negotiation metric for session {}: {}", sessionId, e.getMessage());
        }
    }
}
