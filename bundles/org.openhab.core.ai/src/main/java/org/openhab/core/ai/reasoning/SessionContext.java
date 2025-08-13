package org.openhab.core.ai.reasoning;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class SessionContext {
    private final String sessionId;
    private final String agentId;
    private final Map<String, Object> contextData;
    private final Instant createdAt;
    private Instant lastUpdatedAt;

    public SessionContext(String sessionId, String agentId, Map<String, Object> contextData) {
        this.sessionId = sessionId;
        this.agentId = agentId;
        this.contextData = new ConcurrentHashMap<>(contextData);
        this.createdAt = Instant.now();
        this.lastUpdatedAt = Instant.now();
    }

    public void updateContext(String key, Object value) {
        contextData.put(key, value);
        lastUpdatedAt = Instant.now();
    }

    public void updateContext(Map<String, Object> updates) {
        contextData.putAll(updates);
        lastUpdatedAt = Instant.now();
    }

    public String getSessionId() { return sessionId; }
    public String getAgentId() { return agentId; }
    public Map<String, Object> getContextData() { return new ConcurrentHashMap<>(contextData); }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getLastUpdatedAt() { return lastUpdatedAt; }
}


