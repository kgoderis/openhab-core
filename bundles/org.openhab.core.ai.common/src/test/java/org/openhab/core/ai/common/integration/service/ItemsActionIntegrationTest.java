package org.openhab.core.ai.common.integration.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.core.ai.common.action.AIActionResult;
import org.openhab.core.ai.common.actions.items.*;
import org.openhab.core.items.Item;

/**
 * Integration tests for Items-related AIActions using mocked openHAB services.
 */
class ItemsActionIntegrationTest extends BaseAIActionIntegrationTest {

    private GetItemAction getItemAction;
    private ListItemsAction listItemsAction;
    private CreateItemAction createItemAction;
    private UpdateItemAction updateItemAction;
    private DeleteItemAction deleteItemAction;
    private GetItemStateAction getItemStateAction;
    private SetItemStateAction setItemStateAction;
    private SendItemCommandAction sendItemCommandAction;
    private GetItemMetadataAction getItemMetadataAction;
    private SetItemMetadataAction setItemMetadataAction;
    private GetItemTagsAction getItemTagsAction;
    private SetItemTagsAction setItemTagsAction;
    private GetItemTypeAction getItemTypeAction;
    private GetItemBindingAction getItemBindingAction;
    private GetItemGroupsAction getItemGroupsAction;
    private GetItemHistoryAction getItemHistoryAction;
    private GetItemStatisticsAction getItemStatisticsAction;
    private ValidateItemAction validateItemAction;
    private SearchItemsAction searchItemsAction;
    private BulkItemOperationsAction bulkItemOperationsAction;

    @BeforeEach
    void setUpActions() {
        // Initialize all item actions
        getItemAction = new GetItemAction();
        listItemsAction = new ListItemsAction();
        createItemAction = new CreateItemAction();
        updateItemAction = new UpdateItemAction();
        deleteItemAction = new DeleteItemAction();
        getItemStateAction = new GetItemStateAction();
        setItemStateAction = new SetItemStateAction();
        sendItemCommandAction = new SendItemCommandAction();
        getItemMetadataAction = new GetItemMetadataAction();
        setItemMetadataAction = new SetItemMetadataAction();
        getItemTagsAction = new GetItemTagsAction();
        setItemTagsAction = new SetItemTagsAction();
        getItemTypeAction = new GetItemTypeAction();
        getItemBindingAction = new GetItemBindingAction();
        getItemGroupsAction = new GetItemGroupsAction();
        getItemHistoryAction = new GetItemHistoryAction();
        getItemStatisticsAction = new GetItemStatisticsAction();
        validateItemAction = new ValidateItemAction();
        searchItemsAction = new SearchItemsAction();
        bulkItemOperationsAction = new BulkItemOperationsAction();

        // Initialize actions with context
        getItemAction.initialize(actionContext);
        listItemsAction.initialize(actionContext);
        createItemAction.initialize(actionContext);
        updateItemAction.initialize(actionContext);
        deleteItemAction.initialize(actionContext);
        getItemStateAction.initialize(actionContext);
        setItemStateAction.initialize(actionContext);
        sendItemCommandAction.initialize(actionContext);
        getItemMetadataAction.initialize(actionContext);
        setItemMetadataAction.initialize(actionContext);
        getItemTagsAction.initialize(actionContext);
        setItemTagsAction.initialize(actionContext);
        getItemTypeAction.initialize(actionContext);
        getItemBindingAction.initialize(actionContext);
        getItemGroupsAction.initialize(actionContext);
        getItemHistoryAction.initialize(actionContext);
        getItemStatisticsAction.initialize(actionContext);
        validateItemAction.initialize(actionContext);
        searchItemsAction.initialize(actionContext);
        bulkItemOperationsAction.initialize(actionContext);
    }

    @Test
    void testGetItemAction() throws Exception {
        Map<String, Object> parameters = Map.of("itemName", "TestSwitch");
        AIActionResult result = executeAction(getItemAction, parameters);

        assertSuccess(result);
        assertResultContainsKey(result, "item");
    }

    @Test
    void testListItemsAction() throws Exception {
        Map<String, Object> parameters = Map.of("filter", "all");
        AIActionResult result = executeAction(listItemsAction, parameters);

        assertSuccess(result);
        assertResultContainsKey(result, "items");
        assertResultListSize(result, "items", 12); // 12 test items created in base class
    }

    @Test
    void testListItemsWithTypeFilter() throws Exception {
        Map<String, Object> parameters = Map.of("filter", "type", "type", "Switch");
        AIActionResult result = executeAction(listItemsAction, parameters);

        assertSuccess(result);
        assertResultContainsKey(result, "items");
    }

