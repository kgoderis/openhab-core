package org.openhab.core.ai.agent.negotiation;

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

    private AgentNegotiationService.NegotiationStatus status;
    private @Nullable Map<String, Object> finalAgreement;
    private @Nullable String abortReason;
    private @Nullable Instant completedAt;
    private final List<NegotiationProposal> proposals = new ArrayList<>();

    private NegotiationSession(Builder builder) {
        this.sessionId = builder.sessionId;
        this.initiatorId = builder.initiatorId;
        this.participantIds = builder.participantIds;
        this.templateId = builder.templateId;
        this.strategyId = builder.strategyId;
        this.initialProposal = builder.initialProposal;
        this.status = builder.status;
        this.createdAt = builder.createdAt;
        this.timeoutAt = builder.timeoutAt;
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

    public AgentNegotiationService.NegotiationStatus getStatus() {
        return status;
    }

    public void setStatus(AgentNegotiationService.NegotiationStatus status) {
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

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String sessionId = "";
        private String initiatorId = "";
        private Set<String> participantIds = Set.of();
        private String templateId = "";
        private String strategyId = "";
        private Map<String, Object> initialProposal = Map.of();
        private AgentNegotiationService.NegotiationStatus status = AgentNegotiationService.NegotiationStatus.ACTIVE;
        private Instant createdAt = Instant.now();
        private Instant timeoutAt = Instant.now().plusSeconds(300); // 5 minutes default

        public Builder sessionId(String sessionId) {
            this.sessionId = sessionId;
            return this;
        }

        public Builder initiatorId(String initiatorId) {
            this.initiatorId = initiatorId;
            return this;
        }

        public Builder participantIds(Set<String> participantIds) {
            this.participantIds = participantIds;
            return this;
        }

        public Builder templateId(String templateId) {
            this.templateId = templateId;
            return this;
        }

        public Builder strategyId(String strategyId) {
            this.strategyId = strategyId;
            return this;
        }

        public Builder initialProposal(Map<String, Object> initialProposal) {
            this.initialProposal = initialProposal;
            return this;
        }

        public Builder status(AgentNegotiationService.NegotiationStatus status) {
            this.status = status;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder timeoutAt(Instant timeoutAt) {
            this.timeoutAt = timeoutAt;
            return this;
        }

        public NegotiationSession build() {
            return new NegotiationSession(this);
        }
    }
}
