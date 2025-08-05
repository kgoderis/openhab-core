package org.openhab.core.ai.mcp.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.ai.auth.AIAuditLogger;
import org.openhab.core.ai.auth.AIAuthenticationContext;
import org.openhab.core.ai.auth.AIAuthenticationManager;
import org.openhab.core.ai.auth.AIRoleBasedAccessControl;
import org.openhab.core.ai.tool.internal.MCPSecurityManager;
import org.openhab.core.ai.tool.internal.ToolServerConfiguration;

/**
 * Unit tests for MCPSecurityManager using real SDK classes.
 *
 * Tests authentication, authorization, RBAC integration, audit logging,
 * and security scenarios.
 *
 * 
 */
@ExtendWith(MockitoExtension.class)
class MCPSecurityManagerTest {

    @Mock
    private AIAuthenticationManager authManager;

    @Mock
    private AIRoleBasedAccessControl rbac;

    @Mock
    private AIAuditLogger auditLogger;

    private ToolServerConfiguration config;
    private MCPSecurityManager securityManager;

    @BeforeEach
    void setUp() {
        config = ToolServerConfiguration.builder().serverId("test-server").serverName("Test Server")
                .enableAuthentication(true).primaryAuthMethod("oauth2.1").fallbackAuthMethod("api_key")
                .enableFallbackAuth(true).oauthIssuerUrl("https://oauth.example.com").oauthClientId("test-client")
                .oauthClientSecret("test-secret").oauthRedirectUri("http://localhost:8080/callback")
                .oauthPkceEnabled(true).openhabUsersFile("/path/to/users.properties").openhabUsersEnabled(true)
                .apiKeyHeader("X-API-Key").apiKeyValue("test-api-key").apiKeyEnabled(true).jwtSecret("test-jwt-secret")
                .jwtIssuer("test-issuer").jwtExpirationMinutes(60).jwtEnabled(true).maxConnections(100)
                .rateLimitPerMinute(1000).enableRequestValidation(true).build();

        securityManager = new MCPSecurityManager(authManager, rbac, auditLogger, config);
    }

    @Test
    void testInitialization() {
        // Test that the security manager is properly initialized
        assertNotNull(securityManager);
    }

    @Test
    void testInitializationWithValidDependencies() {
        // Test initialization with valid dependencies
        assertDoesNotThrow(() -> {
            new MCPSecurityManager(authManager, rbac, auditLogger, config);
        });
    }

    @Test
    void testAuthenticateClient() {
        // Test client authentication
        Map<String, String> credentials = Map.of("username", "testuser", "password", "testpass");

        // Mock authentication manager to return a context
        AIAuthenticationContext mockContext = mock(AIAuthenticationContext.class);
        when(authManager.authenticate(any(Map.class), anyString(), anyString())).thenReturn(Optional.of(mockContext));

        Optional<AIAuthenticationContext> result = securityManager.authenticateClient(credentials, "client-123");

        assertTrue(result.isPresent());
        assertEquals(mockContext, result.get());
        verify(authManager).authenticate(credentials, "client-123", "client-123");
    }

    @Test
    void testAuthenticateClientWithEmptyCredentials() {
        // Test authentication with empty credentials
        Map<String, String> credentials = Map.of();
        Optional<AIAuthenticationContext> result = securityManager.authenticateClient(credentials, "client-123");
        assertFalse(result.isPresent());
    }

    @Test
    void testAuthenticateClientWithEmptyClientId() {
        // Test authentication with empty client ID
        Map<String, String> credentials = Map.of("username", "testuser", "password", "testpass");

        Optional<AIAuthenticationContext> result = securityManager.authenticateClient(credentials, "");
        assertFalse(result.isPresent());
    }

    @Test
    void testAuthenticateWithJWT() {
        // Test JWT authentication
        String jwtToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test.signature";

        // Mock authentication manager to return a context
        AIAuthenticationContext mockContext = mock(AIAuthenticationContext.class);
        when(authManager.authenticateWithJWT(anyString(), anyString())).thenReturn(Optional.of(mockContext));

        Optional<AIAuthenticationContext> result = securityManager.authenticateWithJWT(jwtToken, "client-123");

        assertTrue(result.isPresent());
        assertEquals(mockContext, result.get());
        verify(authManager).authenticateWithJWT(jwtToken, "client-123");
    }

