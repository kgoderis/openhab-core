package org.openhab.core.ai.tool.sampling;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Interface for sampling request data.
 * 
 * Represents a request for AI model interaction that requires human approval.
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
     * Get the rejection reason if rejected.
     * 
     * @return the rejection reason or null if not rejected
     */
    @Nullable
    String getRejectionReason();

    /**
     * Set the rejection reason.
     * 
     * @param reason the rejection reason
     */
    void setRejectionReason(String reason);

    /**
     * Get the creation timestamp.
     * 
     * @return the creation timestamp
     */
    long getCreatedAt();
}
