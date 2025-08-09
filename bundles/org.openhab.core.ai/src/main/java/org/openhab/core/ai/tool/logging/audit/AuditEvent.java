package org.openhab.core.ai.tool.logging.audit;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Audit event for tool system operations.
 * 
 * This class represents an audit event that can be logged for security
 * and compliance purposes.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class AuditEvent {

    private final String id;
    private final String level;
    private final String action;
    private final String userId;
    private final String timestamp;
    private final Map<String, Object> details;

    /**
     * Create a new audit event.
     * 
     * @param id the event ID
     * @param level the audit level
     * @param action the action being audited
     * @param userId the user ID
     * @param timestamp the event timestamp
     * @param details additional audit details
     */
    public AuditEvent(String id, String level, String action, String userId, String timestamp,
            Map<String, Object> details) {
        this.id = id;
        this.level = level;
        this.action = action;
        this.userId = userId;
        this.timestamp = timestamp;
        this.details = details;
    }

    /**
     * Get the event ID.
     * 
     * @return the event ID
     */
    public String getId() {
        return id;
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
     * @return the user ID
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Get the event timestamp.
     * 
     * @return the timestamp
     */
    public String getTimestamp() {
        return timestamp;
    }

    /**
     * Get additional audit details.
     * 
     * @return audit details
     */
    public Map<String, Object> getDetails() {
        return details;
    }

    // TODO: Implement audit event validation
    // TODO: Add support for audit event serialization
    // TODO: Implement audit event comparison
    // TODO: Add support for audit event encryption
}
