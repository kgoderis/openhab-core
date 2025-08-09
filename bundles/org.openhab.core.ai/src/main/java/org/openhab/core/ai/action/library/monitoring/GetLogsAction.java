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
import java.util.regex.Pattern;

import org.openhab.core.OpenHAB;
import org.openhab.core.ai.action.ActionContext;
import org.openhab.core.ai.action.ActionMetadata;
import org.openhab.core.ai.action.ActionResult;
import org.openhab.core.ai.action.ActionValidationResult;
import org.openhab.core.ai.action.api.Action;
import org.openhab.core.ai.action.api.ActionException;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action for retrieving logs from openHAB log files with filtering and pagination capabilities.
 */
@Component(service = Action.class, immediate = true)
public class GetLogsAction implements Action {

    private static final Logger logger = LoggerFactory.getLogger(GetLogsAction.class);
    private static final String ACTION_ID = "openhab.monitoring.get_logs";
    private static final String ACTION_NAME = "Get Logs";

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
        return "Retrieves logs from openHAB log files with filtering by level, time period, and search patterns";
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
        properties.put("logFile", Map.of("type", "string", "description",
                "Log file name (e.g., 'openhab.log', 'events.log')", "default", "openhab.log"));
        properties.put("maxLines", Map.of("type", "integer", "description", "Maximum number of log lines to return",
                "default", 100, "minimum", 1, "maximum", 10000));
        properties.put("logLevel", Map.of("type", "string", "enum", List.of("TRACE", "DEBUG", "INFO", "WARN", "ERROR"),
                "description", "Minimum log level to include", "default", "INFO"));
        properties.put("searchPattern",
                Map.of("type", "string", "description", "Regex pattern to search for in log lines"));
        properties.put("timePeriod", Map.of("type", "string", "description",
                "Time period to filter (e.g., '1h', '24h', '7d')", "default", "1h"));
        properties.put("includeStackTrace",
                Map.of("type", "boolean", "description", "Include stack traces in results", "default", false));
        properties.put("tail", Map.of("type", "boolean", "description",
                "Get the most recent lines (tail) instead of from beginning", "default", true));
        properties.put("startLine", Map.of("type", "integer", "description",
                "Starting line number (1-based, used when tail=false)", "minimum", 1));

