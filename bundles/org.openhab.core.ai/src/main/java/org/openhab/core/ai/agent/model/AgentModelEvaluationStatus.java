package org.openhab.core.ai.agent.model;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Status of agent model evaluation.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public enum AgentModelEvaluationStatus {

    /** Evaluation is pending */
    PENDING("Pending"),

    /** Model evaluation was excellent */
    EXCELLENT("Excellent"),

    /** Model evaluation was good */
    GOOD("Good"),

    /** Model evaluation was fair */
    FAIR("Fair"),

    /** Model evaluation was poor */
    POOR("Poor"),

    /** Model was not found */
    NOT_FOUND("Not Found"),

    /** Evaluation encountered an error */
    ERROR("Error");

    private final String displayName;

    AgentModelEvaluationStatus(String displayName) {
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
     * Checks if this status indicates a successful evaluation.
     * 
     * @return true if successful
     */
    public boolean isSuccessful() {
        return this == EXCELLENT || this == GOOD || this == FAIR || this == POOR;
    }

    /**
     * Checks if this status indicates an error condition.
     * 
     * @return true if error
     */
    public boolean isError() {
        return this == ERROR || this == NOT_FOUND;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
