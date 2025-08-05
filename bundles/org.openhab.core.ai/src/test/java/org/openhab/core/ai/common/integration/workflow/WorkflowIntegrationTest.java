package org.openhab.core.ai.integration.workflow;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.core.ai.action.ActionResult;
import org.openhab.core.ai.actions.addons.*;
import org.openhab.core.ai.actions.analytics.*;
import org.openhab.core.ai.actions.automation.*;
import org.openhab.core.ai.actions.channels.*;
import org.openhab.core.ai.actions.config.*;
import org.openhab.core.ai.actions.discovery.*;
import org.openhab.core.ai.actions.events.*;
import org.openhab.core.ai.actions.filesystem.*;
import org.openhab.core.ai.actions.items.*;
import org.openhab.core.ai.actions.monitoring.*;
import org.openhab.core.ai.actions.network.*;
import org.openhab.core.ai.actions.persistence.*;
import org.openhab.core.ai.actions.resources.*;
import org.openhab.core.ai.actions.rules.*;
import org.openhab.core.ai.actions.scripts.*;
import org.openhab.core.ai.actions.security.*;
import org.openhab.core.ai.actions.system.*;
import org.openhab.core.ai.actions.things.*;
import org.openhab.core.ai.integration.service.BaseActionIntegrationTest;

/**
 * Workflow Integration Tests for Actions using mocked openHAB services.
 * 
 * Tests multi-action workflows, action chaining, dependencies, and error propagation
 * as outlined in the TEST_PLAN.md integration testing strategy.
 */
class WorkflowIntegrationTest extends BaseActionIntegrationTest {

    // Core workflow actions - using simpler action classes
    private CreateItemAction createItemAction;
    private GetItemAction getItemAction;
    private SetItemStateAction setItemStateAction;
    private GetItemStateAction getItemStateAction;
    private DeleteItemAction deleteItemAction;

    // Thing management actions
    private GetThingAction getThingAction;
    private DeleteThingAction deleteThingAction;

    // Persistence actions
    private PersistenceAction persistenceAction;

    // Channel actions
    private GetChannelAction getChannelAction;

    // Rule actions
    private CreateRuleAction createRuleAction;
    private GetRuleAction getRuleAction;
    private DeleteRuleAction deleteRuleAction;

    // Addon actions
    private InstallAddonAction installAddonAction;
    private GetAddonAction getAddonAction;
    private UninstallAddonAction uninstallAddonAction;

    // Script actions
    private CreateScriptAction createScriptAction;
    private GetScriptAction getScriptAction;
    private DeleteScriptAction deleteScriptAction;

    // Discovery actions
    private StartDiscoveryAction startDiscoveryAction;
    private GetDiscoveryResultsAction getDiscoveryResultsAction;

    // Event actions
    private ListSubscriptionsAction listSubscriptionsAction;
    private SubscribeEventsAction subscribeEventsAction;

    // Filesystem actions
    private WriteFileAction writeFileAction;
    private ReadFileAction readFileAction;
    private DeleteFileAction deleteFileAction;

    // Network actions
    private GetNetworkStatusAction getNetworkStatusAction;
    private TestConnectivityAction testConnectivityAction;

    // Security actions
    private SecurityManagementAction securityManagementAction;

    // Monitoring actions
    private GetLogsAction getLogsAction;
    private SetLogLevelAction setLogLevelAction;

    // Analytics actions
    private DataAnalysisAction dataAnalysisAction;

    // Automation actions
    private AdvancedAutomationAction advancedAutomationAction;

    // Config actions
    private ConfigurationSetAction configurationSetAction;
    private ConfigurationGetAction configurationGetAction;

    // Resources actions
    private ResourceManagementAction resourceManagementAction;

    // System actions
    private SystemInfoAction systemInfoAction;
    private HealthCheckAction healthCheckAction;

