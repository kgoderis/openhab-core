package org.openhab.core.ai.tool.monitoring;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.withSettings;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.ai.common.monitoring.api.HealthMetrics;
import org.openhab.core.ai.common.monitoring.api.MetricKey;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
import org.openhab.core.ai.common.monitoring.snapshot.SystemHealthMonitorSnapshot;
import org.openhab.core.ai.model.api.ModelProviderType;

/**
 * Unit tests for DefaultSystemHealthMonitor class.
 * 
 * @author Karel Goderis - Initial Contribution
 */
@ExtendWith(MockitoExtension.class)
@NonNullByDefault
public class DefaultSystemHealthMonitorTest {

    @Mock
    private MetricsService metricsService;

    @Mock
    private SystemHealthMonitorSnapshot mockSystemSnapshot;

    @Mock
    private HealthMetrics mockHealthMetrics;

    // Create a mock that implements both interfaces
    private HealthMetrics mockHealthMetricsSnapshot;

    private DefaultSystemHealthMonitor healthMonitor;

    @BeforeEach
    void setUp() {
        healthMonitor = new DefaultSystemHealthMonitor();

        // Create a mock that implements both MetricsSnapshot and HealthMetrics
        mockHealthMetricsSnapshot = mock(HealthMetrics.class,
                withSettings().extraInterfaces(org.openhab.core.ai.common.monitoring.api.MetricsSnapshot.class));

        // Use reflection to set the MetricsService since @Reference is used
        try {
            var field = DefaultSystemHealthMonitor.class.getDeclaredField("metricsService");
            field.setAccessible(true);
            field.set(healthMonitor, metricsService);
        } catch (Exception e) {
            fail("Failed to inject MetricsService: " + e.getMessage());
        }
    }

    @Test
    void testRecordSuccess() {
        // Given
        ModelProviderType provider = ModelProviderType.OPENAI;
        long responseTime = 500L;

        // When
        healthMonitor.recordSuccess(provider, responseTime);

        // Then
        assertTrue(healthMonitor.isProviderHealthy(provider));
    }

    @Test
    void testRecordFailure() {
        // Given
        ModelProviderType provider = ModelProviderType.OPENAI;
        Exception error = new RuntimeException("Test error");

        // When
        healthMonitor.recordFailure(provider, error);

        // Then
        // Provider should still be considered healthy after one failure
        assertTrue(healthMonitor.isProviderHealthy(provider));
    }

    @Test
    void testRecordServiceSuccess() {
        // Given
        String serviceName = "test-service";
        long responseTime = 300L;

        // When
        healthMonitor.recordServiceSuccess(serviceName, responseTime);

        // Then
        assertTrue(healthMonitor.isServiceHealthy(serviceName));
    }

    @Test
    void testRecordServiceFailure() {
        // Given
        String serviceName = "test-service";
        Exception error = new RuntimeException("Service error");

        // When
        healthMonitor.recordServiceFailure(serviceName, error);

        // Then
        // Service should still be considered healthy after one failure
        assertTrue(healthMonitor.isServiceHealthy(serviceName));
    }

    @Test
    void testPerformHealthCheckWithMetricsService() {
        // Given
        ModelProviderType provider = ModelProviderType.OPENAI;

        // When
        CompletableFuture<HealthCheckResult> result = healthMonitor.performHealthCheck(provider);

        // Then
        assertNotNull(result);

        // Wait for completion and verify
        HealthCheckResult healthCheckResult = result.join();
        assertNotNull(healthCheckResult);
        assertEquals(provider.name(), healthCheckResult.getTarget());

        // Verify MetricsService was called for recording
        verify(metricsService, atLeastOnce()).recordOperation(eq("system-health-monitor"), eq("provider-health-check"),
                anyBoolean(), any(Duration.class));
    }

