package org.openhab.core.ai.tool.security.filters;

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.auth.AuthenticationContext;
import org.openhab.core.ai.auth.AuthenticationManager;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
import java.util.Map;
import java.util.Set;
import org.openhab.core.ai.common.monitoring.api.MetricKey;
import org.openhab.core.ai.common.monitoring.api.MetricKeys;
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

    private final long startTime = System.currentTimeMillis();

    // Rate limiting configuration
    private static final int RATE_LIMIT_PER_MINUTE = 1000;
    private static final long RATE_LIMIT_WINDOW_MS = 60000; // 1 minute

    // Rate limiting tracking
    private final Map<String, RateLimitTracker> rateLimitTrackers = new ConcurrentHashMap<>();

    private @Nullable AuthenticationManager authenticationManager;
    private @Nullable MetricsService metricsService;

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
     * Set the metrics service reference.
     * 
     * @param metricsService the metrics service
     */
    @Reference
    public void setMetricsService(MetricsService metricsService) {
        this.metricsService = metricsService;
        logger.debug("Metrics service set for protocol security filter");
    }

    /**
     * Unset the metrics service reference.
     * 
     * @param metricsService the metrics service
     */
    public void unsetMetricsService(@Nullable MetricsService metricsService) {
        this.metricsService = null;
        logger.debug("Metrics service unset for protocol security filter");
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
     * Filter the request.
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

        // Record request
        recordMetrics("protocol-security", "request-received", true, Duration.ZERO);

        // Check rate limiting
        if (isRateLimitExceeded(httpRequest)) {
            recordMetrics("protocol-security", "request-rate-limited", false, Duration.ZERO);
            sendRateLimitResponse(httpResponse);
            return;
        }

        // Authenticate request
        Optional<AuthenticationContext> authContext = authenticateRequest(httpRequest);
        if (authContext.isEmpty()) {
            recordMetrics("protocol-security", "request-authentication-failed", false, Duration.ZERO);
            sendAuthenticationErrorResponse(httpResponse, "Authentication required");
            return;
        }

        // Authorize request
        if (!isAuthorized(httpRequest, authContext.get())) {
            recordMetrics("protocol-security", "request-authorization-failed", false, Duration.ZERO);
            sendSecurityErrorResponse(httpResponse, "Access denied");
            return;
        }

        // Log audit event
        logAuditEvent(httpRequest, "REQUEST_ALLOWED", authContext.get());

        // Continue with the filter chain
        try {
            chain.doFilter(request, response);
            recordMetrics("protocol-security", "request-processed", true, Duration.ZERO);
        } catch (Exception e) {
            recordMetrics("protocol-security", "request-processing-error", false, Duration.ZERO);
            throw e;
        }
    }

    /**
     * Check if rate limit is exceeded.
     * 
     * @param request the HTTP request
     * @return true if rate limit is exceeded
     */
    private boolean isRateLimitExceeded(HttpServletRequest request) {
        String clientIp = getClientIpAddress(request);
        long currentTime = System.currentTimeMillis();

        // Get or create rate limit tracking for this client
        RateLimitTracker tracker = rateLimitTrackers.computeIfAbsent(clientIp,
                k -> new RateLimitTracker(RATE_LIMIT_PER_MINUTE, RATE_LIMIT_WINDOW_MS));

        // Check if rate limit is exceeded
        boolean exceeded = tracker.isRateLimitExceeded(currentTime);

        if (exceeded) {
            logger.warn("Rate limit exceeded for client IP: {} - {} requests in {} ms", clientIp,
                    tracker.getCurrentCount(), RATE_LIMIT_WINDOW_MS);
        }

        return exceeded;
    }

    /**
     * Authenticate the request.
     * 
     * @param request the HTTP request
     * @return authentication context if successful
     */
    private Optional<AuthenticationContext> authenticateRequest(HttpServletRequest request) {
        AuthenticationManager authManager = authenticationManager;
        if (authManager == null) {
            logger.warn("Authentication manager not available");
            return Optional.empty();
        }

        try {
            // Extract credentials from request
            Map<String, String> credentials = extractCredentials(request);
            String protocol = getProtocolFromUri(request.getRequestURI());
            String clientId = getClientIpAddress(request);

            if (protocol == null) {
                logger.warn("Unknown protocol for request: {}", request.getRequestURI());
                return Optional.empty();
            }

            // Try authentication with credentials
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
        } catch (Exception e) {
            logger.error("Authentication error: {}", e.getMessage(), e);
            return Optional.empty();
        }
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
     * Check if the request is authorized.
     * 
     * @param request the HTTP request
     * @param authContext the authentication context
     * @return true if authorized
     */
    private boolean isAuthorized(HttpServletRequest request, AuthenticationContext authContext) {
        String method = request.getMethod();
        String requestURI = request.getRequestURI();

        // MCP protocol authorization
        if (requestURI.startsWith("/mcp/")) {
            return validateMcpRequest(request, authContext);
        }

        // A2A protocol authorization
        if (requestURI.startsWith("/a2a/")) {
            return validateA2ARequest(request, authContext);
        }

        // Unknown protocol - deny by default
        logger.warn("Unknown protocol for request: {}", requestURI);
        return false;
    }

    /**
     * Validate MCP protocol request authorization.
     * 
     * @param request the HTTP request
     * @param authContext the authentication context
     * @return true if MCP authorization passes
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

        logger.debug("MCP authorization passed for request: {} with principal: {}", requestURI,
                authContext.getPrincipalId());
        return true;
    }

    /**
     * Validate A2A protocol request authorization.
     * 
     * @param request the HTTP request
     * @param authContext the authentication context
     * @return true if A2A authorization passes
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

        logger.debug("A2A authorization passed for request: {} with principal: {}", requestURI,
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
     * Get the client IP address.
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
        recordMetrics("protocol-security", "filter-activated", true, Duration.ZERO);
        logger.info("Protocol Security Filter activated - registering with openHAB HTTP server");
    }

    /**
     * Deactivate the filter component.
     */
    @Deactivate
    public void deactivate() {
        recordMetrics("protocol-security", "filter-deactivated", true, Duration.ZERO);
        logger.info("Protocol Security Filter deactivated - unregistering from openHAB HTTP server");
    }

    /**
     * Get filter statistics.
     * 
     * @return filter statistics
     */
    public FilterStatistics getFilterStatistics() {
        MetricsService metrics = metricsService;
        if (metrics == null) {
            logger.warn("MetricsService not available, returning empty statistics");
            return new FilterStatistics(0, 0, System.currentTimeMillis() - startTime);
        }

        MetricKey protocolSecurityKey = MetricKeys.custom("protocol-security", Map.of(), Set.of("counts", "latency"));
        var snapshot = metrics.getSnapshot(protocolSecurityKey, org.openhab.core.ai.common.monitoring.service.snapshot.GenericMetricsSnapshot.class);
        if (snapshot != null) {
            return new FilterStatistics(snapshot.getLong("total"), snapshot.getLong("failure"),
                    System.currentTimeMillis() - startTime);
        }
        return new FilterStatistics(0, 0, System.currentTimeMillis() - startTime);
    }

    /**
     * Reset the filter statistics.
     */
    public void resetStatistics() {
        recordMetrics("protocol-security", "statistics-reset", true, Duration.ZERO);
        logger.info("Protocol Security Filter statistics reset");
    }

    private void recordMetrics(String domain, String operation, boolean success, Duration duration) {
        MetricsService metrics = metricsService;
        if (metrics != null) {
            metrics.recordOperation(domain, operation, success, duration);
        } else {
            logger.warn("MetricsService not available, cannot record metrics for operation: {} - {}", domain,
                    operation);
        }
    }

    /**
     * Rate limit tracker for individual clients.
     */
    private static class RateLimitTracker {
        private final int maxRequests;
        private final long windowMs;
        private long windowStart;
        private int requestCount;

        public RateLimitTracker(int maxRequests, long windowMs) {
            this.maxRequests = maxRequests;
            this.windowMs = windowMs;
            this.windowStart = System.currentTimeMillis();
            this.requestCount = 0;
        }

        public synchronized boolean isRateLimitExceeded(long currentTime) {
            // Reset window if it has expired
            if (currentTime - windowStart > windowMs) {
                windowStart = currentTime;
                requestCount = 0;
            }

            // Increment request count
            requestCount++;

            // Check if rate limit is exceeded
            return requestCount > maxRequests;
        }

        public synchronized int getCurrentCount() {
            return requestCount;
        }
    }
}
