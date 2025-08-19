package org.openhab.core.ai.common.builder;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.agent.collaboration.negotiation.NegotiationSession;
import org.openhab.core.ai.agent.collaboration.negotiation.NegotiationStatus;

/**
 * Unified builder for NegotiationSession objects.
 *
 * <p>
 * This builder provides a standardized way to create NegotiationSession objects
 * with proper validation and consistent API.
 * </p>
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public final class NegotiationSessionBuilder extends SessionBuilder<NegotiationSession> {

    /**
     * Set the negotiation status.
     *
     * @param status the negotiation status
     * @return this builder
     */
    public NegotiationSessionBuilder withNegotiationStatus(NegotiationStatus status) {
        withStatus(Objects.requireNonNull(status, "status"));
        return this;
    }

    @Override
    public NegotiationSession build() {
        validate();
        return new NegotiationSession(sessionId, initiatorId, participantIds, templateId, strategyId, initialProposal,
                (NegotiationStatus) status, createdAt, timeoutAt);
    }

    @Override
    protected void validate() {
        super.validate();
        // Additional validation specific to NegotiationSession
        if (status == null) {
            throw new IllegalArgumentException("status must not be null");
        }
    }

    /**
     * Create a new NegotiationSessionBuilder instance.
     *
     * @return a new builder instance
     */
    public static NegotiationSessionBuilder builder() {
        return new NegotiationSessionBuilder();
    }

    /**
     * Create a builder from an existing NegotiationSession.
     *
     * @param session the existing session
     * @return a builder with values from the existing session
     */
    public static NegotiationSessionBuilder builder(NegotiationSession session) {
        Objects.requireNonNull(session, "session");
        return (NegotiationSessionBuilder) builder().withSessionId(session.getSessionId())
                .withInitiatorId(session.getInitiatorId()).withParticipantIds(session.getParticipantIds())
                .withTemplateId(session.getTemplateId()).withStrategyId(session.getStrategyId())
                .withInitialProposal(session.getInitialProposal()).withStatus(session.getStatus())
                .withCreatedAt(session.getCreatedAt()).withTimeoutAt(session.getTimeoutAt());
    }

    // Getters for the NegotiationSession constructor
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
        return (NegotiationStatus) status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getTimeoutAt() {
        return timeoutAt;
    }
}
