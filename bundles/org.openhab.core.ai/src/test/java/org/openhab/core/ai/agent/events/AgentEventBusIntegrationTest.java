package org.openhab.core.ai.agent.events;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for AgentEventBusIntegration
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
class AgentEventBusIntegrationTest {

    private AgentEventBusIntegration eventBusIntegration;

    @BeforeEach
    void setUp() {
        eventBusIntegration = new AgentEventBusIntegration();
    }

    @Test
    void testGetStatistics() {
        AgentEventBusIntegration.EventBusStatistics statistics = eventBusIntegration.getStatistics();

        assertNotNull(statistics);
        assertEquals(0, statistics.getTotalEventsPublished());
        assertEquals(0, statistics.getTotalEventsDelivered());
        assertEquals(0, statistics.getTotalEventsFiltered());
        assertEquals(0, statistics.getTotalEventsFailed());
        assertEquals(0, statistics.getTotalEventsInDeadLetterQueue());
        assertEquals(0, statistics.getStoredEvents());
        assertEquals(0, statistics.getActiveSubscriptions());
        assertEquals(0, statistics.getRegisteredSchemas());
        assertEquals(0, statistics.getDeadLetterQueueSize());
    }

    @Test
    void testGetSubscribedEventTypes() {
        Set<String> subscribedTypes = eventBusIntegration.getSubscribedEventTypes();

        assertNotNull(subscribedTypes);
        assertTrue(subscribedTypes.contains("org.openhab.core.ai.agent.*"));
        assertEquals(1, subscribedTypes.size());
    }

    @Test
    void testPublishEvent() throws InterruptedException, ExecutionException {
        String eventType = "test.event";
        String sourceAgentId = "test-agent";
        String payload = "test payload";
        AgentEventBusIntegration.EventOptions options = AgentEventBusIntegration.EventOptions.builder().build();

        CompletableFuture<AgentEventBusIntegration.EventPublishResult> future = eventBusIntegration
                .publishEvent(eventType, sourceAgentId, payload, options);

        AgentEventBusIntegration.EventPublishResult result = future.get();

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertTrue(result.getMessage().contains("delivered"));
    }

    @Test
    void testRetryDeadLetterEvents() throws InterruptedException, ExecutionException {
        CompletableFuture<AgentEventBusIntegration.DeadLetterQueueResult> future = eventBusIntegration
                .retryDeadLetterEvents(null, 3);

        AgentEventBusIntegration.DeadLetterQueueResult result = future.get();

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("No events to retry", result.getMessage());
        assertEquals(0, result.getSuccessfulRetries());
        assertEquals(0, result.getTotalRetries());
    }

    @Test
    void testGetEventsForReplay() {
        String eventType = "test.event";
        Instant startTime = Instant.now().minusSeconds(3600); // 1 hour ago
        Instant endTime = Instant.now().plusSeconds(3600); // 1 hour from now
        int limit = 10;

        var events = eventBusIntegration.getEventsForReplay(eventType, startTime, endTime, limit);

        assertNotNull(events);
        assertTrue(events.isEmpty());
    }

    @Test
    void testRegisterEventSchema() {
        String eventType = "test.event";
        var requiredFields = Map.of("field1", "string", "field2", "integer");
        var optionalFields = Map.of("field3", "boolean");
        String version = "1.0.0";

        AgentEventBusIntegration.EventSchema schema = new AgentEventBusIntegration.EventSchema(eventType,
                requiredFields, optionalFields, version);

        eventBusIntegration.registerEventSchema(eventType, schema);

        // Verify schema was registered by checking statistics
        var statistics = eventBusIntegration.getStatistics();
        assertEquals(1, statistics.getRegisteredSchemas());
    }

    @Test
    void testRegisterEventFilter() {
        String filterId = "test-filter";
        AgentEventBusIntegration.EventFilter filter = event -> true; // Always allow events

        eventBusIntegration.registerEventFilter(filterId, filter);

        // No direct way to verify filter registration, but it should not throw an exception
        assertDoesNotThrow(() -> eventBusIntegration.registerEventFilter(filterId, filter));
    }

    @Test
    void testRegisterEventRouter() {
        String routerId = "test-router";
        AgentEventBusIntegration.EventRouter router = event -> CompletableFuture
                .completedFuture(AgentEventBusIntegration.EventPublishResult.success("Test routing"));

        eventBusIntegration.registerEventRouter(routerId, router);

        // No direct way to verify router registration, but it should not throw an exception
        assertDoesNotThrow(() -> eventBusIntegration.registerEventRouter(routerId, router));
    }
}
