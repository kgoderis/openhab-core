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

    public ActionExecutionEvent(ActionExecutionEventBuilder builder) {
        this.eventId = builder.getEventId();
        this.actionId = builder.getActionId();
        this.agentId = builder.getAgentId();
        this.parameters = builder.getParameters();
        this.result = builder.getResult();
        this.error = builder.getError();
        this.status = builder.getStatus();
        this.startTime = builder.getStartTime();
        this.endTime = builder.getEndTime();
        this.executionTime = Duration.between(this.startTime, this.endTime);
        this.origin = builder.getOrigin();
        this.metadata = builder.getMetadata();
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

    // Inner Builder extracted; use top-level org.openhab.core.ai.action.ActionExecutionEventBuilder

    @Override
    public String toString() {
        return String.format("ActionExecutionEvent{eventId='%s', actionId='%s', agentId='%s', status=%s, duration=%s}",
                eventId, actionId, agentId, status, executionTime);
    }
}
