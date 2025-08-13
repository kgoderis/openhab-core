package org.openhab.core.ai.agent.lifecycle;

/**
 * Message status enumeration
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
public enum MessageStatus {
    QUEUED,
    DELIVERED,
    ACKNOWLEDGED,
    DELIVERY_FAILED,
    NO_HANDLERS,
    UNKNOWN
}
