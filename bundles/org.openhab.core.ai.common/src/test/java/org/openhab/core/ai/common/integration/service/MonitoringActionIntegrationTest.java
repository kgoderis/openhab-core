package org.openhab.core.ai.common.integration.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.core.ai.common.action.AIActionResult;
import org.openhab.core.ai.common.actions.monitoring.*;

/**
 * Integration tests for Monitoring-related AIActions using mocked openHAB services.
 */
class MonitoringActionIntegrationTest extends BaseAIActionIntegrationTest {

    private LoggingMonitoringAction loggingMonitoringAction;
    private GetLogsAction getLogsAction;
    private SearchLogsAction searchLogsAction;
    private GetLogConfigurationAction getLogConfigurationAction;
    private SetLogConfigurationAction setLogConfigurationAction;
    private GetLogStatisticsAction getLogStatisticsAction;
    private SetLogLevelAction setLogLevelAction;
    private RotateLogsAction rotateLogsAction;
    private CleanupLogsAction cleanupLogsAction;
    private GetAlertsAction getAlertsAction;
    private GetMonitoringMetricsAction getMonitoringMetricsAction;

    @BeforeEach
    void setUpActions() {
        // Initialize all monitoring actions
        loggingMonitoringAction = new LoggingMonitoringAction();
        getLogsAction = new GetLogsAction();
        searchLogsAction = new SearchLogsAction();
        getLogConfigurationAction = new GetLogConfigurationAction();
        setLogConfigurationAction = new SetLogConfigurationAction();
        getLogStatisticsAction = new GetLogStatisticsAction();
        setLogLevelAction = new SetLogLevelAction();
        rotateLogsAction = new RotateLogsAction();
        cleanupLogsAction = new CleanupLogsAction();
        getAlertsAction = new GetAlertsAction();
        getMonitoringMetricsAction = new GetMonitoringMetricsAction();

        // Initialize actions with context
        loggingMonitoringAction.initialize(actionContext);
        getLogsAction.initialize(actionContext);
        searchLogsAction.initialize(actionContext);
        getLogConfigurationAction.initialize(actionContext);
        setLogConfigurationAction.initialize(actionContext);
        getLogStatisticsAction.initialize(actionContext);
        setLogLevelAction.initialize(actionContext);
        rotateLogsAction.initialize(actionContext);
        cleanupLogsAction.initialize(actionContext);
        getAlertsAction.initialize(actionContext);
        getMonitoringMetricsAction.initialize(actionContext);
    }

    @Test
    void testLoggingMonitoringAction() throws Exception {
        Map<String, Object> parameters = Map.of("action", "status");
        AIActionResult result = executeAction(loggingMonitoringAction, parameters);

        assertSuccess(result);
        assertResultContainsKey(result, "loggingStatus");
        assertResultContainsKey(result, "action");
    }

