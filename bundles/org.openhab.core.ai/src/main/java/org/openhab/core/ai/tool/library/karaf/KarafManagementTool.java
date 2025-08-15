package org.openhab.core.ai.tool.library.karaf;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.RuntimeMXBean;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.tool.api.Tool;
import org.openhab.core.ai.tool.api.ToolContext;
import org.openhab.core.ai.tool.api.ToolException;
import org.openhab.core.ai.tool.api.ToolMetadata;
import org.openhab.core.ai.tool.api.ToolResult;
import org.openhab.core.ai.tool.validation.api.ToolValidationResult;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.startlevel.BundleStartLevel;
import org.osgi.framework.startlevel.FrameworkStartLevel;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.packageadmin.PackageAdmin;

/**
 * Karaf management tool for MCP.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class KarafManagementTool implements Tool {

    private static final String TOOL_ID = "openhab.karaf.management";
    private static final String TOOL_NAME = "Karaf Runtime Management";

    private @Nullable BundleContext bundleContext;

    @Activate
    protected void activate(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    @Override
    public String getId() {
        return TOOL_ID;
    }

    @Override
    public String getName() {
        return TOOL_NAME;
    }

    @Override
    public String getDescription() {
        return "Manages the Karaf runtime container including bundle operations, system information, memory management, and start levels";
    }

    @Override
    public Map<String, Object> getInputSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();
        properties.put("operation", Map.of("type", "string", "enum",
                List.of("list_bundles", "bundle_info", "start_bundle", "stop_bundle", "restart_bundle",
                        "refresh_bundle", "system_info", "memory_info", "gc_collect", "framework_info", "start_levels",
                        "set_start_level", "bundle_headers", "bundle_services", "bundle_dependencies"),
                "description", "Karaf runtime operation to perform"));
        properties.put("bundleId", Map.of("type", "string", "description", "Bundle ID or symbolic name"));
        properties.put("startLevel", Map.of("type", "integer", "description", "Start level to set", "minimum", 1));
        properties.put("includeSystemBundles",
                Map.of("type", "boolean", "description", "Include system bundles in results", "default", false));
        properties.put("state",
                Map.of("type", "string", "enum",
                        List.of("ACTIVE", "RESOLVED", "INSTALLED", "STARTING", "STOPPING", "UNINSTALLED"),
                        "description", "Filter bundles by state"));
        properties.put("dangerousOperations",
                Map.of("type", "boolean", "description", "Allow potentially dangerous operations", "default", false));

        schema.put("properties", properties);
        schema.put("required", List.of("operation"));
        schema.put("additionalProperties", false);
        return schema;
    }

    @Override
    public Map<String, Object> getOutputSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");
        schema.put("properties",
                Map.of("success", Map.of("type", "boolean", "description", "Operation success status"), "data",
                        Map.of("type", "object", "description", "Operation result data"), "message",
                        Map.of("type", "string", "description", "Operation message")));
        return schema;
    }

    @Override
    public ToolValidationResult validateParameters(Map<String, Object> parameters) {
        String operation = (String) parameters.get("operation");
        if (operation == null) {
            return ToolValidationResult.invalid("Missing required parameter: operation");
        }

        if (List.of("bundle_info", "start_bundle", "stop_bundle", "restart_bundle", "refresh_bundle", "set_start_level",
                "bundle_headers", "bundle_services", "bundle_dependencies").contains(operation)) {
            String bundleId = (String) parameters.get("bundleId");
            if (bundleId == null || bundleId.trim().isEmpty()) {
                return ToolValidationResult.invalid("bundleId is required for operation: " + operation);
            }
        }

        if ("set_start_level".equals(operation)) {
            Integer startLevel = (Integer) parameters.get("startLevel");
            if (startLevel == null || startLevel < 1) {
                return ToolValidationResult.invalid("Valid startLevel (>= 1) is required for set_start_level");
            }
        }

        return ToolValidationResult.valid();
    }

    @Override
    public ToolResult execute(Map<String, Object> parameters, ToolContext context) throws ToolException {
        long startTime = System.currentTimeMillis();

        if (bundleContext == null) {
            throw new ToolException(TOOL_ID, "Bundle context not available",
                    org.openhab.core.ai.tool.api.ToolErrorCode.SERVICE_UNAVAILABLE);
        }

        try {
            String operation = (String) parameters.get("operation");
            Map<String, Object> result = switch (operation) {
                case "list_bundles" -> listBundles(parameters);
                case "bundle_info" -> getBundleInfo(parameters);
                case "start_bundle" -> startBundle(parameters);
                case "stop_bundle" -> stopBundle(parameters);
                case "restart_bundle" -> restartBundle(parameters);
                case "refresh_bundle" -> refreshBundle(parameters);
                case "system_info" -> getSystemInfo(parameters);
                case "memory_info" -> getMemoryInfo(parameters);
                case "gc_collect" -> performGarbageCollection(parameters);
                case "framework_info" -> getFrameworkInfo(parameters);
                case "start_levels" -> getStartLevels(parameters);
                case "set_start_level" -> setStartLevel(parameters);
                case "bundle_headers" -> getBundleHeaders(parameters);
                case "bundle_services" -> getBundleServices(parameters);
                case "bundle_dependencies" -> getBundleDependencies(parameters);
                default -> throw new ToolException(TOOL_ID, "Unknown operation: " + operation,
                        org.openhab.core.ai.tool.api.ToolErrorCode.INVALID_PARAMETER);
            };

            long executionTime = System.currentTimeMillis() - startTime;
            return ToolResult.successJson(TOOL_ID, result, executionTime);

        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            throw new ToolException(TOOL_ID, "Karaf management operation failed: " + e.getMessage(), e,
                    org.openhab.core.ai.tool.api.ToolErrorCode.EXECUTION_ERROR);
        }
    }

    @Override
    public ToolMetadata getMetadata() {
        return ToolMetadata.builder().version("1.0.0").author("openHAB")
                .description("Provides Karaf runtime container management capabilities").build();
    }

    private Map<String, Object> listBundles(Map<String, Object> parameters) {
        Map<String, Object> result = new HashMap<>();
        result.put("operation", "list_bundles");
        result.put("timestamp", Instant.now().toString());

        Boolean includeSystemBundles = (Boolean) parameters.getOrDefault("includeSystemBundles", false);
        String stateFilter = (String) parameters.get("state");

        Bundle[] bundles = bundleContext.getBundles();
        List<Map<String, Object>> bundleList = new ArrayList<>();

        for (Bundle bundle : bundles) {
            // Skip system bundle unless requested
            if (!includeSystemBundles && bundle.getBundleId() == 0) {
                continue;
            }

            // Filter by state if specified
            if (stateFilter != null && bundle.getState() != parseState(stateFilter)) {
                continue;
            }

            Map<String, Object> bundleInfo = createBundleInfo(bundle, false);
            bundleList.add(bundleInfo);
        }

        result.put("bundles", bundleList);
        result.put("totalBundles", bundleList.size());
        result.put("includeSystemBundles", includeSystemBundles);
        result.put("stateFilter", stateFilter);

        return result;
    }

    private Map<String, Object> getBundleInfo(Map<String, Object> parameters) {
        Map<String, Object> result = new HashMap<>();
        result.put("operation", "bundle_info");
        result.put("timestamp", Instant.now().toString());

        String bundleId = (String) parameters.get("bundleId");
        Bundle bundle = findBundle(bundleId);

        if (bundle == null) {
            result.put("error", "Bundle not found: " + bundleId);
            return result;
        }

        Map<String, Object> bundleInfo = createBundleInfo(bundle, true);
        result.put("bundleInfo", bundleInfo);

        return result;
    }

    private Map<String, Object> startBundle(Map<String, Object> parameters) {
        Map<String, Object> result = new HashMap<>();
        result.put("operation", "start_bundle");
        result.put("timestamp", Instant.now().toString());

        String bundleId = (String) parameters.get("bundleId");
        Bundle bundle = findBundle(bundleId);

        if (bundle == null) {
            result.put("error", "Bundle not found: " + bundleId);
            return result;
        }

        if (bundle.getBundleId() == 0) {
            result.put("error", "Cannot start system bundle");
            return result;
        }

        try {
            int previousState = bundle.getState();
            bundle.start();
            result.put("success", true);
            result.put("previousState", stateToString(previousState));
            result.put("currentState", stateToString(bundle.getState()));
            result.put("message", "Bundle started successfully");
        } catch (BundleException e) {
            result.put("error", "Failed to start bundle: " + e.getMessage());
        }

        return result;
    }

    private Map<String, Object> stopBundle(Map<String, Object> parameters) {
        Map<String, Object> result = new HashMap<>();
        result.put("operation", "stop_bundle");
        result.put("timestamp", Instant.now().toString());

        String bundleId = (String) parameters.get("bundleId");
        Bundle bundle = findBundle(bundleId);

        if (bundle == null) {
            result.put("error", "Bundle not found: " + bundleId);
            return result;
        }

        if (bundle.getBundleId() == 0) {
            result.put("error", "Cannot stop system bundle");
            return result;
        }

        try {
            int previousState = bundle.getState();
            bundle.stop();
            result.put("success", true);
            result.put("previousState", stateToString(previousState));
            result.put("currentState", stateToString(bundle.getState()));
            result.put("message", "Bundle stopped successfully");
        } catch (BundleException e) {
            result.put("error", "Failed to stop bundle: " + e.getMessage());
        }

        return result;
    }

    private Map<String, Object> restartBundle(Map<String, Object> parameters) {
        Map<String, Object> result = new HashMap<>();
        result.put("operation", "restart_bundle");
        result.put("timestamp", Instant.now().toString());

        String bundleId = (String) parameters.get("bundleId");
        Bundle bundle = findBundle(bundleId);

        if (bundle == null) {
            result.put("error", "Bundle not found: " + bundleId);
            return result;
        }

        if (bundle.getBundleId() == 0) {
            result.put("error", "Cannot restart system bundle");
            return result;
        }

        try {
            int previousState = bundle.getState();
            bundle.stop();
            Thread.sleep(100); // Brief pause
            bundle.start();
            result.put("success", true);
            result.put("previousState", stateToString(previousState));
            result.put("currentState", stateToString(bundle.getState()));
            result.put("message", "Bundle restarted successfully");
        } catch (BundleException | InterruptedException e) {
            result.put("error", "Failed to restart bundle: " + e.getMessage());
        }

        return result;
    }

    private Map<String, Object> refreshBundle(Map<String, Object> parameters) {
        Map<String, Object> result = new HashMap<>();
        result.put("operation", "refresh_bundle");
        result.put("timestamp", Instant.now().toString());

        String bundleId = (String) parameters.get("bundleId");
        Bundle bundle = findBundle(bundleId);

        if (bundle == null) {
            result.put("error", "Bundle not found: " + bundleId);
            return result;
        }

        try {
            ServiceReference<PackageAdmin> ref = bundleContext.getServiceReference(PackageAdmin.class);
            if (ref != null) {
                PackageAdmin packageAdmin = bundleContext.getService(ref);
                if (packageAdmin != null) {
                    packageAdmin.refreshPackages(new Bundle[] { bundle });
                    result.put("success", true);
                    result.put("message", "Bundle refreshed successfully");
                } else {
                    result.put("error", "PackageAdmin service not available");
                }
            } else {
                result.put("error", "PackageAdmin service not found");
            }
        } catch (Exception e) {
            result.put("error", "Failed to refresh bundle: " + e.getMessage());
        }

        return result;
    }

    private Map<String, Object> getSystemInfo(Map<String, Object> parameters) {
        Map<String, Object> result = new HashMap<>();
        result.put("operation", "system_info");
        result.put("timestamp", Instant.now().toString());

        RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();

        Map<String, Object> systemInfo = new HashMap<>();
        String javaVersion = System.getProperty("java.version");
        if (javaVersion == null) {
            javaVersion = "unknown";
        }
        String javaVendor = System.getProperty("java.vendor");
        if (javaVendor == null) {
            javaVendor = "unknown";
        }
        String osName = System.getProperty("os.name");
        if (osName == null) {
            osName = "unknown";
        }
        String osVersion = System.getProperty("os.version");
        if (osVersion == null) {
            osVersion = "unknown";
        }
        String osArch = System.getProperty("os.arch");
        if (osArch == null) {
            osArch = "unknown";
        }
        systemInfo.put("javaVersion", javaVersion);
        systemInfo.put("javaVendor", javaVendor);
        systemInfo.put("osName", osName);
        systemInfo.put("osVersion", osVersion);
        systemInfo.put("osArch", osArch);
        String vmName = runtimeBean.getVmName();
        if (vmName == null) {
            vmName = "unknown";
        }
        String vmVersion = runtimeBean.getVmVersion();
        if (vmVersion == null) {
            vmVersion = "unknown";
        }
        String vmVendor = runtimeBean.getVmVendor();
        if (vmVendor == null) {
            vmVendor = "unknown";
        }
        systemInfo.put("vmName", vmName);
        systemInfo.put("vmVersion", vmVersion);
        systemInfo.put("vmVendor", vmVendor);
        systemInfo.put("uptime", runtimeBean.getUptime());
        systemInfo.put("startTime", runtimeBean.getStartTime());
        systemInfo.put("totalBundles", bundleContext.getBundles().length);

        result.put("systemInfo", systemInfo);
        return result;
    }

    private Map<String, Object> getMemoryInfo(Map<String, Object> parameters) {
        Map<String, Object> result = new HashMap<>();
        result.put("operation", "memory_info");
        result.put("timestamp", Instant.now().toString());

        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        Runtime runtime = Runtime.getRuntime();

        Map<String, Object> memoryInfo = new HashMap<>();
        memoryInfo.put("heapUsed", memoryBean.getHeapMemoryUsage().getUsed());
        memoryInfo.put("heapCommitted", memoryBean.getHeapMemoryUsage().getCommitted());
        memoryInfo.put("heapMax", memoryBean.getHeapMemoryUsage().getMax());
        memoryInfo.put("nonHeapUsed", memoryBean.getNonHeapMemoryUsage().getUsed());
        memoryInfo.put("nonHeapCommitted", memoryBean.getNonHeapMemoryUsage().getCommitted());
        memoryInfo.put("totalMemory", runtime.totalMemory());
        memoryInfo.put("freeMemory", runtime.freeMemory());
        memoryInfo.put("maxMemory", runtime.maxMemory());

        result.put("memoryInfo", memoryInfo);
        return result;
    }

    private Map<String, Object> performGarbageCollection(Map<String, Object> parameters) {
        Map<String, Object> result = new HashMap<>();
        result.put("operation", "gc_collect");
        result.put("timestamp", Instant.now().toString());

        long beforeMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        System.gc();
        long afterMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        result.put("memoryBeforeGC", beforeMemory);
        result.put("memoryAfterGC", afterMemory);
        result.put("memoryFreed", beforeMemory - afterMemory);
        result.put("message", "Garbage collection performed");

        return result;
    }

    private Map<String, Object> getFrameworkInfo(Map<String, Object> parameters) {
        Map<String, Object> result = new HashMap<>();
        result.put("operation", "framework_info");
        result.put("timestamp", Instant.now().toString());

        Bundle systemBundle = bundleContext.getBundle(0);
        Map<String, Object> frameworkInfo = new HashMap<>();

        frameworkInfo.put("symbolicName", systemBundle.getSymbolicName());
        frameworkInfo.put("version", systemBundle.getVersion().toString());
        frameworkInfo.put("state", stateToString(systemBundle.getState()));

        // Get framework start level
        ServiceReference<FrameworkStartLevel> ref = bundleContext.getServiceReference(FrameworkStartLevel.class);
        if (ref != null) {
            FrameworkStartLevel frameworkStartLevel = bundleContext.getService(ref);
            if (frameworkStartLevel != null) {
                frameworkInfo.put("startLevel", frameworkStartLevel.getStartLevel());
                frameworkInfo.put("initialBundleStartLevel", frameworkStartLevel.getInitialBundleStartLevel());
            }
        }

        result.put("frameworkInfo", frameworkInfo);
        return result;
    }

    private Map<String, Object> getStartLevels(Map<String, Object> parameters) {
        Map<String, Object> result = new HashMap<>();
        result.put("operation", "start_levels");
        result.put("timestamp", Instant.now().toString());

        Boolean includeSystemBundles = (Boolean) parameters.getOrDefault("includeSystemBundles", false);
        Bundle[] bundles = bundleContext.getBundles();
        List<Map<String, Object>> startLevels = new ArrayList<>();

        for (Bundle bundle : bundles) {
            if (!includeSystemBundles && bundle.getBundleId() == 0) {
                continue;
            }

            BundleStartLevel bundleStartLevel = bundle.adapt(BundleStartLevel.class);
            if (bundleStartLevel != null) {
                Map<String, Object> info = new HashMap<>();
                info.put("bundleId", bundle.getBundleId());
                info.put("symbolicName", bundle.getSymbolicName());
                info.put("startLevel", bundleStartLevel.getStartLevel());
                info.put("activationPolicyUsed", bundleStartLevel.isActivationPolicyUsed());
                info.put("persistentlyStarted", bundleStartLevel.isPersistentlyStarted());
                startLevels.add(info);
            }
        }

        result.put("bundleStartLevels", startLevels);
        result.put("totalBundles", startLevels.size());

        return result;
    }

    private Map<String, Object> setStartLevel(Map<String, Object> parameters) {
        Map<String, Object> result = new HashMap<>();
        result.put("operation", "set_start_level");
        result.put("timestamp", Instant.now().toString());

        String bundleId = (String) parameters.get("bundleId");
        Integer startLevel = (Integer) parameters.get("startLevel");
        Bundle bundle = findBundle(bundleId);

        if (bundle == null) {
            result.put("error", "Bundle not found: " + bundleId);
            return result;
        }

        BundleStartLevel bundleStartLevel = bundle.adapt(BundleStartLevel.class);
        if (bundleStartLevel != null) {
            int previousStartLevel = bundleStartLevel.getStartLevel();
            bundleStartLevel.setStartLevel(startLevel);
            result.put("success", true);
            result.put("previousStartLevel", previousStartLevel);
            result.put("newStartLevel", startLevel);
            result.put("message", "Start level set successfully");
        } else {
            result.put("error", "Could not adapt bundle to BundleStartLevel");
        }

        return result;
    }

    private Map<String, Object> getBundleHeaders(Map<String, Object> parameters) {
        Map<String, Object> result = new HashMap<>();
        result.put("operation", "bundle_headers");
        result.put("timestamp", Instant.now().toString());

        String bundleId = (String) parameters.get("bundleId");
        Bundle bundle = findBundle(bundleId);

        if (bundle == null) {
            result.put("error", "Bundle not found: " + bundleId);
            return result;
        }

        Map<String, Object> headers = new HashMap<>();
        java.util.Enumeration<String> keys = bundle.getHeaders().keys();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            headers.put(key, bundle.getHeaders().get(key));
        }

        result.put("bundleId", bundle.getBundleId());
        result.put("symbolicName", bundle.getSymbolicName());
        result.put("headers", headers);

        return result;
    }

    private Map<String, Object> getBundleServices(Map<String, Object> parameters) {
        Map<String, Object> result = new HashMap<>();
        result.put("operation", "bundle_services");
        result.put("timestamp", Instant.now().toString());

        String bundleId = (String) parameters.get("bundleId");
        Bundle bundle = findBundle(bundleId);

        if (bundle == null) {
            result.put("error", "Bundle not found: " + bundleId);
            return result;
        }

        List<Map<String, Object>> registeredServices = new ArrayList<>();
        List<Map<String, Object>> servicesInUse = new ArrayList<>();

        ServiceReference<?>[] registered = bundle.getRegisteredServices();
        if (registered != null) {
            for (ServiceReference<?> ref : registered) {
                Map<String, Object> serviceInfo = createServiceInfo(ref);
                registeredServices.add(serviceInfo);
            }
        }

        ServiceReference<?>[] inUse = bundle.getServicesInUse();
        if (inUse != null) {
            for (ServiceReference<?> ref : inUse) {
                Map<String, Object> serviceInfo = createServiceInfo(ref);
                servicesInUse.add(serviceInfo);
            }
        }

        result.put("bundleId", bundle.getBundleId());
        result.put("symbolicName", bundle.getSymbolicName());
        result.put("registeredServices", registeredServices);
        result.put("servicesInUse", servicesInUse);

        return result;
    }

    private Map<String, Object> getBundleDependencies(Map<String, Object> parameters) {
        Map<String, Object> result = new HashMap<>();
        result.put("operation", "bundle_dependencies");
        result.put("timestamp", Instant.now().toString());

        String bundleId = (String) parameters.get("bundleId");
        Bundle bundle = findBundle(bundleId);

        if (bundle == null) {
            result.put("error", "Bundle not found: " + bundleId);
            return result;
        }

        Map<String, Object> dependencies = new HashMap<>();

        // Import/Export packages would need PackageAdmin service
        String requireBundle = bundle.getHeaders().get(Constants.REQUIRE_BUNDLE);
        String importPackage = bundle.getHeaders().get(Constants.IMPORT_PACKAGE);
        String exportPackage = bundle.getHeaders().get(Constants.EXPORT_PACKAGE);

        dependencies.put("requireBundle", requireBundle != null ? parseHeaderValue(requireBundle) : List.of());
        dependencies.put("importPackage", importPackage != null ? parseHeaderValue(importPackage) : List.of());
        dependencies.put("exportPackage", exportPackage != null ? parseHeaderValue(exportPackage) : List.of());

        result.put("bundleId", bundle.getBundleId());
        result.put("symbolicName", bundle.getSymbolicName());
        result.put("dependencies", dependencies);

        return result;
    }

    private Map<String, Object> createBundleInfo(Bundle bundle, boolean detailed) {
        Map<String, Object> info = new HashMap<>();
        info.put("bundleId", bundle.getBundleId());
        info.put("symbolicName", bundle.getSymbolicName());
        info.put("version", bundle.getVersion().toString());
        info.put("state", stateToString(bundle.getState()));
        info.put("location", bundle.getLocation());

        if (detailed) {
            info.put("lastModified", bundle.getLastModified());

            BundleStartLevel bundleStartLevel = bundle.adapt(BundleStartLevel.class);
            if (bundleStartLevel != null) {
                info.put("startLevel", bundleStartLevel.getStartLevel());
                info.put("persistentlyStarted", bundleStartLevel.isPersistentlyStarted());
            }

            // Count services
            ServiceReference<?>[] registered = bundle.getRegisteredServices();
            ServiceReference<?>[] inUse = bundle.getServicesInUse();
            info.put("registeredServicesCount", registered != null ? registered.length : 0);
            info.put("servicesInUseCount", inUse != null ? inUse.length : 0);
        }

        return info;
    }

    private Map<String, Object> createServiceInfo(ServiceReference<?> ref) {
        Map<String, Object> info = new HashMap<>();

        String[] objectClass = (String[]) ref.getProperty(Constants.OBJECTCLASS);
        info.put("objectClass", Arrays.asList(objectClass));
        info.put("serviceId", ref.getProperty(Constants.SERVICE_ID));

        Bundle bundle = ref.getBundle();
        if (bundle != null) {
            info.put("bundleId", bundle.getBundleId());
            info.put("bundleSymbolicName", bundle.getSymbolicName());
        }

        return info;
    }

    private @Nullable Bundle findBundle(String bundleId) {
        try {
            // Try to parse as bundle ID first
            long id = Long.parseLong(bundleId);
            return bundleContext.getBundle(id);
        } catch (NumberFormatException e) {
            // Search by symbolic name
            for (Bundle bundle : bundleContext.getBundles()) {
                if (bundleId.equals(bundle.getSymbolicName())) {
                    return bundle;
                }
            }
            return null;
        }
    }

    private String stateToString(int state) {
        return switch (state) {
            case Bundle.ACTIVE -> "ACTIVE";
            case Bundle.INSTALLED -> "INSTALLED";
            case Bundle.RESOLVED -> "RESOLVED";
            case Bundle.STARTING -> "STARTING";
            case Bundle.STOPPING -> "STOPPING";
            case Bundle.UNINSTALLED -> "UNINSTALLED";
            default -> "UNKNOWN";
        };
    }

    private int parseState(String state) {
        return switch (state.toUpperCase()) {
            case "ACTIVE" -> Bundle.ACTIVE;
            case "INSTALLED" -> Bundle.INSTALLED;
            case "RESOLVED" -> Bundle.RESOLVED;
            case "STARTING" -> Bundle.STARTING;
            case "STOPPING" -> Bundle.STOPPING;
            case "UNINSTALLED" -> Bundle.UNINSTALLED;
            default -> -1;
        };
    }

    private List<String> parseHeaderValue(String headerValue) {
        // Simple comma-separated parsing - could be enhanced for full OSGi header parsing
        return Arrays.stream(headerValue.split(",")).map(String::trim).filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}
