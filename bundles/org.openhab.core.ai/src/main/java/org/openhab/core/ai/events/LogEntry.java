package org.openhab.core.ai.events;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Structured log entry used by the log ingestion pipeline.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public class LogEntry {
    private final String id;
    private final LocalDateTime timestamp;
    private final String loggerName;
    private final LogIngestionPipeline.LogLevel level;
    private final String message;
    private final Map<String, Object> metadata;
    private final Instant processedAt;

    public LogEntry(String id, LocalDateTime timestamp, String loggerName, LogIngestionPipeline.LogLevel level,
            String message, Map<String, Object> metadata, Instant processedAt) {
        this.id = id;
        this.timestamp = timestamp;
        this.loggerName = loggerName;
        this.level = level;
        this.message = message;
        this.metadata = metadata;
        this.processedAt = processedAt;
    }

    public String getId() { return id; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getLoggerName() { return loggerName; }
    public LogIngestionPipeline.LogLevel getLevel() { return level; }
    public String getMessage() { return message; }
    public Map<String, Object> getMetadata() { return metadata; }
    public Instant getProcessedAt() { return processedAt; }
}


