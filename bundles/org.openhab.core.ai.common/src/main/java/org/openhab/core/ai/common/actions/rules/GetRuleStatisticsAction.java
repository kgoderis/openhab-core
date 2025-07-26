package org.openhab.core.ai.common.actions.rules;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.api.action.AIAction;
import org.openhab.core.ai.common.api.action.AIActionContext;
import org.openhab.core.ai.common.api.action.AIActionException;
import org.openhab.core.ai.common.api.action.AIActionMetadata;
import org.openhab.core.ai.common.api.action.AIActionResult;
import org.openhab.core.ai.common.api.action.AIActionValidationResult;
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
 * AIAction for retrieving statistical information about openHAB Rules.
 * This action provides comprehensive statistics including execution metrics, performance data, and usage patterns.
 * 
 * Uses openHAB's persistence services to retrieve real rule execution statistics.
 */
@Component(service = AIAction.class, immediate = true)
public class GetRuleStatisticsAction implements AIAction {

    private static final Logger logger = LoggerFactory.getLogger(GetRuleStatisticsAction.class);

    @Reference
    private RuleRegistry ruleRegistry;

    @Reference
    private @Nullable PersistenceServiceRegistry persistenceServiceRegistry;

    @Reference
    private @Nullable ItemRegistry itemRegistry;

    @Override
    public String getActionId() {
        return "openhab.rules.statistics";
    }

    @Override
    public String getActionName() {
        return "Get Rule Statistics";
    }

    @Override
    public String getDescription() {
        return "Retrieves comprehensive statistical information about openHAB Rules including execution metrics, performance data, and usage patterns";
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
        properties.put("ruleUID", Map.of("type", "string", "description", "The UID of the rule to get statistics for"));
        properties.put("timeRange", Map.of("type", "string", "enum", java.util.List.of("1h", "24h", "7d", "30d", "all"),
                "description", "Time range for statistics", "default", "24h"));
        properties.put("includePerformance",
                Map.of("type", "boolean", "description", "Include performance statistics", "default", true));
        properties.put("includeUsage",
                Map.of("type", "boolean", "description", "Include usage patterns", "default", true));
        properties.put("includeErrors",
                Map.of("type", "boolean", "description", "Include error statistics", "default", true));
        properties.put("includeTrends",
                Map.of("type", "boolean", "description", "Include trend analysis", "default", false));
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
        properties.put("statistics", Map.of("type", "object", "description", "Comprehensive statistics data"));
        properties.put("timeRange", Map.of("type", "string", "description", "Time range used for statistics"));
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

            // Validate timeRange if provided
            String timeRange = (String) parameters.get("timeRange");
            if (timeRange != null && !List.of("1h", "24h", "7d", "30d", "all").contains(timeRange)) {
                return AIActionValidationResult.invalid(List.of("Invalid timeRange: " + timeRange));
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
            String timeRange = (String) parameters.getOrDefault("timeRange", "24h");
            Boolean includePerformance = (Boolean) parameters.getOrDefault("includePerformance", true);
            Boolean includeUsage = (Boolean) parameters.getOrDefault("includeUsage", true);
            Boolean includeErrors = (Boolean) parameters.getOrDefault("includeErrors", true);
            Boolean includeTrends = (Boolean) parameters.getOrDefault("includeTrends", false);
            String persistenceService = (String) parameters.getOrDefault("persistenceService", "rrd4j");

            logger.debug("Getting statistics for rule: {} using service: {} with timeRange: {}", ruleUID,
                    persistenceService, timeRange);

            // Check if rule exists
            Rule rule = ruleRegistry.get(ruleUID);
            if (rule == null) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("ruleUID", ruleUID);
                result.put("notFound", true);
                result.put("error", "Rule not found: " + ruleUID);
                result.put("statistics", new HashMap<>());

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
                result.put("statistics", new HashMap<>());

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
                result.put("statistics", new HashMap<>());

                long executionTime = System.currentTimeMillis() - executionStartTime;
                return AIActionResult.success(result, executionTime);
            }

            if (!(service instanceof QueryablePersistenceService queryableService)) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("ruleUID", ruleUID);
                result.put("notFound", false);
                result.put("error", "Persistence service does not support queries: " + persistenceService);
                result.put("statistics", new HashMap<>());

                long executionTime = System.currentTimeMillis() - executionStartTime;
                return AIActionResult.success(result, executionTime);
            }

