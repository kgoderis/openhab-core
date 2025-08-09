package org.openhab.core.ai.tool.sampling;

/**
 * Enumeration of sampling request statuses.
 * 
 * @author Karel Goderis - Initial Contribution
 */
public enum SamplingStatus {
    /**
     * Request is pending approval.
     */
    PENDING,

    /**
     * Request has been approved.
     */
    APPROVED,

    /**
     * Request has been rejected.
     */
    REJECTED
}
