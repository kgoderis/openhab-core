package org.openhab.core.ai.reasoning.input;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Reasoning input record with prioritization and quality metrics.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ReasoningInput implements Comparable<ReasoningInput> {
    // enums extracted to top-level: ReasoningInputType, ReasoningInputStatus

    private final String id;
    private final ReasoningInputType type;
    private final String content;
    private final Instant timestamp;
    private final Map<String, Object> context;
    private final int priority;
    private double quality;
    private ReasoningInputStatus status;
    private Instant processedAt;

    public ReasoningInput(String id, ReasoningInputType type, String content, Instant timestamp,
            Map<String, Object> context, int priority) {
        this.id = id;
        this.type = type;
        this.content = content;
        this.timestamp = timestamp;
        this.context = new HashMap<>(context);
        this.priority = priority;
        this.quality = 1.0;
        this.status = ReasoningInputStatus.PENDING;
    }

    public String getId() {
        return id;
    }

    public ReasoningInputType getType() {
        return type;
    }

    public String getContent() {
        return content;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public Map<String, Object> getContext() {
        return context;
    }

    public int getPriority() {
        return priority;
    }

    public double getQuality() {
        return quality;
    }

    public void setQuality(double quality) {
        this.quality = quality;
    }

    public ReasoningInputStatus getStatus() {
        return status;
    }

    public void setStatus(ReasoningInputStatus status) {
        this.status = status;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(Instant processedAt) {
        this.processedAt = processedAt;
    }

    public void setContext(Map<String, Object> context) {
        this.context.clear();
        this.context.putAll(context);
    }

    @Override
    public int compareTo(ReasoningInput other) {
        int priorityCompare = Integer.compare(other.priority, this.priority);
        if (priorityCompare != 0) {
            return priorityCompare;
        }

        int qualityCompare = Double.compare(other.quality, this.quality);
        if (qualityCompare != 0) {
            return qualityCompare;
        }

        return this.timestamp.compareTo(other.timestamp);
    }
}
