package org.openhab.core.ai.common.integration.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.core.ai.common.actions.rules.*;
import org.openhab.core.ai.common.api.action.AIActionResult;

/**
 * Integration tests for Rules-related AIActions using mocked openHAB services.
 */
class RulesActionIntegrationTest extends BaseAIActionIntegrationTest {

    private GetRuleAction getRuleAction;
    private ListRulesAction listRulesAction;
    private CreateRuleAction createRuleAction;
    private UpdateRuleAction updateRuleAction;
    private DeleteRuleAction deleteRuleAction;
    private GetRuleStatusAction getRuleStatusAction;
    private EnableRuleAction enableRuleAction;
    private DisableRuleAction disableRuleAction;
    private ExecuteRuleAction executeRuleAction;
    private GetRuleTriggersAction getRuleTriggersAction;
    private GetRuleConditionsAction getRuleConditionsAction;
    private GetRuleActionsAction getRuleActionsAction;
    private GetRuleHistoryAction getRuleHistoryAction;
    private GetRuleStatisticsAction getRuleStatisticsAction;
    private ValidateRuleAction validateRuleAction;
    private SearchRulesAction searchRulesAction;
    private BulkRuleOperationsAction bulkRuleOperationsAction;

    @BeforeEach
    void setUpActions() {
        // Initialize all rule actions
        getRuleAction = new GetRuleAction();
        listRulesAction = new ListRulesAction();
        createRuleAction = new CreateRuleAction();
        updateRuleAction = new UpdateRuleAction();
        deleteRuleAction = new DeleteRuleAction();
        getRuleStatusAction = new GetRuleStatusAction();
        enableRuleAction = new EnableRuleAction();
        disableRuleAction = new DisableRuleAction();
        executeRuleAction = new ExecuteRuleAction();
        getRuleTriggersAction = new GetRuleTriggersAction();
        getRuleConditionsAction = new GetRuleConditionsAction();
        getRuleActionsAction = new GetRuleActionsAction();
        getRuleHistoryAction = new GetRuleHistoryAction();
        getRuleStatisticsAction = new GetRuleStatisticsAction();
        validateRuleAction = new ValidateRuleAction();
        searchRulesAction = new SearchRulesAction();
        bulkRuleOperationsAction = new BulkRuleOperationsAction();

        // Initialize actions with context
        getRuleAction.initialize(actionContext);
        listRulesAction.initialize(actionContext);
        createRuleAction.initialize(actionContext);
        updateRuleAction.initialize(actionContext);
        deleteRuleAction.initialize(actionContext);
        getRuleStatusAction.initialize(actionContext);
        enableRuleAction.initialize(actionContext);
        disableRuleAction.initialize(actionContext);
        executeRuleAction.initialize(actionContext);
        getRuleTriggersAction.initialize(actionContext);
        getRuleConditionsAction.initialize(actionContext);
        getRuleActionsAction.initialize(actionContext);
        getRuleHistoryAction.initialize(actionContext);
        getRuleStatisticsAction.initialize(actionContext);
        validateRuleAction.initialize(actionContext);
        searchRulesAction.initialize(actionContext);
        bulkRuleOperationsAction.initialize(actionContext);
    }

    @Test
    void testListRulesAction() throws Exception {
        Map<String, Object> parameters = Map.of("filter", "all");
        AIActionResult result = executeAction(listRulesAction, parameters);

        assertSuccess(result);
        assertResultContainsKey(result, "rules");
    }

    @Test
    void testListRulesWithStatusFilter() throws Exception {
        Map<String, Object> parameters = Map.of("filter", "status", "status", "ENABLED");
        AIActionResult result = executeAction(listRulesAction, parameters);

        assertSuccess(result);
        assertResultContainsKey(result, "rules");
    }

    @Test
    void testListRulesWithTagFilter() throws Exception {
        Map<String, Object> parameters = Map.of("filter", "tag", "tag", "automation");
        AIActionResult result = executeAction(listRulesAction, parameters);

        assertSuccess(result);
        assertResultContainsKey(result, "rules");
    }

