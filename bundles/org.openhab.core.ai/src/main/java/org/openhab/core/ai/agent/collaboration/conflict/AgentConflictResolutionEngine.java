package org.openhab.core.ai.agent.collaboration.conflict;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.agent.lifecycle.api.AgentRegistry;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Agent Conflict Resolution Engine - Advanced conflict resolution system
 * 
 * This service provides comprehensive conflict resolution capabilities for agents:
 * - Conflict detection and analysis
 * - Conflict resolution strategies
 * - Conflict mediation and negotiation
 * - Conflict escalation procedures
 * - Conflict history and learning
 * - Conflict prevention mechanisms
 * - Conflict performance monitoring
 * - Conflict resolution protocols
 * - Conflict arbitration and decision making
 * - Conflict resolution analytics
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@Component(service = AgentConflictResolutionEngine.class)
@NonNullByDefault
public class AgentConflictResolutionEngine {

    private final Logger logger = LoggerFactory.getLogger(AgentConflictResolutionEngine.class);

    @Reference
    private @Nullable AgentRegistry agentRegistry;

    // Conflict management
    private final Map<String, Conflict> activeConflicts = new ConcurrentHashMap<>();
    private final Map<String, ConflictHistory> conflictHistory = new ConcurrentHashMap<>();
    private final Map<String, ConflictResolutionStrategy> resolutionStrategies = new ConcurrentHashMap<>();
    private final Map<String, ConflictMediator> mediators = new ConcurrentHashMap<>();

    // Conflict prevention and learning
    private final Map<String, ConflictPattern> conflictPatterns = new ConcurrentHashMap<>();
    private final Map<String, PreventionRule> preventionRules = new ConcurrentHashMap<>();
    private final Map<String, ConflictLearningModel> learningModels = new ConcurrentHashMap<>();

    // Performance monitoring
    private final AtomicLong totalConflictsDetected = new AtomicLong(0);
    private final AtomicLong totalConflictsResolved = new AtomicLong(0);
    private final AtomicLong totalConflictsEscalated = new AtomicLong(0);
    private final AtomicLong totalConflictsPrevented = new AtomicLong(0);
    private final AtomicLong totalResolutionTime = new AtomicLong(0);

    // Configuration
    private final AtomicReference<ConflictResolutionConfiguration> configuration = new AtomicReference<>(
            new ConflictResolutionConfiguration());

    // Background processing
    private final ScheduledExecutorService analysisProcessor = Executors.newSingleThreadScheduledExecutor();
    private final ScheduledExecutorService learningProcessor = Executors.newSingleThreadScheduledExecutor();
    private final ScheduledExecutorService preventionProcessor = Executors.newSingleThreadScheduledExecutor();

    @Activate
    public void activate() {
        logger.info("Agent Conflict Resolution Engine activated");

        // Start background processors
        analysisProcessor.scheduleAtFixedRate(this::performConflictAnalysis, 0, 60000, TimeUnit.MILLISECONDS); // 1
                                                                                                               // minute
        learningProcessor.scheduleAtFixedRate(this::updateLearningModels, 0, 300000, TimeUnit.MILLISECONDS); // 5
                                                                                                             // minutes
        preventionProcessor.scheduleAtFixedRate(this::applyPreventionRules, 0, 30000, TimeUnit.MILLISECONDS); // 30
                                                                                                              // seconds
    }

    @Deactivate
    public void deactivate() {
        logger.info("Agent Conflict Resolution Engine deactivated");

        // Shutdown background processors
        shutdownExecutor(analysisProcessor);
        shutdownExecutor(learningProcessor);
        shutdownExecutor(preventionProcessor);
    }

