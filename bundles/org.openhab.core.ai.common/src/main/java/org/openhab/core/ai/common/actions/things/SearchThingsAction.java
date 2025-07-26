package org.openhab.core.ai.common.actions.things;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.openhab.core.ai.common.api.action.AIAction;
import org.openhab.core.ai.common.api.action.AIActionContext;
import org.openhab.core.ai.common.api.action.AIActionException;
import org.openhab.core.ai.common.api.action.AIActionMetadata;
import org.openhab.core.ai.common.api.action.AIActionResult;
import org.openhab.core.ai.common.api.action.AIActionValidationResult;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingStatus;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action to search for things based on various criteria
 * 
 * 
 */
@Component(service = AIAction.class, immediate = true)
public class SearchThingsAction implements AIAction {

    private static final Logger logger = LoggerFactory.getLogger(SearchThingsAction.class);

    @Reference
    private ThingRegistry thingRegistry;

    @Override
    public String getActionId() {
        return "openhab.things.search";
    }

    @Override
    public String getActionName() {
        return "Search Things";
    }

    @Override
    public String getDescription() {
        return "Search for things based on various criteria";
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
        properties.put("bindingId", Map.of("type", "string", "description", "Filter by binding ID", "required", false));
        properties.put("thingTypeUID",
                Map.of("type", "string", "description", "Filter by thing type UID", "required", false));
        properties.put("status", Map.of("type", "string", "description",
                "Filter by thing status (ONLINE, OFFLINE, etc.)", "required", false));
        properties.put("enabled",
                Map.of("type", "boolean", "description", "Filter by enabled state", "required", false));
        properties.put("location",
                Map.of("type", "string", "description", "Filter by location (partial match)", "required", false));
        properties.put("label",
                Map.of("type", "string", "description", "Filter by label (partial match)", "required", false));
        properties.put("maxResults",
                Map.of("type", "number", "description", "Maximum number of results to return", "default", 100));
        properties.put("caseSensitive", Map.of("type", "boolean", "description",
                "Use case-sensitive matching for text fields", "default", false));

        schema.put("properties", properties);
        return schema;
    }

    @Override
    public Map<String, Object> getReturnSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();
        properties.put("things", Map.of("type", "array"));
        properties.put("totalCount", Map.of("type", "number"));
        properties.put("filteredCount", Map.of("type", "number"));
        properties.put("searchCriteria", Map.of("type", "object"));
        properties.put("success", Map.of("type", "boolean"));
        properties.put("timestamp", Map.of("type", "number"));

