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
package org.openhab.core.ai.action;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.model.ActionExecutionEventStatus;

/**
 * Event representing an action execution.
 * 
 * This class tracks individual action execution events including
 * timing, parameters, results, and metadata.
 * 
 * @author Karel Goderis - Initial Contribution
 */
@NonNullByDefault
public class ActionExecutionEvent {

    private final String eventId;
    private final String actionId;
    private final String agentId;
    private final Map<String, Object> parameters;
    private final @Nullable Object result;
    private final @Nullable String error;
    private final ActionExecutionEventStatus status;
    private final Instant startTime;
    private final Instant endTime;
    private final Duration executionTime;
    private final String origin;
    private final Map<String, Object> metadata;

    private ActionExecutionEvent(Builder builder) {
        this.eventId = builder.eventId;
        this.actionId = builder.actionId;
        this.agentId = builder.agentId;
        this.parameters = builder.parameters;
        this.result = builder.result;
        this.error = builder.error;
        this.status = builder.status;
        this.startTime = builder.startTime;
        this.endTime = builder.endTime;
        this.executionTime = Duration.between(builder.startTime, builder.endTime);
        this.origin = builder.origin;
        this.metadata = builder.metadata;
    }

    // Getters
    public String getEventId() {
        return eventId;
    }

    public String getActionId() {
        return actionId;
    }

    public String getAgentId() {
        return agentId;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public @Nullable Object getResult() {
        return result;
    }

    public @Nullable String getError() {
        return error;
    }

    public ActionExecutionEventStatus getStatus() {
        return status;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public Instant getEndTime() {
        return endTime;
    }

    public Duration getExecutionTime() {
        return executionTime;
    }

    public String getOrigin() {
        return origin;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    /**
     * Check if the execution was successful.
     * 
     * @return true if successful, false otherwise
     */
    public boolean isSuccessful() {
        return status == ActionExecutionEventStatus.SUCCESS;
    }

    /**
     * Check if the execution failed.
     * 
     * @return true if failed, false otherwise
     */
    public boolean isFailed() {
        return status == ActionExecutionEventStatus.FAILED;
    }

    /**
     * Check if the execution timed out.
     * 
     * @return true if timed out, false otherwise
     */
    public boolean isTimedOut() {
        return status == ActionExecutionEventStatus.TIMEOUT;
    }

    /**
     * Execution status for action events.
     */
    // ExecutionStatus extracted to top-level: org.openhab.core.ai.model.ActionExecutionEventStatus

    /**
     * Builder for ActionExecutionEvent.
     */
    public static class Builder {
        private String eventId = "";
        private String actionId = "";
        private String agentId = "";
        private Map<String, Object> parameters = Map.of();
        private @Nullable Object result = null;
        private @Nullable String error = null;
        private ActionExecutionEventStatus status = ActionExecutionEventStatus.PENDING;
        private Instant startTime = Instant.now();
        private Instant endTime = Instant.now();
        private String origin = "unknown";
        private Map<String, Object> metadata = Map.of();

        public Builder eventId(String eventId) {
            this.eventId = eventId;
            return this;
        }

        public Builder actionId(String actionId) {
            this.actionId = actionId;
            return this;
        }

        public Builder agentId(String agentId) {
            this.agentId = agentId;
            return this;
        }

        public Builder parameters(Map<String, Object> parameters) {
            this.parameters = parameters;
            return this;
        }

        public Builder result(@Nullable Object result) {
            this.result = result;
            return this;
        }

        public Builder error(@Nullable String error) {
            this.error = error;
            return this;
        }

        public Builder status(ActionExecutionEventStatus status) {
            this.status = status;
            return this;
        }

        public Builder startTime(Instant startTime) {
            this.startTime = startTime;
            return this;
        }

        public Builder endTime(Instant endTime) {
            this.endTime = endTime;
            return this;
        }

        public Builder origin(String origin) {
            this.origin = origin;
            return this;
        }

        public Builder metadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return this;
        }

        public ActionExecutionEvent build() {
            return new ActionExecutionEvent(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String toString() {
        return String.format("ActionExecutionEvent{eventId='%s', actionId='%s', agentId='%s', status=%s, duration=%s}",
                eventId, actionId, agentId, status, executionTime);
    }
}
