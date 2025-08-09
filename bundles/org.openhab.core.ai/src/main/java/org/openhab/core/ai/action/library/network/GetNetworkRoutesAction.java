package org.openhab.core.ai.action.library.network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.action.ActionContext;
import org.openhab.core.ai.action.ActionMetadata;
import org.openhab.core.ai.action.ActionResult;
import org.openhab.core.ai.action.ActionValidationResult;
import org.openhab.core.ai.action.api.Action;
import org.openhab.core.ai.action.api.ActionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action to get network routing information
 * 
 * @author openHAB
 */
@NonNullByDefault
public class GetNetworkRoutesAction implements Action {

    private static final Logger logger = LoggerFactory.getLogger(GetNetworkRoutesAction.class);

    private static final String ACTION_ID = "get_network_routes";
    private static final String ACTION_NAME = "Get Network Routes";
    private static final String DESCRIPTION = "Retrieves network routing table information";
    private static final String CATEGORY = "network";
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

        Map<String, Object> properties = new HashMap<>();
        properties.put("includeDefaultRoute",
                Map.of("type", "boolean", "description", "Include default route information", "default", true));
        properties.put("includeLocalRoutes",
                Map.of("type", "boolean", "description", "Include local network routes", "default", true));
        properties.put("includeStaticRoutes",
                Map.of("type", "boolean", "description", "Include static routes", "default", true));
        properties.put("includeDynamicRoutes",
                Map.of("type", "boolean", "description", "Include dynamic routes", "default", true));

        schema.put("properties", properties);
        schema.put("required", List.of());