    @Test
    void testCreateItemAction() throws Exception {
        Map<String, Object> parameters = Map.of("itemName", "NewTestSwitch", "itemType", "Switch", "label",
                "New Test Switch", "category", "switch");
        AIActionResult result = executeAction(createItemAction, parameters);

        assertSuccess(result);
        assertResultContainsKey(result, "item");

        // Verify item was created
        assertTestItemExists("NewTestSwitch");
    }

    @Test
    void testUpdateItemAction() throws Exception {
        // First create an item
        Map<String, Object> createParams = Map.of("itemName", "UpdateTestSwitch", "itemType", "Switch", "label",
                "Original Label");
        executeAction(createItemAction, createParams);

        // Update the item
        Map<String, Object> updateParams = Map.of("itemName", "UpdateTestSwitch", "label", "Updated Label", "category",
                "updated_switch");
        AIActionResult result = executeAction(updateItemAction, updateParams);

        assertSuccess(result);
        assertResultContainsKey(result, "item");
    }

    @Test
    void testDeleteItemAction() throws Exception {
        // First create an item
        Map<String, Object> createParams = Map.of("itemName", "DeleteTestSwitch", "itemType", "Switch");
        executeAction(createItemAction, createParams);

        // Verify item exists
        assertTestItemExists("DeleteTestSwitch");

        // Delete the item
        Map<String, Object> deleteParams = Map.of("itemName", "DeleteTestSwitch");
        AIActionResult result = executeAction(deleteItemAction, deleteParams);

        assertSuccess(result);

        // Verify item was deleted
        Item deletedItem = getTestItem("DeleteTestSwitch");
        assertNull(deletedItem, "Item should be deleted");
    }

    @Test
    void testGetItemStateAction() throws Exception {
        Map<String, Object> parameters = Map.of("itemName", "TestSwitch");
        AIActionResult result = executeAction(getItemStateAction, parameters);

        assertSuccess(result);
        assertResultContainsKey(result, "state");
        assertResultContainsKey(result, "itemName");
    }

    @Test
    void testSetItemStateAction() throws Exception {
        Map<String, Object> parameters = Map.of("itemName", "TestSwitch", "state", "ON");
        AIActionResult result = executeAction(setItemStateAction, parameters);

        assertSuccess(result);
        assertResultContainsKey(result, "previousState");
        assertResultContainsKey(result, "newState");

        // Verify state was actually set
        assertTestItemState("TestSwitch", "ON");
    }

    @Test
    void testSendItemCommandAction() throws Exception {
        Map<String, Object> parameters = Map.of("itemName", "TestSwitch", "command", "ON");
        AIActionResult result = executeAction(sendItemCommandAction, parameters);

        assertSuccess(result);
        assertResultContainsKey(result, "command");
        assertResultContainsKey(result, "itemName");
    }

    @Test
    void testGetItemMetadataAction() throws Exception {
        Map<String, Object> parameters = Map.of("itemName", "TestSwitch");
        AIActionResult result = executeAction(getItemMetadataAction, parameters);

        assertSuccess(result);
        assertResultContainsKey(result, "metadata");
        assertResultContainsKey(result, "itemName");
    }

    @Test
    void testSetItemMetadataAction() throws Exception {
        Map<String, Object> parameters = Map.of("itemName", "TestSwitch", "namespace", "test", "value", "test_value",
                "configuration", Map.of("key1", "value1"));
        AIActionResult result = executeAction(setItemMetadataAction, parameters);

        assertSuccess(result);
        assertResultContainsKey(result, "metadata");

        // Verify metadata was set by getting it back
        Map<String, Object> getParams = Map.of("itemName", "TestSwitch");
        AIActionResult getResult = executeAction(getItemMetadataAction, getParams);
        assertSuccess(getResult);
    }

    @Test
    void testGetItemTagsAction() throws Exception {
        Map<String, Object> parameters = Map.of("itemName", "TestSwitch");
        AIActionResult result = executeAction(getItemTagsAction, parameters);

        assertSuccess(result);
        assertResultContainsKey(result, "tags");
        assertResultContainsKey(result, "itemName");
    }

    @Test
    void testSetItemTagsAction() throws Exception {
        Map<String, Object> parameters = Map.of("itemName", "TestSwitch", "tags", List.of("test_tag", "switch_tag"));
        AIActionResult result = executeAction(setItemTagsAction, parameters);

        assertSuccess(result);
        assertResultContainsKey(result, "tags");

        // Verify tags were set by getting them back
        Map<String, Object> getParams = Map.of("itemName", "TestSwitch");
        AIActionResult getResult = executeAction(getItemTagsAction, getParams);
        assertSuccess(getResult);
    }

