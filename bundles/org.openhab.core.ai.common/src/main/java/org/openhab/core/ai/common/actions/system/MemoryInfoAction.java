package org.openhab.core.ai.common.actions.system;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.time.Instant;
import java.util.ArrayList;
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
 * AIAction for retrieving detailed memory information including heap, non-heap, and memory pool details.
 * 
 * This action provides comprehensive memory diagnostics using real Java Management APIs.
 */
@Component(service = AIAction.class, immediate = true)
public class MemoryInfoAction implements AIAction {

    private static final Logger logger = LoggerFactory.getLogger(MemoryInfoAction.class);
    private static final String ACTION_ID = "openhab.system.memory-info";
    private static final String ACTION_NAME = "Memory Information";
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
        return "Retrieves detailed memory information including heap, non-heap, and memory pool details";
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
        properties.put("includeMemoryPools",
                Map.of("type", "boolean", "description", "Include detailed memory pool information", "default", true));
        properties.put("includeGarbageCollection",
                Map.of("type", "boolean", "description", "Include garbage collection statistics", "default", true));

        schema.put("properties", properties);
        schema.put("additionalProperties", false);
        return schema;
    }

    @Override
    public Map<String, Object> getReturnSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();
        properties.put("timestamp", Map.of("type", "string", "description", "Timestamp of the memory information"));
        properties.put("heapMemory", Map.of("type", "object", "description", "Heap memory usage information"));
        properties.put("nonHeapMemory", Map.of("type", "object", "description", "Non-heap memory usage information"));
        properties.put("memoryPools", Map.of("type", "array", "description", "Detailed memory pool information"));
        properties.put("garbageCollection", Map.of("type", "object", "description", "Garbage collection statistics"));
        properties.put("memoryAnalysis",
                Map.of("type", "object", "description", "Memory usage analysis and recommendations"));

        schema.put("properties", properties);
        return schema;
    }

    @Override
    public AIActionValidationResult validateParameters(Map<String, Object> parameters) {
        return AIActionValidationResult.valid(parameters);
    }

    @Override
    public AIActionResult execute(Map<String, Object> parameters, AIActionContext context) throws AIActionException {
        logger.debug("Executing MemoryInfoAction with context: {}", context.getProtocol());

        long startTime = System.currentTimeMillis();

        try {
            boolean includeMemoryPools = (Boolean) parameters.getOrDefault("includeMemoryPools", true);
            boolean includeGarbageCollection = (Boolean) parameters.getOrDefault("includeGarbageCollection", true);

            Map<String, Object> result = collectMemoryInfo(includeMemoryPools, includeGarbageCollection);

            long executionTime = System.currentTimeMillis() - startTime;
            return AIActionResult.success(result, executionTime);

        } catch (Exception e) {
            logger.error("Error executing MemoryInfoAction: {}", e.getMessage(), e);
            throw new AIActionException(ACTION_ID, "Failed to collect memory information: " + e.getMessage(), e);
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
                .description("Retrieves detailed memory information using Java Management APIs").version("1.0.0")
                .tags(List.of("system", "memory", "diagnostics")).build();
    }

    @Override
    public void initialize(AIActionContext context) {
        logger.debug("MemoryInfoAction initialized with context: {}", context.getProtocol());
    }

    @Override
    public void cleanup() {
        logger.debug("MemoryInfoAction cleanup completed");
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

    private Map<String, Object> collectMemoryInfo(boolean includeMemoryPools, boolean includeGarbageCollection) {
        Map<String, Object> memoryInfo = new HashMap<>();
        memoryInfo.put("timestamp", Instant.now().toString());

        // Basic memory information
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        memoryInfo.put("heapMemory", formatMemoryUsage(memoryBean.getHeapMemoryUsage()));
        memoryInfo.put("nonHeapMemory", formatMemoryUsage(memoryBean.getNonHeapMemoryUsage()));

        // Memory pools information
        if (includeMemoryPools) {
            List<Map<String, Object>> memoryPools = new ArrayList<>();
            for (MemoryPoolMXBean pool : ManagementFactory.getMemoryPoolMXBeans()) {
                Map<String, Object> poolInfo = new HashMap<>();
                poolInfo.put("name", pool.getName());
                poolInfo.put("type", pool.getType().toString());
                poolInfo.put("usage", formatMemoryUsage(pool.getUsage()));
                poolInfo.put("peakUsage", formatMemoryUsage(pool.getPeakUsage()));
                poolInfo.put("collectionUsage", formatMemoryUsage(pool.getCollectionUsage()));
                poolInfo.put("isValid", pool.isValid());
                poolInfo.put("isCollectionUsageThresholdSupported", pool.isCollectionUsageThresholdSupported());
                poolInfo.put("isUsageThresholdSupported", pool.isUsageThresholdSupported());

                if (pool.isUsageThresholdSupported()) {
                    poolInfo.put("usageThreshold", pool.getUsageThreshold());
                    poolInfo.put("usageThresholdCount", pool.getUsageThresholdCount());
                }

                memoryPools.add(poolInfo);
            }
            memoryInfo.put("memoryPools", memoryPools);
        }

        // Garbage collection information
        if (includeGarbageCollection) {
            Map<String, Object> gcInfo = new HashMap<>();
            List<Map<String, Object>> collectors = new ArrayList<>();

            for (java.lang.management.GarbageCollectorMXBean gcBean : ManagementFactory.getGarbageCollectorMXBeans()) {
                Map<String, Object> collectorInfo = new HashMap<>();
                collectorInfo.put("name", gcBean.getName());
                collectorInfo.put("collectionCount", gcBean.getCollectionCount());
                collectorInfo.put("collectionTime", gcBean.getCollectionTime());
                collectorInfo.put("isValid", gcBean.isValid());

                // Calculate collection rate
                long uptime = ManagementFactory.getRuntimeMXBean().getUptime();
                if (uptime > 0) {
                    double collectionRate = (double) gcBean.getCollectionCount() / (uptime / 1000.0);
                    collectorInfo.put("collectionRate", String.format("%.2f collections/second", collectionRate));
                }

                collectors.add(collectorInfo);
            }

            gcInfo.put("collectors", collectors);
            gcInfo.put("totalCollections", collectors.stream().mapToLong(c -> (Long) c.get("collectionCount")).sum());
            gcInfo.put("totalCollectionTime", collectors.stream().mapToLong(c -> (Long) c.get("collectionTime")).sum());

            memoryInfo.put("garbageCollection", gcInfo);
        }

        // Memory analysis and recommendations
        Map<String, Object> analysis = analyzeMemoryUsage(memoryBean);
        memoryInfo.put("memoryAnalysis", analysis);

        return memoryInfo;
    }

    private Map<String, Object> formatMemoryUsage(java.lang.management.MemoryUsage usage) {
        Map<String, Object> memory = new HashMap<>();
        memory.put("init", formatBytes(usage.getInit()));
        memory.put("used", formatBytes(usage.getUsed()));
        memory.put("committed", formatBytes(usage.getCommitted()));
        memory.put("max", usage.getMax() == -1 ? "unlimited" : formatBytes(usage.getMax()));
        memory.put("initBytes", usage.getInit());
        memory.put("usedBytes", usage.getUsed());
        memory.put("committedBytes", usage.getCommitted());
        memory.put("maxBytes", usage.getMax());

        if (usage.getMax() > 0) {
            double usagePercent = (double) usage.getUsed() / usage.getMax() * 100;
            memory.put("usagePercent", String.format("%.2f%%", usagePercent));
        }

        return memory;
    }

    private Map<String, Object> analyzeMemoryUsage(MemoryMXBean memoryBean) {
        Map<String, Object> analysis = new HashMap<>();

        java.lang.management.MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
        java.lang.management.MemoryUsage nonHeapUsage = memoryBean.getNonHeapMemoryUsage();

        // Heap analysis
        if (heapUsage.getMax() > 0) {
            double heapUsagePercent = (double) heapUsage.getUsed() / heapUsage.getMax() * 100;
            analysis.put("heapUsagePercent", heapUsagePercent);

            if (heapUsagePercent > 90) {
                analysis.put("heapStatus", "CRITICAL");
                analysis.put("heapRecommendation", "Consider increasing heap size or investigating memory leaks");
            } else if (heapUsagePercent > 75) {
                analysis.put("heapStatus", "WARNING");
                analysis.put("heapRecommendation", "Monitor heap usage closely");
            } else {
                analysis.put("heapStatus", "HEALTHY");
                analysis.put("heapRecommendation", "Heap usage is within normal range");
            }
        }

        // Memory efficiency analysis
        long totalUsed = heapUsage.getUsed() + nonHeapUsage.getUsed();
        long totalCommitted = heapUsage.getCommitted() + nonHeapUsage.getCommitted();

        if (totalCommitted > 0) {
            double efficiency = (double) totalUsed / totalCommitted * 100;
            analysis.put("memoryEfficiency", String.format("%.2f%%", efficiency));

            if (efficiency < 50) {
                analysis.put("efficiencyRecommendation", "Consider reducing heap size to improve memory efficiency");
            } else if (efficiency > 90) {
                analysis.put("efficiencyRecommendation", "Memory is efficiently utilized");
            } else {
                analysis.put("efficiencyRecommendation", "Memory efficiency is within normal range");
            }
        }

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