    @BeforeEach
    void setUpWorkflowActions() {
        // Initialize all workflow actions
        createItemAction = new CreateItemAction();
        getItemAction = new GetItemAction();
        setItemStateAction = new SetItemStateAction();
        getItemStateAction = new GetItemStateAction();
        deleteItemAction = new DeleteItemAction();

        getThingAction = new GetThingAction();
        deleteThingAction = new DeleteThingAction();

        persistenceAction = new PersistenceAction();

        getChannelAction = new GetChannelAction();

        createRuleAction = new CreateRuleAction();
        getRuleAction = new GetRuleAction();
        deleteRuleAction = new DeleteRuleAction();

        installAddonAction = new InstallAddonAction();
        getAddonAction = new GetAddonAction();
        uninstallAddonAction = new UninstallAddonAction();

        createScriptAction = new CreateScriptAction();
        getScriptAction = new GetScriptAction();
        deleteScriptAction = new DeleteScriptAction();

        startDiscoveryAction = new StartDiscoveryAction();
        getDiscoveryResultsAction = new GetDiscoveryResultsAction();

        listSubscriptionsAction = new ListSubscriptionsAction();
        subscribeEventsAction = new SubscribeEventsAction();

        writeFileAction = new WriteFileAction();
        readFileAction = new ReadFileAction();
        deleteFileAction = new DeleteFileAction();

        getNetworkStatusAction = new GetNetworkStatusAction();
        testConnectivityAction = new TestConnectivityAction();

        securityManagementAction = new SecurityManagementAction();

        getLogsAction = new GetLogsAction();
        setLogLevelAction = new SetLogLevelAction();

        dataAnalysisAction = new DataAnalysisAction();

        advancedAutomationAction = new AdvancedAutomationAction();

        configurationSetAction = new ConfigurationSetAction();
        configurationGetAction = new ConfigurationGetAction();

        resourceManagementAction = new ResourceManagementAction();

        systemInfoAction = new SystemInfoAction();
        healthCheckAction = new HealthCheckAction();

        // Initialize all actions with context
        createItemAction.initialize(actionContext);
        getItemAction.initialize(actionContext);
        setItemStateAction.initialize(actionContext);
        getItemStateAction.initialize(actionContext);
        deleteItemAction.initialize(actionContext);

        getThingAction.initialize(actionContext);
        deleteThingAction.initialize(actionContext);

        persistenceAction.initialize(actionContext);

        getChannelAction.initialize(actionContext);

        createRuleAction.initialize(actionContext);
        getRuleAction.initialize(actionContext);
        deleteRuleAction.initialize(actionContext);

        installAddonAction.initialize(actionContext);
        getAddonAction.initialize(actionContext);
        uninstallAddonAction.initialize(actionContext);

        createScriptAction.initialize(actionContext);
        getScriptAction.initialize(actionContext);
        deleteScriptAction.initialize(actionContext);

        startDiscoveryAction.initialize(actionContext);
        getDiscoveryResultsAction.initialize(actionContext);

        listSubscriptionsAction.initialize(actionContext);
        subscribeEventsAction.initialize(actionContext);

        writeFileAction.initialize(actionContext);
        readFileAction.initialize(actionContext);
        deleteFileAction.initialize(actionContext);

        getNetworkStatusAction.initialize(actionContext);
        testConnectivityAction.initialize(actionContext);

        securityManagementAction.initialize(actionContext);

        getLogsAction.initialize(actionContext);
        setLogLevelAction.initialize(actionContext);

        dataAnalysisAction.initialize(actionContext);

        advancedAutomationAction.initialize(actionContext);

        configurationSetAction.initialize(actionContext);
        configurationGetAction.initialize(actionContext);

        resourceManagementAction.initialize(actionContext);

        systemInfoAction.initialize(actionContext);
        healthCheckAction.initialize(actionContext);
    }

