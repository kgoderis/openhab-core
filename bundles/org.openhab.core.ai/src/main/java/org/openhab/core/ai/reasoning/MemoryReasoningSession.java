package org.openhab.core.ai.reasoning;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.reasoning.api.ReasoningContext;

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
        interactions.add(new SessionInteraction(input, output, metadata, Instant.now()));
        lastActivityAt = Instant.now();
    }

    public String getSessionId() { return sessionId; }
    public String getAgentId() { return agentId; }
    public ReasoningContext getInitialContext() { return initialContext; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getLastActivityAt() { return lastActivityAt; }
    public List<SessionInteraction> getInteractions() { return new ArrayList<>(interactions); }

    public static class SessionInteraction {
        private final String input;
        private final String output;
        private final Map<String, Object> metadata;
        private final Instant timestamp;

        public SessionInteraction(String input, String output, Map<String, Object> metadata, Instant timestamp) {
            this.input = input;
            this.output = output;
            this.metadata = metadata;
            this.timestamp = timestamp;
        }

        public String getInput() { return input; }
        public String getOutput() { return output; }
        public Map<String, Object> getMetadata() { return metadata; }
        public Instant getTimestamp() { return timestamp; }
    }
}


