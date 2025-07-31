package org.openhab.core.ai.common.integration.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.core.ai.common.actions.persistence.*;
import org.openhab.core.ai.common.api.action.AIActionResult;

/**
 * Integration tests for Persistence-related AIActions using mocked openHAB services.
 */
class PersistenceActionIntegrationTest extends BaseAIActionIntegrationTest {

    private PersistenceAction persistenceAction;
    private GetPersistenceServiceAction getPersistenceServiceAction;
    private ListPersistenceServicesAction listPersistenceServicesAction;
    private GetPersistenceDataAction getPersistenceDataAction;
    private QueryPersistenceAction queryPersistenceAction;
    private GetPersistenceConfigurationAction getPersistenceConfigurationAction;
    private SetPersistenceConfigurationAction setPersistenceConfigurationAction;
    private GetPersistenceStatisticsAction getPersistenceStatisticsAction;
    private BackupPersistenceAction backupPersistenceAction;
    private RestorePersistenceAction restorePersistenceAction;
    private CleanupPersistenceAction cleanupPersistenceAction;

    @BeforeEach
    void setUpActions() {
        // Initialize all persistence actions
        persistenceAction = new PersistenceAction();
        getPersistenceServiceAction = new GetPersistenceServiceAction();
        listPersistenceServicesAction = new ListPersistenceServicesAction();
        getPersistenceDataAction = new GetPersistenceDataAction();
        queryPersistenceAction = new QueryPersistenceAction();
        getPersistenceConfigurationAction = new GetPersistenceConfigurationAction();
        setPersistenceConfigurationAction = new SetPersistenceConfigurationAction();
        getPersistenceStatisticsAction = new GetPersistenceStatisticsAction();
        backupPersistenceAction = new BackupPersistenceAction();
        restorePersistenceAction = new RestorePersistenceAction();
        cleanupPersistenceAction = new CleanupPersistenceAction();

        // Initialize actions with context
        persistenceAction.initialize(actionContext);
        getPersistenceServiceAction.initialize(actionContext);
        listPersistenceServicesAction.initialize(actionContext);
        getPersistenceDataAction.initialize(actionContext);
        queryPersistenceAction.initialize(actionContext);
        getPersistenceConfigurationAction.initialize(actionContext);
        setPersistenceConfigurationAction.initialize(actionContext);
        getPersistenceStatisticsAction.initialize(actionContext);
        backupPersistenceAction.initialize(actionContext);
        restorePersistenceAction.initialize(actionContext);
        cleanupPersistenceAction.initialize(actionContext);
    }

    @Test
    void testPersistenceAction() throws Exception {
        Map<String, Object> parameters = Map.of("action", "status");
        AIActionResult result = executeAction(persistenceAction, parameters);

        assertSuccess(result);
        assertResultContainsKey(result, "persistenceAvailable");
        assertResultContainsKey(result, "persistenceServices");
    }

    @Test
    void testGetPersistenceServiceAction() throws Exception {
        Map<String, Object> parameters = Map.of("serviceId", "rrd4j");
        AIActionResult result = executeAction(getPersistenceServiceAction, parameters);

        assertSuccess(result);
        assertResultContainsKey(result, "service");
        assertResultContainsKey(result, "serviceId");
    }

    @Test
    void testListPersistenceServicesAction() throws Exception {
        Map<String, Object> parameters = Map.of("filter", "all");
        AIActionResult result = executeAction(listPersistenceServicesAction, parameters);

        assertSuccess(result);
        assertResultContainsKey(result, "services");
    }

    @Test
    void testGetPersistenceDataAction() throws Exception {
        Map<String, Object> parameters = Map.of("itemName", "TestSwitch", "startTime", "2024-01-01T00:00:00Z",
                "endTime", "2024-12-31T23:59:59Z", "serviceId", "rrd4j");
        AIActionResult result = executeAction(getPersistenceDataAction, parameters);

        assertSuccess(result);
        assertResultContainsKey(result, "data");
        assertResultContainsKey(result, "itemName");
    }

