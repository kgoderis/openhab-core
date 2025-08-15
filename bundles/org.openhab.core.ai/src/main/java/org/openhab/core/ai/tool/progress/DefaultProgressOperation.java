package org.openhab.core.ai.tool.progress;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.tool.progress.api.tracking.ProgressOperation;
import org.openhab.core.ai.tool.progress.api.tracking.ProgressStatus;

/**
 * Default progress operation implementation extracted from DefaultProgressTrackingService.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
final class DefaultProgressOperation implements ProgressOperation {
    private final String id;
    private final String description;
    private final int totalSteps;
    private ProgressStatus status;
    private int currentStep;
    private String message;
    private final long startTime;
    private long lastUpdateTime;
    private long completionTime;

    DefaultProgressOperation(String id, String description, int totalSteps, ProgressStatus status, long startTime) {
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
