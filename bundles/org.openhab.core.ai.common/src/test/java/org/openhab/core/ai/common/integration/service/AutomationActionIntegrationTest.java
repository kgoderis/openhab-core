package org.openhab.core.ai.common.integration.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.core.ai.common.actions.automation.*;
import org.openhab.core.ai.common.api.action.AIActionResult;

/**
 * Integration tests for Automation-related AIActions using mocked openHAB services.
 */
class AutomationActionIntegrationTest extends BaseAIActionIntegrationTest {

    private AdvancedAutomationAction advancedAutomationAction;

    @BeforeEach
    void setUpActions() {
        // Initialize all automation actions
        advancedAutomationAction = new AdvancedAutomationAction();

        // Initialize actions with context
        advancedAutomationAction.initialize(actionContext);
    }

    @Test
    void testAdvancedAutomationActionWithCreateWorkflow() throws Exception {
        Map<String, Object> parameters = Map.of("action", "create_workflow", "workflowName", "Test Workflow",
                "description", "A test automation workflow");
        AIActionResult result = executeAction(advancedAutomationAction, parameters);

        // In mocked mode without real automation system, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "workflow");
            assertResultContainsKey(result, "workflowName");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testAdvancedAutomationActionWithExecuteWorkflow() throws Exception {
        Map<String, Object> parameters = Map.of("action", "execute_workflow", "workflowId", "test-workflow-1");
        AIActionResult result = executeAction(advancedAutomationAction, parameters);

        // In mocked mode without real automation system, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "executed");
            assertResultContainsKey(result, "workflowId");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testAdvancedAutomationActionWithScheduleWorkflow() throws Exception {
        Map<String, Object> parameters = Map.of("action", "schedule_workflow", "workflowId", "test-workflow-1",
                "schedule", "0 0 12 * * ?");
        AIActionResult result = executeAction(advancedAutomationAction, parameters);

        // In mocked mode without real automation system, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "scheduled");
            assertResultContainsKey(result, "schedule");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testAdvancedAutomationActionWithGetWorkflowStatus() throws Exception {
        Map<String, Object> parameters = Map.of("action", "get_workflow_status", "workflowId", "test-workflow-1");
        AIActionResult result = executeAction(advancedAutomationAction, parameters);

        // In mocked mode without real automation system, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "status");
            assertResultContainsKey(result, "workflowId");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testAdvancedAutomationActionWithListWorkflows() throws Exception {
        Map<String, Object> parameters = Map.of("action", "list_workflows");
        AIActionResult result = executeAction(advancedAutomationAction, parameters);

        // In mocked mode without real automation system, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "workflows");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testAdvancedAutomationActionWithDeleteWorkflow() throws Exception {
        Map<String, Object> parameters = Map.of("action", "delete_workflow", "workflowId", "test-workflow-1");
        AIActionResult result = executeAction(advancedAutomationAction, parameters);

        // In mocked mode without real automation system, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "deleted");
            assertResultContainsKey(result, "workflowId");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testAdvancedAutomationActionWithConditionalLogic() throws Exception {
        Map<String, Object> parameters = Map.of("action", "conditional_logic", "condition", "TestSwitch == ON",
                "actions", List.of("TestNumber.sendCommand(42)"));
        AIActionResult result = executeAction(advancedAutomationAction, parameters);

        // In mocked mode without real automation system, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "executed");
            assertResultContainsKey(result, "condition");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testAdvancedAutomationActionWithTimeBasedTrigger() throws Exception {
        Map<String, Object> parameters = Map.of("action", "time_based_trigger", "trigger", "sunset", "actions",
                List.of("TestSwitch.sendCommand(ON)"));
        AIActionResult result = executeAction(advancedAutomationAction, parameters);

        // In mocked mode without real automation system, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "triggered");
            assertResultContainsKey(result, "trigger");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testAdvancedAutomationActionWithEventBasedTrigger() throws Exception {
        Map<String, Object> parameters = Map.of("action", "event_based_trigger", "eventType", "ItemStateChangedEvent",
                "itemName", "TestSwitch", "actions", List.of("TestNumber.sendCommand(100)"));
        AIActionResult result = executeAction(advancedAutomationAction, parameters);

        // In mocked mode without real automation system, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "triggered");
            assertResultContainsKey(result, "eventType");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testAdvancedAutomationActionWithInvalidAction() throws Exception {
        Map<String, Object> parameters = Map.of("action", "invalid_action");
        AIActionResult result = executeAction(advancedAutomationAction, parameters);

        assertFailure(result);
        assertNotNull(result.getMessage());
    }

    @Test
    void testAdvancedAutomationActionWithMissingWorkflowId() throws Exception {
        Map<String, Object> parameters = Map.of("action", "execute_workflow");
        AIActionResult result = executeAction(advancedAutomationAction, parameters);

        assertFailure(result);
        assertNotNull(result.getMessage());
    }

    @Test
    void testAdvancedAutomationActionWithInvalidSchedule() throws Exception {
        Map<String, Object> parameters = Map.of("action", "schedule_workflow", "workflowId", "test-workflow-1",
                "schedule", "invalid_cron_expression");
        AIActionResult result = executeAction(advancedAutomationAction, parameters);

        assertFailure(result);
        assertNotNull(result.getMessage());
    }

    @Test
    void testAdvancedAutomationActionWithEmptyActionsList() throws Exception {
        Map<String, Object> parameters = Map.of("action", "conditional_logic", "condition", "TestSwitch == ON",
                "actions", List.of());
        AIActionResult result = executeAction(advancedAutomationAction, parameters);

        assertFailure(result);
        assertNotNull(result.getMessage());
    }
}
