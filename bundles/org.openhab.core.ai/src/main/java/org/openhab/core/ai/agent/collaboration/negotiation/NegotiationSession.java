package org.openhab.core.ai.agent.collaboration.negotiation;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Represents a negotiation session between agents
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class NegotiationSession {

    private final String sessionId;
    private final String initiatorId;
    private final Set<String> participantIds;
    private final String templateId;
    private final String strategyId;
    private final Map<String, Object> initialProposal;
    private final Instant createdAt;
    private final Instant timeoutAt;

    private NegotiationStatus status;
    private @Nullable Map<String, Object> finalAgreement;
    private @Nullable String abortReason;
    private @Nullable Instant completedAt;
    private final List<NegotiationProposal> proposals = new ArrayList<>();

    public NegotiationSession(NegotiationSessionBuilder builder) {
        this.sessionId = builder.getSessionId();
        this.initiatorId = builder.getInitiatorId();
        this.participantIds = builder.getParticipantIds();
        this.templateId = builder.getTemplateId();
        this.strategyId = builder.getStrategyId();
        this.initialProposal = builder.getInitialProposal();
        this.status = builder.getStatus();
        this.createdAt = builder.getCreatedAt();
        this.timeoutAt = builder.getTimeoutAt();
    }

    public NegotiationSession(String sessionId, String initiatorId, Set<String> participantIds, String templateId,
            String strategyId, Map<String, Object> initialProposal, NegotiationStatus status, Instant createdAt,
            Instant timeoutAt) {
        this.sessionId = sessionId;
        this.initiatorId = initiatorId;
        this.participantIds = participantIds;
        this.templateId = templateId;
        this.strategyId = strategyId;
        this.initialProposal = initialProposal;
        this.status = status;
        this.createdAt = createdAt;
        this.timeoutAt = timeoutAt;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getInitiatorId() {
        return initiatorId;
    }

    public Set<String> getParticipantIds() {
        return participantIds;
    }

    public String getTemplateId() {
        return templateId;
    }

    public String getStrategyId() {
        return strategyId;
    }

    public Map<String, Object> getInitialProposal() {
        return initialProposal;
    }

    public NegotiationStatus getStatus() {
        return status;
    }

    public void setStatus(NegotiationStatus status) {
        this.status = status;
    }

    public @Nullable Map<String, Object> getFinalAgreement() {
        return finalAgreement;
    }

    public void setFinalAgreement(@Nullable Map<String, Object> finalAgreement) {
        this.finalAgreement = finalAgreement;
    }

    public @Nullable String getAbortReason() {
        return abortReason;
    }

    public void setAbortReason(@Nullable String abortReason) {
        this.abortReason = abortReason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getTimeoutAt() {
        return timeoutAt;
    }

    public @Nullable Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(@Nullable Instant completedAt) {
        this.completedAt = completedAt;
    }

    public List<NegotiationProposal> getProposals() {
        return new ArrayList<>(proposals);
    }

    public void addProposal(NegotiationProposal proposal) {
        proposals.add(proposal);
    }

    public static NegotiationSessionBuilder builder() {
        return new NegotiationSessionBuilder();
    }
}
