package org.openhab.core.ai.servlet;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.auth.AuthenticationContext;
import org.openhab.core.ai.auth.AuthenticationManager;
import org.openhab.core.ai.tool.registry.ToolRegistry;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.whiteboard.propertytypes.HttpWhiteboardServletName;
import org.osgi.service.http.whiteboard.propertytypes.HttpWhiteboardServletPattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.modelcontextprotocol.server.McpAsyncServer;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.HttpServletSseServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema.ServerCapabilities;
import jakarta.servlet.Servlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * MCP Servlet component for openHAB HTTP server integration.
 * 
 * This servlet integrates the MCP protocol with openHAB's HTTP server using
 * the OSGi HTTP Whiteboard pattern. It extends the MCP SDK's HttpServletSseServerTransportProvider
 * and registers itself as an OSGi service for automatic servlet registration.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
@Component(service = Servlet.class, immediate = true)
@HttpWhiteboardServletName("mcp-servlet")
@HttpWhiteboardServletPattern("/mcp/*")
public class ToolServlet extends HttpServletSseServerTransportProvider {

    private static final Logger logger = LoggerFactory.getLogger(ToolServlet.class);

    private final AtomicReference<McpSyncServer> syncServer = new AtomicReference<>();
    private final AtomicReference<McpAsyncServer> asyncServer = new AtomicReference<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private @Nullable ToolRegistry toolRegistry;
    private @Nullable AuthenticationManager authenticationManager;

    /**
     * Create a new MCP servlet instance.
     */
    public ToolServlet() {
        super(new ObjectMapper(), "/mcp/message", "/mcp/sse");
        logger.debug("MCP Servlet created");
    }

    /**
     * Activate the servlet component.
     */
    @Activate
    public void activate() {
        logger.info("MCP Servlet activated - registering with openHAB HTTP server");
        initializeMcpServer();
    }

    /**
     * Deactivate the servlet component.
     */
    @Deactivate
    public void deactivate() {
        logger.info("MCP Servlet deactivated - unregistering from openHAB HTTP server");
        cleanupMcpServer();
    }

    /**
     * Set the tool registry reference.
     * 
     * @param toolRegistry the tool registry
     */
    @Reference
    public void setToolRegistry(ToolRegistry toolRegistry) {
        this.toolRegistry = toolRegistry;
        logger.debug("Tool registry set for MCP servlet");
    }

    /**
     * Unset the tool registry reference.
     * 
     * @param toolRegistry the tool registry
     */
    public void unsetToolRegistry(@Nullable ToolRegistry toolRegistry) {
        this.toolRegistry = null;
        logger.debug("Tool registry unset for MCP servlet");
    }

