package org.openhab.core.ai.agent.collaboration.negotiation;

import java.time.Instant;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Represents a proposal submitted by an agent in a negotiation session
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class NegotiationProposal {

    private final String sessionId;
    private final String agentId;
    private final Map<String, Object> proposal;
    private final Instant submittedAt;

    private NegotiationProposal(Builder builder) {
        this.sessionId = builder.sessionId;
        this.agentId = builder.agentId;
        this.proposal = builder.proposal;
        this.submittedAt = builder.submittedAt;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getAgentId() {
        return agentId;
    }

    public Map<String, Object> getProposal() {
        return proposal;
    }

    public Instant getSubmittedAt() {
        return submittedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String sessionId = "";
        private String agentId = "";
        private Map<String, Object> proposal = Map.of();
        private Instant submittedAt = Instant.now();

        public Builder sessionId(String sessionId) {
            this.sessionId = sessionId;
            return this;
        }

        public Builder agentId(String agentId) {
            this.agentId = agentId;
            return this;
        }

        public Builder proposal(Map<String, Object> proposal) {
            this.proposal = proposal;
            return this;
        }

        public Builder submittedAt(Instant submittedAt) {
            this.submittedAt = submittedAt;
            return this;
        }

        public NegotiationProposal build() {
            return new NegotiationProposal(this);
        }
    }
}
