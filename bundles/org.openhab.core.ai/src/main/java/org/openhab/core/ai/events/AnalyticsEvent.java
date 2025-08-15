package org.openhab.core.ai.events;

import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Analytics event emitted by the event analytics pipeline.
 *
 * Immutable event with type, message, optional data, and timestamp.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class AnalyticsEvent {
    private final String id;
    private final AnalyticsEventType type;
    private final String message;
    private final @Nullable Object data;
    private final Instant timestamp;

    public AnalyticsEvent(String id, AnalyticsEventType type, String message, @Nullable Object data,
            Instant timestamp) {
        this.id = id;
        this.type = type;
        this.message = message;
        this.data = data;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public AnalyticsEventType getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public @Nullable Object getData() {
        return data;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}
