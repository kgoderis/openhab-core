package org.openhab.core.ai.auth;

import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class SecurityViolationPattern {
    private final String principalId;
    private final String violationType;
    private final String protocol;
    private final AtomicLong violationCount = new AtomicLong(0);

    SecurityViolationPattern(String principalId, String violationType, String protocol) {
        this.principalId = principalId;
        this.violationType = violationType;
        this.protocol = protocol;
    }

    void recordViolation() {
        violationCount.incrementAndGet();
    }

    long getViolationCount() {
        return violationCount.get();
    }
}
