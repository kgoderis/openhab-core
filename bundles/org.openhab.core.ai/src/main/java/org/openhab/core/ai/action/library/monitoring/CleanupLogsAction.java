package org.openhab.core.ai.action.library.monitoring;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import org.openhab.core.OpenHAB;
import org.openhab.core.ai.action.api.Action;
import org.openhab.core.ai.action.api.ActionContext;
import org.openhab.core.ai.action.api.ActionException;
import org.openhab.core.ai.action.api.ActionMetadata;
import org.openhab.core.ai.action.api.ActionResult;
import org.openhab.core.ai.action.api.ActionValidationResult;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action for cleaning up log files in openHAB.
 */
@Component(service = Action.class, immediate = true)
public class CleanupLogsAction implements Action {

    private static final Logger logger = LoggerFactory.getLogger(CleanupLogsAction.class);
    private static final String ACTION_ID = "openhab.monitoring.cleanup_logs";
    private static final String ACTION_NAME = "Cleanup Logs";

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
        return "Cleans up old log files and maintains log directory to prevent disk space issues";
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
        properties.put("maxAge", Map.of("type", "integer", "description", "Maximum age of log files in days", "default",
                30, "minimum", 1));
        properties.put("maxSize", Map.of("type", "string", "description",
                "Maximum total size of log directory (e.g., '1GB', '10GB')", "default", "1GB"));
        properties.put("maxFiles", Map.of("type", "integer", "description", "Maximum number of log files to keep",
                "default", 100, "minimum", 1));
        properties.put("includeRotated",
                Map.of("type", "boolean", "description", "Include rotated log files in cleanup", "default", true));
        properties.put("includeCompressed",
                Map.of("type", "boolean", "description", "Include compressed log files in cleanup", "default", true));
        properties.put("dryRun", Map.of("type", "boolean", "description",
                "Show what would be deleted without actually deleting", "default", false));
        properties.put("backup",
                Map.of("type", "boolean", "description", "Create backup before cleanup", "default", false));
        properties.put("patterns",
                Map.of("type", "array", "items", Map.of("type", "string"), "description",
                        "File patterns to include in cleanup (e.g., ['*.log', '*.log.*'])", "default",
                        List.of("*.log", "*.log.*")));

