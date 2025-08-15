package org.openhab.core.ai.tool.notifications.api.events;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Event type enumeration for MCP Utilities.
 * 
 * Defines the different types of events that can be sent.
 * 
 * @author Karel Goderis - Initial Contribution
 */
@NonNullByDefault
public enum EventType {
    INFO,
    WARNING,
    ERROR,
    SUCCESS,
    DEBUG,
    SYSTEM,
    TOOL_EXECUTION,
    SECURITY_ALERT,
    PERFORMANCE_WARNING
}
