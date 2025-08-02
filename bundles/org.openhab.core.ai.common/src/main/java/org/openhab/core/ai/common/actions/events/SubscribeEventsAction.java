package org.openhab.core.ai.common.actions.events;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.actions.events.EventSubscriptionRegistry.SubscriptionInfo;
import org.openhab.core.ai.common.api.action.AIAction;
import org.openhab.core.ai.common.api.action.AIActionContext;
import org.openhab.core.ai.common.api.action.AIActionException;
import org.openhab.core.ai.common.api.action.AIActionMetadata;
import org.openhab.core.ai.common.api.action.AIActionResult;
import org.openhab.core.ai.common.api.action.AIActionValidationResult;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action for subscribing to events in openHAB.
 * 
 * This action provides functionality to subscribe to
 * specific event types and receive notifications.
 * 
 * @author AI Assistant
 * @since 1.0.0
 */
@NonNullByDefault
public class SubscribeEventsAction implements AIAction {

    private static final Logger logger = LoggerFactory.getLogger(SubscribeEventsAction.class);
    private static final String ACTION_ID = "openhab.events.subscribe";
    private static final String ACTION_NAME = "Subscribe Events";
    private static final String DESCRIPTION = "Subscribes to events from the openHAB EventBus with filtering options via SSE";
    private static final String CATEGORY = "events";
    private static final String VERSION = "1.0.0";

    @Reference
    private @Nullable EventSubscriptionRegistry eventSubscriptionRegistry;

    @Override
    public String getActionId() {
        return ACTION_ID;
    }

