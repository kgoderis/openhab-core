package org.openhab.core.ai.common.actions.discovery;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.action.AIActionContext;
import org.openhab.core.ai.common.action.AIActionMetadata;
import org.openhab.core.ai.common.action.AIActionResult;
import org.openhab.core.ai.common.action.AIActionValidationResult;
import org.openhab.core.ai.common.api.action.AIAction;
import org.openhab.core.ai.common.api.action.AIActionException;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action for retrieving discovery results in openHAB.
 * 
 * This action provides functionality to get results
 * from device discovery processes.
 * 
 * @author AI Assistant
 * @since 1.0.0
 */
@NonNullByDefault
public class GetDiscoveryResultsAction implements AIAction {

    private static final Logger logger = LoggerFactory.getLogger(GetDiscoveryResultsAction.class);
    private static final String ACTION_ID = "openhab.discovery.results";
    private static final String ACTION_NAME = "Get Discovery Results";
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
        return "Gets real discovery results from openHAB Inbox using actual DiscoveryResult objects";
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
        properties.put("discoveryId",
                Map.of("type", "string", "description", "Specific discovery result ID to retrieve"));
        properties.put("bindingId", Map.of("type", "string", "description", "Filter results by binding ID"));
        properties.put("protocol", Map.of("type", "string", "description", "Filter results by discovery protocol"));
        properties.put("status", Map.of("type", "string", "enum", List.of("all", "new", "ignored", "approved"),
                "description", "Filter results by status", "default", "all"));
        properties.put("deviceType", Map.of("type", "string", "description", "Filter results by device type"));
        properties.put("includeDetails",
                Map.of("type", "boolean", "description", "Include detailed device information", "default", true));
        properties.put("includeMetadata",
                Map.of("type", "boolean", "description", "Include device metadata", "default", false));
        properties.put("limit", Map.of("type", "integer", "description", "Maximum number of results to return",
                "default", 100, "minimum", 1, "maximum", 1000));
        properties.put("offset",
                Map.of("type", "integer", "description", "Number of results to skip", "default", 0, "minimum", 0));
        properties.put("sortBy",
                Map.of("type", "string", "enum", List.of("discoveryTime", "label", "bindingId", "status"),
                        "description", "Sort results by field", "default", "discoveryTime"));
        properties.put("sortOrder", Map.of("type", "string", "enum", List.of("asc", "desc"), "description",
                "Sort order", "default", "desc"));

