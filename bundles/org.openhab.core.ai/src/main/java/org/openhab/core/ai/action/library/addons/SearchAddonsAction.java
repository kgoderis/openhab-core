package org.openhab.core.ai.action.library.addons;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.action.api.Action;
import org.openhab.core.ai.action.api.ActionContext;
import org.openhab.core.ai.action.api.ActionException;
import org.openhab.core.ai.action.api.ActionMetadata;
import org.openhab.core.ai.action.api.ActionResult;
import org.openhab.core.ai.action.api.ActionValidationResult;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AI Action for searching openHAB addons through real OSGi bundle discovery and filtering.
 * 
 * This action provides comprehensive addon search capabilities including filtering by type,
 * name, version, and other criteria using real OSGi bundle analysis.
 * 
 * @author openHAB
 * @version 1.0.0
 */
@Component(service = Action.class, immediate = true)
public class SearchAddonsAction implements Action {

    private static final Logger logger = LoggerFactory.getLogger(SearchAddonsAction.class);
    private static final String ACTION_ID = "openhab.addons.search";
    private static final String ACTION_NAME = "Search Addons";
    private static final String DESCRIPTION = "Searches openHAB addons using real OSGi bundle discovery and filtering";
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
        properties.put("action", Map.of("type", "string", "enum",
                List.of("search_addons", "search_by_type", "search_by_name", "search_by_version", "search_by_vendor"),
                "description", "Action to perform for addon search"));
        properties.put("query", Map.of("type", "string", "description", "Search query string"));
        properties.put("addonType",
                Map.of("type", "string", "enum",
                        List.of("binding", "transformation", "persistence", "voice", "ui", "core", "all"),
                        "description", "Filter by addon type", "default", "all"));
        properties.put("version", Map.of("type", "string", "description", "Filter by version"));
        properties.put("vendor", Map.of("type", "string", "description", "Filter by vendor"));
        properties.put("state",
                Map.of("type", "string", "enum",
                        List.of("ACTIVE", "INSTALLED", "RESOLVED", "STARTING", "STOPPING", "UNINSTALLED", "all"),
                        "description", "Filter by bundle state", "default", "all"));
        properties.put("includeSystemBundles",
                Map.of("type", "boolean", "description", "Include system bundles in search results", "default", false));
        properties.put("maxResults", Map.of("type", "integer", "minimum", 1, "maximum", 1000, "description",
                "Maximum number of results to return", "default", 100));
        properties.put("includeDetails",
                Map.of("type", "boolean", "description", "Include detailed addon information", "default", false));

