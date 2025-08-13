package org.openhab.core.ai.agent.communication.events;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Event schema definition.
 *
 * Describes required and optional fields for a given event type.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class EventSchema {
    private final String eventType;
    private final Map<String, String> requiredFields;
    private final Map<String, String> optionalFields;
    private final String version;

    public EventSchema(String eventType, Map<String, String> requiredFields, Map<String, String> optionalFields,
            String version) {
        this.eventType = eventType;
        this.requiredFields = requiredFields;
        this.optionalFields = optionalFields;
        this.version = version;
    }

    public String getEventType() { return eventType; }
    public Map<String, String> getRequiredFields() { return requiredFields; }
    public Map<String, String> getOptionalFields() { return optionalFields; }
    public String getVersion() { return version; }
}
