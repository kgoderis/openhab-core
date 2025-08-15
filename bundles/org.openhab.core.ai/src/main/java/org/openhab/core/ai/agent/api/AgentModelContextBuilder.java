package org.openhab.core.ai.agent.api;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.reasoning.context.ContextPriority;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Agent Model Context Builder for building agent-specific contexts.
 * 
 * This class provides a fluent API for building comprehensive agent contexts
 * that include agent-specific information, domain knowledge, and reasoning context.
 * 
 * @author Karel Goderis - Initial Contribution
 */
@Component(service = AgentModelContextBuilder.class)
@NonNullByDefault
public class AgentModelContextBuilder {

    private static final Logger logger = LoggerFactory.getLogger(AgentModelContextBuilder.class);

    private final AtomicLong contextIdCounter = new AtomicLong(0);
    private final Map<String, Object> contextData = new ConcurrentHashMap<>();
    private final Map<String, Object> metadata = new ConcurrentHashMap<>();

    /**
     * Create a new context builder instance.
     * 
     * @return A new AgentModelContextBuilder instance
     */
    public static AgentModelContextBuilder create() {
        return new AgentModelContextBuilder();
    }

    /**
     * Set the agent ID for this context.
     * 
     * @param agentId The agent identifier
     * @return This builder instance for method chaining
     */
    public AgentModelContextBuilder withAgentId(String agentId) {
        contextData.put("agentId", agentId);
        return this;
    }

    /**
     * Set the agent type for this context.
     * 
     * @param agentType The type of agent (e.g., "energy", "security", "comfort")
     * @return This builder instance for method chaining
     */
    public AgentModelContextBuilder withAgentType(String agentType) {
        contextData.put("agentType", agentType);
        return this;
    }

    /**
     * Set the domain context for this agent.
     * 
     * @param domain The domain context (e.g., "home_automation", "energy_management")
     * @return This builder instance for method chaining
     */
    public AgentModelContextBuilder withDomain(String domain) {
        contextData.put("domain", domain);
        return this;
    }

    /**
     * Add agent capabilities to the context.
     * 
     * @param capabilities Map of capability names to capability descriptions
     * @return This builder instance for method chaining
     */
    public AgentModelContextBuilder withCapabilities(Map<String, String> capabilities) {
        contextData.put("capabilities", capabilities);
        return this;
    }

    /**
     * Add agent skills to the context.
     * 
     * @param skills Map of skill names to skill descriptions
     * @return This builder instance for method chaining
     */
    public AgentModelContextBuilder withSkills(Map<String, String> skills) {
        contextData.put("skills", skills);
        return this;
    }

    /**
     * Add current state information to the context.
     * 
     * @param state Map of state variables and their current values
     * @return This builder instance for method chaining
     */
    public AgentModelContextBuilder withCurrentState(Map<String, Object> state) {
        contextData.put("currentState", state);
        return this;
    }

    /**
     * Add historical context to the context.
     * 
     * @param history List of historical events or data points
     * @return This builder instance for method chaining
     */
    public AgentModelContextBuilder withHistory(Object history) {
        contextData.put("history", history);
        return this;
    }

    /**
     * Add user preferences to the context.
     * 
     * @param preferences Map of user preference names to preference values
     * @return This builder instance for method chaining
     */
    public AgentModelContextBuilder withUserPreferences(Map<String, Object> preferences) {
        contextData.put("userPreferences", preferences);
        return this;
    }

    /**
     * Add environmental context to the context.
     * 
     * @param environment Map of environmental factors and their values
     * @return This builder instance for method chaining
     */
    public AgentModelContextBuilder withEnvironment(Map<String, Object> environment) {
        contextData.put("environment", environment);
        return this;
    }

    /**
     * Add reasoning context to the context.
     * 
     * @param reasoning Map of reasoning parameters and their values
     * @return This builder instance for method chaining
     */
    public AgentModelContextBuilder withReasoning(Map<String, Object> reasoning) {
        contextData.put("reasoning", reasoning);
        return this;
    }

    /**
     * Add custom context data.
     * 
     * @param key The context key
     * @param value The context value
     * @return This builder instance for method chaining
     */
    public AgentModelContextBuilder withContextData(String key, Object value) {
        contextData.put(key, value);
        return this;
    }

    /**
     * Add metadata to the context.
     * 
     * @param key The metadata key
     * @param value The metadata value
     * @return This builder instance for method chaining
     */
    public AgentModelContextBuilder withMetadata(String key, Object value) {
        metadata.put(key, value);
        return this;
    }

    /**
     * Set the context priority level.
     * 
     * @param priority The priority level (HIGH, MEDIUM, LOW)
     * @return This builder instance for method chaining
     */
    public AgentModelContextBuilder withPriority(ContextPriority priority) {
        metadata.put("priority", priority);
        return this;
    }

    /**
     * Set the context expiration time.
     * 
     * @param expirationTimeMs The expiration time in milliseconds
     * @return This builder instance for method chaining
     */
    public AgentModelContextBuilder withExpirationTime(long expirationTimeMs) {
        metadata.put("expirationTime", expirationTimeMs);
        return this;
    }

    /**
     * Set the context source.
     * 
     * @param source The source of this context
     * @return This builder instance for method chaining
     */
    public AgentModelContextBuilder withSource(String source) {
        metadata.put("source", source);
        return this;
    }

    /**
     * Build the agent model context.
     * 
     * @return The built AgentModelContext
     */
    public AgentModelContext build() {
        String contextId = generateContextId();
        long timestamp = System.currentTimeMillis();

        // Add default metadata if not set
        if (!metadata.containsKey("priority")) {
            metadata.put("priority", ContextPriority.MEDIUM);
        }
        if (!metadata.containsKey("timestamp")) {
            metadata.put("timestamp", timestamp);
        }
        if (!metadata.containsKey("contextId")) {
            metadata.put("contextId", contextId);
        }

        logger.debug("Building agent model context: {}", contextId);

        return new AgentModelContext(contextId, contextData, metadata);
    }

    /**
     * Generate a unique context ID.
     * 
     * @return A unique context identifier
     */
    private String generateContextId() {
        return "context_" + contextIdCounter.incrementAndGet() + "_" + System.currentTimeMillis();
    }

    /**
     * Context priority levels.
     */
    // Extracted: org.openhab.core.ai.reasoning.ContextPriority

    /**
     * Agent Model Context class.
     */
    // Extracted: org.openhab.core.ai.reasoning.AgentModelContext
}
