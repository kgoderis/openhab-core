package org.openhab.core.ai.action.library.items;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.action.api.Action;
import org.openhab.core.ai.action.api.ActionException;
import org.openhab.core.ai.action.api.ActionMetadata;
import org.openhab.core.ai.action.api.ActionResult;
import org.openhab.core.ai.action.api.ActionValidationResult;
import org.openhab.core.ai.common.context.ExecutionContext;
import org.openhab.core.items.GroupItem;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemNotFoundException;
import org.openhab.core.items.ItemRegistry;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action for getting item groups in openHAB.
 * 
 * This action provides functionality to retrieve
 * group information for items.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class GetItemGroupsAction implements Action {

    private static final Logger logger = LoggerFactory.getLogger(GetItemGroupsAction.class);

    @Reference
    private @Nullable ItemRegistry itemRegistry;

    @Override
    public String getActionId() {
        return "openhab.items.get_groups";
    }

    @Override
    public String getActionName() {
        return "Get Item Groups";
    }

    @Override
    public String getDescription() {
        return "Retrieve group information for items";
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
                Map.of("type", "string", "description", "The name of the item to get groups for", "required", true));
        properties.put("includeGroupDetails", Map.of("type", "boolean", "description",
                "Include detailed information about each group", "default", true));
        properties.put("includeGroupMembers",
                Map.of("type", "boolean", "description", "Include group member information", "default", false));
        properties.put("includeGroupFunctions",
                Map.of("type", "boolean", "description", "Include group function information", "default", false));

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
        properties.put("isGroup", Map.of("type", "boolean"));
        properties.put("groupMemberships", Map.of("type", "array"));
        properties.put("groupDetails", Map.of("type", "object"));
        properties.put("totalGroups", Map.of("type", "integer"));
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
    public ActionResult execute(Map<String, Object> parameters, ExecutionContext context) throws ActionException {
        long executionStartTime = System.currentTimeMillis();

        try {
            String itemName = (String) parameters.get("itemName");
            Boolean includeGroupDetails = (Boolean) parameters.getOrDefault("includeGroupDetails", true);
            Boolean includeGroupMembers = (Boolean) parameters.getOrDefault("includeGroupMembers", false);
            Boolean includeGroupFunctions = (Boolean) parameters.getOrDefault("includeGroupFunctions", false);

            logger.debug("Getting groups for item: {} includeDetails: {}", itemName, includeGroupDetails);

            // Get the item
            Item item = itemRegistry.getItem(itemName);

            Map<String, Object> result = new HashMap<>();
            result.put("itemName", itemName);
            result.put("success", true);
            result.put("timestamp", System.currentTimeMillis());

            // Check if the item itself is a group
            boolean isGroup = item instanceof GroupItem;
            result.put("isGroup", isGroup);

            // Find groups that contain this item
            List<String> groupMemberships = itemRegistry.getAll().stream()
                    .filter(groupItem -> groupItem instanceof GroupItem).map(groupItem -> (GroupItem) groupItem)
                    .filter(groupItem -> groupItem.getMembers().contains(item)).map(GroupItem::getName)
                    .collect(Collectors.toList());

            result.put("groupMemberships", groupMemberships);
            result.put("totalGroups", groupMemberships.size());

            if (includeGroupDetails && !groupMemberships.isEmpty()) {
                Map<String, Object> groupDetails = new HashMap<>();

                for (String groupName : groupMemberships) {
                    Item groupItem = itemRegistry.getItem(groupName);
                    if (groupItem instanceof GroupItem group) {
                        Map<String, Object> groupInfo = new HashMap<>();
                        groupInfo.put("type", group.getType());
                        String baseItemType = "";
                        if (group.getBaseItem() != null) {
                            baseItemType = group.getBaseItem().getType();
                        }
                        groupInfo.put("baseItem", baseItemType);

                        String function = "";
                        if (group.getFunction() != null) {
                            function = group.getFunction().toString();
                        }
                        groupInfo.put("function", function);

                        if (includeGroupMembers) {
                            List<String> memberNames = group.getMembers().stream().map(Item::getName)
                                    .collect(Collectors.toList());
                            groupInfo.put("memberCount", group.getMembers().size());
                            groupInfo.put("members", memberNames);
                        }

                        if (includeGroupFunctions && group.getFunction() != null) {
                            Map<String, Object> functionInfo = new HashMap<>();
                            functionInfo.put("name", group.getFunction().getClass().getSimpleName());
                            functionInfo.put("parameters", group.getFunction().getParameters());
                            groupInfo.put("functionDetails", functionInfo);
                        }

                        groupDetails.put(groupName, groupInfo);
                    }
                }

                result.put("groupDetails", groupDetails);
            }

            if (groupMemberships.isEmpty()) {
                result.put("note", "Item is not a member of any groups");
            }

            long executionTime = System.currentTimeMillis() - executionStartTime;
            logger.debug("Successfully retrieved groups for item: {} in {}ms", itemName, executionTime);

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
            logger.error("Error getting item groups", e);
            throw new ActionException(getActionId(), "Failed to get item groups: " + e.getMessage(), e);
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
        return ActionMetadata.builder().withVersion(getVersion()).withAuthor("openHAB")
                .withDescription("Retrieve group information for openHAB items")
                .withTags(List.of("items", "groups", "membership", "hierarchy"))
                .withDocumentation(
                        "Retrieves group information for openHAB items including group memberships, group details, and member information.")
                .withExamples(List.of("Get group memberships: {\"itemName\": \"LivingRoom_Light\"}",
                        "Get group details: {\"itemName\": \"LivingRoom_Light\", \"includeGroupDetails\": true}",
                        "Get group members: {\"itemName\": \"LivingRoom_Light\", \"includeGroupMembers\": true}",
                        "Get group functions: {\"itemName\": \"LivingRoom_Light\", \"includeGroupFunctions\": true}"))
                .build();
    }

    @Override
    public Map<String, Object> getCapabilities() {
        Map<String, Object> capabilities = new HashMap<>();
        capabilities.put("async", true);
        capabilities.put("validation", true);
        capabilities.put("groupRetrieval", true);
        capabilities.put("groupDetails", true);
        capabilities.put("groupMembers", true);
        capabilities.put("groupFunctions", true);
        capabilities.put("membershipDetection", true);
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
        return itemRegistry != null;
    }
}
