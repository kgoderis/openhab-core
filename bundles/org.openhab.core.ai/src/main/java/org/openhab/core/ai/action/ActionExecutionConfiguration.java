package org.openhab.core.ai.action;

import java.time.Duration;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Configuration for action execution behavior (retries, caching, validation).
 *
 * author Karel Goderis - Initial Contribution
 * since 1.0.0
 */
@NonNullByDefault
public class ActionExecutionConfiguration {
    public @Nullable Integer maxRetryAttempts;
    public @Nullable Duration retryDelay;
    public @Nullable Boolean enableCaching;
    public @Nullable Boolean enableSecurityValidation;
    public @Nullable Duration cacheExpiration;

    public static ActionExecutionConfiguration builder() {
        return new ActionExecutionConfiguration();
    }

    public ActionExecutionConfiguration maxRetryAttempts(int maxRetryAttempts) {
        this.maxRetryAttempts = maxRetryAttempts;
        return this;
    }

    public ActionExecutionConfiguration retryDelay(Duration retryDelay) {
        this.retryDelay = retryDelay;
        return this;
    }

    public ActionExecutionConfiguration enableCaching(boolean enableCaching) {
        this.enableCaching = enableCaching;
        return this;
    }

    public ActionExecutionConfiguration enableSecurityValidation(boolean enableSecurityValidation) {
        this.enableSecurityValidation = enableSecurityValidation;
        return this;
    }

    public ActionExecutionConfiguration cacheExpiration(Duration cacheExpiration) {
        this.cacheExpiration = cacheExpiration;
        return this;
    }
}