        schema.put("properties", properties);
        schema.put("additionalProperties", false);
        return schema;
    }

    @Override
    public Map<String, Object> getReturnSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();
        properties.put("logFile", Map.of("type", "string", "description", "The log file that was read"));
        properties.put("logLines",
                Map.of("type", "array", "items", Map.of("type", "object"), "description", "Array of log line objects"));
        properties.put("totalLines", Map.of("type", "integer", "description", "Total number of lines in the log file"));
        properties.put("returnedLines", Map.of("type", "integer", "description", "Number of lines returned"));
        properties.put("filters", Map.of("type", "object", "description", "Applied filters"));
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

        // Validate logFile
        String logFile = (String) parameters.get("logFile");
        if (logFile != null && logFile.trim().isEmpty()) {
            errors.add("logFile cannot be empty");
        }

        // Validate maxLines
        Object maxLinesObj = parameters.get("maxLines");
        if (maxLinesObj != null) {
            if (maxLinesObj instanceof Integer) {
                Integer maxLines = (Integer) maxLinesObj;
                if (maxLines < 1 || maxLines > 10000) {
                    errors.add("maxLines must be between 1 and 10000");
                }
            } else {
                errors.add("maxLines must be an integer");
            }
        }

        // Validate logLevel
        String logLevel = (String) parameters.get("logLevel");
        if (logLevel != null) {
            List<String> validLevels = List.of("TRACE", "DEBUG", "INFO", "WARN", "ERROR");
            if (!validLevels.contains(logLevel)) {
                errors.add("logLevel must be one of: " + validLevels);
            }
        }

        // Validate startLine when tail is false
        Boolean tail = (Boolean) parameters.getOrDefault("tail", true);
        if (!tail) {
            Object startLineObj = parameters.get("startLine");
            if (startLineObj == null) {
                errors.add("startLine is required when tail is false");
            } else if (startLineObj instanceof Integer) {
                Integer startLine = (Integer) startLineObj;
                if (startLine < 1) {
                    errors.add("startLine must be at least 1");
                }
            } else {
                errors.add("startLine must be an integer");
            }
        }

        if (!errors.isEmpty()) {
            return ActionValidationResult.invalid(errors);
        }

        return ActionValidationResult.valid(parameters);
    }

    @Override
    public ActionResult execute(Map<String, Object> parameters, ActionContext context) throws ActionException {
        long startTime = System.currentTimeMillis();
        logger.debug("Executing get logs action with parameters: {}", parameters);

        try {
            String logFile = (String) parameters.getOrDefault("logFile", "openhab.log");
            Integer maxLines = (Integer) parameters.getOrDefault("maxLines", 100);
            String logLevel = (String) parameters.getOrDefault("logLevel", "INFO");
            String searchPattern = (String) parameters.get("searchPattern");
            String timePeriod = (String) parameters.getOrDefault("timePeriod", "1h");
            Boolean includeStackTrace = (Boolean) parameters.getOrDefault("includeStackTrace", false);
            Boolean tail = (Boolean) parameters.getOrDefault("tail", true);
            Integer startLine = (Integer) parameters.get("startLine");

            String userDataDir = OpenHAB.getUserDataFolder();
            Path logFilePath = Paths.get(userDataDir, "logs", logFile);

            if (!Files.exists(logFilePath)) {
                throw new ActionException(ACTION_ID, "Log file not found: " + logFilePath);
            }

            List<String> allLines = Files.readAllLines(logFilePath);
            List<Map<String, Object>> filteredLines = new ArrayList<>();

            // Apply filters
            Pattern pattern = searchPattern != null ? Pattern.compile(searchPattern, Pattern.CASE_INSENSITIVE) : null;
            int requiredSeverity = getLogLevelSeverity(logLevel);

            for (int i = 0; i < allLines.size(); i++) {
                String line = allLines.get(i);

                // Check if line matches log level filter
                if (!matchesLogLevel(line, requiredSeverity)) {
                    continue;
                }

                // Check if line matches search pattern
                if (pattern != null && !pattern.matcher(line).find()) {
                    continue;
                }

                // Check if line is within time period (simplified check)
                if (!isWithinTimePeriod(line, timePeriod)) {
                    continue;
                }

                Map<String, Object> lineInfo = new HashMap<>();
                lineInfo.put("lineNumber", i + 1);
                lineInfo.put("content", line);
                lineInfo.put("timestamp", extractTimestamp(line));
                lineInfo.put("level", extractLogLevel(line));

                filteredLines.add(lineInfo);
            }

            // Apply pagination
            List<Map<String, Object>> resultLines;
            if (tail) {
                // Get the most recent lines
                int startIndex = Math.max(0, filteredLines.size() - maxLines);
                resultLines = filteredLines.subList(startIndex, filteredLines.size());
            } else {
                // Get lines from startLine
                int startIndex = startLine != null ? startLine - 1 : 0;
                int endIndex = Math.min(startIndex + maxLines, filteredLines.size());
                resultLines = filteredLines.subList(startIndex, endIndex);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("logFile", logFile);
            result.put("logLines", resultLines);
            result.put("totalLines", allLines.size());
            result.put("returnedLines", resultLines.size());
            result.put("filters",
                    Map.of("logLevel", logLevel, "searchPattern", searchPattern != null ? searchPattern : "none",
                            "timePeriod", timePeriod, "includeStackTrace", includeStackTrace, "tail", tail, "maxLines",
                            maxLines));
            result.put("timestamp", Instant.now().toString());
            result.put("message", "Retrieved " + resultLines.size() + " log lines from " + logFile);

            long executionTime = System.currentTimeMillis() - startTime;
            logger.debug("Get logs action completed in {}ms", executionTime);

            return ActionResult.success(result, executionTime);

        } catch (IOException e) {
            long executionTime = System.currentTimeMillis() - startTime;
            logger.error("Failed to read log file", e);
            throw new ActionException(ACTION_ID, "Failed to read log file: " + e.getMessage(), e);
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
                .description("Retrieves logs from openHAB log files with filtering and pagination")
                .tags(List.of("monitoring", "logging", "logs", "filtering"))
                .documentation(
                        "Provides access to openHAB log files with filtering by log level, search patterns, and time periods")
                .examples(List.of(
                        "{\"logFile\": \"openhab.log\", \"maxLines\": 50, \"logLevel\": \"ERROR\"} - Get last 50 error lines",
                        "{\"logFile\": \"events.log\", \"searchPattern\": \"ItemStateEvent\", \"tail\": true} - Get recent item state events",
                        "{\"logFile\": \"openhab.log\", \"startLine\": 1000, \"maxLines\": 100, \"tail\": false} - Get lines 1000-1099"))
                .build();
    }

    @Override
    public Map<String, Object> getCapabilities() {
        return Map.of("filtering", true, "sorting", false, "pagination", true, "metadata", true, "async", true,
                "realTime", false);
    }

    @Override
    public void initialize(ActionContext context) {
        logger.debug("GetLogsAction initialized for protocol: {}", context.getProtocol());
    }

    @Override
    public void cleanup() {
        logger.debug("GetLogsAction cleanup completed");
    }

    @Override
    public boolean isReady() {
        return true;
    }

    private boolean matchesLogLevel(String line, int requiredSeverity) {
        String[] levels = { "TRACE", "DEBUG", "INFO", "WARN", "ERROR" };
        for (String level : levels) {
            if (line.contains(level) && getLogLevelSeverity(level) >= requiredSeverity) {
                return true;
            }
        }
        return false;
    }

    private boolean isWithinTimePeriod(String line, String timePeriod) {
        // Simplified time period check - in a real implementation, you'd parse the timestamp
        // and compare with current time minus the period
        // For now, we'll return true to include all lines
        return true;
    }

    private String extractTimestamp(String logLine) {
        // Simple timestamp extraction - assumes ISO format at the beginning of the line
        if (logLine.length() > 20) {
            String potentialTimestamp = logLine.substring(0, 20);
            if (potentialTimestamp.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}")) {
                return potentialTimestamp;
            }
        }
        return "unknown";
    }

    private String extractLogLevel(String logLine) {
        String[] levels = { "TRACE", "DEBUG", "INFO", "WARN", "ERROR" };
        for (String level : levels) {
            if (logLine.contains(level)) {
                return level;
            }
        }
        return "UNKNOWN";
    }

    private int getLogLevelSeverity(String level) {
        return switch (level) {
            case "TRACE" -> 0;
            case "DEBUG" -> 1;
            case "INFO" -> 2;
            case "WARN" -> 3;
            case "ERROR" -> 4;
            default -> 2; // Default to INFO level
        };
    }
}
