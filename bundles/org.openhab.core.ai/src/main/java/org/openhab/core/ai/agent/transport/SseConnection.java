package org.openhab.core.ai.agent.transport;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * SSE Connection representation for agent transport
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class SseConnection {
    private final String connectionId;
    private final String protocol;
    private final String clientId;
    private final long createdAt;
    private volatile boolean isActive;
    private volatile long lastActivity;

    public SseConnection(String connectionId, String protocol, String clientId) {
        this.connectionId = connectionId;
        this.protocol = protocol;
        this.clientId = clientId;
        this.createdAt = System.currentTimeMillis();
        this.lastActivity = this.createdAt;
        this.isActive = true;
    }

    public String getConnectionId() {
        return connectionId;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getClientId() {
        return clientId;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        this.isActive = active;
    }

    public long getLastActivity() {
        return lastActivity;
    }

    public void updateActivity() {
        this.lastActivity = System.currentTimeMillis();
    }
}
