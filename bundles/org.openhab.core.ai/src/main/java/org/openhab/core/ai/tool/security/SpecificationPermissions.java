package org.openhab.core.ai.tool.security;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Specification permissions for controlling access to tool specifications.
 * 
 * <p>
 * This class defines permissions for tool specifications including allowed actions,
 * allowed resources, and security requirements like encryption and audit logging.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class SpecificationPermissions {
    private final String specificationId;
    private final Set<String> allowedRoles;
    private final Set<String> allowedUsers;
    private final Set<String> deniedUsers;
    private final Set<String> allowedActions;
    private final Set<String> allowedResources;
    private final boolean encryptionRequired;
    private final boolean auditLoggingRequired;

    /**
     * Constructor for SpecificationPermissions.
     * 
     * @param specificationId specification identifier
     * @param allowedActions set of allowed actions
     * @param allowedResources set of allowed resources
     * @param encryptionRequired whether encryption is required
     * @param auditLoggingRequired whether audit logging is required
     */
    public SpecificationPermissions(String specificationId, Set<String> allowedRoles, Set<String> allowedUsers,
            Set<String> deniedUsers, Set<String> allowedActions, Set<String> allowedResources,
            boolean encryptionRequired, boolean auditLoggingRequired) {
        this.specificationId = specificationId;
        this.allowedRoles = allowedRoles;
        this.allowedUsers = allowedUsers;
        this.deniedUsers = deniedUsers;
        this.allowedActions = allowedActions;
        this.allowedResources = allowedResources;
        this.encryptionRequired = encryptionRequired;
        this.auditLoggingRequired = auditLoggingRequired;
    }

    /**
     * Get specification identifier.
     * 
     * @return specification ID
     */
    public String getSpecificationId() {
        return specificationId;
    }

    /**
     * Get set of allowed actions.
     * 
     * @return allowed actions
     */
    public Set<String> getAllowedActions() {
        return allowedActions;
    }

    /**
     * Get set of allowed resources.
     * 
     * @return allowed resources
     */
    public Set<String> getAllowedResources() {
        return allowedResources;
    }

    public Set<String> getAllowedRoles() {
        return allowedRoles;
    }

    public Set<String> getAllowedUsers() {
        return allowedUsers;
    }

    public Set<String> getDeniedUsers() {
        return deniedUsers;
    }

    /**
     * Check if encryption is required.
     * 
     * @return true if encryption is required
     */
    public boolean isEncryptionRequired() {
        return encryptionRequired;
    }

    /**
     * Check if audit logging is required.
     * 
     * @return true if audit logging is required
     */
    public boolean isAuditLoggingRequired() {
        return auditLoggingRequired;
    }
}
