package org.openhab.core.ai.agent.collaboration.context;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Context permission model.
 *
 * Captures read/write/delete/admin permissions for an agent on a context.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ContextPermission {

    private final boolean canRead;
    private final boolean canWrite;
    private final boolean canDelete;
    private final boolean isAdmin;

    public ContextPermission(boolean canRead, boolean canWrite, boolean canDelete, boolean isAdmin) {
        this.canRead = canRead;
        this.canWrite = canWrite;
        this.canDelete = canDelete;
        this.isAdmin = isAdmin;
    }

    public boolean canRead() {
        return canRead;
    }

    public boolean canWrite() {
        return canWrite;
    }

    public boolean canDelete() {
        return canDelete;
    }

    public boolean isAdmin() {
        return isAdmin;
    }
}
