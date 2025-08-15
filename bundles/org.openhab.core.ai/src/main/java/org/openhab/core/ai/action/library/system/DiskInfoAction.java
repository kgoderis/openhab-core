package org.openhab.core.ai.action.library.system;

import java.io.File;
import java.nio.file.FileStore;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

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
 * Action for retrieving detailed disk information including space usage, file systems, and storage analysis.
 * 
 * This action provides comprehensive disk diagnostics using real Java File APIs.
 */
@Component(service = Action.class, immediate = true)
public class DiskInfoAction implements Action {

    private static final Logger logger = LoggerFactory.getLogger(DiskInfoAction.class);
    private static final String ACTION_ID = "openhab.system.disk-info";
    private static final String ACTION_NAME = "Disk Information";
    private static final String CATEGORY = "system";

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
        return "Retrieves detailed disk information including space usage, file systems, and storage analysis";
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
        properties.put("includeFileSystems",
                Map.of("type", "boolean", "description", "Include detailed file system information", "default", true));
        properties.put("includeOpenHABPaths",
                Map.of("type", "boolean", "description", "Include openHAB-specific path analysis", "default", true));
        properties.put("path",
                Map.of("type", "string", "description", "Specific path to analyze (optional)", "default", ""));

        schema.put("properties", properties);
        schema.put("additionalProperties", false);
        return schema;
    }

    @Override
    public Map<String, Object> getReturnSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();
        properties.put("timestamp", Map.of("type", "string", "description", "Timestamp of the disk information"));
        properties.put("fileSystems", Map.of("type", "array", "description", "File system information"));
        properties.put("openHABPaths", Map.of("type", "object", "description", "openHAB-specific path analysis"));
        properties.put("diskAnalysis",
                Map.of("type", "object", "description", "Disk usage analysis and recommendations"));

        schema.put("properties", properties);
        return schema;
    }

    @Override
    public ActionValidationResult validateParameters(Map<String, Object> parameters) {
        return ActionValidationResult.valid(parameters);
    }

    @Override
    public ActionResult execute(Map<String, Object> parameters, ActionContext context) throws ActionException {
        logger.debug("Executing DiskInfoAction with context: {}", context.getProtocol());

        long startTime = System.currentTimeMillis();

        try {
            boolean includeFileSystems = (Boolean) parameters.getOrDefault("includeFileSystems", true);
            boolean includeOpenHABPaths = (Boolean) parameters.getOrDefault("includeOpenHABPaths", true);
            String specificPath = (String) parameters.get("path");

            Map<String, Object> result = collectDiskInfo(includeFileSystems, includeOpenHABPaths, specificPath);

            long executionTime = System.currentTimeMillis() - startTime;
            return ActionResult.success(result, executionTime);

        } catch (Exception e) {
            logger.error("Error executing DiskInfoAction: {}", e.getMessage(), e);
            throw new ActionException(ACTION_ID, "Failed to collect disk information: " + e.getMessage(), e);
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
                .description("Retrieves detailed disk information using Java File APIs").version("1.0.0")
                .tags(List.of("system", "disk", "storage", "diagnostics")).build();
    }

    @Override
    public void initialize(ActionContext context) {
        logger.debug("DiskInfoAction initialized with context: {}", context.getProtocol());
    }

    @Override
    public void cleanup() {
        logger.debug("DiskInfoAction cleanup completed");
    }

    @Override
    public boolean isReady() {
        return true; // No external dependencies required
    }

    @Override
    public Map<String, Object> getCapabilities() {
        Map<String, Object> capabilities = new HashMap<>();
        capabilities.put("supportsAsync", true);
        capabilities.put("supportsRealTimeData", true);
        capabilities.put("requiresPrivileges", false);
        capabilities.put("dataSource", "Java File APIs");
        return capabilities;
    }

    private Map<String, Object> collectDiskInfo(boolean includeFileSystems, boolean includeOpenHABPaths,
            String specificPath) {
        Map<String, Object> diskInfo = new HashMap<>();
        diskInfo.put("timestamp", Instant.now().toString());

        // File systems information
        if (includeFileSystems) {
            List<Map<String, Object>> fileSystems = new ArrayList<>();

            try {
                for (FileStore store : FileSystems.getDefault().getFileStores()) {
                    Map<String, Object> fsInfo = new HashMap<>();

                    try {
                        long totalSpace = store.getTotalSpace();
                        long usableSpace = store.getUsableSpace();
                        long usedSpace = totalSpace - usableSpace;

                        fsInfo.put("name", store.name());
                        fsInfo.put("type", store.type());
                        fsInfo.put("totalSpace", totalSpace);
                        fsInfo.put("usableSpace", usableSpace);
                        fsInfo.put("usedSpace", usedSpace);
                        fsInfo.put("totalSpaceFormatted", formatBytes(totalSpace));
                        fsInfo.put("usableSpaceFormatted", formatBytes(usableSpace));
                        fsInfo.put("usedSpaceFormatted", formatBytes(usedSpace));

                        if (totalSpace > 0) {
                            double usagePercent = (double) usedSpace / totalSpace * 100;
                            fsInfo.put("usagePercent", String.format("%.2f%%", usagePercent));

                            // Usage analysis
                            if (usagePercent > 95) {
                                fsInfo.put("status", "CRITICAL");
                                fsInfo.put("recommendation", "Disk space critical - immediate action required");
                            } else if (usagePercent > 85) {
                                fsInfo.put("status", "WARNING");
                                fsInfo.put("recommendation", "Disk space low - consider cleanup or expansion");
                            } else if (usagePercent > 70) {
                                fsInfo.put("status", "ATTENTION");
                                fsInfo.put("recommendation", "Monitor disk usage closely");
                            } else {
                                fsInfo.put("status", "HEALTHY");
                                fsInfo.put("recommendation", "Disk space adequate");
                            }
                        }

                        // File store attributes
                        fsInfo.put("readOnly", store.isReadOnly());
                        fsInfo.put("blockSize", store.getBlockSize());

                    } catch (Exception e) {
                        fsInfo.put("error", "Failed to get space information: " + e.getMessage());
                        fsInfo.put("status", "ERROR");
                    }

                    fileSystems.add(fsInfo);
                }
            } catch (Exception e) {
                logger.error("Error collecting file system information: {}", e.getMessage(), e);
            }

            diskInfo.put("fileSystems", fileSystems);
        }

        // openHAB-specific path analysis
        if (includeOpenHABPaths) {
            Map<String, Object> openHABPaths = analyzeOpenHABPaths();
            diskInfo.put("openHABPaths", openHABPaths);
        }

        // Specific path analysis
        if (specificPath != null && !specificPath.trim().isEmpty()) {
            Map<String, Object> specificPathInfo = analyzeSpecificPath(specificPath);
            diskInfo.put("specificPath", specificPathInfo);
        }

        // Overall disk analysis and recommendations
        Map<String, Object> analysis = analyzeDiskUsage(diskInfo);
        diskInfo.put("diskAnalysis", analysis);

        return diskInfo;
    }

    private Map<String, Object> analyzeOpenHABPaths() {
        Map<String, Object> openHABPaths = new HashMap<>();

        // Common openHAB paths to analyze
        String[] openHABPathKeys = { "openhab.home", "openhab.userdata", "openhab.conf", "openhab.logdir" };

        for (String pathKey : openHABPathKeys) {
            String pathValue = System.getProperty(pathKey);
            if (pathValue != null && !pathValue.trim().isEmpty()) {
                Map<String, Object> pathInfo = analyzePath(pathValue, pathKey);
                openHABPaths.put(pathKey, pathInfo);
            }
        }

        // Additional important paths
        String userHome = System.getProperty("user.home");
        if (userHome != null) {
            openHABPaths.put("userHome", analyzePath(userHome, "user.home"));
        }

        String userDir = System.getProperty("user.dir");
        if (userDir != null) {
            openHABPaths.put("userDir", analyzePath(userDir, "user.dir"));
        }

        return openHABPaths;
    }

    private Map<String, Object> analyzeSpecificPath(String path) {
        return analyzePath(path, "custom");
    }

    private Map<String, Object> analyzePath(String pathString, String pathType) {
        Map<String, Object> pathInfo = new HashMap<>();
        pathInfo.put("path", pathString);
        pathInfo.put("type", pathType);

        try {
            File file = new File(pathString);
            Path path = file.toPath();

            pathInfo.put("exists", file.exists());
            pathInfo.put("isDirectory", file.isDirectory());
            pathInfo.put("isFile", file.isFile());
            pathInfo.put("canRead", file.canRead());
            pathInfo.put("canWrite", file.canWrite());
            pathInfo.put("canExecute", file.canExecute());

            if (file.exists()) {
                // Get file store for this path
                try {
                    FileStore store = Files.getFileStore(path);
                    long totalSpace = store.getTotalSpace();
                    long usableSpace = store.getUsableSpace();
                    long usedSpace = totalSpace - usableSpace;

                    pathInfo.put("totalSpace", totalSpace);
                    pathInfo.put("usableSpace", usableSpace);
                    pathInfo.put("usedSpace", usedSpace);
                    pathInfo.put("totalSpaceFormatted", formatBytes(totalSpace));
                    pathInfo.put("usableSpaceFormatted", formatBytes(usableSpace));
                    pathInfo.put("usedSpaceFormatted", formatBytes(usedSpace));

                    if (totalSpace > 0) {
                        double usagePercent = (double) usedSpace / totalSpace * 100;
                        pathInfo.put("usagePercent", String.format("%.2f%%", usagePercent));
                    }

                    // Directory size analysis (for directories)
                    if (file.isDirectory()) {
                        long directorySize = calculateDirectorySize(file);
                        pathInfo.put("directorySize", directorySize);
                        pathInfo.put("directorySizeFormatted", formatBytes(directorySize));

                        int fileCount = countFiles(file);
                        int directoryCount = countDirectories(file);
                        pathInfo.put("fileCount", fileCount);
                        pathInfo.put("directoryCount", directoryCount);
                    }

                } catch (Exception e) {
                    pathInfo.put("spaceError", "Failed to get space information: " + e.getMessage());
                }
            }

        } catch (Exception e) {
            pathInfo.put("error", "Failed to analyze path: " + e.getMessage());
        }

        return pathInfo;
    }

    private long calculateDirectorySize(File directory) {
        long size = 0;
        try {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        size += file.length();
                    } else if (file.isDirectory()) {
                        size += calculateDirectorySize(file);
                    }
                }
            }
        } catch (Exception e) {
            logger.debug("Error calculating directory size for {}: {}", directory.getPath(), e.getMessage());
        }
        return size;
    }

    private int countFiles(File directory) {
        int count = 0;
        try {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        count++;
                    } else if (file.isDirectory()) {
                        count += countFiles(file);
                    }
                }
            }
        } catch (Exception e) {
            logger.debug("Error counting files in {}: {}", directory.getPath(), e.getMessage());
        }
        return count;
    }

    private int countDirectories(File directory) {
        int count = 0;
        try {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        count++;
                        count += countDirectories(file);
                    }
                }
            }
        } catch (Exception e) {
            logger.debug("Error counting directories in {}: {}", directory.getPath(), e.getMessage());
        }
        return count;
    }

    private Map<String, Object> analyzeDiskUsage(Map<String, Object> diskInfo) {
        Map<String, Object> analysis = new HashMap<>();

        List<String> recommendations = new ArrayList<>();
        int criticalCount = 0;
        int warningCount = 0;

        // Analyze file systems
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> fileSystems = (List<Map<String, Object>>) diskInfo.get("fileSystems");
        if (fileSystems != null) {
            for (Map<String, Object> fs : fileSystems) {
                String status = (String) fs.get("status");
                if ("CRITICAL".equals(status)) {
                    criticalCount++;
                    recommendations.add("File system " + fs.get("name") + " is critically low on space");
                } else if ("WARNING".equals(status)) {
                    warningCount++;
                    recommendations.add("File system " + fs.get("name") + " is running low on space");
                }
            }
        }

        // Analyze openHAB paths
        @SuppressWarnings("unchecked")
        Map<String, Object> openHABPaths = (Map<String, Object>) diskInfo.get("openHABPaths");
        if (openHABPaths != null) {
            for (Map.Entry<String, Object> entry : openHABPaths.entrySet()) {
                @SuppressWarnings("unchecked")
                Map<String, Object> pathInfo = (Map<String, Object>) entry.getValue();
                Boolean canWrite = (Boolean) pathInfo.get("canWrite");
                if (canWrite != null && !canWrite) {
                    recommendations.add("openHAB path " + entry.getKey() + " is not writable");
                }
            }
        }

        // Overall assessment
        if (criticalCount > 0) {
            analysis.put("overallStatus", "CRITICAL");
            analysis.put("priority", "IMMEDIATE");
        } else if (warningCount > 0) {
            analysis.put("overallStatus", "WARNING");
            analysis.put("priority", "HIGH");
        } else {
            analysis.put("overallStatus", "HEALTHY");
            analysis.put("priority", "NORMAL");
        }

        analysis.put("criticalFileSystems", criticalCount);
        analysis.put("warningFileSystems", warningCount);
        analysis.put("recommendations", recommendations);

        return analysis;
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
