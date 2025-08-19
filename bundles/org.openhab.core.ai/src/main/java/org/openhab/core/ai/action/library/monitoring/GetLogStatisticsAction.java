package org.openhab.core.ai.action.library.monitoring;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.openhab.core.OpenHAB;
import org.openhab.core.ai.action.api.Action;
import org.openhab.core.ai.action.api.ActionException;
import org.openhab.core.ai.action.api.ActionMetadata;
import org.openhab.core.ai.action.api.ActionResult;
import org.openhab.core.ai.action.api.ActionValidationResult;
import org.openhab.core.ai.common.context.ExecutionContext;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action for generating statistics and analysis of log files in openHAB.
 */
@Component(service = Action.class, immediate = true)
public class GetLogStatisticsAction implements Action {

    private static final Logger logger = LoggerFactory.getLogger(GetLogStatisticsAction.class);
    private static final String ACTION_ID = "openhab.monitoring.get_log_statistics";
    private static final String ACTION_NAME = "Get Log Statistics";

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
        return "Generates comprehensive statistics and analysis of log files including error rates, patterns, and trends";
    }

    @Override
    public String getCategory() {
        return "monitoring";
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
        properties.put("logFiles",
                Map.of("type", "array", "items", Map.of("type", "string"), "description",
                        "List of log files to analyze (e.g., ['openhab.log', 'events.log'])", "default",
                        List.of("openhab.log")));
        properties.put("timeRange", Map.of("type", "string", "description",
                "Time range for analysis (e.g., '1h', '24h', '7d', '30d')", "default", "24h"));
        properties.put("includeErrorAnalysis",
                Map.of("type", "boolean", "description", "Include detailed error analysis", "default", true));
        properties.put("includePatternAnalysis",
                Map.of("type", "boolean", "description", "Include pattern and trend analysis", "default", true));
        properties.put("includePerformanceMetrics",
                Map.of("type", "boolean", "description", "Include performance-related metrics", "default", true));
        properties.put("maxTopErrors", Map.of("type", "integer", "description",
                "Maximum number of top errors to include", "default", 10, "minimum", 1, "maximum", 100));
        properties.put("maxTopLoggers", Map.of("type", "integer", "description",
                "Maximum number of top loggers to include", "default", 10, "minimum", 1, "maximum", 100));

        schema.put("properties", properties);
        schema.put("additionalProperties", false);
        return schema;
    }

    @Override
    public Map<String, Object> getReturnSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();
        properties.put("summary", Map.of("type", "object", "description", "Summary statistics"));
        properties.put("errorAnalysis", Map.of("type", "object", "description", "Detailed error analysis"));
        properties.put("patternAnalysis", Map.of("type", "object", "description", "Pattern and trend analysis"));
        properties.put("performanceMetrics", Map.of("type", "object", "description", "Performance-related metrics"));
        properties.put("timeRange", Map.of("type", "string", "description", "Time range analyzed"));
        properties.put("analyzedFiles", Map.of("type", "array", "description", "List of files analyzed"));
        properties.put("timestamp", Map.of("type", "string", "description", "ISO timestamp of the operation"));
        properties.put("message", Map.of("type", "string", "description", "Operation result message"));
        properties.put("error", Map.of("type", "string", "description", "Error message if operation failed"));

        schema.put("properties", properties);
        return schema;
    }

    @Override
    public ActionValidationResult validateParameters(Map<String, Object> parameters) {
        if (parameters == null) {
            return ActionValidationResult.invalid(List.of("Parameters cannot be null"));
        }

        List<String> errors = new ArrayList<>();

        // Validate maxTopErrors
        Object maxTopErrorsObj = parameters.get("maxTopErrors");
        if (maxTopErrorsObj != null) {
            if (maxTopErrorsObj instanceof Integer) {
                Integer maxTopErrors = (Integer) maxTopErrorsObj;
                if (maxTopErrors < 1 || maxTopErrors > 100) {
                    errors.add("maxTopErrors must be between 1 and 100");
                }
            } else {
                errors.add("maxTopErrors must be an integer");
            }
        }

        // Validate maxTopLoggers
        Object maxTopLoggersObj = parameters.get("maxTopLoggers");
        if (maxTopLoggersObj != null) {
            if (maxTopLoggersObj instanceof Integer) {
                Integer maxTopLoggers = (Integer) maxTopLoggersObj;
                if (maxTopLoggers < 1 || maxTopLoggers > 100) {
                    errors.add("maxTopLoggers must be between 1 and 100");
                }
            } else {
                errors.add("maxTopLoggers must be an integer");
            }
        }

        if (!errors.isEmpty()) {
            return ActionValidationResult.invalid(errors);
        }

        return ActionValidationResult.valid(parameters);
    }

    @Override
    public ActionResult execute(Map<String, Object> parameters, ExecutionContext context) throws ActionException {
        long startTime = System.currentTimeMillis();
        logger.debug("Executing get log statistics action with parameters: {}", parameters);

        try {
            @SuppressWarnings("unchecked")
            List<String> logFiles = (List<String>) parameters.getOrDefault("logFiles", List.of("openhab.log"));
            String timeRange = (String) parameters.getOrDefault("timeRange", "24h");
            Boolean includeErrorAnalysis = (Boolean) parameters.getOrDefault("includeErrorAnalysis", true);
            Boolean includePatternAnalysis = (Boolean) parameters.getOrDefault("includePatternAnalysis", true);
            Boolean includePerformanceMetrics = (Boolean) parameters.getOrDefault("includePerformanceMetrics", true);
            Integer maxTopErrors = (Integer) parameters.getOrDefault("maxTopErrors", 10);
            Integer maxTopLoggers = (Integer) parameters.getOrDefault("maxTopLoggers", 10);

            String userDataDir = OpenHAB.getUserDataFolder();
            Path logsDir = Paths.get(userDataDir, "logs");

            Map<String, Object> result = new HashMap<>();
            result.put("timeRange", timeRange);
            result.put("timestamp", Instant.now().toString());

            // Collect statistics from all log files
            Map<String, Object> summary = new HashMap<>();
            Map<String, Object> errorAnalysis = new HashMap<>();
            Map<String, Object> patternAnalysis = new HashMap<>();
            Map<String, Object> performanceMetrics = new HashMap<>();
            List<String> analyzedFiles = new ArrayList<>();

            long totalLines = 0;
            long totalErrors = 0;
            long totalWarnings = 0;
            Map<String, Integer> loggerCounts = new HashMap<>();
            Map<String, Integer> errorPatterns = new HashMap<>();
            Map<String, Integer> hourlyDistribution = new HashMap<>();

            for (String logFile : logFiles) {
                Path logFilePath = logsDir.resolve(logFile);
                if (Files.exists(logFilePath)) {
                    Map<String, Object> fileStats = analyzeLogFile(logFilePath, timeRange);
                    analyzedFiles.add(logFile);

                    // Aggregate statistics
                    totalLines += (Long) fileStats.get("totalLines");
                    totalErrors += (Long) fileStats.get("errorCount");
                    totalWarnings += (Long) fileStats.get("warningCount");

                    // Aggregate logger counts
                    @SuppressWarnings("unchecked")
                    Map<String, Integer> fileLoggerCounts = (Map<String, Integer>) fileStats.get("loggerCounts");
                    fileLoggerCounts.forEach((logger, count) -> loggerCounts.merge(logger, count, Integer::sum));

                    // Aggregate error patterns
                    @SuppressWarnings("unchecked")
                    Map<String, Integer> fileErrorPatterns = (Map<String, Integer>) fileStats.get("errorPatterns");
                    fileErrorPatterns.forEach((pattern, count) -> errorPatterns.merge(pattern, count, Integer::sum));

                    // Aggregate hourly distribution
                    @SuppressWarnings("unchecked")
                    Map<String, Integer> fileHourlyDistribution = (Map<String, Integer>) fileStats
                            .get("hourlyDistribution");
                    fileHourlyDistribution
                            .forEach((hour, count) -> hourlyDistribution.merge(hour, count, Integer::sum));
                }
            }

            // Build summary
            summary.put("totalLines", totalLines);
            summary.put("totalErrors", totalErrors);
            summary.put("totalWarnings", totalWarnings);
            summary.put("errorRate", totalLines > 0 ? (double) totalErrors / totalLines * 100 : 0.0);
            summary.put("warningRate", totalLines > 0 ? (double) totalWarnings / totalLines * 100 : 0.0);
            summary.put("analyzedFiles", analyzedFiles.size());

            // Build error analysis
            if (includeErrorAnalysis) {
                errorAnalysis.put("topErrors", getTopEntries(errorPatterns, maxTopErrors));
                errorAnalysis.put("errorRate", summary.get("errorRate"));
                errorAnalysis.put("totalErrors", totalErrors);
            }

            // Build pattern analysis
            if (includePatternAnalysis) {
                patternAnalysis.put("topLoggers", getTopEntries(loggerCounts, maxTopLoggers));
                patternAnalysis.put("hourlyDistribution", hourlyDistribution);
                patternAnalysis.put("errorPatterns", errorPatterns);
            }

            // Build performance metrics
            if (includePerformanceMetrics) {
                performanceMetrics.put("linesPerHour", calculateLinesPerHour(totalLines, timeRange));
                performanceMetrics.put("errorTrend", calculateErrorTrend(errorPatterns));
                performanceMetrics.put("peakActivityHours", findPeakActivityHours(hourlyDistribution));
            }

            result.put("summary", summary);
            result.put("errorAnalysis", errorAnalysis);
            result.put("patternAnalysis", patternAnalysis);
            result.put("performanceMetrics", performanceMetrics);
            result.put("analyzedFiles", analyzedFiles);
            result.put("message", "Generated statistics for " + analyzedFiles.size() + " log files");

            long executionTime = System.currentTimeMillis() - startTime;
            logger.debug("Get log statistics action completed in {}ms", executionTime);

            return ActionResult.success(result, executionTime);

        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            logger.error("Failed to get log statistics", e);
            throw new ActionException(ACTION_ID, "Failed to get log statistics: " + e.getMessage(), e);
        }
    }

    @Override
    public CompletableFuture<ActionResult> executeAsync(Map<String, Object> parameters, ExecutionContext context) {
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
        return ActionMetadata.builder().withVersion(getVersion()).withAuthor("openHAB")
                .withDescription("Generates comprehensive statistics and analysis of log files")
                .withTags(List.of("monitoring", "logging", "statistics", "analysis"))
                .withDocumentation(
                        "Provides detailed analysis of log files including error rates, patterns, trends, and performance metrics")
                .withExamples(List.of("{\"timeRange\": \"24h\"} - Get statistics for the last 24 hours",
                        "{\"logFiles\": [\"openhab.log\", \"events.log\"], \"includeErrorAnalysis\": true} - Analyze multiple files with error focus",
                        "{\"timeRange\": \"7d\", \"maxTopErrors\": 20} - Get top 20 errors for the last week"))
                .build();
    }

    @Override
    public Map<String, Object> getCapabilities() {
        return Map.of("filtering", true, "sorting", false, "pagination", false, "metadata", true, "async", true,
                "realTime", false);
    }

    @Override
    public void initialize(ExecutionContext context) {
        logger.debug("GetLogStatisticsAction initialized for protocol: {}", context.getProtocol());
    }

    @Override
    public void cleanup() {
        logger.debug("GetLogStatisticsAction cleanup completed");
    }

    @Override
    public boolean isReady() {
        return true;
    }

    private Map<String, Object> analyzeLogFile(Path logFilePath, String timeRange) throws IOException {
        Map<String, Object> stats = new HashMap<>();
        List<String> lines = Files.readAllLines(logFilePath);

        long totalLines = lines.size();
        long errorCount = 0;
        long warningCount = 0;
        Map<String, Integer> loggerCounts = new HashMap<>();
        Map<String, Integer> errorPatterns = new HashMap<>();
        Map<String, Integer> hourlyDistribution = new HashMap<>();

        // Initialize hourly distribution
        for (int i = 0; i < 24; i++) {
            hourlyDistribution.put(String.format("%02d", i), 0);
        }

        for (String line : lines) {
            // Count log levels
            if (line.contains("ERROR")) {
                errorCount++;
                // Extract error pattern
                String errorPattern = extractErrorPattern(line);
                errorPatterns.merge(errorPattern, 1, Integer::sum);
            } else if (line.contains("WARN")) {
                warningCount++;
            }

            // Extract logger name
            String logger = extractLoggerName(line);
            if (logger != null) {
                loggerCounts.merge(logger, 1, Integer::sum);
            }

            // Extract hour for distribution
            String hour = extractHour(line);
            if (hour != null) {
                hourlyDistribution.merge(hour, 1, Integer::sum);
            }
        }

        stats.put("totalLines", totalLines);
        stats.put("errorCount", errorCount);
        stats.put("warningCount", warningCount);
        stats.put("loggerCounts", loggerCounts);
        stats.put("errorPatterns", errorPatterns);
        stats.put("hourlyDistribution", hourlyDistribution);

        return stats;
    }

    private String extractErrorPattern(String line) {
        // Extract a simplified error pattern for grouping similar errors
        if (line.contains("Exception")) {
            return "Exception";
        } else if (line.contains("Connection refused")) {
            return "Connection refused";
        } else if (line.contains("Timeout")) {
            return "Timeout";
        } else if (line.contains("OutOfMemoryError")) {
            return "OutOfMemoryError";
        } else {
            return "Other Error";
        }
    }

    private String extractLoggerName(String line) {
        // Extract logger name from log line
        // This is a simplified extraction - in a real implementation you'd use regex
        if (line.contains("org.openhab")) {
            int start = line.indexOf("org.openhab");
            int end = line.indexOf(" ", start);
            if (end > start) {
                return line.substring(start, end);
            }
        }
        return null;
    }

    private String extractHour(String line) {
        // Extract hour from timestamp
        if (line.length() > 13) {
            String timePart = line.substring(11, 13);
            if (timePart.matches("d{2}")) {
                return timePart;
            }
        }
        return null;
    }

    private List<Map<String, Object>> getTopEntries(Map<String, Integer> entries, int maxCount) {
        return entries.entrySet().stream().sorted(Map.Entry.<String, Integer> comparingByValue().reversed())
                .limit(maxCount).map(entry -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("name", entry.getKey());
                    result.put("count", entry.getValue());
                    return result;
                }).toList();
    }

    private double calculateLinesPerHour(long totalLines, String timeRange) {
        // Simplified calculation - in a real implementation you'd parse the time range
        int hours = switch (timeRange) {
            case "1h" -> 1;
            case "24h" -> 24;
            case "7d" -> 24 * 7;
            case "30d" -> 24 * 30;
            default -> 24;
        };
        return hours > 0 ? (double) totalLines / hours : 0.0;
    }

    private Map<String, Object> calculateErrorTrend(Map<String, Integer> errorPatterns) {
        // Simplified trend calculation
        Map<String, Object> trend = new HashMap<>();
        trend.put("totalErrors", errorPatterns.values().stream().mapToInt(Integer::intValue).sum());
        trend.put("errorTypes", errorPatterns.size());
        trend.put("mostCommonError", errorPatterns.entrySet().stream().max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey).orElse("None"));
        return trend;
    }

    private List<String> findPeakActivityHours(Map<String, Integer> hourlyDistribution) {
        int maxCount = hourlyDistribution.values().stream().mapToInt(Integer::intValue).max().orElse(0);
        return hourlyDistribution.entrySet().stream().filter(entry -> entry.getValue() == maxCount)
                .map(Map.Entry::getKey).toList();
    }
}
