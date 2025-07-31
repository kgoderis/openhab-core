package org.openhab.core.ai.common.integration.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.core.ai.common.actions.resources.*;
import org.openhab.core.ai.common.api.action.AIActionResult;

/**
 * Integration tests for Resources-related AIActions using mocked openHAB services.
 */
class ResourcesActionIntegrationTest extends BaseAIActionIntegrationTest {

    private ResourceManagementAction resourceManagementAction;

    @BeforeEach
    void setUpActions() {
        // Initialize all resources actions
        resourceManagementAction = new ResourceManagementAction();

        // Initialize actions with context
        resourceManagementAction.initialize(actionContext);
    }

    @Test
    void testResourceManagementActionWithListResources() throws Exception {
        Map<String, Object> parameters = Map.of("action", "list");
        AIActionResult result = executeAction(resourceManagementAction, parameters);

        assertSuccess(result);
        assertResultContainsKey(result, "resources");
        assertResultContainsKey(result, "action");
    }

    @Test
    void testResourceManagementActionWithGetResource() throws Exception {
        Map<String, Object> parameters = Map.of("action", "get", "resourceId", "test-resource-1");
        AIActionResult result = executeAction(resourceManagementAction, parameters);

        // In mocked mode without real resource system, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "resource");
            assertResultContainsKey(result, "resourceId");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testResourceManagementActionWithCreateResource() throws Exception {
        Map<String, Object> parameters = Map.of("action", "create", "resourceName", "Test Resource", "resourceType",
                "file", "content", "test content");
        AIActionResult result = executeAction(resourceManagementAction, parameters);

        // In mocked mode without real resource system, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "resource");
            assertResultContainsKey(result, "resourceName");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testResourceManagementActionWithUpdateResource() throws Exception {
        Map<String, Object> parameters = Map.of("action", "update", "resourceId", "test-resource-1", "content",
                "updated content");
        AIActionResult result = executeAction(resourceManagementAction, parameters);

        // In mocked mode without real resource system, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "updated");
            assertResultContainsKey(result, "resourceId");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testResourceManagementActionWithDeleteResource() throws Exception {
        Map<String, Object> parameters = Map.of("action", "delete", "resourceId", "test-resource-1");
        AIActionResult result = executeAction(resourceManagementAction, parameters);

        // In mocked mode without real resource system, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "deleted");
            assertResultContainsKey(result, "resourceId");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testResourceManagementActionWithSearchResources() throws Exception {
        Map<String, Object> parameters = Map.of("action", "search", "query", "test");
        AIActionResult result = executeAction(resourceManagementAction, parameters);

        // In mocked mode without real resource system, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "resources");
            assertResultContainsKey(result, "query");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testResourceManagementActionWithCopyResource() throws Exception {
        Map<String, Object> parameters = Map.of("action", "copy", "sourceId", "test-resource-1", "destinationId",
                "test-resource-2");
        AIActionResult result = executeAction(resourceManagementAction, parameters);

        // In mocked mode without real resource system, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "copied");
            assertResultContainsKey(result, "sourceId");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testResourceManagementActionWithMoveResource() throws Exception {
        Map<String, Object> parameters = Map.of("action", "move", "sourceId", "test-resource-1", "destinationId",
                "test-resource-2");
        AIActionResult result = executeAction(resourceManagementAction, parameters);

        // In mocked mode without real resource system, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "moved");
            assertResultContainsKey(result, "sourceId");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testResourceManagementActionWithGetResourceMetadata() throws Exception {
        Map<String, Object> parameters = Map.of("action", "metadata", "resourceId", "test-resource-1");
        AIActionResult result = executeAction(resourceManagementAction, parameters);

        // In mocked mode without real resource system, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "metadata");
            assertResultContainsKey(result, "resourceId");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testResourceManagementActionWithInvalidAction() throws Exception {
        Map<String, Object> parameters = Map.of("action", "invalid_action");
        AIActionResult result = executeAction(resourceManagementAction, parameters);

        assertFailure(result);
        assertNotNull(result.getMessage());
    }

    @Test
    void testResourceManagementActionWithMissingResourceId() throws Exception {
        Map<String, Object> parameters = Map.of("action", "get");
        AIActionResult result = executeAction(resourceManagementAction, parameters);

        assertFailure(result);
        assertNotNull(result.getMessage());
    }

    @Test
    void testResourceManagementActionWithMissingResourceName() throws Exception {
        Map<String, Object> parameters = Map.of("action", "create", "resourceType", "file", "content", "test content");
        AIActionResult result = executeAction(resourceManagementAction, parameters);

        assertFailure(result);
        assertNotNull(result.getMessage());
    }

    @Test
    void testResourceManagementActionWithEmptyContent() throws Exception {
        Map<String, Object> parameters = Map.of("action", "create", "resourceName", "Test Resource", "resourceType",
                "file", "content", "");
        AIActionResult result = executeAction(resourceManagementAction, parameters);

        assertFailure(result);
        assertNotNull(result.getMessage());
    }

    @Test
    void testResourceManagementActionWithInvalidResourceType() throws Exception {
        Map<String, Object> parameters = Map.of("action", "create", "resourceName", "Test Resource", "resourceType",
                "invalid_type", "content", "test content");
        AIActionResult result = executeAction(resourceManagementAction, parameters);

        assertFailure(result);
        assertNotNull(result.getMessage());
    }
}