    @Test
    void testAuthenticateWithJWTWithEmptyToken() {
        // Test JWT authentication with empty token
        Optional<AIAuthenticationContext> result = securityManager.authenticateWithJWT("", "client-123");
        assertFalse(result.isPresent());
    }

    @Test
    void testAuthenticateWithJWTWithEmptyClientId() {
        // Test JWT authentication with empty client ID
        String jwtToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test.signature";

        Optional<AIAuthenticationContext> result = securityManager.authenticateWithJWT(jwtToken, "");
        assertFalse(result.isPresent());
    }

    @Test
    void testHasPermission() {
        // Test permission checking
        AIAuthenticationContext mockContext = mock(AIAuthenticationContext.class);
        when(rbac.hasPermission(anyString(), anyString(), anyString())).thenReturn(true);

        boolean hasPermission = securityManager.hasPermission(mockContext, "test:permission");

        assertTrue(hasPermission);
        verify(rbac).hasPermission(anyString(), anyString(), eq("test:permission"));
    }

    @Test
    void testHasPermissionWithEmptyPermission() {
        // Test permission checking with empty permission
        AIAuthenticationContext mockContext = mock(AIAuthenticationContext.class);
        boolean hasPermission = securityManager.hasPermission(mockContext, "");
        assertFalse(hasPermission);
    }

    @Test
    void testHasMCPPermission() {
        // Test MCP-specific permission checking
        AIAuthenticationContext mockContext = mock(AIAuthenticationContext.class);
        when(rbac.hasPermission(anyString(), anyString(), anyString())).thenReturn(true);

        boolean hasPermission = securityManager.hasMCPPermission(mockContext, "connect");

        assertTrue(hasPermission);
        verify(rbac).hasPermission(anyString(), anyString(), eq("mcp:connect"));
    }

    @Test
    void testHasMCPPermissionWithEmptyPermission() {
        // Test MCP permission checking with empty permission
        AIAuthenticationContext mockContext = mock(AIAuthenticationContext.class);
        boolean hasPermission = securityManager.hasMCPPermission(mockContext, "");
        assertFalse(hasPermission);
    }

    @Test
    void testValidateRequest() {
        // Test request validation
        boolean isValid = securityManager.validateRequest("client-123", "tools/list");
        assertTrue(isValid);
    }

    @Test
    void testValidateRequestWithEmptyClientId() {
        // Test request validation with empty client ID
        boolean isValid = securityManager.validateRequest("", "tools/list");
        assertFalse(isValid);
    }

    @Test
    void testValidateRequestWithEmptyRequestType() {
        // Test request validation with empty request type
        boolean isValid = securityManager.validateRequest("client-123", "");
        assertFalse(isValid);
    }

    @Test
    void testIsClientBlocked() {
        // Test client blocking status
        boolean isBlocked = securityManager.isClientBlocked("client-123");
        assertFalse(isBlocked); // Should not be blocked initially
    }

    @Test
    void testIsClientBlockedWithEmptyClientId() {
        // Test client blocking status with empty client ID
        boolean isBlocked = securityManager.isClientBlocked("");
        assertFalse(isBlocked);
    }

    @Test
    void testGetSecurityStatistics() {
        // Test getting security statistics
        MCPSecurityManager.SecurityStatistics stats = securityManager.getSecurityStatistics();
        assertNotNull(stats);
        assertEquals(0, stats.getActiveClients());
        assertEquals(0, stats.getFailedAttempts());
        assertEquals(0, stats.getBlockedClients());
        assertTrue(stats.isAuthenticationEnabled());
        assertTrue(stats.isRequestValidationEnabled());
        assertEquals(100, stats.getMaxConnections());
        assertEquals(1000, stats.getRateLimitPerMinute());
        assertEquals(0, stats.getActiveSessions());
    }

    @Test
    void testGetAuthenticationManager() {
        // Test getting authentication manager
        AIAuthenticationManager manager = securityManager.getAuthenticationManager();
        assertNotNull(manager);
        assertEquals(authManager, manager);
    }

    @Test
    void testGetRoleBasedAccessControl() {
        // Test getting RBAC
        AIRoleBasedAccessControl rbacInstance = securityManager.getRoleBasedAccessControl();
        assertNotNull(rbacInstance);
        assertEquals(rbac, rbacInstance);
    }

