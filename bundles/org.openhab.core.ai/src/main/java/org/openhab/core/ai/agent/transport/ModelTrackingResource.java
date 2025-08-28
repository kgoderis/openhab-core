package org.openhab.core.ai.agent.transport;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.agent.core.AgentClientSession;
import org.openhab.core.ai.model.ClientUsageInfo;
import org.openhab.core.ai.model.ModelTrackingService;
import org.openhab.core.ai.model.ProviderUsageStats;
import org.openhab.core.ai.model.SystemUsageStats;
import org.openhab.core.ai.model.api.ModelProviderType;
import org.openhab.core.ai.rest.SharedRestInfrastructure;
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
 * REST API for LLM Client Tracking
 * 
 * <p>
 * This resource provides REST endpoints for monitoring LLM client usage:
 * - Current client usage by agents
 * - Performance metrics and statistics
 * - Provider-specific analytics
 * - System-wide usage statistics
 * - Agent session information
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 */
@JaxrsResource
@JaxrsName("ai/llm-tracking")
@JaxrsApplicationSelect("(" + JaxrsWhiteboardConstants.JAX_RS_NAME + "=" + RESTConstants.JAX_RS_NAME + ")")
@JSONRequired
@Path("ai/llm-tracking")
@NonNullByDefault
@Component(service = RESTResource.class)
public class ModelTrackingResource implements RESTResource {

    @Reference
    private @Nullable ModelTrackingService trackingService;

    @GET
    @Path("status")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("service", "llm-client-tracking");
        status.put("status", "available");
        status.put("version", "1.0");
        status.put("timestamp", Instant.now().toString());

        ModelTrackingService service = trackingService;
        status.put("tracking_service", service != null ? "available" : "unavailable");

