package org.openhab.core.ai.events;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Performance metrics for event-log correlation engine.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class CorrelationPerformanceMetrics {

    private final long totalCorrelationsCreated;
    private final long totalCorrelationsValidated;
    private final long totalProcessingTime;
    private final int correlationCount;
    private final int eventCorrelationCount;
    private final int logCorrelationCount;
    private final int patternCount;

    public CorrelationPerformanceMetrics(long totalCorrelationsCreated, long totalCorrelationsValidated,
            long totalProcessingTime, int correlationCount, int eventCorrelationCount, int logCorrelationCount,
            int patternCount) {
        this.totalCorrelationsCreated = totalCorrelationsCreated;
        this.totalCorrelationsValidated = totalCorrelationsValidated;
        this.totalProcessingTime = totalProcessingTime;
        this.correlationCount = correlationCount;
        this.eventCorrelationCount = eventCorrelationCount;
        this.logCorrelationCount = logCorrelationCount;
        this.patternCount = patternCount;
    }

    public long getTotalCorrelationsCreated() {
        return totalCorrelationsCreated;
    }

    public long getTotalCorrelationsValidated() {
        return totalCorrelationsValidated;
    }

    public long getTotalProcessingTime() {
        return totalProcessingTime;
    }

    public int getCorrelationCount() {
        return correlationCount;
    }

    public int getEventCorrelationCount() {
        return eventCorrelationCount;
    }

    public int getLogCorrelationCount() {
        return logCorrelationCount;
    }

    public int getPatternCount() {
        return patternCount;
    }
}
