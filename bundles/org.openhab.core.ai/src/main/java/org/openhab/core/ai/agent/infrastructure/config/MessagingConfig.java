package org.openhab.core.ai.agent.infrastructure.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class MessagingConfig {
    private int maxMessageSize;
    private long timeoutMs;
    private boolean enableRetry;
    private int maxRetries;

    public MessagingConfig() {}

    public int getMaxMessageSize() { return maxMessageSize; }
    public void setMaxMessageSize(int maxMessageSize) { this.maxMessageSize = maxMessageSize; }
    public long getTimeoutMs() { return timeoutMs; }
    public void setTimeoutMs(long timeoutMs) { this.timeoutMs = timeoutMs; }
    public boolean isEnableRetry() { return enableRetry; }
    public void setEnableRetry(boolean enableRetry) { this.enableRetry = enableRetry; }
    public int getMaxRetries() { return maxRetries; }
    public void setMaxRetries(int maxRetries) { this.maxRetries = maxRetries; }
}