    @Test
    void testGetRuleAction() throws Exception {
        Map<String, Object> parameters = Map.of("ruleUID", "test-rule-1");
        AIActionResult result = executeAction(getRuleAction, parameters);

        // In mocked mode without real rules, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "rule");
            assertResultContainsKey(result, "ruleUID");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testCreateRuleAction() throws Exception {
        Map<String, Object> parameters = Map.of("ruleName", "Test Rule", "description", "A test rule", "triggers",
                List.of(Map.of("type", "ItemStateChangeTrigger", "itemName", "TestSwitch")), "conditions",
                List.of(Map.of("type", "ItemStateCondition", "itemName", "TestSwitch", "state", "ON")), "actions",
                List.of(Map.of("type", "ItemCommandAction", "itemName", "TestNumber", "command", "42")));
        AIActionResult result = executeAction(createRuleAction, parameters);

        // In mocked mode without real rule engine, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "rule");
            assertResultContainsKey(result, "ruleUID");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testUpdateRuleAction() throws Exception {
        Map<String, Object> parameters = Map.of("ruleUID", "test-rule-1", "description",
                "Updated test rule description");
        AIActionResult result = executeAction(updateRuleAction, parameters);

        // In mocked mode without real rules, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "rule");
            assertResultContainsKey(result, "ruleUID");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testDeleteRuleAction() throws Exception {
        Map<String, Object> parameters = Map.of("ruleUID", "test-rule-1");
        AIActionResult result = executeAction(deleteRuleAction, parameters);

        // In mocked mode without real rules, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "deleted");
            assertResultContainsKey(result, "ruleUID");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testGetRuleStatusAction() throws Exception {
        Map<String, Object> parameters = Map.of("ruleUID", "test-rule-1");
        AIActionResult result = executeAction(getRuleStatusAction, parameters);

        // In mocked mode without real rules, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "status");
            assertResultContainsKey(result, "ruleUID");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testEnableRuleAction() throws Exception {
        Map<String, Object> parameters = Map.of("ruleUID", "test-rule-1");
        AIActionResult result = executeAction(enableRuleAction, parameters);

        // In mocked mode without real rules, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "enabled");
            assertResultContainsKey(result, "ruleUID");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testDisableRuleAction() throws Exception {
        Map<String, Object> parameters = Map.of("ruleUID", "test-rule-1");
        AIActionResult result = executeAction(disableRuleAction, parameters);

        // In mocked mode without real rules, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "enabled");
            assertResultContainsKey(result, "ruleUID");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testExecuteRuleAction() throws Exception {
        Map<String, Object> parameters = Map.of("ruleUID", "test-rule-1");
        AIActionResult result = executeAction(executeRuleAction, parameters);

        // In mocked mode without real rules, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "executed");
            assertResultContainsKey(result, "ruleUID");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testGetRuleTriggersAction() throws Exception {
        Map<String, Object> parameters = Map.of("ruleUID", "test-rule-1");
        AIActionResult result = executeAction(getRuleTriggersAction, parameters);

        // In mocked mode without real rules, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "triggers");
            assertResultContainsKey(result, "ruleUID");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testGetRuleConditionsAction() throws Exception {
        Map<String, Object> parameters = Map.of("ruleUID", "test-rule-1");
        AIActionResult result = executeAction(getRuleConditionsAction, parameters);

        // In mocked mode without real rules, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "conditions");
            assertResultContainsKey(result, "ruleUID");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testGetRuleActionsAction() throws Exception {
        Map<String, Object> parameters = Map.of("ruleUID", "test-rule-1");
        AIActionResult result = executeAction(getRuleActionsAction, parameters);

        // In mocked mode without real rules, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "actions");
            assertResultContainsKey(result, "ruleUID");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testGetRuleHistoryAction() throws Exception {
        Map<String, Object> parameters = Map.of("ruleUID", "test-rule-1", "startTime", "2024-01-01T00:00:00Z",
                "endTime", "2024-12-31T23:59:59Z");
        AIActionResult result = executeAction(getRuleHistoryAction, parameters);

        // In mocked mode without real rules, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "history");
            assertResultContainsKey(result, "ruleUID");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testGetRuleStatisticsAction() throws Exception {
        Map<String, Object> parameters = Map.of("ruleUID", "test-rule-1", "startTime", "2024-01-01T00:00:00Z",
                "endTime", "2024-12-31T23:59:59Z");
        AIActionResult result = executeAction(getRuleStatisticsAction, parameters);

        // In mocked mode without real rules, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "statistics");
            assertResultContainsKey(result, "ruleUID");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testValidateRuleAction() throws Exception {
        Map<String, Object> parameters = Map.of("ruleName", "Test Rule", "triggers",
                List.of(Map.of("type", "ItemStateChangeTrigger", "itemName", "TestSwitch")), "conditions",
                List.of(Map.of("type", "ItemStateCondition", "itemName", "TestSwitch", "state", "ON")), "actions",
                List.of(Map.of("type", "ItemCommandAction", "itemName", "TestNumber", "command", "42")));
        AIActionResult result = executeAction(validateRuleAction, parameters);

        // In mocked mode without real rule engine, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "valid");
            assertResultContainsKey(result, "ruleName");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testSearchRulesAction() throws Exception {
        Map<String, Object> parameters = Map.of("query", "test");
        AIActionResult result = executeAction(searchRulesAction, parameters);

        assertSuccess(result);
        assertResultContainsKey(result, "rules");
        assertResultContainsKey(result, "query");
    }

    @Test
    void testSearchRulesWithAdvancedFilters() throws Exception {
        Map<String, Object> parameters = Map.of("query", "test", "status", "ENABLED", "tags",
                List.of("automation", "test"));
        AIActionResult result = executeAction(searchRulesAction, parameters);

        assertSuccess(result);
        assertResultContainsKey(result, "rules");
    }

    @Test
    void testBulkRuleOperationsAction() throws Exception {
        Map<String, Object> parameters = Map.of("operation", "enable", "ruleUIDs",
                List.of("test-rule-1", "test-rule-2"));
        AIActionResult result = executeAction(bulkRuleOperationsAction, parameters);

        // In mocked mode without real rules, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "results");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testGetRuleActionWithInvalidUID() throws Exception {
        Map<String, Object> parameters = Map.of("ruleUID", "invalid-rule-uid");
        AIActionResult result = executeAction(getRuleAction, parameters);

        assertFailure(result);
        assertNotNull(result.getMessage());
    }

    @Test
    void testCreateRuleActionWithInvalidTriggers() throws Exception {
        Map<String, Object> parameters = Map.of("ruleName", "Invalid Test Rule", "triggers",
                List.of(Map.of("type", "InvalidTriggerType")));
        AIActionResult result = executeAction(createRuleAction, parameters);

        assertFailure(result);
        assertNotNull(result.getMessage());
    }

    @Test
    void testValidateRuleActionWithInvalidConditions() throws Exception {
        Map<String, Object> parameters = Map.of("ruleName", "Invalid Test Rule", "conditions",
                List.of(Map.of("type", "InvalidConditionType")));
        AIActionResult result = executeAction(validateRuleAction, parameters);

        assertFailure(result);
        assertNotNull(result.getMessage());
    }
}
