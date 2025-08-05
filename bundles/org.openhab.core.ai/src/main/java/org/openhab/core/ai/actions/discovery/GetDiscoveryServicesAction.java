package org.openhab.core.ai.actions.discovery;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.action.ActionContext;
import org.openhab.core.ai.action.ActionMetadata;
import org.openhab.core.ai.action.ActionResult;
import org.openhab.core.ai.action.ActionValidationResult;
import org.openhab.core.ai.api.action.Action;
import org.openhab.core.ai.api.action.ActionException;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action for retrieving discovery services in openHAB.
 * 
 * This action provides functionality to get information
 * about available discovery services.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class GetDiscoveryServicesAction implements Action {

    private static final Logger logger = LoggerFactory.getLogger(GetDiscoveryServicesAction.class);
    private static final String ACTION_ID = "openhab.discovery.services";
    private static final String ACTION_NAME = "Get Discovery Services";
    private static final String CATEGORY = "discovery";

    @Reference
    private @Nullable ThingRegistry thingRegistry;

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
        return "Gets information about available discovery services";
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
        properties.put("bindingId", Map.of("type", "string", "description", "Filter services by binding ID"));
        properties.put("protocol", Map.of("type", "string", "description", "Filter services by protocol"));
        properties.put("status", Map.of("type", "string", "description", "Filter services by status", "enum",
                List.of("active", "inactive", "error", "all"), "default", "all"));
        properties.put("includeCapabilities",
                Map.of("type", "boolean", "description", "Include service capabilities", "default", true));
        properties.put("includeConfiguration",
                Map.of("type", "boolean", "description", "Include service configuration", "default", false));
        properties.put("includeStatistics",
                Map.of("type", "boolean", "description", "Include service statistics", "default", false));

        schema.put("properties", properties);
        schema.put("additionalProperties", false);
        return schema;
    }

    @Override
    public Map<String, Object> getReturnSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();
        properties.put("services", Map.of("type", "array", "description", "List of discovery services"));
        properties.put("totalCount", Map.of("type", "integer", "description", "Total number of services"));
        properties.put("activeCount", Map.of("type", "integer", "description", "Number of active services"));
        properties.put("inactiveCount", Map.of("type", "integer", "description", "Number of inactive services"));
        properties.put("errorCount", Map.of("type", "integer", "description", "Number of services with errors"));
        properties.put("timestamp", Map.of("type", "string", "description", "Timestamp of the operation"));

        schema.put("properties", properties);
        return schema;
    }

    @Override
    public Map<String, Object> getCapabilities() {
        Map<String, Object> capabilities = new HashMap<>();
        capabilities.put("supportsAsync", true);
        capabilities.put("supportsStreaming", false);
        capabilities.put("requiresAuthentication", true);
        capabilities.put("supportsBulkOperations", false);
        capabilities.put("canIncludeCapabilities", true);
        capabilities.put("canIncludeConfiguration", true);
        capabilities.put("canIncludeStatistics", true);
        return capabilities;
    }

    @Override
    public ActionValidationResult validateParameters(Map<String, Object> parameters) {
        List<String> errors = new ArrayList<>();

        // Validate status if provided
        if (parameters.containsKey("status")) {
            Object status = parameters.get("status");
            if (!(status instanceof String)) {
                errors.add("status must be a string");
            } else {
                String statusValue = (String) status;
                List<String> validStatuses = List.of("active", "inactive", "error", "all");
                if (!validStatuses.contains(statusValue)) {
                    errors.add("status must be one of: " + validStatuses);
                }
            }
        }

        if (errors.isEmpty()) {
            return ActionValidationResult.valid(parameters);
        } else {
            return ActionValidationResult.invalid(errors);
        }
    }

    @Override
    public ActionResult execute(Map<String, Object> parameters, ActionContext context) throws ActionException {
        logger.debug("Executing get discovery services with parameters: {}", parameters);

        long startTime = System.currentTimeMillis();

        try {
            // Extract parameters
            String bindingId = (String) parameters.get("bindingId");
            String protocol = (String) parameters.get("protocol");
            String status = (String) parameters.getOrDefault("status", "all");
            boolean includeCapabilities = (Boolean) parameters.getOrDefault("includeCapabilities", true);
            boolean includeConfiguration = (Boolean) parameters.getOrDefault("includeConfiguration", false);
            boolean includeStatistics = (Boolean) parameters.getOrDefault("includeStatistics", false);

            // Get discovery services
            Map<String, Object> result = getDiscoveryServices(bindingId, protocol, status, includeCapabilities,
                    includeConfiguration, includeStatistics);

            long executionTime = System.currentTimeMillis() - startTime;
            return ActionResult.success(result, executionTime);

        } catch (Exception e) {
            logger.error("Error executing get discovery services", e);
            throw new ActionException(ACTION_ID, "Failed to get discovery services: " + e.getMessage(), e);
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
        return ActionMetadata.builder().author("openHAB")
                .description("Gets information about available discovery services").version("1.0.0")
                .tags(List.of("discovery", "services", "capabilities", "configuration")).build();
    }

    @Override
    public void initialize(ActionContext context) {
        logger.debug("Initializing GetDiscoveryServicesAction");
    }

    @Override
    public void cleanup() {
        logger.debug("Cleaning up GetDiscoveryServicesAction");
    }

    @Override
    public boolean isReady() {
        return thingRegistry != null;
    }

    private Map<String, Object> getDiscoveryServices(String bindingId, String protocol, String status,
            boolean includeCapabilities, boolean includeConfiguration, boolean includeStatistics) {

        logger.debug("Getting discovery services - Binding: {}, Protocol: {}, Status: {}", bindingId, protocol, status);

        List<Map<String, Object>> allServices = new ArrayList<>();
        List<Map<String, Object>> filteredServices = new ArrayList<>();

        // Get all registered discovery services
        if (thingRegistry != null) {
            for (Thing thing : thingRegistry.getAll()) {
                String thingTypeId = thing.getThingTypeUID().getId();
                if (thingTypeId.startsWith("discovery:")) {
                    Map<String, Object> service = new HashMap<>();
                    service.put("serviceId", thing.getUID().getId());
                    service.put("bindingId", thingTypeId.substring("discovery:".length()));
                    String thingLabel = thing.getLabel() != null ? thing.getLabel() : "Unknown";
                    service.put("name", thingLabel);
                    service.put("protocol", thingTypeId.substring("discovery:".length())); // Assuming protocol is part
                                                                                           // of the binding ID
                    service.put("status", "active"); // Default to active for registered services
                    service.put("description", "Discovery service for " + thingTypeId);
                    service.put("version", "1.0.0"); // Placeholder, could be retrieved from ThingType
                    service.put("lastUpdated", Instant.now().toString());

                    if (includeCapabilities) {
                        Map<String, Object> capabilities = new HashMap<>();
                        capabilities.put("supportsAutoDiscovery", true);
                        capabilities.put("supportsManualConfiguration", true);
                        capabilities.put("maxDevices", 100); // Placeholder, could be retrieved from ThingType
                        capabilities.put("supportedDeviceTypes", List.of("bridge", "light", "switch", "sensor")); // Placeholder,
                                                                                                                  // could
                                                                                                                  // be
                                                                                                                  // retrieved
                                                                                                                  // from
                                                                                                                  // ThingType
                        capabilities.put("networkScan", true);
                        capabilities.put("portScan", true);
                        capabilities.put("timeout", 300);
                        service.put("capabilities", capabilities);
                    }

                    if (includeConfiguration) {
                        Map<String, Object> config = new HashMap<>();
                        config.put("scanInterval", 300);
                        config.put("autoApprove", false);
                        config.put("networkTimeout", 30);
                        config.put("retryAttempts", 3);
                        service.put("configuration", config);
                    }

                    if (includeStatistics) {
                        Map<String, Object> stats = new HashMap<>();
                        stats.put("totalDiscoveries", 10); // Placeholder
                        stats.put("successfulDiscoveries", 8); // Placeholder
                        stats.put("failedDiscoveries", 2); // Placeholder
                        stats.put("averageDiscoveryTime", 45); // Placeholder
                        stats.put("lastDiscoveryTime", Instant.now().minusSeconds(1800).toString()); // Placeholder
                        service.put("statistics", stats);
                    }

                    allServices.add(service);
                }
            }
        }

        // Apply filters
        for (Map<String, Object> service : allServices) {
            boolean matchesBinding = bindingId == null || bindingId.equals(service.get("bindingId"));
            boolean matchesProtocol = protocol == null || protocol.equals(service.get("protocol"));
            boolean matchesStatus = "all".equals(status) || status.equals(service.get("status"));

            if (matchesBinding && matchesProtocol && matchesStatus) {
                Map<String, Object> filteredService = new HashMap<>(service);

                // Remove optional fields based on parameters
                if (!includeCapabilities) {
                    filteredService.remove("capabilities");
                }
                if (!includeConfiguration) {
                    filteredService.remove("configuration");
                }
                if (!includeStatistics) {
                    filteredService.remove("statistics");
                }

                filteredServices.add(filteredService);
            }
        }

        // Count by status
        long activeCount = filteredServices.stream().filter(s -> "active".equals(s.get("status"))).count();
        long inactiveCount = filteredServices.stream().filter(s -> "inactive".equals(s.get("status"))).count();
        long errorCount = filteredServices.stream().filter(s -> "error".equals(s.get("status"))).count();

        Map<String, Object> result = new HashMap<>();
        result.put("services", filteredServices);
        result.put("totalCount", filteredServices.size());
        result.put("activeCount", activeCount);
        result.put("inactiveCount", inactiveCount);
        result.put("errorCount", errorCount);
        result.put("timestamp", Instant.now().toString());

        return result;
    }
}