    @Test
    void testGetItemTypeAction() throws Exception {
        Map<String, Object> parameters = Map.of("itemName", "TestSwitch");
        AIActionResult result = executeAction(getItemTypeAction, parameters);

        assertSuccess(result);
        assertResultContainsKey(result, "itemType");
        assertResultContainsKey(result, "itemName");
    }

    @Test
    void testGetItemBindingAction() throws Exception {
        Map<String, Object> parameters = Map.of("itemName", "TestSwitch");
        AIActionResult result = executeAction(getItemBindingAction, parameters);

        assertSuccess(result);
        assertResultContainsKey(result, "binding");
        assertResultContainsKey(result, "itemName");
    }

    @Test
    void testGetItemGroupsAction() throws Exception {
        Map<String, Object> parameters = Map.of("itemName", "TestSwitch");
        AIActionResult result = executeAction(getItemGroupsAction, parameters);

        assertSuccess(result);
        assertResultContainsKey(result, "groups");
        assertResultContainsKey(result, "itemName");
    }

    @Test
    void testGetItemHistoryAction() throws Exception {
        Map<String, Object> parameters = Map.of("itemName", "TestSwitch", "startTime", "2024-01-01T00:00:00Z",
                "endTime", "2024-12-31T23:59:59Z");
        AIActionResult result = executeAction(getItemHistoryAction, parameters);

        assertSuccess(result);
        assertResultContainsKey(result, "history");
        assertResultContainsKey(result, "itemName");
    }

    @Test
    void testGetItemStatisticsAction() throws Exception {
        Map<String, Object> parameters = Map.of("itemName", "TestNumber", "startTime", "2024-01-01T00:00:00Z",
                "endTime", "2024-12-31T23:59:59Z");
        AIActionResult result = executeAction(getItemStatisticsAction, parameters);

        assertSuccess(result);
        assertResultContainsKey(result, "statistics");
        assertResultContainsKey(result, "itemName");
    }

    @Test
    void testValidateItemAction() throws Exception {
        Map<String, Object> parameters = Map.of("itemName", "TestSwitch");
        AIActionResult result = executeAction(validateItemAction, parameters);

        assertSuccess(result);
        assertResultContainsKey(result, "valid");
        assertResultContainsKey(result, "itemName");
    }

    @Test
    void testSearchItemsAction() throws Exception {
        Map<String, Object> parameters = Map.of("query", "Test");
        AIActionResult result = executeAction(searchItemsAction, parameters);

        assertSuccess(result);
        assertResultContainsKey(result, "items");
        assertResultContainsKey(result, "query");
    }

    @Test
    void testBulkItemOperationsAction() throws Exception {
        // Create multiple items first
        Map<String, Object> createParams1 = Map.of("itemName", "BulkTest1", "itemType", "Switch");
        Map<String, Object> createParams2 = Map.of("itemName", "BulkTest2", "itemType", "Number");
        executeAction(createItemAction, createParams1);
        executeAction(createItemAction, createParams2);

        // Perform bulk operation
        Map<String, Object> bulkParams = Map.of("operation", "setState", "items", List.of("BulkTest1", "BulkTest2"),
                "state", "ON");
        AIActionResult result = executeAction(bulkItemOperationsAction, bulkParams);

        assertSuccess(result);
        assertResultContainsKey(result, "results");
    }

    @Test
    void testGetItemActionWithInvalidItem() throws Exception {
        Map<String, Object> parameters = Map.of("itemName", "NonExistentItem");
        AIActionResult result = executeAction(getItemAction, parameters);

        assertFailure(result);
        assertNotNull(result.getMessage());
    }

    @Test
    void testCreateItemActionWithInvalidType() throws Exception {
        Map<String, Object> parameters = Map.of("itemName", "InvalidTestItem", "itemType", "InvalidType");
        AIActionResult result = executeAction(createItemAction, parameters);

        assertFailure(result);
        assertNotNull(result.getMessage());
    }

    @Test
    void testSetItemStateActionWithInvalidState() throws Exception {
        Map<String, Object> parameters = Map.of("itemName", "TestNumber", "state", "INVALID_STATE");
        AIActionResult result = executeAction(setItemStateAction, parameters);

        assertFailure(result);
        assertNotNull(result.getMessage());
    }
}
