/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.core.ai.tool.sampling.models;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.ai.common.monitoring.api.MetricKey;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
import org.openhab.core.ai.common.monitoring.service.OperationRecorder;
import org.openhab.core.ai.common.monitoring.snapshot.ExecutionMetricsSnapshot;

/**
 * Unit tests for SamplingModel MetricsService integration
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
@ExtendWith(MockitoExtension.class)
class SamplingModelTest {

    @Mock
    private MetricsService metricsService;

    @Mock
    private OperationRecorder operationRecorder;

    @Mock
    private ExecutionMetricsSnapshot sampleSnapshot;

    @Mock
    private ExecutionMetricsSnapshot cacheHitSnapshot;

    @Mock
    private ExecutionMetricsSnapshot cacheMissSnapshot;

    private SamplingModel samplingModel;

    @BeforeEach
    void setUp() {
        samplingModel = new SamplingModel("test-model", "Test Model", "Test sampling model", "test-type",
                Map.of("param1", "value1"), Map.of("config1", "configValue1"));

        // Use reflection to inject the mocked MetricsService
        try {
            java.lang.reflect.Field metricsServiceField = SamplingModel.class.getDeclaredField("metricsService");
            metricsServiceField.setAccessible(true);
            metricsServiceField.set(samplingModel, metricsService);
        } catch (Exception e) {
            fail("Failed to inject MetricsService mock: " + e.getMessage());
        }
    }

    @Test
    void testGenerateSampleRecordsCacheMissMetrics() {
        // Given
        Map<String, Object> input = Map.of("param", "value");
        when(metricsService.recordOperation("sampling_model", "cache_miss")).thenReturn(operationRecorder);
        when(metricsService.recordOperation("sampling_model", "sample_generation")).thenReturn(operationRecorder);
        when(operationRecorder.withSuccess(anyBoolean())).thenReturn(operationRecorder);
        when(operationRecorder.withDuration(anyLong())).thenReturn(operationRecorder);
        when(operationRecorder.withData(anyString(), any())).thenReturn(operationRecorder);
        doNothing().when(operationRecorder).record();

        // When
        Object result = samplingModel.generateSample(input);

        // Then
        assertNotNull(result);
        verify(metricsService).recordOperation("sampling_model", "cache_miss");
        verify(metricsService).recordOperation("sampling_model", "sample_generation");
        verify(operationRecorder, atLeast(2)).withSuccess(true);
        verify(operationRecorder, atLeast(1)).withData("modelId", "test-model");
        verify(operationRecorder, atLeast(1)).withData("modelType", "test-type");
        verify(operationRecorder, atLeast(2)).record();
    }

    @Test
    void testGenerateSampleRecordsCacheHitMetrics() {
        // Given
        Map<String, Object> input = Map.of("param", "value");
        when(metricsService.recordOperation("sampling_model", "cache_hit")).thenReturn(operationRecorder);
        when(metricsService.recordOperation("sampling_model", "cache_miss")).thenReturn(operationRecorder);
        when(metricsService.recordOperation("sampling_model", "sample_generation")).thenReturn(operationRecorder);
        when(operationRecorder.withSuccess(anyBoolean())).thenReturn(operationRecorder);
        when(operationRecorder.withDuration(anyLong())).thenReturn(operationRecorder);
        when(operationRecorder.withData(anyString(), any())).thenReturn(operationRecorder);
        doNothing().when(operationRecorder).record();

        // When - first call to populate cache
        samplingModel.generateSample(input);
        // Second call should hit cache
        samplingModel.generateSample(input);

        // Then
        verify(metricsService, atLeast(1)).recordOperation("sampling_model", "cache_hit");
        verify(operationRecorder, atLeast(1)).withSuccess(true);
        verify(operationRecorder, atLeast(1)).withDuration(0L);
        verify(operationRecorder, atLeast(1)).withData("modelId", "test-model");
    }

    @Test
    void testGetStatisticsWithMetricsService() {
        // Given
        when(sampleSnapshot.total()).thenReturn(50L);
        when(sampleSnapshot.totalDurationNanos()).thenReturn(5_000_000_000L); // 5 seconds in nanos
        when(sampleSnapshot.averageMs()).thenReturn(100.0);
        when(cacheHitSnapshot.total()).thenReturn(20L);
        when(cacheMissSnapshot.total()).thenReturn(30L);

        when(metricsService.getSnapshot(any(MetricKey.class), eq(ExecutionMetricsSnapshot.class)))
                .thenReturn(sampleSnapshot).thenReturn(cacheHitSnapshot).thenReturn(cacheMissSnapshot);

        // When
        Map<String, Object> statistics = samplingModel.getStatistics();

        // Then
        assertNotNull(statistics);
        assertEquals(50L, statistics.get("totalSamplesGenerated"));
        assertEquals(5000L, statistics.get("totalExecutionTime")); // 5 seconds in ms
        assertEquals(100.0, statistics.get("averageExecutionTime"));
        assertEquals(20L, statistics.get("cacheHits"));
        assertEquals(30L, statistics.get("cacheMisses"));
        assertEquals(0.4, (Double) statistics.get("cacheHitRate"), 0.01); // 20/(20+30) = 0.4

        verify(metricsService, times(3)).getSnapshot(any(MetricKey.class), eq(ExecutionMetricsSnapshot.class));
    }

    @Test
    void testGetStatisticsWithNullMetricsService() {
        // Given - no MetricsService injected
        SamplingModel modelWithoutMetrics = new SamplingModel("test-model", "Test Model", "Test sampling model",
                "test-type", Map.of("param1", "value1"), Map.of("config1", "configValue1"));

        // When
        Map<String, Object> statistics = modelWithoutMetrics.getStatistics();

        // Then
        assertNotNull(statistics);
        assertEquals(0L, statistics.get("totalSamplesGenerated"));
        assertEquals(0L, statistics.get("totalExecutionTime"));
        assertEquals(0.0, statistics.get("averageExecutionTime"));
        assertEquals(0L, statistics.get("cacheHits"));
        assertEquals(0L, statistics.get("cacheMisses"));
        assertEquals(0.0, statistics.get("cacheHitRate"));
    }

    @Test
    void testGetStatisticsWithNullSnapshots() {
        // Given
        when(metricsService.getSnapshot(any(MetricKey.class), eq(ExecutionMetricsSnapshot.class))).thenReturn(null);

        // When
        Map<String, Object> statistics = samplingModel.getStatistics();

        // Then
        assertNotNull(statistics);
        assertEquals(0L, statistics.get("totalSamplesGenerated"));
        assertEquals(0L, statistics.get("totalExecutionTime"));
        assertEquals(0.0, statistics.get("averageExecutionTime"));
        assertEquals(0L, statistics.get("cacheHits"));
        assertEquals(0L, statistics.get("cacheMisses"));
        assertEquals(0.0, statistics.get("cacheHitRate"));

        verify(metricsService, times(3)).getSnapshot(any(MetricKey.class), eq(ExecutionMetricsSnapshot.class));
    }

    @Test
    void testMetricsServiceErrorHandling() {
        // Given
        Map<String, Object> input = Map.of("param", "value");
        when(metricsService.recordOperation(anyString(), anyString())).thenThrow(new RuntimeException("Metrics error"));

        // When - should not throw exception despite metrics error
        Object result = samplingModel.generateSample(input);

        // Then
        assertNotNull(result); // Sample generation should still succeed
        verify(metricsService).recordOperation("sampling_model", "cache_miss");
        verify(metricsService).recordOperation("sampling_model", "sample_generation");
    }

    @Test
    void testGetStatisticsErrorHandling() {
        // Given
        when(metricsService.getSnapshot(any(MetricKey.class), eq(ExecutionMetricsSnapshot.class)))
                .thenThrow(new RuntimeException("Snapshot error"));

        // When - should not throw exception despite metrics error
        Map<String, Object> statistics = samplingModel.getStatistics();

        // Then
        assertNotNull(statistics);
        // Should return fallback values when metrics fail
        assertEquals(0L, statistics.get("totalSamplesGenerated"));
        assertEquals(0L, statistics.get("totalExecutionTime"));
        assertEquals(0.0, statistics.get("averageExecutionTime"));
        assertEquals(0L, statistics.get("cacheHits"));
        assertEquals(0L, statistics.get("cacheMisses"));
        assertEquals(0.0, statistics.get("cacheHitRate"));

        verify(metricsService, atLeast(1)).getSnapshot(any(MetricKey.class), eq(ExecutionMetricsSnapshot.class));
    }

    @Test
    void testCacheHitRateCalculation() {
        // Test the private calculateCacheHitRate method through getStatistics
        // Given
        when(cacheHitSnapshot.total()).thenReturn(15L);
        when(cacheMissSnapshot.total()).thenReturn(35L);

        when(metricsService.getSnapshot(any(MetricKey.class), eq(ExecutionMetricsSnapshot.class))).thenReturn(null) // sampleSnapshot
                .thenReturn(cacheHitSnapshot).thenReturn(cacheMissSnapshot);

        // When
        Map<String, Object> statistics = samplingModel.getStatistics();

        // Then
        assertEquals(15L, statistics.get("cacheHits"));
        assertEquals(35L, statistics.get("cacheMisses"));
        assertEquals(0.3, (Double) statistics.get("cacheHitRate"), 0.01); // 15/(15+35) = 0.3
    }

    @Test
    void testCacheHitRateWithZeroOperations() {
        // Test cache hit rate calculation when no cache operations occurred
        // Given
        when(cacheHitSnapshot.total()).thenReturn(0L);
        when(cacheMissSnapshot.total()).thenReturn(0L);

        when(metricsService.getSnapshot(any(MetricKey.class), eq(ExecutionMetricsSnapshot.class))).thenReturn(null) // sampleSnapshot
                .thenReturn(cacheHitSnapshot).thenReturn(cacheMissSnapshot);

        // When
        Map<String, Object> statistics = samplingModel.getStatistics();

        // Then
        assertEquals(0L, statistics.get("cacheHits"));
        assertEquals(0L, statistics.get("cacheMisses"));
        assertEquals(0.0, statistics.get("cacheHitRate")); // Should be 0.0 when no operations
    }

    @Test
    void testBasicModelProperties() {
        // Test that basic model properties are accessible
        assertEquals("test-model", samplingModel.getId());
        assertEquals("Test Model", samplingModel.getName());
        assertEquals("Test sampling model", samplingModel.getDescription());
        assertEquals("test-type", samplingModel.getType());
        assertEquals(Map.of("param1", "value1"), samplingModel.getParameters());
        assertEquals(Map.of("config1", "configValue1"), samplingModel.getConfiguration());
    }
}
