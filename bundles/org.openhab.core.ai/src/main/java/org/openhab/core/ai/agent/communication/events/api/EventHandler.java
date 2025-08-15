package org.openhab.core.ai.agent.communication.events.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.agent.communication.events.AgentEvent;

/**
 * Event handler contract.
 *
 * Processes delivered events for a subscription.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface EventHandler {
    void handleEvent(AgentEvent event);
}
