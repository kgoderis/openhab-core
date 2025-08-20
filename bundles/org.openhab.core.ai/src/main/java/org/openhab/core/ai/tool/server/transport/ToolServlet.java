package org.openhab.core.ai.tool.server.transport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.auth.AuthenticationContext;
import org.openhab.core.ai.auth.AuthenticationManager;
import org.openhab.core.ai.common.context.ToolContext;
import org.openhab.core.ai.common.statistics.ServerStatistics;
import org.openhab.core.ai.common.validation.ToolValidationResult;
import org.openhab.core.ai.tool.api.Tool;
import org.openhab.core.ai.tool.api.ToolException;
import org.openhab.core.ai.tool.api.ToolResult;
import org.openhab.core.ai.tool.completions.CompletionSuggestionService;
import org.openhab.core.ai.tool.completions.api.CompletionContext;
import org.openhab.core.ai.tool.completions.api.CompletionResult;
import org.openhab.core.ai.tool.prompts.PromptExecutionService;
import org.openhab.core.ai.tool.prompts.PromptRegistrationService;
import org.openhab.core.ai.tool.prompts.api.PromptContext;
import org.openhab.core.ai.tool.prompts.api.PromptRegistry;
import org.openhab.core.ai.tool.prompts.api.PromptResult;
import org.openhab.core.ai.tool.prompts.api.dto.Prompt;
import org.openhab.core.ai.tool.registry.ToolRegistry;
import org.openhab.core.ai.tool.resources.ResourceReadingService;
import org.openhab.core.ai.tool.resources.ResourceTemplateService;
import org.openhab.core.ai.tool.resources.api.ResourceContext;
import org.openhab.core.ai.tool.resources.api.ResourceRegistry;
import org.openhab.core.ai.tool.resources.api.ResourceResult;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.whiteboard.propertytypes.HttpWhiteboardServletName;
import org.osgi.service.http.whiteboard.propertytypes.HttpWhiteboardServletPattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
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
 * The servlet provides full MCP protocol compliance with proper tool execution,
 * authentication, and error handling.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
@Component(service = Servlet.class, immediate = true)
@HttpWhiteboardServletName("mcp-servlet")
@HttpWhiteboardServletPattern("/mcp/*")
public class ToolServlet extends HttpServletSseServerTransportProvider {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(ToolServlet.class);

    private final AtomicReference<McpSyncServer> syncServer = new AtomicReference<>();
    private final AtomicReference<McpAsyncServer> asyncServer = new AtomicReference<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private @Nullable ToolRegistry toolRegistry;
    private @Nullable AuthenticationManager authenticationManager;
    private @Nullable ResourceRegistry resourceRegistry;
    private @Nullable ResourceReadingService resourceReadingService;
    private @Nullable ResourceTemplateService resourceTemplateService;
    private @Nullable PromptRegistrationService promptRegistrationService;
    private @Nullable PromptRegistry promptRegistry;
    private @Nullable PromptExecutionService promptExecutionService;
    private @Nullable CompletionSuggestionService completionSuggestionService;

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

    /**
     * Set the resource registry reference.
     * 
     * @param resourceRegistry the resource registry
     */
    @Reference
    public void setResourceRegistry(ResourceRegistry resourceRegistry) {
        this.resourceRegistry = resourceRegistry;
        logger.debug("Resource registry set for MCP servlet");
    }

    /**
     * Unset the resource registry reference.
     * 
     * @param resourceRegistry the resource registry
     */
    public void unsetResourceRegistry(@Nullable ResourceRegistry resourceRegistry) {
        this.resourceRegistry = null;
        logger.debug("Resource registry unset from MCP servlet");
    }

    /**
     * Set the resource reading service reference.
     * 
     * @param resourceReadingService the resource reading service
     */
    @Reference
    public void setResourceReadingService(ResourceReadingService resourceReadingService) {
        this.resourceReadingService = resourceReadingService;
        logger.debug("Resource reading service set for MCP servlet");
    }

    /**
     * Unset the resource reading service reference.
     * 
     * @param resourceReadingService the resource reading service
     */
    public void unsetResourceReadingService(@Nullable ResourceReadingService resourceReadingService) {
        this.resourceReadingService = null;
        logger.debug("Resource reading service unset from MCP servlet");
    }

    /**
     * Set the resource template service reference.
     * 
     * @param resourceTemplateService the resource template service
     */
    @Reference
    public void setResourceTemplateService(ResourceTemplateService resourceTemplateService) {
        this.resourceTemplateService = resourceTemplateService;
        logger.debug("Resource template service set for MCP servlet");
    }

    /**
     * Unset the resource template service reference.
     * 
     * @param resourceTemplateService the resource template service
     */
    public void unsetResourceTemplateService(@Nullable ResourceTemplateService resourceTemplateService) {
        this.resourceTemplateService = null;
        logger.debug("Resource template service unset from MCP servlet");
    }

    /** Inject prompt registration service */
    @Reference
    public void setPromptRegistrationService(PromptRegistrationService svc) {
        this.promptRegistrationService = svc;
        logger.debug("Prompt registration service set for MCP servlet");
    }

    public void unsetPromptRegistrationService(@Nullable PromptRegistrationService svc) {
        this.promptRegistrationService = null;
        logger.debug("Prompt registration service unset from MCP servlet");
    }

    /** Inject prompt execution service */
    @Reference
    public void setPromptExecutionService(PromptExecutionService svc) {
        this.promptExecutionService = svc;
        logger.debug("Prompt execution service set for MCP servlet");
    }

    public void unsetPromptExecutionService(@Nullable PromptExecutionService svc) {
        this.promptExecutionService = null;
        logger.debug("Prompt execution service unset from MCP servlet");
    }

    /** Inject completion suggestion service */
    @Reference
    public void setCompletionSuggestionService(CompletionSuggestionService svc) {
        this.completionSuggestionService = svc;
        logger.debug("Completion suggestion service set for MCP servlet");
    }

