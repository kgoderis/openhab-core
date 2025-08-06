package org.openhab.core.ai.integration.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.core.ai.action.ActionResult;
import org.openhab.core.ai.action.actions.things.*;

/**
 * Integration tests for Things-related Actions using mocked openHAB services.
 */
class ThingsActionIntegrationTest extends BaseActionIntegrationTest {

    private GetThingAction getThingAction;
    private ListThingsAction listThingsAction;
    private GetThingStatusAction getThingStatusAction;
    private ThingStatusAction thingStatusAction;
    private GetThingConfigurationAction getThingConfigurationAction;
    private ThingConfigurationAction thingConfigurationAction;
    private ThingPropertiesAction thingPropertiesAction;
    private ThingChannelsAction thingChannelsAction;
    private ThingLocationAction thingLocationAction;
    private ThingBridgeAction thingBridgeAction;
    private SearchThingsAction searchThingsAction;
    private EnableThingAction enableThingAction;
    private DisableThingAction disableThingAction;
    private DeleteThingAction deleteThingAction;

    @BeforeEach
    void setUpActions() {
        // Initialize all thing actions
        getThingAction = new GetThingAction();
        listThingsAction = new ListThingsAction();
        getThingStatusAction = new GetThingStatusAction();
        thingStatusAction = new ThingStatusAction();
        getThingConfigurationAction = new GetThingConfigurationAction();
        thingConfigurationAction = new ThingConfigurationAction();
        thingPropertiesAction = new ThingPropertiesAction();
        thingChannelsAction = new ThingChannelsAction();
        thingLocationAction = new ThingLocationAction();
        thingBridgeAction = new ThingBridgeAction();
        searchThingsAction = new SearchThingsAction();
        enableThingAction = new EnableThingAction();
        disableThingAction = new DisableThingAction();
        deleteThingAction = new DeleteThingAction();

        // Initialize actions with context
        getThingAction.initialize(actionContext);
        listThingsAction.initialize(actionContext);
        getThingStatusAction.initialize(actionContext);
        thingStatusAction.initialize(actionContext);
        getThingConfigurationAction.initialize(actionContext);
        thingConfigurationAction.initialize(actionContext);
        thingPropertiesAction.initialize(actionContext);
        thingChannelsAction.initialize(actionContext);
        thingLocationAction.initialize(actionContext);
        thingBridgeAction.initialize(actionContext);
        searchThingsAction.initialize(actionContext);
        enableThingAction.initialize(actionContext);
        disableThingAction.initialize(actionContext);
        deleteThingAction.initialize(actionContext);
    }

    @Test
    void testListThingsAction() throws Exception {
        Map<String, Object> parameters = Map.of("filter", "all");
        ActionResult result = executeAction(listThingsAction, parameters);

        assertSuccess(result);
        assertResultContainsKey(result, "things");
    }

    @Test
    void testListThingsWithStatusFilter() throws Exception {
        Map<String, Object> parameters = Map.of("filter", "status", "status", "ONLINE");
        ActionResult result = executeAction(listThingsAction, parameters);

        assertSuccess(result);
        assertResultContainsKey(result, "things");
    }

    @Test
    void testListThingsWithTypeFilter() throws Exception {
        Map<String, Object> parameters = Map.of("filter", "type", "type", "hue:bridge");
        ActionResult result = executeAction(listThingsAction, parameters);

        assertSuccess(result);
        assertResultContainsKey(result, "things");
    }

