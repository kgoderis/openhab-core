package org.openhab.core.ai.tool.progress.tracking;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Progress metrics.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ProgressMetrics {
    private final int currentStep;
    private final int totalSteps;
    private final double progressPercentage;
    private final boolean completed;
    private final long startTime;
    private final long currentTime;

    public ProgressMetrics(int currentStep, int totalSteps, double progressPercentage, boolean completed,
            long startTime, long currentTime) {
        this.currentStep = currentStep;
        this.totalSteps = totalSteps;
        this.progressPercentage = progressPercentage;
        this.completed = completed;
        this.startTime = startTime;
        this.currentTime = currentTime;
    }

    public int getCurrentStep() {
        return currentStep;
    }

    public int getTotalSteps() {
        return totalSteps;
    }

    public double getProgressPercentage() {
        return progressPercentage;
    }

    public boolean isCompleted() {
        return completed;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getCurrentTime() {
        return currentTime;
    }

    public long getElapsedTime() {
        return currentTime - startTime;
    }
}


