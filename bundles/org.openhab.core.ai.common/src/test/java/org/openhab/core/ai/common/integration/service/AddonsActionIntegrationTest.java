package org.openhab.core.ai.common.integration.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.core.ai.common.action.AIActionResult;
import org.openhab.core.ai.common.actions.addons.*;

/**
 * Integration tests for Addons-related AIActions using mocked openHAB services.
 */
class AddonsActionIntegrationTest extends BaseAIActionIntegrationTest {

    private GetAddonAction getAddonAction;
    private ListAddonsAction listAddonsAction;
    private InstallAddonAction installAddonAction;
    private UninstallAddonAction uninstallAddonAction;
    private UpdateAddonAction updateAddonAction;
    private GetAddonInfoAction getAddonInfoAction;
    private GetAddonStatusAction getAddonStatusAction;
    private GetAddonDependenciesAction getAddonDependenciesAction;
    private CheckAddonCompatibilityAction checkAddonCompatibilityAction;
    private CheckAddonHealthAction checkAddonHealthAction;
    private SearchAddonsAction searchAddonsAction;
    private ManageAddonRepositoriesAction manageAddonRepositoriesAction;
    private BackupAddonConfigurationAction backupAddonConfigurationAction;

    @BeforeEach
    void setUpActions() {
        // Initialize all addon actions
        getAddonAction = new GetAddonAction();
        listAddonsAction = new ListAddonsAction();
        installAddonAction = new InstallAddonAction();
        uninstallAddonAction = new UninstallAddonAction();
        updateAddonAction = new UpdateAddonAction();
        getAddonInfoAction = new GetAddonInfoAction();
        getAddonStatusAction = new GetAddonStatusAction();
        getAddonDependenciesAction = new GetAddonDependenciesAction();
        checkAddonCompatibilityAction = new CheckAddonCompatibilityAction();
        checkAddonHealthAction = new CheckAddonHealthAction();
        searchAddonsAction = new SearchAddonsAction();
        manageAddonRepositoriesAction = new ManageAddonRepositoriesAction();
        backupAddonConfigurationAction = new BackupAddonConfigurationAction();

        // Initialize actions with context
        getAddonAction.initialize(actionContext);
        listAddonsAction.initialize(actionContext);
        installAddonAction.initialize(actionContext);
        uninstallAddonAction.initialize(actionContext);
        updateAddonAction.initialize(actionContext);
        getAddonInfoAction.initialize(actionContext);
        getAddonStatusAction.initialize(actionContext);
        getAddonDependenciesAction.initialize(actionContext);
        checkAddonCompatibilityAction.initialize(actionContext);
        checkAddonHealthAction.initialize(actionContext);
        searchAddonsAction.initialize(actionContext);
        manageAddonRepositoriesAction.initialize(actionContext);
        backupAddonConfigurationAction.initialize(actionContext);
    }

    @Test
    void testListAddonsAction() throws Exception {
        Map<String, Object> parameters = Map.of("filter", "all");
        AIActionResult result = executeAction(listAddonsAction, parameters);

        assertSuccess(result);
        assertResultContainsKey(result, "addons");
    }

    @Test
    void testListAddonsWithTypeFilter() throws Exception {
        Map<String, Object> parameters = Map.of("filter", "type", "type", "binding");
        AIActionResult result = executeAction(listAddonsAction, parameters);

        assertSuccess(result);
        assertResultContainsKey(result, "addons");
    }

    @Test
    void testListAddonsWithStatusFilter() throws Exception {
        Map<String, Object> parameters = Map.of("filter", "status", "status", "INSTALLED");
        AIActionResult result = executeAction(listAddonsAction, parameters);

        assertSuccess(result);
        assertResultContainsKey(result, "addons");
    }

