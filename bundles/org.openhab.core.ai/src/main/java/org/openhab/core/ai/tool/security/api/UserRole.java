package org.openhab.core.ai.tool.security.api;

import java.time.Instant;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * User role definition for security access control.
 *
 * <p>
 * Represents a role assignment for a user including permissions and assignment metadata.
 * </p>
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class UserRole {
    private final String userId;
    private final String roleName;
    private final Set<String> permissions;
    private final Instant assignedAt;
    private final String assignedBy;

    public UserRole(String userId, String roleName, Set<String> permissions, Instant assignedAt, String assignedBy) {
        this.userId = userId;
        this.roleName = roleName;
        this.permissions = permissions;
        this.assignedAt = assignedAt;
        this.assignedBy = assignedBy;
    }

    public String getUserId() {
        return userId;
    }

    public String getRoleName() {
        return roleName;
    }

    // Backward-compatible alias used by older code
    public String role() {
        return roleName;
    }

    public Set<String> getPermissions() {
        return permissions;
    }

    public Instant getAssignedAt() {
        return assignedAt;
    }

    public String getAssignedBy() {
        return assignedBy;
    }
}
