package org.openhab.core.ai.agent.collaboration.negotiation;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public final class NegotiationSessionBuilder {
    String sessionId = "";
    String initiatorId = "";
    Set<String> participantIds = Set.of();
    String templateId = "";
    String strategyId = "";
    Map<String, Object> initialProposal = Map.of();
    NegotiationStatus status = NegotiationStatus.ACTIVE;
    Instant createdAt = Instant.now();
    Instant timeoutAt = Instant.now().plusSeconds(300);

    public NegotiationSessionBuilder sessionId(String sessionId) {
        this.sessionId = sessionId;
        return this;
    }

    public NegotiationSessionBuilder initiatorId(String initiatorId) {
        this.initiatorId = initiatorId;
        return this;
    }

    public NegotiationSessionBuilder participantIds(Set<String> participantIds) {
        this.participantIds = participantIds;
        return this;
    }

    public NegotiationSessionBuilder templateId(String templateId) {
        this.templateId = templateId;
        return this;
    }

    public NegotiationSessionBuilder strategyId(String strategyId) {
        this.strategyId = strategyId;
        return this;
    }

    public NegotiationSessionBuilder initialProposal(Map<String, Object> initialProposal) {
        this.initialProposal = initialProposal;
        return this;
    }

    public NegotiationSessionBuilder status(NegotiationStatus status) {
        this.status = status;
        return this;
    }

    public NegotiationSessionBuilder createdAt(Instant createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public NegotiationSessionBuilder timeoutAt(Instant timeoutAt) {
        this.timeoutAt = timeoutAt;
        return this;
    }

    public NegotiationSession build() {
        return new NegotiationSession(this);
    }
}