    /**
     * Detect and analyze conflicts
     * 
     * @param conflictData Conflict data to analyze
     * @param agentId Agent ID reporting the conflict
     * @return Conflict detection result
     */
    public CompletableFuture<ConflictDetectionResult> detectConflict(ConflictData conflictData, String agentId) {
        logger.debug("Agent {} reporting potential conflict: {}", agentId, conflictData);

        // Validate agent exists
        AgentRegistry registry = agentRegistry;
        if (registry == null || registry.getAgent(agentId, "system") == null) {
            return CompletableFuture.completedFuture(ConflictDetectionResult.failure("Agent not found: " + agentId));
        }

        // Analyze conflict data
        ConflictAnalysis analysis = analyzeConflictData(conflictData, agentId);
        if (!analysis.isConflictDetected()) {
            return CompletableFuture
                    .completedFuture(ConflictDetectionResult.noConflict("No conflict detected in the provided data"));
        }

        // Create conflict record
        Conflict conflict = Conflict.builder().conflictId(generateConflictId()).conflictData(conflictData)
                .reportedBy(agentId).detectedAt(Instant.now()).analysis(analysis).status(ConflictStatus.DETECTED)
                .priority(analysis.getPriority()).build();

        // Store conflict
        activeConflicts.put(conflict.getConflictId(), conflict);

        // Check for prevention rules
        if (shouldPreventConflict(conflict)) {
            totalConflictsPrevented.incrementAndGet();
            return CompletableFuture
                    .completedFuture(ConflictDetectionResult.prevented("Conflict prevented by prevention rules"));
        }

        // Update history
        updateConflictHistory(conflict);

        totalConflictsDetected.incrementAndGet();
        return CompletableFuture.completedFuture(ConflictDetectionResult.conflictDetected(conflict, analysis));
    }

    /**
     * Resolve conflict using specified strategy
     * 
     * @param conflictId Conflict identifier
     * @param strategyId Strategy to use for resolution
     * @param mediatorId Mediator to use (optional)
     * @return Conflict resolution result
     */
    public CompletableFuture<ConflictResolutionResult> resolveConflict(String conflictId, String strategyId,
            @Nullable String mediatorId) {
        logger.debug("Resolving conflict {} with strategy {} and mediator {}", conflictId, strategyId, mediatorId);

        // Get conflict
        Conflict conflict = activeConflicts.get(conflictId);
        if (conflict == null) {
            return CompletableFuture
                    .completedFuture(ConflictResolutionResult.notFound("Conflict not found: " + conflictId));
        }

        // Get resolution strategy
        ConflictResolutionStrategy strategy = resolutionStrategies.get(strategyId);
        if (strategy == null) {
            return CompletableFuture
                    .completedFuture(ConflictResolutionResult.failure("Resolution strategy not found: " + strategyId));
        }

        // Get mediator if specified
        ConflictMediator mediator = null;
        if (mediatorId != null) {
            mediator = mediators.get(mediatorId);
            if (mediator == null) {
                return CompletableFuture
                        .completedFuture(ConflictResolutionResult.failure("Mediator not found: " + mediatorId));
            }
        }

        // Start resolution process
        Instant resolutionStart = Instant.now();
        conflict.setStatus(ConflictStatus.RESOLVING);
        conflict.setResolutionStrategy(strategyId);
        conflict.setMediator(mediatorId);

        try {
            // Apply resolution strategy
            ConflictResolution resolution = strategy.resolve(conflict, mediator);

            // Update conflict with resolution
            conflict.setResolution(resolution);
            conflict.setResolvedAt(Instant.now());
            conflict.setStatus(ConflictStatus.RESOLVED);

            // Calculate resolution time
            long resolutionTimeMs = Duration.between(resolutionStart, conflict.getResolvedAt()).toMillis();
            totalResolutionTime.addAndGet(resolutionTimeMs);

            // Remove from active conflicts
            activeConflicts.remove(conflictId);

            // Update history
            updateConflictHistory(conflict);

            totalConflictsResolved.incrementAndGet();
            return CompletableFuture.completedFuture(ConflictResolutionResult.success(conflict, resolution));

        } catch (Exception e) {
            logger.error("Error resolving conflict: {}", conflictId, e);
            conflict.setStatus(ConflictStatus.FAILED);
            return CompletableFuture.completedFuture(
                    ConflictResolutionResult.failure("Error during conflict resolution: " + e.getMessage()));
        }
    }

