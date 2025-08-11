package org.openhab.core.ai.reasoning;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Agent Model Decision Engine for model-based decision making.
 * 
 * This class provides intelligent decision-making capabilities using AI models
 * to analyze scenarios, evaluate options, and make optimal decisions.
 * 
 * @author Karel Goderis - Initial Contribution
 */
@Component(service = AgentModelDecisionEngine.class)
@NonNullByDefault
public class AgentModelDecisionEngine {

    private static final Logger logger = LoggerFactory.getLogger(AgentModelDecisionEngine.class);

    @Reference
    private SharedModelReasoningEngine reasoningEngine;

    @Reference
    private AgentModelContextBuilder contextBuilder;

    @Reference
    private AgentModelPromptBuilder promptBuilder;

    /**
     * Make a decision using the AI model.
     * 
     * @param decisionContext The decision context
     * @return A CompletableFuture containing the decision result
     */
    public CompletableFuture<DecisionResult> makeDecision(DecisionContext decisionContext) {
        logger.debug("Making decision for context: {}", decisionContext.getContextId());

        return CompletableFuture.supplyAsync(() -> {
            try {
                // Build agent context
                AgentModelContextBuilder.AgentModelContext agentContext = buildAgentContext(decisionContext);

                // Create decision prompt
                AgentModelPromptBuilder.AgentModelPrompt prompt = createDecisionPrompt(agentContext, decisionContext);

                // Execute reasoning
                String reasoningResult = executeReasoning(prompt);

                // Parse and validate decision
                DecisionResult result = parseDecisionResult(reasoningResult, decisionContext);

                // Validate decision safety
                validateDecisionSafety(result, decisionContext);

                logger.debug("Decision made successfully: {}", result.getDecisionId());
                return result;

            } catch (Exception e) {
                logger.error("Error making decision: {}", e.getMessage(), e);
                return createErrorDecisionResult(decisionContext, e);
            }
        });
    }

    /**
     * Build agent context for decision making.
     * 
     * @param decisionContext The decision context
     * @return The built agent context
     */
    private AgentModelContextBuilder.AgentModelContext buildAgentContext(DecisionContext decisionContext) {
        Map<String, Object> currentState = new ConcurrentHashMap<>();
        currentState.put("scenario", decisionContext.getCurrentState());

        return contextBuilder.create().withAgentId(decisionContext.getAgentId())
                .withAgentType(decisionContext.getAgentType()).withDomain(decisionContext.getDomain())
                .withCurrentState(currentState).withUserPreferences(decisionContext.getUserPreferences())
                .withContextData("constraints", decisionContext.getConstraints())
                .withPriority(AgentModelContextBuilder.ContextPriority.HIGH).withSource("decision_engine").build();
    }

    /**
     * Create decision prompt.
     * 
     * @param agentContext The agent context
     * @param decisionContext The decision context
     * @return The created prompt
     */
    private AgentModelPromptBuilder.AgentModelPrompt createDecisionPrompt(
            AgentModelContextBuilder.AgentModelContext agentContext, DecisionContext decisionContext) {

        return promptBuilder.create().withSystemRole(
                "You are an intelligent decision-making agent responsible for analyzing scenarios and making optimal decisions.")
                .withAgentContext(agentContext).withTask("Analyze decision scenario and recommend optimal action")
                .withCurrentState("Decision Scenario: " + decisionContext.getScenario())
                .withSection("Available Options", decisionContext.getOptions().toString())
                .withConstraints(decisionContext.getConstraints())
                .withReasoningSteps(
                        "1. Analyze the scenario\n2. Evaluate each option\n3. Consider constraints and preferences\n4. Recommend optimal action")
                .withExpectedOutput("Provide decision recommendation with reasoning and expected outcomes")
                .withType(AgentModelPromptBuilder.PromptType.DECISION_MAKING)
                .withPriority(AgentModelPromptBuilder.PromptPriority.HIGH).build();
    }

