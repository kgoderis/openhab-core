package org.openhab.core.ai.rest;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class SessionInfo {
    final String userId;
    final String username;
    final long expirationTime;
    final boolean active;
    final long lastAccessTime;

    public SessionInfo(String userId, String username, long expirationTime, boolean active, long lastAccessTime) {
        this.userId = userId;
        this.username = username;
        this.expirationTime = expirationTime;
        this.active = active;
        this.lastAccessTime = lastAccessTime;
    }
}


