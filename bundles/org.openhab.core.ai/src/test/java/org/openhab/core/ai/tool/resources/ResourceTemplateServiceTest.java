package org.openhab.core.ai.tool.resources;

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
import org.openhab.core.ai.common.monitoring.snapshot.ExecutionMetricsSnapshot;
import org.openhab.core.ai.tool.resources.api.ResourceContext;
import org.openhab.core.ai.tool.resources.api.ResourceResult;

/**
 * Unit tests for ResourceTemplateService MetricsService integration
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
@ExtendWith(MockitoExtension.class)
class ResourceTemplateServiceTest {

    @Mock
    private MetricsService metricsService;

    @Mock
    private ExecutionMetricsSnapshot mockSnapshot;

    private ResourceTemplateService resourceTemplateService;

    @BeforeEach
    void setUp() {
        resourceTemplateService = new ResourceTemplateService();
        // Use reflection to inject the mocked MetricsService
        try {
            java.lang.reflect.Field metricsServiceField = ResourceTemplateService.class
                    .getDeclaredField("metricsService");
            metricsServiceField.setAccessible(true);
            metricsServiceField.set(resourceTemplateService, metricsService);
        } catch (Exception e) {
            fail("Failed to inject MetricsService mock: " + e.getMessage());
        }
    }

    @Test
    void testListTemplatesRecordsMetrics() {
        // Given
        ResourceContext context = new ResourceContext();
        context.setProperty("requestId", "test-request-1");

        // When
        ResourceResult result = resourceTemplateService.listTemplates(context);

        // Then
        verify(metricsService, times(1)).recordOperation(eq("resource-template"), eq("list-templates"));
        assertNotNull(result);
        assertTrue(result.isSuccess());
    }

    @Test
    void testGetParameterCompletionsRecordsMetrics() {
        // Given
        ResourceContext context = new ResourceContext();
        context.setProperty("requestId", "test-request-2");

        // When
        ResourceResult result = resourceTemplateService.getParameterCompletions("test-template",
                Map.of("param1", "value1"), context);

        // Then
        verify(metricsService, times(1)).recordOperation(eq("resource-template"), eq("parameter-completions"));
        assertNotNull(result);
    }

    @Test
    void testGetPerformanceMetricsWithMetricsService() {
        // Given
        when(mockSnapshot.total()).thenReturn(100L);
        when(mockSnapshot.averageMs()).thenReturn(50.0);
        when(mockSnapshot.success()).thenReturn(95L);
        when(mockSnapshot.failure()).thenReturn(5L);

        when(metricsService.getSnapshot(any(MetricKey.class), eq(ExecutionMetricsSnapshot.class)))
                .thenReturn(mockSnapshot);

        // When
        Map<String, Object> metrics = resourceTemplateService.getPerformanceMetrics();

        // Then
        assertNotNull(metrics);
        assertEquals(100L, metrics.get("totalTemplateRequests"));
        assertEquals(50.0, metrics.get("averageTemplateTime"));
        assertEquals(95L, metrics.get("totalTemplateCompletions"));
        assertEquals(5L, metrics.get("totalValidations"));
        assertTrue((Integer) metrics.get("templateCount") > 0);

        verify(metricsService, times(3)).getSnapshot(any(MetricKey.class), eq(ExecutionMetricsSnapshot.class));
    }

    @Test
    void testGetPerformanceMetricsWithNullMetricsService() {
        // Given
        ResourceTemplateService serviceWithoutMetrics = new ResourceTemplateService();

        // When
        Map<String, Object> metrics = serviceWithoutMetrics.getPerformanceMetrics();

        // Then
        assertNotNull(metrics);
        assertEquals(0L, metrics.get("totalTemplateRequests"));
        assertEquals(0.0, metrics.get("averageTemplateTime"));
        assertEquals(0L, metrics.get("totalTemplateCompletions"));
        assertEquals(0L, metrics.get("totalValidations"));
        assertTrue((Integer) metrics.get("templateCount") > 0);
    }

    @Test
    void testGetPerformanceMetricsWithMetricsServiceException() {
        // Given
        when(metricsService.getSnapshot(any(MetricKey.class), eq(ExecutionMetricsSnapshot.class)))
                .thenThrow(new RuntimeException("Metrics service error"));

        // When
        Map<String, Object> metrics = resourceTemplateService.getPerformanceMetrics();

        // Then
        assertNotNull(metrics);
        assertEquals(0L, metrics.get("totalTemplateRequests"));
        assertEquals(0.0, metrics.get("averageTemplateTime"));
        assertEquals(0L, metrics.get("totalTemplateCompletions"));
        assertEquals(0L, metrics.get("totalValidations"));
        assertTrue((Integer) metrics.get("templateCount") > 0);
    }

    @Test
    void testGetPerformanceMetricsWithNullSnapshot() {
        // Given
        when(metricsService.getSnapshot(any(MetricKey.class), eq(ExecutionMetricsSnapshot.class))).thenReturn(null);

        // When
        Map<String, Object> metrics = resourceTemplateService.getPerformanceMetrics();

        // Then
        assertNotNull(metrics);
        assertEquals(0L, metrics.get("totalTemplateRequests"));
        assertEquals(0.0, metrics.get("averageTemplateTime"));
        assertEquals(0L, metrics.get("totalTemplateCompletions"));
        assertEquals(0L, metrics.get("totalValidations"));
        assertTrue((Integer) metrics.get("templateCount") > 0);
    }

    @Test
    void testMetricsServiceErrorHandling() {
        // Given
        when(metricsService.recordOperation(anyString(), anyString()))
                .thenThrow(new RuntimeException("Metrics recording error"));

        ResourceContext context = new ResourceContext();
        context.setProperty("requestId", "test-request-error");

        // When & Then - Should not throw exception
        assertDoesNotThrow(() -> {
            ResourceResult result = resourceTemplateService.listTemplates(context);
            assertNotNull(result);
        });
    }
}
