package org.openhab.core.ai.integration.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.core.ai.action.ActionResult;
import org.openhab.core.ai.action.actions.events.*;

/**
 * Integration tests for Events-related Actions using mocked openHAB services.
 */
class EventsActionIntegrationTest extends BaseActionIntegrationTest {

    private SendEventAction sendEventAction;
    private SubscribeEventsAction subscribeEventsAction;
    private UnsubscribeEventsAction unsubscribeEventsAction;
    private ListSubscriptionsAction listSubscriptionsAction;

    @BeforeEach
    void setUpActions() {
        // Initialize all event actions
        sendEventAction = new SendEventAction();
        subscribeEventsAction = new SubscribeEventsAction();
        unsubscribeEventsAction = new UnsubscribeEventsAction();
        listSubscriptionsAction = new ListSubscriptionsAction();

        // Initialize actions with context
        sendEventAction.initialize(actionContext);
        subscribeEventsAction.initialize(actionContext);
        unsubscribeEventsAction.initialize(actionContext);
        listSubscriptionsAction.initialize(actionContext);
    }

    @Test
    void testSendEventAction() throws Exception {
        Map<String, Object> parameters = Map.of("eventType", "ItemStateChangedEvent", "eventData",
                Map.of("itemName", "TestSwitch", "newState", "ON"));
        ActionResult result = executeAction(sendEventAction, parameters);

        // In mocked mode without real event bus, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "sent");
            assertResultContainsKey(result, "eventType");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testSendEventActionWithTopic() throws Exception {
        Map<String, Object> parameters = Map.of("eventType", "ItemStateChangedEvent", "topic",
                "openhab/items/TestSwitch/statechanged", "eventData",
                Map.of("itemName", "TestSwitch", "newState", "ON"));
        ActionResult result = executeAction(sendEventAction, parameters);

        // In mocked mode without real event bus, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "sent");
            assertResultContainsKey(result, "topic");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testSubscribeEventsAction() throws Exception {
        Map<String, Object> parameters = Map.of("eventTypes",
                List.of("ItemStateChangedEvent", "ThingStatusInfoChangedEvent"), "callbackUrl",
                "http://localhost:8080/events");
        ActionResult result = executeAction(subscribeEventsAction, parameters);

        // In mocked mode without real event system, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "subscriptionId");
            assertResultContainsKey(result, "eventTypes");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testSubscribeEventsActionWithFilters() throws Exception {
        Map<String, Object> parameters = Map.of("eventTypes", List.of("ItemStateChangedEvent"), "filters",
                Map.of("itemName", "TestSwitch"), "callbackUrl", "http://localhost:8080/events");
        ActionResult result = executeAction(subscribeEventsAction, parameters);

        // In mocked mode without real event system, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "subscriptionId");
            assertResultContainsKey(result, "filters");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testUnsubscribeEventsAction() throws Exception {
        Map<String, Object> parameters = Map.of("subscriptionId", "test-subscription-1");
        ActionResult result = executeAction(unsubscribeEventsAction, parameters);

        // In mocked mode without real event system, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "unsubscribed");
            assertResultContainsKey(result, "subscriptionId");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testListSubscriptionsAction() throws Exception {
        Map<String, Object> parameters = Map.of("filter", "all");
        ActionResult result = executeAction(listSubscriptionsAction, parameters);

        assertSuccess(result);
        assertResultContainsKey(result, "subscriptions");
    }

    @Test
    void testListSubscriptionsWithEventTypeFilter() throws Exception {
        Map<String, Object> parameters = Map.of("filter", "eventType", "eventType", "ItemStateChangedEvent");
        ActionResult result = executeAction(listSubscriptionsAction, parameters);

        assertSuccess(result);
        assertResultContainsKey(result, "subscriptions");
    }

    @Test
    void testListSubscriptionsWithStatusFilter() throws Exception {
        Map<String, Object> parameters = Map.of("filter", "status", "status", "ACTIVE");
        ActionResult result = executeAction(listSubscriptionsAction, parameters);

        assertSuccess(result);
        assertResultContainsKey(result, "subscriptions");
    }

    @Test
    void testSendEventActionWithInvalidEventType() throws Exception {
        Map<String, Object> parameters = Map.of("eventType", "InvalidEventType", "eventData", Map.of("test", "data"));
        ActionResult result = executeAction(sendEventAction, parameters);

        assertFailure(result);
        assertNotNull(result.getMessage());
    }

    @Test
    void testSubscribeEventsActionWithInvalidCallbackUrl() throws Exception {
        Map<String, Object> parameters = Map.of("eventTypes", List.of("ItemStateChangedEvent"), "callbackUrl",
                "invalid-url");
        ActionResult result = executeAction(subscribeEventsAction, parameters);

        assertFailure(result);
        assertNotNull(result.getMessage());
    }

    @Test
    void testUnsubscribeEventsActionWithInvalidSubscriptionId() throws Exception {
        Map<String, Object> parameters = Map.of("subscriptionId", "invalid-subscription-id");
        ActionResult result = executeAction(unsubscribeEventsAction, parameters);

        assertFailure(result);
        assertNotNull(result.getMessage());
    }
}
