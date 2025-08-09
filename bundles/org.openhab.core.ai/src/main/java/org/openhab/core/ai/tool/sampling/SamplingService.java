package org.openhab.core.ai.tool.sampling;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.tool.sampling.models.SamplingRequest;

/**
 * Interface for MCP Sampling service.
 * 
 * This service provides AI model interactions with human-in-the-loop approval
 * for sampling requests that require user consent.
 * 
 * @author Karel Goderis - Initial Contribution
 */
@NonNullByDefault
public interface SamplingService {

    /**
     * Create a new sampling message request.
     * 
     * @param modelName the AI model name
     * @param message the message content
     * @param includeContext whether to include conversation context
     * @return the created sampling request
     */
    SamplingRequest createMessage(String modelName, String message, boolean includeContext);

    /**
     * Get a sampling request by ID.
     * 
     * @param requestId the request ID
     * @return the sampling request or null if not found
     */
    @Nullable
    SamplingRequest getRequest(String requestId);

    /**
     * Approve a sampling request.
     * 
     * @param requestId the request ID
     * @return true if approved successfully
     */
    boolean approveRequest(String requestId);

    /**
     * Reject a sampling request.
     * 
     * @param requestId the request ID
     * @param reason the rejection reason
     * @return true if rejected successfully
     */
    boolean rejectRequest(String requestId, String reason);

    /**
     * Get all pending sampling requests.
     * 
     * @return map of pending requests
     */
    Map<String, SamplingRequest> getPendingRequests();

    /**
     * Get all approved sampling requests.
     * 
     * @return map of approved requests
     */
    Map<String, SamplingRequest> getApprovedRequests();

    /**
     * Get all rejected sampling requests.
     * 
     * @return map of rejected requests
     */
    Map<String, SamplingRequest> getRejectedRequests();

    /**
     * Get the count of pending requests.
     * 
     * @return pending request count
     */
    int getPendingRequestCount();

    /**
     * Get the count of approved requests.
     * 
     * @return approved request count
     */
    int getApprovedRequestCount();

    /**
     * Get the count of rejected requests.
     * 
     * @return rejected request count
     */
    int getRejectedRequestCount();

    /**
     * Get performance metrics.
     * 
     * @return performance metrics as a map
     */
    Map<String, Object> getPerformanceMetrics();
}
