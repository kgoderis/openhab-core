package org.openhab.core.ai.agent.transport;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import javax.ws.rs.core.Response;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.ai.agent.communication.notifications.AgentPushNotificationManager;
import org.openhab.core.ai.agent.execution.AgentTaskManager;
import org.openhab.core.ai.agent.lifecycle.AgentRegistry;

/**
 * Integration tests for REST API endpoints
 * 
 * @author Karel Goderis - Initial Contribution
 */
@ExtendWith(MockitoExtension.class)
public class RestApiIntegrationTest {

    @Mock
    private AgentTaskManager taskManager;

    @Mock
    private AgentRegistry agentRegistry;

    @Mock
    private AgentTransportFactory transportFactory;

    @Mock
    private AgentPushNotificationManager pushNotificationManager;

    @Test
    public void testAgentRestExtensions() {
        AgentRestExtensions resource = new AgentRestExtensions();

        // Test basic functionality
        assertNotNull(resource);
        // Note: Actual method testing requires HTTP context
    }

    @Test
    public void testAgentRestResource() {
        AgentRestResource resource = new AgentRestResource();

        // Test basic functionality
        assertNotNull(resource);
        // Note: Actual method testing requires HTTP context and dependencies
    }

    @Test
    public void testAiManagementResource() {
        AiManagementResource resource = new AiManagementResource();
        resource.setAgentTaskManager(taskManager);

        // Test system health
        Response response = resource.getSystemHealth();
        assertEquals(200, response.getStatus());

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getEntity();
        assertNotNull(body);
        assertEquals("healthy", body.get("status"));

        // Test configuration
        response = resource.getCurrentConfiguration();
        assertEquals(200, response.getStatus());

        // Test tools
        response = resource.getAvailableTools();
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testAiUserInfoResource() {
        AiUserInfoResource resource = new AiUserInfoResource();

        // Test status endpoint
        Response response = resource.getStatus();
        assertEquals(200, response.getStatus());

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getEntity();
        assertNotNull(body);
        assertEquals("ai-user-info", body.get("service"));
    }

    @Test
    public void testRestProtocolComplianceResource() {
        RestProtocolComplianceResource resource = new RestProtocolComplianceResource();

        // Test compliance status
        Response response = resource.getComplianceStatus();
        assertEquals(200, response.getStatus());

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getEntity();
        assertNotNull(body);
        assertEquals("compliant", body.get("status"));
    }

    @Test
    public void testMcpProtocolIntegrationResource() {
        McpProtocolIntegrationResource resource = new McpProtocolIntegrationResource();

        // Test MCP status
        Response response = resource.getMcpStatus();
        assertEquals(200, response.getStatus());

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getEntity();
        assertNotNull(body);
        assertEquals("MCP", body.get("protocol"));
    }

    @Test
    public void testAiIntegrationResource() {
        AiIntegrationResource resource = new AiIntegrationResource();

        // Test integration status
        Response response = resource.getIntegrationStatus();
        assertEquals(200, response.getStatus());

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getEntity();
        assertNotNull(body);
        assertEquals("openHAB AI", body.get("integration"));
    }

    @Test
    public void testRestSecurityFramework() {
        // Test security framework utilities
        Response response = org.openhab.core.ai.rest.RestSecurityFramework.forbidden("Test forbidden");
        assertEquals(403, response.getStatus());

        response = org.openhab.core.ai.rest.RestSecurityFramework.unauthorized("Test unauthorized");
        assertEquals(401, response.getStatus());
    }

    @Test
    public void testSharedRestInfrastructure() {
        // Test shared infrastructure utilities
        Map<String, Object> data = Map.of("test", "value");
        Response response = org.openhab.core.ai.rest.SharedRestInfrastructure.okJson(data);
        assertEquals(200, response.getStatus());

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getEntity();
        assertNotNull(body);
        assertEquals("value", body.get("test"));
    }
}
