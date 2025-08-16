package org.openhab.core.ai.tool.security.filters;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.auth.AuthenticationContext;
import org.openhab.core.ai.auth.AuthenticationManager;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.whiteboard.propertytypes.HttpWhiteboardFilterName;
import org.osgi.service.http.whiteboard.propertytypes.HttpWhiteboardFilterPattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Protocol Security Filter for openHAB HTTP server integration.
 * 
 * This filter provides security validation for MCP and A2A protocol requests,
 * implementing authentication, authorization, rate limiting, and audit logging.
 * It integrates with openHAB's HTTP server using the OSGi HTTP Whiteboard pattern.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
@Component(service = Filter.class, immediate = true)
@HttpWhiteboardFilterName("protocol-security-filter")
@HttpWhiteboardFilterPattern({ "/mcp/*", "/a2a/*" })
public class ProtocolSecurityFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(ProtocolSecurityFilter.class);

    private final AtomicLong requestCount = new AtomicLong(0);
    private final AtomicLong blockedCount = new AtomicLong(0);
    private final AtomicLong lastResetTime = new AtomicLong(System.currentTimeMillis());

    // Rate limiting configuration
    private static final int RATE_LIMIT_PER_MINUTE = 1000;
    private static final long RATE_LIMIT_WINDOW_MS = 60000; // 1 minute

    private @Nullable AuthenticationManager authenticationManager;

    /**
     * Create a new protocol security filter instance.
     */
    public ProtocolSecurityFilter() {
        logger.debug("Protocol Security Filter created");
    }

    /**
     * Set the authentication manager reference.
     * 
     * @param authenticationManager the authentication manager
     */
    @Reference
    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
        logger.debug("Authentication manager set for protocol security filter");
    }

    /**
     * Unset the authentication manager reference.
     * 
     * @param authenticationManager the authentication manager
     */
    public void unsetAuthenticationManager(@Nullable AuthenticationManager authenticationManager) {
        this.authenticationManager = null;
        logger.debug("Authentication manager unset for protocol security filter");
    }

    /**
     * Initialize the filter.
     * 
     * @param filterConfig the filter configuration
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        logger.info("Protocol Security Filter initialized");
    }

    /**
     * Destroy the filter.
     */
    @Override
    public void destroy() {
        logger.info("Protocol Security Filter destroyed");
    }

    /**
     * Filter requests for security validation.
     * 
     * @param request the servlet request
     * @param response the servlet response
     * @param chain the filter chain
     * @throws IOException if an I/O error occurs
     * @throws ServletException if a servlet error occurs
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (!(request instanceof HttpServletRequest) || !(response instanceof HttpServletResponse)) {
            chain.doFilter(request, response);
            return;
        }

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String requestURI = httpRequest.getRequestURI();
        String method = httpRequest.getMethod();
        String remoteAddr = getClientIpAddress(httpRequest);

        logger.debug("Processing request: {} {} from {}", method, requestURI, remoteAddr);

        // Increment request count
        long currentRequestCount = requestCount.incrementAndGet();

        try {
            // Check rate limiting
            if (isRateLimitExceeded(currentRequestCount)) {
                logger.warn("Rate limit exceeded for request: {} {} from {}", method, requestURI, remoteAddr);
                blockedCount.incrementAndGet();
                sendRateLimitResponse(httpResponse);
                return;
            }

            // Extract and validate authentication
            Optional<AuthenticationContext> authContext = extractAndValidateAuthentication(httpRequest, requestURI);
            if (authContext.isEmpty()) {
                logger.warn("Authentication failed for request: {} {} from {}", method, requestURI, remoteAddr);
                blockedCount.incrementAndGet();
                sendAuthenticationErrorResponse(httpResponse, "Authentication required");
                return;
            }

            // Store authentication context in request attributes for servlet access
            httpRequest.setAttribute("authenticationContext", authContext.get());

            // Validate protocol-specific security
            if (!validateProtocolSecurity(httpRequest, requestURI, authContext.get())) {
                logger.warn("Protocol security validation failed for request: {} {} from {}", method, requestURI,
                        remoteAddr);
                blockedCount.incrementAndGet();
                sendSecurityErrorResponse(httpResponse, "Security validation failed");
                return;
            }

            // Log the request for audit
            logAuditEvent(httpRequest, "REQUEST_ALLOWED", authContext.get());

            // Continue with the filter chain
            chain.doFilter(request, response);

            // Log successful completion
            logAuditEvent(httpRequest, "REQUEST_COMPLETED", authContext.get());

        } catch (Exception e) {
            logger.error("Error processing request: {} {} from {}", method, requestURI, remoteAddr, e);
            logAuditEvent(httpRequest, "REQUEST_ERROR", null);
            throw e;
        }
    }

    /**
     * Extract and validate authentication from the request.
     * 
     * @param request the HTTP request
     * @param requestURI the request URI
     * @return authentication context if valid
     */
    private Optional<AuthenticationContext> extractAndValidateAuthentication(HttpServletRequest request,
            String requestURI) {
        AuthenticationManager authManager = authenticationManager;
        if (authManager == null) {
            logger.warn("Authentication manager not available");
            return Optional.empty();
        }

        // Determine protocol from URI
        String protocol = getProtocolFromUri(requestURI);
        if (protocol == null) {
            logger.warn("Unknown protocol for request: {}", requestURI);
            return Optional.empty();
        }

        // Extract credentials from headers
        Map<String, String> credentials = extractCredentials(request);
        if (credentials.isEmpty()) {
            logger.debug("No credentials found in request: {}", requestURI);
            return Optional.empty();
        }

        // Get client identifier
        String clientId = getClientIpAddress(request);

        // Authenticate using AuthenticationManager
        Optional<AuthenticationContext> context = authManager.authenticate(credentials, protocol, clientId);
        if (context.isPresent()) {
            logger.debug("Authentication successful for protocol: {}, client: {}", protocol, clientId);
            return context;
        }

        // Try JWT authentication if no other method succeeded
        String jwtToken = extractJwtToken(request);
        if (jwtToken != null) {
            context = authManager.authenticateWithJWT(jwtToken, protocol);
            if (context.isPresent()) {
                logger.debug("JWT authentication successful for protocol: {}, client: {}", protocol, clientId);
                return context;
            }
        }

        logger.debug("Authentication failed for protocol: {}, client: {}", protocol, clientId);
        return Optional.empty();
    }

    /**
     * Extract credentials from HTTP headers.
     * 
     * @param request the HTTP request
     * @return map of credentials
     */
    private Map<String, String> extractCredentials(HttpServletRequest request) {
        Map<String, String> credentials = new HashMap<>();

        // Extract Authorization header
        String authorization = request.getHeader("Authorization");
        if (authorization != null && !authorization.isEmpty()) {
            credentials.put("authorization", authorization);
        }

        // Extract API key header
        String apiKey = request.getHeader("X-API-Key");
        if (apiKey != null && !apiKey.isEmpty()) {
            credentials.put("api_key", apiKey);
        }

        // Extract other potential authentication headers
        String apiKeyAlt = request.getHeader("X-Api-Key");
        if (apiKeyAlt != null && !apiKeyAlt.isEmpty()) {
            credentials.put("api_key", apiKeyAlt);
        }

        return credentials;
    }

    /**
     * Extract JWT token from request.
     * 
     * @param request the HTTP request
     * @return JWT token if present
     */
    private @Nullable String extractJwtToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        return null;
    }

    /**
     * Get protocol from URI.
     * 
     * @param requestURI the request URI
     * @return protocol name or null if unknown
     */
    private @Nullable String getProtocolFromUri(String requestURI) {
        if (requestURI.startsWith("/mcp/")) {
            return "mcp";
        } else if (requestURI.startsWith("/a2a/")) {
            return "a2a";
        }
        return null;
    }

    /**
     * Check if rate limit is exceeded.
     * 
     * @param currentRequestCount the current request count
     * @return true if rate limit is exceeded
     */
    private boolean isRateLimitExceeded(long currentRequestCount) {
        long now = System.currentTimeMillis();
        long lastReset = lastResetTime.get();

        // Reset counter if window has passed
        if (now - lastReset > RATE_LIMIT_WINDOW_MS) {
            if (lastResetTime.compareAndSet(lastReset, now)) {
                requestCount.set(1);
                return false;
            }
        }

        return currentRequestCount > RATE_LIMIT_PER_MINUTE;
    }

    /**
     * Validate protocol-specific security requirements.
     * 
     * @param request the HTTP request
     * @param requestURI the request URI
     * @param authContext the authentication context
     * @return true if security validation passes
     */
    private boolean validateProtocolSecurity(HttpServletRequest request, String requestURI,
            AuthenticationContext authContext) {
        // MCP protocol security validation
        if (requestURI.startsWith("/mcp/")) {
            return validateMcpRequest(request, authContext);
        }

        // A2A protocol security validation
        if (requestURI.startsWith("/a2a/")) {
            return validateA2ARequest(request, authContext);
        }

        // Unknown protocol - deny by default
        logger.warn("Unknown protocol for request: {}", requestURI);
        return false;
    }

    /**
     * Validate MCP protocol request security.
     * 
     * @param request the HTTP request
     * @param authContext the authentication context
     * @return true if MCP security validation passes
     */
    private boolean validateMcpRequest(HttpServletRequest request, AuthenticationContext authContext) {
        String method = request.getMethod();
        String requestURI = request.getRequestURI();

        // Check MCP-specific permissions
        if (method.equals("GET")) {
            // GET requests for MCP tools, resources, prompts
            if (requestURI.contains("/tools") || requestURI.contains("/resources") || requestURI.contains("/prompts")) {
                return hasPermission(authContext, "mcp:read");
            }
        } else if (method.equals("POST")) {
            // POST requests for MCP tool execution
            if (requestURI.contains("/tools") || requestURI.contains("/call")) {
                return hasPermission(authContext, "mcp:execute");
            }
        }

        logger.debug("MCP security validation passed for request: {} with principal: {}", requestURI,
                authContext.getPrincipalId());
        return true;
    }

    /**
     * Validate A2A protocol request security.
     * 
     * @param request the HTTP request
     * @param authContext the authentication context
     * @return true if A2A security validation passes
     */
    private boolean validateA2ARequest(HttpServletRequest request, AuthenticationContext authContext) {
        String method = request.getMethod();
        String requestURI = request.getRequestURI();

        // Check A2A-specific permissions
        if (method.equals("GET")) {
            // GET requests for agent card, health, status
            if (requestURI.contains("/.well-known/agent.json") || requestURI.contains("/health")
                    || requestURI.contains("/status")) {
                return hasPermission(authContext, "a2a:read");
            }
        } else if (method.equals("POST")) {
            // POST requests for message sending, task operations
            if (requestURI.contains("/message/send")) {
                return hasPermission(authContext, "a2a:send");
            } else if (requestURI.contains("/task/")) {
                return hasPermission(authContext, "a2a:task");
            }
        }

        logger.debug("A2A security validation passed for request: {} with principal: {}", requestURI,
                authContext.getPrincipalId());
        return true;
    }

    /**
     * Check if the authentication context has a specific permission.
     * 
     * @param authContext the authentication context
     * @param permission the permission to check
     * @return true if permission is granted
     */
    private boolean hasPermission(AuthenticationContext authContext, String permission) {
        AuthenticationManager authManager = authenticationManager;
        if (authManager == null) {
            logger.warn("Authentication manager not available for permission check");
            return false;
        }

        String protocol = permission.split(":")[0];
        boolean hasPermission = authManager.hasPermission(authContext.getPrincipalId(), permission, protocol);

        if (!hasPermission) {
            logger.warn("Permission denied: {} for principal: {}", permission, authContext.getPrincipalId());
        }

        return hasPermission;
    }

    /**
     * Get the client IP address from the request.
     * 
     * @param request the HTTP request
     * @return the client IP address
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    /**
     * Send a rate limit exceeded response.
     * 
     * @param response the HTTP response
     * @throws IOException if an I/O error occurs
     */
    private void sendRateLimitResponse(HttpServletResponse response) throws IOException {
        response.setStatus(429); // HTTP 429 Too Many Requests
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"Rate limit exceeded\",\"retryAfter\":60}");
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
        response.getWriter().write("{\"error\":\"" + message + "\"}");
    }

    /**
     * Send a security error response.
     * 
     * @param response the HTTP response
     * @param message the error message
     * @throws IOException if an I/O error occurs
     */
    private void sendSecurityErrorResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"" + message + "\"}");
    }

    /**
     * Log an audit event.
     * 
     * @param request the HTTP request
     * @param event the audit event
     * @param authContext the authentication context (can be null)
     */
    private void logAuditEvent(HttpServletRequest request, String event, @Nullable AuthenticationContext authContext) {
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        String remoteAddr = getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");
        String principalId = authContext != null ? authContext.getPrincipalId() : "anonymous";

        logger.info("AUDIT: {} - {} {} from {} (User-Agent: {}, Principal: {})", event, method, requestURI, remoteAddr,
                userAgent, principalId);
    }

    /**
     * Activate the filter component.
     */
    @Activate
    public void activate() {
        logger.info("Protocol Security Filter activated - registering with openHAB HTTP server");
    }

    /**
     * Deactivate the filter component.
     */
    @Deactivate
    public void deactivate() {
        logger.info("Protocol Security Filter deactivated - unregistering from openHAB HTTP server");
    }

    /**
     * Get filter statistics.
     * 
     * @return filter statistics
     */
    public FilterStatistics getFilterStatistics() {
        return new FilterStatistics(requestCount.get(), blockedCount.get(),
                System.currentTimeMillis() - lastResetTime.get());
    }

    /**
     * Reset the filter statistics.
     */
    public void resetStatistics() {
        requestCount.set(0);
        blockedCount.set(0);
        lastResetTime.set(System.currentTimeMillis());
        logger.info("Protocol Security Filter statistics reset");
    }

    /**
     * Filter statistics.
     */
    // FilterStatistics extracted to org.openhab.core.ai.tool.security.filters.FilterStatistics
}
