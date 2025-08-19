package org.openhab.core.ai.reasoning.prompts;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.context.AgentModelContext;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Agent Model Prompt Builder for agent-specific prompt construction.
 * 
 * This class provides a fluent API for building comprehensive prompts
 * that are tailored to specific agent types and reasoning tasks.
 * 
 * @author Karel Goderis - Initial Contribution
 */
@Component(service = AgentModelPromptBuilder.class)
@NonNullByDefault
public class AgentModelPromptBuilder {

    private static final Logger logger = LoggerFactory.getLogger(AgentModelPromptBuilder.class);

    private final StringBuilder promptBuilder = new StringBuilder();
    private final Map<String, Object> parameters = new ConcurrentHashMap<>();
    private final Map<String, Object> metadata = new ConcurrentHashMap<>();

    /**
     * Create a new prompt builder instance.
     * 
     * @return A new AgentModelPromptBuilder instance
     */
    public static AgentModelPromptBuilder create() {
        return new AgentModelPromptBuilder();
    }

    /**
     * Add system role definition to the prompt.
     * 
     * @param role The system role description
     * @return This builder instance for method chaining
     */
    public AgentModelPromptBuilder withSystemRole(String role) {
        promptBuilder.append("System: ").append(role).append("\n\n");
        return this;
    }

    /**
     * Add agent context to the prompt.
     * 
     * @param context The agent context
     * @return This builder instance for method chaining
     */
    public AgentModelPromptBuilder withAgentContext(AgentModelContext context) {
        promptBuilder.append("Agent Context:\n");
        promptBuilder.append("- Agent ID: ").append(context.getContextData("agentId")).append("\n");
        promptBuilder.append("- Agent Type: ").append(context.getContextData("agentType")).append("\n");
        promptBuilder.append("- Domain: ").append(context.getContextData("domain")).append("\n");

        if (context.hasContextData("capabilities")) {
            promptBuilder.append("- Capabilities: ").append(context.getContextData("capabilities")).append("\n");
        }

        if (context.hasContextData("skills")) {
            promptBuilder.append("- Skills: ").append(context.getContextData("skills")).append("\n");
        }

        promptBuilder.append("\n");
        return this;
    }

    /**
     * Add task description to the prompt.
     * 
     * @param task The task description
     * @return This builder instance for method chaining
     */
    public AgentModelPromptBuilder withTask(String task) {
        promptBuilder.append("Task: ").append(task).append("\n\n");
        return this;
    }

    /**
     * Add current state information to the prompt.
     * 
     * @param state The current state description
     * @return This builder instance for method chaining
     */
    public AgentModelPromptBuilder withCurrentState(String state) {
        promptBuilder.append("Current State: ").append(state).append("\n\n");
        return this;
    }

    /**
     * Add historical context to the prompt.
     * 
     * @param history The historical context
     * @return This builder instance for method chaining
     */
    public AgentModelPromptBuilder withHistory(String history) {
        promptBuilder.append("Historical Context: ").append(history).append("\n\n");
        return this;
    }

    /**
     * Add user preferences to the prompt.
     * 
     * @param preferences The user preferences
     * @return This builder instance for method chaining
     */
    public AgentModelPromptBuilder withUserPreferences(String preferences) {
        promptBuilder.append("User Preferences: ").append(preferences).append("\n\n");
        return this;
    }

    /**
     * Add constraints to the prompt.
     * 
     * @param constraints The constraints
     * @return This builder instance for method chaining
     */
    public AgentModelPromptBuilder withConstraints(String constraints) {
        promptBuilder.append("Constraints: ").append(constraints).append("\n\n");
        return this;
    }

    /**
     * Add expected output format to the prompt.
     * 
     * @param format The expected output format
     * @return This builder instance for method chaining
     */
    public AgentModelPromptBuilder withExpectedOutput(String format) {
        promptBuilder.append("Expected Output: ").append(format).append("\n\n");
        return this;
    }

    /**
     * Add examples to the prompt.
     * 
     * @param examples The examples
     * @return This builder instance for method chaining
     */
    public AgentModelPromptBuilder withExamples(String examples) {
        promptBuilder.append("Examples:\n").append(examples).append("\n\n");
        return this;
    }

    /**
     * Add reasoning steps to the prompt.
     * 
     * @param steps The reasoning steps
     * @return This builder instance for method chaining
     */
    public AgentModelPromptBuilder withReasoningSteps(String steps) {
        promptBuilder.append("Reasoning Steps:\n").append(steps).append("\n\n");
        return this;
    }

    /**
     * Add custom prompt section.
     * 
     * @param section The section name
     * @param content The section content
     * @return This builder instance for method chaining
     */
    public AgentModelPromptBuilder withSection(String section, String content) {
        promptBuilder.append(section).append(": ").append(content).append("\n\n");
        return this;
    }

    /**
     * Add parameter to the prompt.
     * 
     * @param key The parameter key
     * @param value The parameter value
     * @return This builder instance for method chaining
     */
    public AgentModelPromptBuilder withParameter(String key, Object value) {
        parameters.put(key, value);
        return this;
    }

    /**
     * Add metadata to the prompt.
     * 
     * @param key The metadata key
     * @param value The metadata value
     * @return This builder instance for method chaining
     */
    public AgentModelPromptBuilder withMetadata(String key, Object value) {
        metadata.put(key, value);
        return this;
    }

    /**
     * Set the prompt type.
     * 
     * @param type The prompt type
     * @return This builder instance for method chaining
     */
    public AgentModelPromptBuilder withType(PromptType type) {
        metadata.put("type", type);
        return this;
    }