    /**
     * Escalate conflict to higher authority
     * 
     * @param conflictId Conflict identifier
     * @param escalationReason Reason for escalation
     * @param escalationLevel Escalation level
     * @return Conflict escalation result
     */
    public CompletableFuture<ConflictEscalationResult> escalateConflict(String conflictId, String escalationReason,
            EscalationLevel escalationLevel) {
        logger.debug("Escalating conflict {} to level {}: {}", conflictId, escalationLevel, escalationReason);

        // Get conflict
        Conflict conflict = activeConflicts.get(conflictId);
        if (conflict == null) {
            return CompletableFuture
                    .completedFuture(ConflictEscalationResult.notFound("Conflict not found: " + conflictId));
        }

        // Create escalation record
        ConflictEscalation escalation = ConflictEscalation.builder().conflictId(conflictId)
                .escalationReason(escalationReason).escalationLevel(escalationLevel).escalatedAt(Instant.now()).build();

        // Update conflict
        conflict.setEscalation(escalation);
        conflict.setStatus(ConflictStatus.ESCALATED);

        // Apply escalation procedures
        applyEscalationProcedures(conflict, escalation);

        totalConflictsEscalated.incrementAndGet();
        return CompletableFuture.completedFuture(ConflictEscalationResult.success(conflict, escalation));
    }

    /**
     * Register conflict resolution strategy
     * 
     * @param strategyId Strategy identifier
     * @param strategy Resolution strategy implementation
     */
    public void registerResolutionStrategy(String strategyId, ConflictResolutionStrategy strategy) {
        resolutionStrategies.put(strategyId, strategy);
        logger.debug("Registered conflict resolution strategy: {}", strategyId);
    }

    /**
     * Register conflict mediator
     * 
     * @param mediatorId Mediator identifier
     * @param mediator Mediator implementation
     */
    public void registerMediator(String mediatorId, ConflictMediator mediator) {
        mediators.put(mediatorId, mediator);
        logger.debug("Registered conflict mediator: {}", mediatorId);
    }

    /**
     * Add conflict prevention rule
     * 
     * @param ruleId Rule identifier
     * @param rule Prevention rule
     */
    public void addPreventionRule(String ruleId, PreventionRule rule) {
        preventionRules.put(ruleId, rule);
        logger.debug("Added conflict prevention rule: {}", ruleId);
    }

    /**
     * Get conflict history for agent
     * 
     * @param agentId Agent identifier
     * @param limit Maximum number of conflicts
     * @return Conflict history
     */
    public List<Conflict> getConflictHistory(String agentId, int limit) {
        ConflictHistory history = conflictHistory.get(agentId);
        if (history == null) {
            return List.of();
        }

        return history.getConflicts().stream().sorted((c1, c2) -> c2.getDetectedAt().compareTo(c1.getDetectedAt()))
                .limit(limit).toList();
    }

    /**
     * Get conflict resolution statistics
     * 
     * @return Conflict resolution statistics
     */
    public ConflictResolutionStatistics getStatistics() {
        return new ConflictResolutionStatistics(totalConflictsDetected.get(), totalConflictsResolved.get(),
                totalConflictsEscalated.get(), totalConflictsPrevented.get(), totalResolutionTime.get(),
                activeConflicts.size(), conflictHistory.size(), resolutionStrategies.size(), mediators.size());
    }

    /**
     * Analyze conflict patterns
     * 
     * @param timeRange Time range for analysis
     * @return Conflict pattern analysis
     */
    public CompletableFuture<ConflictPatternAnalysis> analyzeConflictPatterns(Duration timeRange) {
        logger.debug("Analyzing conflict patterns for time range: {}", timeRange);

        Instant cutoffTime = Instant.now().minus(timeRange);

        // Collect conflicts in time range
        List<Conflict> conflictsInRange = conflictHistory.values().stream()
                .flatMap(history -> history.getConflicts().stream())
                .filter(conflict -> conflict.getDetectedAt().isAfter(cutoffTime)).toList();

        // Analyze patterns
        Map<String, Integer> agentConflictCounts = conflictsInRange.stream()
                .collect(java.util.stream.Collectors.groupingBy(Conflict::getReportedBy, java.util.stream.Collectors
                        .collectingAndThen(java.util.stream.Collectors.counting(), Long::intValue)));

        Map<ConflictType, Integer> typeConflictCounts = conflictsInRange.stream()
                .collect(java.util.stream.Collectors.groupingBy(conflict -> conflict.getAnalysis().getConflictType(),
                        java.util.stream.Collectors.collectingAndThen(java.util.stream.Collectors.counting(),
                                Long::intValue)));

        Map<ConflictPriority, Integer> priorityConflictCounts = conflictsInRange.stream()
                .collect(java.util.stream.Collectors.groupingBy(Conflict::getPriority, java.util.stream.Collectors
                        .collectingAndThen(java.util.stream.Collectors.counting(), Long::intValue)));

        ConflictPatternAnalysis analysis = new ConflictPatternAnalysis(conflictsInRange.size(), agentConflictCounts,
                typeConflictCounts, priorityConflictCounts, timeRange);

        return CompletableFuture.completedFuture(analysis);
    }

