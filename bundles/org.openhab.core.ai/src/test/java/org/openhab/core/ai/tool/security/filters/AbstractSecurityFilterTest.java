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
package org.openhab.core.ai.tool.security.filters;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doNothing;

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

/**
 * Unit tests for AbstractSecurityFilter MetricsService integration
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
@ExtendWith(MockitoExtension.class)
class AbstractSecurityFilterTest {

    @Mock
    private MetricsService metricsService;

    @Mock
    private OperationRecorder operationRecorder;

    @Mock
    private ExecutionMetricsSnapshot authSnapshot;

    @Mock
    private ExecutionMetricsSnapshot cacheHitSnapshot;

    @Mock
    private ExecutionMetricsSnapshot cacheMissSnapshot;

    private TestSecurityFilter securityFilter;

    @BeforeEach
    void setUp() {
        securityFilter = new TestSecurityFilter("test-filter", "Test Filter", 1);
        // Use reflection to inject the mocked MetricsService
        try {
            java.lang.reflect.Field metricsServiceField = AbstractSecurityFilter.class
                    .getDeclaredField("metricsService");
            metricsServiceField.setAccessible(true);
            metricsServiceField.set(securityFilter, metricsService);
        } catch (Exception e) {
            fail("Failed to inject MetricsService mock: " + e.getMessage());
        }
    }

    @Test
    void testAuthenticateRecordsMetrics() {
        // Given
        Map<String, Object> request = Map.of("username", "testuser", "password", "testpass");
        when(metricsService.recordOperation("security-filter", "authentication")).thenReturn(operationRecorder);
        when(operationRecorder.withSuccess(anyBoolean())).thenReturn(operationRecorder);
        when(operationRecorder.withDuration(anyLong())).thenReturn(operationRecorder);
        when(operationRecorder.withData(anyString(), any())).thenReturn(operationRecorder);
        doNothing().when(operationRecorder).record();

        // When
        securityFilter.authenticate(request);

        // Then
        verify(metricsService).recordOperation("security-filter", "authentication");
        verify(operationRecorder).withSuccess(true);
        verify(operationRecorder).withDuration(anyLong());
        verify(operationRecorder).withData("filterId", "test-filter");
        verify(operationRecorder).withData("filterName", "Test Filter");
        verify(operationRecorder).record();
    }

    @Test
    void testAuthenticateWithFailureRecordsMetrics() {
        // Given
        Map<String, Object> request = Map.of("username", "testuser", "password", "wrongpass");
        securityFilter.setShouldFail(true);
        when(metricsService.recordOperation("security-filter", "authentication")).thenReturn(operationRecorder);
        when(operationRecorder.withSuccess(anyBoolean())).thenReturn(operationRecorder);
        when(operationRecorder.withDuration(anyLong())).thenReturn(operationRecorder);
        when(operationRecorder.withData(anyString(), any())).thenReturn(operationRecorder);
        doNothing().when(operationRecorder).record();

        // When
        SecurityResult result = securityFilter.authenticate(request);

        // Then
        assertFalse(result.isSuccess());
        verify(metricsService).recordOperation("security-filter", "authentication");
        verify(operationRecorder).withSuccess(false);
        verify(operationRecorder).record();
    }

    @Test
    void testCacheHitRecordsMetrics() {
        // Given
        Map<String, Object> request = Map.of("username", "testuser", "password", "testpass");
        when(metricsService.recordOperation("security-filter", "cache-hit")).thenReturn(operationRecorder);
        when(metricsService.recordOperation("security-filter", "authentication")).thenReturn(operationRecorder);
        when(operationRecorder.withSuccess(anyBoolean())).thenReturn(operationRecorder);
        when(operationRecorder.withDuration(anyLong())).thenReturn(operationRecorder);
        when(operationRecorder.withData(anyString(), any())).thenReturn(operationRecorder);
        doNothing().when(operationRecorder).record();

        // When - first call to populate cache
        securityFilter.authenticate(request);
        // Second call should hit cache
        securityFilter.authenticate(request);

        // Then
        verify(metricsService).recordOperation("security-filter", "cache-hit");
        verify(operationRecorder, atLeast(1)).withSuccess(true);
        verify(operationRecorder, atLeast(1)).withDuration(0L);
        verify(operationRecorder, atLeast(1)).record();
    }

    @Test
    void testCacheMissRecordsMetrics() {
        // Given
        Map<String, Object> request = Map.of("username", "testuser", "password", "testpass");
        when(metricsService.recordOperation("security-filter", "cache-miss")).thenReturn(operationRecorder);
        when(metricsService.recordOperation("security-filter", "authentication")).thenReturn(operationRecorder);
        when(operationRecorder.withSuccess(anyBoolean())).thenReturn(operationRecorder);
        when(operationRecorder.withDuration(anyLong())).thenReturn(operationRecorder);
        when(operationRecorder.withData(anyString(), any())).thenReturn(operationRecorder);
        doNothing().when(operationRecorder).record();

        // When
        securityFilter.authenticate(request);

        // Then
        verify(metricsService).recordOperation("security-filter", "cache-miss");
        verify(operationRecorder, atLeast(1)).withSuccess(true);
        verify(operationRecorder, atLeast(1)).record();
    }

    @Test
    void testGetMetricsWithMetricsService() {
        // Given
        when(authSnapshot.total()).thenReturn(100L);
        when(authSnapshot.success()).thenReturn(80L);
        when(authSnapshot.failure()).thenReturn(20L);
        when(cacheHitSnapshot.total()).thenReturn(30L);
        when(cacheMissSnapshot.total()).thenReturn(70L);

        when(metricsService.getSnapshot(any(MetricKey.class), eq(ExecutionMetricsSnapshot.class)))
                .thenReturn(authSnapshot).thenReturn(cacheHitSnapshot).thenReturn(cacheMissSnapshot);

        // When
        AuthMetrics metrics = securityFilter.getMetrics();

        // Then
        assertNotNull(metrics);
        assertEquals(100L, metrics.totalRequests);
        assertEquals(80L, metrics.successfulAuthentications);
        assertEquals(20L, metrics.failedAuthentications);
        assertEquals(30L, metrics.cacheHits);
        assertEquals(70L, metrics.cacheMisses);
        assertEquals(0, metrics.cacheSize); // No items in cache yet

        verify(metricsService, times(3)).getSnapshot(any(MetricKey.class), eq(ExecutionMetricsSnapshot.class));
    }

    @Test
    void testGetMetricsWithNullMetricsService() {
        // Given - no MetricsService injected
        TestSecurityFilter filterWithoutMetrics = new TestSecurityFilter("test-filter", "Test Filter", 1);

        // When
        AuthMetrics metrics = filterWithoutMetrics.getMetrics();

        // Then
        assertNotNull(metrics);
        assertEquals(0L, metrics.totalRequests);
        assertEquals(0L, metrics.successfulAuthentications);
        assertEquals(0L, metrics.failedAuthentications);
        assertEquals(0L, metrics.cacheHits);
        assertEquals(0L, metrics.cacheMisses);
        assertEquals(0, metrics.cacheSize);
    }

    @Test
    void testMetricsServiceErrorHandling() {
        // Given
        Map<String, Object> request = Map.of("username", "testuser", "password", "testpass");
        when(metricsService.recordOperation(anyString(), anyString())).thenThrow(new RuntimeException("Metrics error"));

        // When - should not throw exception despite metrics error
        SecurityResult result = securityFilter.authenticate(request);

        // Then
        assertTrue(result.isSuccess()); // Authentication should still succeed
        verify(metricsService).recordOperation("security-filter", "cache-miss");
        verify(metricsService).recordOperation("security-filter", "authentication");
    }

    @Test
    void testGetMetricsErrorHandling() {
        // Given
        when(metricsService.getSnapshot(any(MetricKey.class), eq(ExecutionMetricsSnapshot.class)))
                .thenThrow(new RuntimeException("Snapshot error"));

        // When - should not throw exception despite metrics error
        AuthMetrics metrics = securityFilter.getMetrics();

        // Then
        assertNotNull(metrics);
        assertEquals(0L, metrics.totalRequests); // Should return default values
        verify(metricsService, atLeast(1)).getSnapshot(any(MetricKey.class), eq(ExecutionMetricsSnapshot.class));
    }

    /**
     * Test implementation of AbstractSecurityFilter for testing purposes
     */
    private static class TestSecurityFilter extends AbstractSecurityFilter {
        private boolean shouldFail = false;

        public TestSecurityFilter(String filterId, String filterName, int priority) {
            super(filterId, filterName, priority);
        }

        public void setShouldFail(boolean shouldFail) {
            this.shouldFail = shouldFail;
        }

        @Override
        protected SecurityResult performAuthentication(Map<String, Object> request) {
            if (shouldFail) {
                return SecurityResult.failure("Authentication failed");
            }
            return SecurityResult.success("testuser", "Test User");
        }
    }
}
