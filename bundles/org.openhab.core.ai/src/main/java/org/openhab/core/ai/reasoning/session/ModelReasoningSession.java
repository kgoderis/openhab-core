/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.core.ai.reasoning.session;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.context.AgentModelContext;

/**
 * Manages session state and interaction history for model reasoning sessions.
 * 
 * This class tracks the conversation history, context, and state information
 * for ongoing model reasoning sessions, enabling stateful interactions between
 * agents and AI models.
 * 
 * @author Karel Goderis - Initial Contribution
 */
@NonNullByDefault
public class ModelReasoningSession {

    private final String sessionId;
    private final String agentId;
    private final AgentModelContext initialContext;
    private final List<SessionInteraction> interactionHistory;
    private final Instant createdAt;
    private Instant lastActivityAt;

    /**
     * Creates a new model reasoning session.
     * 
     * @param sessionId the unique session identifier
     * @param agentId the agent identifier
     * @param initialContext the initial context for the session
     */
    public ModelReasoningSession(String sessionId, String agentId, AgentModelContext initialContext) {
        this.sessionId = Objects.requireNonNull(sessionId, "Session ID cannot be null");
        this.agentId = Objects.requireNonNull(agentId, "Agent ID cannot be null");
        this.initialContext = Objects.requireNonNull(initialContext, "Initial context cannot be null");
        this.interactionHistory = new ArrayList<>();
        this.createdAt = Instant.now();
        this.lastActivityAt = Instant.now();
    }

    /**
     * Gets the session identifier.
     * 
     * @return the session ID
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * Gets the agent identifier.
     * 
     * @return the agent ID
     */
    public String getAgentId() {
        return agentId;
    }

    /**
     * Gets the initial context for this session.
     * 
     * @return the initial context
     */
    public AgentModelContext getInitialContext() {
        return initialContext;
    }

    /**
     * Gets the interaction history for this session.
     * 
     * @return an unmodifiable list of session interactions
     */
    public List<SessionInteraction> getInteractionHistory() {
        return Collections.unmodifiableList(interactionHistory);
    }

    /**
     * Gets the timestamp when this session was created.
     * 
     * @return the creation timestamp
     */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /**
     * Gets the timestamp of the last activity in this session.
     * 
     * @return the last activity timestamp
     */
    public Instant getLastActivityAt() {
        return lastActivityAt;
    }

    /**
     * Adds a new interaction to the session history.
     * 
     * @param input the user input
     * @param response the model response
     */
    public void addInteraction(String input, String response) {
        SessionInteraction interaction = new SessionInteraction(input, response, Instant.now());
        interactionHistory.add(interaction);
        lastActivityAt = Instant.now();
    }

    /**
     * Gets the number of interactions in this session.
     * 
     * @return the interaction count
     */
    public int getInteractionCount() {
        return interactionHistory.size();
    }

    /**
     * Gets the duration of this session.
     * 
     * @return the session duration in milliseconds
     */
    public long getSessionDuration() {
        return lastActivityAt.toEpochMilli() - createdAt.toEpochMilli();
    }

    /**
     * Checks if this session has been inactive for the specified duration.
     * 
     * @param maxInactiveDurationMs the maximum inactive duration in milliseconds
     * @return true if the session has been inactive for longer than the specified duration
     */
    public boolean isInactive(long maxInactiveDurationMs) {
        long inactiveDuration = Instant.now().toEpochMilli() - lastActivityAt.toEpochMilli();
        return inactiveDuration > maxInactiveDurationMs;
    }

    /**
     * Represents a single interaction within a reasoning session.
     */
    /* Extracted: org.openhab.core.ai.reasoning.SessionInteraction */
}