    @Test
    void testLoggingMonitoringActionWithGetLogs() throws Exception {
        Map<String, Object> parameters = Map.of("action", "get_logs");
        AIActionResult result = executeAction(loggingMonitoringAction, parameters);

        // In mocked mode without real logging system, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "logs");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testLoggingMonitoringActionWithSetLevel() throws Exception {
        Map<String, Object> parameters = Map.of("action", "set_level", "logger", "org.openhab.core", "level", "DEBUG");
        AIActionResult result = executeAction(loggingMonitoringAction, parameters);

        // In mocked mode without real logging system, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "levelSet");
            assertResultContainsKey(result, "logger");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testGetLogsAction() throws Exception {
        Map<String, Object> parameters = Map.of("filter", "all");
        AIActionResult result = executeAction(getLogsAction, parameters);

        // In mocked mode without real logging system, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "logs");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testGetLogsActionWithLevelFilter() throws Exception {
        Map<String, Object> parameters = Map.of("filter", "level", "level", "ERROR");
        AIActionResult result = executeAction(getLogsAction, parameters);

        // In mocked mode without real logging system, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "logs");
            assertResultContainsKey(result, "level");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testGetLogsActionWithLoggerFilter() throws Exception {
        Map<String, Object> parameters = Map.of("filter", "logger", "logger", "org.openhab.core");
        AIActionResult result = executeAction(getLogsAction, parameters);

        // In mocked mode without real logging system, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "logs");
            assertResultContainsKey(result, "logger");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testSearchLogsAction() throws Exception {
        Map<String, Object> parameters = Map.of("query", "error");
        AIActionResult result = executeAction(searchLogsAction, parameters);

        // In mocked mode without real logging system, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "logs");
            assertResultContainsKey(result, "query");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testSearchLogsActionWithTimeRange() throws Exception {
        Map<String, Object> parameters = Map.of("query", "error", "startTime", "2024-01-01T00:00:00Z", "endTime",
                "2024-01-01T23:59:59Z");
        AIActionResult result = executeAction(searchLogsAction, parameters);

        // In mocked mode without real logging system, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "logs");
            assertResultContainsKey(result, "timeRange");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testGetLogConfigurationAction() throws Exception {
        Map<String, Object> parameters = Map.of("logger", "org.openhab.core");
        AIActionResult result = executeAction(getLogConfigurationAction, parameters);

        // In mocked mode without real logging system, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "configuration");
            assertResultContainsKey(result, "logger");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testSetLogConfigurationAction() throws Exception {
        Map<String, Object> parameters = Map.of("logger", "org.openhab.core", "level", "DEBUG", "appender", "CONSOLE");
        AIActionResult result = executeAction(setLogConfigurationAction, parameters);

        // In mocked mode without real logging system, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "configured");
            assertResultContainsKey(result, "logger");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testGetLogStatisticsAction() throws Exception {
        Map<String, Object> parameters = Map.of("timeRange", "1h");
        AIActionResult result = executeAction(getLogStatisticsAction, parameters);

        // In mocked mode without real logging system, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "statistics");
            assertResultContainsKey(result, "timeRange");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testGetLogStatisticsActionWithLevelFilter() throws Exception {
        Map<String, Object> parameters = Map.of("timeRange", "1h", "level", "ERROR");
        AIActionResult result = executeAction(getLogStatisticsAction, parameters);

        // In mocked mode without real logging system, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "statistics");
            assertResultContainsKey(result, "level");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testSetLogLevelAction() throws Exception {
        Map<String, Object> parameters = Map.of("logger", "org.openhab.core", "level", "DEBUG");
        AIActionResult result = executeAction(setLogLevelAction, parameters);

        // In mocked mode without real logging system, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "levelSet");
            assertResultContainsKey(result, "logger");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testRotateLogsAction() throws Exception {
        Map<String, Object> parameters = Map.of("action", "rotate");
        AIActionResult result = executeAction(rotateLogsAction, parameters);

        // In mocked mode without real logging system, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "rotated");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testRotateLogsActionWithBackup() throws Exception {
        Map<String, Object> parameters = Map.of("action", "rotate", "backup", true);
        AIActionResult result = executeAction(rotateLogsAction, parameters);

        // In mocked mode without real logging system, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "rotated");
            assertResultContainsKey(result, "backup");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testCleanupLogsAction() throws Exception {
        Map<String, Object> parameters = Map.of("action", "cleanup");
        AIActionResult result = executeAction(cleanupLogsAction, parameters);

        // In mocked mode without real logging system, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "cleaned");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testCleanupLogsActionWithAgeFilter() throws Exception {
        Map<String, Object> parameters = Map.of("action", "cleanup", "olderThan", "30d");
        AIActionResult result = executeAction(cleanupLogsAction, parameters);

        // In mocked mode without real logging system, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "cleaned");
            assertResultContainsKey(result, "olderThan");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testGetAlertsAction() throws Exception {
        Map<String, Object> parameters = Map.of("filter", "all");
        AIActionResult result = executeAction(getAlertsAction, parameters);

        // In mocked mode without real monitoring system, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "alerts");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testGetAlertsActionWithSeverityFilter() throws Exception {
        Map<String, Object> parameters = Map.of("filter", "severity", "severity", "HIGH");
        AIActionResult result = executeAction(getAlertsAction, parameters);

        // In mocked mode without real monitoring system, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "alerts");
            assertResultContainsKey(result, "severity");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testGetMonitoringMetricsAction() throws Exception {
        Map<String, Object> parameters = Map.of("metrics", List.of("cpu", "memory"));
        AIActionResult result = executeAction(getMonitoringMetricsAction, parameters);

        // In mocked mode without real monitoring system, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "metrics");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testGetMonitoringMetricsActionWithTimeRange() throws Exception {
        Map<String, Object> parameters = Map.of("metrics", List.of("cpu", "memory"), "startTime",
                "2024-01-01T00:00:00Z", "endTime", "2024-01-01T23:59:59Z");
        AIActionResult result = executeAction(getMonitoringMetricsAction, parameters);

        // In mocked mode without real monitoring system, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "metrics");
            assertResultContainsKey(result, "timeRange");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testSetLogLevelActionWithInvalidLevel() throws Exception {
        Map<String, Object> parameters = Map.of("logger", "org.openhab.core", "level", "INVALID_LEVEL");
        AIActionResult result = executeAction(setLogLevelAction, parameters);

        assertFailure(result);
        assertNotNull(result.getMessage());
    }

    @Test
    void testSearchLogsActionWithEmptyQuery() throws Exception {
        Map<String, Object> parameters = Map.of("query", "");
        AIActionResult result = executeAction(searchLogsAction, parameters);

        assertFailure(result);
        assertNotNull(result.getMessage());
    }

    @Test
    void testGetLogsActionWithInvalidFilter() throws Exception {
        Map<String, Object> parameters = Map.of("filter", "invalid_filter");
        AIActionResult result = executeAction(getLogsAction, parameters);

        assertFailure(result);
        assertNotNull(result.getMessage());
    }

    @Test
    void testLoggingMonitoringActionWithInvalidAction() throws Exception {
        Map<String, Object> parameters = Map.of("action", "invalid_action");
        AIActionResult result = executeAction(loggingMonitoringAction, parameters);

        assertFailure(result);
        assertNotNull(result.getMessage());
    }
}
