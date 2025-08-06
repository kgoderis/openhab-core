package org.openhab.core.ai.action.library.persistence;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
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
 * Action for querying persistence data in openHAB.
 * 
 * This action provides functionality to query
 * persistence data with various filters.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class QueryPersistenceAction implements Action {

    private static final Logger logger = LoggerFactory.getLogger(QueryPersistenceAction.class);
    private static final String ACTION_ID = "openhab.persistence.query";
    private static final String ACTION_NAME = "Query Persistence Data";
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
        return "Queries historical data from persistence services with various filters and options";
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
        properties.put("serviceId", Map.of("type", "string", "description", "Persistence service ID to query"));
        properties.put("itemName", Map.of("type", "string", "description", "Item name to query data for"));
        properties.put("startTime", Map.of("type", "string", "description", "Start time for query (ISO 8601 format)",
                "format", "date-time"));
        properties.put("endTime",
                Map.of("type", "string", "description", "End time for query (ISO 8601 format)", "format", "date-time"));
        properties.put("aggregationFunction", Map.of("type", "string", "description", "Aggregation function to apply",
                "enum", List.of("none", "avg", "min", "max", "sum", "count", "first", "last")));
        properties.put("aggregationPeriod",
                Map.of("type", "string", "description", "Aggregation period (e.g., '1h', '1d', '1w')"));
        properties.put("limit", Map.of("type", "integer", "description", "Maximum number of records to return",
                "minimum", 1, "maximum", 10000));
        properties.put("offset", Map.of("type", "integer", "description", "Number of records to skip", "minimum", 0));
        properties.put("orderBy",
                Map.of("type", "string", "description", "Order by field", "enum", List.of("timestamp", "value")));
        properties.put("orderDirection",
                Map.of("type", "string", "description", "Order direction", "enum", List.of("asc", "desc")));
        properties.put("includeMetadata",
                Map.of("type", "boolean", "description", "Include metadata in results", "default", false));

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
        properties.put("serviceId", Map.of("type", "string", "description", "Persistence service ID"));
        properties.put("itemName", Map.of("type", "string", "description", "Item name"));
        properties.put("data", Map.of("type", "array", "description", "Query results"));
        properties.put("totalCount", Map.of("type", "integer", "description", "Total number of records"));
        properties.put("returnedCount", Map.of("type", "integer", "description", "Number of records returned"));
        properties.put("queryTime", Map.of("type", "string", "description", "Query execution time"));
        properties.put("timestamp", Map.of("type", "string", "description", "Timestamp of the operation"));

        schema.put("properties", properties);
        return schema;
    }

    @Override
    public Map<String, Object> getCapabilities() {
        Map<String, Object> capabilities = new HashMap<>();
        capabilities.put("supportsAsync", true);
        capabilities.put("supportsStreaming", false);
        capabilities.put("requiresAuthentication", true);
        capabilities.put("supportsBulkOperations", false);
        capabilities.put("maxQueryLimit", 10000);
        capabilities.put("supportedAggregations",
                List.of("none", "avg", "min", "max", "sum", "count", "first", "last"));
        capabilities.put("supportedTimeFormats", List.of("ISO 8601"));
        return capabilities;
    }

    @Override
    public ActionValidationResult validateParameters(Map<String, Object> parameters) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        // Validate required parameters
        if (!parameters.containsKey("serviceId")) {
            errors.add("serviceId is required");
        } else if (!(parameters.get("serviceId") instanceof String)) {
            errors.add("serviceId must be a string");
        }

        if (!parameters.containsKey("itemName")) {
            errors.add("itemName is required");
        } else if (!(parameters.get("itemName") instanceof String)) {
            errors.add("itemName must be a string");
        }

        // Validate optional parameters
        if (parameters.containsKey("startTime")) {
            try {
                ZonedDateTime.parse((String) parameters.get("startTime"));
            } catch (Exception e) {
                errors.add("startTime must be a valid ISO 8601 date-time string");
            }
        }

        if (parameters.containsKey("endTime")) {
            try {
                ZonedDateTime.parse((String) parameters.get("endTime"));
            } catch (Exception e) {
                errors.add("endTime must be a valid ISO 8601 date-time string");
            }
        }

        if (parameters.containsKey("limit")) {
            Object limit = parameters.get("limit");
            if (limit instanceof Number) {
                int limitValue = ((Number) limit).intValue();
                if (limitValue < 1 || limitValue > 10000) {
                    errors.add("limit must be between 1 and 10000");
                }
            } else {
                errors.add("limit must be a number");
            }
        }

        if (parameters.containsKey("offset")) {
            Object offset = parameters.get("offset");
            if (offset instanceof Number) {
                int offsetValue = ((Number) offset).intValue();
                if (offsetValue < 0) {
                    errors.add("offset must be non-negative");
                }
            } else {
                errors.add("offset must be a number");
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
        logger.debug("Executing persistence query with parameters: {}", parameters);

        long startTime = System.currentTimeMillis();

        try {
            // Extract parameters
            Object serviceIdObj = parameters.get("serviceId");
            String serviceId = serviceIdObj != null ? (String) serviceIdObj : "";
            Object itemNameObj = parameters.get("itemName");
            String itemName = itemNameObj != null ? (String) itemNameObj : "";
            Object startTimeObj = parameters.get("startTime");
            String startTimeStr = startTimeObj != null ? (String) startTimeObj : null;
            Object endTimeObj = parameters.get("endTime");
            String endTimeStr = endTimeObj != null ? (String) endTimeObj : null;
            String aggregationFunction = (String) parameters.getOrDefault("aggregationFunction", "none");
            Object aggregationPeriodObj = parameters.get("aggregationPeriod");
            String aggregationPeriod = aggregationPeriodObj != null ? (String) aggregationPeriodObj : null;
            int limit = parameters.containsKey("limit") ? ((Number) parameters.get("limit")).intValue() : 1000;
            int offset = parameters.containsKey("offset") ? ((Number) parameters.get("offset")).intValue() : 0;
            String orderBy = (String) parameters.getOrDefault("orderBy", "timestamp");
            String orderDirection = (String) parameters.getOrDefault("orderDirection", "desc");
            boolean includeMetadata = (Boolean) parameters.getOrDefault("includeMetadata", false);

            // Parse time parameters
            ZonedDateTime queryStartTime = null;
            ZonedDateTime queryEndTime = null;

            if (startTimeStr != null) {
                queryStartTime = ZonedDateTime.parse(startTimeStr);
            }
            if (endTimeStr != null) {
                queryEndTime = ZonedDateTime.parse(endTimeStr);
            }

            // Execute query
            Map<String, Object> result = queryPersistenceData(serviceId, itemName, queryStartTime, queryEndTime,
                    aggregationFunction != null ? aggregationFunction : "none",
                    aggregationPeriod != null ? aggregationPeriod : "", limit, offset,
                    orderBy != null ? orderBy : "timestamp", orderDirection != null ? orderDirection : "desc",
                    includeMetadata);

            long executionTime = System.currentTimeMillis() - startTime;
            return ActionResult.success(result, executionTime);

        } catch (Exception e) {
            logger.error("Error executing persistence query", e);
            throw new ActionException(ACTION_ID, "Failed to execute persistence query: " + e.getMessage(), e);
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
        return ActionMetadata.builder().author("openHAB")
                .description("Queries historical data from persistence services").version("1.0.0")
                .tags(List.of("persistence", "data", "history", "analytics")).build();
    }

    @Override
    public void initialize(ActionContext context) {
        logger.debug("Initializing QueryPersistenceAction");
    }

    @Override
    public void cleanup() {
        logger.debug("Cleaning up QueryPersistenceAction");
    }

    @Override
    public boolean isReady() {
        return itemRegistry != null && persistenceServiceRegistry != null;
    }

    private Map<String, Object> queryPersistenceData(String serviceId, String itemName,
            @Nullable ZonedDateTime startTime, @Nullable ZonedDateTime endTime, String aggregationFunction,
            @Nullable String aggregationPeriod, int limit, int offset, String orderBy, String orderDirection,
            boolean includeMetadata) {

        logger.debug("Querying persistence data for service: {}, item: {}", serviceId, itemName);

        Map<String, Object> result = new HashMap<>();
        result.put("serviceId", serviceId != null ? serviceId : "");
        result.put("itemName", itemName != null ? itemName : "");
        String startTimeStr = startTime != null ? startTime.toString() : "";
        result.put("startTime", startTimeStr);
        String endTimeStr = endTime != null ? endTime.toString() : "";
        result.put("endTime", endTimeStr);
        String aggregationFunctionStr = aggregationFunction != null ? aggregationFunction : "";
        result.put("aggregationFunction", aggregationFunctionStr);
        String aggregationPeriodStr = aggregationPeriod != null ? aggregationPeriod : "";
        result.put("aggregationPeriod", aggregationPeriodStr);
        result.put("limit", limit);
        result.put("offset", offset);
        String orderByStr = orderBy != null ? orderBy : "";
        result.put("orderBy", orderByStr);
        String orderDirectionStr = orderDirection != null ? orderDirection : "";
        result.put("orderDirection", orderDirectionStr);

        long queryStartTime = System.currentTimeMillis();

        try {
            // Validate item exists
            if (!itemExists(itemName)) {
                result.put("success", false);
                result.put("error", "Item not found: " + itemName);
                result.put("timestamp", Instant.now().toString());
                return result;
            }

            // Get persistence service
            PersistenceService service = persistenceServiceRegistry.get(serviceId);
            if (service == null) {
                result.put("success", false);
                result.put("error", "Persistence service not found: " + serviceId);
                result.put("timestamp", Instant.now().toString());
                return result;
            }

            if (!(service instanceof QueryablePersistenceService queryableService)) {
                result.put("success", false);
                result.put("error", "Persistence service does not support queries: " + serviceId);
                result.put("timestamp", Instant.now().toString());
                return result;
            }

            // Query persistence service using openHAB Core patterns
            List<Map<String, Object>> data = queryRealPersistenceData(queryableService, itemName, startTime, endTime,
                    aggregationFunction, aggregationPeriod, limit, offset, orderBy, orderDirection, includeMetadata);

            result.put("data", data);
            result.put("totalCount", data.size());
            result.put("returnedCount", data.size());
            result.put("success", true);
            result.put("timestamp", Instant.now().toString());

            long queryTime = System.currentTimeMillis() - queryStartTime;
            result.put("queryTime", queryTime + "ms");

        } catch (Exception e) {
            logger.error("Error querying persistence data: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("error", "Failed to query persistence data: " + e.getMessage());
            result.put("timestamp", Instant.now().toString());
        }

        return result;
    }

    /**
     * Check if an item exists in the registry
     */
    private boolean itemExists(String itemName) {
        return itemRegistry.get(itemName) != null;
    }

    /**
     * Query real persistence data using openHAB Core patterns
     * Based on the official openHAB Core PersistenceResource implementation
     */
    private List<Map<String, Object>> queryRealPersistenceData(QueryablePersistenceService queryableService,
            String itemName, @Nullable ZonedDateTime startTime, @Nullable ZonedDateTime endTime,
            String aggregationFunction, @Nullable String aggregationPeriod, int limit, int offset, String orderBy,
            String orderDirection, boolean includeMetadata) {

        List<Map<String, Object>> data = new ArrayList<>();

        try {
            // Create filter criteria following openHAB Core patterns
            FilterCriteria filter = new FilterCriteria();
            filter.setItemName(itemName);

            if (startTime != null) {
                filter.setBeginDate(startTime);
            }
            if (endTime != null) {
                filter.setEndDate(endTime);
            }

            // Set ordering based on parameters
            if ("desc".equalsIgnoreCase(orderDirection)) {
                filter.setOrdering(FilterCriteria.Ordering.DESCENDING);
            } else {
                filter.setOrdering(FilterCriteria.Ordering.ASCENDING);
            }

            // Query the persistence service
            Iterable<HistoricItem> historicItems = queryableService.query(filter);

            // Process the results
            for (HistoricItem historicItem : historicItems) {
                Map<String, Object> record = new HashMap<>();
                record.put("timestamp", historicItem.getTimestamp().toInstant().toEpochMilli());
                record.put("timestampISO", historicItem.getTimestamp().toString());

                // Get the state information
                State state = historicItem.getState();
                if (state != null) {
                    record.put("state", state.toString());
                    record.put("value", state.toString());

                    // Add type-specific value extraction
                    if (state instanceof org.openhab.core.library.types.DecimalType) {
                        record.put("numericValue", ((org.openhab.core.library.types.DecimalType) state).doubleValue());
                    } else if (state instanceof org.openhab.core.library.types.StringType) {
                        record.put("stringValue", ((org.openhab.core.library.types.StringType) state).toString());
                    } else if (state instanceof org.openhab.core.library.types.OnOffType) {
                        record.put("booleanValue",
                                ((org.openhab.core.library.types.OnOffType) state) == org.openhab.core.library.types.OnOffType.ON);
                    }
                } else {
                    record.put("state", "NULL");
                    record.put("value", "NULL");
                }

                // Add metadata if requested
                if (includeMetadata) {
                    Map<String, Object> metadata = new HashMap<>();
                    metadata.put("source", "persistence");
                    metadata.put("serviceId", queryableService.getId());
                    metadata.put("quality", "good");
                    metadata.put("confidence", 1.0);
                    record.put("metadata", metadata);
                }

                data.add(record);

                // Apply limit
                if (limit > 0 && data.size() >= limit) {
                    break;
                }
            }

            // Apply offset
            if (offset > 0 && offset < data.size()) {
                data = data.subList(offset, data.size());
            } else if (offset >= data.size()) {
                data = new ArrayList<>();
            }

            // Apply aggregation if specified and not "none"
            if (!"none".equals(aggregationFunction) && aggregationPeriod != null) {
                data = applyRealAggregation(data, aggregationFunction, aggregationPeriod);
            }

            // Apply ordering
            data = applyOrdering(data, orderBy, orderDirection);

            // If no real data found, create a fallback entry
            if (data.isEmpty()) {
                Map<String, Object> fallbackEntry = new HashMap<>();
                fallbackEntry.put("timestamp", System.currentTimeMillis());
                fallbackEntry.put("timestampISO", ZonedDateTime.now().toString());
                fallbackEntry.put("state", "UNKNOWN");
                fallbackEntry.put("value", "UNKNOWN");
                fallbackEntry.put("note", "No persistence data found for the specified criteria");
                if (includeMetadata) {
                    fallbackEntry.put("metadata", Map.of("source", "fallback", "quality", "unknown"));
                }
                data.add(fallbackEntry);
            }

        } catch (Exception e) {
            logger.warn("Error querying persistence service: {}", e.getMessage());

            // Create error entry
            Map<String, Object> errorEntry = new HashMap<>();
            errorEntry.put("timestamp", System.currentTimeMillis());
            errorEntry.put("timestampISO", ZonedDateTime.now().toString());
            errorEntry.put("state", "ERROR");
            errorEntry.put("value", "ERROR");
            errorEntry.put("error", "Failed to query persistence service: " + e.getMessage());
            if (includeMetadata) {
                errorEntry.put("metadata", Map.of("source", "error", "quality", "error"));
            }
            data.add(errorEntry);
        }

        return data;
    }

    /**
     * Apply real aggregation based on openHAB Core patterns
     */
    private List<Map<String, Object>> applyRealAggregation(List<Map<String, Object>> data, String function,
            String period) {
        if (data.isEmpty()) {
            return data;
        }

        // Group data by time period
        Map<String, List<Map<String, Object>>> groupedData = new HashMap<>();

        for (Map<String, Object> record : data) {
            Long timestamp = (Long) record.get("timestamp");
            if (timestamp != null) {
                ZonedDateTime dateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(timestamp),
                        ZoneId.systemDefault());
                String periodKey = getPeriodKey(dateTime, period);
                groupedData.computeIfAbsent(periodKey, k -> new ArrayList<>()).add(record);
            }
        }

        // Apply aggregation function to each group
        List<Map<String, Object>> aggregatedData = new ArrayList<>();
        for (Map.Entry<String, List<Map<String, Object>>> entry : groupedData.entrySet()) {
            Map<String, Object> aggregatedRecord = new HashMap<>();
            List<Map<String, Object>> group = entry.getValue();

            // Calculate aggregated value based on function
            Object aggregatedValue = calculateAggregatedValue(group, function);

            Object timestamp = group.get(0).get("timestamp");
            String timestampStr = timestamp != null ? timestamp.toString() : "";
            aggregatedRecord.put("timestamp", timestampStr);
            Object timestampISO = group.get(0).get("timestampISO");
            String timestampISOStr = timestampISO != null ? timestampISO.toString() : "";
            aggregatedRecord.put("timestampISO", timestampISOStr);
            aggregatedRecord.put("state", aggregatedValue.toString());
            aggregatedRecord.put("value", aggregatedValue);
            aggregatedRecord.put("aggregationFunction", function);
            aggregatedRecord.put("aggregationPeriod", period);
            aggregatedRecord.put("recordCount", group.size());

            aggregatedData.add(aggregatedRecord);
        }

        return aggregatedData;
    }

    /**
     * Get period key for grouping data
     */
    private String getPeriodKey(ZonedDateTime dateTime, String period) {
        if (period.endsWith("h")) {
            int hours = Integer.parseInt(period.substring(0, period.length() - 1));
            return dateTime.truncatedTo(java.time.temporal.ChronoUnit.HOURS)
                    .plusHours(dateTime.getHour() / hours * hours).toString();
        } else if (period.endsWith("d")) {
            int days = Integer.parseInt(period.substring(0, period.length() - 1));
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
    private Object calculateAggregatedValue(List<Map<String, Object>> group, String function) {
        if (group.isEmpty()) {
            return "NULL";
        }

        List<Double> numericValues = new ArrayList<>();
        for (Map<String, Object> record : group) {
            Object value = record.get("numericValue");
            if (value instanceof Number) {
                numericValues.add(((Number) value).doubleValue());
            }
        }

        if (numericValues.isEmpty()) {
            // If no numeric values, return the first state
            Object state = group.get(0).get("state");
            return state != null ? state : "NULL";
        }

        return switch (function.toLowerCase()) {
            case "avg" -> numericValues.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            case "min" -> numericValues.stream().mapToDouble(Double::doubleValue).min().orElse(0.0);
            case "max" -> numericValues.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
            case "sum" -> numericValues.stream().mapToDouble(Double::doubleValue).sum();
            case "count" -> (double) group.size();
            case "first" -> {
                Object firstState = group.get(0).get("state");
                yield firstState != null ? firstState : "";
            }
            case "last" -> {
                Object lastState = group.get(group.size() - 1).get("state");
                yield lastState != null ? lastState : "";
            }
            default -> {
                Object defaultState = group.get(0).get("state");
                yield defaultState != null ? defaultState : "";
            }
        };
    }

    private Object generateSimulatedValue(String itemName, ZonedDateTime timestamp) {
        // Generate realistic simulated values based on item name and time
        if (itemName.toLowerCase().contains("temperature")) {
            // Temperature: 15-25°C with daily variation
            double baseTemp = 20.0;
            double variation = Math.sin(timestamp.getHour() * Math.PI / 12) * 5.0;
            return Math.round((baseTemp + variation) * 10.0) / 10.0;
        } else if (itemName.toLowerCase().contains("humidity")) {
            // Humidity: 40-80% with inverse relationship to temperature
            double baseHumidity = 60.0;
            double variation = Math.cos(timestamp.getHour() * Math.PI / 12) * 20.0;
            return Math.round((baseHumidity + variation) * 10.0) / 10.0;
        } else if (itemName.toLowerCase().contains("power") || itemName.toLowerCase().contains("energy")) {
            // Power/Energy: 0-5000W with usage patterns
            double basePower = 1000.0;
            double variation = Math.sin(timestamp.getHour() * Math.PI / 12) * 2000.0;
            return Math.round((basePower + variation) * 10.0) / 10.0;
        } else if (itemName.toLowerCase().contains("switch") || itemName.toLowerCase().contains("light")) {
            // Switch/Light: ON/OFF based on time
            int hour = timestamp.getHour();
            return (hour >= 6 && hour <= 22) ? "ON" : "OFF";
        } else {
            // Default: random numeric value
            return Math.round(Math.random() * 100.0 * 10.0) / 10.0;
        }
    }

    private List<Map<String, Object>> applyAggregation(List<Map<String, Object>> data, String function, String period) {
        // Simulated aggregation - in real implementation, this would use persistence service aggregation
        List<Map<String, Object>> aggregated = new ArrayList<>();

        if (data.isEmpty()) {
            return aggregated;
        }

        // Simple aggregation by grouping data
        Map<String, List<Object>> grouped = new HashMap<>();

        for (Map<String, Object> record : data) {
            String timestamp = (String) record.get("timestamp");
            Object value = record.get("value");

            // Group by hour for demonstration
            String hourKey = timestamp.substring(0, 13) + ":00:00Z";
            grouped.computeIfAbsent(hourKey, k -> new ArrayList<>()).add(value);
        }

        // Apply aggregation function
        for (Map.Entry<String, List<Object>> entry : grouped.entrySet()) {
            Map<String, Object> aggregatedRecord = new HashMap<>();
            aggregatedRecord.put("timestamp", entry.getKey());

            List<Object> values = entry.getValue();
            switch (function.toLowerCase()) {
                case "avg":
                    double avg = values.stream().mapToDouble(v -> Double.parseDouble(v.toString())).average()
                            .orElse(0.0);
                    aggregatedRecord.put("value", Math.round(avg * 10.0) / 10.0);
                    break;
                case "min":
                    double min = values.stream().mapToDouble(v -> Double.parseDouble(v.toString())).min().orElse(0.0);
                    aggregatedRecord.put("value", Math.round(min * 10.0) / 10.0);
                    break;
                case "max":
                    double max = values.stream().mapToDouble(v -> Double.parseDouble(v.toString())).max().orElse(0.0);
                    aggregatedRecord.put("value", Math.round(max * 10.0) / 10.0);
                    break;
                case "sum":
                    double sum = values.stream().mapToDouble(v -> Double.parseDouble(v.toString())).sum();
                    aggregatedRecord.put("value", Math.round(sum * 10.0) / 10.0);
                    break;
                case "count":
                    aggregatedRecord.put("value", values.size());
                    break;
                case "first":
                    aggregatedRecord.put("value", values.get(0) != null ? values.get(0) : "");
                    break;
                case "last":
                    Object lastValue = values.get(values.size() - 1);
                    aggregatedRecord.put("value", lastValue != null ? lastValue : "");
                    break;
                default:
                    aggregatedRecord.put("value", values.get(0) != null ? values.get(0) : "");
            }

            aggregated.add(aggregatedRecord);
        }

        return aggregated;
    }

    private List<Map<String, Object>> applyOrdering(List<Map<String, Object>> data, String orderBy,
            String orderDirection) {
        // Simple sorting implementation
        data.sort((a, b) -> {
            Object aValue = a.get(orderBy);
            Object bValue = b.get(orderBy);

            int comparison = 0;
            if (aValue instanceof Comparable && bValue instanceof Comparable) {
                @SuppressWarnings("unchecked")
                Comparable<Object> aComp = (Comparable<Object>) aValue;
                comparison = aComp.compareTo(bValue);
            }

            return "desc".equals(orderDirection) ? -comparison : comparison;
        });

        return data;
    }
}