    @Test
    void testPerformServiceHealthCheck() {
        // Given
        String serviceName = "test-service";

        // When
        CompletableFuture<HealthCheckResult> result = healthMonitor.performServiceHealthCheck(serviceName);

        // Then
        assertNotNull(result);

        // Wait for completion and verify
        HealthCheckResult healthCheckResult = result.join();
        assertNotNull(healthCheckResult);
        assertEquals(serviceName, healthCheckResult.getTarget());

        // Verify MetricsService was called for recording
        verify(metricsService, atLeastOnce()).recordOperation(eq("system-health-monitor"), eq("service-health-check"),
                anyBoolean(), any(Duration.class));
    }

    @Test
    void testGetProviderHealthMetricsWithMetricsService() {
        // Given
        ModelProviderType provider = ModelProviderType.ANTHROPIC;
        when(metricsService.getSnapshot(any(MetricKey.class)))
                .thenReturn((org.openhab.core.ai.common.monitoring.api.MetricsSnapshot) mockHealthMetricsSnapshot);

        // When
        HealthMetrics result = healthMonitor.getProviderHealthMetrics(provider);

        // Then
        assertNotNull(result);
        verify(metricsService).recordOperation(eq("system-health-monitor"), eq("provider-health-check"), anyBoolean(),
                any(Duration.class));
        verify(metricsService).getSnapshot(any(MetricKey.class));
    }

    @Test
    void testGetServiceHealthMetricsWithMetricsService() {
        // Given
        String serviceName = "metrics-service";
        when(metricsService.getSnapshot(any(MetricKey.class)))
                .thenReturn((org.openhab.core.ai.common.monitoring.api.MetricsSnapshot) mockHealthMetricsSnapshot);

        // When
        HealthMetrics result = healthMonitor.getServiceHealthMetrics(serviceName);

        // Then
        assertNotNull(result);
        verify(metricsService).recordOperation(eq("system-health-monitor"), eq("service-health-check"), anyBoolean(),
                any(Duration.class));
        verify(metricsService).getSnapshot(any(MetricKey.class));
    }

    @Test
    void testGetSystemHealthStatus() {
        // Given
        ModelProviderType provider = ModelProviderType.GOOGLE;
        String serviceName = "test-service";

        // Initialize some state
        healthMonitor.recordSuccess(provider, 200L);
        healthMonitor.recordServiceSuccess(serviceName, 150L);

        // When
        SystemHealthStatus status = healthMonitor.getSystemHealthStatus();

        // Then
        assertNotNull(status);
        assertNotNull(status.getProviderMetrics());
        assertNotNull(status.getServiceMetrics());
    }

    @Test
    void testConfigurationMethodsWithMetricsService() {
        // Test setFailureThreshold
        healthMonitor.setFailureThreshold(10);
        assertEquals(10, healthMonitor.getFailureThreshold());

        // Test setRecoveryTimeout
        Duration timeout = Duration.ofMinutes(10);
        healthMonitor.setRecoveryTimeout(timeout);
        assertEquals(timeout, healthMonitor.getRecoveryTimeout());

        // Test setHealthCheckInterval
        Duration interval = Duration.ofSeconds(60);
        healthMonitor.setHealthCheckInterval(interval);
        assertEquals(interval, healthMonitor.getHealthCheckInterval());

        // Test setMaxResponseTime
        healthMonitor.setMaxResponseTime(10000L);
        assertEquals(10000L, healthMonitor.getMaxResponseTime());

        // Test setMinSuccessRate
        healthMonitor.setMinSuccessRate(0.9);
        assertEquals(0.9, healthMonitor.getMinSuccessRate(), 0.001);

        // Test setMonitoringEnabled
        healthMonitor.setMonitoringEnabled(false);
        assertFalse(healthMonitor.isMonitoringEnabled());

        // Test setAutoRecoveryEnabled
        healthMonitor.setAutoRecoveryEnabled(false);
        assertFalse(healthMonitor.isAutoRecoveryEnabled());

        // Verify all configuration changes were recorded
        verify(metricsService, times(7)).recordOperation(eq("system-health-monitor"), eq("configuration"), eq(true),
                any(Duration.class));
    }

