package org.openhab.core.ai.action.library.items;

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
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemNotFoundException;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingStatus;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action for validating items in openHAB.
 * 
 * This action provides functionality to validate
 * item configurations and properties.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ValidateItemAction implements Action {

    private static final Logger logger = LoggerFactory.getLogger(ValidateItemAction.class);

    @Reference
    private @Nullable ItemRegistry itemRegistry;

    @Reference
    private @Nullable ThingRegistry thingRegistry;

    @Override
    public String getActionId() {
        return "openhab.items.validate";
    }

    @Override
    public String getActionName() {
        return "Validate Item";
    }

    @Override
    public String getDescription() {
        return "Validate items and their configuration";
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
        properties.put("itemName",
                Map.of("type", "string", "description", "The name of the item to validate", "required", true));
        properties.put("validateBinding",
                Map.of("type", "boolean", "description", "Validate binding status and configuration", "default", true));
        properties.put("validateConfiguration",
                Map.of("type", "boolean", "description", "Validate item configuration", "default", true));
        properties.put("validateState",
                Map.of("type", "boolean", "description", "Validate current item state", "default", true));
        properties.put("includeRecommendations",
                Map.of("type", "boolean", "description", "Include improvement recommendations", "default", true));

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
        properties.put("isValid", Map.of("type", "boolean"));
        properties.put("validationResults", Map.of("type", "object"));
        properties.put("issues", Map.of("type", "array"));
        properties.put("recommendations", Map.of("type", "array"));
        properties.put("overallScore", Map.of("type", "number"));
        properties.put("success", Map.of("type", "boolean"));
        properties.put("timestamp", Map.of("type", "number"));

        schema.put("properties", properties);
        return schema;
    }

    @Override
    public ActionValidationResult validateParameters(Map<String, Object> parameters) {
        if (parameters == null) {
            return ActionValidationResult.invalid(List.of("Parameters cannot be null"));
        }

        String itemName = (String) parameters.get("itemName");
        if (itemName == null || itemName.trim().isEmpty()) {
            return ActionValidationResult.invalid(List.of("Item name is required and cannot be empty"));
        }

        // Validate that the item exists
        try {
            itemRegistry.getItem(itemName);
        } catch (ItemNotFoundException e) {
            return ActionValidationResult.invalid(List.of("Item not found: " + itemName));
        } catch (Exception e) {
            return ActionValidationResult.invalid(List.of("Error validating parameters: " + e.getMessage()));
        }

        return ActionValidationResult.valid(parameters);
    }

    @Override
    public ActionResult execute(Map<String, Object> parameters, ActionContext context) throws ActionException {
        long executionStartTime = System.currentTimeMillis();

        try {
            String itemName = (String) parameters.get("itemName");
            Boolean validateBinding = (Boolean) parameters.getOrDefault("validateBinding", true);
            Boolean validateConfiguration = (Boolean) parameters.getOrDefault("validateConfiguration", true);
            Boolean validateState = (Boolean) parameters.getOrDefault("validateState", true);
            Boolean includeRecommendations = (Boolean) parameters.getOrDefault("includeRecommendations", true);

            logger.debug("Validating item: {} validateBinding: {}", itemName, validateBinding);

            // Get the item
            Item item = itemRegistry.getItem(itemName);

            Map<String, Object> result = new HashMap<>();
            result.put("itemName", itemName);
            result.put("success", true);
            result.put("timestamp", System.currentTimeMillis());

            Map<String, Object> validationResults = new HashMap<>();
            List<String> issues = new java.util.ArrayList<>();
            List<String> recommendations = new java.util.ArrayList<>();
            int overallScore = 100; // Start with perfect score

            // Basic item validation
            validationResults.put("itemExists", true);
            validationResults.put("itemType", item.getType());
            validationResults.put("hasLabel", item.getLabel() != null && !item.getLabel().trim().isEmpty());
            validationResults.put("hasCategory", item.getCategory() != null && !item.getCategory().trim().isEmpty());

            if (item.getLabel() == null || item.getLabel().trim().isEmpty()) {
                issues.add("Item has no label");
                overallScore -= 10;
            }

            if (item.getCategory() == null || item.getCategory().trim().isEmpty()) {
                issues.add("Item has no category");
                overallScore -= 5;
            }

            // State validation
            if (validateState) {
                Map<String, Object> stateValidation = new HashMap<>();
                stateValidation.put("hasState", item.getState() != null);
                stateValidation.put("stateType",
                        item.getState() != null ? item.getState().getClass().getSimpleName() : "NULL");
                stateValidation.put("lastStateChange",
                        item.getLastStateChange() != null ? item.getLastStateChange().toString() : "NULL");

                if (item.getState() == null) {
                    issues.add("Item has no current state");
                    overallScore -= 15;
                }

                validationResults.put("stateValidation", stateValidation);
            }

            // Binding validation
            if (validateBinding && thingRegistry != null) {
                Map<String, Object> bindingValidation = new HashMap<>();

                // Find the thing that provides this item
                Thing thing = null;
                Channel channel = null;

                for (Thing t : thingRegistry.getAll()) {
                    for (Channel c : t.getChannels()) {
                        if (itemName.equals(c.getProperties().get("item"))) {
                            thing = t;
                            channel = c;
                            break;
                        }
                    }
                    if (thing != null) {
                        break;
                    }
                }

                if (thing != null && channel != null) {
                    bindingValidation.put("hasBinding", true);
                    bindingValidation.put("bindingId", thing.getThingTypeUID().getBindingId());
                    bindingValidation.put("thingStatus", thing.getStatus().toString());
                    bindingValidation.put("channelId", channel.getUID().getId());

                    // Check thing status
                    if (thing.getStatus() != ThingStatus.ONLINE) {
                        issues.add("Thing is not online: " + thing.getStatus());
                        overallScore -= 20;
                    }

                    // Check channel configuration
                    if (channel.getConfiguration().getProperties().isEmpty()) {
                        issues.add("Channel has no configuration");
                        overallScore -= 5;
                    }
                } else {
                    bindingValidation.put("hasBinding", false);
                    issues.add("Item is not bound to any thing");
                    overallScore -= 25;
                }

                validationResults.put("bindingValidation", bindingValidation);
            }

            // Configuration validation
            if (validateConfiguration) {
                Map<String, Object> configValidation = new HashMap<>();

                // Check for common configuration issues
                boolean hasValidName = itemName.matches("^[a-zA-Z0-9_]+$");
                configValidation.put("hasValidName", hasValidName);

                if (!hasValidName) {
                    issues.add("Item name contains invalid characters (use only letters, numbers, and underscores)");
                    overallScore -= 10;
                }

                // Check for naming conventions
                if (!itemName.contains("_")) {
                    recommendations.add(
                            "Consider using underscores in item name for better organization (e.g., LivingRoom_Light)");
                }

                validationResults.put("configurationValidation", configValidation);
            }

            // Generate recommendations
            if (includeRecommendations) {
                if (item.getLabel() == null || item.getLabel().trim().isEmpty()) {
                    recommendations.add("Add a descriptive label to the item");
                }

                if (item.getCategory() == null || item.getCategory().trim().isEmpty()) {
                    recommendations.add("Add a category to help organize the item");
                }

                if (thingRegistry != null) {
                    boolean hasBinding = false;
                    for (Thing t : thingRegistry.getAll()) {
                        for (Channel c : t.getChannels()) {
                            if (itemName.equals(c.getProperties().get("item"))) {
                                hasBinding = true;
                                break;
                            }
                        }
                        if (hasBinding)
                            break;
                    }

                    if (!hasBinding) {
                        recommendations.add("Consider binding this item to a thing for automation");
                    }
                }
            }

            // Ensure score doesn't go below 0
            overallScore = Math.max(0, overallScore);

            boolean isValid = overallScore >= 70; // Consider valid if score is 70% or higher

            result.put("isValid", isValid);
            result.put("validationResults", validationResults);
            result.put("issues", issues);
            result.put("recommendations", recommendations);
            result.put("overallScore", overallScore);

            long executionTime = System.currentTimeMillis() - executionStartTime;
            logger.debug("Validation completed for item: {} with score: {} in {}ms", itemName, overallScore,
                    executionTime);

            return ActionResult.success(result, executionTime);

        } catch (ItemNotFoundException e) {
            long executionTime = System.currentTimeMillis() - executionStartTime;
            logger.debug("Item not found: {}", e.getMessage());

            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", "Item not found");
            result.put("timestamp", System.currentTimeMillis());

            return ActionResult.success(result, executionTime);

        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - executionStartTime;
            logger.error("Error validating item", e);
            throw new ActionException(getActionId(), "Failed to validate item: " + e.getMessage(), e);
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
                .description("Validate openHAB items and their configuration")
                .tags(List.of("items", "validation", "configuration", "binding", "quality"))
                .documentation(
                        "Validates openHAB items including configuration, binding status, and item integrity. Provides scoring and recommendations for improvement.")
                .examples(List.of("Basic validation: {\"itemName\": \"LivingRoom_Light\"}",
                        "Full validation: {\"itemName\": \"LivingRoom_Light\", \"validateBinding\": true, \"validateConfiguration\": true, \"validateState\": true}",
                        "With recommendations: {\"itemName\": \"LivingRoom_Light\", \"includeRecommendations\": true}"))
                .build();
    }

    @Override
    public Map<String, Object> getCapabilities() {
        Map<String, Object> capabilities = new HashMap<>();
        capabilities.put("async", true);
        capabilities.put("validation", true);
        capabilities.put("bindingValidation", true);
        capabilities.put("configurationValidation", true);
        capabilities.put("stateValidation", true);
        capabilities.put("scoring", true);
        capabilities.put("recommendations", true);
        capabilities.put("qualityAssessment", true);
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
        return itemRegistry != null;
    }
}
