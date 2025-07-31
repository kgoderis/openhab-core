package org.openhab.core.ai.a2a.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.ai.a2a.internal.A2ASecurityManager;
import org.openhab.core.ai.common.auth.AIAuditLogger;
import org.openhab.core.ai.common.auth.AIAuthenticationContext;
import org.openhab.core.ai.common.auth.AIAuthenticationManager;
import org.openhab.core.ai.common.auth.AIRoleBasedAccessControl;

import io.a2a.spec.Message;

@ExtendWith(MockitoExtension.class)
class A2ASecurityManagerTest {

    @Mock
    private AIAuthenticationManager mockAuthManager;

    @Mock
    private AIRoleBasedAccessControl mockRbac;

    @Mock
    private AIAuditLogger mockAuditLogger;

    @Mock
    private Message mockMessage;

    @Mock
    private AIAuthenticationContext mockAuthContext;

    private A2ASecurityManager securityManager;

    @BeforeEach
    void setUp() {
        securityManager = new A2ASecurityManager();
    }

    @Test
    void testConstructor() {
        assertNotNull(securityManager);
    }

    @Test
    void testDeactivate() {
        // Test deactivation
        securityManager.deactivate();

        // Should not throw exception
        assertNotNull(securityManager);
    }

    @Test
    void testAuthenticateA2AMessageWithNullMessage() {
        Optional<AIAuthenticationContext> result = securityManager.authenticateA2AMessage(null);

        assertFalse(result.isPresent());
    }

    @Test
    void testAuthenticateA2AMessageWithValidMessage() {
        // Setup mock message with metadata
        Map<String, Object> metadata = Map.of("senderId", "test-client", "receiverId", "openhab-agent", "priority",
                "normal", "correlationId", "test-correlation-123");

        when(mockMessage.getMetadata()).thenReturn(metadata);

        // Test authentication
        Optional<AIAuthenticationContext> result = securityManager.authenticateA2AMessage(mockMessage);

        // Should return empty since authentication is disabled by default
        assertFalse(result.isPresent());
    }

    @Test
    void testHasA2APermissionWithNullContext() {
        boolean result = securityManager.hasA2APermission(null, "test.permission");

        assertFalse(result);
    }

    @Test
    void testHasA2APermissionWithValidContext() {
        // Test with valid context and permission
        boolean result = securityManager.hasA2APermission(mockAuthContext, "a2a:execute");

        // Should return false since RBAC is not injected
        assertFalse(result);
    }

    @Test
    void testHasA2APermissionWithA2APermissionEnum() {
        // Test with A2APermission enum
        boolean result = securityManager.hasA2APermission(mockAuthContext, A2ASecurityManager.A2APermission.EXECUTE);

        // Should return false since RBAC is not injected
        assertFalse(result);
    }

    @Test
    void testValidateA2ARequestWithNullClientId() {
        boolean result = securityManager.validateA2ARequest(null, "test.request");

        assertFalse(result);
    }

    @Test
    void testValidateA2ARequestWithNullRequestType() {
        boolean result = securityManager.validateA2ARequest("test-client", null);

        assertFalse(result);
    }

    @Test
    void testValidateA2ARequestWithValidParameters() {
        boolean result = securityManager.validateA2ARequest("test-client", "message.send");

        // Should return true for valid parameters
        assertTrue(result);
    }

    @Test
    void testIsClientBlockedWithNullClientId() {
        boolean result = securityManager.isClientBlocked(null);

        assertFalse(result);
    }

    @Test
    void testIsClientBlockedWithValidClientId() {
        boolean result = securityManager.isClientBlocked("test-client");

        // Should return false for valid client
        assertFalse(result);
    }

    @Test
    void testGetSecurityStatistics() {
        A2ASecurityManager.SecurityStatistics statistics = securityManager.getSecurityStatistics();

        assertNotNull(statistics);
        assertEquals(0, statistics.getActiveClients());
        assertEquals(0, statistics.getFailedAttempts());
        assertEquals(0, statistics.getBlockedClients());
        assertFalse(statistics.isAuthenticationEnabled());
        assertTrue(statistics.isRequestValidationEnabled());
        assertEquals(100, statistics.getMaxConnections());
        assertEquals(60, statistics.getRateLimitPerMinute());
        assertEquals(0, statistics.getActiveSessions());
    }

    @Test
    void testGetAuthenticationManager() {
        AIAuthenticationManager result = securityManager.getAuthenticationManager();

        // Should return null since not injected
        assertNull(result);
    }

