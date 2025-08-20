package org.openhab.core.ai.tool.sampling;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.sampling.SamplingStatus;

/**
 * Interface for sampling requests in the AI tool system.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface SamplingRequest {

    /**
     * Get the unique identifier for this sampling request.
     * 
     * @return the request ID
     */
    String getId();

    /**
     * Get the model name for this sampling request.
     * 
     * @return the model name
     */
    String getModelName();

    /**
     * Get the message content for this sampling request.
     * 
     * @return the message content
     */
    String getMessage();

    /**
     * Check if context should be included in the sampling.
     * 
     * @return true if context should be included
     */
    boolean isIncludeContext();

    /**
     * Get the current status of this sampling request.
     * 
     * @return the sampling status
     */
    SamplingStatus getStatus();

    /**
     * Set the sampling status.
     * 
     * @param status the new sampling status
     */
    void setStatus(SamplingStatus status);

    /**
     * Get the rejection reason if the request was rejected.
     * 
     * @return the rejection reason, or null if not rejected
     */
    String getRejectionReason();

    /**
     * Set the rejection reason for this request.
     * 
     * @param rejectionReason the reason for rejection
     */
    void setRejectionReason(String rejectionReason);

    /**
     * Get the timestamp when this request was created.
     * 
     * @return the creation timestamp in milliseconds
     */
    long getCreatedAt();
}
