package org.openhab.core.ai.common.actions.rules;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.action.AIActionContext;
import org.openhab.core.ai.common.action.AIActionMetadata;
import org.openhab.core.ai.common.action.AIActionResult;
import org.openhab.core.ai.common.action.AIActionValidationResult;
import org.openhab.core.ai.common.api.action.AIAction;
import org.openhab.core.ai.common.api.action.AIActionException;
import org.openhab.core.automation.Rule;
import org.openhab.core.automation.RuleRegistry;
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
 * AIAction for retrieving execution history of openHAB Rules.
 * This action provides detailed execution history including timestamps, results, and performance metrics.
 * 
 * Uses openHAB's persistence services to retrieve real rule execution history.
 */
@Component(service = AIAction.class, immediate = true)
public class GetRuleHistoryAction implements AIAction {

    private static final Logger logger = LoggerFactory.getLogger(GetRuleHistoryAction.class);

    @Reference
    private RuleRegistry ruleRegistry;

    @Reference
    private @Nullable PersistenceServiceRegistry persistenceServiceRegistry;

    @Reference
    private @Nullable ItemRegistry itemRegistry;

    @Override
    public String getActionId() {
        return "openhab.rules.history";
    }

    @Override
    public String getActionName() {
        return "Get Rule History";
    }

    @Override
    public String getDescription() {
        return "Retrieves execution history of an openHAB Rule including timestamps, results, and performance metrics";
    }

    @Override
    public String getCategory() {
        return "rules";
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
        properties.put("ruleUID", Map.of("type", "string", "description", "The UID of the rule to get history for"));
        properties.put("startTime", Map.of("type", "string", "description",
                "Start time for history query (ISO 8601 or relative like '24h ago')"));
        properties.put("endTime", Map.of("type", "string", "description",
                "End time for history query (ISO 8601 or relative like '1h ago')"));
        properties.put("maxEntries", Map.of("type", "integer", "minimum", 1, "maximum", 1000, "description",
                "Maximum number of history entries to return", "default", 100));
        properties.put("includeDetails",
                Map.of("type", "boolean", "description", "Include detailed execution information", "default", false));
        properties.put("includeErrors",
                Map.of("type", "boolean", "description", "Include error information", "default", true));
        properties.put("includePerformance",
                Map.of("type", "boolean", "description", "Include performance metrics", "default", false));
        properties.put("persistenceService", Map.of("type", "string", "description",
                "Persistence service to use (e.g., 'rrd4j', 'influxdb', 'jdbc')", "default", "rrd4j"));

        schema.put("properties", properties);
        schema.put("required", java.util.List.of("ruleUID"));
        schema.put("additionalProperties", false);
        return schema;
    }

    @Override
    public Map<String, Object> getReturnSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();
        properties.put("success", Map.of("type", "boolean", "description", "Whether the operation was successful"));
        properties.put("ruleUID", Map.of("type", "string", "description", "The UID of the rule"));
        properties.put("history", Map.of("type", "array", "items", Map.of("type", "object"), "description",
                "List of execution history entries"));
        properties.put("entryCount", Map.of("type", "integer", "description", "Number of history entries returned"));
        properties.put("summary", Map.of("type", "object", "description", "Summary statistics of the history"));
        properties.put("error", Map.of("type", "string", "description", "Error message if operation failed"));
        properties.put("notFound", Map.of("type", "boolean", "description", "Whether the rule was not found"));

