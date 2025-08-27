package org.openhab.core.ai.tool.elicitation;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.tool.elicitation.input.ElicitationRequest;
import org.openhab.core.ai.tool.elicitation.input.ElicitationResult;
import org.openhab.core.ai.tool.elicitation.input.ElicitationStatus;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Elicitation Manager implementation for MCP Client Features
 * 
 * Provides user input request handling during interactions,
 * allowing servers to request specific information from users.
 * 
 * @author Karel Goderis - Initial Contribution
 */
@Component(service = ElicitationService.class, immediate = true)
@NonNullByDefault
public class ElicitationManager implements ElicitationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ElicitationManager.class);

    /** Map of pending elicitation requests by ID */
    private final Map<String, ElicitationRequest> pendingRequests = new ConcurrentHashMap<>();

    /** Performance monitoring - migrated to MetricsService */
    // private final AtomicLong totalRequests = new AtomicLong(0);
    // private final AtomicLong completedRequests = new AtomicLong(0);
    // private final AtomicLong cancelledRequests = new AtomicLong(0);
    // private final AtomicLong totalResponseTimeMs = new AtomicLong(0);

    private MetricsService metricsService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.STATIC)
    protected void setMetricsService(MetricsService metricsService) {
        this.metricsService = metricsService;
        LOGGER.debug("MetricsService set for ElicitationManager");
    }

    @Activate
    public ElicitationManager() {
        LOGGER.debug("Initializing OpenHAB Elicitation Service");
    }

    @Deactivate
    public void deactivate() {
        LOGGER.debug("Deactivating OpenHAB Elicitation Service");
        pendingRequests.clear();
    }

    @Modified
    public void modified() {
        LOGGER.debug("Modifying OpenHAB Elicitation Service");
    }

    @Override
    public CompletableFuture<ElicitationResult> requestInput(ElicitationRequest request) {
        try {
            metricsService.recordOperation("elicitation", "request")
                .withSuccess(true)
                .withDuration(0L)
                .withData("requestId", request.getId())
                .withData("requestType", request.getType().name())
                .record();
        } catch (Exception e) {
            LOGGER.warn("Failed to record elicitation request metrics for request {}: {}", request.getId(), e.getMessage());
            // Graceful degradation: continue with request processing even if metrics recording fails
        }
        long startTime = System.currentTimeMillis();

        try {
            LOGGER.debug("Creating elicitation request: {}", request.getId());

            // Validate request
            validateElicitationRequest(request);

            // Store pending request
            pendingRequests.put(request.getId(), request);

            // TODO: Implement actual user interface interaction
            // This would typically involve:
            // 1. Sending notification to user interface
            // 2. Displaying input form with schema validation
            // 3. Waiting for user response
            // 4. Processing and returning result

            // For now, return a mock result indicating pending status
            ElicitationResult result = new ElicitationResult(request.getId(), ElicitationStatus.PENDING,
                    "Elicitation request requires user input", null, System.currentTimeMillis());

            LOGGER.info("Elicitation request created: {} - Status: {}", request.getId(), result.getStatus());
            return CompletableFuture.completedFuture(result);

        } catch (Exception e) {
            LOGGER.error("Error creating elicitation request: {}", request.getId(), e);
            metricsService.recordOperation("elicitation", "error")
                .withSuccess(false)
                .withDuration(Duration.ofMillis(System.currentTimeMillis() - startTime).toNanos())
                .withData("requestId", request.getId())
                .withData("exceptionType", e.getClass().getSimpleName())
                .withData("errorMessage", e.getMessage() != null ? e.getMessage() : "Unknown error")
                .record();
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    public CompletableFuture<ElicitationResult> provideResponse(String requestId, Object response) {
        try {
            LOGGER.debug("Providing response for elicitation request: {}", requestId);

            ElicitationRequest request = pendingRequests.get(requestId);
            if (request == null) {
                throw new IllegalArgumentException("Elicitation request not found: " + requestId);
            }

            // Validate response against schema
            validateResponse(request, response);

            // Process response
            ElicitationResult result = new ElicitationResult(requestId, ElicitationStatus.COMPLETED,
                    "Elicitation request completed successfully", response, System.currentTimeMillis());

            // Remove from pending requests
            pendingRequests.remove(requestId);
            // completedRequests.incrementAndGet(); // Removed AtomicLong

            LOGGER.info("Elicitation request completed: {} - Status: {}", requestId, result.getStatus());
            return CompletableFuture.completedFuture(result);

        } catch (Exception e) {
            LOGGER.error("Error providing response for elicitation request: {}", requestId, e);
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    public CompletableFuture<ElicitationResult> cancelRequest(String requestId, String reason) {
        try {
            LOGGER.debug("Cancelling elicitation request: {} - Reason: {}", requestId, reason);

            ElicitationRequest request = pendingRequests.get(requestId);
            if (request == null) {
                throw new IllegalArgumentException("Elicitation request not found: " + requestId);
            }

            // Process cancellation
            ElicitationResult result = new ElicitationResult(requestId, ElicitationStatus.CANCELLED, reason, null,
                    System.currentTimeMillis());

            // Remove from pending requests
            pendingRequests.remove(requestId);
            // cancelledRequests.incrementAndGet(); // Removed AtomicLong

            LOGGER.info("Elicitation request cancelled: {} - Reason: {}", requestId, reason);
            return CompletableFuture.completedFuture(result);

        } catch (Exception e) {
            LOGGER.error("Error cancelling elicitation request: {}", requestId, e);
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    public @Nullable ElicitationRequest getPendingRequest(String requestId) {
        return pendingRequests.get(requestId);
    }

    @Override
    public Map<String, ElicitationRequest> getAllPendingRequests() {
        return new ConcurrentHashMap<>(pendingRequests);
    }

    @Override
    public int getPendingRequestCount() {
        return pendingRequests.size();
    }

    @Override
    public Map<String, Object> getPerformanceMetrics() {
        Map<String, Object> metrics = new ConcurrentHashMap<>();
        // metrics.put("totalRequests", totalRequests.get()); // Removed AtomicLong
        // metrics.put("completedRequests", completedRequests.get()); // Removed AtomicLong
        // metrics.put("cancelledRequests", cancelledRequests.get()); // Removed AtomicLong
        metrics.put("pendingRequests", pendingRequests.size());
        // metrics.put("totalResponseTimeMs", totalResponseTimeMs.get()); // Removed AtomicLong
        // metrics.put("averageResponseTimeMs", // Removed AtomicLong
        //         totalRequests.get() > 0 ? totalResponseTimeMs.get() / totalRequests.get() : 0);
        // metrics.put("completionRate", // Removed AtomicLong
        //         totalRequests.get() > 0 ? (double) completedRequests.get() / totalRequests.get() : 0.0);
        return metrics;
    }

    /**
     * Validate an elicitation request
     * 
     * @param request the request to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validateElicitationRequest(ElicitationRequest request) {
        if (request.getId() == null || request.getId().trim().isEmpty()) {
            throw new IllegalArgumentException("Elicitation request ID cannot be null or empty");
        }
        if (request.getPrompt() == null || request.getPrompt().trim().isEmpty()) {
            throw new IllegalArgumentException("Elicitation request prompt cannot be null or empty");
        }
        if (request.getSchema() == null) {
            throw new IllegalArgumentException("Elicitation request schema cannot be null");
        }
    }

    /**
     * Validate a response against the request schema
     * 
     * @param request the elicitation request
     * @param response the user response
     * @throws IllegalArgumentException if validation fails
     */
    private void validateResponse(ElicitationRequest request, Object response) {
        // TODO: Implement actual schema validation
        // This would typically involve:
        // 1. Parsing the JSON schema from the request
        // 2. Validating the response against the schema
        // 3. Throwing appropriate validation errors

        if (response == null) {
            throw new IllegalArgumentException("Response cannot be null");
        }
    }
}
