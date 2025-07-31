package org.openhab.core.ai.common.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.ai.common.actions.analytics.DataAnalysisAction;
import org.openhab.core.ai.common.api.action.AIActionContext;
import org.openhab.core.ai.common.api.action.AIActionException;
import org.openhab.core.ai.common.api.action.AIActionMetadata;
import org.openhab.core.ai.common.api.action.AIActionResult;
import org.openhab.core.ai.common.api.action.AIActionValidationResult;
import org.openhab.core.items.ItemRegistry;

/**
 * Unit tests for DataAnalysisAction.
 * 
 * Tests cover:
 * - Action metadata (ID, name, description, category, version)
 * - Parameter validation (valid and invalid scenarios)
 * - Execution (data analysis scenarios)
 * - Async execution
 * - Error handling
 * - Schema generation
 * - Capabilities verification
 * - Lifecycle management
 */
@ExtendWith(MockitoExtension.class)
class DataAnalysisActionTest {

    @Mock
    private AIActionContext mockContext;

    @Mock
    private ItemRegistry mockItemRegistry;

    private DataAnalysisAction action;

    @BeforeEach
    void setUp() {
        action = new DataAnalysisAction();

        // Setup mock context
        when(mockContext.getProtocol()).thenReturn("mcp");
        when(mockContext.getClientId()).thenReturn("test-client");
        when(mockContext.getSessionId()).thenReturn("test-session");

        // Initialize the action
        action.initialize(mockContext);
    }

    @Test
    void testGetActionId() {
        assertEquals("openhab.data.analysis", action.getActionId());
    }

    @Test
    void testGetActionName() {
        assertEquals("Data Analysis and Reporting", action.getActionName());
    }

    @Test
    void testGetDescription() {
        assertNotNull(action.getDescription());
        assertTrue(action.getDescription().contains("Advanced data analysis"));
    }

    @Test
    void testGetCategory() {
        assertEquals("analytics", action.getCategory());
    }

    @Test
    void testGetVersion() {
        assertEquals("1.0.0", action.getVersion());
    }

