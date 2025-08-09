package org.openhab.core.ai.tool.sampling.models;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Interface for MCP Sampling request data.
 * 
 * This represents a sampling request for AI model interactions
 * with human-in-the-loop approval.
 * 
 * @author Karel Goderis - Initial Contribution
 */
@NonNullByDefault
public interface SamplingRequest {

    /**
     * Get the request ID.
     * 
     * @return the request ID
     */
    String getId();

    /**
     * Get the AI model name.
     * 
     * @return the model name
     */
    String getModelName();

    /**
     * Get the message content.
     * 
     * @return the message content
     */
    String getMessage();

    /**
     * Check if conversation context should be included.
     * 
     * @return true if context should be included
     */
    boolean isIncludeContext();

    /**
     * Get the request status.
     * 
     * @return the request status
     */
    SamplingStatus getStatus();

    /**
     * Set the request status.
     * 
     * @param status the new status
     */
    void setStatus(SamplingStatus status);

    /**
     * Get the rejection reason.
     * 
     * @return the rejection reason
     */
    String getRejectionReason();

    /**
     * Set the rejection reason.
     * 
     * @param rejectionReason the rejection reason
     */
    void setRejectionReason(String rejectionReason);

    /**
     * Get the creation timestamp.
     * 
     * @return the creation timestamp
     */
    long getCreatedAt();

    /**
     * Sampling status enumeration.
     */
    enum SamplingStatus {
        PENDING,
        APPROVED,
        REJECTED
    }
}