            // Calculate time range
            ZonedDateTime endTime = ZonedDateTime.now();
            ZonedDateTime startTime = calculateStartTime(timeRange, endTime);

            // Get real rule statistics using persistence service
            Map<String, Object> statistics = getRealRuleStatistics(ruleUID, queryableService, startTime, endTime,
                    includePerformance, includeUsage, includeErrors, includeTrends);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("ruleUID", ruleUID);
            result.put("notFound", false);
            result.put("statistics", statistics);
            result.put("timeRange", timeRange);
            result.put("persistenceService", persistenceService);
            result.put("queryStartTime", startTime.toString());
            result.put("queryEndTime", endTime.toString());

            long executionTime = System.currentTimeMillis() - executionStartTime;
            logger.debug("Retrieved statistics for rule: {} in {}ms", ruleUID, executionTime);

            return AIActionResult.success(result, executionTime);

        } catch (Exception e) {
            logger.error("Error getting rule statistics: {}", e.getMessage(), e);
            throw new AIActionException(getActionId(), "Failed to get rule statistics: " + e.getMessage(), e);
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
                .tags(java.util.List.of("rules", "statistics", "monitoring")).build();
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
        logger.debug("Initializing GetRuleStatisticsAction");
    }

    @Override
    public void cleanup() {
        logger.debug("Cleaning up GetRuleStatisticsAction");
    }

    @Override
    public boolean isReady() {
        return ruleRegistry != null && persistenceServiceRegistry != null;
    }

    /**
     * Get real rule statistics using persistence service.
     * This method queries the persistence service for rule execution data and calculates comprehensive statistics.
     */
    private Map<String, Object> getRealRuleStatistics(String ruleUID, QueryablePersistenceService queryableService,
            ZonedDateTime startTime, ZonedDateTime endTime, boolean includePerformance, boolean includeUsage,
            boolean includeErrors, boolean includeTrends) {

        Map<String, Object> statistics = new HashMap<>();

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

            // Process the results and calculate statistics
            List<Map<String, Object>> executionData = new ArrayList<>();
            int totalExecutions = 0;
            int successfulExecutions = 0;
            int failedExecutions = 0;
            long totalExecutionTime = 0;
            long minExecutionTime = Long.MAX_VALUE;
            long maxExecutionTime = 0;
            long lastExecutionTime = 0;
            long firstExecutionTime = Long.MAX_VALUE;

            // Performance metrics
            long totalMemoryUsage = 0;
            double totalCpuUsage = 0.0;
            int performanceDataPoints = 0;

            // Error tracking
            Map<String, Integer> errorTypes = new HashMap<>();
            List<String> recentErrors = new ArrayList<>();

            // Usage patterns
            Map<String, Integer> hourlyUsage = new HashMap<>();
            Map<String, Integer> dailyUsage = new HashMap<>();

            for (HistoricItem historicItem : historicItems) {
                totalExecutions++;

                long timestamp = historicItem.getTimestamp().toInstant().toEpochMilli();
                State state = historicItem.getState();

                // Track execution times
                if (timestamp > lastExecutionTime) {
                    lastExecutionTime = timestamp;
                }
                if (timestamp < firstExecutionTime) {
                    firstExecutionTime = timestamp;
                }

                // Parse execution status and time
                String status = "UNKNOWN";
                long executionTime = 0;

                if (state != null) {
                    String stateStr = state.toString().toUpperCase();
                    if (stateStr.contains("SUCCESS") || stateStr.contains("COMPLETED")) {
                        status = "SUCCESS";
                        successfulExecutions++;
                    } else if (stateStr.contains("ERROR") || stateStr.contains("FAILED")) {
                        status = "ERROR";
                        failedExecutions++;

                        // Track error types
                        String errorType = extractErrorType(state);
                        errorTypes.put(errorType, errorTypes.getOrDefault(errorType, 0) + 1);

                        // Track recent errors
                        if (recentErrors.size() < 10) {
                            recentErrors.add(extractErrorMessage(state));
                        }
                    } else if (stateStr.contains("RUNNING")) {
                        status = "RUNNING";
                    }

                    // Extract execution time
                    try {
                        if (stateStr.contains("executionTime=")) {
                            String timeStr = stateStr.split("executionTime=")[1].split(",")[0];
                            executionTime = Long.parseLong(timeStr);
                            totalExecutionTime += executionTime;

                            if (executionTime < minExecutionTime) {
                                minExecutionTime = executionTime;
                            }
                            if (executionTime > maxExecutionTime) {
                                maxExecutionTime = executionTime;
                            }
                        }
                    } catch (Exception e) {
                        logger.debug("Could not parse execution time from state: {}", state);
                    }

                    // Extract performance metrics if requested
                    if (includePerformance) {
                        long memoryUsage = extractMemoryUsage(state);
                        if (memoryUsage > 0) {
                            totalMemoryUsage += memoryUsage;
                            performanceDataPoints++;
                        }

                        double cpuUsage = extractCpuUsage(state);
                        if (cpuUsage > 0) {
                            totalCpuUsage += cpuUsage;
                        }
                    }
                }

                // Track usage patterns if requested
                if (includeUsage) {
                    ZonedDateTime executionDateTime = ZonedDateTime.ofInstant(historicItem.getTimestamp().toInstant(),
                            java.time.ZoneId.systemDefault());

                    String hour = executionDateTime.getHour() + ":00";
                    hourlyUsage.put(hour, hourlyUsage.getOrDefault(hour, 0) + 1);

                    String day = executionDateTime.getDayOfWeek().toString();
                    dailyUsage.put(day, dailyUsage.getOrDefault(day, 0) + 1);
                }

                // Store execution data for trend analysis
                if (includeTrends) {
                    Map<String, Object> executionEntry = new HashMap<>();
                    executionEntry.put("timestamp", timestamp);
                    executionEntry.put("status", status);
                    executionEntry.put("executionTime", executionTime);
                    executionData.add(executionEntry);
                }
            }

            // Calculate basic statistics
            statistics.put("totalExecutions", totalExecutions);
            statistics.put("successfulExecutions", successfulExecutions);
            statistics.put("failedExecutions", failedExecutions);
            statistics.put("successRate", totalExecutions > 0 ? (double) successfulExecutions / totalExecutions : 0.0);
            statistics.put("failureRate", totalExecutions > 0 ? (double) failedExecutions / totalExecutions : 0.0);

            // Execution time statistics
            statistics.put("averageExecutionTime", totalExecutions > 0 ? totalExecutionTime / totalExecutions : 0);
            statistics.put("minExecutionTime", minExecutionTime < Long.MAX_VALUE ? minExecutionTime : 0);
            statistics.put("maxExecutionTime", maxExecutionTime);
            statistics.put("totalExecutionTime", totalExecutionTime);

            // Time range statistics
            statistics.put("lastExecution",
                    lastExecutionTime > 0 ? new java.util.Date(lastExecutionTime).toString() : "Never");
            statistics.put("firstExecution",
                    firstExecutionTime < Long.MAX_VALUE ? new java.util.Date(firstExecutionTime).toString() : "Never");
            statistics.put("executionFrequency", calculateExecutionFrequency(totalExecutions, startTime, endTime));

            // Performance statistics if requested
            if (includePerformance) {
                Map<String, Object> performanceStats = new HashMap<>();
                performanceStats.put("averageMemoryUsage",
                        performanceDataPoints > 0 ? totalMemoryUsage / performanceDataPoints : 0);
                performanceStats.put("averageCpuUsage",
                        performanceDataPoints > 0 ? totalCpuUsage / performanceDataPoints : 0.0);
                performanceStats.put("totalMemoryUsage", totalMemoryUsage);
                performanceStats.put("totalCpuUsage", totalCpuUsage);
                performanceStats.put("performanceDataPoints", performanceDataPoints);
                statistics.put("performance", performanceStats);
            }

            // Error statistics if requested
            if (includeErrors) {
                Map<String, Object> errorStats = new HashMap<>();
                errorStats.put("errorTypes", errorTypes);
                errorStats.put("recentErrors", recentErrors);
                errorStats.put("mostCommonError", findMostCommonError(errorTypes));
                statistics.put("errors", errorStats);
            }

            // Usage patterns if requested
            if (includeUsage) {
                Map<String, Object> usageStats = new HashMap<>();
                usageStats.put("hourlyUsage", hourlyUsage);
                usageStats.put("dailyUsage", dailyUsage);
                usageStats.put("peakHour", findPeakHour(hourlyUsage));
                usageStats.put("peakDay", findPeakDay(dailyUsage));
                statistics.put("usagePatterns", usageStats);
            }

            // Trend analysis if requested
            if (includeTrends && !executionData.isEmpty()) {
                Map<String, Object> trendStats = new HashMap<>();
                trendStats.put("executionTrend", calculateExecutionTrend(executionData));
                trendStats.put("performanceTrend", calculatePerformanceTrend(executionData));
                trendStats.put("errorTrend", calculateErrorTrend(executionData));
                statistics.put("trends", trendStats);
            }

            // If no data found, create fallback statistics
            if (totalExecutions == 0) {
                statistics.put("note",
                        "No execution data found in persistence service. Rule may not have been executed recently.");
                statistics.put("dataAvailable", false);
            } else {
                statistics.put("dataAvailable", true);
            }

        } catch (Exception e) {
            logger.warn("Error querying persistence service for rule statistics: {}", e.getMessage());

            // Create error statistics
            statistics.put("error", "Failed to query persistence service: " + e.getMessage());
            statistics.put("dataAvailable", false);
        }

        return statistics;
    }

    /**
     * Calculate start time based on time range.
     */
    private ZonedDateTime calculateStartTime(String timeRange, ZonedDateTime endTime) {
        switch (timeRange) {
            case "1h":
                return endTime.minusHours(1);
            case "24h":
                return endTime.minusHours(24);
            case "7d":
                return endTime.minusDays(7);
            case "30d":
                return endTime.minusDays(30);
            case "all":
                return endTime.minusYears(100); // Very old date to get all data
            default:
                return endTime.minusHours(24); // Default to 24h
        }
    }

    /**
     * Calculate execution frequency (executions per hour).
     */
    private double calculateExecutionFrequency(int totalExecutions, ZonedDateTime startTime, ZonedDateTime endTime) {
        long durationHours = java.time.Duration.between(startTime, endTime).toHours();
        return durationHours > 0 ? (double) totalExecutions / durationHours : 0.0;
    }

    /**
     * Find the most common error type.
     */
    private String findMostCommonError(Map<String, Integer> errorTypes) {
        return errorTypes.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey)
                .orElse("Unknown");
    }

    /**
     * Find the peak hour of usage.
     */
    private String findPeakHour(Map<String, Integer> hourlyUsage) {
        return hourlyUsage.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey)
                .orElse("Unknown");
    }

    /**
     * Find the peak day of usage.
     */
    private String findPeakDay(Map<String, Integer> dailyUsage) {
        return dailyUsage.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey)
                .orElse("Unknown");
    }

    /**
     * Calculate execution trend (increasing, decreasing, stable).
     */
    private String calculateExecutionTrend(List<Map<String, Object>> executionData) {
        if (executionData.size() < 2) {
            return "insufficient_data";
        }

        // Simple trend calculation based on first and last execution
        long firstTime = (Long) executionData.get(executionData.size() - 1).get("timestamp");
        long lastTime = (Long) executionData.get(0).get("timestamp");

        if (lastTime > firstTime) {
            return "increasing";
        } else if (lastTime < firstTime) {
            return "decreasing";
        } else {
            return "stable";
        }
    }

    /**
     * Calculate performance trend.
     */
    private String calculatePerformanceTrend(List<Map<String, Object>> executionData) {
        if (executionData.size() < 2) {
            return "insufficient_data";
        }

        // Simple performance trend based on execution times
        long firstExecTime = (Long) executionData.get(executionData.size() - 1).get("executionTime");
        long lastExecTime = (Long) executionData.get(0).get("executionTime");

        if (lastExecTime < firstExecTime) {
            return "improving";
        } else if (lastExecTime > firstExecTime) {
            return "degrading";
        } else {
            return "stable";
        }
    }

    /**
     * Calculate error trend.
     */
    private String calculateErrorTrend(List<Map<String, Object>> executionData) {
        if (executionData.size() < 2) {
            return "insufficient_data";
        }

        // Count errors in first and second half of data
        int midPoint = executionData.size() / 2;
        int firstHalfErrors = 0;
        int secondHalfErrors = 0;

        for (int i = 0; i < executionData.size(); i++) {
            String status = (String) executionData.get(i).get("status");
            if ("ERROR".equals(status)) {
                if (i < midPoint) {
                    secondHalfErrors++;
                } else {
                    firstHalfErrors++;
                }
            }
        }

        if (secondHalfErrors < firstHalfErrors) {
            return "decreasing";
        } else if (secondHalfErrors > firstHalfErrors) {
            return "increasing";
        } else {
            return "stable";
        }
    }

    // Helper methods for extracting information from state (same as GetRuleHistoryAction)
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
}
