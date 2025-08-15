package org.openhab.core.ai.tool.progress.tracking;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.tool.progress.api.tracking.ProgressInfo;
import org.openhab.core.ai.tool.progress.api.tracking.ProgressOperation;
import org.openhab.core.ai.tool.progress.api.tracking.ProgressStatus;
import org.openhab.core.ai.tool.progress.api.tracking.ProgressTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of progress tracking logic.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public abstract class DefaultProgressTracker implements ProgressTracker {

    private static final Logger logger = LoggerFactory.getLogger(DefaultProgressTracker.class);

    private final Map<String, ProgressOperation> operations = new ConcurrentHashMap<>();
    private final Map<String, Object> configuration = new ConcurrentHashMap<>();
    private final AtomicInteger totalOperations = new AtomicInteger(0);
    private final AtomicInteger completedOperations = new AtomicInteger(0);
    private final AtomicInteger failedOperations = new AtomicInteger(0);
    private final AtomicLong totalProcessingTime = new AtomicLong(0);
    private final AtomicReference<String> lastError = new AtomicReference<>();

    @Override
    public ProgressTracker startTracking(String operationId, int totalSteps) {
        logger.debug("Starting progress tracking for operation: {} with {} steps", operationId, totalSteps);
        ProgressOperation operation = createProgressOperation(operationId, totalSteps);
        operations.put(operationId, operation);
        totalOperations.incrementAndGet();
        persistOperationStart(operationId, totalSteps);
        sendProgressNotification(operationId, "STARTED", 0, "Operation started");
        return this;
    }

    @Override
    public void updateProgress(String operationId, int currentStep, String message) {
        logger.debug("Updating progress for operation: {} - step {}/{}: {}", operationId, currentStep,
                getTotalSteps(operationId), message);
        ProgressOperation operation = operations.get(operationId);
        if (operation != null) {
            operation.setCurrentStep(currentStep);
            operation.setMessage(message);
            operation.setLastUpdateTime(System.currentTimeMillis());
            persistProgressUpdate(operationId, currentStep, message);
            sendProgressNotification(operationId, "IN_PROGRESS", currentStep, message);
            updateAnalytics(operationId, currentStep);
        }
    }

    @Override
    public void completeProgress(String operationId, String message) {
        logger.debug("Completing progress for operation: {}: {}", operationId, message);
        ProgressOperation operation = operations.get(operationId);
        if (operation != null) {
            operation.setStatus(ProgressStatus.COMPLETED);
            operation.setMessage(message);
            operation.setCompletionTime(System.currentTimeMillis());
            operation.setCurrentStep(operation.getTotalSteps());
            completedOperations.incrementAndGet();
            totalProcessingTime.addAndGet(operation.getCompletionTime() - operation.getStartTime());
            persistOperationCompletion(operationId, message);
            sendProgressNotification(operationId, "COMPLETED", operation.getTotalSteps(), message);
            updateAnalytics(operationId, operation.getTotalSteps());
        }
    }

    @Override
    public ProgressInfo getProgress(String operationId) {
        ProgressOperation operation = operations.get(operationId);
        if (operation != null) {
            return new ProgressInfo(operation.getId(), operation.getCurrentStep(), operation.getTotalSteps(),
                    operation.getStatus().name(), operation.getMessage(), operation.getLastUpdateTime(),
                    getOperationMetadata(operationId));
        }
        // Return a default placeholder when the operation is not found to respect non-null contract
        return new ProgressInfo(operationId, 0, 0, "PENDING", "", System.currentTimeMillis(), java.util.Map.of());
    }

    @Override
    public Map<String, Object> getConfiguration() {
        return new ConcurrentHashMap<>(configuration);
    }

    @Override
    public void updateConfiguration(Map<String, Object> configuration) {
        this.configuration.clear();
        this.configuration.putAll(configuration);
        persistConfiguration();
    }

    protected abstract ProgressOperation createProgressOperation(String operationId, int totalSteps);

    protected abstract void persistOperationStart(String operationId, int totalSteps);

    protected abstract void persistProgressUpdate(String operationId, int currentStep, String message);

    protected abstract void persistOperationCompletion(String operationId, String message);

    protected abstract void persistConfiguration();

    protected abstract void sendProgressNotification(String operationId, String status, int currentStep,
            String message);

    protected abstract Map<String, Object> getOperationMetadata(String operationId);

    private int getTotalSteps(String operationId) {
        ProgressOperation operation = operations.get(operationId);
        return operation != null ? operation.getTotalSteps() : 0;
    }

    private void updateAnalytics(String operationId, int currentStep) {
        logger.debug("Updated analytics for operation: {} at step {}", operationId, currentStep);
    }

    private double calculateSuccessRate() {
        int total = totalOperations.get();
        if (total == 0) {
            return 0.0;
        }
        return (double) completedOperations.get() / total * 100.0;
    }

    private double calculateAverageProcessingTime() {
        int completed = completedOperations.get();
        if (completed == 0) {
            return 0.0;
        }
        return (double) totalProcessingTime.get() / completed;
    }
}
