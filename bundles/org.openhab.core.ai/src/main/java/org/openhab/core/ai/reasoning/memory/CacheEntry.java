package org.openhab.core.ai.reasoning.memory;

import java.time.Duration;
import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.agent.api.AgentModelContext;

/**
 * Cache entry wrapper with expiration and last-access tracking.
 * Used by {@link AgentModelContextCache}.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public final class CacheEntry {
    private final AgentModelContext context;
    private final Instant expirationTime;
    private volatile Instant lastAccess;

    public CacheEntry(AgentModelContext context, Duration expiration) {
        this.context = context;
        this.expirationTime = Instant.now().plus(expiration);
        this.lastAccess = Instant.now();
    }

    public AgentModelContext getContext() {
        return context;
    }

    public Instant getExpirationTime() {
        return expirationTime;
    }

    public Instant getLastAccess() {
        return lastAccess;
    }

    public void updateLastAccess() {
        this.lastAccess = Instant.now();
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expirationTime);
    }
}
