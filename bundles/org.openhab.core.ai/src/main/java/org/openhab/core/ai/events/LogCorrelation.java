package org.openhab.core.ai.events;

import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Correlation between a log entry and a system event/type with confidence.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public class LogCorrelation {
    private final String id;
    private final LogEntry logEntry;
    private final @Nullable Object systemEvent;
    private final String type;
    private final double confidence;
    private final Instant timestamp;

    public LogCorrelation(String id, LogEntry logEntry, @Nullable Object systemEvent, String type, double confidence,
            Instant timestamp) {
        this.id = id;
        this.logEntry = logEntry;
        this.systemEvent = systemEvent;
        this.type = type;
        this.confidence = confidence;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public LogEntry getLogEntry() {
        return logEntry;
    }

    public @Nullable Object getSystemEvent() {
        return systemEvent;
    }

    public String getType() {
        return type;
    }

    public double getConfidence() {
        return confidence;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}