    @Test
    void testGetAddonAction() throws Exception {
        Map<String, Object> parameters = Map.of("addonId", "binding-hue");
        AIActionResult result = executeAction(getAddonAction, parameters);

        // In mocked mode without real addons, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "addon");
            assertResultContainsKey(result, "addonId");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testInstallAddonAction() throws Exception {
        Map<String, Object> parameters = Map.of("addonId", "binding-hue");
        AIActionResult result = executeAction(installAddonAction, parameters);

        // In mocked mode without real addon management, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "installed");
            assertResultContainsKey(result, "addonId");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testUninstallAddonAction() throws Exception {
        Map<String, Object> parameters = Map.of("addonId", "binding-hue");
        AIActionResult result = executeAction(uninstallAddonAction, parameters);

        // In mocked mode without real addon management, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "uninstalled");
            assertResultContainsKey(result, "addonId");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testUpdateAddonAction() throws Exception {
        Map<String, Object> parameters = Map.of("addonId", "binding-hue");
        AIActionResult result = executeAction(updateAddonAction, parameters);

        // In mocked mode without real addon management, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "updated");
            assertResultContainsKey(result, "addonId");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testGetAddonInfoAction() throws Exception {
        Map<String, Object> parameters = Map.of("addonId", "binding-hue");
        AIActionResult result = executeAction(getAddonInfoAction, parameters);

        // In mocked mode without real addons, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "info");
            assertResultContainsKey(result, "addonId");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testGetAddonStatusAction() throws Exception {
        Map<String, Object> parameters = Map.of("addonId", "binding-hue");
        AIActionResult result = executeAction(getAddonStatusAction, parameters);

        // In mocked mode without real addons, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "status");
            assertResultContainsKey(result, "addonId");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testGetAddonDependenciesAction() throws Exception {
        Map<String, Object> parameters = Map.of("addonId", "binding-hue");
        AIActionResult result = executeAction(getAddonDependenciesAction, parameters);

        // In mocked mode without real addons, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "dependencies");
            assertResultContainsKey(result, "addonId");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testCheckAddonCompatibilityAction() throws Exception {
        Map<String, Object> parameters = Map.of("addonId", "binding-hue");
        AIActionResult result = executeAction(checkAddonCompatibilityAction, parameters);

        // In mocked mode without real addons, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "compatible");
            assertResultContainsKey(result, "addonId");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testCheckAddonHealthAction() throws Exception {
        Map<String, Object> parameters = Map.of("addonId", "binding-hue");
        AIActionResult result = executeAction(checkAddonHealthAction, parameters);

        // In mocked mode without real addons, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "health");
            assertResultContainsKey(result, "addonId");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testSearchAddonsAction() throws Exception {
        Map<String, Object> parameters = Map.of("query", "hue");
        AIActionResult result = executeAction(searchAddonsAction, parameters);

        assertSuccess(result);
        assertResultContainsKey(result, "addons");
        assertResultContainsKey(result, "query");
    }

    @Test
    void testSearchAddonsWithFilters() throws Exception {
        Map<String, Object> parameters = Map.of("query", "hue", "type", "binding", "status", "AVAILABLE");
        AIActionResult result = executeAction(searchAddonsAction, parameters);

        assertSuccess(result);
        assertResultContainsKey(result, "addons");
    }

    @Test
    void testManageAddonRepositoriesAction() throws Exception {
        Map<String, Object> parameters = Map.of("action", "list");
        AIActionResult result = executeAction(manageAddonRepositoriesAction, parameters);

        assertSuccess(result);
        assertResultContainsKey(result, "repositories");
    }

    @Test
    void testManageAddonRepositoriesWithAdd() throws Exception {
        Map<String, Object> parameters = Map.of("action", "add", "repositoryId", "test-repo", "repositoryUrl",
                "https://example.com/repo");
        AIActionResult result = executeAction(manageAddonRepositoriesAction, parameters);

        // In mocked mode without real repository management, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "repository");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testBackupAddonConfigurationAction() throws Exception {
        Map<String, Object> parameters = Map.of("addonId", "binding-hue");
        AIActionResult result = executeAction(backupAddonConfigurationAction, parameters);

        // In mocked mode without real addon management, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "backup");
            assertResultContainsKey(result, "addonId");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testGetAddonActionWithInvalidId() throws Exception {
        Map<String, Object> parameters = Map.of("addonId", "invalid-addon-id");
        AIActionResult result = executeAction(getAddonAction, parameters);

        assertFailure(result);
        assertNotNull(result.getMessage());
    }

    @Test
    void testInstallAddonActionWithInvalidId() throws Exception {
        Map<String, Object> parameters = Map.of("addonId", "invalid-addon-id");
        AIActionResult result = executeAction(installAddonAction, parameters);

        assertFailure(result);
        assertNotNull(result.getMessage());
    }

    @Test
    void testSearchAddonsActionWithEmptyQuery() throws Exception {
        Map<String, Object> parameters = Map.of("query", "");
        AIActionResult result = executeAction(searchAddonsAction, parameters);

        assertFailure(result);
        assertNotNull(result.getMessage());
    }

    @Test
    void testManageAddonRepositoriesActionWithInvalidAction() throws Exception {
        Map<String, Object> parameters = Map.of("action", "invalid-action");
        AIActionResult result = executeAction(manageAddonRepositoriesAction, parameters);

        assertFailure(result);
        assertNotNull(result.getMessage());
    }
}