    /**
     * Set the authentication manager reference.
     * 
     * @param authenticationManager the authentication manager
     */
    @Reference
    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
        logger.debug("Authentication manager set for MCP servlet");
    }

    /**
     * Unset the authentication manager reference.
     * 
     * @param authenticationManager the authentication manager
     */
    public void unsetAuthenticationManager(@Nullable AuthenticationManager authenticationManager) {
        this.authenticationManager = null;
        logger.debug("Authentication manager unset for MCP servlet");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Validate authentication context for MCP operations
        if (!validateMcpAuthentication(request, response, "GET")) {
            return;
        }

        super.doGet(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Validate authentication context for MCP operations
        if (!validateMcpAuthentication(request, response, "POST")) {
            return;
        }

        super.doPost(request, response);
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
     * Validate MCP authentication and permissions.
     * 
     * @param request the HTTP request
     * @param response the HTTP response
     * @param method the HTTP method
     * @return true if authentication and authorization pass
     */
    private boolean validateMcpAuthentication(HttpServletRequest request, HttpServletResponse response, String method) {
        try {
            // Extract authentication context from request attributes (set by ProtocolSecurityFilter)
            AuthenticationContext authContext = (AuthenticationContext) request.getAttribute("authenticationContext");
            if (authContext == null) {
                logger.warn("No authentication context found for MCP request: {} {}", method, request.getRequestURI());
                sendAuthenticationErrorResponse(response, "Authentication required for MCP operations");
                return false;
            }

            // Validate authentication context
            AuthenticationManager authManager = authenticationManager;
            if (authManager == null) {
                logger.warn("Authentication manager not available for MCP request validation");
                sendAuthenticationErrorResponse(response, "Authentication service unavailable");
                return false;
            }

            if (!authManager.validateContext(authContext)) {
                logger.warn("Invalid authentication context for MCP request: {} {} from principal: {}", method,
                        request.getRequestURI(), authContext.getPrincipalId());
                sendAuthenticationErrorResponse(response, "Invalid authentication context");
                return false;
            }

            // Check MCP-specific permissions based on request
            String requestURI = request.getRequestURI();
            if (method.equals("GET")) {
                // GET requests for MCP tools, resources, prompts
                if (requestURI.contains("/tools") || requestURI.contains("/resources")
                        || requestURI.contains("/prompts")) {
                    if (!authManager.hasPermission(authContext.getPrincipalId(), "mcp:read", "mcp")) {
                        logger.warn("Permission denied for MCP read operation: {} from principal: {}", requestURI,
                                authContext.getPrincipalId());
                        sendAuthorizationErrorResponse(response, "Insufficient permissions for MCP read operation");
                        return false;
                    }
                }
            } else if (method.equals("POST")) {
                // POST requests for MCP tool execution
                if (requestURI.contains("/tools") || requestURI.contains("/call")) {
                    if (!authManager.hasPermission(authContext.getPrincipalId(), "mcp:execute", "mcp")) {
                        logger.warn("Permission denied for MCP execute operation: {} from principal: {}", requestURI,
                                authContext.getPrincipalId());
                        sendAuthorizationErrorResponse(response, "Insufficient permissions for MCP execute operation");
                        return false;
                    }
                }
            }

            logger.debug("MCP authentication and authorization successful for {} {} from principal: {}", method,
                    requestURI, authContext.getPrincipalId());
            return true;
        } catch (IOException e) {
            logger.error("Error during MCP authentication validation", e);
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
        response.getWriter().write("{\"error\":\"" + message + "\",\"protocol\":\"mcp\"}");
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
        response.getWriter().write("{\"error\":\"" + message + "\",\"protocol\":\"mcp\"}");
    }

    /**
     * Initialize the MCP server.
     */
    private void initializeMcpServer() {
        ToolRegistry registry = toolRegistry;
        if (registry == null) {
            logger.warn("Tool registry not available for MCP server initialization");
            return;
        }

        try {
            // Create sync server
            McpSyncServer sync = createSyncServer(registry);
            syncServer.set(sync);

            // Create async server (optional)
            McpAsyncServer async = createAsyncServer(registry);
            asyncServer.set(async);

            logger.info("MCP server initialized successfully");
        } catch (Exception e) {
            logger.error("Failed to initialize MCP server", e);
        }
    }

    /**
     * Create a sync MCP server.
     * 
     * @param registry the tool registry
     * @return the sync server
     * @throws Exception if server creation fails
     */
    private McpSyncServer createSyncServer(ToolRegistry registry) throws Exception {
        ServerCapabilities capabilities = ServerCapabilities.builder().tools(true).resources(true, true).prompts(true)
                .build();

        McpSyncServer server = McpServer.sync(this).serverInfo("openhab-mcp-server", "1.0.0").capabilities(capabilities)
                .build();

        // Register tools
        McpServerFeatures.SyncToolSpecification[] toolSpecs = registry.getSyncToolSpecifications();
        for (McpServerFeatures.SyncToolSpecification toolSpec : toolSpecs) {
            server.addTool(toolSpec);
        }

        // Register resources
        McpServerFeatures.SyncResourceSpecification[] resourceSpecs = registry.getSyncResourceSpecifications();
        for (McpServerFeatures.SyncResourceSpecification resourceSpec : resourceSpecs) {
            server.addResource(resourceSpec);
        }

        // Register prompts
        McpServerFeatures.SyncPromptSpecification[] promptSpecs = registry.getSyncPromptSpecifications();
        for (McpServerFeatures.SyncPromptSpecification promptSpec : promptSpecs) {
            server.addPrompt(promptSpec);
        }

        logger.debug("MCP sync server created successfully with {} tools, {} resources, {} prompts", toolSpecs.length,
                resourceSpecs.length, promptSpecs.length);
        return server;
    }

    /**
     * Create an async MCP server.
     * 
     * @param registry the tool registry
     * @return the async server
     * @throws Exception if server creation fails
     */
    private McpAsyncServer createAsyncServer(ToolRegistry registry) throws Exception {
        ServerCapabilities capabilities = ServerCapabilities.builder().tools(true).resources(true, true).prompts(true)
                .build();

        McpAsyncServer server = McpServer.async(this).serverInfo("openhab-mcp-server-async", "1.0.0")
                .capabilities(capabilities).build();

        // Register tools
        McpServerFeatures.AsyncToolSpecification[] toolSpecs = registry.getAsyncToolSpecifications();
        for (McpServerFeatures.AsyncToolSpecification toolSpec : toolSpecs) {
            server.addTool(toolSpec).doOnSuccess(v -> logger.info("Async tool registered: {}", toolSpec.tool().name()))
                    .subscribe();
        }

        // Register resources
        McpServerFeatures.AsyncResourceSpecification[] resourceSpecs = registry.getAsyncResourceSpecifications();
        for (McpServerFeatures.AsyncResourceSpecification resourceSpec : resourceSpecs) {
            server.addResource(resourceSpec)
                    .doOnSuccess(v -> logger.info("Async resource registered: {}", resourceSpec.resource().name()))
                    .subscribe();
        }

        // Register prompts
        McpServerFeatures.AsyncPromptSpecification[] promptSpecs = registry.getAsyncPromptSpecifications();
        for (McpServerFeatures.AsyncPromptSpecification promptSpec : promptSpecs) {
            server.addPrompt(promptSpec)
                    .doOnSuccess(v -> logger.info("Async prompt registered: {}", promptSpec.prompt().name()))
                    .subscribe();
        }

        logger.debug("MCP async server created successfully with {} tools, {} resources, {} prompts", toolSpecs.length,
                resourceSpecs.length, promptSpecs.length);
        return server;
    }

    /**
     * Clean up the MCP server.
     */
    private void cleanupMcpServer() {
        McpSyncServer sync = syncServer.getAndSet(null);
        if (sync != null) {
            try {
                sync.close();
                logger.debug("MCP sync server closed");
            } catch (Exception e) {
                logger.warn("Error closing MCP sync server", e);
            }
        }

        McpAsyncServer async = asyncServer.getAndSet(null);
        if (async != null) {
            try {
                async.close();
                logger.debug("MCP async server closed");
            } catch (Exception e) {
                logger.warn("Error closing MCP async server", e);
            }
        }
    }

    /**
     * Check if the servlet is healthy.
     * 
     * @return true if healthy
     */
    public boolean isHealthy() {
        ToolRegistry registry = toolRegistry;
        McpSyncServer sync = syncServer.get();
        McpAsyncServer async = asyncServer.get();

        return registry != null && sync != null && async != null;
    }

    /**
     * Get server statistics.
     * 
     * @return server statistics
     */
    public ServerStatistics getServerStatistics() {
        ToolRegistry registry = toolRegistry;
        McpSyncServer sync = syncServer.get();
        McpAsyncServer async = asyncServer.get();

        return new ServerStatistics(sync != null, async != null, registry != null);
    }

    /**
     * Server statistics.
     */
    public static class ServerStatistics {
        private final boolean syncServerActive;
        private final boolean asyncServerActive;
        private final boolean toolRegistryAvailable;

        public ServerStatistics(boolean syncServerActive, boolean asyncServerActive, boolean toolRegistryAvailable) {
            this.syncServerActive = syncServerActive;
            this.asyncServerActive = asyncServerActive;
            this.toolRegistryAvailable = toolRegistryAvailable;
        }

        public boolean isSyncServerActive() {
            return syncServerActive;
        }

        public boolean isAsyncServerActive() {
            return asyncServerActive;
        }

        public boolean isToolRegistryAvailable() {
            return toolRegistryAvailable;
        }

        @Override
        public String toString() {
            return String.format(
                    "ServerStatistics{syncServerActive=%s, asyncServerActive=%s, toolRegistryAvailable=%s}",
                    syncServerActive, asyncServerActive, toolRegistryAvailable);
        }
    }
}