        return schema;
    }

    @Override
    public Map<String, Object> getReturnSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();
        properties.put("defaultRoute", Map.of("type", "object", "description", "Default route information"));
        properties.put("localRoutes", Map.of("type", "array", "description", "Local network routes"));
        properties.put("staticRoutes", Map.of("type", "array", "description", "Static routes"));
        properties.put("dynamicRoutes", Map.of("type", "array", "description", "Dynamic routes"));
        properties.put("summary", Map.of("type", "object", "description", "Route summary statistics"));
        properties.put("timestamp", Map.of("type", "string", "description", "Timestamp of the route information"));

        schema.put("properties", properties);

        return schema;
    }

    @Override
    public Map<String, Object> getCapabilities() {
        Map<String, Object> capabilities = new HashMap<>();
        capabilities.put("route_information", true);
        capabilities.put("default_route", true);
        capabilities.put("static_routes", true);
        capabilities.put("dynamic_routes", true);
        return capabilities;
    }

    @Override
    public ActionMetadata getMetadata() {
        return ActionMetadata.builder().description(DESCRIPTION).version(VERSION).author("openHAB").build();
    }

    @Override
    public ActionValidationResult validateParameters(Map<String, Object> parameters) {
        List<String> errors = new ArrayList<>();

        // Validate boolean parameters
        validateBooleanParameter(parameters, "includeDefaultRoute", errors);
        validateBooleanParameter(parameters, "includeLocalRoutes", errors);
        validateBooleanParameter(parameters, "includeStaticRoutes", errors);
        validateBooleanParameter(parameters, "includeDynamicRoutes", errors);

        if (errors.isEmpty()) {
            return ActionValidationResult.valid(parameters);
        } else {
            return ActionValidationResult.invalid(errors);
        }
    }

    private void validateBooleanParameter(Map<String, Object> parameters, String paramName, List<String> errors) {
        Object value = parameters.get(paramName);
        if (value != null && !(value instanceof Boolean)) {
            errors.add(paramName + " must be a boolean");
        }
    }

    @Override
    public ActionResult execute(Map<String, Object> parameters, ActionContext context) throws ActionException {
        logger.debug("Executing GetNetworkRoutesAction with context: {}", context.getProtocol());
        long startTime = System.currentTimeMillis();

        try {
            // Extract parameters with defaults
            boolean includeDefaultRoute = (Boolean) parameters.getOrDefault("includeDefaultRoute", true);
            boolean includeLocalRoutes = (Boolean) parameters.getOrDefault("includeLocalRoutes", true);
            boolean includeStaticRoutes = (Boolean) parameters.getOrDefault("includeStaticRoutes", true);
            boolean includeDynamicRoutes = (Boolean) parameters.getOrDefault("includeDynamicRoutes", true);

            Map<String, Object> result = new HashMap<>();

            // Get default route
            if (includeDefaultRoute) {
                result.put("defaultRoute", getDefaultRoute());
            }

            // Get local routes
            if (includeLocalRoutes) {
                result.put("localRoutes", getLocalRoutes());
            }

            // Get static routes
            if (includeStaticRoutes) {
                result.put("staticRoutes", getStaticRoutes());
            }

            // Get dynamic routes
            if (includeDynamicRoutes) {
                result.put("dynamicRoutes", getDynamicRoutes());
            }

            // Generate summary
            result.put("summary", generateRouteSummary(result));
            result.put("timestamp", java.time.Instant.now().toString());

            long executionTime = System.currentTimeMillis() - startTime;
            return ActionResult.success(result, executionTime);

        } catch (Exception e) {
            logger.error("Error getting network routes: {}", e.getMessage(), e);
            throw new ActionException(ACTION_ID, "Failed to get network routes: " + e.getMessage(), e);
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
    public void initialize(ActionContext context) {
        logger.debug("GetNetworkRoutesAction initialized with context: {}", context.getProtocol());
    }

    @Override
    public void cleanup() {
        logger.debug("GetNetworkRoutesAction cleaned up");
    }

    @Override
    public boolean isReady() {
        return true;
    }

    private Map<String, Object> getDefaultRoute() {
        Map<String, Object> defaultRoute = new HashMap<>();

        // Simulated default route information
        defaultRoute.put("destination", "0.0.0.0/0");
        defaultRoute.put("gateway", "192.168.1.1");
        defaultRoute.put("interface", "eth0");
        defaultRoute.put("metric", 100);
        defaultRoute.put("type", "default");
        defaultRoute.put("protocol", "static");
        defaultRoute.put("scope", "global");

        return defaultRoute;
    }

    private List<Map<String, Object>> getLocalRoutes() {
        List<Map<String, Object>> localRoutes = new ArrayList<>();

        // Loopback route
        Map<String, Object> loopbackRoute = new HashMap<>();
        loopbackRoute.put("destination", "127.0.0.0/8");
        loopbackRoute.put("gateway", "0.0.0.0");
        loopbackRoute.put("interface", "lo");
        loopbackRoute.put("metric", 1);
        loopbackRoute.put("type", "local");
        loopbackRoute.put("protocol", "kernel");
        loopbackRoute.put("scope", "host");
        localRoutes.add(loopbackRoute);

        // Local network route
        Map<String, Object> localNetworkRoute = new HashMap<>();
        localNetworkRoute.put("destination", "192.168.1.0/24");
        localNetworkRoute.put("gateway", "0.0.0.0");
        localNetworkRoute.put("interface", "eth0");
        localNetworkRoute.put("metric", 100);
        localNetworkRoute.put("type", "local");
        localNetworkRoute.put("protocol", "kernel");
        localNetworkRoute.put("scope", "link");
        localRoutes.add(localNetworkRoute);

        return localRoutes;
    }

    private List<Map<String, Object>> getStaticRoutes() {
        List<Map<String, Object>> staticRoutes = new ArrayList<>();

        // Example static route to a specific network
        Map<String, Object> staticRoute = new HashMap<>();
        staticRoute.put("destination", "10.0.0.0/8");
        staticRoute.put("gateway", "192.168.1.254");
        staticRoute.put("interface", "eth0");
        staticRoute.put("metric", 200);
        staticRoute.put("type", "static");
        staticRoute.put("protocol", "static");
        staticRoute.put("scope", "global");
        staticRoutes.add(staticRoute);

        return staticRoutes;
    }

    private List<Map<String, Object>> getDynamicRoutes() {
        List<Map<String, Object>> dynamicRoutes = new ArrayList<>();

        // Example dynamic route (e.g., from DHCP or routing protocol)
        Map<String, Object> dynamicRoute = new HashMap<>();
        dynamicRoute.put("destination", "172.16.0.0/12");
        dynamicRoute.put("gateway", "192.168.1.100");
        dynamicRoute.put("interface", "eth0");
        dynamicRoute.put("metric", 150);
        dynamicRoute.put("type", "dynamic");
        dynamicRoute.put("protocol", "dhcp");
        dynamicRoute.put("scope", "global");
        dynamicRoutes.add(dynamicRoute);

        return dynamicRoutes;
    }

    private Map<String, Object> generateRouteSummary(Map<String, Object> routes) {
        Map<String, Object> summary = new HashMap<>();

        int totalRoutes = 0;
        int defaultRoutes = 0;
        int localRoutes = 0;
        int staticRoutes = 0;
        int dynamicRoutes = 0;

        if (routes.containsKey("defaultRoute")) {
            defaultRoutes = 1;
            totalRoutes++;
        }

        if (routes.containsKey("localRoutes")) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> localRoutesList = (List<Map<String, Object>>) routes.get("localRoutes");
            localRoutes = localRoutesList.size();
            totalRoutes += localRoutes;
        }

        if (routes.containsKey("staticRoutes")) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> staticRoutesList = (List<Map<String, Object>>) routes.get("staticRoutes");
            staticRoutes = staticRoutesList.size();
            totalRoutes += staticRoutes;
        }

        if (routes.containsKey("dynamicRoutes")) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> dynamicRoutesList = (List<Map<String, Object>>) routes.get("dynamicRoutes");
            dynamicRoutes = dynamicRoutesList.size();
            totalRoutes += dynamicRoutes;
        }

        summary.put("totalRoutes", totalRoutes);
        summary.put("defaultRoutes", defaultRoutes);
        summary.put("localRoutes", localRoutes);
        summary.put("staticRoutes", staticRoutes);
        summary.put("dynamicRoutes", dynamicRoutes);

        return summary;
    }
}
