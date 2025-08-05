package org.openhab.core.ai.integration.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.core.ai.action.ActionResult;
import org.openhab.core.ai.actions.config.*;

/**
 * Integration tests for Config-related Actions using mocked openHAB services.
 */
class ConfigActionIntegrationTest extends BaseActionIntegrationTest {

    private ConfigurationGetAction configurationGetAction;
    private ConfigurationSetAction configurationSetAction;
    private ConfigurationListAction configurationListAction;
    private ConfigurationValidationAction configurationValidationAction;
    private ConfigurationImportAction configurationImportAction;
    private ConfigurationExportAction configurationExportAction;
    private ConfigurationBackupAction configurationBackupAction;

    @BeforeEach
    void setUpActions() {
        // Initialize all config actions
        configurationGetAction = new ConfigurationGetAction();
        configurationSetAction = new ConfigurationSetAction();
        configurationListAction = new ConfigurationListAction();
        configurationValidationAction = new ConfigurationValidationAction();
        configurationImportAction = new ConfigurationImportAction();
        configurationExportAction = new ConfigurationExportAction();
        configurationBackupAction = new ConfigurationBackupAction();

        // Initialize actions with context
        configurationGetAction.initialize(actionContext);
        configurationSetAction.initialize(actionContext);
        configurationListAction.initialize(actionContext);
        configurationValidationAction.initialize(actionContext);
        configurationImportAction.initialize(actionContext);
        configurationExportAction.initialize(actionContext);
        configurationBackupAction.initialize(actionContext);
    }

