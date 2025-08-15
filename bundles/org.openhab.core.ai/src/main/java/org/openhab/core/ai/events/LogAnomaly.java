package org.openhab.core.ai.events;

import java.time.Instant;
import java.time.LocalDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Represents an anomaly detected from logs, with severity and context.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public class LogAnomaly {
    // enum extracted to top-level: org.openhab.core.ai.events.LogAnomalySeverity

    private final String id;
    private final LogEntry logEntry;
    private final String type;
    private final LogAnomalySeverity severity;
    private final LocalDateTime timestamp;
    private final Instant detectedAt;

    public LogAnomaly(String id, LogEntry logEntry, String type, LogAnomalySeverity severity, LocalDateTime timestamp,
            Instant detectedAt) {
        this.id = id;
        this.logEntry = logEntry;
        this.type = type;
        this.severity = severity;
        this.timestamp = timestamp;
        this.detectedAt = detectedAt;
    }

    public String getId() {
        return id;
    }

    public LogEntry getLogEntry() {
        return logEntry;
    }

    public String getType() {
        return type;
    }

    public LogAnomalySeverity getSeverity() {
        return severity;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public Instant getDetectedAt() {
        return detectedAt;
    }
}
