package org.openhab.core.ai.common.sampling;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Unified sampling status enumeration for openHAB AI components.
 * 
 * Defines the different statuses that can be used for sampling requests
 * across tool and other AI components.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public enum SamplingStatus {
    /**
     * Request is pending approval.
     */
    PENDING("pending", "Pending Approval"),

    /**
     * Request has been approved.
     */
    APPROVED("approved", "Approved"),

    /**
     * Request has been rejected.
     */
    REJECTED("rejected", "Rejected");

    private final String code;
    private final String description;

    /**
     * Create a new sampling status.
     *
     * @param code the status code
     * @param description the status description
     */
    SamplingStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * Get the status code.
     *
     * @return the status code
     */
    public String getCode() {
        return code;
    }

    /**
     * Get the status description.
     *
     * @return the status description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Get sampling status by code.
     *
     * @param code the status code
     * @return the sampling status, or null if not found
     */
    public static SamplingStatus fromCode(String code) {
        if (code == null) {
            return null;
        }

        for (SamplingStatus status : values()) {
            if (status.code.equalsIgnoreCase(code)) {
                return status;
            }
        }
        return null;
    }

    /**
     * Check if this status indicates the request is active.
     *
     * @return true if the request is active
     */
    public boolean isActive() {
        return this == PENDING || this == APPROVED;
    }

    /**
     * Check if this status indicates the request is completed.
     *
     * @return true if the request is completed
     */
    public boolean isCompleted() {
        return this == APPROVED || this == REJECTED;
    }

    @Override
    public String toString() {
        return code + " (" + description + ")";
    }
}