    @Test
    void testMetricsServiceUnavailableGracefulDegradation() {
        // Given - create a new instance without MetricsService
        DefaultSystemHealthMonitor healthMonitorWithoutMetrics = new DefaultSystemHealthMonitor();

        // When - try to use methods that depend on MetricsService
        healthMonitorWithoutMetrics.setFailureThreshold(5);
        healthMonitorWithoutMetrics.recordSuccess(ModelProviderType.OPENAI, 100L);
        HealthMetrics metrics = healthMonitorWithoutMetrics.getProviderHealthMetrics(ModelProviderType.OPENAI);

        // Then - should not throw exceptions and provide default behavior
        assertEquals(5, healthMonitorWithoutMetrics.getFailureThreshold());
        assertTrue(healthMonitorWithoutMetrics.isProviderHealthy(ModelProviderType.OPENAI));
        assertNotNull(metrics);
    }

    @Test
    void testForceProviderRecovery() {
        // Given
        ModelProviderType provider = ModelProviderType.AZURE;

        // Record some failures to put provider in unhealthy state
        for (int i = 0; i < 6; i++) {
            healthMonitor.recordFailure(provider, new RuntimeException("Test failure " + i));
        }

        // When
        healthMonitor.forceProviderRecovery(provider);

        // Then - provider should be recovered (actual recovery logic is in ProviderHealthState)
        // We just verify the method doesn't throw an exception
        assertDoesNotThrow(() -> healthMonitor.forceProviderRecovery(provider));
    }

    @Test
    void testForceServiceRecovery() {
        // Given
        String serviceName = "unhealthy-service";

        // Record some failures
        for (int i = 0; i < 3; i++) {
            healthMonitor.recordServiceFailure(serviceName, new RuntimeException("Service failure " + i));
        }

        // When
        healthMonitor.forceServiceRecovery(serviceName);

        // Then - service should be recovered
        assertDoesNotThrow(() -> healthMonitor.forceServiceRecovery(serviceName));
    }

    @Test
    void testResetProviderHealth() {
        // Given
        ModelProviderType provider = ModelProviderType.GOOGLE;
        healthMonitor.recordSuccess(provider, 100L);
        assertTrue(healthMonitor.isProviderHealthy(provider));

        // When
        healthMonitor.resetProviderHealth(provider);

        // Then - provider should still be considered healthy (new state created)
        assertTrue(healthMonitor.isProviderHealthy(provider));
    }

    @Test
    void testResetServiceHealth() {
        // Given
        String serviceName = "reset-service";
        healthMonitor.recordServiceSuccess(serviceName, 100L);
        assertTrue(healthMonitor.isServiceHealthy(serviceName));

        // When
        healthMonitor.resetServiceHealth(serviceName);

        // Then - service should still be considered healthy (new state created)
        assertTrue(healthMonitor.isServiceHealthy(serviceName));
    }

    @Test
    void testSpecificationPerformanceMonitoring() {
        // Given
        String specificationId = "test-spec";

        // When
        healthMonitor.recordSpecificationExecution(specificationId, 1000L, true);
        healthMonitor.recordSpecificationExecution(specificationId, 1500L, false);

        // Then
        SpecificationPerformanceMetrics metrics = healthMonitor.getSpecificationMetrics(specificationId);
        assertNotNull(metrics);
        assertEquals(specificationId, metrics.specificationId());
        assertEquals(2L, metrics.totalRequests());
        assertEquals(1L, metrics.successfulRequests());
        assertEquals(1L, metrics.failedRequests());
        assertEquals(2500L, metrics.totalResponseTime());
        assertEquals(1250.0, metrics.averageResponseTime(), 0.1);
        assertEquals(0.5, metrics.successRate(), 0.1);

        // Test getAllSpecificationMetrics
        var allMetrics = healthMonitor.getAllSpecificationMetrics();
        assertTrue(allMetrics.containsKey(specificationId));

        // Test getSpecificationAlerts and getSpecificationOptimizations
        var alerts = healthMonitor.getSpecificationAlerts(specificationId);
        var optimizations = healthMonitor.getSpecificationOptimizations(specificationId);
        assertNotNull(alerts);
        assertNotNull(optimizations);

        // Test getSpecificationPerformanceReport
        var report = healthMonitor.getSpecificationPerformanceReport();
        assertNotNull(report);
        assertFalse(report.metrics().isEmpty());
    }
}
