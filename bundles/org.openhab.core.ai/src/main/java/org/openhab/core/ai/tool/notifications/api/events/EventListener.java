package org.openhab.core.ai.tool.notifications.api.events;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Interface for MCP Event listener.
 * 
 * This interface defines the contract for receiving events
 * from the event service.
 * 
 * @author Karel Goderis - Initial Contribution
 */
@NonNullByDefault
public interface EventListener {

    /**
     * Get the listener ID.
     * 
     * @return the listener ID
     */
    String getId();

    /**
     * Handle an event.
     * 
     * @param event the event to handle
     * @return true if event was handled successfully
     */
    boolean onEvent(SystemEvent event);
}
