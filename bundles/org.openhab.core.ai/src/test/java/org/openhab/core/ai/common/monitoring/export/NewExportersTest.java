package org.openhab.core.ai.common.monitoring.export;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
import org.openhab.core.ai.common.monitoring.service.snapshot.DomainAggregatedSnapshot;
import org.openhab.core.ai.common.monitoring.service.snapshot.GenericMetricsSnapshot;

/**
 * Unit tests for the new Phase 4 exporters.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class NewExportersTest {

    @Mock
    private MetricsService mockMetricsService;

    @Mock
    private GenericMetricsSnapshot mockSnapshot;

    @Mock
    private DomainAggregatedSnapshot mockAggregatedSnapshot;

    @Test
    void testPrometheusExporter() {
        // Given
        PrometheusExporter prometheusExporter = new PrometheusExporter();

        when(mockMetricsService.getAllSnapshots(GenericMetricsSnapshot.class)).thenReturn(List.of(mockSnapshot));
        when(mockSnapshot.getDomain()).thenReturn("test_domain");
        when(mockSnapshot.getOperation()).thenReturn("test_operation");
        when(mockSnapshot.total()).thenReturn(100L);
        when(mockSnapshot.success()).thenReturn(90L);
        when(mockSnapshot.failure()).thenReturn(10L);
        when(mockSnapshot.totalDurationNanos()).thenReturn(5000_000_000L);

        // When
        String prometheusOutput = prometheusExporter.exportPrometheus(mockMetricsService);

        // Then
        assertNotNull(prometheusOutput);
        assertTrue(prometheusOutput.contains("# HELP"));
        assertTrue(prometheusOutput.contains("# TYPE"));
        assertTrue(prometheusOutput.contains("test_domain"));
        assertTrue(prometheusOutput.contains("test_operation"));
        assertTrue(prometheusOutput.contains("100"));
        assertTrue(prometheusOutput.contains("90"));
        assertTrue(prometheusOutput.contains("10"));
    }

    @Test
    void testJSONExporter() {
        // Given
        JSONExporter jsonExporter = new JSONExporter();

        when(mockMetricsService.getAllSnapshots(GenericMetricsSnapshot.class)).thenReturn(List.of(mockSnapshot));
        when(mockMetricsService.getDomainAggregatedSnapshot("test_domain")).thenReturn(mockAggregatedSnapshot);
        when(mockSnapshot.getDomain()).thenReturn("test_domain");
        when(mockSnapshot.getOperation()).thenReturn("test_operation");
        when(mockSnapshot.total()).thenReturn(50L);
        when(mockSnapshot.success()).thenReturn(45L);
        when(mockSnapshot.failure()).thenReturn(5L);
        when(mockAggregatedSnapshot.domain()).thenReturn("test_domain");
        when(mockAggregatedSnapshot.totalOperations()).thenReturn(50L);
        when(mockAggregatedSnapshot.successfulOperations()).thenReturn(45L);
        when(mockAggregatedSnapshot.failedOperations()).thenReturn(5L);

        // When
        String jsonOutput = jsonExporter.exportJSON(mockMetricsService);

        // Then
        assertNotNull(jsonOutput);
        assertTrue(jsonOutput.contains("\"timestamp\""));
        assertTrue(jsonOutput.contains("\"metrics\""));
        assertTrue(jsonOutput.contains("\"aggregated\""));
        assertTrue(jsonOutput.contains("test_domain"));
        assertTrue(jsonOutput.contains("test_operation"));
        assertTrue(jsonOutput.contains("50"));
        assertTrue(jsonOutput.contains("45"));
        assertTrue(jsonOutput.contains("5"));
    }

    @Test
    void testJMXExporter() {
        // Given
        JMXExporter jmxExporter = new JMXExporter();

        when(mockMetricsService.getAllSnapshots(GenericMetricsSnapshot.class)).thenReturn(List.of(mockSnapshot));
        when(mockSnapshot.getDomain()).thenReturn("test_domain");
        when(mockSnapshot.getOperation()).thenReturn("test_operation");
        when(mockSnapshot.total()).thenReturn(75L);
        when(mockSnapshot.success()).thenReturn(70L);
        when(mockSnapshot.failure()).thenReturn(5L);

        // When
        jmxExporter.exportJMX(mockMetricsService);

        // Then
        // JMX export returns void, so we just verify no exceptions are thrown
        // The actual JMX registration would be tested in integration tests
    }

    @Test
    void testPrometheusExporterWithEmptyData() {
        // Given
        PrometheusExporter prometheusExporter = new PrometheusExporter();

        when(mockMetricsService.getAllSnapshots(GenericMetricsSnapshot.class)).thenReturn(List.of());

        // When
        String prometheusOutput = prometheusExporter.exportPrometheus(mockMetricsService);

        // Then
        assertNotNull(prometheusOutput);
        assertTrue(prometheusOutput.contains("# HELP"));
        assertTrue(prometheusOutput.contains("# TYPE"));
        // Should not contain any metric data
        assertFalse(prometheusOutput.contains("test_domain"));
    }

    @Test
    void testJSONExporterWithEmptyData() {
        // Given
        JSONExporter jsonExporter = new JSONExporter();

        when(mockMetricsService.getAllSnapshots(GenericMetricsSnapshot.class)).thenReturn(List.of());
        when(mockMetricsService.getDomainAggregatedSnapshot("test_domain")).thenReturn(null);

        // When
        String jsonOutput = jsonExporter.exportJSON(mockMetricsService);

        // Then
        assertNotNull(jsonOutput);
        assertTrue(jsonOutput.contains("\"timestamp\""));
        assertTrue(jsonOutput.contains("\"metrics\""));
        assertTrue(jsonOutput.contains("\"aggregated\""));
        // Should contain empty arrays
        assertTrue(jsonOutput.contains("[]"));
    }

    @Test
    void testJMXExporterWithEmptyData() {
        // Given
        JMXExporter jmxExporter = new JMXExporter();

        when(mockMetricsService.getAllSnapshots(GenericMetricsSnapshot.class)).thenReturn(List.of());

        // When
        jmxExporter.exportJMX(mockMetricsService);

        // Then
        // JMX export returns void, so we just verify no exceptions are thrown
        // The actual JMX registration would be tested in integration tests
    }
}
