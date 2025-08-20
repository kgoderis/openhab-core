package org.openhab.core.ai.common.audit;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Represents an audit event for logging security and operational activities.
 * 
 * This class encapsulates all the information needed to log an audit event
 * including the event type, user information, timestamps, and additional details.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class AuditEvent {

    private final String eventId;
    private final String eventType;
    private final String level;
    private final String action;
    private final @Nullable String userId;
    private final @Nullable String principalId;
    private final @Nullable String protocol;
    private final Instant timestamp;
    private final Map<String, Object> details;
    private final @Nullable String description;

    /**
     * Create a new AuditEvent.
     * 
     * @param eventId Unique event identifier
     * @param eventType Type of event
     * @param level Audit level
     * @param action Action being audited
     * @param userId User identifier
     * @param principalId Principal identifier
     * @param protocol Protocol name
     * @param timestamp When the event occurred
     * @param details Additional event details
     * @param description Event description
     */
    public AuditEvent(String eventId, String eventType, String level, String action, @Nullable String userId,
            @Nullable String principalId, @Nullable String protocol, Instant timestamp, Map<String, Object> details,
            @Nullable String description) {
        this.eventId = Objects.requireNonNull(eventId, "eventId");
        this.eventType = Objects.requireNonNull(eventType, "eventType");
        this.level = Objects.requireNonNull(level, "level");
        this.action = Objects.requireNonNull(action, "action");
        this.userId = userId;
        this.principalId = principalId;
        this.protocol = protocol;
        this.timestamp = Objects.requireNonNull(timestamp, "timestamp");
        this.details = Objects.requireNonNull(details, "details");
        this.description = description;
    }

    /**
     * Get the event ID.
     * 
     * @return the event ID
     */
    public String getEventId() {
        return eventId;
    }

    /**
     * Get the event type.
     * 
     * @return the event type
     */
    public String getEventType() {
        return eventType;
    }

    /**
     * Get the audit level.
     * 
     * @return the audit level
     */
    public String getLevel() {
        return level;
    }

    /**
     * Get the action being audited.
     * 
     * @return the action
     */
    public String getAction() {
        return action;
    }

    /**
     * Get the user ID.
     * 
     * @return the user ID, or null if not available
     */
    public @Nullable String getUserId() {
        return userId;
    }

    /**
     * Get the principal ID.
     * 
     * @return the principal ID, or null if not available
     */
    public @Nullable String getPrincipalId() {
        return principalId;
    }

    /**
     * Get the protocol name.
     * 
     * @return the protocol name, or null if not available
     */
    public @Nullable String getProtocol() {
        return protocol;
    }

    /**
     * Get the event timestamp.
     * 
     * @return the timestamp
     */
    public Instant getTimestamp() {
        return timestamp;
    }

    /**
     * Get the event details.
     * 
     * @return the event details
     */
    public Map<String, Object> getDetails() {
        return details;
    }

    /**
     * Get the event description.
     * 
     * @return the event description, or null if not available
     */
    public @Nullable String getDescription() {
        return description;
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId, eventType, level, action, userId, principalId, protocol, timestamp, details,
                description);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        AuditEvent other = (AuditEvent) obj;
        return Objects.equals(eventId, other.eventId) && Objects.equals(eventType, other.eventType)
                && Objects.equals(level, other.level) && Objects.equals(action, other.action)
                && Objects.equals(userId, other.userId) && Objects.equals(principalId, other.principalId)
                && Objects.equals(protocol, other.protocol) && Objects.equals(timestamp, other.timestamp)
                && Objects.equals(details, other.details) && Objects.equals(description, other.description);
    }

    @Override
    public String toString() {
        return "AuditEvent [eventId=" + eventId + ", eventType=" + eventType + ", level=" + level + ", action=" + action
                + ", userId=" + userId + ", principalId=" + principalId + ", protocol=" + protocol + ", timestamp="
                + timestamp + ", details=" + details + ", description=" + description + "]";
    }
}
