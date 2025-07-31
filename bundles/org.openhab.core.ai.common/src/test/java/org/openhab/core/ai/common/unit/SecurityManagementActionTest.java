package org.openhab.core.ai.common.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.ai.common.actions.security.SecurityManagementAction;
import org.openhab.core.ai.common.api.action.AIActionContext;
import org.openhab.core.ai.common.api.action.AIActionException;
import org.openhab.core.ai.common.api.action.AIActionMetadata;
import org.openhab.core.ai.common.api.action.AIActionResult;
import org.openhab.core.ai.common.api.action.AIActionValidationResult;

/**
 * Unit tests for SecurityManagementAction.
 * 
 * Tests cover:
 * - Action metadata (ID, name, description, category, version)
 * - Parameter validation (valid and invalid scenarios)
 * - Execution (security management scenarios)
 * - Async execution
 * - Error handling
 * - Schema generation
 * - Capabilities verification
 * - Lifecycle management
 */
@ExtendWith(MockitoExtension.class)
class SecurityManagementActionTest {

    @Mock
    private AIActionContext mockContext;

    private SecurityManagementAction action;

    @BeforeEach
    void setUp() {
        action = new SecurityManagementAction();

        // Setup mock context
        when(mockContext.getProtocol()).thenReturn("mcp");
        when(mockContext.getClientId()).thenReturn("test-client");
        when(mockContext.getSessionId()).thenReturn("test-session");

        // Initialize the action
        action.initialize(mockContext);
    }

    @Test
    void testGetActionId() {
        assertEquals("openhab.security.manage", action.getActionId());
    }

    @Test
    void testGetActionName() {
        assertEquals("Security Management", action.getActionName());
    }

    @Test
    void testGetDescription() {
        assertNotNull(action.getDescription());
        assertTrue(action.getDescription().contains("Manages openHAB security"));
    }

    @Test
    void testGetCategory() {
        assertEquals("security", action.getCategory());
    }

    @Test
    void testGetVersion() {
        assertEquals("1.0.0", action.getVersion());
    }

