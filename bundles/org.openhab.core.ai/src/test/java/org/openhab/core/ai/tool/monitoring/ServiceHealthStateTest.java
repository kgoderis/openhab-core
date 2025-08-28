package org.openhab.core.ai.tool.monitoring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.ai.common.monitoring.api.MetricKey;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
import org.openhab.core.ai.common.monitoring.service.OperationRecorder;
import org.openhab.core.ai.common.monitoring.service.snapshot.GenericMetricsSnapshot;

/**
 * Unit tests for {@link ServiceHealthState}.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@NonNullByDefault
class ServiceHealthStateTest {

    private static final String TEST_SERVICE_NAME = "TestService";
    private static final long TEST_RESPONSE_TIME = 1500L;
    private static final String TEST_ERROR_MESSAGE = "Connection timeout";

    @Mock
    private MetricsService metricsService;

    @Mock
    private OperationRecorder operationRecorder;

    private ServiceHealthState serviceHealthState;

    @BeforeEach
    void setUp() {
        serviceHealthState = new ServiceHealthState(TEST_SERVICE_NAME, metricsService);
    }

    @Test
    void testConstructorWithNullMetricsService() {
        // Test that ServiceHealthState can handle null MetricsService
        ServiceHealthState state = new ServiceHealthState(TEST_SERVICE_NAME, null);
        assertNotNull(state);

        // Should not throw exceptions when recording metrics
        state.recordSuccess(TEST_RESPONSE_TIME);
        state.recordFailure(new RuntimeException(TEST_ERROR_MESSAGE));
    }

    @Test
    void testRecordSuccess() {
        // Arrange
        when(metricsService.recordOperation(eq("service-health"), eq(TEST_SERVICE_NAME))).thenReturn(operationRecorder);
        when(operationRecorder.withSuccess(true)).thenReturn(operationRecorder);
        when(operationRecorder.withDuration(any(Long.class))).thenReturn(operationRecorder);
        when(operationRecorder.withData(any(String.class), any(Object.class))).thenReturn(operationRecorder);

        // Act
        serviceHealthState.recordSuccess(TEST_RESPONSE_TIME);

        // Assert
        verify(metricsService).recordOperation("service-health", TEST_SERVICE_NAME);
        verify(operationRecorder).withSuccess(true);
        verify(operationRecorder).withDuration(Duration.ofMillis(TEST_RESPONSE_TIME).toNanos());
        verify(operationRecorder).withData("responseTimeMs", TEST_RESPONSE_TIME);
        verify(operationRecorder).record();
    }

    @Test
    void testRecordSuccessWithMetricsServiceException() {
        // Arrange
        when(metricsService.recordOperation(eq("service-health"), eq(TEST_SERVICE_NAME)))
                .thenThrow(new RuntimeException("MetricsService error"));

        // Act & Assert - should not throw exception
        serviceHealthState.recordSuccess(TEST_RESPONSE_TIME);

        // Verify that the method attempted to use MetricsService
        verify(metricsService).recordOperation("service-health", TEST_SERVICE_NAME);
    }

    @Test
    void testRecordFailure() {
        // Arrange
        RuntimeException testException = new RuntimeException(TEST_ERROR_MESSAGE);
        when(metricsService.recordOperation(eq("service-health"), eq(TEST_SERVICE_NAME))).thenReturn(operationRecorder);
        when(operationRecorder.withSuccess(false)).thenReturn(operationRecorder);
        when(operationRecorder.withDuration(0L)).thenReturn(operationRecorder);
        when(operationRecorder.withData(any(String.class), any(Object.class))).thenReturn(operationRecorder);

        // Act
        serviceHealthState.recordFailure(testException);

        // Assert
        verify(metricsService).recordOperation("service-health", TEST_SERVICE_NAME);
        verify(operationRecorder).withSuccess(false);
        verify(operationRecorder).withDuration(0L);
        verify(operationRecorder).withData("errorType", "RuntimeException");
        verify(operationRecorder).withData("errorMessage", TEST_ERROR_MESSAGE);
        verify(operationRecorder).record();
    }

    @Test
    void testRecordFailureWithNullErrorMessage() {
        // Arrange
        RuntimeException testException = new RuntimeException((String) null);
        when(metricsService.recordOperation(eq("service-health"), eq(TEST_SERVICE_NAME))).thenReturn(operationRecorder);
        when(operationRecorder.withSuccess(false)).thenReturn(operationRecorder);
        when(operationRecorder.withDuration(0L)).thenReturn(operationRecorder);
        when(operationRecorder.withData(any(String.class), any(Object.class))).thenReturn(operationRecorder);

        // Act
        serviceHealthState.recordFailure(testException);

        // Assert
        verify(operationRecorder).withData("errorMessage", "Unknown error");
    }

    @Test
    void testRecordFailureWithMetricsServiceException() {
        // Arrange
        RuntimeException testException = new RuntimeException(TEST_ERROR_MESSAGE);
        when(metricsService.recordOperation(eq("service-health"), eq(TEST_SERVICE_NAME)))
                .thenThrow(new RuntimeException("MetricsService error"));

        // Act & Assert - should not throw exception
        serviceHealthState.recordFailure(testException);

        // Verify that the method attempted to use MetricsService
        verify(metricsService).recordOperation("service-health", TEST_SERVICE_NAME);
    }

    @Test
    void testUpdateFromHealthCheck() {
        // Act
        serviceHealthState.updateFromHealthCheck(true, TEST_RESPONSE_TIME);

        // Assert
        assertNotNull(serviceHealthState.getLastHealthCheck());
    }

    @Test
    void testForceRecovery() {
        // Arrange
        when(metricsService.recordOperation(eq("service-health"), eq(TEST_SERVICE_NAME))).thenReturn(operationRecorder);
        when(operationRecorder.withSuccess(true)).thenReturn(operationRecorder);
        when(operationRecorder.withDuration(0L)).thenReturn(operationRecorder);
        when(operationRecorder.withData(any(String.class), any(Object.class))).thenReturn(operationRecorder);

        // Act
        serviceHealthState.forceRecovery();

        // Assert
        verify(metricsService).recordOperation("service-health", TEST_SERVICE_NAME);
        verify(operationRecorder).withSuccess(true);
        verify(operationRecorder).withDuration(0L);
        verify(operationRecorder).withData("eventType", "recovery");
        verify(operationRecorder).record();
    }

    @Test
    void testForceRecoveryWithMetricsServiceException() {
        // Arrange
        when(metricsService.recordOperation(eq("service-health"), eq(TEST_SERVICE_NAME)))
                .thenThrow(new RuntimeException("MetricsService error"));

        // Act & Assert - should not throw exception
        serviceHealthState.forceRecovery();

        // Verify that the method attempted to use MetricsService
        verify(metricsService).recordOperation("service-health", TEST_SERVICE_NAME);
    }

    @Test
    void testIsHealthyWithGoodMetrics() {
        // Arrange
        setupMockSnapshot(100L, 90L, 10_000_000_000L); // 90% success rate, 100ms average

        // Act
        boolean isHealthy = serviceHealthState.isHealthy();

        // Assert
        assertTrue(isHealthy);
    }

    @Test
    void testIsHealthyWithLowSuccessRate() {
        // Arrange
        setupMockSnapshot(100L, 70L, 1_000_000_000L); // 70% success rate, 10ms average

        // Act
        boolean isHealthy = serviceHealthState.isHealthy();

        // Assert
        assertFalse(isHealthy);
    }

    @Test
    void testIsHealthyWithHighResponseTime() {
        // Arrange
        setupMockSnapshot(100L, 95L, 600_000_000_000L); // 95% success rate, 6000ms average

        // Act
        boolean isHealthy = serviceHealthState.isHealthy();

        // Assert
        assertFalse(isHealthy);
    }

    @Test
    void testGetSuccessRateWithNoData() {
        // Arrange
        setupMockSnapshot(0L, 0L, 0L);

        // Act
        double successRate = serviceHealthState.getSuccessRate();

        // Assert
        assertEquals(0.0, successRate, 0.001);
    }

    @Test
    void testGetSuccessRateWithData() {
        // Arrange
        setupMockSnapshot(100L, 85L, 10_000_000_000L);

        // Act
        double successRate = serviceHealthState.getSuccessRate();

        // Assert
        assertEquals(0.85, successRate, 0.001);
    }

    @Test
    void testGetAverageResponseTimeWithNoData() {
        // Arrange
        setupMockSnapshot(0L, 0L, 0L);

        // Act
        double avgResponseTime = serviceHealthState.getAverageResponseTime();

        // Assert
        assertEquals(0.0, avgResponseTime, 0.001);
    }

    @Test
    void testGetAverageResponseTimeWithData() {
        // Arrange
        setupMockSnapshot(10L, 8L, 50_000_000_000L); // 50 seconds total for 10 requests = 5 seconds average

        // Act
        double avgResponseTime = serviceHealthState.getAverageResponseTime();

        // Assert
        assertEquals(5000.0, avgResponseTime, 0.001); // 5000ms
    }

    @Test
    void testGetSuccessRateWithNullSnapshot() {
        // Arrange
        setupMockSnapshotNull();

        // Act
        double successRate = serviceHealthState.getSuccessRate();

        // Assert
        assertEquals(0.0, successRate, 0.001);
    }

    @Test
    void testGetAverageResponseTimeWithNullSnapshot() {
        // Arrange
        setupMockSnapshotNull();

        // Act
        double avgResponseTime = serviceHealthState.getAverageResponseTime();

        // Assert
        assertEquals(0.0, avgResponseTime, 0.001);
    }

    @Test
    void testGetMetricsSnapshotException() {
        // Arrange
        when(metricsService.getSnapshot(any(MetricKey.class), eq(GenericMetricsSnapshot.class)))
                .thenThrow(new RuntimeException("Snapshot retrieval error"));

        // Act
        double successRate = serviceHealthState.getSuccessRate();
        double avgResponseTime = serviceHealthState.getAverageResponseTime();

        // Assert
        assertEquals(0.0, successRate, 0.001);
        assertEquals(0.0, avgResponseTime, 0.001);
    }

    @Test
    void testRecordOperationBuilderPattern() {
        // Arrange
        when(metricsService.recordOperation(eq("service-health"), eq(TEST_SERVICE_NAME))).thenReturn(operationRecorder);
        when(operationRecorder.withSuccess(any(Boolean.class))).thenReturn(operationRecorder);
        when(operationRecorder.withDuration(any(Long.class))).thenReturn(operationRecorder);
        when(operationRecorder.withData(any(String.class), any(Object.class))).thenReturn(operationRecorder);

        // Act
        serviceHealthState.recordSuccess(TEST_RESPONSE_TIME);

        // Assert - verify the builder pattern is used correctly
        ArgumentCaptor<Long> durationCaptor = ArgumentCaptor.forClass(Long.class);
        verify(operationRecorder).withDuration(durationCaptor.capture());
        assertEquals(Duration.ofMillis(TEST_RESPONSE_TIME).toNanos(), durationCaptor.getValue().longValue());
    }

    @Test
    void testWithNullMetricsServiceGracefulDegradation() {
        // Arrange
        ServiceHealthState nullServiceState = new ServiceHealthState(TEST_SERVICE_NAME, null);

        // Act & Assert - should not throw exceptions
        nullServiceState.recordSuccess(TEST_RESPONSE_TIME);
        nullServiceState.recordFailure(new RuntimeException(TEST_ERROR_MESSAGE));
        nullServiceState.forceRecovery();

        // Check that health calculations return default values
        assertEquals(0.0, nullServiceState.getSuccessRate(), 0.001);
        assertEquals(0.0, nullServiceState.getAverageResponseTime(), 0.001);
        assertFalse(nullServiceState.isHealthy());
    }

    @Test
    void testMetricKeyGeneration() {
        // Arrange
        ArgumentCaptor<MetricKey> metricKeyCaptor = ArgumentCaptor.forClass(MetricKey.class);
        setupMockSnapshot(100L, 85L, 10_000_000_000L);

        // Act
        serviceHealthState.getSuccessRate();

        // Assert
        verify(metricsService).getSnapshot(metricKeyCaptor.capture(), eq(GenericMetricsSnapshot.class));
        MetricKey capturedKey = metricKeyCaptor.getValue();

        // Verify the metric key structure
        assertNotNull(capturedKey);
        // Additional metric key validation would go here based on the MetricKeys.custom implementation
    }

    private void setupMockSnapshot(long total, long success, long totalDurationNanos) {
        GenericMetricsSnapshot mockSnapshot = mock(GenericMetricsSnapshot.class);
        when(mockSnapshot.getTotal()).thenReturn(total);
        when(mockSnapshot.getSuccess()).thenReturn(success);
        when(mockSnapshot.getTotalDurationNanos()).thenReturn(totalDurationNanos);

        when(metricsService.getSnapshot(any(MetricKey.class), eq(GenericMetricsSnapshot.class)))
                .thenReturn(mockSnapshot);
    }

    private void setupMockSnapshotNull() {
        when(metricsService.getSnapshot(any(MetricKey.class), eq(GenericMetricsSnapshot.class))).thenReturn(null);
    }
}
