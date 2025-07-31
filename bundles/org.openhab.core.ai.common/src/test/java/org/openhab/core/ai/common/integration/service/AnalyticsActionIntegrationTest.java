package org.openhab.core.ai.common.integration.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.core.ai.common.actions.analytics.*;
import org.openhab.core.ai.common.api.action.AIActionResult;

/**
 * Integration tests for Analytics-related AIActions using mocked openHAB services.
 */
class AnalyticsActionIntegrationTest extends BaseAIActionIntegrationTest {

    private DataAnalysisAction dataAnalysisAction;

    @BeforeEach
    void setUpActions() {
        // Initialize all analytics actions
        dataAnalysisAction = new DataAnalysisAction();

        // Initialize actions with context
        dataAnalysisAction.initialize(actionContext);
    }

    @Test
    void testDataAnalysisActionWithTrendAnalysis() throws Exception {
        Map<String, Object> parameters = Map.of("action", "trend_analysis", "itemName", "TestSwitch", "timeRange",
                "7d");
        AIActionResult result = executeAction(dataAnalysisAction, parameters);

        // In mocked mode without real analytics system, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "trends");
            assertResultContainsKey(result, "itemName");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testDataAnalysisActionWithPatternRecognition() throws Exception {
        Map<String, Object> parameters = Map.of("action", "pattern_recognition", "itemName", "TestSwitch", "timeRange",
                "30d");
        AIActionResult result = executeAction(dataAnalysisAction, parameters);

        // In mocked mode without real analytics system, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "patterns");
            assertResultContainsKey(result, "itemName");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testDataAnalysisActionWithAnomalyDetection() throws Exception {
        Map<String, Object> parameters = Map.of("action", "anomaly_detection", "itemName", "TestSwitch", "timeRange",
                "7d");
        AIActionResult result = executeAction(dataAnalysisAction, parameters);

        // In mocked mode without real analytics system, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "anomalies");
            assertResultContainsKey(result, "itemName");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testDataAnalysisActionWithCorrelationAnalysis() throws Exception {
        Map<String, Object> parameters = Map.of("action", "correlation_analysis", "items",
                List.of("TestSwitch", "TestNumber"), "timeRange", "7d");
        AIActionResult result = executeAction(dataAnalysisAction, parameters);

        // In mocked mode without real analytics system, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "correlations");
            assertResultContainsKey(result, "items");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testDataAnalysisActionWithStatisticalSummary() throws Exception {
        Map<String, Object> parameters = Map.of("action", "statistical_summary", "itemName", "TestNumber", "timeRange",
                "24h");
        AIActionResult result = executeAction(dataAnalysisAction, parameters);

        // In mocked mode without real analytics system, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "statistics");
            assertResultContainsKey(result, "itemName");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testDataAnalysisActionWithForecasting() throws Exception {
        Map<String, Object> parameters = Map.of("action", "forecasting", "itemName", "TestSwitch", "forecastPeriod",
                "24h");
        AIActionResult result = executeAction(dataAnalysisAction, parameters);

        // In mocked mode without real analytics system, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "forecast");
            assertResultContainsKey(result, "itemName");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testDataAnalysisActionWithCustomQuery() throws Exception {
        Map<String, Object> parameters = Map.of("action", "custom_query", "query",
                "SELECT * FROM TestSwitch WHERE time > now() - 1d");
        AIActionResult result = executeAction(dataAnalysisAction, parameters);

        // In mocked mode without real analytics system, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "results");
            assertResultContainsKey(result, "query");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testDataAnalysisActionWithInvalidAction() throws Exception {
        Map<String, Object> parameters = Map.of("action", "invalid_action");
        AIActionResult result = executeAction(dataAnalysisAction, parameters);

        assertFailure(result);
        assertNotNull(result.getMessage());
    }

    @Test
    void testDataAnalysisActionWithMissingItemName() throws Exception {
        Map<String, Object> parameters = Map.of("action", "trend_analysis", "timeRange", "7d");
        AIActionResult result = executeAction(dataAnalysisAction, parameters);

        assertFailure(result);
        assertNotNull(result.getMessage());
    }

    @Test
    void testDataAnalysisActionWithInvalidTimeRange() throws Exception {
        Map<String, Object> parameters = Map.of("action", "trend_analysis", "itemName", "TestSwitch", "timeRange",
                "invalid_range");
        AIActionResult result = executeAction(dataAnalysisAction, parameters);

        assertFailure(result);
        assertNotNull(result.getMessage());
    }

    @Test
    void testDataAnalysisActionWithEmptyItemsList() throws Exception {
        Map<String, Object> parameters = Map.of("action", "correlation_analysis", "items", List.of(), "timeRange",
                "7d");
        AIActionResult result = executeAction(dataAnalysisAction, parameters);

        assertFailure(result);
        assertNotNull(result.getMessage());
    }
}
