package org.openhab.core.ai.agent.collaboration.negotiation;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class NegotiationSessionBuilder {
    private String sessionId = "";
    private String initiatorId = "";
    private Set<String> participantIds = Set.of();
    private String templateId = "";
    private String strategyId = "";
    private Map<String, Object> initialProposal = Map.of();
    private NegotiationStatus status = NegotiationStatus.ACTIVE;
    private Instant createdAt = Instant.now();
    private Instant timeoutAt = Instant.now().plusSeconds(300);

    public NegotiationSessionBuilder sessionId(String sessionId) { this.sessionId = sessionId; return this; }
    public NegotiationSessionBuilder initiatorId(String initiatorId) { this.initiatorId = initiatorId; return this; }
    public NegotiationSessionBuilder participantIds(Set<String> participantIds) { this.participantIds = participantIds; return this; }
    public NegotiationSessionBuilder templateId(String templateId) { this.templateId = templateId; return this; }
    public NegotiationSessionBuilder strategyId(String strategyId) { this.strategyId = strategyId; return this; }
    public NegotiationSessionBuilder initialProposal(Map<String, Object> initialProposal) { this.initialProposal = initialProposal; return this; }
    public NegotiationSessionBuilder status(NegotiationStatus status) { this.status = status; return this; }
    public NegotiationSessionBuilder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }
    public NegotiationSessionBuilder timeoutAt(Instant timeoutAt) { this.timeoutAt = timeoutAt; return this; }

    public NegotiationSession build() {
        return NegotiationSession.builder().sessionId(sessionId).initiatorId(initiatorId).participantIds(participantIds)
                .templateId(templateId).strategyId(strategyId).initialProposal(initialProposal).status(status)
                .createdAt(createdAt).timeoutAt(timeoutAt).build();
    }
}


