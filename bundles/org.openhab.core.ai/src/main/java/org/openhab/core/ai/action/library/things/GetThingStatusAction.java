package org.openhab.core.ai.action.library.things;

import java.time.ZonedDateTime;
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
import org.openhab.core.persistence.FilterCriteria;
import org.openhab.core.persistence.HistoricItem;
import org.openhab.core.persistence.PersistenceService;
import org.openhab.core.persistence.PersistenceServiceRegistry;
import org.openhab.core.persistence.QueryablePersistenceService;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.types.State;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action to retrieve detailed status information for openHAB things
 * 
 * Uses openHAB's persistence services to retrieve real thing status history.
 */
@Component(service = Action.class, immediate = true)
public class GetThingStatusAction implements Action {

    private static final Logger logger = LoggerFactory.getLogger(GetThingStatusAction.class);

    @Reference
    private ThingRegistry thingRegistry;

    @Reference
    private @Nullable PersistenceServiceRegistry persistenceServiceRegistry;

    @Override
    public String getActionId() {
        return "openhab.things.getStatus";
    }

    @Override
    public String getActionName() {
        return "Get Thing Status";
    }

    @Override
    public String getDescription() {
        return "Retrieve detailed status information for openHAB things";
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
        properties.put("thingUID",
                Map.of("type", "string", "description", "The UID of the thing to get status for", "required", true));
        properties.put("includeStatusHistory",
                Map.of("type", "boolean", "description", "Include status change history", "default", false));
        properties.put("includeStatusDetails",
                Map.of("type", "boolean", "description", "Include detailed status information", "default", true));
        properties.put("persistenceService", Map.of("type", "string", "description",
                "Persistence service to use (e.g., 'rrd4j', 'influxdb', 'jdbc')", "default", "rrd4j"));

        schema.put("properties", properties);
        schema.put("required", List.of("thingUID"));
        return schema;
    }

    @Override
    public Map<String, Object> getReturnSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();
        properties.put("thingUID", Map.of("type", "string"));
        properties.put("status", Map.of("type", "string"));
        properties.put("statusDetail", Map.of("type", "string"));
        properties.put("statusDescription", Map.of("type", "string"));
        properties.put("enabled", Map.of("type", "boolean"));
        properties.put("statusDetails", Map.of("type", "object"));
        properties.put("statusHistory", Map.of("type", "array"));
        properties.put("success", Map.of("type", "boolean"));
        properties.put("timestamp", Map.of("type", "number"));