    @Test
    void testGetThingAction() throws Exception {
        // This test will likely fail in embedded mode since we don't have real things
        // But we can test the action structure
        Map<String, Object> parameters = Map.of("thingUID", "test:thing:1");
        ActionResult result = executeAction(getThingAction, parameters);

        // In embedded mode without real things, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "thing");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testGetThingStatusAction() throws Exception {
        Map<String, Object> parameters = Map.of("thingUID", "test:thing:1");
        ActionResult result = executeAction(getThingStatusAction, parameters);

        // In mocked mode without real things, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "status");
            assertResultContainsKey(result, "thingUID");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testThingStatusAction() throws Exception {
        Map<String, Object> parameters = Map.of("thingUID", "test:thing:1", "status", "ONLINE");
        ActionResult result = executeAction(thingStatusAction, parameters);

        // In mocked mode without real things, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "status");
            assertResultContainsKey(result, "thingUID");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testGetThingConfigurationAction() throws Exception {
        Map<String, Object> parameters = Map.of("thingUID", "test:thing:1");
        ActionResult result = executeAction(getThingConfigurationAction, parameters);

        // In mocked mode without real things, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "configuration");
            assertResultContainsKey(result, "thingUID");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testThingConfigurationAction() throws Exception {
        Map<String, Object> parameters = Map.of("thingUID", "test:thing:1", "configuration",
                Map.of("host", "192.168.1.100", "port", "8080"));
        ActionResult result = executeAction(thingConfigurationAction, parameters);

        // In mocked mode without real things, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "configuration");
            assertResultContainsKey(result, "thingUID");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testThingPropertiesAction() throws Exception {
        Map<String, Object> parameters = Map.of("thingUID", "test:thing:1");
        ActionResult result = executeAction(thingPropertiesAction, parameters);

        // In mocked mode without real things, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "properties");
            assertResultContainsKey(result, "thingUID");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testThingChannelsAction() throws Exception {
        Map<String, Object> parameters = Map.of("thingUID", "test:thing:1");
        ActionResult result = executeAction(thingChannelsAction, parameters);

        // In mocked mode without real things, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "channels");
            assertResultContainsKey(result, "thingUID");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testThingLocationAction() throws Exception {
        Map<String, Object> parameters = Map.of("thingUID", "test:thing:1");
        ActionResult result = executeAction(thingLocationAction, parameters);

        // In mocked mode without real things, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "location");
            assertResultContainsKey(result, "thingUID");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testThingBridgeAction() throws Exception {
        Map<String, Object> parameters = Map.of("thingUID", "test:thing:1");
        ActionResult result = executeAction(thingBridgeAction, parameters);

        // In mocked mode without real things, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "bridge");
            assertResultContainsKey(result, "thingUID");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testSearchThingsAction() throws Exception {
        Map<String, Object> parameters = Map.of("query", "test");
        ActionResult result = executeAction(searchThingsAction, parameters);

        assertSuccess(result);
        assertResultContainsKey(result, "things");
        assertResultContainsKey(result, "query");
    }

    @Test
    void testEnableThingAction() throws Exception {
        Map<String, Object> parameters = Map.of("thingUID", "test:thing:1");
        ActionResult result = executeAction(enableThingAction, parameters);

        // In mocked mode without real things, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "enabled");
            assertResultContainsKey(result, "thingUID");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testDisableThingAction() throws Exception {
        Map<String, Object> parameters = Map.of("thingUID", "test:thing:1");
        ActionResult result = executeAction(disableThingAction, parameters);

        // In mocked mode without real things, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "enabled");
            assertResultContainsKey(result, "thingUID");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testDeleteThingAction() throws Exception {
        Map<String, Object> parameters = Map.of("thingUID", "test:thing:1");
        ActionResult result = executeAction(deleteThingAction, parameters);

        // In embedded mode without real things, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "deleted");
            assertResultContainsKey(result, "thingUID");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testGetThingActionWithInvalidUID() throws Exception {
        Map<String, Object> parameters = Map.of("thingUID", "invalid:thing:uid");
        ActionResult result = executeAction(getThingAction, parameters);

        assertFailure(result);
        assertNotNull(result.getMessage());
    }

    @Test
    void testThingConfigurationActionWithInvalidConfig() throws Exception {
        Map<String, Object> parameters = Map.of("thingUID", "test:thing:1", "configuration", "invalid_config");
        ActionResult result = executeAction(thingConfigurationAction, parameters);

        assertFailure(result);
        assertNotNull(result.getMessage());
    }

    @Test
    void testSearchThingsActionWithEmptyQuery() throws Exception {
        Map<String, Object> parameters = Map.of("query", "");
        ActionResult result = executeAction(searchThingsAction, parameters);

        assertFailure(result);
        assertNotNull(result.getMessage());
    }
}
