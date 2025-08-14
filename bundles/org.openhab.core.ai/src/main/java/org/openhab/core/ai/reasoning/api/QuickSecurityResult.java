package org.openhab.core.ai.reasoning.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class QuickSecurityResult {
    private final boolean allowed;
    private final String reason;
    private final long checkTime;

    public QuickSecurityResult(boolean allowed, String reason, long checkTime) {
        this.allowed = allowed;
        this.reason = reason;
        this.checkTime = checkTime;
    }

    public boolean isAllowed() { return allowed; }
    public String getReason() { return reason; }
    public long getCheckTime() { return checkTime; }
}


