package org.openhab.core.ai.common.builder;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Unified builder for session-related objects in the openHAB AI system.
 *
 * <p>
 * This class provides a common builder pattern for creating session-related objects
 * such as NegotiationSession, CoordinationSession, etc.
 * </p>
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public abstract class SessionBuilder<T> extends AbstractBuilder<T> {

    protected String sessionId = "";
    protected String initiatorId = "";
    protected Set<String> participantIds = Set.of();
    protected String templateId = "";
    protected String strategyId = "";
    protected Map<String, Object> initialProposal = Map.of();
    protected @Nullable Object status;
    protected Instant createdAt = Instant.now();
    protected Instant timeoutAt = Instant.now().plusSeconds(300);
    protected List<String> participants = List.of();
    protected Map<String, Object> sessionData = Map.of();
    protected @Nullable Instant sessionStartTime;
    protected @Nullable Instant sessionEndTime;
    protected Map<String, Object> sessionMetadata = Map.of();

    /**
     * Set the session ID.
     *
     * @param sessionId the session ID
     * @return this builder
     */
    public SessionBuilder<T> withSessionId(String sessionId) {
        this.sessionId = Objects.requireNonNull(sessionId, "sessionId");
        return this;
    }

    /**
     * Set the initiator ID.
     *
     * @param initiatorId the initiator ID
     * @return this builder
     */
    public SessionBuilder<T> withInitiatorId(String initiatorId) {
        this.initiatorId = Objects.requireNonNull(initiatorId, "initiatorId");
        return this;
    }

    /**
     * Set the participant IDs.
     *
     * @param participantIds the participant IDs
     * @return this builder
     */
    public SessionBuilder<T> withParticipantIds(Set<String> participantIds) {
        this.participantIds = Objects.requireNonNull(participantIds, "participantIds");
        return this;
    }

    /**
     * Set the template ID.
     *
     * @param templateId the template ID
     * @return this builder
     */
    public SessionBuilder<T> withTemplateId(String templateId) {
        this.templateId = Objects.requireNonNull(templateId, "templateId");
        return this;
    }

    /**
     * Set the strategy ID.
     *
     * @param strategyId the strategy ID
     * @return this builder
     */
    public SessionBuilder<T> withStrategyId(String strategyId) {
        this.strategyId = Objects.requireNonNull(strategyId, "strategyId");
        return this;
    }

    /**
     * Set the initial proposal.
     *
     * @param initialProposal the initial proposal
     * @return this builder
     */
    public SessionBuilder<T> withInitialProposal(Map<String, Object> initialProposal) {
        this.initialProposal = Objects.requireNonNull(initialProposal, "initialProposal");
        return this;
    }

    /**
     * Set the status.
     *
     * @param status the status
     * @return this builder
     */
    public SessionBuilder<T> withStatus(@Nullable Object status) {
        this.status = status;
        return this;
    }

    /**
     * Set the created timestamp.
     *
     * @param createdAt the created timestamp
     * @return this builder
     */
    public SessionBuilder<T> withCreatedAt(Instant createdAt) {
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
        return this;
    }

    /**
     * Set the timeout timestamp.
     *
     * @param timeoutAt the timeout timestamp
     * @return this builder
     */
    public SessionBuilder<T> withTimeoutAt(Instant timeoutAt) {
        this.timeoutAt = Objects.requireNonNull(timeoutAt, "timeoutAt");
        return this;
    }

    /**
     * Set the participants.
     *
     * @param participants the participants
     * @return this builder
     */
    public SessionBuilder<T> withParticipants(List<String> participants) {
        this.participants = Objects.requireNonNull(participants, "participants");
        return this;
    }

    /**
     * Set the session data.
     *
     * @param sessionData the session data
     * @return this builder
     */
    public SessionBuilder<T> withSessionData(Map<String, Object> sessionData) {
        this.sessionData = Objects.requireNonNull(sessionData, "sessionData");
        return this;
    }

    /**
     * Set the session start time.
     *
     * @param sessionStartTime the session start time
     * @return this builder
     */
    public SessionBuilder<T> withSessionStartTime(@Nullable Instant sessionStartTime) {
        this.sessionStartTime = sessionStartTime;
        return this;
    }

    /**
     * Set the session end time.
     *
     * @param sessionEndTime the session end time
     * @return this builder
     */
    public SessionBuilder<T> withSessionEndTime(@Nullable Instant sessionEndTime) {
        this.sessionEndTime = sessionEndTime;
        return this;
    }

    /**
     * Set the session metadata.
     *
     * @param sessionMetadata the session metadata
     * @return this builder
     */
    public SessionBuilder<T> withSessionMetadata(Map<String, Object> sessionMetadata) {
        this.sessionMetadata = Objects.requireNonNull(sessionMetadata, "sessionMetadata");
        return this;
    }

    @Override
    protected void validate() {
        super.validate();
        // Additional validation specific to SessionBuilder
        if (sessionId.isBlank()) {
            throw new IllegalArgumentException("sessionId must not be blank");
        }
        if (initiatorId.isBlank()) {
            throw new IllegalArgumentException("initiatorId must not be blank");
        }
        if (timeoutAt.isBefore(createdAt)) {
            throw new IllegalArgumentException("timeoutAt must be after createdAt");
        }
    }

    @Override
    protected void doReset() {
        super.doReset();
        sessionId = "";
        initiatorId = "";
        participantIds = Set.of();
        templateId = "";
        strategyId = "";
        initialProposal = Map.of();
        status = null;
        createdAt = Instant.now();
        timeoutAt = Instant.now().plusSeconds(300);
        participants = List.of();
        sessionData = Map.of();
        sessionStartTime = null;
        sessionEndTime = null;
        sessionMetadata = Map.of();
    }
}
