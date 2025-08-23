package org.openhab.core.ai.agents;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.action.ActionRegistry;
import org.openhab.core.ai.action.api.Action;
import org.openhab.core.ai.action.api.ActionError;
import org.openhab.core.ai.action.api.ActionKeys;
import org.openhab.core.ai.action.api.ActionResult;
import org.openhab.core.ai.agent.api.IntelligentAgent;
import org.openhab.core.ai.common.context.ExecutionContext;
import org.openhab.core.ai.common.context.ReasoningContext;
import org.openhab.core.ai.common.response.ModelResponse;
import org.openhab.core.ai.model.ModelParameters;
import org.openhab.core.ai.model.ModelResponseActionParser;
import org.openhab.core.ai.model.api.ModelClient;
import org.openhab.core.ai.reasoning.api.MultiStepReasoningResult;
import org.openhab.core.ai.reasoning.engine.AgentModelDecisionEngine;
import org.openhab.core.ai.reasoning.engine.MultiStepReasoningEngine;
import org.openhab.core.ai.reasoning.engine.ReasoningOrchestrationService;
import org.openhab.core.ai.reasoning.engine.SharedModelReasoningEngine;
import org.openhab.core.ai.reasoning.engine.analysis.ReasoningPatternAnalysis;
import org.openhab.core.ai.reasoning.engine.analysis.ReasoningPerformanceAnalysis;
import org.openhab.core.ai.reasoning.engine.analysis.ReasoningRecommendation;
import org.openhab.core.ai.reasoning.engine.analysis.ReasoningStepAnalysisService;
import org.openhab.core.ai.reasoning.optimization.AgentModelDecisionOptimizer;
import org.openhab.core.ai.reasoning.patterns.BehaviorPattern;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract Intelligent Agent Implementation
 * 
 * <p>
 * This abstract class provides a concrete implementation of IntelligentAgent that:
 * - Integrates MultiStepReasoningEngine for intelligent decision making
 * - Uses ActionRegistry for executing concrete actions
 * - Leverages ModelClient for LLM-based reasoning
 * - Provides learning and adaptation capabilities
 * - Manages agent knowledge and context
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 */
@NonNullByDefault
public abstract class AbstractIntelligentAgent extends BaseAutonomousAgent implements IntelligentAgent {
    private String generateActionId() {
        return getAgentId() + "_action_" + System.currentTimeMillis() + "_" + Thread.currentThread().getId();
    }

    private List<Action> getRegisteredActions() {
        ActionRegistry registry = actionRegistry;
        if (registry == null) {
            return List.of();
        }
        return new ArrayList<>(registry.getAllActions().values());
    }

    private static final Logger logger = LoggerFactory.getLogger(AbstractIntelligentAgent.class);

    // Dependencies
    @Reference
    private @Nullable MultiStepReasoningEngine reasoningEngine;

    @Reference
    private @Nullable ActionRegistry actionRegistry;

    @Reference
    private @Nullable ModelClient modelClient;

    @Reference
    private @Nullable ModelResponseActionParser actionCallParser;

    @Reference
    private @Nullable SharedModelReasoningEngine sharedReasoningEngine;

    @Reference
    private @Nullable AgentModelDecisionEngine decisionEngine;

    @Reference
    private @Nullable ReasoningOrchestrationService orchestrationService;

    @Reference
    private @Nullable ReasoningStepAnalysisService analysisService;

    @Reference
    private @Nullable AgentModelDecisionOptimizer optimizer;

    // Agent knowledge and learning
    private final Map<String, Object> agentKnowledge = new HashMap<>();
    private final Map<String, List<LearningExample>> learningHistory = new HashMap<>();

