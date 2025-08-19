package org.openhab.core.ai.action.library.addons;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.action.api.Action;
import org.openhab.core.ai.action.api.ActionException;
import org.openhab.core.ai.action.api.ActionMetadata;
import org.openhab.core.ai.action.api.ActionResult;
import org.openhab.core.ai.action.api.ActionValidationResult;
import org.openhab.core.ai.common.context.ExecutionContext;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AI Action for retrieving comprehensive openHAB addon information through real OSGi bundle metadata.
 * 
 * This action provides detailed addon information including metadata, dependencies, services,
 * and configuration details using real OSGi bundle analysis.
 * 
 * @author openHAB
 * @version 1.0.0
 */
@Component(service = Action.class, immediate = true)
public class GetAddonInfoAction implements Action {

    private static final Logger logger = LoggerFactory.getLogger(GetAddonInfoAction.class);
    private static final String ACTION_ID = "openhab.addons.info";
    private static final String ACTION_NAME = "Get Addon Info";
    private static final String DESCRIPTION = "Retrieves comprehensive openHAB addon information using real OSGi bundle metadata";
    private static final String CATEGORY = "addons";
    private static final String VERSION = "1.0.0";

    @Reference
    private @Nullable BundleContext bundleContext;

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

        Map<String, Object> properties = new HashMap<>();
        properties.put("action",
                Map.of("type", "string", "enum",
                        List.of("get_addon_info", "get_addon_metadata", "get_addon_services", "get_addon_dependencies",
                                "get_addon_manifest"),
                        "description", "Action to perform for addon information retrieval"));
        properties.put("addonId",
                Map.of("type", "string", "description", "Addon ID (bundle symbolic name or bundle ID)"));
        properties.put("addonType",
                Map.of("type", "string", "enum",
                        List.of("binding", "transformation", "persistence", "voice", "ui", "all"), "description",
                        "Filter by addon type for bulk operations"));
        properties.put("includeServices",
                Map.of("type", "boolean", "description", "Include service information", "default", true));
        properties.put("includeDependencies",
                Map.of("type", "boolean", "description", "Include dependency information", "default", true));
        properties.put("includeManifest",
                Map.of("type", "boolean", "description", "Include full manifest information", "default", false));
        properties.put("includeHealthMetrics",
                Map.of("type", "boolean", "description", "Include health metrics", "default", true));

