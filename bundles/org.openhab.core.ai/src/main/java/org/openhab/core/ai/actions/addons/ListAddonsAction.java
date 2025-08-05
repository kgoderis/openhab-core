package org.openhab.core.ai.actions.addons;

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
import org.openhab.core.ai.api.action.Action;
import org.openhab.core.ai.api.action.ActionException;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AI Action for listing and managing openHAB addons through OSGi bundle analysis.
 * 
 * This action provides comprehensive addon discovery and management capabilities
 * using real OSGi bundle management infrastructure.
 * 
 * @author openHAB
 * @version 1.0.0
 */
@Component(service = Action.class, immediate = true)
public class ListAddonsAction implements Action {

    private static final Logger logger = LoggerFactory.getLogger(ListAddonsAction.class);
    private static final String ACTION_ID = "openhab.addons.list";
    private static final String ACTION_NAME = "List Addons";
    private static final String DESCRIPTION = "Lists and provides information about openHAB addons using real OSGi bundle management";
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
                        List.of("list_addons", "get_addon_info", "addon_summary", "list_by_type"), "description",
                        "Action to perform for addon analysis"));
        properties.put("addonType",
                Map.of("type", "string", "enum",
                        List.of("binding", "transformation", "persistence", "voice", "ui", "all"), "description",
                        "Filter by addon type", "default", "all"));
        properties.put("addonId", Map.of("type", "string", "description", "Specific addon ID to analyze"));
        properties.put("includeDetails",
                Map.of("type", "boolean", "description", "Include detailed addon information", "default", false));
        properties.put("includeSystemBundles",
                Map.of("type", "boolean", "description", "Include system bundles in results", "default", false));
        properties.put("stateFilter",
                Map.of("type", "string", "enum",
                        List.of("ACTIVE", "INSTALLED", "RESOLVED", "STARTING", "STOPPING", "UNINSTALLED"),
                        "description", "Filter by bundle state"));

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

            List<String> validActions = List.of("list_addons", "get_addon_info", "addon_summary", "list_by_type");
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

            // Validate stateFilter if provided
            String stateFilter = (String) parameters.get("stateFilter");
            if (stateFilter != null) {
                List<String> validStates = List.of("ACTIVE", "INSTALLED", "RESOLVED", "STARTING", "STOPPING",
                        "UNINSTALLED");
                if (!validStates.contains(stateFilter)) {
                    return ActionValidationResult
                            .invalid(List.of("Invalid stateFilter: " + stateFilter + ". Valid states: " + validStates));
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
        properties.put("addons", Map.of("type", "array", "description", "List of addon information"));
        properties.put("totalAddons", Map.of("type", "integer", "description", "Total number of addons found"));
        properties.put("addonTypes", Map.of("type", "object", "description", "Count of addons by type"));
        properties.put("summary", Map.of("type", "object", "description", "Summary statistics"));

        schema.put("properties", properties);
        return schema;
    }

    @Override
    public ActionResult execute(Map<String, Object> parameters, ActionContext context) throws ActionException {
        long startTime = System.currentTimeMillis();

        try {
            String action = (String) parameters.get("action");
            Map<String, Object> result = switch (action) {
                case "list_addons" -> listAddons(parameters);
                case "get_addon_info" -> getAddonInfo(parameters);
                case "addon_summary" -> getAddonSummary(parameters);
                case "list_by_type" -> listByType(parameters);
                default -> throw new ActionException(ACTION_ID, "Unknown action: " + action, "INVALID_PARAMETER");
            };

            long executionTime = System.currentTimeMillis() - startTime;
            return ActionResult.success(result, executionTime);

        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            throw new ActionException(ACTION_ID, "Failed to execute addon operation: " + e.getMessage(), e);
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
                .tags(List.of("addons", "bundles", "osgi", "management")).build();
    }

    @Override
    public Map<String, Object> getCapabilities() {
        return Map.of("filtering", true, "sorting", false, "pagination", false, "metadata", true, "async", true,
                "osgi_integration", true, "bundle_management", true);
    }

    @Override
    public void initialize(ActionContext context) {
        logger.debug("ListAddonsAction initialized for protocol: {}", context.getProtocol());
    }

    @Override
    public void cleanup() {
        logger.debug("ListAddonsAction cleanup completed");
    }

    @Override
    public boolean isReady() {
        return bundleContext != null;
    }

    private Map<String, Object> listAddons(Map<String, Object> parameters) {
        Map<String, Object> result = new HashMap<>();
        result.put("action", "list_addons");
        result.put("timestamp", Instant.now().toString());

        if (bundleContext == null) {
            result.put("error", "Bundle context not available");
            return result;
        }

        String addonType = (String) parameters.getOrDefault("addonType", "all");
        Boolean includeDetails = (Boolean) parameters.getOrDefault("includeDetails", false);
        Boolean includeSystemBundles = (Boolean) parameters.getOrDefault("includeSystemBundles", false);
        String stateFilter = (String) parameters.get("stateFilter");

        // Get all bundles
        Bundle[] bundles = bundleContext.getBundles();
        List<Map<String, Object>> addons = new ArrayList<>();
        Map<String, Integer> addonTypeCounts = new HashMap<>();

        for (Bundle bundle : bundles) {
            // Skip system bundle unless requested
            if (!includeSystemBundles && bundle.getBundleId() == 0) {
                continue;
            }

            // Filter by state if specified
            if (stateFilter != null && !stateFilter.equals(getBundleStateString(bundle.getState()))) {
                continue;
            }

            // Analyze bundle to determine if it's an openHAB addon
            Map<String, Object> addonInfo = analyzeBundleAsAddon(bundle, addonType, includeDetails);
            if (addonInfo != null) {
                addons.add(addonInfo);

                // Count by type
                String type = (String) addonInfo.get("addonType");
                addonTypeCounts.put(type, addonTypeCounts.getOrDefault(type, 0) + 1);
            }
        }

        result.put("addons", addons);
        result.put("totalAddons", addons.size());
        result.put("addonTypes", addonTypeCounts);
        result.put("addonType", addonType);
        result.put("includeDetails", includeDetails);
        result.put("includeSystemBundles", includeSystemBundles);
        result.put("stateFilter", stateFilter);
        result.put("message", "Discovered " + addons.size() + " addons");

        return result;
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
        if (addonId == null) {
            result.put("error", "addonId parameter required");
            return result;
        }

        result.put("addonId", addonId);

        // Find bundle by symbolic name or bundle ID
        Bundle bundle = findBundle(addonId);
        if (bundle == null) {
            result.put("error", "Addon not found: " + addonId);
            result.put("notFound", true);
            return result;
        }

        Map<String, Object> addonInfo = analyzeBundleAsAddon(bundle, "all", true);
        result.put("addonInfo", addonInfo);
        result.put("notFound", false);

        return result;
    }

    private Map<String, Object> getAddonSummary(Map<String, Object> parameters) {
        Map<String, Object> result = new HashMap<>();
        result.put("action", "addon_summary");
        result.put("timestamp", Instant.now().toString());

        if (bundleContext == null) {
            result.put("error", "Bundle context not available");
            return result;
        }

        Bundle[] bundles = bundleContext.getBundles();
        Map<String, Integer> addonTypeCounts = new HashMap<>();
        Map<String, Integer> stateCounts = new HashMap<>();
        int totalAddons = 0;
        int activeAddons = 0;

        for (Bundle bundle : bundles) {
            // Skip system bundle
            if (bundle.getBundleId() == 0) {
                continue;
            }

            Map<String, Object> addonInfo = analyzeBundleAsAddon(bundle, "all", false);
            if (addonInfo != null) {
                totalAddons++;

                String type = (String) addonInfo.get("addonType");
                addonTypeCounts.put(type, addonTypeCounts.getOrDefault(type, 0) + 1);

                String state = (String) addonInfo.get("state");
                stateCounts.put(state, stateCounts.getOrDefault(state, 0) + 1);

                if ("ACTIVE".equals(state)) {
                    activeAddons++;
                }
            }
        }

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalAddons", totalAddons);
        summary.put("activeAddons", activeAddons);
        summary.put("addonTypeCounts", addonTypeCounts);
        summary.put("stateCounts", stateCounts);
        summary.put("healthPercentage", totalAddons > 0 ? (double) activeAddons / totalAddons * 100 : 0);

        result.put("summary", summary);
        result.put("message", "Addon summary generated successfully");

        return result;
    }

    private Map<String, Object> listByType(Map<String, Object> parameters) {
        Map<String, Object> result = new HashMap<>();
        result.put("action", "list_by_type");
        result.put("timestamp", Instant.now().toString());

        if (bundleContext == null) {
            result.put("error", "Bundle context not available");
            return result;
        }

        String addonType = (String) parameters.get("addonType");
        if (addonType == null || "all".equals(addonType)) {
            result.put("error", "addonType parameter required and must not be 'all'");
            return result;
        }

        Boolean includeDetails = (Boolean) parameters.getOrDefault("includeDetails", false);

        Bundle[] bundles = bundleContext.getBundles();
        List<Map<String, Object>> addons = new ArrayList<>();

        for (Bundle bundle : bundles) {
            // Skip system bundle
            if (bundle.getBundleId() == 0) {
                continue;
            }

            Map<String, Object> addonInfo = analyzeBundleAsAddon(bundle, addonType, includeDetails);
            if (addonInfo != null) {
                addons.add(addonInfo);
            }
        }

        result.put("addons", addons);
        result.put("addonType", addonType);
        result.put("totalAddons", addons.size());
        result.put("includeDetails", includeDetails);
        result.put("message", "Found " + addons.size() + " addons of type: " + addonType);

        return result;
    }

    private Map<String, Object> analyzeBundleAsAddon(Bundle bundle, String requestedType, boolean includeDetails) {
        String symbolicName = bundle.getSymbolicName();

        // Skip non-openHAB bundles
        if (symbolicName == null || !symbolicName.startsWith("org.openhab")) {
            return null;
        }

        // Determine addon type based on symbolic name patterns
        String addonType = determineAddonType(symbolicName);

        // Filter by requested type
        if (!"all".equals(requestedType) && !requestedType.equals(addonType)) {
            return null;
        }

        Map<String, Object> addonInfo = new HashMap<>();
        addonInfo.put("addonId", symbolicName);
        addonInfo.put("addonType", addonType);
        addonInfo.put("bundleId", bundle.getBundleId());
        addonInfo.put("version", bundle.getVersion().toString());
        addonInfo.put("state", getBundleStateString(bundle.getState()));
        addonInfo.put("lastModified", bundle.getLastModified());

        if (includeDetails) {
            addonInfo.put("location", bundle.getLocation());
            addonInfo.put("headers", getBundleHeaders(bundle));
            addonInfo.put("services", getBundleServices(bundle));
        }

        return addonInfo;
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

    private Map<String, String> getBundleHeaders(Bundle bundle) {
        Map<String, String> headers = new HashMap<>();
        var bundleHeaders = bundle.getHeaders();
        var keys = bundleHeaders.keys();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            headers.put(key, bundleHeaders.get(key));
        }
        return headers;
    }

    private List<String> getBundleServices(Bundle bundle) {
        // This would require more complex OSGi service registry access
        // For now, return empty list
        return new ArrayList<>();
    }
}
