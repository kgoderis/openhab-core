package org.openhab.core.ai.auth;

import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
class PermissionCheckPattern {
    private final String principalId;
    private final String permission;
    private final String protocol;
    private final AtomicLong totalChecks = new AtomicLong(0);
    private final AtomicLong deniedChecks = new AtomicLong(0);

    PermissionCheckPattern(String principalId, String permission, String protocol) {
        this.principalId = principalId;
        this.permission = permission;
        this.protocol = protocol;
    }

    void recordCheck(boolean granted) {
        totalChecks.incrementAndGet();
        if (!granted) { deniedChecks.incrementAndGet(); }
    }

    long getTotalChecks() { return totalChecks.get(); }

    double getDenialRate() {
        long total = totalChecks.get();
        return total > 0 ? (double) deniedChecks.get() / total : 0.0;
    }
}