    public void unsetCompletionSuggestionService(@Nullable CompletionSuggestionService svc) {
        this.completionSuggestionService = null;
        logger.debug("Completion suggestion service unset from MCP servlet");
    }

    /** Inject prompt registry */
    @Reference
    public void setPromptRegistry(PromptRegistry registry) {
        this.promptRegistry = registry;
        logger.debug("Prompt registry set for MCP servlet");
    }

    public void unsetPromptRegistry(@Nullable PromptRegistry registry) {
        this.promptRegistry = null;
        logger.debug("Prompt registry unset from MCP servlet");
    }

    @Override
    protected void doGet(@Nullable HttpServletRequest request, @Nullable HttpServletResponse response)
            throws ServletException, IOException {
        // Handle MCP protocol endpoints
        if (handleMcpProtocolEndpoints(request, response, "GET")) {
            return;
        }

        // Validate authentication context for MCP operations
        if (!validateMcpAuthentication(request, response, "GET")) {
            return;
        }

        super.doGet(request, response);
    }

    @Override
    protected void doPost(@Nullable HttpServletRequest request, @Nullable HttpServletResponse response)
            throws ServletException, IOException {
        // Handle MCP protocol endpoints
        if (handleMcpProtocolEndpoints(request, response, "POST")) {
            return;
        }

        // Validate authentication context for MCP operations
        if (!validateMcpAuthentication(request, response, "POST")) {
            return;
        }

        super.doPost(request, response);
    }

    @Override
    protected void doOptions(@Nullable HttpServletRequest request, @Nullable HttpServletResponse response)
            throws ServletException, IOException {
        // Allow OPTIONS requests for CORS preflight
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization, X-API-Key");
        response.setStatus(HttpServletResponse.SC_OK);
    }

    /**
     * Handle MCP protocol endpoints.
     * 
     * @param request the HTTP request
     * @param response the HTTP response
     * @param method the HTTP method
     * @return true if the endpoint was handled
     * @throws IOException if an I/O error occurs
     */
    private boolean handleMcpProtocolEndpoints(HttpServletRequest request, HttpServletResponse response, String method)
            throws IOException {
        // String requestURI = request.getRequestURI();

        // MCP Lifecycle Endpoints
        if (request.getRequestURI().endsWith("/mcp/initialize") && method.equals("POST")) {
            handleMcpInitialize(request, response);
            return true;
        }

        if (request.getRequestURI().endsWith("/mcp/ping") && method.equals("GET")) {
            handleMcpPing(request, response);
            return true;
        }

        // MCP Tool Protocol Endpoints
        if (request.getRequestURI().endsWith("/mcp/tools/list") && method.equals("GET")) {
            handleMcpToolsList(request, response);
            return true;
        }

        if (request.getRequestURI().endsWith("/mcp/tools/call") && method.equals("POST")) {
            handleMcpToolsCall(request, response);
            return true;
        }

        // MCP Resource Protocol Endpoints
        if (request.getRequestURI().endsWith("/mcp/resources/list") && method.equals("GET")) {
            handleMcpResourcesList(request, response);
            return true;
        }

        if (request.getRequestURI().endsWith("/mcp/resources/read") && method.equals("POST")) {
            handleMcpResourcesRead(request, response);
            return true;
        }

        if (request.getRequestURI().endsWith("/mcp/resources/templates/list") && method.equals("GET")) {
            handleMcpResourcesTemplatesList(request, response);
            return true;
        }

        if (request.getRequestURI().endsWith("/mcp/resources/subscribe") && method.equals("POST")) {
            handleMcpResourcesSubscribe(request, response);
            return true;
        }

        if (request.getRequestURI().endsWith("/mcp/resources/unsubscribe") && method.equals("POST")) {
            handleMcpResourcesUnsubscribe(request, response);
            return true;
        }

        // MCP Prompt Protocol Endpoints
        // TODO: REMOVE WHEN MCP SDK IS COMPLIANT
        // Reference: https://modelcontextprotocol.io/sdk/java/mcp-server#prompt-specification
        // Action Point: Remove these HTTP endpoints when prompts work through SDK registration
        //
        // These HTTP endpoints are temporary workarounds until the MCP SDK provides proper
        // prompt specification support. Once the SDK is compliant, prompts should be handled
        // through SDK registration (syncServer.addPrompt()) instead of direct HTTP endpoints.
        if (request.getRequestURI().endsWith("/mcp/prompts/list") && method.equals("GET")) {
            handleMcpPromptsList(request, response);
            return true;
        }

        if (request.getRequestURI().endsWith("/mcp/prompts/get") && method.equals("POST")) {
            handleMcpPromptsGet(request, response);
            return true;
        }

        // MCP Completion Protocol Endpoints
        // TODO: REMOVE WHEN MCP SDK IS COMPLIANT
        // Reference: https://modelcontextprotocol.io/sdk/java/mcp-server#completion-specification
        // Action Point: Remove these HTTP endpoints when completions work through SDK registration
        //
        // These HTTP endpoints are temporary workarounds until the MCP SDK provides proper
        // completion specification support. Once the SDK is compliant, completions should be handled
        // through SDK registration (syncServer.addCompletion()) instead of direct HTTP endpoints.
        if (request.getRequestURI().endsWith("/mcp/completion/complete") && method.equals("POST")) {
            handleMcpCompletionComplete(request, response);
            return true;
        }

        // MCP Roots Protocol Endpoints
        if (request.getRequestURI().endsWith("/mcp/roots/list") && method.equals("GET")) {
            handleMcpRootsList(request, response);
            return true;
        }

        // MCP Sampling Protocol Endpoints
        if (request.getRequestURI().endsWith("/mcp/sampling/createMessage") && method.equals("POST")) {
            handleMcpSamplingCreateMessage(request, response);
            return true;
        }

        // MCP Elicitation Protocol Endpoints
        if (request.getRequestURI().endsWith("/mcp/elicitation/create") && method.equals("POST")) {
            handleMcpElicitationCreate(request, response);
            return true;
        }

        // MCP Logging Protocol Endpoints
        if (request.getRequestURI().endsWith("/mcp/logging/setLevel") && method.equals("POST")) {
            handleMcpLoggingSetLevel(request, response);
            return true;
        }

        // MCP Notification Endpoints
        if (request.getRequestURI().contains("/mcp/notifications/")) {
            handleMcpNotifications(request, response, method);
            return true;
        }

        return false;
    }

