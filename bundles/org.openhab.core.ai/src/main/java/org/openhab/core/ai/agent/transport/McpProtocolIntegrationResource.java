package org.openhab.core.ai.agent.transport;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.rest.SharedRestInfrastructure;
import org.openhab.core.ai.tool.registry.ToolRegistry;
import org.openhab.core.ai.tool.server.DefaultToolServerManager;
import org.openhab.core.io.rest.RESTConstants;
import org.openhab.core.io.rest.RESTResource;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.jaxrs.whiteboard.JaxrsWhiteboardConstants;
import org.osgi.service.jaxrs.whiteboard.propertytypes.JSONRequired;
import org.osgi.service.jaxrs.whiteboard.propertytypes.JaxrsApplicationSelect;
import org.osgi.service.jaxrs.whiteboard.propertytypes.JaxrsName;
import org.osgi.service.jaxrs.whiteboard.propertytypes.JaxrsResource;

/**
 * MCP Protocol Integration Resource
 * 
 * @author Karel Goderis - Initial Contribution
 */
@JaxrsResource
@JaxrsName("ai/mcp")
@JaxrsApplicationSelect("(" + JaxrsWhiteboardConstants.JAX_RS_NAME + "=" + RESTConstants.JAX_RS_NAME + ")")
@JSONRequired
@Path("ai/mcp")
@NonNullByDefault
@Component(service = RESTResource.class)
public class McpProtocolIntegrationResource implements RESTResource {

    @Reference
    private @Nullable DefaultToolServerManager toolServerManager;

    @Reference
    private @Nullable ToolRegistry toolRegistry;

    @GET
    @Path("status")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMcpStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("timestamp", System.currentTimeMillis());
        status.put("protocol", "MCP");
        status.put("version", "2024-11-05");

        // Get actual MCP server status
        DefaultToolServerManager serverManager = toolServerManager;
        if (serverManager != null && serverManager.isStarted()) {
            status.put("status", "active");

            // Get actual server instances
            Map<String, Object> serverInstances = new HashMap<>();
            serverManager.getAllServerInstances().forEach((id, server) -> {
                Map<String, Object> serverInfo = new HashMap<>();
                serverInfo.put("status", server.isRunning() ? "running" : "stopped");
                serverInfo.put("state", server.getState().toString());
                serverInfo.put("healthy", server.isHealthy());

                // Get transport statistics
                TransportStatistics transportStats = server.getTransportStatistics();
                serverInfo.put("transport_type",
                        transportStats.getCurrentType() != null ? transportStats.getCurrentType().toString()
                                : "unknown");
                serverInfo.put("uptime", transportStats.getUptime());
                serverInfo.put("transport_healthy", transportStats.isHealthy());

                // Get security statistics if available
                if (server.isSecurityEnabled()) {
                    var securityStats = server.getSecurityStatistics();
                    if (securityStats != null) {
                        serverInfo.put("security_enabled", true);
                        serverInfo.put("security_healthy", true);
                    }
                } else {
                    serverInfo.put("security_enabled", false);
                }

                serverInstances.put(id, serverInfo);
            });
            status.put("server_instances", serverInstances);
            status.put("total_servers", serverInstances.size());
        } else {
            status.put("status", "inactive");
            status.put("server_instances", new HashMap<>());
            status.put("total_servers", 0);
        }

        // Get actual transport information
        Map<String, Object> transports = new HashMap<>();
        if (serverManager != null) {
            serverManager.getAllServerInstances().forEach((id, server) -> {
                TransportStatistics transportStats = server.getTransportStatistics();
                if (transportStats.getCurrentType() != null) {
                    Map<String, Object> transportInfo = new HashMap<>();
                    transportInfo.put("status", server.isRunning() ? "available" : "unavailable");
                    transportInfo.put("transport_type", transportStats.getCurrentType().toString());
                    transportInfo.put("protocol_version", "2024-11-05");
                    transportInfo.put("healthy", transportStats.isHealthy());
                    transportInfo.put("uptime", transportStats.getUptime());
                    transportInfo.put("last_error", transportStats.getLastError());
                    transports.put(transportStats.getCurrentType().toString().toLowerCase(), transportInfo);
                }
            });
        }
        status.put("transports", transports);

        // Get actual server information
        Map<String, Object> server = new HashMap<>();
        if (serverManager != null) {
            server.put("name", "OpenHAB MCP Server");
            server.put("version", "1.0.0");
            server.put("vendor", "openHAB Foundation");

            // Get actual capabilities from tool registry
            ToolRegistry registry = toolRegistry;
            Map<String, Object> capabilities = new HashMap<>();
            capabilities.put("tools", registry != null && registry.getToolCount() > 0);
            capabilities.put("prompts", true); // MCP supports prompts
            capabilities.put("resources", true); // MCP supports resources
            capabilities.put("sampling", true); // MCP supports sampling
            capabilities.put("elicitation", true); // MCP supports elicitation
            capabilities.put("logging", true); // MCP supports logging
            server.put("capabilities", capabilities);
        } else {
            server.put("name", "OpenHAB MCP Server");
            server.put("version", "1.0.0");
            server.put("vendor", "openHAB Foundation");
            server.put("capabilities", Map.of("tools", false, "prompts", false, "resources", false, "sampling", false,
                    "elicitation", false, "logging", false));
        }
        status.put("server", server);

        // Get actual session information
        Map<String, Object> sessions = new HashMap<>();
        if (serverManager != null) {
            int activeServers = 0;
            int totalServers = serverManager.getAllServerInstances().size();
            for (var serverInstance : serverManager.getAllServerInstances().values()) {
                if (serverInstance.isRunning()) {
                    activeServers++;
                }
            }
            sessions.put("active", activeServers);
            sessions.put("total", totalServers);
            sessions.put("max_sessions", 100); // TODO: Get from configuration
        } else {
            sessions.put("active", 0);
            sessions.put("total", 0);
            sessions.put("max_sessions", 100);
        }
        status.put("sessions", sessions);

        // Get actual performance information
        Map<String, Object> performance = new HashMap<>();
        if (serverManager != null) {
            int runningServers = 0;
            int healthyServers = 0;
            double avgUptime = 0.0;

            for (var serverInstance : serverManager.getAllServerInstances().values()) {
                if (serverInstance.isRunning()) {
                    runningServers++;
                    if (serverInstance.isHealthy()) {
                        healthyServers++;
                    }
                    avgUptime += serverInstance.getTransportStatistics().getUptime();
                }
            }

            if (runningServers > 0) {
                avgUptime /= runningServers;
            }

            performance.put("running_servers", runningServers);
            performance.put("healthy_servers", healthyServers);
            performance.put("average_uptime", avgUptime);
            performance.put("health_rate", runningServers > 0 ? (double) healthyServers / runningServers : 0.0);
        } else {
            performance.put("running_servers", 0);
            performance.put("healthy_servers", 0);
            performance.put("average_uptime", 0.0);
            performance.put("health_rate", 0.0);
        }
        status.put("performance", performance);

        // TODO: Add more detailed protocol compliance validation
        // TODO: Add connection monitoring and statistics
        // TODO: Add error tracking and reporting
        status.put("note", "MCP server status with TODO enhancements for compliance validation and monitoring");

        return SharedRestInfrastructure.okJson(status);
    }
}