    /**
     * Execute reasoning using the shared model reasoning engine.
     * 
     * @param prompt The decision prompt
     * @return The reasoning result
     */
    private String executeReasoning(AgentModelPromptBuilder.AgentModelPrompt prompt) {
        try {
            // Integrate with SharedModelReasoningEngine for actual reasoning execution
            CompletableFuture<org.openhab.core.ai.model.api.ModelResponse> future = reasoningEngine
                    .reasonAsync("decision-engine", prompt.getPromptText(), new ConcurrentHashMap<>(), null);

            org.openhab.core.ai.model.api.ModelResponse response = future.get();
            return response.getContent();
        } catch (Exception e) {
            logger.error("Error executing decision reasoning: {}", e.getMessage(), e);
            return "Decision: No decision available\nReasoning: Error occurred during reasoning\nExpected Outcome: None\nConfidence: 0%";
        }
    }

    /**
     * Parse decision result from reasoning output.
     * 
     * @param reasoningResult The reasoning result
     * @param decisionContext The original decision context
     * @return The parsed decision result
     */
    private DecisionResult parseDecisionResult(String reasoningResult, DecisionContext decisionContext) {
        // Parse the reasoning result to extract decision components
        String decision = extractDecision(reasoningResult);
        String reasoning = extractReasoning(reasoningResult);
        double confidence = extractConfidence(reasoningResult);
        Map<String, Object> expectedOutcomes = extractExpectedOutcomes(reasoningResult);

        return new DecisionResult(generateDecisionId(), decisionContext.getContextId(), decision, reasoning, confidence,
                expectedOutcomes, System.currentTimeMillis(), DecisionStatus.APPROVED);
    }

    /**
     * Extract decision from reasoning result.
     * 
     * @param reasoningResult The reasoning result
     * @return The extracted decision
     */
    private String extractDecision(String reasoningResult) {
        // Simple extraction - can be enhanced with more sophisticated parsing
        if (reasoningResult.contains("Decision:")) {
            int start = reasoningResult.indexOf("Decision:") + 9;
            int end = reasoningResult.indexOf("\n", start);
            if (end == -1)
                end = reasoningResult.length();
            return reasoningResult.substring(start, end).trim();
        }
        return "No decision extracted";
    }

    /**
     * Extract reasoning from reasoning result.
     * 
     * @param reasoningResult The reasoning result
     * @return The extracted reasoning
     */
    private String extractReasoning(String reasoningResult) {
        // Simple extraction - can be enhanced with more sophisticated parsing
        if (reasoningResult.contains("Reasoning:")) {
            int start = reasoningResult.indexOf("Reasoning:") + 10;
            int end = reasoningResult.indexOf("\n", start);
            if (end == -1)
                end = reasoningResult.length();
            return reasoningResult.substring(start, end).trim();
        }
        return "No reasoning extracted";
    }

    /**
     * Extract confidence from reasoning result.
     * 
     * @param reasoningResult The reasoning result
     * @return The extracted confidence
     */
    private double extractConfidence(String reasoningResult) {
        // Simple extraction - can be enhanced with more sophisticated parsing
        if (reasoningResult.contains("Confidence:")) {
            int start = reasoningResult.indexOf("Confidence:") + 11;
            int end = reasoningResult.indexOf("%", start);
            if (end != -1) {
                try {
                    return Double.parseDouble(reasoningResult.substring(start, end).trim()) / 100.0;
                } catch (NumberFormatException e) {
                    logger.warn("Could not parse confidence value");
                }
            }
        }
        return 0.5; // Default confidence
    }

    /**
     * Extract expected outcomes from reasoning result.
     * 
     * @param reasoningResult The reasoning result
     * @return The extracted expected outcomes
     */
    private Map<String, Object> extractExpectedOutcomes(String reasoningResult) {
        Map<String, Object> outcomes = new ConcurrentHashMap<>();

        // Simple extraction - can be enhanced with more sophisticated parsing
        if (reasoningResult.contains("Expected Outcome:")) {
            int start = reasoningResult.indexOf("Expected Outcome:") + 17;
            int end = reasoningResult.indexOf("\n", start);
            if (end == -1)
                end = reasoningResult.length();
            String outcome = reasoningResult.substring(start, end).trim();
            outcomes.put("primaryOutcome", outcome);
        }

        return outcomes;
    }

    /**
     * Validate decision safety.
     * 
     * @param result The decision result
     * @param decisionContext The decision context
     */
    private void validateDecisionSafety(DecisionResult result, DecisionContext decisionContext) {
        // Check if decision violates any safety constraints
        if (result.getConfidence() < 0.5) {
            logger.warn("Low confidence decision detected: {}", result.getDecisionId());
            result.setStatus(DecisionStatus.REQUIRES_REVIEW);
        }

        // Check for critical decisions that require human review
        if (decisionContext.getRiskLevel() == RiskLevel.CRITICAL) {
            logger.info("Critical decision requires human review: {}", result.getDecisionId());
            result.setStatus(DecisionStatus.REQUIRES_APPROVAL);
        }
    }

