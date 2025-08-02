package org.openhab.core.ai.common.actions.items;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.action.AIActionContext;
import org.openhab.core.ai.common.action.AIActionMetadata;
import org.openhab.core.ai.common.action.AIActionResult;
import org.openhab.core.ai.common.action.AIActionValidationResult;
import org.openhab.core.ai.common.api.action.AIAction;
import org.openhab.core.ai.common.api.action.AIActionException;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemNotFoundException;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.persistence.FilterCriteria;
import org.openhab.core.persistence.HistoricItem;
import org.openhab.core.persistence.PersistenceService;
import org.openhab.core.persistence.PersistenceServiceRegistry;
import org.openhab.core.persistence.QueryablePersistenceService;
import org.openhab.core.types.State;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action to retrieve historical data for a specific item
 * 
 * 
 */
@Component(service = AIAction.class, immediate = true)
@NonNullByDefault
public class GetItemHistoryAction implements AIAction {

    private static final Logger logger = LoggerFactory.getLogger(GetItemHistoryAction.class);

    @Reference
    private @Nullable ItemRegistry itemRegistry;

    @Reference
    private @Nullable PersistenceServiceRegistry persistenceServiceRegistry;

    @Override
    public String getActionId() {
        return "openhab.items.get_history";
    }

    @Override
    public String getActionName() {
        return "Get Item History";
    }

    @Override
    public String getDescription() {
        return "Retrieve historical data for a specific item";
    }

    @Override
    public String getCategory() {
        return "items";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public Map<String, Object> getParameterSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();
        properties.put("itemName",
                Map.of("type", "string", "description", "The name of the item to get history for", "required", true));
        properties.put("serviceId", Map.of("type", "string", "description",
                "The persistence service ID to use (e.g., 'rrd4j', 'influxdb', 'jdbc')", "default", "rrd4j"));
        properties.put("startTime", Map.of("type", "string", "description",
                "Start time for history query (ISO 8601 format, e.g., '2023-01-01T00:00:00Z')", "default", "24h ago"));
        properties.put("endTime", Map.of("type", "string", "description",
                "End time for history query (ISO 8601 format, e.g., '2023-01-02T00:00:00Z')", "default", "now"));
        properties.put("maxResults", Map.of("type", "integer", "description",
                "Maximum number of history entries to return", "default", 100));
        properties.put("includeStateDetails", Map.of("type", "boolean", "description",
                "Include detailed state information in the response", "default", false));

        schema.put("properties", properties);
        schema.put("required", List.of("itemName"));
        return schema;
    }

    @Override
    public Map<String, Object> getReturnSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();
        properties.put("itemName", Map.of("type", "string"));
        properties.put("serviceId", Map.of("type", "string"));
        properties.put("startTime", Map.of("type", "string"));
        properties.put("endTime", Map.of("type", "string"));
        properties.put("totalEntries", Map.of("type", "integer"));
        properties.put("history", Map.of("type", "array"));
        properties.put("success", Map.of("type", "boolean"));
        properties.put("timestamp", Map.of("type", "number"));

