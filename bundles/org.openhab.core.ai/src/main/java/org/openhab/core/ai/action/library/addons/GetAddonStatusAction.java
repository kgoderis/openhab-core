package org.openhab.core.ai.action.library.addons;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.action.ActionContext;
import org.openhab.core.ai.action.ActionMetadata;
import org.openhab.core.ai.action.ActionResult;
import org.openhab.core.ai.action.ActionValidationResult;
import org.openhab.core.ai.action.api.Action;
import org.openhab.core.ai.action.api.ActionException;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AI Action for managing openHAB addon status through real OSGi bundle state management.
 * 
 * This action provides comprehensive addon status monitoring, health checks, and
 * state management using real OSGi bundle lifecycle operations.
 * 
 * @author openHAB
 * @version 1.0.0
 */
@Component(service = Action.class, immediate = true)
public class GetAddonStatusAction implements Action {

    private static final Logger logger = LoggerFactory.getLogger(GetAddonStatusAction.class);
    private static final String ACTION_ID = "openhab.addons.status";
    private static final String ACTION_NAME = "Get Addon Status";
    private static final String DESCRIPTION = "Manages openHAB addon status using real OSGi bundle state management";
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
                Map.of("type", "string", "enum", List.of("get_status", "start_addon", "stop_addon", "restart_addon",
                        "refresh_addon", "health_check"), "description",
                        "Action to perform for addon status management"));
        properties.put("addonId",
                Map.of("type", "string", "description", "Addon ID (bundle symbolic name or bundle ID)"));
        properties.put("addonType",
                Map.of("type", "string", "enum",
                        List.of("binding", "transformation", "persistence", "voice", "ui", "all"), "description",
                        "Filter by addon type for bulk operations"));
        properties.put("includeHealthMetrics",
                Map.of("type", "boolean", "description", "Include detailed health metrics", "default", true));
        properties.put("includeDependencies",
                Map.of("type", "boolean", "description", "Include dependency information", "default", false));
        properties.put("forceOperation", Map.of("type", "boolean", "description",
                "Force operation even if dependencies are not satisfied", "default", false));

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

            List<String> validActions = List.of("get_status", "start_addon", "stop_addon", "restart_addon",
                    "refresh_addon", "health_check");
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
        properties.put("addonId", Map.of("type", "string", "description", "Addon ID that was operated on"));
        properties.put("status", Map.of("type", "object", "description", "Addon status information"));
        properties.put("healthMetrics", Map.of("type", "object", "description", "Health metrics if requested"));
        properties.put("dependencies", Map.of("type", "array", "description", "Dependency information if requested"));
        properties.put("success", Map.of("type", "boolean", "description", "Whether the operation was successful"));

        schema.put("properties", properties);
        return schema;
    }

    @Override
    public ActionResult execute(Map<String, Object> parameters, ActionContext context) throws ActionException {
        long startTime = System.currentTimeMillis();

        try {
            String action = (String) parameters.get("action");
            Map<String, Object> result = switch (action) {
                case "get_status" -> getAddonStatus(parameters);
                case "start_addon" -> startAddon(parameters);
                case "stop_addon" -> stopAddon(parameters);
                case "restart_addon" -> restartAddon(parameters);
                case "refresh_addon" -> refreshAddon(parameters);
                case "health_check" -> checkAddonHealth(parameters);
                default -> throw new ActionException(ACTION_ID, "Unknown action: " + action, "INVALID_PARAMETER");
            };

            long executionTime = System.currentTimeMillis() - startTime;
            return ActionResult.success(result, executionTime);

        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            throw new ActionException(ACTION_ID, "Failed to execute addon status operation: " + e.getMessage(), e);
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
        return ActionMetadata.builder().description(DESCRIPTION).version(VERSION).author("openHAB")
                .tags(List.of("addons", "status", "osgi", "bundle", "health")).build();
    }

    @Override
    public Map<String, Object> getCapabilities() {
        return Map.of("filtering", true, "sorting", false, "pagination", false, "metadata", true, "async", true,
                "osgi_integration", true, "bundle_lifecycle", true, "health_monitoring", true);
    }

    @Override
    public void initialize(ActionContext context) {
        logger.debug("GetAddonStatusAction initialized for protocol: {}", context.getProtocol());
    }

    @Override
    public void cleanup() {
        logger.debug("GetAddonStatusAction cleanup completed");
    }

    @Override
    public boolean isReady() {
        return bundleContext != null;
    }

    private Map<String, Object> getAddonStatus(Map<String, Object> parameters) {
        Map<String, Object> result = new HashMap<>();
        result.put("action", "get_status");
        result.put("timestamp", Instant.now().toString());

        if (bundleContext == null) {
            result.put("error", "Bundle context not available");
            return result;
        }

        String addonId = (String) parameters.get("addonId");
        Boolean includeHealthMetrics = (Boolean) parameters.getOrDefault("includeHealthMetrics", true);
        Boolean includeDependencies = (Boolean) parameters.getOrDefault("includeDependencies", false);

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

        Map<String, Object> status = createStatusInfo(bundle, includeHealthMetrics, includeDependencies);
        result.put("status", status);
        result.put("notFound", false);
        result.put("success", true);

        return result;
    }

    private Map<String, Object> startAddon(Map<String, Object> parameters) {
        Map<String, Object> result = new HashMap<>();
        result.put("action", "start_addon");
        result.put("timestamp", Instant.now().toString());

        if (bundleContext == null) {
            result.put("error", "Bundle context not available");
            return result;
        }

        String addonId = (String) parameters.get("addonId");
        Boolean forceOperation = (Boolean) parameters.getOrDefault("forceOperation", false);

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
            int previousState = bundle.getState();
            bundle.start();
            int currentState = bundle.getState();

            result.put("success", true);
            result.put("previousState", getBundleStateString(previousState));
            result.put("currentState", getBundleStateString(currentState));
            result.put("message", "Addon started successfully");

        } catch (BundleException e) {
            result.put("success", false);
            result.put("error", "Failed to start addon: " + e.getMessage());
            logger.warn("Failed to start addon: {}", addonId, e);
        }

        return result;
    }

    private Map<String, Object> stopAddon(Map<String, Object> parameters) {
        Map<String, Object> result = new HashMap<>();
        result.put("action", "stop_addon");
        result.put("timestamp", Instant.now().toString());

        if (bundleContext == null) {
            result.put("error", "Bundle context not available");
            return result;
        }

        String addonId = (String) parameters.get("addonId");
        Boolean forceOperation = (Boolean) parameters.getOrDefault("forceOperation", false);

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

        // Prevent stopping system bundle
        if (bundle.getBundleId() == 0) {
            result.put("success", false);
            result.put("error", "Cannot stop system bundle");
            return result;
        }

        try {
            int previousState = bundle.getState();
            bundle.stop();
            int currentState = bundle.getState();

            result.put("success", true);
            result.put("previousState", getBundleStateString(previousState));
            result.put("currentState", getBundleStateString(currentState));
            result.put("message", "Addon stopped successfully");

        } catch (BundleException e) {
            result.put("success", false);
            result.put("error", "Failed to stop addon: " + e.getMessage());
            logger.warn("Failed to stop addon: {}", addonId, e);
        }

        return result;
    }

    private Map<String, Object> restartAddon(Map<String, Object> parameters) {
        Map<String, Object> result = new HashMap<>();
        result.put("action", "restart_addon");
        result.put("timestamp", Instant.now().toString());

        if (bundleContext == null) {
            result.put("error", "Bundle context not available");
            return result;
        }

        String addonId = (String) parameters.get("addonId");
        Boolean forceOperation = (Boolean) parameters.getOrDefault("forceOperation", false);

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

        // Prevent restarting system bundle
        if (bundle.getBundleId() == 0) {
            result.put("success", false);
            result.put("error", "Cannot restart system bundle");
            return result;
        }

        try {
            int previousState = bundle.getState();
            bundle.stop();
            bundle.start();
            int currentState = bundle.getState();

            result.put("success", true);
            result.put("previousState", getBundleStateString(previousState));
            result.put("currentState", getBundleStateString(currentState));
            result.put("message", "Addon restarted successfully");

        } catch (BundleException e) {
            result.put("success", false);
            result.put("error", "Failed to restart addon: " + e.getMessage());
            logger.warn("Failed to restart addon: {}", addonId, e);
        }

        return result;
    }

    private Map<String, Object> refreshAddon(Map<String, Object> parameters) {
        Map<String, Object> result = new HashMap<>();
        result.put("action", "refresh_addon");
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
            int previousState = bundle.getState();
            bundle.update();
            int currentState = bundle.getState();

            result.put("success", true);
            result.put("previousState", getBundleStateString(previousState));
            result.put("currentState", getBundleStateString(currentState));
            result.put("message", "Addon refreshed successfully");

        } catch (BundleException e) {
            result.put("success", false);
            result.put("error", "Failed to refresh addon: " + e.getMessage());
            logger.warn("Failed to refresh addon: {}", addonId, e);
        }

        return result;
    }

    private Map<String, Object> checkAddonHealth(Map<String, Object> parameters) {
        Map<String, Object> result = new HashMap<>();
        result.put("action", "health_check");
        result.put("timestamp", Instant.now().toString());

        if (bundleContext == null) {
            result.put("error", "Bundle context not available");
            return result;
        }

        String addonId = (String) parameters.get("addonId");
        String addonType = (String) parameters.get("addonType");

        if (addonId != null) {
            // Single addon health check
            Bundle bundle = findBundle(addonId);
            if (bundle == null) {
                result.put("error", "Addon not found: " + addonId);
                result.put("notFound", true);
                return result;
            }

            Map<String, Object> healthInfo = createHealthInfo(bundle);
            result.put("addonId", addonId);
            result.put("healthInfo", healthInfo);
            result.put("success", true);

        } else if (addonType != null) {
            // Bulk health check by type
            List<Map<String, Object>> healthResults = new ArrayList<>();
            Bundle[] bundles = bundleContext.getBundles();

            for (Bundle bundle : bundles) {
                if (bundle.getBundleId() == 0)
                    continue; // Skip system bundle

                String symbolicName = bundle.getSymbolicName();
                if (symbolicName != null && symbolicName.startsWith("org.openhab")) {
                    String bundleType = determineAddonType(symbolicName);
                    if ("all".equals(addonType) || addonType.equals(bundleType)) {
                        Map<String, Object> healthInfo = createHealthInfo(bundle);
                        healthInfo.put("addonId", symbolicName);
                        healthInfo.put("addonType", bundleType);
                        healthResults.add(healthInfo);
                    }
                }
            }

            result.put("addonType", addonType);
            result.put("healthResults", healthResults);
            result.put("totalChecked", healthResults.size());
            result.put("success", true);

        } else {
            result.put("error", "Either addonId or addonType parameter required");
            return result;
        }

        return result;
    }

    private Map<String, Object> createStatusInfo(Bundle bundle, boolean includeHealthMetrics,
            boolean includeDependencies) {
        Map<String, Object> status = new HashMap<>();

        status.put("bundleId", bundle.getBundleId());
        status.put("symbolicName", bundle.getSymbolicName());
        status.put("version", bundle.getVersion().toString());
        status.put("state", getBundleStateString(bundle.getState()));
        status.put("lastModified", bundle.getLastModified());
        status.put("location", bundle.getLocation());

        if (includeHealthMetrics) {
            status.put("healthMetrics", createHealthInfo(bundle));
        }

        if (includeDependencies) {
            status.put("dependencies", getBundleDependencies(bundle));
        }

        return status;
    }

    private Map<String, Object> createHealthInfo(Bundle bundle) {
        Map<String, Object> health = new HashMap<>();

        int state = bundle.getState();
        health.put("state", getBundleStateString(state));
        health.put("healthy", state == Bundle.ACTIVE);
        health.put("lastModified", bundle.getLastModified());
        health.put("uptime", System.currentTimeMillis() - bundle.getLastModified());

        // Additional health indicators
        health.put("hasValidHeaders", bundle.getHeaders() != null);
        health.put("hasValidLocation", bundle.getLocation() != null && !bundle.getLocation().isEmpty());

        return health;
    }

    private List<String> getBundleDependencies(Bundle bundle) {
        // This would require more complex OSGi dependency analysis
        // For now, return empty list
        return new ArrayList<>();
    }

    private String determineAddonType(String symbolicName) {
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