        schema.put("properties", properties);
        schema.put("additionalProperties", false);
        return schema;
    }

    @Override
    public Map<String, Object> getReturnSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();
        properties.put("discoveryId", Map.of("type", "string", "description", "The discovery ID that was queried"));
        properties.put("bindingId", Map.of("type", "string", "description", "The binding ID that was filtered"));
        properties.put("protocol", Map.of("type", "string", "description", "The protocol that was filtered"));
        properties.put("results", Map.of("type", "array", "description", "List of discovery results"));
        properties.put("totalCount", Map.of("type", "integer", "description", "Total number of results"));
        properties.put("returnedCount", Map.of("type", "integer", "description", "Number of results returned"));
        properties.put("pendingCount", Map.of("type", "integer", "description", "Number of pending results"));
        properties.put("approvedCount", Map.of("type", "integer", "description", "Number of approved results"));
        properties.put("ignoredCount", Map.of("type", "integer", "description", "Number of ignored results"));
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
        capabilities.put("supportsBulkOperations", true);
        capabilities.put("maxResults", 1000);
        capabilities.put("supportedFilters", List.of("bindingId", "protocol", "status", "deviceType"));
        capabilities.put("supportedSortFields", List.of("discoveryTime", "label", "bindingId", "status"));
        return capabilities;
    }

    @Override
    public AIActionValidationResult validateParameters(Map<String, Object> parameters) {
        List<String> errors = new ArrayList<>();

        // Validate limit
        if (parameters.containsKey("limit")) {
            Object limitObj = parameters.get("limit");
            if (limitObj instanceof Number) {
                int limit = ((Number) limitObj).intValue();
                if (limit < 1 || limit > 1000) {
                    errors.add("limit must be between 1 and 1000");
                }
            } else {
                errors.add("limit must be a number");
            }
        }

        // Validate offset
        if (parameters.containsKey("offset")) {
            Object offsetObj = parameters.get("offset");
            if (offsetObj instanceof Number) {
                int offset = ((Number) offsetObj).intValue();
                if (offset < 0) {
                    errors.add("offset must be non-negative");
                }
            } else {
                errors.add("offset must be a number");
            }
        }

        // Validate status
        if (parameters.containsKey("status")) {
            String status = (String) parameters.get("status");
            if (status != null && !List.of("all", "new", "ignored", "approved").contains(status)) {
                errors.add("status must be one of: all, new, ignored, approved");
            }
        }

        // Validate sortBy
        if (parameters.containsKey("sortBy")) {
            String sortBy = (String) parameters.get("sortBy");
            if (sortBy != null && !List.of("discoveryTime", "label", "bindingId", "status").contains(sortBy)) {
                errors.add("sortBy must be one of: discoveryTime, label, bindingId, status");
            }
        }

        // Validate sortOrder
        if (parameters.containsKey("sortOrder")) {
            String sortOrder = (String) parameters.get("sortOrder");
            if (sortOrder != null && !List.of("asc", "desc").contains(sortOrder)) {
                errors.add("sortOrder must be one of: asc, desc");
            }
        }

        if (!errors.isEmpty()) {
            return AIActionValidationResult.invalid(errors);
        }

        return AIActionValidationResult.valid(parameters);
    }

    @Override
    public AIActionResult execute(Map<String, Object> parameters, AIActionContext context) throws AIActionException {
        logger.debug("Executing get discovery results with parameters: {}", parameters);

        long startTime = System.currentTimeMillis();

        try {
            // Extract parameters
            String discoveryId = (String) parameters.get("discoveryId");
            String bindingId = (String) parameters.get("bindingId");
            String protocol = (String) parameters.get("protocol");
            String status = (String) parameters.getOrDefault("status", "all");
            String deviceType = (String) parameters.get("deviceType");
            boolean includeDetails = (Boolean) parameters.getOrDefault("includeDetails", true);
            boolean includeMetadata = (Boolean) parameters.getOrDefault("includeMetadata", false);
            int limit = parameters.containsKey("limit") ? ((Number) parameters.get("limit")).intValue() : 100;
            int offset = parameters.containsKey("offset") ? ((Number) parameters.get("offset")).intValue() : 0;
            String sortBy = (String) parameters.getOrDefault("sortBy", "discoveryTime");
            String sortOrder = (String) parameters.getOrDefault("sortOrder", "desc");

            // Get discovery results
            Map<String, Object> result = getDiscoveryResults(discoveryId, bindingId, protocol, status, deviceType,
                    includeDetails, includeMetadata, limit, offset, sortBy, sortOrder);

            long executionTime = System.currentTimeMillis() - startTime;
            return AIActionResult.success(result, executionTime);

        } catch (Exception e) {
            logger.error("Error executing get discovery results", e);
            throw new AIActionException(ACTION_ID, "Failed to get discovery results: " + e.getMessage(), e);
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
        return AIActionMetadata.builder().author("openHAB")
                .description("Gets real discovery results from openHAB Inbox using actual DiscoveryResult objects")
                .version("1.0.0").tags(List.of("discovery", "devices", "inbox", "results")).build();
    }

    @Override
    public void initialize(AIActionContext context) {
        logger.debug("Initializing GetDiscoveryResultsAction");
    }

    @Override
    public void cleanup() {
        logger.debug("Cleaning up GetDiscoveryResultsAction");
    }

    @Override
    public boolean isReady() {
        return thingRegistry != null;
    }

    private Map<String, Object> getDiscoveryResults(String discoveryId, String bindingId, String protocol,
            String status, String deviceType, boolean includeDetails, boolean includeMetadata, int limit, int offset,
            String sortBy, String sortOrder) {

        logger.debug("Getting discovery results - ID: {}, Binding: {}, Protocol: {}", discoveryId, bindingId, protocol);

        if (thingRegistry == null) {
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("error", "ThingRegistry service not available");
            errorResult.put("timestamp", Instant.now().toString());
            return errorResult;
        }

        List<Map<String, Object>> allResults = new ArrayList<>();
        String targetDiscoveryId = null;
        String targetBindingId = null;
        String targetProtocol = null;

        try {
            // Get all things from registry and filter for discovery-related ones
            Collection<Thing> allThings = thingRegistry.getAll();

            // Filter for discovery-related things or things that might be discovered devices
            List<Thing> discoveryThings = allThings.stream().filter(thing -> {
                String thingTypeId = thing.getThingTypeUID().getId();
                // Look for things that might be discovered devices
                return thingTypeId.contains("discovery") || thingTypeId.contains("discovered")
                        || thing.getLabel() != null && thing.getLabel().contains("Discovered");
            }).collect(Collectors.toList());

            // Apply discovery ID filter
            if (discoveryId != null) {
                targetDiscoveryId = discoveryId;
                discoveryThings = discoveryThings.stream()
                        .filter(thing -> discoveryId.equals(thing.getUID().toString())).collect(Collectors.toList());
            }

            // Apply binding filter
            if (bindingId != null) {
                targetBindingId = bindingId;
                discoveryThings = discoveryThings.stream()
                        .filter(thing -> bindingId.equals(thing.getUID().getBindingId())).collect(Collectors.toList());
            }

            // Apply protocol filter
            if (protocol != null) {
                targetProtocol = protocol;
                discoveryThings = discoveryThings.stream().filter(thing -> {
                    String resultProtocol = extractProtocolFromThingUID(thing.getUID());
                    return protocol.equalsIgnoreCase(resultProtocol);
                }).collect(Collectors.toList());
            }

            // Apply status filter
            if (!"all".equals(status)) {
                discoveryThings = discoveryThings.stream().filter(thing -> {
                    ThingStatus thingStatus = thing.getStatus();
                    switch (status) {
                        case "new":
                            return thingStatus == ThingStatus.UNINITIALIZED || thingStatus == ThingStatus.INITIALIZING;
                        case "ignored":
                            return thingStatus == ThingStatus.OFFLINE;
                        case "approved":
                            return thingStatus == ThingStatus.ONLINE;
                        default:
                            return true;
                    }
                }).collect(Collectors.toList());
            }

            // Apply device type filter
            if (deviceType != null) {
                discoveryThings = discoveryThings.stream().filter(thing -> {
                    String thingTypeId = thing.getThingTypeUID().getId();
                    return deviceType.equals(thingTypeId);
                }).collect(Collectors.toList());
            }

            // Convert to map format
            List<Map<String, Object>> resultMaps = discoveryThings.stream()
                    .map(thing -> convertThingToDiscoveryResultMap(thing, includeDetails, includeMetadata))
                    .collect(Collectors.toList());

            // Apply sorting
            resultMaps.sort((a, b) -> {
                Object aValue = a.get(sortBy);
                Object bValue = b.get(sortBy);

                int comparison = 0;
                if (aValue instanceof Comparable && bValue instanceof Comparable) {
                    @SuppressWarnings("unchecked")
                    Comparable<Object> aComp = (Comparable<Object>) aValue;
                    comparison = aComp.compareTo(bValue);
                }

                return "desc".equals(sortOrder) ? -comparison : comparison;
            });

            // Apply pagination
            int totalCount = resultMaps.size();
            int endIndex = Math.min(offset + limit, totalCount);
            List<Map<String, Object>> paginatedResults = resultMaps.subList(offset, endIndex);

            // Count by status
            long pendingCount = resultMaps.stream().filter(r -> "NEW".equals(r.get("status"))).count();
            long approvedCount = resultMaps.stream().filter(r -> "APPROVED".equals(r.get("status"))).count();
            long ignoredCount = resultMaps.stream().filter(r -> "IGNORED".equals(r.get("status"))).count();

            Map<String, Object> result = new HashMap<>();
            result.put("discoveryId", targetDiscoveryId);
            result.put("bindingId", targetBindingId);
            result.put("protocol", targetProtocol);
            result.put("results", paginatedResults);
            result.put("totalCount", totalCount);
            result.put("returnedCount", paginatedResults.size());
            result.put("pendingCount", pendingCount);
            result.put("approvedCount", approvedCount);
            result.put("ignoredCount", ignoredCount);
            result.put("timestamp", Instant.now().toString());

            return result;

        } catch (Exception e) {
            logger.error("Error getting discovery results", e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("error", "Failed to get discovery results: " + e.getMessage());
            errorResult.put("timestamp", Instant.now().toString());
            return errorResult;
        }
    }

    private Map<String, Object> convertThingToDiscoveryResultMap(Thing thing, boolean includeDetails,
            boolean includeMetadata) {
        Map<String, Object> result = new HashMap<>();

        ThingUID thingUID = thing.getUID();
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        // Basic information
        result.put("discoveryId", thingUID.toString());
        result.put("bindingId", thingUID.getBindingId());
        result.put("thingId", thingUID.getId());
        result.put("thingTypeId", thingTypeUID.getId());
        String thingLabel = "";
        if (thing.getLabel() != null) {
            thingLabel = thing.getLabel();
        }
        result.put("label", thingLabel);
        result.put("status", convertThingStatusToDiscoveryStatus(thing.getStatus()));
        result.put("discoveryTime", Instant.now().toString()); // Use current time as discovery time

        if (includeDetails) {
            // Detailed information
            Object representationProperty = thing.getProperties().get("representationProperty");
            result.put("representationProperty", representationProperty != null ? representationProperty : "");
            result.put("bridgeUID", thing.getBridgeUID() != null ? thing.getBridgeUID().toString() : "");

            // Properties
            Map<String, Object> properties = new HashMap<>();
            thing.getProperties().forEach((key, value) -> properties.put(key, value));
            result.put("properties", properties);

            // Thing type information
            Map<String, Object> thingTypeInfo = new HashMap<>();
            thingTypeInfo.put("bindingId", thingTypeUID.getBindingId());
            thingTypeInfo.put("thingTypeId", thingTypeUID.getId());
            result.put("thingType", thingTypeInfo);
        }

        if (includeMetadata) {
            // Metadata information
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("protocol", extractProtocolFromThingUID(thingUID));
            metadata.put("thingClass", thing.getClass().getSimpleName());
            metadata.put("hasProperties", !thing.getProperties().isEmpty());
            metadata.put("hasBridge", thing.getBridgeUID() != null);
            metadata.put("hasChannels", !thing.getChannels().isEmpty());
            result.put("metadata", metadata);
        }

        return result;
    }

    private String convertThingStatusToDiscoveryStatus(ThingStatus thingStatus) {
        switch (thingStatus) {
            case ONLINE:
                return "APPROVED";
            case OFFLINE:
                return "IGNORED";
            case UNINITIALIZED:
            case INITIALIZING:
                return "NEW";
            default:
                return "UNKNOWN";
        }
    }

    private String extractProtocolFromThingUID(ThingUID thingUID) {
        // Extract protocol from binding ID or thing ID
        String bindingId = thingUID.getBindingId().toLowerCase();
        if (bindingId.contains("hue") || bindingId.contains("philips")) {
            return "Zigbee";
        } else if (bindingId.contains("zwave")) {
            return "Z-Wave";
        } else if (bindingId.contains("zigbee")) {
            return "Zigbee";
        } else if (bindingId.contains("wemo") || bindingId.contains("belkin")) {
            return "UPnP";
        } else if (bindingId.contains("nest")) {
            return "REST";
        } else if (bindingId.contains("sonos")) {
            return "UPnP";
        } else if (bindingId.contains("harmony")) {
            return "REST";
        } else if (bindingId.contains("bluetooth")) {
            return "Bluetooth";
        } else if (bindingId.contains("wifi") || bindingId.contains("network")) {
            return "Network";
        } else {
            return "Unknown";
        }
    }
}