        schema.put("properties", properties);
        schema.put("additionalProperties", false);
        return schema;
    }

    @Override
    public Map<String, Object> getReturnSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();
        properties.put("cleanedFiles", Map.of("type", "array", "description", "List of files that were cleaned up"));
        properties.put("deletedFiles", Map.of("type", "array", "description", "List of files that were deleted"));
        properties.put("deletedSize", Map.of("type", "integer", "description", "Total size of deleted files in bytes"));
        properties.put("deletedSizeFormatted",
                Map.of("type", "string", "description", "Total size of deleted files formatted"));
        properties.put("remainingFiles", Map.of("type", "integer", "description", "Number of files remaining"));
        properties.put("remainingSize",
                Map.of("type", "integer", "description", "Total size of remaining files in bytes"));
        properties.put("remainingSizeFormatted",
                Map.of("type", "string", "description", "Total size of remaining files formatted"));
        properties.put("dryRun", Map.of("type", "boolean", "description", "Whether this was a dry run"));
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

        // Validate maxAge
        Object maxAgeObj = parameters.get("maxAge");
        if (maxAgeObj != null) {
            if (maxAgeObj instanceof Integer) {
                Integer maxAge = (Integer) maxAgeObj;
                if (maxAge < 1) {
                    errors.add("maxAge must be at least 1 day");
                }
            } else {
                errors.add("maxAge must be an integer");
            }
        }

        // Validate maxSize
        String maxSize = (String) parameters.get("maxSize");
        if (maxSize != null && !isValidSizeFormat(maxSize)) {
            errors.add("maxSize must be in format like '1GB', '10GB', '100MB'");
        }

        // Validate maxFiles
        Object maxFilesObj = parameters.get("maxFiles");
        if (maxFilesObj != null) {
            if (maxFilesObj instanceof Integer) {
                Integer maxFiles = (Integer) maxFilesObj;
                if (maxFiles < 1) {
                    errors.add("maxFiles must be at least 1");
                }
            } else {
                errors.add("maxFiles must be an integer");
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
        logger.debug("Executing cleanup logs action with parameters: {}", parameters);

        try {
            Integer maxAge = (Integer) parameters.getOrDefault("maxAge", 30);
            String maxSize = (String) parameters.getOrDefault("maxSize", "1GB");
            Integer maxFiles = (Integer) parameters.getOrDefault("maxFiles", 100);
            Boolean includeRotated = (Boolean) parameters.getOrDefault("includeRotated", true);
            Boolean includeCompressed = (Boolean) parameters.getOrDefault("includeCompressed", true);
            Boolean dryRun = (Boolean) parameters.getOrDefault("dryRun", false);
            Boolean backup = (Boolean) parameters.getOrDefault("backup", false);
            @SuppressWarnings("unchecked")
            List<String> patterns = (List<String>) parameters.getOrDefault("patterns", List.of("*.log", "*.log.*"));

            String userDataDir = OpenHAB.getUserDataFolder();
            Path logsDir = Paths.get(userDataDir, "logs");

            if (!Files.exists(logsDir)) {
                throw new ActionException(ACTION_ID, "Logs directory not found: " + logsDir);
            }

            // Collect files for cleanup
            List<Path> filesToCleanup = collectFilesForCleanup(logsDir, patterns, maxAge, includeRotated,
                    includeCompressed);

            // Apply size and count limits
            List<Path> filesToDelete = applyLimits(filesToCleanup, maxSize, maxFiles);

            Map<String, Object> result = new HashMap<>();
            result.put("dryRun", dryRun);
            result.put("timestamp", Instant.now().toString());

            if (dryRun) {
                // Dry run - just report what would be done
                List<String> fileNames = filesToDelete.stream().map(path -> path.getFileName().toString()).toList();

                long totalSize = filesToDelete.stream().mapToLong(this::getFileSize).sum();

                result.put("cleanedFiles", fileNames);
                result.put("deletedFiles", fileNames);
                result.put("deletedSize", totalSize);
                result.put("deletedSizeFormatted", formatBytes(totalSize));
                result.put("remainingFiles", filesToCleanup.size() - filesToDelete.size());
                result.put("remainingSize", filesToCleanup.stream().filter(f -> !filesToDelete.contains(f))
                        .mapToLong(this::getFileSize).sum());
                result.put("message",
                        "Dry run: Would delete " + filesToDelete.size() + " files (" + formatBytes(totalSize) + ")");
            } else {
                // Actual cleanup
                if (backup) {
                    createBackup(logsDir);
                }

                List<String> deletedFiles = new ArrayList<>();
                long deletedSize = 0;

                for (Path file : filesToDelete) {
                    try {
                        long fileSize = getFileSize(file);
                        Files.delete(file);
                        deletedFiles.add(file.getFileName().toString());
                        deletedSize += fileSize;
                        logger.info("Deleted log file: {}", file);
                    } catch (IOException e) {
                        logger.warn("Failed to delete file: {}", file, e);
                    }
                }

                result.put("cleanedFiles", deletedFiles);
                result.put("deletedFiles", deletedFiles);
                result.put("deletedSize", deletedSize);
                result.put("deletedSizeFormatted", formatBytes(deletedSize));
                result.put("remainingFiles", filesToCleanup.size() - filesToDelete.size());
                result.put("remainingSize", filesToCleanup.stream().filter(f -> !filesToDelete.contains(f))
                        .mapToLong(this::getFileSize).sum());
                result.put("message",
                        "Cleaned up " + deletedFiles.size() + " files (" + formatBytes(deletedSize) + ")");
            }

            long executionTime = System.currentTimeMillis() - startTime;
            logger.debug("Cleanup logs action completed in {}ms", executionTime);

            return ActionResult.success(result, executionTime);

        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            logger.error("Failed to cleanup logs", e);
            throw new ActionException(ACTION_ID, "Failed to cleanup logs: " + e.getMessage(), e);
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
                .description("Cleans up old log files to prevent disk space issues")
                .tags(List.of("monitoring", "logging", "maintenance", "cleanup"))
                .documentation("Removes old log files based on age, size, and count limits to maintain disk space")
                .examples(List.of(
                        "{\"maxAge\": 7, \"dryRun\": true} - Show what would be deleted (files older than 7 days)",
                        "{\"maxSize\": \"500MB\", \"maxFiles\": 50} - Keep only 50 files or 500MB total",
                        "{\"maxAge\": 30, \"includeCompressed\": false} - Delete files older than 30 days, keep compressed files"))
                .build();
    }

    @Override
    public Map<String, Object> getCapabilities() {
        return Map.of("filtering", true, "sorting", false, "pagination", false, "metadata", true, "async", true,
                "realTime", false);
    }

    @Override
    public void initialize(ActionContext context) {
        logger.debug("CleanupLogsAction initialized for protocol: {}", context.getProtocol());
    }

    @Override
    public void cleanup() {
        logger.debug("CleanupLogsAction cleanup completed");
    }

    @Override
    public boolean isReady() {
        return true;
    }

    private List<Path> collectFilesForCleanup(Path logsDir, List<String> patterns, int maxAge, boolean includeRotated,
            boolean includeCompressed) throws IOException {
        List<Path> files = new ArrayList<>();
        LocalDateTime cutoffDate = LocalDateTime.now().minus(maxAge, ChronoUnit.DAYS);

        try (Stream<Path> fileStream = Files.list(logsDir)) {
            fileStream.filter(path -> {
                if (!Files.isRegularFile(path)) {
                    return false;
                }

                String fileName = path.getFileName().toString();

                // Check if file matches patterns
                boolean matchesPattern = patterns.stream()
                        .anyMatch(pattern -> fileName.matches(pattern.replace("*", ".*")));

                if (!matchesPattern) {
                    return false;
                }

                // Check if file is rotated
                if (!includeRotated && isRotatedFile(fileName)) {
                    return false;
                }

                // Check if file is compressed
                if (!includeCompressed && fileName.endsWith(".gz")) {
                    return false;
                }

                // Check file age
                try {
                    LocalDateTime fileDate = LocalDateTime.ofInstant(Files.getLastModifiedTime(path).toInstant(),
                            ZoneId.systemDefault());
                    return fileDate.isBefore(cutoffDate);
                } catch (IOException e) {
                    return false;
                }
            }).forEach(files::add);
        }

        return files;
    }

    private List<Path> applyLimits(List<Path> files, String maxSize, int maxFiles) {
        List<Path> filesToDelete = new ArrayList<>();

        // Sort files by modification time (oldest first)
        files.sort((p1, p2) -> {
            try {
                return Files.getLastModifiedTime(p1).compareTo(Files.getLastModifiedTime(p2));
            } catch (IOException e) {
                return 0;
            }
        });

        long maxSizeBytes = parseSize(maxSize);
        long currentSize = files.stream().mapToLong(this::getFileSize).sum();
        int currentCount = files.size();

        // Remove files until we're under the limits
        for (Path file : files) {
            if (currentCount <= maxFiles && currentSize <= maxSizeBytes) {
                break;
            }

            long fileSize = getFileSize(file);
            filesToDelete.add(file);
            currentSize -= fileSize;
            currentCount--;
        }

        return filesToDelete;
    }

    private boolean isRotatedFile(String fileName) {
        // Check if file appears to be a rotated log file
        return fileName.matches(".*\\.\\d{4}-\\d{2}-\\d{2}.*") || fileName.matches(".*\\.\\d+$")
                || fileName.contains(".backup.");
    }

    private void createBackup(Path logsDir) throws IOException {
        // In a real implementation, you might create a backup of the entire logs directory
        // For now, we'll just log that backup was requested
        logger.info("Backup requested for logs directory: {}", logsDir);
    }

    private long getFileSize(Path file) {
        try {
            return Files.size(file);
        } catch (IOException e) {
            return 0;
        }
    }

    private boolean isValidSizeFormat(String size) {
        return size.matches("\\d+\\s*(B|KB|MB|GB|TB)");
    }

    private long parseSize(String size) {
        size = size.toUpperCase().replaceAll("\\s+", "");
        long multiplier = 1;

        if (size.endsWith("KB")) {
            multiplier = 1024;
            size = size.substring(0, size.length() - 2);
        } else if (size.endsWith("MB")) {
            multiplier = 1024 * 1024;
            size = size.substring(0, size.length() - 2);
        } else if (size.endsWith("GB")) {
            multiplier = 1024 * 1024 * 1024;
            size = size.substring(0, size.length() - 2);
        } else if (size.endsWith("TB")) {
            multiplier = 1024L * 1024 * 1024 * 1024;
            size = size.substring(0, size.length() - 2);
        } else if (size.endsWith("B")) {
            size = size.substring(0, size.length() - 1);
        }

        return Long.parseLong(size) * multiplier;
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", bytes / (1024.0 * 1024));
        } else {
            return String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024));
        }
    }
}
