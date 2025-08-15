package org.openhab.core.ai.agent.execution;

import java.time.Duration;
import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

@NonNullByDefault
public final class CompositionMetric {
    private final String compositionId;
    private final boolean success;
    private final Duration duration;
    private final @Nullable String message;
    private final Instant timestamp;

    public CompositionMetric(String compositionId, boolean success, Duration duration, @Nullable String message,
            Instant timestamp) {
        this.compositionId = compositionId;
        this.success = success;
        this.duration = duration;
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getCompositionId() {
        return compositionId;
    }

    public boolean isSuccess() {
        return success;
    }

    public Duration getDuration() {
        return duration;
    }

    public @Nullable String getMessage() {
        return message;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}
