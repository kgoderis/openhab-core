package org.openhab.core.ai.action.library.monitoring;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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
import org.openhab.core.ai.api.action.Action;
import org.openhab.core.ai.api.action.ActionException;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action for searching log files in openHAB with advanced filtering and search capabilities.
 */
@Component(service = Action.class, immediate = true)
public class SearchLogsAction implements Action {

    private static final Logger logger = LoggerFactory.getLogger(SearchLogsAction.class);
    private static final String ACTION_ID = "openhab.monitoring.search_logs";
    private static final String ACTION_NAME = "Search Logs";

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
        return "Searches log files with advanced filtering, regex support, and time-based queries";
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
        properties.put("query", Map.of("type", "string", "description", "Search query (text or regex pattern)"));
        properties.put("logFiles",
                Map.of("type", "array", "items", Map.of("type", "string"), "description",
                        "List of log files to search (e.g., ['openhab.log', 'events.log'])", "default",
                        List.of("openhab.log")));
        properties.put("logLevel", Map.of("type", "string", "enum", List.of("TRACE", "DEBUG", "INFO", "WARN", "ERROR"),
                "description", "Filter by minimum log level"));
        properties.put("startTime",
                Map.of("type", "string", "description", "Start time for search (ISO format: 2023-01-01T00:00:00)"));
        properties.put("endTime",
                Map.of("type", "string", "description", "End time for search (ISO format: 2023-01-01T23:59:59)"));
        properties.put("maxResults", Map.of("type", "integer", "description", "Maximum number of results to return",
                "default", 100, "minimum", 1, "maximum", 10000));
        properties.put("caseSensitive",
                Map.of("type", "boolean", "description", "Case-sensitive search", "default", false));
        properties.put("useRegex",
                Map.of("type", "boolean", "description", "Treat query as regex pattern", "default", false));
        properties.put("includeContext",
                Map.of("type", "boolean", "description", "Include surrounding context lines", "default", false));
        properties.put("contextLines", Map.of("type", "integer", "description", "Number of context lines to include",
                "default", 2, "minimum", 0, "maximum", 10));
        properties.put("sortBy", Map.of("type", "string", "enum", List.of("timestamp", "file", "level"), "description",
                "Sort results by", "default", "timestamp"));
        properties.put("sortOrder", Map.of("type", "string", "enum", List.of("asc", "desc"), "description",
                "Sort order", "default", "desc"));

