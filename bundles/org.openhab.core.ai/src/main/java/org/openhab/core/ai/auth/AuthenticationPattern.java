package org.openhab.core.ai.auth;

import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
class AuthenticationPattern {
    private final String principalId;
    private final String protocol;
    private final AtomicLong totalAttempts = new AtomicLong(0);
    private final AtomicLong failedAttempts = new AtomicLong(0);

    AuthenticationPattern(String principalId, String protocol) {
        this.principalId = principalId;
        this.protocol = protocol;
    }

    void recordAttempt(boolean success) {
        totalAttempts.incrementAndGet();
        if (!success) {
            failedAttempts.incrementAndGet();
        }
    }

    long getTotalAttempts() { return totalAttempts.get(); }

    double getFailureRate() {
        long total = totalAttempts.get();
        return total > 0 ? (double) failedAttempts.get() / total : 0.0;
    }
}