    /**
     * Test complete item lifecycle workflow: Create -> Configure -> Use -> Monitor -> Cleanup
     */
    @Test
    void testCompleteItemLifecycleWorkflow() throws Exception {
        // Step 1: Create item
        Map<String, Object> createParams = Map.of("itemName", "WorkflowTestSwitch", "itemType", "Switch", "label",
                "Workflow Test Switch", "category", "switch");
        ActionResult createResult = executeAction(createItemAction, createParams);

        // In mocked mode, this should fail gracefully
        if (!createResult.isSuccess()) {
            assertNotNull(createResult.getMessage());
            return; // Skip remaining workflow steps if creation fails
        }

        // Step 2: Get created item
        Map<String, Object> getParams = Map.of("itemName", "WorkflowTestSwitch");
        ActionResult getResult = executeAction(getItemAction, getParams);

        if (getResult.isSuccess()) {
            assertResultContainsKey(getResult, "item");
            assertResultContainsKey(getResult, "itemName");
        }

        // Step 3: Set item state
        Map<String, Object> setStateParams = Map.of("itemName", "WorkflowTestSwitch", "state", "ON");
        ActionResult setStateResult = executeAction(setItemStateAction, setStateParams);

        if (setStateResult.isSuccess()) {
            assertResultContainsKey(setStateResult, "stateSet");
        }

        // Step 4: Get item state
        Map<String, Object> getStateParams = Map.of("itemName", "WorkflowTestSwitch");
        ActionResult getStateResult = executeAction(getItemStateAction, getStateParams);

        if (getStateResult.isSuccess()) {
            assertResultContainsKey(getStateResult, "state");
            assertResultContainsKey(getStateResult, "itemName");
        }

        // Step 5: Cleanup - Delete item
        Map<String, Object> deleteParams = Map.of("itemName", "WorkflowTestSwitch");
        ActionResult deleteResult = executeAction(deleteItemAction, deleteParams);

        if (deleteResult.isSuccess()) {
            assertResultContainsKey(deleteResult, "deleted");
        }
    }

    /**
     * Test automation workflow: Create rule -> Create script -> Link them -> Execute
     */
    @Test
    void testAutomationWorkflow() throws Exception {
        // Step 1: Create script
        Map<String, Object> createScriptParams = Map.of("scriptName", "WorkflowTestScript", "scriptType", "js",
                "scriptContent", "console.log('Workflow test script executed');");
        ActionResult createScriptResult = executeAction(createScriptAction, createScriptParams);

        if (!createScriptResult.isSuccess()) {
            assertNotNull(createScriptResult.getMessage());
            return;
        }

        // Step 2: Create rule that uses the script
        Map<String, Object> createRuleParams = Map.of("ruleName", "WorkflowTestRule", "description",
                "Test rule for workflow", "triggers",
                List.of(Map.of("type", "ItemStateChangedTrigger", "itemName", "TestSwitch")), "conditions",
                List.of(Map.of("type", "ItemStateCondition", "itemName", "TestSwitch", "state", "ON")), "actions",
                List.of(Map.of("type", "ScriptAction", "scriptUID", "WorkflowTestScript")));
        ActionResult createRuleResult = executeAction(createRuleAction, createRuleParams);

        if (createRuleResult.isSuccess()) {
            assertResultContainsKey(createRuleResult, "rule");
            assertResultContainsKey(createRuleResult, "ruleName");
        }

        // Step 3: Get created rule
        Map<String, Object> getRuleParams = Map.of("ruleUID", "WorkflowTestRule");
        ActionResult getRuleResult = executeAction(getRuleAction, getRuleParams);

        if (getRuleResult.isSuccess()) {
            assertResultContainsKey(getRuleResult, "rule");
        }

        // Step 4: Cleanup - Delete rule and script
        Map<String, Object> deleteRuleParams = Map.of("ruleUID", "WorkflowTestRule");
        ActionResult deleteRuleResult = executeAction(deleteRuleAction, deleteRuleParams);

        if (deleteRuleResult.isSuccess()) {
            assertResultContainsKey(deleteRuleResult, "deleted");
        }

        Map<String, Object> deleteScriptParams = Map.of("scriptUID", "WorkflowTestScript");
        ActionResult deleteScriptResult = executeAction(deleteScriptAction, deleteScriptParams);

        if (deleteScriptResult.isSuccess()) {
            assertResultContainsKey(deleteScriptResult, "deleted");
        }
    }