    /**
     * Set the prompt priority.
     * 
     * @param priority The prompt priority
     * @return This builder instance for method chaining
     */
    public AgentModelPromptBuilder withPriority(PromptPriority priority) {
        metadata.put("priority", priority);
        return this;
    }

    /**
     * Set the prompt version.
     * 
     * @param version The prompt version
     * @return This builder instance for method chaining
     */
    public AgentModelPromptBuilder withVersion(String version) {
        metadata.put("version", version);
        return this;
    }

    /**
     * Build the agent model prompt.
     * 
     * @return The built AgentModelPrompt
     */
    public AgentModelPrompt build() {
        String promptText = promptBuilder.toString().trim();
        long timestamp = System.currentTimeMillis();

        // Add default metadata if not set
        if (!metadata.containsKey("type")) {
            metadata.put("type", PromptType.GENERAL);
        }
        if (!metadata.containsKey("priority")) {
            metadata.put("priority", PromptPriority.MEDIUM);
        }
        if (!metadata.containsKey("timestamp")) {
            metadata.put("timestamp", timestamp);
        }
        if (!metadata.containsKey("version")) {
            metadata.put("version", "1.0");
        }

        logger.debug("Building agent model prompt with {} characters", promptText.length());

        return new AgentModelPrompt(promptText, parameters, metadata);
    }

    /**
     * Create a prompt for energy optimization.
     * 
     * @param context The agent context
     * @param currentEnergyUsage The current energy usage
     * @param targetOptimization The target optimization goal
     * @return The built prompt
     */
    public static AgentModelPrompt createEnergyOptimizationPrompt(AgentModelContext context, String currentEnergyUsage,
            String targetOptimization) {

        return create().withSystemRole(
                "You are an intelligent energy management agent responsible for optimizing home energy usage while maintaining comfort and functionality.")
                .withAgentContext(context)
                .withTask("Analyze current energy usage and provide optimization recommendations")
                .withCurrentState("Current Energy Usage: " + currentEnergyUsage)
                .withConstraints(
                        "Must maintain comfort levels, consider user preferences, and respect operational constraints")
                .withExpectedOutput(
                        "Provide specific, actionable recommendations with expected energy savings and implementation steps")
                .withType(PromptType.ENERGY_OPTIMIZATION).withPriority(PromptPriority.HIGH).build();
    }

    /**
     * Create a prompt for security analysis.
     * 
     * @param context The agent context
     * @param securityEvent The security event description
     * @param threatLevel The threat level
     * @return The built prompt
     */
    public static AgentModelPrompt createSecurityAnalysisPrompt(AgentModelContext context, String securityEvent,
            String threatLevel) {

        return create().withSystemRole(
                "You are an intelligent security management agent responsible for analyzing security events and providing appropriate responses.")
                .withAgentContext(context).withTask("Analyze security event and determine appropriate response")
                .withCurrentState("Security Event: " + securityEvent + ", Threat Level: " + threatLevel)
                .withConstraints("Must prioritize safety, consider false positives, and follow security protocols")
                .withExpectedOutput(
                        "Provide threat assessment, recommended actions, and escalation procedures if needed")
                .withType(PromptType.SECURITY_ANALYSIS).withPriority(PromptPriority.HIGH).build();
    }

    /**
     * Create a prompt for comfort optimization.
     * 
     * @param context The agent context
     * @param currentConditions The current environmental conditions
     * @param userPreferences The user preferences
     * @return The built prompt
     */
    public static AgentModelPrompt createComfortOptimizationPrompt(AgentModelContext context, String currentConditions,
            String userPreferences) {

        return create().withSystemRole(
                "You are an intelligent comfort management agent responsible for optimizing home comfort while balancing energy efficiency.")
                .withAgentContext(context)
                .withTask("Optimize home comfort based on current conditions and user preferences")
                .withCurrentState("Current Conditions: " + currentConditions).withUserPreferences(userPreferences)
                .withConstraints("Must balance comfort with energy efficiency and respect user preferences")
                .withExpectedOutput("Provide specific comfort optimization recommendations with expected improvements")
                .withType(PromptType.COMFORT_OPTIMIZATION).withPriority(PromptPriority.MEDIUM).build();
    }

    /**
     * Create a prompt for decision making.
     * 
     * @param context The agent context
     * @param decisionScenario The decision scenario
     * @param options The available options
     * @return The built prompt
     */
    public static AgentModelPrompt createDecisionMakingPrompt(AgentModelContext context, String decisionScenario,
            String options) {

        return create().withSystemRole(
                "You are an intelligent decision-making agent responsible for analyzing scenarios and making optimal decisions.")
                .withAgentContext(context).withTask("Analyze decision scenario and recommend optimal action")
                .withCurrentState("Decision Scenario: " + decisionScenario).withSection("Available Options", options)
                .withReasoningSteps(
                        "1. Analyze the scenario\n2. Evaluate each option\n3. Consider constraints and preferences\n4. Recommend optimal action")
                .withExpectedOutput("Provide decision recommendation with reasoning and expected outcomes")
                .withType(PromptType.DECISION_MAKING).withPriority(PromptPriority.HIGH).build();
    }

    // Extracted to top-level: org.openhab.core.ai.reasoning.PromptType

    // Extracted to top-level: org.openhab.core.ai.reasoning.PromptPriority

    // Extracted to top-level: org.openhab.core.ai.reasoning.AgentModelPrompt
}
