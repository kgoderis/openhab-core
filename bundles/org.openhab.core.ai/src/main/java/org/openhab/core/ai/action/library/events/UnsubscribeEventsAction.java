package org.openhab.core.ai.action.library.events;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.action.ActionContext;
import org.openhab.core.ai.action.ActionMetadata;
import org.openhab.core.ai.action.ActionResult;
import org.openhab.core.ai.action.ActionValidationResult;
import org.openhab.core.ai.action.api.Action;
import org.openhab.core.ai.action.api.ActionException;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action for unsubscribing from events in openHAB.
 * 
 * This action provides functionality to unsubscribe from
 * event subscriptions and stop receiving notifications.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class UnsubscribeEventsAction implements Action {

    private static final Logger logger = LoggerFactory.getLogger(UnsubscribeEventsAction.class);
    private static final String ACTION_ID = "openhab.events.unsubscribe";
    private static final String ACTION_NAME = "Unsubscribe Events";
    private static final String DESCRIPTION = "Unsubscribes from events from the openHAB EventBus";
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
        properties.put("subscriptionId", Map.of("type", "string", "description", "ID of the subscription to remove"));
        properties.put("clientId",
                Map.of("type", "string", "description", "MCP client identifier (optional, for validation)"));

        schema.put("properties", properties);
        schema.put("required", List.of("subscriptionId"));
        schema.put("additionalProperties", false);
        return schema;
    }

    @Override
    public Map<String, Object> getReturnSchema() {
        Map<String, Object> schema = new java.util.HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new java.util.HashMap<>();
        properties.put("subscriptionId", Map.of("type", "string", "description", "ID of the removed subscription"));
        properties.put("status", Map.of("type", "string", "description", "Status of the unsubscription"));
        properties.put("message", Map.of("type", "string", "description", "Result message"));
        properties.put("timestamp", Map.of("type", "string", "description", "Unsubscription timestamp"));

        schema.put("properties", properties);
        return schema;
    }

    @Override
    public ActionValidationResult validateParameters(Map<String, Object> parameters) {
        List<String> errors = new java.util.ArrayList<>();

        // Validate subscriptionId
        Object subscriptionIdObj = parameters.get("subscriptionId");
        if (subscriptionIdObj == null) {
            errors.add("Missing required parameter: subscriptionId");
        } else if (!(subscriptionIdObj instanceof String) || ((String) subscriptionIdObj).trim().isEmpty()) {
            errors.add("subscriptionId must be a non-empty string");
        }

        // Validate clientId (optional)
        Object clientIdObj = parameters.get("clientId");
        if (clientIdObj != null && !(clientIdObj instanceof String)) {
            errors.add("clientId must be a string");
        }

        if (!errors.isEmpty()) {
            return ActionValidationResult.invalid(errors);
        }

        return ActionValidationResult.valid(parameters);
    }

    @Override
    public ActionResult execute(Map<String, Object> parameters, ActionContext context) throws ActionException {
        try {
            logger.debug("Executing UnsubscribeEventsAction with parameters: {}", parameters);

            Map<String, Object> result = unsubscribeFromEvents(parameters, context);

            logger.debug("UnsubscribeEventsAction completed successfully");
            long executionTime = System.currentTimeMillis() - System.currentTimeMillis(); // Will be calculated properly
            return ActionResult.success(result, executionTime);

        } catch (Exception e) {
            logger.error("Error executing UnsubscribeEventsAction: {}", e.getMessage(), e);
            throw new ActionException(ACTION_ID, "Failed to unsubscribe from events: " + e.getMessage(), e);
        }
    }

    @Override
    public CompletableFuture<ActionResult> executeAsync(Map<String, Object> parameters, ActionContext context) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return execute(parameters, context);
            } catch (ActionException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public ActionMetadata getMetadata() {
        return ActionMetadata.builder().description(DESCRIPTION).version(VERSION).build();
    }

    @Override
    public Map<String, Object> getCapabilities() {
        Map<String, Object> capabilities = new java.util.HashMap<>();
        capabilities.put("supportsAsync", true);
        capabilities.put("supportsValidation", true);
        capabilities.put("supportsCleanup", true);
        return capabilities;
    }

    @Override
    public void initialize(ActionContext context) {
        logger.debug("Initializing UnsubscribeEventsAction for protocol: {}", context.getProtocol());
    }

    @Override
    public void cleanup() {
        logger.debug("Cleaning up UnsubscribeEventsAction");
    }

    @Override
    public boolean isReady() {
        return eventSubscriptionRegistry != null;
    }

    private Map<String, Object> unsubscribeFromEvents(Map<String, Object> parameters, ActionContext context) {
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("action", "unsubscribe_events");
        result.put("timestamp", java.time.Instant.now().toString());

        // Extract parameters
        String subscriptionId = (String) parameters.get("subscriptionId");
        String clientId = (String) parameters.get("clientId");

        // Unsubscribe from events
        if (eventSubscriptionRegistry != null) {
            // Validate subscription exists
            if (!eventSubscriptionRegistry.hasSubscription(subscriptionId)) {
                result.put("subscriptionId", subscriptionId);
                result.put("status", "error");
                result.put("message", "Subscription not found: " + subscriptionId);
                logger.warn("Attempted to unsubscribe from non-existent subscription: {}", subscriptionId);
                return result;
            }

            // Optional client validation
            if (clientId != null) {
                EventSubscriptionRegistry.SubscriptionInfo info = eventSubscriptionRegistry
                        .getSubscription(subscriptionId);
                if (info != null && !clientId.equals(info.getClientId())) {
                    result.put("subscriptionId", subscriptionId);
                    result.put("status", "error");
                    result.put("message", "Client ID mismatch for subscription: " + subscriptionId);
                    logger.warn("Client ID mismatch for subscription: expected={}, actual={}", clientId,
                            info.getClientId());
                    return result;
                }
            }

            // Remove subscription
            boolean removed = eventSubscriptionRegistry.unsubscribe(subscriptionId);

            if (removed) {
                result.put("subscriptionId", subscriptionId);
                result.put("status", "success");
                result.put("message", "Successfully unsubscribed from events");

                logger.info("Removed event subscription: subscriptionId={}", subscriptionId);
            } else {
                result.put("subscriptionId", subscriptionId);
                result.put("status", "error");
                result.put("message", "Failed to remove subscription: " + subscriptionId);

                logger.error("Failed to remove event subscription: {}", subscriptionId);
            }
        } else {
            result.put("subscriptionId", subscriptionId);
            result.put("status", "error");
            result.put("message", "EventSubscriptionRegistry not available");
            logger.error("EventSubscriptionRegistry not available");
        }

        return result;
    }
}
