package org.openhab.core.ai.tool.progress;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.tool.progress.tracking.ProgressOperation;
import org.openhab.core.ai.tool.progress.tracking.ProgressStatus;

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

    // TODO: Implement progress persistence
    // TODO: Add support for progress notifications
    // TODO: Implement progress cleanup
    // TODO: Add support for progress metrics

    /**
     * Concrete implementation of ProgressOperation interface.
     */
    private static class DefaultProgressOperation implements ProgressOperation {
        private final String id;
        private final String description;
        private final int totalSteps;
        private ProgressStatus status;
        private int currentStep;
        private String message;
        private final long startTime;
        private long lastUpdateTime;
        private long completionTime;

        public DefaultProgressOperation(String id, String description, int totalSteps, ProgressStatus status,
                long startTime) {
            this.id = id;
            this.description = description;
            this.totalSteps = totalSteps;
            this.status = status;
            this.currentStep = 0;
            this.message = "Operation started";
            this.startTime = startTime;
            this.lastUpdateTime = startTime;
            this.completionTime = 0;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public String getDescription() {
            return description;
        }

        @Override
        public int getTotalSteps() {
            return totalSteps;
        }

        @Override
        public ProgressStatus getStatus() {
            return status;
        }

        @Override
        public void setStatus(ProgressStatus status) {
            this.status = status;
        }

        @Override
        public int getCurrentStep() {
            return currentStep;
        }

        @Override
        public void setCurrentStep(int currentStep) {
            this.currentStep = currentStep;
        }

        @Override
        public String getMessage() {
            return message;
        }

        @Override
        public void setMessage(String message) {
            this.message = message;
        }

        @Override
        public long getStartTime() {
            return startTime;
        }

        @Override
        public long getLastUpdateTime() {
            return lastUpdateTime;
        }

        @Override
        public void setLastUpdateTime(long lastUpdateTime) {
            this.lastUpdateTime = lastUpdateTime;
        }

        @Override
        public long getCompletionTime() {
            return completionTime;
        }

        @Override
        public void setCompletionTime(long completionTime) {
            this.completionTime = completionTime;
        }

        @Override
        public double getProgressPercentage() {
            if (totalSteps <= 0) {
                return 0.0;
            }
            return Math.min(100.0, (double) currentStep / totalSteps * 100.0);
        }
    }
}
