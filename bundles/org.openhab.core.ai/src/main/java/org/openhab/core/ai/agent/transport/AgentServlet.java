package org.openhab.core.ai.agent.transport;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.auth.AuthenticationContext;
import org.openhab.core.ai.auth.AuthenticationManager;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.whiteboard.propertytypes.HttpWhiteboardServletName;
import org.osgi.service.http.whiteboard.propertytypes.HttpWhiteboardServletPattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import jakarta.servlet.Servlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * A2A Servlet component for openHAB HTTP server integration.
 * 
 * This servlet integrates the A2A (Agent-to-Agent) protocol with openHAB's HTTP server using
 * the OSGi HTTP Whiteboard pattern. It handles A2A protocol HTTP endpoints and provides
 * JSON-RPC over HTTP functionality.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
@Component(service = Servlet.class, immediate = true)
@HttpWhiteboardServletName("a2a-servlet")
@HttpWhiteboardServletPattern({ "/a2a/*", "/.well-known/*" })
public class AgentServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(AgentServlet.class);

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AtomicReference<A2ARequestHandler> requestHandler = new AtomicReference<>();

    private @Nullable AuthenticationManager authenticationManager;

    /**
     * Create a new A2A servlet instance.
     */
    public AgentServlet() {
        super();
        logger.debug("A2A Servlet created");
    }

    /**
     * Activate the servlet component.
     */
    @Activate
    public void activate() {
        logger.info("A2A Servlet activated - registering with openHAB HTTP server");
        initializeA2AServer();
    }

    /**
     * Deactivate the servlet component.
     */
    @Deactivate
    public void deactivate() {
        logger.info("A2A Servlet deactivated - unregistering from openHAB HTTP server");
        cleanupA2AServer();
    }

    /**
     * Set the authentication manager reference.
     * 
     * @param authenticationManager the authentication manager
     */
    @Reference
    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
        logger.debug("Authentication manager set for A2A servlet");
    }

    /**
     * Unset the authentication manager reference.
     * 
     * @param authenticationManager the authentication manager
     */
    public void unsetAuthenticationManager(@Nullable AuthenticationManager authenticationManager) {
        this.authenticationManager = null;
        logger.debug("Authentication manager unset for A2A servlet");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        logger.debug("A2A Servlet GET request: {}", request.getRequestURI());

        // Validate authentication context for A2A operations
        if (!validateA2AAuthentication(request, response, "GET")) {
            return;
        }

        String path = request.getRequestURI();
        if (path.endsWith("/.well-known/agent.json") || path.endsWith("/.well-known/agent-card.json")) {
            handleAgentCard(response);
        } else if (path.endsWith("/message/stream")) {
            handleMessageStream(response);
        } else if (path.endsWith("/health")) {
            handleHealthCheck(response);
        } else if (path.endsWith("/status")) {
            handleStatusCheck(response);
        } else {
            response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            response.getWriter().write("GET method not supported for this endpoint");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        logger.debug("A2A Servlet POST request: {}", request.getRequestURI());

        // Validate authentication context for A2A operations
        if (!validateA2AAuthentication(request, response, "POST")) {
            return;
        }

        String path = request.getRequestURI();
        if (path.endsWith("/message/send")) {
            handleMessageSend(request, response);
        } else if (path.endsWith("/task/get")) {
            handleTaskGet(request, response);
        } else if (path.endsWith("/task/cancel")) {
            handleTaskCancel(request, response);
        } else {
            response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            response.getWriter().write("POST method not supported for this endpoint");
        }
    }

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Allow OPTIONS requests for CORS preflight
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization, X-API-Key");
        response.setStatus(HttpServletResponse.SC_OK);
    }

    /**
     * Validate A2A authentication and permissions.
     * 
     * @param request the HTTP request
     * @param response the HTTP response
     * @param method the HTTP method
     * @return true if authentication and authorization pass
     */
    private boolean validateA2AAuthentication(HttpServletRequest request, HttpServletResponse response, String method) {
        try {
            // Extract authentication context from request attributes (set by ProtocolSecurityFilter)
            AuthenticationContext authContext = (AuthenticationContext) request.getAttribute("authenticationContext");
            if (authContext == null) {
                logger.warn("No authentication context found for A2A request: {} {}", method, request.getRequestURI());
                sendAuthenticationErrorResponse(response, "Authentication required for A2A operations");
                return false;
            }

            // Validate authentication context
            AuthenticationManager authManager = authenticationManager;
            if (authManager == null) {
                logger.warn("Authentication manager not available for A2A request validation");
                sendAuthenticationErrorResponse(response, "Authentication service unavailable");
                return false;
            }

            if (!authManager.validateContext(authContext)) {
                logger.warn("Invalid authentication context for A2A request: {} {} from principal: {}", method,
                        request.getRequestURI(), authContext.getPrincipalId());
                sendAuthenticationErrorResponse(response, "Invalid authentication context");
                return false;
            }

            // Check A2A-specific permissions based on request
            String requestURI = request.getRequestURI();
            if (method.equals("GET")) {
                // GET requests for agent card, health, status
                if (requestURI.contains("/.well-known/agent.json") || requestURI.contains("/health")
                        || requestURI.contains("/status")) {
                    if (!authManager.hasPermission(authContext.getPrincipalId(), "a2a:read", "a2a")) {
                        logger.warn("Permission denied for A2A read operation: {} from principal: {}", requestURI,
                                authContext.getPrincipalId());
                        sendAuthorizationErrorResponse(response, "Insufficient permissions for A2A read operation");
                        return false;
                    }
                }
            } else if (method.equals("POST")) {
                // POST requests for message sending, task operations
                if (requestURI.contains("/message/send")) {
                    if (!authManager.hasPermission(authContext.getPrincipalId(), "a2a:send", "a2a")) {
                        logger.warn("Permission denied for A2A send operation: {} from principal: {}", requestURI,
                                authContext.getPrincipalId());
                        sendAuthorizationErrorResponse(response, "Insufficient permissions for A2A send operation");
                        return false;
                    }
                } else if (requestURI.contains("/task/")) {
                    if (!authManager.hasPermission(authContext.getPrincipalId(), "a2a:task", "a2a")) {
                        logger.warn("Permission denied for A2A task operation: {} from principal: {}", requestURI,
                                authContext.getPrincipalId());
                        sendAuthorizationErrorResponse(response, "Insufficient permissions for A2A task operation");
                        return false;
                    }
                }
            }

            logger.debug("A2A authentication and authorization successful for {} {} from principal: {}", method,
                    requestURI, authContext.getPrincipalId());
            return true;
        } catch (IOException e) {
            logger.error("Error during A2A authentication validation", e);
            return false;
        }
    }

    /**
     * Send an authentication error response.
     * 
     * @param response the HTTP response
     * @param message the error message
     * @throws IOException if an I/O error occurs
     */
    private void sendAuthenticationErrorResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"" + message + "\",\"protocol\":\"a2a\"}");
    }

    /**
     * Send an authorization error response.
     * 
     * @param response the HTTP response
     * @param message the error message
     * @throws IOException if an I/O error occurs
     */
    private void sendAuthorizationErrorResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"" + message + "\",\"protocol\":\"a2a\"}");
    }

    /**
     * Handle health check request.
     * 
     * @param response the HTTP response
     * @throws IOException if an I/O error occurs
     */
    private void handleHealthCheck(HttpServletResponse response) throws IOException {
        ObjectNode healthStatus = objectMapper.createObjectNode();
        healthStatus.put("status", "healthy");
        healthStatus.put("protocol", "a2a");
        healthStatus.put("timestamp", System.currentTimeMillis());

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        response.getWriter().write(healthStatus.toString());
    }

    /**
     * Handle status check request.
     * 
     * @param response the HTTP response
     * @throws IOException if an I/O error occurs
     */
    private void handleStatusCheck(HttpServletResponse response) throws IOException {
        ObjectNode status = objectMapper.createObjectNode();
        status.put("status", "running");
        status.put("protocol", "a2a");
        status.put("version", "1.0.0");
        status.put("timestamp", System.currentTimeMillis());

        A2ARequestHandler handler = requestHandler.get();
        if (handler != null) {
            status.put("handlerActive", true);
        } else {
            status.put("handlerActive", false);
        }

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        response.getWriter().write(status.toString());
    }

    /**
     * Handle agent card request.
     * 
     * @param response the HTTP response
     * @throws IOException if an I/O error occurs
     */
    private void handleAgentCard(HttpServletResponse response) throws IOException {
        ObjectNode agentCard = objectMapper.createObjectNode();
        agentCard.put("name", "openHAB A2A Agent");
        agentCard.put("version", "1.0.0");
        agentCard.put("description", "openHAB Agent-to-Agent protocol implementation");
        agentCard.put("capabilities", "message_sending,task_management,skill_execution");

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        response.getWriter().write(agentCard.toString());
    }

    /**
     * Handle SSE message stream subscription.
     */
    private void handleMessageStream(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Connection", "keep-alive");
        response.setContentType("text/event-stream");

        // Initial handshake event: client connected
        response.getWriter().write(":ok\n\n");
        response.getWriter().write("event: ready\n");
        response.getWriter().write("data: {\"status\":\"ready\",\"protocol\":\"a2a\"}\n\n");
        response.getWriter().flush();

        // TODO: For now we end the stream immediately after initial signal. A future
        // update will keep the connection open and dispatch streaming events.
    }

    /**
     * Handle message send request.
     * 
     * @param request the HTTP request
     * @param response the HTTP response
     * @throws IOException if an I/O error occurs
     */
    private void handleMessageSend(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            // Read request body
            BufferedReader reader = request.getReader();
            StringBuilder body = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                body.append(line);
            }

            // Parse JSON request
            JsonNode requestNode = objectMapper.readTree(body.toString());
            String message = requestNode.get("message").asText();

            // Process message (placeholder implementation)
            ObjectNode result = objectMapper.createObjectNode();
            result.put("status", "success");
            result.put("message", "Message received: " + message);
            result.put("timestamp", System.currentTimeMillis());

            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json");
            response.getWriter().write(result.toString());

        } catch (Exception e) {
            sendErrorResponse(response, "Error processing message send request: " + e.getMessage(),
                    HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    /**
     * Handle task get request.
     * 
     * @param request the HTTP request
     * @param response the HTTP response
     * @throws IOException if an I/O error occurs
     */
    private void handleTaskGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            // Read request body
            BufferedReader reader = request.getReader();
            StringBuilder body = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                body.append(line);
            }

            // Parse JSON request
            JsonNode requestNode = objectMapper.readTree(body.toString());
            String taskId = requestNode.get("taskId").asText();

            // Process task get (placeholder implementation)
            ObjectNode result = objectMapper.createObjectNode();
            result.put("status", "success");
            result.put("taskId", taskId);
            result.put("taskStatus", "pending");
            result.put("timestamp", System.currentTimeMillis());

            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json");
            response.getWriter().write(result.toString());

        } catch (Exception e) {
            sendErrorResponse(response, "Error processing task get request: " + e.getMessage(),
                    HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    /**
     * Handle task cancel request.
     * 
     * @param request the HTTP request
     * @param response the HTTP response
     * @throws IOException if an I/O error occurs
     */
    private void handleTaskCancel(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            // Read request body
            BufferedReader reader = request.getReader();
            StringBuilder body = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                body.append(line);
            }

            // Parse JSON request
            JsonNode requestNode = objectMapper.readTree(body.toString());
            String taskId = requestNode.get("taskId").asText();

            // Process task cancel (placeholder implementation)
            ObjectNode result = objectMapper.createObjectNode();
            result.put("status", "success");
            result.put("taskId", taskId);
            result.put("taskStatus", "cancelled");
            result.put("timestamp", System.currentTimeMillis());

            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json");
            response.getWriter().write(result.toString());

        } catch (Exception e) {
            sendErrorResponse(response, "Error processing task cancel request: " + e.getMessage(),
                    HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    /**
     * Send an error response.
     * 
     * @param response the HTTP response
     * @param message the error message
     * @param statusCode the HTTP status code
     * @throws IOException if an I/O error occurs
     */
    private void sendErrorResponse(HttpServletResponse response, String message, int statusCode) throws IOException {
        ObjectNode errorResponse = objectMapper.createObjectNode();
        errorResponse.put("error", message);
        errorResponse.put("protocol", "a2a");
        errorResponse.put("timestamp", System.currentTimeMillis());

        response.setStatus(statusCode);
        response.setContentType("application/json");
        response.getWriter().write(errorResponse.toString());
    }

    /**
     * Initialize the A2A server.
     */
    private void initializeA2AServer() {
        try {
            // Initialize A2A request handler (placeholder implementation)
            A2ARequestHandler handler = new A2ARequestHandler() {
                // TODO: Implement actual A2A request handling
            };
            requestHandler.set(handler);

            logger.info("A2A server initialized successfully");
        } catch (Exception e) {
            logger.error("Failed to initialize A2A server", e);
        }
    }

    /**
     * Clean up the A2A server.
     */
    private void cleanupA2AServer() {
        try {
            A2ARequestHandler handler = requestHandler.getAndSet(null);
            if (handler != null) {
                // Cleanup handler resources if needed
                logger.debug("A2A request handler cleaned up");
            }

            logger.info("A2A server cleanup completed");
        } catch (Exception e) {
            logger.error("Error during A2A server cleanup", e);
        }
    }

    /**
     * Check if the servlet is healthy.
     * 
     * @return true if healthy
     */
    public boolean isHealthy() {
        A2ARequestHandler handler = requestHandler.get();
        return handler != null;
    }

    /**
     * Get server statistics.
     * 
     * @return server statistics
     */
    public ServerStatistics getServerStatistics() {
        A2ARequestHandler handler = requestHandler.get();
        return new ServerStatistics(handler != null, 4); // 4 endpoints: /message/send, /task/get, /task/cancel,
                                                         // /.well-known/agent.json
    }

    /**
     * Server statistics.
     */
    public static class ServerStatistics {
        private final boolean requestHandlerActive;
        private final int endpointCount;

        public ServerStatistics(boolean requestHandlerActive, int endpointCount) {
            this.requestHandlerActive = requestHandlerActive;
            this.endpointCount = endpointCount;
        }

        public boolean isRequestHandlerActive() {
            return requestHandlerActive;
        }

        public int getEndpointCount() {
            return endpointCount;
        }

        @Override
        public String toString() {
            return String.format("ServerStatistics{requestHandlerActive=%s, endpointCount=%d}", requestHandlerActive,
                    endpointCount);
        }
    }

    /**
     * A2A request handler interface.
     */
    public interface A2ARequestHandler {
        // TODO: Define A2A request handler methods
    }
}
