package org.openhab.core.ai.security.config;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.builder.AbstractBuilder;

/**
 * Builder for SecurityConfiguration.
 * 
 * This builder provides a fluent API for creating SecurityConfiguration instances
 * with proper validation and default values.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public final class SecurityConfigurationBuilder extends AbstractBuilder<SecurityConfiguration> {
    String configId = "security";
    @Nullable
    Duration keyRotationInterval;
    @Nullable
    Integer maxAuthenticationFailures;
    @Nullable
    Duration lockoutDuration;
    @Nullable
    Boolean enableAuditLogging;
    @Nullable
    Boolean enableIncidentDetection;
    @Nullable
    Boolean enableEncryption;
    @Nullable
    String encryptionAlgorithm;
    @Nullable
    Integer sessionTimeoutMinutes;
    @Nullable
    Boolean enableMultiFactorAuth;
    @Nullable
    String defaultAuthMethod;
    Map<String, Object> customSettings = new HashMap<>();

    public SecurityConfigurationBuilder() {
        // Default constructor
    }

    public SecurityConfigurationBuilder(SecurityConfiguration source) {
        this.configId = source.getId();
        this.keyRotationInterval = source.getKeyRotationInterval();
        this.maxAuthenticationFailures = source.getMaxAuthenticationFailures();
        this.lockoutDuration = source.getLockoutDuration();
        this.enableAuditLogging = source.isEnableAuditLogging();
        this.enableIncidentDetection = source.isEnableIncidentDetection();
        this.enableEncryption = source.isEnableEncryption();
        this.encryptionAlgorithm = source.getEncryptionAlgorithm();
        this.sessionTimeoutMinutes = source.getSessionTimeoutMinutes();
        this.enableMultiFactorAuth = source.isEnableMultiFactorAuth();
        this.defaultAuthMethod = source.getDefaultAuthMethod();
        this.customSettings = new HashMap<>(source.getCustomOptions());
    }

    public SecurityConfigurationBuilder withConfigId(String configId) {
        this.configId = Objects.requireNonNull(configId, "configId");
        return this;
    }

    public SecurityConfigurationBuilder withKeyRotationInterval(@Nullable Duration keyRotationInterval) {
        this.keyRotationInterval = keyRotationInterval;
        return this;
    }

    public SecurityConfigurationBuilder withMaxAuthenticationFailures(@Nullable Integer maxAuthenticationFailures) {
        this.maxAuthenticationFailures = maxAuthenticationFailures;
        return this;
    }

    public SecurityConfigurationBuilder withLockoutDuration(@Nullable Duration lockoutDuration) {
        this.lockoutDuration = lockoutDuration;
        return this;
    }

    public SecurityConfigurationBuilder withEnableAuditLogging(@Nullable Boolean enableAuditLogging) {
        this.enableAuditLogging = enableAuditLogging;
        return this;
    }

    public SecurityConfigurationBuilder withEnableIncidentDetection(@Nullable Boolean enableIncidentDetection) {
        this.enableIncidentDetection = enableIncidentDetection;
        return this;
    }

    public SecurityConfigurationBuilder withEnableEncryption(@Nullable Boolean enableEncryption) {
        this.enableEncryption = enableEncryption;
        return this;
    }

    public SecurityConfigurationBuilder withEncryptionAlgorithm(@Nullable String encryptionAlgorithm) {
        this.encryptionAlgorithm = encryptionAlgorithm;
        return this;
    }

    public SecurityConfigurationBuilder withSessionTimeoutMinutes(@Nullable Integer sessionTimeoutMinutes) {
        this.sessionTimeoutMinutes = sessionTimeoutMinutes;
        return this;
    }

    public SecurityConfigurationBuilder withEnableMultiFactorAuth(@Nullable Boolean enableMultiFactorAuth) {
        this.enableMultiFactorAuth = enableMultiFactorAuth;
        return this;
    }

    public SecurityConfigurationBuilder withDefaultAuthMethod(@Nullable String defaultAuthMethod) {
        this.defaultAuthMethod = defaultAuthMethod;
        return this;
    }

    public SecurityConfigurationBuilder withCustomSetting(String key, Object value) {
        this.customSettings.put(Objects.requireNonNull(key, "key"), value);
        return this;
    }

    public SecurityConfigurationBuilder withCustomSettings(Map<String, Object> customSettings) {
        this.customSettings.putAll(Objects.requireNonNull(customSettings, "customSettings"));
        return this;
    }

    @Override
    protected void validate() {
        if (configId.isBlank()) {
            addValidationError("configId must not be blank");
        }
        if (keyRotationInterval != null && keyRotationInterval.isNegative()) {
            addValidationError("keyRotationInterval must be non-negative");
        }
        if (maxAuthenticationFailures != null && maxAuthenticationFailures < 0) {
            addValidationError("maxAuthenticationFailures must be non-negative");
        }
        if (lockoutDuration != null && lockoutDuration.isNegative()) {
            addValidationError("lockoutDuration must be non-negative");
        }
        if (sessionTimeoutMinutes != null && sessionTimeoutMinutes < 0) {
            addValidationError("sessionTimeoutMinutes must be non-negative");
        }
        if (encryptionAlgorithm != null && encryptionAlgorithm.isBlank()) {
            addValidationError("encryptionAlgorithm must not be blank");
        }
        if (defaultAuthMethod != null && defaultAuthMethod.isBlank()) {
            addValidationError("defaultAuthMethod must not be blank");
        }
    }

    @Override
    protected void doReset() {
        this.configId = "security";
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
        this.customSettings.clear();
    }

    @Override
    public SecurityConfiguration build() {
        validate();
        return new SecurityConfiguration(this);
    }
}
