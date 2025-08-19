package org.openhab.core.ai.action.library.monitoring;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;
import java.util.stream.Stream;

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
 * Action for managing openHAB logging and monitoring including log management, system metrics, and monitoring data.
 * 
 * 
 */
@Component(service = Action.class, immediate = true)
public class LoggingMonitoringAction implements Action {

    private static final Logger logger = LoggerFactory.getLogger(LoggingMonitoringAction.class);
    private static final String ACTION_ID = "openhab.monitoring.logs";
    private static final String ACTION_NAME = "Logging and Monitoring";

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
        return "Manages openHAB logging and monitoring including log viewing, system metrics, performance monitoring, and alerts";
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
        properties.put("action",
                Map.of("type", "string", "enum",
                        List.of("list_logs", "view_log", "search_logs", "system_metrics", "performance_stats",
                                "log_levels", "log_files", "disk_usage", "memory_stats", "alert_patterns"),
                        "description", "Logging and monitoring action to perform"));
        properties.put("logFile", Map.of("type", "string", "description", "Specific log file name to view"));
        properties.put("maxLines",
                Map.of("type", "integer", "description", "Maximum number of log lines to return", "default", 100));
        properties.put("searchPattern", Map.of("type", "string", "description", "Pattern to search for in logs"));
        properties.put("logLevel", Map.of("type", "string", "enum", List.of("TRACE", "DEBUG", "INFO", "WARN", "ERROR"),
                "description", "Log level filter", "default", "INFO"));
        properties.put("timePeriod", Map.of("type", "string", "description",
                "Time period for filtering (e.g., '1h', '24h')", "default", "1h"));
        properties.put("includeStackTrace",
                Map.of("type", "boolean", "description", "Include stack traces in error logs", "default", false));

