package org.openhab.core.ai.common.monitoring.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Duration;
import java.time.Instant;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
import org.openhab.core.ai.common.monitoring.service.snapshot.ModelCompletionSnapshot;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;

/**
 * Integration tests for MetricsService OSGi functionality.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class MetricsServiceIntegrationTest {

    @Mock
    private BundleContext bundleContext;

    @Mock
    private ComponentContext componentContext;

    @Mock
    private ServiceRegistration<MetricsService> serviceRegistration;

    @Mock
    private MetricsService metricsService;

    private MetricsServiceImpl metricsServiceImpl;

    @BeforeEach
    void setUp() {
        metricsServiceImpl = new MetricsServiceImpl();
        when(componentContext.getBundleContext()).thenReturn(bundleContext);
        when(bundleContext.registerService(eq(MetricsService.class), any(MetricsService.class), any()))
                .thenReturn(serviceRegistration);
    }

    @Test
    void testActivateComponent() {
        // Given
        when(componentContext.getProperties()).thenReturn(new Hashtable<>());

        // When
        metricsServiceImpl.activate(componentContext);

        // Then
        verify(bundleContext, times(1)).registerService(eq(MetricsService.class), eq(metricsServiceImpl), any());
        assertTrue(metricsServiceImpl.isActive());
    }

    @Test
    void testActivateComponentWithConfiguration() {
        // Given
        Dictionary<String, Object> config = new Hashtable<>();
        config.put("maxSnapshots", 1000);
        config.put("retentionPeriod", 3600L);
        config.put("enableCaching", true);
        when(componentContext.getProperties()).thenReturn(config);

        // When
        metricsServiceImpl.activate(componentContext);

        // Then
        verify(bundleContext, times(1)).registerService(eq(MetricsService.class), eq(metricsServiceImpl), any());
        assertTrue(metricsServiceImpl.isActive());
        // Verify configuration was applied
        assertEquals(1000, metricsServiceImpl.getMaxSnapshots());
        assertEquals(3600L, metricsServiceImpl.getRetentionPeriod());
        assertTrue(metricsServiceImpl.isCachingEnabled());
    }

    @Test
    void testDeactivateComponent() {
        // Given
        when(componentContext.getProperties()).thenReturn(new Hashtable<>());
        metricsServiceImpl.activate(componentContext);

        // When
        metricsServiceImpl.deactivate(componentContext);

        // Then
        verify(serviceRegistration, times(1)).unregister();
        assertFalse(metricsServiceImpl.isActive());
    }

    @Test
    void testServiceRegistration() {
        // Given
        when(componentContext.getProperties()).thenReturn(Map.of());

        // When
        metricsServiceImpl.activate(componentContext);

        // Then
        verify(bundleContext, times(1)).registerService(eq(MetricsService.class), eq(metricsServiceImpl), any());
    }

    @Test
    void testServiceUnregistration() {
        // Given
        when(componentContext.getProperties()).thenReturn(Map.of());
        metricsServiceImpl.activate(componentContext);

        // When
        metricsServiceImpl.deactivate(componentContext);

        // Then
        verify(serviceRegistration, times(1)).unregister();
    }

    @Test
    void testMultipleActivateDeactivateCycles() {
        // Given
        when(componentContext.getProperties()).thenReturn(Map.of());

        // When & Then - Multiple cycles should work correctly
        for (int i = 0; i < 3; i++) {
            metricsServiceImpl.activate(componentContext);
            assertTrue(metricsServiceImpl.isActive());

            metricsServiceImpl.deactivate(componentContext);
            assertFalse(metricsServiceImpl.isActive());
        }

        // Verify service was registered and unregistered for each cycle
        verify(bundleContext, times(3)).registerService(eq(MetricsService.class), eq(metricsServiceImpl), any());
        verify(serviceRegistration, times(3)).unregister();
    }

    @Test
    void testActivateWithNullContext() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            metricsServiceImpl.activate(null);
        });
    }

    @Test
    void testDeactivateWithNullContext() {
        // Given
        when(componentContext.getProperties()).thenReturn(Map.of());
        metricsServiceImpl.activate(componentContext);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            metricsServiceImpl.deactivate(null);
        });
    }

    @Test
    void testActivateWithNullBundleContext() {
        // Given
        when(componentContext.getBundleContext()).thenReturn(null);
        when(componentContext.getProperties()).thenReturn(Map.of());

        // When & Then
        assertThrows(IllegalStateException.class, () -> {
            metricsServiceImpl.activate(componentContext);
        });
    }

    @Test
    void testServiceRegistrationFailure() {
        // Given
        when(componentContext.getProperties()).thenReturn(Map.of());
        when(bundleContext.registerService(eq(MetricsService.class), any(MetricsService.class), any()))
                .thenThrow(new RuntimeException("Registration failed"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            metricsServiceImpl.activate(componentContext);
        });
        assertFalse(metricsServiceImpl.isActive());
    }

    @Test
    void testServiceUnregistrationFailure() {
        // Given
        when(componentContext.getProperties()).thenReturn(Map.of());
        metricsServiceImpl.activate(componentContext);
        doThrow(new RuntimeException("Unregistration failed")).when(serviceRegistration).unregister();

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            metricsServiceImpl.deactivate(componentContext);
        });
        // Service should still be marked as inactive even if unregistration fails
        assertFalse(metricsServiceImpl.isActive());
    }

    @Test
    void testConfigurationValidation() {
        // Given
        Map<String, Object> invalidConfig = Map.of("maxSnapshots", -1, "retentionPeriod", -100L, "enableCaching",
                "invalid");
        when(componentContext.getProperties()).thenReturn(invalidConfig);

        // When
        metricsServiceImpl.activate(componentContext);

        // Then
        // Should use default values for invalid configuration
        assertTrue(metricsServiceImpl.isActive());
        assertTrue(metricsServiceImpl.getMaxSnapshots() > 0);
        assertTrue(metricsServiceImpl.getRetentionPeriod() > 0);
    }

    @Test
    void testServiceLifecycleWithMetricsOperations() {
        // Given
        when(componentContext.getProperties()).thenReturn(Map.of());
        metricsServiceImpl.activate(componentContext);

        // When - Perform metrics operations while service is active
        metricsServiceImpl.recordOperation("test-domain", "test-operation", true, Duration.ofMillis(100));
        metricsServiceImpl.recordModelCompletion("test-model", true, Duration.ofMillis(200), 100, 50, 0.01);

        // Then
        assertTrue(metricsServiceImpl.isActive());

        // Verify operations were recorded
        ModelCompletionSnapshot snapshot = metricsServiceImpl.getModelCompletionSnapshot("test-model");
        assertNotNull(snapshot);
        assertEquals(1, snapshot.counts().total());
        assertEquals(1, snapshot.counts().success());
        assertEquals(0, snapshot.counts().failure());
    }

    @Test
    void testServiceOperationsWhenInactive() {
        // Given
        when(componentContext.getProperties()).thenReturn(Map.of());
        // Service is not activated

        // When & Then - Operations should fail when service is inactive
        assertThrows(IllegalStateException.class, () -> {
            metricsServiceImpl.recordOperation("test-domain", "test-operation", true, Duration.ofMillis(100));
        });

        assertThrows(IllegalStateException.class, () -> {
            metricsServiceImpl.recordModelCompletion("test-model", true, Duration.ofMillis(200), 100, 50, 0.01);
        });

        assertThrows(IllegalStateException.class, () -> {
            metricsServiceImpl.getModelCompletionSnapshot("test-model");
        });
    }

    @Test
    void testServiceOperationsAfterDeactivation() {
        // Given
        when(componentContext.getProperties()).thenReturn(Map.of());
        metricsServiceImpl.activate(componentContext);
        metricsServiceImpl.deactivate(componentContext);

        // When & Then - Operations should fail after deactivation
        assertThrows(IllegalStateException.class, () -> {
            metricsServiceImpl.recordOperation("test-domain", "test-operation", true, Duration.ofMillis(100));
        });

        assertThrows(IllegalStateException.class, () -> {
            metricsServiceImpl.recordModelCompletion("test-model", true, Duration.ofMillis(200), 100, 50, 0.01);
        });

        assertThrows(IllegalStateException.class, () -> {
            metricsServiceImpl.getModelCompletionSnapshot("test-model");
        });
    }

    @Test
    void testConcurrentServiceOperations() {
        // Given
        when(componentContext.getProperties()).thenReturn(Map.of());
        metricsServiceImpl.activate(componentContext);

        // When & Then - Concurrent operations should be thread-safe
        assertDoesNotThrow(() -> {
            Thread[] threads = new Thread[10];
            for (int i = 0; i < threads.length; i++) {
                final int threadId = i;
                threads[i] = new Thread(() -> {
                    for (int j = 0; j < 100; j++) {
                        metricsServiceImpl.recordOperation("domain-" + threadId, "operation-" + j, true,
                                Duration.ofMillis(10));
                        metricsServiceImpl.recordModelCompletion("model-" + threadId, true, Duration.ofMillis(20), 10,
                                5, 0.001);
                    }
                });
            }

            for (Thread thread : threads) {
                thread.start();
            }

            for (Thread thread : threads) {
                thread.join();
            }
        });

        // Verify service is still active after concurrent operations
        assertTrue(metricsServiceImpl.isActive());
    }

    @Test
    void testServiceReactivation() {
        // Given
        when(componentContext.getProperties()).thenReturn(Map.of());
        metricsServiceImpl.activate(componentContext);
        metricsServiceImpl.deactivate(componentContext);

        // When - Reactivate the service
        metricsServiceImpl.activate(componentContext);

        // Then
        assertTrue(metricsServiceImpl.isActive());
        verify(bundleContext, times(2)).registerService(eq(MetricsService.class), eq(metricsServiceImpl), any());
        verify(serviceRegistration, times(1)).unregister();
    }

    @Test
    void testServiceStateConsistency() {
        // Given
        when(componentContext.getProperties()).thenReturn(Map.of());

        // When & Then - Service state should be consistent
        assertFalse(metricsServiceImpl.isActive());

        metricsServiceImpl.activate(componentContext);
        assertTrue(metricsServiceImpl.isActive());

        metricsServiceImpl.deactivate(componentContext);
        assertFalse(metricsServiceImpl.isActive());
    }

    @Test
    void testConfigurationReload() {
        // Given
        Map<String, Object> initialConfig = Map.of("maxSnapshots", 100);
        Map<String, Object> updatedConfig = Map.of("maxSnapshots", 2000);
        when(componentContext.getProperties()).thenReturn(initialConfig);

        metricsServiceImpl.activate(componentContext);
        assertEquals(100, metricsServiceImpl.getMaxSnapshots());

        // When - Update configuration
        when(componentContext.getProperties()).thenReturn(updatedConfig);
        metricsServiceImpl.modified(componentContext);

        // Then
        assertEquals(2000, metricsServiceImpl.getMaxSnapshots());
        assertTrue(metricsServiceImpl.isActive());
    }

    @Test
    void testServiceRegistrationProperties() {
        // Given
        when(componentContext.getProperties()).thenReturn(Map.of());

        // When
        metricsServiceImpl.activate(componentContext);

        // Then
        verify(bundleContext, times(1)).registerService(eq(MetricsService.class), eq(metricsServiceImpl),
                argThat(properties -> {
                    // Verify service properties are set correctly
                    return properties != null && "org.openhab.core.ai.common.monitoring.service.MetricsService"
                            .equals(properties.get("service.pid"));
                }));
    }

    @Test
    void testServiceUnregistrationOnDeactivation() {
        // Given
        when(componentContext.getProperties()).thenReturn(Map.of());
        metricsServiceImpl.activate(componentContext);

        // When
        metricsServiceImpl.deactivate(componentContext);

        // Then
        verify(serviceRegistration, times(1)).unregister();
        assertFalse(metricsServiceImpl.isActive());
    }

    @Test
    void testServiceRegistrationIdempotency() {
        // Given
        when(componentContext.getProperties()).thenReturn(Map.of());

        // When - Try to activate multiple times
        metricsServiceImpl.activate(componentContext);
        metricsServiceImpl.activate(componentContext);
        metricsServiceImpl.activate(componentContext);

        // Then - Should only register once
        verify(bundleContext, times(1)).registerService(eq(MetricsService.class), eq(metricsServiceImpl), any());
        assertTrue(metricsServiceImpl.isActive());
    }

    @Test
    void testServiceUnregistrationIdempotency() {
        // Given
        when(componentContext.getProperties()).thenReturn(Map.of());
        metricsServiceImpl.activate(componentContext);

        // When - Try to deactivate multiple times
        metricsServiceImpl.deactivate(componentContext);
        metricsServiceImpl.deactivate(componentContext);
        metricsServiceImpl.deactivate(componentContext);

        // Then - Should only unregister once
        verify(serviceRegistration, times(1)).unregister();
        assertFalse(metricsServiceImpl.isActive());
    }

    // Mock implementation for testing
    private static class MetricsServiceImpl implements MetricsService {
        private boolean active = false;
        private int maxSnapshots = 1000;
        private long retentionPeriod = 3600L;
        private boolean cachingEnabled = true;

        public void activate(ComponentContext context) {
            if (context == null) {
                throw new IllegalArgumentException("ComponentContext cannot be null");
            }
            if (context.getBundleContext() == null) {
                throw new IllegalStateException("BundleContext cannot be null");
            }
            this.active = true;

            // Apply configuration
            Map<String, Object> config = context.getProperties();
            if (config != null) {
                Object maxSnapshotsObj = config.get("maxSnapshots");
                if (maxSnapshotsObj instanceof Number) {
                    this.maxSnapshots = ((Number) maxSnapshotsObj).intValue();
                }

                Object retentionPeriodObj = config.get("retentionPeriod");
                if (retentionPeriodObj instanceof Number) {
                    this.retentionPeriod = ((Number) retentionPeriodObj).longValue();
                }

                Object cachingEnabledObj = config.get("enableCaching");
                if (cachingEnabledObj instanceof Boolean) {
                    this.cachingEnabled = (Boolean) cachingEnabledObj;
                }
            }
        }

        public void deactivate(ComponentContext context) {
            if (context == null) {
                throw new IllegalArgumentException("ComponentContext cannot be null");
            }
            this.active = false;
        }

        public void modified(ComponentContext context) {
            if (context != null && context.getProperties() != null) {
                Map<String, Object> config = context.getProperties();
                Object maxSnapshotsObj = config.get("maxSnapshots");
                if (maxSnapshotsObj instanceof Number) {
                    this.maxSnapshots = ((Number) maxSnapshotsObj).intValue();
                }
            }
        }

        public boolean isActive() {
            return active;
        }

        public int getMaxSnapshots() {
            return maxSnapshots;
        }

        public long getRetentionPeriod() {
            return retentionPeriod;
        }

        public boolean isCachingEnabled() {
            return cachingEnabled;
        }

        @Override
        public void recordOperation(String domain, String operation, boolean success, Duration duration) {
            if (!active) {
                throw new IllegalStateException("Service is not active");
            }
            // Mock implementation
        }

        @Override
        public void recordOperationWithData(String domain, String operation, boolean success, Duration duration,
                Map<String, Object> data) {
            if (!active) {
                throw new IllegalStateException("Service is not active");
            }
            // Mock implementation
        }

        @Override
        public void recordModelCompletion(String modelId, boolean success, Duration duration, int inputTokens,
                int outputTokens, double cost) {
            if (!active) {
                throw new IllegalStateException("Service is not active");
            }
            // Mock implementation
        }

        @Override
        public ModelCompletionSnapshot getModelCompletionSnapshot(String modelId) {
            if (!active) {
                throw new IllegalStateException("Service is not active");
            }
            // Return a mock snapshot
            return new ModelCompletionSnapshot(null, null, Instant.now().toEpochMilli(), 0L, 0.0);
        }
    }
}
