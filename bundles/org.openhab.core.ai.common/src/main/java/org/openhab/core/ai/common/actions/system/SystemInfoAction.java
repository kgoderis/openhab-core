package org.openhab.core.ai.common.actions.system;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.RuntimeMXBean;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
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
 * AIAction for retrieving comprehensive openHAB system information.
 * 
 * 
 */
@Component(service = AIAction.class, immediate = true)
public class SystemInfoAction implements AIAction {

    private static final Logger logger = LoggerFactory.getLogger(SystemInfoAction.class);
    private static final String ACTION_ID = "openhab.system.info";
    private static final String ACTION_NAME = "System Information";

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
        return "Retrieves comprehensive system information including JVM details, memory usage, uptime, and platform information";
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
        schema.put("properties", Map.of("includeDetails",
                Map.of("type", "boolean", "description", "Include detailed technical information", "default", true)));
        schema.put("additionalProperties", false);
        return schema;
    }

    @Override
    public Map<String, Object> getReturnSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();
        properties.put("timestamp", Map.of("type", "string", "description", "ISO timestamp of the system info"));
        properties.put("systemName", Map.of("type", "string", "description", "Operating system name"));
        properties.put("systemVersion", Map.of("type", "string", "description", "Operating system version"));
        properties.put("systemArchitecture", Map.of("type", "string", "description", "System architecture"));
        properties.put("java", Map.of("type", "object", "description", "Java runtime information"));
        properties.put("memory", Map.of("type", "object", "description", "Memory usage information"));
        properties.put("openhab", Map.of("type", "object", "description", "openHAB specific information"));

        schema.put("properties", properties);
        schema.put("required",
                List.of("timestamp", "systemName", "systemVersion", "systemArchitecture", "java", "memory", "openhab"));
        return schema;
    }

    @Override
    public AIActionValidationResult validateParameters(Map<String, Object> parameters) {
        // This action accepts optional parameters, all valid
        return AIActionValidationResult.valid(parameters);
    }

    @Override
    public AIActionResult execute(Map<String, Object> parameters, AIActionContext context) throws AIActionException {
        long startTime = System.currentTimeMillis();
        logger.debug("Executing system info action with parameters: {}", parameters);

        try {
            boolean includeDetails = (Boolean) parameters.getOrDefault("includeDetails", true);
            Map<String, Object> systemInfo = collectSystemInfo(includeDetails);

            long executionTime = System.currentTimeMillis() - startTime;
            logger.debug("System info action completed in {}ms", executionTime);

            return AIActionResult.success(systemInfo, executionTime);

        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            logger.error("Failed to collect system information", e);
            throw new AIActionException(ACTION_ID, "Failed to collect system information: " + e.getMessage(), e);
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
                .description("Provides comprehensive openHAB system information")
                .tags(List.of("system", "info", "monitoring", "diagnostics"))
                .documentation(
                        "Retrieves comprehensive system information including JVM details, memory usage, uptime, and platform information")
                .examples(List.of("{} - Get basic system information",
                        "{\"includeDetails\": false} - Get minimal system information"))
                .build();
    }

    @Override
    public Map<String, Object> getCapabilities() {
        return Map.of("filtering", false, "sorting", false, "pagination", false, "metadata", true, "async", true);
    }

    @Override
    public void initialize(AIActionContext context) {
        logger.debug("SystemInfoAction initialized for protocol: {}", context.getProtocol());
    }

    @Override
    public void cleanup() {
        logger.debug("SystemInfoAction cleanup completed");
    }

    @Override
    public boolean isReady() {
        return true; // No external dependencies required
    }

    private Map<String, Object> collectSystemInfo(boolean includeDetails) {
        Map<String, Object> info = new HashMap<>();

        // Basic system information
        info.put("timestamp",
                Instant.now().atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        info.put("systemName", System.getProperty("os.name", "unknown"));
        info.put("systemVersion", System.getProperty("os.version", "unknown"));
        info.put("systemArchitecture", System.getProperty("os.arch", "unknown"));

        // Java runtime information
        RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
        Map<String, Object> javaInfo = new HashMap<>();
        javaInfo.put("version", System.getProperty("java.version", "unknown"));
        javaInfo.put("vendor", System.getProperty("java.vendor", "unknown"));
        javaInfo.put("home", System.getProperty("java.home", "unknown"));
        javaInfo.put("vmName", runtime.getVmName());
        javaInfo.put("vmVersion", runtime.getVmVersion());
        javaInfo.put("vmVendor", runtime.getVmVendor());
        javaInfo.put("startTime", Instant.ofEpochMilli(runtime.getStartTime()).atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        javaInfo.put("uptime", formatUptime(runtime.getUptime()));

        if (includeDetails) {
            javaInfo.put("inputArguments", runtime.getInputArguments());
            javaInfo.put("classPath", runtime.getClassPath());
            javaInfo.put("libraryPath", runtime.getLibraryPath());
        }

        info.put("java", javaInfo);

        // Memory information
        MemoryMXBean memory = ManagementFactory.getMemoryMXBean();
        Map<String, Object> memoryInfo = new HashMap<>();
        memoryInfo.put("heapMemory", formatMemoryUsage(memory.getHeapMemoryUsage()));
        memoryInfo.put("nonHeapMemory", formatMemoryUsage(memory.getNonHeapMemoryUsage()));
        info.put("memory", memoryInfo);

        // openHAB specific information
        Map<String, Object> openhabInfo = new HashMap<>();
        openhabInfo.put("userHome", System.getProperty("openhab.home", "unknown"));
        openhabInfo.put("userDataDir", System.getProperty("openhab.userdata", "unknown"));
        openhabInfo.put("configDir", System.getProperty("openhab.conf", "unknown"));
        openhabInfo.put("logDir", System.getProperty("openhab.logdir", "unknown"));
        info.put("openhab", openhabInfo);

        // System properties (if details requested)
        if (includeDetails) {
            Map<String, Object> systemProps = new HashMap<>();
            System.getProperties().forEach((key, value) -> {
                if (key instanceof String) {
                    systemProps.put((String) key, value);
                }
            });
            info.put("systemProperties", systemProps);
        }

        return info;
    }

    private String formatUptime(long uptimeMs) {
        long seconds = uptimeMs / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        StringBuilder sb = new StringBuilder();
        if (days > 0) {
            sb.append(days).append(" days, ");
        }
        sb.append(String.format("%02d:%02d:%02d", hours % 24, minutes % 60, seconds % 60));
        return sb.toString();
    }

    private Map<String, Object> formatMemoryUsage(java.lang.management.MemoryUsage usage) {
        Map<String, Object> memory = new HashMap<>();
        memory.put("used", formatBytes(usage.getUsed()));
        memory.put("committed", formatBytes(usage.getCommitted()));
        memory.put("max", usage.getMax() == -1 ? "unlimited" : formatBytes(usage.getMax()));
        memory.put("usedBytes", usage.getUsed());
        memory.put("committedBytes", usage.getCommitted());
        memory.put("maxBytes", usage.getMax());
        return memory;
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
