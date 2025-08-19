package org.openhab.core.ai.common.configuration;

import java.time.Duration;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Unified configuration for action execution behavior.
 * 
 * This class provides configuration settings for action execution including
 * retry policies, caching behavior, security validation, and performance tuning.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ActionExecutionConfiguration extends BaseConfiguration {
    private final @Nullable Integer maxRetryAttempts;
    private final @Nullable Duration retryDelay;
    private final @Nullable Boolean enableCaching;
    private final @Nullable Boolean enableSecurityValidation;
    private final @Nullable Duration cacheExpiration;
    private final @Nullable Duration executionTimeout;
    private final @Nullable Boolean enableAsyncExecution;
    private final @Nullable Integer maxConcurrentExecutions;

    /* package */ ActionExecutionConfiguration(ActionExecutionConfigurationBuilder builder) {
        super(builder.configId, true, "Action Execution Configuration", "1.0.0", builder.customSettings);
        this.maxRetryAttempts = builder.maxRetryAttempts;
        this.retryDelay = builder.retryDelay;
        this.enableCaching = builder.enableCaching;
        this.enableSecurityValidation = builder.enableSecurityValidation;
        this.cacheExpiration = builder.cacheExpiration;
        this.executionTimeout = builder.executionTimeout;
        this.enableAsyncExecution = builder.enableAsyncExecution;
        this.maxConcurrentExecutions = builder.maxConcurrentExecutions;
    }

    public static ActionExecutionConfigurationBuilder builder() {
        return new ActionExecutionConfigurationBuilder();
    }

    /**
     * Get the maximum number of retry attempts.
     * 
     * @return maximum retry attempts, or null if not configured
     */
    public @Nullable Integer getMaxRetryAttempts() {
        return maxRetryAttempts;
    }

    /**
     * Get the delay between retry attempts.
     * 
     * @return retry delay, or null if not configured
     */
    public @Nullable Duration getRetryDelay() {
        return retryDelay;
    }

    /**
     * Check if caching is enabled.
     * 
     * @return true if caching is enabled, false if disabled, null if not configured
     */
    public @Nullable Boolean isEnableCaching() {
        return enableCaching;
    }

    /**
     * Check if security validation is enabled.
     * 
     * @return true if security validation is enabled, false if disabled, null if not configured
     */
    public @Nullable Boolean isEnableSecurityValidation() {
        return enableSecurityValidation;
    }

    /**
     * Get the cache expiration duration.
     * 
     * @return cache expiration duration, or null if not configured
     */
    public @Nullable Duration getCacheExpiration() {
        return cacheExpiration;
    }

    /**
     * Get the execution timeout duration.
     * 
     * @return execution timeout duration, or null if not configured
     */
    public @Nullable Duration getExecutionTimeout() {
        return executionTimeout;
    }

    /**
     * Check if async execution is enabled.
     * 
     * @return true if async execution is enabled, false if disabled, null if not configured
     */
    public @Nullable Boolean isEnableAsyncExecution() {
        return enableAsyncExecution;
    }

    /**
     * Get the maximum number of concurrent executions.
     * 
     * @return maximum concurrent executions, or null if not configured
     */
    public @Nullable Integer getMaxConcurrentExecutions() {
        return maxConcurrentExecutions;
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
        ActionExecutionConfiguration other = (ActionExecutionConfiguration) obj;
        return Objects.equals(maxRetryAttempts, other.maxRetryAttempts) && Objects.equals(retryDelay, other.retryDelay)
                && Objects.equals(enableCaching, other.enableCaching)
                && Objects.equals(enableSecurityValidation, other.enableSecurityValidation)
                && Objects.equals(cacheExpiration, other.cacheExpiration)
                && Objects.equals(executionTimeout, other.executionTimeout)
                && Objects.equals(enableAsyncExecution, other.enableAsyncExecution)
                && Objects.equals(maxConcurrentExecutions, other.maxConcurrentExecutions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), maxRetryAttempts, retryDelay, enableCaching, enableSecurityValidation,
                cacheExpiration, executionTimeout, enableAsyncExecution, maxConcurrentExecutions);
    }

    @Override
    public String toString() {
        return "ActionExecutionConfiguration{" + "maxRetryAttempts=" + maxRetryAttempts + ", retryDelay=" + retryDelay
                + ", enableCaching=" + enableCaching + ", enableSecurityValidation=" + enableSecurityValidation
                + ", cacheExpiration=" + cacheExpiration + ", executionTimeout=" + executionTimeout
                + ", enableAsyncExecution=" + enableAsyncExecution + ", maxConcurrentExecutions="
                + maxConcurrentExecutions + '}';
    }
}
