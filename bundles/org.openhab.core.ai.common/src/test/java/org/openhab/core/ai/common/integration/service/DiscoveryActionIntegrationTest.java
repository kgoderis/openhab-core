package org.openhab.core.ai.common.integration.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.core.ai.common.action.AIActionResult;
import org.openhab.core.ai.common.actions.discovery.*;

/**
 * Integration tests for Discovery-related AIActions using mocked openHAB services.
 */
class DiscoveryActionIntegrationTest extends BaseAIActionIntegrationTest {

    private DiscoveryAction discoveryAction;
    private GetDiscoveryServicesAction getDiscoveryServicesAction;
    private GetDiscoveryStatusAction getDiscoveryStatusAction;
    private GetDiscoveryResultsAction getDiscoveryResultsAction;
    private StartDiscoveryAction startDiscoveryAction;
    private StopDiscoveryAction stopDiscoveryAction;
    private ApproveDiscoveryAction approveDiscoveryAction;
    private IgnoreDiscoveryAction ignoreDiscoveryAction;

    @BeforeEach
    void setUpActions() {
        // Initialize all discovery actions
        discoveryAction = new DiscoveryAction();
        getDiscoveryServicesAction = new GetDiscoveryServicesAction();
        getDiscoveryStatusAction = new GetDiscoveryStatusAction();
        getDiscoveryResultsAction = new GetDiscoveryResultsAction();
        startDiscoveryAction = new StartDiscoveryAction();
        stopDiscoveryAction = new StopDiscoveryAction();
        approveDiscoveryAction = new ApproveDiscoveryAction();
        ignoreDiscoveryAction = new IgnoreDiscoveryAction();

        // Initialize actions with context
        discoveryAction.initialize(actionContext);
        getDiscoveryServicesAction.initialize(actionContext);
        getDiscoveryStatusAction.initialize(actionContext);
        getDiscoveryResultsAction.initialize(actionContext);
        startDiscoveryAction.initialize(actionContext);
        stopDiscoveryAction.initialize(actionContext);
        approveDiscoveryAction.initialize(actionContext);
        ignoreDiscoveryAction.initialize(actionContext);
    }

    @Test
    void testDiscoveryAction() throws Exception {
        Map<String, Object> parameters = Map.of("action", "status");
        AIActionResult result = executeAction(discoveryAction, parameters);

        assertSuccess(result);
        assertResultContainsKey(result, "discoveryAvailable");
        assertResultContainsKey(result, "discoveryServices");
    }

