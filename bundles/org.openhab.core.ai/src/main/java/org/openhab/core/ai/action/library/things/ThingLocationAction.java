package org.openhab.core.ai.action.library.things;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.openhab.core.ai.action.api.Action;
import org.openhab.core.ai.action.api.ActionException;
import org.openhab.core.ai.action.api.ActionMetadata;
import org.openhab.core.ai.action.api.ActionResult;
import org.openhab.core.ai.action.api.ActionValidationResult;
import org.openhab.core.ai.common.context.ExecutionContext;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action to retrieve and manage thing location information
 * 
 * This action provides comprehensive access to thing location information including
 * location details, location-based filtering, and location statistics.
 */
@Component(service = Action.class, immediate = true)
public class ThingLocationAction implements Action {

    private static final Logger logger = LoggerFactory.getLogger(ThingLocationAction.class);

    @Reference
    private ThingRegistry thingRegistry;

    @Override
    public String getActionId() {
        return "openhab.things.location";
    }

    @Override
    public String getActionName() {
        return "Thing Location";
    }

    @Override
    public String getDescription() {
        return "Retrieve and manage thing location information including location details, location-based filtering, and location statistics";
    }

    @Override
    public String getCategory() {
        return "things";
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
        properties.put("thingUID", Map.of("type", "string", "description",
                "The UID of the thing to get location information for", "required", false));
        properties.put("location",
                Map.of("type", "string", "description", "Filter things by location", "required", false));
        properties.put("locationPattern", Map.of("type", "string", "description",
                "Filter things by location pattern (e.g., 'Living*')", "required", false));
        properties.put("includeLocationStatistics",
                Map.of("type", "boolean", "description", "Include location statistics", "default", true));
        properties.put("includeThingDetails",
                Map.of("type", "boolean", "description", "Include thing details in results", "default", true));
        properties.put("sortBy", Map.of("type", "string", "description", "Sort results by (location, label, status)",
                "default", "location"));
        properties.put("sortOrder",
                Map.of("type", "string", "description", "Sort order (asc, desc)", "default", "asc"));

        schema.put("properties", properties);
        return schema;
    }

    @Override
    public Map<String, Object> getReturnSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();
        properties.put("success", Map.of("type", "boolean"));
        properties.put("timestamp", Map.of("type", "number"));
        properties.put("thingUID", Map.of("type", "string"));
        properties.put("found", Map.of("type", "boolean"));
        properties.put("location", Map.of("type", "string"));
        properties.put("things", Map.of("type", "array"));
        properties.put("thingCount", Map.of("type", "number"));
        properties.put("locationStatistics", Map.of("type", "object"));
        properties.put("error", Map.of("type", "string"));