    @Test
    void testGetAuditLogger() {
        // Test getting audit logger
        AIAuditLogger logger = securityManager.getAuditLogger();
        assertNotNull(logger);
        assertEquals(auditLogger, logger);
    }

    @Test
    void testSecurityStatisticsToString() {
        // Test security statistics toString method
        MCPSecurityManager.SecurityStatistics stats = securityManager.getSecurityStatistics();
        String statsString = stats.toString();

        assertNotNull(statsString);
        assertFalse(statsString.isEmpty());
    }

    @Test
    void testSecurityStatisticsEquality() {
        // Test security statistics equality
        MCPSecurityManager.SecurityStatistics stats1 = new MCPSecurityManager.SecurityStatistics(1, 2, 3, true, true,
                100, 1000, 5);

        MCPSecurityManager.SecurityStatistics stats2 = new MCPSecurityManager.SecurityStatistics(1, 2, 3, true, true,
                100, 1000, 5);

        MCPSecurityManager.SecurityStatistics stats3 = new MCPSecurityManager.SecurityStatistics(2, 2, 3, true, true,
                100, 1000, 5);

        assertEquals(stats1, stats2);
        assertNotEquals(stats1, stats3);
        assertNotEquals(stats1, null);
        assertNotEquals(stats1, "not stats");
    }

    @Test
    void testSecurityStatisticsHashCode() {
        // Test security statistics hashCode consistency
        MCPSecurityManager.SecurityStatistics stats1 = new MCPSecurityManager.SecurityStatistics(1, 2, 3, true, true,
                100, 1000, 5);

        MCPSecurityManager.SecurityStatistics stats2 = new MCPSecurityManager.SecurityStatistics(1, 2, 3, true, true,
                100, 1000, 5);

        assertEquals(stats1.hashCode(), stats2.hashCode());
    }

    @Test
    void testConcurrentSecurityOperations() {
        // Test concurrent security operations
        assertDoesNotThrow(() -> {
            Thread thread1 = new Thread(() -> {
                for (int i = 0; i < 10; i++) {
                    securityManager.validateRequest("client-" + i, "tools/list");
                }
            });

            Thread thread2 = new Thread(() -> {
                for (int i = 0; i < 10; i++) {
                    securityManager.isClientBlocked("client-" + i);
                }
            });

            thread1.start();
            thread2.start();

            thread1.join(5000);
            thread2.join(5000);

            MCPSecurityManager.SecurityStatistics stats = securityManager.getSecurityStatistics();
            assertNotNull(stats);
        });
    }

    @Test
    void testMultipleAuthenticationAttempts() {
        // Test multiple authentication attempts
        Map<String, String> credentials = Map.of("username", "testuser", "password", "testpass");

        // Mock authentication manager to return different results
        AIAuthenticationContext mockContext = mock(AIAuthenticationContext.class);
        when(authManager.authenticate(any(Map.class), anyString(), anyString())).thenReturn(Optional.of(mockContext))
                .thenReturn(Optional.empty()).thenReturn(Optional.of(mockContext));

        // First attempt - should succeed
        Optional<AIAuthenticationContext> result1 = securityManager.authenticateClient(credentials, "client-1");
        assertTrue(result1.isPresent());

        // Second attempt - should fail
        Optional<AIAuthenticationContext> result2 = securityManager.authenticateClient(credentials, "client-2");
        assertFalse(result2.isPresent());

        // Third attempt - should succeed
        Optional<AIAuthenticationContext> result3 = securityManager.authenticateClient(credentials, "client-3");
        assertTrue(result3.isPresent());
    }

    @Test
    void testToString() {
        // Test toString method
        String managerString = securityManager.toString();
        assertNotNull(managerString);
        assertFalse(managerString.isEmpty());
    }

    @Test
    void testEquality() {
        // Test equality
        MCPSecurityManager manager1 = new MCPSecurityManager(authManager, rbac, auditLogger, config);
        MCPSecurityManager manager2 = new MCPSecurityManager(authManager, rbac, auditLogger, config);

        // These should be different instances
        assertNotEquals(manager1, manager2);
        assertNotEquals(manager1, null);
        assertNotEquals(manager1, "not a manager");
    }
}