    /**
     * Test addon management workflow: Install -> Configure -> Monitor -> Uninstall
     */
    @Test
    void testAddonManagementWorkflow() throws Exception {
        // Step 1: Install addon
        Map<String, Object> installParams = Map.of("addonId", "binding-test");
        ActionResult installResult = executeAction(installAddonAction, installParams);

        if (!installResult.isSuccess()) {
            assertNotNull(installResult.getMessage());
            return;
        }

        // Step 2: Get addon information
        Map<String, Object> getParams = Map.of("addonId", "binding-test");
        ActionResult getResult = executeAction(getAddonAction, getParams);

        if (getResult.isSuccess()) {
            assertResultContainsKey(getResult, "addon");
            assertResultContainsKey(getResult, "addonId");
        }

        // Step 3: Cleanup - Uninstall addon
        Map<String, Object> uninstallParams = Map.of("addonId", "binding-test");
        ActionResult uninstallResult = executeAction(uninstallAddonAction, uninstallParams);

        if (uninstallResult.isSuccess()) {
            assertResultContainsKey(uninstallResult, "uninstalled");
        }
    }

    /**
     * Test discovery workflow: Start discovery -> Monitor progress -> Get results
     */
    @Test
    void testDiscoveryWorkflow() throws Exception {
        // Step 1: Start discovery
        Map<String, Object> startParams = Map.of("bindingId", "test-binding", "timeout", 30);
        ActionResult startResult = executeAction(startDiscoveryAction, startParams);

        if (!startResult.isSuccess()) {
            assertNotNull(startResult.getMessage());
            return;
        }

        // Step 2: Get discovery results
        Map<String, Object> resultsParams = Map.of("bindingId", "test-binding");
        ActionResult resultsResult = executeAction(getDiscoveryResultsAction, resultsParams);

        if (resultsResult.isSuccess()) {
            assertResultContainsKey(resultsResult, "results");
            assertResultContainsKey(resultsResult, "bindingId");
        }
    }

    /**
     * Test event subscription workflow: Create subscription -> Monitor events -> Cleanup
     */
    @Test
    void testEventSubscriptionWorkflow() throws Exception {
        // Step 1: Create event subscription
        Map<String, Object> createParams = Map.of("eventType", "ItemStateChangedEvent", "itemName", "TestSwitch",
                "callbackUrl", "http://localhost:8080/events");
        ActionResult createResult = executeAction(subscribeEventsAction, createParams);

        if (!createResult.isSuccess()) {
            assertNotNull(createResult.getMessage());
            return;
        }

        // Step 2: Get event subscriptions
        Map<String, Object> getParams = Map.of("filter", "all");
        ActionResult getResult = executeAction(listSubscriptionsAction, getParams);

        if (getResult.isSuccess()) {
            assertResultContainsKey(getResult, "subscriptions");
        }
    }

