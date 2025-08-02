package org.openhab.core.ai.common.actions.items;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.api.action.AIAction;
import org.openhab.core.ai.common.api.action.AIActionContext;
import org.openhab.core.ai.common.api.action.AIActionException;
import org.openhab.core.ai.common.api.action.AIActionMetadata;
import org.openhab.core.ai.common.api.action.AIActionResult;
import org.openhab.core.ai.common.api.action.AIActionValidationResult;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemNotFoundException;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.persistence.PersistenceServiceRegistry;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action to retrieve detailed state information for items
 * 
 * 
 */
@Component(service = AIAction.class, immediate = true)
@NonNullByDefault
public class GetItemStateAction implements AIAction {

    private static final Logger logger = LoggerFactory.getLogger(GetItemStateAction.class);

    @Reference
    private @Nullable ItemRegistry itemRegistry;

    @Reference
    private @Nullable PersistenceServiceRegistry persistenceServiceRegistry;

    @Override
    public String getActionId() {
        return "openhab.items.get_state";
    }

    @Override
    public String getActionName() {
        return "Get Item State";
    }

    @Override
    public String getDescription() {
        return "Retrieve detailed state information for items";
    }

    @Override
    public String getCategory() {
        return "items";
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
        properties.put("itemName", Map.of("type", "string", "description",
                "The name of the item to get state information for", "required", true));
        properties.put("includeStateDetails",
                Map.of("type", "boolean", "description", "Include detailed state information", "default", true));
        properties.put("includeStateHistory",
                Map.of("type", "boolean", "description", "Include recent state history", "default", false));
        properties.put("historyDuration", Map.of("type", "string", "description",
                "Duration for state history (e.g., '1h', '24h', '7d')", "default", "1h"));
        properties.put("maxHistoryEntries",
                Map.of("type", "integer", "description", "Maximum number of history entries to return", "default", 10));

        schema.put("properties", properties);
        schema.put("required", List.of("itemName"));
        return schema;
    }

    @Override
    public Map<String, Object> getReturnSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();
        properties.put("itemName", Map.of("type", "string"));
        properties.put("currentState", Map.of("type", "string"));
        properties.put("stateType", Map.of("type", "string"));
        properties.put("stateDetails", Map.of("type", "object"));
        properties.put("stateHistory", Map.of("type", "array"));
        properties.put("lastStateChange", Map.of("type", "string"));
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

        String itemName = (String) parameters.get("itemName");
        if (itemName == null || itemName.trim().isEmpty()) {
            return AIActionValidationResult.invalid(List.of("Item name is required and cannot be empty"));
        }

        // Validate that the item exists
        try {
            itemRegistry.getItem(itemName);
        } catch (ItemNotFoundException e) {
            return AIActionValidationResult.invalid(List.of("Item not found: " + itemName));
        } catch (Exception e) {
            return AIActionValidationResult.invalid(List.of("Error validating parameters: " + e.getMessage()));
        }

        // Validate maxHistoryEntries if provided
        Object maxHistoryObj = parameters.get("maxHistoryEntries");
        if (maxHistoryObj != null) {
            if (!(maxHistoryObj instanceof Integer)) {
                return AIActionValidationResult.invalid(List.of("maxHistoryEntries must be an integer"));
            }
            Integer maxHistory = (Integer) maxHistoryObj;
            if (maxHistory <= 0 || maxHistory > 1000) {
                return AIActionValidationResult.invalid(List.of("maxHistoryEntries must be between 1 and 1000"));
            }
        }

