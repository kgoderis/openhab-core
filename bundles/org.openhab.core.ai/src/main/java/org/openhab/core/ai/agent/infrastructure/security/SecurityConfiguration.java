package org.openhab.core.ai.agent.infrastructure.security;

import java.time.Duration;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Security configuration for agent security management
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class SecurityConfiguration {
    private Duration keyRotationInterval = Duration.ofDays(30);
    private int maxAuthenticationFailures = 5;
    private Duration lockoutDuration = Duration.ofMinutes(15);
    private boolean enableAuditLogging = true;
    private boolean enableIncidentDetection = true;

    // Getters and setters
    public Duration getKeyRotationInterval() {
        return keyRotationInterval;
    }

    public void setKeyRotationInterval(Duration keyRotationInterval) {
        this.keyRotationInterval = keyRotationInterval;
    }

    public int getMaxAuthenticationFailures() {
        return maxAuthenticationFailures;
    }

    public void setMaxAuthenticationFailures(int maxAuthenticationFailures) {
        this.maxAuthenticationFailures = maxAuthenticationFailures;
    }

    public Duration getLockoutDuration() {
        return lockoutDuration;
    }

    public void setLockoutDuration(Duration lockoutDuration) {
        this.lockoutDuration = lockoutDuration;
    }

    public boolean isEnableAuditLogging() {
        return enableAuditLogging;
    }

    public void setEnableAuditLogging(boolean enableAuditLogging) {
        this.enableAuditLogging = enableAuditLogging;
    }

    public boolean isEnableIncidentDetection() {
        return enableIncidentDetection;
    }

    public void setEnableIncidentDetection(boolean enableIncidentDetection) {
        this.enableIncidentDetection = enableIncidentDetection;
    }
}
