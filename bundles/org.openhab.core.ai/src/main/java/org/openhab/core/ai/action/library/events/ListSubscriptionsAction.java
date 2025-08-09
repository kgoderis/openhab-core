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
import org.openhab.core.ai.action.library.events.EventSubscriptionRegistry.SubscriptionInfo;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action for listing active event subscriptions for MCP clients.
 * 
 * This action provides functionality to:
 * - List all active subscriptions for a client
 * - Get subscription details and metadata
 * - Monitor subscription status
 * - Validate subscription information
 */
@Component(service = Action.class, immediate = true)
@NonNullByDefault
public class ListSubscriptionsAction implements Action {

    private static final Logger logger = LoggerFactory.getLogger(ListSubscriptionsAction.class);
    private static final String ACTION_ID = "openhab.events.list_subscriptions";
    private static final String ACTION_NAME = "List Subscriptions";
    private static final String DESCRIPTION = "Lists active event subscriptions for MCP clients";
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
        properties.put("clientId", Map.of("type", "string", "description", "MCP client identifier"));
        properties.put("includeDetails",
                Map.of("type", "boolean", "description", "Include detailed subscription information", "default", true));

        schema.put("properties", properties);
        schema.put("required", List.of("clientId"));
        schema.put("additionalProperties", false);
        return schema;
    }

    @Override
    public Map<String, Object> getReturnSchema() {
        Map<String, Object> schema = new java.util.HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new java.util.HashMap<>();
        properties.put("clientId", Map.of("type", "string", "description", "MCP client identifier"));
        properties.put("subscriptions", Map.of("type", "array", "description", "List of active subscriptions"));
        properties.put("totalCount", Map.of("type", "integer", "description", "Total number of subscriptions"));
        properties.put("status", Map.of("type", "string", "description", "Status of the operation"));
        properties.put("timestamp", Map.of("type", "string", "description", "Operation timestamp"));

        schema.put("properties", properties);
        return schema;
    }

    @Override
    public ActionValidationResult validateParameters(Map<String, Object> parameters) {
        List<String> errors = new java.util.ArrayList<>();

        // Validate clientId
        Object clientIdObj = parameters.get("clientId");
        if (clientIdObj == null) {
            errors.add("Missing required parameter: clientId");
        } else if (!(clientIdObj instanceof String) || ((String) clientIdObj).trim().isEmpty()) {
            errors.add("clientId must be a non-empty string");
        }

        // Validate includeDetails (optional)
        Object includeDetailsObj = parameters.get("includeDetails");
        if (includeDetailsObj != null && !(includeDetailsObj instanceof Boolean)) {
            errors.add("includeDetails must be a boolean");
        }

        if (!errors.isEmpty()) {
            return ActionValidationResult.invalid(errors);
        }

        return ActionValidationResult.valid(parameters);
    }

    @Override
    public ActionResult execute(Map<String, Object> parameters, ActionContext context) throws ActionException {
        try {
            logger.debug("Executing ListSubscriptionsAction with parameters: {}", parameters);

            Map<String, Object> result = listSubscriptions(parameters, context);

            logger.debug("ListSubscriptionsAction completed successfully");
            long executionTime = System.currentTimeMillis() - System.currentTimeMillis(); // Will be calculated properly
            return ActionResult.success(result, executionTime);

        } catch (Exception e) {
            logger.error("Error executing ListSubscriptionsAction: {}", e.getMessage(), e);
            throw new ActionException(ACTION_ID, "Failed to list subscriptions: " + e.getMessage(), e);
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
        capabilities.put("supportsFiltering", false);
        capabilities.put("supportsPagination", false);
        return capabilities;
    }

    @Override
    public void initialize(ActionContext context) {
        logger.debug("Initializing ListSubscriptionsAction for protocol: {}", context.getProtocol());
    }

    @Override
    public void cleanup() {
        logger.debug("Cleaning up ListSubscriptionsAction");
    }

    @Override
    public boolean isReady() {
        return eventSubscriptionRegistry != null;
    }

    private Map<String, Object> listSubscriptions(Map<String, Object> parameters, ActionContext context) {
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("action", "list_subscriptions");
        result.put("timestamp", java.time.Instant.now().toString());

        // Extract parameters
        String clientId = (String) parameters.get("clientId");
        boolean includeDetails = (Boolean) parameters.getOrDefault("includeDetails", true);

        // List subscriptions
        if (eventSubscriptionRegistry != null) {
            List<SubscriptionInfo> subscriptions = eventSubscriptionRegistry.listSubscriptions(clientId);

            result.put("clientId", clientId);
            result.put("totalCount", subscriptions.size());
            result.put("status", "success");

            if (includeDetails) {
                // Convert SubscriptionInfo objects to maps for JSON serialization
                List<Map<String, Object>> subscriptionDetails = subscriptions.stream()
                        .map(this::convertSubscriptionInfoToMap).toList();
                result.put("subscriptions", subscriptionDetails);
            } else {
                // Just return subscription IDs
                List<String> subscriptionIds = subscriptions.stream().map(SubscriptionInfo::getSubscriptionId).toList();
                result.put("subscriptions", subscriptionIds);
            }

            logger.info("Listed {} subscriptions for client: {}", subscriptions.size(), clientId);
        } else {
            result.put("clientId", clientId);
            result.put("totalCount", 0);
            result.put("subscriptions", List.of());
            result.put("status", "error");
            result.put("message", "EventSubscriptionRegistry not available");
            logger.error("EventSubscriptionRegistry not available");
        }

        return result;
    }

    private Map<String, Object> convertSubscriptionInfoToMap(SubscriptionInfo info) {
        Map<String, Object> map = new java.util.HashMap<>();
        map.put("subscriptionId", info.getSubscriptionId());
        map.put("clientId", info.getClientId());
        map.put("eventTypes", info.getEventTypes());
        Map<String, String> filters = info.getFilters();
        map.put("filters", filters != null ? filters : Map.of());
        map.put("sseUrl", info.getSseUrl());
        map.put("createdAt", info.getCreatedAt());
        return map;
    }
}
