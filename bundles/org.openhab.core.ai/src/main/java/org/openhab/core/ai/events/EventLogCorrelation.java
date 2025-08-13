package org.openhab.core.ai.events;

import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Event-Log correlation record capturing confidence scores and type.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class EventLogCorrelation {

    private final String id;
    private final EventInfo eventInfo;
    private final LogEntry logEntry;
    private double confidence;
    private double temporalConfidence;
    private double patternConfidence;
    private double causalityConfidence;
    private final CorrelationType type;
    private final Instant createdAt;

    public EventLogCorrelation(String id, EventInfo eventInfo, LogEntry logEntry, double confidence,
            double temporalConfidence, double patternConfidence, double causalityConfidence, CorrelationType type,
            Instant createdAt) {
        this.id = id;
        this.eventInfo = eventInfo;
        this.logEntry = logEntry;
        this.confidence = confidence;
        this.temporalConfidence = temporalConfidence;
        this.patternConfidence = patternConfidence;
        this.causalityConfidence = causalityConfidence;
        this.type = type;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public EventInfo getEventInfo() {
        return eventInfo;
    }

    public LogEntry getLogEntry() {
        return logEntry;
    }

    public double getConfidence() {
        return confidence;
    }

    public double getTemporalConfidence() {
        return temporalConfidence;
    }

    public double getPatternConfidence() {
        return patternConfidence;
    }

    public double getCausalityConfidence() {
        return causalityConfidence;
    }

    public CorrelationType getType() {
        return type;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void updateConfidence(double confidence, double temporalConfidence, double patternConfidence,
            double causalityConfidence) {
        this.confidence = confidence;
        this.temporalConfidence = temporalConfidence;
        this.patternConfidence = patternConfidence;
        this.causalityConfidence = causalityConfidence;
    }

    public enum CorrelationType {
        ERROR_CORRELATION,
        WARNING_CORRELATION,
        SECURITY_CORRELATION,
        PERFORMANCE_CORRELATION,
        GENERAL_CORRELATION
    }
}


