package org.openhab.core.ai.action.library.system;

import java.io.File;
import java.lang.management.ClassLoadingMXBean;
import java.lang.management.CompilationMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

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
 * AI Action for collecting comprehensive system diagnostic information.
 * 
 * 
 */
@Component(service = Action.class, immediate = true)
public class SystemDiagnosticsAction implements Action {

    private static final Logger logger = LoggerFactory.getLogger(SystemDiagnosticsAction.class);

    private static final String ACTION_ID = "openhab.system.diagnostics";
    private static final String ACTION_NAME = "System Diagnostics";
    private static final String DESCRIPTION = "Collects comprehensive system diagnostic information including environment, network, file system, and runtime details";
    private static final String CATEGORY = "system";
    private static final String VERSION = "1.0.0";

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
        return DESCRIPTION;
    }

    @Override
    public String getCategory() {
        return CATEGORY;
    }

    @Override
    public String getVersion() {
        return VERSION;
    }

    @Override
    public Map<String, Object> getParameterSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");
        schema.put("properties", Map.of("includeNetworkInfo",
                Map.of("type", "boolean", "description", "Include network interface information", "default", true),
                "includeEnvironmentVariables",
                Map.of("type", "boolean", "description", "Include environment variables (may contain sensitive data)",
                        "default", false),
                "includeSystemProperties",
                Map.of("type", "boolean", "description", "Include all system properties", "default", true)));
        schema.put("additionalProperties", false);
        return schema;
    }

    @Override
    public Map<String, Object> getReturnSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();
        properties.put("diagnosticsId", Map.of("type", "string"));
        properties.put("timestamp", Map.of("type", "string"));
        properties.put("systemInfo", Map.of("type", "object"));
        properties.put("javaInfo", Map.of("type", "object"));
        properties.put("memoryDiagnostics", Map.of("type", "object"));
        properties.put("threadDiagnostics", Map.of("type", "object"));
        properties.put("fileSystemDiagnostics", Map.of("type", "object"));
        properties.put("openHABDiagnostics", Map.of("type", "object"));
        properties.put("networkDiagnostics", Map.of("type", "object"));
        properties.put("environmentVariables", Map.of("type", "object"));
        properties.put("systemProperties", Map.of("type", "object"));
        properties.put("runtimeDiagnostics", Map.of("type", "object"));

        schema.put("properties", properties);
        return schema;
    }

    @Override
    public ActionValidationResult validateParameters(Map<String, Object> parameters) {
        return ActionValidationResult.valid(parameters);
    }

    @Override
    public ActionResult execute(Map<String, Object> parameters, ActionContext context) throws ActionException {
        long startTime = System.currentTimeMillis();

        try {
            logger.debug("Executing system diagnostics action with parameters: {}", parameters);

            boolean includeNetworkInfo = (Boolean) parameters.getOrDefault("includeNetworkInfo", true);
            boolean includeEnvironmentVariables = (Boolean) parameters.getOrDefault("includeEnvironmentVariables",
                    false);
            boolean includeSystemProperties = (Boolean) parameters.getOrDefault("includeSystemProperties", true);

            Map<String, Object> diagnostics = collectDiagnostics(includeNetworkInfo, includeEnvironmentVariables,
                    includeSystemProperties);

            long executionTime = System.currentTimeMillis() - startTime;
            logger.debug("System diagnostics action completed in {}ms", executionTime);

            return ActionResult.success(diagnostics, executionTime);

        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            logger.error("System diagnostics action failed", e);
            throw new ActionException(ACTION_ID, "Failed to collect diagnostics: " + e.getMessage(), e);
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
                .description("Provides comprehensive system diagnostic information for troubleshooting")
                .tags(List.of("diagnostics", "system", "monitoring", "troubleshooting"))
                .documentation(
                        "Collects detailed system information including memory, threads, file system, network, and runtime diagnostics")
                .examples(
                        List.of("Basic diagnostics: {}", "Include environment: {\"includeEnvironmentVariables\": true}",
                                "Network only: {\"includeNetworkInfo\": true, \"includeSystemProperties\": false}"))
                .build();
    }

    @Override
    public Map<String, Object> getCapabilities() {
        return Map.of("system_info", true, "memory_diagnostics", true, "thread_diagnostics", true,
                "file_system_diagnostics", true, "network_diagnostics", true, "environment_variables", true,
                "system_properties", true, "runtime_diagnostics", true, "async", true);
    }

    @Override
    public void initialize(ActionContext context) {
        logger.debug("SystemDiagnosticsAction initialized for protocol: {}", context.getProtocol());
    }

    @Override
    public void cleanup() {
        logger.debug("SystemDiagnosticsAction cleanup completed");
    }

    @Override
    public boolean isReady() {
        return true;
    }

    private Map<String, Object> collectDiagnostics(boolean includeNetworkInfo, boolean includeEnvironmentVariables,
            boolean includeSystemProperties) {
        Map<String, Object> diagnostics = new HashMap<>();

        // Generate diagnostics ID
        String diagnosticsId = "openhab-diagnostics-"
                + Instant.now().atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        diagnostics.put("diagnosticsId", diagnosticsId);
        diagnostics.put("timestamp", Instant.now().toString());

        // Collect system information
        diagnostics.put("systemInfo", collectSystemInfo());

        // Collect Java information
        diagnostics.put("javaInfo", collectJavaInfo());

        // Collect memory diagnostics
        diagnostics.put("memoryDiagnostics", collectMemoryDiagnostics());

        // Collect thread diagnostics
        diagnostics.put("threadDiagnostics", collectThreadDiagnostics());

        // Collect file system diagnostics
        diagnostics.put("fileSystemDiagnostics", collectFileSystemDiagnostics());

        // Collect openHAB diagnostics
        diagnostics.put("openHABDiagnostics", collectOpenHABDiagnostics());

        // Collect network diagnostics if requested
        if (includeNetworkInfo) {
            diagnostics.put("networkDiagnostics", collectNetworkDiagnostics());
        }

        // Collect environment variables if requested
        if (includeEnvironmentVariables) {
            diagnostics.put("environmentVariables", collectEnvironmentVariables());
        }

        // Collect system properties if requested
        if (includeSystemProperties) {
            diagnostics.put("systemProperties", collectSystemProperties());
        }

        // Collect runtime diagnostics
        diagnostics.put("runtimeDiagnostics", collectRuntimeDiagnostics());

        return diagnostics;
    }

    private String getHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            return "unknown";
        }
    }

    private Map<String, Object> collectSystemInfo() {
        Map<String, Object> info = new HashMap<>();

        info.put("hostname", getHostname());
        info.put("osName", System.getProperty("os.name"));
        info.put("osVersion", System.getProperty("os.version"));
        info.put("osArch", System.getProperty("os.arch"));
        info.put("userName", System.getProperty("user.name"));
        info.put("userHome", System.getProperty("user.home"));
        info.put("userDir", System.getProperty("user.dir"));
        info.put("availableProcessors", Runtime.getRuntime().availableProcessors());
        info.put("timestamp",
                Instant.now().atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));

        return info;
    }

    private Map<String, Object> collectJavaInfo() {
        Map<String, Object> info = new HashMap<>();

        info.put("javaVersion", System.getProperty("java.version"));
        info.put("javaVendor", System.getProperty("java.vendor"));
        info.put("javaHome", System.getProperty("java.home"));
        info.put("javaClassPath", System.getProperty("java.class.path"));
        info.put("javaLibraryPath", System.getProperty("java.library.path"));
        info.put("jvmName", ManagementFactory.getRuntimeMXBean().getVmName());
        info.put("jvmVersion", ManagementFactory.getRuntimeMXBean().getVmVersion());
        info.put("jvmVendor", ManagementFactory.getRuntimeMXBean().getVmVendor());
        info.put("startTime", ManagementFactory.getRuntimeMXBean().getStartTime());
        info.put("uptime", ManagementFactory.getRuntimeMXBean().getUptime());

        return info;
    }

    private Map<String, Object> collectMemoryDiagnostics() {
        Map<String, Object> diagnostics = new HashMap<>();

        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        long maxMemory = runtime.maxMemory();

        diagnostics.put("totalMemory", totalMemory);
        diagnostics.put("freeMemory", freeMemory);
        diagnostics.put("usedMemory", usedMemory);
        diagnostics.put("maxMemory", maxMemory);
        diagnostics.put("memoryUsagePercent", totalMemory > 0 ? (double) usedMemory / totalMemory * 100 : 0);
        diagnostics.put("maxMemoryUsagePercent", maxMemory > 0 ? (double) usedMemory / maxMemory * 100 : 0);

        // Memory pool information
        Map<String, Object> memoryPools = new HashMap<>();
        ManagementFactory.getMemoryPoolMXBeans().forEach(pool -> {
            Map<String, Object> poolInfo = new HashMap<>();
            poolInfo.put("type", pool.getType().toString());
            poolInfo.put("usage", formatMemoryUsage(pool.getUsage()));
            poolInfo.put("peakUsage", formatMemoryUsage(pool.getPeakUsage()));
            memoryPools.put(pool.getName(), poolInfo);
        });
        diagnostics.put("memoryPools", memoryPools);

        return diagnostics;
    }

    private Map<String, Object> collectThreadDiagnostics() {
        Map<String, Object> diagnostics = new HashMap<>();

        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        int threadCount = threadBean.getThreadCount();
        int peakThreadCount = threadBean.getPeakThreadCount();
        int daemonThreadCount = threadBean.getDaemonThreadCount();
        long totalStartedThreadCount = threadBean.getTotalStartedThreadCount();

        diagnostics.put("threadCount", threadCount);
        diagnostics.put("peakThreadCount", peakThreadCount);
        diagnostics.put("daemonThreadCount", daemonThreadCount);
        diagnostics.put("totalStartedThreadCount", totalStartedThreadCount);

        // Thread state breakdown
        Map<String, Integer> threadStates = new HashMap<>();
        ThreadInfo[] threadInfos = threadBean.dumpAllThreads(false, false);
        for (ThreadInfo info : threadInfos) {
            String state = info.getThreadState().toString();
            threadStates.merge(state, 1, Integer::sum);
        }
        diagnostics.put("threadStates", threadStates);

        return diagnostics;
    }

    private Map<String, Object> collectFileSystemDiagnostics() {
        Map<String, Object> diagnostics = new HashMap<>();

        File[] roots = File.listRoots();
        List<Map<String, Object>> fileSystems = new ArrayList<>();

        for (File root : roots) {
            Map<String, Object> fsInfo = new HashMap<>();
            fsInfo.put("path", root.getPath());
            fsInfo.put("totalSpace", root.getTotalSpace());
            fsInfo.put("freeSpace", root.getFreeSpace());
            fsInfo.put("usableSpace", root.getUsableSpace());
            fsInfo.put("usedSpace", root.getTotalSpace() - root.getFreeSpace());
            fsInfo.put("usagePercent",
                    root.getTotalSpace() > 0
                            ? (double) (root.getTotalSpace() - root.getFreeSpace()) / root.getTotalSpace() * 100
                            : 0);
            fileSystems.add(fsInfo);
        }

        diagnostics.put("fileSystems", fileSystems);

        // Current working directory info
        File currentDir = new File(".");
        diagnostics.put("currentDirectory", currentDir.getAbsolutePath());
        diagnostics.put("currentDirectoryFreeSpace", currentDir.getFreeSpace());

        return diagnostics;
    }

    private Map<String, Object> collectOpenHABDiagnostics() {
        Map<String, Object> diagnostics = new HashMap<>();

        try {
            // Basic openHAB information
            diagnostics.put("openHABVersion", OpenHAB.getVersion());
            diagnostics.put("configFolder", OpenHAB.getConfigFolder());
            diagnostics.put("userDataFolder", OpenHAB.getUserDataFolder());

            // Check if config folder exists and is writable
            File configFolder = new File(OpenHAB.getConfigFolder());
            diagnostics.put("configFolderExists", configFolder.exists());
            diagnostics.put("configFolderWritable", configFolder.canWrite());

            // Check if user data folder exists and is writable
            File userDataFolder = new File(OpenHAB.getUserDataFolder());
            diagnostics.put("userDataFolderExists", userDataFolder.exists());
            diagnostics.put("userDataFolderWritable", userDataFolder.canWrite());

        } catch (Exception e) {
            diagnostics.put("error", "Failed to collect openHAB diagnostics: " + e.getMessage());
        }

        return diagnostics;
    }

    private Map<String, Object> collectNetworkDiagnostics() {
        Map<String, Object> diagnostics = new HashMap<>();

        try {
            List<Map<String, Object>> interfaces = new ArrayList<>();

            for (NetworkInterface ni : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                Map<String, Object> interfaceInfo = new HashMap<>();
                interfaceInfo.put("name", ni.getName());
                interfaceInfo.put("displayName", ni.getDisplayName());
                interfaceInfo.put("isUp", ni.isUp());
                interfaceInfo.put("isLoopback", ni.isLoopback());
                interfaceInfo.put("isPointToPoint", ni.isPointToPoint());
                interfaceInfo.put("isVirtual", ni.isVirtual());
                interfaceInfo.put("supportsMulticast", ni.supportsMulticast());
                interfaceInfo.put("mtu", ni.getMTU());

                List<String> addresses = new ArrayList<>();
                ni.getInetAddresses().asIterator().forEachRemaining(addr -> addresses.add(addr.getHostAddress()));
                interfaceInfo.put("addresses", addresses);

                interfaces.add(interfaceInfo);
            }

            diagnostics.put("networkInterfaces", interfaces);

            // Local host information
            InetAddress localHost = InetAddress.getLocalHost();
            diagnostics.put("localHostName", localHost.getHostName());
            diagnostics.put("localHostAddress", localHost.getHostAddress());

        } catch (Exception e) {
            diagnostics.put("error", "Failed to collect network diagnostics: " + e.getMessage());
        }

        return diagnostics;
    }

    private Map<String, Object> collectEnvironmentVariables() {
        Map<String, Object> envVars = new HashMap<>();

        Map<String, String> env = System.getenv();
        for (Map.Entry<String, String> entry : env.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            // Filter out sensitive environment variables
            if (isSensitiveEnvironmentVariable(key)) {
                envVars.put(key, "***REDACTED***");
            } else {
                envVars.put(key, value);
            }
        }

        return envVars;
    }

    private boolean isSensitiveEnvironmentVariable(String key) {
        String lowerKey = key.toLowerCase();
        return lowerKey.contains("password") || lowerKey.contains("secret") || lowerKey.contains("key")
                || lowerKey.contains("token") || lowerKey.contains("credential") || lowerKey.contains("auth");
    }

    private Map<String, Object> collectSystemProperties() {
        Map<String, Object> properties = new HashMap<>();

        Properties sysProps = System.getProperties();
        for (String key : sysProps.stringPropertyNames()) {
            properties.put(key, sysProps.getProperty(key));
        }

        return properties;
    }

    private Map<String, Object> collectRuntimeDiagnostics() {
        Map<String, Object> diagnostics = new HashMap<>();

        Runtime runtime = Runtime.getRuntime();
        diagnostics.put("availableProcessors", runtime.availableProcessors());
        diagnostics.put("freeMemory", runtime.freeMemory());
        diagnostics.put("totalMemory", runtime.totalMemory());
        diagnostics.put("maxMemory", runtime.maxMemory());

        // Class loading statistics
        ClassLoadingMXBean classLoadingBean = ManagementFactory.getClassLoadingMXBean();
        diagnostics.put("loadedClassCount", classLoadingBean.getLoadedClassCount());
        diagnostics.put("totalLoadedClassCount", classLoadingBean.getTotalLoadedClassCount());
        diagnostics.put("unloadedClassCount", classLoadingBean.getUnloadedClassCount());

        // Compilation statistics
        CompilationMXBean compilationBean = ManagementFactory.getCompilationMXBean();
        if (compilationBean.isCompilationTimeMonitoringSupported()) {
            diagnostics.put("compilationTimeMonitoringSupported", true);
            diagnostics.put("totalCompilationTime", compilationBean.getTotalCompilationTime());
        } else {
            diagnostics.put("compilationTimeMonitoringSupported", false);
        }

        return diagnostics;
    }

    private Map<String, Object> formatMemoryUsage(java.lang.management.MemoryUsage usage) {
        Map<String, Object> formatted = new HashMap<>();
        formatted.put("init", usage.getInit());
        formatted.put("used", usage.getUsed());
        formatted.put("committed", usage.getCommitted());
        formatted.put("max", usage.getMax());
        formatted.put("usagePercent", usage.getMax() > 0 ? (double) usage.getUsed() / usage.getMax() * 100 : 0);
        return formatted;
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
}
