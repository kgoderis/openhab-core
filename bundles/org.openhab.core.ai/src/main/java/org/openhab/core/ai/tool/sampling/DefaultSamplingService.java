package org.openhab.core.ai.tool.sampling;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.tool.sampling.models.DefaultSamplingRequest;
import org.openhab.core.ai.tool.sampling.models.SamplingRequest;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default Sampling Service implementation for MCP Client Features
 * 
 * Provides AI model interactions with human-in-the-loop approval mechanisms
 * for sampling requests that require user approval.
 * 
 * @author Karel Goderis - Initial Contribution
 */
@Component(service = SamplingService.class, immediate = true)
@NonNullByDefault
public class DefaultSamplingService implements SamplingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultSamplingService.class);

    /** Map of pending sampling requests by ID */
    private final Map<String, SamplingRequest> pendingRequests = new ConcurrentHashMap<>();

    /** Map of approved sampling requests by ID */
    private final Map<String, SamplingRequest> approvedRequests = new ConcurrentHashMap<>();

    /** Map of rejected sampling requests by ID */
    private final Map<String, SamplingRequest> rejectedRequests = new ConcurrentHashMap<>();

    /** Performance monitoring */
    private final AtomicLong totalRequests = new AtomicLong(0);
    private final AtomicLong approvedRequestCount = new AtomicLong(0);
    private final AtomicLong rejectedRequestCount = new AtomicLong(0);
    private final AtomicLong pendingRequestCount = new AtomicLong(0);
    private final AtomicLong totalResponseTimeMs = new AtomicLong(0);

    @Activate
    public DefaultSamplingService() {
        LOGGER.debug("Initializing OpenHAB Sampling Service");
    }

    @Deactivate
    public void deactivate() {
        LOGGER.debug("Deactivating OpenHAB Sampling Service");
        pendingRequests.clear();
    }

    @Modified
    public void modified() {
        LOGGER.debug("Modifying OpenHAB Sampling Service");
    }

    @Override
    public SamplingRequest createMessage(String modelName, String message, boolean includeContext) {
        totalRequests.incrementAndGet();
        long startTime = System.currentTimeMillis();

        try {
            LOGGER.debug("Creating sampling message for model: {}, includeContext: {}", modelName, includeContext);

            // Validate input parameters
            if (modelName == null || modelName.trim().isEmpty()) {
                throw new IllegalArgumentException("Model name cannot be null or empty");
            }
            if (message == null || message.trim().isEmpty()) {
                throw new IllegalArgumentException("Message cannot be null or empty");
            }

            // Create sampling request
            SamplingRequest request = new DefaultSamplingRequest(generateRequestId(), modelName, message,
                    includeContext, SamplingRequest.SamplingStatus.PENDING);

            // Store pending request
            pendingRequests.put(request.getId(), request);
            pendingRequestCount.incrementAndGet();

            LOGGER.info("Created sampling request: {} for model: {}", request.getId(), modelName);
            return request;

        } catch (Exception e) {
            LOGGER.error("Error creating sampling message", e);
            throw new RuntimeException("Failed to create sampling message", e);
        } finally {
            long responseTime = System.currentTimeMillis() - startTime;
            totalResponseTimeMs.addAndGet(responseTime);
        }
    }

    @Override
    public @Nullable SamplingRequest getRequest(String requestId) {
        try {
            LOGGER.debug("Getting sampling request: {}", requestId);

            // Check pending requests
            SamplingRequest request = pendingRequests.get(requestId);
            if (request != null) {
                return request;
            }

            // Check approved requests
            request = approvedRequests.get(requestId);
            if (request != null) {
                return request;
            }

            // Check rejected requests
            request = rejectedRequests.get(requestId);
            if (request != null) {
                return request;
            }

            LOGGER.warn("Sampling request not found: {}", requestId);
            return null;

        } catch (Exception e) {
            LOGGER.error("Error getting sampling request: {}", requestId, e);
            return null;
        }
    }

    @Override
    public boolean approveRequest(String requestId) {
        try {
            LOGGER.debug("Approving sampling request: {}", requestId);

            SamplingRequest request = pendingRequests.remove(requestId);
            if (request == null) {
                LOGGER.warn("Sampling request not found for approval: {}", requestId);
                return false;
            }

            // Update request status
            request.setStatus(SamplingRequest.SamplingStatus.APPROVED);
            approvedRequests.put(requestId, request);

            // Update counters
            pendingRequestCount.decrementAndGet();
            approvedRequestCount.incrementAndGet();

            LOGGER.info("Approved sampling request: {}", requestId);
            return true;

        } catch (Exception e) {
            LOGGER.error("Error approving sampling request: {}", requestId, e);
            return false;
        }
    }

    @Override
    public boolean rejectRequest(String requestId, String reason) {
        try {
            LOGGER.debug("Rejecting sampling request: {} with reason: {}", requestId, reason);

            SamplingRequest request = pendingRequests.remove(requestId);
            if (request == null) {
                LOGGER.warn("Sampling request not found for rejection: {}", requestId);
                return false;
            }

            // Update request status and reason
            request.setStatus(SamplingRequest.SamplingStatus.REJECTED);
            request.setRejectionReason(reason);
            rejectedRequests.put(requestId, request);

            // Update counters
            pendingRequestCount.decrementAndGet();
            rejectedRequestCount.incrementAndGet();

            LOGGER.info("Rejected sampling request: {} with reason: {}", requestId, reason);
            return true;

        } catch (Exception e) {
            LOGGER.error("Error rejecting sampling request: {}", requestId, e);
            return false;
        }
    }

    @Override
    public Map<String, SamplingRequest> getPendingRequests() {
        return new ConcurrentHashMap<>(pendingRequests);
    }

    @Override
    public Map<String, SamplingRequest> getApprovedRequests() {
        return new ConcurrentHashMap<>(approvedRequests);
    }

    @Override
    public Map<String, SamplingRequest> getRejectedRequests() {
        return new ConcurrentHashMap<>(rejectedRequests);
    }

    @Override
    public int getPendingRequestCount() {
        return (int) pendingRequestCount.get();
    }

    @Override
    public int getApprovedRequestCount() {
        return approvedRequests.size();
    }

    @Override
    public int getRejectedRequestCount() {
        return rejectedRequests.size();
    }

    @Override
    public Map<String, Object> getPerformanceMetrics() {
        Map<String, Object> metrics = new ConcurrentHashMap<>();
        metrics.put("totalRequests", totalRequests.get());
        metrics.put("approvedRequests", approvedRequestCount.get());
        metrics.put("rejectedRequests", rejectedRequestCount.get());
        metrics.put("pendingRequests", pendingRequestCount.get());
        metrics.put("totalResponseTimeMs", totalResponseTimeMs.get());
        metrics.put("averageResponseTimeMs",
                totalRequests.get() > 0 ? totalResponseTimeMs.get() / totalRequests.get() : 0);
        metrics.put("approvalRate",
                totalRequests.get() > 0 ? (double) approvedRequestCount.get() / totalRequests.get() : 0.0);
        return metrics;
    }

    /**
     * Generate a unique request ID.
     * 
     * @return unique request ID
     */
    private String generateRequestId() {
        return "sampling_" + System.currentTimeMillis() + "_" + Thread.currentThread().getId();
    }
}