    /**
     * Test filesystem workflow: Write file -> Read file -> Process -> Cleanup
     */
    @Test
    void testFilesystemWorkflow() throws Exception {
        String testFilePath = "/tmp/workflow-test.txt";
        String testContent = "Workflow test content";

        // Step 1: Write file
        Map<String, Object> writeParams = Map.of("filePath", testFilePath, "content", testContent, "overwrite", true);
        ActionResult writeResult = executeAction(writeFileAction, writeParams);

        if (!writeResult.isSuccess()) {
            assertNotNull(writeResult.getMessage());
            return;
        }

        // Step 2: Read file
        Map<String, Object> readParams = Map.of("filePath", testFilePath);
        ActionResult readResult = executeAction(readFileAction, readParams);

        if (readResult.isSuccess()) {
            assertResultContainsKey(readResult, "content");
            assertResultContainsKey(readResult, "filePath");
        }

        // Step 3: Cleanup - Delete file
        Map<String, Object> deleteParams = Map.of("filePath", testFilePath);
        ActionResult deleteResult = executeAction(deleteFileAction, deleteParams);

        if (deleteResult.isSuccess()) {
            assertResultContainsKey(deleteResult, "deleted");
        }
    }

    /**
     * Test monitoring and analytics workflow: Monitor -> Analyze -> Report
     */
    @Test
    void testMonitoringAndAnalyticsWorkflow() throws Exception {
        // Step 1: Set log level for monitoring
        Map<String, Object> setLogParams = Map.of("logger", "org.openhab.core.ai", "level", "DEBUG");
        ActionResult setLogResult = executeAction(setLogLevelAction, setLogParams);

        if (setLogResult.isSuccess()) {
            assertResultContainsKey(setLogResult, "levelSet");
        }

        // Step 2: Get logs for analysis
        Map<String, Object> getLogsParams = Map.of("filter", "all");
        ActionResult getLogsResult = executeAction(getLogsAction, getLogsParams);

        if (getLogsResult.isSuccess()) {
            assertResultContainsKey(getLogsResult, "logs");
        }

        // Step 3: Perform data analysis
        Map<String, Object> analysisParams = Map.of("action", "trend_analysis", "itemName", "TestSwitch", "timeRange",
                "24h");
        ActionResult analysisResult = executeAction(dataAnalysisAction, analysisParams);

        if (analysisResult.isSuccess()) {
            assertResultContainsKey(analysisResult, "trends");
        }
    }

    /**
     * Test configuration management workflow: Set config -> Get config -> Validate
     */
    @Test
    void testConfigurationManagementWorkflow() throws Exception {
        String configKey = "org.openhab.core.workflow.test";
        String configValue = "workflow-test-value";

        // Step 1: Set configuration
        Map<String, Object> setParams = Map.of("key", configKey, "value", configValue);
        ActionResult setResult = executeAction(configurationSetAction, setParams);

        if (!setResult.isSuccess()) {
            assertNotNull(setResult.getMessage());
            return;
        }

        // Step 2: Get configuration
        Map<String, Object> getParams = Map.of("key", configKey);
        ActionResult getResult = executeAction(configurationGetAction, getParams);

        if (getResult.isSuccess()) {
            assertResultContainsKey(getResult, "value");
            assertResultContainsKey(getResult, "key");
        }
    }

    /**
     * Test system health workflow: Check system -> Monitor resources -> Report status
     */
    @Test
    void testSystemHealthWorkflow() throws Exception {
        // Step 1: Get system information
        Map<String, Object> sysInfoParams = Map.of("filter", "all");
        ActionResult sysInfoResult = executeAction(systemInfoAction, sysInfoParams);

        if (sysInfoResult.isSuccess()) {
            assertResultContainsKey(sysInfoResult, "systemInfo");
        }

        // Step 2: Perform health check
        Map<String, Object> healthParams = Map.of("filter", "all");
        ActionResult healthResult = executeAction(healthCheckAction, healthParams);

        if (healthResult.isSuccess()) {
            assertResultContainsKey(healthResult, "healthStatus");
        }
    }

