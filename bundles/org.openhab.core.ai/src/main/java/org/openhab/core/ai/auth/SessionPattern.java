package org.openhab.core.ai.auth;

import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
class SessionPattern {
    private final String principalId;
    private final String sessionId;
    private final AtomicLong creationCount = new AtomicLong(0);
    private final AtomicLong timeoutCount = new AtomicLong(0);

    SessionPattern(String principalId, String sessionId) {
        this.principalId = principalId;
        this.sessionId = sessionId;
    }

    void recordCreation() {
        creationCount.incrementAndGet();
    }

    void recordTimeout() {
        timeoutCount.incrementAndGet();
    }

    long getCreationCount() {
        return creationCount.get();
    }

    long getTimeoutCount() {
        return timeoutCount.get();
    }
}