    @Test
    void testGetRoleBasedAccessControl() {
        AIRoleBasedAccessControl result = securityManager.getRoleBasedAccessControl();

        // Should return null since not injected
        assertNull(result);
    }

    @Test
    void testGetAuditLogger() {
        AIAuditLogger result = securityManager.getAuditLogger();

        // Should return null since not injected
        assertNull(result);
    }

    @Test
    void testA2APermissionEnum() {
        // Test all A2APermission enum values
        assertEquals("connect", A2ASecurityManager.A2APermission.CONNECT.getPermission());
        assertEquals("execute", A2ASecurityManager.A2APermission.EXECUTE.getPermission());
        assertEquals("read", A2ASecurityManager.A2APermission.READ.getPermission());
        assertEquals("write", A2ASecurityManager.A2APermission.WRITE.getPermission());
        assertEquals("admin", A2ASecurityManager.A2APermission.ADMIN.getPermission());
    }

    @Test
    void testSecurityStatisticsConstructor() {
        A2ASecurityManager.SecurityStatistics statistics = new A2ASecurityManager.SecurityStatistics(5, 10, 2, true,
                false, 50, 30, 3);

        assertEquals(5, statistics.getActiveClients());
        assertEquals(10, statistics.getFailedAttempts());
        assertEquals(2, statistics.getBlockedClients());
        assertTrue(statistics.isAuthenticationEnabled());
        assertFalse(statistics.isRequestValidationEnabled());
        assertEquals(50, statistics.getMaxConnections());
        assertEquals(30, statistics.getRateLimitPerMinute());
        assertEquals(3, statistics.getActiveSessions());
    }

    @Test
    void testMessageMetadataExtraction() {
        // Test message with various metadata combinations
        Map<String, Object> metadata1 = Map.of("senderId", "client1");
        when(mockMessage.getMetadata()).thenReturn(metadata1);

        Optional<AIAuthenticationContext> result1 = securityManager.authenticateA2AMessage(mockMessage);
        assertFalse(result1.isPresent()); // Authentication disabled by default

        Map<String, Object> metadata2 = Map.of("senderId", "client2", "priority", "high", "correlationId", "corr-123");
        when(mockMessage.getMetadata()).thenReturn(metadata2);

        Optional<AIAuthenticationContext> result2 = securityManager.authenticateA2AMessage(mockMessage);
        assertFalse(result2.isPresent()); // Authentication disabled by default
    }

    @Test
    void testRequestValidationWithDifferentTypes() {
        // Test various request types
        assertTrue(securityManager.validateA2ARequest("client1", "message.send"));
        assertTrue(securityManager.validateA2ARequest("client2", "task.get"));
        assertTrue(securityManager.validateA2ARequest("client3", "task.cancel"));
        assertTrue(securityManager.validateA2ARequest("client4", "message.send.stream"));

        // Test invalid request types
        assertFalse(securityManager.validateA2ARequest("client1", ""));
        assertFalse(securityManager.validateA2ARequest("client2", "invalid.request"));
    }

    @Test
    void testClientBlockingLogic() {
        // Test that clients are not blocked by default
        assertFalse(securityManager.isClientBlocked("client1"));
        assertFalse(securityManager.isClientBlocked("client2"));
        assertFalse(securityManager.isClientBlocked("client3"));
    }

    @Test
    void testPermissionValidationWithDifferentPermissions() {
        // Test various permissions
        assertFalse(securityManager.hasA2APermission(mockAuthContext, "a2a:connect"));
        assertFalse(securityManager.hasA2APermission(mockAuthContext, "a2a:execute"));
        assertFalse(securityManager.hasA2APermission(mockAuthContext, "a2a:read"));
        assertFalse(securityManager.hasA2APermission(mockAuthContext, "a2a:write"));
        assertFalse(securityManager.hasA2APermission(mockAuthContext, "a2a:admin"));

        // Test invalid permissions
        assertFalse(securityManager.hasA2APermission(mockAuthContext, "invalid:permission"));
        assertFalse(securityManager.hasA2APermission(mockAuthContext, ""));
    }

    @Test
    void testSecurityManagerLifecycle() {
        // Test complete lifecycle
        assertNotNull(securityManager);

        A2ASecurityManager.SecurityStatistics stats = securityManager.getSecurityStatistics();
        assertNotNull(stats);

        securityManager.deactivate();
        assertNotNull(securityManager);
    }
}