    /**
     * Train conflict learning model
     * 
     * @param modelId Model identifier
     * @param trainingData Training data
     * @return Training result
     */
    public CompletableFuture<ModelTrainingResult> trainLearningModel(String modelId, List<Conflict> trainingData) {
        logger.debug("Training conflict learning model: {} with {} conflicts", modelId, trainingData.size());

        // Create or update learning model
        ConflictLearningModel model = learningModels.computeIfAbsent(modelId,
                id -> new DefaultConflictLearningModel(id));

        // Train the model
        try {
            model.train(trainingData);
            return CompletableFuture.completedFuture(ModelTrainingResult
                    .success("Model trained successfully with " + trainingData.size() + " conflicts"));
        } catch (Exception e) {
            logger.error("Error training model: {}", modelId, e);
            return CompletableFuture
                    .completedFuture(ModelTrainingResult.failure("Error training model: " + e.getMessage()));
        }
    }

    // Private helper methods

    private String generateConflictId() {
        return "conflict_" + System.currentTimeMillis() + "_" + Thread.currentThread().getId();
    }

    private ConflictAnalysis analyzeConflictData(ConflictData conflictData, String agentId) {
        // Analyze conflict data to determine if it's actually a conflict
        // This is a placeholder for actual conflict analysis logic

        ConflictType conflictType = determineConflictType(conflictData);
        ConflictPriority priority = determineConflictPriority(conflictData);
        boolean isConflict = conflictType != ConflictType.NONE;

        return new ConflictAnalysis(isConflict, conflictType, priority, conflictData.getSeverity(),
                conflictData.getDescription());
    }

    private ConflictType determineConflictType(ConflictData conflictData) {
        // Determine conflict type based on data
        // This is a placeholder for actual conflict type determination logic

        if (conflictData.getSeverity() > 7) {
            return ConflictType.RESOURCE_CONFLICT;
        } else if (conflictData.getSeverity() > 5) {
            return ConflictType.POLICY_CONFLICT;
        } else if (conflictData.getSeverity() > 3) {
            return ConflictType.COORDINATION_CONFLICT;
        } else {
            return ConflictType.NONE;
        }
    }

    private ConflictPriority determineConflictPriority(ConflictData conflictData) {
        // Determine conflict priority based on data
        // This is a placeholder for actual priority determination logic

        if (conflictData.getSeverity() > 8) {
            return ConflictPriority.CRITICAL;
        } else if (conflictData.getSeverity() > 6) {
            return ConflictPriority.HIGH;
        } else if (conflictData.getSeverity() > 4) {
            return ConflictPriority.MEDIUM;
        } else {
            return ConflictPriority.LOW;
        }
    }

    private boolean shouldPreventConflict(Conflict conflict) {
        // Check prevention rules
        for (PreventionRule rule : preventionRules.values()) {
            if (rule.shouldPrevent(conflict)) {
                logger.debug("Conflict {} prevented by rule: {}", conflict.getConflictId(), rule.getRuleId());
                return true;
            }
        }
        return false;
    }

    private void updateConflictHistory(Conflict conflict) {
        String agentId = conflict.getReportedBy();
        conflictHistory.computeIfAbsent(agentId, id -> new ConflictHistory(id)).addConflict(conflict);
    }

