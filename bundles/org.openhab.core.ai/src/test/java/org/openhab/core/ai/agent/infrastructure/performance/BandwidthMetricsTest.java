package org.openhab.core.ai.agent.infrastructure.performance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
import org.openhab.core.ai.common.monitoring.service.OperationRecorder;
import org.openhab.core.ai.common.monitoring.service.snapshot.BandwidthSnapshot;
import org.openhab.core.ai.common.monitoring.service.snapshot.GenericMetricsSnapshot;

/**
 * Unit tests for BandwidthMetrics class.
 * 
 * <p>
 * Tests the migration to centralized MetricsService, ensuring that
 * the class correctly uses the MetricsService for all statistics
 * collection and follows the centralized-only approach.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class BandwidthMetricsTest {

    @Mock
    private MetricsService metricsService;

    @Mock
    private OperationRecorder operationRecorder;

    @Mock
    private GenericMetricsSnapshot genericSnapshot;

    private BandwidthMetrics bandwidthMetrics;

    @BeforeEach
    void setUp() throws Exception {
        bandwidthMetrics = new BandwidthMetrics("test-agent");

        // Inject the mock MetricsService using reflection
        Field metricsServiceField = BandwidthMetrics.class.getDeclaredField("metricsService");
        metricsServiceField.setAccessible(true);
        metricsServiceField.set(bandwidthMetrics, metricsService);
    }

    @Test
    void testConstructor() {
        assertEquals("test-agent", bandwidthMetrics.getAgentId());
    }

    @Test
    void testRecordBandwidthWithMetricsService() {
        // Arrange
        when(metricsService.recordOperation(eq("bandwidth"), eq("test-agent"))).thenReturn(operationRecorder);
        when(operationRecorder.withSuccess(any(Boolean.class))).thenReturn(operationRecorder);
        when(operationRecorder.withDuration(any(Long.class))).thenReturn(operationRecorder);
        when(operationRecorder.withData(anyString(), any())).thenReturn(operationRecorder);

        // Act
        bandwidthMetrics.recordBandwidth(1024L);

        // Assert
        verify(metricsService).recordOperation("bandwidth", "test-agent");
        verify(operationRecorder).withSuccess(true);
        verify(operationRecorder).withData("bytesPerSecond", 1024L);
        verify(operationRecorder).withData("agentId", "test-agent");
        verify(operationRecorder).withData("transferType", "bandwidth_measurement");
        verify(operationRecorder).record();
    }

    @Test
    void testRecordBandwidthWithZeroBytes() {
        // Arrange
        when(metricsService.recordOperation(eq("bandwidth"), eq("test-agent"))).thenReturn(operationRecorder);
        when(operationRecorder.withSuccess(any(Boolean.class))).thenReturn(operationRecorder);
        when(operationRecorder.withDuration(any(Long.class))).thenReturn(operationRecorder);
        when(operationRecorder.withData(anyString(), any())).thenReturn(operationRecorder);

        // Act
        bandwidthMetrics.recordBandwidth(0L);

        // Assert
        verify(operationRecorder).withSuccess(false); // Should mark as failure for zero bytes
    }

    @Test
    void testGetStatisticsFromMetricsService() {
        // Arrange
        when(metricsService.getSnapshot("bandwidth", "test-agent")).thenReturn(genericSnapshot);
        when(genericSnapshot.getTotal()).thenReturn(10L);
        when(genericSnapshot.getSuccess()).thenReturn(8L);
        when(genericSnapshot.getFailure()).thenReturn(2L);
        when(genericSnapshot.getTotalDurationNanos()).thenReturn(1000000000L);
        when(genericSnapshot.getLong("bytesPerSecond", 0L)).thenReturn(1024L);
        when(genericSnapshot.getLong("totalBytes", 0L)).thenReturn(10240L);
        when(genericSnapshot.getDouble("peakBandwidth", 0.0)).thenReturn(2048.0);
        when(genericSnapshot.getDouble("utilizationPercent", 0.0)).thenReturn(75.0);

        // Act
        BandwidthSnapshot snapshot = bandwidthMetrics.getStatistics();

        // Assert
        assertNotNull(snapshot);
        assertEquals(10L, snapshot.total());
        assertEquals(8L, snapshot.success());
        assertEquals(2L, snapshot.failure());
        assertEquals(1000000000L, snapshot.totalDurationNanos());
        assertEquals(1024L, snapshot.getAverageBandwidthBytesPerSecond());
        assertEquals(10240L, snapshot.getTotalBytesTransferred());
        assertEquals(2048.0, snapshot.getPeakBandwidthBytesPerSecond());
        assertEquals(75.0, snapshot.getBandwidthUtilizationPercent());
    }

    @Test
    void testGetStatisticsWithNullMetricsService() throws Exception {
        // Arrange - set metricsService to null
        Field metricsServiceField = BandwidthMetrics.class.getDeclaredField("metricsService");
        metricsServiceField.setAccessible(true);
        metricsServiceField.set(bandwidthMetrics, null);

        // Act
        BandwidthSnapshot snapshot = bandwidthMetrics.getStatistics();

        // Assert
        assertNotNull(snapshot);
        assertEquals(0L, snapshot.total());
        assertEquals(0L, snapshot.success());
        assertEquals(0L, snapshot.failure());
        assertEquals(0L, snapshot.getTotalBytesTransferred());
    }

    @Test
    void testGetTotalBytesFromMetricsService() {
        // Arrange
        when(metricsService.getSnapshot("bandwidth", "test-agent")).thenReturn(genericSnapshot);
        when(genericSnapshot.getTotal()).thenReturn(10L);
        when(genericSnapshot.getSuccess()).thenReturn(8L);
        when(genericSnapshot.getFailure()).thenReturn(2L);
        when(genericSnapshot.getTotalDurationNanos()).thenReturn(1000000000L);
        when(genericSnapshot.getLong("bytesPerSecond", 0L)).thenReturn(1024L);
        when(genericSnapshot.getLong("totalBytes", 0L)).thenReturn(10240L);
        when(genericSnapshot.getDouble("peakBandwidth", 0.0)).thenReturn(2048.0);
        when(genericSnapshot.getDouble("utilizationPercent", 0.0)).thenReturn(75.0);

        // Act
        long totalBytes = bandwidthMetrics.getTotalBytes();

        // Assert
        assertEquals(10240L, totalBytes);
        verify(metricsService).getSnapshot("bandwidth", "test-agent");
    }

    @Test
    void testGetAverageBandwidthLegacyMethod() {
        // Test the legacy getAverageBandwidth method still works
        bandwidthMetrics.recordBandwidth(1000L);
        bandwidthMetrics.recordBandwidth(2000L);
        bandwidthMetrics.recordBandwidth(3000L);

        long avgBandwidth = bandwidthMetrics.getAverageBandwidth();
        assertEquals(2000L, avgBandwidth);
    }

    @Test
    void testRecordBandwidthWithMetricsServiceException() throws Exception {
        // Arrange - Mock MetricsService to throw exception
        when(metricsService.recordOperation(anyString(), anyString()))
                .thenThrow(new RuntimeException("Test exception"));

        // Act - Should not throw exception due to error handling
        bandwidthMetrics.recordBandwidth(1024L);

        // Assert - Method should complete without throwing exception
        // The bandwidth sample should still be recorded locally
        assertEquals(1024L, bandwidthMetrics.getAverageBandwidth());
    }
}