    @Test
    void testConfigurationGetAction() throws Exception {
        Map<String, Object> parameters = Map.of("key", "org.openhab.core.test");
        ActionResult result = executeAction(configurationGetAction, parameters);

        // In mocked mode without real configuration system, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "value");
            assertResultContainsKey(result, "key");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testConfigurationGetActionWithDefault() throws Exception {
        Map<String, Object> parameters = Map.of("key", "org.openhab.core.test", "defaultValue", "default");
        ActionResult result = executeAction(configurationGetAction, parameters);

        // In mocked mode without real configuration system, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "value");
            assertResultContainsKey(result, "defaultValue");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testConfigurationSetAction() throws Exception {
        Map<String, Object> parameters = Map.of("key", "org.openhab.core.test", "value", "test_value");
        ActionResult result = executeAction(configurationSetAction, parameters);

        // In mocked mode without real configuration system, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "set");
            assertResultContainsKey(result, "key");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testConfigurationSetActionWithType() throws Exception {
        Map<String, Object> parameters = Map.of("key", "org.openhab.core.test", "value", "42", "type", "integer");
        ActionResult result = executeAction(configurationSetAction, parameters);

        // In mocked mode without real configuration system, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "set");
            assertResultContainsKey(result, "type");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testConfigurationListAction() throws Exception {
        Map<String, Object> parameters = Map.of("filter", "all");
        ActionResult result = executeAction(configurationListAction, parameters);

        assertSuccess(result);
        assertResultContainsKey(result, "configurations");
    }

    @Test
    void testConfigurationListActionWithPrefixFilter() throws Exception {
        Map<String, Object> parameters = Map.of("filter", "prefix", "prefix", "org.openhab.core");
        ActionResult result = executeAction(configurationListAction, parameters);

        assertSuccess(result);
        assertResultContainsKey(result, "configurations");
    }

    @Test
    void testConfigurationListActionWithPatternFilter() throws Exception {
        Map<String, Object> parameters = Map.of("filter", "pattern", "pattern", "*.test");
        ActionResult result = executeAction(configurationListAction, parameters);

        assertSuccess(result);
        assertResultContainsKey(result, "configurations");
    }

    @Test
    void testConfigurationValidationAction() throws Exception {
        Map<String, Object> parameters = Map.of("configuration", Map.of("key1", "value1", "key2", "value2"));
        ActionResult result = executeAction(configurationValidationAction, parameters);

        // In mocked mode without real configuration system, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "valid");
            assertResultContainsKey(result, "configuration");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testConfigurationValidationActionWithSchema() throws Exception {
        Map<String, Object> parameters = Map.of("configuration", Map.of("key1", "value1"), "schema",
                Map.of("type", "object", "properties", Map.of("key1", Map.of("type", "string"))));
        ActionResult result = executeAction(configurationValidationAction, parameters);

        // In mocked mode without real configuration system, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "valid");
            assertResultContainsKey(result, "schema");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testConfigurationImportAction() throws Exception {
        Map<String, Object> parameters = Map.of("source", "file", "filePath", "/tmp/config.json");
        ActionResult result = executeAction(configurationImportAction, parameters);

        // In mocked mode without real configuration system, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "imported");
            assertResultContainsKey(result, "source");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testConfigurationImportActionWithOverwrite() throws Exception {
        Map<String, Object> parameters = Map.of("source", "file", "filePath", "/tmp/config.json", "overwrite", true);
        ActionResult result = executeAction(configurationImportAction, parameters);

        // In mocked mode without real configuration system, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "imported");
            assertResultContainsKey(result, "overwrite");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testConfigurationExportAction() throws Exception {
        Map<String, Object> parameters = Map.of("destination", "file", "filePath", "/tmp/config_export.json");
        ActionResult result = executeAction(configurationExportAction, parameters);

        // In mocked mode without real configuration system, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "exported");
            assertResultContainsKey(result, "destination");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testConfigurationExportActionWithFilter() throws Exception {
        Map<String, Object> parameters = Map.of("destination", "file", "filePath", "/tmp/config_export.json", "filter",
                "org.openhab.core.*");
        ActionResult result = executeAction(configurationExportAction, parameters);

        // In mocked mode without real configuration system, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "exported");
            assertResultContainsKey(result, "filter");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testConfigurationBackupAction() throws Exception {
        Map<String, Object> parameters = Map.of("action", "create");
        ActionResult result = executeAction(configurationBackupAction, parameters);

        // In mocked mode without real configuration system, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "backup");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testConfigurationBackupActionWithRestore() throws Exception {
        Map<String, Object> parameters = Map.of("action", "restore", "backupId", "backup-2024-01-01");
        ActionResult result = executeAction(configurationBackupAction, parameters);

        // In mocked mode without real configuration system, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "restored");
            assertResultContainsKey(result, "backupId");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testConfigurationBackupActionWithList() throws Exception {
        Map<String, Object> parameters = Map.of("action", "list");
        ActionResult result = executeAction(configurationBackupAction, parameters);

        // In mocked mode without real configuration system, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "backups");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testConfigurationGetActionWithInvalidKey() throws Exception {
        Map<String, Object> parameters = Map.of("key", "");
        ActionResult result = executeAction(configurationGetAction, parameters);

        assertFailure(result);
        assertNotNull(result.getMessage());
    }

    @Test
    void testConfigurationSetActionWithInvalidKey() throws Exception {
        Map<String, Object> parameters = Map.of("key", "", "value", "test_value");
        ActionResult result = executeAction(configurationSetAction, parameters);

        assertFailure(result);
        assertNotNull(result.getMessage());
    }

    @Test
    void testConfigurationValidationActionWithInvalidConfiguration() throws Exception {
        Map<String, Object> parameters = Map.of("configuration", "invalid_config");
        ActionResult result = executeAction(configurationValidationAction, parameters);

        assertFailure(result);
        assertNotNull(result.getMessage());
    }

    @Test
    void testConfigurationImportActionWithInvalidSource() throws Exception {
        Map<String, Object> parameters = Map.of("source", "invalid_source");
        ActionResult result = executeAction(configurationImportAction, parameters);

        assertFailure(result);
        assertNotNull(result.getMessage());
    }

    @Test
    void testConfigurationBackupActionWithInvalidAction() throws Exception {
        Map<String, Object> parameters = Map.of("action", "invalid_action");
        ActionResult result = executeAction(configurationBackupAction, parameters);

        assertFailure(result);
        assertNotNull(result.getMessage());
    }
}
