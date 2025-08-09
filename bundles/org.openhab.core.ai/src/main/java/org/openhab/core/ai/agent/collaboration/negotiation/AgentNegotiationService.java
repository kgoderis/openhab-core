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
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.agent.lifecycle.AgentRegistry;
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

    // Performance monitoring
    private final AtomicLong totalNegotiations = new AtomicLong(0);
    private final AtomicLong successfulNegotiations = new AtomicLong(0);
    private final AtomicLong failedNegotiations = new AtomicLong(0);
    private final AtomicLong timeoutNegotiations = new AtomicLong(0);
    private final AtomicLong abortedNegotiations = new AtomicLong(0);

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
            NegotiationSession session = NegotiationSession.builder().sessionId(sessionId).initiatorId(initiatorId)
                    .participantIds(participantIds).templateId(templateId).strategyId(strategyId)
                    .initialProposal(initialProposal).status(NegotiationStatus.ACTIVE).createdAt(Instant.now())
                    .timeoutAt(Instant.now().plus(template.getTimeout())).build();

            activeSessions.put(sessionId, session);
            totalNegotiations.incrementAndGet();

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
            NegotiationProposal negotiationProposal = NegotiationProposal.builder().sessionId(sessionId)
                    .agentId(agentId).proposal(proposal).submittedAt(Instant.now()).build();

            session.addProposal(negotiationProposal);

            // Apply negotiation strategy
            NegotiationStrategy strategy = strategies.get(session.getStrategyId());
            if (strategy != null) {
                NegotiationOutcome outcome = strategy.evaluateProposal(session, negotiationProposal);
                if (outcome.agreementReached()) {
                    session.setStatus(NegotiationStatus.AGREED);
                    session.setFinalAgreement(outcome.agreement());
                    session.setCompletedAt(Instant.now());
                    successfulNegotiations.incrementAndGet();
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
            abortedNegotiations.incrementAndGet();

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
        return new NegotiationStatistics(totalNegotiations.get(), successfulNegotiations.get(),
                failedNegotiations.get(), timeoutNegotiations.get(), abortedNegotiations.get(), activeSessions.size(),
                templates.size(), strategies.size());
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
                    timeoutNegotiations.incrementAndGet();
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
        registerTemplate(NegotiationTemplate.builder().templateId("resource-allocation").name("Resource Allocation")
                .description("Template for resource allocation negotiations").timeout(Duration.ofMinutes(10))
                .maxRounds(5).build());

        registerTemplate(NegotiationTemplate.builder().templateId("task-delegation").name("Task Delegation")
                .description("Template for task delegation negotiations").timeout(Duration.ofMinutes(5)).maxRounds(3)
                .build());

        registerTemplate(NegotiationTemplate.builder().templateId("conflict-resolution").name("Conflict Resolution")
                .description("Template for conflict resolution negotiations").timeout(Duration.ofMinutes(15))
                .maxRounds(7).build());
    }

    @Reference
    public void setAgentRegistry(AgentRegistry agentRegistry) {
        this.agentRegistry = agentRegistry;
    }

    public void unsetAgentRegistry(AgentRegistry agentRegistry) {
        this.agentRegistry = null;
    }

    // Data classes
    public record NegotiationStatistics(long totalNegotiations, long successfulNegotiations, long failedNegotiations,
            long timeoutNegotiations, long abortedNegotiations, int activeSessions, int templates, int strategies) {
    }

    public record NegotiationResult(String sessionId, NegotiationStatus status, String message,
            @Nullable Map<String, Object> agreement, @Nullable NegotiationProposal proposal) {

        static NegotiationResult agreement(NegotiationSession session, Map<String, Object> agreement) {
            return new NegotiationResult(session.getSessionId(), NegotiationStatus.AGREED, "Agreement reached",
                    agreement, null);
        }

        static NegotiationResult proposalAccepted(NegotiationSession session, NegotiationProposal proposal) {
            return new NegotiationResult(session.getSessionId(), NegotiationStatus.ACTIVE, "Proposal accepted", null,
                    proposal);
        }

        static NegotiationResult aborted(NegotiationSession session, String reason) {
            return new NegotiationResult(session.getSessionId(), NegotiationStatus.ABORTED, "Aborted: " + reason, null,
                    null);
        }

        static NegotiationResult notFound(String message) {
            return new NegotiationResult("", NegotiationStatus.NOT_FOUND, message, null, null);
        }

        static NegotiationResult failure(String message) {
            return new NegotiationResult("", NegotiationStatus.FAILED, message, null, null);
        }
    }

    public record NegotiationOutcome(boolean agreementReached, @Nullable Map<String, Object> agreement, String reason) {

        static NegotiationOutcome agreement(Map<String, Object> agreement) {
            return new NegotiationOutcome(true, agreement, "Agreement reached");
        }

        static NegotiationOutcome noAgreement(String reason) {
            return new NegotiationOutcome(false, null, reason);
        }
    }

    // Interfaces
    public interface NegotiationStrategy {
        String getStrategyId();

        NegotiationOutcome evaluateProposal(NegotiationSession session, NegotiationProposal proposal);
    }

    public interface LearningNegotiationStrategy extends NegotiationStrategy {
        void learn(NegotiationSession session);
    }

    // Default strategy implementations
    public static class DefaultNegotiationStrategy implements NegotiationStrategy {
        private final String strategyId;

        public DefaultNegotiationStrategy(String strategyId) {
            this.strategyId = strategyId;
        }

        @Override
        public String getStrategyId() {
            return strategyId;
        }

        @Override
        public NegotiationOutcome evaluateProposal(NegotiationSession session, NegotiationProposal proposal) {
            // Simple agreement if all participants have submitted proposals
            if (session.getProposals().size() >= session.getParticipantIds().size()) {
                return NegotiationOutcome.agreement(Map.of("agreement", "default"));
            }
            return NegotiationOutcome.noAgreement("Waiting for more proposals");
        }
    }

    public static class CompromiseNegotiationStrategy implements NegotiationStrategy {
        private final String strategyId;

        public CompromiseNegotiationStrategy(String strategyId) {
            this.strategyId = strategyId;
        }

        @Override
        public String getStrategyId() {
            return strategyId;
        }

        @Override
        public NegotiationOutcome evaluateProposal(NegotiationSession session, NegotiationProposal proposal) {
            // Compromise-based agreement
            if (session.getProposals().size() >= 2) {
                return NegotiationOutcome.agreement(Map.of("agreement", "compromise"));
            }
            return NegotiationOutcome.noAgreement("Need at least 2 proposals for compromise");
        }
    }

    public static class CompetitiveNegotiationStrategy implements NegotiationStrategy {
        private final String strategyId;

        public CompetitiveNegotiationStrategy(String strategyId) {
            this.strategyId = strategyId;
        }

        @Override
        public String getStrategyId() {
            return strategyId;
        }

        @Override
        public NegotiationOutcome evaluateProposal(NegotiationSession session, NegotiationProposal proposal) {
            // Competitive agreement based on proposal strength
            if (session.getProposals().size() >= session.getParticipantIds().size()) {
                return NegotiationOutcome.agreement(Map.of("agreement", "competitive"));
            }
            return NegotiationOutcome.noAgreement("Waiting for all competitive proposals");
        }
    }
}
