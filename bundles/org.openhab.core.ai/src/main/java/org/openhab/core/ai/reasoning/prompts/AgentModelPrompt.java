package org.openhab.core.ai.reasoning.prompts;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

@NonNullByDefault
public class AgentModelPrompt {
    private final String promptText;
    private final Map<String, Object> parameters;
    private final Map<String, Object> metadata;

    public AgentModelPrompt(String promptText, Map<String, Object> parameters, Map<String, Object> metadata) {
        this.promptText = promptText;
        this.parameters = new ConcurrentHashMap<>(parameters);
        this.metadata = new ConcurrentHashMap<>(metadata);
    }

    public String getPromptText() {
        return promptText;
    }

    public Map<String, Object> getParameters() {
        return new ConcurrentHashMap<>(parameters);
    }

    public Map<String, Object> getMetadata() {
        return new ConcurrentHashMap<>(metadata);
    }

    @Nullable
    public Object getParameter(String key) {
        return parameters.get(key);
    }

    @Nullable
    public Object getMetadata(String key) {
        return metadata.get(key);
    }

    public boolean hasParameter(String key) {
        return parameters.containsKey(key);
    }

    public boolean hasMetadata(String key) {
        return metadata.containsKey(key);
    }

    public PromptType getType() {
        return (PromptType) metadata.getOrDefault("type", PromptType.GENERAL);
    }

    public PromptPriority getPriority() {
        return (PromptPriority) metadata.getOrDefault("priority", PromptPriority.MEDIUM);
    }

    public String getVersion() {
        return (String) metadata.getOrDefault("version", "1.0");
    }

    public long getTimestamp() {
        return (Long) metadata.getOrDefault("timestamp", 0L);
    }

    @Override
    public String toString() {
        return "AgentModelPrompt{type=" + getType() + ", priority=" + getPriority() + ", length=" + promptText.length()
                + ", parameters=" + parameters.size() + "}";
    }
}