        schema.put("properties", properties);
        return schema;
    }

    @Override
    public ActionValidationResult validateParameters(Map<String, Object> parameters) {
        if (parameters == null || parameters.isEmpty()) {
            return ActionValidationResult.invalid(List.of("Parameters cannot be null or empty"));
        }

        String thingUID = (String) parameters.get("thingUID");
        if (thingUID != null && !thingUID.trim().isEmpty()) {
            try {
                new ThingUID(thingUID);
            } catch (IllegalArgumentException e) {
                return ActionValidationResult.invalid(List.of("Invalid thingUID format: " + thingUID));
            }
        }

        String sortBy = (String) parameters.getOrDefault("sortBy", "location");
        List<String> validSortBy = List.of("location", "label", "status");
        if (!validSortBy.contains(sortBy)) {
            return ActionValidationResult.invalid(List.of("Invalid sortBy. Must be one of: " + validSortBy));
        }

        String sortOrder = (String) parameters.getOrDefault("sortOrder", "asc");
        List<String> validSortOrder = List.of("asc", "desc");
        if (!validSortOrder.contains(sortOrder)) {
            return ActionValidationResult.invalid(List.of("Invalid sortOrder. Must be one of: " + validSortOrder));
        }

        return ActionValidationResult.valid(parameters);
    }

    @Override
    public ActionResult execute(Map<String, Object> parameters, ExecutionContext context) throws ActionException {
        long executionStartTime = System.currentTimeMillis();

        try {
            String thingUID = (String) parameters.get("thingUID");
            String location = (String) parameters.get("location");
            String locationPattern = (String) parameters.get("locationPattern");
            Boolean includeLocationStatistics = (Boolean) parameters.getOrDefault("includeLocationStatistics", true);
            Boolean includeThingDetails = (Boolean) parameters.getOrDefault("includeThingDetails", true);
            String sortBy = (String) parameters.getOrDefault("sortBy", "location");
            String sortOrder = (String) parameters.getOrDefault("sortOrder", "asc");

            logger.debug("Getting location information for thing: {}", thingUID);

            Map<String, Object> result = getThingLocationInfo(thingUID, location, locationPattern,
                    includeLocationStatistics, includeThingDetails, sortBy, sortOrder);

            return ActionResult.success(result, System.currentTimeMillis() - executionStartTime);

        } catch (Exception e) {
            logger.error("Error executing ThingLocationAction: {}", e.getMessage(), e);
            throw new ActionException(getActionId(), "Failed to get thing location information: " + e.getMessage(),
                    "EXECUTION_ERROR");
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
        return ActionMetadata.builder().withVersion(getVersion()).withDescription(getDescription()).build();
    }

    @Override
    public Map<String, Object> getCapabilities() {
        Map<String, Object> capabilities = new HashMap<>();
        capabilities.put("realThingRegistryIntegration", true);
        capabilities.put("locationFiltering", true);
        capabilities.put("locationStatistics", true);
        capabilities.put("thingDetails", true);
        capabilities.put("sorting", true);
        return capabilities;
    }

    @Override
    public void initialize(ExecutionContext context) {
        // No initialization needed
    }

    @Override
    public void cleanup() {
        // No cleanup needed
    }

    @Override
    public boolean isReady() {
        return thingRegistry != null;
    }

    private Map<String, Object> getThingLocationInfo(String thingUID, String location, String locationPattern,
            boolean includeLocationStatistics, boolean includeThingDetails, String sortBy, String sortOrder) {

        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("timestamp", System.currentTimeMillis());

        try {
            // If specific thingUID is provided, get location for that thing
            if (thingUID != null && !thingUID.trim().isEmpty()) {
                result.put("thingUID", thingUID);

                ThingUID uid = new ThingUID(thingUID);
                Thing thing = thingRegistry.get(uid);

                if (thing == null) {
                    result.put("found", false);
                    result.put("error", "Thing not found: " + thingUID);
                    return result;
                }

                result.put("found", true);
                result.put("location", thing.getLocation());
                result.put("things", List.of());
                result.put("thingCount", 0);

                if (includeLocationStatistics) {
                    Map<String, Object> stats = getLocationStatistics(thing.getLocation());
                    result.put("locationStatistics", stats);
                }

                result.put("success", true);
                return result;
            }

            // Otherwise, get all things and filter by location
            List<Thing> allThings = new ArrayList<>(thingRegistry.getAll());
            List<Thing> filteredThings = allThings.stream().filter(thing -> {
                String thingLocation = thing.getLocation();

                // Filter by exact location
                if (location != null && !location.trim().isEmpty()) {
                    return location.equals(thingLocation);
                }

                // Filter by location pattern
                if (locationPattern != null && !locationPattern.trim().isEmpty()) {
                    if (thingLocation == null) {
                        return false;
                    }
                    String pattern = locationPattern.replace("*", ".*");
                    return thingLocation.matches(pattern);
                }

                return true; // No filter applied
            }).collect(Collectors.toList());

            // Sort things
            filteredThings.sort((t1, t2) -> {
                int comparison = 0;
                switch (sortBy) {
                    case "location":
                        String loc1 = t1.getLocation() != null ? t1.getLocation() : "";
                        String loc2 = t2.getLocation() != null ? t2.getLocation() : "";
                        comparison = loc1.compareToIgnoreCase(loc2);
                        break;
                    case "label":
                        String label1 = t1.getLabel() != null ? t1.getLabel() : "";
                        String label2 = t2.getLabel() != null ? t2.getLabel() : "";
                        comparison = label1.compareToIgnoreCase(label2);
                        break;
                    case "status":
                        comparison = t1.getStatus().toString().compareTo(t2.getStatus().toString());
                        break;
                }
                return "desc".equals(sortOrder) ? -comparison : comparison;
            });

            // Convert to maps
            List<Map<String, Object>> thingMaps = filteredThings.stream().map(
                    thing -> includeThingDetails ? convertThingToDetailedMap(thing) : convertThingToSimpleMap(thing))
                    .collect(Collectors.toList());

            result.put("things", thingMaps);
            result.put("thingCount", thingMaps.size());

            // Get location statistics
            if (includeLocationStatistics) {
                Map<String, Object> stats = getLocationStatistics(location);
                result.put("locationStatistics", stats);
            }

            result.put("success", true);

        } catch (Exception e) {
            logger.error("Error getting thing location info: {}", e.getMessage(), e);
            result.put("error", "Failed to get thing location information: " + e.getMessage());
        }

        return result;
    }

    private Map<String, Object> getLocationStatistics(String location) {
        Map<String, Object> stats = new HashMap<>();

        List<Thing> allThings = new ArrayList<>(thingRegistry.getAll());

        // Count things by location
        Map<String, Long> locationCounts = allThings.stream().filter(thing -> thing.getLocation() != null)
                .collect(Collectors.groupingBy(thing -> thing.getLocation(), Collectors.counting()));

        stats.put("totalThings", allThings.size());
        stats.put("thingsWithLocation", locationCounts.values().stream().mapToLong(Long::longValue).sum());
        stats.put("thingsWithoutLocation",
                allThings.size() - locationCounts.values().stream().mapToLong(Long::longValue).sum());
        stats.put("uniqueLocations", locationCounts.size());
        stats.put("locationCounts", locationCounts);

        if (location != null && !location.trim().isEmpty()) {
            stats.put("thingsInLocation", locationCounts.getOrDefault(location, 0L));
        }

        return stats;
    }

    private Map<String, Object> convertThingToSimpleMap(Thing thing) {
        Map<String, Object> thingMap = new HashMap<>();
        thingMap.put("uid", thing.getUID().toString());
        thingMap.put("label", thing.getLabel());
        thingMap.put("location", thing.getLocation());
        thingMap.put("status", thing.getStatus().toString());
        return thingMap;
    }

    private Map<String, Object> convertThingToDetailedMap(Thing thing) {
        Map<String, Object> thingMap = new HashMap<>();
        thingMap.put("uid", thing.getUID().toString());
        thingMap.put("thingTypeUID", thing.getThingTypeUID() != null ? thing.getThingTypeUID().toString() : null);
        thingMap.put("label", thing.getLabel());
        thingMap.put("location", thing.getLocation());
        thingMap.put("status", thing.getStatus().toString());
        thingMap.put("enabled", thing.isEnabled());
        thingMap.put("bridgeUID", thing.getBridgeUID() != null ? thing.getBridgeUID().toString() : null);
        thingMap.put("channelCount", thing.getChannels().size());
        return thingMap;
    }
}
