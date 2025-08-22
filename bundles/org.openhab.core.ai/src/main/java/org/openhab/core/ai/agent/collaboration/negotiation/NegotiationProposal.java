package org.openhab.core.ai.agent.collaboration.negotiation;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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
        this.proposal = Map.copyOf(builder.proposal);
        this.submittedAt = builder.submittedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        NegotiationProposal that = (NegotiationProposal) o;
        return sessionId.equals(that.sessionId) && agentId.equals(that.agentId) && proposal.equals(that.proposal)
                && submittedAt.equals(that.submittedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sessionId, agentId, proposal, submittedAt);
    }

    @Override
    public String toString() {
        return "NegotiationProposal [sessionId=" + sessionId + ", agentId=" + agentId + ", proposal=" + proposal
                + ", submittedAt=" + submittedAt + "]";
    }

    public static final class Builder {
        private String sessionId = "";
        private String agentId = "";
        private Map<String, Object> proposal = Map.of();
        private Instant submittedAt = Instant.now();

        public Builder() {
        }

        public Builder(NegotiationProposal source) {
            this.sessionId = source.sessionId;
            this.agentId = source.agentId;
            this.proposal = new HashMap<>(source.proposal);
            this.submittedAt = source.submittedAt;
        }

        public Builder withSessionId(String sessionId) {
            this.sessionId = Objects.requireNonNull(sessionId, "sessionId");
            return this;
        }

        public Builder withAgentId(String agentId) {
            this.agentId = Objects.requireNonNull(agentId, "agentId");
            return this;
        }

        public Builder withProposal(Map<String, Object> proposal) {
            this.proposal = new HashMap<>(Objects.requireNonNull(proposal, "proposal"));
            return this;
        }

        public Builder withSubmittedAt(Instant submittedAt) {
            this.submittedAt = Objects.requireNonNull(submittedAt, "submittedAt");
            return this;
        }

        public NegotiationProposal build() {
            if (sessionId.isBlank()) {
                throw new IllegalArgumentException("sessionId must not be blank");
            }
            if (agentId.isBlank()) {
                throw new IllegalArgumentException("agentId must not be blank");
            }
            return new NegotiationProposal(this);
        }
    }
}