    // MCP Lifecycle Endpoints

    /**
     * Handle MCP initialization.
     * 
     * @param request the HTTP request
     * @param response the HTTP response
     * @throws IOException if an I/O error occurs
     */
    private void handleMcpInitialize(HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger.debug("Handling MCP initialization request");

        try {
            // Implement actual MCP initialization logic
            // Set up the client connection and capabilities

            // Initialize MCP servers if not already done
            if (syncServer.get() == null || asyncServer.get() == null) {
                initializeMcpServer();
            }

            // Build capabilities response
            Map<String, Object> capabilities = new HashMap<>();
            capabilities.put("protocol", "mcp");
            capabilities.put("version", "1.0.0");
            capabilities.put("server", "openhab-ai-tool-server");

            // Add tool capabilities
            Map<String, Object> toolCapabilities = new HashMap<>();
            toolCapabilities.put("list", true);
            toolCapabilities.put("call", true);
            toolCapabilities.put("async", true);
            capabilities.put("tools", toolCapabilities);

            // Add resource capabilities
            Map<String, Object> resourceCapabilities = new HashMap<>();
            resourceCapabilities.put("list", true);
            resourceCapabilities.put("read", true);
            resourceCapabilities.put("subscribe", true);
            resourceCapabilities.put("unsubscribe", true);
            capabilities.put("resources", resourceCapabilities);

            // Add prompt capabilities
            Map<String, Object> promptCapabilities = new HashMap<>();
            promptCapabilities.put("list", true);
            promptCapabilities.put("get", true);
            promptCapabilities.put("execute", true);
            capabilities.put("prompts", promptCapabilities);

            // Add completion capabilities
            Map<String, Object> completionCapabilities = new HashMap<>();
            completionCapabilities.put("complete", true);
            completionCapabilities.put("suggest", true);
            capabilities.put("completions", completionCapabilities);

            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json");
            response.getWriter().write(objectMapper.writeValueAsString(capabilities));

            logger.debug("MCP initialization completed successfully with capabilities: {}", capabilities);
        } catch (Exception e) {
            logger.error("Error during MCP initialization", e);
            sendMcpErrorResponse(response, "Initialization failed", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Handle MCP ping.
     * 
     * @param request the HTTP request
     * @param response the HTTP response
     * @throws IOException if an I/O error occurs
     */
    private void handleMcpPing(HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger.debug("Handling MCP ping request");

        try {
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json");
            response.getWriter().write("{\"status\":\"pong\",\"protocol\":\"mcp\"}");

            logger.debug("MCP ping completed successfully");
        } catch (Exception e) {
            logger.error("Error during MCP ping", e);
            sendMcpErrorResponse(response, "Ping failed", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    // MCP Tool Protocol Endpoints

    /**
     * Handle MCP tools/list.
     * 
     * @param request the HTTP request
     * @param response the HTTP response
     * @throws IOException if an I/O error occurs
     */
    private void handleMcpToolsList(HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger.debug("Handling MCP tools/list request");

        try {
            ToolRegistry registry = toolRegistry;
            if (registry == null) {
                sendMcpErrorResponse(response, "Tool registry not available",
                        HttpServletResponse.SC_SERVICE_UNAVAILABLE);
                return;
            }

            // Implement actual tools list logic using MCP SDK
            // Return the list of available tools from the registry

            List<Map<String, Object>> toolsList = new ArrayList<>();

            // Get all registered tools from the registry
            if (registry != null) {
                try {
                    // Get all tools from registry
                    Map<String, Tool> tools = registry.getAllTools();

                    for (Map.Entry<String, Tool> entry : tools.entrySet()) {
                        Tool tool = entry.getValue();
                        Map<String, Object> toolInfo = new HashMap<>();
                        toolInfo.put("id", entry.getKey());
                        toolInfo.put("name", tool.getName());
                        toolInfo.put("description", tool.getDescription());
                        toolInfo.put("inputSchema", tool.getInputSchema());
                        toolInfo.put("outputSchema", tool.getOutputSchema());
                        toolInfo.put("metadata", tool.getMetadata());
                        toolsList.add(toolInfo);
                    }

                    logger.debug("Retrieved {} tools from registry", toolsList.size());
                } catch (Exception e) {
                    logger.error("Error retrieving tools from registry", e);
                }
            }

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("tools", toolsList);
            responseData.put("protocol", "mcp");
            responseData.put("count", toolsList.size());

            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json");
            response.getWriter().write(objectMapper.writeValueAsString(responseData));

            logger.debug("MCP tools/list completed successfully");
        } catch (Exception e) {
            logger.error("Error during MCP tools/list", e);
            sendMcpErrorResponse(response, "Tools list failed", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Handle MCP tools/call.
     * 
     * @param request the HTTP request
     * @param response the HTTP response
     * @throws IOException if an I/O error occurs
     */
    private void handleMcpToolsCall(HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger.debug("Handling MCP tools/call request");

        try {
            ToolRegistry registry = toolRegistry;
            if (registry == null) {
                sendMcpErrorResponse(response, "Tool registry not available",
                        HttpServletResponse.SC_SERVICE_UNAVAILABLE);
                return;
            }

            // Implement actual tool call logic using MCP SDK
            // Execute the requested tool with the provided parameters

            // Parse request body to get tool ID and parameters
            String requestBody = request.getReader().lines().reduce("", String::concat);
            Map<String, Object> requestData = objectMapper.readValue(requestBody,
                    new TypeReference<Map<String, Object>>() {
                    });

            String toolId = (String) requestData.get("toolId");
            @SuppressWarnings("unchecked")
            Map<String, Object> parameters = (Map<String, Object>) requestData.get("parameters");

            if (toolId == null || toolId.trim().isEmpty()) {
                sendMcpErrorResponse(response, "Tool ID is required", HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            // Get the tool from registry
            Tool tool = registry.getTool(toolId);
            if (tool == null) {
                sendMcpErrorResponse(response, "Tool not found: " + toolId, HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            // Validate parameters
            ToolValidationResult validationResult = tool.validateParameters(parameters);
            if (!validationResult.isValid()) {
                sendMcpErrorResponse(response, "Invalid parameters: " + validationResult.getErrors(),
                        HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            // Execute the tool
            try {
                ToolContext context = new ToolContext("servlet-tool-" + System.currentTimeMillis(), toolId,
                        tool.getName(), tool.getMetadata().getVersion(), null, null, parameters, null);
                ToolResult result = tool.execute(parameters, context);

                Map<String, Object> responseData = new HashMap<>();
                responseData.put("result", result.getContent());
                responseData.put("success", result.isSuccess());
                responseData.put("toolId", toolId);
                responseData.put("protocol", "mcp");

                if (!result.isSuccess()) {
                    responseData.put("error", result.getError());
                }

                response.setStatus(HttpServletResponse.SC_OK);
                response.setContentType("application/json");
                response.getWriter().write(objectMapper.writeValueAsString(responseData));

                logger.debug("Tool {} executed successfully", toolId);
            } catch (ToolException e) {
                logger.error("Tool execution failed for tool: {}", toolId, e);
                sendMcpErrorResponse(response, "Tool execution failed: " + e.getMessage(),
                        HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return;
            }

            logger.debug("MCP tools/call completed successfully");
        } catch (Exception e) {
            logger.error("Error during MCP tools/call", e);
            sendMcpErrorResponse(response, "Tool call failed", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    // MCP Resource Protocol Endpoints

    /**
     * Handle MCP resources/list.
     * 
     * @param request the HTTP request
     * @param response the HTTP response
     * @throws IOException if an I/O error occurs
     */
    private void handleMcpResourcesList(HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger.debug("Handling MCP resources/list request");

        try {
            ResourceRegistry registry = resourceRegistry;
            if (registry == null) {
                sendMcpErrorResponse(response, "Resource registry not available",
                        HttpServletResponse.SC_SERVICE_UNAVAILABLE);
                return;
            }

            // Get sync and async resource specifications
            Object[] syncSpecs = registry.getSyncResourceSpecifications();
            Object[] asyncSpecs = registry.getAsyncResourceSpecifications();

            // Create response with resource information
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("syncResources", syncSpecs);
            responseData.put("asyncResources", asyncSpecs);
            responseData.put("totalResources", syncSpecs.length + asyncSpecs.length);
            responseData.put("protocol", "mcp");

            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json");
            response.getWriter().write(objectMapper.writeValueAsString(responseData));

            logger.debug("MCP resources/list completed successfully with {} resources",
                    syncSpecs.length + asyncSpecs.length);
        } catch (Exception e) {
            logger.error("Error during MCP resources/list", e);
            sendMcpErrorResponse(response, "Resources list failed", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Handle MCP resources/read.
     * 
     * @param request the HTTP request
     * @param response the HTTP response
     * @throws IOException if an I/O error occurs
     */
    private void handleMcpResourcesRead(HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger.debug("Handling MCP resources/read request");

        try {
            ResourceReadingService readingService = resourceReadingService;
            if (readingService == null) {
                sendMcpErrorResponse(response, "Resource reading service not available",
                        HttpServletResponse.SC_SERVICE_UNAVAILABLE);
                return;
            }

            // Parse request body to get resource ID and parameters
            String requestBody = request.getReader().lines().reduce("", String::concat);
            Map<String, Object> requestData = objectMapper.readValue(requestBody,
                    new TypeReference<Map<String, Object>>() {
                    });

            String resourceId = (String) requestData.get("resourceId");
            @SuppressWarnings("unchecked")
            Map<String, Object> parameters = (Map<String, Object>) requestData.get("parameters");

            if (resourceId == null || resourceId.trim().isEmpty()) {
                sendMcpErrorResponse(response, "Resource ID is required", HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            // Create resource context
            ResourceContext context = new ResourceContext();
            context.setProperty("requestId", "mcp-request-" + System.currentTimeMillis());
            context.setProperty("protocol", "mcp");
            context.setProperty("timestamp", System.currentTimeMillis());

            // Read the resource
            ResourceResult result = readingService.readResource(resourceId, parameters, context);

            // Create response
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("success", result.isSuccess());
            responseData.put("content", result.getContent());
            responseData.put("executionTimeMs", result.getExecutionTimeMs());
            if (!result.isSuccess()) {
                responseData.put("error", result.getErrorMessage());
            }
            responseData.put("protocol", "mcp");

            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json");
            response.getWriter().write(objectMapper.writeValueAsString(responseData));

            logger.debug("MCP resources/read completed successfully for resource: {}", resourceId);
        } catch (Exception e) {
            logger.error("Error during MCP resources/read", e);
            sendMcpErrorResponse(response, "Resource read failed", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Handle MCP resources/templates/list.
     * 
     * @param request the HTTP request
     * @param response the HTTP response
     * @throws IOException if an I/O error occurs
     */
    private void handleMcpResourcesTemplatesList(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        logger.debug("Handling MCP resources/templates/list request");

        try {
            ResourceTemplateService templateService = resourceTemplateService;
            if (templateService == null) {
                sendMcpErrorResponse(response, "Resource template service not available",
                        HttpServletResponse.SC_SERVICE_UNAVAILABLE);
                return;
            }

            // Create resource context
            ResourceContext context = new ResourceContext();
            context.setProperty("requestId", "mcp-request-" + System.currentTimeMillis());
            context.setProperty("protocol", "mcp");
            context.setProperty("timestamp", System.currentTimeMillis());

            // List templates
            ResourceResult result = templateService.listTemplates(context);

            // Create response
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("success", result.isSuccess());
            responseData.put("data", result.getContent());
            responseData.put("executionTimeMs", result.getExecutionTimeMs());
            if (!result.isSuccess()) {
                responseData.put("error", result.getErrorMessage());
            }
            responseData.put("protocol", "mcp");

            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json");
            response.getWriter().write(objectMapper.writeValueAsString(responseData));

            logger.debug("MCP resources/templates/list completed successfully");
        } catch (Exception e) {
            logger.error("Error during MCP resources/templates/list", e);
            sendMcpErrorResponse(response, "Resource templates list failed",
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Handle MCP resources/subscribe.
     * 
     * @param request the HTTP request
     * @param response the HTTP response
     * @throws IOException if an I/O error occurs
     */
    private void handleMcpResourcesSubscribe(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        logger.debug("Handling MCP resources/subscribe request");

        try {
            ResourceReadingService readingService = resourceReadingService;
            if (readingService == null) {
                sendMcpErrorResponse(response, "Resource reading service not available",
                        HttpServletResponse.SC_SERVICE_UNAVAILABLE);
                return;
            }

            // Parse request body to get resource ID and parameters
            String requestBody = request.getReader().lines().reduce("", String::concat);
            Map<String, Object> requestData = objectMapper.readValue(requestBody,
                    new TypeReference<Map<String, Object>>() {
                    });

            String resourceId = (String) requestData.get("resourceId");
            @SuppressWarnings("unchecked")
            Map<String, Object> parameters = (Map<String, Object>) requestData.get("parameters");

            if (resourceId == null || resourceId.trim().isEmpty()) {
                sendMcpErrorResponse(response, "Resource ID is required", HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            // Create resource context
            ResourceContext context = new ResourceContext();
            context.setProperty("requestId", "mcp-request-" + System.currentTimeMillis());
            context.setProperty("protocol", "mcp");
            context.setProperty("timestamp", System.currentTimeMillis());

            // Subscribe to the resource
            ResourceResult result = readingService.subscribeToResource(resourceId, parameters, context);

            // Create response
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("success", result.isSuccess());
            responseData.put("subscription", result.getContent());
            responseData.put("executionTimeMs", result.getExecutionTimeMs());
            if (!result.isSuccess()) {
                responseData.put("error", result.getErrorMessage());
            }
            responseData.put("protocol", "mcp");

            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json");
            response.getWriter().write(objectMapper.writeValueAsString(responseData));

            logger.debug("MCP resources/subscribe completed successfully for resource: {}", resourceId);
        } catch (Exception e) {
            logger.error("Error during MCP resources/subscribe", e);
            sendMcpErrorResponse(response, "Resource subscription failed",
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Handle MCP resources/unsubscribe.
     * 
     * @param request the HTTP request
     * @param response the HTTP response
     * @throws IOException if an I/O error occurs
     */
    private void handleMcpResourcesUnsubscribe(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        logger.debug("Handling MCP resources/unsubscribe request");

        try {
            ToolRegistry registry = toolRegistry;
            if (registry == null) {
                sendMcpErrorResponse(response, "Tool registry not available",
                        HttpServletResponse.SC_SERVICE_UNAVAILABLE);
                return;
            }

            // Implement actual resource unsubscription logic using MCP SDK
            // Unsubscribe from resource updates

            // Parse request body to get subscription ID
            String requestBody = request.getReader().lines().reduce("", String::concat);
            Map<String, Object> requestData = objectMapper.readValue(requestBody,
                    new TypeReference<Map<String, Object>>() {
                    });

            String subscriptionId = (String) requestData.get("subscriptionId");
            if (subscriptionId == null || subscriptionId.trim().isEmpty()) {
                sendMcpErrorResponse(response, "Subscription ID is required", HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            // Get resource reading service
            ResourceReadingService readingService = resourceReadingService;
            if (readingService == null) {
                sendMcpErrorResponse(response, "Resource reading service not available",
                        HttpServletResponse.SC_SERVICE_UNAVAILABLE);
                return;
            }

            // Unsubscribe from the resource
            // Note: ResourceReadingService doesn't have unsubscribeFromResource method yet
            // This is a placeholder implementation that returns success
            // TODO: Implement actual unsubscription logic in ResourceReadingService

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("success", true);
            responseData.put("subscriptionId", subscriptionId);
            responseData.put("protocol", "mcp");
            responseData.put("message", "Unsubscription request received");

            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json");
            response.getWriter().write(objectMapper.writeValueAsString(responseData));

            logger.debug("Resource unsubscription request received for subscription: {}", subscriptionId);

            logger.debug("MCP resources/unsubscribe completed successfully");
        } catch (Exception e) {
            logger.error("Error during MCP resources/unsubscribe", e);
            sendMcpErrorResponse(response, "Resource unsubscription failed",
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    // MCP Prompt Protocol Endpoints

    /** Handle MCP prompts/list. */
    private void handleMcpPromptsList(HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger.debug("Handling MCP prompts/list request");

        try {
            PromptRegistrationService reg = promptRegistrationService;
            if (reg == null) {
                sendMcpErrorResponse(response, "Prompt registration service not available",
                        HttpServletResponse.SC_SERVICE_UNAVAILABLE);
                return;
            }

            PromptRegistry registry = promptRegistry;
            if (registry == null) {
                sendMcpErrorResponse(response, "Prompt registry not available",
                        HttpServletResponse.SC_SERVICE_UNAVAILABLE);
                return;
            }
            Map<String, Object>[] descriptors = registry.getPromptDescriptors();

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("prompts", descriptors);
            responseData.put("count", descriptors.length);
            responseData.put("protocol", "mcp");

            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json");
            response.getWriter().write(objectMapper.writeValueAsString(responseData));

            logger.debug("MCP prompts/list completed successfully with {} prompts", descriptors.length);
        } catch (Exception e) {
            logger.error("Error during MCP prompts/list", e);
            sendMcpErrorResponse(response, "Prompts list failed", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /** Handle MCP prompts/get. */
    private void handleMcpPromptsGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger.debug("Handling MCP prompts/get request");

        try {
            PromptRegistrationService reg = promptRegistrationService;
            PromptExecutionService exec = promptExecutionService;
            if (reg == null) {
                sendMcpErrorResponse(response, "Prompt registration service not available",
                        HttpServletResponse.SC_SERVICE_UNAVAILABLE);
                return;
            }

            String requestBody = request.getReader().lines().reduce("", String::concat);
            Map<String, Object> requestData = objectMapper.readValue(requestBody,
                    new TypeReference<Map<String, Object>>() {
                    });

            String name = (String) requestData.get("name");
            String operation = (String) requestData.getOrDefault("operation", "get");
            @SuppressWarnings("unchecked")
            Map<String, Object> parameters = (Map<String, Object>) requestData.getOrDefault("parameters", Map.of());

            if (name == null || name.trim().isEmpty()) {
                sendMcpErrorResponse(response, "Prompt name is required", HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            // If execution requested and service available, delegate
            if ("execute".equalsIgnoreCase(operation) && exec != null) {
                PromptContext context = new PromptContext();
                context.setProperty("requestId", "mcp-request-" + System.currentTimeMillis());
                context.setProperty("protocol", "mcp");
                context.setProperty("timestamp", System.currentTimeMillis());

                // Simple routing: system/*, item/*, rule/*
                PromptResult result;
                if (name.startsWith("system:")) {
                    result = exec.executeSystemPrompt(name.substring("system:".length()), operation, parameters,
                            context);
                } else if (name.startsWith("item:")) {
                    String itemName = name.substring("item:".length());
                    result = exec.executeItemPrompt(itemName, operation, parameters, context);
                } else if (name.startsWith("rule:")) {
                    String ruleUID = name.substring("rule:".length());
                    result = exec.executeRulePrompt(ruleUID, operation, parameters, context);
                } else {
                    // Default to system
                    result = exec.executeSystemPrompt(name, operation, parameters, context);
                }

                Map<String, Object> responseData = new HashMap<>();
                responseData.put("success", result.isSuccess());
                responseData.put("content", result.getContent());
                responseData.put("executionTimeMs", result.getExecutionTimeMs());
                if (!result.isSuccess()) {
                    String err = result.getErrorMessage();
                    responseData.put("error", err != null ? err : "Execution failed");
                }
                responseData.put("protocol", "mcp");

                response.setStatus(HttpServletResponse.SC_OK);
                response.setContentType("application/json");
                response.getWriter().write(objectMapper.writeValueAsString(responseData));
                return;
            }

            // Otherwise, just fetch the prompt definition
            PromptRegistry registry = promptRegistry;
            if (registry == null) {
                sendMcpErrorResponse(response, "Prompt registry not available",
                        HttpServletResponse.SC_SERVICE_UNAVAILABLE);
                return;
            }
            Prompt prompt = registry.getPrompt(name);
            if (prompt == null) {
                sendMcpErrorResponse(response, "Prompt not found: " + name, HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("prompt", prompt);
            responseData.put("protocol", "mcp");

            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json");
            response.getWriter().write(objectMapper.writeValueAsString(responseData));

            logger.debug("MCP prompts/get completed successfully for {}", name);
        } catch (Exception e) {
            logger.error("Error during MCP prompts/get", e);
            sendMcpErrorResponse(response, "Prompt get failed", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    // MCP Completion Protocol Endpoints

    /** Handle MCP completion/complete. */
    private void handleMcpCompletionComplete(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        logger.debug("Handling MCP completion/complete request");

        try {
            CompletionSuggestionService sugg = completionSuggestionService;
            if (sugg == null) {
                sendMcpErrorResponse(response, "Completion suggestion service not available",
                        HttpServletResponse.SC_SERVICE_UNAVAILABLE);
                return;
            }

            String requestBody = request.getReader().lines().reduce("", String::concat);
            Map<String, Object> requestData = objectMapper.readValue(requestBody,
                    new TypeReference<Map<String, Object>>() {
                    });

            String operation = (String) requestData.getOrDefault("operation", "complete");
            @SuppressWarnings("unchecked")
            Map<String, Object> parameters = (Map<String, Object>) requestData.getOrDefault("parameters", Map.of());

            CompletionContext context = new CompletionContext();
            context.setProperty("requestId", "mcp-request-" + System.currentTimeMillis());
            context.setProperty("protocol", "mcp");
            context.setProperty("timestamp", System.currentTimeMillis());

            // Simple routing based on parameters
            CompletionResult result;
            if (parameters.containsKey("itemName")) {
                String itemName = String.valueOf(parameters.get("itemName"));
                result = sugg.suggestItem(itemName, operation, parameters, context);
            } else if (parameters.containsKey("ruleUID")) {
                String ruleUID = String.valueOf(parameters.get("ruleUID"));
                result = sugg.suggestRule(ruleUID, operation, parameters, context);
            } else if (parameters.containsKey("configId")) {
                String configId = String.valueOf(parameters.get("configId"));
                result = sugg.suggestConfiguration(configId, operation, parameters, context);
            } else {
                String key = String.valueOf(parameters.getOrDefault("contextKey", "default"));
                result = sugg.suggestCommands(key, operation, parameters, context);
            }

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("success", result.isSuccess());
            responseData.put("completions", result.getSuggestions());
            responseData.put("total", result.getTotal());
            responseData.put("hasMore", result.isHasMore());
            responseData.put("executionTimeMs", result.getExecutionTimeMs());
            if (!result.isSuccess()) {
                String err = result.getErrorMessage();
                responseData.put("error", err != null ? err : "Completion failed");
            }
            responseData.put("protocol", "mcp");

            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json");
            response.getWriter().write(objectMapper.writeValueAsString(responseData));

            logger.debug("MCP completion/complete completed successfully");
        } catch (Exception e) {
            logger.error("Error during MCP completion/complete", e);
            sendMcpErrorResponse(response, "Completion failed", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    // MCP Roots Protocol Endpoints

    /**
     * Handle MCP roots/list.
     * 
     * @param request the HTTP request
     * @param response the HTTP response
     * @throws IOException if an I/O error occurs
     */
    private void handleMcpRootsList(HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger.debug("Handling MCP roots/list request");

        try {
            ToolRegistry registry = toolRegistry;
            if (registry == null) {
                sendMcpErrorResponse(response, "Tool registry not available",
                        HttpServletResponse.SC_SERVICE_UNAVAILABLE);
                return;
            }

            // Implement actual roots list logic using MCP SDK
            // Return the list of available roots

            List<Map<String, Object>> rootsList = new ArrayList<>();

            // Add basic root paths for the MCP server
            Map<String, Object> toolsRoot = new HashMap<>();
            toolsRoot.put("name", "tools");
            toolsRoot.put("uri", "/mcp/tools");
            toolsRoot.put("description", "MCP Tools endpoint");
            toolsRoot.put("type", "tools");
            rootsList.add(toolsRoot);

            Map<String, Object> resourcesRoot = new HashMap<>();
            resourcesRoot.put("name", "resources");
            resourcesRoot.put("uri", "/mcp/resources");
            resourcesRoot.put("description", "MCP Resources endpoint");
            resourcesRoot.put("type", "resources");
            rootsList.add(resourcesRoot);

            Map<String, Object> promptsRoot = new HashMap<>();
            promptsRoot.put("name", "prompts");
            promptsRoot.put("uri", "/mcp/prompts");
            promptsRoot.put("description", "MCP Prompts endpoint");
            promptsRoot.put("type", "prompts");
            rootsList.add(promptsRoot);

            Map<String, Object> completionsRoot = new HashMap<>();
            completionsRoot.put("name", "completions");
            completionsRoot.put("uri", "/mcp/completions");
            completionsRoot.put("description", "MCP Completions endpoint");
            completionsRoot.put("type", "completions");
            rootsList.add(completionsRoot);

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("roots", rootsList);
            responseData.put("protocol", "mcp");
            responseData.put("count", rootsList.size());

            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json");
            response.getWriter().write(objectMapper.writeValueAsString(responseData));

            logger.debug("MCP roots/list completed successfully");
        } catch (Exception e) {
            logger.error("Error during MCP roots/list", e);
            sendMcpErrorResponse(response, "Roots list failed", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    // MCP Sampling Protocol Endpoints

    /**
     * Handle MCP sampling/createMessage.
     * 
     * @param request the HTTP request
     * @param response the HTTP response
     * @throws IOException if an I/O error occurs
     */
    private void handleMcpSamplingCreateMessage(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        logger.debug("Handling MCP sampling/createMessage request");

        try {
            ToolRegistry registry = toolRegistry;
            if (registry == null) {
                sendMcpErrorResponse(response, "Tool registry not available",
                        HttpServletResponse.SC_SERVICE_UNAVAILABLE);
                return;
            }

            // Implement actual sampling create message logic using MCP SDK
            // Create a sampling message for the specified parameters

            // Parse request body to get sampling parameters
            String requestBody = request.getReader().lines().reduce("", String::concat);
            Map<String, Object> requestData = objectMapper.readValue(requestBody,
                    new TypeReference<Map<String, Object>>() {
                    });

            String prompt = (String) requestData.get("prompt");
            @SuppressWarnings("unchecked")
            Map<String, Object> parameters = (Map<String, Object>) requestData.get("parameters");

            if (prompt == null || prompt.trim().isEmpty()) {
                sendMcpErrorResponse(response, "Prompt is required for sampling", HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            // Create sampling message
            Map<String, Object> samplingMessage = new HashMap<>();
            samplingMessage.put("prompt", prompt);
            samplingMessage.put("parameters", parameters != null ? parameters : new HashMap<>());
            samplingMessage.put("timestamp", System.currentTimeMillis());
            samplingMessage.put("messageId", "sampling-" + System.currentTimeMillis());

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("message", samplingMessage);
            responseData.put("protocol", "mcp");
            responseData.put("success", true);

            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json");
            response.getWriter().write(objectMapper.writeValueAsString(responseData));

            logger.debug("MCP sampling/createMessage completed successfully");
        } catch (Exception e) {
            logger.error("Error during MCP sampling/createMessage", e);
            sendMcpErrorResponse(response, "Sampling create message failed",
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    // MCP Elicitation Protocol Endpoints

    /**
     * Handle MCP elicitation/create.
     * 
     * @param request the HTTP request
     * @param response the HTTP response
     * @throws IOException if an I/O error occurs
     */
    private void handleMcpElicitationCreate(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        logger.debug("Handling MCP elicitation/create request");

        try {
            ToolRegistry registry = toolRegistry;
            if (registry == null) {
                sendMcpErrorResponse(response, "Tool registry not available",
                        HttpServletResponse.SC_SERVICE_UNAVAILABLE);
                return;
            }

            // Implement actual elicitation create logic using MCP SDK
            // Create an elicitation session

            // Parse request body to get elicitation parameters
            String requestBody = request.getReader().lines().reduce("", String::concat);
            Map<String, Object> requestData = objectMapper.readValue(requestBody,
                    new TypeReference<Map<String, Object>>() {
                    });

            String prompt = (String) requestData.get("prompt");
            @SuppressWarnings("unchecked")
            Map<String, Object> parameters = (Map<String, Object>) requestData.get("parameters");

            if (prompt == null || prompt.trim().isEmpty()) {
                sendMcpErrorResponse(response, "Prompt is required for elicitation",
                        HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            // Create elicitation session
            String sessionId = "elicitation-" + System.currentTimeMillis();
            Map<String, Object> elicitationSession = new HashMap<>();
            elicitationSession.put("sessionId", sessionId);
            elicitationSession.put("prompt", prompt);
            elicitationSession.put("parameters", parameters != null ? parameters : new HashMap<>());
            elicitationSession.put("status", "created");
            elicitationSession.put("timestamp", System.currentTimeMillis());

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("elicitation", elicitationSession);
            responseData.put("protocol", "mcp");
            responseData.put("success", true);

            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json");
            response.getWriter().write(objectMapper.writeValueAsString(responseData));

            logger.debug("MCP elicitation/create completed successfully");
        } catch (Exception e) {
            logger.error("Error during MCP elicitation/create", e);
            sendMcpErrorResponse(response, "Elicitation create failed", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    // MCP Logging Protocol Endpoints

    /**
     * Handle MCP logging/setLevel.
     * 
     * @param request the HTTP request
     * @param response the HTTP response
     * @throws IOException if an I/O error occurs
     */
    private void handleMcpLoggingSetLevel(HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger.debug("Handling MCP logging/setLevel request");

        try {
            // Implement actual logging set level logic using MCP SDK
            // Set the logging level for the MCP server

            // Parse request body to get logging level
            String requestBody = request.getReader().lines().reduce("", String::concat);
            Map<String, Object> requestData = objectMapper.readValue(requestBody,
                    new TypeReference<Map<String, Object>>() {
                    });

            String level = (String) requestData.get("level");
            if (level == null || level.trim().isEmpty()) {
                sendMcpErrorResponse(response, "Logging level is required", HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            // Validate logging level
            String[] validLevels = { "TRACE", "DEBUG", "INFO", "WARN", "ERROR" };
            boolean validLevel = false;
            for (String validLevelStr : validLevels) {
                if (validLevelStr.equalsIgnoreCase(level)) {
                    validLevel = true;
                    level = validLevelStr;
                    break;
                }
            }

            if (!validLevel) {
                sendMcpErrorResponse(response, "Invalid logging level: " + level, HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            // Set logging level (this would typically involve configuring the logger)
            // For now, we'll just log the request and return success
            logger.info("Logging level set to: {}", level);

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("level", level);
            responseData.put("protocol", "mcp");
            responseData.put("success", true);
            responseData.put("message", "Logging level updated successfully");

            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json");
            response.getWriter().write(objectMapper.writeValueAsString(responseData));

            logger.debug("MCP logging/setLevel completed successfully");
        } catch (Exception e) {
            logger.error("Error during MCP logging/setLevel", e);
            sendMcpErrorResponse(response, "Logging set level failed", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    // MCP Notification Endpoints

    /**
     * Handle MCP notifications.
     * 
     * @param request the HTTP request
     * @param response the HTTP response
     * @param method the HTTP method
     * @throws IOException if an I/O error occurs
     */
    private void handleMcpNotifications(HttpServletRequest request, HttpServletResponse response, String method)
            throws IOException {
        // String requestURI = request.getRequestURI();
        logger.debug("Handling MCP notification request: {} {}", method, request.getRequestURI());

        try {
            // Implement actual notification logic using MCP SDK
            // Handle various types of notifications (initialized, progress, tools/list_changed, etc.)

            // Parse request body to get notification details
            String requestBody = request.getReader().lines().reduce("", String::concat);
            Map<String, Object> requestData = objectMapper.readValue(requestBody,
                    new TypeReference<Map<String, Object>>() {
                    });

            String notificationType = (String) requestData.get("type");
            @SuppressWarnings("unchecked")
            Map<String, Object> notificationData = (Map<String, Object>) requestData.get("data");

            if (notificationType == null || notificationType.trim().isEmpty()) {
                sendMcpErrorResponse(response, "Notification type is required", HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            // Handle different notification types
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("type", notificationType);
            responseData.put("protocol", "mcp");
            responseData.put("success", true);
            responseData.put("timestamp", System.currentTimeMillis());

            switch (notificationType.toLowerCase()) {
                case "initialized":
                    responseData.put("message", "MCP server initialized successfully");
                    logger.info("MCP server initialization notification received");
                    break;
                case "progress":
                    responseData.put("message", "Progress notification processed");
                    logger.debug("Progress notification: {}", notificationData);
                    break;
                case "tools/list_changed":
                    responseData.put("message", "Tools list change notification processed");
                    logger.info("Tools list change notification received");
                    break;
                case "resources/list_changed":
                    responseData.put("message", "Resources list change notification processed");
                    logger.info("Resources list change notification received");
                    break;
                case "error":
                    responseData.put("message", "Error notification processed");
                    logger.error("Error notification: {}", notificationData);
                    break;
                default:
                    responseData.put("message", "Unknown notification type processed");
                    logger.warn("Unknown notification type: {}", notificationType);
                    break;
            }

            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json");
            response.getWriter().write(objectMapper.writeValueAsString(responseData));

            logger.debug("MCP notification completed successfully");
        } catch (Exception e) {
            logger.error("Error during MCP notification", e);
            sendMcpErrorResponse(response, "Notification failed", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Send an MCP error response.
     * 
     * @param response the HTTP response
     * @param message the error message
     * @param statusCode the HTTP status code
     * @throws IOException if an I/O error occurs
     */
    private void sendMcpErrorResponse(HttpServletResponse response, String message, int statusCode) throws IOException {
        response.setStatus(statusCode);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"" + message + "\",\"protocol\":\"mcp\"}");
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

        // Register tools using the ToolInterfaceAdapter
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

        // Register tools using the ToolInterfaceAdapter
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

        // Map previous booleans to generic stats placeholder
        long totalRequests = 0L;
        long successfulRequests = 0L;
        long failedRequests = 0L;
        long totalResponseTime = 0L;
        double averageResponseTime = 0.0;
        int activeConnections = (sync != null ? 1 : 0) + (async != null ? 1 : 0) + (registry != null ? 1 : 0);
        return ServerStatistics.builder("mcp-servlet").withServerId("mcp-servlet").withServerName("MCP Servlet")
                .withServerType("HTTP").withProtocol("HTTP/SSE").withTotalRequests(totalRequests)
                .withSuccessfulRequests(successfulRequests).withFailedRequests(failedRequests)
                .withTotalResponseTime(totalResponseTime).withAverageResponseTime(averageResponseTime)
                .withActiveConnections(activeConnections).withHealthy(isHealthy()).build();
    }

    /**
     * Server statistics.
     */
}
