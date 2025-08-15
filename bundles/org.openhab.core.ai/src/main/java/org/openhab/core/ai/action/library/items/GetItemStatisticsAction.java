package org.openhab.core.ai.action.library.items;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.action.api.Action;
import org.openhab.core.ai.action.api.ActionContext;
import org.openhab.core.ai.action.api.ActionException;
import org.openhab.core.ai.action.api.ActionMetadata;
import org.openhab.core.ai.action.api.ActionResult;
import org.openhab.core.ai.action.api.ActionValidationResult;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemNotFoundException;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.persistence.FilterCriteria;
import org.openhab.core.persistence.HistoricItem;
import org.openhab.core.persistence.PersistenceService;
import org.openhab.core.persistence.PersistenceServiceRegistry;
import org.openhab.core.persistence.QueryablePersistenceService;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action for getting item statistics in openHAB.
 * 
 * This action provides functionality to retrieve
 * statistical information about items.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class GetItemStatisticsAction implements Action {

    private static final Logger logger = LoggerFactory.getLogger(GetItemStatisticsAction.class);

    @Reference
    private @Nullable ItemRegistry itemRegistry;

    @Reference
    private @Nullable PersistenceServiceRegistry persistenceServiceRegistry;

    @Override
    public String getActionId() {
        return "openhab.items.get_statistics";
    }

    @Override
    public String getActionName() {
        return "Get Item Statistics";
    }

    @Override
    public String getDescription() {
        return "Retrieve statistical information about items";
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
        properties.put("itemName", Map.of("type", "string", "description", "The name of the item to get statistics for",
                "required", true));
        properties.put("serviceId", Map.of("type", "string", "description",
                "The persistence service ID to use for historical data", "default", "rrd4j"));
        properties.put("timeRange", Map.of("type", "string", "description",
                "Time range for statistics (e.g., '24h', '7d', '30d', '1y')", "default", "24h"));
        properties.put("includeStateBreakdown", Map.of("type", "boolean", "description",
                "Include breakdown of states and their frequencies", "default", true));
        properties.put("includeChangePatterns", Map.of("type", "boolean", "description",
                "Include patterns of state changes over time", "default", false));
        properties.put("includeUsageMetrics", Map.of("type", "boolean", "description",
                "Include usage metrics like average time in states", "default", true));

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
        properties.put("timeRange", Map.of("type", "string"));
        properties.put("totalStateChanges", Map.of("type", "integer"));
        properties.put("averageChangesPerHour", Map.of("type", "number"));
        properties.put("mostFrequentState", Map.of("type", "string"));
        properties.put("currentState", Map.of("type", "string"));
        properties.put("stateBreakdown", Map.of("type", "object"));
        properties.put("usageMetrics", Map.of("type", "object"));
        properties.put("changePatterns", Map.of("type", "object"));
        properties.put("success", Map.of("type", "boolean"));
        properties.put("timestamp", Map.of("type", "number"));

        schema.put("properties", properties);
        return schema;
    }

    @Override
    public ActionValidationResult validateParameters(Map<String, Object> parameters) {
        if (parameters == null) {
            return ActionValidationResult.invalid(List.of("Parameters cannot be null"));
        }

        String itemName = (String) parameters.get("itemName");
        if (itemName == null || itemName.trim().isEmpty()) {
            return ActionValidationResult.invalid(List.of("Item name is required and cannot be empty"));
        }

        // Validate that the item exists
        try {
            itemRegistry.getItem(itemName);
        } catch (ItemNotFoundException e) {
            return ActionValidationResult.invalid(List.of("Item not found: " + itemName));
        } catch (Exception e) {
            return ActionValidationResult.invalid(List.of("Error validating parameters: " + e.getMessage()));
        }

        // Validate time range format
        String timeRange = (String) parameters.getOrDefault("timeRange", "24h");
        if (!isValidTimeRange(timeRange)) {
            return ActionValidationResult.invalid(List.of("Invalid time range format: " + timeRange));
        }

        return ActionValidationResult.valid(parameters);
    }

    @Override
    public ActionResult execute(Map<String, Object> parameters, ActionContext context) throws ActionException {
        long executionStartTime = System.currentTimeMillis();

        try {
            String itemName = (String) parameters.get("itemName");
            String serviceId = (String) parameters.getOrDefault("serviceId", "rrd4j");
            String timeRange = (String) parameters.getOrDefault("timeRange", "24h");
            Boolean includeStateBreakdown = (Boolean) parameters.getOrDefault("includeStateBreakdown", true);
            Boolean includeChangePatterns = (Boolean) parameters.getOrDefault("includeChangePatterns", false);
            Boolean includeUsageMetrics = (Boolean) parameters.getOrDefault("includeUsageMetrics", true);

            logger.debug("Getting statistics for item: {} over time range: {}", itemName, timeRange);

            // Get the item
            Item item = itemRegistry.getItem(itemName);
            String currentState = item.getState() != null ? item.getState().toString() : "NULL";

            // Calculate time range
            ZonedDateTime endTime = ZonedDateTime.now();
            ZonedDateTime startTime = parseTimeRange(timeRange, endTime);
            Duration duration = Duration.between(startTime, endTime);

            Map<String, Object> result = new HashMap<>();
            result.put("itemName", itemName);
            result.put("timeRange", timeRange);
            result.put("currentState", currentState);
            result.put("success", true);
            result.put("timestamp", System.currentTimeMillis());

            // Check if persistence services are available for historical data
            if (persistenceServiceRegistry != null) {
                PersistenceService service = persistenceServiceRegistry.get(serviceId);
                if (service instanceof QueryablePersistenceService queryableService) {
                    // Implement real persistence API integration with ItemHistoryDTO
                    Map<String, Object> statistics = calculateAdvancedStatistics(item, duration, queryableService);
                    result.putAll(statistics);
                } else {
                    // Fallback to basic statistics without historical data
                    Map<String, Object> basicStats = calculateBasicStatistics(item, duration);
                    result.putAll(basicStats);
                    result.put("note", "Persistence service not available, using basic statistics");
                }
            } else {
                // Fallback to basic statistics without persistence
                Map<String, Object> basicStats = calculateBasicStatistics(item, duration);
                result.putAll(basicStats);
                result.put("note", "Persistence service registry not available, using basic statistics");
            }

            long executionTime = System.currentTimeMillis() - executionStartTime;
            logger.debug("Successfully retrieved statistics for item: {} in {}ms", itemName, executionTime);

            return ActionResult.success(result, executionTime);

        } catch (ItemNotFoundException e) {
            long executionTime = System.currentTimeMillis() - executionStartTime;
            logger.debug("Item not found: {}", e.getMessage());

            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", "Item not found");
            result.put("timestamp", System.currentTimeMillis());

            return ActionResult.success(result, executionTime);

        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - executionStartTime;
            logger.error("Error getting item statistics", e);
            throw new ActionException(getActionId(), "Failed to get item statistics: " + e.getMessage(), e);
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
        return ActionMetadata.builder().version(getVersion()).author("openHAB")
                .description("Retrieve statistical information about openHAB items")
                .tags(List.of("items", "statistics", "analytics", "metrics"))
                .documentation(
                        "Retrieves comprehensive statistical information about openHAB items including state change patterns, usage metrics, and frequency analysis.")
                .examples(List.of("Get basic statistics: {\"itemName\": \"LivingRoom_Light\"}",
                        "Get detailed statistics: {\"itemName\": \"LivingRoom_Temperature\", \"timeRange\": \"7d\", \"includeStateBreakdown\": true, \"includeUsageMetrics\": true}",
                        "Get change patterns: {\"itemName\": \"LivingRoom_Light\", \"includeChangePatterns\": true}"))
                .build();
    }

    @Override
    public Map<String, Object> getCapabilities() {
        Map<String, Object> capabilities = new HashMap<>();
        capabilities.put("async", true);
        capabilities.put("validation", true);
        capabilities.put("timeRangeAnalysis", true);
        capabilities.put("stateBreakdown", true);
        capabilities.put("usageMetrics", true);
        capabilities.put("changePatterns", true);
        capabilities.put("historicalData", true);
        return capabilities;
    }

    @Override
    public void initialize(ActionContext context) {
        // No initialization needed
    }

    @Override
    public void cleanup() {
        // No cleanup needed
    }

    @Override
    public boolean isReady() {
        return itemRegistry != null;
    }

    /**
     * Calculate basic statistics without historical data
     */
    private Map<String, Object> calculateBasicStatistics(Item item, Duration duration) {
        Map<String, Object> statistics = new HashMap<>();

        statistics.put("totalStateChanges", 0);
        statistics.put("averageChangesPerHour", 0.0);
        statistics.put("mostFrequentState", item.getState() != null ? item.getState().toString() : "NULL");

        Map<String, Object> usageMetrics = new HashMap<>();
        usageMetrics.put("currentState", item.getState() != null ? item.getState().toString() : "NULL");
        usageMetrics.put("itemType", item.getType());
        usageMetrics.put("lastStateChange",
                item.getLastStateChange() != null ? item.getLastStateChange().toString() : "NULL");
        statistics.put("usageMetrics", usageMetrics);

        return statistics;
    }

    /**
     * Parse time range string to calculate start time
     */
    private ZonedDateTime parseTimeRange(String timeRange, ZonedDateTime endTime) {
        if (timeRange.endsWith("h")) {
            int hours = Integer.parseInt(timeRange.substring(0, timeRange.length() - 1));
            return endTime.minusHours(hours);
        } else if (timeRange.endsWith("d")) {
            int days = Integer.parseInt(timeRange.substring(0, timeRange.length() - 1));
            return endTime.minusDays(days);
        } else if (timeRange.endsWith("w")) {
            int weeks = Integer.parseInt(timeRange.substring(0, timeRange.length() - 1));
            return endTime.minusWeeks(weeks);
        } else if (timeRange.endsWith("M")) {
            int months = Integer.parseInt(timeRange.substring(0, timeRange.length() - 1));
            return endTime.minusMonths(months);
        } else if (timeRange.endsWith("y")) {
            int years = Integer.parseInt(timeRange.substring(0, timeRange.length() - 1));
            return endTime.minusYears(years);
        } else {
            // Default to 24 hours
            return endTime.minusHours(24);
        }
    }

    /**
     * Validate time range format
     */
    private boolean isValidTimeRange(String timeRange) {
        if (timeRange == null || timeRange.trim().isEmpty()) {
            return false;
        }

        try {
            if (timeRange.endsWith("h") || timeRange.endsWith("d") || timeRange.endsWith("w") || timeRange.endsWith("M")
                    || timeRange.endsWith("y")) {
                String number = timeRange.substring(0, timeRange.length() - 1);
                int value = Integer.parseInt(number);
                return value > 0;
            }
            return false;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Calculate advanced statistics using persistence service
     */
    private Map<String, Object> calculateAdvancedStatistics(Item item, Duration duration,
            QueryablePersistenceService service) {
        Map<String, Object> statistics = new HashMap<>();

        try {
            ZonedDateTime endTime = ZonedDateTime.now();
            ZonedDateTime startTime = endTime.minus(duration);

            // Query historical data
            FilterCriteria filter = new FilterCriteria();
            filter.setItemName(item.getName());
            filter.setBeginDate(startTime);
            filter.setEndDate(endTime);
            filter.setOrdering(FilterCriteria.Ordering.DESCENDING);

            Iterable<HistoricItem> historicItems = service.query(filter);

            if (historicItems != null) {
                List<HistoricItem> itemsList = new ArrayList<>();
                for (HistoricItem historicItem : historicItems) {
                    itemsList.add(historicItem);
                }

                if (!itemsList.isEmpty()) {
                    // Calculate advanced statistics
                    statistics.put("totalStateChanges", itemsList.size());
                    statistics.put("firstStateChange",
                            itemsList.get(itemsList.size() - 1).getTimestamp().toInstant().toEpochMilli());
                    statistics.put("lastStateChange", itemsList.get(0).getTimestamp().toInstant().toEpochMilli());

                    // Calculate state breakdown
                    Map<String, Integer> stateBreakdown = new HashMap<>();
                    for (HistoricItem historicItem : itemsList) {
                        String state = historicItem.getState().toString();
                        stateBreakdown.put(state, stateBreakdown.getOrDefault(state, 0) + 1);
                    }
                    statistics.put("stateBreakdown", stateBreakdown);

                    // Calculate change frequency
                    long timeSpan = endTime.toInstant().toEpochMilli() - startTime.toInstant().toEpochMilli();
                    double changesPerHour = (itemsList.size() * 3600000.0) / timeSpan;
                    statistics.put("changesPerHour", Math.round(changesPerHour * 100.0) / 100.0);

                    // Calculate average time between changes
                    if (itemsList.size() > 1) {
                        long totalTimeBetweenChanges = 0;
                        for (int i = 0; i < itemsList.size() - 1; i++) {
                            long timeDiff = itemsList.get(i).getTimestamp().toInstant().toEpochMilli()
                                    - itemsList.get(i + 1).getTimestamp().toInstant().toEpochMilli();
                            totalTimeBetweenChanges += timeDiff;
                        }
                        double avgTimeBetweenChanges = totalTimeBetweenChanges / (itemsList.size() - 1.0);
                        statistics.put("averageTimeBetweenChangesMs", avgTimeBetweenChanges);
                    }

                    // Calculate most common state
                    String mostCommonState = stateBreakdown.entrySet().stream().max(Map.Entry.comparingByValue())
                            .map(Map.Entry::getKey).orElse("unknown");
                    statistics.put("mostCommonState", mostCommonState);

                    // Calculate state change patterns
                    Map<String, Integer> changePatterns = new HashMap<>();
                    for (int i = 0; i < itemsList.size() - 1; i++) {
                        String fromState = itemsList.get(i + 1).getState().toString();
                        String toState = itemsList.get(i).getState().toString();
                        String pattern = fromState + " -> " + toState;
                        changePatterns.put(pattern, changePatterns.getOrDefault(pattern, 0) + 1);
                    }
                    statistics.put("changePatterns", changePatterns);

                    // Calculate usage metrics
                    long totalActiveTime = calculateTotalActiveTime(itemsList, startTime, endTime);
                    statistics.put("totalActiveTimeMs", totalActiveTime);
                    statistics.put("activeTimePercentage", (totalActiveTime * 100.0) / timeSpan);

                } else {
                    // No historical data available
                    statistics.put("totalStateChanges", 0);
                    statistics.put("note", "No historical data available for the specified time range");
                }
            }

        } catch (Exception e) {
            logger.debug("Error calculating advanced statistics for item {}: {}", item.getName(), e.getMessage());
            // Fallback to basic statistics
            statistics.putAll(calculateBasicStatistics(item, duration));
            statistics.put("note", "Error calculating advanced statistics, using basic statistics");
        }

        return statistics;
    }

    /**
     * Calculate total active time based on state changes
     */
    private long calculateTotalActiveTime(List<HistoricItem> items, ZonedDateTime startTime, ZonedDateTime endTime) {
        long totalActiveTime = 0;

        for (int i = 0; i < items.size(); i++) {
            HistoricItem item = items.get(i);
            long itemTime = item.getTimestamp().toInstant().toEpochMilli();

            // Check if the state indicates "active" (customize based on item type)
            String state = item.getState().toString().toLowerCase();
            boolean isActive = state.equals("on") || state.equals("open") || state.equals("active")
                    || state.equals("true") || !state.equals("off") && !state.equals("closed")
                            && !state.equals("inactive") && !state.equals("false");

            if (isActive) {
                // Calculate duration of this active state
                long nextTime = (i < items.size() - 1) ? items.get(i + 1).getTimestamp().toInstant().toEpochMilli()
                        : endTime.toInstant().toEpochMilli();
                totalActiveTime += (nextTime - itemTime);
            }
        }

        return totalActiveTime;
    }
}