    @Override
    public CompletableFuture<ActionResult> executeIntelligentAction(String actionName, Map<String, Object> parameters) {
        return CompletableFuture.supplyAsync(() -> {
            String actionId = generateActionId();
            Instant startTime = Instant.now();

            try {
                logger.debug("Agent {} executing intelligent action: {}", getAgentId(), actionName);

                // Initialize specialized reasoning components if not already done
                initializeSpecializedComponentsIfNeeded();

                // 1. Create reasoning context
                ReasoningContext reasoningContext = createEnhancedReasoningContext(actionName, parameters);

                // 2. Execute reasoning using orchestration service if available, otherwise use basic reasoning
                MultiStepReasoningResult reasoningResult;
                ReasoningOrchestrationService orchestration = orchestrationService;
                if (orchestration != null && modelClient != null) {
                    // Use orchestration service for coordinated reasoning across multiple engines
                    // Create model parameters for the orchestration
                    ModelParameters modelParams = ModelParameters.builder().withMaxTokens(2048).withTemperature(0.7)
                            .withTimeoutMs(30000).build();

                    // For now, use the basic reasoning engine since orchestration requires IntelligentToolClient
                    // TODO: Implement IntelligentToolClient integration for full orchestration
                    MultiStepReasoningEngine engine = reasoningEngine;
                    if (engine == null) {
                        throw new IllegalStateException("No reasoning engine available");
                    }
                    reasoningResult = engine.reasonAsync(reasoningContext).get();
                } else {
                    // Fallback to basic multi-step reasoning
                    MultiStepReasoningEngine engine = reasoningEngine;
                    if (engine == null) {
                        throw new IllegalStateException("No reasoning engine available");
                    }
                    reasoningResult = engine.reasonAsync(reasoningContext).get();
                }

                // 2.5. Analyze and optimize reasoning patterns
                analyzeAndOptimizeReasoning(reasoningResult);

                // 3. Parse reasoning result to extract concrete actions
                List<ExecutionContext> actionsToExecute = parseReasoningToActions(reasoningResult, actionName,
                        parameters);

                // 4. Execute concrete actions
                List<ActionResult> actionResults = executeActionsWithMonitoring(actionsToExecute);

                // 5. Aggregate results
                ActionResult finalResult = aggregateResults(actionId, actionResults, reasoningResult);

                // 6. Learn from the execution
                learnFromAction(actionName, parameters, finalResult, finalResult.isSuccess());

                logger.debug("Agent {} completed intelligent action: {} in {}ms", getAgentId(), actionName,
                        Duration.between(startTime, Instant.now()).toMillis());

                return finalResult;

            } catch (Exception e) {
                logger.error("Agent {} failed to execute intelligent action: {}", getAgentId(), actionName, e);
                return ActionResult.error("Intelligent action execution failed: " + e.getMessage(), null, 0);
            } finally {
                totalProcessingTime.addAndGet(Duration.between(startTime, Instant.now()).toMillis());
            }
        });
    }

    @Override
    public CompletableFuture<List<ExecutionContext>> planActions(String goal, Map<String, Object> context) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.debug("Agent {} planning actions for goal: {}", getAgentId(), goal);

                // Create reasoning context for planning
                Map<String, Object> metadata = new HashMap<>();
                metadata.put("context", context);
                metadata.put("agentContext", getContext().getAllValues());
                metadata.put("knowledge", agentKnowledge);

                ReasoningContext reasoningContext = ReasoningContext.builder().withInitialContext("Goal: " + goal)
                        .withCurrentContext("Goal: " + goal).withDomain(getSpecialization())
                        .withSessionId("session-" + System.currentTimeMillis()).withMetadata(metadata).build();

                // Use decision engine if available for enhanced planning
                AgentModelDecisionEngine decision = decisionEngine;
                if (decision != null) {
                    // Use decision engine for sophisticated planning
                    // TODO: Implement decision engine integration when available
                    logger.debug("Decision engine available but not yet integrated");
                }

                // Use shared reasoning engine if available for collaborative planning
                SharedModelReasoningEngine shared = sharedReasoningEngine;
                if (shared != null) {
                    // Use shared reasoning engine for collaborative planning
                    // TODO: Implement shared reasoning engine integration when available
                    logger.debug("Shared reasoning engine available but not yet integrated");
                }

                // Execute reasoning for planning
                MultiStepReasoningResult reasoningResult = reasoningEngine.reasonAsync(reasoningContext).get();