        schema.put("properties", properties);
        schema.put("required", List.of("action"));
        schema.put("additionalProperties", false);
        return schema;
    }

    @Override
    public Map<String, Object> getReturnSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();
        properties.put("action", Map.of("type", "string", "description", "The action that was performed"));
        properties.put("timestamp", Map.of("type", "string", "description", "ISO timestamp of the operation"));
        properties.put("logFiles", Map.of("type", "array", "description", "List of log files (for list_logs action)"));
        properties.put("logLines", Map.of("type", "array", "description", "Log lines content (for view_log action)"));
        properties.put("searchResults",
                Map.of("type", "array", "description", "Search results (for search_logs action)"));
        properties.put("metrics",
                Map.of("type", "object", "description", "System metrics (for system_metrics action)"));
        properties.put("performance",
                Map.of("type", "object", "description", "Performance statistics (for performance_stats action)"));
        properties.put("logLevels",
                Map.of("type", "object", "description", "Log level information (for log_levels action)"));
        properties.put("diskUsage",
                Map.of("type", "object", "description", "Disk usage information (for disk_usage action)"));
        properties.put("memoryStats",
                Map.of("type", "object", "description", "Memory statistics (for memory_stats action)"));
        properties.put("alertPatterns",
                Map.of("type", "array", "description", "Alert patterns (for alert_patterns action)"));
        properties.put("totalFiles", Map.of("type", "integer", "description", "Total number of files found"));
        properties.put("logsDirectory", Map.of("type", "string", "description", "Path to logs directory"));
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

        String action = (String) parameters.get("action");
        if (action == null) {
            return ActionValidationResult.invalid(List.of("Missing required parameter: action"));
        }

        List<String> validActions = List.of("list_logs", "view_log", "search_logs", "system_metrics",
                "performance_stats", "log_levels", "log_files", "disk_usage", "memory_stats", "alert_patterns");
        if (!validActions.contains(action)) {
            return ActionValidationResult.invalid(List.of("Invalid action. Must be one of: " + validActions));
        }

        if ("view_log".equals(action)) {
            String logFile = (String) parameters.get("logFile");
            if (logFile == null || logFile.trim().isEmpty()) {
                return ActionValidationResult.invalid(List.of("logFile is required for view_log action"));
            }
        }

        if ("search_logs".equals(action)) {
            String searchPattern = (String) parameters.get("searchPattern");
            if (searchPattern == null || searchPattern.trim().isEmpty()) {
                return ActionValidationResult.invalid(List.of("searchPattern is required for search_logs action"));
            }
        }

        return ActionValidationResult.valid(parameters);
    }

    @Override
    public ActionResult execute(Map<String, Object> parameters, ExecutionContext context) throws ActionException {
        long startTime = System.currentTimeMillis();
        logger.debug("Executing logging monitoring action with parameters: {}", parameters);

        try {
            String action = (String) parameters.get("action");
            Map<String, Object> result = switch (action) {
                case "list_logs" -> listLogFiles();
                case "view_log" -> viewLogFile(parameters);
                case "search_logs" -> searchInLogs(parameters);
                case "system_metrics" -> getSystemMetrics();
                case "performance_stats" -> getPerformanceStats();
                case "log_levels" -> getLogLevels();
                case "log_files" -> getLogFilesInfo();
                case "disk_usage" -> getDiskUsage();
                case "memory_stats" -> getMemoryStats();
                case "alert_patterns" -> getAlertPatterns(parameters);
                default -> throw new ActionException(ACTION_ID, "Unknown action: " + action);
            };

            long executionTime = System.currentTimeMillis() - startTime;
            logger.debug("Logging monitoring action '{}' completed in {}ms", action, executionTime);

            return ActionResult.success(result, executionTime);

        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            logger.error("Failed to execute logging/monitoring operation", e);
            throw new ActionException(ACTION_ID, "Failed to execute logging/monitoring operation: " + e.getMessage(),
                    e);
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
                .withDescription("Provides comprehensive logging and monitoring capabilities for openHAB")
                .withTags(List.of("monitoring", "logging", "metrics", "performance", "alerts"))
                .withDocumentation(
                        "Manages openHAB logging and monitoring including log viewing, system metrics, performance monitoring, and alerts")
                .withExamples(List.of("{\"action\": \"list_logs\"} - List all log files",
                        "{\"action\": \"view_log\", \"logFile\": \"openhab.log\", \"maxLines\": 50} - View last 50 lines of openhab.log",
                        "{\"action\": \"search_logs\", \"searchPattern\": \"ERROR\"} - Search for ERROR entries in logs",
                        "{\"action\": \"system_metrics\"} - Get current system metrics",
                        "{\"action\": \"performance_stats\"} - Get performance statistics"))
                .build();
    }

    @Override
    public Map<String, Object> getCapabilities() {
        return Map.of("filtering", true, "sorting", false, "pagination", true, "metadata", true, "async", true);
    }

    @Override
    public void initialize(ExecutionContext context) {
        logger.debug("LoggingMonitoringAction initialized for protocol: {}", context.getProtocol());
    }

    @Override
    public void cleanup() {
        logger.debug("LoggingMonitoringAction cleanup completed");
    }

    @Override
    public boolean isReady() {
        return true; // No external dependencies required
    }

    private Map<String, Object> listLogFiles() {
        Map<String, Object> result = new HashMap<>();
        result.put("action", "list_logs");
        result.put("timestamp", Instant.now().toString());

        String userDataDir = OpenHAB.getUserDataFolder();
        Path logsDir = Paths.get(userDataDir, "logs");

        List<Map<String, Object>> logFiles = new ArrayList<>();

        if (Files.exists(logsDir) && Files.isDirectory(logsDir)) {
            try (Stream<Path> files = Files.list(logsDir)) {
                files.filter(path -> Files.isRegularFile(path) && path.getFileName().toString().endsWith(".log"))
                        .forEach(logFile -> {
                            Map<String, Object> fileInfo = new HashMap<>();
                            fileInfo.put("filename", logFile.getFileName().toString());
                            fileInfo.put("path", logFile.toString());
                            try {
                                fileInfo.put("size", Files.size(logFile));
                                fileInfo.put("lastModified", Files.getLastModifiedTime(logFile).toString());
                            } catch (IOException e) {
                                fileInfo.put("error", "Could not read file info: " + e.getMessage());
                            }
                            logFiles.add(fileInfo);
                        });
            } catch (IOException e) {
                result.put("error", "Failed to list log directory: " + e.getMessage());
            }
        }

        result.put("logFiles", logFiles);
        result.put("totalFiles", logFiles.size());
        result.put("logsDirectory", logsDir.toString());
        result.put("message", "Found " + logFiles.size() + " log files");

        return result;
    }

    private Map<String, Object> viewLogFile(Map<String, Object> parameters) {
        Map<String, Object> result = new HashMap<>();
        result.put("action", "view_log");
        result.put("timestamp", Instant.now().toString());

        String logFileName = (String) parameters.get("logFile");
        Integer maxLines = (Integer) parameters.getOrDefault("maxLines", 100);
        String logLevel = (String) parameters.getOrDefault("logLevel", "INFO");

        String userDataDir = OpenHAB.getUserDataFolder();
        Path logFile = Paths.get(userDataDir, "logs", logFileName);

        result.put("logFile", logFileName);
        result.put("maxLines", maxLines);
        result.put("logLevel", logLevel);

        if (!Files.exists(logFile)) {
            result.put("error", "Log file not found: " + logFile);
            result.put("logLines", List.of());
            return result;
        }

        try {
            List<String> allLines = Files.readAllLines(logFile);
            List<String> filteredLines = allLines.stream().filter(line -> filterByLogLevel(line, logLevel))
                    .skip(Math.max(0, allLines.size() - maxLines)).toList();

            result.put("logLines", filteredLines);
            result.put("totalLines", allLines.size());
            result.put("returnedLines", filteredLines.size());
            result.put("message", "Retrieved " + filteredLines.size() + " lines from " + logFileName);

        } catch (IOException e) {
            result.put("error", "Failed to read log file: " + e.getMessage());
            result.put("logLines", List.of());
        }

        return result;
    }

    private Map<String, Object> searchInLogs(Map<String, Object> parameters) {
        Map<String, Object> result = new HashMap<>();
        result.put("action", "search_logs");
        result.put("timestamp", Instant.now().toString());

        String searchPattern = (String) parameters.get("searchPattern");
        String logLevel = (String) parameters.getOrDefault("logLevel", "INFO");
        String timePeriod = (String) parameters.getOrDefault("timePeriod", "1h");
        boolean includeStackTrace = (Boolean) parameters.getOrDefault("includeStackTrace", false);

        result.put("searchPattern", searchPattern);
        result.put("logLevel", logLevel);
        result.put("timePeriod", timePeriod);

        String userDataDir = OpenHAB.getUserDataFolder();
        Path logsDir = Paths.get(userDataDir, "logs");

        List<Map<String, Object>> searchResults = new ArrayList<>();
        Pattern pattern = Pattern.compile(searchPattern, Pattern.CASE_INSENSITIVE);

        if (Files.exists(logsDir) && Files.isDirectory(logsDir)) {
            try (Stream<Path> files = Files.list(logsDir)) {
                files.filter(path -> Files.isRegularFile(path) && path.getFileName().toString().endsWith(".log"))
                        .forEach(logFile -> {
                            try {
                                List<String> lines = Files.readAllLines(logFile);
                                for (int i = 0; i < lines.size(); i++) {
                                    String line = lines.get(i);
                                    if (pattern.matcher(line).find() && filterByLogLevel(line, logLevel)) {
                                        Map<String, Object> match = new HashMap<>();
                                        match.put("file", logFile.getFileName().toString());
                                        match.put("lineNumber", i + 1);
                                        match.put("content", line);
                                        match.put("timestamp", extractTimestamp(line));
                                        searchResults.add(match);
                                    }
                                }
                            } catch (IOException e) {
                                logger.warn("Failed to search in log file: {}", logFile, e);
                            }
                        });
            } catch (IOException e) {
                result.put("error", "Failed to search log directory: " + e.getMessage());
            }
        }

        result.put("searchResults", searchResults);
        result.put("totalMatches", searchResults.size());
        result.put("message", "Found " + searchResults.size() + " matches for pattern: " + searchPattern);

        return result;
    }

    private Map<String, Object> getSystemMetrics() {
        Map<String, Object> result = new HashMap<>();
        result.put("action", "system_metrics");
        result.put("timestamp", Instant.now().toString());

        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();

        Map<String, Object> metrics = new HashMap<>();
        metrics.put("systemLoadAverage", osBean.getSystemLoadAverage());
        metrics.put("availableProcessors", osBean.getAvailableProcessors());
        metrics.put("uptime", runtimeBean.getUptime());
        metrics.put("uptimeFormatted", formatUptime(runtimeBean.getUptime()));

        // Try to get additional metrics if available
        try {
            if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
                com.sun.management.OperatingSystemMXBean sunBean = (com.sun.management.OperatingSystemMXBean) osBean;
                metrics.put("processCpuLoad", sunBean.getProcessCpuLoad());
                metrics.put("systemCpuLoad", sunBean.getSystemCpuLoad());
                metrics.put("totalPhysicalMemory", sunBean.getTotalPhysicalMemorySize());
                metrics.put("freePhysicalMemory", sunBean.getFreePhysicalMemorySize());
                metrics.put("totalSwapSpace", sunBean.getTotalSwapSpaceSize());
                metrics.put("freeSwapSpace", sunBean.getFreeSwapSpaceSize());
            }
        } catch (Exception e) {
            logger.debug("Platform-specific metrics not available: {}", e.getMessage());
        }

        result.put("metrics", metrics);
        result.put("message", "System metrics collected successfully");

        return result;
    }

    private Map<String, Object> getPerformanceStats() {
        Map<String, Object> result = new HashMap<>();
        result.put("action", "performance_stats");
        result.put("timestamp", Instant.now().toString());

        RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();

        Map<String, Object> performance = new HashMap<>();
        performance.put("startTime", runtimeBean.getStartTime());
        performance.put("startTimeFormatted", Instant.ofEpochMilli(runtimeBean.getStartTime()).toString());
        performance.put("uptime", runtimeBean.getUptime());
        performance.put("uptimeFormatted", formatUptime(runtimeBean.getUptime()));

        // Memory statistics
        Map<String, Object> memoryStats = new HashMap<>();
        memoryStats.put("heapMemoryUsage", formatMemoryUsage(memoryBean.getHeapMemoryUsage()));
        memoryStats.put("nonHeapMemoryUsage", formatMemoryUsage(memoryBean.getNonHeapMemoryUsage()));
        performance.put("memory", memoryStats);

        result.put("performance", performance);
        result.put("message", "Performance statistics collected successfully");

        return result;
    }

    private Map<String, Object> getLogLevels() {
        Map<String, Object> result = new HashMap<>();
        result.put("action", "log_levels");
        result.put("timestamp", Instant.now().toString());

        Map<String, Object> logLevels = new HashMap<>();
        logLevels.put("TRACE",
                Map.of("description", getLogLevelDescription("TRACE"), "severity", getLogLevelSeverity("TRACE")));
        logLevels.put("DEBUG",
                Map.of("description", getLogLevelDescription("DEBUG"), "severity", getLogLevelSeverity("DEBUG")));
        logLevels.put("INFO",
                Map.of("description", getLogLevelDescription("INFO"), "severity", getLogLevelSeverity("INFO")));
        logLevels.put("WARN",
                Map.of("description", getLogLevelDescription("WARN"), "severity", getLogLevelSeverity("WARN")));
        logLevels.put("ERROR",
                Map.of("description", getLogLevelDescription("ERROR"), "severity", getLogLevelSeverity("ERROR")));

        result.put("logLevels", logLevels);
        result.put("message", "Log level information retrieved successfully");

        return result;
    }

    private Map<String, Object> getLogFilesInfo() {
        Map<String, Object> result = new HashMap<>();
        result.put("action", "log_files");
        result.put("timestamp", Instant.now().toString());

        String userDataDir = OpenHAB.getUserDataFolder();
        Path logsDir = Paths.get(userDataDir, "logs");

        Map<String, Object> logFilesInfo = new HashMap<>();
        AtomicLong totalSize = new AtomicLong(0);
        AtomicInteger totalFiles = new AtomicInteger(0);

        if (Files.exists(logsDir) && Files.isDirectory(logsDir)) {
            try (Stream<Path> files = Files.list(logsDir)) {
                files.filter(path -> Files.isRegularFile(path) && path.getFileName().toString().endsWith(".log"))
                        .forEach(logFile -> {
                            try {
                                long size = Files.size(logFile);
                                totalSize.addAndGet(size);
                                totalFiles.incrementAndGet();

                                Map<String, Object> fileInfo = new HashMap<>();
                                fileInfo.put("size", size);
                                fileInfo.put("sizeFormatted", formatBytes(size));
                                fileInfo.put("lastModified", Files.getLastModifiedTime(logFile).toString());
                                fileInfo.put("readable", Files.isReadable(logFile));
                                fileInfo.put("writable", Files.isWritable(logFile));

                                logFilesInfo.put(logFile.getFileName().toString(), fileInfo);
                            } catch (IOException e) {
                                logger.warn("Failed to get info for log file: {}", logFile, e);
                            }
                        });
            } catch (IOException e) {
                result.put("error", "Failed to get log files info: " + e.getMessage());
            }
        }

        result.put("logFilesInfo", logFilesInfo);
        result.put("totalFiles", totalFiles.get());
        result.put("totalSize", totalSize.get());
        result.put("totalSizeFormatted", formatBytes(totalSize.get()));
        result.put("message", "Log files information collected successfully");

        return result;
    }

    private Map<String, Object> getDiskUsage() {
        Map<String, Object> result = new HashMap<>();
        result.put("action", "disk_usage");
        result.put("timestamp", Instant.now().toString());

        String userDataDir = OpenHAB.getUserDataFolder();
        Path userDataPath = Paths.get(userDataDir);

        Map<String, Object> diskUsage = new HashMap<>();
        try {
            long totalSpace = Files.getFileStore(userDataPath).getTotalSpace();
            long usableSpace = Files.getFileStore(userDataPath).getUsableSpace();
            long usedSpace = totalSpace - usableSpace;

            diskUsage.put("totalSpace", totalSpace);
            diskUsage.put("totalSpaceFormatted", formatBytes(totalSpace));
            diskUsage.put("usableSpace", usableSpace);
            diskUsage.put("usableSpaceFormatted", formatBytes(usableSpace));
            diskUsage.put("usedSpace", usedSpace);
            diskUsage.put("usedSpaceFormatted", formatBytes(usedSpace));
            diskUsage.put("usagePercentage", String.format("%.2f%%", (usedSpace * 100.0) / totalSpace));

            result.put("diskUsage", diskUsage);
            result.put("message", "Disk usage information collected successfully");
        } catch (IOException e) {
            result.put("error", "Failed to get disk usage: " + e.getMessage());
        }

        return result;
    }

    private Map<String, Object> getMemoryStats() {
        Map<String, Object> result = new HashMap<>();
        result.put("action", "memory_stats");
        result.put("timestamp", Instant.now().toString());

        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        Runtime runtime = Runtime.getRuntime();

        Map<String, Object> memoryStats = new HashMap<>();
        memoryStats.put("heapMemoryUsage", formatMemoryUsage(memoryBean.getHeapMemoryUsage()));
        memoryStats.put("nonHeapMemoryUsage", formatMemoryUsage(memoryBean.getNonHeapMemoryUsage()));

        // Runtime memory info
        Map<String, Object> runtimeMemory = new HashMap<>();
        runtimeMemory.put("totalMemory", runtime.totalMemory());
        runtimeMemory.put("totalMemoryFormatted", formatBytes(runtime.totalMemory()));
        runtimeMemory.put("freeMemory", runtime.freeMemory());
        runtimeMemory.put("freeMemoryFormatted", formatBytes(runtime.freeMemory()));
        runtimeMemory.put("maxMemory", runtime.maxMemory());
        runtimeMemory.put("maxMemoryFormatted", formatBytes(runtime.maxMemory()));
        runtimeMemory.put("usedMemory", runtime.totalMemory() - runtime.freeMemory());
        runtimeMemory.put("usedMemoryFormatted", formatBytes(runtime.totalMemory() - runtime.freeMemory()));

        memoryStats.put("runtimeMemory", runtimeMemory);
        result.put("memoryStats", memoryStats);
        result.put("message", "Memory statistics collected successfully");

        return result;
    }

    private Map<String, Object> getAlertPatterns(Map<String, Object> parameters) {
        Map<String, Object> result = new HashMap<>();
        result.put("action", "alert_patterns");
        result.put("timestamp", Instant.now().toString());

        List<Map<String, Object>> alertPatterns = new ArrayList<>();

        // Common error patterns
        alertPatterns.add(Map.of("pattern", "ERROR", "description", "Error level log entries", "severity", "high",
                "category", "error"));

        alertPatterns.add(Map.of("pattern", "Exception", "description", "Java exceptions", "severity", "high",
                "category", "exception"));

        alertPatterns.add(Map.of("pattern", "OutOfMemoryError", "description", "Memory exhaustion errors", "severity",
                "critical", "category", "memory"));

        alertPatterns.add(Map.of("pattern", "Connection refused", "description", "Network connection failures",
                "severity", "medium", "category", "network"));

        alertPatterns.add(Map.of("pattern", "Timeout", "description", "Operation timeout errors", "severity", "medium",
                "category", "timeout"));

        result.put("alertPatterns", alertPatterns);
        result.put("totalPatterns", alertPatterns.size());
        result.put("message", "Alert patterns retrieved successfully");

        return result;
    }

    private boolean filterByLogLevel(String line, String logLevel) {
        int requiredSeverity = getLogLevelSeverity(logLevel);
        String[] levels = { "TRACE", "DEBUG", "INFO", "WARN", "ERROR" };

        for (String level : levels) {
            if (line.contains(level) && getLogLevelSeverity(level) >= requiredSeverity) {
                return true;
            }
        }
        return false;
    }

    private String extractTimestamp(String logLine) {
        // Simple timestamp extraction - assumes ISO format at the beginning of the line
        if (logLine.length() > 20) {
            String potentialTimestamp = logLine.substring(0, 20);
            if (potentialTimestamp.matches("d{4}-d{2}-d{2}Td{2}:d{2}:d{2}")) {
                return potentialTimestamp;
            }
        }
        return "unknown";
    }

    private String getLogLevelDescription(String level) {
        return switch (level) {
            case "TRACE" -> "Most detailed logging level";
            case "DEBUG" -> "Detailed information for debugging";
            case "INFO" -> "General information messages";
            case "WARN" -> "Warning messages";
            case "ERROR" -> "Error messages";
            default -> "Unknown log level";
        };
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

    private String formatUptime(long uptimeMs) {
        long seconds = uptimeMs / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) {
            return String.format("%dd %dh %dm %ds", days, hours % 24, minutes % 60, seconds % 60);
        } else if (hours > 0) {
            return String.format("%dh %dm %ds", hours, minutes % 60, seconds % 60);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, seconds % 60);
        } else {
            return String.format("%ds", seconds);
        }
    }

    private Map<String, Object> formatMemoryUsage(java.lang.management.MemoryUsage usage) {
        Map<String, Object> formatted = new HashMap<>();
        formatted.put("init", usage.getInit());
        formatted.put("initFormatted", formatBytes(usage.getInit()));
        formatted.put("used", usage.getUsed());
        formatted.put("usedFormatted", formatBytes(usage.getUsed()));
        formatted.put("committed", usage.getCommitted());
        formatted.put("committedFormatted", formatBytes(usage.getCommitted()));
        formatted.put("max", usage.getMax());
        formatted.put("maxFormatted", formatBytes(usage.getMax()));
        return formatted;
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024)
            return bytes + " B";
        if (bytes < 1024 * 1024)
            return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024)
            return String.format("%.1f MB", bytes / (1024.0 * 1024));
        return String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024));
    }
}
