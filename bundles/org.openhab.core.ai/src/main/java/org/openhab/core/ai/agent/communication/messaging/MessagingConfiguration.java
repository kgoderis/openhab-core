package org.openhab.core.ai.agent.communication.messaging;

import java.time.Duration;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Messaging configuration parameters.
 *
 * Holds tunable values for the messaging subsystem.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class MessagingConfiguration {
    private Duration messageTimeout = Duration.ofMinutes(5);
    private int maxRetries = 3;
    private Duration retryInterval = Duration.ofSeconds(30);
    private Duration messageRetentionPeriod = Duration.ofDays(7);
    private boolean enableEncryption = false;
    private boolean enablePersistence = true;
    private int maxMessageSize = 1024 * 1024; // 1MB

    public Duration getMessageTimeout() {
        return messageTimeout;
    }

    public void setMessageTimeout(Duration messageTimeout) {
        this.messageTimeout = messageTimeout;
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

    public Duration getMessageRetentionPeriod() {
        return messageRetentionPeriod;
    }

    public void setMessageRetentionPeriod(Duration messageRetentionPeriod) {
        this.messageRetentionPeriod = messageRetentionPeriod;
    }

    public boolean isEnableEncryption() {
        return enableEncryption;
    }

    public void setEnableEncryption(boolean enableEncryption) {
        this.enableEncryption = enableEncryption;
    }

    public boolean isEnablePersistence() {
        return enablePersistence;
    }

    public void setEnablePersistence(boolean enablePersistence) {
        this.enablePersistence = enablePersistence;
    }

    public int getMaxMessageSize() {
        return maxMessageSize;
    }

    public void setMaxMessageSize(int maxMessageSize) {
        this.maxMessageSize = maxMessageSize;
    }
}
