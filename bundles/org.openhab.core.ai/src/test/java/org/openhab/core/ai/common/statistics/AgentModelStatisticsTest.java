package org.openhab.core.ai.common.statistics;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for AgentModelStatistics.
 * 
 * @author Karel Goderis - Initial Contribution
 */
class AgentModelStatisticsTest {

    @Test
    void testBuilder() {
        Instant now = Instant.now();
        AgentModelStatistics stats = AgentModelStatistics.builder().withId("test-agent").withAgentId("agent-123")
                .withTotalRequests(100).withSuccessfulRequests(90).withFailedRequests(10).withCacheHits(80)
                .withCacheMisses(20).withTotalResponseTimeMs(5000).withAverageResponseTimeMs(50)
                .withMinResponseTimeMs(10).withMaxResponseTimeMs(200).withTotalTokensUsed(5000).withTotalCost(100)
                .withLastRequestTime(now).withLastSuccessTime(now).withLastFailureTime(now).withLastError("Test error")
                .build();

        assertEquals("test-agent", stats.getId());
        assertEquals("agent-123", stats.getAgentId());
        assertEquals(100, stats.getTotalRequests());
        assertEquals(90, stats.getSuccessfulRequests());
        assertEquals(10, stats.getFailedRequests());
        assertEquals(80, stats.getCacheHits());
        assertEquals(20, stats.getCacheMisses());
        assertEquals(5000, stats.getTotalResponseTimeMs());
        assertEquals(50, stats.getAverageResponseTimeMs());
        assertEquals(10, stats.getMinResponseTimeMs());
        assertEquals(200, stats.getMaxResponseTimeMs());
        assertEquals(5000, stats.getTotalTokensUsed());
        assertEquals(100, stats.getTotalCost());
        assertEquals(now, stats.getLastRequestTime());
        assertEquals(now, stats.getLastSuccessTime());
        assertEquals(now, stats.getLastFailureTime());
        assertEquals("Test error", stats.getLastError());
        assertEquals(StatisticsType.EXECUTION, stats.getType());
    }

    @Test
    void testToBuilder() {
        AgentModelStatistics original = AgentModelStatistics.builder().withId("test-agent").withAgentId("agent-123")
                .withTotalRequests(100).withSuccessfulRequests(90).build();

        AgentModelStatistics copy = original.toBuilder().withFailedRequests(15).build();

        assertEquals("test-agent", copy.getId());
        assertEquals("agent-123", copy.getAgentId());
        assertEquals(100, copy.getTotalRequests());
        assertEquals(90, copy.getSuccessfulRequests());
        assertEquals(15, copy.getFailedRequests());
    }

    @Test
    void testCacheHitRate() {
        AgentModelStatistics stats = AgentModelStatistics.builder().withId("test").withAgentId("agent-123")
                .withCacheHits(80).withCacheMisses(20).build();

        assertEquals(80.0, stats.getCacheHitRate(), 0.01);
    }

    @Test
    void testCacheHitRateZero() {
        AgentModelStatistics stats = AgentModelStatistics.builder().withId("test").withAgentId("agent-123")
                .withCacheHits(0).withCacheMisses(0).build();

        assertEquals(0.0, stats.getCacheHitRate(), 0.01);
    }

    @Test
    void testSuccessRate() {
        AgentModelStatistics stats = AgentModelStatistics.builder().withId("test").withAgentId("agent-123")
                .withTotalRequests(100).withSuccessfulRequests(90).withFailedRequests(10).build();

        assertEquals(90.0, stats.getSuccessRate(), 0.01);
    }

    @Test
    void testSuccessRateZero() {
        AgentModelStatistics stats = AgentModelStatistics.builder().withId("test").withAgentId("agent-123")
                .withTotalRequests(0).withSuccessfulRequests(0).withFailedRequests(0).build();

        assertEquals(0.0, stats.getSuccessRate(), 0.01);
    }

    @Test
    void testValidation() {
        AgentModelStatistics.Builder builder = AgentModelStatistics.builder().withId("").withAgentId("");

        assertFalse(builder.isValid());
        assertNotNull(builder.getValidationErrors());
        assertTrue(builder.getValidationErrors().contains("id cannot be blank"));
        assertTrue(builder.getValidationErrors().contains("agentId cannot be blank"));
    }

