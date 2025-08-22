package org.openhab.core.ai.common.statistics;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link InfoStatistics}.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
class InfoStatisticsTest {

    @Test
    void testBuilder() {
        InfoStatistics stats = InfoStatistics.builder().withInfoId("test-info").withInfoType("session")
                .withInfoName("Test Session").withInfoStatus("active").withCreationTime(100L).withLastUpdateTime(200L)
                .withActive(true).withAttributes(Map.of("user", "testuser")).withMetrics(Map.of("custom", "value"))
                .build();

        assertEquals("test-info", stats.getInfoId());
        assertEquals("session", stats.getInfoType());
        assertEquals("Test Session", stats.getInfoName());
        assertEquals("active", stats.getInfoStatus());
        assertEquals(100L, stats.getCreationTime());
        assertEquals(200L, stats.getLastUpdateTime());
        assertTrue(stats.isActive());
        assertEquals("testuser", stats.getAttribute("user"));
        assertEquals("value", stats.getMetric("custom"));
        assertEquals(StatisticsType.MONITORING, stats.getType());
        assertNotNull(stats.getId());
        assertNotNull(stats.getTimestamp());
    }

    @Test
    void testAttributes() {
        InfoStatistics stats = InfoStatistics.builder().withInfoId("test-info").withInfoType("session")
                .withInfoName("Test Session").withInfoStatus("active")
                .withAttributes(Map.of("key1", "value1", "key2", "value2")).build();

        assertTrue(stats.hasAttribute("key1"));
        assertTrue(stats.hasAttribute("key2"));
        assertFalse(stats.hasAttribute("key3"));
        assertEquals("value1", stats.getAttribute("key1"));
        assertEquals("value2", stats.getAttribute("key2"));
        assertNull(stats.getAttribute("key3"));

        Map<String, String> attributes = stats.getAttributes();
        assertEquals(2, attributes.size());
        assertEquals("value1", attributes.get("key1"));
        assertEquals("value2", attributes.get("key2"));
    }

    @Test
    void testValidation() {
        assertThrows(IllegalArgumentException.class, () -> {
            InfoStatistics.builder().withInfoId("").withInfoType("session").withInfoName("Test Session")
                    .withInfoStatus("active").build();
        });

        assertThrows(IllegalArgumentException.class, () -> {
            InfoStatistics.builder().withInfoId("test-info").withInfoType("").withInfoName("Test Session")
                    .withInfoStatus("active").build();
        });

        assertThrows(IllegalArgumentException.class, () -> {
            InfoStatistics.builder().withInfoId("test-info").withInfoType("session").withInfoName("")
                    .withInfoStatus("active").build();
        });

        assertThrows(IllegalArgumentException.class, () -> {
            InfoStatistics.builder().withInfoId("test-info").withInfoType("session").withInfoName("Test Session")
                    .withInfoStatus("").build();
        });

        assertThrows(IllegalArgumentException.class, () -> {
            InfoStatistics.builder().withInfoId("test-info").withInfoType("session").withInfoName("Test Session")
                    .withInfoStatus("active").withCreationTime(-1).build();
        });

        assertThrows(IllegalArgumentException.class, () -> {
            InfoStatistics.builder().withInfoId("test-info").withInfoType("session").withInfoName("Test Session")
                    .withInfoStatus("active").withCreationTime(200L).withLastUpdateTime(100L).build();
        });
    }

    @Test
    void testEqualsAndHashCode() {
        InfoStatistics stats1 = InfoStatistics.builder().withId("test-id").withInfoId("test-info")
                .withInfoType("session").withInfoName("Test Session").withInfoStatus("active").build();

        InfoStatistics stats2 = InfoStatistics.builder().withId("test-id").withInfoId("test-info")
                .withInfoType("session").withInfoName("Test Session").withInfoStatus("active").build();

        assertEquals(stats1, stats2);
        assertEquals(stats1.hashCode(), stats2.hashCode());
    }

    @Test
    void testToString() {
        InfoStatistics stats = InfoStatistics.builder().withInfoId("test-info").withInfoType("session")
                .withInfoName("Test Session").withInfoStatus("active").build();

        String str = stats.toString();
        assertTrue(str.contains("test-info"));
        assertTrue(str.contains("session"));
        assertTrue(str.contains("Test Session"));
        assertTrue(str.contains("active"));
    }
}