    private void applyEscalationProcedures(Conflict conflict, ConflictEscalation escalation) {
        // Apply escalation procedures based on level
        // This is a placeholder for actual escalation procedures

        switch (escalation.getEscalationLevel()) {
            case SUPERVISOR:
                // Notify supervisor
                logger.info("Conflict {} escalated to supervisor: {}", conflict.getConflictId(),
                        escalation.getEscalationReason());
                break;
            case ADMINISTRATOR:
                // Notify administrator
                logger.warn("Conflict {} escalated to administrator: {}", conflict.getConflictId(),
                        escalation.getEscalationReason());
                break;
            case SYSTEM:
                // System-level intervention
                logger.error("Conflict {} escalated to system level: {}", conflict.getConflictId(),
                        escalation.getEscalationReason());
                break;
        }
    }

    private void performConflictAnalysis() {
        try {
            logger.debug("Performing conflict analysis");

            // Analyze active conflicts for patterns and trends
            for (Conflict conflict : activeConflicts.values()) {
                try {
                    // Analyze conflict patterns
                    analyzeConflictPattern(conflict);

                    // Check for escalation conditions
                    checkEscalationConditions(conflict);

                    // Update conflict metrics
                    updateConflictMetrics(conflict);

                } catch (Exception e) {
                    logger.error("Error analyzing conflict {}: {}", conflict.getConflictId(), e.getMessage());
                }
            }

            // Generate conflict trend analysis
            generateConflictTrendAnalysis();

        } catch (Exception e) {
            logger.error("Error in conflict analysis: {}", e.getMessage(), e);
        }
    }

    private void analyzeConflictPattern(Conflict conflict) {
        try {
            // Analyze conflict characteristics
            ConflictType type = conflict.getAnalysis().getConflictType();
            ConflictPriority priority = conflict.getPriority();
            int severity = conflict.getAnalysis().getSeverity();

            // Track pattern statistics
            String patternKey = type + "_" + priority + "_" + severity;
            conflictPatterns.computeIfAbsent(patternKey, k -> new DefaultConflictPattern(k)).recordOccurrence(conflict);

            logger.debug("Analyzed conflict pattern for conflict {}: {}", conflict.getConflictId(), patternKey);

        } catch (Exception e) {
            logger.error("Error analyzing conflict pattern for conflict {}: {}", conflict.getConflictId(),
                    e.getMessage());
        }
    }

    private void checkEscalationConditions(Conflict conflict) {
        try {
            ConflictResolutionConfiguration config = configuration.get();

            if (config.isEnableAutoEscalation()) {
                // Check if conflict has been active too long
                Duration conflictAge = Duration.between(conflict.getDetectedAt(), Instant.now());

                if (conflictAge.compareTo(config.getAutoEscalationDelay()) > 0) {
                    // Auto-escalate the conflict
                    escalateConflict(conflict.getConflictId(),
                            "Auto-escalation: conflict active for " + conflictAge.toMinutes() + " minutes",
                            EscalationLevel.SUPERVISOR);

                    logger.info("Auto-escalated conflict {} after {} minutes", conflict.getConflictId(),
                            conflictAge.toMinutes());
                }
            }

        } catch (Exception e) {
            logger.error("Error checking escalation conditions for conflict {}: {}", conflict.getConflictId(),
                    e.getMessage());
        }
    }

    private void updateConflictMetrics(Conflict conflict) {
        try {
            // Update conflict metrics for monitoring
            String agentId = conflict.getReportedBy();
            ConflictHistory history = conflictHistory.get(agentId);

            if (history != null) {
                // Update agent-specific metrics
                updateAgentConflictMetrics(agentId, conflict);
            }

        } catch (Exception e) {
            logger.error("Error updating conflict metrics for conflict {}: {}", conflict.getConflictId(),
                    e.getMessage());
        }
    }

    private void updateAgentConflictMetrics(String agentId, Conflict conflict) {
        try {
            // Update agent-specific conflict statistics
            // This could include frequency, types, resolution success rates, etc.
            logger.debug("Updated conflict metrics for agent {}: conflict {}", agentId, conflict.getConflictId());

        } catch (Exception e) {
            logger.error("Error updating agent conflict metrics for agent {}: {}", agentId, e.getMessage());
        }
    }

