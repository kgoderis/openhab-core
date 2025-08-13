package org.openhab.core.ai.agent.communication.events;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Event filter contract.
 *
 * Determines whether an event should be delivered to a subscription.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface EventFilter {
    boolean shouldDeliver(AgentEvent event);
}
