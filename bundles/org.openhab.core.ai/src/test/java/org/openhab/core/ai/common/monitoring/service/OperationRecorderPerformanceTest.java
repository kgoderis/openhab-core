package org.openhab.core.ai.common.monitoring.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.ai.common.monitoring.api.MetricsService;

/**
 * Test class for OperationRecorder performance metrics enhancements.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class OperationRecorderPerformanceTest {

    @Mock
    private MetricsService metricsService;

    private OperationRecorder recorder;

    @BeforeEach
    void setUp() {
        recorder = new OperationRecorder(metricsService, "test", "operation");
    }

    @Test
    void testWithTimingContext() {
        // Test valid timing context
        OperationRecorder result = recorder.withTimingContext(5, 1000L, 500L);
        assertSame(recorder, result);

        // Test invalid complexity
        assertThrows(IllegalArgumentException.class, () -> {
            recorder.withTimingContext(0, 1000L, 500L);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            recorder.withTimingContext(11, 1000L, 500L);
        });

        // Test invalid data sizes
        assertThrows(IllegalArgumentException.class, () -> {
            recorder.withTimingContext(5, -1L, 500L);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            recorder.withTimingContext(5, 1000L, -1L);
        });

        // Test recording with timing context
        recorder.withSuccess(true).withDuration(Duration.ofMillis(100).toNanos()).withTimingContext(7, 2000L, 1000L)
                .record();

        verify(metricsService).recordOperationWithData(eq("test"), eq("operation"), eq(true),
                eq(Duration.ofMillis(100)),
                argThat(data -> data.containsKey("complexity") && data.get("complexity").equals(7)
                        && data.containsKey("inputDataSize") && data.get("inputDataSize").equals(2000L)
                        && data.containsKey("outputDataSize") && data.get("outputDataSize").equals(1000L)));
    }

    @Test
    void testWithResourceUtilization() {
        // Test valid resource utilization
        OperationRecorder result = recorder.withResourceUtilization(100_000_000L, 50.0, 25L);
        assertSame(recorder, result);

        // Test invalid memory usage
        assertThrows(IllegalArgumentException.class, () -> {
            recorder.withResourceUtilization(-1L, 50.0, 25L);
        });

        // Test invalid CPU usage
        assertThrows(IllegalArgumentException.class, () -> {
            recorder.withResourceUtilization(100_000_000L, -1.0, 25L);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            recorder.withResourceUtilization(100_000_000L, 101.0, 25L);
        });

        // Test invalid disk I/O time
        assertThrows(IllegalArgumentException.class, () -> {
            recorder.withResourceUtilization(100_000_000L, 50.0, -1L);
        });

        // Test recording with resource utilization
        recorder.withSuccess(true).withDuration(Duration.ofMillis(200).toNanos())
                .withResourceUtilization(150_000_000L, 75.5, 50L).record();

        verify(metricsService).recordOperationWithData(eq("test"), eq("operation"), eq(true),
                eq(Duration.ofMillis(200)),
                argThat(data -> data.containsKey("memoryUsedBytes") && data.get("memoryUsedBytes").equals(150_000_000L)
                        && data.containsKey("cpuUsagePercent") && data.get("cpuUsagePercent").equals(75.5)
                        && data.containsKey("diskIOTimeMs") && data.get("diskIOTimeMs").equals(50L)));
    }

    @Test
    void testWithConcurrencyMetrics() {
        // Test valid concurrency metrics
        OperationRecorder result = recorder.withConcurrencyMetrics(5, 10, 2);
        assertSame(recorder, result);

        // Test invalid concurrent operations
        assertThrows(IllegalArgumentException.class, () -> {
            recorder.withConcurrencyMetrics(-1, 10, 2);
        });

        // Test invalid queue size
        assertThrows(IllegalArgumentException.class, () -> {
            recorder.withConcurrencyMetrics(5, -1, 2);
        });

        // Test invalid contention count
        assertThrows(IllegalArgumentException.class, () -> {
            recorder.withConcurrencyMetrics(5, 10, -1);
        });

        // Test recording with concurrency metrics
        recorder.withSuccess(true).withDuration(Duration.ofMillis(150).toNanos()).withConcurrencyMetrics(8, 15, 3)
                .record();

        verify(metricsService).recordOperationWithData(eq("test"), eq("operation"), eq(true),
                eq(Duration.ofMillis(150)),
                argThat(data -> data.containsKey("concurrentOperations") && data.get("concurrentOperations").equals(8)
                        && data.containsKey("queueSize") && data.get("queueSize").equals(15)
                        && data.containsKey("contentionCount") && data.get("contentionCount").equals(3)));
    }

    @Test
    void testWithQualityMetrics() {
        // Test valid quality metrics with error
        OperationRecorder result = recorder.withQualityMetrics("timeout", 3, 2, true);
        assertSame(recorder, result);

        // Test valid quality metrics without error
        result = recorder.withQualityMetrics(null, null, 0, false);
        assertSame(recorder, result);

        // Test invalid retry count
        assertThrows(IllegalArgumentException.class, () -> {
            recorder.withQualityMetrics("timeout", 3, -1, true);
        });

        // Test invalid error severity
        assertThrows(IllegalArgumentException.class, () -> {
            recorder.withQualityMetrics("timeout", 0, 2, true);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            recorder.withQualityMetrics("timeout", 6, 2, true);
        });

        // Test recording with quality metrics
        recorder.withSuccess(false).withDuration(Duration.ofMillis(300).toNanos())
                .withQualityMetrics("connection-error", 4, 3, true).record();

        verify(metricsService).recordOperationWithData(eq("test"), eq("operation"), eq(false),
                eq(Duration.ofMillis(300)),
                argThat(data -> data.containsKey("errorCategory")
                        && data.get("errorCategory").equals("connection-error") && data.containsKey("errorSeverity")
                        && data.get("errorSeverity").equals(4) && data.containsKey("retryCount")
                        && data.get("retryCount").equals(3) && data.containsKey("fallbackUsed")
                        && data.get("fallbackUsed").equals(true)));
    }

    @Test
    void testWithUserExperienceMetrics() {
        // Test valid user experience metrics
        OperationRecorder result = recorder.withUserExperienceMetrics(250L, 1500L, 4);
        assertSame(recorder, result);

        // Test without satisfaction score
        result = recorder.withUserExperienceMetrics(250L, 1500L, null);
        assertSame(recorder, result);

        // Test invalid user perceived latency
        assertThrows(IllegalArgumentException.class, () -> {
            recorder.withUserExperienceMetrics(-1L, 1500L, 4);
        });

        // Test invalid user interaction time
        assertThrows(IllegalArgumentException.class, () -> {
            recorder.withUserExperienceMetrics(250L, -1L, 4);
        });

        // Test invalid satisfaction score
        assertThrows(IllegalArgumentException.class, () -> {
            recorder.withUserExperienceMetrics(250L, 1500L, 0);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            recorder.withUserExperienceMetrics(250L, 1500L, 6);
        });

        // Test recording with user experience metrics
        recorder.withSuccess(true).withDuration(Duration.ofMillis(200).toNanos())
                .withUserExperienceMetrics(300L, 2000L, 5).record();

        verify(metricsService).recordOperationWithData(eq("test"), eq("operation"), eq(true),
                eq(Duration.ofMillis(200)),
                argThat(data -> data.containsKey("userPerceivedLatencyMs")
                        && data.get("userPerceivedLatencyMs").equals(300L) && data.containsKey("userInteractionTimeMs")
                        && data.get("userInteractionTimeMs").equals(2000L) && data.containsKey("userSatisfactionScore")
                        && data.get("userSatisfactionScore").equals(5)));
    }

    @Test
    void testWithSystemHealthCorrelation() {
        // Test valid system health correlation
        OperationRecorder result = recorder.withSystemHealthCorrelation(85.5, 2, 90.0, 1.5);
        assertSame(recorder, result);

        // Test invalid system health score
        assertThrows(IllegalArgumentException.class, () -> {
            recorder.withSystemHealthCorrelation(-1.0, 2, 90.0, 1.5);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            recorder.withSystemHealthCorrelation(101.0, 2, 90.0, 1.5);
        });

        // Test invalid active alerts
        assertThrows(IllegalArgumentException.class, () -> {
            recorder.withSystemHealthCorrelation(85.5, -1, 90.0, 1.5);
        });

        // Test invalid resource availability
        assertThrows(IllegalArgumentException.class, () -> {
            recorder.withSystemHealthCorrelation(85.5, 2, -1.0, 1.5);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            recorder.withSystemHealthCorrelation(85.5, 2, 101.0, 1.5);
        });

        // Test invalid system load average
        assertThrows(IllegalArgumentException.class, () -> {
            recorder.withSystemHealthCorrelation(85.5, 2, 90.0, -1.0);
        });

        // Test recording with system health correlation
        recorder.withSuccess(true).withDuration(Duration.ofMillis(100).toNanos())
                .withSystemHealthCorrelation(92.3, 1, 88.7, 0.8).record();

        verify(metricsService).recordOperationWithData(eq("test"), eq("operation"), eq(true),
                eq(Duration.ofMillis(100)),
                argThat(data -> data.containsKey("systemHealthScore") && data.get("systemHealthScore").equals(92.3)
                        && data.containsKey("activeAlerts") && data.get("activeAlerts").equals(1)
                        && data.containsKey("resourceAvailability") && data.get("resourceAvailability").equals(88.7)
                        && data.containsKey("systemLoadAverage") && data.get("systemLoadAverage").equals(0.8)));
    }

    @Test
    void testCombinedPerformanceMetrics() {
        // Test recording with all performance metrics combined
        recorder.withSuccess(true).withDuration(Duration.ofMillis(500).toNanos()).withTimingContext(8, 5000L, 2000L)
                .withResourceUtilization(200_000_000L, 65.0, 100L).withConcurrencyMetrics(6, 12, 2)
                .withQualityMetrics(null, null, 1, false).withUserExperienceMetrics(600L, 3000L, 4)
                .withSystemHealthCorrelation(88.5, 1, 85.0, 1.2).record();

        verify(metricsService).recordOperationWithData(eq("test"), eq("operation"), eq(true),
                eq(Duration.ofMillis(500)), argThat(data -> {
                    // Verify all performance metrics are present
                    return data.containsKey("complexity") && data.containsKey("inputDataSize")
                            && data.containsKey("outputDataSize") && data.containsKey("memoryUsedBytes")
                            && data.containsKey("cpuUsagePercent") && data.containsKey("diskIOTimeMs")
                            && data.containsKey("concurrentOperations") && data.containsKey("queueSize")
                            && data.containsKey("contentionCount") && data.containsKey("retryCount")
                            && data.containsKey("fallbackUsed") && data.containsKey("userPerceivedLatencyMs")
                            && data.containsKey("userInteractionTimeMs") && data.containsKey("userSatisfactionScore")
                            && data.containsKey("systemHealthScore") && data.containsKey("activeAlerts")
                            && data.containsKey("resourceAvailability") && data.containsKey("systemLoadAverage");
                }));
    }

    @Test
    void testMethodChaining() {
        // Test that all methods can be chained together
        OperationRecorder result = recorder.withSuccess(true).withDuration(Duration.ofMillis(100).toNanos())
                .withTimingContext(5, 1000L, 500L).withResourceUtilization(50_000_000L, 25.0, 10L)
                .withConcurrencyMetrics(3, 5, 1).withQualityMetrics(null, null, 0, false)
                .withUserExperienceMetrics(150L, 1000L, 4).withSystemHealthCorrelation(95.0, 0, 98.0, 0.5);

        assertSame(recorder, result);

        // Verify the recording works
        result.record();
        verify(metricsService).recordOperationWithData(anyString(), anyString(), anyBoolean(), any(Duration.class),
                any());
    }
}
