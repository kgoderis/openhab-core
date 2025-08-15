package org.openhab.core.ai.agent.communication.events;

import java.time.Duration;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Event bus configuration parameters.
 *
 * Holds tunable values for the event bus subsystem.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class EventBusConfiguration {
    private Duration eventTimeout = Duration.ofMinutes(5);
    private int maxRetries = 3;
    private Duration retryInterval = Duration.ofSeconds(30);
    private Duration eventRetentionPeriod = Duration.ofDays(7);
    private boolean enablePersistence = true;
    private boolean enableSchemaValidation = true;
    private int maxEventBatchSize = 100;
    private Duration batchTimeout = Duration.ofSeconds(5);

    public Duration getEventTimeout() {
        return eventTimeout;
    }

    public void setEventTimeout(Duration eventTimeout) {
        this.eventTimeout = eventTimeout;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public Duration getRetryInterval() {
        return retryInterval;
    }

    public void setRetryInterval(Duration retryInterval) {
        this.retryInterval = retryInterval;
    }

    public Duration getEventRetentionPeriod() {
        return eventRetentionPeriod;
    }

    public void setEventRetentionPeriod(Duration eventRetentionPeriod) {
        this.eventRetentionPeriod = eventRetentionPeriod;
    }

    public boolean isEnablePersistence() {
        return enablePersistence;
    }

    public void setEnablePersistence(boolean enablePersistence) {
        this.enablePersistence = enablePersistence;
    }

    public boolean isEnableSchemaValidation() {
        return enableSchemaValidation;
    }

    public void setEnableSchemaValidation(boolean enableSchemaValidation) {
        this.enableSchemaValidation = enableSchemaValidation;
    }

    public int getMaxEventBatchSize() {
        return maxEventBatchSize;
    }

    public void setMaxEventBatchSize(int maxEventBatchSize) {
        this.maxEventBatchSize = maxEventBatchSize;
    }

    public Duration getBatchTimeout() {
        return batchTimeout;
    }

    public void setBatchTimeout(Duration batchTimeout) {
        this.batchTimeout = batchTimeout;
    }
}
