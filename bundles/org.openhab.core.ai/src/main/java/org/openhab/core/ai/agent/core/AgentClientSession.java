package org.openhab.core.ai.agent.core;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.model.api.ModelProviderType;

/**
 * Tracks an agent's session with a specific provider/model.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class AgentClientSession {
    private final String agentId;
    private final ModelProviderType providerType;
    private final String modelName;
    private final @Nullable String sessionId;
    private final AtomicReference<Instant> lastUsed = new AtomicReference<>(Instant.now());
    private final AtomicReference<Boolean> active = new AtomicReference<>(true);

    public AgentClientSession(String agentId, ModelProviderType providerType, String modelName,
            @Nullable String sessionId) {
        this.agentId = agentId;
        this.providerType = providerType;
        this.modelName = modelName;
        this.sessionId = sessionId;
    }

    public void updateLastUsed(Instant time) {
        lastUsed.set(time);
    }

    public void markInactive() {
        active.set(false);
    }

    public String getAgentId() {
        return agentId;
    }

    public ModelProviderType getProviderType() {
        return providerType;
    }

    public String getModelName() {
        return modelName;
    }

    public @Nullable String getSessionId() {
        return sessionId;
    }

    public Instant getLastUsed() {
        return lastUsed.get();
    }

    public boolean isActive() {
        return active.get();
    }

    public Duration getSessionDuration() {
        return Duration.between(lastUsed.get(), Instant.now());
    }
}
