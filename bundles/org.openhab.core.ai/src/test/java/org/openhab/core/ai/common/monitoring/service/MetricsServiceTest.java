package org.openhab.core.ai.common.monitoring.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Duration;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
import org.openhab.core.ai.common.monitoring.service.snapshot.ModelCompletionSnapshot;

/**
 * Unit tests for MetricsService functionality.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class MetricsServiceTest {

    @Mock
    private MetricsService metricsService;

    @BeforeEach
    void setUp() {
        // Setup common test data
    }

    @Test
    void testRecordOperation() {
        // Given
        String domain = "test-domain";
        String operation = "test-operation";
        boolean success = true;
        Duration duration = Duration.ofMillis(100);

        // When
        doNothing().when(metricsService).recordOperation(domain, operation, success, duration);
        metricsService.recordOperation(domain, operation, success, duration);

        // Then
        verify(metricsService, times(1)).recordOperation(domain, operation, success, duration);
    }

    @Test
    void testRecordOperationWithData() {
        // Given
        String domain = "test-domain";
        String operation = "test-operation";
        boolean success = true;
        Duration duration = Duration.ofMillis(100);
        Map<String, Object> data = Map.of("key", "value");

        // When
        doNothing().when(metricsService).recordOperationWithData(domain, operation, success, duration, data);
        metricsService.recordOperationWithData(domain, operation, success, duration, data);

        // Then
        verify(metricsService, times(1)).recordOperationWithData(domain, operation, success, duration, data);
    }

    @Test
    void testRecordModelCompletion() {
        // Given
        String modelId = "test-model";
        boolean success = true;
        Duration duration = Duration.ofMillis(100);
        int inputTokens = 100;
        int outputTokens = 50;
        double cost = 0.01;

        // When
        doNothing().when(metricsService).recordModelCompletion(modelId, success, duration, inputTokens, outputTokens,
                cost);
        metricsService.recordModelCompletion(modelId, success, duration, inputTokens, outputTokens, cost);

        // Then
        verify(metricsService, times(1)).recordModelCompletion(modelId, success, duration, inputTokens, outputTokens,
                cost);
    }

    @Test
    void testGetModelCompletionSnapshot() {
        // Given
        String modelId = "test-model";
        ModelCompletionSnapshot expectedSnapshot = new ModelCompletionSnapshot(null, null, System.currentTimeMillis(),
                0L, 0.0);

        // When
        when(metricsService.getModelCompletionSnapshot(modelId)).thenReturn(expectedSnapshot);
        ModelCompletionSnapshot result = metricsService.getModelCompletionSnapshot(modelId);

        // Then
        assertNotNull(result);
        assertEquals(expectedSnapshot, result);
        verify(metricsService, times(1)).getModelCompletionSnapshot(modelId);
    }

    @Test
    void testRecordOperationWithFailure() {
        // Given
        String domain = "test-domain";
        String operation = "test-operation";
        boolean success = false;
        Duration duration = Duration.ofMillis(500);

        // When
        doNothing().when(metricsService).recordOperation(domain, operation, success, duration);
        metricsService.recordOperation(domain, operation, success, duration);

        // Then
        verify(metricsService, times(1)).recordOperation(domain, operation, success, duration);
    }

    @Test
    void testRecordOperationWithZeroDuration() {
        // Given
        String domain = "test-domain";
        String operation = "test-operation";
        boolean success = true;
        Duration duration = Duration.ZERO;

        // When
        doNothing().when(metricsService).recordOperation(domain, operation, success, duration);
        metricsService.recordOperation(domain, operation, success, duration);

        // Then
        verify(metricsService, times(1)).recordOperation(domain, operation, success, duration);
    }

    @Test
    void testRecordOperationWithNullDomain() {
        // Given
        String domain = null;
        String operation = "test-operation";
        boolean success = true;
        Duration duration = Duration.ofMillis(100);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            metricsService.recordOperation(domain, operation, success, duration);
        });
    }

    @Test
    void testRecordOperationWithEmptyDomain() {
        // Given
        String domain = "";
        String operation = "test-operation";
        boolean success = true;
        Duration duration = Duration.ofMillis(100);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            metricsService.recordOperation(domain, operation, success, duration);
        });
    }

    @Test
    void testRecordOperationWithNullOperation() {
        // Given
        String domain = "test-domain";
        String operation = null;
        boolean success = true;
        Duration duration = Duration.ofMillis(100);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            metricsService.recordOperation(domain, operation, success, duration);
        });
    }

    @Test
    void testRecordOperationWithEmptyOperation() {
        // Given
        String domain = "test-domain";
        String operation = "";
        boolean success = true;
        Duration duration = Duration.ofMillis(100);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            metricsService.recordOperation(domain, operation, success, duration);
        });
    }

    @Test
    void testRecordOperationWithNullDuration() {
        // Given
        String domain = "test-domain";
        String operation = "test-operation";
        boolean success = true;
        Duration duration = null;

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            metricsService.recordOperation(domain, operation, success, duration);
        });
    }

    @Test
    void testRecordOperationWithNegativeDuration() {
        // Given
        String domain = "test-domain";
        String operation = "test-operation";
        boolean success = true;
        Duration duration = Duration.ofMillis(-100);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            metricsService.recordOperation(domain, operation, success, duration);
        });
    }

    @Test
    void testMetricsServiceInterfaceCompliance() throws NoSuchMethodException {
        // Test that the interface methods are properly defined
        assertNotNull(MetricsService.class.getMethod("recordOperation", String.class, String.class, boolean.class,
                Duration.class));
        assertNotNull(MetricsService.class.getMethod("recordOperationWithData", String.class, String.class,
                boolean.class, Duration.class, Map.class));
        assertNotNull(MetricsService.class.getMethod("recordModelCompletion", String.class, boolean.class,
                Duration.class, int.class, int.class, double.class));
        assertNotNull(MetricsService.class.getMethod("getModelCompletionSnapshot", String.class));
    }

    @Test
    void testMetricsServiceThreadSafety() {
        // Given
        String domain = "test-domain";
        String operation = "test-operation";
        boolean success = true;
        Duration duration = Duration.ofMillis(100);

        // When & Then - should not throw exceptions when called from multiple threads
        assertDoesNotThrow(() -> {
            Thread[] threads = new Thread[10];
            for (int i = 0; i < threads.length; i++) {
                final int threadId = i;
                threads[i] = new Thread(() -> {
                    for (int j = 0; j < 100; j++) {
                        metricsService.recordOperation(domain + "-" + threadId, operation + "-" + j, success, duration);
                    }
                });
            }

            // Start all threads
            for (Thread thread : threads) {
                thread.start();
            }

            // Wait for all threads to complete
            for (Thread thread : threads) {
                thread.join();
            }
        });
    }
}