        schema.put("properties", properties);
        return schema;
    }

    @Override
    public AIActionValidationResult validateParameters(Map<String, Object> parameters) {
        if (parameters == null) {
            return AIActionValidationResult.invalid(List.of("Parameters cannot be null"));
        }

        String itemName = (String) parameters.get("itemName");
        if (itemName == null || itemName.trim().isEmpty()) {
            return AIActionValidationResult.invalid(List.of("Item name is required and cannot be empty"));
        }

        // Validate that the item exists
        try {
            itemRegistry.getItem(itemName);
        } catch (ItemNotFoundException e) {
            return AIActionValidationResult.invalid(List.of("Item not found: " + itemName));
        } catch (Exception e) {
            return AIActionValidationResult.invalid(List.of("Error validating parameters: " + e.getMessage()));
        }

        // Validate service ID if provided and persistence registry is available
        String serviceId = (String) parameters.get("serviceId");
        if (serviceId != null && persistenceServiceRegistry != null) {
            PersistenceService service = persistenceServiceRegistry.get(serviceId);
            if (service == null) {
                return AIActionValidationResult.invalid(List.of("Persistence service not found: " + serviceId));
            }
            if (!(service instanceof QueryablePersistenceService)) {
                return AIActionValidationResult
                        .invalid(List.of("Persistence service does not support queries: " + serviceId));
            }
        }

        return AIActionValidationResult.valid(parameters);
    }

    @Override
    public AIActionResult execute(Map<String, Object> parameters, AIActionContext context) throws AIActionException {
        long executionStartTime = System.currentTimeMillis();

        try {
            String itemName = (String) parameters.get("itemName");
            String serviceId = (String) parameters.getOrDefault("serviceId", "rrd4j");
            String startTimeStr = (String) parameters.getOrDefault("startTime", "24h ago");
            String endTimeStr = (String) parameters.getOrDefault("endTime", "now");
            Integer maxResults = (Integer) parameters.getOrDefault("maxResults", 100);
            Boolean includeStateDetails = (Boolean) parameters.getOrDefault("includeStateDetails", false);

            logger.debug("Getting history for item: {} using service: {} from {} to {} (max: {})", itemName, serviceId,
                    startTimeStr, endTimeStr, maxResults);

            // Check if persistence services are available
            if (persistenceServiceRegistry == null) {
                Map<String, Object> result = new HashMap<>();
                result.put("itemName", itemName);
                result.put("serviceId", serviceId);
                result.put("startTime", startTimeStr);
                result.put("endTime", endTimeStr);
                result.put("totalEntries", 0);
                result.put("history", List.of());
                result.put("success", false);
                result.put("timestamp", System.currentTimeMillis());
                result.put("error", "Persistence service registry not available");

                long executionTime = System.currentTimeMillis() - executionStartTime;
                return AIActionResult.success(result, executionTime);
            }

            // Get the item
            Item item = itemRegistry.getItem(itemName);

            // Get the persistence service
            PersistenceService service = persistenceServiceRegistry.get(serviceId);
            if (service == null) {
                Map<String, Object> result = new HashMap<>();
                result.put("itemName", itemName);
                result.put("serviceId", serviceId);
                result.put("startTime", startTimeStr);
                result.put("endTime", endTimeStr);
                result.put("totalEntries", 0);
                result.put("history", List.of());
                result.put("success", false);
                result.put("timestamp", System.currentTimeMillis());
                result.put("error", "Persistence service not found: " + serviceId);

                long executionTime = System.currentTimeMillis() - executionStartTime;
                return AIActionResult.success(result, executionTime);
            }

            if (!(service instanceof QueryablePersistenceService queryableService)) {
                Map<String, Object> result = new HashMap<>();
                result.put("itemName", itemName);
                result.put("serviceId", serviceId);
                result.put("startTime", startTimeStr);
                result.put("endTime", endTimeStr);
                result.put("totalEntries", 0);
                result.put("history", List.of());
                result.put("success", false);
                result.put("timestamp", System.currentTimeMillis());
                result.put("error", "Persistence service does not support queries: " + serviceId);

                long executionTime = System.currentTimeMillis() - executionStartTime;
                return AIActionResult.success(result, executionTime);
            }

            // Parse time parameters
            ZonedDateTime queryStartTime = parseTimeParameter(startTimeStr);
            ZonedDateTime queryEndTime = parseTimeParameter(endTimeStr);

            // Get real item history using persistence service
            List<Map<String, Object>> historyList = getRealItemHistory(itemName, queryableService, queryStartTime,
                    queryEndTime, maxResults, includeStateDetails);

            Map<String, Object> result = new HashMap<>();
            result.put("itemName", itemName);
            result.put("serviceId", serviceId);
            result.put("startTime", startTimeStr);
            result.put("endTime", endTimeStr);
            result.put("totalEntries", historyList.size());
            result.put("history", historyList);
            result.put("success", true);
            result.put("timestamp", System.currentTimeMillis());

            long executionTime = System.currentTimeMillis() - executionStartTime;
            logger.debug("Successfully retrieved history for item: {} in {}ms", itemName, executionTime);

            return AIActionResult.success(result, executionTime);

        } catch (ItemNotFoundException e) {
            long executionTime = System.currentTimeMillis() - executionStartTime;
            logger.debug("Item not found: {}", e.getMessage());

            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", "Item not found");
            result.put("timestamp", System.currentTimeMillis());

            return AIActionResult.success(result, executionTime);

        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - executionStartTime;
            logger.error("Error getting item history", e);
            throw new AIActionException(getActionId(), "Failed to get item history: " + e.getMessage(), e);
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
        return AIActionMetadata.builder().version(getVersion()).author("openHAB")
                .description("Retrieve historical data for a specific openHAB item")
                .tags(List.of("items", "history", "persistence", "data"))
                .documentation(
                        "Retrieves historical state data for a specific openHAB item using the configured persistence service. Supports time range queries and result limiting.")
                .examples(List.of("Get last 24 hours: {\"itemName\": \"LivingRoom_Temperature\"}",
                        "Get specific time range: {\"itemName\": \"LivingRoom_Temperature\", \"startTime\": \"2023-01-01T00:00:00Z\", \"endTime\": \"2023-01-02T00:00:00Z\"}",
                        "Get with custom service: {\"itemName\": \"LivingRoom_Temperature\", \"serviceId\": \"influxdb\", \"maxResults\": 50}",
                        "Get with state details: {\"itemName\": \"LivingRoom_Temperature\", \"includeStateDetails\": true}"))
                .build();
    }

    @Override
    public Map<String, Object> getCapabilities() {
        Map<String, Object> capabilities = new HashMap<>();
        capabilities.put("async", true);
        capabilities.put("validation", true);
        capabilities.put("timeRangeQueries", true);
        capabilities.put("resultLimiting", true);
        capabilities.put("multipleServices", true);
        capabilities.put("stateDetails", true);
        return capabilities;
    }

    @Override
    public void initialize(AIActionContext context) {
        // No initialization needed
    }

    @Override
    public void cleanup() {
        // No cleanup needed
    }

    @Override
    public boolean isReady() {
        return itemRegistry != null && persistenceServiceRegistry != null;
    }

    /**
     * Parse time parameter which can be either ISO 8601 format or relative time
     */
    private ZonedDateTime parseTimeParameter(String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty()) {
            return ZonedDateTime.now();
        }

        // Handle relative time expressions
        if (timeStr.equals("now")) {
            return ZonedDateTime.now();
        }

        // Handle relative time with ago
        if (timeStr.endsWith(" ago")) {
            String amount = timeStr.substring(0, timeStr.length() - 4).trim();
            return parseRelativeTime(amount);
        }

        // Try to parse as ISO 8601
        try {
            return ZonedDateTime.parse(timeStr);
        } catch (Exception e) {
            logger.debug("Failed to parse time as ISO 8601, trying relative time: {}", timeStr);
            return parseRelativeTime(timeStr);
        }
    }

    /**
     * Parse relative time expressions like "1h", "30m", "2d", etc.
     */
    private ZonedDateTime parseRelativeTime(String amount) {
        ZonedDateTime now = ZonedDateTime.now();

        if (amount.endsWith("h")) {
            int hours = Integer.parseInt(amount.substring(0, amount.length() - 1));
            return now.minusHours(hours);
        } else if (amount.endsWith("m")) {
            int minutes = Integer.parseInt(amount.substring(0, amount.length() - 1));
            return now.minusMinutes(minutes);
        } else if (amount.endsWith("d")) {
            int days = Integer.parseInt(amount.substring(0, amount.length() - 1));
            return now.minusDays(days);
        } else if (amount.endsWith("w")) {
            int weeks = Integer.parseInt(amount.substring(0, amount.length() - 1));
            return now.minusWeeks(weeks);
        } else if (amount.endsWith("M")) {
            int months = Integer.parseInt(amount.substring(0, amount.length() - 1));
            return now.minusMonths(months);
        } else if (amount.endsWith("y")) {
            int years = Integer.parseInt(amount.substring(0, amount.length() - 1));
            return now.minusYears(years);
        } else {
            // Default to hours if no unit specified
            try {
                int hours = Integer.parseInt(amount);
                return now.minusHours(hours);
            } catch (NumberFormatException e) {
                logger.warn("Invalid time format: {}, using now", amount);
                return now;
            }
        }
    }

    /**
     * Get real item history using persistence service.
     * This method queries the persistence service for item state changes.
     */
    private List<Map<String, Object>> getRealItemHistory(String itemName, QueryablePersistenceService queryableService,
            ZonedDateTime startTime, ZonedDateTime endTime, int maxResults, boolean includeStateDetails) {

        List<Map<String, Object>> history = new ArrayList<>();

        try {
            // Create filter criteria for item state changes
            FilterCriteria filter = new FilterCriteria();
            filter.setItemName(itemName);
            filter.setBeginDate(startTime);
            filter.setEndDate(endTime);
            filter.setOrdering(FilterCriteria.Ordering.DESCENDING);

            // Query the persistence service
            Iterable<HistoricItem> historicItems = queryableService.query(filter);

            // Process the results
            for (HistoricItem historicItem : historicItems) {
                Map<String, Object> entry = new HashMap<>();
                entry.put("timestamp", historicItem.getTimestamp().toInstant().toEpochMilli());
                entry.put("historyId",
                        "hist-" + itemName + "-" + historicItem.getTimestamp().toInstant().toEpochMilli());

                // Get the state information
                State state = historicItem.getState();
                if (state != null) {
                    entry.put("state", state.toString());

                    if (includeStateDetails) {
                        entry.put("stateType", state.getClass().getSimpleName());
                        entry.put("stateValue", state.toString());

                        // Add additional state details based on type
                        if (state instanceof org.openhab.core.library.types.DecimalType) {
                            entry.put("numericValue",
                                    ((org.openhab.core.library.types.DecimalType) state).doubleValue());
                        } else if (state instanceof org.openhab.core.library.types.StringType) {
                            entry.put("stringValue", ((org.openhab.core.library.types.StringType) state).toString());
                        } else if (state instanceof org.openhab.core.library.types.OnOffType) {
                            entry.put("booleanValue",
                                    ((org.openhab.core.library.types.OnOffType) state) == org.openhab.core.library.types.OnOffType.ON);
                        }
                    }
                } else {
                    entry.put("state", "NULL");
                    if (includeStateDetails) {
                        entry.put("stateType", "NULL");
                    }
                }

                history.add(entry);

                // Limit results if maxResults is specified
                if (maxResults > 0 && history.size() >= maxResults) {
                    break;
                }
            }

            // If no real history found, create a fallback entry
            if (history.isEmpty()) {
                Map<String, Object> fallbackEntry = new HashMap<>();
                fallbackEntry.put("timestamp", System.currentTimeMillis());
                fallbackEntry.put("historyId", "hist-" + itemName + "-fallback");
                fallbackEntry.put("state", "UNKNOWN");
                fallbackEntry.put("note",
                        "No history found in persistence service. Item may not have state changes in the specified time range.");
                if (includeStateDetails) {
                    fallbackEntry.put("stateType", "UNKNOWN");
                }
                history.add(fallbackEntry);
            }

        } catch (Exception e) {
            logger.warn("Error querying persistence service for item history: {}", e.getMessage());

            // Create error entry
            Map<String, Object> errorEntry = new HashMap<>();
            errorEntry.put("timestamp", System.currentTimeMillis());
            errorEntry.put("historyId", "hist-" + itemName + "-error");
            errorEntry.put("state", "ERROR");
            errorEntry.put("error", "Failed to query persistence service: " + e.getMessage());
            if (includeStateDetails) {
                errorEntry.put("stateType", "ERROR");
            }
            history.add(errorEntry);
        }

        return history;
    }
}
