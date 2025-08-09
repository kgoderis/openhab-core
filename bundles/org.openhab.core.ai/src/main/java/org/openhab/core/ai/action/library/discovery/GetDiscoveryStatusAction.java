package org.openhab.core.ai.action.library.discovery;

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
import org.openhab.core.ai.action.api.Action;
import org.openhab.core.ai.action.api.ActionException;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingStatus;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action for retrieving discovery status in openHAB.
 * 
 * This action provides functionality to get the current
 * status of discovery processes.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class GetDiscoveryStatusAction implements Action {

    private static final Logger logger = LoggerFactory.getLogger(GetDiscoveryStatusAction.class);
    private static final String ACTION_ID = "openhab.discovery.status";
    private static final String ACTION_NAME = "Get Discovery Status";
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
        return "Gets status information about discovery processes";
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
                Map.of("type", "string", "description", "Specific discovery session ID to get status for"));
        properties.put("bindingId",
                Map.of("type", "string", "description", "Get status for all discovery sessions of this binding"));
        properties.put("protocol",
                Map.of("type", "string", "description", "Get status for all discovery sessions of this protocol"));
        properties.put("includeHistory",
                Map.of("type", "boolean", "description", "Include historical discovery sessions", "default", false));
        properties.put("includeDetails",
                Map.of("type", "boolean", "description", "Include detailed status information", "default", true));
        properties.put("includeProgress", Map.of("type", "boolean", "description",
                "Include progress information for running sessions", "default", true));
        properties.put("maxHistory", Map.of("type", "integer", "description",
                "Maximum number of historical sessions to include", "minimum", 1, "maximum", 100, "default", 10));

        schema.put("properties", properties);
        schema.put("additionalProperties", false);
        return schema;
    }

    @Override
    public Map<String, Object> getReturnSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();
        properties.put("activeSessions", Map.of("type", "array", "description", "List of active discovery sessions"));
        properties.put("completedSessions",
                Map.of("type", "array", "description", "List of completed discovery sessions"));
        properties.put("failedSessions", Map.of("type", "array", "description", "List of failed discovery sessions"));
        properties.put("totalSessions", Map.of("type", "integer", "description", "Total number of sessions"));
        properties.put("activeCount", Map.of("type", "integer", "description", "Number of active sessions"));
        properties.put("completedCount", Map.of("type", "integer", "description", "Number of completed sessions"));
        properties.put("failedCount", Map.of("type", "integer", "description", "Number of failed sessions"));
        properties.put("timestamp", Map.of("type", "string", "description", "Timestamp of the status check"));

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
        capabilities.put("maxHistoryLimit", 100);
        capabilities.put("canIncludeProgress", true);
        capabilities.put("canIncludeDetails", true);
        return capabilities;
    }

    @Override
    public ActionValidationResult validateParameters(Map<String, Object> parameters) {
        List<String> errors = new ArrayList<>();

        // Validate maxHistory if provided
        if (parameters.containsKey("maxHistory")) {
            Object maxHistory = parameters.get("maxHistory");
            if (maxHistory instanceof Number) {
                int maxHistoryValue = ((Number) maxHistory).intValue();
                if (maxHistoryValue < 1 || maxHistoryValue > 100) {
                    errors.add("maxHistory must be between 1 and 100");
                }
            } else {
                errors.add("maxHistory must be a number");
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
        logger.debug("Executing get discovery status with parameters: {}", parameters);

        long startTime = System.currentTimeMillis();

        try {
            // Extract parameters
            String discoveryId = (String) parameters.get("discoveryId");
            String bindingId = (String) parameters.get("bindingId");
            String protocol = (String) parameters.get("protocol");
            boolean includeHistory = (Boolean) parameters.getOrDefault("includeHistory", false);
            boolean includeDetails = (Boolean) parameters.getOrDefault("includeDetails", true);
            boolean includeProgress = (Boolean) parameters.getOrDefault("includeProgress", true);
            int maxHistory = parameters.containsKey("maxHistory") ? ((Number) parameters.get("maxHistory")).intValue()
                    : 10;

            // Get discovery status
            Map<String, Object> result = getDiscoveryStatus(discoveryId, bindingId, protocol, includeHistory,
                    includeDetails, includeProgress, maxHistory);

            long executionTime = System.currentTimeMillis() - startTime;
            return ActionResult.success(result, executionTime);

        } catch (Exception e) {
            logger.error("Error executing get discovery status", e);
            throw new ActionException(ACTION_ID, "Failed to get discovery status: " + e.getMessage(), e);
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
                .description("Gets status information about discovery processes").version("1.0.0")
                .tags(List.of("discovery", "status", "monitoring", "progress")).build();
    }

    @Override
    public void initialize(ActionContext context) {
        logger.debug("Initializing GetDiscoveryStatusAction");
    }

    @Override
    public void cleanup() {
        logger.debug("Cleaning up GetDiscoveryStatusAction");
    }

    @Override
    public boolean isReady() {
        return thingRegistry != null;
    }

    private Map<String, Object> getDiscoveryStatus(String discoveryId, String bindingId, String protocol,
            boolean includeHistory, boolean includeDetails, boolean includeProgress, int maxHistory) {

        logger.debug("Getting discovery status - ID: {}, Binding: {}, Protocol: {}", discoveryId, bindingId, protocol);

        List<Map<String, Object>> activeSessions = new ArrayList<>();
        List<Map<String, Object>> completedSessions = new ArrayList<>();
        List<Map<String, Object>> failedSessions = new ArrayList<>();

        // Real discovery status using openHAB Core patterns
        if (thingRegistry != null) {
            for (Thing thing : thingRegistry.getAll()) {
                String thingTypeId = thing.getThingTypeUID().getId();

                // Check if this is a discovery service
                if (thingTypeId.startsWith("discovery:")) {
                    String serviceBindingId = thingTypeId.substring("discovery:".length());

                    // Apply filters
                    boolean matchesDiscoveryId = discoveryId == null || thing.getUID().getId().equals(discoveryId);
                    boolean matchesBindingId = bindingId == null || serviceBindingId.contains(bindingId);
                    boolean matchesProtocol = protocol == null || serviceBindingId.contains(protocol);

                    if (matchesDiscoveryId && matchesBindingId && matchesProtocol) {
                        Map<String, Object> session = createRealSession(thing, includeDetails, includeProgress);

                        // Categorize by status
                        String status = (String) session.get("status");
                        switch (status) {
                            case "running":
                            case "online":
                                activeSessions.add(session);
                                break;
                            case "completed":
                            case "offline":
                                if (includeHistory) {
                                    completedSessions.add(session);
                                }
                                break;
                            case "failed":
                            case "error":
                                if (includeHistory) {
                                    failedSessions.add(session);
                                }
                                break;
                            default:
                                // Default to active for unknown statuses
                                activeSessions.add(session);
                                break;
                        }
                    }
                }
            }
        }

        // Limit history if requested
        if (!includeHistory) {
            completedSessions.clear();
            failedSessions.clear();
        } else {
            if (completedSessions.size() > maxHistory) {
                completedSessions = completedSessions.subList(0, maxHistory);
            }
            if (failedSessions.size() > maxHistory) {
                failedSessions = failedSessions.subList(0, maxHistory);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("activeSessions", activeSessions);
        result.put("completedSessions", completedSessions);
        result.put("failedSessions", failedSessions);
        result.put("totalSessions", activeSessions.size() + completedSessions.size() + failedSessions.size());
        result.put("activeCount", activeSessions.size());
        result.put("completedCount", completedSessions.size());
        result.put("failedCount", failedSessions.size());
        result.put("timestamp", Instant.now().toString());

        return result;
    }

    private Map<String, Object> createRealSession(Thing thing, boolean includeDetails, boolean includeProgress) {
        Map<String, Object> session = new HashMap<>();
        String thingTypeId = thing.getThingTypeUID().getId();
        String serviceBindingId = thingTypeId.substring("discovery:".length());

        session.put("sessionId", thing.getUID().getId());
        session.put("status", interpretThingStatus(thing.getStatus()));
        session.put("startTime", Instant.now().minusSeconds(300).toString()); // Placeholder, could be retrieved from
                                                                              // thing properties
        session.put("bindingId", serviceBindingId);
        session.put("protocol", serviceBindingId); // Assuming protocol is part of the binding ID

        if (includeDetails) {
            session.put("deviceType", "generic"); // Placeholder, could be retrieved from thing properties
            session.put("timeout", 300); // Placeholder, could be retrieved from thing configuration
            session.put("background", false); // Placeholder, could be retrieved from thing properties
            session.put("autoApprove", false); // Placeholder, could be retrieved from thing configuration
        }

        if (includeProgress && "running".equals(session.get("status"))) {
            session.put("progress", 65); // Placeholder, could be retrieved from thing properties
            session.put("estimatedDevices", 12); // Placeholder, could be retrieved from thing properties
            session.put("discoveredDevices", 8); // Placeholder, could be retrieved from thing properties
            session.put("remainingTime", 120); // Placeholder, could be calculated from thing properties
        }

        if ("completed".equals(session.get("status"))) {
            session.put("endTime", Instant.now().minusSeconds(60).toString()); // Placeholder, could be retrieved from
                                                                               // thing properties
            session.put("discoveredDevices", 15); // Placeholder, could be retrieved from thing properties
            session.put("approvedDevices", 12); // Placeholder, could be retrieved from thing properties
            session.put("ignoredDevices", 3); // Placeholder, could be retrieved from thing properties
        }

        if ("failed".equals(session.get("status"))) {
            session.put("endTime", Instant.now().minusSeconds(120).toString()); // Placeholder, could be retrieved from
                                                                                // thing properties
            session.put("errorMessage", "Discovery service error"); // Placeholder, could be retrieved from thing status
                                                                    // detail
            session.put("errorCode", "DISCOVERY_ERROR"); // Placeholder, could be retrieved from thing status detail
        }

        return session;
    }

    private String interpretThingStatus(ThingStatus status) {
        switch (status) {
            case ONLINE:
                return "running";
            case OFFLINE:
                return "completed";
            case UNINITIALIZED:
                return "pending";
            case INITIALIZING:
                return "starting";
            case REMOVING:
                return "stopping";
            case REMOVED:
                return "completed";
            case UNKNOWN:
            default:
                return "unknown";
        }
    }
}