        return SharedRestInfrastructure.okJson(status);
    }

    @GET
    @Path("clients")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllClients() {
        ModelTrackingService service = trackingService;
        if (service == null) {
            return SharedRestInfrastructure.error(503, "LLM Client Tracking Service not available");
        }

        try {
            Map<String, ClientUsageInfo> clientUsage = service.getClientUsage();
            List<Map<String, Object>> clients = new ArrayList<>();

            for (Map.Entry<String, ClientUsageInfo> entry : clientUsage.entrySet()) {
                ClientUsageInfo usage = entry.getValue();
                Map<String, Object> client = new HashMap<>();
                client.put("client_key", entry.getKey());
                client.put("provider_type", usage.getProviderType().name());
                client.put("model_name", usage.getModelName());
                client.put("active_agents", usage.getActiveAgents().size());
                client.put("total_requests", usage.getTotalRequests());
                client.put("total_tokens", usage.getTotalTokens());
                client.put("total_cost", usage.getTotalCost());
                client.put("success_rate", usage.getSuccessRate());
                client.put("average_response_time", usage.getAverageResponseTime());
                client.put("last_used", usage.getLastUsed().toString());
                client.put("agent_ids", new ArrayList<>(usage.getActiveAgents().keySet()));
                clients.add(client);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("clients", clients);
            response.put("total", clients.size());
            response.put("timestamp", Instant.now().toString());

            return SharedRestInfrastructure.okJson(response);
        } catch (Exception e) {
            return SharedRestInfrastructure.error(500, "Error retrieving client usage: " + e.getMessage());
        }
    }

    @GET
    @Path("clients/{providerType}/{modelName}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getClientDetails(@PathParam("providerType") String providerTypeStr,
            @PathParam("modelName") String modelName) {
        ModelTrackingService service = trackingService;
        if (service == null) {
            return SharedRestInfrastructure.error(503, "LLM Client Tracking Service not available");
        }

        try {
            ModelProviderType providerType = ModelProviderType.valueOf(providerTypeStr.toUpperCase());
            ClientUsageInfo usage = service.getClientUsage(providerType, modelName);

            if (usage == null) {
                return SharedRestInfrastructure.error(404, "Client not found: " + providerType + "/" + modelName);
            }

            Map<String, Object> client = new HashMap<>();
            client.put("provider_type", usage.getProviderType().name());
            client.put("model_name", usage.getModelName());
            client.put("active_agents", usage.getActiveAgents().size());
            client.put("total_requests", usage.getTotalRequests());
            client.put("total_tokens", usage.getTotalTokens());
            client.put("total_cost", usage.getTotalCost());
            client.put("successful_requests", usage.getSuccessfulRequests());
            client.put("failed_requests", usage.getFailedRequests());
            client.put("success_rate", usage.getSuccessRate());
            client.put("average_response_time", usage.getAverageResponseTime());
            client.put("last_used", usage.getLastUsed().toString());
            client.put("agent_ids", new ArrayList<>(usage.getActiveAgents().keySet()));

            // Add performance metrics from centralized MetricsService
            Map<String, Object> performanceData = service.getClientPerformanceData(providerType, modelName);
            if (!performanceData.isEmpty()) {
                Map<String, Object> metrics = new HashMap<>();
                metrics.put("average_response_time", performanceData.get("averageResponseTime"));
                metrics.put("min_response_time", performanceData.get("minResponseTime"));
                metrics.put("max_response_time", performanceData.get("maxResponseTime"));
                metrics.put("error_rate", performanceData.get("errorRate"));
                metrics.put("total_errors", performanceData.get("totalErrors"));
                metrics.put("total_requests", performanceData.get("totalRequests"));
                metrics.put("successful_requests", performanceData.get("successfulRequests"));
                client.put("performance_metrics", metrics);
            }

            return SharedRestInfrastructure.okJson(client);
        } catch (IllegalArgumentException e) {
            return SharedRestInfrastructure.error(400, "Invalid provider type: " + providerTypeStr);
        } catch (Exception e) {
            return SharedRestInfrastructure.error(500, "Error retrieving client details: " + e.getMessage());
        }
    }

    @GET
    @Path("clients/{providerType}/{modelName}/agents")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getClientAgents(@PathParam("providerType") String providerTypeStr,
            @PathParam("modelName") String modelName) {
        ModelTrackingService service = trackingService;
        if (service == null) {
            return SharedRestInfrastructure.error(503, "LLM Client Tracking Service not available");
        }

        try {
            ModelProviderType providerType = ModelProviderType.valueOf(providerTypeStr.toUpperCase());
            List<String> agentIds = service.getClientAgents(providerType, modelName);

            Map<String, Object> response = new HashMap<>();
            response.put("provider_type", providerType.name());
            response.put("model_name", modelName);
            response.put("agent_ids", agentIds);
            response.put("count", agentIds.size());
            response.put("timestamp", Instant.now().toString());

            return SharedRestInfrastructure.okJson(response);
        } catch (IllegalArgumentException e) {
            return SharedRestInfrastructure.error(400, "Invalid provider type: " + providerTypeStr);
        } catch (Exception e) {
            return SharedRestInfrastructure.error(500, "Error retrieving client agents: " + e.getMessage());
        }
    }

    @GET
    @Path("agents")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllAgents() {
        ModelTrackingService service = trackingService;
        if (service == null) {
            return SharedRestInfrastructure.error(503, "LLM Client Tracking Service not available");
        }

        try {
            List<AgentClientSession> activeSessions = service.getActiveSessions();
            Map<String, List<Map<String, Object>>> agentSessions = new HashMap<>();

            for (AgentClientSession session : activeSessions) {
                String agentId = session.getAgentId();
                agentSessions.computeIfAbsent(agentId, k -> new ArrayList<>()).add(createSessionMap(session));
            }

            List<Map<String, Object>> agents = new ArrayList<>();
            for (Map.Entry<String, List<Map<String, Object>>> entry : agentSessions.entrySet()) {
                Map<String, Object> agent = new HashMap<>();
                agent.put("agent_id", entry.getKey());
                agent.put("active_sessions", entry.getValue().size());
                agent.put("sessions", entry.getValue());
                agents.add(agent);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("agents", agents);
            response.put("total", agents.size());
            response.put("timestamp", Instant.now().toString());

            return SharedRestInfrastructure.okJson(response);
        } catch (Exception e) {
            return SharedRestInfrastructure.error(500, "Error retrieving agents: " + e.getMessage());
        }
    }

    @GET
    @Path("agents/{agentId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAgentDetails(@PathParam("agentId") String agentId) {
        ModelTrackingService service = trackingService;
        if (service == null) {
            return SharedRestInfrastructure.error(503, "LLM Client Tracking Service not available");
        }

        try {
            List<AgentClientSession> sessions = service.getAgentSessions(agentId);
            List<ClientUsageInfo> clients = service.getAgentClients(agentId);

            Map<String, Object> agent = new HashMap<>();
            agent.put("agent_id", agentId);
            agent.put("active_sessions", sessions.stream().filter(s -> s.isActive()).count());
            agent.put("total_sessions", sessions.size());
            agent.put("active_clients", clients.size());

            List<Map<String, Object>> sessionMaps = sessions.stream().map(this::createSessionMap)
                    .collect(Collectors.toList());
            agent.put("sessions", sessionMaps);

            List<Map<String, Object>> clientMaps = clients.stream().map(this::createClientUsageMap)
                    .collect(Collectors.toList());
            agent.put("clients", clientMaps);

            return SharedRestInfrastructure.okJson(agent);
        } catch (Exception e) {
            return SharedRestInfrastructure.error(500, "Error retrieving agent details: " + e.getMessage());
        }
    }

    @GET
    @Path("providers")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllProviders() {
        ModelTrackingService service = trackingService;
        if (service == null) {
            return SharedRestInfrastructure.error(503, "LLM Client Tracking Service not available");
        }

        try {
            Map<ModelProviderType, ProviderUsageStats> providerStats = service.getProviderStats();
            List<Map<String, Object>> providers = new ArrayList<>();

            for (Map.Entry<ModelProviderType, ProviderUsageStats> entry : providerStats.entrySet()) {
                ProviderUsageStats stats = entry.getValue();
                Map<String, Object> provider = new HashMap<>();
                provider.put("provider_type", entry.getKey().name());
                provider.put("display_name", entry.getKey().getDisplayName());
                provider.put("total_requests", stats.getTotalRequests());
                provider.put("total_tokens", stats.getTotalTokens());
                provider.put("total_cost", stats.getTotalCost());
                provider.put("success_rate", stats.getSuccessRate());
                provider.put("average_response_time", stats.getAverageResponseTime());
                provider.put("model_usage", stats.getModelUsage());
                provider.put("agent_usage", stats.getAgentUsage());
                providers.add(provider);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("providers", providers);
            response.put("total", providers.size());
            response.put("timestamp", Instant.now().toString());

            return SharedRestInfrastructure.okJson(response);
        } catch (Exception e) {
            return SharedRestInfrastructure.error(500, "Error retrieving provider stats: " + e.getMessage());
        }
    }

    @GET
    @Path("providers/{providerType}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProviderDetails(@PathParam("providerType") String providerTypeStr) {
        ModelTrackingService service = trackingService;
        if (service == null) {
            return SharedRestInfrastructure.error(503, "LLM Client Tracking Service not available");
        }

        try {
            ModelProviderType providerType = ModelProviderType.valueOf(providerTypeStr.toUpperCase());
            Map<ModelProviderType, ProviderUsageStats> providerStats = service.getProviderStats();
            ProviderUsageStats stats = providerStats.get(providerType);

            if (stats == null) {
                return SharedRestInfrastructure.error(404, "Provider not found: " + providerType);
            }

            Map<String, Object> provider = new HashMap<>();
            provider.put("provider_type", providerType.name());
            provider.put("display_name", providerType.getDisplayName());
            provider.put("is_cloud_provider", providerType.isCloudProvider());
            provider.put("is_local_provider", providerType.isLocalProvider());
            provider.put("total_requests", stats.getTotalRequests());
            provider.put("total_tokens", stats.getTotalTokens());
            provider.put("total_cost", stats.getTotalCost());
            provider.put("successful_requests", stats.getSuccessfulRequests());
            provider.put("failed_requests", stats.getFailedRequests());
            provider.put("success_rate", stats.getSuccessRate());
            provider.put("average_response_time", stats.getAverageResponseTime());
            provider.put("model_usage", stats.getModelUsage());
            provider.put("agent_usage", stats.getAgentUsage());

            return SharedRestInfrastructure.okJson(provider);
        } catch (IllegalArgumentException e) {
            return SharedRestInfrastructure.error(400, "Invalid provider type: " + providerTypeStr);
        } catch (Exception e) {
            return SharedRestInfrastructure.error(500, "Error retrieving provider details: " + e.getMessage());
        }
    }

    @GET
    @Path("system/stats")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSystemStats() {
        ModelTrackingService service = trackingService;
        if (service == null) {
            return SharedRestInfrastructure.error(503, "LLM Client Tracking Service not available");
        }

        try {
            SystemUsageStats stats = service.getSystemStats();

            Map<String, Object> systemStats = new HashMap<>();
            systemStats.put("total_requests", stats.getTotalRequests());
            systemStats.put("total_tokens", stats.getTotalTokens());
            systemStats.put("total_cost", stats.getTotalCost());
            systemStats.put("average_response_time", stats.getAverageResponseTime());
            systemStats.put("error_rate", stats.getErrorRate());
            systemStats.put("last_request_time", stats.getLastRequestTime().toString());
            systemStats.put("timestamp", Instant.now().toString());

            return SharedRestInfrastructure.okJson(systemStats);
        } catch (Exception e) {
            return SharedRestInfrastructure.error(500, "Error retrieving system stats: " + e.getMessage());
        }
    }

    @GET
    @Path("system/health")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSystemHealth() {
        ModelTrackingService service = trackingService;
        if (service == null) {
            return SharedRestInfrastructure.error(503, "LLM Client Tracking Service not available");
        }

        try {
            SystemUsageStats stats = service.getSystemStats();
            Map<String, ClientUsageInfo> clientUsage = service.getClientUsage();
            List<AgentClientSession> activeSessions = service.getActiveSessions();

            Map<String, Object> health = new HashMap<>();
            health.put("status", "healthy");
            health.put("timestamp", Instant.now().toString());
            health.put("active_clients", clientUsage.size());
            health.put("active_sessions", activeSessions.size());
            health.put("total_requests", stats.getTotalRequests());
            health.put("error_rate", stats.getErrorRate());
            health.put("average_response_time", stats.getAverageResponseTime());

            // Check for potential issues
            List<String> warnings = new ArrayList<>();
            if (stats.getErrorRate() > 0.1) {
                warnings.add("High error rate detected: " + (stats.getErrorRate() * 100) + "%");
            }
            if (stats.getAverageResponseTime() > 10000) {
                warnings.add("High average response time: " + stats.getAverageResponseTime() + "ms");
            }
            if (activeSessions.isEmpty()) {
                warnings.add("No active LLM client sessions");
            }

            health.put("warnings", warnings);
            health.put("has_warnings", !warnings.isEmpty());

            return SharedRestInfrastructure.okJson(health);
        } catch (Exception e) {
            return SharedRestInfrastructure.error(500, "Error retrieving system health: " + e.getMessage());
        }
    }

    // Helper methods

    private Map<String, Object> createSessionMap(AgentClientSession session) {
        Map<String, Object> sessionMap = new HashMap<>();
        sessionMap.put("agent_id", session.getAgentId());
        sessionMap.put("provider_type", session.getProviderType().name());
        sessionMap.put("model_name", session.getModelName());
        sessionMap.put("session_id", session.getSessionId());
        sessionMap.put("last_used", session.getLastUsed().toString());
        sessionMap.put("is_active", session.isActive());
        sessionMap.put("session_duration_seconds", session.getSessionDuration().getSeconds());
        return sessionMap;
    }

    private Map<String, Object> createClientUsageMap(ClientUsageInfo usage) {
        Map<String, Object> clientMap = new HashMap<>();
        clientMap.put("provider_type", usage.getProviderType().name());
        clientMap.put("model_name", usage.getModelName());
        clientMap.put("active_agents", usage.getActiveAgents().size());
        clientMap.put("total_requests", usage.getTotalRequests());
        clientMap.put("total_tokens", usage.getTotalTokens());
        clientMap.put("total_cost", usage.getTotalCost());
        clientMap.put("success_rate", usage.getSuccessRate());
        clientMap.put("average_response_time", usage.getAverageResponseTime());
        clientMap.put("last_used", usage.getLastUsed().toString());
        return clientMap;
    }
}
