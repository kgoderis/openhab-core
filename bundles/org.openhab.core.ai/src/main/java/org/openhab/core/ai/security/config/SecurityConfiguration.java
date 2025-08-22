package org.openhab.core.ai.security.config;

import java.time.Duration;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.config.common.BaseConfiguration;

/**
 * Unified configuration for security management.
 * 
 * This class provides configuration settings for security features including
 * authentication, authorization, audit logging, and incident detection.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class SecurityConfiguration extends BaseConfiguration {
    private final @Nullable Duration keyRotationInterval;
    private final @Nullable Integer maxAuthenticationFailures;
    private final @Nullable Duration lockoutDuration;
    private final @Nullable Boolean enableAuditLogging;
    private final @Nullable Boolean enableIncidentDetection;
    private final @Nullable Boolean enableEncryption;
    private final @Nullable String encryptionAlgorithm;
    private final @Nullable Integer sessionTimeoutMinutes;
    private final @Nullable Boolean enableMultiFactorAuth;
    private final @Nullable String defaultAuthMethod;

    /* package */ SecurityConfiguration(SecurityConfigurationBuilder builder) {
        super(builder.configId, true, "Security Configuration", "1.0.0", builder.customSettings);
        this.keyRotationInterval = builder.keyRotationInterval;
        this.maxAuthenticationFailures = builder.maxAuthenticationFailures;
        this.lockoutDuration = builder.lockoutDuration;
        this.enableAuditLogging = builder.enableAuditLogging;
        this.enableIncidentDetection = builder.enableIncidentDetection;
        this.enableEncryption = builder.enableEncryption;
        this.encryptionAlgorithm = builder.encryptionAlgorithm;
        this.sessionTimeoutMinutes = builder.sessionTimeoutMinutes;
        this.enableMultiFactorAuth = builder.enableMultiFactorAuth;
        this.defaultAuthMethod = builder.defaultAuthMethod;
    }

    /**
     * Default constructor for SecurityConfiguration.
     * Creates a basic security configuration with default values.
     */
    public SecurityConfiguration() {
        super("default-security", true, "Security Configuration", "1.0.0", null);
        this.keyRotationInterval = null;
        this.maxAuthenticationFailures = null;
        this.lockoutDuration = null;
        this.enableAuditLogging = null;
        this.enableIncidentDetection = null;
        this.enableEncryption = null;
        this.encryptionAlgorithm = null;
        this.sessionTimeoutMinutes = null;
        this.enableMultiFactorAuth = null;
        this.defaultAuthMethod = null;
    }

    public static SecurityConfigurationBuilder builder() {
        return new SecurityConfigurationBuilder();
    }

    /**
     * Get the key rotation interval.
     * 
     * @return key rotation interval, or null if not configured
     */
    public @Nullable Duration getKeyRotationInterval() {
        return keyRotationInterval;
    }

    /**
     * Get the maximum number of authentication failures before lockout.
     * 
     * @return maximum authentication failures, or null if not configured
     */
    public @Nullable Integer getMaxAuthenticationFailures() {
        return maxAuthenticationFailures;
    }

    /**
     * Get the lockout duration after authentication failures.
     * 
     * @return lockout duration, or null if not configured
     */
    public @Nullable Duration getLockoutDuration() {
        return lockoutDuration;
    }

    /**
     * Check if audit logging is enabled.
     * 
     * @return true if audit logging is enabled, false if disabled, null if not configured
     */
    public @Nullable Boolean isEnableAuditLogging() {
        return enableAuditLogging;
    }

    /**
     * Check if incident detection is enabled.
     * 
     * @return true if incident detection is enabled, false if disabled, null if not configured
     */
    public @Nullable Boolean isEnableIncidentDetection() {
        return enableIncidentDetection;
    }

    /**
     * Check if encryption is enabled.
     * 
     * @return true if encryption is enabled, false if disabled, null if not configured
     */
    public @Nullable Boolean isEnableEncryption() {
        return enableEncryption;
    }

    /**
     * Get the encryption algorithm.
     * 
     * @return encryption algorithm, or null if not configured
     */
    public @Nullable String getEncryptionAlgorithm() {
        return encryptionAlgorithm;
    }

    /**
     * Get the session timeout in minutes.
     * 
     * @return session timeout in minutes, or null if not configured
     */
    public @Nullable Integer getSessionTimeoutMinutes() {
        return sessionTimeoutMinutes;
    }

    /**
     * Check if multi-factor authentication is enabled.
     * 
     * @return true if multi-factor auth is enabled, false if disabled, null if not configured
     */
    public @Nullable Boolean isEnableMultiFactorAuth() {
        return enableMultiFactorAuth;
    }

    /**
     * Get the default authentication method.
     * 
     * @return default authentication method, or null if not configured
     */
    public @Nullable String getDefaultAuthMethod() {
        return defaultAuthMethod;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        if (!super.equals(obj)) {
            return false;
        }
        SecurityConfiguration other = (SecurityConfiguration) obj;
        return Objects.equals(keyRotationInterval, other.keyRotationInterval)
                && Objects.equals(maxAuthenticationFailures, other.maxAuthenticationFailures)
                && Objects.equals(lockoutDuration, other.lockoutDuration)
                && Objects.equals(enableAuditLogging, other.enableAuditLogging)
                && Objects.equals(enableIncidentDetection, other.enableIncidentDetection)
                && Objects.equals(enableEncryption, other.enableEncryption)
                && Objects.equals(encryptionAlgorithm, other.encryptionAlgorithm)
                && Objects.equals(sessionTimeoutMinutes, other.sessionTimeoutMinutes)
                && Objects.equals(enableMultiFactorAuth, other.enableMultiFactorAuth)
                && Objects.equals(defaultAuthMethod, other.defaultAuthMethod);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), keyRotationInterval, maxAuthenticationFailures, lockoutDuration,
                enableAuditLogging, enableIncidentDetection, enableEncryption, encryptionAlgorithm,
                sessionTimeoutMinutes, enableMultiFactorAuth, defaultAuthMethod);
    }

    @Override
    public String toString() {
        return "SecurityConfiguration{" + "keyRotationInterval=" + keyRotationInterval + ", maxAuthenticationFailures="
                + maxAuthenticationFailures + ", lockoutDuration=" + lockoutDuration + ", enableAuditLogging="
                + enableAuditLogging + ", enableIncidentDetection=" + enableIncidentDetection + ", enableEncryption="
                + enableEncryption + ", encryptionAlgorithm='" + encryptionAlgorithm + '\'' + ", sessionTimeoutMinutes="
                + sessionTimeoutMinutes + ", enableMultiFactorAuth=" + enableMultiFactorAuth + ", defaultAuthMethod='"
                + defaultAuthMethod + '\'' + '}';
    }
}
