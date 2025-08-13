package org.openhab.core.ai.agent.collaboration.coordination;

import java.time.Instant;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.agent.collaboration.coordination.api.CoordinationProtocol;
import org.openhab.core.ai.agent.collaboration.coordination.api.CoordinationResult;

/**
 * Coordination session data extracted from {@link AgentCoordinationManager}.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class CoordinationSession {
    private final String sessionId;
    private final List<String> agentIds;
    private final CoordinationProtocol protocol;
    private final Instant startTime;
    private CoordinationState state;
    private @Nullable CoordinationResult result;
    private @Nullable String error;

    private CoordinationSession(Builder builder) {
        this.sessionId = builder.sessionId;
        this.agentIds = builder.agentIds;
        this.protocol = builder.protocol;
        this.startTime = builder.startTime;
        this.state = builder.state;
    }

    public String getSessionId() {
        return sessionId;
    }

    public List<String> getAgentIds() {
        return agentIds;
    }

    public CoordinationProtocol getProtocol() {
        return protocol;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public CoordinationState getState() {
        return state;
    }

    public void setState(CoordinationState state) {
        this.state = state;
    }

    public @Nullable CoordinationResult getResult() {
        return result;
    }

    public void setResult(CoordinationResult result) {
        this.result = result;
    }

    public @Nullable String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String sessionId;
        private List<String> agentIds;
        private CoordinationProtocol protocol;
        private Instant startTime;
        private CoordinationState state;

        public Builder sessionId(String sessionId) {
            this.sessionId = sessionId;
            return this;
        }

        public Builder agentIds(List<String> agentIds) {
            this.agentIds = agentIds;
            return this;
        }

        public Builder protocol(CoordinationProtocol protocol) {
            this.protocol = protocol;
            return this;
        }

        public Builder startTime(Instant startTime) {
            this.startTime = startTime;
            return this;
        }

        public Builder state(CoordinationState state) {
            this.state = state;
            return this;
        }

        public CoordinationSession build() {
            return new CoordinationSession(this);
        }
    }
}


