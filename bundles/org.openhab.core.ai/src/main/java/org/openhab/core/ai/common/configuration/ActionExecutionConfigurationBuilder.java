package org.openhab.core.ai.common.configuration;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.builder.AbstractBuilder;

/**
 * Builder for ActionExecutionConfiguration.
 * 
 * This builder provides a fluent API for creating ActionExecutionConfiguration instances
 * with proper validation and default values.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public final class ActionExecutionConfigurationBuilder extends AbstractBuilder<ActionExecutionConfiguration> {
    String configId = "action-execution";
    @Nullable
    Integer maxRetryAttempts;
    @Nullable
    Duration retryDelay;
    @Nullable
    Boolean enableCaching;
    @Nullable
    Boolean enableSecurityValidation;
    @Nullable
    Duration cacheExpiration;
    @Nullable
    Duration executionTimeout;
    @Nullable
    Boolean enableAsyncExecution;
    @Nullable
    Integer maxConcurrentExecutions;
    Map<String, Object> customSettings = new HashMap<>();

    public ActionExecutionConfigurationBuilder() {
        // Default constructor
    }

    public ActionExecutionConfigurationBuilder(ActionExecutionConfiguration source) {
        this.configId = source.getId();
        this.maxRetryAttempts = source.getMaxRetryAttempts();
        this.retryDelay = source.getRetryDelay();
        this.enableCaching = source.isEnableCaching();
        this.enableSecurityValidation = source.isEnableSecurityValidation();
        this.cacheExpiration = source.getCacheExpiration();
        this.executionTimeout = source.getExecutionTimeout();
        this.enableAsyncExecution = source.isEnableAsyncExecution();
        this.maxConcurrentExecutions = source.getMaxConcurrentExecutions();
        this.customSettings = new HashMap<>(source.getCustomOptions());
    }

    public ActionExecutionConfigurationBuilder withConfigId(String configId) {
        this.configId = Objects.requireNonNull(configId, "configId");
        return this;
    }

    public ActionExecutionConfigurationBuilder withMaxRetryAttempts(@Nullable Integer maxRetryAttempts) {
        this.maxRetryAttempts = maxRetryAttempts;
        return this;
    }

    public ActionExecutionConfigurationBuilder withRetryDelay(@Nullable Duration retryDelay) {
        this.retryDelay = retryDelay;
        return this;
    }

    public ActionExecutionConfigurationBuilder withEnableCaching(@Nullable Boolean enableCaching) {
        this.enableCaching = enableCaching;
        return this;
    }

    public ActionExecutionConfigurationBuilder withEnableSecurityValidation(
            @Nullable Boolean enableSecurityValidation) {
        this.enableSecurityValidation = enableSecurityValidation;
        return this;
    }

    public ActionExecutionConfigurationBuilder withCacheExpiration(@Nullable Duration cacheExpiration) {
        this.cacheExpiration = cacheExpiration;
        return this;
    }

    public ActionExecutionConfigurationBuilder withExecutionTimeout(@Nullable Duration executionTimeout) {
        this.executionTimeout = executionTimeout;
        return this;
    }

    public ActionExecutionConfigurationBuilder withEnableAsyncExecution(@Nullable Boolean enableAsyncExecution) {
        this.enableAsyncExecution = enableAsyncExecution;
        return this;
    }

    public ActionExecutionConfigurationBuilder withMaxConcurrentExecutions(@Nullable Integer maxConcurrentExecutions) {
        this.maxConcurrentExecutions = maxConcurrentExecutions;
        return this;
    }

    public ActionExecutionConfigurationBuilder withCustomSetting(String key, Object value) {
        this.customSettings.put(Objects.requireNonNull(key, "key"), value);
        return this;
    }

    public ActionExecutionConfigurationBuilder withCustomSettings(Map<String, Object> customSettings) {
        this.customSettings.putAll(Objects.requireNonNull(customSettings, "customSettings"));
        return this;
    }

    @Override
    protected void validate() {
        if (configId.isBlank()) {
            addValidationError("configId must not be blank");
        }
        if (maxRetryAttempts != null && maxRetryAttempts < 0) {
            addValidationError("maxRetryAttempts must be non-negative");
        }
        if (retryDelay != null && retryDelay.isNegative()) {
            addValidationError("retryDelay must be non-negative");
        }
        if (cacheExpiration != null && cacheExpiration.isNegative()) {
            addValidationError("cacheExpiration must be non-negative");
        }
        if (executionTimeout != null && executionTimeout.isNegative()) {
            addValidationError("executionTimeout must be non-negative");
        }
        if (maxConcurrentExecutions != null && maxConcurrentExecutions <= 0) {
            addValidationError("maxConcurrentExecutions must be positive");
        }
    }

    @Override
    protected void doReset() {
        this.configId = "action-execution";
        this.maxRetryAttempts = null;
        this.retryDelay = null;
        this.enableCaching = null;
        this.enableSecurityValidation = null;
        this.cacheExpiration = null;
        this.executionTimeout = null;
        this.enableAsyncExecution = null;
        this.maxConcurrentExecutions = null;
        this.customSettings.clear();
    }

    @Override
    public ActionExecutionConfiguration build() {
        validate();
        return new ActionExecutionConfiguration(this);
    }
}
