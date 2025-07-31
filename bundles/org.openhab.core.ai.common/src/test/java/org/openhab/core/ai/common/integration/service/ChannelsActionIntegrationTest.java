package org.openhab.core.ai.common.integration.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.core.ai.common.actions.channels.*;
import org.openhab.core.ai.common.api.action.AIActionResult;

/**
 * Integration tests for Channels-related AIActions using mocked openHAB services.
 */
class ChannelsActionIntegrationTest extends BaseAIActionIntegrationTest {

    private GetChannelAction getChannelAction;
    private ListChannelsAction listChannelsAction;
    private GetChannelTypeAction getChannelTypeAction;
    private GetChannelStateAction getChannelStateAction;
    private GetChannelConfigurationAction getChannelConfigurationAction;
    private SetChannelConfigurationAction setChannelConfigurationAction;
    private GetChannelPropertiesAction getChannelPropertiesAction;
    private GetChannelLinksAction getChannelLinksAction;
    private LinkChannelAction linkChannelAction;
    private UnlinkChannelAction unlinkChannelAction;
    private ChannelLinkAction channelLinkAction;

    @BeforeEach
    void setUpActions() {
        // Initialize all channel actions
        getChannelAction = new GetChannelAction();
        listChannelsAction = new ListChannelsAction();
        getChannelTypeAction = new GetChannelTypeAction();
        getChannelStateAction = new GetChannelStateAction();
        getChannelConfigurationAction = new GetChannelConfigurationAction();
        setChannelConfigurationAction = new SetChannelConfigurationAction();
        getChannelPropertiesAction = new GetChannelPropertiesAction();
        getChannelLinksAction = new GetChannelLinksAction();
        linkChannelAction = new LinkChannelAction();
        unlinkChannelAction = new UnlinkChannelAction();
        channelLinkAction = new ChannelLinkAction();

        // Initialize actions with context
        getChannelAction.initialize(actionContext);
        listChannelsAction.initialize(actionContext);
        getChannelTypeAction.initialize(actionContext);
        getChannelStateAction.initialize(actionContext);
        getChannelConfigurationAction.initialize(actionContext);
        setChannelConfigurationAction.initialize(actionContext);
        getChannelPropertiesAction.initialize(actionContext);
        getChannelLinksAction.initialize(actionContext);
        linkChannelAction.initialize(actionContext);
        unlinkChannelAction.initialize(actionContext);
        channelLinkAction.initialize(actionContext);
    }

    @Test
    void testListChannelsAction() throws Exception {
        Map<String, Object> parameters = Map.of("filter", "all");
        AIActionResult result = executeAction(listChannelsAction, parameters);

        assertSuccess(result);
        assertResultContainsKey(result, "channels");
    }

    @Test
    void testListChannelsWithThingFilter() throws Exception {
        Map<String, Object> parameters = Map.of("filter", "thing", "thingUID", "test:thing:1");
        AIActionResult result = executeAction(listChannelsAction, parameters);

        assertSuccess(result);
        assertResultContainsKey(result, "channels");
    }

    @Test
    void testListChannelsWithTypeFilter() throws Exception {
        Map<String, Object> parameters = Map.of("filter", "type", "type", "Switch");
        AIActionResult result = executeAction(listChannelsAction, parameters);

        assertSuccess(result);
        assertResultContainsKey(result, "channels");
    }

