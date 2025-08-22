package org.openhab.core.ai.security.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class ServerSecurityConfiguration {
    public boolean isEnableAuthentication() {
        return true;
    }

    public boolean isEnableRequestValidation() {
        return true;
    }

    public int getMaxConnections() {
        return 100;
    }

    public int getRateLimitPerMinute() {
        return 1000;
    }
}
