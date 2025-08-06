package org.openhab.core.ai.action.library.system;

import java.io.File;
import java.lang.management.ClassLoadingMXBean;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

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
 * Action for performing comprehensive system health checks.
 * 
 * 
 */
@Component(service = Action.class, immediate = true)
public class HealthCheckAction implements Action {

    private static final Logger logger = LoggerFactory.getLogger(HealthCheckAction.class);
    private static final String ACTION_ID = "openhab.system.healthcheck";
    private static final String ACTION_NAME = "System Health Check";

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
        return "Performs comprehensive system health checks including memory, disk space, file permissions, and system resources";
    }

    @Override
    public String getCategory() {
        return "system";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public Map<String, Object> getParameterSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");
        schema.put("properties", Map.of("includeDetailedChecks", Map.of("type", "boolean", "description",
                "Include detailed health checks that may take longer to execute", "default", false)));
        schema.put("additionalProperties", false);
        return schema;
    }

    @Override
    public Map<String, Object> getReturnSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();
        properties.put("timestamp", Map.of("type", "string", "description", "ISO timestamp of the health check"));
        properties.put("overallStatus", Map.of("type", "string", "enum",
                List.of("HEALTHY", "WARNING", "CRITICAL", "ERROR"), "description", "Overall system health status"));
        properties.put("checks", Map.of("type", "array", "description", "List of individual health checks"));
        properties.put("summary", Map.of("type", "object", "description", "Summary statistics of health checks"));

        schema.put("properties", properties);
        schema.put("required", List.of("timestamp", "overallStatus", "checks", "summary"));
        return schema;
    }

    @Override
    public ActionValidationResult validateParameters(Map<String, Object> parameters) {
        if (parameters == null) {
            return ActionValidationResult.valid(parameters); // No parameters required
        }
        return ActionValidationResult.valid(parameters);
    }

    @Override
    public ActionResult execute(Map<String, Object> parameters, ActionContext context) throws ActionException {
        long startTime = System.currentTimeMillis();
        logger.debug("Executing health check action with parameters: {}", parameters);

        try {
            boolean includeDetailedChecks = (Boolean) parameters.getOrDefault("includeDetailedChecks", false);
            Map<String, Object> healthReport = performHealthChecks(includeDetailedChecks);

            long executionTime = System.currentTimeMillis() - startTime;
            logger.debug("Health check action completed in {}ms", executionTime);

            return ActionResult.success(healthReport, executionTime);

        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            logger.error("Failed to perform health checks", e);
            throw new ActionException(ACTION_ID, "Failed to perform health checks: " + e.getMessage(), e);
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
                .description("Performs comprehensive openHAB system health checks")
                .tags(List.of("system", "health", "monitoring", "diagnostics"))
                .documentation(
                        "Provides comprehensive system health monitoring including memory, disk space, threads, and system resources")
                .examples(List.of("{} - Perform basic health checks",
                        "{\"includeDetailedChecks\": true} - Perform detailed health checks including GC and class loader analysis"))
                .build();
    }

    @Override
    public Map<String, Object> getCapabilities() {
        return Map.of("filtering", false, "sorting", false, "pagination", false, "metadata", true, "async", true);
    }

    @Override
    public void initialize(ActionContext context) {
        logger.debug("HealthCheckAction initialized for protocol: {}", context.getProtocol());
    }

    @Override
    public void cleanup() {
        logger.debug("HealthCheckAction cleanup completed");
    }

    @Override
    public boolean isReady() {
        return true; // No external dependencies required
    }

    private Map<String, Object> performHealthChecks(boolean includeDetailedChecks) {
        Map<String, Object> report = new HashMap<>();
        List<Map<String, Object>> checks = new ArrayList<>();

        report.put("timestamp", Instant.now().toString());
        report.put("overallStatus", "UNKNOWN");

        // Memory Health Check
        checks.add(checkMemoryHealth());

        // Disk Space Health Check
        checks.add(checkDiskSpaceHealth());

        // Thread Health Check
        checks.add(checkThreadHealth());

        // System Load Health Check
        checks.add(checkSystemLoadHealth());

        // File System Permissions Check
        checks.add(checkFileSystemPermissions());

        // Java Version Check
        checks.add(checkJavaVersion());

        if (includeDetailedChecks) {
            // More intensive checks
            checks.add(checkGarbageCollectionHealth());
            checks.add(checkClassLoaderHealth());
            checks.add(checkSystemPropertiesHealth());
        }

        report.put("checks", checks);

        // Calculate overall status
        String overallStatus = calculateOverallStatus(checks);
        report.put("overallStatus", overallStatus);

        // Summary statistics
        Map<String, Object> summary = calculateSummary(checks);
        report.put("summary", summary);

        return report;
    }

    private Map<String, Object> checkMemoryHealth() {
        Map<String, Object> check = new HashMap<>();
        check.put("name", "Memory Health");
        check.put("category", "MEMORY");

        try {
            Runtime runtime = Runtime.getRuntime();
            long maxMemory = runtime.maxMemory();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemory = totalMemory - freeMemory;
            double usagePercent = (usedMemory * 100.0) / maxMemory;

            String status;
            String message;
            if (usagePercent > 90) {
                status = "CRITICAL";
                message = String.format("Memory usage critical: %.1f%% used", usagePercent);
            } else if (usagePercent > 75) {
                status = "WARNING";
                message = String.format("Memory usage high: %.1f%% used", usagePercent);
            } else {
                status = "HEALTHY";
                message = String.format("Memory usage normal: %.1f%% used", usagePercent);
            }

            check.put("status", status);
            check.put("message", message);
            check.put("details",
                    Map.of("usedMemory", usedMemory, "maxMemory", maxMemory, "usagePercent", usagePercent));

        } catch (Exception e) {
            check.put("status", "ERROR");
            check.put("message", "Failed to check memory health: " + e.getMessage());
        }

        return check;
    }

    private Map<String, Object> checkDiskSpaceHealth() {
        Map<String, Object> check = new HashMap<>();
        check.put("name", "Disk Space Health");
        check.put("category", "STORAGE");

        try {
            String userHome = System.getProperty("openhab.userdata", System.getProperty("user.home"));
            File homeDir = new File(userHome);

            long totalSpace = homeDir.getTotalSpace();
            long freeSpace = homeDir.getFreeSpace();
            long usedSpace = totalSpace - freeSpace;
            double usagePercent = (usedSpace * 100.0) / totalSpace;

            String status;
            String message;
            if (usagePercent > 95) {
                status = "CRITICAL";
                message = String.format("Disk space critical: %.1f%% used", usagePercent);
            } else if (usagePercent > 85) {
                status = "WARNING";
                message = String.format("Disk space low: %.1f%% used", usagePercent);
            } else {
                status = "HEALTHY";
                message = String.format("Disk space adequate: %.1f%% used", usagePercent);
            }

            check.put("status", status);
            check.put("message", message);
            check.put("details", Map.of("totalSpace", totalSpace, "freeSpace", freeSpace, "usagePercent", usagePercent,
                    "path", homeDir.getAbsolutePath()));

        } catch (Exception e) {
            check.put("status", "ERROR");
            check.put("message", "Failed to check disk space health: " + e.getMessage());
        }

        return check;
    }

    private Map<String, Object> checkThreadHealth() {
        Map<String, Object> check = new HashMap<>();
        check.put("name", "Thread Health");
        check.put("category", "THREADS");

        try {
            ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
            int threadCount = threadBean.getThreadCount();
            int peakThreadCount = threadBean.getPeakThreadCount();
            int daemonThreadCount = threadBean.getDaemonThreadCount();

            String status;
            String message;
            if (threadCount > 1000) {
                status = "CRITICAL";
                message = String.format("Thread count critical: %d active threads", threadCount);
            } else if (threadCount > 500) {
                status = "WARNING";
                message = String.format("Thread count high: %d active threads", threadCount);
            } else {
                status = "HEALTHY";
                message = String.format("Thread count normal: %d active threads", threadCount);
            }

            check.put("status", status);
            check.put("message", message);
            check.put("details", Map.of("threadCount", threadCount, "peakThreadCount", peakThreadCount,
                    "daemonThreadCount", daemonThreadCount));

        } catch (Exception e) {
            check.put("status", "ERROR");
            check.put("message", "Failed to check thread health: " + e.getMessage());
        }

        return check;
    }

    private Map<String, Object> checkSystemLoadHealth() {
        Map<String, Object> check = new HashMap<>();
        check.put("name", "System Load Health");
        check.put("category", "SYSTEM");

        try {
            OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
            double systemLoad = osBean.getSystemLoadAverage();
            int availableProcessors = osBean.getAvailableProcessors();

            String status;
            String message;
            if (systemLoad > availableProcessors * 2) {
                status = "CRITICAL";
                message = String.format("System load critical: %.2f (processors: %d)", systemLoad, availableProcessors);
            } else if (systemLoad > availableProcessors) {
                status = "WARNING";
                message = String.format("System load high: %.2f (processors: %d)", systemLoad, availableProcessors);
            } else {
                status = "HEALTHY";
                message = String.format("System load normal: %.2f (processors: %d)", systemLoad, availableProcessors);
            }

            check.put("status", status);
            check.put("message", message);
            check.put("details", Map.of("systemLoad", systemLoad, "availableProcessors", availableProcessors));

        } catch (Exception e) {
            check.put("status", "ERROR");
            check.put("message", "Failed to check system load health: " + e.getMessage());
        }

        return check;
    }

    private Map<String, Object> checkFileSystemPermissions() {
        Map<String, Object> check = new HashMap<>();
        check.put("name", "File System Permissions");
        check.put("category", "PERMISSIONS");

        try {
            String userHome = System.getProperty("openhab.userdata", System.getProperty("user.home"));
            File homeDir = new File(userHome);

            boolean canRead = homeDir.canRead();
            boolean canWrite = homeDir.canWrite();
            boolean canExecute = homeDir.canExecute();

            String status;
            String message;
            if (!canRead || !canWrite) {
                status = "CRITICAL";
                message = "File system permissions insufficient for openHAB operation";
            } else if (!canExecute) {
                status = "WARNING";
                message = "File system execute permissions limited";
            } else {
                status = "HEALTHY";
                message = "File system permissions adequate";
            }

            check.put("status", status);
            check.put("message", message);
            check.put("details", Map.of("canRead", canRead, "canWrite", canWrite, "canExecute", canExecute, "path",
                    homeDir.getAbsolutePath()));

        } catch (Exception e) {
            check.put("status", "ERROR");
            check.put("message", "Failed to check file system permissions: " + e.getMessage());
        }

        return check;
    }

    private Map<String, Object> checkJavaVersion() {
        Map<String, Object> check = new HashMap<>();
        check.put("name", "Java Version");
        check.put("category", "JAVA");

        try {
            String javaVersion = System.getProperty("java.version");
            String javaVendor = System.getProperty("java.vendor");
            String javaHome = System.getProperty("java.home");

            String status = "HEALTHY";
            String message = String.format("Java version: %s (%s)", javaVersion, javaVendor);

            check.put("status", status);
            check.put("message", message);
            check.put("details", Map.of("javaVersion", javaVersion, "javaVendor", javaVendor, "javaHome", javaHome));

        } catch (Exception e) {
            check.put("status", "ERROR");
            check.put("message", "Failed to check Java version: " + e.getMessage());
        }

        return check;
    }

    private Map<String, Object> checkGarbageCollectionHealth() {
        Map<String, Object> check = new HashMap<>();
        check.put("name", "Garbage Collection Health");
        check.put("category", "MEMORY");

        try {
            List<GarbageCollectorMXBean> gcBeans = ManagementFactory.getGarbageCollectorMXBeans();
            long totalGcCount = 0;
            long totalGcTime = 0;

            for (GarbageCollectorMXBean gcBean : gcBeans) {
                totalGcCount += gcBean.getCollectionCount();
                totalGcTime += gcBean.getCollectionTime();
            }

            String status = "HEALTHY";
            String message = String.format("GC operations: %d collections, %d ms total time", totalGcCount,
                    totalGcTime);

            check.put("status", status);
            check.put("message", message);
            check.put("details",
                    Map.of("totalGcCount", totalGcCount, "totalGcTime", totalGcTime, "gcBeansCount", gcBeans.size()));

        } catch (Exception e) {
            check.put("status", "ERROR");
            check.put("message", "Failed to check garbage collection health: " + e.getMessage());
        }

        return check;
    }

    private Map<String, Object> checkClassLoaderHealth() {
        Map<String, Object> check = new HashMap<>();
        check.put("name", "Class Loader Health");
        check.put("category", "JAVA");

        try {
            ClassLoadingMXBean classLoadingBean = ManagementFactory.getClassLoadingMXBean();
            int loadedClassCount = classLoadingBean.getLoadedClassCount();
            long totalLoadedClassCount = classLoadingBean.getTotalLoadedClassCount();
            long unloadedClassCount = classLoadingBean.getUnloadedClassCount();

            String status = "HEALTHY";
            String message = String.format("Classes: %d loaded, %d total loaded, %d unloaded", loadedClassCount,
                    totalLoadedClassCount, unloadedClassCount);

            check.put("status", status);
            check.put("message", message);
            check.put("details", Map.of("loadedClassCount", loadedClassCount, "totalLoadedClassCount",
                    totalLoadedClassCount, "unloadedClassCount", unloadedClassCount));

        } catch (Exception e) {
            check.put("status", "ERROR");
            check.put("message", "Failed to check class loader health: " + e.getMessage());
        }

        return check;
    }

    private Map<String, Object> checkSystemPropertiesHealth() {
        Map<String, Object> check = new HashMap<>();
        check.put("name", "System Properties Health");
        check.put("category", "SYSTEM");

        try {
            String osName = System.getProperty("os.name");
            String osVersion = System.getProperty("os.version");
            String osArch = System.getProperty("os.arch");
            String userDir = System.getProperty("user.dir");

            String status = "HEALTHY";
            String message = String.format("OS: %s %s (%s)", osName, osVersion, osArch);

            check.put("status", status);
            check.put("message", message);
            check.put("details",
                    Map.of("osName", osName, "osVersion", osVersion, "osArch", osArch, "userDir", userDir));

        } catch (Exception e) {
            check.put("status", "ERROR");
            check.put("message", "Failed to check system properties health: " + e.getMessage());
        }

        return check;
    }

    private String calculateOverallStatus(List<Map<String, Object>> checks) {
        boolean hasCritical = false;
        boolean hasWarning = false;
        boolean hasError = false;

        for (Map<String, Object> check : checks) {
            String status = (String) check.get("status");
            if ("CRITICAL".equals(status)) {
                hasCritical = true;
            } else if ("WARNING".equals(status)) {
                hasWarning = true;
            } else if ("ERROR".equals(status)) {
                hasError = true;
            }
        }

        if (hasCritical) {
            return "CRITICAL";
        } else if (hasWarning) {
            return "WARNING";
        } else if (hasError) {
            return "ERROR";
        } else {
            return "HEALTHY";
        }
    }

    private Map<String, Object> calculateSummary(List<Map<String, Object>> checks) {
        Map<String, Object> summary = new HashMap<>();
        int healthy = 0, warning = 0, critical = 0, error = 0;

        for (Map<String, Object> check : checks) {
            String status = (String) check.get("status");
            switch (status) {
                case "HEALTHY" -> healthy++;
                case "WARNING" -> warning++;
                case "CRITICAL" -> critical++;
                case "ERROR" -> error++;
            }
        }

        summary.put("total", checks.size());
        summary.put("healthy", healthy);
        summary.put("warning", warning);
        summary.put("critical", critical);
        summary.put("error", error);

        return summary;
    }
}
