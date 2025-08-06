package org.openhab.core.ai.events;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.events.Event;

/**
 * Event Router - Event routing interface
 * 
 * This interface provides event routing capabilities to direct events
 * to appropriate destinations based on routing rules.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface EventRouter {

    /**
     * Route an event to appropriate destinations
     * 
     * @param event The event to route
     */
    void routeEvent(Event event);
}
