package org.openhab.core.ai.reasoning;

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

    @Override
    public String toString() {
        return "AgentModelContext{contextId='" + contextId + "', dataSize=" + contextData.size() + ", metadataSize="
                + metadata.size() + "}";
    }
}