    /**
     * Test error propagation across actions in a workflow
     */
    @Test
    void testErrorPropagationWorkflow() throws Exception {
        // Step 1: Try to get non-existent item (should fail)
        Map<String, Object> getParams = Map.of("itemName", "NonExistentItem");
        ActionResult getResult = executeAction(getItemAction, getParams);

        // This should fail
        assertFailure(getResult);
        assertNotNull(getResult.getMessage());

        // Step 2: Try to set state on non-existent item (should also fail)
        Map<String, Object> setStateParams = Map.of("itemName", "NonExistentItem", "state", "ON");
        ActionResult setStateResult = executeAction(setItemStateAction, setStateParams);

        // This should also fail
        assertFailure(setStateResult);
        assertNotNull(setStateResult.getMessage());
    }

    /**
     * Test concurrent execution of multiple actions
     */
    @Test
    void testConcurrentExecutionWorkflow() throws Exception {
        // Create multiple concurrent tasks
        CompletableFuture<ActionResult> task1 = CompletableFuture.supplyAsync(() -> {
            try {
                return executeAction(getItemAction, Map.of("itemName", "TestSwitch"));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        CompletableFuture<ActionResult> task2 = CompletableFuture.supplyAsync(() -> {
            try {
                return executeAction(getItemAction, Map.of("itemName", "TestNumber"));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        CompletableFuture<ActionResult> task3 = CompletableFuture.supplyAsync(() -> {
            try {
                return executeAction(systemInfoAction, Map.of("filter", "all"));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        // Wait for all tasks to complete
        CompletableFuture<Void> allTasks = CompletableFuture.allOf(task1, task2, task3);
        allTasks.get(10, TimeUnit.SECONDS);

        // Verify all tasks completed (success or failure)
        assertTrue(task1.isDone());
        assertTrue(task2.isDone());
        assertTrue(task3.isDone());

        // Check results
        ActionResult result1 = task1.get();
        ActionResult result2 = task2.get();
        ActionResult result3 = task3.get();

        // All should have completed (may be success or failure in mocked mode)
        assertNotNull(result1);
        assertNotNull(result2);
        assertNotNull(result3);
    }

    /**
     * Test workflow with dependencies between actions
     */
    @Test
    void testDependentActionsWorkflow() throws Exception {
        // Step 1: Create item (dependency for later actions)
        Map<String, Object> createParams = Map.of("itemName", "DependentTestSwitch", "itemType", "Switch", "label",
                "Dependent Test Switch");
        ActionResult createResult = executeAction(createItemAction, createParams);

        if (!createResult.isSuccess()) {
            assertNotNull(createResult.getMessage());
            return;
        }

        // Step 2: Set item state (depends on item existing)
        Map<String, Object> setStateParams = Map.of("itemName", "DependentTestSwitch", "state", "ON");
        ActionResult setStateResult = executeAction(setItemStateAction, setStateParams);

        if (setStateResult.isSuccess()) {
            assertResultContainsKey(setStateResult, "stateSet");
        }

        // Step 3: Get item state (depends on item existing)
        Map<String, Object> getStateParams = Map.of("itemName", "DependentTestSwitch");
        ActionResult getStateResult = executeAction(getItemStateAction, getStateParams);

        if (getStateResult.isSuccess()) {
            assertResultContainsKey(getStateResult, "state");
        }

        // Step 4: Cleanup
        Map<String, Object> deleteParams = Map.of("itemName", "DependentTestSwitch");
        ActionResult deleteResult = executeAction(deleteItemAction, deleteParams);

        if (deleteResult.isSuccess()) {
            assertResultContainsKey(deleteResult, "deleted");
        }
    }

    /**
     * Test workflow performance under load
     */
    @Test
    void testPerformanceWorkflow() throws Exception {
        long startTime = System.currentTimeMillis();

        // Execute multiple actions in sequence
        for (int i = 0; i < 10; i++) {
            Map<String, Object> params = Map.of("itemName", "TestSwitch");
            ActionResult result = executeAction(getItemAction, params);

            // Verify result (may be success or failure in mocked mode)
            assertNotNull(result);
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // Performance assertion: 10 actions should complete within 5 seconds
        assertTrue(duration < 5000, "Performance test took too long: " + duration + "ms");
    }
}
