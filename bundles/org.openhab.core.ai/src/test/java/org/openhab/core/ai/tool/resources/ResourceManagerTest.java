package org.openhab.core.ai.tool.resources;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.ai.common.monitoring.api.MetricKey;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
import org.openhab.core.ai.model.api.ModelProviderType;
import org.openhab.core.ai.tool.resources.monitoring.ResourceManagerSnapshot;
import org.openhab.core.ai.tool.resources.monitoring.ResourceManagerStatistics;

/**
 * Unit tests for ResourceManager MetricsService integration.
 * 
 * @author Karel Goderis - Initial Contribution
 */
@NonNullByDefault
@ExtendWith(MockitoExtension.class)
class ResourceManagerTest {

    @Mock
    private MetricsService metricsService;

    private ResourceManager resourceManager;

    @BeforeEach
    void setUp() {
        resourceManager = new ResourceManager();
        resourceManager.setMetricsService(metricsService);
    }

    @Test
    void testSuccessfulRequestRecordsMetrics() {
        // Given
        String requestId = "test-request-1";
        ModelProviderType provider = ModelProviderType.OPENAI;

        // When
        CompletableFuture<String> future = resourceManager.submitRequest(requestId, provider, 1, () -> "success");

        // Then
        assertDoesNotThrow(() -> future.get());
        assertEquals("success", future.join());

        // Verify metrics were recorded
        verify(metricsService, atLeastOnce()).recordOperationWithData(eq("resource-manager"), anyString(), eq(true),
                any(Duration.class), any());
    }

    @Test
    void testFailedRequestRecordsMetrics() {
        // Given
        String requestId = "test-request-2";
        ModelProviderType provider = ModelProviderType.ANTHROPIC;
        RuntimeException testException = new RuntimeException("Test failure");

        // When
        CompletableFuture<String> future = resourceManager.submitRequest(requestId, provider, 1, () -> {
            throw testException;
        });

        // Then
        assertThrows(RuntimeException.class, () -> future.get());

        // Verify failure metrics were recorded
        verify(metricsService, atLeastOnce()).recordOperationWithData(eq("resource-manager"), anyString(), eq(false),
                any(Duration.class), any());
    }

    @Test
    void testResourceLimitExceededRecordsRejection() {
        // Given
        resourceManager.setMaxConcurrentRequests(0); // Force rejection
        String requestId = "test-request-3";
        ModelProviderType provider = ModelProviderType.GOOGLE;

        // When
        CompletableFuture<String> future = resourceManager.submitRequest(requestId, provider, 1, () -> "success");

        // Then
        assertTrue(future.isCompletedExceptionally());

        // Verify rejection metrics were recorded
        verify(metricsService).recordOperationWithData(eq("resource-manager"), eq("request-rejected"), eq(false),
                eq(Duration.ZERO), any());
    }

    @Test
    void testGetResourceManagerSnapshotWithMetricsService() {
        // Given
        ResourceManagerSnapshot mockSnapshot = ResourceManagerSnapshot.createWithSystemResources(10L, 8L, 1L, 1L, 0L,
                5000000000L, 5, 10);
        when(metricsService.getSnapshot(any(MetricKey.class), eq(ResourceManagerSnapshot.class)))
                .thenReturn(mockSnapshot);

        // When
        ResourceManagerSnapshot result = resourceManager.getResourceManagerSnapshot();

        // Then
        assertNotNull(result);
        assertEquals(10L, result.totalRequests());
        assertEquals(8L, result.successfulRequests());
        verify(metricsService).getSnapshot(any(MetricKey.class), eq(ResourceManagerSnapshot.class));
    }

    @Test
    void testGetResourceManagerSnapshotWithoutMetricsService() {
        // Given
        resourceManager.unsetMetricsService(metricsService);

        // When
        ResourceManagerSnapshot result = resourceManager.getResourceManagerSnapshot();

        // Then
        assertNull(result);
    }

    @Test
    void testGetResourceManagerStatisticsWithMetricsService() {
        // Given
        Duration timeRange = Duration.ofHours(1);
        long now = System.currentTimeMillis();
        ResourceManagerStatistics mockStatistics = ResourceManagerStatistics.empty(now - timeRange.toMillis(), now);
        when(metricsService.getStatistics(any(MetricKey.class), eq(ResourceManagerStatistics.class), eq(timeRange)))
                .thenReturn(mockStatistics);

        // When
        ResourceManagerStatistics result = resourceManager.getResourceManagerStatistics(timeRange);

        // Then
        assertNotNull(result);
        assertEquals("stable", result.trendDirection());
        verify(metricsService).getStatistics(any(MetricKey.class), eq(ResourceManagerStatistics.class), eq(timeRange));
    }

    @Test
    void testGetResourceManagerStatisticsWithoutMetricsService() {
        // Given
        resourceManager.unsetMetricsService(metricsService);
        Duration timeRange = Duration.ofHours(1);

        // When
        ResourceManagerStatistics result = resourceManager.getResourceManagerStatistics(timeRange);

        // Then
        assertNotNull(result);
        assertEquals("stable", result.trendDirection());
        assertEquals(0.0, result.trendPercentage());
    }

    @Test
    void testCanAcceptRequestLogic() {
        // Given
        resourceManager.setMaxConcurrentRequests(10);
        ModelProviderType provider = ModelProviderType.OPENAI;

        // When
        boolean canAccept = resourceManager.canAcceptRequest(provider);

        // Then
        assertTrue(canAccept);
    }

    @Test
    void testCannotAcceptRequestWhenLimitExceeded() {
        // Given
        resourceManager.setMaxConcurrentRequests(0);
        ModelProviderType provider = ModelProviderType.OPENAI;

        // When
        boolean canAccept = resourceManager.canAcceptRequest(provider);

        // Then
        assertFalse(canAccept);
    }

    @Test
    void testMetricsServiceErrorHandling() {
        // Given
        doThrow(new RuntimeException("Metrics service error")).when(metricsService).recordOperationWithData(anyString(),
                anyString(), anyBoolean(), any(), any());

        String requestId = "test-request-error";
        ModelProviderType provider = ModelProviderType.OPENAI;

        // When/Then - should not throw despite metrics service error
        assertDoesNotThrow(() -> {
            CompletableFuture<String> future = resourceManager.submitRequest(requestId, provider, 1, () -> "success");
            future.join();
        });
    }

    @Test
    void testResourceUsageStatisticsIntegration() {
        // Given
        ResourceManagerSnapshot mockSnapshot = ResourceManagerSnapshot.createWithSystemResources(5L, 4L, 1L, 0L, 0L,
                2000000000L, 3, 10);
        when(metricsService.getSnapshot(any(MetricKey.class), eq(ResourceManagerSnapshot.class)))
                .thenReturn(mockSnapshot);

        // When
        ResourceUsageStatistics stats = resourceManager.getResourceUsageStatistics();

        // Then
        assertNotNull(stats);
        assertEquals(5L, stats.getTotalRequestsProcessed());
        assertEquals(4L, stats.getCurrentConcurrentRequests());
    }

    @Test
    void testPerformCleanupWithHighMemoryUsage() {
        // Given
        resourceManager.setMaxMemoryUsage(1L); // Very low limit to trigger GC suggestion

        // When/Then - should not throw
        assertDoesNotThrow(() -> resourceManager.performCleanup());
    }
}