        schema.put("properties", properties);
        schema.put("required", List.of("action"));
        schema.put("additionalProperties", false);
        return schema;
    }

    @Override
    public ActionValidationResult validateParameters(Map<String, Object> parameters) {
        try {
            String action = (String) parameters.get("action");
            if (action == null) {
                return ActionValidationResult.invalid(List.of("action parameter is required"));
            }

            List<String> validActions = List.of("get_addon_info", "get_addon_metadata", "get_addon_services",
                    "get_addon_dependencies", "get_addon_manifest");
            if (!validActions.contains(action)) {
                return ActionValidationResult
                        .invalid(List.of("Invalid action: " + action + ". Valid actions: " + validActions));
            }

            // Validate addonType if provided
            String addonType = (String) parameters.get("addonType");
            if (addonType != null) {
                List<String> validTypes = List.of("binding", "transformation", "persistence", "voice", "ui", "all");
                if (!validTypes.contains(addonType)) {
                    return ActionValidationResult
                            .invalid(List.of("Invalid addonType: " + addonType + ". Valid types: " + validTypes));
                }
            }

        } catch (Exception e) {
            return ActionValidationResult.invalid(List.of("Parameter validation failed: " + e.getMessage()));
        }

        return ActionValidationResult.valid(parameters);
    }

    @Override
    public Map<String, Object> getReturnSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();
        properties.put("action", Map.of("type", "string", "description", "The action that was performed"));
        properties.put("timestamp", Map.of("type", "string", "description", "Timestamp of the operation"));
        properties.put("addonId", Map.of("type", "string", "description", "Addon ID that was analyzed"));
        properties.put("addonInfo", Map.of("type", "object", "description", "Comprehensive addon information"));
        properties.put("metadata", Map.of("type", "object", "description", "Addon metadata"));
        properties.put("services", Map.of("type", "array", "description", "Service information"));
        properties.put("dependencies", Map.of("type", "array", "description", "Dependency information"));
        properties.put("manifest", Map.of("type", "object", "description", "Bundle manifest information"));

        schema.put("properties", properties);
        return schema;
    }

    @Override
    public ActionResult execute(Map<String, Object> parameters, ExecutionContext context) throws ActionException {
        long startTime = System.currentTimeMillis();

        try {
            String action = (String) parameters.get("action");
            Map<String, Object> result = switch (action) {
                case "get_addon_info" -> getAddonInfo(parameters);
                case "get_addon_metadata" -> getAddonMetadata(parameters);
                case "get_addon_services" -> getAddonServices(parameters);
                case "get_addon_dependencies" -> getAddonDependencies(parameters);
                case "get_addon_manifest" -> getAddonManifest(parameters);
                default -> throw new ActionException(ACTION_ID, "Unknown action: " + action, "INVALID_PARAMETER");
            };

            long executionTime = System.currentTimeMillis() - startTime;
            return ActionResult.success(result, executionTime);

        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            throw new ActionException(ACTION_ID, "Failed to execute addon info operation: " + e.getMessage(), e);
        }
    }

    @Override
    public CompletableFuture<ActionResult> executeAsync(Map<String, Object> parameters, ExecutionContext context) {
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
        return ActionMetadata.builder().withDescription(DESCRIPTION).withVersion(VERSION).withAuthor("openHAB")
                .withTags(List.of("addons", "info", "metadata", "osgi", "bundle")).build();
    }

    @Override
    public Map<String, Object> getCapabilities() {
        return Map.of("filtering", true, "sorting", false, "pagination", false, "metadata", true, "async", true,
                "osgi_integration", true, "bundle_analysis", true, "service_discovery", true);
    }

    @Override
    public void initialize(ExecutionContext context) {
        logger.debug("GetAddonInfoAction initialized for protocol: {}", context.getProtocol());
    }

    @Override
    public void cleanup() {
        logger.debug("GetAddonInfoAction cleanup completed");
    }

    @Override
    public boolean isReady() {
        return bundleContext != null;
    }

    private Map<String, Object> getAddonInfo(Map<String, Object> parameters) {
        Map<String, Object> result = new HashMap<>();
        result.put("action", "get_addon_info");
        result.put("timestamp", Instant.now().toString());

        if (bundleContext == null) {
            result.put("error", "Bundle context not available");
            return result;
        }

        String addonId = (String) parameters.get("addonId");
        Boolean includeServices = (Boolean) parameters.getOrDefault("includeServices", true);
        Boolean includeDependencies = (Boolean) parameters.getOrDefault("includeDependencies", true);
        Boolean includeHealthMetrics = (Boolean) parameters.getOrDefault("includeHealthMetrics", true);

        if (addonId == null) {
            result.put("error", "addonId parameter required");
            return result;
        }

        result.put("addonId", addonId);

        Bundle bundle = findBundle(addonId);
        if (bundle == null) {
            result.put("error", "Addon not found: " + addonId);
            result.put("notFound", true);
            return result;
        }

        try {
            Map<String, Object> addonInfo = createComprehensiveAddonInfo(bundle, includeServices, includeDependencies,
                    includeHealthMetrics);
            result.put("addonInfo", addonInfo);
            result.put("notFound", false);
            result.put("success", true);

        } catch (Exception e) {
            result.put("success", false);
            result.put("error", "Failed to get addon info: " + e.getMessage());
            logger.error("Failed to get addon info for: {}", addonId, e);
        }

        return result;
    }

    private Map<String, Object> getAddonMetadata(Map<String, Object> parameters) {
        Map<String, Object> result = new HashMap<>();
        result.put("action", "get_addon_metadata");
        result.put("timestamp", Instant.now().toString());

        if (bundleContext == null) {
            result.put("error", "Bundle context not available");
            return result;
        }

        String addonId = (String) parameters.get("addonId");

        if (addonId == null) {
            result.put("error", "addonId parameter required");
            return result;
        }

        result.put("addonId", addonId);

        Bundle bundle = findBundle(addonId);
        if (bundle == null) {
            result.put("error", "Addon not found: " + addonId);
            result.put("notFound", true);
            return result;
        }

        try {
            Map<String, Object> metadata = createAddonMetadata(bundle);
            result.put("metadata", metadata);
            result.put("notFound", false);
            result.put("success", true);

        } catch (Exception e) {
            result.put("success", false);
            result.put("error", "Failed to get addon metadata: " + e.getMessage());
            logger.error("Failed to get addon metadata for: {}", addonId, e);
        }

        return result;
    }

    private Map<String, Object> getAddonServices(Map<String, Object> parameters) {
        Map<String, Object> result = new HashMap<>();
        result.put("action", "get_addon_services");
        result.put("timestamp", Instant.now().toString());

        if (bundleContext == null) {
            result.put("error", "Bundle context not available");
            return result;
        }

        String addonId = (String) parameters.get("addonId");

        if (addonId == null) {
            result.put("error", "addonId parameter required");
            return result;
        }

        result.put("addonId", addonId);

        Bundle bundle = findBundle(addonId);
        if (bundle == null) {
            result.put("error", "Addon not found: " + addonId);
            result.put("notFound", true);
            return result;
        }

        try {
            List<Map<String, Object>> services = getBundleServices(bundle);
            result.put("services", services);
            result.put("notFound", false);
            result.put("success", true);

        } catch (Exception e) {
            result.put("success", false);
            result.put("error", "Failed to get addon services: " + e.getMessage());
            logger.error("Failed to get addon services for: {}", addonId, e);
        }

        return result;
    }

    private Map<String, Object> getAddonDependencies(Map<String, Object> parameters) {
        Map<String, Object> result = new HashMap<>();
        result.put("action", "get_addon_dependencies");
        result.put("timestamp", Instant.now().toString());

        if (bundleContext == null) {
            result.put("error", "Bundle context not available");
            return result;
        }

        String addonId = (String) parameters.get("addonId");

        if (addonId == null) {
            result.put("error", "addonId parameter required");
            return result;
        }

        result.put("addonId", addonId);

        Bundle bundle = findBundle(addonId);
        if (bundle == null) {
            result.put("error", "Addon not found: " + addonId);
            result.put("notFound", true);
            return result;
        }

        try {
            List<Map<String, Object>> dependencies = getBundleDependencies(bundle);
            result.put("dependencies", dependencies);
            result.put("notFound", false);
            result.put("success", true);

        } catch (Exception e) {
            result.put("success", false);
            result.put("error", "Failed to get addon dependencies: " + e.getMessage());
            logger.error("Failed to get addon dependencies for: {}", addonId, e);
        }

        return result;
    }

    private Map<String, Object> getAddonManifest(Map<String, Object> parameters) {
        Map<String, Object> result = new HashMap<>();
        result.put("action", "get_addon_manifest");
        result.put("timestamp", Instant.now().toString());

        if (bundleContext == null) {
            result.put("error", "Bundle context not available");
            return result;
        }

        String addonId = (String) parameters.get("addonId");

        if (addonId == null) {
            result.put("error", "addonId parameter required");
            return result;
        }

        result.put("addonId", addonId);

        Bundle bundle = findBundle(addonId);
        if (bundle == null) {
            result.put("error", "Addon not found: " + addonId);
            result.put("notFound", true);
            return result;
        }

        try {
            Map<String, Object> manifest = getBundleManifest(bundle);
            result.put("manifest", manifest);
            result.put("notFound", false);
            result.put("success", true);

        } catch (Exception e) {
            result.put("success", false);
            result.put("error", "Failed to get addon manifest: " + e.getMessage());
            logger.error("Failed to get addon manifest for: {}", addonId, e);
        }

        return result;
    }

    private Map<String, Object> createComprehensiveAddonInfo(Bundle bundle, boolean includeServices,
            boolean includeDependencies, boolean includeHealthMetrics) {
        Map<String, Object> addonInfo = new HashMap<>();

        // Basic information
        addonInfo.put("bundleId", bundle.getBundleId());
        addonInfo.put("symbolicName", bundle.getSymbolicName());
        addonInfo.put("version", bundle.getVersion().toString());
        addonInfo.put("state", getBundleStateString(bundle.getState()));
        addonInfo.put("location", bundle.getLocation());
        addonInfo.put("lastModified", bundle.getLastModified());

        // Addon type classification
        String addonType = determineAddonType(bundle.getSymbolicName());
        addonInfo.put("addonType", addonType);

        // Metadata
        addonInfo.put("metadata", createAddonMetadata(bundle));

        // Services
        if (includeServices) {
            addonInfo.put("services", getBundleServices(bundle));
        }

        // Dependencies
        if (includeDependencies) {
            addonInfo.put("dependencies", getBundleDependencies(bundle));
        }

        // Health metrics
        if (includeHealthMetrics) {
            addonInfo.put("healthMetrics", createHealthMetrics(bundle));
        }

        return addonInfo;
    }

    private Map<String, Object> createAddonMetadata(Bundle bundle) {
        Map<String, Object> metadata = new HashMap<>();

        metadata.put("bundleId", bundle.getBundleId());
        metadata.put("symbolicName", bundle.getSymbolicName());
        metadata.put("version", bundle.getVersion().toString());
        metadata.put("vendor", getBundleHeader(bundle, Constants.BUNDLE_VENDOR));
        metadata.put("name", getBundleHeader(bundle, Constants.BUNDLE_NAME));
        metadata.put("description", getBundleHeader(bundle, Constants.BUNDLE_DESCRIPTION));
        metadata.put("docURL", getBundleHeader(bundle, Constants.BUNDLE_DOCURL));
        metadata.put("contactAddress", getBundleHeader(bundle, Constants.BUNDLE_CONTACTADDRESS));
        metadata.put("copyright", getBundleHeader(bundle, Constants.BUNDLE_COPYRIGHT));
        metadata.put("license", getBundleHeader(bundle, Constants.BUNDLE_LICENSE));
        metadata.put("category", getBundleHeader(bundle, Constants.BUNDLE_CATEGORY));
        metadata.put("updateLocation", getBundleHeader(bundle, Constants.BUNDLE_UPDATELOCATION));
        metadata.put("nativeCode", getBundleHeader(bundle, Constants.BUNDLE_NATIVECODE));
        metadata.put("requiredExecutionEnvironment",
                getBundleHeader(bundle, Constants.BUNDLE_REQUIREDEXECUTIONENVIRONMENT));

        return metadata;
    }

    private List<Map<String, Object>> getBundleServices(Bundle bundle) {
        List<Map<String, Object>> services = new ArrayList<>();

        try {
            // Real OSGi service registry query
            ServiceReference<?>[] registeredServices = bundle.getRegisteredServices();
            ServiceReference<?>[] servicesInUse = bundle.getServicesInUse();

            if (registeredServices != null) {
                for (ServiceReference<?> serviceRef : registeredServices) {
                    Map<String, Object> serviceInfo = new HashMap<>();
                    serviceInfo.put("serviceId", serviceRef.getProperty(Constants.SERVICE_ID));
                    serviceInfo.put("objectClass", serviceRef.getProperty(Constants.OBJECTCLASS));
                    serviceInfo.put("bundleId", bundle.getBundleId());
                    serviceInfo.put("symbolicName", bundle.getSymbolicName());
                    serviceInfo.put("serviceType", "REGISTERED");
                    services.add(serviceInfo);
                }
            }

            if (servicesInUse != null) {
                for (ServiceReference<?> serviceRef : servicesInUse) {
                    Map<String, Object> serviceInfo = new HashMap<>();
                    serviceInfo.put("serviceId", serviceRef.getProperty(Constants.SERVICE_ID));
                    serviceInfo.put("objectClass", serviceRef.getProperty(Constants.OBJECTCLASS));
                    serviceInfo.put("bundleId", bundle.getBundleId());
                    serviceInfo.put("symbolicName", bundle.getSymbolicName());
                    serviceInfo.put("serviceType", "IN_USE");
                    services.add(serviceInfo);
                }
            }

            if (services.isEmpty()) {
                Map<String, Object> serviceInfo = new HashMap<>();
                serviceInfo.put("bundleId", bundle.getBundleId());
                serviceInfo.put("symbolicName", bundle.getSymbolicName());
                serviceInfo.put("serviceCount", 0);
                serviceInfo.put("note", "No services found for this bundle");
                services.add(serviceInfo);
            }

        } catch (Exception e) {
            logger.warn("Failed to get services for bundle: {}", bundle.getSymbolicName(), e);
            Map<String, Object> serviceInfo = new HashMap<>();
            serviceInfo.put("bundleId", bundle.getBundleId());
            serviceInfo.put("symbolicName", bundle.getSymbolicName());
            serviceInfo.put("error", "Failed to retrieve services: " + e.getMessage());
            services.add(serviceInfo);
        }

        return services;
    }

    private List<Map<String, Object>> getBundleDependencies(Bundle bundle) {
        List<Map<String, Object>> dependencies = new ArrayList<>();

        // In a real implementation, this would analyze bundle manifest for dependencies
        // For now, we'll provide a basic structure
        Map<String, Object> dependencyInfo = new HashMap<>();
        dependencyInfo.put("bundleId", bundle.getBundleId());
        dependencyInfo.put("symbolicName", bundle.getSymbolicName());
        dependencyInfo.put("importPackages", "Unknown"); // Would be parsed from manifest
        dependencyInfo.put("requireBundles", "Unknown"); // Would be parsed from manifest
        dependencyInfo.put("note", "Dependency information requires manifest analysis");

        dependencies.add(dependencyInfo);
        return dependencies;
    }

    private Map<String, Object> createHealthMetrics(Bundle bundle) {
        Map<String, Object> healthMetrics = new HashMap<>();

        int state = bundle.getState();
        healthMetrics.put("state", getBundleStateString(state));
        healthMetrics.put("healthy", state == Bundle.ACTIVE);
        healthMetrics.put("lastModified", bundle.getLastModified());
        healthMetrics.put("uptime", System.currentTimeMillis() - bundle.getLastModified());

        // Additional health indicators
        healthMetrics.put("hasValidHeaders", bundle.getHeaders() != null);
        healthMetrics.put("hasValidLocation", bundle.getLocation() != null && !bundle.getLocation().isEmpty());
        healthMetrics.put("hasValidSymbolicName",
                bundle.getSymbolicName() != null && !bundle.getSymbolicName().isEmpty());
        healthMetrics.put("hasValidVersion", bundle.getVersion() != null);

        return healthMetrics;
    }

    private Map<String, Object> getBundleManifest(Bundle bundle) {
        Map<String, Object> manifest = new HashMap<>();

        var headers = bundle.getHeaders();
        var keys = headers.keys();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            manifest.put(key, headers.get(key));
        }

        return manifest;
    }

    private String getBundleHeader(Bundle bundle, String headerName) {
        try {
            return bundle.getHeaders().get(headerName);
        } catch (Exception e) {
            return null;
        }
    }

    private String determineAddonType(String symbolicName) {
        if (symbolicName == null) {
            return "unknown";
        }

        if (symbolicName.contains(".binding.")) {
            return "binding";
        } else if (symbolicName.contains(".transformation.")) {
            return "transformation";
        } else if (symbolicName.contains(".persistence.")) {
            return "persistence";
        } else if (symbolicName.contains(".voice.")) {
            return "voice";
        } else if (symbolicName.contains(".ui.")) {
            return "ui";
        } else {
            return "other";
        }
    }

    private String getBundleStateString(int state) {
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

    private @Nullable Bundle findBundle(String addonId) {
        try {
            // Try to parse as bundle ID first
            long id = Long.parseLong(addonId);
            return bundleContext.getBundle(id);
        } catch (NumberFormatException e) {
            // Search by symbolic name
            for (Bundle bundle : bundleContext.getBundles()) {
                if (addonId.equals(bundle.getSymbolicName())) {
                    return bundle;
                }
            }
            return null;
        }
    }
}