        schema.put("properties", properties);
        schema.put("required", List.of("query"));
        schema.put("additionalProperties", false);
        return schema;
    }

    @Override
    public Map<String, Object> getReturnSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();
        properties.put("query", Map.of("type", "string", "description", "The search query that was executed"));
        properties.put("results",
                Map.of("type", "array", "items", Map.of("type", "object"), "description", "Search results"));
        properties.put("totalResults", Map.of("type", "integer", "description", "Total number of results found"));
        properties.put("returnedResults", Map.of("type", "integer", "description", "Number of results returned"));
        properties.put("searchedFiles", Map.of("type", "array", "description", "List of files that were searched"));
        properties.put("filters", Map.of("type", "object", "description", "Applied filters"));
        properties.put("executionTime",
                Map.of("type", "number", "description", "Search execution time in milliseconds"));
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

        // Validate query
        String query = (String) parameters.get("query");
        if (query == null || query.trim().isEmpty()) {
            errors.add("query is required and cannot be empty");
        }

        // Validate maxResults
        Object maxResultsObj = parameters.get("maxResults");
        if (maxResultsObj != null) {
            if (maxResultsObj instanceof Integer) {
                Integer maxResults = (Integer) maxResultsObj;
                if (maxResults < 1 || maxResults > 10000) {
                    errors.add("maxResults must be between 1 and 10000");
                }
            } else {
                errors.add("maxResults must be an integer");
            }
        }

        // Validate contextLines
        Object contextLinesObj = parameters.get("contextLines");
        if (contextLinesObj != null) {
            if (contextLinesObj instanceof Integer) {
                Integer contextLines = (Integer) contextLinesObj;
                if (contextLines < 0 || contextLines > 10) {
                    errors.add("contextLines must be between 0 and 10");
                }
            } else {
                errors.add("contextLines must be an integer");
            }
        }

        // Validate time format
        String startTime = (String) parameters.get("startTime");
        if (startTime != null && !isValidTimeFormat(startTime)) {
            errors.add("startTime must be in ISO format (YYYY-MM-DDTHH:mm:ss)");
        }

        String endTime = (String) parameters.get("endTime");
        if (endTime != null && !isValidTimeFormat(endTime)) {
            errors.add("endTime must be in ISO format (YYYY-MM-DDTHH:mm:ss)");
        }

        if (!errors.isEmpty()) {
            return ActionValidationResult.invalid(errors);
        }

        return ActionValidationResult.valid(parameters);
    }

    @Override
    public ActionResult execute(Map<String, Object> parameters, ActionContext context) throws ActionException {
        long startTime = System.currentTimeMillis();
        logger.debug("Executing search logs action with parameters: {}", parameters);

        try {
            String query = (String) parameters.get("query");
            @SuppressWarnings("unchecked")
            List<String> logFiles = (List<String>) parameters.getOrDefault("logFiles", List.of("openhab.log"));
            String logLevel = (String) parameters.get("logLevel");
            String startTimeStr = (String) parameters.get("startTime");
            String endTimeStr = (String) parameters.get("endTime");
            Integer maxResults = (Integer) parameters.getOrDefault("maxResults", 100);
            Boolean caseSensitive = (Boolean) parameters.getOrDefault("caseSensitive", false);
            Boolean useRegex = (Boolean) parameters.getOrDefault("useRegex", false);
            Boolean includeContext = (Boolean) parameters.getOrDefault("includeContext", false);
            Integer contextLines = (Integer) parameters.getOrDefault("contextLines", 2);
            String sortBy = (String) parameters.getOrDefault("sortBy", "timestamp");
            String sortOrder = (String) parameters.getOrDefault("sortOrder", "desc");

            String userDataDir = OpenHAB.getUserDataFolder();
            Path logsDir = Paths.get(userDataDir, "logs");

            // Parse time filters
            LocalDateTime startTimeFilter = startTimeStr != null ? parseTime(startTimeStr) : null;
            LocalDateTime endTimeFilter = endTimeStr != null ? parseTime(endTimeStr) : null;

            // Compile search pattern
            Pattern searchPattern;
            if (useRegex) {
                int flags = caseSensitive ? 0 : Pattern.CASE_INSENSITIVE;
                searchPattern = Pattern.compile(query, flags);
            } else {
                String escapedQuery = Pattern.quote(query);
                int flags = caseSensitive ? 0 : Pattern.CASE_INSENSITIVE;
                searchPattern = Pattern.compile(escapedQuery, flags);
            }

            List<Map<String, Object>> allResults = new ArrayList<>();
            List<String> searchedFiles = new ArrayList<>();

            // Search each log file
            for (String logFile : logFiles) {
                Path logFilePath = logsDir.resolve(logFile);
                if (Files.exists(logFilePath)) {
                    List<Map<String, Object>> fileResults = searchFile(logFilePath, searchPattern, logLevel,
                            startTimeFilter, endTimeFilter, includeContext, contextLines);
                    allResults.addAll(fileResults);
                    searchedFiles.add(logFile);
                } else {
                    logger.warn("Log file not found: {}", logFilePath);
                }
            }

            // Sort results
            sortResults(allResults, sortBy, sortOrder);

            // Limit results
            List<Map<String, Object>> limitedResults = allResults.stream().limit(maxResults).toList();

            Map<String, Object> result = new HashMap<>();
            result.put("query", query);
            result.put("results", limitedResults);
            result.put("totalResults", allResults.size());
            result.put("returnedResults", limitedResults.size());
            result.put("searchedFiles", searchedFiles);
            result.put("filters",
                    Map.of("logLevel", logLevel != null ? logLevel : "none", "startTime",
                            startTimeStr != null ? startTimeStr : "none", "endTime",
                            endTimeStr != null ? endTimeStr : "none", "caseSensitive", caseSensitive, "useRegex",
                            useRegex, "includeContext", includeContext, "contextLines", contextLines));
            result.put("executionTime", System.currentTimeMillis() - startTime);
            result.put("timestamp", Instant.now().toString());
            result.put("message", "Found " + allResults.size() + " results, returned " + limitedResults.size());

            long executionTime = System.currentTimeMillis() - startTime;
            logger.debug("Search logs action completed in {}ms", executionTime);

            return ActionResult.success(result, executionTime);

        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            logger.error("Failed to search logs", e);
            throw new ActionException(ACTION_ID, "Failed to search logs: " + e.getMessage(), e);
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
                .description("Searches log files with advanced filtering and regex support")
                .tags(List.of("monitoring", "logging", "search", "filtering"))
                .documentation(
                        "Provides powerful search capabilities for log files including regex patterns, time filtering, and context lines")
                .examples(List.of("{\"query\": \"ERROR\", \"logLevel\": \"ERROR\"} - Search for ERROR entries",
                        "{\"query\": \"ItemStateEvent\", \"useRegex\": true, \"includeContext\": true} - Search for ItemStateEvent with context",
                        "{\"query\": \"Exception\", \"startTime\": \"2023-01-01T00:00:00\", \"endTime\": \"2023-01-01T23:59:59\"} - Search for exceptions on specific date"))
                .build();
    }

    @Override
    public Map<String, Object> getCapabilities() {
        return Map.of("filtering", true, "sorting", true, "pagination", true, "metadata", true, "async", true,
                "realTime", false);
    }

    @Override
    public void initialize(ActionContext context) {
        logger.debug("SearchLogsAction initialized for protocol: {}", context.getProtocol());
    }

    @Override
    public void cleanup() {
        logger.debug("SearchLogsAction cleanup completed");
    }

    @Override
    public boolean isReady() {
        return true;
    }

    private List<Map<String, Object>> searchFile(Path logFilePath, Pattern searchPattern, String logLevel,
            LocalDateTime startTime, LocalDateTime endTime, boolean includeContext, int contextLines)
            throws IOException {
        List<Map<String, Object>> results = new ArrayList<>();
        List<String> allLines = Files.readAllLines(logFilePath);

        int requiredSeverity = logLevel != null ? getLogLevelSeverity(logLevel) : 0;

        for (int i = 0; i < allLines.size(); i++) {
            String line = allLines.get(i);

            // Check log level filter
            if (logLevel != null && !matchesLogLevel(line, requiredSeverity)) {
                continue;
            }

            // Check time filter
            if ((startTime != null || endTime != null) && !isWithinTimeRange(line, startTime, endTime)) {
                continue;
            }

            // Check search pattern
            if (searchPattern.matcher(line).find()) {
                Map<String, Object> result = new HashMap<>();
                result.put("file", logFilePath.getFileName().toString());
                result.put("lineNumber", i + 1);
                result.put("content", line);
                result.put("timestamp", extractTimestamp(line));
                result.put("level", extractLogLevel(line));

                // Add context if requested
                if (includeContext && contextLines > 0) {
                    List<String> context = new ArrayList<>();
                    int start = Math.max(0, i - contextLines);
                    int end = Math.min(allLines.size(), i + contextLines + 1);

                    for (int j = start; j < end; j++) {
                        if (j != i) { // Don't include the matched line in context
                            context.add(allLines.get(j));
                        }
                    }
                    result.put("context", context);
                }

                results.add(result);
            }
        }

        return results;
    }

    private void sortResults(List<Map<String, Object>> results, String sortBy, String sortOrder) {
        results.sort((r1, r2) -> {
            int comparison = 0;

            switch (sortBy) {
                case "timestamp":
                    String t1 = (String) r1.get("timestamp");
                    String t2 = (String) r2.get("timestamp");
                    comparison = t1.compareTo(t2);
                    break;
                case "file":
                    String f1 = (String) r1.get("file");
                    String f2 = (String) r2.get("file");
                    comparison = f1.compareTo(f2);
                    break;
                case "level":
                    String l1 = (String) r1.get("level");
                    String l2 = (String) r2.get("level");
                    comparison = getLogLevelSeverity(l1) - getLogLevelSeverity(l2);
                    break;
                default:
                    comparison = 0;
            }

            return "desc".equals(sortOrder) ? -comparison : comparison;
        });
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

    private boolean isWithinTimeRange(String line, LocalDateTime startTime, LocalDateTime endTime) {
        String timestamp = extractTimestamp(line);
        if ("unknown".equals(timestamp)) {
            return true; // Include lines without timestamp
        }

        try {
            LocalDateTime lineTime = parseTime(timestamp);
            return (startTime == null || !lineTime.isBefore(startTime))
                    && (endTime == null || !lineTime.isAfter(endTime));
        } catch (DateTimeParseException e) {
            return true; // Include lines with invalid timestamp
        }
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

    private boolean isValidTimeFormat(String time) {
        try {
            parseTime(time);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    private LocalDateTime parseTime(String time) {
        return LocalDateTime.parse(time, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}
