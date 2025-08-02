package org.openhab.core.ai.common.actions.monitoring;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.openhab.core.OpenHAB;
import org.openhab.core.ai.common.action.AIActionContext;
import org.openhab.core.ai.common.action.AIActionMetadata;
import org.openhab.core.ai.common.action.AIActionResult;
import org.openhab.core.ai.common.action.AIActionValidationResult;
import org.openhab.core.ai.common.api.action.AIAction;
import org.openhab.core.ai.common.api.action.AIActionException;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AIAction for retrieving comprehensive system monitoring metrics for openHAB.
 */
@Component(service = AIAction.class, immediate = true)
public class GetMonitoringMetricsAction implements AIAction {

    private static final Logger logger = LoggerFactory.getLogger(GetMonitoringMetricsAction.class);
    private static final String ACTION_ID = "openhab.monitoring.get_monitoring_metrics";
    private static final String ACTION_NAME = "Get Monitoring Metrics";

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
        return "Retrieves comprehensive system monitoring metrics including CPU, memory, disk, and JVM statistics";
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
        properties.put("includeSystemMetrics", Map.of("type", "boolean", "description",
                "Include system-level metrics (CPU, memory, disk)", "default", true));
        properties.put("includeJvmMetrics",
                Map.of("type", "boolean", "description", "Include JVM-specific metrics", "default", true));
        properties.put("includeThreadMetrics",
                Map.of("type", "boolean", "description", "Include thread-related metrics", "default", true));
        properties.put("includeDiskMetrics",
                Map.of("type", "boolean", "description", "Include disk usage metrics", "default", true));
        properties.put("includeNetworkMetrics",
                Map.of("type", "boolean", "description", "Include network-related metrics", "default", false));
        properties.put("includeProcessMetrics",
                Map.of("type", "boolean", "description", "Include process-specific metrics", "default", true));

