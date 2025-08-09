package org.openhab.core.ai.tool.elicitation;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.tool.elicitation.input.ElicitationRequest;
import org.openhab.core.ai.tool.elicitation.input.ElicitationResult;

/**
 * Interface for MCP Elicitation service.
 * 
 * This service provides user input request handling during interactions,
 * allowing servers to request specific information from users.
 * 
 * @author Karel Goderis - Initial Contribution
 */
@NonNullByDefault
public interface ElicitationService {

    /**
     * Request input from the user.
     * 
     * @param request the elicitation request
     * @return future with the elicitation result
     */
    CompletableFuture<ElicitationResult> requestInput(ElicitationRequest request);

    /**
     * Provide a response to an elicitation request.
     * 
     * @param requestId the request ID
     * @param response the user response
     * @return future with the elicitation result
     */
    CompletableFuture<ElicitationResult> provideResponse(String requestId, Object response);

    /**
     * Cancel an elicitation request.
     * 
     * @param requestId the request ID
     * @param reason the cancellation reason
     * @return future with the elicitation result
     */
    CompletableFuture<ElicitationResult> cancelRequest(String requestId, String reason);

    /**
     * Get a pending elicitation request by ID.
     * 
     * @param requestId the request ID
     * @return the elicitation request or null if not found
     */
    @Nullable
    ElicitationRequest getPendingRequest(String requestId);

    /**
     * Get all pending elicitation requests.
     * 
     * @return map of pending requests
     */
    Map<String, ElicitationRequest> getAllPendingRequests();

    /**
     * Get the count of pending requests.
     * 
     * @return pending request count
     */
    int getPendingRequestCount();

    /**
     * Get performance metrics.
     * 
     * @return performance metrics as a map
     */
    Map<String, Object> getPerformanceMetrics();
}
