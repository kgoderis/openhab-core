package org.openhab.core.ai.agent.infrastructure.performance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
import org.openhab.core.ai.common.monitoring.service.OperationRecorder;
import org.openhab.core.ai.common.monitoring.service.snapshot.GenericMetricsSnapshot;
import org.openhab.core.ai.common.monitoring.service.snapshot.MessageLatencySnapshot;

/**
 * Unit tests for MessageLatencyMetrics.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class MessageLatencyMetricsTest {

    private static final String AGENT_ID = "test-agent";
    private static final String MESSAGE_TYPE = "test-message";

    @Mock
    private MetricsService metricsService;

    @Mock
    private OperationRecorder operationRecorder;

    @Mock
    private GenericMetricsSnapshot genericSnapshot;

    private MessageLatencyMetrics messageLatencyMetrics;

    @BeforeEach
    void setUp() throws Exception {
        messageLatencyMetrics = new MessageLatencyMetrics(AGENT_ID);

        // Inject the mock MetricsService using reflection
        Field metricsServiceField = MessageLatencyMetrics.class.getDeclaredField("metricsService");
        metricsServiceField.setAccessible(true);
        metricsServiceField.set(messageLatencyMetrics, metricsService);
    }

    @Test
    void testRecordLatency_Success() {
        // Arrange
        Duration latency = Duration.ofMillis(1000);
        when(metricsService.recordOperation(anyString(), anyString())).thenReturn(operationRecorder);
        when(operationRecorder.withSuccess(any(Boolean.class))).thenReturn(operationRecorder);
        when(operationRecorder.withDuration(any(Long.class))).thenReturn(operationRecorder);
        when(operationRecorder.withData(anyString(), any())).thenReturn(operationRecorder);

        // Act
        messageLatencyMetrics.recordLatency(MESSAGE_TYPE, latency);

        // Assert
        verify(metricsService).recordOperation("message-latency", AGENT_ID);
        verify(operationRecorder).withSuccess(true); // Should be true for latency < 5000ms
        verify(operationRecorder).withDuration(latency.toNanos());
        verify(operationRecorder).withData("messageType", MESSAGE_TYPE);
        verify(operationRecorder).withData("latencyMs", latency.toMillis());
        verify(operationRecorder).record();
    }

    @Test
    void testRecordLatency_Failure() {
        // Arrange
        Duration latency = Duration.ofMillis(6000); // Above 5000ms threshold
        when(metricsService.recordOperation(anyString(), anyString())).thenReturn(operationRecorder);
        when(operationRecorder.withSuccess(any(Boolean.class))).thenReturn(operationRecorder);
        when(operationRecorder.withDuration(any(Long.class))).thenReturn(operationRecorder);
        when(operationRecorder.withData(anyString(), any())).thenReturn(operationRecorder);

        // Act
        messageLatencyMetrics.recordLatency(MESSAGE_TYPE, latency);

        // Assert
        verify(operationRecorder).withSuccess(false); // Should be false for latency >= 5000ms
    }

    @Test
    void testGetStatistics_WithMetricsService() {
        // Arrange
        when(metricsService.getSnapshot(eq("message-latency"), eq(AGENT_ID))).thenReturn(genericSnapshot);
        when(genericSnapshot.getDouble("averageLatencyMs", 0.0)).thenReturn(150.0);
        when(genericSnapshot.getDouble("messageThroughput", 0.0)).thenReturn(10.0);
        when(genericSnapshot.getLong("messageQueueDepth", 0L)).thenReturn(5L);
        when(genericSnapshot.getTotal()).thenReturn(100L);
        when(genericSnapshot.getSuccess()).thenReturn(95L);
        when(genericSnapshot.getFailure()).thenReturn(5L);
        when(genericSnapshot.getTotalDurationNanos()).thenReturn(15000000000L); // 15 seconds in nanos
        when(genericSnapshot.getTimestampMs()).thenReturn(System.currentTimeMillis());

        // Act
        MessageLatencySnapshot statistics = messageLatencyMetrics.getStatistics();

        // Assert
        assertNotNull(statistics);
        assertEquals(AGENT_ID, statistics.getAgentId());
        assertEquals(100L, statistics.total());
        assertEquals(95L, statistics.success());
        assertEquals(5L, statistics.failure());
        assertEquals(150.0, statistics.getAverageLatencyMs());
        verify(metricsService).getSnapshot("message-latency", AGENT_ID);
    }

    @Test
    void testGetStatistics_WithoutMetricsService() throws Exception {
        // Arrange - Remove MetricsService reference
        Field metricsServiceField = MessageLatencyMetrics.class.getDeclaredField("metricsService");
        metricsServiceField.setAccessible(true);
        metricsServiceField.set(messageLatencyMetrics, null);

        // Act
        MessageLatencySnapshot statistics = messageLatencyMetrics.getStatistics();

        // Assert
        assertNotNull(statistics);
        assertEquals(AGENT_ID, statistics.getAgentId());
        assertEquals(0L, statistics.total());
        assertEquals(0L, statistics.success());
        assertEquals(0L, statistics.failure());
    }

    @Test
    void testGetTotalMessages() {
        // Arrange
        when(metricsService.getSnapshot(eq("message-latency"), eq(AGENT_ID))).thenReturn(genericSnapshot);
        when(genericSnapshot.getTotal()).thenReturn(42L);
        when(genericSnapshot.getSuccess()).thenReturn(40L);
        when(genericSnapshot.getFailure()).thenReturn(2L);
        when(genericSnapshot.getTotalDurationNanos()).thenReturn(1000000000L);
        when(genericSnapshot.getTimestampMs()).thenReturn(System.currentTimeMillis());
        when(genericSnapshot.getDouble("averageLatencyMs", 0.0)).thenReturn(100.0);
        when(genericSnapshot.getDouble("messageThroughput", 0.0)).thenReturn(5.0);
        when(genericSnapshot.getLong("messageQueueDepth", 0L)).thenReturn(0L);

        // Act
        long totalMessages = messageLatencyMetrics.getTotalMessages();

        // Assert
        assertEquals(42L, totalMessages);
    }

    @Test
    void testRecordLatency_MetricsServiceThrowsException() {
        // Arrange
        Duration latency = Duration.ofMillis(1000);
        when(metricsService.recordOperation(anyString(), anyString()))
                .thenThrow(new RuntimeException("Test exception"));

        // Act - Should not throw exception due to graceful degradation
        messageLatencyMetrics.recordLatency(MESSAGE_TYPE, latency);

        // Assert - Verify that we still tried to record
        verify(metricsService).recordOperation("message-latency", AGENT_ID);
    }

    @Test
    void testGetStatistics_MetricsServiceThrowsException() {
        // Arrange
        when(metricsService.getSnapshot(anyString(), anyString())).thenThrow(new RuntimeException("Test exception"));

        // Act
        MessageLatencySnapshot statistics = messageLatencyMetrics.getStatistics();

        // Assert - Should return empty snapshot on exception
        assertNotNull(statistics);
        assertEquals(AGENT_ID, statistics.getAgentId());
        assertEquals(0L, statistics.total());
    }

    @Test
    void testGetAgentId() {
        // Act & Assert
        assertEquals(AGENT_ID, messageLatencyMetrics.getAgentId());
    }

    @Test
    void testGetAverageLatency_WithData() {
        // Arrange - Add some local latency data
        messageLatencyMetrics.recordLatency("type1", Duration.ofMillis(100));
        messageLatencyMetrics.recordLatency("type1", Duration.ofMillis(200));
        messageLatencyMetrics.recordLatency("type2", Duration.ofMillis(300));

        // Act
        Duration averageLatency = messageLatencyMetrics.getAverageLatency();

        // Assert
        assertNotNull(averageLatency);
        assertEquals(200, averageLatency.toMillis()); // (100 + 200 + 300) / 3 = 200
    }

    @Test
    void testGetAverageLatency_NoData() {
        // Act
        Duration averageLatency = messageLatencyMetrics.getAverageLatency();

        // Assert
        assertEquals(null, averageLatency);
    }
}
