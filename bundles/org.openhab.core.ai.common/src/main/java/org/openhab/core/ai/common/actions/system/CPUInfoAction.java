package org.openhab.core.ai.common.actions.system;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

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
 * AIAction for retrieving detailed CPU information including load, processors, and thread statistics.
 * 
 * This action provides comprehensive CPU diagnostics using real Java Management APIs.
 */
@Component(service = AIAction.class, immediate = true)
public class CPUInfoAction implements AIAction {

    private static final Logger logger = LoggerFactory.getLogger(CPUInfoAction.class);
    private static final String ACTION_ID = "openhab.system.cpu-info";
    private static final String ACTION_NAME = "CPU Information";
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
        return "Retrieves detailed CPU information including load, processors, and thread statistics";
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
        properties.put("includeThreadInfo",
                Map.of("type", "boolean", "description", "Include detailed thread information", "default", true));
        properties.put("includeProcessInfo",
                Map.of("type", "boolean", "description", "Include process information", "default", true));

        schema.put("properties", properties);
        schema.put("additionalProperties", false);
        return schema;
    }

    @Override
    public Map<String, Object> getReturnSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();
        properties.put("timestamp", Map.of("type", "string", "description", "Timestamp of the CPU information"));
        properties.put("systemLoad", Map.of("type", "object", "description", "System load information"));
        properties.put("processorInfo", Map.of("type", "object", "description", "Processor information"));
        properties.put("threadInfo", Map.of("type", "object", "description", "Thread statistics"));
        properties.put("processInfo", Map.of("type", "object", "description", "Process information"));
        properties.put("cpuAnalysis",
                Map.of("type", "object", "description", "CPU usage analysis and recommendations"));

        schema.put("properties", properties);
        return schema;
    }

    @Override
    public AIActionValidationResult validateParameters(Map<String, Object> parameters) {
        return AIActionValidationResult.valid(parameters);
    }

    @Override
    public AIActionResult execute(Map<String, Object> parameters, AIActionContext context) throws AIActionException {
        logger.debug("Executing CPUInfoAction with context: {}", context.getProtocol());

        long startTime = System.currentTimeMillis();

        try {
            boolean includeThreadInfo = (Boolean) parameters.getOrDefault("includeThreadInfo", true);
            boolean includeProcessInfo = (Boolean) parameters.getOrDefault("includeProcessInfo", true);

            Map<String, Object> result = collectCPUInfo(includeThreadInfo, includeProcessInfo);

            long executionTime = System.currentTimeMillis() - startTime;
            return AIActionResult.success(result, executionTime);

        } catch (Exception e) {
            logger.error("Error executing CPUInfoAction: {}", e.getMessage(), e);
            throw new AIActionException(ACTION_ID, "Failed to collect CPU information: " + e.getMessage(), e);
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
        return AIActionMetadata.builder().author("openHAB")
                .description("Retrieves detailed CPU information using Java Management APIs").version("1.0.0")
                .tags(List.of("system", "cpu", "diagnostics")).build();
    }

    @Override
    public void initialize(AIActionContext context) {
        logger.debug("CPUInfoAction initialized with context: {}", context.getProtocol());
    }

    @Override
    public void cleanup() {
        logger.debug("CPUInfoAction cleanup completed");
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
        capabilities.put("dataSource", "Java Management APIs");
        return capabilities;
    }

    private Map<String, Object> collectCPUInfo(boolean includeThreadInfo, boolean includeProcessInfo) {
        Map<String, Object> cpuInfo = new HashMap<>();
        cpuInfo.put("timestamp", Instant.now().toString());

        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();

        // System load information
        Map<String, Object> systemLoad = new HashMap<>();
        double loadAverage = osBean.getSystemLoadAverage();
        systemLoad.put("systemLoadAverage", loadAverage);
        systemLoad.put("systemLoadAverageFormatted", loadAverage >= 0 ? String.format("%.2f", loadAverage) : "N/A");

        // Load analysis
        int availableProcessors = osBean.getAvailableProcessors();
        systemLoad.put("availableProcessors", availableProcessors);

        if (loadAverage >= 0) {
            double loadPerProcessor = loadAverage / availableProcessors;
            systemLoad.put("loadPerProcessor", String.format("%.2f", loadPerProcessor));

            if (loadPerProcessor > 2.0) {
                systemLoad.put("loadStatus", "CRITICAL");
                systemLoad.put("loadRecommendation", "System is heavily loaded, consider reducing workload");
            } else if (loadPerProcessor > 1.0) {
                systemLoad.put("loadStatus", "WARNING");
                systemLoad.put("loadRecommendation", "System is moderately loaded, monitor closely");
            } else {
                systemLoad.put("loadStatus", "HEALTHY");
                systemLoad.put("loadRecommendation", "System load is within normal range");
            }
        } else {
            systemLoad.put("loadStatus", "UNKNOWN");
            systemLoad.put("loadRecommendation", "Load average not available on this platform");
        }

        cpuInfo.put("systemLoad", systemLoad);

        // Processor information
        Map<String, Object> processorInfo = new HashMap<>();
        processorInfo.put("availableProcessors", availableProcessors);
        processorInfo.put("architecture", osBean.getArch());
        processorInfo.put("operatingSystem", osBean.getName());
        processorInfo.put("operatingSystemVersion", osBean.getVersion());

        // CPU time information (if supported)
        if (threadBean.isCurrentThreadCpuTimeSupported()) {
            processorInfo.put("cpuTimeSupported", true);
            long currentThreadCpuTime = threadBean.getCurrentThreadCpuTime();
            processorInfo.put("currentThreadCpuTime", currentThreadCpuTime);
            processorInfo.put("currentThreadCpuTimeFormatted", formatDuration(currentThreadCpuTime / 1000000));
        } else {
            processorInfo.put("cpuTimeSupported", false);
        }

        cpuInfo.put("processorInfo", processorInfo);

        // Thread information
        if (includeThreadInfo) {
            Map<String, Object> threadInfo = new HashMap<>();
            threadInfo.put("threadCount", threadBean.getThreadCount());
            threadInfo.put("peakThreadCount", threadBean.getPeakThreadCount());
            threadInfo.put("daemonThreadCount", threadBean.getDaemonThreadCount());
            threadInfo.put("totalStartedThreadCount", threadBean.getTotalStartedThreadCount());

            // Thread analysis
            int threadCount = threadBean.getThreadCount();
            if (threadCount > 1000) {
                threadInfo.put("threadStatus", "CRITICAL");
                threadInfo.put("threadRecommendation", "High thread count detected, investigate for thread leaks");
            } else if (threadCount > 500) {
                threadInfo.put("threadStatus", "WARNING");
                threadInfo.put("threadRecommendation", "Moderate thread count, monitor for increases");
            } else {
                threadInfo.put("threadStatus", "HEALTHY");
                threadInfo.put("threadRecommendation", "Thread count is within normal range");
            }

            cpuInfo.put("threadInfo", threadInfo);
        }

        // Process information
        if (includeProcessInfo) {
            Map<String, Object> processInfo = new HashMap<>();
            processInfo.put("processId", getProcessId());
            processInfo.put("processCpuLoad", getProcessCpuLoad(osBean));

            // Memory usage for the process
            Runtime runtime = Runtime.getRuntime();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemory = totalMemory - freeMemory;

            processInfo.put("processMemoryUsed", formatBytes(usedMemory));
            processInfo.put("processMemoryTotal", formatBytes(totalMemory));
            processInfo.put("processMemoryFree", formatBytes(freeMemory));

            if (totalMemory > 0) {
                double memoryUsagePercent = (double) usedMemory / totalMemory * 100;
                processInfo.put("processMemoryUsagePercent", String.format("%.2f%%", memoryUsagePercent));
            }

            cpuInfo.put("processInfo", processInfo);
        }

        // CPU analysis and recommendations
        Map<String, Object> analysis = analyzeCPUUsage(osBean, threadBean);
        cpuInfo.put("cpuAnalysis", analysis);

        return cpuInfo;
    }

    private String getProcessId() {
        try {
            String processName = ManagementFactory.getRuntimeMXBean().getName();
            return processName.split("@")[0];
        } catch (Exception e) {
            return "unknown";
        }
    }

    private String getProcessCpuLoad(OperatingSystemMXBean osBean) {
        try {
            // This is a simplified approach - in a real implementation you'd need to
            // calculate CPU usage over time intervals
            return "N/A (requires time-based calculation)";
        } catch (Exception e) {
            return "N/A";
        }
    }

    private Map<String, Object> analyzeCPUUsage(OperatingSystemMXBean osBean, ThreadMXBean threadBean) {
        Map<String, Object> analysis = new HashMap<>();

        double loadAverage = osBean.getSystemLoadAverage();
        int availableProcessors = osBean.getAvailableProcessors();
        int threadCount = threadBean.getThreadCount();

        // Overall system health assessment
        int healthScore = 100;
        List<String> recommendations = new ArrayList<>();

        // Load-based assessment
        if (loadAverage >= 0) {
            double loadPerProcessor = loadAverage / availableProcessors;
            if (loadPerProcessor > 2.0) {
                healthScore -= 40;
                recommendations.add("System load is critical - consider reducing workload or adding resources");
            } else if (loadPerProcessor > 1.0) {
                healthScore -= 20;
                recommendations.add("System load is high - monitor closely and consider optimization");
            }
        }

        // Thread-based assessment
        if (threadCount > 1000) {
            healthScore -= 30;
            recommendations.add("Thread count is very high - investigate for thread leaks");
        } else if (threadCount > 500) {
            healthScore -= 15;
            recommendations.add("Thread count is elevated - monitor for increases");
        }

        // Processor utilization assessment
        if (availableProcessors < 2) {
            healthScore -= 10;
            recommendations.add("Single processor detected - consider multi-core system for better performance");
        }

        analysis.put("healthScore", Math.max(0, healthScore));
        analysis.put("recommendations", recommendations);

        if (healthScore >= 80) {
            analysis.put("overallStatus", "HEALTHY");
        } else if (healthScore >= 60) {
            analysis.put("overallStatus", "WARNING");
        } else {
            analysis.put("overallStatus", "CRITICAL");
        }

        return analysis;
    }

    private String formatDuration(long milliseconds) {
        long seconds = milliseconds / 1000;
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
        if (bytes < 1024)
            return bytes + " B";
        if (bytes < 1024 * 1024)
            return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024)
            return String.format("%.1f MB", bytes / (1024.0 * 1024));
        return String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024));
    }
}
