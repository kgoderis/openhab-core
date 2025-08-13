package org.openhab.core.ai.agent.collaboration.coordination;

import java.time.Instant;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.agent.collaboration.coordination.api.CoordinationProtocol;

/**
 * Builder for {@link CoordinationSession}.
 */
@NonNullByDefault
public class CoordinationSessionBuilder {
    private String sessionId;
    private List<String> agentIds;
    private CoordinationProtocol protocol;
    private Instant startTime;
    private CoordinationState state;

    public CoordinationSessionBuilder sessionId(String sessionId) { this.sessionId = sessionId; return this; }
    public CoordinationSessionBuilder agentIds(List<String> agentIds) { this.agentIds = agentIds; return this; }
    public CoordinationSessionBuilder protocol(CoordinationProtocol protocol) { this.protocol = protocol; return this; }
    public CoordinationSessionBuilder startTime(Instant startTime) { this.startTime = startTime; return this; }
    public CoordinationSessionBuilder state(CoordinationState state) { this.state = state; return this; }

    public CoordinationSession build() {
        return CoordinationSession.builder()
                .sessionId(sessionId)
                .agentIds(agentIds)
                .protocol(protocol)
                .startTime(startTime)
                .state(state)
                .build();
    }
}


