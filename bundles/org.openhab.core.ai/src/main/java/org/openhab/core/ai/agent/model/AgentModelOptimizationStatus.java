package org.openhab.core.ai.agent.model;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Status of agent model optimization.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public enum AgentModelOptimizationStatus {

    /** Optimization is pending */
    PENDING("Pending"),

    /** Significant improvement achieved */
    SIGNIFICANT_IMPROVEMENT("Significant Improvement"),

    /** Moderate improvement achieved */
    MODERATE_IMPROVEMENT("Moderate Improvement"),

    /** Minor improvement achieved */
    MINOR_IMPROVEMENT("Minor Improvement"),

    /** No improvement achieved */
    NO_IMPROVEMENT("No Improvement"),

    /** Model was not found */
    NOT_FOUND("Not Found"),

    /** Optimization encountered an error */
    ERROR("Error");

    private final String displayName;

    AgentModelOptimizationStatus(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Gets the display name for this status.
     * 
     * @return The display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Checks if this status indicates a successful optimization.
     * 
     * @return true if successful
     */
    public boolean isSuccessful() {
        return this == SIGNIFICANT_IMPROVEMENT || this == MODERATE_IMPROVEMENT || this == MINOR_IMPROVEMENT
                || this == NO_IMPROVEMENT;
    }

    /**
     * Checks if this status indicates an error condition.
     * 
     * @return true if error
     */
    public boolean isError() {
        return this == ERROR || this == NOT_FOUND;
    }

    /**
     * Checks if this status indicates an improvement was achieved.
     * 
     * @return true if improvement achieved
     */
    public boolean hasImprovement() {
        return this == SIGNIFICANT_IMPROVEMENT || this == MODERATE_IMPROVEMENT || this == MINOR_IMPROVEMENT;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
