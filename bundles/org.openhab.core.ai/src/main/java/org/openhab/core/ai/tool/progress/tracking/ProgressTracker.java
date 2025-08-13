package org.openhab.core.ai.tool.progress.tracking;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tracker for monitoring tool operation progress.
 * 
 * This interface defines the contract for progress trackers that can monitor
 * and report the progress of long-running tool operations.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface ProgressTracker {

    /**
     * Get the tracker ID.
     * 
     * @return the tracker ID
     */
    String getTrackerId();

    /**
     * Get the tracker name.
     * 
     * @return the tracker name
     */
    String getTrackerName();

    /**
     * Get the tracker description.
     * 
     * @return the tracker description
     */
    String getTrackerDescription();

    /**
     * Start tracking progress for an operation.
     * 
     * @param operationId the operation ID
     * @param totalSteps the total number of steps
     * @return the progress tracker instance
     */
    ProgressTracker startTracking(String operationId, int totalSteps);

    /**
     * Update the progress for an operation.
     * 
     * @param operationId the operation ID
     * @param currentStep the current step
     * @param message the progress message
     */
    void updateProgress(String operationId, int currentStep, String message);

    /**
     * Complete the progress tracking for an operation.
     * 
     * @param operationId the operation ID
     * @param message the completion message
     */
    void completeProgress(String operationId, String message);

    /**
     * Get the current progress for an operation.
     * 
     * @param operationId the operation ID
     * @return the current progress
     */
    ProgressInfo getProgress(String operationId);

    /**
     * Get the tracker configuration.
     * 
     * @return the tracker configuration
     */
    Map<String, Object> getConfiguration();

    /**
     * Update the tracker configuration.
     * 
     * @param configuration the new configuration
     */
    void updateConfiguration(Map<String, Object> configuration);

    /**
     * Default implementation of progress tracking logic.
     */
    abstract class DefaultProgressTracker implements ProgressTracker {

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

            // Persist operation start
            persistOperationStart(operationId, totalSteps);

            // Send notification
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

                // Persist progress update
                persistProgressUpdate(operationId, currentStep, message);

                // Send notification
                sendProgressNotification(operationId, "IN_PROGRESS", currentStep, message);

                // Update analytics
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

                // Persist completion
                persistOperationCompletion(operationId, message);

                // Send notification
                sendProgressNotification(operationId, "COMPLETED", operation.getTotalSteps(), message);

                // Update analytics
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
            return null;
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

        /**
         * Get analytics data for this tracker.
         * 
         * @return analytics data
         */
        public Map<String, Object> getAnalytics() {
            Map<String, Object> analytics = new ConcurrentHashMap<>();
            analytics.put("totalOperations", totalOperations.get());
            analytics.put("completedOperations", completedOperations.get());
            analytics.put("failedOperations", failedOperations.get());
            analytics.put("successRate", calculateSuccessRate());
            analytics.put("averageProcessingTime", calculateAverageProcessingTime());
            analytics.put("lastError", lastError.get());
            return analytics;
        }

        // Abstract methods to be implemented by concrete classes
        protected abstract ProgressOperation createProgressOperation(String operationId, int totalSteps);

        protected abstract void persistOperationStart(String operationId, int totalSteps);

        protected abstract void persistProgressUpdate(String operationId, int currentStep, String message);

        protected abstract void persistOperationCompletion(String operationId, String message);

        protected abstract void persistConfiguration();

        protected abstract void sendProgressNotification(String operationId, String status, int currentStep,
                String message);

        protected abstract Map<String, Object> getOperationMetadata(String operationId);

        // Helper methods
        private int getTotalSteps(String operationId) {
            ProgressOperation operation = operations.get(operationId);
            return operation != null ? operation.getTotalSteps() : 0;
        }

        private void updateAnalytics(String operationId, int currentStep) {
            // Update real-time analytics
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
}
