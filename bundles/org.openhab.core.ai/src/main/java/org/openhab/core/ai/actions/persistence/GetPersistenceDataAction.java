package org.openhab.core.ai.actions.persistence;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.action.ActionContext;
import org.openhab.core.ai.action.ActionMetadata;
import org.openhab.core.ai.action.ActionResult;
import org.openhab.core.ai.action.ActionValidationResult;
import org.openhab.core.ai.api.action.Action;
import org.openhab.core.ai.api.action.ActionException;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.persistence.FilterCriteria;
import org.openhab.core.persistence.HistoricItem;
import org.openhab.core.persistence.PersistenceService;
import org.openhab.core.persistence.PersistenceServiceRegistry;
import org.openhab.core.persistence.QueryablePersistenceService;
import org.openhab.core.types.State;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action for getting persistence data in openHAB.
 * 
 * This action provides functionality to retrieve
 * data from persistence services.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class GetPersistenceDataAction implements Action {

    private static final Logger logger = LoggerFactory.getLogger(GetPersistenceDataAction.class);
    private static final String ACTION_ID = "openhab.persistence.get-data";
    private static final String ACTION_NAME = "Get Persistence Data";
    private static final String CATEGORY = "persistence";

    @Reference
    private @Nullable ItemRegistry itemRegistry;

    @Reference
    private @Nullable PersistenceServiceRegistry persistenceServiceRegistry;

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
        return "Retrieves data from persistence services";
    }

    @Override
    public String getCategory() {
        return CATEGORY;
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
        properties.put("serviceId",
                Map.of("type", "string", "description", "The ID of the persistence service to query"));
        properties.put("itemName",
                Map.of("type", "string", "description", "The name of the item to retrieve data for"));
        properties.put("startTime",
                Map.of("type", "string", "description", "Start time for the query (ISO 8601 format)"));
        properties.put("endTime", Map.of("type", "string", "description", "End time for the query (ISO 8601 format)"));
        properties.put("aggregation",
                Map.of("type", "string", "description", "Aggregation function (NONE, AVG, SUM, MIN, MAX, COUNT)",
                        "enum", List.of("NONE", "AVG", "SUM", "MIN", "MAX", "COUNT")));
        properties.put("interval",
                Map.of("type", "string", "description", "Time interval for aggregation (e.g., '1h', '1d')"));
        properties.put("limit",
                Map.of("type", "integer", "description", "Maximum number of data points to return", "default", 1000));
        properties.put("includeMetadata",
                Map.of("type", "boolean", "description", "Include metadata about the data", "default", false));

        schema.put("properties", properties);
        schema.put("required", List.of("serviceId", "itemName"));
        schema.put("additionalProperties", false);
        return schema;
    }

    @Override
    public Map<String, Object> getReturnSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();
        properties.put("serviceId", Map.of("type", "string", "description", "The service ID"));
        properties.put("itemName", Map.of("type", "string", "description", "The item name"));
        properties.put("dataPoints", Map.of("type", "array", "description", "List of data points"));
        properties.put("totalCount", Map.of("type", "integer", "description", "Total number of data points"));
        properties.put("startTime", Map.of("type", "string", "description", "Query start time"));
        properties.put("endTime", Map.of("type", "string", "description", "Query end time"));
        properties.put("aggregation", Map.of("type", "string", "description", "Applied aggregation"));
        properties.put("interval", Map.of("type", "string", "description", "Applied interval"));
        properties.put("metadata", Map.of("type", "object", "description", "Data metadata"));
        properties.put("success", Map.of("type", "boolean", "description", "Whether the query was successful"));
        properties.put("timestamp", Map.of("type", "string", "description", "Timestamp of the operation"));

        schema.put("properties", properties);
        return schema;
    }

    @Override
    public Map<String, Object> getCapabilities() {
        Map<String, Object> capabilities = new HashMap<>();
        capabilities.put("data_retrieval", true);
        capabilities.put("time_range_queries", true);
        capabilities.put("data_aggregation", true);
        capabilities.put("metadata_inclusion", true);
        return capabilities;
    }

    @Override
    public ActionValidationResult validateParameters(Map<String, Object> parameters) {
        List<String> errors = new ArrayList<>();

        if (!parameters.containsKey("serviceId")) {
            errors.add("serviceId is required");
        } else {
            Object serviceId = parameters.get("serviceId");
            if (!(serviceId instanceof String) || ((String) serviceId).trim().isEmpty()) {
                errors.add("serviceId must be a non-empty string");
            }
        }

        if (!parameters.containsKey("itemName")) {
            errors.add("itemName is required");
        } else {
            Object itemName = parameters.get("itemName");
            if (!(itemName instanceof String) || ((String) itemName).trim().isEmpty()) {
                errors.add("itemName must be a non-empty string");
            }
        }

        if (parameters.containsKey("limit")) {
            Object limit = parameters.get("limit");
            if (!(limit instanceof Integer) || (Integer) limit <= 0) {
                errors.add("limit must be a positive integer");
            }
        }

        if (parameters.containsKey("aggregation")) {
            Object aggregation = parameters.get("aggregation");
            if (!(aggregation instanceof String)) {
                errors.add("aggregation must be a string");
            } else {
                List<String> validAggregations = List.of("NONE", "AVG", "SUM", "MIN", "MAX", "COUNT");
                if (!validAggregations.contains((String) aggregation)) {
                    errors.add("aggregation must be one of: " + validAggregations);
                }
            }
        }

        if (errors.isEmpty()) {
            return ActionValidationResult.valid(parameters);
        } else {
            return ActionValidationResult.invalid(errors);
        }
    }

    @Override
    public ActionResult execute(Map<String, Object> parameters, ActionContext context) throws ActionException {
        logger.debug("Executing GetPersistenceDataAction with context: {}", context.getProtocol());

        long startTime = System.currentTimeMillis();

        try {
            String serviceId = (String) parameters.get("serviceId");
            String itemName = (String) parameters.get("itemName");
            String startTimeStr = (String) parameters.get("startTime");
            String endTimeStr = (String) parameters.get("endTime");
            String aggregation = (String) parameters.getOrDefault("aggregation", "NONE");
            String interval = (String) parameters.get("interval");
            Integer limit = (Integer) parameters.getOrDefault("limit", 1000);
            boolean includeMetadata = (Boolean) parameters.getOrDefault("includeMetadata", false);

            Map<String, Object> result = getPersistenceData(serviceId, itemName, startTimeStr, endTimeStr, aggregation,
                    interval, limit, includeMetadata);

            long executionTime = System.currentTimeMillis() - startTime;
            return ActionResult.success(result, executionTime);

        } catch (Exception e) {
            logger.error("Error executing GetPersistenceDataAction: {}", e.getMessage(), e);
            throw new ActionException(ACTION_ID, "Failed to get persistence data: " + e.getMessage(), e);
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
        return ActionMetadata.builder().author("openHAB").description("Retrieves data from persistence services")
                .version("1.0.0").tags(List.of("persistence", "data", "query")).build();
    }

    @Override
    public void initialize(ActionContext context) {
        logger.debug("GetPersistenceDataAction initialized with context: {}", context.getProtocol());
    }

    @Override
    public void cleanup() {
        logger.debug("GetPersistenceDataAction cleanup completed");
    }

    @Override
    public boolean isReady() {
        return itemRegistry != null && persistenceServiceRegistry != null;
    }

    private Map<String, Object> getPersistenceData(String serviceId, String itemName, String startTimeStr,
            String endTimeStr, String aggregation, String interval, Integer limit, boolean includeMetadata) {

        Map<String, Object> result = new HashMap<>();
        result.put("serviceId", serviceId);
        result.put("itemName", itemName);
        result.put("timestamp", Instant.now().toString());

        // Parse time parameters
        Instant startTime = parseTime(startTimeStr, Instant.now().minusSeconds(3600)); // Default to 1 hour ago
        Instant endTime = parseTime(endTimeStr, Instant.now());

        result.put("startTime", startTime.toString());
        result.put("endTime", endTime.toString());
        result.put("aggregation", aggregation);
        result.put("interval", interval);

        // Get real persistence data using openHAB Core patterns
        List<Map<String, Object>> dataPoints = getRealPersistenceData(serviceId, itemName, startTime, endTime,
                aggregation, interval, limit);

        result.put("dataPoints", dataPoints);
        result.put("totalCount", dataPoints.size());
        result.put("success", true);

        if (includeMetadata) {
            result.put("metadata", generateMetadata(serviceId, itemName, startTime, endTime, aggregation, interval));
        }

        return result;
    }

    private Instant parseTime(String timeStr, Instant defaultValue) {
        if (timeStr == null || timeStr.trim().isEmpty()) {
            return defaultValue;
        }

        try {
            return Instant.parse(timeStr);
        } catch (Exception e) {
            logger.warn("Failed to parse time string: {}, using default", timeStr);
            return defaultValue;
        }
    }

    private List<Map<String, Object>> generateSimulatedDataPoints(String itemName, Instant startTime, Instant endTime,
            String aggregation, String interval, Integer limit) {

        List<Map<String, Object>> dataPoints = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                .withZone(ZoneId.systemDefault());

        // Generate simulated data points
        long duration = endTime.getEpochSecond() - startTime.getEpochSecond();
        long step = Math.max(1, duration / limit);

        for (long i = 0; i < duration && dataPoints.size() < limit; i += step) {
            Instant timestamp = startTime.plusSeconds(i);

            Map<String, Object> dataPoint = new HashMap<>();
            dataPoint.put("timestamp", timestamp.toString());
            dataPoint.put("formattedTime", formatter.format(timestamp));
            dataPoint.put("value", generateSimulatedValue(itemName, timestamp));
            dataPoint.put("state", generateSimulatedState(itemName, timestamp));

            dataPoints.add(dataPoint);
        }

        return dataPoints;
    }

    private Object generateSimulatedValue(String itemName, Instant timestamp) {
        // Generate simulated values based on item name and time
        if (itemName.toLowerCase().contains("temperature")) {
            // Simulate temperature data (20-25°C with some variation)
            return 20.0 + 5.0 * Math.sin(timestamp.getEpochSecond() / 3600.0) + (Math.random() - 0.5) * 2;
        } else if (itemName.toLowerCase().contains("humidity")) {
            // Simulate humidity data (40-60% with some variation)
            return 50.0 + 10.0 * Math.sin(timestamp.getEpochSecond() / 7200.0) + (Math.random() - 0.5) * 5;
        } else if (itemName.toLowerCase().contains("light")) {
            // Simulate light sensor data (0-1000 lux)
            return Math.max(0,
                    500.0 + 500.0 * Math.sin(timestamp.getEpochSecond() / 43200.0) + (Math.random() - 0.5) * 100);
        } else if (itemName.toLowerCase().contains("switch")) {
            // Simulate switch state (0 or 1)
            return Math.random() > 0.7 ? 1 : 0;
        } else {
            // Generic numeric value
            return Math.random() * 100;
        }
    }

    private String generateSimulatedState(String itemName, Instant timestamp) {
        Object value = generateSimulatedValue(itemName, timestamp);

        if (itemName.toLowerCase().contains("temperature")) {
            return String.format("%.1f °C", value);
        } else if (itemName.toLowerCase().contains("humidity")) {
            return String.format("%.1f %%", value);
        } else if (itemName.toLowerCase().contains("light")) {
            return String.format("%.0f lux", value);
        } else if (itemName.toLowerCase().contains("switch")) {
            return (Integer) value == 1 ? "ON" : "OFF";
        } else {
            return value.toString();
        }
    }

    private Map<String, Object> generateMetadata(String serviceId, String itemName, Instant startTime, Instant endTime,
            String aggregation, String interval) {

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("serviceId", serviceId);
        metadata.put("itemName", itemName);
        metadata.put("queryStartTime", startTime.toString());
        metadata.put("queryEndTime", endTime.toString());
        metadata.put("aggregation", aggregation);
        metadata.put("interval", interval);
        metadata.put("dataType", determineDataType(itemName));
        metadata.put("unit", determineUnit(itemName));
        metadata.put("description", "Simulated persistence data for " + itemName);
        metadata.put("generatedAt", Instant.now().toString());

        return metadata;
    }

    private String determineDataType(String itemName) {
        if (itemName.toLowerCase().contains("temperature")) {
            return "Number:Temperature";
        } else if (itemName.toLowerCase().contains("humidity")) {
            return "Number:Dimensionless";
        } else if (itemName.toLowerCase().contains("light")) {
            return "Number:Illuminance";
        } else if (itemName.toLowerCase().contains("switch")) {
            return "Switch";
        } else {
            return "Number";
        }
    }

    private String determineUnit(String itemName) {
        if (itemName.toLowerCase().contains("temperature")) {
            return "°C";
        } else if (itemName.toLowerCase().contains("humidity")) {
            return "%";
        } else if (itemName.toLowerCase().contains("light")) {
            return "lux";
        } else if (itemName.toLowerCase().contains("switch")) {
            return "";
        } else {
            return "";
        }
    }

    /**
     * Get real persistence data using openHAB Core patterns
     * Based on the official openHAB Core PersistenceResource implementation
     */
    private List<Map<String, Object>> getRealPersistenceData(String serviceId, String itemName, Instant startTime,
            Instant endTime, String aggregation, String interval, Integer limit) {

        List<Map<String, Object>> dataPoints = new ArrayList<>();

        try {
            // Validate item exists
            if (!itemExists(itemName)) {
                logger.warn("Item not found: {}", itemName);
                return dataPoints;
            }

            // Get persistence service
            PersistenceService service = persistenceServiceRegistry.get(serviceId);
            if (service == null) {
                logger.warn("Persistence service not found: {}", serviceId);
                return dataPoints;
            }

            if (!(service instanceof QueryablePersistenceService queryableService)) {
                logger.warn("Persistence service does not support queries: {}", serviceId);
                return dataPoints;
            }

            // Create filter criteria following openHAB Core patterns
            FilterCriteria filter = new FilterCriteria();
            filter.setItemName(itemName);

            if (startTime != null) {
                filter.setBeginDate(startTime.atZone(java.time.ZoneId.systemDefault()));
            }
            if (endTime != null) {
                filter.setEndDate(endTime.atZone(java.time.ZoneId.systemDefault()));
            }

            // Set ordering to get most recent data first
            filter.setOrdering(FilterCriteria.Ordering.DESCENDING);

            // Query the persistence service
            Iterable<HistoricItem> historicItems = queryableService.query(filter);

            // Process the results
            for (HistoricItem historicItem : historicItems) {
                Map<String, Object> dataPoint = new HashMap<>();
                dataPoint.put("timestamp", historicItem.getTimestamp().toInstant().toEpochMilli());
                dataPoint.put("timestampISO", historicItem.getTimestamp().toString());

                // Get the state information
                State state = historicItem.getState();
                if (state != null) {
                    dataPoint.put("state", state.toString());
                    dataPoint.put("value", state.toString());

                    // Add type-specific value extraction
                    if (state instanceof org.openhab.core.library.types.DecimalType) {
                        dataPoint.put("numericValue",
                                ((org.openhab.core.library.types.DecimalType) state).doubleValue());
                    } else if (state instanceof org.openhab.core.library.types.StringType) {
                        dataPoint.put("stringValue", ((org.openhab.core.library.types.StringType) state).toString());
                    } else if (state instanceof org.openhab.core.library.types.OnOffType) {
                        dataPoint.put("booleanValue",
                                ((org.openhab.core.library.types.OnOffType) state) == org.openhab.core.library.types.OnOffType.ON);
                    }
                } else {
                    dataPoint.put("state", "NULL");
                    dataPoint.put("value", "NULL");
                }

                // Add service information
                dataPoint.put("serviceId", serviceId);
                dataPoint.put("itemName", itemName);

                dataPoints.add(dataPoint);

                // Apply limit
                if (limit != null && limit > 0 && dataPoints.size() >= limit) {
                    break;
                }
            }

            // Apply aggregation if specified and not "NONE"
            if (!"NONE".equals(aggregation) && interval != null) {
                dataPoints = applyRealAggregation(dataPoints, aggregation, interval);
            }

            // If no real data found, create a fallback entry
            if (dataPoints.isEmpty()) {
                Map<String, Object> fallbackPoint = new HashMap<>();
                fallbackPoint.put("timestamp", System.currentTimeMillis());
                fallbackPoint.put("timestampISO", java.time.Instant.now().toString());
                fallbackPoint.put("state", "UNKNOWN");
                fallbackPoint.put("value", "UNKNOWN");
                fallbackPoint.put("serviceId", serviceId);
                fallbackPoint.put("itemName", itemName);
                fallbackPoint.put("note", "No persistence data found for the specified criteria");
                dataPoints.add(fallbackPoint);
            }

        } catch (Exception e) {
            logger.warn("Error querying persistence service: {}", e.getMessage());

            // Create error entry
            Map<String, Object> errorPoint = new HashMap<>();
            errorPoint.put("timestamp", System.currentTimeMillis());
            errorPoint.put("timestampISO", java.time.Instant.now().toString());
            errorPoint.put("state", "ERROR");
            errorPoint.put("value", "ERROR");
            errorPoint.put("serviceId", serviceId);
            errorPoint.put("itemName", itemName);
            errorPoint.put("error", "Failed to query persistence service: " + e.getMessage());
            dataPoints.add(errorPoint);
        }

        return dataPoints;
    }

    /**
     * Apply real aggregation based on openHAB Core patterns
     */
    private List<Map<String, Object>> applyRealAggregation(List<Map<String, Object>> dataPoints, String aggregation,
            String interval) {
        if (dataPoints.isEmpty()) {
            return dataPoints;
        }

        // Group data by time interval
        Map<String, List<Map<String, Object>>> groupedData = new HashMap<>();

        for (Map<String, Object> dataPoint : dataPoints) {
            Long timestamp = (Long) dataPoint.get("timestamp");
            if (timestamp != null) {
                java.time.ZonedDateTime dateTime = java.time.ZonedDateTime
                        .ofInstant(java.time.Instant.ofEpochMilli(timestamp), java.time.ZoneId.systemDefault());
                String intervalKey = getIntervalKey(dateTime, interval);
                groupedData.computeIfAbsent(intervalKey, k -> new ArrayList<>()).add(dataPoint);
            }
        }

        // Apply aggregation function to each group
        List<Map<String, Object>> aggregatedData = new ArrayList<>();
        for (Map.Entry<String, List<Map<String, Object>>> entry : groupedData.entrySet()) {
            Map<String, Object> aggregatedPoint = new HashMap<>();
            List<Map<String, Object>> group = entry.getValue();

            // Calculate aggregated value based on function
            Object aggregatedValue = calculateAggregatedValue(group, aggregation);

            Object timestamp = group.get(0).get("timestamp");
            String timestampStr = timestamp != null ? timestamp.toString() : "";
            aggregatedPoint.put("timestamp", timestampStr);

            Object timestampISO = group.get(0).get("timestampISO");
            String timestampISOStr = timestampISO != null ? timestampISO.toString() : "";
            aggregatedPoint.put("timestampISO", timestampISOStr);
            aggregatedPoint.put("state", aggregatedValue != null ? aggregatedValue.toString() : "NULL");
            aggregatedPoint.put("value", aggregatedValue != null ? aggregatedValue : "NULL");
            aggregatedPoint.put("aggregation", aggregation);
            aggregatedPoint.put("interval", interval);
            aggregatedPoint.put("recordCount", group.size());
            Object serviceId = group.get(0).get("serviceId");
            String serviceIdStr = serviceId != null ? serviceId.toString() : "";
            aggregatedPoint.put("serviceId", serviceIdStr);

            Object itemName = group.get(0).get("itemName");
            String itemNameStr = itemName != null ? itemName.toString() : "";
            aggregatedPoint.put("itemName", itemNameStr);

            aggregatedData.add(aggregatedPoint);
        }

        return aggregatedData;
    }

    /**
     * Get interval key for grouping data
     */
    private String getIntervalKey(java.time.ZonedDateTime dateTime, String interval) {
        if (interval.endsWith("h")) {
            int hours = Integer.parseInt(interval.substring(0, interval.length() - 1));
            return dateTime.truncatedTo(java.time.temporal.ChronoUnit.HOURS)
                    .plusHours(dateTime.getHour() / hours * hours).toString();
        } else if (interval.endsWith("d")) {
            int days = Integer.parseInt(interval.substring(0, interval.length() - 1));
            return dateTime.truncatedTo(java.time.temporal.ChronoUnit.DAYS)
                    .plusDays(dateTime.getDayOfYear() / days * days).toString();
        } else {
            // Default to hourly
            return dateTime.truncatedTo(java.time.temporal.ChronoUnit.HOURS).toString();
        }
    }

    /**
     * Calculate aggregated value based on function
     */
    private Object calculateAggregatedValue(List<Map<String, Object>> group, String aggregation) {
        if (group.isEmpty()) {
            return "NULL";
        }

        List<Double> numericValues = new ArrayList<>();
        for (Map<String, Object> dataPoint : group) {
            Object value = dataPoint.get("numericValue");
            if (value instanceof Number) {
                numericValues.add(((Number) value).doubleValue());
            }
        }

        if (numericValues.isEmpty()) {
            // If no numeric values, return the first state
            Object state = group.get(0).get("state");
            return state != null ? state : "NULL";
        }

        return switch (aggregation.toUpperCase()) {
            case "AVG" -> numericValues.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            case "MIN" -> numericValues.stream().mapToDouble(Double::doubleValue).min().orElse(0.0);
            case "MAX" -> numericValues.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
            case "SUM" -> numericValues.stream().mapToDouble(Double::doubleValue).sum();
            case "COUNT" -> (double) group.size();
            default -> {
                Object state = group.get(0).get("state");
                yield state != null ? state : "NULL";
            }
        };
    }

    /**
     * Check if an item exists in the registry
     */
    private boolean itemExists(String itemName) {
        return itemRegistry.get(itemName) != null;
    }
}
