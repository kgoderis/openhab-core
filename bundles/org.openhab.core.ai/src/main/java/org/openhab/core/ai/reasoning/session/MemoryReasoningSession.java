package org.openhab.core.ai.reasoning.session;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.context.ReasoningContext;

@NonNullByDefault
public class MemoryReasoningSession {
    private final String sessionId;
    private final String agentId;
    private final ReasoningContext initialContext;
    private final Instant createdAt;
    private Instant lastActivityAt;
    private final List<SessionInteraction> interactions = new ArrayList<>();

    public MemoryReasoningSession(String sessionId, String agentId, ReasoningContext initialContext) {
        this.sessionId = sessionId;
        this.agentId = agentId;
        this.initialContext = initialContext;
        this.createdAt = Instant.now();
        this.lastActivityAt = Instant.now();
    }

    public void addInteraction(String input, String output, Map<String, Object> metadata) {
        interactions.add(new SessionInteraction(input, output, Instant.now()));
        lastActivityAt = Instant.now();
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getAgentId() {
        return agentId;
    }

    public ReasoningContext getInitialContext() {
        return initialContext;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getLastActivityAt() {
        return lastActivityAt;
    }

    public List<SessionInteraction> getInteractions() {
        return new ArrayList<>(interactions);
    }
}
