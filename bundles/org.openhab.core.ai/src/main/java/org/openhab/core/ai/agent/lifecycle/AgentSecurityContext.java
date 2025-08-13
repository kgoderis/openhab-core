package org.openhab.core.ai.agent.lifecycle;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Agent security context for access control
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class AgentSecurityContext {
    private final Set<String> owners;
    private final Set<String> permissions;

    public AgentSecurityContext(Set<String> owners, Set<String> permissions) {
        this.owners = new CopyOnWriteArraySet<>(owners);
        this.permissions = new CopyOnWriteArraySet<>(permissions);
    }

    public Set<String> getOwners() {
        return Set.copyOf(owners);
    }

    public Set<String> getPermissions() {
        return Set.copyOf(permissions);
    }
}