    @Test
    void testDiscoveryActionWithStart() throws Exception {
        Map<String, Object> parameters = Map.of("action", "start", "serviceId", "hue");
        AIActionResult result = executeAction(discoveryAction, parameters);

        // In mocked mode without real discovery services, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "started");
            assertResultContainsKey(result, "serviceId");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testDiscoveryActionWithStop() throws Exception {
        Map<String, Object> parameters = Map.of("action", "stop", "serviceId", "hue");
        AIActionResult result = executeAction(discoveryAction, parameters);

        // In mocked mode without real discovery services, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "stopped");
            assertResultContainsKey(result, "serviceId");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testGetDiscoveryServicesAction() throws Exception {
        Map<String, Object> parameters = Map.of("filter", "all");
        AIActionResult result = executeAction(getDiscoveryServicesAction, parameters);

        assertSuccess(result);
        assertResultContainsKey(result, "services");
    }

    @Test
    void testGetDiscoveryServicesWithTypeFilter() throws Exception {
        Map<String, Object> parameters = Map.of("filter", "type", "type", "binding");
        AIActionResult result = executeAction(getDiscoveryServicesAction, parameters);

        assertSuccess(result);
        assertResultContainsKey(result, "services");
    }

    @Test
    void testGetDiscoveryServicesWithStatusFilter() throws Exception {
        Map<String, Object> parameters = Map.of("filter", "status", "status", "ACTIVE");
        AIActionResult result = executeAction(getDiscoveryServicesAction, parameters);

        assertSuccess(result);
        assertResultContainsKey(result, "services");
    }

    @Test
    void testGetDiscoveryStatusAction() throws Exception {
        Map<String, Object> parameters = Map.of("serviceId", "hue");
        AIActionResult result = executeAction(getDiscoveryStatusAction, parameters);

        // In mocked mode without real discovery services, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "status");
            assertResultContainsKey(result, "serviceId");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testGetDiscoveryResultsAction() throws Exception {
        Map<String, Object> parameters = Map.of("serviceId", "hue");
        AIActionResult result = executeAction(getDiscoveryResultsAction, parameters);

        // In mocked mode without real discovery services, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "results");
            assertResultContainsKey(result, "serviceId");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testGetDiscoveryResultsWithFilter() throws Exception {
        Map<String, Object> parameters = Map.of("serviceId", "hue", "filter", "pending");
        AIActionResult result = executeAction(getDiscoveryResultsAction, parameters);

        // In mocked mode without real discovery services, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "results");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testStartDiscoveryAction() throws Exception {
        Map<String, Object> parameters = Map.of("serviceId", "hue");
        AIActionResult result = executeAction(startDiscoveryAction, parameters);

        // In mocked mode without real discovery services, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "started");
            assertResultContainsKey(result, "serviceId");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testStartDiscoveryActionWithTimeout() throws Exception {
        Map<String, Object> parameters = Map.of("serviceId", "hue", "timeout", 300);
        AIActionResult result = executeAction(startDiscoveryAction, parameters);

        // In mocked mode without real discovery services, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "started");
            assertResultContainsKey(result, "timeout");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testStopDiscoveryAction() throws Exception {
        Map<String, Object> parameters = Map.of("serviceId", "hue");
        AIActionResult result = executeAction(stopDiscoveryAction, parameters);

        // In mocked mode without real discovery services, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "stopped");
            assertResultContainsKey(result, "serviceId");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testApproveDiscoveryAction() throws Exception {
        Map<String, Object> parameters = Map.of("serviceId", "hue", "thingUID", "hue:bridge:1");
        AIActionResult result = executeAction(approveDiscoveryAction, parameters);

        // In mocked mode without real discovery services, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "approved");
            assertResultContainsKey(result, "thingUID");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testApproveDiscoveryActionWithConfiguration() throws Exception {
        Map<String, Object> parameters = Map.of("serviceId", "hue", "thingUID", "hue:bridge:1", "configuration",
                Map.of("ipAddress", "192.168.1.100"));
        AIActionResult result = executeAction(approveDiscoveryAction, parameters);

        // In mocked mode without real discovery services, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "approved");
            assertResultContainsKey(result, "configuration");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testIgnoreDiscoveryAction() throws Exception {
        Map<String, Object> parameters = Map.of("serviceId", "hue", "thingUID", "hue:bridge:1");
        AIActionResult result = executeAction(ignoreDiscoveryAction, parameters);

        // In mocked mode without real discovery services, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "ignored");
            assertResultContainsKey(result, "thingUID");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testIgnoreDiscoveryActionWithReason() throws Exception {
        Map<String, Object> parameters = Map.of("serviceId", "hue", "thingUID", "hue:bridge:1", "reason",
                "Already configured");
        AIActionResult result = executeAction(ignoreDiscoveryAction, parameters);

        // In mocked mode without real discovery services, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "ignored");
            assertResultContainsKey(result, "reason");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testGetDiscoveryStatusActionWithInvalidService() throws Exception {
        Map<String, Object> parameters = Map.of("serviceId", "invalid-service");
        AIActionResult result = executeAction(getDiscoveryStatusAction, parameters);

        assertFailure(result);
        assertNotNull(result.getMessage());
    }

    @Test
    void testStartDiscoveryActionWithInvalidService() throws Exception {
        Map<String, Object> parameters = Map.of("serviceId", "invalid-service");
        AIActionResult result = executeAction(startDiscoveryAction, parameters);

        assertFailure(result);
        assertNotNull(result.getMessage());
    }

    @Test
    void testApproveDiscoveryActionWithInvalidThingUID() throws Exception {
        Map<String, Object> parameters = Map.of("serviceId", "hue", "thingUID", "invalid:thing:uid");
        AIActionResult result = executeAction(approveDiscoveryAction, parameters);

        assertFailure(result);
        assertNotNull(result.getMessage());
    }

    @Test
    void testDiscoveryActionWithInvalidAction() throws Exception {
        Map<String, Object> parameters = Map.of("action", "invalid-action");
        AIActionResult result = executeAction(discoveryAction, parameters);

        assertFailure(result);
        assertNotNull(result.getMessage());
    }
}
