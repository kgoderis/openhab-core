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
package org.openhab.core.ai.model;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.agent.api.AgentModelContextBuilder;

/**
 * Represents the context information for agent-specific model interactions.
 * 
 * This class encapsulates all the contextual information that an agent provides
 * to the model integration service, including domain-specific knowledge, current
 * state, preferences, and constraints.
 * 
 * @author Karel Goderis - Initial Contribution
 */
@NonNullByDefault
public class AgentModelContext {

    private final String agentId;
    private final String agentType;
    private final Map<String, Object> contextData;
    private final Map<String, String> metadata;
    private final Instant timestamp;
    private final @Nullable String sessionId;
    private final int priority;

    // Constructor using inner Builder removed; use AgentModelContextBuilder instead

    /* package */ AgentModelContext(AgentModelContextBuilder builder) {
        this.agentId = Objects.requireNonNull(builder.agentId, "Agent ID cannot be null");
        this.agentType = Objects.requireNonNull(builder.agentType, "Agent type cannot be null");
        this.contextData = new HashMap<>(builder.contextData);
        this.metadata = new HashMap<>(builder.metadata);
        this.timestamp = builder.timestamp != null ? builder.timestamp : Instant.now();
        this.sessionId = builder.sessionId;
        this.priority = builder.priority;
    }

    /**
     * Gets the unique identifier of the agent.
     * 
     * @return the agent ID
     */
    public String getAgentId() {
        return agentId;
    }

    /**
     * Gets the type of the agent.
     * 
     * @return the agent type
     */
    public String getAgentType() {
        return agentType;
    }

    /**
     * Gets the context data map containing domain-specific information.
     * 
     * @return the context data map
     */
    public Map<String, Object> getContextData() {
        return new HashMap<>(contextData);
    }

    /**
     * Gets a specific context data value by key.
     * 
     * @param key the context data key
     * @return the context data value, or null if not found
     */
    @SuppressWarnings("unchecked")
    public @Nullable <T> T getContextData(String key) {
        return (T) contextData.get(key);
    }

    /**
     * Gets the metadata map containing additional context information.
     * 
     * @return the metadata map
     */
    public Map<String, String> getMetadata() {
        return new HashMap<>(metadata);
    }

    /**
     * Gets a specific metadata value by key.
     * 
     * @param key the metadata key
     * @return the metadata value, or null if not found
     */
    public @Nullable String getMetadata(String key) {
        return metadata.get(key);
    }

    /**
     * Gets the timestamp when this context was created.
     * 
     * @return the creation timestamp
     */
    public Instant getTimestamp() {
        return timestamp;
    }

    /**
     * Gets the session identifier if this context is part of a session.
     * 
     * @return the session ID, or null if not part of a session
     */
    public @Nullable String getSessionId() {
        return sessionId;
    }

    /**
     * Gets the priority level of this context.
     * 
     * @return the priority level
     */
    public int getPriority() {
        return priority;
    }

    /**
     * Creates a new context with additional context data.
     * 
     * @param key the context data key
     * @param value the context data value
     * @return a new context with the additional data
     */
    public AgentModelContext withContextData(String key, Object value) {
        return new AgentModelContextBuilder(this).contextData(key, value).build();
    }

    /**
     * Creates a new context with additional metadata.
     * 
     * @param key the metadata key
     * @param value the metadata value
     * @return a new context with the additional metadata
     */
    public AgentModelContext withMetadata(String key, String value) {
        return new AgentModelContextBuilder(this).metadata(key, value).build();
    }

    /**
     * Creates a new context with a session ID.
     * 
     * @param sessionId the session identifier
     * @return a new context with the session ID
     */
    public AgentModelContext withSessionId(String sessionId) {
        return new AgentModelContextBuilder(this).sessionId(sessionId).build();
    }

    /**
     * Creates a new context with a different priority.
     * 
     * @param priority the new priority level
     * @return a new context with the updated priority
     */
    public AgentModelContext withPriority(int priority) {
        return new AgentModelContextBuilder(this).priority(priority).build();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        AgentModelContext other = (AgentModelContext) obj;
        return Objects.equals(agentId, other.agentId) && Objects.equals(agentType, other.agentType)
                && Objects.equals(contextData, other.contextData) && Objects.equals(metadata, other.metadata)
                && Objects.equals(timestamp, other.timestamp) && Objects.equals(sessionId, other.sessionId)
                && priority == other.priority;
    }

    @Override
    public int hashCode() {
        return Objects.hash(agentId, agentType, contextData, metadata, timestamp, sessionId, priority);
    }

    @Override
    public String toString() {
        return "AgentModelContext [agentId=" + agentId + ", agentType=" + agentType + ", contextData=" + contextData
                + ", metadata=" + metadata + ", timestamp=" + timestamp + ", sessionId=" + sessionId + ", priority="
                + priority + "]";
    }

    // Builder extracted to top-level: see AgentModelContextBuilder
}
