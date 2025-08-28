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
package org.openhab.core.ai.tool.resources;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Duration;
import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.ai.common.monitoring.api.MetricKey;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
import org.openhab.core.ai.common.monitoring.service.OperationRecorder;
import org.openhab.core.ai.common.monitoring.snapshot.ProviderResourceSnapshot;
import org.openhab.core.ai.common.monitoring.snapshot.ProviderResourceStatistics;
import org.openhab.core.ai.model.api.ModelProviderType;

/**
 * Unit tests for {@link ProviderResourceUsage}.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@NonNullByDefault
class ProviderResourceUsageTest {

    @Mock
    private MetricsService metricsService;

    @Mock
    private OperationRecorder operationRecorder;

    @Mock
    private ProviderResourceSnapshot snapshot;

    private ProviderResourceUsage providerResourceUsage;

    @BeforeEach
    void setUp() {
        providerResourceUsage = new ProviderResourceUsage(ModelProviderType.OPENAI);
        // Set the metrics service using reflection since it's a private field
        try {
            var field = ProviderResourceUsage.class.getDeclaredField("metricsService");
            field.setAccessible(true);
            field.set(providerResourceUsage, metricsService);
        } catch (Exception e) {
            fail("Failed to set metricsService field: " + e.getMessage());
        }
    }

    @Test
    void testInitialState() {
        // Given: A new ProviderResourceUsage instance
        ProviderResourceUsage usage = new ProviderResourceUsage(ModelProviderType.ANTHROPIC);

        // Then: Initial state should be correct
        assertEquals(ModelProviderType.ANTHROPIC, usage.getProvider());
        assertEquals(0L, usage.getConcurrentRequests());
        assertNotNull(usage.getLastActivity());
        assertTrue(usage.getLastActivity().isBefore(Instant.now().plusSeconds(1)));
    }

    @Test
    void testUpdateUsageSuccess() {
        // Given: MetricsService is set up to return the operation recorder
        when(metricsService.recordOperation("provider-resource", "OPENAI")).thenReturn(operationRecorder);
        when(operationRecorder.withSuccess(true)).thenReturn(operationRecorder);
        when(operationRecorder.withDuration(0L)).thenReturn(operationRecorder);
        when(operationRecorder.withData("provider", "OPENAI")).thenReturn(operationRecorder);
        when(operationRecorder.withData("component", "ProviderResourceUsage")).thenReturn(operationRecorder);

        Instant beforeUpdate = Instant.now();

        // When: Update usage with success
        providerResourceUsage.updateUsage(2, 1);

        // Then: Concurrent requests should be updated and metrics recorded
        assertEquals(2L, providerResourceUsage.getConcurrentRequests());
        assertTrue(providerResourceUsage.getLastActivity().isAfter(beforeUpdate));

        verify(metricsService).recordOperation("provider-resource", "OPENAI");
        verify(operationRecorder).withSuccess(true);
        verify(operationRecorder).withDuration(0L);
        verify(operationRecorder).withData("provider", "OPENAI");
        verify(operationRecorder).withData("component", "ProviderResourceUsage");
        verify(operationRecorder).record();
    }

    @Test
    void testUpdateUsageFailure() {
        // Given: MetricsService is set up to return the operation recorder
        when(metricsService.recordOperation("provider-resource", "OPENAI")).thenReturn(operationRecorder);
        when(operationRecorder.withSuccess(false)).thenReturn(operationRecorder);
        when(operationRecorder.withDuration(0L)).thenReturn(operationRecorder);
        when(operationRecorder.withData("provider", "OPENAI")).thenReturn(operationRecorder);
        when(operationRecorder.withData("component", "ProviderResourceUsage")).thenReturn(operationRecorder);

        // When: Update usage with failure
        providerResourceUsage.updateUsage(1, 0);

        // Then: Concurrent requests should be updated and failure recorded
        assertEquals(1L, providerResourceUsage.getConcurrentRequests());

        verify(metricsService).recordOperation("provider-resource", "OPENAI");
        verify(operationRecorder).withSuccess(false);
        verify(operationRecorder).record();
    }

    @Test
    void testUpdateUsageNegativeConcurrentRequests() {
        // Given: Initial state with some concurrent requests
        providerResourceUsage.updateUsage(3, 1);
        reset(metricsService, operationRecorder);

        when(metricsService.recordOperation("provider-resource", "OPENAI")).thenReturn(operationRecorder);
        when(operationRecorder.withSuccess(true)).thenReturn(operationRecorder);
        when(operationRecorder.withDuration(0L)).thenReturn(operationRecorder);
        when(operationRecorder.withData("provider", "OPENAI")).thenReturn(operationRecorder);
        when(operationRecorder.withData("component", "ProviderResourceUsage")).thenReturn(operationRecorder);

        // When: Decrease concurrent requests below zero
        providerResourceUsage.updateUsage(-5, 1);

        // Then: Concurrent requests should not go below zero
        assertEquals(0L, providerResourceUsage.getConcurrentRequests());
    }

    @Test
    void testRecordRequest() {
        // Given: MetricsService is set up to return the operation recorder
        when(metricsService.recordOperation("provider-resource", "OPENAI")).thenReturn(operationRecorder);
        when(operationRecorder.withSuccess(true)).thenReturn(operationRecorder);
        when(operationRecorder.withDuration(5000000L)).thenReturn(operationRecorder); // 5ms in nanos
        when(operationRecorder.withData("provider", "OPENAI")).thenReturn(operationRecorder);
        when(operationRecorder.withData("component", "ProviderResourceUsage")).thenReturn(operationRecorder);

        Duration requestDuration = Duration.ofMillis(5);
        Instant beforeRecord = Instant.now();

        // When: Record a request
        providerResourceUsage.recordRequest("test-request", true, requestDuration);

        // Then: Request should be recorded with correct duration
        assertTrue(providerResourceUsage.getLastActivity().isAfter(beforeRecord));

        verify(metricsService).recordOperation("provider-resource", "OPENAI");
        verify(operationRecorder).withSuccess(true);
        verify(operationRecorder).withDuration(5000000L); // 5ms in nanos
        verify(operationRecorder).record();
    }

    @Test
    void testGetTotalRequestsWithSnapshot() {
        // Given: MetricsService returns a snapshot
        when(metricsService.getSnapshot(any(MetricKey.class), eq(ProviderResourceSnapshot.class))).thenReturn(snapshot);
        when(snapshot.total()).thenReturn(42L);

        // When: Get total requests
        long totalRequests = providerResourceUsage.getTotalRequests();

        // Then: Should return value from snapshot
        assertEquals(42L, totalRequests);
        verify(metricsService).getSnapshot(any(MetricKey.class), eq(ProviderResourceSnapshot.class));
    }

    @Test
    void testGetTotalRequestsWithoutSnapshot() {
        // Given: MetricsService returns null
        when(metricsService.getSnapshot(any(MetricKey.class), eq(ProviderResourceSnapshot.class))).thenReturn(null);

        // When: Get total requests
        long totalRequests = providerResourceUsage.getTotalRequests();

        // Then: Should return 0
        assertEquals(0L, totalRequests);
    }

    @Test
    void testGetSuccessfulRequestsWithSnapshot() {
        // Given: MetricsService returns a snapshot
        when(metricsService.getSnapshot(any(MetricKey.class), eq(ProviderResourceSnapshot.class))).thenReturn(snapshot);
        when(snapshot.success()).thenReturn(35L);

        // When: Get successful requests
        long successfulRequests = providerResourceUsage.getSuccessfulRequests();

        // Then: Should return value from snapshot
        assertEquals(35L, successfulRequests);
    }

    @Test
    void testGetFailedRequestsWithSnapshot() {
        // Given: MetricsService returns a snapshot
        when(metricsService.getSnapshot(any(MetricKey.class), eq(ProviderResourceSnapshot.class))).thenReturn(snapshot);
        when(snapshot.failure()).thenReturn(7L);

        // When: Get failed requests
        long failedRequests = providerResourceUsage.getFailedRequests();

        // Then: Should return value from snapshot
        assertEquals(7L, failedRequests);
    }

    @Test
    void testGetSuccessRateWithSnapshot() {
        // Given: MetricsService returns a snapshot
        when(metricsService.getSnapshot(any(MetricKey.class), eq(ProviderResourceSnapshot.class))).thenReturn(snapshot);
        when(snapshot.successRate()).thenReturn(0.85);

        // When: Get success rate
        double successRate = providerResourceUsage.getSuccessRate();

        // Then: Should return value from snapshot
        assertEquals(0.85, successRate, 0.001);
    }

    @Test
    void testGetSuccessRateWithoutSnapshot() {
        // Given: MetricsService returns null
        when(metricsService.getSnapshot(any(MetricKey.class), eq(ProviderResourceSnapshot.class))).thenReturn(null);

        // When: Get success rate
        double successRate = providerResourceUsage.getSuccessRate();

        // Then: Should return 0.0
        assertEquals(0.0, successRate, 0.001);
    }

    @Test
    void testGetStatistics() {
        // Given: A time range for statistics
        Duration timeRange = Duration.ofHours(1);

        // When: Get statistics
        ProviderResourceStatistics statistics = providerResourceUsage.getStatistics(timeRange);

        // Then: Should return empty statistics (until full implementation)
        assertNotNull(statistics);
        assertEquals("OPENAI", statistics.getProviderName());
        assertEquals(0.0, statistics.getPeakMemoryUsage(), 0.001);
    }

    @Test
    void testGetSnapshotWithoutMetricsService() {
        // Given: No MetricsService
        ProviderResourceUsage usageWithoutMetrics = new ProviderResourceUsage(ModelProviderType.GOOGLE);

        // When: Get snapshot
        ProviderResourceSnapshot result = usageWithoutMetrics.getSnapshot();

        // Then: Should return null
        assertNull(result);
    }

    @Test
    void testMetricsServiceException() {
        // Given: MetricsService throws exception
        when(metricsService.recordOperation("provider-resource", "OPENAI"))
                .thenThrow(new RuntimeException("Metrics service error"));

        // When: Update usage (should not throw exception)
        assertDoesNotThrow(() -> providerResourceUsage.updateUsage(1, 1));

        // Then: Concurrent requests should still be updated
        assertEquals(1L, providerResourceUsage.getConcurrentRequests());
    }

    @Test
    void testGetSnapshotException() {
        // Given: MetricsService throws exception during getSnapshot
        when(metricsService.getSnapshot(any(MetricKey.class), eq(ProviderResourceSnapshot.class)))
                .thenThrow(new RuntimeException("Snapshot error"));

        // When: Get total requests (should not throw exception)
        long totalRequests = assertDoesNotThrow(() -> providerResourceUsage.getTotalRequests());

        // Then: Should return 0 (graceful degradation)
        assertEquals(0L, totalRequests);
    }
}