    private void generateConflictTrendAnalysis() {
        try {
            // Generate trend analysis for recent conflicts
            Duration analysisWindow = Duration.ofHours(1);
            Instant cutoffTime = Instant.now().minus(analysisWindow);

            long recentConflicts = conflictHistory.values().stream().flatMap(history -> history.getConflicts().stream())
                    .filter(conflict -> conflict.getDetectedAt().isAfter(cutoffTime)).count();

            if (recentConflicts > 10) {
                logger.warn("High conflict rate detected: {} conflicts in the last hour", recentConflicts);
            }

        } catch (Exception e) {
            logger.error("Error generating conflict trend analysis: {}", e.getMessage());
        }
    }

    private void updateLearningModels() {
        try {
            logger.debug("Updating conflict learning models");

            // Update learning models with recent conflict data
            for (Map.Entry<String, ConflictLearningModel> entry : learningModels.entrySet()) {
                String modelId = entry.getKey();
                ConflictLearningModel model = entry.getValue();

                try {
                    // Get recent conflicts for training
                    List<Conflict> recentConflicts = getRecentConflictsForTraining();

                    if (!recentConflicts.isEmpty()) {
                        // Retrain the model with new data
                        model.train(recentConflicts);

                        logger.debug("Updated learning model {} with {} recent conflicts", modelId,
                                recentConflicts.size());
                    }

                } catch (Exception e) {
                    logger.error("Error updating learning model {}: {}", modelId, e.getMessage());
                }
            }

        } catch (Exception e) {
            logger.error("Error in learning model updates: {}", e.getMessage(), e);
        }
    }

    private List<Conflict> getRecentConflictsForTraining() {
        try {
            // Get conflicts from the last 24 hours for training
            Duration trainingWindow = Duration.ofHours(24);
            Instant cutoffTime = Instant.now().minus(trainingWindow);

            return conflictHistory.values().stream().flatMap(history -> history.getConflicts().stream())
                    .filter(conflict -> conflict.getDetectedAt().isAfter(cutoffTime))
                    .filter(conflict -> conflict.getStatus() == ConflictStatus.RESOLVED).limit(100) // Limit to prevent
                                                                                                    // memory issues
                    .toList();

        } catch (Exception e) {
            logger.error("Error getting recent conflicts for training: {}", e.getMessage());
            return List.of();
        }
    }

    private void applyPreventionRules() {
        try {
            logger.debug("Applying conflict prevention rules");

            // Apply prevention rules to active conflicts
            for (Conflict conflict : activeConflicts.values()) {
                try {
                    // Check if any prevention rules should be applied
                    for (PreventionRule rule : preventionRules.values()) {
                        if (rule.shouldPrevent(conflict)) {
                            // Apply prevention action
                            applyPreventionAction(conflict, rule);
                            break; // Only apply one rule per conflict
                        }
                    }

                } catch (Exception e) {
                    logger.error("Error applying prevention rules to conflict {}: {}", conflict.getConflictId(),
                            e.getMessage());
                }
            }

        } catch (Exception e) {
            logger.error("Error in prevention rule application: {}", e.getMessage(), e);
        }
    }

    private void applyPreventionAction(Conflict conflict, PreventionRule rule) {
        try {
            logger.info("Applying prevention rule {} to conflict {}", rule.getRuleId(), conflict.getConflictId());

            // Apply the prevention action based on the rule
            // This could involve modifying the conflict, notifying agents, etc.

            // Mark conflict as prevented
            conflict.setStatus(ConflictStatus.RESOLVED);

            // Remove from active conflicts
            activeConflicts.remove(conflict.getConflictId());

            // Update statistics
            totalConflictsPrevented.incrementAndGet();

        } catch (Exception e) {
            logger.error("Error applying prevention action for conflict {}: {}", conflict.getConflictId(),
                    e.getMessage());
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

    // Inner classes and interfaces extracted to top-level types in
    // org.openhab.core.ai.agent.collaboration.conflict
}
