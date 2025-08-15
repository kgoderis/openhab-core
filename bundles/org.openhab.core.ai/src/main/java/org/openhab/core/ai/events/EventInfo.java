package org.openhab.core.ai.events;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Event information model used for event-log correlation processing.
 *
 * <p>
 * Encapsulates core event metadata and a data map for arbitrary attributes.
 * </p>
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class EventInfo {

    private final String id;
    private final String type;
    private final String source;
    private final Instant timestamp;
    private final Map<String, Object> data;

    public EventInfo(String id, String type, String source, Instant timestamp, Map<String, Object> data) {
        this.id = id;
        this.type = type;
        this.source = source;
        this.timestamp = timestamp;
        this.data = new HashMap<>(data);
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getSource() {
        return source;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public Map<String, Object> getData() {
        return data;
    }
}
