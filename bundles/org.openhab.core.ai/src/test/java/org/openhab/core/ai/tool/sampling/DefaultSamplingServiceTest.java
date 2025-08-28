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
package org.openhab.core.ai.tool.sampling;

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
import org.openhab.core.ai.common.monitoring.snapshot.SamplingServiceSnapshot;
import org.openhab.core.ai.common.sampling.SamplingStatus;

/**
 * Unit tests for DefaultSamplingService MetricsService integration
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
@ExtendWith(MockitoExtension.class)
class DefaultSamplingServiceTest {

    @Mock
    private MetricsService metricsService;

    @Mock
    private OperationRecorder operationRecorder;

    @Mock
    private ExecutionMetricsSnapshot createSnapshot;

    @Mock
    private ExecutionMetricsSnapshot approveSnapshot;

    @Mock
    private ExecutionMetricsSnapshot rejectSnapshot;

    @Mock
    private SamplingServiceSnapshot samplingSnapshot;

    private DefaultSamplingService samplingService;

    @BeforeEach
    void setUp() throws Exception {
        samplingService = new DefaultSamplingService();

        // Use reflection to inject the mocked MetricsService
        java.lang.reflect.Field metricsServiceField = DefaultSamplingService.class.getDeclaredField("metricsService");
        metricsServiceField.setAccessible(true);
        metricsServiceField.set(samplingService, metricsService);

        // Setup common mock behaviors
        when(metricsService.recordOperation(anyString(), anyString())).thenReturn(operationRecorder);
        when(operationRecorder.withSuccess(anyBoolean())).thenReturn(operationRecorder);
        when(operationRecorder.withDuration(anyLong())).thenReturn(operationRecorder);
        when(operationRecorder.withData(anyString(), any())).thenReturn(operationRecorder);
        doNothing().when(operationRecorder).record();
    }

    @Test
    void testCreateMessageRecordsSuccessMetrics() {
        // Given
        String modelName = "test-model";
        String message = "test message";
        boolean includeContext = true;

        // When
        SamplingRequest request = samplingService.createMessage(modelName, message, includeContext);

        // Then
        assertNotNull(request);
        assertEquals(SamplingStatus.PENDING, request.getStatus());
        assertEquals(modelName, request.getModelName());
        assertEquals(message, request.getMessage());
        assertEquals(includeContext, request.isIncludeContext());

        // Verify metrics recording
        verify(metricsService).recordOperation("sampling-service", "create-message");
        verify(operationRecorder).withSuccess(true);
        verify(operationRecorder).withData("modelName", modelName);
        verify(operationRecorder).withData("includeContext", includeContext);
        verify(operationRecorder, atLeast(1)).withDuration(anyLong());
        verify(operationRecorder).record();
    }

    @Test
    void testCreateMessageWithInvalidInputRecordsFailureMetrics() {
        // Given
        String modelName = ""; // Invalid empty model name
        String message = "test message";
        boolean includeContext = false;

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            samplingService.createMessage(modelName, message, includeContext);
        });

        // Verify failure metrics recording
        verify(metricsService).recordOperation("sampling-service", "create-message");
        verify(operationRecorder).withSuccess(false);
        verify(operationRecorder).withData("modelName", modelName);
        verify(operationRecorder).withData("includeContext", includeContext);
        verify(operationRecorder).withData(eq("error"), anyString());
        verify(operationRecorder).record();
    }

    @Test
    void testApproveRequestRecordsSuccessMetrics() {
        // Given
        String modelName = "test-model";
        String message = "test message";
        SamplingRequest request = samplingService.createMessage(modelName, message, false);
        String requestId = request.getId();

        // Reset mock interactions from createMessage call
        reset(metricsService, operationRecorder);
        when(metricsService.recordOperation(anyString(), anyString())).thenReturn(operationRecorder);
        when(operationRecorder.withSuccess(anyBoolean())).thenReturn(operationRecorder);
        when(operationRecorder.withDuration(anyLong())).thenReturn(operationRecorder);
        when(operationRecorder.withData(anyString(), any())).thenReturn(operationRecorder);
        doNothing().when(operationRecorder).record();

        // When
        boolean result = samplingService.approveRequest(requestId);

        // Then
        assertTrue(result);
        assertEquals(SamplingStatus.APPROVED, request.getStatus());

        // Verify metrics recording
        verify(metricsService).recordOperation("sampling-service", "approve-request");
        verify(operationRecorder).withSuccess(true);
        verify(operationRecorder).withData("requestId", requestId);
        verify(operationRecorder, atLeast(1)).withDuration(anyLong());
        verify(operationRecorder).record();
    }

    @Test
    void testApproveNonExistentRequestRecordsFailureMetrics() {
        // Given
        String nonExistentRequestId = "non-existent-id";

        // When
        boolean result = samplingService.approveRequest(nonExistentRequestId);

        // Then
        assertFalse(result);

        // Verify failure metrics recording
        verify(metricsService).recordOperation("sampling-service", "approve-request");
        verify(operationRecorder).withSuccess(false);
        verify(operationRecorder).withData("requestId", nonExistentRequestId);
        verify(operationRecorder).withData("error", "Request not found");
        verify(operationRecorder).record();
    }

    @Test
    void testRejectRequestRecordsSuccessMetrics() {
        // Given
        String modelName = "test-model";
        String message = "test message";
        SamplingRequest request = samplingService.createMessage(modelName, message, false);
        String requestId = request.getId();
        String reason = "Test rejection reason";

        // Reset mock interactions from createMessage call
        reset(metricsService, operationRecorder);
        when(metricsService.recordOperation(anyString(), anyString())).thenReturn(operationRecorder);
        when(operationRecorder.withSuccess(anyBoolean())).thenReturn(operationRecorder);
        when(operationRecorder.withDuration(anyLong())).thenReturn(operationRecorder);
        when(operationRecorder.withData(anyString(), any())).thenReturn(operationRecorder);
        doNothing().when(operationRecorder).record();

        // When
        boolean result = samplingService.rejectRequest(requestId, reason);

        // Then
        assertTrue(result);
        assertEquals(SamplingStatus.REJECTED, request.getStatus());
        assertEquals(reason, request.getRejectionReason());

        // Verify metrics recording
        verify(metricsService).recordOperation("sampling-service", "reject-request");
        verify(operationRecorder).withSuccess(true);
        verify(operationRecorder).withData("requestId", requestId);
        verify(operationRecorder).withData("reason", reason);
        verify(operationRecorder, atLeast(1)).withDuration(anyLong());
        verify(operationRecorder).record();
    }

    @Test
    void testRejectNonExistentRequestRecordsFailureMetrics() {
        // Given
        String nonExistentRequestId = "non-existent-id";
        String reason = "Test rejection reason";

        // When
        boolean result = samplingService.rejectRequest(nonExistentRequestId, reason);

        // Then
        assertFalse(result);

        // Verify failure metrics recording
        verify(metricsService).recordOperation("sampling-service", "reject-request");
        verify(operationRecorder).withSuccess(false);
        verify(operationRecorder).withData("requestId", nonExistentRequestId);
        verify(operationRecorder).withData("error", "Request not found");
        verify(operationRecorder).record();
    }

    @Test
    void testGetPerformanceMetricsWithMetricsService() {
        // Given
        when(createSnapshot.total()).thenReturn(100L);
        when(createSnapshot.averageMs()).thenReturn(150.0);
        when(approveSnapshot.success()).thenReturn(60L);
        when(rejectSnapshot.success()).thenReturn(30L);

        when(metricsService.getSnapshot(any(MetricKey.class), eq(ExecutionMetricsSnapshot.class)))
                .thenReturn(createSnapshot) // create-message snapshot
                .thenReturn(approveSnapshot) // approve-request snapshot
                .thenReturn(rejectSnapshot); // reject-request snapshot

        // When
        Map<String, Object> metrics = samplingService.getPerformanceMetrics();

        // Then
        assertNotNull(metrics);
        assertEquals(100L, metrics.get("totalRequests"));
        assertEquals(150.0, metrics.get("averageResponseTimeMs"));
        assertEquals(60L, metrics.get("approvedRequests"));
        assertEquals(30L, metrics.get("rejectedRequests"));
        assertEquals(0.6, (Double) metrics.get("approvalRate"), 0.01); // 60/100 = 0.6

        verify(metricsService, times(3)).getSnapshot(any(MetricKey.class), eq(ExecutionMetricsSnapshot.class));
    }

    @Test
    void testGetPerformanceMetricsWithNullSnapshots() {
        // Given
        when(metricsService.getSnapshot(any(MetricKey.class), eq(ExecutionMetricsSnapshot.class))).thenReturn(null);

        // When
        Map<String, Object> metrics = samplingService.getPerformanceMetrics();

        // Then
        assertNotNull(metrics);
        assertEquals(0L, metrics.get("totalRequests"));
        assertEquals(0.0, metrics.get("averageResponseTimeMs"));
        assertEquals(0L, metrics.get("approvedRequests"));
        assertEquals(0L, metrics.get("rejectedRequests"));
        assertEquals(0.0, metrics.get("approvalRate"));

        verify(metricsService, times(3)).getSnapshot(any(MetricKey.class), eq(ExecutionMetricsSnapshot.class));
    }

    @Test
    void testGetPerformanceMetricsWithoutMetricsService() throws Exception {
        // Given - no MetricsService injected
        DefaultSamplingService serviceWithoutMetrics = new DefaultSamplingService();

        // When
        Map<String, Object> metrics = serviceWithoutMetrics.getPerformanceMetrics();

        // Then
        assertNotNull(metrics);
        assertEquals(0L, metrics.get("totalRequests"));
        assertEquals(0.0, metrics.get("averageResponseTimeMs"));
        assertEquals(0L, metrics.get("approvedRequests"));
        assertEquals(0L, metrics.get("rejectedRequests"));
        assertEquals(0.0, metrics.get("approvalRate"));
        assertEquals(0, metrics.get("pendingRequests"));
    }

    @Test
    void testMetricsServiceErrorHandling() {
        // Given
        when(metricsService.recordOperation(anyString(), anyString())).thenThrow(new RuntimeException("Metrics error"));

        // When - should not throw exception despite metrics error
        SamplingRequest request = samplingService.createMessage("test-model", "test message", false);

        // Then
        assertNotNull(request); // Service operation should still succeed
        assertEquals(SamplingStatus.PENDING, request.getStatus());

        verify(metricsService).recordOperation("sampling-service", "create-message");
    }

    @Test
    void testGetPerformanceMetricsErrorHandling() {
        // Given
        when(metricsService.getSnapshot(any(MetricKey.class), eq(ExecutionMetricsSnapshot.class)))
                .thenThrow(new RuntimeException("Snapshot error"));

        // When - should not throw exception despite metrics error
        Map<String, Object> metrics = samplingService.getPerformanceMetrics();

        // Then
        assertNotNull(metrics);
        // Should return fallback values when metrics fail
        assertEquals(0L, metrics.get("totalRequests"));
        assertEquals(0L, metrics.get("approvedRequests"));
        assertEquals(0L, metrics.get("rejectedRequests"));
        assertEquals(0.0, metrics.get("averageResponseTimeMs"));
        assertEquals(0.0, metrics.get("approvalRate"));

        verify(metricsService, atLeast(1)).getSnapshot(any(MetricKey.class), eq(ExecutionMetricsSnapshot.class));
    }

    @Test
    void testApprovalRateCalculation() {
        // Given
        when(createSnapshot.total()).thenReturn(200L);
        when(createSnapshot.averageMs()).thenReturn(100.0);
        when(approveSnapshot.success()).thenReturn(140L);
        when(rejectSnapshot.success()).thenReturn(50L);

        when(metricsService.getSnapshot(any(MetricKey.class), eq(ExecutionMetricsSnapshot.class)))
                .thenReturn(createSnapshot).thenReturn(approveSnapshot).thenReturn(rejectSnapshot);

        // When
        Map<String, Object> metrics = samplingService.getPerformanceMetrics();

        // Then
        assertEquals(140L, metrics.get("approvedRequests"));
        assertEquals(50L, metrics.get("rejectedRequests"));
        assertEquals(0.7, (Double) metrics.get("approvalRate"), 0.01); // 140/200 = 0.7
    }

    @Test
    void testApprovalRateWithZeroRequests() {
        // Given
        when(createSnapshot.total()).thenReturn(0L);
        when(createSnapshot.averageMs()).thenReturn(0.0);
        when(approveSnapshot.success()).thenReturn(0L);
        when(rejectSnapshot.success()).thenReturn(0L);

        when(metricsService.getSnapshot(any(MetricKey.class), eq(ExecutionMetricsSnapshot.class)))
                .thenReturn(createSnapshot).thenReturn(approveSnapshot).thenReturn(rejectSnapshot);

        // When
        Map<String, Object> metrics = samplingService.getPerformanceMetrics();

        // Then
        assertEquals(0L, metrics.get("totalRequests"));
        assertEquals(0L, metrics.get("approvedRequests"));
        assertEquals(0L, metrics.get("rejectedRequests"));
        assertEquals(0.0, metrics.get("approvalRate")); // Should be 0.0 when no requests
    }

    @Test
    void testGetRequestFunctionality() {
        // Given
        String modelName = "test-model";
        String message = "test message";
        SamplingRequest request = samplingService.createMessage(modelName, message, false);
        String requestId = request.getId();

        // When
        SamplingRequest retrievedRequest = samplingService.getRequest(requestId);

        // Then
        assertNotNull(retrievedRequest);
        assertEquals(requestId, retrievedRequest.getId());
        assertEquals(modelName, retrievedRequest.getModelName());
        assertEquals(message, retrievedRequest.getMessage());
        assertEquals(SamplingStatus.PENDING, retrievedRequest.getStatus());
    }

    @Test
    void testGetNonExistentRequest() {
        // When
        SamplingRequest request = samplingService.getRequest("non-existent-id");

        // Then
        assertNull(request);
    }

    @Test
    void testRequestCollections() {
        // Given
        String modelName = "test-model";
        SamplingRequest request1 = samplingService.createMessage(modelName, "message1", false);
        SamplingRequest request2 = samplingService.createMessage(modelName, "message2", false);
        SamplingRequest request3 = samplingService.createMessage(modelName, "message3", false);

        // Approve one, reject one, leave one pending
        samplingService.approveRequest(request1.getId());
        samplingService.rejectRequest(request2.getId(), "test reason");

        // When
        Map<String, SamplingRequest> pendingRequests = samplingService.getPendingRequests();
        Map<String, SamplingRequest> approvedRequests = samplingService.getApprovedRequests();
        Map<String, SamplingRequest> rejectedRequests = samplingService.getRejectedRequests();

        // Then
        assertEquals(1, pendingRequests.size());
        assertEquals(1, approvedRequests.size());
        assertEquals(1, rejectedRequests.size());

        assertTrue(pendingRequests.containsKey(request3.getId()));
        assertTrue(approvedRequests.containsKey(request1.getId()));
        assertTrue(rejectedRequests.containsKey(request2.getId()));

        assertEquals(1, samplingService.getPendingRequestCount());
        assertEquals(1, samplingService.getApprovedRequestCount());
        assertEquals(1, samplingService.getRejectedRequestCount());
    }

    @Test
    void testPendingRequestsIncludedInMetrics() {
        // Given
        String modelName = "test-model";
        samplingService.createMessage(modelName, "message1", false);
        samplingService.createMessage(modelName, "message2", false);

        when(createSnapshot.total()).thenReturn(2L);
        when(createSnapshot.averageMs()).thenReturn(100.0);
        when(approveSnapshot.success()).thenReturn(0L);
        when(rejectSnapshot.success()).thenReturn(0L);

        when(metricsService.getSnapshot(any(MetricKey.class), eq(ExecutionMetricsSnapshot.class)))
                .thenReturn(createSnapshot).thenReturn(approveSnapshot).thenReturn(rejectSnapshot);

        // When
        Map<String, Object> metrics = samplingService.getPerformanceMetrics();

        // Then
        assertEquals(2, metrics.get("currentPendingRequests")); // Current pending count
        assertEquals(2L, metrics.get("totalRequests"));
        assertEquals(0L, metrics.get("approvedRequests"));
        assertEquals(0L, metrics.get("rejectedRequests"));
    }

    @Test
    void testGetPerformanceMetricsWithSamplingServiceSnapshot() {
        // Given
        when(samplingSnapshot.total()).thenReturn(100L);
        when(samplingSnapshot.success()).thenReturn(95L);
        when(samplingSnapshot.failure()).thenReturn(5L);
        when(samplingSnapshot.averageMs()).thenReturn(150.0);
        when(samplingSnapshot.approvedRequests()).thenReturn(75L);
        when(samplingSnapshot.rejectedRequests()).thenReturn(20L);
        when(samplingSnapshot.pendingRequests()).thenReturn(5L);
        when(samplingSnapshot.approvalRate()).thenReturn(0.75);
        when(samplingSnapshot.approvalRatePercentage()).thenReturn(75.0);
        when(samplingSnapshot.rejectionRatePercentage()).thenReturn(20.0);
        when(samplingSnapshot.pendingRatioPercentage()).thenReturn(5.0);
        when(samplingSnapshot.efficiencyScore()).thenReturn(0.85);
        when(samplingSnapshot.processingThroughput()).thenReturn(2.5);
        when(samplingSnapshot.totalSamplesGenerated()).thenReturn(200L);
        when(samplingSnapshot.cacheHits()).thenReturn(180L);
        when(samplingSnapshot.cacheMisses()).thenReturn(20L);
        when(samplingSnapshot.cacheSize()).thenReturn(50);
        when(samplingSnapshot.cacheHitRate()).thenReturn(0.9);
        when(samplingSnapshot.cacheEfficiency()).thenReturn(90.0);
        when(samplingSnapshot.modelType()).thenReturn("gpt-4");
        when(samplingSnapshot.modelVersion()).thenReturn("1.0");

        when(metricsService.getSnapshot(any(MetricKey.class), eq(SamplingServiceSnapshot.class)))
                .thenReturn(samplingSnapshot);

        // When
        Map<String, Object> metrics = samplingService.getPerformanceMetrics();

        // Then
        assertNotNull(metrics);

        // Basic execution metrics
        assertEquals(100L, metrics.get("totalRequests"));
        assertEquals(95L, metrics.get("successfulRequests"));
        assertEquals(5L, metrics.get("failedRequests"));
        assertEquals(150.0, metrics.get("averageResponseTimeMs"));

        // Sampling-specific metrics
        assertEquals(75L, metrics.get("approvedRequests"));
        assertEquals(20L, metrics.get("rejectedRequests"));
        assertEquals(5L, metrics.get("pendingRequests"));

        // Calculated metrics
        assertEquals(0.75, metrics.get("approvalRate"));
        assertEquals(75.0, metrics.get("approvalRatePercentage"));
        assertEquals(20.0, metrics.get("rejectionRatePercentage"));
        assertEquals(5.0, metrics.get("pendingRatioPercentage"));
        assertEquals(0.85, metrics.get("efficiencyScore"));
        assertEquals(2.5, metrics.get("processingThroughput"));

        // Cache metrics
        assertEquals(200L, metrics.get("totalSamplesGenerated"));
        assertEquals(180L, metrics.get("cacheHits"));
        assertEquals(20L, metrics.get("cacheMisses"));
        assertEquals(50, metrics.get("cacheSize"));
        assertEquals(0.9, metrics.get("cacheHitRate"));
        assertEquals(90.0, metrics.get("cacheEfficiency"));

        // Model information
        assertEquals("gpt-4", metrics.get("modelType"));
        assertEquals("1.0", metrics.get("modelVersion"));

        verify(metricsService).getSnapshot(any(MetricKey.class), eq(SamplingServiceSnapshot.class));
    }

    @Test
    void testGetPerformanceMetricsFallbackToExecutionSnapshot() {
        // Given - SamplingServiceSnapshot is null, fallback to ExecutionMetricsSnapshot
        when(metricsService.getSnapshot(any(MetricKey.class), eq(SamplingServiceSnapshot.class))).thenReturn(null);
        when(metricsService.getSnapshot(any(MetricKey.class), eq(ExecutionMetricsSnapshot.class)))
                .thenReturn(createSnapshot);

        when(createSnapshot.total()).thenReturn(50L);
        when(createSnapshot.success()).thenReturn(45L);
        when(createSnapshot.failure()).thenReturn(5L);
        when(createSnapshot.averageMs()).thenReturn(200.0);

        // Add some requests to test live counts
        String modelName = "test-model";
        SamplingRequest request1 = samplingService.createMessage(modelName, "message1", false);
        samplingService.createMessage(modelName, "message2", false);
        samplingService.approveRequest(request1.getId());

        // When
        Map<String, Object> metrics = samplingService.getPerformanceMetrics();

        // Then
        assertNotNull(metrics);
        assertEquals(50L, metrics.get("totalRequests"));
        assertEquals(45L, metrics.get("successfulRequests"));
        assertEquals(5L, metrics.get("failedRequests"));
        assertEquals(200.0, metrics.get("averageResponseTimeMs"));

        // Should use live counts for sampling-specific metrics
        assertEquals(1L, metrics.get("approvedRequests")); // From live count
        assertEquals(0L, metrics.get("rejectedRequests")); // From live count
        assertEquals(1L, metrics.get("pendingRequests")); // From live count

        // Approval rate should be calculated from live counts
        assertEquals(1.0, (Double) metrics.get("approvalRate"), 0.01); // 1 approved / 1 total processed

        verify(metricsService).getSnapshot(any(MetricKey.class), eq(SamplingServiceSnapshot.class));
        verify(metricsService).getSnapshot(any(MetricKey.class), eq(ExecutionMetricsSnapshot.class));
    }
}