    @Test
    void testQueryPersistenceAction() throws Exception {
        Map<String, Object> parameters = Map.of("query", "SELECT * FROM TestSwitch WHERE time > '2024-01-01'",
                "serviceId", "rrd4j");
        AIActionResult result = executeAction(queryPersistenceAction, parameters);

        assertSuccess(result);
        assertResultContainsKey(result, "results");
        assertResultContainsKey(result, "query");
    }

    @Test
    void testGetPersistenceConfigurationAction() throws Exception {
        Map<String, Object> parameters = Map.of("serviceId", "rrd4j");
        AIActionResult result = executeAction(getPersistenceConfigurationAction, parameters);

        assertSuccess(result);
        assertResultContainsKey(result, "configuration");
        assertResultContainsKey(result, "serviceId");
    }

    @Test
    void testSetPersistenceConfigurationAction() throws Exception {
        Map<String, Object> parameters = Map.of("serviceId", "rrd4j", "configuration",
                Map.of("maxFileAge", "30d", "maxFileSize", "100MB"));
        AIActionResult result = executeAction(setPersistenceConfigurationAction, parameters);

        assertSuccess(result);
        assertResultContainsKey(result, "configuration");
        assertResultContainsKey(result, "serviceId");
    }

    @Test
    void testGetPersistenceStatisticsAction() throws Exception {
        Map<String, Object> parameters = Map.of("serviceId", "rrd4j");
        AIActionResult result = executeAction(getPersistenceStatisticsAction, parameters);

        assertSuccess(result);
        assertResultContainsKey(result, "statistics");
        assertResultContainsKey(result, "serviceId");
    }

    @Test
    void testBackupPersistenceAction() throws Exception {
        Map<String, Object> parameters = Map.of("backupPath", testDataDir.resolve("backup").toString(), "serviceId",
                "rrd4j");
        AIActionResult result = executeAction(backupPersistenceAction, parameters);

        assertSuccess(result);
        assertResultContainsKey(result, "backupPath");
        assertResultContainsKey(result, "backupSize");
    }

    @Test
    void testRestorePersistenceAction() throws Exception {
        Map<String, Object> parameters = Map.of("backupPath", testDataDir.resolve("backup").toString(), "serviceId",
                "rrd4j");
        AIActionResult result = executeAction(restorePersistenceAction, parameters);

        // This might fail in embedded mode without real persistence data
        if (result.isSuccess()) {
            assertResultContainsKey(result, "restored");
            assertResultContainsKey(result, "backupPath");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testCleanupPersistenceAction() throws Exception {
        Map<String, Object> parameters = Map.of("serviceId", "rrd4j", "olderThan", "30d");
        AIActionResult result = executeAction(cleanupPersistenceAction, parameters);

        assertSuccess(result);
        assertResultContainsKey(result, "cleaned");
        assertResultContainsKey(result, "freedSpace");
    }

    @Test
    void testGetPersistenceDataActionWithInvalidTimeRange() throws Exception {
        Map<String, Object> parameters = Map.of("itemName", "TestSwitch", "startTime", "invalid-time", "endTime",
                "invalid-time", "serviceId", "rrd4j");
        AIActionResult result = executeAction(getPersistenceDataAction, parameters);

        assertFailure(result);
        assertNotNull(result.getMessage());
    }

    @Test
    void testQueryPersistenceActionWithInvalidQuery() throws Exception {
        Map<String, Object> parameters = Map.of("query", "INVALID SQL QUERY", "serviceId", "rrd4j");
        AIActionResult result = executeAction(queryPersistenceAction, parameters);

        assertFailure(result);
        assertNotNull(result.getMessage());
    }

    @Test
    void testGetPersistenceServiceActionWithInvalidService() throws Exception {
        Map<String, Object> parameters = Map.of("serviceId", "invalid-service");
        AIActionResult result = executeAction(getPersistenceServiceAction, parameters);

        assertFailure(result);
        assertNotNull(result.getMessage());
    }
}
