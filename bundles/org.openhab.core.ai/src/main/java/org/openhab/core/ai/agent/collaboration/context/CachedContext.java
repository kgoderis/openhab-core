package org.openhab.core.ai.agent.collaboration.context;

import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.agent.collaboration.ContextVersion;
import org.openhab.core.ai.agent.collaboration.SharedContext;

/**
 * Cached context entry with expiry tracking.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class CachedContext {

    private final SharedContext context;
    private final ContextVersion version;
    private final Instant expiryTime;

    public CachedContext(SharedContext context, ContextVersion version, Instant expiryTime) {
        this.context = context;
        this.version = version;
        this.expiryTime = expiryTime;
    }

    public SharedContext getContext() {
        return context;
    }

    public ContextVersion getVersion() {
        return version;
    }

    public Instant getExpiryTime() {
        return expiryTime;
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiryTime);
    }
}
