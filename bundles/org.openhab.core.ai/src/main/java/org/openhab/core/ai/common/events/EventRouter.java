package org.openhab.core.ai.common.events;

import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.agent.communication.events.AgentEvent;
import org.openhab.core.ai.agent.communication.events.api.EventPublishResult;
import org.openhab.core.events.Event;

/**
 * Unified Event Router - Comprehensive event routing system
 * 
 * This interface provides comprehensive event routing capabilities including:
 * - Basic event routing for openHAB Events
 * - Asynchronous event routing for AgentEvents with result tracking
 * - Event routing to appropriate destinations based on routing rules
 * - Event publish result tracking for agent communication
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface EventRouter {

    /**
     * Route an openHAB Event to appropriate destinations
     * 
     * @param event The openHAB Event to route
     */
    default void routeEvent(Event event) {
        // Default implementation does nothing
    }

    /**
     * Route an AgentEvent to appropriate subscribers or processing pipelines
     * 
     * @param event The AgentEvent to route
     * @return CompletableFuture containing the EventPublishResult indicating the outcome
     */
    default CompletableFuture<EventPublishResult> routeEvent(AgentEvent event) {
        // Default implementation returns a completed future with success result
        return CompletableFuture.completedFuture(EventPublishResult.success("Event routed successfully"));
    }
}