        schema.put("properties", properties);
        return schema;
    }

    @Override
    public AIActionValidationResult validateParameters(Map<String, Object> parameters) {
        if (parameters == null) {
            return AIActionValidationResult.invalid(List.of("Parameters cannot be null"));
        }

        // Validate status if provided
        String status = (String) parameters.get("status");
        if (status != null && !status.trim().isEmpty()) {
            try {
                ThingStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                return AIActionValidationResult.invalid(List.of("Invalid status: " + status));
            }
        }

        // Validate maxResults if provided
        Object maxResultsObj = parameters.get("maxResults");
        if (maxResultsObj != null) {
            if (maxResultsObj instanceof Number) {
                int maxResults = ((Number) maxResultsObj).intValue();
                if (maxResults <= 0) {
                    return AIActionValidationResult.invalid(List.of("maxResults must be positive"));
                }
            } else {
                return AIActionValidationResult.invalid(List.of("maxResults must be a number"));
            }
        }

        return AIActionValidationResult.valid(parameters);
    }

    @Override
    public AIActionResult execute(Map<String, Object> parameters, AIActionContext context) throws AIActionException {
        long executionStartTime = System.currentTimeMillis();

        try {
            String bindingId = (String) parameters.get("bindingId");
            String thingTypeUID = (String) parameters.get("thingTypeUID");
            String status = (String) parameters.get("status");
            Boolean enabled = (Boolean) parameters.get("enabled");
            String location = (String) parameters.get("location");
            String label = (String) parameters.get("label");
            Integer maxResults = parameters.get("maxResults") != null
                    ? ((Number) parameters.get("maxResults")).intValue()
                    : 100;
            Boolean caseSensitive = (Boolean) parameters.getOrDefault("caseSensitive", false);

            logger.debug(
                    "Searching things with criteria: bindingId={}, thingTypeUID={}, status={}, enabled={}, location={}, label={}",
                    bindingId, thingTypeUID, status, enabled, location, label);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("timestamp", System.currentTimeMillis());

            // Store search criteria
            Map<String, Object> searchCriteria = new HashMap<>();
            searchCriteria.put("bindingId", bindingId);
            searchCriteria.put("thingTypeUID", thingTypeUID);
            searchCriteria.put("status", status);
            searchCriteria.put("enabled", enabled);
            searchCriteria.put("location", location);
            searchCriteria.put("label", label);
            searchCriteria.put("maxResults", maxResults);
            searchCriteria.put("caseSensitive", caseSensitive);
            result.put("searchCriteria", searchCriteria);

            // Get all things
            List<Thing> allThings = new java.util.ArrayList<>(thingRegistry.getAll());
            result.put("totalCount", allThings.size());

            // Apply filters
            List<Thing> filteredThings = allThings.stream().filter(thing -> matchesBindingId(thing, bindingId))
                    .filter(thing -> matchesThingTypeUID(thing, thingTypeUID))
                    .filter(thing -> matchesStatus(thing, status)).filter(thing -> matchesEnabled(thing, enabled))
                    .filter(thing -> matchesLocation(thing, location, caseSensitive))
                    .filter(thing -> matchesLabel(thing, label, caseSensitive)).limit(maxResults)
                    .collect(Collectors.toList());

            result.put("filteredCount", filteredThings.size());

            // Convert things to result format
            List<Map<String, Object>> thingsResult = filteredThings.stream().map(this::convertThingToMap)
                    .collect(Collectors.toList());

            result.put("things", thingsResult);

            long executionTime = System.currentTimeMillis() - executionStartTime;
            logger.debug("Successfully searched things: found {} results in {}ms", filteredThings.size(),
                    executionTime);

            return AIActionResult.success(result, executionTime);

        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - executionStartTime;
            logger.error("Error searching things", e);
            throw new AIActionException(getActionId(), "Failed to search things: " + e.getMessage(), e);
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
                .description("Search for things based on various criteria")
                .tags(List.of("things", "search", "filter", "find"))
                .documentation(
                        "Searches for openHAB things based on binding ID, thing type, status, enabled state, location, and label.")
                .examples(List.of("Search all things: {}", "Search by binding: {\"bindingId\": \"zwave\"}",
                        "Search by status: {\"status\": \"ONLINE\"}",
                        "Search by location: {\"location\": \"Living Room\"}",
                        "Search by label: {\"label\": \"Light\"}",
                        "Complex search: {\"bindingId\": \"zwave\", \"status\": \"ONLINE\", \"enabled\": true, \"maxResults\": 50}"))
                .build();
    }

    @Override
    public Map<String, Object> getCapabilities() {
        Map<String, Object> capabilities = new HashMap<>();
        capabilities.put("async", true);
        capabilities.put("validation", true);
        capabilities.put("thingSearch", true);
        capabilities.put("bindingFilter", true);
        capabilities.put("statusFilter", true);
        capabilities.put("locationFilter", true);
        capabilities.put("labelFilter", true);
        capabilities.put("resultLimiting", true);
        return capabilities;
    }

    @Override
    public void initialize(AIActionContext context) {
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

    // Filter methods
    private boolean matchesBindingId(Thing thing, String bindingId) {
        return bindingId == null || bindingId.trim().isEmpty()
                || thing.getThingTypeUID().getBindingId().equals(bindingId);
    }

    private boolean matchesThingTypeUID(Thing thing, String thingTypeUID) {
        return thingTypeUID == null || thingTypeUID.trim().isEmpty()
                || thing.getThingTypeUID().getAsString().equals(thingTypeUID);
    }

    private boolean matchesStatus(Thing thing, String status) {
        return status == null || status.trim().isEmpty() || thing.getStatus().toString().equals(status.toUpperCase());
    }

    private boolean matchesEnabled(Thing thing, Boolean enabled) {
        return enabled == null || thing.isEnabled() == enabled;
    }

    private boolean matchesLocation(Thing thing, String location, boolean caseSensitive) {
        if (location == null || location.trim().isEmpty()) {
            return true;
        }
        String thingLocation = thing.getLocation();
        if (thingLocation == null) {
            return false;
        }
        if (caseSensitive) {
            return thingLocation.contains(location);
        } else {
            return thingLocation.toLowerCase().contains(location.toLowerCase());
        }
    }

    private boolean matchesLabel(Thing thing, String label, boolean caseSensitive) {
        if (label == null || label.trim().isEmpty()) {
            return true;
        }
        String thingLabel = thing.getLabel();
        if (thingLabel == null) {
            return false;
        }
        if (caseSensitive) {
            return thingLabel.contains(label);
        } else {
            return thingLabel.toLowerCase().contains(label.toLowerCase());
        }
    }

    private Map<String, Object> convertThingToMap(Thing thing) {
        Map<String, Object> thingMap = new HashMap<>();
        thingMap.put("uid", thing.getUID().getAsString());
        thingMap.put("thingTypeUID", thing.getThingTypeUID().getAsString());
        thingMap.put("bindingId", thing.getThingTypeUID().getBindingId());
        thingMap.put("label", thing.getLabel());
        thingMap.put("location", thing.getLocation());
        thingMap.put("status", thing.getStatus().toString());
        thingMap.put("enabled", thing.isEnabled());
        thingMap.put("bridgeUID", thing.getBridgeUID() != null ? thing.getBridgeUID().getAsString() : null);
        thingMap.put("channelCount", thing.getChannels().size());
        return thingMap;
    }
}
