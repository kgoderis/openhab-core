package org.openhab.core.ai.common.actions.system;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.openhab.core.ai.common.api.action.AIAction;
import org.openhab.core.ai.common.api.action.AIActionContext;
import org.openhab.core.ai.common.api.action.AIActionException;
import org.openhab.core.ai.common.api.action.AIActionMetadata;
import org.openhab.core.ai.common.api.action.AIActionResult;
import org.openhab.core.ai.common.api.action.AIActionValidationResult;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AIAction for retrieving real-time openHAB system status.
 * 
 * 
 */
@Component(service = AIAction.class, immediate = true)
public class SystemStatusAction implements AIAction {

    private static final Logger logger = LoggerFactory.getLogger(SystemStatusAction.class);
    private static final String ACTION_ID = "openhab.system.status";
    private static final String ACTION_NAME = "System Status";

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
        return "Retrieves real-time system status including CPU usage, memory consumption, thread count, and system load";
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
        schema.put("properties", Map.of());
        schema.put("additionalProperties", false);
        return schema;
    }

    @Override
    public Map<String, Object> getReturnSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();
        properties.put("timestamp", Map.of("type", "string", "description", "ISO timestamp of the status check"));
        properties.put("status", Map.of("type", "string", "description", "Overall system status"));
        properties.put("cpu", Map.of("type", "object", "description", "CPU and system load information"));
        properties.put("memory", Map.of("type", "object", "description", "Memory usage information"));
        properties.put("threads", Map.of("type", "object", "description", "Thread information"));
        properties.put("garbageCollection", Map.of("type", "object", "description", "Garbage collection statistics"));
        properties.put("health", Map.of("type", "object", "description", "System health assessment"));

        schema.put("properties", properties);
        schema.put("required",
                List.of("timestamp", "status", "cpu", "memory", "threads", "garbageCollection", "health"));
        return schema;
    }

    @Override
    public AIActionValidationResult validateParameters(Map<String, Object> parameters) {
        // This action accepts no parameters, all valid
        return AIActionValidationResult.valid(parameters);
    }

    @Override
    public AIActionResult execute(Map<String, Object> parameters, AIActionContext context) throws AIActionException {
        long startTime = System.currentTimeMillis();
        logger.debug("Executing system status action with parameters: {}", parameters);

        try {
            Map<String, Object> status = collectSystemStatus();

            long executionTime = System.currentTimeMillis() - startTime;
            logger.debug("System status action completed in {}ms", executionTime);

            return AIActionResult.success(status, executionTime);

        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            logger.error("Failed to collect system status", e);
            throw new AIActionException(ACTION_ID, "Failed to collect system status: " + e.getMessage(), e);
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
                .description("Provides real-time openHAB system status information")
                .tags(List.of("system", "status", "monitoring", "real-time"))
                .documentation(
                        "Retrieves real-time system status including CPU usage, memory consumption, thread count, and system load")
                .examples(List.of("{} - Get current system status")).build();
    }

    @Override
    public Map<String, Object> getCapabilities() {
        return Map.of("filtering", false, "sorting", false, "pagination", false, "metadata", true, "async", true);
    }

    @Override
    public void initialize(AIActionContext context) {
        logger.debug("SystemStatusAction initialized for protocol: {}", context.getProtocol());
    }

    @Override
    public void cleanup() {
        logger.debug("SystemStatusAction cleanup completed");
    }

    @Override
    public boolean isReady() {
        return true; // No external dependencies required
    }

    private Map<String, Object> collectSystemStatus() {
        Map<String, Object> status = new HashMap<>();

        status.put("timestamp", Instant.now().toString());
        status.put("status", "RUNNING");

        // CPU and System Load
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        Map<String, Object> cpuInfo = new HashMap<>();
        cpuInfo.put("availableProcessors", osBean.getAvailableProcessors());
        cpuInfo.put("systemLoadAverage", osBean.getSystemLoadAverage());

        // Try to get additional CPU info if available (Java 14+ or platform-specific)
        try {
            if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
                com.sun.management.OperatingSystemMXBean sunBean = (com.sun.management.OperatingSystemMXBean) osBean;
                cpuInfo.put("processCpuLoad", String.format("%.2f%%", sunBean.getProcessCpuLoad() * 100));
                cpuInfo.put("systemCpuLoad", String.format("%.2f%%", sunBean.getSystemCpuLoad() * 100));
                cpuInfo.put("processCpuTime", sunBean.getProcessCpuTime());
            }
        } catch (Exception e) {
            // Platform-specific beans not available, continue with basic info
            logger.debug("Platform-specific CPU information not available: {}", e.getMessage());
        }

        status.put("cpu", cpuInfo);

        // Memory Status
        Runtime runtime = Runtime.getRuntime();
        Map<String, Object> memoryStatus = new HashMap<>();
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;

        memoryStatus.put("maxMemory", maxMemory);
        memoryStatus.put("totalMemory", totalMemory);
        memoryStatus.put("usedMemory", usedMemory);
        memoryStatus.put("freeMemory", freeMemory);
        memoryStatus.put("memoryUsage", String.format("%.2f%%", (usedMemory * 100.0) / maxMemory));

        // Human readable formats
        memoryStatus.put("maxMemoryFormatted", formatBytes(maxMemory));
        memoryStatus.put("totalMemoryFormatted", formatBytes(totalMemory));
        memoryStatus.put("usedMemoryFormatted", formatBytes(usedMemory));
        memoryStatus.put("freeMemoryFormatted", formatBytes(freeMemory));

        status.put("memory", memoryStatus);

        // Thread Information
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        Map<String, Object> threadInfo = new HashMap<>();
        threadInfo.put("totalStartedThreadCount", threadBean.getTotalStartedThreadCount());
        threadInfo.put("threadCount", threadBean.getThreadCount());
        threadInfo.put("peakThreadCount", threadBean.getPeakThreadCount());
        threadInfo.put("daemonThreadCount", threadBean.getDaemonThreadCount());

        status.put("threads", threadInfo);

        // Garbage Collection Status
        Map<String, Object> gcInfo = new HashMap<>();
        List<GarbageCollectorMXBean> gcBeans = ManagementFactory.getGarbageCollectorMXBeans();
        for (GarbageCollectorMXBean gcBean : gcBeans) {
            Map<String, Object> collectorInfo = new HashMap<>();
            collectorInfo.put("collectionCount", gcBean.getCollectionCount());
            collectorInfo.put("collectionTime", gcBean.getCollectionTime());
            gcInfo.put(gcBean.getName(), collectorInfo);
        }
        status.put("garbageCollection", gcInfo);

        // System Health Assessment
        Map<String, Object> health = new HashMap<>();
        health.put("memoryPressure", assessMemoryPressure(usedMemory, maxMemory));
        health.put("threadHealth", assessThreadHealth(threadBean.getThreadCount()));
        health.put("overallStatus", assessOverallHealth(usedMemory, maxMemory, threadBean.getThreadCount()));

        status.put("health", health);

        return status;
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

    private String assessMemoryPressure(long usedMemory, long maxMemory) {
        double usage = (usedMemory * 100.0) / maxMemory;
        if (usage > 90)
            return "CRITICAL";
        if (usage > 75)
            return "HIGH";
        if (usage > 50)
            return "MODERATE";
        return "LOW";
    }

    private String assessThreadHealth(int threadCount) {
        if (threadCount > 1000)
            return "HIGH_THREAD_COUNT";
        if (threadCount > 500)
            return "MODERATE_THREAD_COUNT";
        return "NORMAL";
    }

    private String assessOverallHealth(long usedMemory, long maxMemory, int threadCount) {
        double memoryUsage = (usedMemory * 100.0) / maxMemory;

        if (memoryUsage > 90 || threadCount > 1000)
            return "CRITICAL";
        if (memoryUsage > 75 || threadCount > 500)
            return "WARNING";
        return "HEALTHY";
    }
}