                // Parse reasoning to extract planned actions
                return parseReasoningToActions(reasoningResult, "plan", Map.of("goal", goal));

            } catch (Exception e) {
                logger.error("Agent {} failed to plan actions for goal: {}", getAgentId(), goal, e);
                return new ArrayList<>();
            }
        });
    }

    @Override
    public CompletableFuture<MultiStepReasoningResult> reason(ReasoningContext reasoningContext) {
        if (reasoningEngine == null) {
            return CompletableFuture.failedFuture(new IllegalStateException("Reasoning engine not available"));
        }
        return reasoningEngine.reasonAsync(reasoningContext);
    }

    @Override
    public void learnFromAction(String actionName, Map<String, Object> parameters, ActionResult result,
            boolean success) {
        try {
            // Create learning example
            LearningExample example = new LearningExample(actionName, parameters, result, success,
                    getContext().getAllValues(), Instant.now());

            // Store in learning history
            learningHistory.computeIfAbsent(actionName, k -> new ArrayList<>()).add(example);

            // Update agent knowledge based on learning
            updateKnowledgeFromLearning(example);

            logger.debug("Agent {} learned from action: {} (success: {})", getAgentId(), actionName, success);

        } catch (Exception e) {
            logger.error("Agent {} failed to learn from action: {}", getAgentId(), actionName, e);
        }
    }

    @Override
    public @Nullable MultiStepReasoningEngine getReasoningEngine() {
        return reasoningEngine;
    }

    @Override
    public @Nullable ActionRegistry getActionRegistry() {
        return actionRegistry;
    }

    @Override
    public @Nullable ModelClient getModelClient() {
        return modelClient;
    }

    @Override
    public void updateKnowledge(Map<String, Object> knowledge) {
        agentKnowledge.putAll(knowledge);
        logger.debug("Agent {} updated knowledge with {} entries", getAgentId(), knowledge.size());
    }

    @Override
    public Map<String, Object> getCapabilities() {
        Map<String, Object> capabilities = new HashMap<>();
        capabilities.put("specialization", getSpecialization());
        capabilities.put("reasoning", true);
        capabilities.put("learning", true);
        capabilities.put("actions", getRegisteredActions().size());
        capabilities.put("knowledge", agentKnowledge.size());

        // Add specialized reasoning component capabilities
        capabilities.put("patternAnalysis", analysisService != null);
        capabilities.put("optimization", optimizer != null);
        capabilities.put("behaviorPatterns", agentKnowledge.containsKey("behaviorPattern"));
        capabilities.put("orchestration", orchestrationService != null);
        capabilities.put("sharedReasoning", sharedReasoningEngine != null);
        capabilities.put("decisionEngine", decisionEngine != null);

        return capabilities;
    }

    @Override
    public boolean canHandleAction(String actionName) {
        // Check if this agent can handle the action based on specialization
        return actionName.toLowerCase().contains(getSpecialization().toLowerCase())
                || getRegisteredActions().stream().anyMatch(action -> action.getActionName().equals(actionName));
    }

    // Helper methods
    private ReasoningContext createReasoningContext(String actionName, Map<String, Object> parameters) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("agentContext", getContext().getAllValues());
        metadata.put("knowledge", agentKnowledge);
        metadata.put("learningHistory", learningHistory);
        metadata.put("capabilities", getCapabilities());

        ReasoningContext context = ReasoningContext.builder()
                .withInitialContext("Action: " + actionName + " with parameters: " + parameters)
                .withCurrentContext("Action: " + actionName + " with parameters: " + parameters)
                .withDomain(getSpecialization()).withSessionId("session-" + System.currentTimeMillis())
                .withMetadata(metadata).build();
        return context;
    }

    /**
     * Get agent specialization
     * 
     * @return the agent specialization (e.g., "energy", "security", "comfort")
     */
    public abstract String getSpecialization();

    /**
     * Initialize specialized reasoning components if not already done.
     */
    private void initializeSpecializedComponentsIfNeeded() {
        // Check if specialized components are already initialized
        if (!agentKnowledge.containsKey("specializedComponentsInitialized")) {
            integrateSpecializedReasoningComponents();
            agentKnowledge.put("specializedComponentsInitialized", true);
        }
    }

    /**
     * Integrate with specialized reasoning components for enhanced capabilities.
     * This method provides hooks for integrating with analysis, monitoring,
     * decision-making, and other specialized reasoning components.
     */
    protected void integrateSpecializedReasoningComponents() {
        logger.debug("Agent {} integrating specialized reasoning components", getAgentId());

        // Initialize behavior pattern analysis
        initializeBehaviorPatternAnalysis();

        // Initialize optimization strategies
        initializeOptimizationStrategies();

        // Initialize analysis service integration
        initializeAnalysisServiceIntegration();

        logger.debug("Agent {} specialized reasoning components integration completed", getAgentId());
    }

    /**
     * Initialize behavior pattern analysis for the agent.
     */
    private void initializeBehaviorPatternAnalysis() {
        try {
            // Create behavior pattern for this agent
            BehaviorPattern agentPattern = new BehaviorPattern(getAgentId());

            // Store in agent knowledge for future analysis
            agentKnowledge.put("behaviorPattern", agentPattern);

            logger.debug("Agent {} behavior pattern analysis initialized", getAgentId());
        } catch (Exception e) {
            logger.warn("Failed to initialize behavior pattern analysis for agent {}", getAgentId(), e);
        }
    }

    /**
     * Initialize optimization strategies for the agent.
     */
    private void initializeOptimizationStrategies() {
        try {
            AgentModelDecisionOptimizer decisionOptimizer = optimizer;
            if (decisionOptimizer != null) {
                // Register optimization strategies for this agent
                // This would integrate with the optimizer to provide agent-specific optimization
                logger.debug("Agent {} optimization strategies initialized", getAgentId());
            } else {
                logger.debug("No decision optimizer available for agent {}", getAgentId());
            }
        } catch (Exception e) {
            logger.warn("Failed to initialize optimization strategies for agent {}", getAgentId(), e);
        }
    }

    /**
     * Initialize analysis service integration for the agent.
     */
    private void initializeAnalysisServiceIntegration() {
        try {
            ReasoningStepAnalysisService analysis = analysisService;
            if (analysis != null) {
                // Register agent for analysis service
                // This would enable the analysis service to track this agent's reasoning patterns
                logger.debug("Agent {} analysis service integration initialized", getAgentId());
            } else {
                logger.debug("No analysis service available for agent {}", getAgentId());
            }
        } catch (Exception e) {
            logger.warn("Failed to initialize analysis service integration for agent {}", getAgentId(), e);
        }
    }

    /**
     * Enhanced reasoning context creation with specialized components integration.
     */
    private ReasoningContext createEnhancedReasoningContext(String actionName, Map<String, Object> parameters) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("agentContext", getContext().getAllValues());
        metadata.put("knowledge", agentKnowledge);
        metadata.put("learningHistory", learningHistory);
        metadata.put("capabilities", getCapabilities());
        metadata.put("specialization", getSpecialization());
        metadata.put("reasoningEngines", getAvailableReasoningEngines());

        ReasoningContext context = ReasoningContext.builder()
                .withInitialContext("Action: " + actionName + " with parameters: " + parameters)
                .withCurrentContext("Action: " + actionName + " with parameters: " + parameters)
                .withDomain(getSpecialization()).withSessionId("session-" + System.currentTimeMillis())
                .withMetadata(metadata).build();
        return context;
    }

    /**
     * Get list of available reasoning engines for this agent.
     */
    private Map<String, Boolean> getAvailableReasoningEngines() {
        Map<String, Boolean> engines = new HashMap<>();
        engines.put("multiStepReasoning", reasoningEngine != null);
        engines.put("sharedReasoning", sharedReasoningEngine != null);
        engines.put("decisionEngine", decisionEngine != null);
        engines.put("orchestrationService", orchestrationService != null);
        engines.put("actionCallParser", actionCallParser != null);
        return engines;
    }

    private List<ExecutionContext> parseReasoningToActions(MultiStepReasoningResult reasoningResult,
            String originalAction, Map<String, Object> originalParams) {
        List<ExecutionContext> actions = new ArrayList<>();

        try {
            // Use ModelResponseActionParser if available
            ModelResponseActionParser parser = actionCallParser;
            if (parser != null) {
                // Create a mock ModelResponse from the reasoning result for parsing
                // This is a temporary approach - ideally the reasoning engine should return ModelResponse
                String reasoningContent = reasoningResult.getFinalReasoning();
                if (reasoningContent != null && !reasoningContent.isEmpty()) {
                    // Create a simple ModelResponse for parsing
                    ModelResponse mockResponse = ModelResponse.builder().withContent(reasoningContent)
                            .withModelName("reasoning-engine").withProviderType("internal").build();

                    String sessionId = "session-" + System.currentTimeMillis();
                    List<ExecutionContext> parsedActions = parser.parseActionCalls(mockResponse, sessionId);
                    actions.addAll(parsedActions);
                }
            }

            // Fallback: Extract actions from reasoning steps if no parser available
            if (actions.isEmpty()) {
                for (var step : reasoningResult.getSteps()) {
                    // Create action context from reasoning step
                    ExecutionContext actionContext = ExecutionContext.builder().withProtocol("openhab")
                            .withClientId(getAgentId()).withSessionId("session-" + System.currentTimeMillis())
                            .withCorrelationId("corr-" + System.currentTimeMillis())
                            .withValues(Map.of("reasoning.step", step.getStepNumber(), "reasoning.content",
                                    step.getReasoning(), "original.action", originalAction, "original.params",
                                    originalParams))
                            .build();
                    actions.add(actionContext);
                }
            }

            logger.debug("Parsed {} actions from reasoning result", actions.size());

        } catch (Exception e) {
            logger.error("Failed to parse reasoning to actions", e);
        }

        return actions;
    }

    /**
     * Handle model integration errors and provide fallback mechanisms.
     */
    private ActionResult handleModelIntegrationError(Exception e, String actionName, String actionId) {
        logger.error("Model integration error for action {}: {}", actionName, e.getMessage(), e);

        // Try fallback reasoning if available
        try {
            if (reasoningEngine != null) {
                logger.debug("Attempting fallback reasoning for action: {}", actionName);
                // Create a simple fallback context
                ReasoningContext fallbackContext = ReasoningContext.builder()
                        .withInitialContext("Fallback reasoning for: " + actionName)
                        .withCurrentContext("Fallback reasoning for: " + actionName).withDomain(getSpecialization())
                        .withSessionId("fallback-" + System.currentTimeMillis()).build();

                MultiStepReasoningResult fallbackResult = reasoningEngine.reasonAsync(fallbackContext).get();
                return ActionResult.success("Fallback reasoning completed: " + fallbackResult.getFinalReasoning(), 0);
            }
        } catch (Exception fallbackError) {
            logger.error("Fallback reasoning also failed for action: {}", actionName, fallbackError);
        }

        // Return error result if no fallback available
        return ActionResult.error("Model integration failed: " + e.getMessage(),
                new ActionError("MODEL_INTEGRATION_ERROR", e.getMessage()), 0);
    }

    /**
     * Enhanced action execution with better error handling and monitoring.
     */
    private List<ActionResult> executeActionsWithMonitoring(List<ExecutionContext> actions) {
        List<ActionResult> results = new ArrayList<>();

        for (ExecutionContext action : actions) {
            Instant startTime = Instant.now();
            try {
                if (actionRegistry != null) {
                    // Get action from registry and execute
                    String actionId = action.getCorrelationId();
                    Action fetchedAction = actionRegistry.getAction(actionId);
                    if (fetchedAction == null) {
                        // Fallback: try matching by name or alternative candidate from protocol context
                        String nameCandidate = action.getValue(ActionKeys.ACTION_ID.getKey(), String.class);
                        if (nameCandidate == null) {
                            nameCandidate = actionId;
                        }
                        for (Action a : getRegisteredActions()) {
                            if (a.getActionId().equals(actionId) || a.getActionId().equals(nameCandidate)
                                    || a.getActionName().equalsIgnoreCase(nameCandidate)) {
                                fetchedAction = a;
                                break;
                            }
                        }
                    }
                    if (fetchedAction != null) {
                        ActionResult result = fetchedAction.execute(action.getAllValues(), action);
                        long duration = Duration.between(startTime, Instant.now()).toMillis();
                        results.add(ActionResult.success(result, duration));

                        // Log performance metrics
                        logger.debug("Action {} executed successfully in {}ms", actionId, duration);
                    } else {
                        results.add(ActionResult.error("Action not found: " + actionId,
                                new ActionError("ACTION_NOT_FOUND", "Action not found: " + actionId), 0));
                    }
                } else {
                    results.add(ActionResult.error("Action registry not available",
                            new ActionError("REGISTRY_UNAVAILABLE", "Action registry not available"), 0));
                }
            } catch (Exception e) {
                long duration = Duration.between(startTime, Instant.now()).toMillis();
                logger.error("Failed to execute action {} in {}ms", action.getCorrelationId(), duration, e);
                results.add(ActionResult.error("Action execution failed: " + e.getMessage(),
                        new ActionError("EXECUTION_ERROR", "Action execution failed: " + e.getMessage()), duration));
            }
        }

        return results;
    }

    private ActionResult aggregateResults(String actionId, List<ActionResult> actionResults,
            MultiStepReasoningResult reasoningResult) {
        // Aggregate multiple action results into a single result
        boolean allSuccess = actionResults.stream().allMatch(ActionResult::isSuccess);

        if (allSuccess) {
            return ActionResult.success(actionResults, 0);
        } else {
            String error = actionResults.stream().filter(result -> !result.isSuccess()).map(ActionResult::getMessage)
                    .findFirst().orElse("Unknown error");
            return ActionResult.error(error, new ActionError("AGGREGATION_ERROR", error), 0);
        }
    }

    private void updateKnowledgeFromLearning(LearningExample example) {
        // Update agent knowledge based on learning example
        String key = "learning." + example.getActionName() + "." + (example.isSuccess() ? "success" : "failure");
        agentKnowledge.put(key, example);
    }

    /**
     * Analyze reasoning patterns and apply optimizations.
     */
    private void analyzeAndOptimizeReasoning(MultiStepReasoningResult reasoningResult) {
        try {
            // Analyze reasoning patterns
            analyzeReasoningPatterns(reasoningResult);

            // Apply optimizations
            applyReasoningOptimizations(reasoningResult);

            // Update behavior patterns
            updateBehaviorPatterns(reasoningResult);

        } catch (Exception e) {
            logger.warn("Failed to analyze and optimize reasoning for agent {}", getAgentId(), e);
        }
    }

    /**
     * Analyze reasoning patterns using the analysis service.
     */
    private void analyzeReasoningPatterns(MultiStepReasoningResult reasoningResult) {
        ReasoningStepAnalysisService analysis = analysisService;
        if (analysis == null) {
            return;
        }

        try {
            // Get reasoning steps directly - no conversion needed since they're already engine ReasoningStep
            List<org.openhab.core.ai.reasoning.engine.api.ReasoningStep> engineSteps = reasoningResult.getSteps();

            if (!engineSteps.isEmpty()) {
                // Analyze patterns
                ReasoningPatternAnalysis patternAnalysis = analysis.analyzePatterns(engineSteps);

                // Analyze performance
                ReasoningPerformanceAnalysis performanceAnalysis = analysis.analyzePerformance(engineSteps);

                // Generate recommendations
                List<ReasoningRecommendation> recommendations = analysis.generateRecommendations(engineSteps);

                // Store analysis results in agent knowledge
                agentKnowledge.put("lastPatternAnalysis", patternAnalysis);
                agentKnowledge.put("lastPerformanceAnalysis", performanceAnalysis);
                agentKnowledge.put("lastRecommendations", recommendations);

                logger.debug("Agent {} reasoning analysis completed: {} patterns, {} recommendations", getAgentId(),
                        patternAnalysis.getIdentifiedPatterns().size(), recommendations.size());
            }
        } catch (Exception e) {
            logger.warn("Failed to analyze reasoning patterns for agent {}", getAgentId(), e);
        }
    }

    /**
     * Apply reasoning optimizations using the optimizer.
     */
    private void applyReasoningOptimizations(MultiStepReasoningResult reasoningResult) {
        AgentModelDecisionOptimizer decisionOptimizer = optimizer;
        if (decisionOptimizer == null) {
            return;
        }

        try {
            // Apply optimizations based on reasoning result
            // This would integrate with the optimizer to improve future reasoning
            logger.debug("Agent {} applying reasoning optimizations", getAgentId());

            // Store optimization context for future use
            agentKnowledge.put("lastOptimizationContext", reasoningResult);

        } catch (Exception e) {
            logger.warn("Failed to apply reasoning optimizations for agent {}", getAgentId(), e);
        }
    }

    /**
     * Update behavior patterns based on reasoning results.
     */
    private void updateBehaviorPatterns(MultiStepReasoningResult reasoningResult) {
        try {
            BehaviorPattern agentPattern = (BehaviorPattern) agentKnowledge.get("behaviorPattern");
            if (agentPattern != null) {
                // Analyze the reasoning interaction
                Map<String, Object> interactionData = Map.of("actionType", "reasoning", "steps",
                        reasoningResult.getSteps().size(), "confidence", reasoningResult.getConfidence(), "completed",
                        reasoningResult.isCompleted(), "duration", reasoningResult.getTotalDurationMs());

                boolean patternDetected = agentPattern.analyzeInteraction("reasoning", interactionData);

                if (patternDetected) {
                    logger.debug("Agent {} behavior pattern detected in reasoning", getAgentId());
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to update behavior patterns for agent {}", getAgentId(), e);
        }
    }

    // LearningExample extracted to org.openhab.core.ai.agents.LearningExample
}
