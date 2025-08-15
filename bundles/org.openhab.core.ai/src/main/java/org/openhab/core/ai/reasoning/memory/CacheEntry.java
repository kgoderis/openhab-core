package org.openhab.core.ai.reasoning.memory;

import java.time.Duration;
import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Cache entry wrapper with expiration and last-access tracking.
 * Used by {@link AgentModelContextCache}.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
final class CacheEntry {
    private final org.openhab.core.ai.agent.api.AgentModelContext context;
    private final Instant expirationTime;
    private volatile Instant lastAccess;

    CacheEntry(org.openhab.core.ai.agent.api.AgentModelContext context, Duration expiration) {
        this.context = context;
        this.expirationTime = Instant.now().plus(expiration);
        this.lastAccess = Instant.now();
    }

    org.openhab.core.ai.agent.api.AgentModelContext getContext() {
        return context;
    }

    Instant getExpirationTime() {
        return expirationTime;
    }

    Instant getLastAccess() {
        return lastAccess;
    }

    void updateLastAccess() {
        this.lastAccess = Instant.now();
    }

    boolean isExpired() {
        return Instant.now().isAfter(expirationTime);
    }
}
