package org.openhab.core.ai.common.context;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Unified Agent Model Context for building agent-specific contexts.
 * 
 * <p>
 * This class provides a comprehensive context for agent operations including:
 * - Agent identification and type information
 * - Domain-specific context data
 * - Capabilities and skills
 * - Current state and historical data
 * - User preferences and environmental factors
 * - Reasoning context and metadata
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public final class AgentModelContext extends BaseContext {

    private static final String CONTEXT_TYPE = "agent_model";
    private static final String VERSION = "1.0.0";

    private AgentModelContext(String contextId, Map<String, Object> values, Map<String, Object> metadata) {
        super(contextId, CONTEXT_TYPE, VERSION, values, metadata);
    }

    private AgentModelContext(String contextId, Map<String, Object> values, Map<String, Object> metadata,
            Instant createdAt, Instant lastModifiedAt) {
        super(contextId, CONTEXT_TYPE, VERSION, values, metadata, createdAt, lastModifiedAt);
    }

    /**
     * Get the agent ID.
     * 
     * @return the agent ID or null if not set
     */
    public @Nullable String getAgentId() {
        return getValue("agentId", String.class);
    }

    /**
     * Get the agent type.
     * 
     * @return the agent type or null if not set
     */
    public @Nullable String getAgentType() {
        return getValue("agentType", String.class);
    }

    /**
     * Get the domain context.
     * 
     * @return the domain or null if not set
     */
    public @Nullable String getDomain() {
        return getValue("domain", String.class);
    }

    /**
     * Get the agent capabilities.
     * 
     * @return immutable map of capabilities or empty map if not set
     */
    @SuppressWarnings("unchecked")
    public Map<String, String> getCapabilities() {
        Object value = getValue("capabilities");
        if (value instanceof Map<?, ?> map) {
            return Collections.unmodifiableMap((Map<String, String>) map);
        }
        return Collections.emptyMap();
    }

    /**
     * Get the agent skills.
     * 
     * @return immutable map of skills or empty map if not set
     */
    @SuppressWarnings("unchecked")
    public Map<String, String> getSkills() {
        Object value = getValue("skills");
        if (value instanceof Map<?, ?> map) {
            return Collections.unmodifiableMap((Map<String, String>) map);
        }
        return Collections.emptyMap();
    }

    /**
     * Get the current state.
     * 
     * @return immutable map of current state or empty map if not set
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getCurrentState() {
        Object value = getValue("currentState");
        if (value instanceof Map<?, ?> map) {
            return Collections.unmodifiableMap((Map<String, Object>) map);
        }
        return Collections.emptyMap();
    }

    /**
     * Get the historical data.
     * 
     * @return the history object or null if not set
     */
    public @Nullable Object getHistory() {
        return getValue("history");
    }

    /**
     * Get the user preferences.
     * 
     * @return immutable map of user preferences or empty map if not set
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getUserPreferences() {
        Object value = getValue("userPreferences");
        if (value instanceof Map<?, ?> map) {
            return Collections.unmodifiableMap((Map<String, Object>) map);
        }
        return Collections.emptyMap();
    }

    /**
     * Get the environmental factors.
     * 
     * @return immutable map of environmental factors or empty map if not set
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getEnvironment() {
        Object value = getValue("environment");
        if (value instanceof Map<?, ?> map) {
            return Collections.unmodifiableMap((Map<String, Object>) map);
        }
        return Collections.emptyMap();
    }

    /**
     * Get the reasoning context.
     * 
     * @return immutable map of reasoning parameters or empty map if not set
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getReasoning() {
        Object value = getValue("reasoning");
        if (value instanceof Map<?, ?> map) {
            return Collections.unmodifiableMap((Map<String, Object>) map);
        }
        return Collections.emptyMap();
    }

    /**
     * Get the specialization.
     * 
     * @return the specialization or null if not set
     */
    public @Nullable String getSpecialization() {
        return getValue("specialization", String.class);
    }

    /**
     * Get the context priority.
     * 
     * @return the priority or null if not set
     */
    public @Nullable String getPriority() {
        Object value = getMetadata("priority");
        return value instanceof String ? (String) value : null;
    }

    /**
     * Get the expiration time.
     * 
     * @return the expiration time in milliseconds or null if not set
     */
    public @Nullable Long getExpirationTime() {
        Object value = getMetadata("expirationTime");
        return value instanceof Long ? (Long) value : null;
    }

    /**
     * Get the context source.
     * 
     * @return the source or null if not set
     */
    public @Nullable String getSource() {
        Object value = getMetadata("source");
        return value instanceof String ? (String) value : null;
    }

    /**
     * Check if the context has expired.
     * 
     * @return true if the context has expired, false otherwise
     */
    public boolean isExpired() {
        Long expirationTime = getExpirationTime();
        if (expirationTime == null) {
            return false;
        }
        return System.currentTimeMillis() > expirationTime;
    }

    /**
     * Create a new builder for AgentModelContext.
     * 
     * @return a new AgentModelContextBuilder instance
     */
    public static AgentModelContextBuilder builder() {
        return new AgentModelContextBuilder();
    }

    /**
     * Create a copy of this context with updated values.
     * 
     * @return a new AgentModelContextBuilder pre-populated with this context's data
     */
    public AgentModelContextBuilder toBuilder() {
        return new AgentModelContextBuilder(this);
    }

    @Override
    public String toString() {
        return String.format("AgentModelContext{id='%s', agentId='%s', agentType='%s', domain='%s', size=%d}",
                getContextId(), getAgentId(), getAgentType(), getDomain(), size());
    }

    @Override
    protected BaseContext createCopy(Map<String, Object> newValues, Map<String, Object> newMetadata) {
        return new AgentModelContext(getContextId(), newValues, newMetadata);
    }

    public Map<String, Object> getConstraints() {
        return (Map<String, Object>) getValue("constraints");
    }

    public Map<String, Object> getPreferences() {
        return (Map<String, Object>) getValue("preferences");
    }

    public Map<String, String> getPromptTemplates() {
        return (Map<String, String>) getValue("promptTemplates");
    }

    public Object getContextData(String key) {
        return getValue(key);
    }

    public boolean hasContextData(String key) {
        return hasValue(key);
    }

    public Map<String, Object> getContextData() {
        return getAllValues();
    }

    /**
     * Builder for AgentModelContext.
     */
    public static final class AgentModelContextBuilder {
        private String contextId;
        private final Map<String, Object> values = new java.util.HashMap<>();
        private final Map<String, Object> metadata = new java.util.HashMap<>();

        public AgentModelContextBuilder() {
            this.contextId = "agent_context_" + System.currentTimeMillis() + "_" + Objects.hash(this);
        }

        public AgentModelContextBuilder(AgentModelContext source) {
            this.contextId = source.getContextId();
            this.values.putAll(source.getAllValues());
            this.metadata.putAll(source.getMetadata());
        }

        public AgentModelContextBuilder withContextId(String contextId) {
            this.contextId = Objects.requireNonNull(contextId, "Context ID cannot be null");
            return this;
        }

        public AgentModelContextBuilder withAgentId(String agentId) {
            this.values.put("agentId", Objects.requireNonNull(agentId, "Agent ID cannot be null"));
            return this;
        }

        public AgentModelContextBuilder withAgentType(String agentType) {
            this.values.put("agentType", Objects.requireNonNull(agentType, "Agent type cannot be null"));
            return this;
        }

        public AgentModelContextBuilder withDomain(String domain) {
            this.values.put("domain", Objects.requireNonNull(domain, "Domain cannot be null"));
            return this;
        }

        public AgentModelContextBuilder withCapabilities(Map<String, String> capabilities) {
            this.values.put("capabilities", Objects.requireNonNull(capabilities, "Capabilities cannot be null"));
            return this;
        }

        public AgentModelContextBuilder withSkills(Map<String, String> skills) {
            this.values.put("skills", Objects.requireNonNull(skills, "Skills cannot be null"));
            return this;
        }

        public AgentModelContextBuilder withCurrentState(Map<String, Object> state) {
            this.values.put("currentState", Objects.requireNonNull(state, "Current state cannot be null"));
            return this;
        }

        public AgentModelContextBuilder withHistory(Object history) {
            this.values.put("history", history);
            return this;
        }

        public AgentModelContextBuilder withUserPreferences(Map<String, Object> preferences) {
            this.values.put("userPreferences", Objects.requireNonNull(preferences, "User preferences cannot be null"));
            return this;
        }

        public AgentModelContextBuilder withEnvironment(Map<String, Object> environment) {
            this.values.put("environment", Objects.requireNonNull(environment, "Environment cannot be null"));
            return this;
        }

        public AgentModelContextBuilder withReasoning(Map<String, Object> reasoning) {
            this.values.put("reasoning", Objects.requireNonNull(reasoning, "Reasoning cannot be null"));
            return this;
        }

        public AgentModelContextBuilder withSpecialization(String specialization) {
            this.values.put("specialization", Objects.requireNonNull(specialization, "Specialization cannot be null"));
            return this;
        }

        public AgentModelContextBuilder withPriority(String priority) {
            this.metadata.put("priority", Objects.requireNonNull(priority, "Priority cannot be null"));
            return this;
        }

        public AgentModelContextBuilder withExpirationTime(long expirationTimeMs) {
            this.metadata.put("expirationTime", expirationTimeMs);
            return this;
        }

        public AgentModelContextBuilder withSource(String source) {
            this.metadata.put("source", Objects.requireNonNull(source, "Source cannot be null"));
            return this;
        }

        public AgentModelContextBuilder withValue(String key, Object value) {
            this.values.put(Objects.requireNonNull(key, "Key cannot be null"), value);
            return this;
        }

        public AgentModelContextBuilder withMetadata(String key, Object value) {
            this.metadata.put(Objects.requireNonNull(key, "Key cannot be null"), value);
            return this;
        }

        public AgentModelContext build() {
            validate();
            return new AgentModelContext(contextId, values, metadata);
        }

        public AgentModelContextBuilder create() {
            return this;
        }

        private void validate() {
            if (contextId == null || contextId.trim().isEmpty()) {
                throw new IllegalArgumentException("Context ID cannot be null or empty");
            }
        }
    }
}
