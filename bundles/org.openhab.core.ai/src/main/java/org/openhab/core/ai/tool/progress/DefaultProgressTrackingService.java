package org.openhab.core.ai.tool.progress;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.tool.progress.api.tracking.ProgressOperation;
import org.openhab.core.ai.tool.progress.api.tracking.ProgressStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of Progress Tracking Service for MCP Tools.
 * 
 * This service provides progress tracking functionality for long-running
 * operations in the MCP tool server.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class DefaultProgressTrackingService implements ProgressService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultProgressTrackingService.class);

    private final ConcurrentHashMap<String, ProgressOperation> operations = new ConcurrentHashMap<>();

    @Override
    public boolean beginOperation(String operationId, String description, int totalSteps) {
        DefaultProgressOperation operation = new DefaultProgressOperation(operationId, description, totalSteps,
                ProgressStatus.IN_PROGRESS, System.currentTimeMillis());
        operations.put(operationId, operation);
        return true;
    }

    @Override
    public boolean reportProgress(String operationId, int currentStep, String message) {
        ProgressOperation operation = operations.get(operationId);
        if (operation != null) {
            operation.setCurrentStep(currentStep);
            operation.setMessage(message);
            operation.setLastUpdateTime(System.currentTimeMillis());

            // Persist progress and send notification
            persistProgress(operationId, operation);
            sendProgressNotification(operationId, operation);

            return true;
        }
        return false;
    }

    @Override
    public boolean endOperation(String operationId, ProgressStatus status, String finalMessage) {
        ProgressOperation operation = operations.get(operationId);
        if (operation != null) {
            operation.setStatus(status);
            operation.setMessage(finalMessage);
            operation.setCompletionTime(System.currentTimeMillis());

            // Persist progress and send notification
            persistProgress(operationId, operation);
            sendProgressNotification(operationId, operation);

            return true;
        }
        return false;
    }

    @Override
    @Nullable
    public ProgressOperation getOperation(String operationId) {
        return operations.get(operationId);
    }

    @Override
    public Map<String, ProgressOperation> getAllActiveOperations() {
        return operations.entrySet().stream()
                .filter(entry -> entry.getValue().getStatus() == ProgressStatus.IN_PROGRESS)
                .collect(java.util.stream.Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public int getActiveOperationCount() {
        return (int) operations.values().stream()
                .filter(operation -> operation.getStatus() == ProgressStatus.IN_PROGRESS).count();
    }

    @Override
    public int getTotalSteps(String operationId) {
        ProgressOperation operation = operations.get(operationId);
        return operation != null ? operation.getTotalSteps() : 0;
    }

    @Override
    public Map<String, Object> getPerformanceMetrics() {
        return Map.of("totalOperations", operations.size(), "activeOperations", getActiveOperationCount(),
                "completedOperations",
                (int) operations.values().stream().filter(op -> op.getStatus() == ProgressStatus.COMPLETED).count());
    }

    // Progress persistence and cleanup
    private final Map<String, ProgressOperation> completedOperations = new ConcurrentHashMap<>();
    private final java.util.concurrent.ScheduledExecutorService cleanupExecutor = java.util.concurrent.Executors
            .newSingleThreadScheduledExecutor();

    public DefaultProgressTrackingService() {
        // Schedule cleanup task to run every hour
        cleanupExecutor.scheduleAtFixedRate(this::cleanupOldOperations, 1, 1, java.util.concurrent.TimeUnit.HOURS);
    }

    /**
     * Clean up old completed operations to prevent memory leaks.
     */
    private void cleanupOldOperations() {
        long cutoffTime = System.currentTimeMillis() - (24 * 60 * 60 * 1000); // 24 hours ago
        completedOperations.entrySet().removeIf(entry -> entry.getValue().getCompletionTime() < cutoffTime);

        // Also clean up old active operations that haven't been updated in a while
        long staleCutoff = System.currentTimeMillis() - (60 * 60 * 1000); // 1 hour ago
        operations.entrySet().removeIf(entry -> {
            ProgressOperation op = entry.getValue();
            return op.getStatus() != ProgressStatus.IN_PROGRESS && op.getLastUpdateTime() < staleCutoff;
        });
    }

    /**
     * Persist progress information to storage.
     */
    private void persistProgress(String operationId, ProgressOperation operation) {
        // TODO: Implement actual persistence to database or file system
        // For now, just store in memory
        if (operation.getStatus() == ProgressStatus.COMPLETED || operation.getStatus() == ProgressStatus.FAILED) {
            completedOperations.put(operationId, operation);
        }
    }

    /**
     * Send progress notification to subscribers.
     */
    private void sendProgressNotification(String operationId, ProgressOperation operation) {
        // TODO: Implement actual notification system (WebSocket, SSE, etc.)
        // For now, just log the notification
        LOGGER.debug("Progress notification for operation {}: {} - {}%", operationId, operation.getStatus(),
                operation.getProgressPercentage());
    }

    /**
     * Concrete implementation of ProgressOperation interface.
     */
    // Extracted: org.openhab.core.ai.tool.progress.DefaultProgressOperation
}
