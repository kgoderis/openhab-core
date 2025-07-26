package org.openhab.core.ai.common.auth;

import java.util.Set;

/**
 * Role-Based Access Control interface for AI protocols.
 * 
 * This interface defines the contract for managing roles, permissions,
 * and access control in AI protocol implementations.
 * 
 * 
 */
public interface AIRoleBasedAccessControl {

    /**
     * Get permissions for a principal in a specific protocol.
     * 
     * @param principalId Principal identifier
     * @param protocol Protocol name (mcp, a2a)
     * @return Set of permissions
     */
    Set<String> getPermissions(String principalId, String protocol);

    /**
     * Check if a principal has a specific permission.
     * 
     * @param principalId Principal identifier
     * @param permission Permission to check
     * @param protocol Protocol name
     * @return true if permission is granted
     */
    boolean hasPermission(String principalId, String permission, String protocol);

    /**
     * Get roles assigned to a principal.
     * 
     * @param principalId Principal identifier
     * @return Set of role names
     */
    Set<String> getRoles(String principalId);

    /**
     * Assign a role to a principal.
     * 
     * @param principalId Principal identifier
     * @param roleName Role name to assign
     * @return true if role was assigned successfully
     */
    boolean assignRole(String principalId, String roleName);

    /**
     * Remove a role from a principal.
     * 
     * @param principalId Principal identifier
     * @param roleName Role name to remove
     * @return true if role was removed successfully
     */
    boolean removeRole(String principalId, String roleName);

    /**
     * Define a new role with permissions.
     * 
     * @param roleName Role name
     * @param permissions Set of permissions for this role
     * @param protocol Protocol this role applies to
     * @return true if role was created successfully
     */
    boolean defineRole(String roleName, Set<String> permissions, String protocol);

    /**
     * Remove a role definition.
     * 
     * @param roleName Role name to remove
     * @param protocol Protocol name
     * @return true if role was removed successfully
     */
    boolean removeRoleDefinition(String roleName, String protocol);

    /**
     * Get all available roles for a protocol.
     * 
     * @param protocol Protocol name
     * @return Set of role names
     */
    Set<String> getAvailableRoles(String protocol);

    /**
     * Get permissions for a specific role.
     * 
     * @param roleName Role name
     * @param protocol Protocol name
     * @return Set of permissions for the role
     */
    Set<String> getRolePermissions(String roleName, String protocol);

    /**
     * Check if a role exists for a protocol.
     * 
     * @param roleName Role name
     * @param protocol Protocol name
     * @return true if role exists
     */
    boolean roleExists(String roleName, String protocol);
}
