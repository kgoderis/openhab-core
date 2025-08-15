package org.openhab.core.ai.reasoning.actions;

import java.time.Instant;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Autonomous Action DTO extracted from AutonomousEventProcessor.
 */
@NonNullByDefault
public class AutonomousAction {
    private final String id;
    private final String description;
    private final String type;
    private final double confidence;
    private final Map<String, Object> parameters;
    private final String agentId;
    private final Instant timestamp;
    private boolean overridden;
    private @Nullable String overrideReason;
    private @Nullable Instant overrideTimestamp;

    public AutonomousAction(String id, String description, String type, double confidence,
            Map<String, Object> parameters, String agentId) {
        this.id = id;
        this.description = description;
        this.type = type;
        this.confidence = confidence;
        this.parameters = parameters;
        this.agentId = agentId;
        this.timestamp = Instant.now();
        this.overridden = false;
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public String getType() {
        return type;
    }

    public double getConfidence() {
        return confidence;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public String getAgentId() {
        return agentId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public boolean isOverridden() {
        return overridden;
    }

    public void setOverridden(boolean overridden) {
        this.overridden = overridden;
    }

    public @Nullable String getOverrideReason() {
        return overrideReason;
    }

    public void setOverrideReason(@Nullable String overrideReason) {
        this.overrideReason = overrideReason;
    }

    public @Nullable Instant getOverrideTimestamp() {
        return overrideTimestamp;
    }

    public void setOverrideTimestamp(@Nullable Instant overrideTimestamp) {
        this.overrideTimestamp = overrideTimestamp;
    }
}
