package org.openhab.core.ai.agent.collaboration;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Unified conflict type enumeration for openHAB AI agent collaboration.
 * 
 * Defines the different types of conflicts that can occur during agent
 * collaboration, coordination, and conflict resolution.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public enum ConflictType {
    /**
     * No conflict detected.
     */
    NONE("none", "No Conflict"),

    /**
     * Resource conflict.
     * Conflicts over shared resources, access rights, or resource allocation.
     */
    RESOURCE_CONFLICT("resource_conflict", "Resource Conflict"),

    /**
     * Policy conflict.
     * Conflicts over security policies, access policies, or operational policies.
     */
    POLICY_CONFLICT("policy_conflict", "Policy Conflict"),

    /**
     * Priority conflict.
     * Conflicts over task priorities, execution order, or importance levels.
     */
    PRIORITY_CONFLICT("priority_conflict", "Priority Conflict"),

    /**
     * Coordination conflict.
     * Conflicts over coordination strategies, synchronization, or workflow coordination.
     */
    COORDINATION_CONFLICT("coordination_conflict", "Coordination Conflict"),

    /**
     * Communication conflict.
     * Conflicts over communication protocols, message formats, or communication channels.
     */
    COMMUNICATION_CONFLICT("communication_conflict", "Communication Conflict"),

    /**
     * General conflict.
     * Generic conflicts that don't fit into specific categories.
     */
    GENERAL_CONFLICT("general_conflict", "General Conflict");

    private final String code;
    private final String description;

    /**
     * Create a new conflict type.
     *
     * @param code the conflict type code
     * @param description the conflict type description
     */
    ConflictType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * Get the conflict type code.
     *
     * @return the conflict type code
     */
    public String getCode() {
        return code;
    }

    /**
     * Get the conflict type description.
     *
     * @return the conflict type description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Get conflict type by code.
     *
     * @param code the conflict type code
     * @return the conflict type, or null if not found
     */
    public static ConflictType fromCode(String code) {
        if (code == null) {
            return null;
        }

        for (ConflictType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        return null;
    }

    /**
     * Check if this conflict type is a coordination-related conflict.
     *
     * @return true if coordination-related
     */
    public boolean isCoordinationConflict() {
        return this == COORDINATION_CONFLICT || this == PRIORITY_CONFLICT || this == GENERAL_CONFLICT;
    }

    /**
     * Check if this conflict type is a resource-related conflict.
     *
     * @return true if resource-related
     */
    public boolean isResourceConflict() {
        return this == RESOURCE_CONFLICT || this == POLICY_CONFLICT;
    }

    /**
     * Check if this conflict type is a communication-related conflict.
     *
     * @return true if communication-related
     */
    public boolean isCommunicationConflict() {
        return this == COMMUNICATION_CONFLICT;
    }

    @Override
    public String toString() {
        return code + " (" + description + ")";
    }
}
