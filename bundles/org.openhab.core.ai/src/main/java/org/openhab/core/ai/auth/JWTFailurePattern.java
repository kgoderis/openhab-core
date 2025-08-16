package org.openhab.core.ai.auth;

import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class JWTFailurePattern {
    private final String tokenPrefix;
    private final String reason;
    private final AtomicLong failureCount = new AtomicLong(0);

    JWTFailurePattern(String tokenPrefix, String reason) {
        this.tokenPrefix = tokenPrefix;
        this.reason = reason;
    }

    void recordFailure() {
        failureCount.incrementAndGet();
    }

    long getFailureCount() {
        return failureCount.get();
    }
}
