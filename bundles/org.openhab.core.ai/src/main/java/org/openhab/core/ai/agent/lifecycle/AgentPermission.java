package org.openhab.core.ai.agent.lifecycle;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public enum AgentPermission {
    CONNECT("connect"),
    EXECUTE("execute"),
    READ("read"),
    WRITE("write"),
    ADMIN("admin");

    private final String permission;

    AgentPermission(String permission) {
        this.permission = permission;
    }

    public String getPermission() {
        return permission;
    }
}
