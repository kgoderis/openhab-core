package org.openhab.core.ai.agent.collaboration.negotiation;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

    private NegotiationSession(Builder builder) {
        this.sessionId = builder.sessionId;
        this.initiatorId = builder.initiatorId;
        this.participantIds = Set.copyOf(builder.participantIds);
        this.templateId = builder.templateId;
        this.strategyId = builder.strategyId;
        this.initialProposal = Map.copyOf(builder.initialProposal);
        this.status = builder.status;
        this.createdAt = builder.createdAt;
        this.timeoutAt = builder.timeoutAt;
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

    public static final class Builder {
        private String sessionId = "";
        private String initiatorId = "";
        private Set<String> participantIds = Set.of();
        private String templateId = "";
        private String strategyId = "";
        private Map<String, Object> initialProposal = Map.of();
        private NegotiationStatus status = NegotiationStatus.ACTIVE;
        private Instant createdAt = Instant.now();
        private Instant timeoutAt = Instant.now().plusSeconds(300);

        public Builder() {
        }

        public Builder(NegotiationSession source) {
            this.sessionId = source.sessionId;
            this.initiatorId = source.initiatorId;
            this.participantIds = new HashSet<>(source.participantIds);
            this.templateId = source.templateId;
            this.strategyId = source.strategyId;
            this.initialProposal = new HashMap<>(source.initialProposal);
            this.status = source.status;
            this.createdAt = source.createdAt;
            this.timeoutAt = source.timeoutAt;
        }

        public Builder withSessionId(String sessionId) {
            this.sessionId = Objects.requireNonNull(sessionId, "sessionId");
            return this;
        }

        public Builder withInitiatorId(String initiatorId) {
            this.initiatorId = Objects.requireNonNull(initiatorId, "initiatorId");
            return this;
        }

        public Builder withParticipantIds(Set<String> participantIds) {
            this.participantIds = new HashSet<>(Objects.requireNonNull(participantIds, "participantIds"));
            return this;
        }

        public Builder withTemplateId(String templateId) {
            this.templateId = Objects.requireNonNull(templateId, "templateId");
            return this;
        }

        public Builder withStrategyId(String strategyId) {
            this.strategyId = Objects.requireNonNull(strategyId, "strategyId");
            return this;
        }

        public Builder withInitialProposal(Map<String, Object> initialProposal) {
            this.initialProposal = new HashMap<>(Objects.requireNonNull(initialProposal, "initialProposal"));
            return this;
        }

        public Builder withStatus(NegotiationStatus status) {
            this.status = Objects.requireNonNull(status, "status");
            return this;
        }

        public Builder withCreatedAt(Instant createdAt) {
            this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
            return this;
        }

        public Builder withTimeoutAt(Instant timeoutAt) {
            this.timeoutAt = Objects.requireNonNull(timeoutAt, "timeoutAt");
            return this;
        }

        public NegotiationSession build() {
            if (sessionId.isBlank()) {
                throw new IllegalArgumentException("sessionId must not be blank");
            }
            if (initiatorId.isBlank()) {
                throw new IllegalArgumentException("initiatorId must not be blank");
            }
            if (participantIds.isEmpty()) {
                throw new IllegalArgumentException("participantIds must not be empty");
            }
            if (templateId.isBlank()) {
                throw new IllegalArgumentException("templateId must not be blank");
            }
            if (strategyId.isBlank()) {
                throw new IllegalArgumentException("strategyId must not be blank");
            }
            if (timeoutAt.isBefore(createdAt)) {
                throw new IllegalArgumentException("timeoutAt must be after createdAt");
            }
            return new NegotiationSession(this);
        }
    }
}
