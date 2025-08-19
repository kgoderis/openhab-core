package org.openhab.core.ai.common.security;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for SecurityStatistics interface and BaseSecurityStatistics class.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
class SecurityStatisticsTest {

    @Test
    void testSecurityStatisticsInterface() {
        // Test that the interface defines the expected methods
        SecurityStatistics stats = new TestSecurityStatistics(100, 80, 20, 5, Instant.now());

        assertEquals(100, stats.getTotalOperations());
        assertEquals(80, stats.getSuccessfulOperations());
        assertEquals(20, stats.getFailedOperations());
        assertEquals(5, stats.getSecurityViolations());
        assertNotNull(stats.getLastOperationTime());

        // Test calculated rates
        assertEquals(80.0, stats.getSuccessRate(), 0.01);
        assertEquals(20.0, stats.getFailureRate(), 0.01);
        assertEquals(5.0, stats.getViolationRate(), 0.01);
    }

    @Test
    void testBaseSecurityStatistics() {
        Instant now = Instant.now();
        BaseSecurityStatistics stats = new TestSecurityStatistics(100, 80, 20, 5, now);

        assertEquals(100, stats.getTotalOperations());
        assertEquals(80, stats.getSuccessfulOperations());
        assertEquals(20, stats.getFailedOperations());
        assertEquals(5, stats.getSecurityViolations());
        assertEquals(now, stats.getLastOperationTime());

        // Test equals and hashCode
        BaseSecurityStatistics stats2 = new TestSecurityStatistics(100, 80, 20, 5, now);
        assertEquals(stats, stats2);
        assertEquals(stats.hashCode(), stats2.hashCode());

        // Test toString
        String toString = stats.toString();
        assertTrue(toString.contains("BaseSecurityStatistics"));
        assertTrue(toString.contains("totalOperations=100"));
        assertTrue(toString.contains("successfulOperations=80"));
        assertTrue(toString.contains("failedOperations=20"));
        assertTrue(toString.contains("securityViolations=5"));
    }

    @Test
    void testZeroOperations() {
        SecurityStatistics stats = new TestSecurityStatistics(0, 0, 0, 0, null);

        assertEquals(0, stats.getTotalOperations());
        assertEquals(0, stats.getSuccessfulOperations());
        assertEquals(0, stats.getFailedOperations());
        assertEquals(0, stats.getSecurityViolations());
        assertNull(stats.getLastOperationTime());

        // Test rates with zero operations
        assertEquals(0.0, stats.getSuccessRate(), 0.01);
        assertEquals(0.0, stats.getFailureRate(), 0.01);
        assertEquals(0.0, stats.getViolationRate(), 0.01);
    }

    @Test
    void testToolSecurityStatistics() {
        Instant now = Instant.now();
        ToolSecurityStatistics stats = new ToolSecurityStatistics(100, 80, 20, 5, now);

        // Test base functionality
        assertEquals(100, stats.getTotalOperations());
        assertEquals(80, stats.getSuccessfulOperations());
        assertEquals(20, stats.getFailedOperations());
        assertEquals(5, stats.getSecurityViolations());
        assertEquals(now, stats.getLastOperationTime());

        // Test tool-specific functionality
        assertEquals(5, stats.getSecurityAlertsCount());

        // Test backward-compatible aliases
        assertEquals(100, stats.getTotalAccessAttempts());
        assertEquals(80, stats.getAllowedAccessAttempts());
        assertEquals(20, stats.getDeniedAccessAttempts());
        assertEquals(now, stats.getLastAccessTime());
        assertEquals(100, stats.getTotalRequests());
        assertEquals(80, stats.getAllowedRequests());
        assertEquals(20, stats.getDeniedRequests());
    }

    @Test
    void testAgentSecurityStatistics() {
        Instant now = Instant.now();
        AgentSecurityStatistics stats = new AgentSecurityStatistics(100, 80, 20, 5, now, 10, 2, true, true, 50, 100, 8);

        // Test base functionality
        assertEquals(100, stats.getTotalOperations());
        assertEquals(80, stats.getSuccessfulOperations());
        assertEquals(20, stats.getFailedOperations());
        assertEquals(5, stats.getSecurityViolations());
        assertEquals(now, stats.getLastOperationTime());

        // Test agent-specific functionality
        assertEquals(10, stats.getActiveClients());
        assertEquals(2, stats.getBlockedClients());
        assertTrue(stats.isAuthenticationEnabled());
        assertTrue(stats.isRequestValidationEnabled());
        assertEquals(50, stats.getMaxConnections());
        assertEquals(100, stats.getRateLimitPerMinute());
        assertEquals(8, stats.getActiveSessions());

        // Test backward-compatible aliases
        assertEquals(20, stats.getFailedAttempts());
    }

    @Test
    void testMessageSecurityStatistics() {
        Instant now = Instant.now();
        MessageSecurityStatistics stats = new MessageSecurityStatistics(100, 80, 20, 5, now, 50, 45, 40, 3, 10, 5, 25);

        // Test base functionality
        assertEquals(100, stats.getTotalOperations());
        assertEquals(80, stats.getSuccessfulOperations());
        assertEquals(20, stats.getFailedOperations());
        assertEquals(5, stats.getSecurityViolations());
        assertEquals(now, stats.getLastOperationTime());

        // Test message-specific functionality
        assertEquals(50, stats.getTotalMessagesEncrypted());
        assertEquals(45, stats.getTotalMessagesDecrypted());
        assertEquals(40, stats.getTotalSignaturesVerified());
        assertEquals(3, stats.getTotalAuthenticationFailures());
        assertEquals(10, stats.getSecurityPolicies());
        assertEquals(5, stats.getAgentKeyPairs());
        assertEquals(25, stats.getAuditLogs());

        // Test backward-compatible aliases
        assertEquals(5, stats.getTotalSecurityIncidents());
        assertEquals(5, stats.getSecurityIncidents());
    }

    /**
     * Test implementation of SecurityStatistics for testing purposes.
     */
    private static class TestSecurityStatistics extends BaseSecurityStatistics {
        public TestSecurityStatistics(long totalOperations, long successfulOperations, long failedOperations,
                long securityViolations, Instant lastOperationTime) {
            super(totalOperations, successfulOperations, failedOperations, securityViolations, lastOperationTime);
        }
    }
}