        schema.put("properties", properties);
        return schema;
    }

    @Override
    public ActionValidationResult validateParameters(Map<String, Object> parameters) {
        try {
            String thingUID = (String) parameters.get("thingUID");
            if (thingUID == null || thingUID.trim().isEmpty()) {
                return ActionValidationResult.invalid(List.of("thingUID is required"));
            }

            return ActionValidationResult.valid(parameters);

        } catch (Exception e) {
            return ActionValidationResult.invalid(List.of("Parameter validation failed: " + e.getMessage()));
        }
    }

    @Override
    public ActionResult execute(Map<String, Object> parameters, ActionContext context) throws ActionException {
        long executionStartTime = System.currentTimeMillis();

        try {
            String thingUID = (String) parameters.get("thingUID");
            Boolean includeStatusHistory = (Boolean) parameters.getOrDefault("includeStatusHistory", false);
            Boolean includeStatusDetails = (Boolean) parameters.getOrDefault("includeStatusDetails", true);
            String persistenceService = (String) parameters.getOrDefault("persistenceService", "rrd4j");

            logger.debug("Getting status for thing: {} using service: {}", thingUID, persistenceService);

            Map<String, Object> result = new HashMap<>();
            result.put("thingUID", thingUID);
            result.put("success", true);
            result.put("timestamp", System.currentTimeMillis());

            // Get the thing
            ThingUID uid = new ThingUID(thingUID);
            Thing thing = thingRegistry.get(uid);

            if (thing == null) {
                result.put("error", "Thing not found");
                return ActionResult.success(result, System.currentTimeMillis() - executionStartTime);
            }

            // Basic status information
            ThingStatus status = thing.getStatus();
            result.put("status", status.toString());
            result.put("statusDetail", thing.getStatusInfo().getStatusDetail().toString());
            result.put("statusDescription", thing.getStatusInfo().getDescription());
            result.put("enabled", thing.isEnabled());

            // Detailed status information
            if (includeStatusDetails) {
                Map<String, Object> statusDetails = new HashMap<>();
                statusDetails.put("status", status.toString());
                statusDetails.put("statusDetail", thing.getStatusInfo().getStatusDetail().toString());
                statusDetails.put("description", thing.getStatusInfo().getDescription());
                statusDetails.put("enabled", thing.isEnabled());
                statusDetails.put("bindingId", thing.getThingTypeUID().getBindingId());
                statusDetails.put("thingTypeUID", thing.getThingTypeUID().getAsString());
                statusDetails.put("label", thing.getLabel());
                statusDetails.put("location", thing.getLocation());
                statusDetails.put("bridgeUID",
                        thing.getBridgeUID() != null ? thing.getBridgeUID().getAsString() : null);
                statusDetails.put("channelCount", thing.getChannels().size());
                statusDetails.put("configurationCount", thing.getConfiguration().getProperties().size());
                statusDetails.put("propertiesCount", thing.getProperties().size());

                // Status interpretation
                String statusInterpretation = interpretStatus(status);
                statusDetails.put("interpretation", statusInterpretation);
                statusDetails.put("isOnline", status == ThingStatus.ONLINE);
                statusDetails.put("isOffline", status == ThingStatus.OFFLINE);
                statusDetails.put("isUninitialized", status == ThingStatus.UNINITIALIZED);
                statusDetails.put("isInitializing", status == ThingStatus.INITIALIZING);
                statusDetails.put("isRemoving", status == ThingStatus.REMOVING);
                statusDetails.put("isRemoved", status == ThingStatus.REMOVED);
                statusDetails.put("isUnknown", status == ThingStatus.UNKNOWN);

                result.put("statusDetails", statusDetails);
            } else {
                result.put("statusDetails", Map.of());
            }

            // Status history (real implementation)
            if (includeStatusHistory) {
                List<Map<String, Object>> statusHistory = getRealStatusHistory(thingUID, persistenceService);
                result.put("statusHistory", statusHistory);
                result.put("historyEntryCount", statusHistory.size());
            } else {
                result.put("statusHistory", List.of());
                result.put("historyEntryCount", 0);
            }

            long executionTime = System.currentTimeMillis() - executionStartTime;
            logger.debug("Successfully retrieved status for thing: {} in {}ms", thingUID, executionTime);

            return ActionResult.success(result, executionTime);

        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - executionStartTime;
            logger.error("Error getting thing status", e);
            throw new ActionException(getActionId(), "Failed to get thing status: " + e.getMessage(), e);
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
                .description("Retrieve detailed status information for openHAB things")
                .tags(List.of("things", "status", "monitoring", "health"))
                .documentation(
                        "Retrieves comprehensive status information for openHAB things including current status, details, and optional history.")
                .examples(List.of("Get basic status: {\"thingUID\": \"binding:type:id\"}",
                        "Get with details: {\"thingUID\": \"binding:type:id\", \"includeStatusDetails\": true}",
                        "Get with history: {\"thingUID\": \"binding:type:id\", \"includeStatusHistory\": true}",
                        "Get everything: {\"thingUID\": \"binding:type:id\", \"includeStatusDetails\": true, \"includeStatusHistory\": true}"))
                .build();
    }

    @Override
    public Map<String, Object> getCapabilities() {
        Map<String, Object> capabilities = new HashMap<>();
        capabilities.put("async", true);
        capabilities.put("validation", true);
        capabilities.put("statusMonitoring", true);
        capabilities.put("statusDetails", true);
        capabilities.put("statusHistory", true);
        capabilities.put("statusInterpretation", true);
        capabilities.put("healthCheck", true);
        capabilities.put("usesPersistenceService", true);
        return capabilities;
    }

    @Override
    public void initialize(ActionContext context) {
        // No initialization needed
    }

    @Override
    public void cleanup() {
        // No cleanup needed
    }

    @Override
    public boolean isReady() {
        return thingRegistry != null && persistenceServiceRegistry != null;
    }

    /**
     * Interpret the thing status
     */
    private String interpretStatus(ThingStatus status) {
        switch (status) {
            case ONLINE:
                return "Thing is online and functioning normally";
            case OFFLINE:
                return "Thing is offline or not responding";
            case UNINITIALIZED:
                return "Thing is not yet initialized";
            case INITIALIZING:
                return "Thing is currently initializing";
            case REMOVING:
                return "Thing is being removed";
            case REMOVED:
                return "Thing has been removed";
            case UNKNOWN:
            default:
                return "Thing status is unknown";
        }
    }

    /**
     * Get real status history using persistence service.
     * This method queries the persistence service for thing status change events.
     */
    private List<Map<String, Object>> getRealStatusHistory(String thingUID, String persistenceService) {
        List<Map<String, Object>> history = new ArrayList<>();

        try {
            // Check if persistence services are available
            if (persistenceServiceRegistry == null) {
                logger.debug("Persistence service registry not available for thing: {}", thingUID);
                return createFallbackHistory("Persistence service registry not available");
            }

            // Get the persistence service
            PersistenceService service = persistenceServiceRegistry.get(persistenceService);
            if (service == null) {
                logger.debug("Persistence service not found: {} for thing: {}", persistenceService, thingUID);
                return createFallbackHistory("Persistence service not found: " + persistenceService);
            }

            if (!(service instanceof QueryablePersistenceService queryableService)) {
                logger.debug("Persistence service does not support queries: {} for thing: {}", persistenceService,
                        thingUID);
                return createFallbackHistory("Persistence service does not support queries: " + persistenceService);
            }

            // Create filter criteria for thing status events
            FilterCriteria filter = new FilterCriteria();

            // Query for thing status events - using a virtual item name pattern for thing status events
            String thingStatusItemName = "Thing_Status_" + thingUID.replace(":", "_");
            filter.setItemName(thingStatusItemName);

            // Query for the last 24 hours of status changes
            ZonedDateTime endTime = ZonedDateTime.now();
            ZonedDateTime startTime = endTime.minusHours(24);
            filter.setBeginDate(startTime);
            filter.setEndDate(endTime);
            filter.setOrdering(FilterCriteria.Ordering.DESCENDING);

            // Query the persistence service
            Iterable<HistoricItem> historicItems = queryableService.query(filter);

            // Process the results
            for (HistoricItem historicItem : historicItems) {
                Map<String, Object> entry = new HashMap<>();
                entry.put("timestamp", historicItem.getTimestamp().toInstant().toEpochMilli());
                entry.put("statusId",
                        "status-" + thingUID + "-" + historicItem.getTimestamp().toInstant().toEpochMilli());

                // Parse the state to determine status
                State state = historicItem.getState();
                String status = "UNKNOWN";
                String statusDetail = "Unknown";

                if (state != null) {
                    String stateStr = state.toString().toUpperCase();
                    if (stateStr.contains("ONLINE")) {
                        status = "ONLINE";
                        statusDetail = "Thing came online";
                    } else if (stateStr.contains("OFFLINE")) {
                        status = "OFFLINE";
                        statusDetail = "Thing went offline";
                    } else if (stateStr.contains("UNINITIALIZED")) {
                        status = "UNINITIALIZED";
                        statusDetail = "Thing became uninitialized";
                    } else if (stateStr.contains("INITIALIZING")) {
                        status = "INITIALIZING";
                        statusDetail = "Thing started initializing";
                    } else if (stateStr.contains("REMOVING")) {
                        status = "REMOVING";
                        statusDetail = "Thing is being removed";
                    } else if (stateStr.contains("REMOVED")) {
                        status = "REMOVED";
                        statusDetail = "Thing was removed";
                    } else if (stateStr.contains("UNKNOWN")) {
                        status = "UNKNOWN";
                        statusDetail = "Thing status became unknown";
                    }

                    // Extract additional details if available
                    if (stateStr.contains("detail=")) {
                        try {
                            String detail = stateStr.split("detail=")[1].split(",")[0];
                            statusDetail = detail;
                        } catch (Exception e) {
                            logger.debug("Could not parse status detail from state: {}", state);
                        }
                    }

                    // Extract error information if available
                    if (stateStr.contains("error=")) {
                        try {
                            String error = stateStr.split("error=")[1].split(",")[0];
                            entry.put("error", error);
                        } catch (Exception e) {
                            logger.debug("Could not parse error from state: {}", state);
                        }
                    }
                }

                entry.put("status", status);
                entry.put("statusDetail", statusDetail);
                entry.put("interpretation", interpretStatus(ThingStatus.valueOf(status)));

                history.add(entry);
            }

            // If no data found, create fallback history
            if (history.isEmpty()) {
                logger.debug("No status history found in persistence service for thing: {}", thingUID);
                return createFallbackHistory("No status history found in persistence service");
            }

        } catch (Exception e) {
            logger.warn("Error querying persistence service for thing status history: {}", e.getMessage());
            return createFallbackHistory("Error querying persistence service: " + e.getMessage());
        }

        return history;
    }

    /**
     * Create fallback history when persistence service is not available.
     */
    private List<Map<String, Object>> createFallbackHistory(String reason) {
        Map<String, Object> fallbackEntry = new HashMap<>();
        fallbackEntry.put("timestamp", System.currentTimeMillis());
        fallbackEntry.put("statusId", "fallback-status");
        fallbackEntry.put("status", "UNKNOWN");
        fallbackEntry.put("statusDetail", reason);
        fallbackEntry.put("interpretation", "Status history not available");
        fallbackEntry.put("note", "This is fallback data - persistence service integration required for real history");

        return List.of(fallbackEntry);
    }
}
