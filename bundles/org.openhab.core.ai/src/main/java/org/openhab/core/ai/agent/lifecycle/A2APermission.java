package org.openhab.core.ai.agent.lifecycle;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public enum A2APermission {
    CONNECT("connect"),
    EXECUTE("execute"),
    READ("read"),
    WRITE("write"),
    ADMIN("admin");

    private final String permission;

    A2APermission(String permission) {
        this.permission = permission;
    }

    public String getPermission() {
        return permission;
    }
}
