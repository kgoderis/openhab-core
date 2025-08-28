package org.openhab.core.ai.tool.progress;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.MetricKey;
import org.openhab.core.ai.common.monitoring.api.MetricKeys;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
import org.openhab.core.ai.tool.progress.api.tracking.ProgressOperation;
import org.openhab.core.ai.tool.progress.api.tracking.ProgressStatus;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Progress Tracking Manager implementation for MCP Utilities
 * 
 * Provides long-running operation progress tracking for MCP operations,
 * including tool execution progress, resource loading, and system operations.
 * 
 * @author Karel Goderis - Initial Contribution
 */
@Component(service = ProgressService.class, immediate = true)
@NonNullByDefault
public class ProgressTrackingManager implements ProgressService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProgressTrackingManager.class);

    /** Map of active progress operations by ID */
    private final Map<String, ProgressOperation> activeOperations = new ConcurrentHashMap<>();

    @Reference
    private @Nullable MetricsService metricsService;

    @Activate
    public ProgressTrackingManager() {
        LOGGER.debug("Initializing Progress Tracking Manager");
    }

    @Deactivate
    public void deactivate() {
        LOGGER.debug("Deactivating OpenHAB Progress Tracking Service");
        activeOperations.clear();
    }

    @Modified
    public void modified() {
        LOGGER.debug("Modifying OpenHAB Progress Tracking Service");
    }

    @Override
    public boolean beginOperation(String operationId, String description, int totalSteps) {
        long startTime = System.nanoTime();
        boolean success = false;

        try {
            LOGGER.debug("Beginning progress operation: {} - Description: {} - Total Steps: {}", operationId,
                    description, totalSteps);

            // Validate input parameters
            if (operationId == null || operationId.trim().isEmpty()) {
                throw new IllegalArgumentException("Operation ID cannot be null or empty");
            }
            if (description == null || description.trim().isEmpty()) {
                throw new IllegalArgumentException("Operation description cannot be null or empty");
            }
            if (totalSteps <= 0) {
                throw new IllegalArgumentException("Total steps must be greater than 0");
            }

            // Check if operation already exists
            if (activeOperations.containsKey(operationId)) {
                LOGGER.warn("Progress operation already exists: {}", operationId);
                return false;
            }

            // Create and add operation
            ProgressOperation operation = new DefaultProgressOperation(operationId, description, totalSteps,
                    ProgressStatus.IN_PROGRESS, System.currentTimeMillis());
            activeOperations.put(operationId, operation);

            success = true;
            LOGGER.info("Progress operation begun: {} - Description: {}", operationId, description);
            return true;

        } catch (Exception e) {
            LOGGER.error("Error beginning progress operation: {} - Description: {}", operationId, description, e);
            return false;
        } finally {
            long durationNanos = System.nanoTime() - startTime;
            recordMetrics("progress-tracking", "begin-operation", success, durationNanos);
        }
    }

    @Override
    public boolean reportProgress(String operationId, int currentStep, String message) {
        long startTime = System.nanoTime();
        boolean success = false;

        try {
            LOGGER.debug("Reporting progress for operation: {} - Step: {}/{} - Message: {}", operationId, currentStep,
                    getTotalSteps(operationId), message);

            ProgressOperation operation = activeOperations.get(operationId);
            if (operation == null) {
                LOGGER.warn("Progress operation not found: {}", operationId);
                return false;
            }

            // Update operation progress
            operation.setCurrentStep(currentStep);
            operation.setMessage(message);
            operation.setLastUpdateTime(System.currentTimeMillis());

            // Check if operation is complete
            if (currentStep >= operation.getTotalSteps()) {
                operation.setStatus(ProgressStatus.COMPLETED);
                operation.setCompletionTime(System.currentTimeMillis());
                activeOperations.remove(operationId);
                success = true;
                LOGGER.info("Progress operation completed: {} - Final Step: {}", operationId, currentStep);
            } else {
                success = true;
            }

            return success;

        } catch (Exception e) {
            LOGGER.error("Error reporting progress for operation: {} - Step: {}", operationId, currentStep, e);
            return false;
        } finally {
            long durationNanos = System.nanoTime() - startTime;
            recordMetrics("progress-tracking", "report-progress", success, durationNanos);
        }
    }

    @Override
    public boolean endOperation(String operationId, ProgressStatus status, String finalMessage) {
        long startTime = System.nanoTime();
        boolean success = false;

        try {
            LOGGER.debug("Ending progress operation: {} - Status: {} - Message: {}", operationId, status, finalMessage);

            ProgressOperation operation = activeOperations.remove(operationId);
            if (operation == null) {
                LOGGER.warn("Progress operation not found for ending: {}", operationId);
                return false;
            }

            // Update operation status
            operation.setStatus(status);
            operation.setMessage(finalMessage);
            operation.setCompletionTime(System.currentTimeMillis());

            success = true;
            LOGGER.info("Progress operation ended: {} - Status: {} - Final Message: {}", operationId, status,
                    finalMessage);
            return true;

        } catch (Exception e) {
            LOGGER.error("Error ending progress operation: {} - Status: {}", operationId, status, e);
            return false;
        } finally {
            long durationNanos = System.nanoTime() - startTime;
            recordMetrics("progress-tracking", "end-operation", success, durationNanos);
        }
    }

    @Override
    public @Nullable ProgressOperation getOperation(String operationId) {
        return activeOperations.get(operationId);
    }

    @Override
    public Map<String, ProgressOperation> getAllActiveOperations() {
        return new ConcurrentHashMap<>(activeOperations);
    }

    @Override
    public int getActiveOperationCount() {
        return activeOperations.size();
    }

    @Override
    public int getTotalSteps(String operationId) {
        ProgressOperation operation = activeOperations.get(operationId);
        return operation != null ? operation.getTotalSteps() : 0;
    }

    @Override
    public Map<String, Object> getPerformanceMetrics() {
        Map<String, Object> result = new ConcurrentHashMap<>();
        MetricsService metrics = metricsService;
        if (metrics != null) {
            try {
                MetricKey progressTrackingKey = MetricKeys.custom("progress-tracking", Map.of(),
                        Set.of("counts", "latency"));
                var snapshot = metrics.getSnapshot(progressTrackingKey,
                        org.openhab.core.ai.common.monitoring.service.snapshot.GenericMetricsSnapshot.class);

                if (snapshot != null) {
                    long totalOperations = snapshot.getLong("total");
                    long failedOperations = snapshot.getLong("failure");
                    long successfulOperations = totalOperations - failedOperations;
                    long totalDurationNanos = snapshot.getLong("totalDurationNanos");

                    result.put("totalOperations", totalOperations);
                    result.put("completedOperations", successfulOperations);
                    result.put("cancelledOperations", 0L); // Placeholder or derive from domain-specific data
                    result.put("activeOperations", activeOperations.size());
                    result.put("totalResponseTimeMs", totalDurationNanos / 1_000_000); // Convert nanos to ms

                    if (totalOperations > 0) {
                        result.put("averageResponseTimeMs", (totalDurationNanos / 1_000_000) / totalOperations);
                        result.put("completionRate", (double) successfulOperations / totalOperations);
                    } else {
                        result.put("averageResponseTimeMs", 0L);
                        result.put("completionRate", 0.0);
                    }
                }
            } catch (Exception e) {
                LOGGER.warn("Error retrieving metrics for progress-tracking: {}", e.getMessage());
                // Fallback to default values
                result.put("totalOperations", 0L);
                result.put("completedOperations", 0L);
                result.put("cancelledOperations", 0L);
                result.put("activeOperations", activeOperations.size());
                result.put("totalResponseTimeMs", 0L);
                result.put("averageResponseTimeMs", 0L);
                result.put("completionRate", 0.0);
            }
        } else {
            // Fallback to default values if MetricsService is not available
            result.put("totalOperations", 0L);
            result.put("completedOperations", 0L);
            result.put("cancelledOperations", 0L);
            result.put("activeOperations", activeOperations.size());
            result.put("totalResponseTimeMs", 0L);
            result.put("averageResponseTimeMs", 0L);
            result.put("completionRate", 0.0);
        }
        return result;
    }

    private void recordMetrics(String domain, String operation, boolean success, long durationNanos) {
        MetricsService metrics = metricsService;
        if (metrics != null) {
            try {
                Map<String, Object> context = Map.of(
                    "domain", domain,
                    "operation", operation
                );
                metrics.recordOperationWithData(domain, operation, success, Duration.ofNanos(durationNanos), context);
            } catch (Exception e) {
                LOGGER.warn("Failed to record progress tracking metrics for operation {} - {}: {}", domain, operation,
                        e.getMessage());
                // Graceful degradation: continue with progress tracking even if metrics recording fails
            }
        } else {
            LOGGER.warn("MetricsService not available, cannot record metrics for operation: {} - {}", domain,
                    operation);
        }
    }

    // Extracted: org.openhab.core.ai.tool.progress.DefaultProgressOperation
}
