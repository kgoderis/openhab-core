package org.openhab.core.ai.common.response;

import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.builder.AbstractBuilder;

/**
 * Builder for {@link AgentResponse} in the unified response hierarchy.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public final class AgentResponseBuilder extends AbstractBuilder<AgentResponse> {

    private @Nullable String id;
    private @Nullable Object data;
    private long timestamp = System.currentTimeMillis();
    private String agentId = "";
    private String action = "";
    private long processingTimeMs = 0L;
    private Map<String, Object> metadata = Map.of();
    private @Nullable String errorMessage;
    private String responseType = "SUCCESS";

    public AgentResponseBuilder withId(String id) {
        this.id = Objects.requireNonNull(id, "id");
        return this;
    }

    public AgentResponseBuilder withData(@Nullable Object data) {
        this.data = data;
        return this;
    }

    public AgentResponseBuilder withTimestamp(long timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public AgentResponseBuilder withAgentId(String agentId) {
        this.agentId = Objects.requireNonNull(agentId, "agentId");
        return this;
    }

    public AgentResponseBuilder withAction(String action) {
        this.action = Objects.requireNonNull(action, "action");
        return this;
    }

    public AgentResponseBuilder withProcessingTimeMs(long ms) {
        this.processingTimeMs = ms;
        return this;
    }

    public AgentResponseBuilder withMetadata(Map<String, Object> metadata) {
        this.metadata = Objects.requireNonNull(metadata, "metadata");
        return this;
    }

    public AgentResponseBuilder withErrorMessage(@Nullable String errorMessage) {
        this.errorMessage = errorMessage;
        return this;
    }

    public AgentResponseBuilder withResponseType(String responseType) {
        this.responseType = Objects.requireNonNull(responseType, "responseType");
        return this;
    }

    @Override
    protected void validate() {
        validateRequiredString(agentId, "agentId");
        validateRequiredString(action, "action");
        validateRequiredString(responseType, "responseType");
        if (processingTimeMs < 0) {
            addValidationError("processingTimeMs must be >= 0");
        }
    }

    @Override
    protected void doReset() {
        id = null;
        data = null;
        timestamp = System.currentTimeMillis();
        agentId = "";
        action = "";
        processingTimeMs = 0L;
        metadata = Map.of();
        errorMessage = null;
        responseType = "SUCCESS";
    }

    @Override
    public AgentResponse build() {
        if (!isValid()) {
            throw new IllegalArgumentException("Invalid AgentResponseBuilder state: " + getValidationErrors());
        }
        String resolvedId = id != null ? id
                : ("agent-response-" + System.currentTimeMillis() + "-" + System.nanoTime());
        return new AgentResponse(resolvedId, data, timestamp, agentId, action, processingTimeMs, metadata, errorMessage,
                responseType);
    }
}