    @Test
    void testValidateParametersWithValidQueryData() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("operation", "query_data");
        parameters.put("itemName", "TestItem");
        parameters.put("startTime", "2024-01-01T00:00:00Z");
        parameters.put("endTime", "2024-01-02T00:00:00Z");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertTrue(result.isValid());
        assertEquals(parameters, result.getSanitizedParameters());
    }

    @Test
    void testValidateParametersWithValidAggregateData() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("operation", "aggregate_data");
        parameters.put("itemNames", java.util.List.of("Item1", "Item2"));
        parameters.put("aggregation", "avg");
        parameters.put("groupBy", "hour");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertTrue(result.isValid());
        assertEquals(parameters, result.getSanitizedParameters());
    }

    @Test
    void testValidateParametersWithValidAnalyzeTrends() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("operation", "analyze_trends");
        parameters.put("itemName", "TestItem");
        parameters.put("interval", "1d");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertTrue(result.isValid());
        assertEquals(parameters, result.getSanitizedParameters());
    }

    @Test
    void testValidateParametersWithValidGenerateReport() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("operation", "generate_report");
        parameters.put("itemNames", java.util.List.of("Item1", "Item2", "Item3"));

        AIActionValidationResult result = action.validateParameters(parameters);

        assertTrue(result.isValid());
        assertEquals(parameters, result.getSanitizedParameters());
    }

    @Test
    void testValidateParametersWithValidTransformData() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("operation", "transform_data");
        parameters.put("itemName", "TestItem");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertTrue(result.isValid());
        assertEquals(parameters, result.getSanitizedParameters());
    }

    @Test
    void testValidateParametersWithValidVisualizeData() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("operation", "visualize_data");
        parameters.put("itemNames", java.util.List.of("Item1", "Item2"));

        AIActionValidationResult result = action.validateParameters(parameters);

        assertTrue(result.isValid());
        assertEquals(parameters, result.getSanitizedParameters());
    }

    @Test
    void testValidateParametersWithValidCorrelationAnalysis() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("operation", "correlation_analysis");
        parameters.put("itemNames", java.util.List.of("Item1", "Item2", "Item3"));

        AIActionValidationResult result = action.validateParameters(parameters);

        assertTrue(result.isValid());
        assertEquals(parameters, result.getSanitizedParameters());
    }

    @Test
    void testValidateParametersWithValidAnomalyDetection() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("operation", "anomaly_detection");
        parameters.put("itemName", "TestItem");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertTrue(result.isValid());
        assertEquals(parameters, result.getSanitizedParameters());
    }

    @Test
    void testValidateParametersWithValidTimeSeriesAnalysis() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("operation", "time_series_analysis");
        parameters.put("itemName", "TestItem");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertTrue(result.isValid());
        assertEquals(parameters, result.getSanitizedParameters());
    }

    @Test
    void testValidateParametersWithValidStatisticalSummary() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("operation", "statistical_summary");
        parameters.put("itemNames", java.util.List.of("Item1", "Item2"));

        AIActionValidationResult result = action.validateParameters(parameters);

        assertTrue(result.isValid());
        assertEquals(parameters, result.getSanitizedParameters());
    }

    @Test
    void testValidateParametersWithValidExportData() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("operation", "export_data");
        parameters.put("itemName", "TestItem");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertTrue(result.isValid());
        assertEquals(parameters, result.getSanitizedParameters());
    }

    @Test
    void testValidateParametersWithValidDataProfiling() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("operation", "data_profiling");
        parameters.put("itemName", "TestItem");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertTrue(result.isValid());
        assertEquals(parameters, result.getSanitizedParameters());
    }

    @Test
    void testValidateParametersWithMissingOperation() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("itemName", "TestItem");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.contains("operation")));
    }

    @Test
    void testValidateParametersWithInvalidOperation() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("operation", "invalid_operation");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.contains("operation")));
    }

    @Test
    void testValidateParametersWithInvalidAggregation() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("operation", "aggregate_data");
        parameters.put("aggregation", "invalid_agg");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.contains("aggregation")));
    }

    @Test
    void testValidateParametersWithInvalidGroupBy() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("operation", "aggregate_data");
        parameters.put("groupBy", "invalid_group");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.contains("groupBy")));
    }

    @Test
    void testValidateParametersWithInvalidLimit() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("operation", "query_data");
        parameters.put("limit", -1);

        AIActionValidationResult result = action.validateParameters(parameters);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.contains("limit")));
    }

    @Test
    void testExecuteQueryData() throws AIActionException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("operation", "query_data");
        parameters.put("itemName", "TestItem");
        parameters.put("startTime", "2024-01-01T00:00:00Z");
        parameters.put("endTime", "2024-01-02T00:00:00Z");

        AIActionResult result = action.execute(parameters, mockContext);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals("query_data", data.get("operation"));
        assertNotNull(data.get("timestamp"));
        assertNotNull(data.get("data"));
    }

    @Test
    void testExecuteAggregateData() throws AIActionException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("operation", "aggregate_data");
        parameters.put("itemNames", java.util.List.of("Item1", "Item2"));
        parameters.put("aggregation", "avg");
        parameters.put("groupBy", "hour");

        AIActionResult result = action.execute(parameters, mockContext);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals("aggregate_data", data.get("operation"));
        assertNotNull(data.get("timestamp"));
        assertNotNull(data.get("aggregatedData"));
    }

    @Test
    void testExecuteAnalyzeTrends() throws AIActionException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("operation", "analyze_trends");
        parameters.put("itemName", "TestItem");
        parameters.put("interval", "1d");

        AIActionResult result = action.execute(parameters, mockContext);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals("analyze_trends", data.get("operation"));
        assertNotNull(data.get("timestamp"));
        assertNotNull(data.get("trends"));
    }

    @Test
    void testExecuteGenerateReport() throws AIActionException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("operation", "generate_report");
        parameters.put("itemNames", java.util.List.of("Item1", "Item2"));

        AIActionResult result = action.execute(parameters, mockContext);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals("generate_report", data.get("operation"));
        assertNotNull(data.get("timestamp"));
        assertNotNull(data.get("report"));
    }

    @Test
    void testExecuteCorrelationAnalysis() throws AIActionException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("operation", "correlation_analysis");
        parameters.put("itemNames", java.util.List.of("Item1", "Item2", "Item3"));

        AIActionResult result = action.execute(parameters, mockContext);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals("correlation_analysis", data.get("operation"));
        assertNotNull(data.get("timestamp"));
        assertNotNull(data.get("correlations"));
    }

    @Test
    void testExecuteAnomalyDetection() throws AIActionException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("operation", "anomaly_detection");
        parameters.put("itemName", "TestItem");

        AIActionResult result = action.execute(parameters, mockContext);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals("anomaly_detection", data.get("operation"));
        assertNotNull(data.get("timestamp"));
        assertNotNull(data.get("anomalies"));
    }

    @Test
    void testExecuteTimeSeriesAnalysis() throws AIActionException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("operation", "time_series_analysis");
        parameters.put("itemName", "TestItem");

        AIActionResult result = action.execute(parameters, mockContext);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals("time_series_analysis", data.get("operation"));
        assertNotNull(data.get("timestamp"));
        assertNotNull(data.get("timeSeries"));
    }

    @Test
    void testExecuteStatisticalSummary() throws AIActionException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("operation", "statistical_summary");
        parameters.put("itemNames", java.util.List.of("Item1", "Item2"));

        AIActionResult result = action.execute(parameters, mockContext);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals("statistical_summary", data.get("operation"));
        assertNotNull(data.get("timestamp"));
        assertNotNull(data.get("statistics"));
    }

    @Test
    void testExecuteAsync() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("operation", "query_data");
        parameters.put("itemName", "TestItem");

        CompletableFuture<AIActionResult> future = action.executeAsync(parameters, mockContext);

        assertNotNull(future);
        assertTrue(future.isDone());

        AIActionResult result = future.join();
        assertNotNull(result);
        assertTrue(result.isSuccess());
    }

    @Test
    void testExecuteWithInvalidParameters() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("invalidParam", "invalidValue");

        assertThrows(AIActionException.class, () -> {
            action.execute(parameters, mockContext);
        });
    }

    @Test
    void testExecuteWithInvalidContext() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("operation", "query_data");
        AIActionContext invalidContext = AIActionContext.builder().build();

        assertThrows(AIActionException.class, () -> {
            action.execute(parameters, invalidContext);
        });
    }

    @Test
    void testExecuteAsyncWithInvalidParameters() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("invalidParam", "invalidValue");

        assertThrows(AIActionException.class, () -> {
            action.executeAsync(parameters, mockContext);
        });
    }

    @Test
    void testExecuteAsyncWithInvalidContext() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("operation", "query_data");
        AIActionContext invalidContext = AIActionContext.builder().build();

        assertThrows(AIActionException.class, () -> {
            action.executeAsync(parameters, invalidContext);
        });
    }

    @Test
    void testGetParameterSchema() {
        Map<String, Object> schema = action.getParameterSchema();

        assertNotNull(schema);
        assertEquals("object", schema.get("type"));

        @SuppressWarnings("unchecked")
        Map<String, Object> properties = (Map<String, Object>) schema.get("properties");
        assertNotNull(properties);
        assertTrue(properties.containsKey("operation"));
        assertTrue(properties.containsKey("itemName"));
        assertTrue(properties.containsKey("itemNames"));
        assertTrue(properties.containsKey("startTime"));
        assertTrue(properties.containsKey("endTime"));
        assertTrue(properties.containsKey("interval"));
        assertTrue(properties.containsKey("aggregation"));
        assertTrue(properties.containsKey("groupBy"));
        assertTrue(properties.containsKey("limit"));

        @SuppressWarnings("unchecked")
        java.util.List<String> required = (java.util.List<String>) schema.get("required");
        assertNotNull(required);
        assertTrue(required.contains("operation"));
    }

    @Test
    void testGetReturnSchema() {
        Map<String, Object> schema = action.getReturnSchema();

        assertNotNull(schema);
        assertEquals("object", schema.get("type"));

        @SuppressWarnings("unchecked")
        Map<String, Object> properties = (Map<String, Object>) schema.get("properties");
        assertNotNull(properties);
        assertTrue(properties.containsKey("operation"));
        assertTrue(properties.containsKey("timestamp"));
        assertTrue(properties.containsKey("data"));
        assertTrue(properties.containsKey("aggregatedData"));
        assertTrue(properties.containsKey("trends"));
        assertTrue(properties.containsKey("report"));
        assertTrue(properties.containsKey("correlations"));
        assertTrue(properties.containsKey("anomalies"));
        assertTrue(properties.containsKey("timeSeries"));
        assertTrue(properties.containsKey("statistics"));
    }

    @Test
    void testGetMetadata() {
        AIActionMetadata metadata = action.getMetadata();

        assertNotNull(metadata);
        assertEquals("1.0.0", metadata.getVersion());
        assertNotNull(metadata.getDescription());
        assertNotNull(metadata.getTags());
    }

    @Test
    void testGetCapabilities() {
        Map<String, Object> capabilities = action.getCapabilities();

        assertNotNull(capabilities);
        assertTrue(capabilities.containsKey("supportsAsync"));
        assertTrue(capabilities.containsKey("supportsValidation"));
        assertTrue(capabilities.containsKey("supportsQuerying"));
        assertTrue(capabilities.containsKey("supportsAggregation"));
        assertTrue(capabilities.containsKey("supportsVisualization"));
    }

    @Test
    void testIsReady() {
        assertTrue(action.isReady());
    }

    @Test
    void testCleanup() {
        // Should not throw any exception
        assertDoesNotThrow(() -> action.cleanup());
    }

    @Test
    void testInitialize() {
        // Should not throw any exception
        assertDoesNotThrow(() -> action.initialize(mockContext));
    }
}