    /**
     * Create error decision result.
     * 
     * @param decisionContext The decision context
     * @param error The error that occurred
     * @return The error decision result
     */
    private DecisionResult createErrorDecisionResult(DecisionContext decisionContext, Exception error) {
        return new DecisionResult(generateDecisionId(), decisionContext.getContextId(),
                "ERROR: Decision could not be made", "Error occurred during decision making: " + error.getMessage(),
                0.0, new ConcurrentHashMap<>(), System.currentTimeMillis(), DecisionStatus.ERROR);
    }

    /**
     * Generate a unique decision ID.
     * 
     * @return A unique decision identifier
     */
    private String generateDecisionId() {
        return "decision_" + System.currentTimeMillis() + "_" + Thread.currentThread().getId();
    }

    /**
     * Decision Context class.
     */
    public static class DecisionContext {
        private final String contextId;
        private final String agentId;
        private final String agentType;
        private final String domain;
        private final String scenario;
        private final Map<String, Object> options;
        private final String currentState;
        private final Map<String, Object> userPreferences;
        private final String constraints;
        private final RiskLevel riskLevel;

        public DecisionContext(String contextId, String agentId, String agentType, String domain, String scenario,
                Map<String, Object> options, String currentState, Map<String, Object> userPreferences,
                String constraints, RiskLevel riskLevel) {
            this.contextId = contextId;
            this.agentId = agentId;
            this.agentType = agentType;
            this.domain = domain;
            this.scenario = scenario;
            this.options = new ConcurrentHashMap<>(options);
            this.currentState = currentState;
            this.userPreferences = new ConcurrentHashMap<>(userPreferences);
            this.constraints = constraints;
            this.riskLevel = riskLevel;
        }

        public String getContextId() {
            return contextId;
        }

        public String getAgentId() {
            return agentId;
        }

        public String getAgentType() {
            return agentType;
        }

        public String getDomain() {
            return domain;
        }

        public String getScenario() {
            return scenario;
        }

        public Map<String, Object> getOptions() {
            return new ConcurrentHashMap<>(options);
        }

        public String getCurrentState() {
            return currentState;
        }

        public Map<String, Object> getUserPreferences() {
            return new ConcurrentHashMap<>(userPreferences);
        }

        public String getConstraints() {
            return constraints;
        }

        public RiskLevel getRiskLevel() {
            return riskLevel;
        }
    }

    /**
     * Decision Result class.
     */
    public static class DecisionResult {
        private final String decisionId;
        private final String contextId;
        private final String decision;
        private final String reasoning;
        private final double confidence;
        private final Map<String, Object> expectedOutcomes;
        private final long timestamp;
        private DecisionStatus status;

        public DecisionResult(String decisionId, String contextId, String decision, String reasoning, double confidence,
                Map<String, Object> expectedOutcomes, long timestamp, DecisionStatus status) {
            this.decisionId = decisionId;
            this.contextId = contextId;
            this.decision = decision;
            this.reasoning = reasoning;
            this.confidence = confidence;
            this.expectedOutcomes = new ConcurrentHashMap<>(expectedOutcomes);
            this.timestamp = timestamp;
            this.status = status;
        }

        public String getDecisionId() {
            return decisionId;
        }

        public String getContextId() {
            return contextId;
        }

        public String getDecision() {
            return decision;
        }

        public String getReasoning() {
            return reasoning;
        }

        public double getConfidence() {
            return confidence;
        }

        public Map<String, Object> getExpectedOutcomes() {
            return new ConcurrentHashMap<>(expectedOutcomes);
        }

        public long getTimestamp() {
            return timestamp;
        }

        public DecisionStatus getStatus() {
            return status;
        }

        public void setStatus(DecisionStatus status) {
            this.status = status;
        }
    }

    /**
     * Decision status enum.
     */
    public enum DecisionStatus {
        APPROVED,
        REQUIRES_REVIEW,
        REQUIRES_APPROVAL,
        REJECTED,
        ERROR
    }

    /**
     * Risk level enum.
     */
    public enum RiskLevel {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }
}
