package org.openhab.core.ai.auth;

import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class LogoutPattern {
    private final String principalId;
    private final AtomicLong logoutCount = new AtomicLong(0);

    public LogoutPattern(String principalId) {
        this.principalId = principalId;
    }

    void recordLogout() {
        logoutCount.incrementAndGet();
    }

    long getLogoutCount() {
        return logoutCount.get();
    }
}
