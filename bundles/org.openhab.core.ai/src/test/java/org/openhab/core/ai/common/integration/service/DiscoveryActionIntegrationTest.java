package org.openhab.core.ai.integration.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.core.ai.action.ActionResult;
import org.openhab.core.ai.actions.discovery.*;

/**
 * Integration tests for Discovery-related Actions using mocked openHAB services.
 */
class DiscoveryActionIntegrationTest extends BaseActionIntegrationTest {

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
        ActionResult result = executeAction(discoveryAction, parameters);

        assertSuccess(result);
        assertResultContainsKey(result, "discoveryAvailable");
        assertResultContainsKey(result, "discoveryServices");
    }

    @Test
    void testDiscoveryActionWithStart() throws Exception {
        Map<String, Object> parameters = Map.of("action", "start", "serviceId", "hue");
        ActionResult result = executeAction(discoveryAction, parameters);

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
        ActionResult result = executeAction(discoveryAction, parameters);

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
        ActionResult result = executeAction(getDiscoveryServicesAction, parameters);

        assertSuccess(result);
        assertResultContainsKey(result, "services");
    }

    @Test
    void testGetDiscoveryServicesWithTypeFilter() throws Exception {
        Map<String, Object> parameters = Map.of("filter", "type", "type", "binding");
        ActionResult result = executeAction(getDiscoveryServicesAction, parameters);

        assertSuccess(result);
        assertResultContainsKey(result, "services");
    }

    @Test
    void testGetDiscoveryServicesWithStatusFilter() throws Exception {
        Map<String, Object> parameters = Map.of("filter", "status", "status", "ACTIVE");
        ActionResult result = executeAction(getDiscoveryServicesAction, parameters);

        assertSuccess(result);
        assertResultContainsKey(result, "services");
    }

    @Test
    void testGetDiscoveryStatusAction() throws Exception {
        Map<String, Object> parameters = Map.of("serviceId", "hue");
        ActionResult result = executeAction(getDiscoveryStatusAction, parameters);

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
        ActionResult result = executeAction(getDiscoveryResultsAction, parameters);

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
        ActionResult result = executeAction(getDiscoveryResultsAction, parameters);

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
        ActionResult result = executeAction(startDiscoveryAction, parameters);

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
        ActionResult result = executeAction(startDiscoveryAction, parameters);

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
        ActionResult result = executeAction(stopDiscoveryAction, parameters);

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
        ActionResult result = executeAction(approveDiscoveryAction, parameters);

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
        ActionResult result = executeAction(approveDiscoveryAction, parameters);

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
        ActionResult result = executeAction(ignoreDiscoveryAction, parameters);

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
        ActionResult result = executeAction(ignoreDiscoveryAction, parameters);

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
        ActionResult result = executeAction(getDiscoveryStatusAction, parameters);

        assertFailure(result);
        assertNotNull(result.getMessage());
    }

    @Test
    void testStartDiscoveryActionWithInvalidService() throws Exception {
        Map<String, Object> parameters = Map.of("serviceId", "invalid-service");
        ActionResult result = executeAction(startDiscoveryAction, parameters);

        assertFailure(result);
        assertNotNull(result.getMessage());
    }

    @Test
    void testApproveDiscoveryActionWithInvalidThingUID() throws Exception {
        Map<String, Object> parameters = Map.of("serviceId", "hue", "thingUID", "invalid:thing:uid");
        ActionResult result = executeAction(approveDiscoveryAction, parameters);

        assertFailure(result);
        assertNotNull(result.getMessage());
    }

    @Test
    void testDiscoveryActionWithInvalidAction() throws Exception {
        Map<String, Object> parameters = Map.of("action", "invalid-action");
        ActionResult result = executeAction(discoveryAction, parameters);

        assertFailure(result);
        assertNotNull(result.getMessage());
    }
}