    @Override
    public String getActionName() {
        return ACTION_NAME;
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    @Override
    public String getCategory() {
        return CATEGORY;
    }

    @Override
    public String getVersion() {
        return VERSION;
    }

    @Override
    public Map<String, Object> getParameterSchema() {
        Map<String, Object> schema = new java.util.HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new java.util.HashMap<>();
        properties.put("eventTypes", Map.of("type", "array", "description", "List of event types to subscribe to",
                "items", Map.of("type", "string")));
        properties.put("filters", Map.of("type", "object", "description",
                "Optional filters for event filtering (e.g., itemName, thingUID, topic, source)"));
        properties.put("clientId",
                Map.of("type", "string", "description", "MCP client identifier for the subscription"));

        schema.put("properties", properties);
        schema.put("required", List.of("eventTypes", "clientId"));
        schema.put("additionalProperties", false);
        return schema;
    }

    @Override
    public Map<String, Object> getReturnSchema() {
        Map<String, Object> schema = new java.util.HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new java.util.HashMap<>();
        properties.put("subscriptionId", Map.of("type", "string", "description", "Unique subscription identifier"));
        properties.put("sseUrl", Map.of("type", "string", "description", "SSE endpoint URL for receiving events"));
        properties.put("eventTypes", Map.of("type", "array", "description", "List of subscribed event types"));
        properties.put("filters", Map.of("type", "object", "description", "Applied filters"));
        properties.put("status", Map.of("type", "string", "description", "Subscription status"));
        properties.put("timestamp", Map.of("type", "string", "description", "Subscription creation timestamp"));

        schema.put("properties", properties);
        return schema;
    }

    @Override
    public AIActionValidationResult validateParameters(Map<String, Object> parameters) {
        List<String> errors = new java.util.ArrayList<>();

        // Validate eventTypes
        Object eventTypesObj = parameters.get("eventTypes");
        if (eventTypesObj == null) {
            errors.add("Missing required parameter: eventTypes");
        } else if (!(eventTypesObj instanceof List)) {
            errors.add("eventTypes must be a list");
        } else {
            List<?> eventTypes = (List<?>) eventTypesObj;
            if (eventTypes.isEmpty()) {
                errors.add("eventTypes cannot be empty");
            } else {
                for (Object eventType : eventTypes) {
                    if (!(eventType instanceof String)) {
                        errors.add("All eventTypes must be strings");
                        break;
                    }
                }
            }
        }

        // Validate clientId
        Object clientIdObj = parameters.get("clientId");
        if (clientIdObj == null) {
            errors.add("Missing required parameter: clientId");
        } else if (!(clientIdObj instanceof String) || ((String) clientIdObj).trim().isEmpty()) {
            errors.add("clientId must be a non-empty string");
        }

        // Validate filters (optional)
        Object filtersObj = parameters.get("filters");
        if (filtersObj != null && !(filtersObj instanceof Map)) {
            errors.add("filters must be an object");
        }

        if (!errors.isEmpty()) {
            return AIActionValidationResult.invalid(errors);
        }

        return AIActionValidationResult.valid(parameters);
    }

    @Override
    public AIActionResult execute(Map<String, Object> parameters, AIActionContext context) throws AIActionException {
        try {
            logger.debug("Executing SubscribeEventsAction with parameters: {}", parameters);

            Map<String, Object> result = subscribeToEvents(parameters, context);

            logger.debug("SubscribeEventsAction completed successfully");
            long executionTime = System.currentTimeMillis() - System.currentTimeMillis(); // Will be calculated properly
            return AIActionResult.success(result, executionTime);

        } catch (Exception e) {
            logger.error("Error executing SubscribeEventsAction: {}", e.getMessage(), e);
            throw new AIActionException(ACTION_ID, "Failed to subscribe to events: " + e.getMessage(), e);
        }
    }

    @Override
    public CompletableFuture<AIActionResult> executeAsync(Map<String, Object> parameters, AIActionContext context) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return execute(parameters, context);
            } catch (AIActionException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public AIActionMetadata getMetadata() {
        return AIActionMetadata.builder().description(DESCRIPTION).version(VERSION).build();
    }

    @Override
    public Map<String, Object> getCapabilities() {
        Map<String, Object> capabilities = new java.util.HashMap<>();
        capabilities.put("supportsAsync", true);
        capabilities.put("supportsFiltering", true);
        capabilities.put("supportsSSE", true);
        capabilities.put("maxEventTypes", 50);
        capabilities.put("maxFilters", 20);
        return capabilities;
    }

    @Override
    public void initialize(AIActionContext context) {
        logger.debug("Initializing SubscribeEventsAction for protocol: {}", context.getProtocol());
    }

    @Override
    public void cleanup() {
        logger.debug("Cleaning up SubscribeEventsAction");
    }

    @Override
    public boolean isReady() {
        return eventSubscriptionRegistry != null;
    }

    private Map<String, Object> subscribeToEvents(Map<String, Object> parameters, AIActionContext context) {
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("action", "subscribe_events");
        result.put("timestamp", java.time.Instant.now().toString());

        // Extract parameters
        @SuppressWarnings("unchecked")
        List<String> eventTypesList = (List<String>) parameters.get("eventTypes");
        String clientId = (String) parameters.get("clientId");
        @SuppressWarnings("unchecked")
        Map<String, String> filters = (Map<String, String>) parameters.get("filters");

        // Convert event types to set
        Set<String> eventTypes = new HashSet<>(eventTypesList);

        // Subscribe to events
        if (eventSubscriptionRegistry != null) {
            SubscriptionInfo subscriptionInfo = eventSubscriptionRegistry.subscribe(clientId, eventTypes, filters);

            result.put("subscriptionId", subscriptionInfo.getSubscriptionId());
            result.put("sseUrl", subscriptionInfo.getSseUrl());
            result.put("eventTypes", subscriptionInfo.getEventTypes());
            Map<String, String> subscriptionFilters = subscriptionInfo.getFilters();
            result.put("filters", subscriptionFilters != null ? subscriptionFilters : Map.of());
            result.put("status", "active");
            result.put("message", "Successfully subscribed to events");

            logger.info("Created event subscription: clientId={}, subscriptionId={}, eventTypes={}", clientId,
                    subscriptionInfo.getSubscriptionId(), eventTypes);
        } else {
            result.put("status", "error");
            result.put("message", "EventSubscriptionRegistry not available");
            logger.error("EventSubscriptionRegistry not available");
        }

        return result;
    }
}
