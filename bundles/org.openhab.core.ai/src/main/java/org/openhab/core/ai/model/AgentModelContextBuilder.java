package org.openhab.core.ai.model;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

@NonNullByDefault
public class AgentModelContextBuilder {
    String agentId;
    String agentType;
    final Map<String, Object> contextData = new HashMap<>();
    final Map<String, String> metadata = new HashMap<>();
    @Nullable Instant timestamp;
    @Nullable String sessionId;
    int priority = 0;

    public AgentModelContextBuilder agentId(String agentId) { this.agentId = agentId; return this; }
    public AgentModelContextBuilder agentType(String agentType) { this.agentType = agentType; return this; }
    public AgentModelContextBuilder contextData(String key, Object value) { this.contextData.put(key, value); return this; }
    public AgentModelContextBuilder contextData(Map<String, Object> contextData) { this.contextData.putAll(contextData); return this; }
    public AgentModelContextBuilder metadata(String key, String value) { this.metadata.put(key, value); return this; }
    public AgentModelContextBuilder metadata(Map<String, String> metadata) { this.metadata.putAll(metadata); return this; }
    public AgentModelContextBuilder timestamp(Instant timestamp) { this.timestamp = timestamp; return this; }
    public AgentModelContextBuilder sessionId(String sessionId) { this.sessionId = sessionId; return this; }
    public AgentModelContextBuilder priority(int priority) { this.priority = priority; return this; }

    public AgentModelContextBuilder() {}
    public AgentModelContextBuilder(AgentModelContext context) {
        this.agentId = context.getAgentId();
        this.agentType = context.getAgentType();
        this.contextData.putAll(context.getContextData());
        this.metadata.putAll(context.getMetadata());
        this.timestamp = context.getTimestamp();
        this.sessionId = context.getSessionId();
        this.priority = context.getPriority();
    }

    public AgentModelContext build() { return new AgentModelContext(this); }
}


