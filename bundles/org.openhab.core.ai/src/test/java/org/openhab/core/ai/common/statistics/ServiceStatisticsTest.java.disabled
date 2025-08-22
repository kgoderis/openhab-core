package org.openhab.core.ai.common.statistics;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link ServiceStatistics}.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
class ServiceStatisticsTest {

    @Test
    void testBuilder() {
        ServiceStatistics stats = ServiceStatistics.builder().withServiceId("test-service")
                .withServiceName("Test Service").withServiceType("http").withRequestCount(100).withErrorCount(5)
                .withHealthy(true).withLastRequestTime(1000L).withLastErrorTime(500L).withRegistrationTime(100L)
                .withMetrics(Map.of("custom", "value")).build();

        assertEquals("test-service", stats.getServiceId());
        assertEquals("Test Service", stats.getServiceName());
        assertEquals("http", stats.getServiceType());
        assertEquals(100, stats.getRequestCount());
        assertEquals(5, stats.getErrorCount());
        assertTrue(stats.isHealthy());
        assertEquals(1000L, stats.getLastRequestTime());
        assertEquals(500L, stats.getLastErrorTime());
        assertEquals(100L, stats.getRegistrationTime());
        assertEquals("value", stats.getMetric("custom"));
        assertEquals(StatisticsType.MONITORING, stats.getType());
        assertNotNull(stats.getId());
        assertNotNull(stats.getTimestamp());
    }

    @Test
    void testSuccessRate() {
        ServiceStatistics stats = ServiceStatistics.builder().withServiceId("test-service")
                .withServiceName("Test Service").withServiceType("http").withRequestCount(100).withErrorCount(10)
                .build();

        assertEquals(90.0, stats.getSuccessRate(), 0.01);
    }

    @Test
    void testSuccessRateZeroRequests() {
        ServiceStatistics stats = ServiceStatistics.builder().withServiceId("test-service")
                .withServiceName("Test Service").withServiceType("http").withRequestCount(0).withErrorCount(0).build();

        assertEquals(0.0, stats.getSuccessRate(), 0.01);
    }

    @Test
    void testValidation() {
        assertThrows(IllegalArgumentException.class, () -> {
            ServiceStatistics.builder().withServiceId("").withServiceName("Test Service").withServiceType("http")
                    .build();
        });

        assertThrows(IllegalArgumentException.class, () -> {
            ServiceStatistics.builder().withServiceId("test-service").withServiceName("").withServiceType("http")
                    .build();
        });

        assertThrows(IllegalArgumentException.class, () -> {
            ServiceStatistics.builder().withServiceId("test-service").withServiceName("Test Service")
                    .withServiceType("").build();
        });

        assertThrows(IllegalArgumentException.class, () -> {
            ServiceStatistics.builder().withServiceId("test-service").withServiceName("Test Service")
                    .withServiceType("http").withRequestCount(-1).build();
        });

        assertThrows(IllegalArgumentException.class, () -> {
            ServiceStatistics.builder().withServiceId("test-service").withServiceName("Test Service")
                    .withServiceType("http").withRequestCount(10).withErrorCount(15).build();
        });
    }

    @Test
    void testEqualsAndHashCode() {
        ServiceStatistics stats1 = ServiceStatistics.builder().withId("test-id").withServiceId("test-service")
                .withServiceName("Test Service").withServiceType("http").build();

        ServiceStatistics stats2 = ServiceStatistics.builder().withId("test-id").withServiceId("test-service")
                .withServiceName("Test Service").withServiceType("http").build();

        assertEquals(stats1, stats2);
        assertEquals(stats1.hashCode(), stats2.hashCode());
    }

    @Test
    void testToString() {
        ServiceStatistics stats = ServiceStatistics.builder().withServiceId("test-service")
                .withServiceName("Test Service").withServiceType("http").build();

        String str = stats.toString();
        assertTrue(str.contains("test-service"));
        assertTrue(str.contains("Test Service"));
        assertTrue(str.contains("http"));
    }
}