    @Test
    void testValidationNegativeValues() {
        AgentModelStatistics.Builder builder = AgentModelStatistics.builder().withId("test").withAgentId("agent-123")
                .withTotalRequests(-1).withSuccessfulRequests(-1).withFailedRequests(-1);

        assertFalse(builder.isValid());
        assertNotNull(builder.getValidationErrors());
        assertTrue(builder.getValidationErrors().contains("totalRequests must be non-negative"));
        assertTrue(builder.getValidationErrors().contains("successfulRequests must be non-negative"));
        assertTrue(builder.getValidationErrors().contains("failedRequests must be non-negative"));
    }

    @Test
    void testValidationInconsistentCounts() {
        AgentModelStatistics.Builder builder = AgentModelStatistics.builder().withId("test").withAgentId("agent-123")
                .withTotalRequests(100).withSuccessfulRequests(60).withFailedRequests(50);

        assertFalse(builder.isValid());
        assertNotNull(builder.getValidationErrors());
        assertTrue(builder.getValidationErrors()
                .contains("successfulRequests + failedRequests cannot exceed totalRequests"));
    }

    @Test
    void testEqualsAndHashCode() {
        AgentModelStatistics stats1 = AgentModelStatistics.builder().withId("test").withAgentId("agent-123")
                .withTotalRequests(100).build();

        AgentModelStatistics stats2 = AgentModelStatistics.builder().withId("test").withAgentId("agent-123")
                .withTotalRequests(100).build();

        assertEquals(stats1, stats2);
        assertEquals(stats1.hashCode(), stats2.hashCode());
    }

    @Test
    void testNotEquals() {
        AgentModelStatistics stats1 = AgentModelStatistics.builder().withId("test1").withAgentId("agent-123")
                .withTotalRequests(100).build();

        AgentModelStatistics stats2 = AgentModelStatistics.builder().withId("test2").withAgentId("agent-123")
                .withTotalRequests(100).build();

        assertNotEquals(stats1, stats2);
        assertNotEquals(stats1.hashCode(), stats2.hashCode());
    }

    @Test
    void testToString() {
        AgentModelStatistics stats = AgentModelStatistics.builder().withId("test").withAgentId("agent-123")
                .withTotalRequests(100).withSuccessfulRequests(90).withFailedRequests(10).withCacheHits(80)
                .withCacheMisses(20).build();

        String result = stats.toString();
        assertTrue(result.contains("test"));
        assertTrue(result.contains("agent-123"));
        assertTrue(result.contains("100"));
        assertTrue(result.contains("90"));
        assertTrue(result.contains("10"));
        assertTrue(result.contains("80.00%"));
        assertTrue(result.contains("90.00%"));
    }

    @Test
    void testMetrics() {
        AgentModelStatistics stats = AgentModelStatistics.builder().withId("test").withAgentId("agent-123")
                .withTotalRequests(100).withSuccessfulRequests(90).withFailedRequests(10).withCacheHits(80)
                .withCacheMisses(20).build();

        assertEquals("agent-123", stats.getMetric("agentId"));
        assertEquals(100L, stats.getMetric("totalRequests"));
        assertEquals(90L, stats.getMetric("successfulRequests"));
        assertEquals(10L, stats.getMetric("failedRequests"));
        assertEquals(80L, stats.getMetric("cacheHits"));
        assertEquals(20L, stats.getMetric("cacheMisses"));
        assertEquals(80.0, (Double) stats.getMetric("cacheHitRate"), 0.01);
        assertEquals(90.0, (Double) stats.getMetric("successRate"), 0.01);
        assertTrue(stats.hasMetric("agentId"));
        assertFalse(stats.hasMetric("nonexistent"));
    }

    @Test
    void testReset() {
        AgentModelStatistics.Builder builder = AgentModelStatistics.builder().withId("test").withAgentId("agent-123")
                .withTotalRequests(100);

        builder.reset();

        AgentModelStatistics stats = builder.build();
        assertEquals("", stats.getId());
        assertEquals("", stats.getAgentId());
        assertEquals(0, stats.getTotalRequests());
    }
}