    @Test
    void testValidateParametersWithValidSecurityStatus() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("action", "security_status");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertTrue(result.isValid());
        assertEquals(parameters, result.getSanitizedParameters());
    }

    @Test
    void testValidateParametersWithValidListUsers() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("action", "list_users");
        parameters.put("maxResults", 100);

        AIActionValidationResult result = action.validateParameters(parameters);

        assertTrue(result.isValid());
        assertEquals(parameters, result.getSanitizedParameters());
    }

    @Test
    void testValidateParametersWithValidUserInfo() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("action", "user_info");
        parameters.put("username", "testuser");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertTrue(result.isValid());
        assertEquals(parameters, result.getSanitizedParameters());
    }

    @Test
    void testValidateParametersWithValidAuthenticationMethods() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("action", "authentication_methods");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertTrue(result.isValid());
        assertEquals(parameters, result.getSanitizedParameters());
    }

    @Test
    void testValidateParametersWithValidSecurityConfig() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("action", "security_config");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertTrue(result.isValid());
        assertEquals(parameters, result.getSanitizedParameters());
    }

    @Test
    void testValidateParametersWithValidAuditLog() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("action", "audit_log");
        parameters.put("timePeriod", "24h");
        parameters.put("logLevel", "ERROR");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertTrue(result.isValid());
        assertEquals(parameters, result.getSanitizedParameters());
    }

    @Test
    void testValidateParametersWithValidCertificateInfo() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("action", "certificate_info");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertTrue(result.isValid());
        assertEquals(parameters, result.getSanitizedParameters());
    }

    @Test
    void testValidateParametersWithValidPermissionsCheck() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("action", "permissions_check");
        parameters.put("username", "testuser");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertTrue(result.isValid());
        assertEquals(parameters, result.getSanitizedParameters());
    }

    @Test
    void testValidateParametersWithMissingAction() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("username", "testuser");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.contains("action")));
    }

    @Test
    void testValidateParametersWithInvalidAction() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("action", "invalid_action");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.contains("action")));
    }

    @Test
    void testValidateParametersWithInvalidMaxResults() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("action", "list_users");
        parameters.put("maxResults", -1);

        AIActionValidationResult result = action.validateParameters(parameters);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.contains("maxResults")));
    }

    @Test
    void testValidateParametersWithInvalidLogLevel() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("action", "audit_log");
        parameters.put("logLevel", "INVALID_LEVEL");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.contains("logLevel")));
    }

    @Test
    void testExecuteSecurityStatus() throws AIActionException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("action", "security_status");

        AIActionResult result = action.execute(parameters, mockContext);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals("security_status", data.get("action"));
        assertNotNull(data.get("timestamp"));
        assertNotNull(data.get("securityStatus"));
    }

    @Test
    void testExecuteListUsers() throws AIActionException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("action", "list_users");
        parameters.put("maxResults", 50);

        AIActionResult result = action.execute(parameters, mockContext);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals("list_users", data.get("action"));
        assertNotNull(data.get("timestamp"));
        assertNotNull(data.get("users"));
    }

    @Test
    void testExecuteUserInfo() throws AIActionException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("action", "user_info");
        parameters.put("username", "testuser");

        AIActionResult result = action.execute(parameters, mockContext);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals("user_info", data.get("action"));
        assertNotNull(data.get("timestamp"));
        assertNotNull(data.get("userInfo"));
    }

    @Test
    void testExecuteAuthenticationMethods() throws AIActionException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("action", "authentication_methods");

        AIActionResult result = action.execute(parameters, mockContext);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals("authentication_methods", data.get("action"));
        assertNotNull(data.get("timestamp"));
        assertNotNull(data.get("authenticationMethods"));
    }

    @Test
    void testExecuteSecurityConfig() throws AIActionException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("action", "security_config");

        AIActionResult result = action.execute(parameters, mockContext);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals("security_config", data.get("action"));
        assertNotNull(data.get("timestamp"));
        assertNotNull(data.get("securityConfig"));
    }

    @Test
    void testExecuteAuditLog() throws AIActionException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("action", "audit_log");
        parameters.put("timePeriod", "24h");
        parameters.put("logLevel", "ERROR");

        AIActionResult result = action.execute(parameters, mockContext);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals("audit_log", data.get("action"));
        assertNotNull(data.get("timestamp"));
        assertNotNull(data.get("auditLog"));
    }

    @Test
    void testExecuteCertificateInfo() throws AIActionException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("action", "certificate_info");

        AIActionResult result = action.execute(parameters, mockContext);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals("certificate_info", data.get("action"));
        assertNotNull(data.get("timestamp"));
        assertNotNull(data.get("certificateInfo"));
    }

    @Test
    void testExecutePermissionsCheck() throws AIActionException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("action", "permissions_check");
        parameters.put("username", "testuser");

        AIActionResult result = action.execute(parameters, mockContext);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals("permissions_check", data.get("action"));
        assertNotNull(data.get("timestamp"));
        assertNotNull(data.get("permissions"));
    }

    @Test
    void testExecuteAsync() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("action", "security_status");

        CompletableFuture<AIActionResult> future = action.executeAsync(parameters, mockContext);

        assertNotNull(future);
        assertTrue(future.isDone());

        AIActionResult result = future.join();
        assertNotNull(result);
        assertTrue(result.isSuccess());
    }

    @Test
    void testExecuteWithInvalidParameters() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("invalidParam", "invalidValue");

        assertThrows(AIActionException.class, () -> {
            action.execute(parameters, mockContext);
        });
    }

    @Test
    void testExecuteWithInvalidContext() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("action", "security_status");
        AIActionContext invalidContext = AIActionContext.builder().build();

        assertThrows(AIActionException.class, () -> {
            action.execute(parameters, invalidContext);
        });
    }

    @Test
    void testExecuteAsyncWithInvalidParameters() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("invalidParam", "invalidValue");

        assertThrows(AIActionException.class, () -> {
            action.executeAsync(parameters, mockContext);
        });
    }

    @Test
    void testExecuteAsyncWithInvalidContext() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("action", "security_status");
        AIActionContext invalidContext = AIActionContext.builder().build();

        assertThrows(AIActionException.class, () -> {
            action.executeAsync(parameters, invalidContext);
        });
    }

    @Test
    void testGetParameterSchema() {
        Map<String, Object> schema = action.getParameterSchema();

        assertNotNull(schema);
        assertEquals("object", schema.get("type"));

        @SuppressWarnings("unchecked")
        Map<String, Object> properties = (Map<String, Object>) schema.get("properties");
        assertNotNull(properties);
        assertTrue(properties.containsKey("action"));
        assertTrue(properties.containsKey("username"));
        assertTrue(properties.containsKey("maxResults"));
        assertTrue(properties.containsKey("timePeriod"));
        assertTrue(properties.containsKey("logLevel"));

        @SuppressWarnings("unchecked")
        java.util.List<String> required = (java.util.List<String>) schema.get("required");
        assertNotNull(required);
        assertTrue(required.contains("action"));
    }

    @Test
    void testGetReturnSchema() {
        Map<String, Object> schema = action.getReturnSchema();

        assertNotNull(schema);
        assertEquals("object", schema.get("type"));

        @SuppressWarnings("unchecked")
        Map<String, Object> properties = (Map<String, Object>) schema.get("properties");
        assertNotNull(properties);
        assertTrue(properties.containsKey("action"));
        assertTrue(properties.containsKey("timestamp"));
        assertTrue(properties.containsKey("securityStatus"));
        assertTrue(properties.containsKey("users"));
        assertTrue(properties.containsKey("userInfo"));
        assertTrue(properties.containsKey("authenticationMethods"));
        assertTrue(properties.containsKey("securityConfig"));
        assertTrue(properties.containsKey("auditLog"));
    }

    @Test
    void testGetMetadata() {
        AIActionMetadata metadata = action.getMetadata();

        assertNotNull(metadata);
        assertEquals("1.0.0", metadata.getVersion());
        assertNotNull(metadata.getDescription());
        assertNotNull(metadata.getTags());
    }

    @Test
    void testGetCapabilities() {
        Map<String, Object> capabilities = action.getCapabilities();

        assertNotNull(capabilities);
        assertTrue(capabilities.containsKey("supportsAsync"));
        assertTrue(capabilities.containsKey("supportsValidation"));
        assertTrue(capabilities.containsKey("supportsUserManagement"));
        assertTrue(capabilities.containsKey("supportsAuditLogging"));
    }

    @Test
    void testIsReady() {
        assertTrue(action.isReady());
    }

    @Test
    void testCleanup() {
        // Should not throw any exception
        assertDoesNotThrow(() -> action.cleanup());
    }

    @Test
    void testInitialize() {
        // Should not throw any exception
        assertDoesNotThrow(() -> action.initialize(mockContext));
    }
}
