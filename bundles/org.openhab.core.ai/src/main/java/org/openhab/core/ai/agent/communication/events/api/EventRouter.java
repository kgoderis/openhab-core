package org.openhab.core.ai.agent.communication.events.api;

import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Event router contract.
 *
 * Routes {@link AgentEvent} instances to appropriate subscribers or processing
 * pipelines and returns an {@link EventPublishResult} indicating the outcome.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface EventRouter {
    CompletableFuture<EventPublishResult> routeEvent(AgentEvent event);
}