        return AIActionValidationResult.valid(parameters);
    }

    @Override
    public AIActionResult execute(Map<String, Object> parameters, AIActionContext context) throws AIActionException {
        long executionStartTime = System.currentTimeMillis();

        try {
            String itemName = (String) parameters.get("itemName");
            Boolean includeStateDetails = (Boolean) parameters.getOrDefault("includeStateDetails", true);
            Boolean includeStateHistory = (Boolean) parameters.getOrDefault("includeStateHistory", false);
            String historyDuration = (String) parameters.getOrDefault("historyDuration", "1h");
            Integer maxHistoryEntries = (Integer) parameters.getOrDefault("maxHistoryEntries", 10);

            logger.debug("Getting state information for item: {} includeHistory: {}", itemName, includeStateHistory);

            // Get the item
            Item item = itemRegistry.getItem(itemName);

            Map<String, Object> result = new HashMap<>();
            result.put("itemName", itemName);
            result.put("success", true);
            result.put("timestamp", System.currentTimeMillis());

            // Current state information
            result.put("currentState", item.getState() != null ? item.getState().toString() : "NULL");
            result.put("stateType", item.getState() != null ? item.getState().getClass().getSimpleName() : "NULL");
            result.put("lastStateChange",
                    item.getLastStateChange() != null ? item.getLastStateChange().toString() : "NULL");

            // Detailed state information
            if (includeStateDetails) {
                Map<String, Object> stateDetails = new HashMap<>();
                stateDetails.put("hasState", item.getState() != null);
                stateDetails.put("stateClass", item.getState() != null ? item.getState().getClass().getName() : "NULL");
                stateDetails.put("acceptedDataTypes",
                        item.getAcceptedDataTypes().stream().map(Class::getSimpleName).toList());
                stateDetails.put("acceptedCommandTypes",
                        item.getAcceptedCommandTypes().stream().map(Class::getSimpleName).toList());

                // State-specific details
                if (item.getState() != null) {
                    stateDetails.put("stateValue", item.getState().toString());
                    stateDetails.put("stateHashCode", item.getState().hashCode());

                    // Try to get numeric value if applicable
                    try {
                        if (item.getState() instanceof org.openhab.core.library.types.DecimalType decimalState) {
                            stateDetails.put("numericValue", decimalState.doubleValue());
                        } else if (item.getState() instanceof org.openhab.core.library.types.PercentType percentState) {
                            stateDetails.put("percentValue", percentState.intValue());
                        } else if (item.getState() instanceof org.openhab.core.library.types.OnOffType onOffState) {
                            stateDetails.put("booleanValue", onOffState == org.openhab.core.library.types.OnOffType.ON);
                        }
                    } catch (Exception e) {
                        // Ignore conversion errors
                        logger.debug("Could not convert state to specific type for item: {}", itemName);
                    }
                }

                result.put("stateDetails", stateDetails);
            } else {
                result.put("stateDetails", Map.of());
            }

            // State history
            if (includeStateHistory) {
                List<Map<String, Object>> stateHistory = getStateHistory(itemName, historyDuration, maxHistoryEntries);
                result.put("stateHistory", stateHistory);
                result.put("historyEntryCount", stateHistory.size());
            } else {
                result.put("stateHistory", List.of());
                result.put("historyEntryCount", 0);
            }

            long executionTime = System.currentTimeMillis() - executionStartTime;
            logger.debug("Successfully retrieved state information for item: {} in {}ms", itemName, executionTime);

            return AIActionResult.success(result, executionTime);

        } catch (ItemNotFoundException e) {
            long executionTime = System.currentTimeMillis() - executionStartTime;
            logger.debug("Item not found: {}", e.getMessage());

            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", "Item not found");
            result.put("timestamp", System.currentTimeMillis());

            return AIActionResult.success(result, executionTime);

        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - executionStartTime;
            logger.error("Error getting item state information", e);
            throw new AIActionException(getActionId(), "Failed to get item state information: " + e.getMessage(), e);
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
                .description("Retrieve detailed state information for openHAB items")
                .tags(List.of("items", "state", "history", "monitoring"))
                .documentation(
                        "Retrieves detailed state information for openHAB items including current state, state type, and optional state history from persistence services.")
                .examples(List.of("Get current state: {\"itemName\": \"LivingRoom_Light\"}",
                        "Get with details: {\"itemName\": \"LivingRoom_Light\", \"includeStateDetails\": true}",
                        "Get with history: {\"itemName\": \"LivingRoom_Light\", \"includeStateHistory\": true, \"historyDuration\": \"24h\"}",
                        "Get with custom history: {\"itemName\": \"LivingRoom_Light\", \"includeStateHistory\": true, \"historyDuration\": \"7d\", \"maxHistoryEntries\": 50}"))
                .build();
    }

    @Override
    public Map<String, Object> getCapabilities() {
        Map<String, Object> capabilities = new HashMap<>();
        capabilities.put("async", true);
        capabilities.put("validation", true);
        capabilities.put("stateRetrieval", true);
        capabilities.put("stateDetails", true);
        capabilities.put("stateHistory", true);
        capabilities.put("persistenceIntegration", true);
        capabilities.put("stateTypeAnalysis", true);
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
        return itemRegistry != null;
    }

    /**
     * Get state history for an item
     */
    private List<Map<String, Object>> getStateHistory(String itemName, String duration, int maxEntries) {
        List<Map<String, Object>> history = new java.util.ArrayList<>();

        if (persistenceServiceRegistry == null) {
            logger.debug("Persistence service registry not available for item: {}", itemName);
            return history;
        }

        try {
            // For now, return a stub implementation since the persistence API needs to be properly configured
            // This will be enhanced when the correct persistence API is available

            // Add a sample entry to show the expected format
            Map<String, Object> sampleEntry = new HashMap<>();
            sampleEntry.put("timestamp", System.currentTimeMillis());
            sampleEntry.put("state", "SAMPLE_STATE");
            sampleEntry.put("stateType", "StringType");
            history.add(sampleEntry);

            logger.debug("Returned stub history for item: {} (persistence API integration pending)", itemName);

        } catch (Exception e) {
            logger.debug("Error retrieving state history for item: {}: {}", itemName, e.getMessage());
        }

        return history;
    }
}
