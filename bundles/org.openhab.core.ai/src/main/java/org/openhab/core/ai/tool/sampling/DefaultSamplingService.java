package org.openhab.core.ai.tool.sampling;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.MetricKey;
import org.openhab.core.ai.common.monitoring.api.MetricKeys;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
import org.openhab.core.ai.common.monitoring.snapshot.ExecutionMetricsSnapshot;
import org.openhab.core.ai.common.monitoring.snapshot.SamplingServiceSnapshot;
import org.openhab.core.ai.common.sampling.SamplingStatus;
import org.openhab.core.ai.tool.sampling.models.DefaultSamplingRequest;
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

    // Performance monitoring - using centralized MetricsService instead of AtomicLong counters
    @Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
    private volatile @Nullable MetricsService metricsService;

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
                    includeContext, SamplingStatus.PENDING);

            // Store pending request
            pendingRequests.put(request.getId(), request);

            // Record metrics using centralized MetricsService
            long responseTime = System.currentTimeMillis() - startTime;
            if (metricsService != null) {
                try {
                    metricsService.recordOperation("sampling-service", "create-message").withSuccess(true)
                            .withDuration(Duration.ofMillis(responseTime).toNanos()).withData("modelName", modelName)
                            .withData("includeContext", includeContext).withData("responseTimeMs", responseTime)
                            .withData("pendingRequests", pendingRequests.size()).record();
                } catch (Exception e) {
                    LOGGER.warn("Failed to record sampling service create message metrics", e);
                }
            }

            LOGGER.info("Created sampling request: {} for model: {}", request.getId(), modelName);
            return request;

        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;

            // Record failure metrics using centralized MetricsService
            if (metricsService != null) {
                try {
                    metricsService.recordOperation("sampling-service", "create-message").withSuccess(false)
                            .withDuration(Duration.ofMillis(responseTime).toNanos()).withData("modelName", modelName)
                            .withData("includeContext", includeContext).withData("error", e.getMessage())
                            .withData("responseTimeMs", responseTime).record();
                } catch (Exception metricsError) {
                    LOGGER.warn("Failed to record sampling service create message failure metrics", metricsError);
                }
            }

            LOGGER.error("Error creating sampling message", e);
            throw new RuntimeException("Failed to create sampling message", e);
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
        long startTime = System.currentTimeMillis();

        try {
            LOGGER.debug("Approving sampling request: {}", requestId);

            SamplingRequest request = pendingRequests.remove(requestId);
            if (request == null) {
                LOGGER.warn("Sampling request not found for approval: {}", requestId);

                // Record failure metrics
                long responseTime = System.currentTimeMillis() - startTime;
                if (metricsService != null) {
                    try {
                        metricsService.recordOperation("sampling-service", "approve-request").withSuccess(false)
                                .withDuration(Duration.ofMillis(responseTime).toNanos())
                                .withData("requestId", requestId).withData("error", "Request not found")
                                .withData("responseTimeMs", responseTime).record();
                    } catch (Exception e) {
                        LOGGER.warn("Failed to record sampling service approve request failure metrics", e);
                    }
                }

                return false;
            }

            // Update request status
            request.setStatus(SamplingStatus.APPROVED);
            approvedRequests.put(requestId, request);

            // Record success metrics using centralized MetricsService
            long responseTime = System.currentTimeMillis() - startTime;
            if (metricsService != null) {
                try {
                    metricsService.recordOperation("sampling-service", "approve-request").withSuccess(true)
                            .withDuration(Duration.ofMillis(responseTime).toNanos()).withData("requestId", requestId)
                            .withData("responseTimeMs", responseTime)
                            .withData("pendingRequests", pendingRequests.size())
                            .withData("approvedRequests", approvedRequests.size()).record();
                } catch (Exception e) {
                    LOGGER.warn("Failed to record sampling service approve request metrics", e);
                }
            }

            LOGGER.info("Approved sampling request: {}", requestId);
            return true;

        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;

            // Record failure metrics using centralized MetricsService
            if (metricsService != null) {
                try {
                    metricsService.recordOperation("sampling-service", "approve-request").withSuccess(false)
                            .withDuration(Duration.ofMillis(responseTime).toNanos()).withData("requestId", requestId)
                            .withData("error", e.getMessage()).withData("responseTimeMs", responseTime).record();
                } catch (Exception metricsError) {
                    LOGGER.warn("Failed to record sampling service approve request failure metrics", metricsError);
                }
            }

            LOGGER.error("Error approving sampling request: {}", requestId, e);
            return false;
        }
    }

    @Override
    public boolean rejectRequest(String requestId, String reason) {
        long startTime = System.currentTimeMillis();

        try {
            LOGGER.debug("Rejecting sampling request: {} with reason: {}", requestId, reason);

            SamplingRequest request = pendingRequests.remove(requestId);
            if (request == null) {
                LOGGER.warn("Sampling request not found for rejection: {}", requestId);

                // Record failure metrics
                long responseTime = System.currentTimeMillis() - startTime;
                if (metricsService != null) {
                    try {
                        metricsService.recordOperation("sampling-service", "reject-request").withSuccess(false)
                                .withDuration(Duration.ofMillis(responseTime).toNanos())
                                .withData("requestId", requestId).withData("error", "Request not found")
                                .withData("responseTimeMs", responseTime).record();
                    } catch (Exception e) {
                        LOGGER.warn("Failed to record sampling service reject request failure metrics", e);
                    }
                }

                return false;
            }

            // Update request status and reason
            request.setStatus(SamplingStatus.REJECTED);
            request.setRejectionReason(reason);
            rejectedRequests.put(requestId, request);

            // Record success metrics using centralized MetricsService
            long responseTime = System.currentTimeMillis() - startTime;
            if (metricsService != null) {
                try {
                    metricsService.recordOperation("sampling-service", "reject-request").withSuccess(true)
                            .withDuration(Duration.ofMillis(responseTime).toNanos()).withData("requestId", requestId)
                            .withData("reason", reason).withData("responseTimeMs", responseTime)
                            .withData("pendingRequests", pendingRequests.size())
                            .withData("rejectedRequests", rejectedRequests.size()).record();
                } catch (Exception e) {
                    LOGGER.warn("Failed to record sampling service reject request metrics", e);
                }
            }

            LOGGER.info("Rejected sampling request: {} with reason: {}", requestId, reason);
            return true;

        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;

            // Record failure metrics using centralized MetricsService
            if (metricsService != null) {
                try {
                    metricsService.recordOperation("sampling-service", "reject-request").withSuccess(false)
                            .withDuration(Duration.ofMillis(responseTime).toNanos()).withData("requestId", requestId)
                            .withData("error", e.getMessage()).withData("responseTimeMs", responseTime).record();
                } catch (Exception metricsError) {
                    LOGGER.warn("Failed to record sampling service reject request failure metrics", metricsError);
                }
            }

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
        return pendingRequests.size();
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

        if (metricsService != null) {
            try {
                // Get sampling service snapshot
                MetricKey samplingKey = MetricKeys.execution("sampling-service");
                SamplingServiceSnapshot snapshot = metricsService.getSnapshot(samplingKey,
                        SamplingServiceSnapshot.class);

                if (snapshot != null) {
                    // Basic execution metrics
                    metrics.put("totalRequests", snapshot.total());
                    metrics.put("successfulRequests", snapshot.success());
                    metrics.put("failedRequests", snapshot.failure());
                    metrics.put("averageResponseTimeMs", snapshot.averageMs());

                    // Sampling-specific metrics
                    metrics.put("approvedRequests", snapshot.approvedRequests());
                    metrics.put("rejectedRequests", snapshot.rejectedRequests());
                    metrics.put("pendingRequests", snapshot.pendingRequests());

                    // Calculated metrics
                    metrics.put("approvalRate", snapshot.approvalRate());
                    metrics.put("approvalRatePercentage", snapshot.approvalRatePercentage());
                    metrics.put("rejectionRatePercentage", snapshot.rejectionRatePercentage());
                    metrics.put("pendingRatioPercentage", snapshot.pendingRatioPercentage());
                    metrics.put("efficiencyScore", snapshot.efficiencyScore());
                    metrics.put("processingThroughput", snapshot.processingThroughput());

                    // Cache metrics (from SamplingMetrics interface)
                    metrics.put("totalSamplesGenerated", snapshot.totalSamplesGenerated());
                    metrics.put("cacheHits", snapshot.cacheHits());
                    metrics.put("cacheMisses", snapshot.cacheMisses());
                    metrics.put("cacheSize", snapshot.cacheSize());
                    metrics.put("cacheHitRate", snapshot.cacheHitRate());
                    metrics.put("cacheEfficiency", snapshot.cacheEfficiency());

                    // Model information
                    metrics.put("modelType", snapshot.modelType());
                    metrics.put("modelVersion", snapshot.modelVersion());
                } else {
                    // Fallback to ExecutionMetricsSnapshot if SamplingServiceSnapshot not available
                    ExecutionMetricsSnapshot execSnapshot = metricsService.getSnapshot(samplingKey,
                            ExecutionMetricsSnapshot.class);

                    if (execSnapshot != null) {
                        metrics.put("totalRequests", execSnapshot.total());
                        metrics.put("successfulRequests", execSnapshot.success());
                        metrics.put("failedRequests", execSnapshot.failure());
                        metrics.put("averageResponseTimeMs", execSnapshot.averageMs());

                        // Use current counts for sampling-specific metrics
                        metrics.put("approvedRequests", (long) getApprovedRequestCount());
                        metrics.put("rejectedRequests", (long) getRejectedRequestCount());
                        metrics.put("pendingRequests", (long) getPendingRequestCount());

                        // Calculate basic approval rate
                        long totalProcessed = (long) getApprovedRequestCount() + getRejectedRequestCount();
                        metrics.put("approvalRate",
                                totalProcessed > 0 ? (double) getApprovedRequestCount() / totalProcessed : 0.0);
                    } else {
                        setDefaultMetrics(metrics);
                    }
                }

            } catch (Exception e) {
                LOGGER.warn("Failed to retrieve performance metrics from MetricsService", e);
                setDefaultMetrics(metrics);
            }
        } else {
            // No MetricsService available, return default values
            setDefaultMetrics(metrics);
        }

        // Always add current live counts for reference
        metrics.put("currentPendingRequests", pendingRequests.size());
        metrics.put("currentApprovedRequests", approvedRequests.size());
        metrics.put("currentRejectedRequests", rejectedRequests.size());

        return metrics;
    }

    /**
     * Set default metric values when MetricsService is unavailable or fails.
     * 
     * @param metrics the metrics map to populate
     */
    private void setDefaultMetrics(Map<String, Object> metrics) {
        metrics.put("totalRequests", 0L);
        metrics.put("successfulRequests", 0L);
        metrics.put("failedRequests", 0L);
        metrics.put("averageResponseTimeMs", 0.0);
        metrics.put("approvedRequests", 0L);
        metrics.put("rejectedRequests", 0L);
        metrics.put("pendingRequests", 0L);
        metrics.put("approvalRate", 0.0);
        metrics.put("approvalRatePercentage", 0.0);
        metrics.put("rejectionRatePercentage", 0.0);
        metrics.put("pendingRatioPercentage", 0.0);
        metrics.put("efficiencyScore", 0.0);
        metrics.put("processingThroughput", 0.0);
        metrics.put("totalSamplesGenerated", 0L);
        metrics.put("cacheHits", 0L);
        metrics.put("cacheMisses", 0L);
        metrics.put("cacheSize", 0);
        metrics.put("cacheHitRate", 0.0);
        metrics.put("cacheEfficiency", 0.0);
        metrics.put("modelType", "");
        metrics.put("modelVersion", "");
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