        schema.put("properties", properties);
        schema.put("required", List.of("action"));
        schema.put("additionalProperties", false);
        return schema;
    }

    @Override
    public ActionValidationResult validateParameters(Map<String, Object> parameters) {
        try {
            if (!parameters.containsKey("action")) {
                return ActionValidationResult.invalid(List.of("Missing required parameter: action"));
            }

            String action = (String) parameters.get("action");
            if (action == null || action.trim().isEmpty()) {
                return ActionValidationResult.invalid(List.of("action cannot be null or empty"));
            }

            return ActionValidationResult.valid(parameters);
        } catch (Exception e) {
            return ActionValidationResult.invalid(List.of("Parameter validation failed: " + e.getMessage()));
        }
    }

    @Override
    public Map<String, Object> getReturnSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();
        properties.put("success", Map.of("type", "boolean", "description", "Whether the operation was successful"));
        properties.put("totalResults", Map.of("type", "integer", "description", "Total number of results found"));
        properties.put("results", Map.of("type", "array", "description", "List of matching addons"));
        properties.put("searchCriteria", Map.of("type", "object", "description", "Search criteria used"));
        properties.put("error", Map.of("type", "string", "description", "Error message if operation failed"));
        properties.put("timestamp", Map.of("type", "string", "description", "Timestamp of the operation"));

        schema.put("properties", properties);
        return schema;
    }

    @Override
    public ActionResult execute(Map<String, Object> parameters, ActionContext context) throws ActionException {
        try {
            logger.debug("Executing SearchAddonsAction with parameters: {}", parameters);

            long startTime = System.currentTimeMillis();
            Map<String, Object> result = searchAddons(parameters);
            long executionTime = System.currentTimeMillis() - startTime;

            return ActionResult.success(result, executionTime);
        } catch (Exception e) {
            logger.error("Error executing SearchAddonsAction", e);
            throw new ActionException(ACTION_ID, "Failed to search addons: " + e.getMessage(), e);
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
        return ActionMetadata.builder().version(VERSION).description(DESCRIPTION).build();
    }

    @Override
    public Map<String, Object> getCapabilities() {
        Map<String, Object> capabilities = new HashMap<>();
        capabilities.put("supportsAsync", true);
        capabilities.put("maxConcurrentExecutions", 10);
        capabilities.put("timeout", 30000);
        return capabilities;
    }

    @Override
    public void initialize(ActionContext context) {
        logger.debug("Initializing SearchAddonsAction");
    }

    @Override
    public void cleanup() {
        logger.debug("Cleaning up SearchAddonsAction");
    }

    @Override
    public boolean isReady() {
        return bundleContext != null;
    }

    private Map<String, Object> searchAddons(Map<String, Object> parameters) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("timestamp", Instant.now().toString());

        String action = (String) parameters.get("action");
        String query = (String) parameters.get("query");
        String addonType = (String) parameters.getOrDefault("addonType", "all");
        String version = (String) parameters.get("version");
        String vendor = (String) parameters.get("vendor");
        String state = (String) parameters.getOrDefault("state", "all");
        boolean includeSystemBundles = (Boolean) parameters.getOrDefault("includeSystemBundles", false);
        int maxResults = (Integer) parameters.getOrDefault("maxResults", 100);
        boolean includeDetails = (Boolean) parameters.getOrDefault("includeDetails", false);

        if (bundleContext == null) {
            result.put("error", "BundleContext not available");
            return result;
        }

        try {
            List<Map<String, Object>> searchResults = new ArrayList<>();
            Bundle[] bundles = bundleContext.getBundles();

            for (Bundle bundle : bundles) {
                // Skip system bundles unless explicitly requested
                if (!includeSystemBundles && isSystemBundle(bundle)) {
                    continue;
                }

                // Apply filters
                if (!matchesTypeFilter(bundle, addonType)) {
                    continue;
                }

                if (!matchesStateFilter(bundle, state)) {
                    continue;
                }

                if (version != null && !version.trim().isEmpty() && !matchesVersionFilter(bundle, version)) {
                    continue;
                }

                if (vendor != null && !vendor.trim().isEmpty() && !matchesVendorFilter(bundle, vendor)) {
                    continue;
                }

                // Apply search query
                if (query != null && !query.trim().isEmpty()) {
                    switch (action) {
                        case "search_by_name":
                            if (!matchesNameQuery(bundle, query)) {
                                continue;
                            }
                            break;
                        case "search_by_version":
                            if (!matchesVersionQuery(bundle, query)) {
                                continue;
                            }
                            break;
                        case "search_by_vendor":
                            if (!matchesVendorQuery(bundle, query)) {
                                continue;
                            }
                            break;
                        default: // search_addons or search_by_type
                            if (!matchesGeneralQuery(bundle, query)) {
                                continue;
                            }
                            break;
                    }
                }

                // Add matching bundle to results
                Map<String, Object> addonInfo = createAddonInfo(bundle, includeDetails);
                searchResults.add(addonInfo);

                // Check max results limit
                if (searchResults.size() >= maxResults) {
                    break;
                }
            }

            // Create search criteria summary
            Map<String, Object> searchCriteria = new HashMap<>();
            searchCriteria.put("action", action);
            searchCriteria.put("query", query);
            searchCriteria.put("addonType", addonType);
            searchCriteria.put("version", version);
            searchCriteria.put("vendor", vendor);
            searchCriteria.put("state", state);
            searchCriteria.put("includeSystemBundles", includeSystemBundles);
            searchCriteria.put("maxResults", maxResults);

            result.put("success", true);
            result.put("totalResults", searchResults.size());
            result.put("results", searchResults);
            result.put("searchCriteria", searchCriteria);

        } catch (Exception e) {
            logger.error("Error searching addons", e);
            result.put("error", "Failed to search addons: " + e.getMessage());
        }

        return result;
    }

    private boolean isSystemBundle(Bundle bundle) {
        String symbolicName = bundle.getSymbolicName();
        return symbolicName != null && (symbolicName.startsWith("org.eclipse.osgi")
                || symbolicName.startsWith("org.apache.felix") || symbolicName.startsWith("org.osgi")
                || symbolicName.startsWith("sun.misc") || symbolicName.startsWith("java."));
    }

    private boolean matchesTypeFilter(Bundle bundle, String addonType) {
        if ("all".equals(addonType)) {
            return true;
        }

        String bundleType = determineAddonType(bundle.getSymbolicName());
        return addonType.equals(bundleType);
    }

    private boolean matchesStateFilter(Bundle bundle, String state) {
        if ("all".equals(state)) {
            return true;
        }

        String bundleState = getBundleStateString(bundle.getState());
        return state.equals(bundleState);
    }

    private boolean matchesVersionFilter(Bundle bundle, String version) {
        try {
            String bundleVersion = bundle.getVersion().toString();
            return bundleVersion.contains(version);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean matchesVendorFilter(Bundle bundle, String vendor) {
        try {
            String bundleVendor = getBundleHeader(bundle, Constants.BUNDLE_VENDOR);
            return bundleVendor.toLowerCase().contains(vendor.toLowerCase());
        } catch (Exception e) {
            return false;
        }
    }

    private boolean matchesNameQuery(Bundle bundle, String query) {
        String symbolicName = bundle.getSymbolicName();
        if (symbolicName == null) {
            return false;
        }
        return symbolicName.toLowerCase().contains(query.toLowerCase());
    }

    private boolean matchesVersionQuery(Bundle bundle, String query) {
        try {
            String version = bundle.getVersion().toString();
            return version.toLowerCase().contains(query.toLowerCase());
        } catch (Exception e) {
            return false;
        }
    }

    private boolean matchesVendorQuery(Bundle bundle, String query) {
        try {
            String vendor = getBundleHeader(bundle, Constants.BUNDLE_VENDOR);
            return vendor.toLowerCase().contains(query.toLowerCase());
        } catch (Exception e) {
            return false;
        }
    }

    private boolean matchesGeneralQuery(Bundle bundle, String query) {
        // Search across multiple fields
        String symbolicName = bundle.getSymbolicName();
        String description = getBundleHeader(bundle, Constants.BUNDLE_DESCRIPTION);
        String vendor = getBundleHeader(bundle, Constants.BUNDLE_VENDOR);
        String version = bundle.getVersion().toString();

        String searchText = (symbolicName + " " + description + " " + vendor + " " + version).toLowerCase();
        return searchText.contains(query.toLowerCase());
    }

    private Map<String, Object> createAddonInfo(Bundle bundle, boolean includeDetails) {
        Map<String, Object> addonInfo = new HashMap<>();

        addonInfo.put("bundleId", bundle.getBundleId());
        addonInfo.put("symbolicName", bundle.getSymbolicName());
        addonInfo.put("version", bundle.getVersion().toString());
        addonInfo.put("state", getBundleStateString(bundle.getState()));
        addonInfo.put("addonType", determineAddonType(bundle.getSymbolicName()));

        if (includeDetails) {
            addonInfo.put("location", bundle.getLocation());
            addonInfo.put("lastModified", bundle.getLastModified());
            addonInfo.put("vendor", getBundleHeader(bundle, Constants.BUNDLE_VENDOR));
            addonInfo.put("description", getBundleHeader(bundle, Constants.BUNDLE_DESCRIPTION));
            addonInfo.put("copyright", getBundleHeader(bundle, Constants.BUNDLE_COPYRIGHT));
            addonInfo.put("docURL", getBundleHeader(bundle, Constants.BUNDLE_DOCURL));
        }

        return addonInfo;
    }

    private String determineAddonType(String symbolicName) {
        if (symbolicName == null) {
            return "unknown";
        }

        String lowerName = symbolicName.toLowerCase();

        if (lowerName.contains("binding")) {
            return "binding";
        } else if (lowerName.contains("transformation")) {
            return "transformation";
        } else if (lowerName.contains("persistence")) {
            return "persistence";
        } else if (lowerName.contains("voice")) {
            return "voice";
        } else if (lowerName.contains("ui")) {
            return "ui";
        } else if (lowerName.contains("core")) {
            return "core";
        } else {
            return "other";
        }
    }

    private String getBundleStateString(int state) {
        switch (state) {
            case Bundle.ACTIVE:
                return "ACTIVE";
            case Bundle.INSTALLED:
                return "INSTALLED";
            case Bundle.RESOLVED:
                return "RESOLVED";
            case Bundle.STARTING:
                return "STARTING";
            case Bundle.STOPPING:
                return "STOPPING";
            case Bundle.UNINSTALLED:
                return "UNINSTALLED";
            default:
                return "UNKNOWN";
        }
    }

    private String getBundleHeader(Bundle bundle, String headerName) {
        try {
            String value = bundle.getHeaders().get(headerName);
            return value != null ? value : "";
        } catch (Exception e) {
            return "";
        }
    }
}