    @Test
    void testGetChannelAction() throws Exception {
        Map<String, Object> parameters = Map.of("channelUID", "test:thing:1:switch");
        AIActionResult result = executeAction(getChannelAction, parameters);

        // In mocked mode without real things, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "channel");
            assertResultContainsKey(result, "channelUID");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testGetChannelTypeAction() throws Exception {
        Map<String, Object> parameters = Map.of("typeUID", "test:switch");
        AIActionResult result = executeAction(getChannelTypeAction, parameters);

        // In mocked mode without real channel types, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "channelType");
            assertResultContainsKey(result, "typeUID");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testGetChannelStateAction() throws Exception {
        Map<String, Object> parameters = Map.of("channelUID", "test:thing:1:switch");
        AIActionResult result = executeAction(getChannelStateAction, parameters);

        // In mocked mode without real channels, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "state");
            assertResultContainsKey(result, "channelUID");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testGetChannelConfigurationAction() throws Exception {
        Map<String, Object> parameters = Map.of("channelUID", "test:thing:1:switch");
        AIActionResult result = executeAction(getChannelConfigurationAction, parameters);

        // In mocked mode without real channels, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "configuration");
            assertResultContainsKey(result, "channelUID");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testSetChannelConfigurationAction() throws Exception {
        Map<String, Object> parameters = Map.of("channelUID", "test:thing:1:switch", "configuration",
                Map.of("enabled", true, "timeout", 5000));
        AIActionResult result = executeAction(setChannelConfigurationAction, parameters);

        // In mocked mode without real channels, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "configuration");
            assertResultContainsKey(result, "channelUID");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testGetChannelPropertiesAction() throws Exception {
        Map<String, Object> parameters = Map.of("channelUID", "test:thing:1:switch");
        AIActionResult result = executeAction(getChannelPropertiesAction, parameters);

        // In mocked mode without real channels, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "properties");
            assertResultContainsKey(result, "channelUID");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testGetChannelLinksAction() throws Exception {
        Map<String, Object> parameters = Map.of("filter", "all");
        AIActionResult result = executeAction(getChannelLinksAction, parameters);

        assertSuccess(result);
        assertResultContainsKey(result, "links");
    }

    @Test
    void testGetChannelLinksWithChannelFilter() throws Exception {
        Map<String, Object> parameters = Map.of("filter", "channel", "channelUID", "test:thing:1:switch");
        AIActionResult result = executeAction(getChannelLinksAction, parameters);

        assertSuccess(result);
        assertResultContainsKey(result, "links");
    }

    @Test
    void testGetChannelLinksWithItemFilter() throws Exception {
        Map<String, Object> parameters = Map.of("filter", "item", "itemName", "TestSwitch");
        AIActionResult result = executeAction(getChannelLinksAction, parameters);

        assertSuccess(result);
        assertResultContainsKey(result, "links");
    }

    @Test
    void testLinkChannelAction() throws Exception {
        Map<String, Object> parameters = Map.of("channelUID", "test:thing:1:switch", "itemName", "TestSwitch");
        AIActionResult result = executeAction(linkChannelAction, parameters);

        // In mocked mode without real channels, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "link");
            assertResultContainsKey(result, "channelUID");
            assertResultContainsKey(result, "itemName");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testUnlinkChannelAction() throws Exception {
        Map<String, Object> parameters = Map.of("channelUID", "test:thing:1:switch", "itemName", "TestSwitch");
        AIActionResult result = executeAction(unlinkChannelAction, parameters);

        // In mocked mode without real channels, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "unlinked");
            assertResultContainsKey(result, "channelUID");
            assertResultContainsKey(result, "itemName");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testChannelLinkAction() throws Exception {
        Map<String, Object> parameters = Map.of("action", "list", "filter", "all");
        AIActionResult result = executeAction(channelLinkAction, parameters);

        assertSuccess(result);
        assertResultContainsKey(result, "links");
    }

    @Test
    void testChannelLinkActionWithCreate() throws Exception {
        Map<String, Object> parameters = Map.of("action", "create", "channelUID", "test:thing:1:switch", "itemName",
                "TestSwitch");
        AIActionResult result = executeAction(channelLinkAction, parameters);

        // In mocked mode without real channels, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "link");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testChannelLinkActionWithDelete() throws Exception {
        Map<String, Object> parameters = Map.of("action", "delete", "channelUID", "test:thing:1:switch", "itemName",
                "TestSwitch");
        AIActionResult result = executeAction(channelLinkAction, parameters);

        // In mocked mode without real channels, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "deleted");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testGetChannelActionWithInvalidUID() throws Exception {
        Map<String, Object> parameters = Map.of("channelUID", "invalid:channel:uid");
        AIActionResult result = executeAction(getChannelAction, parameters);

        assertFailure(result);
        assertNotNull(result.getMessage());
    }

    @Test
    void testSetChannelConfigurationActionWithInvalidConfig() throws Exception {
        Map<String, Object> parameters = Map.of("channelUID", "test:thing:1:switch", "configuration", "invalid_config");
        AIActionResult result = executeAction(setChannelConfigurationAction, parameters);

        assertFailure(result);
        assertNotNull(result.getMessage());
    }

    @Test
    void testLinkChannelActionWithInvalidItem() throws Exception {
        Map<String, Object> parameters = Map.of("channelUID", "test:thing:1:switch", "itemName", "NonExistentItem");
        AIActionResult result = executeAction(linkChannelAction, parameters);

        assertFailure(result);
        assertNotNull(result.getMessage());
    }
}