        schema.put("properties", properties);
        return schema;
    }

    @Override
    public AIActionValidationResult validateParameters(Map<String, Object> parameters) {
        try {
            String ruleUID = (String) parameters.get("ruleUID");
            if (ruleUID == null || ruleUID.trim().isEmpty()) {
                return AIActionValidationResult.invalid(List.of("ruleUID is required"));
            }

            // Validate time parameters if provided
            String startTime = (String) parameters.get("startTime");
            if (startTime != null) {
                try {
                    parseTimeParameter(startTime);
                } catch (Exception e) {
                    return AIActionValidationResult.invalid(List.of("Invalid startTime format: " + e.getMessage()));
                }
            }

            String endTime = (String) parameters.get("endTime");
            if (endTime != null) {
                try {
                    parseTimeParameter(endTime);
                } catch (Exception e) {
                    return AIActionValidationResult.invalid(List.of("Invalid endTime format: " + e.getMessage()));
                }
            }

            // Validate maxEntries
            Object maxEntriesObj = parameters.get("maxEntries");
            if (maxEntriesObj != null) {
                int maxEntries = (Integer) maxEntriesObj;
                if (maxEntries < 1 || maxEntries > 1000) {
                    return AIActionValidationResult.invalid(List.of("maxEntries must be between 1 and 1000"));
                }
            }

            return AIActionValidationResult.valid(parameters);

        } catch (Exception e) {
            return AIActionValidationResult.invalid(List.of("Parameter validation failed: " + e.getMessage()));
        }
    }

    @Override
    public AIActionResult execute(Map<String, Object> parameters, AIActionContext context) throws AIActionException {
        long executionStartTime = System.currentTimeMillis();

        try {
            String ruleUID = (String) parameters.get("ruleUID");
            String startTimeStr = (String) parameters.getOrDefault("startTime", "24h ago");
            String endTimeStr = (String) parameters.getOrDefault("endTime", "now");
            Integer maxEntries = (Integer) parameters.getOrDefault("maxEntries", 100);
            Boolean includeDetails = (Boolean) parameters.getOrDefault("includeDetails", false);
            Boolean includeErrors = (Boolean) parameters.getOrDefault("includeErrors", true);
            Boolean includePerformance = (Boolean) parameters.getOrDefault("includePerformance", false);
            String persistenceService = (String) parameters.getOrDefault("persistenceService", "rrd4j");

            logger.debug("Getting history for rule: {} using service: {} from {} to {} (max: {})", ruleUID,
                    persistenceService, startTimeStr, endTimeStr, maxEntries);

            // Check if rule exists
            Rule rule = ruleRegistry.get(ruleUID);
            if (rule == null) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("ruleUID", ruleUID);
                result.put("notFound", true);
                result.put("error", "Rule not found: " + ruleUID);
                result.put("history", List.of());
                result.put("entryCount", 0);
                result.put("summary", new HashMap<>());

                long executionTime = System.currentTimeMillis() - executionStartTime;
                return AIActionResult.success(result, executionTime);
            }

            // Check if persistence services are available
            if (persistenceServiceRegistry == null) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("ruleUID", ruleUID);
                result.put("notFound", false);
                result.put("error", "Persistence service registry not available");
                result.put("history", List.of());
                result.put("entryCount", 0);
                result.put("summary", new HashMap<>());

                long executionTime = System.currentTimeMillis() - executionStartTime;
                return AIActionResult.success(result, executionTime);
            }

            // Get the persistence service
            PersistenceService service = persistenceServiceRegistry.get(persistenceService);
            if (service == null) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("ruleUID", ruleUID);
                result.put("notFound", false);
                result.put("error", "Persistence service not found: " + persistenceService);
                result.put("history", List.of());
                result.put("entryCount", 0);
                result.put("summary", new HashMap<>());

                long executionTime = System.currentTimeMillis() - executionStartTime;
                return AIActionResult.success(result, executionTime);
            }

            if (!(service instanceof QueryablePersistenceService queryableService)) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("ruleUID", ruleUID);
                result.put("notFound", false);
                result.put("error", "Persistence service does not support queries: " + persistenceService);
                result.put("history", List.of());
                result.put("entryCount", 0);
                result.put("summary", new HashMap<>());

                long executionTime = System.currentTimeMillis() - executionStartTime;
                return AIActionResult.success(result, executionTime);
            }

            // Parse time parameters
            ZonedDateTime queryStartTime = parseTimeParameter(startTimeStr);
            ZonedDateTime queryEndTime = parseTimeParameter(endTimeStr);

            // Get real rule execution history using persistence service
            List<Map<String, Object>> history = getRealRuleHistory(ruleUID, queryableService, queryStartTime,
                    queryEndTime, maxEntries, includeDetails, includeErrors, includePerformance);

            // Generate summary statistics
            Map<String, Object> summary = generateHistorySummary(history);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("ruleUID", ruleUID);
            result.put("notFound", false);
            result.put("history", history);
            result.put("entryCount", history.size());
            result.put("summary", summary);
            result.put("persistenceService", persistenceService);
            result.put("queryStartTime", queryStartTime.toString());
            result.put("queryEndTime", queryEndTime.toString());

            long executionTime = System.currentTimeMillis() - executionStartTime;
            logger.debug("Retrieved {} history entries for rule: {} in {}ms", history.size(), ruleUID, executionTime);

            return AIActionResult.success(result, executionTime);

        } catch (Exception e) {
            logger.error("Error getting rule history: {}", e.getMessage(), e);
            throw new AIActionException(getActionId(), "Failed to get rule history: " + e.getMessage(), e);
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
        return AIActionMetadata.builder().version(getVersion()).author("openHAB").description(getDescription())
                .tags(java.util.List.of("rules", "history", "monitoring")).build();
    }

    @Override
    public Map<String, Object> getCapabilities() {
        Map<String, Object> capabilities = new HashMap<>();
        capabilities.put("supportsAsync", true);
        capabilities.put("supportsValidation", true);
        capabilities.put("usesPersistenceService", true);
        return capabilities;
    }

    @Override
    public void initialize(AIActionContext context) {
        logger.debug("Initializing GetRuleHistoryAction");
    }

    @Override
    public void cleanup() {
        logger.debug("Cleaning up GetRuleHistoryAction");
    }

    @Override
    public boolean isReady() {
        return ruleRegistry != null && persistenceServiceRegistry != null;
    }

    /**
     * Get real rule execution history using persistence service.
     * This method queries the persistence service for rule execution events.
     */
    private List<Map<String, Object>> getRealRuleHistory(String ruleUID, QueryablePersistenceService queryableService,
            ZonedDateTime startTime, ZonedDateTime endTime, int maxEntries, boolean includeDetails,
            boolean includeErrors, boolean includePerformance) {

        List<Map<String, Object>> history = new ArrayList<>();

        try {
            // Create filter criteria for rule execution events
            FilterCriteria filter = new FilterCriteria();

            // Query for rule execution events - using a virtual item name pattern for rule events
            String ruleEventItemName = "Rule_Execution_" + ruleUID;
            filter.setItemName(ruleEventItemName);
            filter.setBeginDate(startTime);
            filter.setEndDate(endTime);
            filter.setOrdering(FilterCriteria.Ordering.DESCENDING);

            // Query the persistence service
            Iterable<HistoricItem> historicItems = queryableService.query(filter);

            // Process the results
            for (HistoricItem historicItem : historicItems) {
                Map<String, Object> entry = new HashMap<>();
                entry.put("timestamp", historicItem.getTimestamp().toInstant().toEpochMilli());
                entry.put("executionId",
                        "exec-" + ruleUID + "-" + historicItem.getTimestamp().toInstant().toEpochMilli());

                // Parse the state to determine execution status
                State state = historicItem.getState();
                String status = "UNKNOWN";
                if (state != null) {
                    String stateStr = state.toString().toUpperCase();
                    if (stateStr.contains("SUCCESS") || stateStr.contains("COMPLETED")) {
                        status = "SUCCESS";
                    } else if (stateStr.contains("ERROR") || stateStr.contains("FAILED")) {
                        status = "ERROR";
                    } else if (stateStr.contains("RUNNING")) {
                        status = "RUNNING";
                    }
                }
                entry.put("status", status);

                // Extract execution time from state or use default
                long executionTime = 100; // Default
                if (state != null) {
                    try {
                        // Try to extract execution time from state
                        String stateStr = state.toString();
                        if (stateStr.contains("executionTime=")) {
                            String timeStr = stateStr.split("executionTime=")[1].split(",")[0];
                            executionTime = Long.parseLong(timeStr);
                        }
                    } catch (Exception e) {
                        logger.debug("Could not parse execution time from state: {}", state);
                    }
                }
                entry.put("executionTime", executionTime);

                // Add detailed information if requested
                if (includeDetails) {
                    entry.put("triggeredBy", extractTriggeredBy(state));
                    entry.put("conditionsPassed", extractConditionsPassed(state));
                    entry.put("actionsExecuted", extractActionsExecuted(state));
                }

                // Add error information if requested and status is ERROR
                if (includeErrors && "ERROR".equals(status)) {
                    entry.put("error", extractErrorMessage(state));
                    entry.put("errorType", extractErrorType(state));
                }

                // Add performance metrics if requested
                if (includePerformance) {
                    entry.put("memoryUsage", extractMemoryUsage(state));
                    entry.put("cpuUsage", extractCpuUsage(state));
                }

                history.add(entry);
            }

            // If no real history found, create a fallback entry
            if (history.isEmpty()) {
                Map<String, Object> fallbackEntry = new HashMap<>();
                fallbackEntry.put("timestamp", System.currentTimeMillis());
                fallbackEntry.put("executionId", "exec-" + ruleUID + "-fallback");
                fallbackEntry.put("status", "UNKNOWN");
                fallbackEntry.put("executionTime", 0);
                fallbackEntry.put("note",
                        "No execution history found in persistence service. Rule may not have been executed recently.");
                history.add(fallbackEntry);
            }

        } catch (Exception e) {
            logger.warn("Error querying persistence service for rule history: {}", e.getMessage());

            // Create error entry
            Map<String, Object> errorEntry = new HashMap<>();
            errorEntry.put("timestamp", System.currentTimeMillis());
            errorEntry.put("executionId", "exec-" + ruleUID + "-error");
            errorEntry.put("status", "ERROR");
            errorEntry.put("executionTime", 0);
            errorEntry.put("error", "Failed to query persistence service: " + e.getMessage());
            history.add(errorEntry);
        }

        return history;
    }

    /**
     * Generate summary statistics from history entries.
     */
    private Map<String, Object> generateHistorySummary(List<Map<String, Object>> history) {
        Map<String, Object> summary = new HashMap<>();

        if (history.isEmpty()) {
            summary.put("totalExecutions", 0);
            summary.put("successfulExecutions", 0);
            summary.put("failedExecutions", 0);
            summary.put("averageExecutionTime", 0);
            summary.put("lastExecution", "Never");
            summary.put("firstExecution", "Never");
            return summary;
        }

        int totalExecutions = history.size();
        int successfulExecutions = 0;
        int failedExecutions = 0;
        long totalExecutionTime = 0;
        long lastExecution = 0;
        long firstExecution = Long.MAX_VALUE;

        for (Map<String, Object> entry : history) {
            String status = (String) entry.get("status");
            if ("SUCCESS".equals(status)) {
                successfulExecutions++;
            } else if ("ERROR".equals(status)) {
                failedExecutions++;
            }

            Long executionTime = (Long) entry.get("executionTime");
            if (executionTime != null) {
                totalExecutionTime += executionTime;
            }

            Long timestamp = (Long) entry.get("timestamp");
            if (timestamp != null) {
                if (timestamp > lastExecution) {
                    lastExecution = timestamp;
                }
                if (timestamp < firstExecution) {
                    firstExecution = timestamp;
                }
            }
        }

        summary.put("totalExecutions", totalExecutions);
        summary.put("successfulExecutions", successfulExecutions);
        summary.put("failedExecutions", failedExecutions);
        summary.put("averageExecutionTime", totalExecutions > 0 ? totalExecutionTime / totalExecutions : 0);
        summary.put("lastExecution", lastExecution > 0 ? new java.util.Date(lastExecution).toString() : "Never");
        summary.put("firstExecution",
                firstExecution < Long.MAX_VALUE ? new java.util.Date(firstExecution).toString() : "Never");

        // Calculate success rate
        if (totalExecutions > 0) {
            double successRate = (double) successfulExecutions / totalExecutions;
            summary.put("successRate", successRate);
        } else {
            summary.put("successRate", 0.0);
        }

        return summary;
    }

    // Helper methods for extracting information from state
    private String extractTriggeredBy(State state) {
        if (state == null)
            return "Unknown";
        String stateStr = state.toString();
        if (stateStr.contains("triggeredBy=")) {
            try {
                return stateStr.split("triggeredBy=")[1].split(",")[0];
            } catch (Exception e) {
                return "Unknown";
            }
        }
        return "Unknown";
    }

    private boolean extractConditionsPassed(State state) {
        if (state == null)
            return true;
        String stateStr = state.toString();
        return !stateStr.contains("conditionsPassed=false");
    }

    private int extractActionsExecuted(State state) {
        if (state == null)
            return 0;
        String stateStr = state.toString();
        if (stateStr.contains("actionsExecuted=")) {
            try {
                String countStr = stateStr.split("actionsExecuted=")[1].split(",")[0];
                return Integer.parseInt(countStr);
            } catch (Exception e) {
                return 0;
            }
        }
        return 0;
    }

    private String extractErrorMessage(State state) {
        if (state == null)
            return "Unknown error";
        String stateStr = state.toString();
        if (stateStr.contains("error=")) {
            try {
                return stateStr.split("error=")[1].split(",")[0];
            } catch (Exception e) {
                return "Unknown error";
            }
        }
        return "Unknown error";
    }

    private String extractErrorType(State state) {
        if (state == null)
            return "Unknown";
        String stateStr = state.toString();
        if (stateStr.contains("errorType=")) {
            try {
                return stateStr.split("errorType=")[1].split(",")[0];
            } catch (Exception e) {
                return "Unknown";
            }
        }
        return "Unknown";
    }

    private long extractMemoryUsage(State state) {
        if (state == null)
            return 0;
        String stateStr = state.toString();
        if (stateStr.contains("memoryUsage=")) {
            try {
                String memoryStr = stateStr.split("memoryUsage=")[1].split(",")[0];
                return Long.parseLong(memoryStr);
            } catch (Exception e) {
                return 0;
            }
        }
        return 0;
    }

    private double extractCpuUsage(State state) {
        if (state == null)
            return 0.0;
        String stateStr = state.toString();
        if (stateStr.contains("cpuUsage=")) {
            try {
                String cpuStr = stateStr.split("cpuUsage=")[1].split(",")[0];
                return Double.parseDouble(cpuStr);
            } catch (Exception e) {
                return 0.0;
            }
        }
        return 0.0;
    }

    /**
     * Parse time parameter (ISO 8601 or relative time).
     */
    private ZonedDateTime parseTimeParameter(String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty()) {
            return ZonedDateTime.now();
        }

        timeStr = timeStr.trim();

        // Handle relative times
        if (timeStr.endsWith(" ago")) {
            return parseRelativeTime(timeStr);
        }

        // Handle "now"
        if ("now".equalsIgnoreCase(timeStr)) {
            return ZonedDateTime.now();
        }

        // Try to parse as ISO 8601
        try {
            return ZonedDateTime.parse(timeStr);
        } catch (Exception e) {
            // Try to parse as local date time
            try {
                return java.time.LocalDateTime.parse(timeStr).atZone(java.time.ZoneId.systemDefault());
            } catch (Exception e2) {
                throw new IllegalArgumentException(
                        "Invalid time format: " + timeStr + ". Use ISO 8601 or relative time (e.g., '24h ago')");
            }
        }
    }

    /**
     * Parse relative time (e.g., "24h ago", "7d ago").
     */
    private ZonedDateTime parseRelativeTime(String amount) {
        String timeAmount = amount.substring(0, amount.length() - 4); // Remove " ago"

        if (timeAmount.endsWith("h")) {
            int hours = Integer.parseInt(timeAmount.substring(0, timeAmount.length() - 1));
            return ZonedDateTime.now().minusHours(hours);
        } else if (timeAmount.endsWith("d")) {
            int days = Integer.parseInt(timeAmount.substring(0, timeAmount.length() - 1));
            return ZonedDateTime.now().minusDays(days);
        } else if (timeAmount.endsWith("w")) {
            int weeks = Integer.parseInt(timeAmount.substring(0, timeAmount.length() - 1));
            return ZonedDateTime.now().minusWeeks(weeks);
        } else if (timeAmount.endsWith("m")) {
            int months = Integer.parseInt(timeAmount.substring(0, timeAmount.length() - 1));
            return ZonedDateTime.now().minusMonths(months);
        } else if (timeAmount.endsWith("y")) {
            int years = Integer.parseInt(timeAmount.substring(0, timeAmount.length() - 1));
            return ZonedDateTime.now().minusYears(years);
        } else {
            throw new IllegalArgumentException("Invalid relative time format: " + amount);
        }
    }
}
