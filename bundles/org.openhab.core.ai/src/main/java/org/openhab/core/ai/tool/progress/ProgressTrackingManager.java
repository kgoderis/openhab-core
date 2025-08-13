package org.openhab.core.ai.tool.progress;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.tool.progress.tracking.ProgressOperation;
import org.openhab.core.ai.tool.progress.tracking.ProgressStatus;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
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

    /** Performance monitoring */
    private final AtomicLong totalOperations = new AtomicLong(0);
    private final AtomicLong completedOperations = new AtomicLong(0);
    private final AtomicLong cancelledOperations = new AtomicLong(0);
    private final AtomicLong totalResponseTimeMs = new AtomicLong(0);

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
        totalOperations.incrementAndGet();
        long startTime = System.currentTimeMillis();

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

            LOGGER.info("Progress operation begun: {} - Description: {}", operationId, description);
            return true;

        } catch (Exception e) {
            LOGGER.error("Error beginning progress operation: {} - Description: {}", operationId, description, e);
            return false;
        } finally {
            long responseTime = System.currentTimeMillis() - startTime;
            totalResponseTimeMs.addAndGet(responseTime);
        }
    }

    @Override
    public boolean reportProgress(String operationId, int currentStep, String message) {
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
                completedOperations.incrementAndGet();
                LOGGER.info("Progress operation completed: {} - Final Step: {}", operationId, currentStep);
            }

            return true;

        } catch (Exception e) {
            LOGGER.error("Error reporting progress for operation: {} - Step: {}", operationId, currentStep, e);
            return false;
        }
    }

    @Override
    public boolean endOperation(String operationId, ProgressStatus status, String finalMessage) {
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

            // Update counters
            if (status == ProgressStatus.COMPLETED) {
                completedOperations.incrementAndGet();
            } else if (status == ProgressStatus.CANCELLED) {
                cancelledOperations.incrementAndGet();
            }

            LOGGER.info("Progress operation ended: {} - Status: {} - Final Message: {}", operationId, status,
                    finalMessage);
            return true;

        } catch (Exception e) {
            LOGGER.error("Error ending progress operation: {} - Status: {}", operationId, status, e);
            return false;
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
        Map<String, Object> metrics = new ConcurrentHashMap<>();
        metrics.put("totalOperations", totalOperations.get());
        metrics.put("completedOperations", completedOperations.get());
        metrics.put("cancelledOperations", cancelledOperations.get());
        metrics.put("activeOperations", activeOperations.size());
        metrics.put("totalResponseTimeMs", totalResponseTimeMs.get());
        metrics.put("averageResponseTimeMs",
                totalOperations.get() > 0 ? totalResponseTimeMs.get() / totalOperations.get() : 0);
        metrics.put("completionRate",
                totalOperations.get() > 0 ? (double) completedOperations.get() / totalOperations.get() : 0.0);
        return metrics;
    }

    // Extracted: org.openhab.core.ai.tool.progress.DefaultProgressOperation
}