        schema.put("properties", properties);
        schema.put("additionalProperties", false);
        return schema;
    }

    @Override
    public Map<String, Object> getReturnSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();
        properties.put("systemMetrics", Map.of("type", "object", "description", "System-level metrics"));
        properties.put("jvmMetrics", Map.of("type", "object", "description", "JVM-specific metrics"));
        properties.put("threadMetrics", Map.of("type", "object", "description", "Thread-related metrics"));
        properties.put("diskMetrics", Map.of("type", "object", "description", "Disk usage metrics"));
        properties.put("networkMetrics", Map.of("type", "object", "description", "Network-related metrics"));
        properties.put("processMetrics", Map.of("type", "object", "description", "Process-specific metrics"));
        properties.put("timestamp", Map.of("type", "string", "description", "ISO timestamp of the operation"));
        properties.put("message", Map.of("type", "string", "description", "Operation result message"));
        properties.put("error", Map.of("type", "string", "description", "Error message if operation failed"));

        schema.put("properties", properties);
        return schema;
    }

    @Override
    public AIActionValidationResult validateParameters(Map<String, Object> parameters) {
        if (parameters == null) {
            return AIActionValidationResult.invalid(List.of("Parameters cannot be null"));
        }

        // All parameters are optional, so no validation needed
        return AIActionValidationResult.valid(parameters);
    }

    @Override
    public AIActionResult execute(Map<String, Object> parameters, AIActionContext context) throws AIActionException {
        long startTime = System.currentTimeMillis();
        logger.debug("Executing get monitoring metrics action with parameters: {}", parameters);

        try {
            Boolean includeSystemMetrics = (Boolean) parameters.getOrDefault("includeSystemMetrics", true);
            Boolean includeJvmMetrics = (Boolean) parameters.getOrDefault("includeJvmMetrics", true);
            Boolean includeThreadMetrics = (Boolean) parameters.getOrDefault("includeThreadMetrics", true);
            Boolean includeDiskMetrics = (Boolean) parameters.getOrDefault("includeDiskMetrics", true);
            Boolean includeNetworkMetrics = (Boolean) parameters.getOrDefault("includeNetworkMetrics", false);
            Boolean includeProcessMetrics = (Boolean) parameters.getOrDefault("includeProcessMetrics", true);

            Map<String, Object> result = new HashMap<>();
            result.put("timestamp", Instant.now().toString());

            // Collect system metrics
            if (includeSystemMetrics) {
                result.put("systemMetrics", getSystemMetrics());
            }

            // Collect JVM metrics
            if (includeJvmMetrics) {
                result.put("jvmMetrics", getJvmMetrics());
            }

            // Collect thread metrics
            if (includeThreadMetrics) {
                result.put("threadMetrics", getThreadMetrics());
            }

            // Collect disk metrics
            if (includeDiskMetrics) {
                result.put("diskMetrics", getDiskMetrics());
            }

            // Collect network metrics
            if (includeNetworkMetrics) {
                result.put("networkMetrics", getNetworkMetrics());
            }

            // Collect process metrics
            if (includeProcessMetrics) {
                result.put("processMetrics", getProcessMetrics());
            }

            result.put("message", "Monitoring metrics collected successfully");

            long executionTime = System.currentTimeMillis() - startTime;
            logger.debug("Get monitoring metrics action completed in {}ms", executionTime);

            return AIActionResult.success(result, executionTime);

        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            logger.error("Failed to get monitoring metrics", e);
            throw new AIActionException(ACTION_ID, "Failed to get monitoring metrics: " + e.getMessage(), e);
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
        return AIActionMetadata.builder().version(getVersion()).author("openHAB")
                .description("Retrieves comprehensive system monitoring metrics")
                .tags(List.of("monitoring", "metrics", "system", "performance"))
                .documentation(
                        "Provides detailed system monitoring metrics including CPU, memory, disk, JVM, and thread statistics")
                .examples(List.of("{} - Get all available metrics",
                        "{\"includeSystemMetrics\": true, \"includeJvmMetrics\": false} - Get only system metrics",
                        "{\"includeThreadMetrics\": true, \"includeNetworkMetrics\": true} - Focus on thread and network metrics"))
                .build();
    }

    @Override
    public Map<String, Object> getCapabilities() {
        return Map.of("filtering", true, "sorting", false, "pagination", false, "metadata", true, "async", true,
                "realTime", true);
    }

    @Override
    public void initialize(AIActionContext context) {
        logger.debug("GetMonitoringMetricsAction initialized for protocol: {}", context.getProtocol());
    }

    @Override
    public void cleanup() {
        logger.debug("GetMonitoringMetricsAction cleanup completed");
    }

    @Override
    public boolean isReady() {
        return true;
    }

    private Map<String, Object> getSystemMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();

        metrics.put("systemLoadAverage", osBean.getSystemLoadAverage());
        metrics.put("availableProcessors", osBean.getAvailableProcessors());
        metrics.put("arch", osBean.getArch());
        metrics.put("name", osBean.getName());
        metrics.put("version", osBean.getVersion());

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
                metrics.put("committedVirtualMemory", sunBean.getCommittedVirtualMemorySize());
            }
        } catch (Exception e) {
            logger.debug("Platform-specific system metrics not available: {}", e.getMessage());
        }

        return metrics;
    }

    private Map<String, Object> getJvmMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();

        // Runtime information
        metrics.put("startTime", runtimeBean.getStartTime());
        metrics.put("startTimeFormatted", Instant.ofEpochMilli(runtimeBean.getStartTime()).toString());
        metrics.put("uptime", runtimeBean.getUptime());
        metrics.put("uptimeFormatted", formatUptime(runtimeBean.getUptime()));
        metrics.put("name", runtimeBean.getName());
        metrics.put("version", runtimeBean.getVmVersion());
        metrics.put("vendor", runtimeBean.getVmVendor());

        // Memory information
        Map<String, Object> heapMemory = new HashMap<>();
        heapMemory.put("init", memoryBean.getHeapMemoryUsage().getInit());
        heapMemory.put("initFormatted", formatBytes(memoryBean.getHeapMemoryUsage().getInit()));
        heapMemory.put("used", memoryBean.getHeapMemoryUsage().getUsed());
        heapMemory.put("usedFormatted", formatBytes(memoryBean.getHeapMemoryUsage().getUsed()));
        heapMemory.put("committed", memoryBean.getHeapMemoryUsage().getCommitted());
        heapMemory.put("committedFormatted", formatBytes(memoryBean.getHeapMemoryUsage().getCommitted()));
        heapMemory.put("max", memoryBean.getHeapMemoryUsage().getMax());
        heapMemory.put("maxFormatted", formatBytes(memoryBean.getHeapMemoryUsage().getMax()));
        metrics.put("heapMemory", heapMemory);

        Map<String, Object> nonHeapMemory = new HashMap<>();
        nonHeapMemory.put("init", memoryBean.getNonHeapMemoryUsage().getInit());
        nonHeapMemory.put("initFormatted", formatBytes(memoryBean.getNonHeapMemoryUsage().getInit()));
        nonHeapMemory.put("used", memoryBean.getNonHeapMemoryUsage().getUsed());
        nonHeapMemory.put("usedFormatted", formatBytes(memoryBean.getNonHeapMemoryUsage().getUsed()));
        nonHeapMemory.put("committed", memoryBean.getNonHeapMemoryUsage().getCommitted());
        nonHeapMemory.put("committedFormatted", formatBytes(memoryBean.getNonHeapMemoryUsage().getCommitted()));
        nonHeapMemory.put("max", memoryBean.getNonHeapMemoryUsage().getMax());
        nonHeapMemory.put("maxFormatted", formatBytes(memoryBean.getNonHeapMemoryUsage().getMax()));
        metrics.put("nonHeapMemory", nonHeapMemory);

        return metrics;
    }

    private Map<String, Object> getThreadMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();

        metrics.put("threadCount", threadBean.getThreadCount());
        metrics.put("peakThreadCount", threadBean.getPeakThreadCount());
        metrics.put("daemonThreadCount", threadBean.getDaemonThreadCount());
        metrics.put("totalStartedThreadCount", threadBean.getTotalStartedThreadCount());

        // Thread state distribution
        Map<String, Integer> threadStates = new HashMap<>();
        Thread.State[] states = Thread.State.values();
        for (Thread.State state : states) {
            threadStates.put(state.name(), 0);
        }

        // Count threads by state
        Thread[] threads = new Thread[threadBean.getThreadCount()];
        threadBean.getThreadInfo(threadBean.getAllThreadIds());
        for (Thread thread : threads) {
            if (thread != null) {
                Thread.State state = thread.getState();
                threadStates.merge(state.name(), 1, Integer::sum);
            }
        }
        metrics.put("threadStates", threadStates);

        return metrics;
    }

    private Map<String, Object> getDiskMetrics() {
        Map<String, Object> metrics = new HashMap<>();

        try {
            String userDataDir = OpenHAB.getUserDataFolder();
            Path userDataPath = Paths.get(userDataDir);

            long totalSpace = Files.getFileStore(userDataPath).getTotalSpace();
            long usableSpace = Files.getFileStore(userDataPath).getUsableSpace();
            long usedSpace = totalSpace - usableSpace;

            metrics.put("totalSpace", totalSpace);
            metrics.put("totalSpaceFormatted", formatBytes(totalSpace));
            metrics.put("usableSpace", usableSpace);
            metrics.put("usableSpaceFormatted", formatBytes(usableSpace));
            metrics.put("usedSpace", usedSpace);
            metrics.put("usedSpaceFormatted", formatBytes(usedSpace));
            metrics.put("usagePercentage", String.format("%.2f%%", (usedSpace * 100.0) / totalSpace));
            metrics.put("freeSpace", usableSpace);
            metrics.put("freeSpaceFormatted", formatBytes(usableSpace));
            metrics.put("freeSpacePercentage", String.format("%.2f%%", (usableSpace * 100.0) / totalSpace));

        } catch (Exception e) {
            logger.warn("Failed to get disk metrics: {}", e.getMessage());
            metrics.put("error", "Failed to get disk metrics: " + e.getMessage());
        }

        return metrics;
    }

    private Map<String, Object> getNetworkMetrics() {
        Map<String, Object> metrics = new HashMap<>();

        // In a real implementation, you would use NetworkInterface and other network APIs
        // to get detailed network statistics
        metrics.put("note", "Network metrics collection not implemented in this version");
        metrics.put("available", false);

        return metrics;
    }

    private Map<String, Object> getProcessMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        Runtime runtime = Runtime.getRuntime();

        metrics.put("totalMemory", runtime.totalMemory());
        metrics.put("totalMemoryFormatted", formatBytes(runtime.totalMemory()));
        metrics.put("freeMemory", runtime.freeMemory());
        metrics.put("freeMemoryFormatted", formatBytes(runtime.freeMemory()));
        metrics.put("maxMemory", runtime.maxMemory());
        metrics.put("maxMemoryFormatted", formatBytes(runtime.maxMemory()));
        metrics.put("usedMemory", runtime.totalMemory() - runtime.freeMemory());
        metrics.put("usedMemoryFormatted", formatBytes(runtime.totalMemory() - runtime.freeMemory()));

        // Process ID
        String processId = ManagementFactory.getRuntimeMXBean().getName();
        if (processId.contains("@")) {
            processId = processId.split("@")[0];
        }
        metrics.put("processId", processId);

        return metrics;
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
