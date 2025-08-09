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
import org.openhab.core.ai.action.ActionContext;
import org.openhab.core.ai.action.ActionRegistry;
import org.openhab.core.ai.action.ActionResult;
import org.openhab.core.ai.agent.api.IntelligentAgent;
import org.openhab.core.ai.model.api.ModelClient;
import org.openhab.core.ai.reasoning.MultiStepReasoningEngine;
import org.openhab.core.ai.reasoning.api.MultiStepReasoningResult;
import org.openhab.core.ai.reasoning.api.ReasoningContext;
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

    private List<org.openhab.core.ai.action.api.Action> getRegisteredActions() {
        ActionRegistry registry = actionRegistry;
        if (registry == null) {
            return java.util.List.of();
        }
        return new java.util.ArrayList<>(registry.getAllActions().values());
    }

    private static final Logger logger = LoggerFactory.getLogger(AbstractIntelligentAgent.class);

    // Dependencies
    @Reference
    private @Nullable MultiStepReasoningEngine reasoningEngine;

    @Reference
    private @Nullable ActionRegistry actionRegistry;

    @Reference
    private @Nullable ModelClient modelClient;

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

                // 1. Create reasoning context
                ReasoningContext reasoningContext = createReasoningContext(actionName, parameters);

                // 2. Execute reasoning to determine what actions to take
                MultiStepReasoningResult reasoningResult = reasoningEngine.reasonAsync(reasoningContext).get();

                // 3. Parse reasoning result to extract concrete actions
                List<ActionContext> actionsToExecute = parseReasoningToActions(reasoningResult, actionName, parameters);

                // 4. Execute concrete actions
                List<ActionResult> actionResults = executeActions(actionsToExecute);

                // 5. Aggregate results
                ActionResult finalResult = aggregateResults(actionId, actionResults, reasoningResult);

                // 6. Learn from the execution
                learnFromAction(actionName, parameters, finalResult, finalResult.isSuccess());

                logger.debug("Agent {} completed intelligent action: {} in {}ms", getAgentId(), actionName,
                        java.time.Duration.between(startTime, Instant.now()).toMillis());

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
    public CompletableFuture<List<ActionContext>> planActions(String goal, Map<String, Object> context) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.debug("Agent {} planning actions for goal: {}", getAgentId(), goal);

                // Create reasoning context for planning
                Map<String, Object> metadata = new HashMap<>();
                metadata.put("context", context);
                metadata.put("agentContext", getContext().getAll());
                metadata.put("knowledge", agentKnowledge);

                ReasoningContext reasoningContext = ReasoningContext.builder().initialContext("Goal: " + goal)
                        .currentContext("Goal: " + goal).domain(getSpecialization())
                        .sessionId("session-" + System.currentTimeMillis()).metadata(metadata).build();

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
                    getContext().getAll(), Instant.now());

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
    public MultiStepReasoningEngine getReasoningEngine() {
        return reasoningEngine;
    }

    @Override
    public ActionRegistry getActionRegistry() {
        return actionRegistry;
    }

    @Override
    public ModelClient getModelClient() {
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
        metadata.put("agentContext", getContext().getAll());
        metadata.put("knowledge", agentKnowledge);
        metadata.put("learningHistory", learningHistory);
        metadata.put("capabilities", getCapabilities());

        ReasoningContext context = ReasoningContext.builder()
                .initialContext("Action: " + actionName + " with parameters: " + parameters)
                .currentContext("Action: " + actionName + " with parameters: " + parameters).domain(getSpecialization())
                .sessionId("session-" + System.currentTimeMillis()).metadata(metadata).build();
        return context;
    }

    private List<ActionContext> parseReasoningToActions(MultiStepReasoningResult reasoningResult, String originalAction,
            Map<String, Object> originalParams) {
        List<ActionContext> actions = new ArrayList<>();

        try {
            // Extract actions from reasoning steps
            for (var step : reasoningResult.getSteps()) {
                // Parse actions from reasoning step
                // This would integrate with the existing action parsing logic
                // For now, create a simple action context
                ActionContext actionContext = ActionContext.builder().protocol("openhab").clientId(getAgentId())
                        .sessionId("session-" + System.currentTimeMillis())
                        .protocolContext(java.util.Map.of("actionId", originalAction))
                        .correlationId("corr-" + System.currentTimeMillis()).build();

                actions.add(actionContext);
            }

        } catch (Exception e) {
            logger.error("Failed to parse reasoning to actions", e);
        }

        return actions;
    }

    private List<ActionResult> executeActions(List<ActionContext> actions) {
        List<ActionResult> results = new ArrayList<>();

        for (ActionContext action : actions) {
            try {
                if (actionRegistry != null) {
                    // Get action from registry and execute
                    String actionId = action.getCorrelationId();
                    org.openhab.core.ai.action.api.Action fetchedAction = actionRegistry.getAction(actionId);
                    if (fetchedAction == null) {
                        // Fallback: try matching by name or alternative candidate from protocol context
                        String nameCandidate = String
                                .valueOf(action.getProtocolContext().getOrDefault("actionId", actionId));
                        for (org.openhab.core.ai.action.api.Action a : getRegisteredActions()) {
                            if (a.getActionId().equals(actionId) || a.getActionId().equals(nameCandidate)
                                    || a.getActionName().equalsIgnoreCase(nameCandidate)) {
                                fetchedAction = a;
                                break;
                            }
                        }
                    }
                    if (fetchedAction != null) {
                        org.openhab.core.ai.action.ActionResult result = fetchedAction
                                .execute(action.getProtocolContext(), action);
                        results.add(ActionResult.success(result,
                                Duration.between(Instant.now(), Instant.now()).toMillis()));
                    } else {
                        results.add(ActionResult.error("Action not found: " + actionId, null, 0));
                    }
                } else {
                    results.add(ActionResult.error("Action registry not available", null, 0));
                }
            } catch (Exception e) {
                logger.error("Failed to execute action", e);
                results.add(ActionResult.error("Action execution failed: " + e.getMessage(), null, 0));
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
            return ActionResult.error(error, null, 0);
        }
    }

    private void updateKnowledgeFromLearning(LearningExample example) {
        // Update agent knowledge based on learning example
        String key = "learning." + example.getActionName() + "." + (example.isSuccess() ? "success" : "failure");
        agentKnowledge.put(key, example);
    }

    // Data classes
    public static class LearningExample {
        private final String actionName;
        private final Map<String, Object> parameters;
        private final ActionResult result;
        private final boolean success;
        private final Map<String, Object> context;
        private final Instant timestamp;

        public LearningExample(String actionName, Map<String, Object> parameters, ActionResult result, boolean success,
                Map<String, Object> context, Instant timestamp) {
            this.actionName = actionName;
            this.parameters = new HashMap<>(parameters);
            this.result = result;
            this.success = success;
            this.context = new HashMap<>(context);
            this.timestamp = timestamp;
        }

        public String getActionName() {
            return actionName;
        }

        public Map<String, Object> getParameters() {
            return parameters;
        }

        public ActionResult getResult() {
            return result;
        }

        public boolean isSuccess() {
            return success;
        }

        public Map<String, Object> getContext() {
            return context;
        }

        public Instant getTimestamp() {
            return timestamp;
        }
    }
}
