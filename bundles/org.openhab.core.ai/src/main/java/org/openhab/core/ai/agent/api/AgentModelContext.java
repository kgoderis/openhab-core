package org.openhab.core.ai.agent.api;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

@NonNullByDefault
public class AgentModelContext {
    private final String contextId;
    private final Map<String, Object> contextData;
    private final Map<String, Object> metadata;

    public AgentModelContext(String contextId, Map<String, Object> contextData, Map<String, Object> metadata) {
        this.contextId = contextId;
        this.contextData = new ConcurrentHashMap<>(contextData);
        this.metadata = new ConcurrentHashMap<>(metadata);
    }

    /**
     * Create a new builder for AgentModelContext.
     *
     * @return A new AgentModelContextBuilder instance
     */
    public static AgentModelContextBuilder builder() {
        return AgentModelContextBuilder.create();
    }

    public String getContextId() {
        return contextId;
    }

    public Map<String, Object> getContextData() {
        return new ConcurrentHashMap<>(contextData);
    }

    public Map<String, Object> getMetadata() {
        return new ConcurrentHashMap<>(metadata);
    }

    public @Nullable Object getContextData(String key) {
        return contextData.get(key);
    }

    public @Nullable Object getMetadata(String key) {
        return metadata.get(key);
    }

    public boolean hasContextData(String key) {
        return contextData.containsKey(key);
    }

    public boolean hasMetadata(String key) {
        return metadata.containsKey(key);
    }

    /**
     * Returns the agent specialization if present in metadata under key "specialization".
     *
     * @return specialization string or null if not present
     */
    public @Nullable String getSpecialization() {
        @Nullable
        Object value = metadata.get("specialization");
        return value != null ? String.valueOf(value) : null;
    }

    /**
     * Returns the domain if present in metadata under key "domain".
     *
     * @return domain string or null if not present
     */
    public @Nullable String getDomain() {
        @Nullable
        Object value = metadata.get("domain");
        return value != null ? String.valueOf(value) : null;
    }

    /**
     * Returns capabilities map if present in context data under key "capabilities".
     *
     * @return immutable copy of capabilities map, or empty map if absent
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getCapabilities() {
        @Nullable
        Object value = contextData.get("capabilities");
        if (value instanceof Map<?, ?> map) {
            return Collections.unmodifiableMap(new ConcurrentHashMap<>((Map<String, Object>) map));
        }
        return Collections.emptyMap();
    }

    /**
     * Returns constraints map if present in context data under key "constraints".
     *
     * @return immutable copy of constraints map, or empty map if absent
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getConstraints() {
        @Nullable
        Object value = contextData.get("constraints");
        if (value instanceof Map<?, ?> map) {
            return Collections.unmodifiableMap(new ConcurrentHashMap<>((Map<String, Object>) map));
        }
        return Collections.emptyMap();
    }

    /**
     * Returns preferences map if present in context data under key "preferences".
     *
     * @return immutable copy of preferences map, or empty map if absent
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getPreferences() {
        @Nullable
        Object value = contextData.get("preferences");
        if (value instanceof Map<?, ?> map) {
            return Collections.unmodifiableMap(new ConcurrentHashMap<>((Map<String, Object>) map));
        }
        return Collections.emptyMap();
    }

    /**
     * Returns prompt templates if present in metadata under key "promptTemplates".
     * The expected value type is Map<String, String>.
     *
     * @return immutable copy of prompt templates, or empty map if absent
     */
    @SuppressWarnings("unchecked")
    public Map<String, String> getPromptTemplates() {
        @Nullable
        Object value = metadata.get("promptTemplates");
        if (value instanceof Map<?, ?> map) {
            return Collections.unmodifiableMap(new ConcurrentHashMap<>((Map<String, String>) map));
        }
        return Collections.emptyMap();
    }

    @Override
    public String toString() {
        return "AgentModelContext{contextId='" + contextId + "', dataSize=" + contextData.size() + ", metadataSize="
                + metadata.size() + "}";
    }
}
