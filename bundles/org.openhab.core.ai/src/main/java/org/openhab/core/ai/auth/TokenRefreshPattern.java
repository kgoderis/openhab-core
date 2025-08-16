package org.openhab.core.ai.auth;

import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class TokenRefreshPattern {
    private final String principalId;
    private final AtomicLong refreshCount = new AtomicLong(0);

    TokenRefreshPattern(String principalId) {
        this.principalId = principalId;
    }

    void recordRefresh() {
        refreshCount.incrementAndGet();
    }

    long getRefreshCount() {
        return refreshCount.get();
    }
}
