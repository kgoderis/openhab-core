package org.openhab.core.ai.a2a.internal;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.action.AIActionRegistry;
import org.openhab.core.service.ReadyService;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.a2a.spec.AgentCard;
import io.a2a.spec.JSONRPCError;
import io.a2a.spec.MessageSendParams;
import io.a2a.spec.SendMessageRequest;
import io.a2a.spec.Task;
import io.a2a.spec.TaskIdParams;
import io.a2a.spec.TaskQueryParams;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

/**
 * REST endpoint for A2A operations.
 * 
 * @author AI Assistant
 * @since 1.0.0
 */
@NonNullByDefault
public class A2ARestEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(A2ARestEndpoint.class);

    @Reference
    private @Nullable AIActionRegistry actionRegistry;

    @Reference
    private @Nullable A2ASkillRegistry skillRegistry;

    @Reference
    private @Nullable BundleContext bundleContext;

    @Reference
    private @Nullable ReadyService readyService;

    private @Nullable A2AServerManager serverManager;

    /**
     * Initialize the server manager.
     */
    public void initialize() {
        if (serverManager != null) {
            return; // Already initialized
        }

        // Wait for A2A server to be ready
        if (readyService == null) {
            logger.error("ReadyService not available");
            throw new RuntimeException("ReadyService not available");
        }

        if (!readyService.isReady(A2AServerManager.A2A_SERVER_READY)) {
            logger.warn("A2A Server not ready - waiting for core services");
            throw new RuntimeException("A2A Server not ready - please wait for core services to initialize");
        }

        // Get the server manager service
        try {
            if (bundleContext == null) {
                logger.error("BundleContext not available");
                throw new RuntimeException("BundleContext not available");
            }

            serverManager = bundleContext.getService(bundleContext.getServiceReference(A2AServerManager.class));
            if (serverManager == null) {
                throw new RuntimeException("A2A Server Manager service not available");
            }
            logger.debug("A2A Server Manager initialized successfully");
        } catch (Exception e) {
            logger.error("Failed to initialize A2A Server Manager", e);
            throw new RuntimeException("Failed to initialize A2A Server Manager: " + e.getMessage(), e);
        }
    }

    /**
     * Get the OpenHAB agent card with capabilities.
     * 
     * @return agent card
     */
    @GET
    @Path("/agent-card")
    @Produces(MediaType.APPLICATION_JSON)
    public AgentCard getAgentCard() {
        logger.debug("GET /a2a/agent-card - Getting agent card");

        try {
            initialize();
            return serverManager.getAgentCard();
        } catch (Exception e) {
            logger.error("Error getting agent card", e);
            throw new RuntimeException("Failed to get agent card: " + e.getMessage(), e);
        }
    }

    /**
     * Send a message to the OpenHAB agent.
     * 
     * @param request the message send request
     * @return message send response
     */
    @POST
    @Path("/send-message")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> sendMessage(SendMessageRequest request) {
        logger.debug("POST /a2a/send-message - Sending message: {}", request);

        try {
            initialize();

            // Extract parameters from the request
            MessageSendParams params = request.getParams();

            // Process via request handler
            io.a2a.spec.EventKind response = serverManager.getRequestHandler().onMessageSend(params);

            // Create response
            return Map.of("taskId", "task-id-" + System.currentTimeMillis(), "status", "accepted", "response",
                    response != null ? response.getKind() : "unknown");

        } catch (JSONRPCError e) {
            logger.error("JSON-RPC error in send message", e);
            throw new RuntimeException("JSON-RPC Error: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Error sending message", e);
            throw new RuntimeException("Failed to send message: " + e.getMessage(), e);
        }
    }

    /**
     * Get task information.
     * 
     * @param taskId the task ID
     * @return task information
     */
    @GET
    @Path("/task")
    @Produces(MediaType.APPLICATION_JSON)
    public Task getTask(@QueryParam("taskId") String taskId) {
        logger.debug("GET /a2a/task - Getting task: {}", taskId);

        try {
            initialize();

            TaskQueryParams params = new TaskQueryParams(taskId);
            Task task = serverManager.getRequestHandler().onGetTask(params);

            if (task != null) {
                return task;
            } else {
                throw new RuntimeException("Task not found: " + taskId);
            }

        } catch (JSONRPCError e) {
            logger.error("JSON-RPC error in get task", e);
            throw new RuntimeException("JSON-RPC Error: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Error getting task", e);
            throw new RuntimeException("Failed to get task: " + e.getMessage(), e);
        }
    }

    /**
     * Cancel a task.
     * 
     * @param request the cancel task request
     * @return cancellation response
     */
    @POST
    @Path("/cancel-task")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> cancelTask(Map<String, String> request) {
        String taskId = request.get("taskId");
        logger.debug("POST /a2a/cancel-task - Cancelling task: {}", taskId);

        try {
            initialize();

            TaskIdParams params = new TaskIdParams(taskId);
            Task task = serverManager.getRequestHandler().onCancelTask(params);

            return Map.of("success", true, "taskId", taskId, "status", "cancelled");

        } catch (JSONRPCError e) {
            logger.error("JSON-RPC error in cancel task", e);
            throw new RuntimeException("JSON-RPC Error: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Error cancelling task", e);
            throw new RuntimeException("Failed to cancel task: " + e.getMessage(), e);
        }
    }

    /**
     * Health check endpoint.
     * 
     * @return health status
     */
    @GET
    @Path("/health")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> healthCheck() {
        logger.debug("GET /a2a/health - Health check");

        try {
            initialize();

            return Map.of("status", "healthy", "service", "OpenHAB A2A SDK Server", "version", "1.0.0", "timestamp",
                    System.currentTimeMillis());

        } catch (Exception e) {
            logger.error("Health check failed", e);
            throw new RuntimeException("Health check failed: " + e.getMessage(), e);
        }
    }
}
