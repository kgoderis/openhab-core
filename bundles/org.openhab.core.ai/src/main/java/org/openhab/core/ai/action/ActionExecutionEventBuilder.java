package org.openhab.core.ai.action;

import java.time.Instant;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.model.ActionExecutionEventStatus;

@NonNullByDefault
public class ActionExecutionEventBuilder {
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

    public ActionExecutionEventBuilder eventId(String eventId) {
        this.eventId = eventId;
        return this;
    }

    public ActionExecutionEventBuilder actionId(String actionId) {
        this.actionId = actionId;
        return this;
    }

    public ActionExecutionEventBuilder agentId(String agentId) {
        this.agentId = agentId;
        return this;
    }

    public ActionExecutionEventBuilder parameters(Map<String, Object> parameters) {
        this.parameters = parameters;
        return this;
    }

    public ActionExecutionEventBuilder result(@Nullable Object result) {
        this.result = result;
        return this;
    }

    public ActionExecutionEventBuilder error(@Nullable String error) {
        this.error = error;
        return this;
    }

    public ActionExecutionEventBuilder status(ActionExecutionEventStatus status) {
        this.status = status;
        return this;
    }

    public ActionExecutionEventBuilder startTime(Instant startTime) {
        this.startTime = startTime;
        return this;
    }

    public ActionExecutionEventBuilder endTime(Instant endTime) {
        this.endTime = endTime;
        return this;
    }

    public ActionExecutionEventBuilder origin(String origin) {
        this.origin = origin;
        return this;
    }

    public ActionExecutionEventBuilder metadata(Map<String, Object> metadata) {
        this.metadata = metadata;
        return this;
    }

    public ActionExecutionEvent build() {
        return new ActionExecutionEvent(this);
    }

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

    public String getOrigin() {
        return origin;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }
}
